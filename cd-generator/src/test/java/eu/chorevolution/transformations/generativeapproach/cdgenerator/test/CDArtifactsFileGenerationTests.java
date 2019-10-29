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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
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
import org.xml.sax.InputSource;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ProjectionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLDefinitionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPMNUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.CDGenerationUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.WSDLUtils;

public class CDArtifactsFileGenerationTests {

	private static final String ARTIFACTS_FILE_SUFFIX = "Artifacts.wsdl";
	private static final String CHOREOGRAPHY_FOLDER_NAME = "choreography";
	private static final String CD_PREFIX = "cd";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar 
			+ "test" + File.separatorChar + "resources" + File.separatorChar ;
	private static final String WSDLS_FOLDER_NAME = "wsdls";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDArtifactsFileGenerationTests.class);
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void generateCDArtifactsFile_01() {
		
		String cdParticipantName = "STApp";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "CD-STApp.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-STApp" + File.separatorChar 
				+CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar + "types.xsd").getAbsolutePath();			
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-STApp" + File.separatorChar +WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String artifactsWSDLOut = wsdlsFolder + File.separatorChar +CD_PREFIX+WSDLUtils
				.formatParticipantNameForWSDL(cdParticipantName)+ARTIFACTS_FILE_SUFFIX;
				
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));			
		try {			
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			
    		WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    		HashMap<String, WSDLDefinitionData> wsdlDefinitions = new HashMap<>();
			// parse CD-TOURISTAGENT WSDL file
			Definition definitionwsdl = wsdlReader.readWSDL(null,
					new InputSource(new ByteArrayInputStream(FileUtils
							.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
									+ "cdTouristAgent.wsdl")))));
			WSDLDefinitionData wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("touristagent");
			wsdlDefinitionData.setWsdlFileName("cdtouristagent");
			wsdlDefinitionData.setWsdl(definitionwsdl);			
			wsdlDefinitions.put("Tourist Agent", wsdlDefinitionData);				
			// parse CD-STApp WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdSTApp.wsdl")))));
			WSDLDefinitionData cdWSDLDefinitionData = new WSDLDefinitionData();
			cdWSDLDefinitionData.setPrefix("stapp");
			cdWSDLDefinitionData.setWsdlFileName("cdstapp");				
			cdWSDLDefinitionData.setWsdl(definitionwsdl);
			
    		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
    		
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns,
					projectionData.getCdName(), projectionData.getParticipantName(), choreographyData,
					wsdlDefinitions, cdWSDLDefinitionData);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
            
            DOMSource source = new DOMSource(artifactsWSDL);
            StreamResult sr = new StreamResult(new File(artifactsWSDLOut));
            transformer.transform(source, sr);
		
		} catch (CDGeneratorException | TransformerException | IOException | WSDLException e) {
			LOGGER.error("CDArtifactsFile_01 > ",e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateCDArtifactsFile_02() {
		
		String cdParticipantName = "Tourist Agent";
		String bpmnFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent" 
				+ File.separatorChar +CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "CD-TouristAgent.bpmn2").getAbsolutePath();
		String typesFileAbsolutePath = new File(TEST_RESOURCES + "test_WP5_CD-TouristAgent"  
				+ File.separatorChar + CHOREOGRAPHY_FOLDER_NAME+ File.separatorChar 
				+ "types.xsd").getAbsolutePath();
		String wsdlsFolder = TEST_RESOURCES+ "test_WP5_CD-TouristAgent" + File.separatorChar 
				+ WSDLS_FOLDER_NAME; 
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar+".","") + File.separatorChar;
		String artifactsWSDLOut = wsdlsFolder + File.separatorChar +CD_PREFIX+WSDLUtils
				.formatParticipantNameForWSDL(cdParticipantName)+ARTIFACTS_FILE_SUFFIX;	

		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(cdParticipantName);
		projectionData.setCdName(Utils.createCDname(cdParticipantName));			
		try {				
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));		
			
    		WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    		HashMap<String, WSDLDefinitionData> wsdlDefinitions = new HashMap<>();
			// parse CD-TRIPPLANNER WSDL file
			Definition definitionwsdl = wsdlReader.readWSDL(null, 
					new InputSource(new ByteArrayInputStream(FileUtils
							.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTripPlanner.wsdl")))));
			WSDLDefinitionData wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("tripplanner");
			wsdlDefinitionData.setWsdlFileName("cdtripplanner");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Trip Planner", wsdlDefinitionData);				
			// parse NEWS WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"news.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("news");
			wsdlDefinitionData.setWsdlFileName("news");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("News", wsdlDefinitionData);
			// parse POI WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"poi.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("poi");
			wsdlDefinitionData.setWsdlFileName("poi");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Poi", wsdlDefinitionData);	
			// parse TOURIST AGENT WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"TouristAgent.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("touristagent");
			wsdlDefinitionData.setWsdlFileName("touristagent");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Tourist Agent", wsdlDefinitionData);					
			// parse CD-TOURISTAGENT WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTouristAgent.wsdl")))));
			WSDLDefinitionData cdWSDLDefinitionData = new WSDLDefinitionData();
			cdWSDLDefinitionData.setPrefix("touristagent");
			cdWSDLDefinitionData.setWsdlFileName("cdtouristagent");
			cdWSDLDefinitionData.setWsdl(definitionwsdl);				
						
    		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
    		
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, 
					projectionData.getCdName(), projectionData.getParticipantName(), choreographyData,
					wsdlDefinitions, cdWSDLDefinitionData);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
            
            DOMSource source = new DOMSource(artifactsWSDL);
            StreamResult sr = new StreamResult(new File(artifactsWSDLOut));
            transformer.transform(source, sr);
		
		} catch (CDGeneratorException | TransformerException | IOException | WSDLException e) {
			LOGGER.error("CDArtifactsFile_02 > ",e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCDArtifactsFile_03() {
		
		String prosumerName = "Trip Planner";
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
		String artifactsWSDLOut = wsdlsFolder + File.separatorChar +CD_PREFIX+WSDLUtils
				.formatParticipantNameForWSDL(prosumerName)+ARTIFACTS_FILE_SUFFIX;		
		
		ProjectionData projectionData = new ProjectionData();
		projectionData.setParticipantName(prosumerName);
		projectionData.setCdName(Utils.createCDname(prosumerName));		
		try {			
			projectionData.setBpmn(FileUtils.readFileToByteArray(new File(bpmnFileAbsolutePath)));
			projectionData.setTypes(FileUtils.readFileToByteArray(new File(typesFileAbsolutePath)));
			
    		WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    		HashMap<String, WSDLDefinitionData> wsdlDefinitions = new HashMap<>();
			// parse JOURNEY PLANNER WSDL file
			Definition definitionwsdl = wsdlReader.readWSDL(null, new InputSource(
					new ByteArrayInputStream(FileUtils.readFileToByteArray(
							new File(wsdlsFolderAbsolutePath+"journeyplanner.wsdl")))));
			WSDLDefinitionData wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("journeyplanner");
			wsdlDefinitionData.setWsdlFileName("journeyplanner");
			wsdlDefinitionData.setWsdl(definitionwsdl);			
			wsdlDefinitions.put("Journey Planner", wsdlDefinitionData);
			// parse OSMPARKING WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"osmparking.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("osmparking");
			wsdlDefinitionData.setWsdlFileName("osmparking");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("OSM Parking", wsdlDefinitionData);			
			// parse PUBLIC TRANSPORTATION WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"publictransportation.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("publictransportation");
			wsdlDefinitionData.setWsdlFileName("publictransportation");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Public Transportation", wsdlDefinitionData);	
			// parse TRAFFIC INFORMATION WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"trafficinformation.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("trafficinformation");
			wsdlDefinitionData.setWsdlFileName("trafficinformation");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Traffic Information", wsdlDefinitionData);
			// parse WEATHER WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"weather.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("weather");
			wsdlDefinitionData.setWsdlFileName("weather");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Weather", wsdlDefinitionData);	
			// parse TRIP PLANNER WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"TripPlanner.wsdl")))));
			wsdlDefinitionData = new WSDLDefinitionData();
			wsdlDefinitionData.setPrefix("tripplanner");
			wsdlDefinitionData.setWsdlFileName("tripplanner");
			wsdlDefinitionData.setWsdl(definitionwsdl);	
			wsdlDefinitions.put("Trip Planner", wsdlDefinitionData);					
			// parse CD-TRIPPLANNER WSDL file
			definitionwsdl = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(FileUtils
					.readFileToByteArray(new File(wsdlsFolderAbsolutePath+"cdTripPlanner.wsdl")))));
			WSDLDefinitionData cdWSDLDefinitionData = new WSDLDefinitionData();
			cdWSDLDefinitionData.setPrefix("tripplanner");
			cdWSDLDefinitionData.setWsdlFileName("cdtripplanner");
			cdWSDLDefinitionData.setWsdl(definitionwsdl);				

    		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
			String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
			
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns,
					projectionData.getCdName(), projectionData.getParticipantName(), choreographyData,
					wsdlDefinitions, cdWSDLDefinitionData);
						
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
            
            DOMSource source = new DOMSource(artifactsWSDL);
            StreamResult sr = new StreamResult(new File(artifactsWSDLOut));
            transformer.transform(source, sr);
            
		} catch (CDGeneratorException | TransformerException | IOException | WSDLException e) {
			LOGGER.error("CDArtifactsFile_03 > ",e);
			Assert.assertTrue(false);
		}
	}
	
}
