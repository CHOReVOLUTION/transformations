/*
* Copyright 2016 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl;

import java.io.File;
import java.io.IOException;

import javax.wsdl.WSDLException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.CDConsumerPartGenerator;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.CDConsumerPartGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.CDConsumerPartData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.GeneratingJavaData;

public class CDConsumerPartGeneratorImpl implements CDConsumerPartGenerator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CDConsumerPartGeneratorImpl.class);
	
	private static final String BINDING_XML_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar + "binding.xml";
	private static final String CONTEXT_XML_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "META-INF" + File.separatorChar + "context.xml";
	private static final String CXF_XML_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "spring" + File.separatorChar + "cxf.xml";
	private static final String GROUP_ID_BASE = "eu.chorevolution.prosumers";
	private static final String INDEX_JSP_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "index.jsp";
	private static final String LOG4J_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar + "log4j.properties";
	private static final String PACKAGE_NAME_BASE = "eu.chorevolution.prosumer";
	private static final String POM_XML_FILE_NAME = "pom.xml";
	private static final String WEB_XML_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "web.xml";
	private static final String WEB_MVC_CONFIG_XML_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "spring" + File.separatorChar + "webmvc-config.xml";
	private static final String ROOT_CONTEXT_XML_FILE_NAME = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "spring" + File.separatorChar + "root-context.xml";
	private static final String WSDL_EXTENSION = ".wsdl";



	@Override
	public byte[] generateCDConsumerPart(String consumerPartName, byte[] wsdl) throws CDConsumerPartGeneratorException {

		CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
		cdConsumerPartData.setName(consumerPartName);
		cdConsumerPartData.setArtifactId(consumerPartName);
		cdConsumerPartData.setGroupId(GROUP_ID_BASE);
		cdConsumerPartData.setPackagename(PACKAGE_NAME_BASE + "." + Utility.createArtifactName(consumerPartName));
		cdConsumerPartData.setWsdlname(consumerPartName);

		String tempFolderPath = Utility.createTemporaryFolderFromMillisAndGetPath();
		String cdTempFolderPath = tempFolderPath + File.separatorChar + cdConsumerPartData.getName();
		
		generateCDConsumerPartInternal(cdConsumerPartData, wsdl, cdTempFolderPath);

		byte[] consumerArtifact = Utility.createTarGzOfDirectory(cdTempFolderPath, tempFolderPath, cdConsumerPartData.getName());
		Utility.deleteFolder(tempFolderPath);

		return consumerArtifact;
	}
	
	public void generateCDConsumerPartInternal(CDConsumerPartData cdConsumerPartData, byte[] wsdl, String cdTempFolderPath) throws CDConsumerPartGeneratorException {

		
		String bindingXMLFilePath = cdTempFolderPath + File.separatorChar + BINDING_XML_FILE_NAME;
		String contextXMLFilePath = cdTempFolderPath + File.separatorChar + CONTEXT_XML_FILE_NAME;
		String cxfFilePath = cdTempFolderPath + File.separator + CXF_XML_FILE_NAME;
		String indexFilePath = cdTempFolderPath + File.separatorChar + INDEX_JSP_FILE_NAME;
		String log4jFilePath = cdTempFolderPath + File.separatorChar + LOG4J_FILE_NAME;
		String pomFilePath = cdTempFolderPath + File.separatorChar + POM_XML_FILE_NAME;
		String webMvcXMLFilePath = cdTempFolderPath + File.separatorChar + WEB_MVC_CONFIG_XML_FILE_NAME;
		String rootContextXMLFilePath = cdTempFolderPath + File.separatorChar + ROOT_CONTEXT_XML_FILE_NAME;
		String webXMLFilePath = cdTempFolderPath + File.separatorChar + WEB_XML_FILE_NAME;
		String wsdlFilePath = cdTempFolderPath + File.separator + "src" + File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar + cdConsumerPartData.getName() + WSDL_EXTENSION;

		Utility.createProjectFolders(cdTempFolderPath);
		

		try {
			FileUtils.writeByteArrayToFile(new File(wsdlFilePath), wsdl);
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl);
			CDConsumerPartGeneratorUtility.generateBindingXMLFile(cdConsumerPartData, bindingXMLFilePath);
			CDConsumerPartGeneratorUtility.generateContextXMLFile(cdConsumerPartData, contextXMLFilePath);
			CDConsumerPartGeneratorUtility.generateCxfXMLFile(cdConsumerPartData, wsdlInfo, cxfFilePath);
			CDConsumerPartGeneratorUtility.generateIndexJspFile(cdConsumerPartData, indexFilePath);
			CDConsumerPartGeneratorUtility.generateLog4jPropertiesFile(cdConsumerPartData, log4jFilePath);
			CDConsumerPartGeneratorUtility.generatePomXMLFile(cdConsumerPartData, pomFilePath);
			CDConsumerPartGeneratorUtility.generateWebMvcConfigXMLFile(cdConsumerPartData, webMvcXMLFilePath);
			CDConsumerPartGeneratorUtility.generateRootContextXMLFile(cdConsumerPartData, rootContextXMLFilePath);
			CDConsumerPartGeneratorUtility.generateWebXMLFile(cdConsumerPartData, webXMLFilePath);
			GeneratingJavaData generatingJavaData = Utility.createGeneratingJavaData(cdConsumerPartData, wsdlInfo);
			
			//Start the java code copying and generation
			String modelPackageName = cdConsumerPartData.getPackagename() + ".model";
			String modelPackagePath = Utility.createJavaPackageFolder(cdTempFolderPath, modelPackageName);
			CDConsumerPartGeneratorUtility.generateMessagesData(modelPackagePath, generatingJavaData);
			CDConsumerPartGeneratorUtility.generateChoreographyLoopIndexes(modelPackagePath, generatingJavaData);
			
			//Generate the Web Service Implementation class
			String wsPackageName = cdConsumerPartData.getPackagename() + ".webservices";
			String wsImplPath = Utility.createJavaPackageFolder(cdTempFolderPath, wsPackageName);
			CDConsumerPartGeneratorUtility.generateApacheCXFWSImpl(wsImplPath, generatingJavaData);
			
			//Generate the Business Service Interface
			String businessPackageName = cdConsumerPartData.getPackagename() + ".business";
			String businessPath = Utility.createJavaPackageFolder(cdTempFolderPath, businessPackageName);
			CDConsumerPartGeneratorUtility.generateBusinessServiceInterface(businessPath, generatingJavaData);

			CDConsumerPartGeneratorUtility.generateChoreographyDataService(businessPath, generatingJavaData);
			CDConsumerPartGeneratorUtility.generateChoreographyInstanceMessages(businessPath, generatingJavaData);
			CDConsumerPartGeneratorUtility.generateChoreographyInstanceMessagesStore(businessPath, generatingJavaData);
			
			//Generate the Spring Business Data Service Implementation
			String businessDataServicePackageName = cdConsumerPartData.getPackagename() + ".business.impl.dataservice";
			String businessDataServicePath = Utility.createJavaPackageFolder(cdTempFolderPath, businessDataServicePackageName);
			CDConsumerPartGeneratorUtility.generateSpringChoreographiesDataServiceImpl(businessDataServicePath, generatingJavaData);
			CDConsumerPartGeneratorUtility.generateSpringChoreographyInstanceMessagesStoreImpl(businessDataServicePath, generatingJavaData);
			
			
			//Generate the Spring Business Service Interface Implementation
			String businessImplPackageName = cdConsumerPartData.getPackagename() + ".business.impl.service";
			String businessImplPath = Utility.createJavaPackageFolder(cdTempFolderPath, businessImplPackageName);
			CDConsumerPartGeneratorUtility.generateSpringServiceImpl(businessImplPath, generatingJavaData);
			
			
		} catch (WSDLException | IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateCDConsumerPart see log file for details ");
		}

	}

}
