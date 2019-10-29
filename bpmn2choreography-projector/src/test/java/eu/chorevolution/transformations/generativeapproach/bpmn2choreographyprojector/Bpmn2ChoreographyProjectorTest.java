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
package eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector;

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

public class Bpmn2ChoreographyProjectorTest {
    private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
            + File.separatorChar + "resources" + File.separatorChar;

    private static final String BPMN2_FILE_EXTENSION = ".bpmn2";
    private static final String XSD_FILE_EXTENSION = ".xsd";

    private static final String INPUT_TEST_RESOURCES_FOLDER_NAME = "input";
    private static final String OUTPUT_TEST_RESOURCES_FOLDER_NAME = "output";

    private static Logger logger = LoggerFactory.getLogger(Bpmn2ChoreographyProjectorTest.class);

    @Rule
    public TestName currentTestName = new TestName();

    @Before
    public void setUp() {
    }

    @Test
    public void test_01() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_02() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_03() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_04() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_05() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_06() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_07() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_08() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_09() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_10() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_11_wp4() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_12_wp5() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_13() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_14() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_15() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_16() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    @Test
    public void test_17() {
        Assert.assertTrue(runProjector(currentTestName.getMethodName()));
    }

    private boolean runProjector(String testName) {
        boolean generatedAllProjectionsWithoutErrors = true;

        FileDeleteStrategy.FORCE.deleteQuietly(
                new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME));

        File resourcesDirectory = new File(
                TEST_RESOURCES + testName + File.separatorChar + INPUT_TEST_RESOURCES_FOLDER_NAME);
        File[] directoryListing = resourcesDirectory.listFiles();
        if (directoryListing != null) {

            for (File file : directoryListing) {
                if (!file.getName().equals(testName + BPMN2_FILE_EXTENSION)
                        && !file.getName().endsWith(XSD_FILE_EXTENSION)) {
                    try {

                        String participantName = file.getName().replace(testName + "_", "")
                                .replace(BPMN2_FILE_EXTENSION, "");

                        File BPMN2File = new File(
                                TEST_RESOURCES + testName + File.separatorChar + INPUT_TEST_RESOURCES_FOLDER_NAME
                                        + File.separatorChar + testName + BPMN2_FILE_EXTENSION);

                        File XSDTypesFile = new File(TEST_RESOURCES + testName + File.separatorChar
                                + INPUT_TEST_RESOURCES_FOLDER_NAME + File.separatorChar + "types" + XSD_FILE_EXTENSION);

                        Bpmn2ChoreographyProjectorRequest bpmn2ChoreographyProjectorRequest;
                        if (XSDTypesFile.exists()) {
                            bpmn2ChoreographyProjectorRequest = new Bpmn2ChoreographyProjectorRequest(
                            		FileUtils.readFileToByteArray(BPMN2File), participantName);
                        } else {
                            bpmn2ChoreographyProjectorRequest = new Bpmn2ChoreographyProjectorRequest(
                            		FileUtils.readFileToByteArray(BPMN2File), participantName);
                        }

                        Bpmn2ChoreographyProjector bpmnProjector = new Bpmn2ChoreographyProjector();
                        Bpmn2ChoreographyProjectorResponse bpmn2ChoreographyProjectorResponse = bpmnProjector.project(bpmn2ChoreographyProjectorRequest);


                        FileUtils.writeByteArrayToFile(
                                        new File(TEST_RESOURCES + testName + File.separatorChar
                                                + OUTPUT_TEST_RESOURCES_FOLDER_NAME + File.separatorChar
                                                + file.getName()),
                                        bpmn2ChoreographyProjectorResponse.getBpmn2Content());
                        
                        if (XSDTypesFile.exists()){
                        	FileUtils.writeByteArrayToFile(new File(TEST_RESOURCES + testName + File.separatorChar
                                    + OUTPUT_TEST_RESOURCES_FOLDER_NAME + File.separatorChar + "types"
                                    + XSD_FILE_EXTENSION), FileUtils.readFileToByteArray(XSDTypesFile));
                        }
                    } catch (Exception e) {
                        StringWriter errors = new StringWriter();
                        e.printStackTrace(new PrintWriter(errors));
                        logger.error(testName + " > " + errors.toString());
                        generatedAllProjectionsWithoutErrors = false;
                    }
                }
            }

        }
        return generatedAllProjectionsWithoutErrors;
    }
}
