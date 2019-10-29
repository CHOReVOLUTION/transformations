/*
  * Copyright 2015 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.AdapterComponentData;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.BindingComponentData;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.ComponentData;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.ConsumerComponentData;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.SecurityComponentData;

public class ChoreographyArchitectureGeneratorTest {
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
			+ File.separatorChar + "resources" + File.separatorChar;

	private static final String BPMN2_FILE_EXTENSION = ".bpmn2";
	private static final String CHOREOARCH_FILE_EXTENSION = ".chorarch";
	private static final String INPUT_TEST_RESOURCES_FOLDER_NAME = "input";
	private static final String OUTPUT_TEST_RESOURCES_FOLDER_NAME = "output";

	private static final String DEPLOYABLESERVICE_TAR_GZ_EXTENSION = ".tar.gz";
	private static final String DEPLOYABLESERVICE_WAR_EXTENSION = ".war";

	private static Logger logger = LoggerFactory.getLogger(ChoreographyArchitectureGeneratorTest.class);

	@Rule
	public TestName currentTestName = new TestName();

	@Before
	public void setUp() {
	}

	/*
	 * @Test public void test_01() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_02() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_03() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_04() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_05() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_06() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_07() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_08() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_09() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 * 
	 * @Test public void test_10() {
	 * Assert.assertTrue(runTrasformator(currentTestName.getMethodName())); }
	 */

	@Test
	public void test_11_wp4() {
		List<ComponentData> clientParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("ND", "cdND", "http://localhost:8080/cdND" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						null, null, null, null));
			}
		};
		List<ComponentData> prosumerParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("SEADA-SEARP", "cdSEADA-SEARP",
						"http://localhost:8080/cdSEADA-SEARP" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("SEADA-SEARP",
								"http://localhost:8080/SEADA-SEARP" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));

				add(new ComponentData("SEADA-SEATSA", "cdSEADA-SEATSA",
						"http://localhost:8080/cdSEADA-SEATSA" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("SEADA-SEATSA",
								"http://localhost:8080/SEADA-SEATSA" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));

				add(new ComponentData("SEADA-TRAFFIC", "cdSEADA-TRAFFIC",
						"http://localhost:8080/cdSEADA-TRAFFIC" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("SEADA-TRAFFIC",
								"http://localhost:8080/SEADA-TRAFFIC" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));
			}
		};
		List<ComponentData> providerParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("DTS-ACCIDENTS", "DTS-ACCIDENTS",
						"http://localhost:8080/DTS-ACCIDENTS/DTS-ACCIDENTS/", null, null,
						new BindingComponentData("bcDTS-ACCIDENTS",
								"http://localhost:8080/bcDTS-ACCIDENTS" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("DTS-BRIDGE", "DTS-BRIDGE", "http://localhost:8080/DTS-BRIDGE/DTS-BRIDGE/", null,
						null, new BindingComponentData("bcDTS-BRIDGE",
								"http://localhost:8080/bcDTS-BRIDGE" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("DTS-CONGESTION", "DTS-CONGESTION",
						"http://localhost:8080/DTS-CONGESTION/DTS-CONGESTION/", null, null,
						new BindingComponentData("bcDTS-CONGESTION",
								"http://localhost:8080/bcDTS-CONGESTION" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("DTS-GOOGLE", "DTS-GOOGLE", "http://localhost:8080/DTS-GOOGLE/DTS-GOOGLE/", null,
						null, new BindingComponentData("bcDTS-GOOGLE",
								"http://localhost:8080/bcDTS-GOOGLE" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("DTS-HERE", "DTS-HERE", "http://localhost:8080/DTS-HERE/DTS-HERE/", null, null,
						new BindingComponentData("bcDTS-HERE",
								"http://localhost:8080/bcDTS-HERE" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("DTS-WEATHER", "DTS-WEATHER", "http://localhost:8080/DTS-WEATHER/DTS-WEATHER/",
						null, null, new BindingComponentData("bcDTS-WEATHER",
								"http://localhost:8080/bcDTS-WEATHER" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
			}
		};

		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), clientParticipants, prosumerParticipants,
				providerParticipants));
	}

	@Test
	public void test_12_wp5() {
		List<ComponentData> clientParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("STApp", "cdSTApp",
						"http://localhost:8080/cdSTApp" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION, null,
						new SecurityComponentData("sfSTApp",
								"http://localhost:8080/sfSTApp" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new BindingComponentData("bcSTApp",
								"http://localhost:8080/bcSTApp" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
			}
		};
		List<ComponentData> prosumerParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("Tourist Agent", "cdTouristAgent",
						"http://localhost:8080/cdTouristAgent" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("TouristAgent",
								"http://localhost:8080/TouristAgent" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));
				add(new ComponentData("Trip Planner", "cdTripPlanner",
						"http://localhost:8080/cdTripPlanner" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("TripPlanner",
								"http://localhost:8080/TripPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));
			}
		};
		List<ComponentData> providerParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("Poi", "Poi", "http://93.62.202.242/poi/poi/", null, new SecurityComponentData(
						"sfPoi", "http://localhost:8080/sfPoi" + DEPLOYABLESERVICE_WAR_EXTENSION), null, null));
				add(new ComponentData("Traffic Information", "TrafficInformation",
						"http://93.62.202.242/trafficinformation/trafficinformation/", null,
						new SecurityComponentData("sfTrafficInformation",
								"http://localhost:8080/sfTrafficInformation" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null));
				add(new ComponentData("Journey Planner", "JourneyPlanner",
						"http://93.62.202.242/journeyplanner/journeyplanner/", null,
						new SecurityComponentData("sfJourneyPlanner",
								"http://localhost:8080/sfJourneyPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null));
				add(new ComponentData("OSM Parking", "OSMParking", "http://93.62.202.242/osmparking/osmparking/", null,
						new SecurityComponentData("sfOSMParking",
								"http://localhost:8080/sfOSMParking" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null));
				add(new ComponentData("Public Transportation", "PublicTransportation",
						"http://93.62.202.242/publictransportation/publictransportation/", null, null, null, null));
				add(new ComponentData("Weather", "Weather", "http://93.62.202.242/weather/weather/", null, null,
						new BindingComponentData("bcWeather",
								"http://localhost:8080/bcWeather" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("News", "News", "http://93.62.202.242/news/news/", null, null, null, null));
			}
		};

		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), clientParticipants, prosumerParticipants,
				providerParticipants));
	}

	@Test
	public void test_12_wp5_new() {
		List<ComponentData> clientParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("STApp", "cdSTApp",
						"http://localhost:8080/cdSTApp" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION, null,
						new SecurityComponentData("sfSTApp",
								"http://localhost:8080/sfSTApp" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new BindingComponentData("bcSTApp",
								"http://localhost:8080/bcSTApp" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adSTApp",
								"http://localhost:8080/adSTApp" + DEPLOYABLESERVICE_WAR_EXTENSION)));
			}
		};
		List<ComponentData> prosumerParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("Tourist Agent", "cdTouristAgent",
						"http://localhost:8080/cdTouristAgent" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("TouristAgent",
								"http://localhost:8080/TouristAgent" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));
				add(new ComponentData("Tourism Information Planner", "cdTourismInformationPlanner",
						"http://localhost:8080/cdTourismInformationPlanner" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("TourismInformationPlanner",
								"http://localhost:8080/TourismInformationPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));
				add(new ComponentData("Mobility Information Planner", "cdMobilityInformationPlanner",
						"http://localhost:8080/cdMobilityInformationPlanner" + DEPLOYABLESERVICE_TAR_GZ_EXTENSION,
						new ConsumerComponentData("MobilityInformationPlanner",
								"http://localhost:8080/MobilityInformationPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null, null, null));
			}
		};
		List<ComponentData> providerParticipants = new ArrayList<ComponentData>() {
			{
				add(new ComponentData("Personal Weather Stations", "PersonalWeatherStations",
						"http://93.62.202.242/weather/weather/", null, null,
						new BindingComponentData("bcPersonalWeatherStations",
								"http://localhost:8080/bcPersonalWeatherStations" + DEPLOYABLESERVICE_WAR_EXTENSION),
						null));
				add(new ComponentData("Poi", "Poi", "http://93.62.202.242/poi/poi/", null,
						new SecurityComponentData("sfPoi",
								"http://localhost:8080/sfPoi" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new BindingComponentData("bcPoi",
								"http://localhost:8080/bcPoi" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adPoi",
								"http://localhost:8080/adPoi" + DEPLOYABLESERVICE_WAR_EXTENSION)));
				add(new ComponentData("News", "News", "http://93.62.202.242/news/news/", null, null,
						new BindingComponentData("bcNews",
								"http://localhost:8080/bcNews" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adNews",
								"http://localhost:8080/adNews" + DEPLOYABLESERVICE_WAR_EXTENSION)));
				add(new ComponentData("Public Transportation", "PublicTransportation",
						"http://93.62.202.242/publictransportation/publictransportation/", null, null,
						new BindingComponentData("bcPublicTransportation",
								"http://localhost:8080/bcPublicTransportation" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adPublicTransportation",
								"http://localhost:8080/adPublicTransportation" + DEPLOYABLESERVICE_WAR_EXTENSION)));
				add(new ComponentData("Parking", "Parking", "http://93.62.202.242/osmparking/osmparking/", null,
						new SecurityComponentData("sfParking",
								"http://localhost:8080/sfParking" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new BindingComponentData("bcParking",
								"http://localhost:8080/bcParking" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adParking",
								"http://localhost:8080/adParking" + DEPLOYABLESERVICE_WAR_EXTENSION)));
				add(new ComponentData("Journey Planner", "JourneyPlanner",
						"http://93.62.202.242/journeyplanner/journeyplanner/", null,
						new SecurityComponentData("sfJourneyPlanner",
								"http://localhost:8080/sfJourneyPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new BindingComponentData("bcJourneyPlanner",
								"http://localhost:8080/bcJourneyPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adJourneyPlanner",
								"http://localhost:8080/adJourneyPlanner" + DEPLOYABLESERVICE_WAR_EXTENSION)));
				add(new ComponentData("Traffic", "Traffic", "http://93.62.202.242/traffic/traffic/", null,
						new SecurityComponentData("sfTraffic",
								"http://localhost:8080/sfTraffic" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new BindingComponentData("bcTraffic",
								"http://localhost:8080/bcTraffic" + DEPLOYABLESERVICE_WAR_EXTENSION),
						new AdapterComponentData("adTraffic",
								"http://localhost:8080/adTraffic" + DEPLOYABLESERVICE_WAR_EXTENSION)));
			}
		};

		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), clientParticipants, prosumerParticipants,
				providerParticipants));
	}

	private boolean runGenerator(String testName, List<ComponentData> clientParticipants,
			List<ComponentData> prosumerParticipants, List<ComponentData> providerParticipants) {
		boolean generatedAllChoreographyArchitectureWithoutErrors = true;

		FileDeleteStrategy.FORCE.deleteQuietly(
				new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME));

		File BPMN2File = new File(TEST_RESOURCES + testName + File.separatorChar + INPUT_TEST_RESOURCES_FOLDER_NAME
				+ File.separatorChar + testName + BPMN2_FILE_EXTENSION);

		try {
			ChoreographyArchitectureGeneratorRequest bpmn2ChoreoArchTransformatorRequest = new ChoreographyArchitectureGeneratorRequest(
					FileUtils.readFileToByteArray(BPMN2File), clientParticipants, prosumerParticipants,
					providerParticipants);

			FileUtils
					.writeByteArrayToFile(
							new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME
									+ File.separatorChar
									+ BPMN2File.getName().replace(BPMN2_FILE_EXTENSION, CHOREOARCH_FILE_EXTENSION)),
							new ChoreographyArchitectureGenerator().generate(bpmn2ChoreoArchTransformatorRequest)
									.getChoreographyArchitecture());

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.error(testName + " > " + errors.toString());
			generatedAllChoreographyArchitectureWithoutErrors = false;
		}

		return generatedAllChoreographyArchitectureWithoutErrors;
	}
}
