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
package eu.chorevolution.transformations.generativeapproach.bcgenerator.util;

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

import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.BCGeneratorException;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BCData;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.WSDLTypesMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class BCFileGeneratorUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCFileGeneratorUtility.class);

	private static final String TEMPLATE_FOLDER_PATH = "/bc_templates";
	private static final String TEMPLATE_LOG4J_PROPERTIES = "log4j-properties.ftlh";
	private static final String TEMPLATE_BINDING_XML = "binding-xml.ftlh";
	private static final String TEMPLATE_CONTEXT_XML = "context-xml.ftlh";
	private static final String TEMPLATE_WEBMVC_CONFIG_XML = "webmvc-config-xml.ftlh";
	private static final String TEMPLATE_ROOT_CONTEXT_XML = "root-context-xml.ftlh";
	private static final String TEMPLATE_BASE_SERVICE_WSDL = "baseService-wsdl.ftlh";
	private static final String TEMPLATE_BUSINESSEXCEPTION = "businessException-java.ftlh";
	private static final String TEMPLATE_CONFIGURABLE_SERVICE_IMPL = "configurableServiceImpl-java.ftlh";
	private static final String TEMPLATE_SET_INVOCATION_ADDRES_UTILS = "setInvocationAddressUtils-java.ftlh";
	private static final String TEMPLATE_ARTIFACT_ENDPOINT_DATA = "artifactEndpointData-java.ftlh";


	private static final String TEMPLATE_SOAP_POM_XML = "soap" + File.separatorChar + "pom-xml.ftlh";
	private static final String TEMPLATE_SOAP_WEB_XML = "soap" + File.separatorChar + "web-xml.ftlh";
	private static final String TEMPLATE_SOAP_CXF_FILE = "soap" + File.separatorChar + "cxf-xml.ftlh";
	private static final String TEMPLATE_SOAP_PTIMPL = "soap" + File.separatorChar + "ptImpl-java.ftlh";
	private static final String TEMPLATE_SOAP_SERVICE = "soap" + File.separatorChar + "service-java.ftlh";
	private static final String TEMPLATE_SOAP_SERVICEIMPL = "soap" + File.separatorChar + "serviceImpl-java.ftlh";
	private static final String TEMPLATE_SOAP_WSDL = "soap" + File.separatorChar + "wsdl.ftlh";

	private static final String TEMPLATE_REST_POM_XML = "rest" + File.separatorChar + "pom-xml.ftlh";
	private static final String TEMPLATE_REST_WEB_XML = "rest" + File.separatorChar + "web-xml.ftlh";
	private static final String TEMPLATE_REST_CXF_FILE = "rest" + File.separatorChar + "cxf-xml.ftlh";
	private static final String TEMPLATE_REST_JAVA_FILE = "rest" + File.separatorChar + "rest-java.ftlh";

	private static final String UTF8_ENCODING = "UTF-8";

	private static final String PLACEHOLDER_BCDATA = "bcData";

	private static final Configuration configuration;

	static {
		configuration = new Configuration(Configuration.VERSION_2_3_25);
		configuration.setClassForTemplateLoading(BCFileGeneratorUtility.class, TEMPLATE_FOLDER_PATH);
		configuration.setDefaultEncoding(UTF8_ENCODING);
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
	}

	public static void generatePomXMLFile(BCData bcData, String pomFilePath) {

		generateFile(bcData, pomFilePath, TEMPLATE_SOAP_POM_XML);
	}

	public static void generateRESTPomXMLFile(BCData bcData, String pomFilePath) {

		generateFile(bcData, pomFilePath, TEMPLATE_REST_POM_XML);
	}

	public static void generateWSDLFile(BCData bcData, GIDLModel gidlModel, String wsdlFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("operationName", GidlModelsUtility.getFirstOperation(gidlModel).getName());
		data.put("inputMessageName", GidlModelsUtility.getInputMessageName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
		data.put("inputRootElement", GidlModelsUtility.getInputRootElementName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
		data.put("outputMessageName", GidlModelsUtility.getOutputMessageName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
		data.put("outputRootElement", GidlModelsUtility.getOutputRootElementName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));

		data.put("types", new WSDLTypesMap(gidlModel));
		
		generateFile(bcData, data, wsdlFilePath, TEMPLATE_SOAP_WSDL);
	}

//	public static void generateRESTWSDLFile(BCData bcData, GIDLModel gidlModel, String wsdlFilePath) {
//
//		Map<String, Object> data = new HashMap<>();
//		data.put("operationName", GidlModelsUtility.getFirstOperation(gidlModel).getName());
//		data.put("inputMessageName", GidlModelsUtility.getInputMessageName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
//		data.put("inputRootElement", GidlModelsUtility.getInputRootElementName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
//		data.put("outputMessageName", GidlModelsUtility.getOutputMessageName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
//		data.put("outputRootElement", GidlModelsUtility.getOutputRootElementName(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));
//
//		data.put("types", new WSDLTypesMap(gidlModel));
//		
//		generateFile(bcData, data, wsdlFilePath, TEMPLATE_REST_WSDL);
//	}

	public static void generateLog4jPropertiesFile(BCData bcData, String log4jPropertiesFilePath) {

		generateFile(bcData, log4jPropertiesFilePath, TEMPLATE_LOG4J_PROPERTIES);
	}

	public static void generateBindingXMLFile(BCData bcData, String bindingFilePath) {

		generateFile(bcData, bindingFilePath, TEMPLATE_BINDING_XML);
	}

	public static void generateContextXMLFile(BCData bcData, String contextFilePath) {

		generateFile(bcData, contextFilePath, TEMPLATE_CONTEXT_XML);
	}

	public static void generateRootContextXMLFile(BCData bcData, String rootContextXMLFilePath) {

		generateFile(bcData, rootContextXMLFilePath, TEMPLATE_ROOT_CONTEXT_XML);
	}

	public static void generateWebXMLFile(BCData bcData, String webXMLFilePath) {

		generateFile(bcData, webXMLFilePath, TEMPLATE_SOAP_WEB_XML);
	}

	public static void generateRESTWebXMLFile(BCData bcData, String webXMLFilePath) {

		generateFile(bcData, webXMLFilePath, TEMPLATE_REST_WEB_XML);
	}
	
	public static void generateWebMvcConfigXMLFile(BCData bcData, String webMvcConfigXMLFilePath) {

		generateFile(bcData, webMvcConfigXMLFilePath, TEMPLATE_WEBMVC_CONFIG_XML);
	}

	public static void generateRESTWebMvcConfigXMLFile(BCData bcData, String webMvcConfigXMLFilePath) {

		generateFile(bcData, webMvcConfigXMLFilePath, TEMPLATE_WEBMVC_CONFIG_XML);
	}

	public static void generateCxfXMLFile(BCData bcData, String cxfXMLFilePath) {

		generateFile(bcData, cxfXMLFilePath, TEMPLATE_SOAP_CXF_FILE);
	}

	public static void generateRESTCxfXMLFile(BCData bcData, String cxfXMLFilePath) {

		generateFile(bcData, cxfXMLFilePath, TEMPLATE_REST_CXF_FILE);
	}

	public static void generateBaseServiceWSDLFile(BCData bcData, String baseServiceWSDLFilePath) {

		generateFile(bcData, baseServiceWSDLFilePath, TEMPLATE_BASE_SERVICE_WSDL);
	}

	public static void generatePTImplJavaFile(BCData bcData, GIDLModel gidlModel, String ptImplJavaFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("operationName", GidlModelsUtility.getFirstOperation(gidlModel).getName());
		data.put("inputMessageName", GidlModelsUtility.getInputMessageName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("inputRootElement", GidlModelsUtility.getInputRootElementName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("outputMessageName", GidlModelsUtility.getOutputMessageName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("outputRootElement", GidlModelsUtility.getOutputRootElementName(GidlModelsUtility.getFirstOperation(gidlModel)));

		data.put("isOutputRootMultiple", GidlModelsUtility.isOutputRootElementMultiple(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));

		generateFile(bcData, data, ptImplJavaFilePath, TEMPLATE_SOAP_PTIMPL);
	}

	public static void generateServiceJavaFile(BCData bcData, GIDLModel gidlModel, String serviceJavaFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("operationName", GidlModelsUtility.getFirstOperation(gidlModel).getName());
		data.put("inputMessageName", GidlModelsUtility.getInputMessageName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("inputRootElement", GidlModelsUtility.getInputRootElementName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("outputMessageName", GidlModelsUtility.getOutputMessageName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("outputRootElement", GidlModelsUtility.getOutputRootElementName(GidlModelsUtility.getFirstOperation(gidlModel)));

		data.put("isOutputRootMultiple", GidlModelsUtility.isOutputRootElementMultiple(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));

		generateFile(bcData, data, serviceJavaFilePath, TEMPLATE_SOAP_SERVICE);
	}

	public static void generateServiceImplJavaFile(BCData bcData, GIDLModel gidlModel, String serviceImplJavaFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("operationName", GidlModelsUtility.getFirstOperation(gidlModel).getName());
		data.put("inputMessageName", GidlModelsUtility.getInputMessageName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("inputRootElement", GidlModelsUtility.getInputRootElementName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("outputMessageName", GidlModelsUtility.getOutputMessageName(GidlModelsUtility.getFirstOperation(gidlModel)));
		data.put("outputRootElement", GidlModelsUtility.getOutputRootElementName(GidlModelsUtility.getFirstOperation(gidlModel)));

		data.put("isOutputRootMultiple", GidlModelsUtility.isOutputRootElementMultiple(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));

		data.put("types", new WSDLTypesMap(gidlModel));

		data.put("inputDataElements", GidlModelsUtility.getInputDataElements(gidlModel));

		data.put("url", gidlModel.getHostAddress() + GidlModelsUtility.getOperationUri(gidlModel.getHasInterfaces().get(0).getHasOperations().get(0)));

		generateFile(bcData, data, serviceImplJavaFilePath, TEMPLATE_SOAP_SERVICEIMPL);
	}

	public static void generateRESTJavaFile(BCData bcData, WSDLInfo wsdlModel, String restJavaFilePath) {

		Map<String, Object> data = new HashMap<>();
		data.put("operationName", WSDLUtility.getOperationName(wsdlModel));
		data.put("inputRootItem",WSDLUtility.getInputMessageRootItemName(wsdlModel));
		data.put("outputRootItem", WSDLUtility.getOutputMessageRootItemName(wsdlModel));

		data.put("requestValues", WSDLUtility.getInputMessageMapper(wsdlModel));

		generateFile(bcData, data, restJavaFilePath, TEMPLATE_REST_JAVA_FILE);
	}

	public static void generateBusinessExceptionJavaFile(BCData bcData, String businessExceptionJavaFilePath) {

		generateFile(bcData, businessExceptionJavaFilePath, TEMPLATE_BUSINESSEXCEPTION);
	}
	
	public static void generateConfigurableServiceImplJavaFile(BCData bcData, String configurableServiceImplJavaFilePath) {

		generateFile(bcData, configurableServiceImplJavaFilePath, TEMPLATE_CONFIGURABLE_SERVICE_IMPL);
	}

	public static void generateSetInvocationAddressUtilsJavaFile(BCData bcData, String setInvocationAddressUtilsJavaFilePath) {

		generateFile(bcData, setInvocationAddressUtilsJavaFilePath, TEMPLATE_SET_INVOCATION_ADDRES_UTILS);
	}

	public static void generateArtifactEndpointDataJavaFile(BCData bcData, String artifactEndpointDataJavaFilePath) {

		generateFile(bcData, artifactEndpointDataJavaFilePath, TEMPLATE_ARTIFACT_ENDPOINT_DATA);
	}

	public static void generateSecurityHelperJavaFile(BCData bcData, String securityHelperJavaFilePath) {

		generateFile(bcData, securityHelperJavaFilePath, "soap" + File.separatorChar + "securityHelper-java.ftlh");
	}

	private static void generateFile(BCData bcData, String filePath, String templateName) {

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_BCDATA, bcData);
		generateFile(dataModel, filePath, templateName);
	}

	private static void generateFile(BCData bcData, Map<String, Object> dataModel, String filePath, String templateName) {

		dataModel.put(PLACEHOLDER_BCDATA, bcData);
		generateFile(dataModel, filePath, templateName);
	}

	private static void generateFile(Map<String, Object> dataModel, String filePath, String templateName) {

		Template template;
		try {
			template = configuration.getTemplate(templateName);
			mergeTemplateDataModelIntoFile(template, dataModel, filePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new BCGeneratorException("Exception into generateFile see log file for details ");
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
			throw new BCGeneratorException("Exception into mergeTemplateDataModelIntoFile see log file for details ");
		}

	}

}
