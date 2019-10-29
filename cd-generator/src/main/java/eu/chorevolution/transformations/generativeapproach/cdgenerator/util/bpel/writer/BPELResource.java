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

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.bpel.model.Process;
import org.eclipse.emf.ecore.resource.Resource;
import org.xml.sax.ErrorHandler;


public interface BPELResource extends Resource {

    /**
     * Converts the BPEL model to an XML DOM model and then write the DOM model to the output stream.
     * @param out 
     * @param args 
     * @throws Exception 
	 * 
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#doSave(OutputStream, Map)
	 */
	void doSave(OutputStream out, Map<?, ?> args) throws Exception;
	/**
	 * Set an error handler for SAX Parser
	 * @param errorHandler
	 * @see Bugzilla 324165
	 */
	void setErrorHandler(ErrorHandler errorHandler);
	
	/**
	 * Return the current SAX Parser error handler
	 * @return error handler
	 * @see Bugzilla 324165
	 */
	ErrorHandler getErrorHandler();
    
    /**
     * Returns the BPEL process contained by this resource or <code>null</code> if there is none.
     * @return the process
     */
    Process getProcess();

    /** 
     * If unset the default value is {@link org.eclipse.bpel.model.util.BPELConstants#NAMESPACE}.
     * 
     * @return the namespace URI for the process/resource. 
     */
    String getNamespaceURI();    
    
    /**
     * Sets the <code>namespaceURI</code> for the process/resource.
     * Expected to be one of the BPEL namespaces defined by 
     * {@link org.eclipse.bpel.model.util.BPELConstants}.
     * @param namespaceURI the namespace URI
     */
    void setNamespaceURI(String namespaceURI);
    
    /**
     * Returns <code>true</code> if the resource will be saved using a prefix for the BPEL namespace.
     * Returns <code>false</code> if the resource will be saved with BPEL as the default namespace.
     * 
     * @return true/false accordingly 
     */
    boolean getOptionUseNSPrefix();
    
    /**
     * Sets the useNSPrefix option.
     * @param useNSPrefix 
     * @see #getOptionUseNSPrefix()
     */
    void setOptionUseNSPrefix(boolean useNSPrefix);
   
    interface MapListener {
        public void objectAdded(Object key, Object value);
    }
    
}
