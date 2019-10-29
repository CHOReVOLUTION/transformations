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

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class GenerateWP5Test {
	
	private static final String JOURNEY_PLANNER_NAME = "adJourneyPlanner";
	private static final String JOURNEY_PLANNER_INPUT_WSDL_NAME = "bcJourneyPlanner.wsdl";
	private static final String JOURNEY_PLANNER_INPUT_ADAPTER_MODEL_NAME = "adJourneyPlanner.adapter";

	private static final String NEWS_NAME = "adNews";
	private static final String NEWS_INPUT_WSDL_NAME = "bcNews.wsdl";
	private static final String NEWS_INPUT_ADAPTER_MODEL_NAME = "adNews.adapter";

	private static final String PARKING_NAME = "adParking";
	private static final String PARKING_INPUT_WSDL_NAME = "bcParking.wsdl";
	private static final String PARKING_INPUT_ADAPTER_MODEL_NAME = "adParking.adapter";
	
	private static final String POI_NAME = "adPoi";
	private static final String POI_INPUT_WSDL_NAME = "bcPoi.wsdl";
	private static final String POI_INPUT_ADAPTER_MODEL_NAME = "adPoi.adapter";

	private static final String PUBLICTRANSPORTATION_NAME = "adPublicTransportation";
	private static final String PUBLICTRANSPORTATION_INPUT_WSDL_NAME = "bcPublicTransportation.wsdl";
	private static final String PUBLICTRANSPORTATION_INPUT_ADAPTER_MODEL_NAME = "adPublicTransportation.adapter";

	private static final String PWS_NAME = "adPersonalWeatherStations";
	private static final String PWS_INPUT_WSDL_NAME = "bcPersonalWeatherStations.wsdl";
	private static final String PWS_INPUT_ADAPTER_MODEL_NAME = "adPersonalWeatherStations.adapter";

	private static final String TRAFFIC_NAME = "adTraffic";
	private static final String TRAFFIC_INPUT_WSDL_NAME = "bcTraffic.wsdl";
	private static final String TRAFFIC_INPUT_ADAPTER_MODEL_NAME = "adTraffic.adapter";
	
	private static final String WP5_RESOURCES_FOLDER = ADGenerationTest.TEST_RESOURCES + File.separatorChar + "WP5";

	@Test
	public void generateJourneyPlanner() {

		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + JOURNEY_PLANNER_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + JOURNEY_PLANNER_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(JOURNEY_PLANNER_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateNews() {
		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + NEWS_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + NEWS_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(NEWS_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateParking() {
		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PARKING_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PARKING_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(PARKING_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generatePoi() {
		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + POI_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + POI_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(POI_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generatePublicTransportation() {
		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PUBLICTRANSPORTATION_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PUBLICTRANSPORTATION_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(PUBLICTRANSPORTATION_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generatePersonalWeatherStations() {
		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PWS_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + PWS_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(PWS_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void generateTraffic() {
		try {
			byte[] bcWsdl = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + TRAFFIC_INPUT_WSDL_NAME));
			byte[] adapterModel = FileUtils.readFileToByteArray(new File(WP5_RESOURCES_FOLDER + File.separatorChar + TRAFFIC_INPUT_ADAPTER_MODEL_NAME));

			ADGenerationTest.testGeneration(TRAFFIC_NAME, adapterModel, bcWsdl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
