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

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.BCData;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.BCFileGeneratorUtility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.GidlModelsUtility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.Utility;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.util.WSDLUtility;

public class BCGenerationTest {

	public static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test"
			+ File.separatorChar + "resources";
	public static final String TEST_OUTPUT_RESOURCES = "." + File.separatorChar + "target";

	public static final String TEST_BC_NAME = "bcSTApp";
	public static final String TEST_GROUP_ID = "eu.chorevolution.bc";
	public static final String TEST_TNS = "http://eu.chorevolution.bc/stapp";
	public static final String TEST_OUTPUT_WSDL_NAME = "cdSTApp";
	public static final String TEST_BC_SERVICE_NAME = "STApp";

	private static final String GIDL_FILE_NAME = "WP5/bcPublicTransportation.gidl";
	private static final String WSDL_FILE_NAME = "WP5/cdSTApp.wsdl";

	@Test
	public void serviceImplGeneratingTest() {
		try {

			byte[] gidlBytes = FileUtils
					.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + GIDL_FILE_NAME));

			GIDLModel gidlModel = GidlModelsUtility.loadGIDLModel(gidlBytes);

			BCData bcData = new BCData();
			bcData.setName(TEST_BC_NAME);
			bcData.setRoleName(
					TEST_BC_NAME.startsWith("bc") ? StringUtils.capitalize(TEST_BC_NAME.substring(2)) : TEST_BC_NAME);
			bcData.setArtifactId(TEST_BC_NAME);
			bcData.setGroupId(TEST_GROUP_ID);
			bcData.setPackagename(TEST_GROUP_ID + "." + Utility.createArtifactName(TEST_BC_NAME));
			bcData.setServicename(StringUtils.capitalize(TEST_BC_NAME));
			bcData.setWsdlname(TEST_BC_NAME);

			BCFileGeneratorUtility.generateServiceImplJavaFile(bcData, gidlModel,
					TEST_OUTPUT_RESOURCES + File.separatorChar + bcData.getServicename() + "ServiceImpl.java");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void restGeneratingTest() {
		try {

			byte[] wsdl = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + WSDL_FILE_NAME));
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl,
					TEST_BC_NAME.startsWith("bc") ? StringUtils.capitalize(TEST_BC_NAME.substring(2)) : TEST_BC_NAME);

			BCData bcData = new BCData();
			bcData.setName(TEST_BC_NAME);
			bcData.setRoleName(
					TEST_BC_NAME.startsWith("bc") ? StringUtils.capitalize(TEST_BC_NAME.substring(2)) : TEST_BC_NAME);
			bcData.setArtifactId(TEST_BC_NAME);
			bcData.setGroupId(TEST_GROUP_ID);
			bcData.setPackagename(TEST_GROUP_ID + "." + Utility.createArtifactName(TEST_BC_NAME));
			bcData.setServicename(wsdlInfo.getServiceName());
			bcData.setWsdlname(TEST_BC_NAME);

			BCFileGeneratorUtility.generateRESTJavaFile(bcData, wsdlInfo,
					TEST_OUTPUT_RESOURCES + File.separatorChar + bcData.getServicename() + "REST.java");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void wsdlGeneratingTest() {
		try {

			byte[] gidlBytes = FileUtils
					.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + GIDL_FILE_NAME));

			GIDLModel adapterModel = GidlModelsUtility.loadGIDLModel(gidlBytes);

			BCData bcData = new BCData();
			bcData.setName(TEST_BC_NAME);
			bcData.setArtifactId(TEST_BC_NAME);
			bcData.setGroupId(TEST_GROUP_ID);
			bcData.setPackagename(TEST_GROUP_ID + "." + Utility.createArtifactName(TEST_BC_NAME));
			bcData.setServicename(StringUtils.capitalize(TEST_BC_SERVICE_NAME));
			bcData.setWsdlname(TEST_BC_NAME);
			bcData.setTargetNamespace(TEST_TNS);

			BCFileGeneratorUtility.generateWSDLFile(bcData, adapterModel,
					TEST_OUTPUT_RESOURCES + File.separatorChar + bcData.getWsdlname());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void wsdlReadingTest() {
		try {
			byte[] wsdl = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + WSDL_FILE_NAME));
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl,
					TEST_BC_NAME.startsWith("bc") ? StringUtils.capitalize(TEST_BC_NAME.substring(2)) : TEST_BC_NAME);
			String serviceName = wsdlInfo.getServiceName();
			String targetNamespace = wsdlInfo.getTargetNS();
			Definition definition = wsdlInfo.getDefinition();
			Service service = (Service) definition.getAllServices()
					.get(definition.getAllServices().keySet().toArray()[0]);
			String serviceLocationURI = ((SOAPAddress) service
					.getPort((String) service.getPorts().keySet().toArray()[0]).getExtensibilityElements().get(0))
							.getLocationURI();

			System.out.println("SERVICE NAME: " + serviceName + "\nTARGET NAMESPACE: " + targetNamespace
					+ "\nLOCATION URI: " + serviceLocationURI);

			System.out.println("OPERATION NAME: " + WSDLUtility.getOperationName(wsdlInfo));
			System.out.println("INPUT TYPE NAME: " + WSDLUtility.getInputMessageRootItemName(wsdlInfo));
			System.out.println("OUTPUT TYPE NAME: " + WSDLUtility.getOutputMessageRootItemName(wsdlInfo));

			System.out.println(
					"REST SERVICE INPUT PARAMETERS: " + WSDLUtility.getInputMessageMapper(wsdlInfo).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
