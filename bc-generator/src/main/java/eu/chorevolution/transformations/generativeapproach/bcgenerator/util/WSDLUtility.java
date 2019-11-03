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
package eu.chorevolution.transformations.generativeapproach.bcgenerator.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.RequestElementInfo;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.RequestMapper;
import eu.chorevolution.transformations.generativeapproach.bcgenerator.model.WSDLInfo;

public class WSDLUtility {

	public static WSDLInfo readWSDLInfo(byte[] wsdl, String name) throws WSDLException {
		Definition definition = readWSDLFile(wsdl);

		WSDLInfo wsdlInfo = new WSDLInfo();
		wsdlInfo.setDefinition(definition);

		wsdlInfo.setName(name);

		// get the local part of the first service inside the definition
		Collection<?> services =  definition.getAllServices().values();
 
		for (Object serviceObject : services) {
			Service service = (Service) serviceObject;
			if (service.getQName().getLocalPart().startsWith(name)) {
				wsdlInfo.setServiceName(service.getQName().getLocalPart());
			}
		}

		// get name of the first PortType
		Collection<?> portTypes = definition.getAllPortTypes().values();
		for (Object portTypeObject : portTypes) {
			PortType portType = (PortType) portTypeObject;
			if (portType.getQName().getLocalPart().startsWith(name)) {
				wsdlInfo.setPortName(Utility.deleteSpecialChar(portType.getQName().getLocalPart()));
			}
		}
		
		// get target namespace of the definition
		wsdlInfo.setTargetNS(definition.getTargetNamespace());

		try {
			Document wsdlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(wsdl));
			wsdlInfo.setTypes(((Document) wsdlDocument).getElementsByTagName("xsd:complexType"));
			wsdlInfo.setMessageElements(((Document) wsdlDocument).getElementsByTagName("xsd:element"));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		return wsdlInfo;
	}

	private static Definition readWSDLFile(byte[] wsdl) throws WSDLException {

		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		reader.setFeature("javax.wsdl.verbose", false);
		reader.setFeature("javax.wsdl.importDocuments", true);
		return reader.readWSDL(null, new InputSource(new ByteArrayInputStream(wsdl)));
	}

	public static String getOperationName(WSDLInfo wsdlInfo) {
		PortType portType = null;
		
		Collection<?> portTypes = wsdlInfo.getDefinition().getAllPortTypes().values();
		for (Object portTypeObject : portTypes) {
			PortType currentPortType = (PortType) portTypeObject;
			if (currentPortType.getQName().getLocalPart().startsWith(wsdlInfo.getName())) {
				portType = currentPortType;
				break;
			}
		}
		return ((Operation) portType.getOperations().get(0)).getName();
	}

	public static String getInputMessageRootItemName(WSDLInfo wsdlInfo) {
		PortType portType = null;
		
		Collection<?> portTypes = wsdlInfo.getDefinition().getAllPortTypes().values();
		for (Object portTypeObject : portTypes) {
			PortType currentPortType = (PortType) portTypeObject;
			if (currentPortType.getQName().getLocalPart().startsWith(wsdlInfo.getName())) {
				portType = currentPortType;
				break;
			}
		}
		String inputMessageElementName = ((Operation) portType.getOperations().get(0)).getInput().getMessage()
				.getPart("parameters").getElementName().getLocalPart();
		NodeList wsdlElements = wsdlInfo.getMessageElements();
		for (int i = 0; i < wsdlElements.getLength(); i++) {
			Node element = wsdlElements.item(i);
			if (inputMessageElementName.equals(element.getAttributes().getNamedItem("name").getNodeValue())) {
				return element.getAttributes().getNamedItem("type").getNodeValue();
			}
		}
		return inputMessageElementName;
	}

