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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Process;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPMNUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.CDGenerationUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.WSDLUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELReader;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELResource;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELResourceFactoryImpl;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELWriter;

public class CDBPELGenerationTests {

	private static final String BPEL_CONTENT_FOLDER_NAME = "bpelContent";
	private static final String BPEL_FILE_EXTENSION = ".bpel";
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
			+ File.separatorChar + "resources" + File.separatorChar;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDBPELGenerationTests.class);

	@Before
	public void setUp() {
	}

	@Test
	public void generateCDBPEL_01() {

		String prosumerName = "B";
		String bpelContentPath = TEST_RESOURCES + "test_01" + File.separatorChar + BPEL_CONTENT_FOLDER_NAME;
		String bpelProcessOut = bpelContentPath + File.separatorChar + Utils.createCDname(prosumerName)
				+ BPEL_FILE_EXTENSION;

		try {
			FileUtils.forceMkdir(new File(bpelContentPath));
			BPELData bpelData = CDGenerationUtils.generateCDBPELprocess(null, null);
			File file = new File(bpelProcessOut);
			URI fileUriTempModelNormalized = URI.createFileURI(file.getAbsolutePath());
			Resource bpelResource = new BPELResourceFactoryImpl().createResource(fileUriTempModelNormalized);
			bpelResource.getContents().add((EObject) bpelData.getProcess());
			BPELWriter bpelWriter = new BPELWriter((BPELResource) bpelResource,
					DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			// BPELWriter bpelWriter = new BPELWriter((BPELResource)
			// bpelResource,null);
			// org.eclipse.bpel.model.resource.BPELWriter bpelWriter = new
			// org.eclipse.bpel.model.resource.BPELWriter((org.eclipse.bpel.model.resource.BPELResource)
			// bpelResource,null);
			// OutputStream out = new FileOutputStream(new
			// File(bpelProcessOut));
			File out = new File(bpelProcessOut);
			// Map<String,Boolean> args = new HashMap<String,Boolean>();
			// args.put("bpel.skip.auto.import",new Boolean(true));
			// bpelWriter.write((BPELResource) bpelResource, out, args);
			bpelWriter.write(bpelData, out);
			// bpelWriter.write((org.eclipse.bpel.model.resource.BPELResource)
			// bpelResource, out, args);
		} catch (IOException | ParserConfigurationException e) {
			LOGGER.error("generateCDBPEL_01 > ", e);
			Assert.assertTrue(false);
		}

		// File file = new File(bpelProcessOut);
		// // create resource from the Model
		// URI fileUriTempModelNormalized =
		// URI.createFileURI(file.getAbsolutePath());
		// Resource resourceModelNormalized = new
		// XMIResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		// // add model in model resourceModel
		// resourceModelNormalized.getContents().add((EObject) process);
		// try {
		// // save model
		// resourceModelNormalized.save(Collections.EMPTY_MAP);
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	@Test
	public void generateCDBPEL_01_2() {

		String BpelFilePath = TEST_RESOURCES + "test_01" + File.separatorChar 
				+ "bpel" + File.separatorChar + "cdND.bpel";       
		
        URI uri = URI.createFileURI(BpelFilePath);
        BPELResource bpelResource = (BPELResource) new BPELResourceFactoryImpl().createResource(uri);
		try {
			InputStream inputStream = new FileInputStream(new File(BpelFilePath));
			BPELReader bpelReader = new BPELReader();
			bpelReader.read(bpelResource, inputStream);
			bpelResource.getProcess();
		} catch (FileNotFoundException e) {
			LOGGER.error("generateCDBPEL_01_2 > ", e);
			Assert.assertTrue(false);
		}	
	}

	@Test
	public void generateCDBPEL_02() {

		String prosumerName = "Trip Planner";
		String bpmnPath = TEST_RESOURCES + File.separatorChar + "test_WP5_CD-TripPlanner" 
				+ File.separatorChar + "choreography" + File.separatorChar + "CD-TripPlanner.bpmn2";
		// String bpmnPath =
		// TEST_RESOURCES+File.separatorChar+"test_01"+File.separatorChar+"choreography"+File.separatorChar
		// +"scenario_1.bpmn2";
		String bpelContentPath = TEST_RESOURCES + "test_WP5_CD-TripPlanner" + File.separatorChar
				+ BPEL_CONTENT_FOLDER_NAME;
		String bpelProcessOut = bpelContentPath + File.separatorChar + Utils.createCDname(prosumerName)
				+ BPEL_FILE_EXTENSION;

		// try {
		// FileUtils.forceMkdir(new File(bpelContentPath));
		// } catch (IOException e1) {
		// logger.error("generateCDBPEL_02 > ",e1);
		// Assert.assertTrue(false);
		// }
		//
		// ChoreographyData choreographyData =
		// BPMNUtils.readChoreographyData(new File(bpmnPath));
		// //BPELData bpelData =
		// BPELUtils.visit2(choreographyData.getChoreography());
		// BPELData bpelData =
		// CDGenerationUtils.createBpelProcessFromChoreography(prosumerName,choreographyData
		//.getChoreography());
		// File file = new File(bpelProcessOut);
		// URI fileUriTempModelNormalized =
		// URI.createFileURI(file.getAbsolutePath());
		// Resource bpelResource = new
		// BPELResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		// bpelResource.getContents().add((EObject) bpelData.getProcess());
		// BPELWriter bpelWriter;
		// try {
		// bpelWriter = new BPELWriter((BPELResource)
		// bpelResource,DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
		// File out = new File(bpelProcessOut);
		// bpelWriter.write(bpelData,out);
		// } catch (ParserConfigurationException | IOException e) {
		// logger.error("generateCDBPEL_02 > ",e);
		// Assert.assertTrue(false);
		// }

	}

	@Test
	public void addImportsTest_01() {

		String prosumerName = "Trip Planner";
		String cdName = "cdtripplanner";
		String bpmnPath = TEST_RESOURCES + File.separatorChar + "test_WP5_CD-TripPlanner" 
				+ File.separatorChar + "choreography" + File.separatorChar + "CD-TripPlanner.bpmn2";
		String bpelContentPath = TEST_RESOURCES + "test_WP5_CD-TripPlanner" + File.separatorChar
				+ BPEL_CONTENT_FOLDER_NAME;
		String bpelProcessOut = bpelContentPath + File.separatorChar + Utils.createCDname(prosumerName)
				+ BPEL_FILE_EXTENSION;
		String wsdlsFolder = TEST_RESOURCES + "test_WP5_CD-TripPlanner" + File.separatorChar + "wsdls";
		String wsdlsFolderAbsolutePath = (new File(wsdlsFolder).getAbsolutePath())
				.replace(File.separatorChar + ".", "") + File.separatorChar;

		Process process = BPELFactory.eINSTANCE.createProcess();
		process.setName(cdName + ".bpel");

		try {
			FileUtils.forceMkdir(new File(bpelContentPath));
		} catch (IOException e1) {
			LOGGER.error("addImportsTest_01 > ", e1);
			Assert.assertTrue(false);
		}

		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(new File(bpmnPath));
		String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
		Definition cdWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData,
				choreographytns, prosumerName);

		List<WSDLData> wsdlData = new ArrayList<>();
		HashMap<String, Definition> providersWSDL = new HashMap<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			WSDLData data = new WSDLData("journeyplanner",
					FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
							+ "journeyplanner.wsdl")));
			wsdlData.add(data);
			// parse WSDL file
			WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
			Definition definition = wsdlReader.readWSDL(null,
					new InputSource(new ByteArrayInputStream(data.getWsdl())));
			providersWSDL.put("journeyplanner", definition);
			data = new WSDLData("osmparking",
					FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
							+ "osmparking.wsdl")));
			wsdlData.add(data);
			// parse WSDL file
			definition = wsdlReader.readWSDL(null, new InputSource(
					new ByteArrayInputStream(data.getWsdl())));
			providersWSDL.put("osmparking", definition);
			data = new WSDLData("publictransportation",
					FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
							+ "publictransportation.wsdl")));
			wsdlData.add(data);
			definition = wsdlReader.readWSDL(null, 
					new InputSource(new ByteArrayInputStream(data.getWsdl())));
			providersWSDL.put("publictransportation", definition);
			data = new WSDLData("trafficinformation",
					FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath 
							+ "trafficinformation.wsdl")));
			wsdlData.add(data);
			definition = wsdlReader.readWSDL(null, new InputSource(
					new ByteArrayInputStream(data.getWsdl())));
			providersWSDL.put("trafficinformation", definition);
			data = new WSDLData("tripplanner",
					FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "TripPlanner.wsdl")));
			wsdlData.add(data);
			definition = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(data.getWsdl())));
			providersWSDL.put("tripplanner", definition);
			data = new WSDLData("weather",
					FileUtils.readFileToByteArray(new File(wsdlsFolderAbsolutePath + "weather.wsdl")));
			wsdlData.add(data);
			definition = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(data.getWsdl())));
			providersWSDL.put("weather", definition);
		} catch (ParserConfigurationException | IOException | WSDLException e) {
			LOGGER.error("addImportsTest_01 > ", e);
			Assert.assertTrue(false);
		}

		// Document artifactsWSDL =
		// CDGenerationUtils.generateProsumerArtifactsDocument(wsdlData,prosumerName);

		Definition prosumerWSDLconsumerPartDefinition = CDGenerationUtils.generateCDProsumerPartWSDL(
				choreographyData, choreographytns, prosumerName);
		providersWSDL.put(WSDLUtils.formatParticipantNameForWSDL(prosumerName),
				prosumerWSDLconsumerPartDefinition);

		// HashMap<String, String> importedNamespacesData =
		// BPELUtils.addWSDLImportsToBpelProcessAndGetImportNamespaceData(cdName,
		// process,cdWSDLdefinition, artifactsWSDL, providersWSDL);

		BPELData bpelData = new BPELData();
		bpelData.setProcess(process);
		// bpelData.setImportedNamespacesData(importedNamespacesData);

		File file = new File(bpelProcessOut);
		URI fileUriTempModelNormalized = URI.createFileURI(file.getAbsolutePath());
		Resource bpelResource = new BPELResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		bpelResource.getContents().add((EObject) bpelData.getProcess());
		BPELWriter bpelWriter;

		try {
			bpelWriter = new BPELWriter((BPELResource) bpelResource,
					DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			File out = new File(bpelProcessOut);
			bpelWriter.write(bpelData, out);
		} catch (IOException | ParserConfigurationException e) {
			LOGGER.error("addImportsTest_01 > ", e);
			Assert.assertTrue(false);
		}

	}

}
