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
package eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import eu.chorevolution.modelingnotations.chorarch.ChorArchModel;

public class ChoreographyArchitectureGeneratorTestsUtils {

	public static void save(ChorArchModel chorarchModel, File file) {
		// create resource from the Model
		URI fileUriTempModelNormalized = URI.createFileURI(file.getAbsolutePath());
		Resource resourceModelNormalized = new XMIResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		// add model in model resourceModel
		resourceModelNormalized.getContents().add(chorarchModel);
		try {
			resourceModelNormalized.save(Collections.EMPTY_MAP);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
