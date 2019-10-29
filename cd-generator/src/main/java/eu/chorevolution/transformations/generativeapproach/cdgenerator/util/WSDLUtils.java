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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.DataTypeWSDLInformation;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.MessageData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.OperationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ServiceDeployBPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLDefinitionData;

public class WSDLUtils {
	
	private static final String BINDING_NAME_SUFFIX = "Binding";
	private static final String CALLBACK_NAME_SUFFIX = "Callback";
	private static final String COLON = ":";
	private static final String CHOREOGRAPHY_ID_TYPENAME = "choreographyId";	
	private static final String CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME = "choreographyInstanceRequest";	
	private static final String CHOREOGRAPHY_TASK_NAME_ELEMENTNAME = "choreographyTaskName";
	private static final String COMPLEX_TYPE_XSD_LABEL = "complexType";
	private static final String CDNAME_NAME_PREFIX = "cd";
	private static final String ELEMENT_REQUEST_SUFFIX = "ElementRequest";
	private static final String ELEMENT_RESPONSE_SUFFIX = "ElementResponse";	
	private static final String ELEMENT_XSD_LABEL = "element";
	private static final String HYPHEN = "-";
	private static final String INPUT_MESSAGE_DATA_ELEMENTNAME = "inputMessageData";	
	private static final String INPUT_MESSAGE_NAME_ELEMENTNAME = "inputMessageName";
	private static final String LOOP_INDEXES_ELEMENTNAME = "loopIndexes";
	private static final String OUTPUT_MESSAGE_DATA_ELEMENTNAME = "outputMessageData";
	private static final String OUTPUT_MESSAGE_NAME_ELEMENTNAME = "outputMessageName";	
	private static final String LITERAL_NAME_LABEL = "literal";	
	private static final String MESSAGE_DATA_ELEMENTNAME = "messageData";
	private static final String MESSAGE_REQUEST_SUFFIX = "MessageRequest";
	private static final String MESSAGE_RESPONSE_SUFFIX = "MessageResponse";
	private static final String NAME_LABEL = "name";
	private static final String NAMESPACE_SEPARATOR = ":";	
	private static final String PARAMETERS_NAME_LABEL = "parameters";	
	private static final String PORT_TYPE_SUFFIX = "PT";
	private static final String PORT_NAME_SUFFIX = "Port";
	private static final String SENDER_PARTICIPANT_NAME_ELEMENTNAME = "senderParticipantName";
	private static final String SEND_OPERATION_PREFIX = "send";
	private static final String SEND_REQUEST_TYPE_TYPENAME = "sendRequestType";
	private static final String SEND_REQUEST_TYPE_WITH_LOOP_TYPENAME = "sendRequestTypeWithLoop";
	private static final String SEQUENCE_XSD_LABEL = "sequence";
	private static final String SERVICE_NAME_SUFFIX = "Service";
	private static final String SOAP_BINDING_STYLE_VALUE = "document";
	private static final String SOAP_BINDING_TRANSPORT_URI_VALUE = "http://schemas.xmlsoap.org/soap/http";	 
	private static final String SLASH = "/";
	private static final String RECEIVER_PARTICIPANT_NAME_ELEMENTNAME = "receiverParticipantName";	
	private static final String RECEIVE_OPERATION_PREFIX = "receive";
	private static final String RETURN_SUFFIX = "Return";
	private static final String ROLE_SUFFIX ="Role";
	private static final String TARGET_NS_ATTRIBUTE_NAME = "targetNamespace";
	private static final String TYPE_LABEL = "type";
	private static final String TYPE_SUFFIX = "Type";	
	private static final String TYPES_FILENAME = "types.xsd";
	private static final String WSDL_FILE_EXTENSION = ".wsdl";
	private static final String XMLNS_LABEL = "xmlns";
	private static final String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
	private static final String XMLSCHEMA_STRINGTYPE_NAME = "string";	
	private static final String XMLNS_TNS_LABEL = "xmlns:tns";
		
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(WSDLUtils.class);
	
	public static String formatParticipantName(String name){
		
		return StringUtils.lowerCase(StringUtils.deleteWhitespace(name));
	}	
	
	public static String formatParticipantNameForWSDL(String name){
		
		return StringUtils.lowerCase(StringUtils.remove(StringUtils.deleteWhitespace(name),HYPHEN));
	}
	
	public static String formatOperationName(String operationName){
		
		return StringUtils.uncapitalize(StringUtils.deleteWhitespace(operationName));
	}
		
