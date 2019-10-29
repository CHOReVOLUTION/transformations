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

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import eu.chorevolution.transformations.generativeapproach.bcgenerator.impl.BCGeneratorImpl;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BC;

public class WP5TestWAR {

	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
			+ File.separatorChar + "resources";

	private static final String JOURNEY_PLANNER_NAME = "bcJourneyPlanner";
	private static final String JOURNEY_PLANNER_INPUT_GIDL_NAME = "bcJourneyPlanner.gidl";
	private static final String NEWS_NAME = "bcNews";
	private static final String NEWS_INPUT_GIDL_NAME = "bcNews.gidl";

	private static final String PARKING_NAME = "bcParking";
	private static final String PARKING_INPUT_GIDL_NAME = "bcParking.gidl";
	
	private static final String POI_NAME = "bcPoi";
	private static final String POI_INPUT_GIDL_NAME = "bcPoi.gidl";

	private static final String PUBLICTRANSPORTATION_NAME = "bcPublicTransportation";	
	private static final String PUBLICTRANSPORTATION_INPUT_GIDL_NAME = "bcPublicTransportation.gidl";

	private static final String PWS_NAME = "bcPersonalWeatherStations";
	private static final String PWS_INPUT_GIDL_NAME = "bcPersonalWeatherStations.gidl";

	private static final String TRAFFIC_NAME = "bcTraffic";
	private static final String TRAFFIC_INPUT_GIDL_NAME = "bcTraffic.gidl";

	private static final String STAPP_NAME = "bcSTApp";
	private static final String STAPP_INPUT_WSDL_NAME = "cdSTApp.wsdl";
	
	private static final String WP5_RESOURCES_FOLDER = TEST_RESOURCES + File.separatorChar + "WP5";
	
	private static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "target";


	private static final String WAR_EXTENSION = ".war";
	
	@Test
	public void generateJourneyPlanner() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + JOURNEY_PLANNER_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(JOURNEY_PLANNER_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + JOURNEY_PLANNER_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + JOURNEY_PLANNER_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateNews() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + NEWS_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(NEWS_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + NEWS_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + NEWS_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateParking() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PARKING_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(PARKING_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + PARKING_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + PARKING_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generatePoi() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + POI_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(POI_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + POI_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + POI_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generatePublicTransportation() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PUBLICTRANSPORTATION_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(PUBLICTRANSPORTATION_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + PUBLICTRANSPORTATION_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + PUBLICTRANSPORTATION_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generatePersonalWeatherStations() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PWS_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(PWS_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + PWS_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + PWS_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateTraffic() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + TRAFFIC_INPUT_GIDL_NAME));
			BC result = bcGenerator.generateBC(TRAFFIC_NAME, gidl, BCProtocolType.SOAP, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + TRAFFIC_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + TRAFFIC_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	@Test
	public void generateSTApp() {
		try {
			BCGenerator bcGenerator = new BCGeneratorImpl();
			byte[] gidl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + STAPP_INPUT_WSDL_NAME));
			BC result = bcGenerator.generateBC(STAPP_NAME, gidl, BCProtocolType.REST, BCGenerator.BC_GENERATION_TYPE_SRC);
			Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + STAPP_NAME + WAR_EXTENSION),
					result.getArtifact());
			if (result.getWsdl() != null) {
				Files.write(Paths.get(TEST_OUTPUT_RESOURCES + File.separatorChar + STAPP_NAME + ".wsdl"),
						result.getWsdl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
