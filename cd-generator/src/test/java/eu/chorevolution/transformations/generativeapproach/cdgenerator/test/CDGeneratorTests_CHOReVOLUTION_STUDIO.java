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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGenerator;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.impl.CDGeneratorImpl;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.CD;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ProjectionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;

public class CDGeneratorTests_CHOReVOLUTION_STUDIO {
	
	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String CHOREOGRAPHIES_FOLDER_NAME = "choreographies";
	private static final String TARGZ_EXTENSION = ".tar.gz";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar
			+ "test" +File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDLS_FOLDER_NAME = "wsdls";
	
	private static final Logger LOGGER =
			LoggerFactory.getLogger(CDGeneratorTests_CHOReVOLUTION_STUDIO.class);	

	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_01() {
		
		String cdParticipantName = "STApp";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO").getAbsolutePath()
				.replace(File.separatorChar+".","")+File.separatorChar+cdName+TARGZ_EXTENSION;		

		String cdClientParticipantName = "STApp";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();  
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner"); 
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "cdSTApp.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Tourist Agent")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TouristAgent" +File.separatorChar + "cdTouristAgent.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TouristAgent" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Trip Planner")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TripPlanner" +File.separatorChar + "cdTripPlanner.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TripPlanner" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
						
	        CD cd = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_01 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_01_NoCorrelationTasks() {
		
		String cdParticipantName = "STApp";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO"
				+ File.separatorChar + CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO").getAbsolutePath()
				.replace(File.separatorChar+".","")+File.separatorChar+cdName+TARGZ_EXTENSION;		

		String cdClientParticipantName = "STApp";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();  
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner"); 
		
		// tasksCorrelationData can be null or an empty object       
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData = null;
		//tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "cdSTApp.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Tourist Agent")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TouristAgent" +File.separatorChar + "cdTouristAgent.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TouristAgent" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Trip Planner")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TripPlanner" +File.separatorChar + "cdTripPlanner.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-TripPlanner" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
						
	        CD cd = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_01_NoCorrelationTasks > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_02() {
		
		String cdParticipantName = "Tourist Agent";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" +File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","")
				+File.separatorChar;	
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "STApp";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();  
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner"); 
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "cdSTApp.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Tourist Agent")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TouristAgent" +File.separatorChar + "cdTouristAgent.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TouristAgent" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Trip Planner")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TripPlanner" +File.separatorChar + "cdTripPlanner.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TripPlanner" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
			WSDLData data = new WSDLData("Poi", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
					+"poi.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("News", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "news.wsdl")));
	        wsdlData.add(data);
			
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_02 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_03() {
		
		String cdParticipantName = "Trip Planner";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES
				+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTripPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;	
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "STApp";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();  
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner"); 
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "cdSTApp.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-STApp" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Tourist Agent")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TouristAgent" +File.separatorChar + "cdTouristAgent.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TouristAgent" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Trip Planner")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TripPlanner" +File.separatorChar + "cdTripPlanner.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-TripPlanner" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
			WSDLData data = new WSDLData("Traffic Information", FileUtils
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
			
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_03 > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_04() {
		
		String cdParticipantName = "ND";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdND.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "ND";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
				"Set Eco Speed Route Information"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
					+File.separatorChar + "cdND.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
					+File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("SEADA-SEARP")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "cdSEADA-SEARP.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-SEATSA")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "cdSEADA-SEATSA.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-TRAFFIC")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "cdSEADA-TRAFFIC.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
			
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
						
	        CD cd = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_04 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_05() {
		
		String cdParticipantName = "SEADA-SEARP";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEARP.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;	
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "ND";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
				"Set Eco Speed Route Information"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
					+File.separatorChar + "cdND.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
					+File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("SEADA-SEARP")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "cdSEADA-SEARP.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-SEATSA")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "cdSEADA-SEATSA.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-TRAFFIC")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "cdSEADA-TRAFFIC.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
			WSDLData data = new WSDLData("DTS-GOOGLE", FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"DTS-GOOGLE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-HERE", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "DTS-HERE.wsdl")));
	        wsdlData.add(data);
			
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_05 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_06() {
		
		String cdParticipantName = "SEADA-SEATSA";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEATSA.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "ND";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
				"Set Eco Speed Route Information"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
					+File.separatorChar + "cdND.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
					+File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("SEADA-SEARP")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "cdSEADA-SEARP.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-SEATSA")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "cdSEADA-SEATSA.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-TRAFFIC")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "cdSEADA-TRAFFIC.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_06 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_07() {
		
		String cdParticipantName = "SEADA-TRAFFIC";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
		+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
		+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;	
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") 
				+ File.separatorChar + cdName + TARGZ_EXTENSION;		

		String cdClientParticipantName = "ND";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("ND");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("SEADA-SEARP");
        prosumersParticipantsNames.add("SEADA-SEATSA");
        prosumersParticipantsNames.add("SEADA-TRAFFIC");
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
		tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
				"Set Eco Speed Route Information"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
			+File.separatorChar + "cdND.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar + "CD-ND" 
			+File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("SEADA-SEARP")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "cdSEADA-SEARP.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEARP" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-SEATSA")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "cdSEADA-SEATSA.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-SEATSA" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("SEADA-TRAFFIC")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "cdSEADA-TRAFFIC.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+ "CD-SEADA-TRAFFIC" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
			WSDLData data = new WSDLData("DTS-ACCIDENTS", FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"bcDTS-ACCIDENTS.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("DTS-BRIDGE", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"bcDTS-BRIDGE.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-CONGESTION", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"bcDTS-CONGESTION.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("DTS-WEATHER", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"bcDTS-WEATHER.wsdl")));
	        wsdlData.add(data);	  
			
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_07 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_08() {
		
		String cdParticipantName = "Customer";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_GettingStarted_CD_Customer_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_Customer_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdCustomer.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_Customer_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String targz = new File(TEST_RESOURCES + "test_GettingStarted_CD_Customer_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "Customer";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("Customer");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();  
        prosumersParticipantsNames.add("Order Processor");  
        prosumersParticipantsNames.add("Shipper"); 
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Order Product","Notify Order Information"));	
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar
					+ "CD-Customer" + File.separatorChar + "cdCustomer.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
					+ "CD-Customer" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray
					(new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Order Processor")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-OrderProcessor" +File.separatorChar + "cdOrderProcessor.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-OrderProcessor" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Shipper")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-Shipper" +File.separatorChar + "cdShipper.bpmn2").getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-Shipper" +File.separatorChar + "types.xsd").getAbsolutePath();
				}				
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
						
	        CD cd = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_08 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_09() {
		
		String cdParticipantName = "Order Processor";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES 
				+ "test_GettingStarted_CD_OrderProcessor_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_OrderProcessor_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "cdOrderProcessor.bpmn2")
				.getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_OrderProcessor_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_GettingStarted_CD_OrderProcessor_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;	
		String targz = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_OrderProcessor_CHOReVOLUTION_STUDIO").getAbsolutePath()
				.replace(File.separatorChar+".","")+File.separatorChar+cdName+TARGZ_EXTENSION;		

		String cdClientParticipantName = "Customer";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("Customer");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("Order Processor");  
        prosumersParticipantsNames.add("Shipper"); 
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Order Product","Notify Order Information"));		
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
					+ "CD-Customer" + File.separatorChar + "cdCustomer.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
					+ "CD-Customer" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Order Processor")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-OrderProcessor" +File.separatorChar + "cdOrderProcessor.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-OrderProcessor" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Shipper")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-Shipper" +File.separatorChar + "cdShipper.bpmn2").getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-Shipper" +File.separatorChar + "types.xsd").getAbsolutePath();
				}					
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
			WSDLData data = new WSDLData("Invoicer", FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"Invoicer.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("Payment System", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"PaymentSystem.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Scheduler", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"Scheduler.wsdl")));
	        wsdlData.add(data);
	  			
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_09 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void cdGenerator_CHOReVOLUTION_STUDIO_10() {
		
		String cdParticipantName = "Shipper";		
		String cdName = Utils.createCDname(cdParticipantName);
		String choreographiesFolder = TEST_RESOURCES + "test_GettingStarted_CD_Shipper_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHIES_FOLDER_NAME;
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_Shipper_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "cdShipper.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_GettingStarted_CD_Shipper_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_GettingStarted_CD_Shipper_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;	
		String targz = new File(TEST_RESOURCES + "test_GettingStarted_CD_Shipper_CHOReVOLUTION_STUDIO")
				.getAbsolutePath().replace(File.separatorChar+".","") + File.separatorChar + cdName 
				+ TARGZ_EXTENSION;		

		String cdClientParticipantName = "Customer";
		
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("Customer");		
		
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("Order Processor");  
        prosumersParticipantsNames.add("Shipper"); 
		
		List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
		tasksCorrelationData.add(new TaskCorrelationData("Order Product","Notify Order Information"));		
		
		CDGenerator cdGenerator = new CDGeneratorImpl();
        List<WSDLData> wsdlData = new ArrayList<>();
        
		try {
			
			String clientBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
					+ "CD-Customer" + File.separatorChar + "cdCustomer.bpmn2").getAbsolutePath();
			String clientTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
					+ "CD-Customer" + File.separatorChar + "types.xsd").getAbsolutePath();			
			ProjectionData clientProjectionData = new ProjectionData();
			clientProjectionData.setBpmn(FileUtils.readFileToByteArray(
					new File(clientBpmnFileAbsolutePath)));
			clientProjectionData.setTypes(FileUtils.readFileToByteArray(
					new File(clientTypesFileAbsolutePath)));
			clientProjectionData.setParticipantName(cdClientParticipantName);
			clientProjectionData.setCdName(Utils.createCDname(cdClientParticipantName));			
			
			byte[] cdClientWSDL = cdGenerator.generateCDClientWSDL(clientProjectionData,
					tasksCorrelationData);
			wsdlData.add(new WSDLData(cdClientParticipantName, cdClientWSDL));
			
			for (String prosumerParticipantName : prosumersParticipantsNames) {
				ProjectionData participantProjectionData = new ProjectionData();
				String participantBpmnFileAbsolutePath = null;
				String participantTypesFileAbsolutePath = null;
				if(prosumerParticipantName.equals("Order Processor")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-OrderProcessor" +File.separatorChar + "cdOrderProcessor.bpmn2")
							.getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-OrderProcessor" +File.separatorChar + "types.xsd").getAbsolutePath();
				}
				if(prosumerParticipantName.equals("Shipper")){
					participantBpmnFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-Shipper" +File.separatorChar + "cdShipper.bpmn2").getAbsolutePath();
					participantTypesFileAbsolutePath = new File(choreographiesFolder+ File.separatorChar 
							+"CD-Shipper" +File.separatorChar + "types.xsd").getAbsolutePath();
				}					
				participantProjectionData.setBpmn(FileUtils
						.readFileToByteArray(new File(participantBpmnFileAbsolutePath)));
				participantProjectionData.setTypes(FileUtils
						.readFileToByteArray(new File(participantTypesFileAbsolutePath)));
				participantProjectionData.setParticipantName(prosumerParticipantName);
				participantProjectionData.setCdName(Utils.createCDname(prosumerParticipantName));			
				byte [] participantWSDL = cdGenerator.generateCDWSDL(participantProjectionData);
				wsdlData.add(new WSDLData(prosumerParticipantName, participantWSDL));			
			}
	        
			ProjectionData projectionData = new ProjectionData();
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			projectionData.setParticipantName(cdParticipantName);
			projectionData.setCdName(cdName);		
	
			WSDLData data = new WSDLData("Carrier", FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"Carrier.wsdl")));
	        wsdlData.add(data);			
	  			
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
			
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());
	        	        
		} catch (IOException e) {
			LOGGER.error("cdGenerator_CHOReVOLUTION_STUDIO_10 > ",e);
			Assert.assertTrue(false);
		}
	}	
}
