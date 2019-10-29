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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreeMarkerUtils {

	private static final String BASE_SERVICE_ARTIFACTS_TEMPLATE_NAME = "base-service-artifacts-wsdl.ftlh";
	private static final String BASE_SERVICE_BPEL_PROCESS_TEMPLATE_NAME = "base-service-bpel.ftlh";		
	private static final String BASE_SERVICE_WSDL_TEMPLATE_NAME = "base-service-wsdl.ftlh";		
	private static final String ECLIPSE_PROJECT_TEMPLATE_NAME = "eclipse-project.ftlh";	
	private static final String ECLIPSE_COMMON_COMPONENT_TEMPLATE_NAME =
			"eclipse-org.eclipse.wst.common-component"+ ".ftlh";	
	private static final String ECLIPSE_FACET_CORE_TEMPLATE_NAME = 
			"eclipse-org-eclipse-wst-common-project-facet"+ "-core-xml.ftlh";
	private static final String PLACEHOLDER_CDNAME = "cdName";
	private static final String PLACEHOLDER_PARTICIPANTNAME = "participantName";
	private static final String TEMPLATE_FOLDER_PATH = "/templates";
	private static final String UTF8_ENCODING = "UTF-8";
	
	private static final Configuration CONFIGURATION;
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(FreeMarkerUtils.class);	

	static {
		CONFIGURATION = new Configuration(Configuration.VERSION_2_3_25);
		CONFIGURATION.setClassForTemplateLoading(FreeMarkerUtils.class, TEMPLATE_FOLDER_PATH);
		CONFIGURATION.setDefaultEncoding(UTF8_ENCODING);
		CONFIGURATION.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		CONFIGURATION.setLogTemplateExceptions(false);
	}

	public static void generateBaseServiceBpelProcess(String participantName,
			String baseServiceBpelProcessPath){
		
		// log execution flow
		LOGGER.entry();	
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_PARTICIPANTNAME, participantName);
		Template template;
		try {
			template = CONFIGURATION.getTemplate(BASE_SERVICE_BPEL_PROCESS_TEMPLATE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, baseServiceBpelProcessPath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into generateBaseServiceWSDL see log file for details");
		}
		// log execution flow
		LOGGER.exit();	
	}
	
	public static void generateBaseServiceArtifactsWSDL(String baseServiceArtifactsWSDLPath){
		
		// log execution flow
		LOGGER.entry();	
		Map<String, Object> dataModel = new HashMap<>();
		Template template;
		try {
			template = CONFIGURATION.getTemplate(BASE_SERVICE_ARTIFACTS_TEMPLATE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, baseServiceArtifactsWSDLPath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into generateBaseServiceWSDL see log file for details");
		}
		// log execution flow
		LOGGER.exit();	
	}	
	
	public static void generateBaseServiceWSDL(String cdName, String baseServiceWSDLpath){

		// log execution flow
		LOGGER.entry();	
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_CDNAME, cdName);
		Template template;
		try {
			template = CONFIGURATION.getTemplate(BASE_SERVICE_WSDL_TEMPLATE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, baseServiceWSDLpath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into generateBaseServiceWSDL see log file for details");
		}
		// log execution flow
		LOGGER.exit();	
	}
	
	public static void generateEclipseFacetCoreFile(String eclipseFacetCoreFilePath){

		// log execution flow
		LOGGER.entry();	
		Map<String, Object> dataModel = new HashMap<>();
		Template template;
		try {
			template = CONFIGURATION.getTemplate(ECLIPSE_FACET_CORE_TEMPLATE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, eclipseFacetCoreFilePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into generateEclipseFacetCoreFile see log file for details");
		}
		// log execution flow
		LOGGER.exit();	
	}	

	public static void generateEclipseCommonComponentFile(String cdName,
			String eclipseCommonComponentFilePath){
	
		// log execution flow
		LOGGER.entry();	
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_CDNAME, cdName);	
		Template template;
		try {
			template = CONFIGURATION.getTemplate(ECLIPSE_COMMON_COMPONENT_TEMPLATE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, eclipseCommonComponentFilePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into generateEclipseCommonComponentFile see log file for details");
		}	
		// log execution flow
		LOGGER.exit();	
	}	
	
	public static void generateEclipseProjectFile(String cdName, String eclipseProjectFilePath){

		// log execution flow
		LOGGER.entry();	
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put(PLACEHOLDER_CDNAME, cdName);	
		Template template;
		try {
			template = CONFIGURATION.getTemplate(ECLIPSE_PROJECT_TEMPLATE_NAME);
			mergeTemplateDataModelIntoFile(template, dataModel, eclipseProjectFilePath);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into generateEclipseProjectFile see log file for details");
		}
		// log execution flow
		LOGGER.exit();	
	}
	
	public static void mergeTemplateDataModelIntoFile(Template template, Map<String, Object> dataModel,
			String filePath) {

		// log execution flow
		LOGGER.entry();	
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(filePath);
			Writer out = new OutputStreamWriter(outputStream);
			template.process(dataModel, out);
		} catch (TemplateException | IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into mergeTemplateDataModelIntoFile see log file for details ");
		}
		// log execution flow
		LOGGER.exit();	
	}
	
	
	
}
