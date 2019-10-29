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
package eu.chorevolution.transformations.generativeapproach.adgenerator.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.commons.lang3.StringUtils;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.transformations.generativeapproach.adgenerator.ADGenerator;
import eu.chorevolution.transformations.generativeapproach.adgenerator.ADGeneratorException;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.Adapter;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.AdapterData;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.ADFileGeneratorUtility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.AdapterModelsUtility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.Utility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.WSDLUtility;

public class ADGeneratorImpl implements ADGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ADGeneratorImpl.class);

	private static final String GROUP_ID_BASE = "eu.chorevolution.ad";
	private static final String PACKAGE_NAME_BASE = "eu.chorevolution.ad";
	private static final String AD_TNS_BASE = "http://eu.chorevolution.ad/";

	private static final String RESOURCES_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "resources";
	private static final String WEB_INF_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "webapp"
			+ File.separatorChar + "WEB-INF";
	private static final String META_INF_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "webapp"
			+ File.separatorChar + "META-INF";

	private static final String POM_XML_FILE_NAME = "pom.xml";
	private static final String LOG4J_FILE_NAME = "log4j.properties";
	private static final String CONTEXT_XML_FILE_NAME = "context.xml";
	private static final String WEB_XML_FILE_NAME = "web.xml";
	private static final String ROOT_CONTEXT_XML_FILE_NAME = "root-context.xml";
	private static final String WEB_MVC_CONFIG_XML_FILE_NAME = "webmvc-config.xml";
	private static final String CXF_FILE_NAME = "cxf.xml";
	private static final String INTEGRATION_XML_FILE_NAME = "si-components.xml";
	private static final String REQUEST_ADAPTER_FILE_NAME = "RequestTranslator.java";
	private static final String RESPONSE_ADAPTER_FILE_NAME = "ResponseTranslator.java";
	private static final String REQUEST_DATA_FILE_NAME = "RequestData.java";
	private static final String DESTINATION_URI_PROVIDER_FILE_NAME = "OutboundDestinationURIProvider.java";
	private static final String TRANSFORMATION_UTILS_FILE_NAME = "TransformationUtils.java";
	private static final String TRANSFORMATION_RULE_HANDLER_FILE_NAME = "TransformationRuleHandler.java";
	private static final String BASE_SERVICE_WSDL_FILE_NAME = "BaseService.wsdl";
	private static final String CONFIGURABLE_SERVICE_IMPL_FILE_NAME = "ConfigurableServiceImpl.java";
	private static final String SET_INVOCATION_ADDRES_UTILS_FILE_NAME = "SetInvocationAddressUtils.java";
	private static final String ARTIFACT_ENDPOINT_DATA_FILE_NAME = "ArtifactEndpointData.java";

	private static final String WAR_SUFFIX = ".war";
	private final static String TAR_GZ_EXTENSION = ".tar.gz";

	@Override
	public Adapter generateAdapter(String adapterName, byte[] adapterModel, byte[] targetServiceWSDL,
			int adapterGenerationType) throws ADGeneratorException {

		AdapterData adapterData = new AdapterData();
		adapterData.setName(adapterName);
		adapterData.setRoleName(
				adapterName.startsWith("ad") ? StringUtils.capitalize(adapterName.substring(2)) : adapterName);
		adapterData.setArtifactId(adapterName);
		adapterData.setGroupId(GROUP_ID_BASE);
		adapterData.setPackagename(PACKAGE_NAME_BASE + "." + Utility.createArtifactName(adapterName));
		adapterData.setServicename(adapterName);
		adapterData.setWsdlname(adapterName);
		adapterData.setAdTargetNamespace(AD_TNS_BASE + adapterName);

		String tempFolderPath = Utility.createTemporaryFolderFromMillisAndGetPath();
		String adTempFolderPath = tempFolderPath + File.separatorChar + adapterData.getName();
		AdapterModel parsedAdapterModel = AdapterModelsUtility.loadAdapterModel(adapterModel);

		try {
			WSDLInfo parsedWsdl = WSDLUtility.readWSDLInfo(targetServiceWSDL);
			Definition definition = parsedWsdl.getDefinition();
			Service service = (Service) definition.getAllServices()
					.get(definition.getAllServices().keySet().toArray()[0]);
			String serviceLocationURI = ((SOAPAddress) service
					.getPort((String) service.getPorts().keySet().toArray()[0]).getExtensibilityElements().get(0))
							.getLocationURI();

			adapterData.setServiceLocationURI(serviceLocationURI);
			adapterData.setBcTargetNamespace(parsedWsdl.getTargetNS());
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ADGeneratorException("Exception into generateAdapter, see log file for details ");
		}

		generateAdapterInternal(adapterData, parsedAdapterModel, adTempFolderPath);

		try {
			if (adapterGenerationType == ADGenerator.ADAPTER_GENERATION_TYPE_WAR) {
				return generateWar(adapterName, adapterData, adTempFolderPath);
			} else {
				return generateSrc(adapterName, adapterData, tempFolderPath, adTempFolderPath);
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ADGeneratorException("Exception into generateAdapter, see log file for details ");

		} finally {
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				Utility.deleteFolder(tempFolderPath);
			}
		}

	}

	public void generateAdapterInternal(AdapterData adapterData, AdapterModel adapterModel, String adTempFolderPath)
			throws ADGeneratorException {

		String pomFilePath = adTempFolderPath + File.separatorChar + POM_XML_FILE_NAME;
		String adapterWSDLFilePath = adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "wsdl"
				+ File.separatorChar + adapterData.getWsdlname();
		String log4jFilePath = adTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ LOG4J_FILE_NAME;
		String contextXMLFilePath = adTempFolderPath + File.separatorChar + META_INF_PATH + File.separatorChar
				+ CONTEXT_XML_FILE_NAME;
		String webXMLFilePath = adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar
				+ WEB_XML_FILE_NAME;
		String webMvcXMLFilePath = adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "spring"
				+ File.separatorChar + WEB_MVC_CONFIG_XML_FILE_NAME;
		String rootContextXMLFilePath = adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar
				+ "spring" + File.separatorChar + ROOT_CONTEXT_XML_FILE_NAME;
		String cxfXMLFilePath = adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "spring"
				+ File.separatorChar + CXF_FILE_NAME;
		String integrationXMLFilePath = adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar
				+ "spring" + File.separatorChar + "integration" + File.separatorChar + INTEGRATION_XML_FILE_NAME;
		String baseServiceWSDLFilePath = adTempFolderPath + File.separatorChar + RESOURCES_PATH + File.separatorChar
				+ BASE_SERVICE_WSDL_FILE_NAME;

		Utility.createProjectFolders(adTempFolderPath);

		// Generate Project Files
		ADFileGeneratorUtility.generatePomXMLFile(adapterData, pomFilePath);
		ADFileGeneratorUtility.generateLog4jPropertiesFile(adapterData, log4jFilePath);
		ADFileGeneratorUtility.generateContextXMLFile(adapterData, contextXMLFilePath);
		ADFileGeneratorUtility.generateWebXMLFile(adapterData, webXMLFilePath);
		ADFileGeneratorUtility.generateWebMvcConfigXMLFile(adapterData, webMvcXMLFilePath);
		ADFileGeneratorUtility.generateRootContextXMLFile(adapterData, rootContextXMLFilePath);
		ADFileGeneratorUtility.generateCxfXMLFile(adapterData, cxfXMLFilePath);
		ADFileGeneratorUtility.generateIntegrationXMLFile(adapterData, integrationXMLFilePath);
		ADFileGeneratorUtility.generateAdapterWSDLFile(adapterData, adapterModel, adapterWSDLFilePath);
		ADFileGeneratorUtility.generateBaseServiceWSDLFile(adapterData, baseServiceWSDLFilePath);

		// Generate Adapter Java Classes
		String tranformerPackageName = adapterData.getPackagename() + ".integration";
		String transformerPackageFolder = Utility.createJavaPackageFolder(adTempFolderPath, tranformerPackageName);

		String integrationUtilsPackageName = adapterData.getPackagename() + ".integration.util";
		String integrationUtilsPackageFolder = Utility.createJavaPackageFolder(adTempFolderPath,
				integrationUtilsPackageName);

		String webservicesPackageName = adapterData.getPackagename() + ".webservices";
		String webservicesPackageFolder = Utility.createJavaPackageFolder(adTempFolderPath, webservicesPackageName);

		String businessUtilPackageName = adapterData.getPackagename() + ".business.util";
		String businessUtilPackageFolder = Utility.createJavaPackageFolder(adTempFolderPath, businessUtilPackageName);

		String businessModelPackageName = adapterData.getPackagename() + ".business.model";
		String businessModelPackageFolder = Utility.createJavaPackageFolder(adTempFolderPath, businessModelPackageName);

		String requestAdapterJavaFilePath = transformerPackageFolder + File.separatorChar + REQUEST_ADAPTER_FILE_NAME;
		String responseAdapterJavaFilePath = transformerPackageFolder + File.separatorChar + RESPONSE_ADAPTER_FILE_NAME;
		String requestDataJavaFilePath = transformerPackageFolder + File.separatorChar + REQUEST_DATA_FILE_NAME;
		String destinationURIProviderJavaFilePath = integrationUtilsPackageFolder + File.separatorChar
				+ DESTINATION_URI_PROVIDER_FILE_NAME;
		String transformationUtilsJavaFilePath = integrationUtilsPackageFolder + File.separatorChar
				+ TRANSFORMATION_UTILS_FILE_NAME;
		String transformationRuleHandlerJavaFilePath = integrationUtilsPackageFolder + File.separatorChar
				+ TRANSFORMATION_RULE_HANDLER_FILE_NAME;
		String configurableServiceImplJavaFilePath = webservicesPackageFolder + File.separatorChar
				+ CONFIGURABLE_SERVICE_IMPL_FILE_NAME;
		String setInvocationAddressUtilsJavaFilePath = businessUtilPackageFolder + File.separatorChar
				+ SET_INVOCATION_ADDRES_UTILS_FILE_NAME;
		String artifactEndpointDataJavaFilePath = businessModelPackageFolder + File.separatorChar
				+ ARTIFACT_ENDPOINT_DATA_FILE_NAME;

		ADFileGeneratorUtility.generateRequestTranslatorJavaFile(adapterData, adapterModel, requestAdapterJavaFilePath);
		ADFileGeneratorUtility.generateResponseTranslatorJavaFile(adapterData, adapterModel,
				responseAdapterJavaFilePath);
		ADFileGeneratorUtility.generateRequestDataJavaFile(adapterData, requestDataJavaFilePath);
		ADFileGeneratorUtility.generateDestinationURIProviderJavaFile(adapterData, destinationURIProviderJavaFilePath);
		ADFileGeneratorUtility.generateTransformationUtilsJavaFile(adapterData, transformationUtilsJavaFilePath);
		ADFileGeneratorUtility.generateTransformationRuleHandlerJavaFile(adapterData,
				transformationRuleHandlerJavaFilePath);
		ADFileGeneratorUtility.generateConfigurableServiceImplJavaFile(adapterData,
				configurableServiceImplJavaFilePath);
		ADFileGeneratorUtility.generateSetInvocationAddressUtilsJavaFile(adapterData,
				setInvocationAddressUtilsJavaFilePath);
		ADFileGeneratorUtility.generateArtifactEndpointDataJavaFile(adapterData, artifactEndpointDataJavaFilePath);
	}

	private Adapter generateWar(String adapterName, AdapterData adapterData, String adTempFolderPath)
			throws IOException {
		EmbeddedMaven.forProject(adTempFolderPath + File.separatorChar + "pom.xml").useMaven3Version("3.3.9")
				.setGoals("package").build();

		Adapter adapter = new Adapter();
		adapter.setName(adapterName);
		adapter.setArtifact(Files.readAllBytes(Paths.get(
				adTempFolderPath + File.separatorChar + "target" + File.separatorChar + adapterName + WAR_SUFFIX)));
		adapter.setWsdl(Files.readAllBytes(Paths.get(adTempFolderPath + File.separatorChar + WEB_INF_PATH
				+ File.separatorChar + "wsdl" + File.separatorChar + adapterData.getWsdlname())));

		return adapter;
	}

	private Adapter generateSrc(String adapterName, AdapterData adapterData, String tempFolderPath, String adTempFolderPath)
			throws IOException {

		Utility.createTarGzOfDirectory(adTempFolderPath, tempFolderPath, adapterData.getName());
		Adapter adapter = new Adapter();
		adapter.setName(adapterName);
		adapter.setArtifact(Files.readAllBytes(Paths.get(tempFolderPath + File.separatorChar + adapterData.getName() + TAR_GZ_EXTENSION)));
		adapter.setWsdl(Files.readAllBytes(Paths.get(adTempFolderPath + File.separatorChar + WEB_INF_PATH
				+ File.separatorChar + "wsdl" + File.separatorChar + adapterData.getWsdlname())));
		return adapter;
	}

}
