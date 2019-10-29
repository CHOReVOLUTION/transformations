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
package eu.chorevolution.transformations.generativeapproach.gidlgenerator;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GIDLGeneratorTest {
	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
			+ File.separatorChar + "resources" + File.separatorChar;

	private static final String WSDL_FILE_EXTENSION = ".wsdl";
	private static final String GIDL_FILE_EXTENSION = ".gidl";
	private static final String INPUT_TEST_RESOURCES_FOLDER_NAME = "input";
	private static final String OUTPUT_TEST_RESOURCES_FOLDER_NAME = "output";

	private static Logger logger = LoggerFactory.getLogger(GIDLGeneratorTest.class);

	@Rule
	public TestName currentTestName = new TestName();

	@Before
	public void setUp() {
	}

	@Test
	public void test_01() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(),"STApp" ));
	}

	@Test
	public void test_02() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), "dts-accidents"));
	}
	
	@Test
	public void test_03() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), "journeyplanner"));
	}

	@Test
	public void test_04() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), "bridge"));
	}
	
	@Test
	public void test_05() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), "congestion"));
	}

	@Test
	public void test_06() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), "news"));
	}
	
	@Test
	public void test_07() {
		Assert.assertTrue(runGenerator(currentTestName.getMethodName(), "DTS-HERE"));
	}
	
	private boolean runGenerator(String testName, String fileName) {
		boolean genratedGIDLModel = true;

		FileDeleteStrategy.FORCE.deleteQuietly(
				new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME));

		File WSDLFile = new File(TEST_RESOURCES + testName + File.separatorChar + INPUT_TEST_RESOURCES_FOLDER_NAME
				+ File.separatorChar + fileName + WSDL_FILE_EXTENSION);

		try {
			GIDLGeneratorRequest gidlGeneatorRequest = new GIDLGeneratorRequest(
					FileUtils.readFileToByteArray(WSDLFile), fileName);

			FileUtils.writeByteArrayToFile(
					new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME
							+ File.separatorChar
							+ WSDLFile.getName().replace(WSDL_FILE_EXTENSION, GIDL_FILE_EXTENSION)),
					new GIDLGenerator().generate(gidlGeneatorRequest).getGidlContent());

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.error(testName + " > " + errors.toString());
			genratedGIDLModel = false;
		}

		return genratedGIDLModel;
	}
}
