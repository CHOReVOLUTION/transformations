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
package eu.chorevolution.transformations.generativeapproach.adgenerator.util;

import java.io.ByteArrayInputStream;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.xml.sax.InputSource;

import eu.chorevolution.transformations.generativeapproach.adgenerator.model.WSDLInfo;

public class WSDLUtility {

	public static WSDLInfo readWSDLInfo(byte[] wsdl) throws WSDLException{
		Definition definition = readWSDLFile(wsdl);

		WSDLInfo wsdlInfo = new WSDLInfo();
		wsdlInfo.setDefinition(definition);
		
		// get the local part of the first service inside the definition
		Service service = (Service) definition.getAllServices().get(definition.getAllServices().keySet().toArray()[0]);
		wsdlInfo.setServiceName(service.getQName().getLocalPart());
		String portName = ((PortType) definition.getAllPortTypes().get(definition.getAllPortTypes().keySet().toArray()[0])).getQName().getLocalPart();
				
		// get name of the first PortType 
		wsdlInfo.setPortName(Utility.deleteSpecialChar(portName));
		// get target namespace of the definition
		wsdlInfo.setTargetNS(definition.getTargetNamespace());
		return wsdlInfo;
	}
	
	private static Definition readWSDLFile(byte[] wsdl) throws WSDLException {

		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		reader.setFeature("javax.wsdl.verbose", false);
		reader.setFeature("javax.wsdl.importDocuments", true);
		return reader.readWSDL(null, new InputSource(new ByteArrayInputStream(wsdl)));
	}
}