	public static String getOutputMessageRootItemName(WSDLInfo wsdlInfo) {
		PortType portType = null;
		
		Collection<?> portTypes = wsdlInfo.getDefinition().getAllPortTypes().values();
		for (Object portTypeObject : portTypes) {
			PortType currentPortType = (PortType) portTypeObject;
			if (currentPortType.getQName().getLocalPart().startsWith(wsdlInfo.getName())) {
				portType = currentPortType;
				break;
			}
		}
		String outputMessageElementName = ((Operation) portType.getOperations().get(0)).getOutput().getMessage()
				.getPart("parameters").getElementName().getLocalPart();
		NodeList wsdlElements = wsdlInfo.getMessageElements();
		for (int i = 0; i < wsdlElements.getLength(); i++) {
			Node element = wsdlElements.item(i);
			if (outputMessageElementName.equals(element.getAttributes().getNamedItem("name").getNodeValue())) {
				return element.getAttributes().getNamedItem("type").getNodeValue();
			}
		}
		return outputMessageElementName;
	}

	public static RequestMapper getInputMessageMapper(WSDLInfo wsdlInfo) {

		RequestMapper requestMapper = new RequestMapper();

		// ASSUMPTION: the structure of the message is the following: Request
		// wrapper type, request type, parameters

		// Find wrapper
		String wrapperTypeName = getInputMessageRootItemName(wsdlInfo);
		Node wrapperTypeElement = findType(wrapperTypeName, wsdlInfo);
		requestMapper.setRequestWrapperTypeName(wrapperTypeName);

		// Find type
		NodeList wrapperInnerElements = getComplexTypeInnerNodes(wrapperTypeElement);
		Node requestTypeElement = wrapperInnerElements.item(1);
		requestMapper.setRequestWrapperParameterName(requestTypeElement.getAttributes().getNamedItem("name").getNodeValue());
		String requestTypeName = requestTypeElement.getAttributes().getNamedItem("type").getNodeValue();
		requestMapper.setRequestWrapperTypeName(requestTypeName);

		// Find elements
		Node requestType = findType(requestTypeName, wsdlInfo);
		NodeList requestElements = requestType.getChildNodes().item(1).getChildNodes();
		for (int i = 0; i < requestElements.getLength(); i++) {
			Node requestElement = requestElements.item(i);
			if (!"#text".equals(requestElement.getNodeName())) {
				String elementName = requestElement.getAttributes().getNamedItem("name").getNodeValue();
				String elementType = requestElement.getAttributes().getNamedItem("type").getNodeValue();

				RequestElementInfo elementInfo = new RequestElementInfo();
				elementInfo.setName(elementName);
				if ("xsd:string".equals(elementType)) {
					elementType = "String";
				} else if ("xsd:integer".equals(elementType)) {
					elementType = "int";
				} else if (elementType.startsWith("xsd:")) {
					elementType = elementType.substring(4, elementType.length());
				}

				if (elementType.startsWith("tns:")) {
					elementType = elementType.substring(4, elementType.length());
				}

				if(!("boolean".equals(elementType) || "int".equals(elementType) || "integer".equals(elementType) || "long".equals(elementType) || "float".equals(elementType) || "double".equals(elementType) || "String".equals(elementType))) {
					elementInfo.setEnumeration(true);
				} else {
					elementInfo.setEnumeration(false);
				}

				
				elementInfo.setType(elementType);
				requestMapper.getRequestElements().put(elementName, elementInfo);
			}
		}
		return requestMapper;
	}

	public static Node findType(String typeName, WSDLInfo wsdlInfo) {
		NodeList wsdlTypes = wsdlInfo.getTypes();

		for (int i = 0; i < wsdlTypes.getLength(); i++) {
			Node element = wsdlTypes.item(i);
			if (typeName.equals(element.getAttributes().getNamedItem("name").getNodeValue())) {
				return element;
			}
		}
		return null;
	}

	public static NodeList getComplexTypeInnerNodes(Node node) {
		NodeList innerNodes = node.getChildNodes();
		for (int i = 0; i < innerNodes.getLength(); i++) {
			Node innerNode = innerNodes.item(i);
			if ("xsd:sequence".equals(innerNode.getNodeName())) {
				return innerNode.getChildNodes();
			}
		}
		return null;
	}
}
