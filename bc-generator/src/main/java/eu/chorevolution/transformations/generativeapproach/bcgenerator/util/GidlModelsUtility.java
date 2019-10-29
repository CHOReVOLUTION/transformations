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
package eu.chorevolution.transformations.generativeapproach.bcgenerator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import eu.chorevolution.modelingnotations.gidl.ComplexType;
import eu.chorevolution.modelingnotations.gidl.DataType;
import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.modelingnotations.gidl.OccurrencesTypes;
import eu.chorevolution.modelingnotations.gidl.Operation;
import eu.chorevolution.modelingnotations.gidl.SimpleType;
import eu.chorevolution.modelingnotations.gidl.impl.GidlPackageImpl;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.BCGeneratorException;

public class GidlModelsUtility {

	private static final String TEMPFILE_SUFFIX = "bcgenerator";
	private static final String GIDL_FILE_EXTENSION = ".gidl";

	public static GIDLModel loadGIDLModel(final byte[] gidlContent)
			throws BCGeneratorException {
		File gidlFile;
		try {
			gidlFile = File.createTempFile(TEMPFILE_SUFFIX,
					GIDL_FILE_EXTENSION);
			IOUtils.write(gidlContent, FileUtils.openOutputStream(gidlFile));
		} catch (IOException e1) {
			throw new BCGeneratorException(
					"Internal Error while loading the GIDL model");
		}

		URI adapterModelURI = URI.createURI(gidlFile.toURI().toString());

		GidlPackageImpl.init();
		Resource resource = new XMIResourceFactoryImpl().createResource(adapterModelURI);

		try {
			resource.load(null);

		} catch (IOException e) {
			e.printStackTrace();
//			throw new BCGeneratorException(
//					"Error to load the resource: " + resource.getURI().toFileString());
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(gidlFile);
		}

		return (GIDLModel) resource.getContents().get(0);
	}

	public static Operation getFirstOperation(GIDLModel gidlModel) {
		if (gidlModel.getHasInterfaces().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined interfaces");
		}
		if (gidlModel.getHasInterfaces().get(0).getHasOperations().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined operations");
		}
		return gidlModel.getHasInterfaces().get(0).getHasOperations().get(0);
	}

	public static String getInputMessageName(Operation operation) {
		return operation.getName();
	}

	public static String getOutputMessageName(Operation operation) {
		return operation.getName() + "Response";
	}

	public static String getInputRootElementName(Operation operation) {
		if (operation.getInputData().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined input data");
		}
		if (operation.getInputData().get(0).getHasDataType().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined input data type for operation " + operation.getName());
		}
		return operation.getInputData().get(0).getHasDataType().get(0).getName();
	}

	public static String getOutputRootElementName(Operation operation) {
		if (operation.getOutputData().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined output data");
		}
		if (operation.getOutputData().get(0).getHasDataType().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined output data type for operation " + operation.getName());
		}
		return operation.getOutputData().get(0).getHasDataType().get(0).getName();
	}

	public static boolean isOutputRootElementMultiple(Operation operation) {
		if (operation.getOutputData().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined output data");
		}
		if (operation.getOutputData().get(0).getHasDataType().isEmpty()) {
			throw new BCGeneratorException("GIDL model has not defined output data type for operation " + operation.getName());
		}
		return operation.getOutputData().get(0).getHasDataType().get(0).getMaxOccurs().equals(OccurrencesTypes.UNBOUNDED);
	}

	public static String getOperationUri(Operation operation) {
		return operation.getHasScope().getUri();
	}

	public static List<String> getInputDataElements(GIDLModel gidlModel) {

		List<String> elements = new ArrayList<>();
		
		Operation inputOperation = gidlModel.getHasInterfaces().get(0).getHasOperations().get(0);
		DataType inputElement = inputOperation.getInputData().get(0).getHasDataType().get(0);

		if (inputElement instanceof SimpleType) {
			elements.add(inputElement.getName());
		} else {
			ComplexType complexInput = (ComplexType) inputElement;
			for (DataType element : complexInput.getHasDataType()) {
				elements.add(element.getName());
			}
		}

		return elements;
	}
}
