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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.wsdl.Definition;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGenerator;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.impl.CDGeneratorImpl;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ArtifactData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ProjectionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLDefinitionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPELUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPMNUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.CDGenerationUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.WSDLUtils;

public class CDGenerationTests_CHOReVOLUTION_STUDIO {

	private static final String ARTIFACTS_PREFIX = "artifacts";
	private static final String ARTIFACTS_SUFFIX = "Artifacts";
	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String DEPLOY_FILE_NAME = "deploy";
	private static final String PROPERTIES_FILE_NAME = "properties";
	private static final String PROPERTIES_ALIASES_FILE_NAME = "propertiesAliases";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar 
			+ "test" + File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDLS_FOLDER_NAME = "wsdls";
	private static final Logger LOGGER = 
			LoggerFactory.getLogger(CDGenerationTests_CHOReVOLUTION_STUDIO.class);
	
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_01() {
		
		String cdClientParticipantName = "STApp";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
             
		try {
			
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTouristAgent.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);	
			
			// write WSDL cd to file
			WSDLUtils
				.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, 
					cdName, choreographytns, tasksCorrelationData, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdClientWSDLDefinitionData, 
					artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);				
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);
	
			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_01 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_01_NoCorrelationTasks() {
		
		String cdClientParticipantName = "STApp";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath()).replace(File
				.separatorChar+".","")
				+File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		// tasksCorrelationData can be null or an empty object
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		//tasksCorrelationData = null;
		//tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
             
		try {
			
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTouristAgent.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);	
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, cdName,
							choreographytns, tasksCorrelationData, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions,
							cdClientWSDLDefinitionData, artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);				
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);
	
			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_01_NoCorrelationTasks > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_02() {
				
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTouristAgent.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath()).replace(File
				.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");          
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner");  
		try {
			
	        data = new WSDLData("Poi", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "Poi.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Trip Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTripPlanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("News", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "News.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("STApp", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSTApp.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);	
			// create WSDL prosumer part
			Definition consumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(consumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData consumerPartWSDLDefinitionData = new WSDLDefinitionData();
			consumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			consumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			consumerPartWSDLDefinitionData.setWsdl(consumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, consumerPartWSDLDefinitionData);
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);				
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName,
					choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					consumerPartWSDLDefinitionData, artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);				
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);			
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_02 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_03() {

		String cdParticipantName = "Trip Planner";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTripPlanner.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO";
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
		
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");          
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner");    
		try {
			
	        data = new WSDLData("Traffic Information", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"TrafficInformation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Journey Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"JourneyPlanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("OSM Parking", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"OSMParking.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Public Transportation", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"PublicTransportation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Weather", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"Weather.wsdl")));
	        wsdlData.add(data); 
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTouristAgent.wsdl")));
	        wsdlData.add(data); 	        
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);	
			// create WSDL prosumer part
			Definition consumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(consumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData consumerPartWSDLDefinitionData = new WSDLDefinitionData();
			consumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			consumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			consumerPartWSDLDefinitionData.setWsdl(consumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, consumerPartWSDLDefinitionData);
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);				
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
			
			// write all the wsdl including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
						choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
						consumerPartWSDLDefinitionData, artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);				
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);	
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_03 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_04() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CDND.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
				"Set Eco Speed Route Information"));	
		
		try {
			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, 
						cdName, choreographytns, tasksCorrelationData, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdClientWSDLDefinitionData,
						artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.
							getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);	
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_04 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_05() {
				
		String cdParticipantName = "SEADA-SEARP";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEARP.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar 
				+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("ND", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "cdND.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-GOOGLE", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"DTS-GOOGLE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-HERE", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"DTS-HERE.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
						choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
						prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_05 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_06() {
				
		String cdParticipantName = "SEADA-SEATSA";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEATSA.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath()).replace(File
				.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("ND", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"cdND.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
						choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
						prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, 
							bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_06 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_06_WithLoopIndex() {
				
		String cdParticipantName = "SEADA-SEATSA";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-SEATSA[WithLoopIndex].bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath()).replace(File
				.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("ND", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"cdND.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
						choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
						prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils
				.createSetInvocationAddressElements(cdParticipantName, cdName, destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_06_WithLoopIndex > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_07() {
				
		String cdParticipantName = "SEADA-TRAFFIC";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-TRAFFIC.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("DTS-ACCIDENTS", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-ACCIDENTS.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-BRIDGE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-BRIDGE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-CONGESTION", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-CONGESTION.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-WEATHER", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-WEATHER.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_07 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_08() {
				
		String cdParticipantName = "Mobility Information Planner";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdMobilityInformationPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Journey Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "journeyplanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("Parking", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "parking.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Public Transportation", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "publicTransportation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Traffic", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "traffic.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdTouristAgent.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_08 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_09() {
		
		String cdClientParticipantName = "STApp";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Tourist Guide","Set Tourist Guide"));
		
		try {
			
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTouristAgent.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, 
						cdName, choreographytns, tasksCorrelationData, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdClientWSDLDefinitionData,
						artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.
							getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);	
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_09 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_10() {
				
		String cdParticipantName = "Tourism Information Planner";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTourismInformationPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("News", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "news.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("Personal Weather Stations", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "personalweatherstations.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Poi", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "poi.wsdl")));
	        wsdlData.add(data);        
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdTouristAgent.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_10 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_11() {
				
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Mobility Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdMobilityInformationPlanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("STApp", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSTApp.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Tourism Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdTourismInformationPlanner.wsdl")));
	        wsdlData.add(data);        
 	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_11 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_12() {
				
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Mobility Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdMobilityInformationPlanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("STApp", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSTApp.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Tourism Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdTourismInformationPlanner.wsdl")));
	        wsdlData.add(data);        
 	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_12 > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_13() {
				
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Mobility Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdMobilityInformationPlanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("STApp", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSTApp.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Tourism Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdTourismInformationPlanner.wsdl")));
	        wsdlData.add(data);        
 	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_13 > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_14() {
				
		String cdParticipantName = "Mobility Information Planner";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdMobilityInformationPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Journey Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "journeyplanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("Parking", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "parking.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Public Transportation", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "publicTransportation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Traffic", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "traffic.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdTouristAgent.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_14 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_15() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CDND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
				"Set Eco Speed Route Information"));	
		
		try {
			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, 
						cdName, choreographytns, tasksCorrelationData, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdClientWSDLDefinitionData,
						artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.
							getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);	
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_15 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_16() {
				
		String cdParticipantName = "SEADA-SEARP";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEARP.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("ND", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "cdND.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-GOOGLE", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"DTS-GOOGLE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-HERE", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"DTS-HERE.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
						choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
						prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_16 > ",e);
			Assert.assertTrue(false);
		}
	}	
	

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_17() {
				
		String cdParticipantName = "SEADA-SEATSA";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEATSA.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath()).replace(File
				.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("ND", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"cdND.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
						choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
						prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, 
							bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_17 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_18() {
				
		String cdParticipantName = "SEADA-TRAFFIC";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("DTS-ACCIDENTS", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-ACCIDENTS.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-BRIDGE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-BRIDGE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-CONGESTION", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-CONGESTION.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-WEATHER", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-WEATHER.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_18 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_19() {
				
		String cdParticipantName = "DTS-SEGMENT-TRAFFIC";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdDTS-SEGMENT-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO"
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("Traffic Information Collector");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("DTS-SEGMENT-TRAFFIC");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("DTS-ACCIDENTS", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-ACCIDENTS.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-BRIDGE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-BRIDGE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-CONGESTION", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-CONGESTION.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-WEATHER", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-WEATHER.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_19 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_20() {
				
		String cdParticipantName = "SEADA-TRAFFIC";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO"
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("Traffic Information Collector");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("DTS-SEGMENT-TRAFFIC");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("DTS-SEGMENT-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdDTS-SEGMENT-TRAFFIC.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-AREA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "DTS-AREA-TRAFFIC.wsdl")));
	        wsdlData.add(data);
 	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_20 > ",e);
			Assert.assertTrue(false);
		}
	}		

	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_21() {
		
		String cdClientParticipantName = "Traffic Information Collector";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection"
				+ "_CD_Traffic_Information_Collector_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTrafficInformationCollector.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection"
				+ "_CD_Traffic_Information_Collector_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES + "test_WP4_TrafficAreaInformationCollection"
				+ "_CD_Traffic_Information_Collector_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_TrafficAreaInformationCollection"
				+ "_CD_Traffic_Information_Collector_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
	
		try {
			
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-TRAFFIC.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, 
						cdName, choreographytns, tasksCorrelationData, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdClientWSDLDefinitionData,
						artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.
							getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);	
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_21 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_22() {
				
		String cdParticipantName = "SEADA-SEATSA";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-SEATSA.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO"
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        
		try {

	        data = new WSDLData("ND", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdND.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);	
	        data = new WSDLData("DTS-AREA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "DTS-AREA-TRAFFIC.wsdl")));
	        wsdlData.add(data);		        
        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_22 > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_23() {
				
		String cdParticipantName = "SEADA-SEARP";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-SEARP.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO"
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        
		try {

	        data = new WSDLData("DTS-GOOGLE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-GOOGLE.wsdl")));
	        wsdlData.add(data);	
	        data = new WSDLData("DTS-HERE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-HERE.wsdl")));
	        wsdlData.add(data);	
	        data = new WSDLData("ND", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdND.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);	
	                
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_23 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_24() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdND.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES + "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information"
				,"Set Eco Speed Route Information"));
		
		try {
			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);
			
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdClientParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);

			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd WSDL and cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils
					.createCDClientBpelProcessFromChoreography(cdClientParticipantName, 
						cdName, choreographytns, tasksCorrelationData, 
						BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
						choreographyData.getChoreographies(), wsdlDefinitions, cdClientWSDLDefinitionData,
						artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.
							getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);	
			
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_24 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_25() {
				
		String cdParticipantName = "Mobility Information Planner";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdMobilityInformationPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Journey Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "journeyplanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("Parking", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "parking.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Public Transportation", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "publicTransportation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Traffic", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "traffic.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdTouristAgent.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_25 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_26() {
				
		String cdParticipantName = "SEADA-TRAFFIC";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop_cond_exp" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar 
				+ "cdSEADA-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop_cond_exp" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop_cond_exp" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop_cond_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("ND");
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
        
		try {
			
	        data = new WSDLData("DTS-ACCIDENTS", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-ACCIDENTS.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-BRIDGE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-BRIDGE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-CONGESTION", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-CONGESTION.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-WEATHER", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "bcDTS-WEATHER.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);	 
	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_26 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_CHOReVOLUTION_STUDIO_27() {
				
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp" 
				+File.separatorChar+WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO-loop-chor_task_cond_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");
        prosumersParticipantsNames.add("Tourist Agent");
        prosumersParticipantsNames.add("Mobility Information Planner");
        prosumersParticipantsNames.add("Tourism Information Planner");
        
		try {
			
	        data = new WSDLData("Mobility Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdMobilityInformationPlanner.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("STApp", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSTApp.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Tourism Information Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        				+ "cdTourismInformationPlanner.wsdl")));
	        wsdlData.add(data);        
 	        
			FileUtils.forceMkdir(new File(destinationFolder));
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);
		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
	
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of prosumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
			
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
						
			// write all the wsdl including cd prosumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils
					.createCDProsumerBpelProcessFromChoreography(cdParticipantName, cdName, 
							choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
							BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
							choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
							prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			

			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
			
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);			

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdParticipantName, cdName,
					destinationFolder);	
			
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
		} catch (IOException e) {
			LOGGER.error("generateCD_CHOReVOLUTION_STUDIO_27 > ",e);
			Assert.assertTrue(false);
		}
	}		
}
