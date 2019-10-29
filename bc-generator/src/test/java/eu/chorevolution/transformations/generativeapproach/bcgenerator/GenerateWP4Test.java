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
package eu.chorevolution.transformations.generativeapproach.bcgenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.wsdl.WSDLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.junit.Test;

import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.impl.BCGeneratorImpl;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BC;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BCData;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.GidlModelsUtility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.Utility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.WSDLUtility;

public class GenerateWP4Test {
	
	private static final String DTS_ACCIDENTS_NAME = "bcDTS-Accidents";
	private static final String DTS_ACCIDENTS_INPUT_GIDL_NAME = "dts-accidents.gidl";

	private static final String DTS_BRIDGE_NAME = "bcDTS-Bridge";
	private static final String DTS_BRIDGE_INPUT_GIDL_NAME = "dts-bridge.gidl";

	private static final String DTS_CONGESTION_NAME = "bcDTS-Congestion";
	private static final String DTS_CONGESTION_INPUT_GIDL_NAME = "dts-congestion.gidl";

	private static final String DTS_GOOGLE_NAME = "bcDTS-Google";
	private static final String DTS_GOOGLE_INPUT_GIDL_NAME = "dts-google.gidl";

	private static final String DTS_HERE_NAME = "bcDTS-Here";
	private static final String DTS_HERE_INPUT_GIDL_NAME = "dts-here.gidl";

	private static final String DTS_WEATHER_NAME = "bcDTS-Weather";
	private static final String DTS_WEATHER_INPUT_GIDL_NAME = "dts-weather.gidl";
	
	private static final String WP4_RESOURCES_FOLDER = BCGenerationTest.TEST_RESOURCES + File.separatorChar + "WP4";
	

	private static final String GROUP_ID_BASE = "eu.chorevolution.bc";
	private static final String PACKAGE_NAME_BASE = "eu.chorevolution.bc";
	private static final String TNS_BASE = "http://eu.chorevolution.bc/";
	private static final String RESOURCES_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "resources";
	private static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "target";

	@Test
	public void generateDTSAccidents() {

		try {
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP4_RESOURCES_FOLDER + File.separatorChar + DTS_ACCIDENTS_INPUT_GIDL_NAME));

			testGeneration(DTS_ACCIDENTS_NAME, gidl, BCProtocolType.SOAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateDTSBridge() {

		try {
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP4_RESOURCES_FOLDER + File.separatorChar + DTS_BRIDGE_INPUT_GIDL_NAME));

			testGeneration(DTS_BRIDGE_NAME, gidl, BCProtocolType.SOAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateDTSCongestion() {

		try {
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP4_RESOURCES_FOLDER + File.separatorChar + DTS_CONGESTION_INPUT_GIDL_NAME));

			testGeneration(DTS_CONGESTION_NAME, gidl, BCProtocolType.SOAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateDTSGoogle() {

		try {
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP4_RESOURCES_FOLDER + File.separatorChar + DTS_GOOGLE_INPUT_GIDL_NAME));

			testGeneration(DTS_GOOGLE_NAME, gidl, BCProtocolType.SOAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateDTSHere() {

		try {
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP4_RESOURCES_FOLDER + File.separatorChar + DTS_HERE_INPUT_GIDL_NAME));

			testGeneration(DTS_HERE_NAME, gidl, BCProtocolType.SOAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateDTSWeather() {

		try {
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP4_RESOURCES_FOLDER + File.separatorChar + DTS_WEATHER_INPUT_GIDL_NAME));

			testGeneration(DTS_WEATHER_NAME, gidl, BCProtocolType.SOAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void testGeneration(String bcName, byte[] interfaceModel, BCProtocolType protocol) {

		String artifactName = bcName;
		if (protocol.equals(BCProtocolType.REST)) {
			bcName = bcName.startsWith("bc") ? bcName.substring(2) : bcName;
		}

		BCData bcData = new BCData();
		bcData.setName(Utility.createName(bcName));
		bcData.setRoleName(bcName.startsWith("bc") ? StringUtils.capitalize(bcName.substring(2)) : bcName);
		bcData.setArtifactId(artifactName);
		bcData.setGroupId(GROUP_ID_BASE);
		bcData.setPackagename(PACKAGE_NAME_BASE + "." + Utility.createArtifactName(bcName));
		bcData.setServicename(Utility.createName(bcName));
		bcData.setWsdlname(Utility.createName(bcName));
		bcData.setTargetNamespace(TNS_BASE + Utility.createName(bcName));

		String tempFolderPath = Utility.createTemporaryFolderFromMillisAndGetPath();
		String bcTempFolderPath = tempFolderPath + File.separatorChar + bcData.getName();

		BCGeneratorImpl generator = new BCGeneratorImpl();

		if (protocol.equals(BCProtocolType.REST)) {
			try {
				WSDLInfo parsedWsdlModel = WSDLUtility.readWSDLInfo(interfaceModel, bcData.getRoleName());
				generator.generateRest(bcData, parsedWsdlModel, interfaceModel, bcTempFolderPath);
			} catch (WSDLException e) {
				e.printStackTrace();
			}
		} else {
			GIDLModel parsedGidlModel = GidlModelsUtility.loadGIDLModel(interfaceModel);
			generator.generateSoap(bcData, parsedGidlModel, bcTempFolderPath);
		}

		try {
			EmbeddedMaven.forProject(bcTempFolderPath + File.separatorChar + "pom.xml").useMaven3Version("3.3.9")
					.setGoals("package").build();

			BC bc = new BC();
			bc.setName(bcName);
			bc.setArtifact(Files.readAllBytes(Paths.get(bcTempFolderPath + File.separatorChar + "target"
					+ File.separatorChar + bcData.getArtifactId() + ".war")));
			if (protocol.equals(BCProtocolType.SOAP)) {
				bc.setWsdl(Files.readAllBytes(Paths.get(bcTempFolderPath + File.separatorChar + RESOURCES_PATH
						+ File.separatorChar + bcData.getWsdlname())));
			}

			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + bcData.getArtifactId() + ".war"),
					bc.getArtifact());
			if (bc.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + bcData.getArtifactId() + ".wsdl"),
						bc.getWsdl());
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new BCGeneratorException("Exception into testGeneration, see log file for details ");

		} finally {
			Utility.createZipOfDirectory(bcTempFolderPath, TEST_OUTPUT_RESOURCES, bcData.getArtifactId());
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				Utility.deleteFolder(tempFolderPath);
			}
		}
	}


}
