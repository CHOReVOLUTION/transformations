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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;


public class BPELValidatingResourceFactoryImpl extends BPELResourceFactoryImpl {
	
	// Properties for validating BPEL document
	
	protected boolean fValidating = false;
	protected EntityResolver fEntityResolver = null;
	protected ErrorHandler fErrorHandler = null;
	protected ResourceSet fResourceSet = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(BPELValidatingResourceFactoryImpl.class);
	
	protected BPELValidatingResourceFactoryImpl() {	
		fValidating = false;
		fEntityResolver = null;
		fErrorHandler = null;
		fResourceSet = null;
	}
	
	/**
	 * @param resourceSet
	 * @param entityResolver
	 * @param errorHandler
	 */
	@SuppressWarnings("nls")
	public BPELValidatingResourceFactoryImpl(ResourceSet resourceSet, EntityResolver entityResolver, 
			ErrorHandler errorHandler) {	
		
		fValidating = true;
		
		fEntityResolver = entityResolver;
		fErrorHandler = errorHandler;
		fResourceSet = resourceSet;
		
		Resource.Factory.Registry resourceFactoryRegistry = resourceSet.getResourceFactoryRegistry();
		resourceFactoryRegistry.getExtensionToFactoryMap().put("bpel", this);
	}
	
	/**
	 * @see org.eclipse.bpel.model.resource.BPELResourceFactoryImpl#createResource
	 * (org.eclipse.emf.common.util.URI)
	 */
	@Override
	public Resource createResource(URI uri) {
		try {
			return new BPELResourceImpl(uri, fEntityResolver, fErrorHandler);
		} catch (IOException exc) {
			LOGGER.error(exc.getMessage(), exc);
			return null;
		}
	}
}
