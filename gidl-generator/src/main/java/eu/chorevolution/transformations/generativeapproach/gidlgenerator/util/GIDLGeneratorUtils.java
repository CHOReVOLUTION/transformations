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
package eu.chorevolution.transformations.generativeapproach.gidlgenerator.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.modelingnotations.gidl.Scope;
import eu.chorevolution.modelingnotations.gidl.impl.GidlFactoryImpl;
import eu.chorevolution.transformations.generativeapproach.gidlgenerator.GIDLGeneratorException;

public class GIDLGeneratorUtils {
	private static final String TEMPFILE_SUFFIX = "gidlgenerator";
	private static final String GIDL_FILE_EXTENSION = ".gidl";
	private static final String OPERATION_VERB_POST = "POST";

	public static byte[] geGIDLContent(final GIDLModel gidlModel) throws GIDLGeneratorException {

		File gidlFile;
		try {
			gidlFile = File.createTempFile(TEMPFILE_SUFFIX, GIDL_FILE_EXTENSION);
		} catch (IOException e) {
			throw new GIDLGeneratorException("Internal Error while creating the GIDL Model");
		}

		// create resource from the Model
		URI fileUriTempModelNormalized = URI.createFileURI(gidlFile.getAbsolutePath());
		Resource resourceModelNormalized = new XMIResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		// add model in model resourceModel
		resourceModelNormalized.getContents().add(gidlModel);

		byte[] gidlContent;

		try {
			resourceModelNormalized.save(Collections.EMPTY_MAP);
			gidlContent = FileUtils.readFileToByteArray(gidlFile);

		} catch (IOException e) {
			throw new GIDLGeneratorException("Internal Error while reading the GIDL Model");
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(gidlFile);
		}

		return gidlContent;

	}

	public static Scope createScope(String name) {
		Scope gidlScope = GidlFactoryImpl.eINSTANCE.createScope();
		gidlScope.setName(name);
		gidlScope.setUri(name);
		// START FIX to use POST verb 
		gidlScope.setVerb(OPERATION_VERB_POST);
		// END FIX
		return gidlScope;
	}
	// -----------------------------------------------------------------------

	/**
	 * <p>
	 * {@code GIDLGeneratorUtils} instances should NOT be constructed in
	 * standard programming. Instead, the class should be used statically.
	 * </p>
	 *
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean
	 * instance to operate.
	 * </p>
	 */
	public GIDLGeneratorUtils() {
		super();
	}
}
