/*
* Copyright 2017 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.adgenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.junit.Test;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.transformations.generativeapproach.adgenerator.impl.ADGeneratorImpl;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.Adapter;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.AdapterData;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.ADFileGeneratorUtility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.AdapterModelsUtility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.Utility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.WSDLUtility;

public class ADGenerationTest {

	public static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources";
	public static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar + "output";

	public static final String TEST_ADAPTER_NAME = "adJourneyPlanner";
	public static final String TEST_GROUP_ID = "eu.chorevolution.smt.ad";
	public static final String TEST_BC_TNS = "http://eu.chorevolution.smt/bc/journeyplanner";
	public static final String TEST_AD_TNS = "http://eu.chorevolution.smt/ad/journeyplanner";
	public static final String TEST_OUTPUT_WSDL_NAME = "adJourneyPlanner";
	public static final String TEST_ADAPTER_SERVICE_NAME = "JourneyPlanner";

	private static final String ADAPTER_MODEL_FILE_NAME = "WP5/journeyplanner.adapter";
	private static final String WSDL_IN_FILE_NAME = "WP5/bcJourneyPlanner.wsdl";

	private static final String GROUP_ID_BASE = "eu.chorevolution.ad";
	private static final String PACKAGE_NAME_BASE = "eu.chorevolution.ad";
	private static final String AD_TNS_BASE = "http://eu.chorevolution.smt/ad/";
	private static final String WEB_INF_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF";

	public static void testGeneration(String adapterName, byte[] adapterModel, byte[] bcWsdl) {

		AdapterData adapterData = new AdapterData();
		adapterData.setName(adapterName);
		adapterData.setRoleName(adapterName.startsWith("ad") ? StringUtils.capitalize(adapterName.substring(2)) : adapterName);
		adapterData.setArtifactId(adapterName);
		adapterData.setGroupId(GROUP_ID_BASE);
		adapterData.setPackagename(PACKAGE_NAME_BASE + "." + Utility.createArtifactName(adapterName));
		adapterData.setServicename(adapterName);
		adapterData.setWsdlname(adapterName);
		adapterData.setAdTargetNamespace(AD_TNS_BASE + adapterName);

		String tempFolderPath = Utility.createTemporaryFolderFromMillisAndGetPath();
		String adTempFolderPath = tempFolderPath + File.separatorChar + adapterData.getName();
		AdapterModel parsedAdapterModel = AdapterModelsUtility.loadAdapterModel(adapterModel);
		
		try {
			WSDLInfo parsedWsdl = WSDLUtility.readWSDLInfo(bcWsdl);
			Definition definition = parsedWsdl.getDefinition();
			Service service = (Service) definition.getAllServices().get(definition.getAllServices().keySet().toArray()[0]);
			String serviceLocationURI = ((SOAPAddress)service.getPort((String)service.getPorts().keySet().toArray()[0]).getExtensibilityElements().get(0)).getLocationURI();

			adapterData.setServiceLocationURI(serviceLocationURI);
			adapterData.setBcTargetNamespace(parsedWsdl.getTargetNS());
		} catch (WSDLException e) {
			throw new ADGeneratorException("Exception into testGeneration, see log file for details ");
		}	

		ADGeneratorImpl generator = new ADGeneratorImpl();
		generator.generateAdapterInternal(adapterData, parsedAdapterModel, adTempFolderPath);

		try {
			EmbeddedMaven.forProject(adTempFolderPath + File.separatorChar + "pom.xml")
			.useMaven3Version("3.3.9")
			.setGoals("package")
			.build();

			Adapter adapter = new Adapter();
			adapter.setName(adapterName);
			adapter.setArtifact(Files.readAllBytes(Paths.get(adTempFolderPath + File.separatorChar + "target" + File.separatorChar + adapterName + ".war")));
			adapter.setWsdl(Files.readAllBytes(Paths.get(adTempFolderPath + File.separatorChar + WEB_INF_PATH + File.separatorChar + "wsdl" + File.separatorChar + adapterData.getWsdlname())));

			Utility.createZipOfDirectory(adTempFolderPath, TEST_OUTPUT_RESOURCES, adapterName);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + adapterName + ".war"), adapter.getArtifact());
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + adapterData.getWsdlname()), adapter.getWsdl());

		} catch (IOException e) {
			throw new ADGeneratorException("Exception into testGeneration, see log file for details ");

		} finally {
			if(!System.getProperty("os.name").toLowerCase().contains("win")){
				Utility.deleteFolder(tempFolderPath);
			}
		}
	}

	@Test
	public void requestTranslatorGeneratingTest() {
		try {
			
			byte[] adapterModelBytes = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + ADAPTER_MODEL_FILE_NAME));
			
			AdapterModel adapterModel = AdapterModelsUtility.loadAdapterModel(adapterModelBytes);
			
			AdapterData adapterData = new AdapterData();
			adapterData.setName(TEST_ADAPTER_NAME);
			adapterData.setArtifactId(TEST_ADAPTER_NAME);
			adapterData.setGroupId(TEST_GROUP_ID);
			adapterData.setPackagename(TEST_GROUP_ID + "." + Utility.createArtifactName(TEST_ADAPTER_NAME));
			adapterData.setWsdlname(TEST_ADAPTER_NAME);
			adapterData.setBcTargetNamespace(TEST_BC_TNS);
			
			ADFileGeneratorUtility.generateRequestTranslatorJavaFile(adapterData, adapterModel, TEST_OUTPUT_RESOURCES + File.separatorChar + "RequestTranslator.java");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void responseTranslatorGeneratingTest() {
		try {

			byte[] adapterModelBytes = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + ADAPTER_MODEL_FILE_NAME));
			
			AdapterModel adapterModel = AdapterModelsUtility.loadAdapterModel(adapterModelBytes);
			
			AdapterData adapterData = new AdapterData();
			adapterData.setName(TEST_ADAPTER_NAME);
			adapterData.setArtifactId(TEST_ADAPTER_NAME);
			adapterData.setGroupId(TEST_GROUP_ID);
			adapterData.setPackagename(TEST_GROUP_ID + "." + Utility.createArtifactName(TEST_ADAPTER_NAME));
			adapterData.setWsdlname(TEST_ADAPTER_NAME);
			adapterData.setAdTargetNamespace(TEST_AD_TNS);

			ADFileGeneratorUtility.generateResponseTranslatorJavaFile(adapterData, adapterModel, TEST_OUTPUT_RESOURCES + File.separatorChar + "ResponseTranslator.java");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void wsdlGeneratingTest() {
		try {

			byte[] adapterModelBytes = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + ADAPTER_MODEL_FILE_NAME));
			byte[] wsdlBytes = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + WSDL_IN_FILE_NAME));
			
			AdapterModel adapterModel = AdapterModelsUtility.loadAdapterModel(adapterModelBytes);
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdlBytes);

			Definition definition = wsdlInfo.getDefinition();
			Service service = (Service) definition.getAllServices().get(definition.getAllServices().keySet().toArray()[0]);
			String serviceLocationURI = ((SOAPAddress)service.getPort((String)service.getPorts().keySet().toArray()[0]).getExtensibilityElements().get(0)).getLocationURI();

			AdapterData adapterData = new AdapterData();
			adapterData.setName(TEST_ADAPTER_NAME);
			adapterData.setArtifactId(TEST_ADAPTER_NAME);
			adapterData.setGroupId(TEST_GROUP_ID);
			adapterData.setPackagename(TEST_GROUP_ID + "." + Utility.createArtifactName(TEST_ADAPTER_NAME));
			adapterData.setServicename(TEST_ADAPTER_SERVICE_NAME);
			adapterData.setWsdlname(TEST_ADAPTER_NAME);
			adapterData.setAdTargetNamespace(TEST_AD_TNS);
			adapterData.setBcTargetNamespace(wsdlInfo.getTargetNS());
			adapterData.setServiceLocationURI(serviceLocationURI);

			ADFileGeneratorUtility.generateAdapterWSDLFile(adapterData, adapterModel, TEST_OUTPUT_RESOURCES + File.separatorChar + adapterData.getWsdlname());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
