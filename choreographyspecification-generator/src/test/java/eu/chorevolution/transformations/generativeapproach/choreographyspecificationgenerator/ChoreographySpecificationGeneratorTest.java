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
package eu.chorevolution.transformations.generativeapproach.choreographyspecificationgenerator;

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

public class ChoreographySpecificationGeneratorTest {
   private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
         + File.separatorChar + "resources" + File.separatorChar;

   private static final String CHOREOARCH_FILE_EXTENSION = ".chorarch";
   private static final String CHOREOSPEC_FILE_EXTENSION = ".xml";
   private static final String INPUT_TEST_RESOURCES_FOLDER_NAME = "input";
   private static final String OUTPUT_TEST_RESOURCES_FOLDER_NAME = "output";

   private static Logger logger = LoggerFactory.getLogger(ChoreographySpecificationGeneratorTest.class);

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
      Assert.assertTrue(runGenerator(currentTestName.getMethodName()));
   }

   @Test
   public void test_12_wp5() {

      Assert.assertTrue(runGenerator(currentTestName.getMethodName()));
   }

   private boolean runGenerator(String testName) {
      boolean generatedChoreographySpecificationWithoutErrors = true;
      FileDeleteStrategy.FORCE.deleteQuietly(
            new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME));

      File choreographyArchitectureFile = new File(TEST_RESOURCES + testName + File.separatorChar
            + INPUT_TEST_RESOURCES_FOLDER_NAME + File.separatorChar + testName + CHOREOARCH_FILE_EXTENSION);

      try {

         ChoreographySpecificationGeneratorRequest choreographySpecificationGeneratorRequest = new ChoreographySpecificationGeneratorRequest(
               FileUtils.readFileToByteArray(choreographyArchitectureFile));

         FileUtils.writeByteArrayToFile(
               new File(TEST_RESOURCES + testName + File.separatorChar + OUTPUT_TEST_RESOURCES_FOLDER_NAME
                     + File.separatorChar
                     + choreographyArchitectureFile.getName().replace(CHOREOARCH_FILE_EXTENSION,
                           CHOREOSPEC_FILE_EXTENSION)),
               new ChoreographySpecificationGenerator().generate(choreographySpecificationGeneratorRequest)
                     .getChoreographySpecification());

      } catch (Exception e) {
         StringWriter errors = new StringWriter();
         e.printStackTrace(new PrintWriter(errors));
         logger.error(testName + " > " + errors.toString());
         generatedChoreographySpecificationWithoutErrors = false;
      }

      return generatedChoreographySpecificationWithoutErrors;
   }

}
