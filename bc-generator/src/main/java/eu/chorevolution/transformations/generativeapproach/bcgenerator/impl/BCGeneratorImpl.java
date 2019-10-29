/*
* Copyright 2017 The CHOReVOLUTION project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.chorevolution.transformations.generativeapproach.bcgenerator.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.wsdl.WSDLException;

import org.apache.commons.lang3.StringUtils;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.BCGenerator;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.BCGeneratorException;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.BCProtocolType;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BC;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BCData;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.BCFileGeneratorUtility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.GidlModelsUtility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.Utility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.WSDLUtility;

public class BCGeneratorImpl implements BCGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCGeneratorImpl.class);

	private static final String GROUP_ID_BASE = "eu.chorevolution.bc";
	private static final String PACKAGE_NAME_BASE = "eu.chorevolution.bc";
	private static final String TNS_BASE = "http://eu.chorevolution.bc/";

	private static final String RESOURCES_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "resources";
	private static final String WEB_INF_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "webapp"
			+ File.separatorChar + "WEB-INF";
	private static final String META_INF_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "webapp"
			+ File.separatorChar + "META-INF";
	private static final String WAR_SUFFIX = ".war";
	private final static String TAR_GZ_EXTENSION = ".tar.gz";

	private static final String POM_XML_FILE_NAME = "pom.xml";
	private static final String LOG4J_FILE_NAME = "log4j.properties";
	private static final String CONTEXT_XML_FILE_NAME = "context.xml";
	private static final String WEB_XML_FILE_NAME = "web.xml";
	private static final String ROOT_CONTEXT_XML_FILE_NAME = "root-context.xml";
	private static final String WEB_MVC_CONFIG_XML_FILE_NAME = "webmvc-config.xml";
	private static final String CXF_FILE_NAME = "cxf.xml";
	private static final String BASE_SERVICE_WSDL_FILE_NAME = "BaseService.wsdl";
	private static final String BINDING_XML_FILE_NAME = "binding.xml";
	private static final String BUSINESSEXCEPTION_JAVA_FILE_NAME = "BusinessException.java";
	private static final String CONFIGURABLE_SERVICE_IMPL_FILE_NAME = "ConfigurableServiceImpl.java";
	private static final String SET_INVOCATION_ADDRES_UTILS_FILE_NAME = "SetInvocationAddressUtils.java";
	private static final String ARTIFACT_ENDPOINT_DATA_FILE_NAME = "ArtifactEndpointData.java";

	@Override
	public BC generateBC(String bcName, byte[] interfaceModel, BCProtocolType protocol, int bcGenerationType)
			throws BCGeneratorException {

		String artifactName = bcName;
		if (protocol.equals(BCProtocolType.REST)) {
			bcName = bcName.startsWith("bc") ? bcName.substring(2) : bcName;
		}

		BCData bcData = new BCData();
		bcData.setName(Utility.createName(bcName));
		bcData.setRoleName(bcName.startsWith("bc") ? StringUtils.capitalize(bcName.substring(2)) : bcName);
		bcData.setArtifactId(artifactName);
		bcData.setGroupId(GROUP_ID_BASE);
		bcData.setPackagename(PACKAGE_NAME_BASE + "." + Utility.createArtifactName(bcName));
		bcData.setServicename(Utility.createName(bcName));
		bcData.setWsdlname(Utility.createName(bcName));
		bcData.setTargetNamespace(TNS_BASE + Utility.createName(bcName));

		String tempFolderPath = Utility.createTemporaryFolderFromMillisAndGetPath();
		String bcTempFolderPath = tempFolderPath + File.separatorChar + bcData.getName();

		if (protocol.equals(BCProtocolType.REST)) {
			try {
				WSDLInfo parsedWsdlModel = WSDLUtility.readWSDLInfo(interfaceModel, bcData.getRoleName());
				generateRest(bcData, parsedWsdlModel, interfaceModel, bcTempFolderPath);
			} catch (WSDLException e) {
				e.printStackTrace();
			}
		} else {
			GIDLModel parsedGidlModel = GidlModelsUtility.loadGIDLModel(interfaceModel);
			generateSoap(bcData, parsedGidlModel, bcTempFolderPath);
		}

		try {
			if (bcGenerationType == BCGenerator.BC_GENERATION_TYPE_WAR) {
				return generateWar(bcName, bcData, bcTempFolderPath, protocol);
			} else {
				return generateSrc(bcName, bcData, tempFolderPath, bcTempFolderPath, protocol);
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new BCGeneratorException("Exception into generateBC, see log file for details ");

		} finally {
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				Utility.deleteFolder(tempFolderPath);
			}
		}

	}

	public void generateSoap(BCData bcData, GIDLModel gidlModel, String bcTempFolderPath) throws BCGeneratorException {

		String pomFilePath = bcTempFolderPath + File.separatorChar + POM_XML_FILE_NAME;
		String wsdlFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ bcData.getWsdlname();
		String bindingFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ BINDING_XML_FILE_NAME;
		String log4jFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ LOG4J_FILE_NAME;
		String contextXMLFilePath = bcTempFolderPath + File.separatorChar + META_INF_PATH + File.separatorChar
				+ CONTEXT_XML_FILE_NAME;
		String webXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar
				+ WEB_XML_FILE_NAME;
		String webMvcXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "spring"
				+ File.separatorChar + WEB_MVC_CONFIG_XML_FILE_NAME;
		String rootContextXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar
				+ "spring" + File.separatorChar + ROOT_CONTEXT_XML_FILE_NAME;
		String cxfXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "spring"
				+ File.separatorChar + CXF_FILE_NAME;
		String baseServiceWSDLFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ BASE_SERVICE_WSDL_FILE_NAME;

		Utility.createProjectFolders(bcTempFolderPath);

		// Generate Project Files
		BCFileGeneratorUtility.generatePomXMLFile(bcData, pomFilePath);
		BCFileGeneratorUtility.generateLog4jPropertiesFile(bcData, log4jFilePath);
		BCFileGeneratorUtility.generateBindingXMLFile(bcData, bindingFilePath);
		BCFileGeneratorUtility.generateContextXMLFile(bcData, contextXMLFilePath);
		BCFileGeneratorUtility.generateWebXMLFile(bcData, webXMLFilePath);
		BCFileGeneratorUtility.generateWebMvcConfigXMLFile(bcData, webMvcXMLFilePath);
		BCFileGeneratorUtility.generateRootContextXMLFile(bcData, rootContextXMLFilePath);
		BCFileGeneratorUtility.generateCxfXMLFile(bcData, cxfXMLFilePath);
		BCFileGeneratorUtility.generateWSDLFile(bcData, gidlModel, wsdlFilePath);
		BCFileGeneratorUtility.generateBaseServiceWSDLFile(bcData, baseServiceWSDLFilePath);

		// Generate Binding Component Java Classes
		String webservicesPackageName = bcData.getPackagename() + ".webservices";
		String webservicesPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, webservicesPackageName);

		String businessPackageName = bcData.getPackagename() + ".business";
		String businessPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, businessPackageName);

		String businessImplPackageName = bcData.getPackagename() + ".business.impl";
		String businessImplPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, businessImplPackageName);

		String businessModelPackageName = bcData.getPackagename() + ".business.model";
		String businessModelPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, businessModelPackageName);

		String utilPackageName = bcData.getPackagename() + ".util";
		String utilPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, utilPackageName);

		String configurableServiceImplJavaFilePath = webservicesPackageFolder + File.separatorChar
				+ CONFIGURABLE_SERVICE_IMPL_FILE_NAME;
		String businessExceptionJavaFilePath = businessPackageFolder + File.separatorChar
				+ BUSINESSEXCEPTION_JAVA_FILE_NAME;
		String serviceJavaFilePath = businessPackageFolder + File.separatorChar
				+ StringUtils.capitalize(bcData.getServicename()) + "Service.java";
		String ptImplJavaFilePath = webservicesPackageFolder + File.separatorChar
				+ StringUtils.capitalize(bcData.getServicename()) + "PTImpl.java";
		String serviceImplJavaFilePath = businessImplPackageFolder + File.separatorChar
				+ StringUtils.capitalize(bcData.getServicename()) + "ServiceImpl.java";
		String setInvocationAddressUtilsJavaFilePath = utilPackageFolder + File.separatorChar
				+ SET_INVOCATION_ADDRES_UTILS_FILE_NAME;
		String artifactEndpointDataJavaFilePath = businessModelPackageFolder + File.separatorChar
				+ ARTIFACT_ENDPOINT_DATA_FILE_NAME;

		BCFileGeneratorUtility.generateConfigurableServiceImplJavaFile(bcData, configurableServiceImplJavaFilePath);
		BCFileGeneratorUtility.generateBusinessExceptionJavaFile(bcData, businessExceptionJavaFilePath);
		BCFileGeneratorUtility.generateServiceJavaFile(bcData, gidlModel, serviceJavaFilePath);
		BCFileGeneratorUtility.generatePTImplJavaFile(bcData, gidlModel, ptImplJavaFilePath);
		BCFileGeneratorUtility.generateServiceImplJavaFile(bcData, gidlModel, serviceImplJavaFilePath);
		BCFileGeneratorUtility.generateSetInvocationAddressUtilsJavaFile(bcData, setInvocationAddressUtilsJavaFilePath);
		BCFileGeneratorUtility.generateArtifactEndpointDataJavaFile(bcData, artifactEndpointDataJavaFilePath);

		if (bcData.getName().equals("bcPersonalWeatherStations")) {
			String securityHelperJavaFilePath = businessImplPackageFolder + File.separatorChar + "SecurityHelper.java";
			BCFileGeneratorUtility.generateSecurityHelperJavaFile(bcData, securityHelperJavaFilePath);
		}

	}

	public void generateRest(BCData bcData, WSDLInfo wsdlModel, byte[] wsdl, String bcTempFolderPath)
			throws BCGeneratorException {
		String pomFilePath = bcTempFolderPath + File.separatorChar + POM_XML_FILE_NAME;
		String wsdlFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar + "cd"
				+ bcData.getServicename() + ".wsdl";
		String bindingFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ BINDING_XML_FILE_NAME;
		String log4jFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ LOG4J_FILE_NAME;
		String contextXMLFilePath = bcTempFolderPath + File.separatorChar + META_INF_PATH + File.separatorChar
				+ CONTEXT_XML_FILE_NAME;
		String webXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar
				+ WEB_XML_FILE_NAME;
		String webMvcXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "spring"
				+ File.separatorChar + WEB_MVC_CONFIG_XML_FILE_NAME;
		String cxfXMLFilePath = bcTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "spring"
				+ File.separatorChar + CXF_FILE_NAME;
		String baseServiceWSDLFilePath = bcTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ BASE_SERVICE_WSDL_FILE_NAME;

		Utility.createProjectFolders(bcTempFolderPath);

		// Generate Project Files
		BCFileGeneratorUtility.generateRESTPomXMLFile(bcData, pomFilePath);
		BCFileGeneratorUtility.generateLog4jPropertiesFile(bcData, log4jFilePath);
		BCFileGeneratorUtility.generateBindingXMLFile(bcData, bindingFilePath);
		BCFileGeneratorUtility.generateContextXMLFile(bcData, contextXMLFilePath);
		BCFileGeneratorUtility.generateRESTWebXMLFile(bcData, webXMLFilePath);
		BCFileGeneratorUtility.generateWebMvcConfigXMLFile(bcData, webMvcXMLFilePath);
		BCFileGeneratorUtility.generateRESTCxfXMLFile(bcData, cxfXMLFilePath);
		BCFileGeneratorUtility.generateBaseServiceWSDLFile(bcData, baseServiceWSDLFilePath);

		// Save CD WSDL
		try {
			Files.write(Paths.get(wsdlFilePath), wsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Generate Binding Component Java Classes
		String basePackageName = bcData.getPackagename();
		String basePackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, basePackageName);

		String webservicesPackageName = bcData.getPackagename() + ".webservices";
		String webservicesPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, webservicesPackageName);

		String businessPackageName = bcData.getPackagename() + ".business";
		String businessPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, businessPackageName);

		String businessModelPackageName = bcData.getPackagename() + ".business.model";
		String businessModelPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, businessModelPackageName);

		String utilPackageName = bcData.getPackagename() + ".util";
		String utilPackageFolder = Utility.createJavaPackageFolder(bcTempFolderPath, utilPackageName);

		String configurableServiceImplJavaFilePath = webservicesPackageFolder + File.separatorChar
				+ CONFIGURABLE_SERVICE_IMPL_FILE_NAME;
		String businessExceptionJavaFilePath = businessPackageFolder + File.separatorChar
				+ BUSINESSEXCEPTION_JAVA_FILE_NAME;
		String setInvocationAddressUtilsJavaFilePath = utilPackageFolder + File.separatorChar
				+ SET_INVOCATION_ADDRES_UTILS_FILE_NAME;
		String artifactEndpointDataJavaFilePath = businessModelPackageFolder + File.separatorChar
				+ ARTIFACT_ENDPOINT_DATA_FILE_NAME;
		String restJavaFilePath = basePackageFolder + File.separatorChar + bcData.getServicename() + "REST.java";

		BCFileGeneratorUtility.generateConfigurableServiceImplJavaFile(bcData, configurableServiceImplJavaFilePath);
		BCFileGeneratorUtility.generateBusinessExceptionJavaFile(bcData, businessExceptionJavaFilePath);
		BCFileGeneratorUtility.generateSetInvocationAddressUtilsJavaFile(bcData, setInvocationAddressUtilsJavaFilePath);
		BCFileGeneratorUtility.generateArtifactEndpointDataJavaFile(bcData, artifactEndpointDataJavaFilePath);
		BCFileGeneratorUtility.generateRESTJavaFile(bcData, wsdlModel, restJavaFilePath);
	}

	private BC generateWar(String bcName, BCData bcData, String bcTempFolderPath, BCProtocolType protocol)
			throws IOException {
		EmbeddedMaven.forProject(bcTempFolderPath + File.separatorChar + "pom.xml").useMaven3Version("3.3.9")
				.setGoals("package").build();

		BC bc = new BC();
		bc.setName(bcName);
		bc.setArtifact(Files.readAllBytes(Paths.get(bcTempFolderPath + File.separatorChar + "target"
				+ File.separatorChar + bcData.getArtifactId() + WAR_SUFFIX)));
		if (protocol.equals(BCProtocolType.SOAP)) {
			bc.setWsdl(Files.readAllBytes(Paths.get(bcTempFolderPath + File.separatorChar + RESOURCES_PATH
					+ File.separatorChar + bcData.getWsdlname())));
		}

		return bc;
	}

	private BC generateSrc(String bcName, BCData bcData, String tempFolderPath, String bcTempFolderPath,
			BCProtocolType protocol) throws IOException {
		
		Utility.createTarGzOfDirectory(bcTempFolderPath, tempFolderPath, bcData.getName());
		BC bc = new BC();
		bc.setName(bcName);
		bc.setArtifact(Files.readAllBytes(Paths.get(tempFolderPath + File.separatorChar + bcData.getName() + TAR_GZ_EXTENSION)));
		if (protocol.equals(BCProtocolType.SOAP)) {
			bc.setWsdl(Files.readAllBytes(Paths.get(bcTempFolderPath + File.separatorChar + RESOURCES_PATH
					+ File.separatorChar + bcData.getWsdlname())));
		}

		return bc;
	}

};