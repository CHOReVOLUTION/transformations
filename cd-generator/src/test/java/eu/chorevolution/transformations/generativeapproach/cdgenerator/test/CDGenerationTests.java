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

public class CDGenerationTests {

	private static final String ARTIFACTS_PREFIX = "artifacts";
	private static final String ARTIFACTS_SUFFIX = "Artifacts";
	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String DEPLOY_FILE_NAME = "deploy";
	private static final String PROPERTIES_FILE_NAME = "properties";
	private static final String PROPERTIES_ALIASES_FILE_NAME = "propertiesAliases";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar 
			+ "test" +File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDLS_FOLDER_NAME = "wsdls";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDGenerationTests.class);

	
	@Before
	public void setUp() {
	}
		
	@Test
	public void generateCD_01() {
		
		String cdClientParticipantName = "STApp";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-STApp.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-STApp";	
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
			
			cdClientWSDLDefinitionData.setWsdlFileName(cdName);
			
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
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
					.createPropertiesAliasesWSDLDocument(choreographytns, 
					bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_01 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCD_01_NoCorrelationTasks() {
		
		String cdClientParticipantName = "STApp";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-STApp.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-STApp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		// tasksCorrelationData can be null or an empty object
		//List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		List<TaskCorrelationData> tasksCorrelationData = null;
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
			
			cdClientWSDLDefinitionData.setWsdlFileName(cdName);
			
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
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
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_01_NoCorrelationTasks > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_02() {
				
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-TouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TouristAgent" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-TouristAgent";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar)
				.append(cdName).append(File.separatorChar).toString();
				
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
	        		+ "poi.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Trip Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTripPlanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("News", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "news.wsdl")));
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
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL consumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
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
						
			// write all the wsdl including cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdParticipantName, cdName, 
					choreographytns, prosumersParticipantsNames, clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_02 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_03() {

		String cdParticipantName = "Trip Planner";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TripPlanner" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar 
				+ "CD-TripPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TripPlanner" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TripPlanner" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP5_CD-TripPlanner";
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

	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTouristAgent.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Traffic Information", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"trafficinformation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Journey Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"journeyplanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("OSM Parking", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"osmparking.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Public Transportation", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"publictransportation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Weather", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"weather.wsdl")));
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
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils.getWSDLDefinitionDataOfWSDLs(
					wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);	
			// create WSDL consumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils.generateCDProsumerPartWSDL(
					choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
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
			
			// write all the wsdl including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdParticipantName, cdName, choreographytns, prosumersParticipantsNames,
					clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_03 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCD_04() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_04 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_05() {
				
		String cdParticipantName = "SEADA-SEARP";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-SEADA-SEARP.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEARP" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP";	
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
			
			// create WSDL consumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils.generateCDProsumerPartWSDL(
					choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
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
						
			// write all the wsdl including cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdParticipantName, cdName, choreographytns, prosumersParticipantsNames,
					clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_05 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_06() {
				
		String cdParticipantName = "SEADA-SEATSA";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-SEADA-SEATSA.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEATSA" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA";	
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
	        data = new WSDLData("SEADA-SEARP", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
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
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils.getWSDLDefinitionDataOfWSDLs(
					wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL consumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils.generateCDProsumerPartWSDL(
					choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
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
						
			// write all the wsdl including cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdParticipantName, cdName, choreographytns, prosumersParticipantsNames,
					clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_06 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCD_07() {
				
		String cdParticipantName = "SEADA-TRAFFIC";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC"
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-SEADA-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC"
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-TRAFFIC" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC";	
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
			
	        data = new WSDLData("DTS-ACCIDENTS", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "bcDTS-ACCIDENTS.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-BRIDGE", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "bcDTS-BRIDGE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-CONGESTION", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "bcDTS-CONGESTION.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-WEATHER", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "bcDTS-WEATHER.wsdl")));
	        wsdlData.add(data);	        
	        data = new WSDLData("SEADA-SEATSA", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "cdSEADA-SEATSA.wsdl")));
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
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils.getWSDLDefinitionDataOfWSDLs(
					wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL consumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils.generateCDProsumerPartWSDL(
					choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
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
						
			// write all the wsdl including cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdParticipantName, cdName, choreographytns, prosumersParticipantsNames,
					clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_07 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_08() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-chor_task_cond_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-chor_task_cond_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-chor_task_cond_exp" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-chor_task_cond_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_08 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_09() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-chor_task_num_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-chor_task_num_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-chor_task_num_exp" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-chor_task_num_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_09 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_10() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_cond_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_cond_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-subchor_cond_exp" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-subchor_cond_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_10 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_11() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-subchor_num_exp";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_11 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_12() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_12 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_13() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer_2" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer_2" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_timer_2";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_13 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_14() {
				
		String cdParticipantName = "SEADA-SEATSA";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_2" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-SEADA-SEATSA.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_2" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEATSA_2" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_2";	
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
	        data = new WSDLData("SEADA-SEARP", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils.readFileToByteArray(new File(
	        		wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
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
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils.getWSDLDefinitionDataOfWSDLs(
					wsdlData, cdParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdParticipantName);
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			
			// create WSDL consumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils.generateCDProsumerPartWSDL(
					choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setParticipantName(cdParticipantName);
			prosumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
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
						
			// write all the wsdl including cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
					destinationFolder);
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdParticipantName, cdName, choreographytns, prosumersParticipantsNames,
					clientProsumersParticipantsNames, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);
			
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils.createPropertiesAliasesWSDLDocument(
					choreographytns, bpelData.getPropertyAliasesData());
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
			LOGGER.error("generateCD_14 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_15() {
		
		String cdClientParticipantName = "ND";
		String cdName = Utils.createCDname(cdClientParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_2" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_2" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_2" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String destination = TEST_RESOURCES + "test_WP4_SEADA_CD_ND-loop-subchor_num_exp_2";	
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
				
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		
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
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitions, destinationFolder);

			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
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
			LOGGER.error("generateCD_15 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
}
