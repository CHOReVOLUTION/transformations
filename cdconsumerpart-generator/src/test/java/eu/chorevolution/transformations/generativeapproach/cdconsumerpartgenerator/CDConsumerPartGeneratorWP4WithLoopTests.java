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
package eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl.CDConsumerPartGeneratorImpl;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.CDConsumerPartData;

public class CDConsumerPartGeneratorWP4WithLoopTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(CDConsumerPartGeneratorWP4WithLoopTests.class);

	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources";
	private static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar + "output";

	@Before
	public void setUp() {
	}

	@Test
	public void generateCDConsumerPart_01() {
		try {
			CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
			cdConsumerPartData.setName("SEADA-SEATSA");
			cdConsumerPartData.setArtifactId("SEADA-SEATSA");
			cdConsumerPartData.setGroupId("eu.chorevolution.urbantrafficcoordination.seada.prosumer");
			cdConsumerPartData.setPackagename("eu.chorevolution.urbantrafficcoordination.seada.prosumer.seadaseatsa");
			cdConsumerPartData.setWsdlname("SEADA-SEATSA");
			String cdTempFolderPath = TEST_OUTPUT_RESOURCES + File.separatorChar + cdConsumerPartData.getName();
			String wsdlFileName = TEST_RESOURCES + File.separatorChar + "wp4" + File.separatorChar + "withloop" + File.separatorChar + "SEADA-SEATSA.wsdl";
			byte[] wsdl = FileUtils.readFileToByteArray(new File(wsdlFileName));

			FileUtils.forceMkdir(new File(TEST_OUTPUT_RESOURCES));

			CDConsumerPartGeneratorImpl cdConsumerPartGenerator = new CDConsumerPartGeneratorImpl();
			cdConsumerPartGenerator.generateCDConsumerPartInternal(cdConsumerPartData, wsdl, cdTempFolderPath);

		} catch (CDConsumerPartGeneratorException | IOException e) {
			e.printStackTrace();
			LOGGER.error("generateCDConsumerPart_02 > ", e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateCDConsumerPart_02() {
		try {
			CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
			cdConsumerPartData.setName("SEADA-TRAFFIC");
			cdConsumerPartData.setArtifactId("SEADA-TRAFFIC");
			cdConsumerPartData.setGroupId("eu.chorevolution.urbantrafficcoordination.seada.prosumer");
			cdConsumerPartData.setPackagename("eu.chorevolution.urbantrafficcoordination.seada.prosumer.seadatraffic");
			cdConsumerPartData.setWsdlname("SEADA-TRAFFIC");
			String cdTempFolderPath = TEST_OUTPUT_RESOURCES + File.separatorChar + cdConsumerPartData.getName();
			String wsdlFileName = TEST_RESOURCES + File.separatorChar + "wp4" + File.separatorChar + "withloop" + File.separatorChar + "SEADA-TRAFFIC.wsdl";
			byte[] wsdl = FileUtils.readFileToByteArray(new File(wsdlFileName));

			FileUtils.forceMkdir(new File(TEST_OUTPUT_RESOURCES));

			CDConsumerPartGeneratorImpl cdConsumerPartGenerator = new CDConsumerPartGeneratorImpl();
			cdConsumerPartGenerator.generateCDConsumerPartInternal(cdConsumerPartData, wsdl, cdTempFolderPath);

		} catch (CDConsumerPartGeneratorException | IOException e) {
			e.printStackTrace();
			LOGGER.error("generateCDConsumerPart_03 > ", e);
			Assert.assertTrue(false);
		}
	}

}
