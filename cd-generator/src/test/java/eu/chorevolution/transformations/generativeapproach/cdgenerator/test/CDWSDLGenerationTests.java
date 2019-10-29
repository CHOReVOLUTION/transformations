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

import javax.wsdl.Definition;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPMNUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.CDGenerationUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;

public class CDWSDLGenerationTests {

	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" 
			+ File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDL_EXTENSION = ".wsdl";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDWSDLGenerationTests.class);

	@Before
	public void setUp() {
	}

	
	@Test
	public void generateCDWSDL_01() {

		String prosumerName = "B";
		String bpmnIn = TEST_RESOURCES + "test_01" + File.separatorChar +CHOREOGRAPHY_FOLDER_NAME 
				+ File.separatorChar + "scenario_1.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_01" + File.separatorChar+Utils.createCDname(prosumerName);
		String prosumerWSDLOut = cdFolderPath + File.separatorChar +Utils.createCDname(prosumerName) 
			+ WSDL_EXTENSION;		
	
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));	
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
	        DOMSource source = new DOMSource(prosumerWSDL);
	        StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
	        transformer.transform(source, sr);   				
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_01 > ",e);
			Assert.assertTrue(false);
		}
       			

	}
	
	@Test
	public void generateCD_02() {
			
		String prosumerName = "B";
		String bpmnIn = TEST_RESOURCES + "test_02" + File.separatorChar + CHOREOGRAPHY_FOLDER_NAME 
				+ File.separatorChar + "scenario_2.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_02" +File.separatorChar+Utils
				.createCDname(prosumerName);
		String prosumerWSDLOut = cdFolderPath + File.separatorChar +Utils.createCDname(prosumerName)
			+ WSDL_EXTENSION;	
	   
		try { 
			FileUtils.forceMkdir(new File(cdFolderPath));
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);			
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
	        DOMSource source = new DOMSource(prosumerWSDL);
	        StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
			transformer.transform(source, sr);
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_02 > ",e);
			Assert.assertTrue(false);
		}

	}
	
	@Test
	public void generateCD_03() {
		
		String prosumerName = "Tourist Agent";
		String cdName = "cdTouristAgent";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-TouristAgent" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-TouristAgent.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-TouristAgent" + File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		

		try {
			FileUtils.forceMkdir(new File(cdFolderPath));			
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);	
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);           
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_03 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_04() {
		
		String prosumerName = "Trip Planner";
		String cdName = "cdTripPlanner";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-TripPlanner" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-TripPlanner.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-TripPlanner" + File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		
	
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));	
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);			
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);        
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_04 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_05() {
		
		String prosumerName = "STApp";
		String cdName = "cdSTApp";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar +CHOREOGRAPHY_FOLDER_NAME
				+ File.separatorChar + "CD-STApp.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar + cdName ;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		
	
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));			
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Trip Plan","Set Trip Plan"));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDClientWSDL(choreographyData, 
					choreographytns, prosumerName,tasksCorrelationData);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);        
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCD_05 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_05_NoCorrelationTasks() {
		
		String prosumerName = "STApp";
		String cdName = "cdSTApp";
		String bpmnIn = TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar +CHOREOGRAPHY_FOLDER_NAME
				+ File.separatorChar + "CD-STApp.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar + cdName ;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		
	
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			// tasksCorrelationData can be null or an empty object			
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			//tasksCorrelationData = null;
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDClientWSDL(choreographyData, 
					choreographytns, prosumerName, tasksCorrelationData);	
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);        
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCD_05_NoCorrelationTasks > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCD_06() {
		
		String prosumerName = "ND";
		String cdName = "cdND";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_ND" + File.separatorChar 
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-ND.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_ND" + File.separatorChar + cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		
	
		try {
			FileUtils.forceMkdir(new File(cdFolderPath));		
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));			
			List<TaskCorrelationData> tasksCorrelationData = new ArrayList<>();
			tasksCorrelationData.add(new TaskCorrelationData("Get Eco Routes","Set Eco Routes"));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDClientWSDL(choreographyData,
					choreographytns, prosumerName,tasksCorrelationData);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);        
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCD_06 > ",e);
			Assert.assertTrue(false);
		}
	}		
	
	@Test
	public void generateCD_07() {
		
		String prosumerName = "SEADA-SEARP";
		String cdName = "cdSEADA-SEARP";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP" + File.separatorChar
				+ CHOREOGRAPHY_FOLDER_NAME + File.separatorChar + "CD-SEADA-SEARP.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEARP" + File.separatorChar
				+ cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		

		try {
			FileUtils.forceMkdir(new File(cdFolderPath));			
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);	
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);           
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_07 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCD_8() {
		
		String prosumerName = "SEADA-SEATSA";
		String cdName = "cdSEADA-SEATSA";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-SEADA-SEATSA.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-SEATSA" + File.separatorChar
				+ cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		

		try {
			FileUtils.forceMkdir(new File(cdFolderPath));			
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);	
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);           
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_8 > ",e);
			Assert.assertTrue(false);
		}
	}	

	@Test
	public void generateCD_9() {
		
		String prosumerName = "SEADA-TRAFFIC";
		String cdName = "cdSEADA-TRAFFIC";
		String bpmnIn = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-SEADA-TRAFFIC.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_WP4_SEADA_CD_SEADA-TRAFFIC" + File.separatorChar
				+ cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		

		try {
			FileUtils.forceMkdir(new File(cdFolderPath));			
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);	
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);           
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_9 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	@Test
	public void generateCD_10() {
		
		String prosumerName = "B";
		String cdName = "cdB";
		String bpmnIn = TEST_RESOURCES + "test_loop-chor_task_num_exp" + File.separatorChar 
				+ "choreography-b" + File.separatorChar + "loop-chor_task.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_loop-chor_task_num_exp" + File.separatorChar
				+ cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		

		try {
			FileUtils.forceMkdir(new File(cdFolderPath));			
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);	
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);           
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_10 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCD_11() {
		
		String prosumerName = "B";
		String cdName = "cdB";
		String bpmnIn = TEST_RESOURCES + "test_loop-subchor_num_exp" + File.separatorChar 
				+ "choreography-b" + File.separatorChar + "loop-subchor.bpmn2";
		String cdFolderPath = TEST_RESOURCES + "test_loop-subchor_num_exp" + File.separatorChar
				+ cdName;
		String prosumerWSDLOut = cdFolderPath + File.separatorChar + cdName + WSDL_EXTENSION;		

		try {
			FileUtils.forceMkdir(new File(cdFolderPath));			
			ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnIn));
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);	
			Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData, 
					choreographytns, prosumerName);
			Document prosumerWSDL = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
					.getDocument(prosumerWSDLdefinition);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
            DOMSource source = new DOMSource(prosumerWSDL);
            StreamResult sr = new StreamResult(new File(prosumerWSDLOut));
            transformer.transform(source, sr);           
		} catch (TransformerException | IOException | javax.wsdl.WSDLException e) {
			LOGGER.error("generateCDWSDL_11 > ",e);
			Assert.assertTrue(false);
		}
	}	
	
	
}
