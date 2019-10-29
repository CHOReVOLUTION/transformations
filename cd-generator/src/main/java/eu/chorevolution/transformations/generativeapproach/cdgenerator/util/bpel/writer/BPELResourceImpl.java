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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.adapters.INamespaceMap;
import org.eclipse.bpel.model.util.BPELConstants;
import org.eclipse.bpel.model.util.BPELProxyURI;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.model.util.ImportResolver;
import org.eclipse.bpel.model.util.ImportResolverRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;


public class BPELResourceImpl extends XMLResourceImpl implements BPELResource { 
	protected static boolean USE_IMPORTS = false;

    /** @see #getNamespaceURI() */
    private String processNamespaceURI = BPELConstants.NAMESPACE;
    
    /** @see #getOptionUseNSPrefix() */
    private boolean optionUseNSPrefix = true;
    
	// Properties for validating bpel document
	protected boolean validating = false;
	protected EntityResolver entityResolver = null;
	protected ErrorHandler errorHandler = null;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BPELResourceImpl.class);
	
	public BPELResourceImpl() {
		super();
	}

	public BPELResourceImpl(URI arg0) {
		super(arg0);
	}
	
	public BPELResourceImpl(URI uri, EntityResolver entityResolver, ErrorHandler errorHandler)
			throws IOException {
		super(uri);
		this.entityResolver = entityResolver;
		this.errorHandler = errorHandler;
		validating = true;
	}

	public void setErrorHandler(ErrorHandler errorHandler){
		this.errorHandler = errorHandler;
		validating = true;
	}

	public ErrorHandler getErrorHandler(){
		return errorHandler;
	}

	/**
	 * Convert the BPEL model to an XML DOM model and then write the DOM model
	 * to the output stream.
	 */
	@Override
	public void doSave(OutputStream out, Map<?, ?> args) throws IOException{
        INamespaceMap<String, String> nsMap = BPELUtils.getNamespaceMap(this.getProcess());
        if (getOptionUseNSPrefix()) {
        	nsMap.remove("");
            List<String> prefix = nsMap.getReverse(getNamespaceURI());
            if (prefix.isEmpty()){
            	nsMap.put(BPELConstants.PREFIX, getNamespaceURI());
            }
        } else {
            nsMap.put("", getNamespaceURI());
        }

        File outFile = null;
        byte[] bytes = null;
        IOUtils.write(bytes, out);
        FileUtils.writeByteArrayToFile(outFile, bytes);
        BPELWriter writer = new BPELWriter();
		BPELData bpelData = new BPELData();
		bpelData.setProcess(this.getProcess());
		writer.write(bpelData, outFile);
	}
	
	/** 
	 * Convert a BPEL XML document into the BPEL EMF model.
     * After loading, the process' checks for process type (executable/abstract)
     * and resets the current namespace accordingly. If the process type is abstract
     * and no profile has been set, the default abstract process profile is being inserted. 
     * 
	 */
	@Override
	public void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException{
      
        BPELReader reader = null;
        Document document = null;
        
        if (options != null 
        		&& (document = (org.w3c.dom.Document)options.get("DOMDocument")) != null) {
        	reader = new BPELReader();
        	reader.read(this, document);
     	
        } else {
        	try {
        		reader = new BPELReader( getDOMParser() );
        	} catch (IOException ioe) {
				LOGGER.error(ioe.getMessage(), ioe);
        		throw ioe;
        	} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);      		
        		throw new IOException("Problem create parser");
        	}
    		reader.read(this, inputStream);
        }
    	
    	//Check for process type Abstract/ Executable
    	Element processElement = (document != null)? document.getDocumentElement(): null;
    	if (processElement != null && reader.isAbstractProcess(processElement))
    	{
    		setNamespaceURI(BPELConstants.NAMESPACE_ABSTRACT_2007);
    		//TODO: Let user decide whether to use a profile
    		if (reader.getProfileNamespace(processElement) != null){
    			getProcess().setAbstractProcessProfile(reader.getProfileNamespace(processElement));   			
    		} else {
    			getProcess().setAbstractProcessProfile(BPELConstants.NAMESPACE_ABSTRACT_PROFILE);
    		}
		} else {
        	setNamespaceURI(BPELConstants.NAMESPACE);
        }
    	
    	boolean usePrefix =
    		checkUseNSPrefix(BPELConstants.NAMESPACE_2004) ||
    		checkUseNSPrefix(BPELConstants.NAMESPACE_2007) ||
    		checkUseNSPrefix(BPELConstants.NAMESPACE_ABSTRACT_2007);
    	this.setOptionUseNSPrefix(usePrefix);
	}

	private boolean checkUseNSPrefix(String bpelNamespace) {
        INamespaceMap<String, String> nsMap = BPELUtils.getNamespaceMap(getProcess());
        List<String> prefixes;
        prefixes = nsMap.getReverse(bpelNamespace);
        for (int i=0; i<prefixes.size(); ++i) {
        	String ns = prefixes.get(i);
        	if (ns!=null && !ns.equals("")) {
        		return true;
        	}
        }
        return false;
	}
        
    /*
     * TODO Implement getURIFragment to return our encoding.
     */
    @Override
	public String getURIFragment(EObject eObject)
    {
        return super.getURIFragment(eObject);
    }
    
    /**
     * Find and return the EObject represented by the given uriFragment.
     *
     * @return the resolved EObject or null if none could be found.
     */
    @Override
	public EObject getEObject(String uriFragment) {
	    if (uriFragment == null) return null;
	    try {
	    	// Consult the superclass
		    EObject eObject = super.getEObject(uriFragment);
		    if (eObject != null) return eObject;
		    // Consult our helper method
	        eObject = getEObjectExtended(uriFragment);
	        if (eObject != null) return eObject;
			return null;
	    } catch (RuntimeException e) {
			LOGGER.error(e.getMessage(), e);
	        throw e;
	    }
	}

    /**
     * Helper method for resolving the EObject.
     * 
     */
    protected EObject getEObjectExtended(String uriFragment) 
    {
    	// RTP: this implementation should be extensible
    	
        BPELProxyURI proxyURI = new BPELProxyURI(uriFragment);
        
        QName qname = proxyURI.getQName();
        String typeName = proxyURI.getTypeName();

        if (qname == null || typeName == null) {
            return null;
        }
        
        EObject result = null;
        
    	// Try the BPEL imports if any exist.
        Process process = getProcess();
        if (process == null) {
        	return result;
        }
        

        for (Import imp : process.getImports()){
            // The null and "" problem ...
            String ns = imp.getNamespace();
            if (ns == null) {
            	ns = javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
            }
            
            if (ns.equals(qname.getNamespaceURI()) == false || 
             	imp.getLocation() == null ) {
            	continue;
            }

            for (ImportResolver r : ImportResolverRegistry.INSTANCE.getResolvers(imp.getImportType())){
                result = r.resolve(imp, qname, proxyURI.getID(), proxyURI.getTypeName());
                if (result != null) {
                    return result;
                }
            }
        }
        
        // Failed to resolve.
        return result;
    }

   
	public Process getProcess() {
	    return getContents().size() == 1 && getContents()
	    		.get(0) instanceof Process ? (Process) getContents().get(0) : null;
	}
		
	protected DocumentBuilder getDocumentBuilder() throws IOException {
		
		final DocumentBuilderFactory factory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();

		if (validating && factory.getClass().getName().indexOf("org.apache.xerces") != -1)
		{
		  // turn dynamic schema validation on
		  factory.setAttribute("http://apache.org/xml/features/validation/dynamic", Boolean.TRUE);
		  // turn schema validation on
		  factory.setAttribute("http://apache.org/xml/features/validation/schema", Boolean.TRUE);
		  // set the default schemaLocation for syntactical validation
          factory.setAttribute("http://apache.org/xml/properties/schema/external-schemaLocation",
               BPELConstants.NAMESPACE);
		}

		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);

		factory.setValidating(validating);
		factory.setNamespaceAware(true);

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException exc) {
			LOGGER.error(exc.getMessage(), exc);
			throw new IOException(exc.toString());
		}
		if (validating) {
			builder.setEntityResolver( entityResolver );
			builder.setErrorHandler( errorHandler );
		}

		return builder;
	}
	
	

	
	@SuppressWarnings({ "nls", "boxing" })
	protected DOMParser getDOMParser() throws Exception {


		DOMParser domParser = new DOMParser () {

			protected XMLLocator mLocator;
			
			protected int fLineNo = 0;
			protected int fColumnNo = 0;
			protected int fOffset = 0;
			
			void lastSource () {
				
				fLineNo = mLocator.getLineNumber();
				fColumnNo = mLocator.getColumnNumber();
				fOffset = mLocator.getCharacterOffset() ;

			}
					
			/**
			 * @see org.apache.xerces.parsers.AbstractDOMParser#startDocument(
			 * org.apache.xerces.xni.XMLLocator, java.lang.String, org.apache.xerces.xni.NamespaceContext,
			 * org.apache.xerces.xni.Augmentations)
			 */
			@Override
			public void startDocument(XMLLocator arg0, String arg1, NamespaceContext arg2,
					Augmentations arg3) throws XNIException {
				mLocator = arg0;							
				super.startDocument(arg0, arg1, arg2, arg3);
				lastSource();					
			}
			

			

			/**
			 * @see org.apache.xerces.parsers.AbstractDOMParser#characters(org.apache.xerces.xni.XMLString, 
			 * org.apache.xerces.xni.Augmentations)
			 */
			@Override
			public void characters(XMLString arg0, Augmentations arg1) throws XNIException {
				super.characters(arg0, arg1);
				lastSource();
			}




			/**
			 * @see org.apache.xerces.parsers.AbstractDOMParser#comment(org.apache.xerces.xni.XMLString, 
			 * org.apache.xerces.xni.Augmentations)
			 */
			@Override
			public void comment(XMLString arg0, Augmentations arg1) throws XNIException {
				super.comment(arg0, arg1);
				lastSource();
				
			}



			/* (non-Javadoc)
			 * @see org.apache.xerces.parsers.AbstractDOMParser#textDecl(java.lang.String, java.lang.String,
			 *  org.apache.xerces.xni.Augmentations)
			 */
			@Override
			public void textDecl(String arg0, String arg1, Augmentations arg2) throws XNIException {
				super.textDecl(arg0, arg1, arg2);
				lastSource();												
			}



			/**
			 * @see org.apache.xerces.parsers.AbstractDOMParser#startElement(org.apache.xerces.xni.QName, 
			 * org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
			 */
			@Override
			public void startElement (org.apache.xerces.xni.QName arg0, XMLAttributes arg1,
					Augmentations arg2) throws XNIException {				
				super.startElement(arg0, arg1, arg2);
				// p("startElement: {0} {1}", arg0,arg1);
				
				if (fCurrentNode != null) {				
					// start of element
					fCurrentNode.setUserData("location.line", fLineNo, null);
					fCurrentNode.setUserData("location.column", fColumnNo, null);									
					fCurrentNode.setUserData("location.charStart", fOffset+1, null);
					fCurrentNode.setUserData("location.charEnd", fOffset + arg0.rawname.length()+1 , null);
					
					// end of element
					fCurrentNode.setUserData("location2.line", mLocator.getLineNumber(), null);
					fCurrentNode.setUserData("location2.column", mLocator.getColumnNumber(), null);
					fCurrentNode.setUserData("location2.charStart", mLocator.getCharacterOffset(), null);
					fCurrentNode.setUserData("location2.charEnd", mLocator.getCharacterOffset(), null);					
				}				
				lastSource();
			}
			
			
			@Override
			public void startCDATA( Augmentations aug ) {				
				super.startCDATA(aug);
				lastSource();
				
			}
			
			@Override
			public void endCDATA( Augmentations aug ) {				
				super.endCDATA(aug);
				lastSource();				
			}
			
			@Override
			public void endElement ( org.apache.xerces.xni.QName element, Augmentations aug ) {							
				super.endElement(element, aug);	
				lastSource();	
			}			
			
		};
						
		if (validating){
		  // turn dynamic schema validation on
		  domParser.setFeature("http://apache.org/xml/features/validation/dynamic", Boolean.TRUE);
		  // turn schema validation on
		  domParser.setFeature("http://apache.org/xml/features/validation/schema", Boolean.TRUE);
		  // set the default schemaLocation for syntactical validation
		  domParser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
              BPELConstants.NAMESPACE);
		}				
		domParser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false );
		domParser.setFeature( "http://apache.org/xml/features/xinclude", false);
		
		if (validating) {
			domParser.setEntityResolver( entityResolver );
			domParser.setErrorHandler( errorHandler );
		}
		
		return domParser;
	}
	
	
	public static void setUseImports(boolean useImports) {
	    USE_IMPORTS = useImports;
	}

    public String getNamespaceURI() {
        return processNamespaceURI;
    }
    
    public void setNamespaceURI(String namespaceURI) {
        processNamespaceURI = namespaceURI;
    }
    
    public boolean getOptionUseNSPrefix() {
        return optionUseNSPrefix;
    }
    
    public void setOptionUseNSPrefix(boolean useNSPrefix) {
        optionUseNSPrefix = useNSPrefix;
    }
}	