	public static String createPortTypeName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(PORT_TYPE_SUFFIX).toString());		
	}
	
	public static String createPortName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(PORT_NAME_SUFFIX).toString());		
	}
	
	public static String createBindingName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(BINDING_NAME_SUFFIX).toString());
	}
	
	public static String createNamespace(String namespaceURI, String localPart){
		
		return new StringBuilder(namespaceURI).append(SLASH).append(localPart).toString();
	}
	
	public static String createServiceName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(SERVICE_NAME_SUFFIX).toString());
	}
	
	public static String createCDbaseName(String initatingParticipant, String receivingParticipant){
		
		return new StringBuilder(CDNAME_NAME_PREFIX).append(WSDLUtils
				.formatParticipantNameForWSDL(receivingParticipant)).append(WSDLUtils
						.formatParticipantNameForWSDL(initatingParticipant)).toString();
	}
	
	public static String createCDName(String participantName){
		
		return new StringBuilder(CDNAME_NAME_PREFIX).append(WSDLUtils
				.formatParticipantNameForWSDL(participantName)).toString();
	}	
	
	public static String createCDcallbackBaseName(String initatingParticipant, String receivingParticipant){
		
		return new StringBuilder(CDNAME_NAME_PREFIX).append(WSDLUtils
				.formatParticipantNameForWSDL(receivingParticipant)).append(WSDLUtils
						.formatParticipantNameForWSDL(initatingParticipant)).append(CALLBACK_NAME_SUFFIX)
						.toString();
	}
		
	public static String createCDAddress(String base, String localPart){
		
		return new StringBuilder(base).append(StringUtils.deleteWhitespace(localPart)).toString();
	}
	
	public static String createProsumerPartAddress(String base, String localPart){
		
		return new StringBuilder(base).append(StringUtils.deleteWhitespace(localPart)).append(SLASH)
				.append(StringUtils.deleteWhitespace(localPart)).toString();
	}

	public static String createSendOperationName(String name){

		return new StringBuilder(SEND_OPERATION_PREFIX).append(StringUtils.capitalize(name)).toString();
	}
	
	public static String createInputMessageName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(MESSAGE_REQUEST_SUFFIX)
				.toString());
	}
	
	public static String createOutputMessageName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(MESSAGE_RESPONSE_SUFFIX)
				.toString());
	}	

	public static String createInputMessageElementName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(ELEMENT_REQUEST_SUFFIX)
				.toString());
	}
	
	public static String createOutputMessageElementName(String name){
		
		return StringUtils.deleteWhitespace(new StringBuilder(name).append(ELEMENT_RESPONSE_SUFFIX)
				.toString());
	}		
	
	public static String createSendDefinitionsElementsBaseName(String name){
		
		return new StringBuilder(SEND_OPERATION_PREFIX).append(StringUtils.capitalize(StringUtils
				.deleteWhitespace(name))).toString();		
	}

	public static String createReceiveDefinitionsElementsBaseName(String name){
		
		return new StringBuilder(RECEIVE_OPERATION_PREFIX).append(StringUtils.capitalize(StringUtils
				.deleteWhitespace(name))).toString();		
	}	
	
	public static String createSendInputMessageName(String name){

		return new StringBuilder(SEND_OPERATION_PREFIX).append(StringUtils.capitalize(name))
				.append(MESSAGE_REQUEST_SUFFIX).toString();
	}
	
	public static String createSendOutputMessageName(String name){

		return new StringBuilder().append(name).append(MESSAGE_RESPONSE_SUFFIX).toString();
	}
	
	public static String createReceiveOperationName(String name){

		return new StringBuilder(RECEIVE_OPERATION_PREFIX).append(StringUtils.capitalize(name)).toString();
	}
	
	public static String createReceiveInputMessageName(String name){

		return new StringBuilder(RECEIVE_OPERATION_PREFIX).append(StringUtils.capitalize(name))
				.append(MESSAGE_REQUEST_SUFFIX).toString();
	}
	
	public static String createReceiveOutputMessageName(String name){

		return new StringBuilder(RECEIVE_OPERATION_PREFIX).append(StringUtils.capitalize(name))
				.append(MESSAGE_RESPONSE_SUFFIX).toString();
	}

	public static String createInputMessageTypeName(String name){

		return new StringBuilder(name).append(TYPE_SUFFIX).toString();
	}	
	
	public static String createOutputMessageTypeName(String name){

		return new StringBuilder(name).append(RETURN_SUFFIX).append(TYPE_SUFFIX).toString();
	}
	
	public static String createImportLocationValue(String name){
		
		return new StringBuilder(name).append(WSDL_FILE_EXTENSION).toString();
	}
		
	public static String createPartnerLinkRoleNameValue(String name){
		
		return new StringBuilder(name).append(ROLE_SUFFIX).toString();
	}
	
	public static String getLocalPartOfNamespaceIdentifier(String namespace){
		
		return StringUtils.split(namespace,NAMESPACE_SEPARATOR)[1]; 
	}
	
	public static void writeWSDLDefinitionToFile(Definition wsdl, String destination, String fileName){
		
		// log execution flow
		LOGGER.entry();
		try {
			Document documentWSDL = WSDLFactory.newInstance().newWSDLWriter().getDocument(wsdl);
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
	        DOMSource source = new DOMSource(documentWSDL);
	        StreamResult sr = new StreamResult(new File(createWSDLFilePath(destination,fileName)));
	        transformer.transform(source, sr);   
		} catch (TransformerFactoryConfigurationError | TransformerException | WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into writeWSDLDescriptionToFile see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
	}

	public static void writeWSDLDefinitionsToDestinationFolder(
			Map<String,WSDLDefinitionData> wsdlDefinitions, String destination){
	
		// log execution flow
		LOGGER.entry();
		for (Entry<String, WSDLDefinitionData> wsdlDefinitionData : wsdlDefinitions.entrySet()) {
			WSDLUtils.writeWSDLDefinitionToFile(wsdlDefinitionData.getValue().getWsdl(), destination,
					wsdlDefinitionData.getValue().getWsdlFileName());			
		}
		// log execution flow
		LOGGER.exit();		
	}
	
	public static void writeWSDLDocumentToFile(Document wsdl, String destination, String fileName){
	
		// log execution flow
		LOGGER.entry();		
		try {
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
	        DOMSource source = new DOMSource(wsdl);
	        StreamResult sr = new StreamResult(new File(createWSDLFilePath(destination, fileName)));
	        transformer.transform(source, sr);   
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into writeWSDLDocumentToFile see log file for details ");
		}
		// log execution flow
		LOGGER.exit();	
	}
	
	public static Definition parseWSDLDocumentToDefinition(Document wsdlDocument){
		
		// log execution flow
		LOGGER.entry();	
		WSDLFactory wsdlFactory;
		Definition wsdlDefinition;
		try {
			wsdlFactory = WSDLFactory.newInstance();
			WSDLReader reader = wsdlFactory.newWSDLReader();
			wsdlDefinition = reader.readWSDL(null, wsdlDocument);
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into parseWSDLDocumentToDefinition see log file for details ");
		}
		// log execution flow
		LOGGER.exit();	
		return wsdlDefinition;
	}
	
	public static byte[] readWSDLtoByteArray(String destination, String fileName){
		
		// log execution flow
		LOGGER.entry();
		byte[] wsdl = null;
		try {
			wsdl = FileUtils.readFileToByteArray(new File(createWSDLFilePath(destination, fileName)));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Exception into readWSDLasByteArray see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
		return wsdl;
	}

	public static String createWSDLFilePath(String destination, String fileName){
		
		return new StringBuilder(destination).append(File.separator).append(fileName)
				.append(WSDL_FILE_EXTENSION).toString();
	}
	
	public static void writeWSDLsToDestinationFolder(List<WSDLData> wsdlData,String destination){

		// log execution flow
		LOGGER.entry();
		for (WSDLData data : wsdlData) {
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setNamespaceAware(true);
		    DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			    Document document = builder.parse(new ByteArrayInputStream(data.getWsdl()));
			    writeWSDLDocumentToFile(document, destination, WSDLUtils.formatParticipantNameForWSDL(data
			    		.getParticipantName()));
			} catch (ParserConfigurationException | SAXException | IOException e) {
				LOGGER.error(e.getMessage(),e);
				throw new CDGeneratorException(
						"Exception into writeWSDLsToDestinationFolder see log file for details ");
			}
		}
		// log execution flow
		LOGGER.exit();
	}
	
	public static String getTargetNamespace(Document document){
		
		// log execution flow
		LOGGER.entry();
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
		NamespaceResolver namespaceResolver = new NamespaceResolver();
		xpath.setNamespaceContext(namespaceResolver);
		NodeList nodes = null;
		try {
			nodes = (NodeList) ((Object) xpath.evaluate("/definitions",document, XPathConstants.NODESET));
		} catch (XPathExpressionException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Exception into getTargetNamespace see log file for details ");
		}		
		String tns = nodes.item(0).getAttributes().getNamedItem("targetNamespace").getNodeValue();
		// log execution flow
		LOGGER.exit();
		return tns;
	}
	
	public static boolean existOperationIntoPortType(PortType portType, String operationName){
		
		// log execution flow
		LOGGER.entry();
		Iterator<?> iterator = portType.getOperations().iterator();
		while(iterator.hasNext()){
			Operation operation = (Operation) iterator.next();
			if(operation.getName().equals(operationName)){
				// log execution flow
				LOGGER.exit(true);				
				return true;
			}	
		}
		// log execution flow
		LOGGER.exit(false);
		return false;
	}
	
	public static Operation getOperationFromPortType(PortType portType, String operationName){

		// log execution flow
		LOGGER.entry();
		Iterator<?> iterator = portType.getOperations().iterator();
		while(iterator.hasNext()){
			Operation operation = (Operation) iterator.next();
			if(operation.getName().equalsIgnoreCase(StringUtils.deleteWhitespace(operationName))){
				// log execution flow
				LOGGER.exit();				
				return operation;
			}	
		}
		// log execution flow
		LOGGER.exit();
		return null;
	}	
	
	
	public static Part getFirstPartOfMap(Map<?, ?> parts){

		// log execution flow
		LOGGER.entry();
		Iterator<?> entries = parts.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) entries.next();
			if(entry.getValue() != null){
				// log execution flow
				LOGGER.exit();				
				return (Part)entry.getValue();
			}	
		}
		// log execution flow
		LOGGER.exit();		
		return null;
	}

	public static boolean existOperationIntoWSDLDefinition(Definition wsdl, String operationName){

		// log execution flow
		LOGGER.entry();
		Iterator<?> entries = wsdl.getPortTypes().entrySet().iterator();
		while (entries.hasNext()) {
			// find portType that contains the operation
			PortType portType = ((PortType) ((Entry<?, ?>) entries.next()).getValue());
			if(WSDLUtils.getOperationFromPortType(portType,operationName) != null){
				// log execution flow
				LOGGER.exit(true);				
				return true;			
			}	
		}	
		// log execution flow
		LOGGER.exit(false);
		return false;
	}
	
	public static OperationData getOperationDataWSDLOperation(Definition wsdl, String operationName){
		
		// log execution flow
		LOGGER.entry("Target Namespace: "+wsdl.getTargetNamespace(),"Operation name: "+operationName);
		
		OperationData operationData = new OperationData();
		operationData.setTargetNS(wsdl.getTargetNamespace());
		Iterator<?> entries = wsdl.getPortTypes().entrySet().iterator();
		while (entries.hasNext()) {
			// find portType that contains the operation
			PortType portType = ((PortType) ((Entry<?, ?>) entries.next()).getValue());
			Operation operation = WSDLUtils.getOperationFromPortType(portType, operationName); 
			if(operation != null){
				operationData.setName(operation.getName());
				operationData.setPortTypeName(portType.getQName().getLocalPart());
				MessageData inputMessageData = new MessageData();
				// TODO it is assumed that the input part corresponds to the first part of the message
				// get the first part
				Part inputPart = WSDLUtils.getFirstPartOfMap(operation.getInput().getMessage().getParts());
				inputMessageData.setName(operation.getInput().getMessage().getQName().getLocalPart());
				inputMessageData.setPartName(inputPart.getName());
				inputMessageData.setElementName(inputPart.getElementName().getLocalPart());	
				inputMessageData.setElementTypeName(WSDLUtils.getElementTypeName(wsdl, inputPart
						.getElementName().getLocalPart()));
				operationData.setInputMessageData(inputMessageData);
				if(operation.getOutput() != null){
					MessageData outputMessageData = new MessageData();	
					// get the first part
					Part outputPart = WSDLUtils.getFirstPartOfMap(operation.getOutput().getMessage()
							.getParts());	
					outputMessageData.setName(operation.getOutput().getMessage().getQName().getLocalPart());
					outputMessageData.setPartName(outputPart.getName());
					outputMessageData.setElementName(outputPart.getElementName().getLocalPart());
					outputMessageData.setElementTypeName(WSDLUtils.getElementTypeName(wsdl, outputPart
							.getElementName().getLocalPart()));
					operationData.setOutputMessageData(outputMessageData);
				}
			}
		}	
		LOGGER.info("Operation Data: "+operationData+" for Operation: "+operationName);

		// log execution flow
		LOGGER.exit();
		return operationData;
	}
	
	public static DataTypeWSDLInformation getDataTypeWSDLInformation(Definition wsdl, String operationName, 
			String dataTypeName, boolean isDataTypeContainedIntoInputMessage){
		
		// log execution flow
		LOGGER.entry();
		
		DataTypeWSDLInformation dataTypeWSDLInformation = new DataTypeWSDLInformation();
		dataTypeWSDLInformation.setDataTypeName(dataTypeName);
		Iterator<?> entries = wsdl.getPortTypes().entrySet().iterator();
		Operation operation = null;
		// search the operation on every port type
		while (entries.hasNext()){
			PortType portType = ((PortType) ((Entry<?, ?>) entries.next()).getValue());
			// get the operation corresponding to operationName
			operation = WSDLUtils.getOperationFromPortType(portType, operationName); 
			if(operation != null){
				break;
			}
		}
		// get the input or output message of the operation
		Message message = null;
		if(isDataTypeContainedIntoInputMessage){
			// get the input message of the operation 
			message = operation.getInput().getMessage();
		}
		else{
			// get the output message of the operation
			message = operation.getOutput().getMessage();			
		}	
		// set message name of dataTypeWSDLInformation
		dataTypeWSDLInformation.setMessageName(message.getQName().getLocalPart());
		// TODO it is assumed that the input part corresponds to the first part of the message
		// get the first part of the message
		Part part = WSDLUtils.getFirstPartOfMap(message.getParts());
		// set part name of dataTypeWSDLInformation
		dataTypeWSDLInformation.setPartName(part.getName());
		// get the name of the element that has type data type
		dataTypeWSDLInformation.setElementName(WSDLUtils.getInnerElementNameOfElementWithTypeName(wsdl, 
				part.getElementName().getLocalPart(), dataTypeName));
		// check if element name is main element of the message
		if(dataTypeWSDLInformation.getElementName().equals(part.getElementName().getLocalPart())){
			// element name is the main element of the message
			dataTypeWSDLInformation.setMainElementOfMessage(true);
		}
		else{
			// element name is not the main element of the message
			dataTypeWSDLInformation.setMainElementOfMessage(false);			
		}
		// log execution flow
		LOGGER.exit();
		return dataTypeWSDLInformation;
	}
	
	public static String getOperationNameFromWSDLDefinition(Definition wsdl, String operationName){
		
		// log execution flow
		LOGGER.entry("Target Namespace: "+wsdl.getTargetNamespace()," Operation name: "+operationName);
		
		Iterator<?> entries = wsdl.getPortTypes().entrySet().iterator();
		while (entries.hasNext()) {
			// find portType that contains the operation
			PortType portType = ((PortType) ((Entry<?, ?>) entries.next()).getValue());
			Operation operation = WSDLUtils.getOperationFromPortType(portType, operationName); 
			if(operation != null){
				LOGGER.info("Operation: "+operationName+" found on WSDL with TNS: "+wsdl.getTargetNamespace()
					+" as operation: "+operation.getName());	
				// log execution flow
				LOGGER.exit();
				return operation.getName();
			}
		}	

		LOGGER.info("NO Operation: "+operationName+" found on WSDL with TNS: "
				+wsdl.getTargetNamespace() +"!");			

		throw new CDGeneratorException("NO Operation: "+operationName+" found on WSDL with TNS: "
				+wsdl.getTargetNamespace() +"!");
	}
	
	
	public static String getFirstPartNameWSDLMessage(Definition wsdl,String messageName){
		
		Iterator<?> entries = wsdl.getMessages().entrySet().iterator();
		while (entries.hasNext()) {
			Message message = ((Message) ((Entry<?, ?>) entries.next()).getValue());	
			if(message.getQName().getLocalPart().equals(messageName))
				return WSDLUtils.getFirstPartOfMap(message.getParts()).getName();
		}
		return null;
	}
	
	public static List<WSDLDefinitionData> getWSDLDefinitionDataOfWSDL(List<WSDLData> wsdlData){

		// log execution flow
		LOGGER.entry();
		List<WSDLDefinitionData> wsdlDefinitionsData = new ArrayList<>();
		WSDLReader wsdlReader;
		try {
			wsdlReader = WSDLFactory.newInstance().newWSDLReader();
			for (WSDLData wsdldataitem : wsdlData) {
				WSDLDefinitionData wsdlDefinitionData = new WSDLDefinitionData();
				String participantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(wsdldataitem
						.getParticipantName());
				// parse WSDL file
				Definition definitionwsdl = wsdlReader.readWSDL(null,
						new InputSource(new ByteArrayInputStream(wsdldataitem.getWsdl())));
				wsdlDefinitionData.setPrefix(participantNameNormalized);
				wsdlDefinitionData.setWsdlFileName(participantNameNormalized);
				wsdlDefinitionData.setWsdl(definitionwsdl);
				wsdlDefinitionsData.add(wsdlDefinitionData);
			}
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Exception into getTargetNamespace see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
		return wsdlDefinitionsData;
	}
	
	public static HashMap<String, WSDLDefinitionData> getWSDLDefinitionDataOfWSDLs(List<WSDLData> wsdlData, 
			String cdParticipantName, String cdName){
		
		// log execution flow
		LOGGER.entry();
		HashMap<String, WSDLDefinitionData> wsdlDefinitions = new HashMap<>();
		WSDLReader wsdlReader;
		try {
			wsdlReader = WSDLFactory.newInstance().newWSDLReader();
			for (WSDLData wsdldataitem : wsdlData) {
				WSDLDefinitionData wsdlDefinitionData = new WSDLDefinitionData();
				String participantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(wsdldataitem
						.getParticipantName());
				// parse WSDL file
				Definition definitionwsdl = wsdlReader.readWSDL(null, new InputSource(
						new ByteArrayInputStream(wsdldataitem.getWsdl())));
				wsdlDefinitionData.setParticipantName(wsdldataitem.getParticipantName());
				if(wsdldataitem.getParticipantName().equals(cdParticipantName)){
					wsdlDefinitionData.setPrefix(WSDLUtils.formatParticipantNameForWSDL(cdName));
					wsdlDefinitionData.setWsdlFileName(cdName);					
				}
				else{
					wsdlDefinitionData.setPrefix(participantNameNormalized);
					wsdlDefinitionData.setWsdlFileName(participantNameNormalized);
				}
				wsdlDefinitionData.setWsdl(definitionwsdl);
				wsdlDefinitions.put(wsdldataitem.getParticipantName(), wsdlDefinitionData);
			}
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Exception into getTargetNamespace see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
		return wsdlDefinitions;
	}
		
	public static String generateInitializationMessageString(Definition definition, String elementName){
		
		// log execution flow
		LOGGER.entry();
		String tempFolderPath = Utils.createTemporaryFolderFromMillisAndGetPath();
		String xsdPath = tempFolderPath+File.separatorChar+TYPES_FILENAME;	
		Types types = definition.getTypes();		
		Schema schema = (Schema) types.getExtensibilityElements().get(0);				
		// bound the target namespace of the schema to the prefix xmlns:tns
		schema.getElement().setAttribute(XMLNS_TNS_LABEL, schema.getElement()
				.getAttribute(TARGET_NS_ATTRIBUTE_NAME));	
		XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
		DOMSource source = new DOMSource(schema.getElement());
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		ByteArrayInputStream inputStream = null;
		XmlSchema xmlschema = null;
		try {			
	      	TransformerFactory.newInstance().newTransformer().transform(source, result);
	      	inputStream = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8"));
	        xmlschema = xmlSchemaCollection.read(new StreamSource(inputStream));        
	        xmlschema.write(new FileOutputStream(xsdPath));
		} catch (UnsupportedEncodingException | FileNotFoundException | TransformerException 
					| TransformerFactoryConfigurationError e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into generateInitializationMessageString see log file for details");
		}			
		QName element = new QName(xmlschema.getTargetNamespace(),elementName);	
		String sampleElement = XSDSchemaUtils.generateSampleXMLofElement(xsdPath,element);	
		Utils.deleteFolder(tempFolderPath);
		// log execution flow
		LOGGER.exit();
		return sampleElement;
	}
		
	public static void setTargetNamespaceSchema(Schema schema, String targetNS){
		
		for (int i = 0; i < schema.getElement().getAttributes().getLength(); i++) {
			if(!schema.getElement().getAttributes().item(i).getNodeName().equals(TARGET_NS_ATTRIBUTE_NAME)){
						schema.getElement().removeAttribute(schema.getElement().getAttributes().item(i)
								.getNodeName());
			}
		}
		schema.getElement().setAttribute(XMLNS_LABEL,targetNS);	
		schema.getElement().removeAttribute(XMLNS_TNS_LABEL);	
	}
	
	public static String getElementTypeName(Definition definition, String elementName){

		// log execution flow
		LOGGER.entry();
		Types types = definition.getTypes();			
		Schema schema = (Schema) types.getExtensibilityElements().get(0);			
        Node node = schema.getElement().getFirstChild();
        // iterate over first childs of schema
        while(node != null){
        	// check if a node is an element with name element name
        	if(node.getNodeName().contains(ELEMENT_XSD_LABEL) && node.getAttributes().getNamedItem(NAME_LABEL)
        			.getNodeValue().equals(elementName)){
        		// check if the element has type attribute
        		if(node.getAttributes().getNamedItem(TYPE_LABEL) != null){
        			// the element has a type attribute
        			// get element type
	    			if(node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue().contains(COLON)){
	    				LOGGER.info("Element type of element "+elementName+" is "+StringUtils.split(node
	    						.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue(), COLON)[1]);
	    				// log execution flow
	    				LOGGER.exit();
	    				return StringUtils.split(node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue(), 
	    						COLON)[1];
	    			}	
	    			else{
	    				LOGGER.info("Element type of element "+elementName+" is "+node
	    						.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue());	    				
	    				// log execution flow
	    				LOGGER.exit();
	            		return node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue(); 
	    			}	
        		}
    			// the element has not a type attribute	    			
    			else{
    				// it is defined by an anonymous type
    				// so the type name is the element name
    				return elementName;
    			}	        		
        	}
        	node = node.getNextSibling();       	
        } 
		LOGGER.info("No element type name found for element "+elementName);
		throw new CDGeneratorException("No element type name found for element "+elementName
				+" see log file for details.");
	}
	
	public static String getInnerElementNameOfElementWithTypeName(Definition definition, 
			String elementName, String dataTypeName){
	
		// log execution flow
		LOGGER.entry();
		Types types = definition.getTypes();			
		Schema schema = (Schema) types.getExtensibilityElements().get(0);			
        Node node = schema.getElement().getFirstChild();
        String complexTypeName = null;
        // search the complex type name of the message element 
        // iterate over the children of schema
        while(node != null){
        	// check if a node is an element with name element name      	
        	if(node.getNodeName().contains(ELEMENT_XSD_LABEL) && node.getAttributes().getNamedItem(NAME_LABEL)
        	   .getNodeValue().equals(elementName)){
        		// check if the element has type attribute     		
        		if(node.getAttributes().getNamedItem(TYPE_LABEL) != null){
        			// the element has type attribute   
        			// get complex type name   			
	    			if(node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue().contains(COLON)){
	    				complexTypeName = StringUtils.split(node.getAttributes().getNamedItem(TYPE_LABEL)
	    						.getNodeValue(), COLON)[1];
	    			}	
	    			else{
	    				complexTypeName = node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue();      
	    			}
        		}	
        		else{
        			// the element has not the type attribute  
        			// check if the element name is the same as data type name
        			if(node.getAttributes().getNamedItem(NAME_LABEL).getNodeValue().equals(dataTypeName)){
	    				LOGGER.info("Inner element with data type "+dataTypeName+" is "+node.getAttributes()
	    					.getNamedItem(NAME_LABEL).getNodeValue());	    				
	    				// log execution flow
	    				LOGGER.exit();
            			// return element name
            			return node.getAttributes().getNamedItem(NAME_LABEL).getNodeValue();     				
        			}
        		}        		
        		// check if the complex type name has been found
        		if(complexTypeName != null){
            		// the complex type name has been found
        			// check if the complex type name found is the same as data type name
        			if(complexTypeName.equals(dataTypeName)){
        				// the complex type name found is the same as data type name
            			// return element name
	    				LOGGER.info("Inner element with data type "+dataTypeName+" is "+node.getAttributes()
	    					.getNamedItem(NAME_LABEL).getNodeValue());	    				
	    				// log execution flow
	    				LOGGER.exit();
            			return node.getAttributes().getNamedItem(NAME_LABEL).getNodeValue();   
        			}
        			else{
        				break;
        			}	
        		}
        	}
        	node = node.getNextSibling();       	
        }      	
    	// search the complex type with complexTypeName inside the schema
    	node = schema.getElement().getFirstChild();
        while(node != null){
        	if(node.getNodeName().contains(COMPLEX_TYPE_XSD_LABEL) && node.getAttributes()
        			.getNamedItem(NAME_LABEL)
        			.getNodeValue().equals(complexTypeName)){
        		// a complex type with complexTypeName has been found
        		// search the <sequence> child of complex type node
        		Node sequenceNode = null;
        		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
        			sequenceNode = node.getChildNodes().item(i);
        			// check if sequenceNode is a <sequence>
					if(sequenceNode.getNodeName().contains(SEQUENCE_XSD_LABEL)){
						break;
					}
				}
        		// search inside the sequence node the element that has type data type
        		for (int i = 0; i < sequenceNode.getChildNodes().getLength(); i++) {
					Node sequenceChildNode = sequenceNode.getChildNodes().item(i);
					// check if the child node is an element
		        	if(sequenceChildNode.getNodeName().contains(ELEMENT_XSD_LABEL)){
		        		// child node is an element
		        		String typeName = null;
		        		// check if the element has type attribute     		
		        		if(sequenceChildNode.getAttributes().getNamedItem(TYPE_LABEL) != null){
			        		// get element typeName
			    			if(sequenceChildNode.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue()
			    					.contains(COLON)){
			    				typeName = StringUtils.split(sequenceChildNode.getAttributes()
			    						.getNamedItem(TYPE_LABEL).getNodeValue(), COLON)[1];
			    			}	
			    			else{
			    				typeName = sequenceChildNode.getAttributes().getNamedItem(TYPE_LABEL)
			    						.getNodeValue(); 
			    			}
			    			// check if element type name is data type name
			    			if(typeName.equals(dataTypeName)){
			    				// element type name is data type name
			    				// return element name
			    				LOGGER.info("Inner element with data type "+dataTypeName+" is "
			    						+sequenceChildNode.getAttributes().getNamedItem(NAME_LABEL)
			    						.getNodeValue());	    				
			    				// log execution flow
			    				LOGGER.exit();			    				
			    				return sequenceChildNode.getAttributes().getNamedItem(NAME_LABEL)
			    						.getNodeValue();
			    			}		        			
		        		}
		        	}
				}
        	}
        	node = node.getNextSibling();
        }      
		LOGGER.info("No Inner element with data type "+dataTypeName+" found.");
		throw new CDGeneratorException("No Inner element with data type "+dataTypeName
				+" found see log file for details.");		
	}	
	
	public static WSDLDefinitionData getWSDLDefinitionData(List<WSDLDefinitionData> wsdlDefinitions, 
			String participantName){
		
		// log execution flow
		LOGGER.entry();		
		for (WSDLDefinitionData wsdlDefinitionData : wsdlDefinitions) {
			if(wsdlDefinitionData.getParticipantName().equalsIgnoreCase(participantName)){
				LOGGER.info("WSDLDefinitionData of participant "+participantName+" found.");
				// log execution flow
				LOGGER.exit();
				return wsdlDefinitionData;
			}	
		}
		LOGGER.info("WSDLDefinitionData of participant "+participantName+" not found.");
		throw new CDGeneratorException("WSDLDefinitionData of participant "+participantName
				+" not found see log file for details.");	
	}
		
	
	public static byte[] getWSDLByteArrayFromDefinition(Definition wsdl){

		// log execution flow
		LOGGER.entry();	
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
			byteArrayOutputStream = new ByteArrayOutputStream();
			wsdlWriter.writeWSDL(wsdl,byteArrayOutputStream);
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into getWSDLByteArrayFromDefinition see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
		return byteArrayOutputStream.toByteArray();		
	}
	
	public static String getArtifactNamespace(Document document){
		
		return document.getFirstChild().getAttributes().getNamedItem(TARGET_NS_ATTRIBUTE_NAME)
				.getNodeValue();
	}
		
	public static ServiceDeployBPELData getServiceDeployBPELDataOfPortType(
			Map<String,WSDLDefinitionData> wsdlDefinitions, String portTypeName){
		
		// log execution flow
		LOGGER.entry("Port Type name: "+portTypeName);
		
		ServiceDeployBPELData serviceDeployBPELData = null;				
		for (Entry<String, WSDLDefinitionData> wsdlDefinitionsEntry : wsdlDefinitions.entrySet()) {
			WSDLDefinitionData wsdlDefinitionData = wsdlDefinitionsEntry.getValue();			
			Definition wsdl = wsdlDefinitionData.getWsdl();			
			serviceDeployBPELData = WSDLUtils.getServiceDeployBPELDataOfWSDLDefinition(wsdl,
					wsdlDefinitionData.getPrefix(), portTypeName);
			if(serviceDeployBPELData != null){
				// log serviceDeployBPELData
				LOGGER.info("Service Deploy Bpel Data: "+serviceDeployBPELData);	
				// log execution flow
				LOGGER.exit();
				return serviceDeployBPELData;
			}	
		}	
		
		// log serviceDeployBPELData
		LOGGER.info("No Service Deploy Bpel Data found!");	
		// log execution flow
		LOGGER.exit();
		
		return serviceDeployBPELData;
	}
	
	public static void removeXmlSchemaElement(XmlSchema xmlSchema, String xmlSchemaElementName) {
		
		// log execution flow
		LOGGER.entry();
		
		Iterator<XmlSchemaObject> iterator = xmlSchema.getItems().iterator();
		while (iterator.hasNext()) {
			XmlSchemaObject xmlSchemaObject = (XmlSchemaObject) iterator.next();
			if(xmlSchemaObject instanceof XmlSchemaElement && 
				(((XmlSchemaElement)xmlSchemaObject).getName().equals(xmlSchemaElementName))) {
				
				// log xml schema element removed 
				LOGGER.info("The xml schema element: "+xmlSchemaElementName+" has been removed!");	
				
				iterator.remove();
			}					
		}
		
		// log execution flow
		LOGGER.exit();
	}
	
	
	public static ServiceDeployBPELData getServiceDeployBPELDataOfWSDLDefinition(Definition wsdl,
			String prefix, String portTypeName){
	
		// log execution flow
		LOGGER.entry();
		ServiceDeployBPELData serviceDeployBPELData = null;
		Iterator<?> iterator = wsdl.getAllBindings().entrySet().iterator();
		// find the binding name corresponding to portTypeName	
		String bindingName = null;
		while(iterator.hasNext()){
			Binding binding = ((Binding) ((Entry<?, ?>) iterator.next()).getValue());
			if(binding.getPortType().getQName().getLocalPart().equals(portTypeName)){
				// binding name found
				bindingName = binding.getQName().getLocalPart();
				break;
			}
		}
		if(bindingName != null){
			boolean dataFound = false;
			// search through all the services 
			iterator = wsdl.getAllServices().entrySet().iterator();
			while(iterator.hasNext() && !dataFound){
				Service service = ((Service) ((Entry<?, ?>) iterator.next()).getValue());	
				Iterator<?> iteratorPort = service.getPorts().entrySet().iterator();
				// search through all the ports the one corresponding to the binding found
				while(iteratorPort.hasNext() && !dataFound){
					Port port = ((Port) ((Entry<?, ?>) iteratorPort.next()).getValue());		
					if(port.getBinding().getQName().getLocalPart().equals(bindingName)){
						// port corresponding to binding found
						serviceDeployBPELData = new ServiceDeployBPELData(prefix, wsdl.getTargetNamespace(),
								service.getQName().getLocalPart(), port.getName());
						// set dataFound to true in order to exit from both cycles
						dataFound = true;
					}						
				}
			}
		}
		// log execution flow
		LOGGER.exit();
		return serviceDeployBPELData;		
	}
	
	public static void addCDProsumerPartOperationElementsToWSDLDefinition(Definition definition,
			XmlSchema xmlschema, String targetNS, String operationName, String inputMessageName,
			String inputMessageTypeName, String outputMessageName, String outputMessageTypeName,
			String baseNameDefinitionElements, String baseAddress){

		// log execution flow
		LOGGER.entry();
		if(!WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName)){
			// the definition doesn't contain the operation			
			// create port type name based on baseName
			String portTypeName = WSDLUtils.createPortTypeName(baseNameDefinitionElements);
			// check if the definition already contains that portType
			if(definition.getPortType(new QName(targetNS, portTypeName)) == null){
				// definition doesn't contain that portType	
				// create <portType>
				PortType portType = definition.createPortType();
				portType.setQName(new QName(targetNS,portTypeName));
				portType.setUndefined(false);
				// add <portType> to definition
				definition.addPortType(portType);
				// create <binding>
				Binding binding = definition.createBinding();
				binding.setUndefined(false);
				binding.setQName(new QName(targetNS, WSDLUtils
						.createBindingName(baseNameDefinitionElements)));
				// set <portType> to <binding>
				binding.setPortType(portType);
				// add <binding> to definition
				definition.addBinding(binding);	
				// create <soap:binding>
			    SOAPBinding soapBinding = new SOAPBindingImpl();
			    soapBinding.setStyle(SOAP_BINDING_STYLE_VALUE);
			    soapBinding.setTransportURI(SOAP_BINDING_TRANSPORT_URI_VALUE);
			    // set <soap:binding> to <binding>
				binding.addExtensibilityElement(soapBinding);
				// create <service>
				Service service = definition.createService();
				service.setQName(new QName(targetNS, WSDLUtils
						.createServiceName(baseNameDefinitionElements)));	
				// add <service> to definition
				definition.addService(service);	
				// create <port>
				Port port = definition.createPort();
				port.setName(WSDLUtils.createPortName(baseNameDefinitionElements));
				// set <binding> to <port> 
				port.setBinding(binding);
				// add <port> to <service>
				service.addPort(port);
				// create <soap:address>
				SOAPAddress soapAddress = new SOAPAddressImpl();
				soapAddress.setLocationURI(WSDLUtils.createProsumerPartAddress(baseAddress,
						baseNameDefinitionElements));
				// add <soap:address> to <port>
				port.addExtensibilityElement(soapAddress);							
			}						
			// create input <message> of the operation
			// <message>
			Message inputmsg = definition.createMessage();
			inputmsg.setQName(new QName(targetNS, WSDLUtils.createInputMessageName(inputMessageTypeName)));
			// create part for input message
			// <message> / <part>
			Part inputmsgpart = definition.createPart();
			inputmsgpart.setName(PARAMETERS_NAME_LABEL);
			// set element to inputmsgpart					
			inputmsgpart.setElementName(new QName(targetNS, WSDLUtils
					.createInputMessageElementName(inputMessageTypeName)));				
			inputmsg.setUndefined(false);
			// set part into message
			inputmsg.addPart(inputmsgpart);
			// add message to definition
			definition.addMessage(inputmsg);
			// create <operation>	
			// <portType> / <operation>
			Operation operation = definition.createOperation();
			operation.setName(operationName);
			operation.setUndefined(false);
			// <portType> / <operation> / <input>
			// create input element for the operation
			Input input = definition.createInput();	
			// set message into input 
			input.setMessage(inputmsg);
			// set input into operation
			operation.setInput(input);
			
			// create input type name
			String inputTypeName = WSDLUtils.createInputMessageTypeName(inputMessageName);			
			// create complex type for the input message of the operation
			XmlSchemaComplexType complexTypeInput = new XmlSchemaComplexType(xmlschema, true);
			complexTypeInput.setName(inputTypeName);
			// create choreographyId element
			XmlSchemaElement inputTypeChoreographyIdElement = new XmlSchemaElement(xmlschema, false);
			inputTypeChoreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED
			inputTypeChoreographyIdElement
				.setSchemaTypeName(new QName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME));			
			// create senderParticipantName element
			XmlSchemaElement senderParticipantNameElement = new XmlSchemaElement(xmlschema, false);
			senderParticipantNameElement.setName(SENDER_PARTICIPANT_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			senderParticipantNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create receiverParticipantName element
			XmlSchemaElement receiverParticipantNameElement = new XmlSchemaElement(xmlschema, false);
			receiverParticipantNameElement.setName(RECEIVER_PARTICIPANT_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			receiverParticipantNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create choreographyTaskName element
			XmlSchemaElement choreographyTaskNameElement = new XmlSchemaElement(xmlschema, false);
			choreographyTaskNameElement.setName(CHOREOGRAPHY_TASK_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			choreographyTaskNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create messageName element
			XmlSchemaElement messageInputNameElement = new XmlSchemaElement(xmlschema, false);
			messageInputNameElement.setName(INPUT_MESSAGE_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			messageInputNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create inputTypeMessage element
			XmlSchemaElement inputTypeMessageElement = new XmlSchemaElement(xmlschema, false);
			inputTypeMessageElement.setName(INPUT_MESSAGE_DATA_ELEMENTNAME);			
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			inputTypeMessageElement.setSchemaTypeName(new QName(inputMessageTypeName));	
			XmlSchemaSequence sequenceInput = new XmlSchemaSequence();	
			// add elements to sequence
			sequenceInput.getItems().add(inputTypeChoreographyIdElement);
			sequenceInput.getItems().add(senderParticipantNameElement);
			sequenceInput.getItems().add(receiverParticipantNameElement);
			sequenceInput.getItems().add(choreographyTaskNameElement);
			sequenceInput.getItems().add(messageInputNameElement);
			sequenceInput.getItems().add(inputTypeMessageElement);
			// check if an output message has been specified
			if(outputMessageTypeName != null){
				// output message specified
				// create messageOutputNameElement element
				XmlSchemaElement messageOutputNameElement = new XmlSchemaElement(xmlschema, false);
				messageOutputNameElement.setName(OUTPUT_MESSAGE_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				messageOutputNameElement
					.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));	
				// add messageOutputNameElement to sequenceInput 
				sequenceInput.getItems().add(messageOutputNameElement);
			}
			// set complexTypeInReceive particle to sequenceInReceive 
			complexTypeInput.setParticle(sequenceInput);
						
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementIn = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementIn.setName(WSDLUtils.createInputMessageElementName(inputMessageTypeName));					
			xmlSchemaElementIn.setSchemaTypeName(new QName(inputTypeName));	
			
			// create output message type name base name
			String outputMessageNameBaseName;
			// check if an output message has been specified
			if(outputMessageTypeName != null){
				// output message specified
				// set outputMessageTypeNameBaseName to outputMessageName
				outputMessageNameBaseName = outputMessageName;
			}
			else{
				// no output message specified
				// set outputMessageTypeNameBaseName to inputMessageName
				outputMessageNameBaseName = inputMessageName;				
			}		
			// create output message of the operation
			// <message>
			Message outputmsg = definition.createMessage();
			outputmsg.setQName(new QName(targetNS, WSDLUtils
					.createOutputMessageName(outputMessageNameBaseName)));
			// create part for output message
			// <message> / <part>
			Part outputmsgpart = definition.createPart();
			outputmsgpart.setName(PARAMETERS_NAME_LABEL);							
			// set element to outputmsgpart
			outputmsgpart.setElementName(new QName(targetNS, WSDLUtils
					.createOutputMessageElementName(outputMessageNameBaseName)));							
			outputmsg.setUndefined(false);
			// set part into message
			outputmsg.addPart(outputmsgpart);
			// add message to definition
			definition.addMessage(outputmsg);
			// <portType> / <operation> / <output>
			// create output element for the operation
			Output output = definition.createOutput();	
			// set message into output 
			output.setMessage(outputmsg);
			// set output into operation
			operation.setOutput(output);
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementOut = new XmlSchemaElement(xmlschema, true);
			// set element output message name
			xmlSchemaElementOut.setName(WSDLUtils.createOutputMessageElementName(outputMessageNameBaseName));
			// no output message specified
			// create outputTypeName
			String outputTypeName = WSDLUtils.createOutputMessageTypeName(outputMessageNameBaseName);			
			// create empty complex type
			XmlSchemaComplexType complexTypeOutput = new XmlSchemaComplexType(xmlschema, true);
			complexTypeOutput.setName(outputTypeName);
			// create sequence for the complex type representing the output of the operation
			XmlSchemaSequence sequenceOutput = new XmlSchemaSequence();	
			// check if an output message has been specified
			if(outputMessageTypeName != null){
				// output message specified
				// create sequence with the outputMessageTypeName wrapped to message data
				// otherwise an empty sequence is created
				
				// create outputTypeMessage element
				XmlSchemaElement outputTypeMessageElement = new XmlSchemaElement(xmlschema, false);
				outputTypeMessageElement.setName(OUTPUT_MESSAGE_DATA_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				outputTypeMessageElement.setSchemaTypeName(new QName(outputMessageTypeName));	
				// add element to sequence
				sequenceOutput.getItems().add(outputTypeMessageElement);
			}
			// set complexTypeOutput particle to sequenceOutput 
			complexTypeOutput.setParticle(sequenceOutput);				
			// set element output message type to outputTypeName				
			xmlSchemaElementOut.setSchemaTypeName(new QName(outputTypeName));										
			// get portType
			PortType portType = definition.getPortType(new QName(targetNS, portTypeName));
			// add <operation> to the <portType>						
			portType.addOperation(operation);
			// <binding> / <operation>
			// create binding for operation
			BindingOperation bindingOperation = definition.createBindingOperation();
			bindingOperation.setName(operation.getName());
			SOAPOperation soapOperation = new SOAPOperationImpl();
			soapOperation.setSoapActionURI("");  
			bindingOperation.addExtensibilityElement(soapOperation);
			// <binding> / <operation> / <input> 
			// create input binding for operation
			BindingInput bindingInput = definition.createBindingInput();
			SOAPBody soapBodyInput = new SOAPBodyImpl();
			soapBodyInput.setUse(LITERAL_NAME_LABEL);
			bindingInput.addExtensibilityElement(soapBodyInput);
			// set input binding to binding operation
			bindingOperation.setBindingInput(bindingInput);
			// create output binding 
			// <binding> / <operation> / <output>
			// create output binding for operation			
			BindingOutput bindingOutput =  definition.createBindingOutput();
			SOAPBody soapBodyOutput = new SOAPBodyImpl();
			soapBodyOutput.setUse(LITERAL_NAME_LABEL);
			bindingOutput.addExtensibilityElement(soapBodyOutput);
			// set output binding to binding operation
			bindingOperation.setBindingOutput(bindingOutput);							
			// get binding
			Binding binding = definition.getBinding(new QName(targetNS, WSDLUtils
					.createBindingName(baseNameDefinitionElements)));
			// add binding operation 	
			binding.addBindingOperation(bindingOperation);
		}
		// log execution flow
		LOGGER.exit();
	}
							
	public static void addCDProsumerPartSendOperationElementsToWSDLDefinition(Definition definition, 
			XmlSchema xmlschema, String targetNS, String choreographyMessageName,
			String choreographyMessageTypeName, String baseNameDefinitionElements, String baseAddress,
			boolean isOperationLooped){

		// log execution flow
		LOGGER.entry();
		// create send  definitions elements base name
		String sendDefinitionsElementsBaseName = WSDLUtils
				.createSendDefinitionsElementsBaseName(choreographyMessageName);	
		// check if the operation already exists or if it exists but the operation to be created is
		// looped 
		if(!WSDLUtils.existOperationIntoWSDLDefinition(definition, sendDefinitionsElementsBaseName) ||
		   (WSDLUtils.existOperationIntoWSDLDefinition(definition, sendDefinitionsElementsBaseName) && 
		    isOperationLooped)){
			// the definition doesn't contain the operation or it contains the operation but
			// the operation to be created is looped and so the existing operation has to be 
			// replaced by the looped one		
			// check if choreography instance request type exists inside the schema
			if(xmlschema.getTypeByName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME) == null){
				// choreography instance request type doesn't exist inside the schema
				// create complex type choreographyInstanceRequest
				XmlSchemaComplexType complexTypeChoreographyInstanceRequest = 
						new XmlSchemaComplexType(xmlschema, true);
				complexTypeChoreographyInstanceRequest.setName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME);
				// create choreographyId element
				XmlSchemaElement choreographyIdElement = new XmlSchemaElement(xmlschema, false);
				choreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
				choreographyIdElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));
				// create choreographyInstanceRequest complex type sequence
				XmlSchemaSequence sequenceInChoreographyInstanceRequest = new XmlSchemaSequence();
				// add element to sequence
				sequenceInChoreographyInstanceRequest.getItems().add(choreographyIdElement);
				// set complexTypeChoreographyInstanceRequest particle to 
				// sequenceInChoreographyInstanceRequest 
				complexTypeChoreographyInstanceRequest.setParticle(sequenceInChoreographyInstanceRequest);			
			}		
			// check if the operation is looped and send request with loop type complex type does not 
			// exist inside the schema 
			// OR
			// if the operation is not looped and send request complex type 
			// does not exist inside the schema
			if((isOperationLooped && xmlschema.getTypeByName(SEND_REQUEST_TYPE_WITH_LOOP_TYPENAME) == null)
				|| (!isOperationLooped && xmlschema.getTypeByName(SEND_REQUEST_TYPE_TYPENAME) == null)){

				// create choreographyId element for sendRequestType
				XmlSchemaElement sendRequestTypeChoreographyIdElement = 
						new XmlSchemaElement(xmlschema, false);
				sendRequestTypeChoreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED
				sendRequestTypeChoreographyIdElement
				.setSchemaTypeName(new QName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME));			
				// create senderParticipantName element
				XmlSchemaElement senderParticipantNameElement = new XmlSchemaElement(xmlschema, false);
				senderParticipantNameElement.setName(SENDER_PARTICIPANT_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				senderParticipantNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS,
						XMLSCHEMA_STRINGTYPE_NAME));				
				// create receiverParticipantName element
				XmlSchemaElement receiverParticipantNameElement = new XmlSchemaElement(xmlschema, false);
				receiverParticipantNameElement.setName(RECEIVER_PARTICIPANT_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				receiverParticipantNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS,
						XMLSCHEMA_STRINGTYPE_NAME));				
				// create choreographyTaskName element
				XmlSchemaElement choreographyTaskNameElement = new XmlSchemaElement(xmlschema, false);
				choreographyTaskNameElement.setName(CHOREOGRAPHY_TASK_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				choreographyTaskNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS,
						XMLSCHEMA_STRINGTYPE_NAME));				
				// create messageName element
				XmlSchemaElement messageInputNameElement = new XmlSchemaElement(xmlschema, false);
				messageInputNameElement.setName(INPUT_MESSAGE_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				messageInputNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS,
						XMLSCHEMA_STRINGTYPE_NAME));	
				
				// create complex type sendRequestType
				XmlSchemaComplexType complexTypeSendRequestType = new XmlSchemaComplexType(xmlschema, true);
				// check if the send operation belongs to a loop
				if(isOperationLooped) {
					// the send operation belongs to a loop
					// check if send request type with loop exists inside the schema
					if(xmlschema.getTypeByName(SEND_REQUEST_TYPE_WITH_LOOP_TYPENAME) == null){
						// send request type with loop doesn't exist inside the schema	
						// set complexTypeSendRequestType name to send request type with loop 
						complexTypeSendRequestType.setName(SEND_REQUEST_TYPE_WITH_LOOP_TYPENAME);					
					}
				}
				else {
					// the send operation does not belong to a loop
					// check if send request type exists inside the schema
					if(xmlschema.getTypeByName(SEND_REQUEST_TYPE_TYPENAME) == null){
						// send request type doesn't exist inside the schema	
						// set complexTypeSendRequestType name to send request type 
						complexTypeSendRequestType.setName(SEND_REQUEST_TYPE_TYPENAME);						
					}			
				}

				// create sequence for complex type sendRequestType
				XmlSchemaSequence sequencesSendRequestType = new XmlSchemaSequence();	
				// add elements to sequence
				sequencesSendRequestType.getItems().add(sendRequestTypeChoreographyIdElement);
				sequencesSendRequestType.getItems().add(senderParticipantNameElement);
				sequencesSendRequestType.getItems().add(receiverParticipantNameElement);
				sequencesSendRequestType.getItems().add(choreographyTaskNameElement);
				sequencesSendRequestType.getItems().add(messageInputNameElement);	
				// check if the send operation belongs to a loop
				if(isOperationLooped) {
					// the operation belongs to a loop
					// create loopIndexes element
					XmlSchemaElement loopIndexesElement = new XmlSchemaElement(xmlschema, false);
					loopIndexesElement.setName(LOOP_INDEXES_ELEMENTNAME);	
					// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
					loopIndexesElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));	
					// add loopIndexesElement to sequencesSendRequestType
					sequencesSendRequestType.getItems().add(loopIndexesElement);		
				}				
				
				// set complexTypeSendRequestType particle to sequenceInReceive 
				complexTypeSendRequestType.setParticle(sequencesSendRequestType);					
			}
								
			// create send input element name
			String sendInputElementName = WSDLUtils
					.createInputMessageElementName(sendDefinitionsElementsBaseName);
			// create send output element name
			String sendOutputElementName = WSDLUtils
					.createOutputMessageElementName(sendDefinitionsElementsBaseName);		
			// create port type name based on baseName
			String portTypeName = WSDLUtils.createPortTypeName(baseNameDefinitionElements);
			// check if the definition already contains that portType
			if(definition.getPortType(new QName(targetNS, portTypeName)) == null){
			// definition doesn't contain that portType	
				// create <portType>
				PortType portType = definition.createPortType();
				portType.setQName(new QName(targetNS,portTypeName));
				portType.setUndefined(false);
				// add <portType> to definition
				definition.addPortType(portType);
				// create <binding>
				Binding binding = definition.createBinding();
				binding.setUndefined(false);
				binding.setQName(new QName(targetNS, WSDLUtils
						.createBindingName(baseNameDefinitionElements)));
				// set <portType> to <binding>
				binding.setPortType(portType);
				// add <binding> to definition
				definition.addBinding(binding);	
				// create <soap:binding>
			    SOAPBinding soapBinding = new SOAPBindingImpl();
			    soapBinding.setStyle(SOAP_BINDING_STYLE_VALUE);
			    soapBinding.setTransportURI(SOAP_BINDING_TRANSPORT_URI_VALUE);
			    // set <soap:binding> to <binding>
				binding.addExtensibilityElement(soapBinding);
				// create <service>
				Service service = definition.createService();
				service.setQName(new QName(targetNS, WSDLUtils
						.createServiceName(baseNameDefinitionElements)));	
				// add <service> to definition
				definition.addService(service);	
				// create <port>
				Port port = definition.createPort();
				port.setName(WSDLUtils.createPortName(baseNameDefinitionElements));
				// set <binding> to <port> 
				port.setBinding(binding);
				// add <port> to <service>
				service.addPort(port);
				// create <soap:address>
				SOAPAddress soapAddress = new SOAPAddressImpl();
				soapAddress.setLocationURI(WSDLUtils.createProsumerPartAddress(baseAddress,
						baseNameDefinitionElements));
				// add <soap:address> to <port>
				port.addExtensibilityElement(soapAddress);							
			}
			// check if the operation is already defined inside the definition and 
			// if the operation to be created is looped
			if (WSDLUtils.existOperationIntoWSDLDefinition(definition, sendDefinitionsElementsBaseName) && 
				    isOperationLooped) {
				// the operation is already defined inside the definition and 
				// the operation to be created is looped
				// remove both schema elements with names sendInputElementName and sendOutputElementName
				// related to the existing operation 
				WSDLUtils.removeXmlSchemaElement(xmlschema, sendInputElementName);
				WSDLUtils.removeXmlSchemaElement(xmlschema, sendOutputElementName);
			}
	
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementIn = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementIn.setName(sendInputElementName);	
			
			// check if the send operation belongs to a loop
			if(isOperationLooped) {
				// the send operation belongs to a loop
				// set xmlSchemaElementIn schema type to send request with loop type
				xmlSchemaElementIn.setSchemaTypeName(new QName(SEND_REQUEST_TYPE_WITH_LOOP_TYPENAME));
			}
			else {
				// the send operation does not belong to a loop
				// set xmlSchemaElementIn schema type to send request 
				xmlSchemaElementIn.setSchemaTypeName(new QName(SEND_REQUEST_TYPE_TYPENAME));
			}
								
			// create input <message> of the operation
			// <message>
			Message inputmsg = definition.createMessage();
			inputmsg.setQName(new QName(targetNS, WSDLUtils
					.createInputMessageName(sendDefinitionsElementsBaseName)));
			// create part for input message
			// <message> / <part>
			Part inputmsgpart = definition.createPart();
			inputmsgpart.setName(PARAMETERS_NAME_LABEL);
			// set element to inputmsgpart					
			inputmsgpart.setElementName(new QName(targetNS, sendInputElementName));				
			inputmsg.setUndefined(false);
			// set part into message
			inputmsg.addPart(inputmsgpart);
			// add message to definition
			definition.addMessage(inputmsg);
			// create <operation>	
			// <portType> / <operation>
			Operation operation = definition.createOperation();
			operation.setName(sendDefinitionsElementsBaseName);
			operation.setUndefined(false);
			// <portType> / <operation> / <input>
			// create input element for the operation
			Input input = definition.createInput();	
			// set message into input 
			input.setMessage(inputmsg);
			// set input into operation
			operation.setInput(input);				
			// create output message of the operation
			// <message>	
			Message outputmsg = definition.createMessage();
			outputmsg.setQName(new QName(targetNS, WSDLUtils
					.createOutputMessageName(sendDefinitionsElementsBaseName)));
			// create part for output message
			// <message> / <part>
			Part outputmsgpart = definition.createPart();
			outputmsgpart.setName(PARAMETERS_NAME_LABEL);							
			// set element to outputmsgpart
			outputmsgpart.setElementName(new QName(targetNS, sendOutputElementName));							
			outputmsg.setUndefined(false);
			// set part into message
			outputmsg.addPart(outputmsgpart);
			// add message to definition
			definition.addMessage(outputmsg);
			// <portType> / <operation> / <output>
			// create output element for the operation
			Output output = definition.createOutput();	
			// set message into output 
			output.setMessage(outputmsg);
			// set output into operation
			operation.setOutput(output);
			
			// create complex type for the output message
			// create output type name
			String outputTypeName = WSDLUtils.createOutputMessageTypeName(choreographyMessageTypeName);	
			
			// check if the complex type for the element of the output message of send operation
			// already exist
			if(xmlschema.getTypeByName(outputTypeName) == null){
				// complex type for the element of the output message of send operation doesn't exist
				// create complex type for the element of the output message of send operation
				XmlSchemaComplexType complexTypeOutput = new XmlSchemaComplexType(xmlschema, true);
				complexTypeOutput.setName(outputTypeName);
				// create outputTypeMessage element
				// <types> / <schema> / <element>
				XmlSchemaElement outputTypeMessageElement = new XmlSchemaElement(xmlschema, false);
				outputTypeMessageElement.setName(INPUT_MESSAGE_DATA_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				outputTypeMessageElement.setSchemaTypeName(new QName(choreographyMessageTypeName));
				XmlSchemaSequence sequenceOutput = new XmlSchemaSequence();	
				// add element to sequence
				sequenceOutput.getItems().add(outputTypeMessageElement);								
				// set complexTypeInReceive particle to sequenceInReceive 
				complexTypeOutput.setParticle(sequenceOutput);	
			}	
			
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementOut = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementOut.setName(sendOutputElementName);					
			xmlSchemaElementOut.setSchemaTypeName(new QName(outputTypeName));	
						
			// get portType
			PortType portType = definition.getPortType(new QName(targetNS, portTypeName));
			
			// check if the operation is already defined inside the definition and 
			// if the operation to be created is looped
			if (WSDLUtils.existOperationIntoWSDLDefinition(definition, sendDefinitionsElementsBaseName) && 
				    isOperationLooped) {
				// the operation is already defined inside the definition and 
				// the operation to be created is looped		
				// remove the existing operation from portType
				portType.removeOperation(operation.getName(), operation.getInput().getName(),
						operation.getOutput().getName());
			}
			
			// add <operation> to the <portType>						
			portType.addOperation(operation);
			// <binding> / <operation>
			// create binding for operation
			BindingOperation bindingOperation = definition.createBindingOperation();
			bindingOperation.setName(operation.getName());
			SOAPOperation soapOperation = new SOAPOperationImpl();
			soapOperation.setSoapActionURI("");  
			bindingOperation.addExtensibilityElement(soapOperation);
			// <binding> / <operation> / <input> 
			// create input binding for operation
			BindingInput bindingInput = definition.createBindingInput();
			SOAPBody soapBodyInput = new SOAPBodyImpl();
			soapBodyInput.setUse(LITERAL_NAME_LABEL);
			bindingInput.addExtensibilityElement(soapBodyInput);
			// set input binding to binding operation
			bindingOperation.setBindingInput(bindingInput);
			// <binding> / <operation> / <output>
			// create output binding for operation			
			BindingOutput bindingOutput =  definition.createBindingOutput();
			SOAPBody soapBodyOutput = new SOAPBodyImpl();
			soapBodyOutput.setUse(LITERAL_NAME_LABEL);
			bindingOutput.addExtensibilityElement(soapBodyOutput);
			// set output binding to binding operation
			bindingOperation.setBindingOutput(bindingOutput);			
			// get binding
			Binding binding = definition.getBinding(new QName(targetNS, 
					WSDLUtils.createBindingName(baseNameDefinitionElements)));
			
			// check if the operation is already defined inside the definition and 
			// if the operation to be created is looped
			if (WSDLUtils.existOperationIntoWSDLDefinition(definition, sendDefinitionsElementsBaseName) && 
				    isOperationLooped) {
				// the operation is already defined inside the definition and 
				// the operation to be created is looped		
				// remove the existing operation from binding
				binding.removeBindingOperation(operation.getName(), operation.getInput().getName(),
						operation.getOutput().getName());
			}				
			
			// add binding operation 	
			binding.addBindingOperation(bindingOperation);			
		}
		// log execution flow
		LOGGER.exit();		
	}			

	public static void addCDProsumerPartReceiveOperationElementsToWSDLDefinition(Definition definition, 
			XmlSchema xmlschema, String targetNS, String choreographyMessageName,
			String choreographyMessageTypeName, String baseNameDefinitionElements, String baseAddress){

		// log execution flow
		LOGGER.entry();
		// create receive definitions elements base name
		String receiveDefinitionsElementsBaseName = WSDLUtils
				.createReceiveDefinitionsElementsBaseName(choreographyMessageName);
		// check if the definition contains the operation
		if(!WSDLUtils.existOperationIntoWSDLDefinition(definition, receiveDefinitionsElementsBaseName)){
			// the definition doesn't contain the operation
			// check if choreography instance request type exists inside xml schema
			if(xmlschema.getTypeByName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME) == null){
				// choreography instance request type doesn't exist inside xml schema			
				// create complex type choreographyInstanceRequest
				XmlSchemaComplexType complexTypeChoreographyInstanceRequest = 
						new XmlSchemaComplexType(xmlschema, true);
				complexTypeChoreographyInstanceRequest.setName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME);
				// create choreographyId element
				XmlSchemaElement choreographyIdElement = new XmlSchemaElement(xmlschema, false);
				choreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
				choreographyIdElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));
				// create choreographyInstanceRequest complex type sequence
				XmlSchemaSequence sequenceInChoreographyInstanceRequest = new XmlSchemaSequence();
				// add element to sequence
				sequenceInChoreographyInstanceRequest.getItems().add(choreographyIdElement);
				// set siaComplexTypeIn particle to sequenceInChoreographyInstanceRequest 
				complexTypeChoreographyInstanceRequest.setParticle(sequenceInChoreographyInstanceRequest);		
			}
			// create receive input type name
			String receiveInputTypeName = WSDLUtils
					.createInputMessageTypeName(receiveDefinitionsElementsBaseName);
			// create receive input element name
			String receiveInputElementName = WSDLUtils
					.createInputMessageElementName(receiveDefinitionsElementsBaseName);
			// create receive input type name
			String receiveOutputTypeName = WSDLUtils
					.createOutputMessageTypeName(receiveDefinitionsElementsBaseName);		
			// create receive output element name
			String receiveOutputElementName = WSDLUtils
					.createOutputMessageElementName(receiveDefinitionsElementsBaseName);
			
			// create complex type for the input message of the receive operation
			XmlSchemaComplexType complexTypeInReceive = new XmlSchemaComplexType(xmlschema, true);
			complexTypeInReceive.setName(receiveInputTypeName);
			// create choreographyId element
			XmlSchemaElement receiveTypeChoreographyIdElement = new XmlSchemaElement(xmlschema, false);
			receiveTypeChoreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED
			receiveTypeChoreographyIdElement
				.setSchemaTypeName(new QName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME));			
			// create senderParticipantName element
			XmlSchemaElement senderParticipantNameElement = new XmlSchemaElement(xmlschema, false);
			senderParticipantNameElement.setName(SENDER_PARTICIPANT_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			senderParticipantNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create receiverParticipantName element
			XmlSchemaElement receiverParticipantNameElement = new XmlSchemaElement(xmlschema, false);
			receiverParticipantNameElement.setName(RECEIVER_PARTICIPANT_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			receiverParticipantNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create choreographyTaskName element
			XmlSchemaElement choreographyTaskNameElement = new XmlSchemaElement(xmlschema, false);
			choreographyTaskNameElement.setName(CHOREOGRAPHY_TASK_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			choreographyTaskNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create messageName element
			XmlSchemaElement messageOutputNameElement = new XmlSchemaElement(xmlschema, false);
			messageOutputNameElement.setName(OUTPUT_MESSAGE_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			messageOutputNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create receiveTypeMessage element
			XmlSchemaElement receiveTypeMessageElement = new XmlSchemaElement(xmlschema, false);
			receiveTypeMessageElement.setName(OUTPUT_MESSAGE_DATA_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			receiveTypeMessageElement.setSchemaTypeName(new QName(choreographyMessageTypeName));
			// create sequence for the complex type representing the input of the receive operation
			XmlSchemaSequence sequenceInReceive = new XmlSchemaSequence();	
			// add elements to sequence
			sequenceInReceive.getItems().add(receiveTypeChoreographyIdElement);
			sequenceInReceive.getItems().add(senderParticipantNameElement);
			sequenceInReceive.getItems().add(receiverParticipantNameElement);
			sequenceInReceive.getItems().add(choreographyTaskNameElement);
			sequenceInReceive.getItems().add(messageOutputNameElement);
			sequenceInReceive.getItems().add(receiveTypeMessageElement);									
			// set complexTypeInReceive particle to sequenceInReceive 
			complexTypeInReceive.setParticle(sequenceInReceive);
			
			// create complex type for the output of the receive operation
			XmlSchemaComplexType complexTypeOutReceive = new XmlSchemaComplexType(xmlschema, true);
			complexTypeOutReceive.setName(receiveOutputTypeName);
			// create sequence for the complex type representing the output of the receive operation
			XmlSchemaSequence sequenceOutReceive = new XmlSchemaSequence();	
			// set complexTypeOutReceive particle to sequenceOutReceive 
			complexTypeOutReceive.setParticle(sequenceOutReceive);			
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementIn = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementIn.setName(receiveInputElementName);
			// set the type of the input element to complexTypeInReceive
			xmlSchemaElementIn.setSchemaTypeName(new QName(receiveInputTypeName));		
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementOut = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementOut.setName(receiveOutputElementName);		
			// set the type of the output element to complexTypeOutReceive
			xmlSchemaElementOut.setSchemaTypeName(new QName(receiveOutputTypeName));									
				
			// create port type name based on baseName
			String portTypeName = WSDLUtils.createPortTypeName(baseNameDefinitionElements);
			// check if the definition already contains that portType
			if(definition.getPortType(new QName(targetNS, portTypeName)) == null){
			// definition doesn't contain that portType	
				// create <portType>
				PortType portType = definition.createPortType();
				portType.setQName(new QName(targetNS,portTypeName));
				portType.setUndefined(false);
				// add <portType> to definition
				definition.addPortType(portType);
				// create <binding>
				Binding binding = definition.createBinding();
				binding.setUndefined(false);
				binding.setQName(new QName(targetNS, WSDLUtils
						.createBindingName(baseNameDefinitionElements)));
				// set <portType> to <binding>
				binding.setPortType(portType);
				// add <binding> to definition
				definition.addBinding(binding);	
				// create <soap:binding>
			    SOAPBinding soapBinding = new SOAPBindingImpl();
			    soapBinding.setStyle(SOAP_BINDING_STYLE_VALUE);
			    soapBinding.setTransportURI(SOAP_BINDING_TRANSPORT_URI_VALUE);
			    // set <soap:binding> to <binding>
				binding.addExtensibilityElement(soapBinding);
				// create <service>
				Service service = definition.createService();
				service.setQName(new QName(targetNS, WSDLUtils
						.createServiceName(baseNameDefinitionElements)));	
				// add <service> to definition
				definition.addService(service);	
				// create <port>
				Port port = definition.createPort();
				port.setName(WSDLUtils.createPortName(baseNameDefinitionElements));
				// set <binding> to <port> 
				port.setBinding(binding);
				// add <port> to <service>
				service.addPort(port);
				// create <soap:address>
				SOAPAddress soapAddress = new SOAPAddressImpl();
				soapAddress.setLocationURI(WSDLUtils.createProsumerPartAddress(baseAddress, 
						baseNameDefinitionElements));
				// add <soap:address> to <port>
				port.addExtensibilityElement(soapAddress);							
			}						
			// create input <message> of the operation
			// <message>
			Message inputmsg = definition.createMessage();
			inputmsg.setQName(new QName(targetNS, WSDLUtils
					.createInputMessageName(receiveDefinitionsElementsBaseName)));
			// create part for input message
			// <message> / <part>
			Part inputmsgpart = definition.createPart();
			inputmsgpart.setName(PARAMETERS_NAME_LABEL);
			// set element to inputmsgpart					
			inputmsgpart.setElementName(new QName(targetNS, receiveInputElementName));				
			inputmsg.setUndefined(false);
			// set part into message
			inputmsg.addPart(inputmsgpart);
			// add message to definition
			definition.addMessage(inputmsg);
			// create <operation>	
			// <portType> / <operation>
			Operation operation = definition.createOperation();
			operation.setName(receiveDefinitionsElementsBaseName);
			operation.setUndefined(false);
			// <portType> / <operation> / <input>
			// create input element for the operation
			Input input = definition.createInput();	
			// set message into input 
			input.setMessage(inputmsg);
			// set input into operation
			operation.setInput(input);					
			// create output message of the operation
			// <message>
			Message outputmsg = definition.createMessage();
			outputmsg.setQName(new QName(targetNS, WSDLUtils
					.createOutputMessageName(receiveDefinitionsElementsBaseName)));
			// create part for output message
			// <message> / <part>
			Part outputmsgpart = definition.createPart();
			outputmsgpart.setName(PARAMETERS_NAME_LABEL);							
			// set element to outputmsgpart
			outputmsgpart.setElementName(new QName(targetNS, receiveOutputElementName));							
			outputmsg.setUndefined(false);
			// set part into message
			outputmsg.addPart(outputmsgpart);
			// add message to definition
			definition.addMessage(outputmsg);
			// <portType> / <operation> / <output>
			// create output element for the operation
			Output output = definition.createOutput();	
			// set message into output 
			output.setMessage(outputmsg);
			// set output into operation
			operation.setOutput(output);					
			// get portType
			PortType portType = definition.getPortType(new QName(targetNS, portTypeName));
			// add <operation> to the <portType>						
			portType.addOperation(operation);
			// <binding> / <operation>
			// create binding for operation
			BindingOperation bindingOperation = definition.createBindingOperation();
			bindingOperation.setName(operation.getName());
			SOAPOperation soapOperation = new SOAPOperationImpl();
			soapOperation.setSoapActionURI("");  
			bindingOperation.addExtensibilityElement(soapOperation);
			// <binding> / <operation> / <input> 
			// create input binding for operation
			BindingInput bindingInput = definition.createBindingInput();
			SOAPBody soapBodyInput = new SOAPBodyImpl();
			soapBodyInput.setUse(LITERAL_NAME_LABEL);
			bindingInput.addExtensibilityElement(soapBodyInput);
			// set input binding to binding operation
			bindingOperation.setBindingInput(bindingInput);
			// <binding> / <operation> / <output>
			// create output binding for operation			
			BindingOutput bindingOutput =  definition.createBindingOutput();
			SOAPBody soapBodyOutput = new SOAPBodyImpl();
			soapBodyOutput.setUse(LITERAL_NAME_LABEL);
			bindingOutput.addExtensibilityElement(soapBodyOutput);
			// set output binding to binding operation
			bindingOperation.setBindingOutput(bindingOutput);							
			// get binding
			Binding binding = definition.getBinding(new QName(targetNS, 
					WSDLUtils.createBindingName(baseNameDefinitionElements)));
			// add binding operation 	
			binding.addBindingOperation(bindingOperation);	
		}
		// log execution flow
		LOGGER.exit();		
	}
	
	public static void addCDProsumerOperationElementsToWSDLDefinition(Definition definition,
			XmlSchema xmlschema, String targetNS, String operationName, boolean isOperationLooped,
			String inputMessageName, String inputMessageTypeName, String outputMessageName,
			String outputMessageTypeName, String baseNameDefinitionElements, String baseAddress){

		// log execution flow
		LOGGER.entry();
		
		// check if the operation already exists or if it exists but the operation to be created is
		// looped 
		if(!WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName) ||
		   (WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName) && 
		    isOperationLooped)){
			// the definition doesn't contain the operation or it contains the operation but
			// the operation to be created is looped and so the existing operation has to be 
			// replaced by the looped one			
			// create port type name based on baseName
			String portTypeName = WSDLUtils.createPortTypeName(baseNameDefinitionElements);
			// check if the definition already contains that portType
			if(definition.getPortType(new QName(targetNS, portTypeName)) == null){
				// definition doesn't contain that portType	
				// create <portType>
				PortType portType = definition.createPortType();
				portType.setQName(new QName(targetNS,portTypeName));
				portType.setUndefined(false);
				// add <portType> to definition
				definition.addPortType(portType);
				// create <binding>
				Binding binding = definition.createBinding();
				binding.setUndefined(false);
				binding.setQName(new QName(targetNS, WSDLUtils
						.createBindingName(baseNameDefinitionElements)));
				// set <portType> to <binding>
				binding.setPortType(portType);
				// add <binding> to definition
				definition.addBinding(binding);	
				// create <soap:binding>
			    SOAPBinding soapBinding = new SOAPBindingImpl();
			    soapBinding.setStyle(SOAP_BINDING_STYLE_VALUE);
			    soapBinding.setTransportURI(SOAP_BINDING_TRANSPORT_URI_VALUE);
			    // set <soap:binding> to <binding>
				binding.addExtensibilityElement(soapBinding);
				// create <service>
				Service service = definition.createService();
				service.setQName(new QName(targetNS, WSDLUtils
						.createServiceName(baseNameDefinitionElements)));	
				// add <service> to definition
				definition.addService(service);	
				// create <port>
				Port port = definition.createPort();
				port.setName(WSDLUtils.createPortName(baseNameDefinitionElements));
				// set <binding> to <port> 
				port.setBinding(binding);
				// add <port> to <service>
				service.addPort(port);
				// create <soap:address>
				SOAPAddress soapAddress = new SOAPAddressImpl();
				soapAddress.setLocationURI(WSDLUtils.createCDAddress(baseAddress,
						baseNameDefinitionElements));
				// add <soap:address> to <port>
				port.addExtensibilityElement(soapAddress);							
			}						
			// create input <message> of the operation
			// <message>
			Message inputmsg = definition.createMessage();
			inputmsg.setQName(new QName(targetNS, WSDLUtils.createInputMessageName(inputMessageName)));
			// create part for input message
			// <message> / <part>
			Part inputmsgpart = definition.createPart();
			inputmsgpart.setName(PARAMETERS_NAME_LABEL);
			// set element to inputmsgpart					
			inputmsgpart.setElementName(new QName(targetNS, WSDLUtils
					.createInputMessageElementName(inputMessageTypeName)));				
			inputmsg.setUndefined(false);
			// set part into message
			inputmsg.addPart(inputmsgpart);
			// add message to definition
			definition.addMessage(inputmsg);
			// create <operation>	
			// <portType> / <operation>
			Operation operation = definition.createOperation();
			operation.setName(operationName);
			operation.setUndefined(false);
			// <portType> / <operation> / <input>
			// create input element for the operation
			Input input = definition.createInput();	
			// set message into input 
			input.setMessage(inputmsg);
			// set input into operation
			operation.setInput(input);
				
			// check if choreography instance request type exists inside xml schema
			if(xmlschema.getTypeByName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME) == null){
				// choreography instance request type doesn't exist inside xml schema			
				// create complex type choreographyInstanceRequest
				XmlSchemaComplexType complexTypeChoreographyInstanceRequest = 
						new XmlSchemaComplexType(xmlschema, true);
				complexTypeChoreographyInstanceRequest.setName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME);
				// create choreographyId element
				XmlSchemaElement choreographyIdElement = new XmlSchemaElement(xmlschema, false);
				choreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
				choreographyIdElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));
				// create choreographyInstanceRequest complex type sequence
				XmlSchemaSequence sequenceInChoreographyInstanceRequest = new XmlSchemaSequence();
				// add element to sequence
				sequenceInChoreographyInstanceRequest.getItems().add(choreographyIdElement);
				// set siaComplexTypeIn particle to sequenceInChoreographyInstanceRequest 
				complexTypeChoreographyInstanceRequest.setParticle(sequenceInChoreographyInstanceRequest);		
			}
					
			// create input type name
			String inputTypeName = WSDLUtils.createInputMessageTypeName(inputMessageTypeName);			
			// create complex type for the input message of the operation
			XmlSchemaComplexType complexTypeInput = new XmlSchemaComplexType(xmlschema, true);
			complexTypeInput.setName(inputTypeName);
			// create choreographyId element
			XmlSchemaElement inputTypeChoreographyIdElement = new XmlSchemaElement(xmlschema, false);
			inputTypeChoreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED
			inputTypeChoreographyIdElement
				.setSchemaTypeName(new QName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME));			
			// create senderParticipantName element
			XmlSchemaElement senderParticipantNameElement = new XmlSchemaElement(xmlschema, false);
			senderParticipantNameElement.setName(SENDER_PARTICIPANT_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			senderParticipantNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create receiverParticipantName element
			XmlSchemaElement receiverParticipantNameElement = new XmlSchemaElement(xmlschema, false);
			receiverParticipantNameElement.setName(RECEIVER_PARTICIPANT_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			receiverParticipantNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create choreographyTaskName element
			XmlSchemaElement choreographyTaskNameElement = new XmlSchemaElement(xmlschema, false);
			choreographyTaskNameElement.setName(CHOREOGRAPHY_TASK_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			choreographyTaskNameElement
				.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create messageName element
			XmlSchemaElement messageInputNameElement = new XmlSchemaElement(xmlschema, false);
			messageInputNameElement.setName(INPUT_MESSAGE_NAME_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			messageInputNameElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));				
			// create inputTypeMessage element
			XmlSchemaElement inputTypeMessageElement = new XmlSchemaElement(xmlschema, false);
			inputTypeMessageElement.setName(INPUT_MESSAGE_DATA_ELEMENTNAME);	
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			inputTypeMessageElement.setSchemaTypeName(new QName(inputMessageTypeName));	
			XmlSchemaSequence sequenceInput = new XmlSchemaSequence();	
			// add elements to sequence
			sequenceInput.getItems().add(inputTypeChoreographyIdElement);
			sequenceInput.getItems().add(senderParticipantNameElement);
			sequenceInput.getItems().add(receiverParticipantNameElement);
			sequenceInput.getItems().add(choreographyTaskNameElement);
			sequenceInput.getItems().add(messageInputNameElement);
			
			// check if the operation belongs to a loop
			if(isOperationLooped) {
				// the operation belongs to a loop
				// create loopIndexes element
				XmlSchemaElement loopIndexesElement = new XmlSchemaElement(xmlschema, false);
				loopIndexesElement.setName(LOOP_INDEXES_ELEMENTNAME);	
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				loopIndexesElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));	
				// add loopIndexesElement to sequenceInput
				sequenceInput.getItems().add(loopIndexesElement);		
			}
			// add inputTypeMessageElement to sequenceInput
			sequenceInput.getItems().add(inputTypeMessageElement);
 			
			// check if an output message has been specified
			if(outputMessageTypeName != null){
				// output message specified
				// create messageOutputNameElement element
				XmlSchemaElement messageOutputNameElement = new XmlSchemaElement(xmlschema, false);
				messageOutputNameElement.setName(OUTPUT_MESSAGE_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				messageOutputNameElement
					.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));	
				// add messageOutputNameElement to sequenceInput 
				sequenceInput.getItems().add(messageOutputNameElement);
			}
			
			complexTypeInput.setParticle(sequenceInput);
						
			// check if the operation is already defined inside the definition and 
			// if the operation to be created is looped
			if (WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName) && 
				    isOperationLooped) {
				// the operation is already defined inside the definition and 
				// the operation to be created is looped
				// remove schema elements related to the input message of the existing operation 
				WSDLUtils.removeXmlSchemaElement(xmlschema, WSDLUtils
						.createInputMessageElementName(inputMessageTypeName));
			}			
			
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementIn = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementIn.setName(WSDLUtils.createInputMessageElementName(inputMessageTypeName));					
			xmlSchemaElementIn.setSchemaTypeName(new QName(inputTypeName));	
			
			// check if an output message is present
			if(outputMessageTypeName != null){				
				// create output message of the operation
				// <message>
				Message outputmsg = definition.createMessage();
				outputmsg.setQName(new QName(targetNS, WSDLUtils
						.createOutputMessageName(outputMessageName)));
				// create part for output message
				// <message> / <part>
				Part outputmsgpart = definition.createPart();
				outputmsgpart.setName(PARAMETERS_NAME_LABEL);							
				// set element to outputmsgpart
				outputmsgpart.setElementName(new QName(targetNS, WSDLUtils
						.createOutputMessageElementName(outputMessageTypeName)));							
				outputmsg.setUndefined(false);
				// set part into message
				outputmsg.addPart(outputmsgpart);
				// add message to definition
				definition.addMessage(outputmsg);
				// <portType> / <operation> / <output>
				// create output element for the operation
				Output output = definition.createOutput();	
				// set message into output 
				output.setMessage(outputmsg);
				// set output into operation
				operation.setOutput(output);

				// create messageOutputNameElement element
				XmlSchemaElement messageOutputNameElement = new XmlSchemaElement(xmlschema, false);
				messageOutputNameElement.setName(OUTPUT_MESSAGE_NAME_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				messageOutputNameElement
					.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));					
				
				// create complex type for the output message
				// create output type name
				String outputTypeName = WSDLUtils.createOutputMessageTypeName(inputMessageTypeName);			
				// create complex type for the input message of the operation
				XmlSchemaComplexType complexTypeOutput = new XmlSchemaComplexType(xmlschema, true);
				complexTypeOutput.setName(outputTypeName);				
				// create outputTypeMessage element
				XmlSchemaElement outputTypeMessageElement = new XmlSchemaElement(xmlschema, false);
				outputTypeMessageElement.setName(OUTPUT_MESSAGE_DATA_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				outputTypeMessageElement.setSchemaTypeName(new QName(outputMessageTypeName));	
				XmlSchemaSequence sequenceOutput = new XmlSchemaSequence();	
				// add elements to sequence
				sequenceOutput.getItems().add(inputTypeChoreographyIdElement);
				sequenceOutput.getItems().add(senderParticipantNameElement);
				sequenceOutput.getItems().add(receiverParticipantNameElement);
				sequenceOutput.getItems().add(choreographyTaskNameElement);
				sequenceOutput.getItems().add(messageOutputNameElement);
				sequenceOutput.getItems().add(outputTypeMessageElement);									
				// set complexTypeInReceive particle to sequenceInReceive 
				complexTypeOutput.setParticle(sequenceOutput);				

				// check if the operation is already defined inside the definition and 
				// if the operation to be created is looped
				if (WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName) && 
					    isOperationLooped) {
					// the operation is already defined inside the definition and 
					// the operation to be created is looped
					// remove schema elements related to the output message of the existing operation 
					WSDLUtils.removeXmlSchemaElement(xmlschema, WSDLUtils
							.createInputMessageElementName(outputMessageTypeName));
				}				
				
				// create into schema the element corresponding to the part of the message
				// <types> / <schema> / <element>
				XmlSchemaElement xmlSchemaElementOut = new XmlSchemaElement(xmlschema, true);
				// set element output message name
				xmlSchemaElementOut.setName(WSDLUtils.createOutputMessageElementName(outputMessageTypeName));
				// set element output message type to outputMessageTypeName			
				xmlSchemaElementOut.setSchemaTypeName(new QName(outputTypeName));				
			}
			// get portType
			PortType portType = definition.getPortType(new QName(targetNS, portTypeName));
		
			// check if the operation is already defined inside the definition and 
			// if the operation to be created is looped
			if (WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName) && 
				    isOperationLooped) {
				// the operation is already defined inside the definition and 
				// the operation to be created is looped		
				// remove the existing operation from portType
				portType.removeOperation(operation.getName(), operation.getInput().getName(),
						operation.getOutput().getName());
			}				
			
			// add <operation> to the <portType>						
			portType.addOperation(operation);
			// <binding> / <operation>
			// create binding for operation
			BindingOperation bindingOperation = definition.createBindingOperation();
			bindingOperation.setName(operation.getName());
			SOAPOperation soapOperation = new SOAPOperationImpl();
			soapOperation.setSoapActionURI("");  
			bindingOperation.addExtensibilityElement(soapOperation);
			// <binding> / <operation> / <input> 
			// create input binding for operation
			BindingInput bindingInput = definition.createBindingInput();
			SOAPBody soapBodyInput = new SOAPBodyImpl();
			soapBodyInput.setUse(LITERAL_NAME_LABEL);
			bindingInput.addExtensibilityElement(soapBodyInput);
			// set input binding to binding operation
			bindingOperation.setBindingInput(bindingInput);
			// check if an output message is present
			if(outputMessageTypeName != null){	
				// create output binding 
				// <binding> / <operation> / <output>
				// create output binding for operation			
				BindingOutput bindingOutput =  definition.createBindingOutput();
				SOAPBody soapBodyOutput = new SOAPBodyImpl();
				soapBodyOutput.setUse(LITERAL_NAME_LABEL);
				bindingOutput.addExtensibilityElement(soapBodyOutput);
				// set output binding to binding operation
				bindingOperation.setBindingOutput(bindingOutput);
			}
			// get binding
			Binding binding = definition.getBinding(new QName(targetNS, WSDLUtils
					.createBindingName(baseNameDefinitionElements)));
			
			// check if the operation is already defined inside the definition and 
			// if the operation to be created is looped
			if (WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName) && 
				    isOperationLooped) {
				// the operation is already defined inside the definition and 
				// the operation to be created is looped		
				// remove the existing operation from binding
				binding.removeBindingOperation(operation.getName(), operation.getInput().getName(),
						operation.getOutput() != null ? operation.getOutput().getName() : null);
			}			
			
			// add binding operation 	
			binding.addBindingOperation(bindingOperation);
		}
		// log execution flow
		LOGGER.exit();			
	}
	
	public static void addCDClientOperationElementsToWSDLDefinition(Definition definition,
			XmlSchema xmlschema, String targetNS, String operationName, String inputMessageName,
			String inputMessageTypeName, String outputMessageName, String outputMessageTypeName,
			String baseNameDefinitionElements, String baseAddress, boolean isChoreographyIdRequired){

		// log execution flow
		LOGGER.entry();	
		
		// check if the operation already exists 
		if(!WSDLUtils.existOperationIntoWSDLDefinition(definition, operationName)){
			// the definition doesn't contain the operation 
			// create port type name based on baseName
			String portTypeName = WSDLUtils.createPortTypeName(baseNameDefinitionElements);
			// check if the definition already contains that portType
			if(definition.getPortType(new QName(targetNS, portTypeName)) == null){
				// definition doesn't contain that portType	
				// create <portType>
				PortType portType = definition.createPortType();
				portType.setQName(new QName(targetNS,portTypeName));
				portType.setUndefined(false);
				// add <portType> to definition
				definition.addPortType(portType);
				// create <binding>
				Binding binding = definition.createBinding();
				binding.setUndefined(false);
				binding.setQName(new QName(targetNS, WSDLUtils
						.createBindingName(baseNameDefinitionElements)));
				// set <portType> to <binding>
				binding.setPortType(portType);
				// add <binding> to definition
				definition.addBinding(binding);	
				// create <soap:binding>
			    SOAPBinding soapBinding = new SOAPBindingImpl();
			    soapBinding.setStyle(SOAP_BINDING_STYLE_VALUE);
			    soapBinding.setTransportURI(SOAP_BINDING_TRANSPORT_URI_VALUE);
			    // set <soap:binding> to <binding>
				binding.addExtensibilityElement(soapBinding);
				// create <service>
				Service service = definition.createService();
				service.setQName(new QName(targetNS, WSDLUtils
						.createServiceName(baseNameDefinitionElements)));	
				// add <service> to definition
				definition.addService(service);	
				// create <port>
				Port port = definition.createPort();
				port.setName(WSDLUtils.createPortName(baseNameDefinitionElements));
				// set <binding> to <port> 
				port.setBinding(binding);
				// add <port> to <service>
				service.addPort(port);
				// create <soap:address>
				SOAPAddress soapAddress = new SOAPAddressImpl();
				soapAddress.setLocationURI(WSDLUtils.createCDAddress(baseAddress,
						baseNameDefinitionElements));
				// add <soap:address> to <port>
				port.addExtensibilityElement(soapAddress);							
			}						
			// create input <message> of the operation
			// <message>
			Message inputmsg = definition.createMessage();
			inputmsg.setQName(new QName(targetNS, WSDLUtils.createInputMessageName(inputMessageName)));
			// create part for input message
			// <message> / <part>
			Part inputmsgpart = definition.createPart();
			inputmsgpart.setName(PARAMETERS_NAME_LABEL);
			// set element to inputmsgpart					
			inputmsgpart.setElementName(new QName(targetNS, WSDLUtils
					.createInputMessageElementName(inputMessageTypeName)));				
			inputmsg.setUndefined(false);
			// set part into message
			inputmsg.addPart(inputmsgpart);
			// add message to definition
			definition.addMessage(inputmsg);
			// create <operation>	
			// <portType> / <operation>
			Operation operation = definition.createOperation();
			operation.setName(operationName);
			operation.setUndefined(false);
			// <portType> / <operation> / <input>
			// create input element for the operation
			Input input = definition.createInput();	
			// set message into input 
			input.setMessage(inputmsg);
			// set input into operation
			operation.setInput(input);
	
			// check if choreography instance request type exists inside xml schema
			if(xmlschema.getTypeByName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME) == null){
				// choreography instance request type doesn't exist inside xml schema			
				// create complex type choreographyInstanceRequest
				XmlSchemaComplexType complexTypeChoreographyInstanceRequest = new XmlSchemaComplexType(
						xmlschema, true);
				complexTypeChoreographyInstanceRequest.setName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME);
				// create choreographyId element
				XmlSchemaElement choreographyIdElement = new XmlSchemaElement(xmlschema, false);
				choreographyIdElement.setName(CHOREOGRAPHY_ID_TYPENAME);
				choreographyIdElement.setSchemaTypeName(new QName(XMLSCHEMA_NS, XMLSCHEMA_STRINGTYPE_NAME));
				// create choreographyInstanceRequest complex type sequence
				XmlSchemaSequence sequenceInChoreographyInstanceRequest = new XmlSchemaSequence();
				// add element to sequence
				sequenceInChoreographyInstanceRequest.getItems().add(choreographyIdElement);
				// set siaComplexTypeIn particle to sequenceInChoreographyInstanceRequest 
				complexTypeChoreographyInstanceRequest.setParticle(sequenceInChoreographyInstanceRequest);		
			}				
			
			// create input type name
			String inputTypeName = WSDLUtils.createInputMessageTypeName(inputMessageTypeName);			
			// create complex type for the input message of the operation
			XmlSchemaComplexType complexTypeInput = new XmlSchemaComplexType(xmlschema, true);
			complexTypeInput.setName(inputTypeName);		
			// create sequence for the complex type of the element related to the input message
			XmlSchemaSequence sequenceInput = new XmlSchemaSequence();
			// check if the input message of the operation has to contain choreography id
			if(isChoreographyIdRequired) {
				// create choreographyId element
				XmlSchemaElement inputTypeChoreographyIdElementIn = new XmlSchemaElement(xmlschema, false);
				inputTypeChoreographyIdElementIn.setName(CHOREOGRAPHY_ID_TYPENAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED
				inputTypeChoreographyIdElementIn
					.setSchemaTypeName(new QName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME));
				// add choreographyId element to sequence
				sequenceInput.getItems().add(inputTypeChoreographyIdElementIn);
			}	

			// create inputTypeMessage element
			XmlSchemaElement inputTypeMessageElement = new XmlSchemaElement(xmlschema, false);							
			inputTypeMessageElement.setName(MESSAGE_DATA_ELEMENTNAME);
			// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
			inputTypeMessageElement.setSchemaTypeName(new QName(inputMessageTypeName));
			
			// add message data element to sequence
			sequenceInput.getItems().add(inputTypeMessageElement);	
			// set complexTypeInput particle to sequenceInReceive 
			complexTypeInput.setParticle(sequenceInput);
						
			// create into schema the element corresponding to the part of the message
			// <types> / <schema> / <element>
			XmlSchemaElement xmlSchemaElementIn = new XmlSchemaElement(xmlschema, true);
			xmlSchemaElementIn.setName(WSDLUtils.createInputMessageElementName(inputMessageTypeName));
			xmlSchemaElementIn.setSchemaTypeName(new QName(inputTypeName));				
			// check if an output message is present
			if(outputMessageTypeName != null){				
				// create output message of the operation
				// <message>
				Message outputmsg = definition.createMessage();
				outputmsg.setQName(new QName(targetNS, WSDLUtils
						.createOutputMessageName(outputMessageName)));
				// create part for output message
				// <message> / <part>
				Part outputmsgpart = definition.createPart();
				outputmsgpart.setName(PARAMETERS_NAME_LABEL);							
				// set element to outputmsgpart
				outputmsgpart.setElementName(new QName(targetNS, WSDLUtils
						.createOutputMessageElementName(outputMessageTypeName)));							
				outputmsg.setUndefined(false);
				// set part into message
				outputmsg.addPart(outputmsgpart);
				// add message to definition
				definition.addMessage(outputmsg);
				// <portType> / <operation> / <output>
				// create output element for the operation
				Output output = definition.createOutput();	
				// set message into output 
				output.setMessage(outputmsg);
				// set output into operation
				operation.setOutput(output);	
				
				// create output type name
				String outputTypeName = WSDLUtils.createOutputMessageTypeName(outputMessageTypeName);			
				// create complex type for the output message of the operation
				XmlSchemaComplexType complexTypeOutput = new XmlSchemaComplexType(xmlschema, true);
				complexTypeOutput.setName(outputTypeName);
				
				// create into schema the element corresponding to the part of the message
				// <types> / <schema> / <element>
				XmlSchemaElement xmlSchemaElementOut = new XmlSchemaElement(xmlschema, true);
				xmlSchemaElementOut.setName(WSDLUtils.createOutputMessageElementName(outputMessageTypeName));
				
				// create sequence for the complex type of the element related to the output message
				XmlSchemaSequence sequenceOutput = new XmlSchemaSequence();	
				// create choreographyId element
				XmlSchemaElement inputTypeChoreographyIdElementOut = new XmlSchemaElement(xmlschema, false);
				inputTypeChoreographyIdElementOut.setName(CHOREOGRAPHY_ID_TYPENAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED
				inputTypeChoreographyIdElementOut
					.setSchemaTypeName(new QName(CHOREOGRAPHY_INSTANCE_REQUEST_TYPENAME));
				// add choreographyId element to sequence
				sequenceOutput.getItems().add(inputTypeChoreographyIdElementOut);				
	
				// create outputTypeMessage element
				XmlSchemaElement outputTypeMessageElement = new XmlSchemaElement(xmlschema, false);							
				outputTypeMessageElement.setName(MESSAGE_DATA_ELEMENTNAME);
				// --- CHECK IF THE NAMESPACE HAS TO BE SPECIFIED									
				outputTypeMessageElement.setSchemaTypeName(new QName(outputMessageTypeName));

				// add message data element to sequence
				sequenceOutput.getItems().add(outputTypeMessageElement);	
				
				// set complexTypeOutput particle to sequenceOutput 
				complexTypeOutput.setParticle(sequenceOutput);
				xmlSchemaElementOut.setSchemaTypeName(new QName(outputTypeName));							
			}
			// get portType
			PortType portType = definition.getPortType(new QName(targetNS, portTypeName));			
			// add <operation> to the <portType>						
			portType.addOperation(operation);
			// <binding> / <operation>
			// create binding for operation
			BindingOperation bindingOperation = definition.createBindingOperation();
			bindingOperation.setName(operation.getName());
			SOAPOperation soapOperation = new SOAPOperationImpl();
			soapOperation.setSoapActionURI("");  
			bindingOperation.addExtensibilityElement(soapOperation);
			// <binding> / <operation> / <input> 
			// create input binding for operation
			BindingInput bindingInput = definition.createBindingInput();
			SOAPBody soapBodyInput = new SOAPBodyImpl();
			soapBodyInput.setUse(LITERAL_NAME_LABEL);
			bindingInput.addExtensibilityElement(soapBodyInput);
			// set input binding to binding operation
			bindingOperation.setBindingInput(bindingInput);
			// check if an output message is present
			if(outputMessageTypeName != null){	
				// create output binding 
				// <binding> / <operation> / <output>
				// create output binding for operation			
				BindingOutput bindingOutput =  definition.createBindingOutput();
				SOAPBody soapBodyOutput = new SOAPBodyImpl();
				soapBodyOutput.setUse(LITERAL_NAME_LABEL);
				bindingOutput.addExtensibilityElement(soapBodyOutput);
				// set output binding to binding operation
				bindingOperation.setBindingOutput(bindingOutput);
			}
			// get binding
			Binding binding = definition.getBinding(new QName(targetNS, WSDLUtils
					.createBindingName(baseNameDefinitionElements)));			
			// add binding operation 	
			binding.addBindingOperation(bindingOperation);
		}
		// log execution flow
		LOGGER.exit();			
	}		
}
