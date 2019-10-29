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

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl.CDConsumerPartGeneratorUtility;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.CDConsumerPartData;

public class CDConsumerPartRootContextXMLGenerationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(CDConsumerPartRootContextXMLGenerationTests.class);
	
	private static final String ROOT_CONTEXT_XML_FILE_NAME = "root-context.xml";
	private static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar + "output";


	@Before
	public void setUp() {
	}

	@Test
	public void generateRootContextXMLCDConsumerPart_01() {

		String webMvcXMLFilePath = TEST_OUTPUT_RESOURCES + File.separatorChar + ROOT_CONTEXT_XML_FILE_NAME;
		try {
			FileUtils.forceMkdir(new File(TEST_OUTPUT_RESOURCES));
			CDConsumerPartData cdConsumerPartData = new CDConsumerPartData();
			cdConsumerPartData.setName("test-rootcontext");
			cdConsumerPartData.setArtifactId("testrootcontext");
			cdConsumerPartData.setGroupId("eu.chorevolution.testrootcontext.prosumers");
			cdConsumerPartData.setPackagename("eu.chorevolution.testrootcontext.prosumers");
			cdConsumerPartData.setWsdlname("testrootcontext");
			CDConsumerPartGeneratorUtility.generateRootContextXMLFile(cdConsumerPartData, webMvcXMLFilePath);
		} catch (IOException e) {
			LOGGER.error("generateRootContextXMLCDConsumerPart_01 > ", e);
			Assert.assertTrue(false);
		}
	}

}
