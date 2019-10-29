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
package eu.chorevolution.transformations.generativeapproach.adgenerator.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.transformations.generativeapproach.adgenerator.ADGeneratorException;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.AdapterData;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.RequestTranslationData;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.ResponseTranslationData;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.WSDLTypesMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ADFileGeneratorUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ADFileGeneratorUtility.class);

	private static final String TEMPLATE_FOLDER_PATH = "/adapter_templates";
	private static final String TEMPLATE_POM_XML_NAME = "pom-xml.ftlh";
	private static final String TEMPLATE_ADAPTER_WSDL_NAME = "adapter-wsdl.ftlh";
	private static final String TEMPLATE_LOG4J_PROPERTIES_NAME = "log4j-properties.ftlh";
	private static final String TEMPLATE_CONTEXT_XML_NAME = "context-xml.ftlh";
	private static final String TEMPLATE_WEB_XML_NAME = "web-xml.ftlh";
	private static final String TEMPLATE_ROOT_CONTEXT_XML_NAME = "root-context-xml.ftlh";
	private static final String TEMPLATE_WEBMVC_CONFIG_XML_NAME = "webmvc-config-xml.ftlh";
	private static final String TEMPLATE_CXF_FILE_NAME = "cxf-xml.ftlh";
	private static final String TEMPLATE_INTEGRATION_XML_NAME = "si-components-xml.ftlh";
	private static final String TEMPLATE_REQUEST_ADAPTER_NAME = "requestTranslator-java.ftlh";
	private static final String TEMPLATE_RESPONSE_ADAPTER_NAME = "responseTranslator-java.ftlh";
	private static final String TEMPLATE_REQUEST_DATA_NAME = "requestData-java.ftlh";
	private static final String TEMPLATE_TRANSFORMATION_UTILS_NAME = "transformationUtils-java.ftlh";
	private static final String TEMPLATE_TRANSFORMATION_RULE_HANDLER_NAME = "transformationRuleHandler-java.ftlh";
	private static final String TEMPLATE_DESTINATION_URI_PROVIDER_NAME = "outboundDestinationURIProvider-java.ftlh";
	private static final String TEMPLATE_BASE_SERVICE_WSDL_NAME = "baseService-wsdl.ftlh";
	private static final String TEMPLATE_CONFIGURABLE_SERVICE_IMPL_NAME = "configurableServiceImpl-java.ftlh";
	private static final String TEMPLATE_SET_INVOCATION_ADDRES_UTILS_NAME = "setInvocationAddressUtils-java.ftlh";
	private static final String TEMPLATE_ARTIFACT_ENDPOINT_DATA_NAME = "artifactEndpointData-java.ftlh";
	private static final String UTF8_ENCODING = "UTF-8";

	private static final String PLACEHOLDER_ADAPTERDATA = "adapterData";

	private static final Configuration configuration;

	static {
		configuration = new Configuration(Configuration.VERSION_2_3_25);
		configuration.setClassForTemplateLoading(ADFileGeneratorUtility.class, TEMPLATE_FOLDER_PATH);
		configuration.setDefaultEncoding(UTF8_ENCODING);
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
	}

	public static void generatePomXMLFile(AdapterData adapterData, String pomFilePath) {

		generateFile(adapterData, pomFilePath, TEMPLATE_POM_XML_NAME);
	}

	public static void generateAdapterWSDLFile(AdapterData adapterData, AdapterModel adapterModel, String adWsdlFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("operationName", adapterModel.getHasChoreographyTasks().get(0).getName());
		data.put("inputRootElement", AdapterModelsUtility.getInputRootElementName(adapterModel.getHasChoreographyTasks().get(0)));
		data.put("outputRootElement", AdapterModelsUtility.getOutputRootElementName(adapterModel.getHasChoreographyTasks().get(0)));

		data.put("types", new WSDLTypesMap(adapterModel));
		
		generateFile(adapterData, data, adWsdlFilePath, TEMPLATE_ADAPTER_WSDL_NAME);
	}

	public static void generateLog4jPropertiesFile(AdapterData adapterData, String log4jPropertiesFilePath) {

		generateFile(adapterData, log4jPropertiesFilePath, TEMPLATE_LOG4J_PROPERTIES_NAME);
	}

	public static void generateContextXMLFile(AdapterData adapterData, String contextFilePath) {

		generateFile(adapterData, contextFilePath, TEMPLATE_CONTEXT_XML_NAME);
	}

	public static void generateRootContextXMLFile(AdapterData adapterData, String rootContextXMLFilePath) {

		generateFile(adapterData, rootContextXMLFilePath, TEMPLATE_ROOT_CONTEXT_XML_NAME);
	}

	public static void generateWebXMLFile(AdapterData adapterData, String webXMLFilePath) {

		generateFile(adapterData, webXMLFilePath, TEMPLATE_WEB_XML_NAME);
	}	
	
	public static void generateWebMvcConfigXMLFile(AdapterData adapterData, String webMvcConfigXMLFilePath) {

		generateFile(adapterData, webMvcConfigXMLFilePath, TEMPLATE_WEBMVC_CONFIG_XML_NAME);
	}

	public static void generateCxfXMLFile(AdapterData adapterData, String cxfXMLFilePath) {

		generateFile(adapterData, cxfXMLFilePath, TEMPLATE_CXF_FILE_NAME);
	}

	public static void generateIntegrationXMLFile(AdapterData adapterData, String integrationXMLFilePath) {

		generateFile(adapterData, integrationXMLFilePath, TEMPLATE_INTEGRATION_XML_NAME);
	}

	public static void generateRequestTranslatorJavaFile(AdapterData adapterData, AdapterModel adapterModel, String requestAdapterJavaFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("inputRootElement", AdapterModelsUtility.getInputOperationRootElementName(adapterModel.getHasOperations().get(0)));

		RequestTranslationData translationData = new RequestTranslationData(adapterModel);
		data.put("translationDataRoot", translationData);
		
		generateFile(adapterData, data, requestAdapterJavaFilePath, TEMPLATE_REQUEST_ADAPTER_NAME);
	}

	public static void generateResponseTranslatorJavaFile(AdapterData adapterData, AdapterModel adapterModel, String responseAdapterJavaFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("responseRootElement", AdapterModelsUtility.getOutputRootElementName(adapterModel.getHasChoreographyTasks().get(0)));

		ResponseTranslationData translationData = new ResponseTranslationData(adapterModel);
		data.put("translationDataRoot", translationData);

		generateFile(adapterData, data, responseAdapterJavaFilePath, TEMPLATE_RESPONSE_ADAPTER_NAME);
	}

	public static void generateRequestDataJavaFile(AdapterData adapterData, String requestDataJavaFilePath) {

		generateFile(adapterData, requestDataJavaFilePath, TEMPLATE_REQUEST_DATA_NAME);
	}


	public static void generateTransformationUtilsJavaFile(AdapterData adapterData, String transformationUtilsJavaFilePath) {

		generateFile(adapterData, transformationUtilsJavaFilePath, TEMPLATE_TRANSFORMATION_UTILS_NAME);
	}

	public static void generateTransformationRuleHandlerJavaFile(AdapterData adapterData, String transformationRuleHandlerJavaFilePath) {

		generateFile(adapterData, transformationRuleHandlerJavaFilePath, TEMPLATE_TRANSFORMATION_RULE_HANDLER_NAME);
	}

	public static void generateDestinationURIProviderJavaFile(AdapterData adapterData, String destinationURIProviderJavaFilePath) {

		generateFile(adapterData, destinationURIProviderJavaFilePath, TEMPLATE_DESTINATION_URI_PROVIDER_NAME);
	}

	public static void generateBaseServiceWSDLFile(AdapterData adapterData, String baseServiceWSDLFilePath) {

		generateFile(adapterData, baseServiceWSDLFilePath, TEMPLATE_BASE_SERVICE_WSDL_NAME);
	}

	public static void generateConfigurableServiceImplJavaFile(AdapterData adapterData, String configurableServiceImplJavaFilePath) {

		generateFile(adapterData, configurableServiceImplJavaFilePath, TEMPLATE_CONFIGURABLE_SERVICE_IMPL_NAME);
	}

	public static void generateSetInvocationAddressUtilsJavaFile(AdapterData adapterData, String setInvocationAddressUtilsJavaFilePath) {

		generateFile(adapterData, setInvocationAddressUtilsJavaFilePath, TEMPLATE_SET_INVOCATION_ADDRES_UTILS_NAME);
	}

	public static void generateArtifactEndpointDataJavaFile(AdapterData adapterData, String artifactEndpointDataJavaFilePath) {

		generateFile(adapterData, artifactEndpointDataJavaFilePath, TEMPLATE_ARTIFACT_ENDPOINT_DATA_NAME);
	}

	private static void generateFile(AdapterData adapterData, String filePath, String templateName) {

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_ADAPTERDATA, adapterData);
		generateFile(dataModel, filePath, templateName);
	}

	private static void generateFile(AdapterData adapterData, Map<String, Object> dataModel, String filePath, String templateName) {

		dataModel.put(PLACEHOLDER_ADAPTERDATA, adapterData);
		generateFile(dataModel, filePath, templateName);
	}

	private static void generateFile(Map<String, Object> dataModel, String filePath, String templateName) {

		Template template;
		try {
			template = configuration.getTemplate(templateName);
			mergeTemplateDataModelIntoFile(template, dataModel, filePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ADGeneratorException("Exception into generateFile see log file for details ");
		}
	}

	private static void mergeTemplateDataModelIntoFile(Template template, Map<String, Object> dataModel, String filePath) {

		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(filePath);
			Writer out = new OutputStreamWriter(outputStream);
			template.process(dataModel, out);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw new ADGeneratorException("Exception into mergeTemplateDataModelIntoFile see log file for details ");
		}

	}

}
