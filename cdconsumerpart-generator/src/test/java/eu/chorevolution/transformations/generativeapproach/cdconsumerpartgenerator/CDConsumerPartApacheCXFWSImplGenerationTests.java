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

import javax.wsdl.WSDLException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl.CDConsumerPartGeneratorUtility;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl.Utility;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl.WSDLUtility;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.CDConsumerPartData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.GeneratingJavaData;

public class CDConsumerPartApacheCXFWSImplGenerationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(CDConsumerPartApacheCXFWSImplGenerationTests.class);

	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources";
	private static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar + "output";

	@Before
	public void setUp() {
	}

	@Test
	public void generateApacheCXFWSImplCDConsumerPart_01() {

		try {
			FileUtils.forceMkdir(new File(TEST_OUTPUT_RESOURCES));
			CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
			cdConsumerPartData.setName("TouristAgent");
			cdConsumerPartData.setArtifactId("TouristAgent");
			cdConsumerPartData.setGroupId("eu.chorevolution.smt.prosumer");
			cdConsumerPartData.setPackagename("eu.chorevolution.smt.prosumer.touristagent");
			cdConsumerPartData.setWsdlname("TouristAgent");
			String wsdlFileName = TEST_RESOURCES + File.separatorChar + "wp5" + File.separatorChar + "old" + File.separatorChar + "TouristAgent.wsdl";
			byte[] wsdl = FileUtils.readFileToByteArray(new File(wsdlFileName));
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl);
			GeneratingJavaData generatingJavaData = Utility.createGeneratingJavaData(cdConsumerPartData, wsdlInfo);
			CDConsumerPartGeneratorUtility.generateApacheCXFWSImpl(TEST_OUTPUT_RESOURCES, generatingJavaData);

		} catch (WSDLException | IOException e) {
			e.printStackTrace();
			LOGGER.error("generateApacheCXFWSImplCDConsumerPart_01 > ", e);
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void generateApacheCXFWSImplCDConsumerPart_02() {
		//Generated the seadasearp webservice impl wp4 without loop
		try {
			CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
			cdConsumerPartData.setName("SEADA-SEARP");
			cdConsumerPartData.setArtifactId("SEADA-SEARP");
			cdConsumerPartData.setGroupId("eu.chorevolution.urbantrafficcoordination.seada.prosumer");
			cdConsumerPartData.setPackagename("eu.chorevolution.urbantrafficcoordination.seada.prosumer.seadasearp");
			cdConsumerPartData.setWsdlname("SEADA-SEARP");
			String wsdlFileName = TEST_RESOURCES + File.separatorChar + "wp4" + File.separatorChar + "withoutloop" + File.separatorChar + "SEADA-SEARP.wsdl";
			byte[] wsdl = FileUtils.readFileToByteArray(new File(wsdlFileName));
			
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl);
			GeneratingJavaData generatingJavaData = Utility.createGeneratingJavaData(cdConsumerPartData, wsdlInfo);
			CDConsumerPartGeneratorUtility.generateApacheCXFWSImpl(TEST_OUTPUT_RESOURCES, generatingJavaData);

		} catch (WSDLException | IOException e) {
			e.printStackTrace();
			LOGGER.error("generateApacheCXFWSImplCDConsumerPart_02 > ", e);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void generateApacheCXFWSImplCDConsumerPart_03() {
		//Generated the seadaseatsa webservice impl wp4 WITH loop
		try {
			CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
			cdConsumerPartData.setName("SEADA-SEATSA");
			cdConsumerPartData.setArtifactId("SEADA-SEATSA");
			cdConsumerPartData.setGroupId("eu.chorevolution.urbantrafficcoordination.seada.prosumer");
			cdConsumerPartData.setPackagename("eu.chorevolution.urbantrafficcoordination.seada.prosumer.seadaseatsa");
			cdConsumerPartData.setWsdlname("SEADA-SEATSA");
			String wsdlFileName = TEST_RESOURCES + File.separatorChar + "wp4" + File.separatorChar + "withloop" + File.separatorChar + "SEADA-SEATSA.wsdl";
			byte[] wsdl = FileUtils.readFileToByteArray(new File(wsdlFileName));
			
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl);
			GeneratingJavaData generatingJavaData = Utility.createGeneratingJavaData(cdConsumerPartData, wsdlInfo);
			CDConsumerPartGeneratorUtility.generateApacheCXFWSImpl(TEST_OUTPUT_RESOURCES, generatingJavaData);

		} catch (WSDLException | IOException e) {
			e.printStackTrace();
			LOGGER.error("generateApacheCXFWSImplCDConsumerPart_03 > ", e);
			Assert.assertTrue(false);
		}
	}	
	
}
