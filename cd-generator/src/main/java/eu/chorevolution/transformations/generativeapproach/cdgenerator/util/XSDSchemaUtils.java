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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.xerces.xs.XSModel;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;

public class XSDSchemaUtils {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(WSDLUtils.class);
		
	public static XmlSchema getXmlSchema(byte[] content){

		InputStream is = new ByteArrayInputStream(content);
		XmlSchemaCollection schemaCol = new XmlSchemaCollection();
		XmlSchema schema = schemaCol.read(new StreamSource(is));
		return schema;	
	}
	
	public static Element getElementFromXmlSchema(XmlSchema schema){

		// log execution flow
		LOGGER.entry();	
		Element schemaElement = null;
		Writer writer = new StringWriter();
		schema.write(writer);			
		try {
			schemaElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(
					new StringReader(writer.toString()))).getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into getElementFromXmlSchema see log file for details.");
		}	
		// log execution flow
		LOGGER.exit();	
		return schemaElement; 
	}
	
	public static String getXmlSchemaTns(XmlSchema schema){
		
		return schema.getTargetNamespace();
	}
	
	public static String generateSampleXMLofElement(String xsdpath, QName element){

		// log execution flow
		LOGGER.entry();	
		XSModel xsModel = new XSParser().parse(xsdpath);
		XSInstance xsInstance = new XSInstance();
		// avoid generation of comments
		xsInstance.showContentModel = Boolean.FALSE;
		// set xsInstance to generate only 1 element
		xsInstance.minimumElementsGenerated = 1;
		xsInstance.maximumElementsGenerated = 1;
		xsInstance.minimumListItemsGenerated = 1;
		xsInstance.maximumListItemsGenerated = 1;

		StringWriter writer = new StringWriter();
		try {		
			XMLDocument sampleXml = new XMLDocument(new StreamResult(writer), true, 4, null);
			xsInstance.generate(xsModel, element, sampleXml);
		} catch (TransformerConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into generateSampleXMLofAnElement see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
		return writer.toString();
	}

}
