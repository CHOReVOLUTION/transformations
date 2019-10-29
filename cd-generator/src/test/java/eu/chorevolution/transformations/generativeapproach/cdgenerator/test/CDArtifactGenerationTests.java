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
import org.junit.Before;
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

public class CDArtifactGenerationTests {
	
	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String TARGZ_EXTENSION = ".tar.gz";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar 
			+ "test" + File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDLS_FOLDER_NAME = "wsdls";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDArtifactGenerationTests.class);
	
	@Before
	public void setUp() {
	}

	
	@Test
	public void generateCDArtifact_01() {

		String cdClientParticipantName = "STApp";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-STApp.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-STApp").getAbsolutePath().replace(File
				.separatorChar+".","")+File.separatorChar+Utils.createCDname(cdClientParticipantName) 
				+ TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdClientParticipantName);
		projectionData.setCdName(Utils.createCDname(cdClientParticipantName));
		
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;        
		try {
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdtouristagent.wsdl")));
	        wsdlData.add(data);
	        
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
        
	        CD cdClient = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cdClient.getArtifact());
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_01 > ",e);
			Assert.assertTrue(false);
		}	
	}
	
	@Test
	public void generateCDArtifact_01_NoCorrelationTasks() {

		String cdClientParticipantName = "STApp";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-STApp.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-STApp").getAbsolutePath().replace(File
				.separatorChar+".","")+File.separatorChar+Utils.createCDname(cdClientParticipantName)
				+ TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdClientParticipantName);
		projectionData.setCdName(Utils.createCDname(cdClientParticipantName));
		
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;        
		try {
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdtouristagent.wsdl")));
	        wsdlData.add(data);
			// tasksCorrelationData can be null or an empty object       
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData = null;
			//tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
        
	        CD cdClient = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cdClient.getArtifact());
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_01_NoCorrelationTasks > ",e);
			Assert.assertTrue(false);
		}	
	}	
	
	@Test
	public void generateCDArtifact_02() {
		
		String cdParticipantName = "Tourist Agent";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME 
				+ File.separatorChar + "CD-TouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME 
				+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TouristAgent" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;		
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent").getAbsolutePath().replace(File
				.separatorChar+".","")+File.separatorChar+Utils.createCDname(cdParticipantName) 
				+ TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));
		
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");          
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner");       
		try {
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
	        data = new WSDLData("Poi", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "poi.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Trip Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdTripPlanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("News", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
	        		+ "news.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("STApp", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"cdSTApp.wsdl")));
	        wsdlData.add(data);
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());	
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_02 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDArtifact_03() {

		String cdParticipantName = "Trip Planner";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TripPlanner" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-TripPlanner.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TripPlanner" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TripPlanner" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String targz = new File(TEST_RESOURCES + "test_WP5_CD-TripPlanner").getAbsolutePath().replace(File
				.separatorChar+".","")+File.separatorChar+Utils.createCDname(cdParticipantName) 
				+ TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));
		
		List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;
        List<String> clientProsumersParticipantsNames = new ArrayList<>();
        clientProsumersParticipantsNames.add("STApp");
        List<String> prosumersParticipantsNames = new ArrayList<>();
        prosumersParticipantsNames.add("STApp");          
        prosumersParticipantsNames.add("Tourist Agent");  
        prosumersParticipantsNames.add("Trip Planner");          
		try {
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			
	        data = new WSDLData("Traffic Information", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "TrafficInformation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Journey Planner", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "JourneyPlanner.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("OSM Parking", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "OSMParking.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Public Transportation", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "PublicTransportation.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("Weather", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+ "Weather.wsdl")));
	        wsdlData.add(data); 
	        data = new WSDLData("Tourist Agent", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdTouristAgent.wsdl")));
	        wsdlData.add(data);
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());	
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_03 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCDArtifact_04() {

		String cdClientParticipantName = "ND";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-ND.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_ND" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND").getAbsolutePath().replace(File
				.separatorChar+".","")+File.separatorChar+Utils.createCDname(cdClientParticipantName)
				+ TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdClientParticipantName);
		projectionData.setCdName(Utils.createCDname(cdClientParticipantName));
		
        List<WSDLData> wsdlData = new ArrayList<>();
        WSDLData data;        
		try {
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-SEATSA", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSEADA-SEATSA.wsdl")));
	        wsdlData.add(data);
	        
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information", 
					"Set Eco Speed Route Information"));	
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdClientParticipantName, cdGenerator.generateCDClientWSDL(projectionData, 
	        		tasksCorrelationData));
	        wsdlData.add(data);
        
	        CD cdClient = cdGenerator.generateCDClient(tasksCorrelationData, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cdClient.getArtifact());
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_04 > ",e);
			Assert.assertTrue(false);
		}	
	}	

	@Test
	public void generateCDArtifact_05() {

		String cdParticipantName = "SEADA-SEARP";
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
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP").getAbsolutePath()
				.replace(File.separatorChar+".","")+File.separatorChar 
				+ Utils.createCDname(cdParticipantName) + TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));
		
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
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			
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
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());	
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_05 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCDArtifact_06() {

		String cdParticipantName = "SEADA-SEATSA";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA" 
				+ File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-SEADA-SEATSA.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA" 
				+ File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-SEATSA" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA").getAbsolutePath()
				.replace(File.separatorChar+".","") + File.separatorChar+Utils
				.createCDname(cdParticipantName) + TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));
		
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
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			
	        data = new WSDLData("ND", FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath
	        		+"cdND.wsdl")));
	        wsdlData.add(data);			
	        data = new WSDLData("SEADA-SEARP", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-SEARP.wsdl")));
	        wsdlData.add(data);
	        data = new WSDLData("SEADA-TRAFFIC", FileUtils
	        		.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "cdSEADA-TRAFFIC.wsdl")));
	        wsdlData.add(data);
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());	
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_06 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDArtifact_07() {

		String cdParticipantName = "SEADA-TRAFFIC";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC" 
				+ File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-SEADA-TRAFFIC.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC" 
				+ File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP4_SEADA_CD_SEADA-TRAFFIC" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String targz = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC").getAbsolutePath()
				.replace(File.separatorChar+".","")+File.separatorChar+Utils
				.createCDname(cdParticipantName)+TARGZ_EXTENSION;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));
		
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
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			
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
	        
			// create CD WSDL and add it to wsdlData
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        data = new WSDLData(cdParticipantName, cdGenerator.generateCDWSDL(projectionData));
	        wsdlData.add(data);
	        CD cd = cdGenerator.generateCDProsumer(prosumersParticipantsNames,
	        		clientProsumersParticipantsNames, projectionData, wsdlData);
	        FileUtils.writeByteArrayToFile(new File(targz), cd.getArtifact());	
		} catch (IOException e) {
			LOGGER.error("generateCDArtifact_07 > ",e);
			Assert.assertTrue(false);
		}
	}	
}
