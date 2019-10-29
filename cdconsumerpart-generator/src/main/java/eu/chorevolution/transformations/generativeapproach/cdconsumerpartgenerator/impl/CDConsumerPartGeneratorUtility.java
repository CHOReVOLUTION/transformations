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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.CDConsumerPartGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.CDConsumerPartData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.GeneratingJavaData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.WSDLInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class CDConsumerPartGeneratorUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDConsumerPartGeneratorUtility.class);

	private static final String TEMPLATE_BINDING_XML_NAME = "binding-xml.ftlh";
	private static final String PLACEHOLDER_CONSUMER_NAME_POM = "consumerpart";
	private static final String PLACEHOLDER_WSDLINFO_NAME_POM = "wsdlinfo";
	private static final String PLACEHOLDER_GENERATING_JAVA_DATA_NAME = "generatingJavaData";
	private static final String TEMPLATE_CXF_XML_NAME = "cxf-xml.ftlh";
	private static final String TEMPLATE_CONTEXT_XML_NAME = "context-xml.ftlh";
	private static final String TEMPLATE_FOLDER_PATH = "/templates";
	private static final String TEMPLATE_INDEX_JSP_NAME = "index-jsp.ftlh";
	private static final String TEMPLATE_LOG4J_PROPERTIES_NAME = "log4j-properties.ftlh";
	private static final String TEMPLATE_POM_XML_NAME = "pom-xml.ftlh";
	private static final String TEMPLATE_WEB_XML_NAME = "web-xml.ftlh";
	private static final String TEMPLATE_WEBMVC_CONFIG_XML_NAME = "webmvc-config-xml.ftlh";
	private static final String TEMPLATE_ROOT_CONTEXT_XML_NAME = "root-context-xml.ftlh";
	private static final String TEMPLATE_WEB_SERVICE_IMPL_JAVA_NAME = "webservice-impl-java.ftlh";
	private static final String TEMPLATE_SPRING_SERVICE_IMPL_NAME = "spring-service-impl-java.ftlh";
	private static final String TEMPLATE_MESSAGES_DATA_NAME = "messages-data-java.ftlh";
	private static final String TEMPLATE_CHOREOGRAPHY_LOOP_INDEX_NAME = "choreographyloopindexes-java.ftlh";
	
	private static final String TEMPLATE_CHOREOGRAPHY_DATA_SERVICE_NAME = "choreography-data-service-java.ftlh";
	private static final String TEMPLATE_CHOREOGRAPHY_INSTANCE_MESSAGES_NAME = "choreography-instance-messages-java.ftlh";
	private static final String TEMPLATE_CHOREOGRAPHY_INSTANCE_MESSAGES_STORE_NAME = "choreography-instance-messages-store-java.ftlh";
	private static final String TEMPLATE_CHOREOGRAPHIES_DATA_SERVICE_IMPL_NAME = "choreographies-data-service-impl-java.ftlh";
	private static final String TEMPLATE_CHOREOGRAPHY_INSTANCE_MESSAGES_STORE_IMPL_NAME = "choreography-instance-messages-store-impl-java.ftlh";
	
	private static final String TEMPLATE_BUSINESS_SERVICE_INTERFACE_NAME = "business-service-interface-java.ftlh";
	private static final String UTF8_ENCODING = "UTF-8";

	private static final Configuration configuration;

	static {
		configuration = new Configuration(Configuration.VERSION_2_3_25);
		configuration.setClassForTemplateLoading(CDConsumerPartGeneratorUtility.class, TEMPLATE_FOLDER_PATH);
		configuration.setDefaultEncoding(UTF8_ENCODING);
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
	}

	public static void generatePomXMLFile(CDConsumerPartData cdConsumerPartData, String pomFilePath) {

		generateFile(cdConsumerPartData, pomFilePath, TEMPLATE_POM_XML_NAME);
	}

	public static void generateLog4jPropertiesFile(CDConsumerPartData cdConsumerPartData, String log4jPropertiesFilePath) {

		generateFile(cdConsumerPartData, log4jPropertiesFilePath, TEMPLATE_LOG4J_PROPERTIES_NAME);
	}

	public static void generateBindingXMLFile(CDConsumerPartData cdConsumerPartData, String bindingFilePath) {

		generateFile(cdConsumerPartData, bindingFilePath, TEMPLATE_BINDING_XML_NAME);
	}

	public static void generateIndexJspFile(CDConsumerPartData cdConsumerPartData, String indexFilePath) {

		generateFile(cdConsumerPartData, indexFilePath, TEMPLATE_INDEX_JSP_NAME);
	}

	public static void generateContextXMLFile(CDConsumerPartData cdConsumerPartData, String contextFilePath) {

		generateFile(cdConsumerPartData, contextFilePath, TEMPLATE_CONTEXT_XML_NAME);
	}

	public static void generateWebXMLFile(CDConsumerPartData cdConsumerPartData, String webXMLFilePath) {

		generateFile(cdConsumerPartData, webXMLFilePath, TEMPLATE_WEB_XML_NAME);
	}

	public static void generateWebMvcConfigXMLFile(CDConsumerPartData cdConsumerPartData, String webMvcConfigXMLFilePath) {

		generateFile(cdConsumerPartData, webMvcConfigXMLFilePath, TEMPLATE_WEBMVC_CONFIG_XML_NAME);
	}

	public static void generateRootContextXMLFile(CDConsumerPartData cdConsumerPartData, String rootContextXMLFilePath) {

		generateFile(cdConsumerPartData, rootContextXMLFilePath, TEMPLATE_ROOT_CONTEXT_XML_NAME);
	}

	public static void generateCxfXMLFile(CDConsumerPartData cdConsumerPartData, WSDLInfo wsdlInfo, String cxfXMLFilePath) {

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_CONSUMER_NAME_POM, cdConsumerPartData);
		dataModel.put(PLACEHOLDER_WSDLINFO_NAME_POM, wsdlInfo);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CXF_XML_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, cxfXMLFilePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateCxfXMLFile see log file for details ");
		}
	}

	public static void generateApacheCXFWSImpl(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + generatingJavaData.getJavaName() + ".java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_WEB_SERVICE_IMPL_JAVA_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateApacheCXFWSImpl see log file for details ");
		}

	}

	public static void generateBusinessServiceInterface(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + generatingJavaData.getSimpleName() + "Service.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_BUSINESS_SERVICE_INTERFACE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateBusinessServiceInterface see log file for details ");
		}

	}

	public static void generateSpringServiceImpl(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + generatingJavaData.getSimpleName() + "ServiceImpl.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_SPRING_SERVICE_IMPL_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateSpringServiceImpl see log file for details ");
		}

	}

	public static void generateMessagesData(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "MessagesData.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_MESSAGES_DATA_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateMessagesData see log file for details ");
		}

	}

	public static void generateChoreographyLoopIndexes(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "ChoreographyLoopIndexes.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CHOREOGRAPHY_LOOP_INDEX_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateChoreographyLoopIndexes see log file for details ");
		}

	}

	public static void generateChoreographyDataService(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "ChoreographyDataService.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CHOREOGRAPHY_DATA_SERVICE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateChoreographyDataService see log file for details ");
		}
	}

	public static void generateChoreographyInstanceMessages(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "ChoreographyInstanceMessages.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CHOREOGRAPHY_INSTANCE_MESSAGES_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateChoreographyDataService see log file for details ");
		}
	}

	public static void generateChoreographyInstanceMessagesStore(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "ChoreographyInstanceMessagesStore.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CHOREOGRAPHY_INSTANCE_MESSAGES_STORE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateChoreographyDataService see log file for details ");
		}
	}
	

	public static void generateSpringChoreographiesDataServiceImpl(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "ChoreographiesDataServiceImpl.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CHOREOGRAPHIES_DATA_SERVICE_IMPL_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateSpringChoreographiesDataServiceImpl see log file for details ");
		}
		
	}
	

	public static void generateSpringChoreographyInstanceMessagesStoreImpl(String javaPath, GeneratingJavaData generatingJavaData) {
		String javaFilenamePath = javaPath + File.separatorChar + "ChoreographyInstanceMessagesStoreImpl.java";

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_GENERATING_JAVA_DATA_NAME, generatingJavaData);
		Template template;
		try {
			template = configuration.getTemplate(TEMPLATE_CHOREOGRAPHY_INSTANCE_MESSAGES_STORE_IMPL_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, javaFilenamePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateSpringChoreographyInstanceMessagesStoreImpl see log file for details ");
		}
		
	}
	
	public static void generateFile(CDConsumerPartData cdConsumerPartData, String filePath, String templateName) {

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_CONSUMER_NAME_POM, cdConsumerPartData);
		Template template;
		try {
			template = configuration.getTemplate(templateName);
			mergeTemplateDataModelIntoFile(template, dataModel, filePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into generateFile see log file for details ");
		}
	}

	public static void mergeTemplateDataModelIntoFile(Template template, Map<String, Object> dataModel, String filePath) {

		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(filePath);
			Writer out = new OutputStreamWriter(outputStream);
			template.process(dataModel, out);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into mergeTemplateDataModelIntoFile see log file for details ");
		}

	}

}
