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
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPMNUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.CDGenerationUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.WSDLUtils;

public class CDDeployGenerationTests {

	
	private static final String ARTIFACTS_PREFIX = "artifacts";
	private static final String ARTIFACTS_SUFFIX = "Artifacts";
	private static final String DEPLOY_FILE_NAME = "deploy";
	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar 
			+ "test" + File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDLS_FOLDER_NAME = "wsdls";

	private static final Logger LOGGER = LoggerFactory.getLogger(CDDeployGenerationTests.class);
	
	@Before
	public void setUp() {
	}

	@Test
	public void generateDeploy_01() {
		
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
		TaskCorrelationData taskCorrelationData = new TaskCorrelationData("Get Trip Plan","Set Trip Plan");
		tasksCorrelationData.add(taskCorrelationData);
             
		try {
			
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdtouristagent.wsdl")));
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
	        data = new WSDLData(cdClientParticipantName, cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
	        wsdlData.add(data);
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitions, cdClientWSDLDefinitionData);
//			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// add to wsdlDefinitionsAllArtifacts cdWSDLDefinitionData 
//			wsdlDefinitionsAllArtifacts.put(cdClientParticipantName, cdWSDLDefinitionData);
			
			// write WSDL cd to file
//			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
//				cdName);
			
			// write all the WSDL including cd WSDL and cd consumer part WSDL
//			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts, destinationFolder);

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

//			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(),
							wsdlDefinitions, cdClientWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);

			
		} catch (IOException e) {
			LOGGER.error("generateCD_03 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	
	@Test
	public void generateDeploy_02() {
		
		String cdParticipantName = "Tourist Agent";
		String cdParticipantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = Utils.createCDname(cdParticipantName);
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar 
				+ "CD-TouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
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
        // TODO fill prosumersParticipantsNames list
		try {
			
	        data = new WSDLData("Poi", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"poi.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Trip Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTripPlanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("News", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"news.wsdl")));
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
			// create WSDL consumer part
			Definition consumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns, cdParticipantName);
			// write WSDL consumer part to file
//			WSDLUtils.writeWSDLDefinitionToFile(consumerPartWSDLDefinition, destinationFolder,
//				cdParticipantNameNormalized);
			// create consumerPartWSDLDefinitionData
			WSDLDefinitionData consumerPartWSDLDefinitionData = new WSDLDefinitionData();
			consumerPartWSDLDefinitionData.setPrefix(cdParticipantNameNormalized);
			consumerPartWSDLDefinitionData.setWsdlFileName(cdParticipantNameNormalized);
			consumerPartWSDLDefinitionData.setWsdl(consumerPartWSDLDefinition);
			// create list of WSDLDefinitionData corresponding to the wsdl of
			// all artifacts(including consumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);;
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdParticipantName, consumerPartWSDLDefinitionData);
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdParticipantName, choreographyData, wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData);
//			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
//			// add to wsdlDefinitionsAllArtifacts cdWSDLDefinitionData 
//			wsdlDefinitionsAllArtifacts.put(cdParticipantName, cdWSDLDefinitionData);
			
			// write WSDL cd to file
//			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
			
			
			// write all the wsdl including cd WSDL and cd consumer part WSDL
//			WSDLUtils.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts,
//				destinationFolder);
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
					consumerPartWSDLDefinitionData, artifactData);

//			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);		
					
			Document deployDocument = CDGenerationUtils
					.generateCDDeployDocument(bpelData.getProcess().getPartnerLinks().getChildren(), 
							wsdlDefinitionsAllArtifacts, cdWSDLDefinitionData, bpelData.getProcess()
							.getTargetNamespace(), cdName, artifactsWSDL);
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);
			
		} catch (IOException e) {
			LOGGER.error("generateDeploy_02 > ",e);
			Assert.assertTrue(false);
		}		


	}
	
}
