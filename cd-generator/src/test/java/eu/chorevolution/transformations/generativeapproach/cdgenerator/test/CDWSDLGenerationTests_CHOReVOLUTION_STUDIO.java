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
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ProjectionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;

public class CDWSDLGenerationTests_CHOReVOLUTION_STUDIO {

	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar 
			+ "test" + File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDL_EXTENSION = ".wsdl";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDWSDLGenerationTests.class);
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void generateCDWSDL_01() {
		
		String prosumerName = "Tourist Agent";
		String cdName = "cdTouristAgent";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTouristAgent.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
					+ "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				    + File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_01 > ",e);
			Assert.assertTrue(false);
		}
	
	}
	
	@Test
	public void generateCDWSDL_02() {
		
		String prosumerName = "Trip Planner";
		String cdName = "cdTripPlanner";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdTripPlanner.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-TripPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_02 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_03() {
		
		String prosumerName = "STApp";
		String cdName = "cdSTApp";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_03 > ",e);
			Assert.assertTrue(false);
		}
	
	}	
	

	@Test
	public void generateCDWSDL_03_NoCorrelationTasks() {
		
		String prosumerName = "STApp";
		String cdName = "cdSTApp";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			// tasksCorrelationData can be null or an empty object
			//List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			List<TaskCorrelationData> tasksCorrelationData = null;
			//tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_03_NoCorrelationTasks > ",e);
			Assert.assertTrue(false);
		}
	
	}		
	
	@Test
	public void generateCDWSDL_04() {
		
		String prosumerName = "ND";
		String cdName = "cdND";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdND.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut), cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_04 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_05() {
		
		String prosumerName = "SEADA-SEARP";
		String cdName = "cdSEADA-SEARP";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEARP.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_05 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDWSDL_06() {
		
		String prosumerName = "SEADA-SEATSA";
		String cdName = "cdSEADA-SEATSA";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEATSA.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_06 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_07() {
		
		String prosumerName = "SEADA-TRAFFIC";
		String cdName = "cdSEADA-TRAFFIC";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-TRAFFIC.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_07 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_08() {
		
		String prosumerName = "Mobility Information Planner";
		String cdName = "cdMobilityInformationPlanner";
		String bpmnIn = TEST_RESOURCES + "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdMobilityInformationPlanner.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_08 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	
	@Test
	public void generateCDWSDL_09() {
		
		String prosumerName = "STApp";
		String cdName = "cdSTApp";
		String bpmnIn = TEST_RESOURCES + "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSTApp.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Tourist Guide","Set Tourist Guide"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut), cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_09 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_10() {
		
		String prosumerName = "Tourism Information Planner";
		String cdName = "cdTourismInformationPlanner";
		String bpmnIn = TEST_RESOURCES + "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTourismInformationPlanner.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TourismInformationPlanner_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_10 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_11() {
		
		String prosumerName = "Tourist Agent";
		String cdName = "cdTouristAgent";
		String bpmnIn = TEST_RESOURCES + "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdTouristAgent.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP5_V2_CD-TouristAgent_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_11 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_12() {
		
		String prosumerName = "STApp";
		String cdName = "cdSTApp";
		String bpmnIn = TEST_RESOURCES 
				+ "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "cdSTApp.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP5_V2_CD-STApp_CHOReVOLUTION_STUDIO-loop-client-chor_task_num_exp" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Tourist Guide","Set Tourist Guide"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut), cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_12 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDWSDL_13() {
		
		String prosumerName = "Mobility Information Planner";
		String cdName = "cdMobilityInformationPlanner";
		String bpmnIn = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdMobilityInformationPlanner.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP5_V2_CD-MobilityInformationPlanner_CHOReVOLUTION_STUDIO-loop-chor_task_num_exp" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_13 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDWSDL_14() {
		
		String prosumerName = "ND";
		String cdName = "cdND";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "cdND.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_ND_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar +cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut), cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_14 > ",e);
			Assert.assertTrue(false);
		}
	}	
		
	@Test
	public void generateCDWSDL_15() {
		
		String prosumerName = "SEADA-SEARP";
		String cdName = "cdSEADA-SEARP";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEARP.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_15 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCDWSDL_16() {
		
		String prosumerName = "SEADA-SEATSA";
		String cdName = "cdSEADA-SEATSA";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "cdSEADA-SEATSA.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_16 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCDWSDL_17() {
		
		String prosumerName = "SEADA-TRAFFIC";
		String cdName = "cdSEADA-TRAFFIC";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-TRAFFIC.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP4_SEADA_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO_V2_loop" + File.separatorChar 
				+ cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_17 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCDWSDL_18() {
		
		String prosumerName = "SEADA-TRAFFIC";
		String cdName = "cdSEADA-TRAFFIC";
		String bpmnIn = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-TRAFFIC.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_SEADA-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
		
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_18 > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCDWSDL_19() {
		
		String prosumerName = "DTS-SEGMENT-TRAFFIC";
		String cdName = "cdDTS-SEGMENT-TRAFFIC";
		String bpmnIn = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdDTS-SEGMENT-TRAFFIC.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP4_TrafficAreaInformationCollection_CD_DTS-SEGMENT-TRAFFIC_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;	
		
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_19 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDWSDL_20() {
		
		String prosumerName = "SEADA-SEARP";
		String cdName = "cdSEADA-SEARP";
		String bpmnIn = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-SEARP.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEARP_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;	
		
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_20 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDWSDL_21() {
		
		String prosumerName = "ND";
		String cdName = "cdND";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "cdND.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_V2_CD_ND_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;		
			
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));	
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Speed Route Information"
					,"Set Eco Speed Route Information"));
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut), cdGenerator
	        		.generateCDClientWSDL(projectionData, tasksCorrelationData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_21 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDWSDL_22() {
		
		String prosumerName = "SEADA-SEATSA";
		String cdName = "cdSEADA-SEATSA";
		String bpmnIn = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "cdSEADA-SEATSA.bpmn2";
		String typesFileAbsolutePath = new File(TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "types.xsd")
				.getAbsolutePath();
		String cdFolderPath = TEST_RESOURCES 
				+ "test_WP4_SEADA_V2_CD_SEADA-SEATSA_CHOReVOLUTION_STUDIO" 
				+ File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + File.separatorChar + cdName 
				+ WSDL_EXTENSION;	
		
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));						
			ProjectionData projectionData = new ProjectionData();
			projectionData.setParticipantName(prosumerName);
			projectionData.setCdName(Utils.createCDname(prosumerName));
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnIn)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
	        CDGenerator cdGenerator = new CDGeneratorImpl();
	        FileUtils.writeByteArrayToFile(new File(prosumerWSDLOut),cdGenerator
	        		.generateCDWSDL(projectionData));
		} catch (IOException e) {
			LOGGER.error("generateCDWSDL_CHOReVOLUTIONStudio_22 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
}
