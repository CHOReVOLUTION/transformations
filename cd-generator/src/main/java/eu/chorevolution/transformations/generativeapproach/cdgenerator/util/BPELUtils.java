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

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Correlation;
import org.eclipse.bpel.model.CorrelationPattern;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.Correlations;
import org.eclipse.bpel.model.EndpointReferenceRole;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
import org.eclipse.bpel.model.partnerlinktype.PartnerlinktypeFactory;
import org.eclipse.bpel.model.partnerlinktype.Role;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyLoopType;
import org.eclipse.bpmn2.ChoreographyTask;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.wst.wsdl.Message;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.Part;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.WSDLFactory;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ArtifactData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyMessageInformation;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.DataTypeWSDLInformation;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.LoopedFlowNodeData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.MessageData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.OperationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PartnerLinkTypeData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesMessageType;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLDefinitionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELResource;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELResourceFactoryImpl;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer.BPELWriter;

public class BPELUtils {

	private static final String ARTIFACTS_DOCUMENT_ROLE_LABEL = "role";
	private static final String ASSIGN_NAME_PREFIX = "Assign";
	private static final String BPEL_NS = "http://docs.oasis-open.org/wsbpel/2.0/process/executable";
	private static final String BPEL_NS_PREFIX = "bpel";
	private static final String BPEL_FILE_EXTENSION = ".bpel";
	private static final String BPEL_VARIABLE_ACCESSOR_SYMBOL = "$";
	private static final String BPEL_MESSAGE_PART_SEPARATOR_SYMBOL = ".";
	private static final String BRACKET_LEFT = "(";
	private static final String BRACKET_RIGHT = ")";
	private static final String CD_LABEL_NS = "cd";
	private static final String CDNAME_PARTNER_LINK_TYPE_NAME_PREFIX = "CD";
	private static final String CORRELATION_INITIATE_JOIN_LABEL = "join";
	private static final String CORRELATION_SET_CHOREOGRAPHY_ID_NAME = "CorrelationSetChoreographyID";
	private static final String CHOREOGRAPHY_ID_VARIABLE_NAME = "choreographyID";
	private static final String COLON_LABEL = ":";
	private static final String COMMA_LABEL = ",";
	private static final String ELEMENT_DEFAULT_NAME = "element";
	private static final String FOR_EACH_PREFIX= "ForEach";
	private static final String FLOW_NAME_PREFIX = "FLow";
	private static final String HASH = "#";
	private static final String HYPHEN = "-";
	private static final String IF_NAME_PREFIX = "If";
	private static final String INPUT_LABEL = "Input";
	private static final String INVOKE_NAME_PREFIX = "Invoke";
	private static final String LOOP_NAME_PREFIX = "loopIndexes";
	private static final String NAME_LABEL = "name";	
	private static final String OUTPUT_LABEL = "Output";
	private static final String PARTICIPANT_NAME_VARIABLE_NAME = "participantName";
	private static final String PARTICIPANT_NAME_VARIABLE_NAME_SUFFIX = "participant_name";
	private static final String PARTICIPANT_PARTNER_LINK_ADDRESS_VARIABLE_NAME_SUFFIX =
			"partnerlink_address";
	private static final String PLNKTYPE_SUFFIX ="PLT";
	private static final String PORT_TYPE_LABEL = "portType";
	private static final String PROCESS_LABEL_NS = "process";
	private static final String PROPERTIES_ALIASES_FILE_NAME = "propertiesAliases";
	private static final String PROPERTIES_ALIASES_NAME_LABEL = "propertiesAliases";
	private static final String PROPERTIES_FILE_NAME = "properties";
	private static final String PROPERTIES_NAME_LABEL = "properties";
	private static final String PROPERTIES_PREFIX = "properties";
	private static final String QUOTATION_MARK= "'";
	private static final String RECEIVE_NAME_PREFIX = "Receive";	
	private static final String REPLY_NAME_PREFIX = "Reply";
	private static final String SEQUENCE_NAME_PREFIX = "Sequence";
	private static final String SIA_MANAGER_GET_ADDRESS_METHOD_NAME = "getParticipantAddress";
	private static final String SIA_MANAGER_NS_URI = 
			"java:eu.chorevolution.cd.utility.sia.endpoints.manager.services.SiaEndpointsManagerService";
	private static final String SIA_MANAGER_PREFIX = "siaendpointsmanager";
	private static final String SLASH_SEPARATOR_SYMBOL = "/";
	private static final String START_COUNTER_VALUE = "1";
	private static final String TRUE_VALUE = "true";
	private static final String XSD_PREFIX = "xsd";
	private static final String XSD_STRING_TYPE_NAME = "string";
	private static final String XSD_TARGET_NAMESPACE = "http://www.w3.org/2001/XMLSchema" ;	
	private static final String UNDERSCORE = "_";
	private static final String WHILE_PREFIX= "While";
	private static final String WSDL_IMPORT_TYPE = "http://schemas.xmlsoap.org/wsdl/";
	private static final String WSDL_EXTENSION = ".wsdl";
	private static final String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
	private static final String XMLSCHEMA_NS_PREFIX = "xsd";
	private static final String XMLNS_LABEL = "xmlns";
	private static final String XPATH_CONCAT_OPERATOR_NAME = "concat";
	private static final String XPATH_EXPRESSION_CURRENT_DATE_TIME_AS_STRING_ONLY_NUMBERS =
			"replace(string(current-dateTime()),'[^0-9]', '')";
	private static final String XPATH_QUERY_CHOREOGRAPHY_ID = "choreographyId/choreographyId";
	private static final String XPATH_QUERY_CHOREOGRAPHY_TASK_NAME = "choreographyTaskName";	
	private static final String XPATH_QUERY_INPUT_MESSAGE_NAME = "inputMessageName";	
	private static final String XPATH_QUERY_INPUT_MESSAGE_DATA = "inputMessageData";
	private static final String XPATH_QUERY_LOOP_INDEXES = "loopIndexes";
	private static final String XPATH_1_0_QUERY_LANGUAGE = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0";
	private static final String XPATH_2_0_QUERY_LANGUAGE = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath2.0";
	private static final String XPATH_QUERY_MESSAGE_DATA = "messageData";	
	private static final String XPATH_QUERY_OUTPUT_MESSAGE_NAME = "outputMessageName";	
	private static final String XPATH_QUERY_OUTPUT_MESSAGE_DATA = "outputMessageData";	
	private static final String XPATH_QUERY_SENDER_PARTICIPANT_NAME = "senderParticipantName";
	private static final String XPATH_QUERY_RECEIVER_PARTICIPANT_NAME = "receiverParticipantName";
	
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(BPELUtils.class);
	
	public static void writeBPELprocessToFile(BPELData bpelData, String destination, String fileName){
		
		String bpelProcessOut = new StringBuilder(destination).append(File.separator).append(fileName)
				.append(BPEL_FILE_EXTENSION).toString();
		File file = new File(bpelProcessOut);
		URI fileUriTempModelNormalized = URI.createFileURI(file.getAbsolutePath());
		Resource bpelResource = new BPELResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		bpelResource.getContents().add((EObject) bpelData.getProcess());
		try {
			BPELWriter bpelWriter = new BPELWriter((BPELResource) bpelResource, DocumentBuilderFactory
					.newInstance().newDocumentBuilder().newDocument());
			File out = new File(bpelProcessOut);
//			OutputStream out = new FileOutputStream(new File(bpelProcessOut));
//			Map<String,Boolean> args = new HashMap<String,Boolean>();
//			args.put("bpel.skip.auto.import",new Boolean(true));
////			bpelWriter.write((BPELResource) bpelResource, out, args);
			bpelWriter.write(bpelData, out);
		} catch (IOException | ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDGeneratorException(
					"Exception into createBPELprocessToFile see log file for details ");
		}
	}

	
	public static PartnerLinkTypeData getPartnerLinkTypeData(Document document, String portName){
		
		// log execution flow
		LOGGER.entry("Port name: "+portName);
		
		PartnerLinkTypeData partnerLinkTypeData = null;
		NodeList nodeList = document.getFirstChild().getChildNodes();
		for (int i = 0, len = nodeList.getLength(); i < len; i++){
	        Node node = nodeList.item(i);
		    if(node.hasChildNodes()){
		    	Node roleElement = null;
		    	if(node.getFirstChild().getNodeName().equals(ARTIFACTS_DOCUMENT_ROLE_LABEL))
		    		roleElement = node.getFirstChild();
		    	else
		    		roleElement = node.getFirstChild().getNextSibling();
		    	while(roleElement != null){
				    if(WSDLUtils.getLocalPartOfNamespaceIdentifier(roleElement.getAttributes()
				    		.getNamedItem(PORT_TYPE_LABEL).getNodeValue()).equals(portName)){
				    	partnerLinkTypeData = new PartnerLinkTypeData();
						partnerLinkTypeData.setPortTypeName(portName);
				    	partnerLinkTypeData.setName(node.getAttributes().getNamedItem(NAME_LABEL)
				    			.getNodeValue());
				    	partnerLinkTypeData.setRoleName(roleElement.getAttributes().getNamedItem(NAME_LABEL)
				    			.getNodeValue());
				    }
				    roleElement = roleElement.getNextSibling();
		    	}
		    }   
		}   
		
		// log partner like type information
		LOGGER.info("Partner Link Type name: "+partnerLinkTypeData.getName()+" Role: "
				+partnerLinkTypeData.getRoleName());
		// log execution flow
		LOGGER.exit();
		
		return partnerLinkTypeData;
	}
	
	public static String getPortTypeNameOfRoleOfPartnerLinkType(Document document,
			String partnerLinkTypeName, String roleName){
		
		String portTypeName = null;
		NodeList nodeList = document.getFirstChild().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++){
	        Node node = nodeList.item(i);
		    if(node.hasChildNodes() && 
			   node.getAttributes().getNamedItem(NAME_LABEL) != null &&
			   node.getAttributes().getNamedItem(NAME_LABEL).getNodeValue().equals(Utils
					   .getLocalPartXMLQName(partnerLinkTypeName))){
		    	Node roleElement = null;
		    	if(node.getFirstChild().getNodeName().equals(ARTIFACTS_DOCUMENT_ROLE_LABEL)) {
		    		roleElement = node.getFirstChild();
		    	}	
		    	else {
		    		roleElement = node.getFirstChild().getNextSibling();
		    	}	
		    	while(roleElement != null){
		    		if(roleElement.getAttributes().getNamedItem(NAME_LABEL).getNodeValue().equals(roleName)){
		    			return Utils.getLocalPartXMLQName(roleElement.getAttributes()
		    					.getNamedItem(PORT_TYPE_LABEL).getNodeValue());
		    		}
				roleElement = roleElement.getNextSibling();
		    	}
		    }	
		}
		return portTypeName;
	}
		
	public static Pick getMainPickElementFromBpelElements(Deque<BPELExtensibleElement> bpelElements){
		
		for (BPELExtensibleElement bpelElement : bpelElements) {
			if(bpelElement instanceof Pick){
				return (Pick) bpelElement;
			}
		}
		return null;
	}
	
	public static Map<String, String> addWSDLImportsToCDProsumerBpelProcessAndGetImportNamespaceData(
			Process process, String choreographyTNS, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, WSDLDefinitionData prosumerPartWSDLDefinitionData, 
			ArtifactData artifactData,  String propertiesTNS){

		HashMap<String, String> importedNamespacesData = new HashMap<>();
		importedNamespacesData.put(BPEL_NS_PREFIX,BPEL_NS);	

		// add xsd prefix and namespace to importedNamespacesData
		importedNamespacesData.put(XSD_PREFIX, XSD_TARGET_NAMESPACE);
				
		// add properties import item
		Import propertiesImportItem = BPELFactory.eINSTANCE.createImport();
		propertiesImportItem.setImportType(WSDL_IMPORT_TYPE);
		propertiesImportItem.setLocation(PROPERTIES_FILE_NAME+WSDL_EXTENSION);		
		propertiesImportItem.setNamespace(WSDLUtils.createNamespace(choreographyTNS, PROPERTIES_NAME_LABEL));		
		// add import to process
		process.getImports().add(propertiesImportItem);	
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(PROPERTIES_PREFIX, WSDLUtils
				.createNamespace(choreographyTNS, PROPERTIES_NAME_LABEL));
		
		// add propertiesAliases import item
		Import propertiesAliasesImportItem = BPELFactory.eINSTANCE.createImport();	
		propertiesAliasesImportItem.setImportType(WSDL_IMPORT_TYPE);	
		propertiesAliasesImportItem.setLocation(PROPERTIES_ALIASES_FILE_NAME+WSDL_EXTENSION);
		propertiesAliasesImportItem.setNamespace(WSDLUtils.createNamespace(choreographyTNS, 
				PROPERTIES_ALIASES_NAME_LABEL));		
		// add import to process
		process.getImports().add(propertiesAliasesImportItem);				
		
		// add artifacts import item
		Import importItemArtifact = BPELFactory.eINSTANCE.createImport();	
		importItemArtifact.setImportType(WSDL_IMPORT_TYPE);	
		importItemArtifact.setLocation(artifactData.getFileName()+WSDL_EXTENSION);
		importItemArtifact.setNamespace(artifactData.getNamespace());
		// add import to process
		process.getImports().add(importItemArtifact);
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(artifactData.getPrefix(),artifactData.getNamespace());
	
		// add cd import item
		Import importItemCD = BPELFactory.eINSTANCE.createImport();
		importItemCD.setImportType(WSDL_IMPORT_TYPE);	
		importItemCD.setLocation(cdWSDLDefinitionData.getWsdlFileName()+WSDL_EXTENSION);
		importItemCD.setNamespace(cdWSDLDefinitionData.getWsdl().getTargetNamespace());
		// add import to process
		process.getImports().add(importItemCD);
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(cdWSDLDefinitionData.getPrefix(), cdWSDLDefinitionData.getWsdl()
				.getTargetNamespace());
		
		// add consumer part import item
		Import importItemProsumerPart = BPELFactory.eINSTANCE.createImport();		
		importItemProsumerPart.setImportType(WSDL_IMPORT_TYPE);	
		importItemProsumerPart.setLocation(prosumerPartWSDLDefinitionData.getWsdlFileName()+WSDL_EXTENSION);
		importItemProsumerPart.setNamespace(prosumerPartWSDLDefinitionData.getWsdl().getTargetNamespace());
		// add import to process
		process.getImports().add(importItemProsumerPart);
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(prosumerPartWSDLDefinitionData.getPrefix(), prosumerPartWSDLDefinitionData
				.getWsdl().getTargetNamespace());
			
		for (Entry<String, WSDLDefinitionData> wsdlDefinitionData : wsdlDefinitions.entrySet()) {
			// create importItem 
			Import importItem = BPELFactory.eINSTANCE.createImport();	
			importItem.setImportType(WSDL_IMPORT_TYPE);
			importItem.setLocation(wsdlDefinitionData.getValue().getWsdlFileName()+WSDL_EXTENSION);
			importItem.setNamespace(wsdlDefinitionData.getValue().getWsdl().getTargetNamespace());
			// add import to process
			process.getImports().add(importItem);
			// add prefix and namespace to importedNamespacesData
			importedNamespacesData.put(wsdlDefinitionData.getValue().getPrefix(), wsdlDefinitionData
					.getValue().getWsdl().getTargetNamespace());
		}
		
		// add properties and namespace to importedNamespacesData
		importedNamespacesData.put(PROPERTIES_PREFIX, propertiesTNS);				
		// add xsd namespace to importedNamespacesData
		importedNamespacesData.put(XMLSCHEMA_NS_PREFIX, XMLSCHEMA_NS);

		
		return importedNamespacesData;
	}
	
	public static Map<String, String> addWSDLImportsToCDClientBpelProcessAndGetImportNamespaceData(
			Process process, String choreographyTNS, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, ArtifactData artifactData){

		HashMap<String, String> importedNamespacesData = new HashMap<>();
		importedNamespacesData.put(BPEL_NS_PREFIX,BPEL_NS);	

		// add xsd prefix and namespace to importedNamespacesData
		importedNamespacesData.put(XSD_PREFIX, XSD_TARGET_NAMESPACE);		
		
		// add properties import item
		Import propertiesImportItem = BPELFactory.eINSTANCE.createImport();
		propertiesImportItem.setImportType(WSDL_IMPORT_TYPE);
		propertiesImportItem.setLocation(PROPERTIES_FILE_NAME+WSDL_EXTENSION);		
		propertiesImportItem.setNamespace(WSDLUtils.createNamespace(choreographyTNS, PROPERTIES_NAME_LABEL));		
		// add import to process
		process.getImports().add(propertiesImportItem);	
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(PROPERTIES_PREFIX, WSDLUtils
				.createNamespace(choreographyTNS, PROPERTIES_NAME_LABEL));
		
		// add propertiesAliases import item
		Import propertiesAliasesImportItem = BPELFactory.eINSTANCE.createImport();	
		propertiesAliasesImportItem.setImportType(WSDL_IMPORT_TYPE);	
		propertiesAliasesImportItem.setLocation(PROPERTIES_ALIASES_FILE_NAME+WSDL_EXTENSION);
		propertiesAliasesImportItem.setNamespace(WSDLUtils.createNamespace(choreographyTNS, 
				PROPERTIES_ALIASES_NAME_LABEL));		
		// add import to process
		process.getImports().add(propertiesAliasesImportItem);			
		
		// add artifacts import item
		Import importItemArtifact = BPELFactory.eINSTANCE.createImport();	
		importItemArtifact.setImportType(WSDL_IMPORT_TYPE);	
		importItemArtifact.setLocation(artifactData.getFileName()+WSDL_EXTENSION);
		importItemArtifact.setNamespace(artifactData.getNamespace());
		// add import to process
		process.getImports().add(importItemArtifact);
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(artifactData.getPrefix(),artifactData.getNamespace());
	
		// add cd import item
		Import importItemCD = BPELFactory.eINSTANCE.createImport();
		importItemCD.setImportType(WSDL_IMPORT_TYPE);	
		importItemCD.setLocation(cdWSDLDefinitionData.getWsdlFileName()+WSDL_EXTENSION);
		importItemCD.setNamespace(cdWSDLDefinitionData.getWsdl().getTargetNamespace());
		// add import to process
		process.getImports().add(importItemCD);
		// add prefix and namespace to importedNamespacesData
		importedNamespacesData.put(cdWSDLDefinitionData.getPrefix(), cdWSDLDefinitionData.getWsdl()
				.getTargetNamespace());
				
		for (Entry<String, WSDLDefinitionData> wsdlDefinitionData : wsdlDefinitions.entrySet()) {
			// create importItem 
			Import importItem = BPELFactory.eINSTANCE.createImport();	
			importItem.setImportType(WSDL_IMPORT_TYPE);
			importItem.setLocation(wsdlDefinitionData.getValue().getWsdlFileName()+WSDL_EXTENSION);
			importItem.setNamespace(wsdlDefinitionData.getValue().getWsdl().getTargetNamespace());
			// add import to process
			process.getImports().add(importItem);
			// add prefix and namespace to importedNamespacesData
			importedNamespacesData.put(wsdlDefinitionData.getValue().getPrefix(), wsdlDefinitionData
					.getValue().getWsdl().getTargetNamespace());
		}
		return importedNamespacesData;
	}
	
	public static void createCDProsumerOnMessageChoreographyTaskActivities(String cdName,
			Participant cdParticipant, ChoreographyTask task, WSDLDefinitionData cdWSDLDefinitionData, 
			WSDLDefinitionData prosumerPartWSDLDefinitionData, Process process, ArtifactData artifactData, 
			Deque<BPELExtensibleElement> bpelElements,
			Deque<org.eclipse.bpel.model.Sequence> sequences, PropertyAliasesData propertyAliasesData,
			boolean isProsumerClientParticipant){
		
		// log execution flow
		LOGGER.entry("CD name: "+cdName," Task name: "+task.getName()," CD WSDL Definition Data: "
				+cdWSDLDefinitionData +" Prosumer Part WSDL Definition Data: "
				+prosumerPartWSDLDefinitionData);
		
		LOGGER.info("Creating CD "+cdName+" Prosumer onMessage activities for choreography task "
				+task.getName());
		
		String taskName = WSDLUtils.formatOperationName(task.getName());
		// create initiatingMessageName
		String initiatingMessageName = BPMNUtils.getMessageNameChoreographyTaskSentToParticipant(task,
				cdParticipant);
		// create returnMessageName
		String returnMessageName = BPMNUtils.getMessageNameChoreographyTaskSentFromParticipant(task,
				cdParticipant);
		// get onMessageOperationData
		OperationData taskOperationData = WSDLUtils.getOperationDataWSDLOperation(cdWSDLDefinitionData.
				getWsdl(), taskName);	
		// get partner link for on message
		PartnerLinkTypeData partnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(artifactData.
				getArtifact(), taskOperationData.getPortTypeName());
		
		// get or create partnerLink
		PartnerLink partnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(BPELUtils
				.createPartnerLinkNameFromPartnerLinkTypeName(partnerLinkTypeData.getName()), 
				taskName, process, partnerLinkTypeData, artifactData,true);
		// create operation
		Operation operation = WSDLFactory.eINSTANCE.createOperation();
		operation.setName(taskName);			
		// create portType
		PortType portType = WSDLFactory.eINSTANCE.createPortType();
		portType.setQName(new QName(cdWSDLDefinitionData.getPrefix(), taskOperationData.getPortTypeName()));
		// get or create variable
		Variable operationInputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
				.createInputVariableName(cdName, taskName), cdWSDLDefinitionData.getPrefix(), 
				taskOperationData.getInputMessageData().getName());
		
		// create correlation set for choreographyID
		CorrelationSet correlationSetChoreographyID = BPELFactory.eINSTANCE.createCorrelationSet();
		correlationSetChoreographyID.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
		// create correlation for choreographyID
		Correlation correlationChoreographyID = BPELFactory.eINSTANCE.createCorrelation();
		correlationChoreographyID.setSet(correlationSetChoreographyID);
		correlationChoreographyID.setInitiate(CORRELATION_INITIATE_JOIN_LABEL);
		// create activityCorrelations
		Correlations activityCorrelations = BPELFactory.eINSTANCE.createCorrelations();
		activityCorrelations.getChildren().add(correlationChoreographyID);
		
		// create onMessage
		OnMessage onMessage = BPELFactory.eINSTANCE.createOnMessage();	
		// create onMessageSequence
		Sequence onMessageSequence = BPELFactory.eINSTANCE.createSequence();
		onMessageSequence.setName(BPELUtils.createSequenceName(taskName));
		// set onMessage operation
		onMessage.setOperation(operation);
		// set onMessage partnerLink
		onMessage.setPartnerLink(partnerLink);			
		// set onMessage portType
		onMessage.setPortType(portType);			
		// set onMessage variable
		onMessage.setVariable(operationInputVariable);
		
		// create assign choreographyID Variable
		Assign assignChoreographyIDVariable = BPELUtils
				.createAssignChoreographyIDVariableFromVariable(process,
					taskOperationData.getInputMessageData(), operationInputVariable);		
				
		// add assignChoreographyIDVariable to onMessageSequence
		onMessageSequence.getActivities().add(assignChoreographyIDVariable);
		
		// get or create participant name variable
		Variable participantNameVariable = BPELUtils.getOrCreateStringVariable(process,
				PARTICIPANT_NAME_VARIABLE_NAME);
		// create assign participantName Variable
		Assign assignParticipantNameVariable = BPELUtils.createAssignStringVariable(process, 
				participantNameVariable, cdParticipant.getName());
		// add assignParticipantNameVariable to onMessageSequence
		onMessageSequence.getActivities().add(assignParticipantNameVariable);
		
		// set onMessageSequence as the activity of onMessage
		onMessage.setActivity(onMessageSequence);
		// set activityCorrelations on onMessage 
		onMessage.setCorrelations(activityCorrelations);
				
		// add onMessageSequence to sequences as the last sequence
		sequences.addLast(onMessageSequence);
		
		// get the main pick element from bpel elements
		Pick mainPick = BPELUtils.getMainPickElementFromBpelElements(bpelElements);
		// append onMessage to the onMessage elements of the main pick
		mainPick.getMessages().add(onMessage);
						
		// log onMessage creation
		LOGGER.info("Created onMessage element for operation: "+operation.getName());		
		
		// create propertyAliasesMessageType for Input Message
		PropertyAliasesMessageType propertyAliasesMessageType = new PropertyAliasesMessageType(
				taskOperationData.getInputMessageData().getName(), taskOperationData.getInputMessageData()
				.getPartName());
		// add propertyAliasesMessageType to propertyAliasesData
		Utils.addPropertyAliasesData(propertyAliasesData, cdWSDLDefinitionData.getParticipantName(), 
				cdWSDLDefinitionData.getWsdlFileName(), cdWSDLDefinitionData.getWsdl().getTargetNamespace(), 
				propertyAliasesMessageType);		
		// create receiveProsumerPartOperationData
		OperationData receiveProsumerPartOperationData = WSDLUtils
				.getOperationDataWSDLOperation(prosumerPartWSDLDefinitionData.getWsdl(), taskName);
		// create receiveProsumerPartInputVariable
		Variable receiveProsumerPartInputVariable = BPELUtils.getOrCreateVariableForMessageType(process, 
				BPELUtils.createInputVariableName(cdParticipant.getName(), taskName), 
				prosumerPartWSDLDefinitionData.getPrefix(), 
				receiveProsumerPartOperationData.getInputMessageData().getName());
		// create receiveProsumerPartOutputVariable
		Variable receiveProsumerPartOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process, 
				BPELUtils.createOutputVariableName(cdParticipant.getName(), taskName), 
				prosumerPartWSDLDefinitionData.getPrefix(), receiveProsumerPartOperationData
				.getOutputMessageData().getName());
		// get or create choreographyID variable
		Variable choreographyIDVariable = BPELUtils.getOrCreateStringVariable(process,
				CHOREOGRAPHY_ID_VARIABLE_NAME);		
		Assign assignProsumerPartOperationInput;
		// check if choreography task is request response
		if(returnMessageName != null){
			// choreography task is request-response
			// create assignProsumerPartOperationInput
			assignProsumerPartOperationInput = BPELUtils.createAssignOperationInput(
					prosumerPartWSDLDefinitionData, taskOperationData, operationInputVariable,
					choreographyIDVariable, receiveProsumerPartOperationData,
					receiveProsumerPartInputVariable, true);
		}
		else{
			// choreography task is not request-response
			// create assignProsumerPartOperationInput
			assignProsumerPartOperationInput = BPELUtils.createAssignOperationInput(
					prosumerPartWSDLDefinitionData, taskOperationData, operationInputVariable,
					choreographyIDVariable, receiveProsumerPartOperationData,
					receiveProsumerPartInputVariable, false);	
		}	
		// add assignProsumerPartOperationInput to onMessageSequence
		onMessageSequence.getActivities().add(assignProsumerPartOperationInput);
		
		// get or create partner link
		PartnerLinkTypeData invokeReceiveProsumerPartPartnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(
				artifactData.getArtifact(), receiveProsumerPartOperationData.getPortTypeName());
		PartnerLink invokeReceiveProsumerPartPartnerLink = BPELUtils
				.getPartnerLinkOrCreateIfNotExist(WSDLUtils
				.formatParticipantNameForWSDL(prosumerPartWSDLDefinitionData.getParticipantName()),
				taskName, process, invokeReceiveProsumerPartPartnerLinkTypeData, artifactData, false);	
		
		// create Activities for SIA Manager for invokeReceiveProsumerPart
		// get or create variable corresponding to prosumerPartnerLinkAddressVariable
		Variable prosumerPartnerLinkAddressVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantPartnerLinkAddressVariableName(cdParticipant.getName()));		
		// create assign invokeReceiveProsumerPartPartnerLink to prosumerPartnerLinkAddressVariable
		Assign assignFromProsumerPartnerLinkToVariable = BPELUtils
				.createAssignFromPartnerLinkToVariable(process, invokeReceiveProsumerPartPartnerLink, true,
						prosumerPartnerLinkAddressVariable);
		// add assignFromProsumerPartnerLinkToVariable to onMessageSequence
		onMessageSequence.getActivities().add(assignFromProsumerPartnerLinkToVariable);	
		// create assignFromSIAManagerToProsumerPartnerLink
		Assign assignFromSIAManagerToProsumerPartnerLink = BPELUtils
				.createAssignFromSIAManagerToPartnerLink(invokeReceiveProsumerPartPartnerLink,
						participantNameVariable, participantNameVariable,
						prosumerPartnerLinkAddressVariable);
		// add assignFromSIAManagerToProsumerPartnerLink to onMessageSequence
		onMessageSequence.getActivities().add(assignFromSIAManagerToProsumerPartnerLink);
		
		// create prosumer part receive Invoke
		Invoke invokeReceiveProsumerPart = BPELFactory.eINSTANCE.createInvoke();
		invokeReceiveProsumerPart.setName(BPELUtils.createInvokeName(prosumerPartWSDLDefinitionData
				.getParticipantName(), taskName));	
		// set partner link 
		invokeReceiveProsumerPart.setPartnerLink(invokeReceiveProsumerPartPartnerLink);	
		// create invokeReceiveProsumerPartOperation
		Operation invokeReceiveProsumerPartOperation = WSDLFactory.eINSTANCE.createOperation();
		invokeReceiveProsumerPartOperation.setName(taskName);	
		// set operation name
		invokeReceiveProsumerPart.setOperation(invokeReceiveProsumerPartOperation);	
		// set input variable
		invokeReceiveProsumerPart.setInputVariable(receiveProsumerPartInputVariable);
		// set output variable
		invokeReceiveProsumerPart.setOutputVariable(receiveProsumerPartOutputVariable);		
		// add invokeReceiveConsumer to onMessageSequence
		onMessageSequence.getActivities().add(invokeReceiveProsumerPart);	
		
		// log invoke creation
		LOGGER.info("Created invoke element for operation: "+invokeReceiveProsumerPartOperation.getName());		
		
		// check if choreography task is request response
		if(returnMessageName != null){
			// choreography task is request-response
			// create operationOutputVariable
			Variable operationOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
					.createOutputVariableName(cdName, taskName), cdWSDLDefinitionData.getPrefix(), 
					taskOperationData.getOutputMessageData().getName());		
			// create assign reply
			Assign assignReply = BPELUtils.createAssignCDProsumerReply(cdWSDLDefinitionData,
					taskOperationData, operationOutputVariable, choreographyIDVariable,
					receiveProsumerPartOperationData, receiveProsumerPartOutputVariable,
					isProsumerClientParticipant);
			// add assignReply to onMessageSequence
			onMessageSequence.getActivities().add(assignReply);	
			// create reply
			Reply reply = BPELFactory.eINSTANCE.createReply();
			reply.setName(BPELUtils.createReplyName(WSDLUtils.formatOperationName(task.getName())));
			reply.setOperation(operation);
			reply.setPartnerLink(partnerLink);
			reply.setPortType(portType);
			reply.setVariable(operationOutputVariable);
			// set activityCorrelations on reply
			reply.setCorrelations(activityCorrelations);
			// add reply to onMessageSequence
			onMessageSequence.getActivities().add(reply);		
			
			// log reply creation
			LOGGER.info("Created reply element for operation: "+operation.getName());			
			
			// create propertyAliasesMessageType for Input Message
			PropertyAliasesMessageType propertyAliasesMessageTypeReply = new PropertyAliasesMessageType(
					taskOperationData.getOutputMessageData().getName(), taskOperationData
					.getOutputMessageData().getPartName());
			// add propertyAliasesMessageType to propertyAliasesData
			Utils.addPropertyAliasesData(propertyAliasesData, cdWSDLDefinitionData.getParticipantName(), 
					cdWSDLDefinitionData.getWsdlFileName(), cdWSDLDefinitionData.getWsdl()
					.getTargetNamespace(), propertyAliasesMessageTypeReply);	
			
			// log creation of propertyAlias
			LOGGER.info("Created propertyAlias for message: "+taskOperationData.getOutputMessageData()
				.getName());			
		}
		
		// log execution flow
		LOGGER.exit();		
	}
	
		
	public static void createCDProsumerReceivingChoreographyTaskActivities(String cdName,
			Participant cdParticipant, List<Choreography> choreographies, ChoreographyTask task,
			WSDLDefinitionData cdWSDLDefinitionData, WSDLDefinitionData prosumerPartWSDLDefinitionData,
			Map<String, WSDLDefinitionData> wsdlDefinitions, Process process, ArtifactData artifactData,
			Deque<BPELExtensibleElement> bpelElements, Deque<org.eclipse.bpel.model.Sequence> sequences,
			PropertyAliasesData propertyAliasesData, boolean isProsumerClientParticipant,
			Deque<LoopedFlowNodeData> loopedFlowNodes){
	
		// log execution flow
		LOGGER.entry("CD name: "+cdName+" Task name: "+task.getName()+" CD WSDL Definition Data: "
		+ cdWSDLDefinitionData +" Prosumer Part WSDL Definition Data: "+prosumerPartWSDLDefinitionData);
		
		LOGGER.info("Creating CD "+cdName+" Prosumer receiving activities for choreography task "
				+task.getName());
		
		String taskName = WSDLUtils.formatOperationName(task.getName());
		// create initiatingMessageName
		String initiatingMessageName = BPMNUtils.getMessageNameChoreographyTaskSentToParticipant(task,
				cdParticipant);
		// create returnMessageName
		String returnMessageName = BPMNUtils.getMessageNameChoreographyTaskSentFromParticipant(task,
				cdParticipant);
		// get onMessageOperationData
		OperationData taskOperationData = WSDLUtils.getOperationDataWSDLOperation(cdWSDLDefinitionData
				.getWsdl(), taskName);	
		// get partner link for on message
		PartnerLinkTypeData partnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(artifactData
				.getArtifact(), taskOperationData.getPortTypeName());
		// get or create partnerLink
		PartnerLink partnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(BPELUtils
				.createPartnerLinkNameFromPartnerLinkTypeName(partnerLinkTypeData.getName()), taskName,
				process, partnerLinkTypeData, artifactData,true);
		// create operation
		Operation operation = WSDLFactory.eINSTANCE.createOperation();
		operation.setName(taskName);			
		// create portType
		PortType portType = WSDLFactory.eINSTANCE.createPortType();
		portType.setQName(new QName(cdWSDLDefinitionData.getPrefix(), taskOperationData.getPortTypeName()));
		// get or create variable
		Variable operationInputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
				.createInputVariableName(cdName, taskName), cdWSDLDefinitionData.getPrefix(), 
				taskOperationData.getInputMessageData().getName());
		// create sequenceActivities
		Sequence sequenceActivities = null;
		
		// create correlation set for choreographyID
		CorrelationSet correlationSetChoreographyID = BPELFactory.eINSTANCE.createCorrelationSet();
		correlationSetChoreographyID.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
		// create correlation for choreographyID
		Correlation correlationChoreographyID = BPELFactory.eINSTANCE.createCorrelation();
		correlationChoreographyID.setSet(correlationSetChoreographyID);
		correlationChoreographyID.setInitiate(CORRELATION_INITIATE_JOIN_LABEL);
		// create activityCorrelations
		Correlations activityCorrelations = BPELFactory.eINSTANCE.createCorrelations();
		activityCorrelations.getChildren().add(correlationChoreographyID);
		
		// check if no onMessage have been defined inside process
		if(((Pick)(((org.eclipse.bpel.model.Sequence) process.getActivity()).getActivities().get(0)))
				.getMessages().size() == 0){
			// no onMessage have been defined inside process	
			// create onMessage
			OnMessage onMessage = BPELFactory.eINSTANCE.createOnMessage();	
			// create onMessageSequence
			Sequence onMessageSequence = BPELFactory.eINSTANCE.createSequence();
			onMessageSequence.setName(BPELUtils.createSequenceName(taskName));
			// set onMessage operation
			onMessage.setOperation(operation);
			// set onMessage partnerLink
			onMessage.setPartnerLink(partnerLink);			
			// set onMessage portType
			onMessage.setPortType(portType);			
			// set onMessage variable
			onMessage.setVariable(operationInputVariable);
						
			// create assign choreographyID Variable
			Assign assignChoreographyIDVariable = BPELUtils
					.createAssignChoreographyIDVariableFromVariable(process,
							taskOperationData.getInputMessageData(), operationInputVariable);
			
			// add assignChoreographyIDVariable to onMessageSequence
			onMessageSequence.getActivities().add(assignChoreographyIDVariable);
			
			// get or create participant name variable
			Variable participantNameVariable = BPELUtils.getOrCreateStringVariable(process, 
					PARTICIPANT_NAME_VARIABLE_NAME);
			// create assign participantName Variable
			Assign assignParticipantNameVariable = BPELUtils.createAssignStringVariable(process, 
					participantNameVariable, cdParticipant.getName());
			// add assignParticipantNameVariable to onMessageSequence
			onMessageSequence.getActivities().add(assignParticipantNameVariable);
			
			// set onMessageSequence as the activity of onMessage
			onMessage.setActivity(onMessageSequence);
			// set activityCorrelations on onMessage 
			onMessage.setCorrelations(activityCorrelations);
					
			// add onMessageSequence to sequences as the last sequence
			sequences.addLast(onMessageSequence);
			// add onMessage to the main pick of the process
			((Pick) bpelElements.getLast()).getMessages().add(onMessage);
						
			// set sequenceActivities to onMessageSequence
			sequenceActivities = onMessageSequence;
			
			// log onMessage creation
			LOGGER.info("Created onMessage element for operation: "+operation.getName());
		}
		else{			
			// check if the choreography task is looped
			if(task.getLoopType().compareTo(ChoreographyLoopType.NONE) != 0){
				// the choreography task is looped
				
				// log choreography task information
				LOGGER.info("The choreography task: "+task.getName()+" is looped!");
														
				// create a sequence 
				org.eclipse.bpel.model.Sequence sequenceLoop = BPELFactory.eINSTANCE.createSequence();		
				// check if the loop has a numeric expression
				if(BPMNUtils.getLoopNumericExpression(task) != null){
					// the loop has a numeric expression	
					// create the variable for the for each counter
					Variable varForEach = BPELFactory.eINSTANCE.createVariable();
					varForEach.setName(StringUtils.deleteWhitespace(task.getName()));

					// create ForEach
					ForEach forEach = BPELFactory.eINSTANCE.createForEach();
					forEach.setParallel(false);
					forEach.setCounterName(varForEach);
					forEach.setName(BPELUtils.createForEachName(task.getName()));
					// create for each counter start expression
					Expression startExpression = BPELFactory.eINSTANCE.createExpression();
					startExpression.setBody(START_COUNTER_VALUE);
					startExpression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
					// set for each counter start value
					forEach.setStartCounterValue(startExpression);
					// set for each counter final value
					forEach.setFinalCounterValue(BPELUtils.createCDProsumerXPathExpression(BPMNUtils
							.getLoopNumericExpression(task), process, cdParticipant.getName(), wsdlDefinitions,
							cdWSDLDefinitionData, prosumerPartWSDLDefinitionData, choreographies));
					// create scope
					Scope scope = BPELFactory.eINSTANCE.createScope();
					// set scope as the activity of the ForEach 
					forEach.setActivity(scope);		
					// set the sequenceLoop as the activity of the scope 
					scope.setActivity(sequenceLoop);	
					// attach the ForEach just created to the last sequence of sequences
					sequences.getLast().getActivities().add(forEach);					
					// add the ForEach as the last element of bpelElements 
					bpelElements.addLast(forEach);	
					
					// log for each creation
					LOGGER.info("Created forEach for the choreography task: "+task.getName());	
				}
				
				// check if the loop has a conditional expression
				if(BPMNUtils.getLoopConditionalExpression(task) != null){
					// the loop has a conditional expression
					// create while 
					While whileLoop = BPELFactory.eINSTANCE.createWhile();
					whileLoop.setName(BPELUtils.createWhileName(task.getName()));
					// create condition 
					Condition condition = BPELFactory.eINSTANCE.createCondition();
					condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
					// set condition body
					condition.setBody(BPELUtils.createCDProsumerXPathExpression(BPMNUtils
						.getLoopConditionalExpression(task), process, cdParticipant.getName(),
						wsdlDefinitions, cdWSDLDefinitionData, prosumerPartWSDLDefinitionData,
						choreographies).getBody());
					// set condition of whileLoop
					whileLoop.setCondition(condition);
					// set the sequenceLoop as the activity of the whileLoop
					whileLoop.setActivity(sequenceLoop);
					// attach the while just created to the last sequence of sequences
					sequences.getLast().getActivities().add(whileLoop);					
					// add the while as the last element of bpelElements 
					bpelElements.addLast(whileLoop);		
					
					// log while creation
					LOGGER.info("Created While for the choreography task: "+task.getName());	
				}
				// add to sequences the sequence sequenceLoop as last element
				sequences.addLast(sequenceLoop);			
				// add LoopedFlowNodeData related to the just created loop to loopedFlowNodes
				loopedFlowNodes.addLast(new LoopedFlowNodeData(task.getName()));					
			}			
			// the choreography task is not looped
			
			// log choreography task information
			LOGGER.info("The choreography task: "+task.getName()+" is not looped!");
			
			// onMessage have been defined inside process
			// create receive
			Receive receive = BPELFactory.eINSTANCE.createReceive();
			receive.setName(BPELUtils.createReceiveName(cdParticipant.getName(), taskName));
			receive.setCreateInstance(false);	
			// set receive partnerLink
			receive.setPartnerLink(partnerLink);
			// set receive operation
			receive.setOperation(operation);
			// set receive portType
			receive.setPortType(portType);
			// set receive variable
			receive.setVariable(operationInputVariable);
			// set activityCorrelations on receive
			receive.setCorrelations(activityCorrelations);
			
			// add to receiveCD to last sequence into sequences
			sequences.getLast().getActivities().add(receive);				
			
			// set sequenceActivities to sequence
			sequenceActivities = sequences.getLast();
			
			// log receive creation
			LOGGER.info("Created receive element for operation: "+operation.getName());
		}
		
		// check if the last LoopedFlowNodeData of loopedFlowNodes
		// has no variable name specified
		if(!loopedFlowNodes.isEmpty() && loopedFlowNodes.getLast().getVariableName() == null) {
			// the last LoopedFlowNodeData of loopedFlowNodes has no variable name specified	
			// create assign loopIndexes
			Assign assignLoopIndexes = BPELUtils.createAssignLoopIndexes(process, loopedFlowNodes.getLast()
					.getName(), taskOperationData.getInputMessageData(), operationInputVariable);
			// add assignLoopIndexes to the activities of sequenceActivities
			sequenceActivities.getActivities().add(assignLoopIndexes);
			// set the variable name of the last LoopedFlowNodeData of loopedFlowNodes
			loopedFlowNodes.getLast().setVariableName(BPELUtils
					.createLoopVariableName(StringUtils.deleteWhitespace(loopedFlowNodes
							.getLast().getName())));
		}
		
		// create propertyAliasesMessageType for Input Message
		PropertyAliasesMessageType propertyAliasesMessageType = new PropertyAliasesMessageType(
				taskOperationData.getInputMessageData().getName(), taskOperationData
				.getInputMessageData().getPartName());
		// add propertyAliasesMessageType to propertyAliasesData
		Utils.addPropertyAliasesData(propertyAliasesData, cdWSDLDefinitionData.getParticipantName(), 
				cdWSDLDefinitionData.getWsdlFileName(), cdWSDLDefinitionData.getWsdl().getTargetNamespace(), 
				propertyAliasesMessageType);
		
		// log creation of propertyAlias
		LOGGER.info("Created propertyAlias for message: "+taskOperationData.getInputMessageData().getName());
		
		// create receiveProsumerPartOperationData
		OperationData receiveProsumerPartOperationData = WSDLUtils.getOperationDataWSDLOperation(
				prosumerPartWSDLDefinitionData.getWsdl(), taskName);
		// create receiveProsumerPartInputVariable
		Variable receiveProsumerPartInputVariable = BPELUtils.getOrCreateVariableForMessageType(process,
				BPELUtils.createInputVariableName(cdParticipant.getName(), taskName),
				prosumerPartWSDLDefinitionData.getPrefix(), receiveProsumerPartOperationData
				.getInputMessageData().getName());
		// create receiveProsumerPartOutputVariable
		Variable receiveProsumerPartOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process,
				BPELUtils.createOutputVariableName(cdParticipant.getName(), taskName), 
				prosumerPartWSDLDefinitionData.getPrefix(),
				receiveProsumerPartOperationData.getOutputMessageData().getName());
		// get or create choreographyID variable
		Variable choreographyIDVariable = BPELUtils.getOrCreateStringVariable(process,
				CHOREOGRAPHY_ID_VARIABLE_NAME);		
		Assign assignProsumerPartOperationInput;
		// check if choreography task is request response
		if(returnMessageName != null){
			// choreography task is request-response
			// create assignProsumerPartOperationInput
			assignProsumerPartOperationInput = BPELUtils.createAssignOperationInput(
					prosumerPartWSDLDefinitionData, taskOperationData, operationInputVariable,
					choreographyIDVariable, receiveProsumerPartOperationData,
					receiveProsumerPartInputVariable, true);
		}
		else{
			// choreography task is not request-response
			// create assignProsumerPartOperationInput
			assignProsumerPartOperationInput = BPELUtils.createAssignOperationInput(
					prosumerPartWSDLDefinitionData, taskOperationData, operationInputVariable,
					choreographyIDVariable, receiveProsumerPartOperationData,
					receiveProsumerPartInputVariable, false);	
		}	
		// add assignProsumerPartOperationInput to sequenceActivities
		sequenceActivities.getActivities().add(assignProsumerPartOperationInput);
		
		// get or create partner link
		PartnerLinkTypeData invokeReceiveProsumerPartPartnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(
				artifactData.getArtifact(), receiveProsumerPartOperationData.getPortTypeName());
		PartnerLink invokeReceiveProsumerPartPartnerLink = BPELUtils
				.getPartnerLinkOrCreateIfNotExist(WSDLUtils
				.formatParticipantNameForWSDL(prosumerPartWSDLDefinitionData.getParticipantName()),
				taskName, process, invokeReceiveProsumerPartPartnerLinkTypeData, artifactData, false);	
		
		// create Activities for SIA Manager for invokeReceiveProsumerPart
		// get or create variable corresponding to prosumerPartnerLinkAddressVariable
		Variable prosumerPartnerLinkAddressVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantPartnerLinkAddressVariableName(cdParticipant.getName()));		
		// create assign invokeReceiveProsumerPartPartnerLink to prosumerPartnerLinkAddressVariable
		Assign assignFromProsumerPartnerLinkToVariable = BPELUtils
				.createAssignFromPartnerLinkToVariable(process, invokeReceiveProsumerPartPartnerLink, true,
						prosumerPartnerLinkAddressVariable);
		// add assignFromProsumerPartnerLinkToVariable to sequenceActivities
		sequenceActivities.getActivities().add(assignFromProsumerPartnerLinkToVariable);	
		// get or create participant name variable
		Variable participantNameVariable = BPELUtils.getOrCreateStringVariable(process, 
				PARTICIPANT_NAME_VARIABLE_NAME);
		// create assignFromSIAManagerToProsumerPartnerLink
		Assign assignFromSIAManagerToProsumerPartnerLink = BPELUtils
				.createAssignFromSIAManagerToPartnerLink(invokeReceiveProsumerPartPartnerLink,
						participantNameVariable, participantNameVariable,
						prosumerPartnerLinkAddressVariable);
		// add assignFromSIAManagerToProsumerPartnerLink to sequenceActivities
		sequenceActivities.getActivities().add(assignFromSIAManagerToProsumerPartnerLink);	
		
		// create prosumer part receive Invoke
		Invoke invokeReceiveProsumerPart = BPELFactory.eINSTANCE.createInvoke();
		invokeReceiveProsumerPart.setName(BPELUtils.createInvokeName(prosumerPartWSDLDefinitionData
				.getParticipantName(), taskName));	
		// set partner link 
		invokeReceiveProsumerPart.setPartnerLink(invokeReceiveProsumerPartPartnerLink);	
		// create invokeReceiveProsumerPartOperation
		Operation invokeReceiveProsumerPartOperation = WSDLFactory.eINSTANCE.createOperation();
		invokeReceiveProsumerPartOperation.setName(taskName);	
		// set operation name
		invokeReceiveProsumerPart.setOperation(invokeReceiveProsumerPartOperation);	
		// set input variable
		invokeReceiveProsumerPart.setInputVariable(receiveProsumerPartInputVariable);
		// set output variable
		invokeReceiveProsumerPart.setOutputVariable(receiveProsumerPartOutputVariable);		
		// add invokeReceiveConsumer to sequenceActivities
		sequenceActivities.getActivities().add(invokeReceiveProsumerPart);	
		
		// log invoke creation
		LOGGER.info("Created invoke element for operation: "+invokeReceiveProsumerPartOperation.getName());
		
		// check if choreography task is request response
		if(returnMessageName != null){
			// choreography task is request-response
			// create operationOutputVariable
			Variable operationOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process,BPELUtils
					.createOutputVariableName(cdName, taskName), cdWSDLDefinitionData.getPrefix(), 
					taskOperationData.getOutputMessageData().getName());			
			// create assign reply
			Assign assignReply = BPELUtils.createAssignCDProsumerReply(cdWSDLDefinitionData,
					taskOperationData, operationOutputVariable, choreographyIDVariable,
					receiveProsumerPartOperationData, receiveProsumerPartOutputVariable,
					isProsumerClientParticipant);
			// add assignReply to sequenceActivities
			sequenceActivities.getActivities().add(assignReply);	
			// create reply
			Reply reply = BPELFactory.eINSTANCE.createReply();
			reply.setName(BPELUtils.createReplyName(WSDLUtils.formatOperationName(task.getName())));
			reply.setOperation(operation);
			reply.setPartnerLink(partnerLink);
			reply.setPortType(portType);
			reply.setVariable(operationOutputVariable);
			// set activityCorrelations on reply
			reply.setCorrelations(activityCorrelations);
			// add reply to sequenceActivities
			sequenceActivities.getActivities().add(reply);		
			
			// log reply creation
			LOGGER.info("Created reply element for operation: "+operation.getName());
			
			// create propertyAliasesMessageType for Input Message
			PropertyAliasesMessageType propertyAliasesMessageTypeReply = new PropertyAliasesMessageType(
					taskOperationData.getOutputMessageData().getName(), taskOperationData
					.getOutputMessageData().getPartName());
			// add propertyAliasesMessageType to propertyAliasesData
			Utils.addPropertyAliasesData(propertyAliasesData, cdWSDLDefinitionData.getParticipantName(), 
					cdWSDLDefinitionData.getWsdlFileName(), cdWSDLDefinitionData.getWsdl()
					.getTargetNamespace(), propertyAliasesMessageTypeReply);
			
				// log creation of propertyAlias
			LOGGER.info("Created propertyAlias for message: "+taskOperationData.getOutputMessageData()
				.getName());
		
		}
		
		// check if the implemented is not an onMessage (sequences.size() > 1)
		// TODO: when a 'looped' onMessage is implemented this check has to be removed
		// and if the choreography task is looped
		if(sequences.size() > 1 && task.getLoopType().compareTo(ChoreographyLoopType.NONE) != 0){
			// the choreography task is looped 
			// remove the last element inside sequences, bpelElements	 and loopedFlowNodes
			sequences.pollLast();
			bpelElements.pollLast();
			loopedFlowNodes.pollLast();
		}
		
		// log execution flow
		LOGGER.exit();		
	}
	
	public static void createCDProsumerInitiatingChoreographyTaskActivities(Participant cdParticipant,
			List<Choreography> choreographies, ChoreographyTask task, boolean isReceiverParticipantProsumer,
			boolean isReceiverParticipantClient, WSDLDefinitionData cdWSDLDefinitionData,
			WSDLDefinitionData prosumerPartWSDLDefinitionData,
			Map<String, WSDLDefinitionData> wsdlDefinitions, Process process, ArtifactData artifactData,
			Deque<BPELExtensibleElement> bpelElements, Deque<org.eclipse.bpel.model.Sequence> sequences,
			WSDLDefinitionData receiverWSDLDefinitionData, PropertyAliasesData propertyAliasesData,
			Deque<LoopedFlowNodeData> loopedFlowNodes){
	
		// log execution flow
		LOGGER.entry("Task name: "+task.getName()," CD WSDL Definition Data: "+cdWSDLDefinitionData 
				+" Prosumer Part WSDL Definition Data: "+prosumerPartWSDLDefinitionData);
		
		LOGGER.info("Creating CD Prosumer of participant "+ cdParticipant.getName() 
			+" initiating activities for choreography task "+task.getName());
		
		String taskName = WSDLUtils.formatOperationName(task.getName());
		String choreographyInitiatingMessageName = BPMNUtils
				.getMessageNameChoreographyTaskSentFromParticipant(task, cdParticipant);
		String choreographyReturnMessageName = BPMNUtils
				.getMessageNameChoreographyTaskSentToParticipant(task, cdParticipant);
		// create sendProsumerPartOperationName
		String sendProsumerPartOperationName = WSDLUtils
				.createSendOperationName(choreographyInitiatingMessageName);		
		// get sendProsumerPartOperationData
		OperationData sendProsumerPartOperationData = WSDLUtils.getOperationDataWSDLOperation(
				prosumerPartWSDLDefinitionData.getWsdl(), sendProsumerPartOperationName);	
		// create variables for send prosumer part operation
		Variable sendProsumerPartInputVariable = BPELUtils.getOrCreateVariableForMessageType(process,
				BPELUtils.createInputVariableName(cdParticipant.getName(), sendProsumerPartOperationName), 
				prosumerPartWSDLDefinitionData.getPrefix(), sendProsumerPartOperationData
				.getInputMessageData().getName());		
		Variable sendProsumerPartOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process,
				BPELUtils.createOutputVariableName(cdParticipant.getName(), sendProsumerPartOperationName), 
				prosumerPartWSDLDefinitionData.getPrefix(), sendProsumerPartOperationData
				.getOutputMessageData().getName());
		// get choreographyID variable
		Variable choreographyIDVariable = BPELUtils
				.getVariableFromName(process, CHOREOGRAPHY_ID_VARIABLE_NAME);
		// get participant name variable
		Variable participantNameVariable = BPELUtils
				.getVariableFromName(process, PARTICIPANT_NAME_VARIABLE_NAME);
		// get receiver participant name
		String receiverParticipantName = BPMNUtils.getReceivingParticipant(task).getName();
		
		// check if the choreography task is looped
		if(task.getLoopType().compareTo(ChoreographyLoopType.NONE) != 0){
			// the choreography task is looped
			
			// log choreography task information
			LOGGER.info("The choreography task: "+task.getName()+" is looped!");
													
			// create a sequence 
			org.eclipse.bpel.model.Sequence sequenceLoop = BPELFactory.eINSTANCE.createSequence();		
			// check if the loop has a numeric expression
			if(BPMNUtils.getLoopNumericExpression(task) != null){
				// the loop has a numeric expression	
				// create the variable for the for each counter
				Variable varForEach = BPELFactory.eINSTANCE.createVariable();
				varForEach.setName(StringUtils.deleteWhitespace(task.getName()));

				// create ForEach
				ForEach forEach = BPELFactory.eINSTANCE.createForEach();
				forEach.setParallel(false);
				forEach.setCounterName(varForEach);
				forEach.setName(BPELUtils.createForEachName(task.getName()));
				// create for each counter start expression
				Expression startExpression = BPELFactory.eINSTANCE.createExpression();
				startExpression.setBody(START_COUNTER_VALUE);
				startExpression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
				// set for each counter start value
				forEach.setStartCounterValue(startExpression);
				// set for each counter final value
				forEach.setFinalCounterValue(BPELUtils.createCDProsumerXPathExpression(BPMNUtils
						.getLoopNumericExpression(task), process, cdParticipant.getName(), wsdlDefinitions,
						cdWSDLDefinitionData, prosumerPartWSDLDefinitionData, choreographies));
				// create scope
				Scope scope = BPELFactory.eINSTANCE.createScope();
				// set scope as the activity of the ForEach 
				forEach.setActivity(scope);		
				// set the sequenceLoop as the activity of the scope 
				scope.setActivity(sequenceLoop);	
				// attach the ForEach just created to the last sequence of sequences
				sequences.getLast().getActivities().add(forEach);					
				// add the ForEach as the last element of bpelElements 
				bpelElements.addLast(forEach);	
				
				// create assign for loop variable 
				Assign assignLoopVariable = BPELUtils.createAssignLoopVariable(process,
						StringUtils.deleteWhitespace(task.getName()));
				// add assignLoopVariable to sequenceForEach
				sequenceLoop.getActivities().add(assignLoopVariable);
				
				// log for each creation
				LOGGER.info("Created forEach for the choreography task: "+task.getName());	
			}
			
			// check if the loop has a conditional expression
			if(BPMNUtils.getLoopConditionalExpression(task) != null){
				// the loop has a conditional expression
				// create while 
				While whileLoop = BPELFactory.eINSTANCE.createWhile();
				whileLoop.setName(BPELUtils.createWhileName(task.getName()));
				// create condition 
				Condition condition = BPELFactory.eINSTANCE.createCondition();
				condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
				// set condition body
				condition.setBody(BPELUtils.createCDProsumerXPathExpression(BPMNUtils
					.getLoopConditionalExpression(task), process, cdParticipant.getName(), wsdlDefinitions,
						cdWSDLDefinitionData, prosumerPartWSDLDefinitionData, choreographies).getBody());
				// set condition of whileLoop
				whileLoop.setCondition(condition);
				// set the sequenceLoop as the activity of the whileLoop
				whileLoop.setActivity(sequenceLoop);
				// attach the while just created to the last sequence of sequences
				sequences.getLast().getActivities().add(whileLoop);					
				// add the while as the last element of bpelElements 
				bpelElements.addLast(whileLoop);	
				
				// create assign for loop variable 
				Assign assignLoopVariable = BPELUtils.createAssignLoopVariable(process,
						StringUtils.deleteWhitespace(task.getName()));
				// add assignLoopVariable to sequenceForEach
				sequenceLoop.getActivities().add(assignLoopVariable);
				
				// log while creation
				LOGGER.info("Created While for the choreography task: "+task.getName());	
			}
			// add to sequences the sequence sequenceLoop as last element
			sequences.addLast(sequenceLoop);			
			// add LoopedFlowNodeData related to the just created loop to loopedFlowNodes
			loopedFlowNodes.addLast(new LoopedFlowNodeData(task.getName(),
					BPELUtils.createLoopVariableName(task.getName())));					
		}
		
		// create assignSendInput
		Assign assignSendInput = BPELUtils.createAssignSendInputProsumerPart(process,
				prosumerPartWSDLDefinitionData, sendProsumerPartOperationData,
				sendProsumerPartInputVariable, choreographyIDVariable, participantNameVariable,
				receiverParticipantName, task.getName(), choreographyInitiatingMessageName,
				loopedFlowNodes);
		// add assignSendInput to the last sequence of sequences
		sequences.getLast().getActivities().add(assignSendInput);
				
		// get partner link
		PartnerLinkTypeData sendPartnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(artifactData
				.getArtifact(), sendProsumerPartOperationData.getPortTypeName());
		PartnerLink invokeSendPartnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(WSDLUtils
				.formatParticipantNameForWSDL(prosumerPartWSDLDefinitionData.getParticipantName()), 
				sendProsumerPartOperationName, process, sendPartnerLinkTypeData, artifactData, false);
		
		// create Activities for SIA Manager for invokeSend
		// get or create variable corresponding to prosumerPartnerLinkAddressVariable
		Variable prosumerPartnerLinkAddressVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantPartnerLinkAddressVariableName(cdParticipant.getName()));		
		// create assign invokeSendPartnerLink to prosumerPartnerLinkAddressVariable
		Assign assignFromProsumerPartnerLinkToVariable = BPELUtils
				.createAssignFromPartnerLinkToVariable(process, invokeSendPartnerLink, true,
						prosumerPartnerLinkAddressVariable);
		// add assignFromProsumerPartnerLinkToVariable to the last sequence of sequences
		sequences.getLast().getActivities().add(assignFromProsumerPartnerLinkToVariable);				
		// create assignFromSIAManagerToProsumerPartnerLink
		Assign assignFromSIAManagerToProsumerPartnerLink = BPELUtils
				.createAssignFromSIAManagerToPartnerLink(invokeSendPartnerLink, participantNameVariable,
						participantNameVariable, prosumerPartnerLinkAddressVariable);
		// add assignFromSIAManagerToProsumerPartnerLink to the last sequence of sequences
		sequences.getLast().getActivities().add(assignFromSIAManagerToProsumerPartnerLink);	
				
		// create send Invoke
		Invoke invokeSend = BPELFactory.eINSTANCE.createInvoke();
		invokeSend.setName(BPELUtils.createInvokeName(prosumerPartWSDLDefinitionData.getParticipantName(), 
				sendProsumerPartOperationName));
		// set partner link 
		invokeSend.setPartnerLink(invokeSendPartnerLink);
		// create operation
		Operation sendOperation = WSDLFactory.eINSTANCE.createOperation();
		sendOperation.setName(sendProsumerPartOperationName);	
		// set operation name
		invokeSend.setOperation(sendOperation);	
		// set input variable
		invokeSend.setInputVariable(sendProsumerPartInputVariable);
		// set output variable
		invokeSend.setOutputVariable(sendProsumerPartOutputVariable);			
		// add invokeSend to the last sequence of sequences
		sequences.getLast().getActivities().add(invokeSend);
		
		// log invoke creation
		LOGGER.info("Created invoke element for operation: "+sendOperation.getName());
			
		// get operationData
		OperationData operationData = WSDLUtils.getOperationDataWSDLOperation(receiverWSDLDefinitionData
				.getWsdl(), taskName);	
		// create operationInputVariable
		Variable operationInputVariable = BPELUtils.getOrCreateVariableForMessageType(process ,BPELUtils
				.createInputVariableName(receiverWSDLDefinitionData.getParticipantName(), taskName), 
				receiverWSDLDefinitionData.getPrefix(), operationData.getInputMessageData().getName());
		// if an output message exist create operationOutputVariable
		Variable operationOutputVariable = null;
		if(operationData.getOutputMessageData() != null){				
			operationOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
					.createOutputVariableName(receiverWSDLDefinitionData.getParticipantName(), taskName), 
					receiverWSDLDefinitionData.getPrefix(), operationData.getOutputMessageData().getName());
		}			
		
		// check if the receiver participant of the choreography task is a prosumer 
		// or the client participant
		if(isReceiverParticipantProsumer || isReceiverParticipantClient){
			// the receiver participant of the choreography task is a prosumer or the client participant
			// create assignProsumerOperationInput
			Assign assignProsumerOperationInput = BPELUtils.createAssignProsumerOperationInput(process, 
					receiverWSDLDefinitionData, sendProsumerPartOperationData,
					sendProsumerPartOutputVariable, operationData, operationInputVariable,
					choreographyIDVariable, participantNameVariable, receiverParticipantName,
					task.getName(), choreographyInitiatingMessageName, isReceiverParticipantClient,
					loopedFlowNodes);
			// add assignProsumerOperationInput to the last sequence of sequences
			sequences.getLast().getActivities().add(assignProsumerOperationInput);			
		}
		else{
			// the receiver participant of the choreography task is a provider
			Assign assignProviderOperationInput = BPELUtils.createAssignProviderOperationInput(
					sendProsumerPartOutputVariable, sendProsumerPartOperationData, operationData, 
					operationInputVariable, receiverWSDLDefinitionData);
			// add assignProviderOperationInput to the last sequence of sequences
			sequences.getLast().getActivities().add(assignProviderOperationInput);			
		}

		// get partner link
		PartnerLinkTypeData invokePartnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(artifactData
				.getArtifact(), operationData.getPortTypeName());
		PartnerLink invokePartnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(WSDLUtils
				.formatParticipantNameForWSDL(receiverWSDLDefinitionData.getParticipantName()), taskName,
				process, invokePartnerLinkTypeData, artifactData, false);

		// create Activities for SIA Manager for invoke		
		// get or create variable corresponding to receiverParticipantName
		Variable invokeParticipantNameVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantVariableName(receiverParticipantName));
		// create assignInvokeParticipantNameVariable
		Assign assignInvokeParticipantNameVariable = BPELUtils.createAssignStringVariable(process, 
				invokeParticipantNameVariable, receiverParticipantName);
		// add assignInvokeParticipantNameVariable to the last sequence of sequences
		sequences.getLast().getActivities().add(assignInvokeParticipantNameVariable);			
		// get or create variable corresponding to invokePartnerLinkVariable
		Variable invokePartnerLinkAddressVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantPartnerLinkAddressVariableName(receiverParticipantName));	
		// create assignPartnerLinkAddressVariable
		Assign assignPartnerLinkAddressVariable = BPELUtils.createAssignFromPartnerLinkToVariable(process, 
				invokePartnerLink, true, invokePartnerLinkAddressVariable);
		// add assignPartnerLinkAddressVariable to the last sequence of sequences
		sequences.getLast().getActivities().add(assignPartnerLinkAddressVariable);		
		// create assignFromSIAManagerToInvokePartnerLink
		Assign assignFromSIAManagerToInvokePartnerLink = BPELUtils
				.createAssignFromSIAManagerToPartnerLink(invokePartnerLink, participantNameVariable,
						invokeParticipantNameVariable, invokePartnerLinkAddressVariable);
		// add assignFromSIAManagerToInvokePartnerLink to the last sequence of sequences
		sequences.getLast().getActivities().add(assignFromSIAManagerToInvokePartnerLink);			
				
		// create invoke 
		Invoke invoke = BPELFactory.eINSTANCE.createInvoke();
		invoke.setName(BPELUtils.createInvokeName(receiverWSDLDefinitionData.getParticipantName(),taskName));
		// set partner link 
		invoke.setPartnerLink(invokePartnerLink);	
		// create operation
		Operation invokeProviderOperation = WSDLFactory.eINSTANCE.createOperation();
		invokeProviderOperation.setName(operationData.getName());	
		// set operation name
		invoke.setOperation(invokeProviderOperation);	
		// set input variable
		invoke.setInputVariable(operationInputVariable);
		// check if operation as an output
		if(operationOutputVariable != null){
			// set output variable
			invoke.setOutputVariable(operationOutputVariable);				
		}
		
		// check if the receiver participant of the choreography task is a prosumer
		if(isReceiverParticipantProsumer){
			// the receiver participant of the choreography task is a prosumer
			// create correlation set for choreographyID
			CorrelationSet correlationSetChoreographyID = BPELFactory.eINSTANCE.createCorrelationSet();
			correlationSetChoreographyID.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
			// create correlation for choreographyID
			Correlation correlationChoreographyID = BPELFactory.eINSTANCE.createCorrelation();
			correlationChoreographyID.setSet(correlationSetChoreographyID);
			correlationChoreographyID.setInitiate(CORRELATION_INITIATE_JOIN_LABEL);
			// check if operation as an output 
			if(operationOutputVariable != null){
				// create correlation pattern request-response
				correlationChoreographyID.setPattern(CorrelationPattern.REQUESTRESPONSE_LITERAL);
			}
			// create invokeCorrelations
			Correlations invokeCorrelations = BPELFactory.eINSTANCE.createCorrelations();
			invokeCorrelations.getChildren().add(correlationChoreographyID);
			// set invokeCorrelations to invoke
			invoke.setCorrelations(invokeCorrelations);
			
			// create propertyAliasesMessageType for Input Message
			PropertyAliasesMessageType propertyAliasesMessageType = new PropertyAliasesMessageType(
					operationData.getInputMessageData().getName(), operationData.getInputMessageData()
					.getPartName());
			// add propertyAliasesMessageType to propertyAliasesData
			Utils.addPropertyAliasesData(propertyAliasesData, receiverWSDLDefinitionData
					.getParticipantName(), receiverWSDLDefinitionData.getWsdlFileName(),
					receiverWSDLDefinitionData.getWsdl().getTargetNamespace(), propertyAliasesMessageType);		
			
			// log creation of propertyAlias
			LOGGER.info("Created propertyAlias for message: "+operationData.getInputMessageData().getName());
		}	
			
		// add invokeProvider to the last sequence of sequences
		sequences.getLast().getActivities().add(invoke);
		
		// log invoke creation
		LOGGER.info("Created invoke element for operation: "+invokeProviderOperation.getName());
		
		// check if operation as an output 
		if(operationOutputVariable != null){
			String receiveOperationName = WSDLUtils.createReceiveOperationName(BPMNUtils
					.getMessageNameChoreographyTaskSentToParticipant(task, cdParticipant));	
			// create receiveProsumerPartOperationData
			OperationData receiveProsumerPartOperationData = WSDLUtils
					.getOperationDataWSDLOperation(prosumerPartWSDLDefinitionData.getWsdl(),
							receiveOperationName);
			// create input and output variable for receive prosumer part 
			Variable receiveInputProsumerPartVariable = BPELUtils.getOrCreateVariableForMessageType(process, 
					BPELUtils.createInputVariableName(cdParticipant.getName(), receiveOperationName), 
					prosumerPartWSDLDefinitionData.getPrefix(), receiveProsumerPartOperationData
					.getInputMessageData().getName());			
			Variable receiveOutputProsumerPartVariable = BPELUtils.getOrCreateVariableForMessageType(process, 
					BPELUtils.createOutputVariableName(cdParticipant.getName(), receiveOperationName), 
					prosumerPartWSDLDefinitionData.getPrefix(), receiveProsumerPartOperationData
					.getOutputMessageData().getName());
			
			// create receive activities
			// check if the receiver participant of the choreography task is a prosumer
			if(isReceiverParticipantProsumer){
				// create assignReceiveProsumerOperation
				Assign assignReceiveProsumerOperation = BPELUtils
						.createAssignReceiveInputProsumerPartFromProsumer(prosumerPartWSDLDefinitionData,
								receiveProsumerPartOperationData, operationOutputVariable,
								choreographyIDVariable, receiveProsumerPartOperationData,
								receiveInputProsumerPartVariable, choreographyReturnMessageName);
				// add assignReceiveProsumerOperation to the last sequence of sequences
				sequences.getLast().getActivities().add(assignReceiveProsumerOperation);
				
				// create propertyAliasesMessageType for Output Message
				PropertyAliasesMessageType propertyAliasesMessageType = new PropertyAliasesMessageType(
						operationData.getOutputMessageData().getName(), operationData
						.getOutputMessageData().getPartName());
				// add propertyAliasesMessageType to propertyAliasesData
				Utils.addPropertyAliasesData(propertyAliasesData, cdWSDLDefinitionData.getParticipantName(), 
						cdWSDLDefinitionData.getWsdlFileName(), cdWSDLDefinitionData.getWsdl()
						.getTargetNamespace(), propertyAliasesMessageType);			

				// log creation of propertyAlias
				LOGGER.info("Created propertyAlias for message: "+operationData.getOutputMessageData()
					.getName());				
				
			}
			else{
				// create assignReceiveProviderOperation
				Assign assignReceiveProviderOperation = BPELUtils
						.createAssignReceiveInputProsumerPartFromProvider(
						prosumerPartWSDLDefinitionData, operationData, operationOutputVariable, 
						receiveProsumerPartOperationData, receiveInputProsumerPartVariable,
						choreographyIDVariable,participantNameVariable, receiverParticipantName,
						task.getName(), choreographyReturnMessageName);
				// add assignReceiveProviderOperation to the last sequence of sequences
				sequences.getLast().getActivities().add(assignReceiveProviderOperation);
			}
			
			// create Activities for SIA Manager for invokeReceive
			// get or create variable corresponding to prosumerPartnerLinkAddressVariable
			Variable prosumerPartnerLinkAddressVariableReceive = BPELUtils.getOrCreateStringVariable(process, 
					BPELUtils.createParticipantPartnerLinkAddressVariableName(cdParticipant.getName()));		
			// create assign invokeSendPartnerLink to prosumerPartnerLinkAddressVariable
			Assign assignFromProsumerPartnerLinkToVariableReceive = BPELUtils
					.createAssignFromPartnerLinkToVariable(process, invokeSendPartnerLink, true,
							prosumerPartnerLinkAddressVariableReceive);
			// add assignFromProsumerPartnerLinkToVariable to the last sequence of sequences
			sequences.getLast().getActivities().add(assignFromProsumerPartnerLinkToVariableReceive);				
			// create assignFromSIAManagerToProsumerPartnerLink
			Assign assignFromSIAManagerToProsumerPartnerLinkReceive = BPELUtils
					.createAssignFromSIAManagerToPartnerLink(invokeSendPartnerLink, participantNameVariable,
							participantNameVariable, prosumerPartnerLinkAddressVariableReceive);
			// add assignFromSIAManagerToProsumerPartnerLink to the last sequence of sequences
			sequences.getLast().getActivities().add(assignFromSIAManagerToProsumerPartnerLinkReceive);	
			
			// create prosumer receive Invoke
			Invoke invokeReceiveProsumerPart = BPELFactory.eINSTANCE.createInvoke();
			invokeReceiveProsumerPart.setName(BPELUtils.createInvokeName(prosumerPartWSDLDefinitionData
					.getParticipantName(), receiveOperationName));	
			// get partner link
			PartnerLinkTypeData invokeReceiveProsumerPartPartnerLinkTypeData = BPELUtils
					.getPartnerLinkTypeData(artifactData.getArtifact(),
							receiveProsumerPartOperationData.getPortTypeName());
			PartnerLink invokeReceiveProsumerPartPartnerLink = BPELUtils
					.getPartnerLinkOrCreateIfNotExist(WSDLUtils
							.formatParticipantNameForWSDL(prosumerPartWSDLDefinitionData
									.getParticipantName()), receiveOperationName, process,
							invokeReceiveProsumerPartPartnerLinkTypeData, artifactData, false);
			// set partner link 
			invokeReceiveProsumerPart.setPartnerLink(invokeReceiveProsumerPartPartnerLink);		
			Operation invokeReceiveConsumerOperation = WSDLFactory.eINSTANCE.createOperation();
			invokeReceiveConsumerOperation.setName(receiveOperationName);	
			// set operation name
			invokeReceiveProsumerPart.setOperation(invokeReceiveConsumerOperation);	
			// set input variable
			invokeReceiveProsumerPart.setInputVariable(receiveInputProsumerPartVariable);
			// set output variable
			invokeReceiveProsumerPart.setOutputVariable(receiveOutputProsumerPartVariable);					
			// add invokeReceiveProsumerPart to the last sequence of sequences
			sequences.getLast().getActivities().add(invokeReceiveProsumerPart);	
			
			// log invoke creation
			LOGGER.info("Created invoke element for operation: "+invokeReceiveConsumerOperation.getName());			
		}
		
		// check if the choreography task is looped
		if(task.getLoopType().compareTo(ChoreographyLoopType.NONE) != 0){
			// the choreography task is looped 
			// remove the last element inside sequences, bpelElements	 and loopedFlowNodes
			sequences.pollLast();
			bpelElements.pollLast();
			loopedFlowNodes.pollLast();
		}
		
		// log execution flow
		LOGGER.exit();		
		
	}
					
	public static Assign createAssignReceiveInputProsumerPartFromProvider(
			WSDLDefinitionData prosumerPartWSDLDefinitionData, OperationData providerOperationData, 
			Variable receiveCDInputVariable, OperationData receiveProsumerPartOperationData, 
			Variable receiveProsumerPartInputVariable, Variable choreographyIDVariable, 
			Variable participantNameVariable, String senderParticipantName, String choreographyTaskName, 
			String outputMessageName){
		
		// log execution flow
		LOGGER.entry("Receive Provider Operation name: "+providerOperationData.getName()
			+"Receive CD Variable name: "+receiveCDInputVariable.getName());
		
		// create assign
		Assign assignReceiveInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignReceiveInput.setName(BPELUtils.createAssignName(receiveProsumerPartInputVariable.getName()));
		assignReceiveInput.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(
				prosumerPartWSDLDefinitionData.getWsdl(), receiveProsumerPartOperationData
				.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(receiveProsumerPartInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// set partToInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignReceiveInput
		assignReceiveInput.getCopy().add(copyInitialize);
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();	
		fromChoreographyId.setVariable(choreographyIDVariable);
		// add fromChoreographyId to copyChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(receiveProsumerPartInputVariable);	
		// create part for copyChoreographyId
		Part partChoreographyId = WSDLFactory.eINSTANCE.createPart();
		partChoreographyId.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// set partChoreographyId
		toChoreographyId.setPart(partChoreographyId);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReceiveInput
		assignReceiveInput.getCopy().add(copyChoreographyId);	
		
		// create copy for senderParticipantName
		Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create from for senderParticipantName
		From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
		fromSenderParticipantName.setLiteral(senderParticipantName);
		// set from of copySenderParticipantName to fromSenderParticipantName
		copySenderParticipantName.setFrom(fromSenderParticipantName);		
		// create to for senderParticipantName
		To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
		toSenderParticipantName.setVariable(receiveProsumerPartInputVariable);
		// create part for senderParticipantName
		Part partSenderParticipantName = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantName.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// set partSenderParticipantName to toSenderParticipantName
		toSenderParticipantName.setPart(partSenderParticipantName);
		// create query for senderParticipantName
		Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
		// set toSenderParticipantName query to queryToSenderParticipantName
		toSenderParticipantName.setQuery(queryToSenderParticipantName);		
		// set to of copySenderParticipantName to toSenderParticipantName
		copySenderParticipantName.setTo(toSenderParticipantName);		
		// add copysenderParticipantName to assignReceiveInput
		assignReceiveInput.getCopy().add(copySenderParticipantName);		
		
		// create copy for receiverParticipantName
		Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();	
		// create from for receiverParticipantName
		From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
		fromReceiverParticipantName.setVariable(participantNameVariable);
		// set from of copyReceiverParticipantName to fromReceiverParticipantName
		copyReceiverParticipantName.setFrom(fromReceiverParticipantName);		
		// create to for receiverParticipantName
		To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
		toReceiverParticipantName.setVariable(receiveProsumerPartInputVariable);
		// create part for receiverParticipantName
		Part partReceiverParticipantName = WSDLFactory.eINSTANCE.createPart();		
		partReceiverParticipantName.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// set partReceiverParticipantName to toReceiverParticipantName
		toReceiverParticipantName.setPart(partReceiverParticipantName);
		// create query for receiverParticipantName
		Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
		// set toReceiverParticipantName query to queryReceiverParticipantName
		toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
		// set to of copyReceiverParticipantName to toReceiverParticipantName
		copyReceiverParticipantName.setTo(toReceiverParticipantName);		
		// add copyReceiverParticipantName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyReceiverParticipantName);			
		
		// create copy for choreographyTaskName
		Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();			
		// create from for choreographyTaskName
		From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
		fromChoreographyTaskName.setLiteral(choreographyTaskName);
		// set from of copyChoreographyTaskName to fromChoreographyTaskName
		copyChoreographyTaskName.setFrom(fromChoreographyTaskName);		
		// create to for choreographyTaskName
		To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
		toChoreographyTaskName.setVariable(receiveProsumerPartInputVariable);
		// create part for choreographyTaskName
		Part partChoreographyTaskName = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyTaskName.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// set partChoreographyTaskName to toChoreographyTaskName
		toChoreographyTaskName.setPart(partChoreographyTaskName);
		// create query for choreographyTaskName
		Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
		// set toChoreographyTaskName query to queryChoreographyTaskName
		toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
		// set to of copyChoreographyTaskName to toChoreographyTaskName
		copyChoreographyTaskName.setTo(toChoreographyTaskName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyChoreographyTaskName);		
		
		// create copy for outputMessageName
		Copy copyOutputMessageName = BPELFactory.eINSTANCE.createCopy();			
		// create from for outputMessageName
		From fromOutputMessageName = BPELFactory.eINSTANCE.createFrom();		
		fromOutputMessageName.setLiteral(outputMessageName);
		// set from of copyOutputMessageName to fromOutputMessageName
		copyOutputMessageName.setFrom(fromOutputMessageName);		
		// create to for outputMessageName
		To toOutputMessageName = BPELFactory.eINSTANCE.createTo();	
		toOutputMessageName.setVariable(receiveProsumerPartInputVariable);
		// create part for outputMessageName
		Part partOutputMessageName = WSDLFactory.eINSTANCE.createPart();		
		partOutputMessageName.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// set partOutputMessageName to toOutputMessageName
		toOutputMessageName.setPart(partOutputMessageName);
		// create query for outputMessageName
		Query queryOutputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);			
		// set toOutputMessageName query to queryOutputMessageName
		toOutputMessageName.setQuery(queryOutputMessageName);		
		// set to of copyInputMessageName to toOutputMessageName
		copyOutputMessageName.setTo(toOutputMessageName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyOutputMessageName);			
			
		// create copy for outputMessageData
		Copy copyOutputMessageData = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyOutputMessageData
		Part partOutputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
		partOutputMessageDataInput.setName(providerOperationData.getOutputMessageData().getPartName());
		// create from for outputMessageData
		From fromOutputMessageData = BPELFactory.eINSTANCE.createFrom();	
		fromOutputMessageData.setPart(partOutputMessageDataInput);
		fromOutputMessageData.setVariable(receiveCDInputVariable);		
		// set from of copyOutputMessageData to fromOutputMessageData
		copyOutputMessageData.setFrom(fromOutputMessageData);	
		// create to for copyOutputMessageData
		To toOutputMessageData = BPELFactory.eINSTANCE.createTo();	
		// create part for output of copyOutputMessageData
		Part partOutputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
		partOutputMessageDataOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// set partOutputMessageDataOutput to toOutputMessageData
		toOutputMessageData.setPart(partOutputMessageDataOutput);
		// set variable to toDataItem
		toOutputMessageData.setVariable(receiveProsumerPartInputVariable);
		// create query for toDataItem
		Query querytoDataItem = BPELFactory.eINSTANCE.createQuery();
		querytoDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		querytoDataItem.setValue(XPATH_QUERY_OUTPUT_MESSAGE_DATA);		
		// set querytoDataItem to toDataItem
		toOutputMessageData.setQuery(querytoDataItem);
		// set to of copyDataItem to toDataItem		
		copyOutputMessageData.setTo(toOutputMessageData);
		// add copyDataItem to assignReceiveInput
		assignReceiveInput.getCopy().add(copyOutputMessageData);		
		
		// log Assing information
		LOGGER.info("Assign: "+assignReceiveInput.getName()+" created!");
		// log exectution flow
		LOGGER.exit();
		
		return assignReceiveInput;
	}
	
	public static Assign createAssignReceiveInputProsumerPartFromProsumer(
			WSDLDefinitionData prosumerPartWSDLDefinitionData, OperationData receiveCDOperationData, 
			Variable receiveCDInputVariable, Variable choreographyIDVariable, 
			OperationData receiveProsumerPartOperationData, Variable receiveProsumerPartInputVariable,
			String outputMessageName){
		
		// log execution flow
		LOGGER.entry("Receive CD Operation name: "+receiveCDOperationData.getName()
			+"Receive CD Variable name: "+receiveCDInputVariable.getName());
		
		// create assign
		Assign assignReceiveInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignReceiveInput.setName(BPELUtils.createAssignName(receiveProsumerPartInputVariable.getName()));
		assignReceiveInput.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(
				prosumerPartWSDLDefinitionData.getWsdl(), receiveProsumerPartOperationData
				.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(receiveProsumerPartInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignReceiveInput
		assignReceiveInput.getCopy().add(copyInitialize);
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();
		// create part for choreographyId
		Part partChoreographyIdInput = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyIdInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();
		fromChoreographyId.setVariable(choreographyIDVariable);
				
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create part for copyChoreographyId
		Part partChoreographyId = WSDLFactory.eINSTANCE.createPart();
		partChoreographyId.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(receiveProsumerPartInputVariable);	
		// set partChoreographyId
		toChoreographyId.setPart(partChoreographyId);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReceiveInput
		assignReceiveInput.getCopy().add(copyChoreographyId);	
		
		// create copy for senderParticipantName
		Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create part for senderParticipantName
		Part partSenderParticipantNameInput = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for senderParticipantName
		From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
		fromSenderParticipantName.setVariable(receiveCDInputVariable);
		fromSenderParticipantName.setPart(partSenderParticipantNameInput);
		// create query for senderParticipantName
		Query queryFromSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryFromSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);
		// set fromSenderParticipantName query to queryFromSenderParticipantName
		fromSenderParticipantName.setQuery(queryFromSenderParticipantName);		
		// set from of copySenderParticipantName to fromSenderParticipantName
		copySenderParticipantName.setFrom(fromSenderParticipantName);
		// create part for senderParticipantName
		Part partSenderParticipantNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());	
		// create to for senderParticipantName
		To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
		toSenderParticipantName.setVariable(receiveProsumerPartInputVariable);
		// set partSenderParticipantNameOutput to toSenderParticipantName
		toSenderParticipantName.setPart(partSenderParticipantNameOutput);
		// create query for senderParticipantName
		Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
		// set toSenderParticipantName query to queryToSenderParticipantName
		toSenderParticipantName.setQuery(queryToSenderParticipantName);		
		// set to of copySenderParticipantName to toSenderParticipantName
		copySenderParticipantName.setTo(toSenderParticipantName);		
		// add copysenderParticipantName to assignReceiveInput
		assignReceiveInput.getCopy().add(copySenderParticipantName);		
		
		// create copy for receiverParticipantName
		Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create part for input of receiverParticipantName
		Part partReceiverParticipantNameInput = WSDLFactory.eINSTANCE.createPart();
		partReceiverParticipantNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for receiverParticipantName
		From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
		fromReceiverParticipantName.setVariable(receiveCDInputVariable);
		fromReceiverParticipantName.setPart(partReceiverParticipantNameInput);
		// create query for receiverParticipantName
		Query queryFromReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryFromReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);
		// set fromReceiverParticipantName query to queryFromReceiverParticipantName
		fromReceiverParticipantName.setQuery(queryFromReceiverParticipantName);			
		// set from of copyReceiverParticipantName to fromReceiverParticipantName
		copyReceiverParticipantName.setFrom(fromReceiverParticipantName);
		// create part for receiverParticipantName
		Part partReceiverParticipantNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partReceiverParticipantNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// create to for receiverParticipantName
		To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
		toReceiverParticipantName.setVariable(receiveProsumerPartInputVariable);
		// set partReceiverParticipantNameOutput to toReceiverParticipantName
		toReceiverParticipantName.setPart(partReceiverParticipantNameOutput);
		// create query for receiverParticipantName
		Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
		// set toReceiverParticipantName query to queryReceiverParticipantName
		toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
		// set to of copyReceiverParticipantName to toReceiverParticipantName
		copyReceiverParticipantName.setTo(toReceiverParticipantName);		
		// add copyReceiverParticipantName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyReceiverParticipantName);			
		
		// create copy for choreographyTaskName
		Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyChoreographyTaskName
		Part partChoreographyTaskNameInput = WSDLFactory.eINSTANCE.createPart();
		partChoreographyTaskNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for choreographyTaskName
		From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
		fromChoreographyTaskName.setVariable(receiveCDInputVariable);
		fromChoreographyTaskName.setPart(partChoreographyTaskNameInput);
		// create query for choreographyTaskName
		Query queryFromChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryFromChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);
		// set fromChoreographyTaskName query to queryFromChoreographyTaskName
		fromChoreographyTaskName.setQuery(queryFromChoreographyTaskName);		
		// set from of copyChoreographyTaskName to fromChoreographyTaskName
		copyChoreographyTaskName.setFrom(fromChoreographyTaskName);	
		// create part for choreographyTaskName
		Part partChoreographyTaskNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyTaskNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// create to for choreographyTaskName
		To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
		toChoreographyTaskName.setVariable(receiveProsumerPartInputVariable);
		// set partChoreographyTaskNameOutput to toChoreographyTaskName
		toChoreographyTaskName.setPart(partChoreographyTaskNameOutput);
		// create query for choreographyTaskName
		Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
		// set toChoreographyTaskName query to queryChoreographyTaskName
		toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
		// set to of copyChoreographyTaskName to toChoreographyTaskName
		copyChoreographyTaskName.setTo(toChoreographyTaskName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyChoreographyTaskName);		
		
		// create copy for outputMessageName
		Copy copyOutputMessageName = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyOutputMessageData
		Part partOutputMessageNameInput = WSDLFactory.eINSTANCE.createPart();
		partOutputMessageNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for outputMessageName
		From fromOutputMessageName = BPELFactory.eINSTANCE.createFrom();
		fromOutputMessageName.setPart(partOutputMessageNameInput);
		fromOutputMessageName.setVariable(receiveCDInputVariable);
		// create query for outputMessageName
		Query queryFromOutputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryFromOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);
		// set fromOutputMessageName query to queryFromOutputMessageName
		fromOutputMessageName.setQuery(queryFromOutputMessageName);				
		// set from of copyOutputMessageName to fromOutputMessageName
		copyOutputMessageName.setFrom(fromOutputMessageName);
		// create part for outputMessageName
		Part partOutputMessageNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partOutputMessageNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());		
		// create to for outputMessageName
		To toOutputMessageName = BPELFactory.eINSTANCE.createTo();	
		toOutputMessageName.setVariable(receiveProsumerPartInputVariable);
		// set partOutputMessageNameOutput to toOutputMessageName
		toOutputMessageName.setPart(partOutputMessageNameOutput);
		// create query for outputMessageName
		Query queryOutputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);			
		// set toOutputMessageName query to queryOutputMessageName
		toOutputMessageName.setQuery(queryOutputMessageName);		
		// set to of copyInputMessageName to toOutputMessageName
		copyOutputMessageName.setTo(toOutputMessageName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyOutputMessageName);			
			
		// create copy for outputMessageData
		Copy copyOutputMessageData = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyOutputMessageData
		Part partOutputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
		partOutputMessageDataInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for outputMessageData
		From fromOutputMessageData = BPELFactory.eINSTANCE.createFrom();	
		fromOutputMessageData.setPart(partOutputMessageDataInput);
		fromOutputMessageData.setVariable(receiveCDInputVariable);	
		// create query for outputMessageName
		Query queryFromOutputMessageData = BPELFactory.eINSTANCE.createQuery();
		queryFromOutputMessageData.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromOutputMessageData.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);
		// set from of copyOutputMessageData to fromOutputMessageData
		copyOutputMessageData.setFrom(fromOutputMessageData);
		// create part for output of copyOutputMessageData
		Part partOutputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
		partOutputMessageDataOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());		
		// create to for copyOutputMessageData
		To toOutputMessageData = BPELFactory.eINSTANCE.createTo();	
		// set partOutputMessageDataOutput to toOutputMessageData
		toOutputMessageData.setPart(partOutputMessageDataOutput);
		// set variable to toDataItem
		toOutputMessageData.setVariable(receiveProsumerPartInputVariable);
		// create query for toDataItem
		Query querytoDataItem = BPELFactory.eINSTANCE.createQuery();
		querytoDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		querytoDataItem.setValue(XPATH_QUERY_OUTPUT_MESSAGE_DATA);		
		// set querytoDataItem to toDataItem
		toOutputMessageData.setQuery(querytoDataItem);
		// set to of copyDataItem to toDataItem		
		copyOutputMessageData.setTo(toOutputMessageData);
		// add copyDataItem to assignReceiveInput
		assignReceiveInput.getCopy().add(copyOutputMessageData);		
		
		// log Assing information
		LOGGER.info("Assign: "+assignReceiveInput.getName()+" created!");
		// log exectution flow
		LOGGER.exit();
		
		return assignReceiveInput;
	}
	
	public static Assign createAssignOperationInput(WSDLDefinitionData prosumerPartWSDLDefinitionData, 
			OperationData receiveCDOperationData, Variable receiveCDInputVariable,
			Variable choreographyIDVariable, OperationData receiveProsumerPartOperationData,
			Variable receiveProsumerPartInputVariable, boolean isTaskRequestResponse){
		
		// log execution flow
		LOGGER.entry("CD Operation name: "+receiveCDOperationData.getName()+" CD Variable name: "
		+receiveCDInputVariable.getName());
		
		// create assign
		Assign assignReceiveInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignReceiveInput.setName(BPELUtils.createAssignName(receiveProsumerPartInputVariable.getName()));
		assignReceiveInput.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(
				prosumerPartWSDLDefinitionData.getWsdl(), receiveProsumerPartOperationData
				.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(receiveProsumerPartInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignReceiveInput
		assignReceiveInput.getCopy().add(copyInitialize);
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();
		// create part for choreographyId
		Part partChoreographyIdInput = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyIdInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();	
		fromChoreographyId.setVariable(choreographyIDVariable);
				
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create part for copyChoreographyId
		Part partChoreographyId = WSDLFactory.eINSTANCE.createPart();
		partChoreographyId.setName(receiveProsumerPartOperationData.getInputMessageData().getPartName());
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(receiveProsumerPartInputVariable);	
		// set partChoreographyId to toChoreographyId
		toChoreographyId.setPart(partChoreographyId);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReceiveInput
		assignReceiveInput.getCopy().add(copyChoreographyId);	
		
		// create copy for senderParticipantName
		Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create part for senderParticipantName
		Part partSenderParticipantNameInput = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for senderParticipantName
		From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
		fromSenderParticipantName.setVariable(receiveCDInputVariable);
		fromSenderParticipantName.setPart(partSenderParticipantNameInput);
		// create query for senderParticipantName
		Query queryFromSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryFromSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);
		// set fromSenderParticipantName query to queryFromSenderParticipantName
		fromSenderParticipantName.setQuery(queryFromSenderParticipantName);		
		// set from of copySenderParticipantName to fromSenderParticipantName
		copySenderParticipantName.setFrom(fromSenderParticipantName);
		// create part for senderParticipantName
		Part partSenderParticipantNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// create to for senderParticipantName
		To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
		toSenderParticipantName.setVariable(receiveProsumerPartInputVariable);
		// set partSenderParticipantNameOutput to toSenderParticipantName
		toSenderParticipantName.setPart(partSenderParticipantNameOutput);
		// create query for senderParticipantName
		Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
		// set toSenderParticipantName query to queryToSenderParticipantName
		toSenderParticipantName.setQuery(queryToSenderParticipantName);		
		// set to of copySenderParticipantName to toSenderParticipantName
		copySenderParticipantName.setTo(toSenderParticipantName);		
		// add copysenderParticipantName to assignReceiveInput
		assignReceiveInput.getCopy().add(copySenderParticipantName);		
		
		// create copy for receiverParticipantName
		Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create part for input of receiverParticipantName
		Part partReceiverParticipantNameInput = WSDLFactory.eINSTANCE.createPart();
		partReceiverParticipantNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for receiverParticipantName
		From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
		fromReceiverParticipantName.setVariable(receiveCDInputVariable);
		fromReceiverParticipantName.setPart(partReceiverParticipantNameInput);
		// create query for receiverParticipantName
		Query queryFromReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryFromReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);
		// set fromReceiverParticipantName query to queryFromReceiverParticipantName
		fromReceiverParticipantName.setQuery(queryFromReceiverParticipantName);			
		// set from of copyReceiverParticipantName to fromReceiverParticipantName
		copyReceiverParticipantName.setFrom(fromReceiverParticipantName);
		// create part for receiverParticipantName
		Part partReceiverParticipantNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partReceiverParticipantNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// create to for receiverParticipantName
		To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
		toReceiverParticipantName.setVariable(receiveProsumerPartInputVariable);
		// set partReceiverParticipantNameOutput to toReceiverParticipantName
		toReceiverParticipantName.setPart(partReceiverParticipantNameOutput);
		// create query for receiverParticipantName
		Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
		// set toReceiverParticipantName query to queryReceiverParticipantName
		toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
		// set to of copyReceiverParticipantName to toReceiverParticipantName
		copyReceiverParticipantName.setTo(toReceiverParticipantName);		
		// add copyReceiverParticipantName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyReceiverParticipantName);			
		
		// create copy for choreographyTaskName
		Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyChoreographyTaskName
		Part partChoreographyTaskNameInput = WSDLFactory.eINSTANCE.createPart();
		partChoreographyTaskNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for choreographyTaskName
		From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
		fromChoreographyTaskName.setVariable(receiveCDInputVariable);
		fromChoreographyTaskName.setPart(partChoreographyTaskNameInput);
		// create query for choreographyTaskName
		Query queryFromChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryFromChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);
		// set fromChoreographyTaskName query to queryFromChoreographyTaskName
		fromChoreographyTaskName.setQuery(queryFromChoreographyTaskName);		
		// set from of copyChoreographyTaskName to fromChoreographyTaskName
		copyChoreographyTaskName.setFrom(fromChoreographyTaskName);	
		// create part for choreographyTaskName
		Part partChoreographyTaskNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyTaskNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// create to for choreographyTaskName
		To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
		toChoreographyTaskName.setVariable(receiveProsumerPartInputVariable);
		// set partChoreographyTaskNameOutput to toChoreographyTaskName
		toChoreographyTaskName.setPart(partChoreographyTaskNameOutput);
		// create query for choreographyTaskName
		Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
		// set toChoreographyTaskName query to queryChoreographyTaskName
		toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
		// set to of copyChoreographyTaskName to toChoreographyTaskName
		copyChoreographyTaskName.setTo(toChoreographyTaskName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyChoreographyTaskName);		
		
		// create copy for inputMessageName
		Copy copyInputMessageName = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyInputMessageName
		Part partInputMessageNameInput = WSDLFactory.eINSTANCE.createPart();
		partInputMessageNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for inputMessageName
		From fromInputMessageName = BPELFactory.eINSTANCE.createFrom();
		fromInputMessageName.setPart(partInputMessageNameInput);
		fromInputMessageName.setVariable(receiveCDInputVariable);
		// create query for inputMessageName
		Query queryFromInputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryFromInputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromInputMessageName.setValue(XPATH_QUERY_INPUT_MESSAGE_NAME);
		// set fromInputMessageName query to queryFromInputMessageName
		fromInputMessageName.setQuery(queryFromInputMessageName);				
		// set from of copyInputMessageName to fromInputMessageName
		copyInputMessageName.setFrom(fromInputMessageName);
		// create part for outputMessageName
		Part partInputMessageNameOutput = WSDLFactory.eINSTANCE.createPart();		
		partInputMessageNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());		
		// create to for inputMessageName
		To toInputMessageName = BPELFactory.eINSTANCE.createTo();	
		toInputMessageName.setVariable(receiveProsumerPartInputVariable);
		// set partInputMessageNameOutput to toInputMessageName 
		toInputMessageName.setPart(partInputMessageNameOutput);
		// create query for inputMessageName
		Query queryInputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryInputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryInputMessageName.setValue(XPATH_QUERY_INPUT_MESSAGE_NAME);			
		// set toInputMessageName query to queryInputMessageName
		toInputMessageName.setQuery(queryInputMessageName);		
		// set to of copyInputMessageName to toOutputMessageName
		copyInputMessageName.setTo(toInputMessageName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignReceiveInput.getCopy().add(copyInputMessageName);			
			
		// create copy for inputMessageData
		Copy copyInputMessageData = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyInputMessageData
		Part partInputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
		partInputMessageDataInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
		// create from for fromInputMessageData
		From fromInputMessageData = BPELFactory.eINSTANCE.createFrom();	
		fromInputMessageData.setPart(partInputMessageDataInput);
		fromInputMessageData.setVariable(receiveCDInputVariable);	
		// create query for fromDataItem
		Query queryFromDataItem = BPELFactory.eINSTANCE.createQuery();
		queryFromDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromDataItem.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);		
		fromInputMessageData.setQuery(queryFromDataItem);		
		// set from of copyInputMessageData to fromInputMessageData
		copyInputMessageData.setFrom(fromInputMessageData);
		// create part for output of copyInputMessageData
		Part partInputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
		partInputMessageDataOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
				.getPartName());		
		// create to for copyInputMessageData
		To toOutputMessageData = BPELFactory.eINSTANCE.createTo();	
		// set partOutputMessageDataOutput to toOutputMessageData
		toOutputMessageData.setPart(partInputMessageDataOutput);
		// set variable to toDataItem
		toOutputMessageData.setVariable(receiveProsumerPartInputVariable);
		// create query for toDataItem
		Query queryToDataItem = BPELFactory.eINSTANCE.createQuery();
		queryToDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToDataItem.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);		
		// set querytoDataItem to toDataItem
		toOutputMessageData.setQuery(queryToDataItem);
		// set to of copyDataItem to toDataItem		
		copyInputMessageData.setTo(toOutputMessageData);
		// add copyDataItem to assignReceiveInput
		assignReceiveInput.getCopy().add(copyInputMessageData);		
		
		// check if the task is request-response
		if(isTaskRequestResponse){
			// task request-response
			// create copy for outputMessageName
			Copy copyOutputMessageName = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyOutputMessageData
			Part partOutputMessageNameInput = WSDLFactory.eINSTANCE.createPart();
			partOutputMessageNameInput.setName(receiveCDOperationData.getInputMessageData().getPartName());
			// create from for outputMessageName
			From fromOutputMessageName = BPELFactory.eINSTANCE.createFrom();
			fromOutputMessageName.setPart(partOutputMessageNameInput);
			fromOutputMessageName.setVariable(receiveCDInputVariable);
			// create query for outputMessageName
			Query queryFromOutputMessageName = BPELFactory.eINSTANCE.createQuery();
			queryFromOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryFromOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);
			// set fromOutputMessageName query to queryFromOutputMessageName
			fromOutputMessageName.setQuery(queryFromOutputMessageName);				
			// set from of copyOutputMessageName to fromOutputMessageName
			copyOutputMessageName.setFrom(fromOutputMessageName);
			// create part for outputMessageName
			Part partOutputMessageNameOutput = WSDLFactory.eINSTANCE.createPart();		
			partOutputMessageNameOutput.setName(receiveProsumerPartOperationData.getInputMessageData()
					.getPartName());		
			// create to for outputMessageName
			To toOutputMessageName = BPELFactory.eINSTANCE.createTo();	
			toOutputMessageName.setVariable(receiveProsumerPartInputVariable);
			// set partOutputMessageNameOutput to toOutputMessageName
			toOutputMessageName.setPart(partOutputMessageNameOutput);
			// create query for outputMessageName
			Query queryOutputMessageName = BPELFactory.eINSTANCE.createQuery();
			queryOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);			
			// set toOutputMessageName query to queryOutputMessageName
			toOutputMessageName.setQuery(queryOutputMessageName);		
			// set to of copyInputMessageName to toOutputMessageName
			copyOutputMessageName.setTo(toOutputMessageName);		
			// add copyChoreographyTaskName to assignReceiveInput
			assignReceiveInput.getCopy().add(copyOutputMessageName);					
		}
		
		// log Assing information
		LOGGER.info("Assign: "+assignReceiveInput.getName()+" created!");
		// log exectution flow
		LOGGER.exit();
		
		return assignReceiveInput;
	}
	
	public static Assign createAssignCDProsumerReply(WSDLDefinitionData cdWSDLDefinitionData, 
			OperationData replyCDOperationData, Variable replyCDVariable, Variable choreographyIDVariable, 
			OperationData messageDataOperationData, Variable messageDataVariable,
			boolean isProsumerClientParticipant){
		

		// log execution flow
		LOGGER.entry("Reply CD Operation name: "+replyCDOperationData.getName(),"Reply CD Variable name: "
		+replyCDVariable.getName());		
		
		// create assign
		Assign assignReply = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignReply.setName(BPELUtils.createAssignName(replyCDVariable.getName()));
		assignReply.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(cdWSDLDefinitionData
				.getWsdl(), replyCDOperationData.getOutputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(replyCDVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(replyCDOperationData.getOutputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignSendInput
		assignReply.getCopy().add(copyInitialize);	
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();	
		fromChoreographyId.setVariable(choreographyIDVariable);
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(replyCDVariable);	
		// create part for copyChoreographyId
		Part partChoreographyIdTo = WSDLFactory.eINSTANCE.createPart();
		partChoreographyIdTo.setName(replyCDOperationData.getOutputMessageData().getPartName());
		// set partChoreographyId to toChoreographyId
		toChoreographyId.setPart(partChoreographyIdTo);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReply
		assignReply.getCopy().add(copyChoreographyId);	
		
		// check if prosumer is the client
		if(!isProsumerClientParticipant){
			// prosumer is not the client participant
			// create copy for senderParticipantName
			Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
			// create part for senderParticipantName
			Part partSenderParticipantNameInput = WSDLFactory.eINSTANCE.createPart();		
			partSenderParticipantNameInput.setName(messageDataOperationData.getInputMessageData()
					.getPartName());
			// create from for senderParticipantName
			From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
			fromSenderParticipantName.setVariable(messageDataVariable);
			fromSenderParticipantName.setPart(partSenderParticipantNameInput);
			// create query for senderParticipantName
			Query queryFromSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
			queryFromSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryFromSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);
			// set fromSenderParticipantName query to queryFromSenderParticipantName
			fromSenderParticipantName.setQuery(queryFromSenderParticipantName);		
			// set from of copySenderParticipantName to fromSenderParticipantName
			copySenderParticipantName.setFrom(fromSenderParticipantName);
			// create part for senderParticipantName
			Part partSenderParticipantNameOutput = WSDLFactory.eINSTANCE.createPart();		
			partSenderParticipantNameOutput.setName(replyCDOperationData.getOutputMessageData()
					.getPartName());
			// create to for senderParticipantName
			To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
			toSenderParticipantName.setVariable(replyCDVariable);
			// set partSenderParticipantNameOutput to toSenderParticipantName
			toSenderParticipantName.setPart(partSenderParticipantNameOutput);
			// create query for senderParticipantName
			Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
			queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
			// set toSenderParticipantName query to queryToSenderParticipantName
			toSenderParticipantName.setQuery(queryToSenderParticipantName);		
			// set to of copySenderParticipantName to toSenderParticipantName
			copySenderParticipantName.setTo(toSenderParticipantName);		
			// add copysenderParticipantName to assignReply
			assignReply.getCopy().add(copySenderParticipantName);		
		
			// create copy for receiverParticipantName
			Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();
			// create part for input of receiverParticipantName
			Part partReceiverParticipantNameInput = WSDLFactory.eINSTANCE.createPart();
			partReceiverParticipantNameInput.setName(messageDataOperationData.getInputMessageData()
					.getPartName());
			// create from for receiverParticipantName
			From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
			fromReceiverParticipantName.setVariable(messageDataVariable);
			fromReceiverParticipantName.setPart(partReceiverParticipantNameInput);
			// create query for receiverParticipantName
			Query queryFromReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
			queryFromReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryFromReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);
			// set fromReceiverParticipantName query to queryFromReceiverParticipantName
			fromReceiverParticipantName.setQuery(queryFromReceiverParticipantName);			
			// set from of copyReceiverParticipantName to fromReceiverParticipantName
			copyReceiverParticipantName.setFrom(fromReceiverParticipantName);
			// create part for receiverParticipantName
			Part partReceiverParticipantNameOutput = WSDLFactory.eINSTANCE.createPart();		
			partReceiverParticipantNameOutput.setName(replyCDOperationData.getInputMessageData()
					.getPartName());
			// create to for receiverParticipantName
			To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
			toReceiverParticipantName.setVariable(replyCDVariable);
			// set partReceiverParticipantNameOutput to toReceiverParticipantName
			toReceiverParticipantName.setPart(partReceiverParticipantNameOutput);
			// create query for receiverParticipantName
			Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
			queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
			// set toReceiverParticipantName query to queryReceiverParticipantName
			toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
			// set to of copyReceiverParticipantName to toReceiverParticipantName
			copyReceiverParticipantName.setTo(toReceiverParticipantName);		
			// add copyReceiverParticipantName to assignReply
			assignReply.getCopy().add(copyReceiverParticipantName);			
		
			// create copy for choreographyTaskName
			Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyChoreographyTaskName
			Part partChoreographyTaskNameInput = WSDLFactory.eINSTANCE.createPart();
			partChoreographyTaskNameInput.setName(messageDataOperationData.getInputMessageData()
					.getPartName());
			// create from for choreographyTaskName
			From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
			fromChoreographyTaskName.setVariable(messageDataVariable);
			fromChoreographyTaskName.setPart(partChoreographyTaskNameInput);
			// create query for choreographyTaskName
			Query queryFromChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
			queryFromChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryFromChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);
			// set fromChoreographyTaskName query to queryFromChoreographyTaskName
			fromChoreographyTaskName.setQuery(queryFromChoreographyTaskName);		
			// set from of copyChoreographyTaskName to fromChoreographyTaskName
			copyChoreographyTaskName.setFrom(fromChoreographyTaskName);	
			// create part for choreographyTaskName
			Part partChoreographyTaskNameOutput = WSDLFactory.eINSTANCE.createPart();		
			partChoreographyTaskNameOutput.setName(replyCDOperationData.getInputMessageData().getPartName());
			// create to for choreographyTaskName
			To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
			toChoreographyTaskName.setVariable(replyCDVariable);
			// set partChoreographyTaskNameOutput to toChoreographyTaskName
			toChoreographyTaskName.setPart(partChoreographyTaskNameOutput);
			// create query for choreographyTaskName
			Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
			queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
			// set toChoreographyTaskName query to queryChoreographyTaskName
			toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
			// set to of copyChoreographyTaskName to toChoreographyTaskName
			copyChoreographyTaskName.setTo(toChoreographyTaskName);		
			// add copyChoreographyTaskName to assignReply
			assignReply.getCopy().add(copyChoreographyTaskName);			
		
			// create copy for outputMessageName
			Copy copyOutputMessageName = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyOutputMessageData
			Part partOutputMessageNameInput = WSDLFactory.eINSTANCE.createPart();
			partOutputMessageNameInput.setName(messageDataOperationData.getInputMessageData().getPartName());
			// create from for outputMessageName
			From fromOutputMessageName = BPELFactory.eINSTANCE.createFrom();
			fromOutputMessageName.setPart(partOutputMessageNameInput);
			fromOutputMessageName.setVariable(messageDataVariable);
			// create query for outputMessageName
			Query queryFromOutputMessageName = BPELFactory.eINSTANCE.createQuery();
			queryFromOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryFromOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);
			// set fromOutputMessageName query to queryFromOutputMessageName
			fromOutputMessageName.setQuery(queryFromOutputMessageName);				
			// set from of copyOutputMessageName to fromOutputMessageName
			copyOutputMessageName.setFrom(fromOutputMessageName);
			// create part for outputMessageName
			Part partOutputMessageNameOutput = WSDLFactory.eINSTANCE.createPart();		
			partOutputMessageNameOutput.setName(replyCDOperationData.getInputMessageData().getPartName());		
			// create to for outputMessageName
			To toOutputMessageName = BPELFactory.eINSTANCE.createTo();	
			toOutputMessageName.setVariable(replyCDVariable);
			// set partOutputMessageNameOutput to toOutputMessageName
			toOutputMessageName.setPart(partOutputMessageNameOutput);
			// create query for outputMessageName
			Query queryOutputMessageName = BPELFactory.eINSTANCE.createQuery();
			queryOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryOutputMessageName.setValue(XPATH_QUERY_OUTPUT_MESSAGE_NAME);			
			// set toOutputMessageName query to queryOutputMessageName
			toOutputMessageName.setQuery(queryOutputMessageName);		
			// set to of copyInputMessageName to toOutputMessageName
			copyOutputMessageName.setTo(toOutputMessageName);		
			// add copyChoreographyTaskName to assignReply
			assignReply.getCopy().add(copyOutputMessageName);			
			
			// create copy for outputMessageData
			Copy copyOutputMessageData = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyOutputMessageData
			Part partOutputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
			partOutputMessageDataInput.setName(messageDataOperationData.getInputMessageData().getPartName());
			// create from for outputMessageData
			From fromOutputMessageData = BPELFactory.eINSTANCE.createFrom();	
			fromOutputMessageData.setPart(partOutputMessageDataInput);
			fromOutputMessageData.setVariable(messageDataVariable);		
			// set from of copyOutputMessageData to fromOutputMessageData
			copyOutputMessageData.setFrom(fromOutputMessageData);
			// create part for output of copyOutputMessageData
			Part partOutputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
			partOutputMessageDataOutput.setName(replyCDOperationData.getInputMessageData().getPartName());		
			// create to for copyOutputMessageData
			To toOutputMessageData = BPELFactory.eINSTANCE.createTo();	
			// set partOutputMessageDataOutput to toOutputMessageData
			toOutputMessageData.setPart(partOutputMessageDataOutput);
			// set variable to toDataItem
			toOutputMessageData.setVariable(replyCDVariable);
			// create query for toDataItem
			Query querytoDataItem = BPELFactory.eINSTANCE.createQuery();
			querytoDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItem.setValue(XPATH_QUERY_OUTPUT_MESSAGE_DATA);		
			// set querytoDataItem to toDataItem
			toOutputMessageData.setQuery(querytoDataItem);
			// set to of copyDataItem to toDataItem		
			copyOutputMessageData.setTo(toOutputMessageData);
			// add copyDataItem to assignReply
			assignReply.getCopy().add(copyOutputMessageData);
		}
		else{
			// prosumer is the client participant
			
			// create copy for MessageData
			Copy copyMessageData = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyMessageData
			Part partMessageDataInput = WSDLFactory.eINSTANCE.createPart();
			partMessageDataInput.setName(messageDataOperationData.getInputMessageData().getPartName());
			// create from for copyMessageData
			From fromMessageData = BPELFactory.eINSTANCE.createFrom();	
			fromMessageData.setPart(partMessageDataInput);
			fromMessageData.setVariable(messageDataVariable);
			// create query for toDataItem
			Query querytoDataItemInputFrom = BPELFactory.eINSTANCE.createQuery();
			querytoDataItemInputFrom.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItemInputFrom.setValue(XPATH_QUERY_OUTPUT_MESSAGE_DATA);		
			// set querytoDataItemInputFrom to fromInputMessageData
			fromMessageData.setQuery(querytoDataItemInputFrom);
			// set from of copyOutputMessageData to fromOutputMessageData
			copyMessageData.setFrom(fromMessageData);	
			// create to for copyOutputMessageData
			To toMessageData = BPELFactory.eINSTANCE.createTo();	
			// create part for output of copyOutputMessageData
			Part partMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
			partMessageDataOutput.setName(replyCDOperationData.getInputMessageData().getPartName());
			// set partOutputMessageDataOutput to toOutputMessageData
			toMessageData.setPart(partMessageDataOutput);
			// set variable to toDataItem
			toMessageData.setVariable(replyCDVariable);
			// create query for toDataItem
			Query querytoDataItemInputTo = BPELFactory.eINSTANCE.createQuery();
			querytoDataItemInputTo.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItemInputTo.setValue(XPATH_QUERY_OUTPUT_MESSAGE_DATA);		
			// set querytoDataItem to toDataItem
			toMessageData.setQuery(querytoDataItemInputTo);
			// set to of copyDataItem to toDataItem		
			copyMessageData.setTo(toMessageData);
			// add copyDataItem to assignReply
			assignReply.getCopy().add(copyMessageData);
			
		}		
		
		// log Assing information
		LOGGER.info("Assign: "+assignReply.getName()+" created!");
		// log exectution flow
		LOGGER.exit();
	
		return assignReply;
	}
	
	public static Assign createAssignSendInputProsumerPart(Process process, 
			WSDLDefinitionData prosumerPartWSDLDefinitionData, OperationData sendProsumerPartOperationData,
			Variable sendInputVariable, Variable choreographyIDVariable, Variable participantNameVariable,
			String receiverParticipantName, String choreographyTaskName, String inputMessageName,
			Deque<LoopedFlowNodeData> loopedFlowNodes){
				
		// log execution flow
		LOGGER.entry("Send Operation name: "+sendProsumerPartOperationData.getName()
			+"Send Input Variable name: "+sendInputVariable.getName());
		
		// create assign
		Assign assignSendInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignSendInput.setName(BPELUtils.createAssignName(sendInputVariable.getName()));
		assignSendInput.setValidate(false);
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils
				.generateInitializationMessageString(prosumerPartWSDLDefinitionData.getWsdl(),
						sendProsumerPartOperationData.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(sendInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(sendProsumerPartOperationData.getInputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignSendInput
		assignSendInput.getCopy().add(copyInitialize);
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();	
		fromChoreographyId.setVariable(choreographyIDVariable);
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(sendInputVariable);	
		// create part for copyChoreographyId
		Part partChoreographyId = WSDLFactory.eINSTANCE.createPart();
		partChoreographyId.setName(sendProsumerPartOperationData.getInputMessageData().getPartName());
		// set partChoreographyId to toChoreographyId
		toChoreographyId.setPart(partChoreographyId);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);		
		// add copyChoreographyId to assignSendInput
		assignSendInput.getCopy().add(copyChoreographyId);
				
		// create copy for senderParticipantName
		Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create from for senderParticipantName
		From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
		fromSenderParticipantName.setVariable(participantNameVariable);
		// set from of copySenderParticipantName to fromSenderParticipantName
		copySenderParticipantName.setFrom(fromSenderParticipantName);		
		// create to for senderParticipantName
		To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
		toSenderParticipantName.setVariable(sendInputVariable);
		// create part for senderParticipantName
		Part partSenderParticipantName = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantName.setName(sendProsumerPartOperationData.getInputMessageData().getPartName());
		// set partSenderParticipantName to toSenderParticipantName
		toSenderParticipantName.setPart(partSenderParticipantName);
		// create query for senderParticipantName
		Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
		// set toSenderParticipantName query to queryToSenderParticipantName
		toSenderParticipantName.setQuery(queryToSenderParticipantName);		
		// set to of copySenderParticipantName to toSenderParticipantName
		copySenderParticipantName.setTo(toSenderParticipantName);		
		// add copysenderParticipantName to assignSendInput
		assignSendInput.getCopy().add(copySenderParticipantName);
		
		// create copy for receiverParticipantName
		Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();	
		// create from for receiverParticipantName
		From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
		fromReceiverParticipantName.setLiteral(receiverParticipantName);
		// set from of copyReceiverParticipantName to fromReceiverParticipantName
		copyReceiverParticipantName.setFrom(fromReceiverParticipantName);		
		// create to for receiverParticipantName
		To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
		toReceiverParticipantName.setVariable(sendInputVariable);
		// create part for receiverParticipantName
		Part partReceiverParticipantName = WSDLFactory.eINSTANCE.createPart();		
		partReceiverParticipantName.setName(sendProsumerPartOperationData.getInputMessageData()
				.getPartName());
		// set partReceiverParticipantName to toReceiverParticipantName
		toReceiverParticipantName.setPart(partReceiverParticipantName);
		// create query for receiverParticipantName
		Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
		// set toReceiverParticipantName query to queryReceiverParticipantName
		toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
		// set to of copyReceiverParticipantName to toReceiverParticipantName
		copyReceiverParticipantName.setTo(toReceiverParticipantName);		
		// add copyReceiverParticipantName to assignSendInput
		assignSendInput.getCopy().add(copyReceiverParticipantName);		

		// create copy for choreographyTaskName
		Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();			
		// create from for choreographyTaskName
		From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
		fromChoreographyTaskName.setLiteral(choreographyTaskName);
		// set from of copyChoreographyTaskName to fromChoreographyTaskName
		copyChoreographyTaskName.setFrom(fromChoreographyTaskName);		
		// create to for choreographyTaskName
		To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
		toChoreographyTaskName.setVariable(sendInputVariable);
		// create part for choreographyTaskName
		Part partChoreographyTaskName = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyTaskName.setName(sendProsumerPartOperationData.getInputMessageData().getPartName());
		// set partChoreographyTaskName to toChoreographyTaskName
		toChoreographyTaskName.setPart(partChoreographyTaskName);
		// create query for choreographyTaskName
		Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
		// set toChoreographyTaskName query to queryChoreographyTaskName
		toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
		// set to of copyChoreographyTaskName to toChoreographyTaskName
		copyChoreographyTaskName.setTo(toChoreographyTaskName);		
		// add copyChoreographyTaskName to assignSendInput
		assignSendInput.getCopy().add(copyChoreographyTaskName);				

		// create copy for inputMessageName
		Copy copyInputMessageName = BPELFactory.eINSTANCE.createCopy();			
		// create from for inputMessageName
		From fromInputMessageName = BPELFactory.eINSTANCE.createFrom();		
		fromInputMessageName.setLiteral(inputMessageName);
		// set from of copyInputMessageName to fromInputMessageName
		copyInputMessageName.setFrom(fromInputMessageName);		
		// create to for inputMessageName
		To toInputMessageName = BPELFactory.eINSTANCE.createTo();	
		toInputMessageName.setVariable(sendInputVariable);
		// create part for inputMessageName
		Part partInputMessageName = WSDLFactory.eINSTANCE.createPart();		
		partInputMessageName.setName(sendProsumerPartOperationData.getInputMessageData().getPartName());
		// set partInputMessageName to toInputMessageName
		toInputMessageName.setPart(partInputMessageName);
		// create query for inputMessageName
		Query queryInputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryInputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryInputMessageName.setValue(XPATH_QUERY_INPUT_MESSAGE_NAME);			
		// set toInputMessageName query to queryInputMessageName
		toInputMessageName.setQuery(queryInputMessageName);		
		// set to of copyInputMessageName to toInputMessageName
		copyInputMessageName.setTo(toInputMessageName);		
		// add copyChoreographyTaskName to assignSendInput
		assignSendInput.getCopy().add(copyInputMessageName);	
		
		// create copy for loop indexes
		Copy copyLoopIndexes = BPELFactory.eINSTANCE.createCopy();			
		// create from for loop indexes
		From fromLoopIndexes = BPELFactory.eINSTANCE.createFrom();			
		// check if loopedFlowNodes is empty
		if(!loopedFlowNodes.isEmpty()) {
			// loopedFlowNodes is not empty 			
			// create expression for fromLoopIndexes
			Expression fromExpression = BPELFactory.eINSTANCE.createExpression();
			fromExpression.setExpressionLanguage(XPATH_2_0_QUERY_LANGUAGE);
			// check if there is more than one element inside loopedFlowNodes
			if(loopedFlowNodes.size() > 1) {
				// there is more than one element inside loopedFlowNodes
				// create and initialize fromLoopIndexesExpressionBody
				StringBuilder fromLoopIndexesExpressionBody = new 
						StringBuilder(XPATH_CONCAT_OPERATOR_NAME + BRACKET_LEFT);
				// iterate over loopedFlowNodes	
				for (Iterator iterator = loopedFlowNodes.iterator(); iterator.hasNext();) {
					LoopedFlowNodeData loopedFlowNodeData = (LoopedFlowNodeData) iterator.next();
					// append the variable name of the current LoopedFlowNodeData of loopedFlowNodes
					// to fromLoopIndexesExpressionBody
					fromLoopIndexesExpressionBody.append(BPEL_VARIABLE_ACCESSOR_SYMBOL 
							+ loopedFlowNodeData.getVariableName());
					// check if there are any remaining LoopedFlowNodeData in loopedFlowNode
					if(iterator.hasNext()) {
						// there are any remaining LoopedFlowNodeData in loopedFlowNode
						// append ,',', to fromLoopIndexesExpressionBody
						fromLoopIndexesExpressionBody.append(COMMA_LABEL + QUOTATION_MARK + COMMA_LABEL 
								+ QUOTATION_MARK + COMMA_LABEL);
					}		
				}	
				// append ) to fromLoopIndexesExpressionBody
				fromLoopIndexesExpressionBody.append(BRACKET_RIGHT);
				// set fromLoopIndexesExpressionBody as body of fromExpression
				fromExpression.setBody(fromLoopIndexesExpressionBody.toString());	
				// set expression of fromLoopIndexes to fromExpression
				fromLoopIndexes.setExpression(fromExpression);	
			}
			else {				
				// there is one element inside loopedFlowNodes
				// get or create the loop variable
				Variable loopVariable = BPELUtils.getOrCreateStringVariable(process,
						loopedFlowNodes.getLast().getVariableName());
				// set loopVariable as variable of fromLoopIndexes
				fromLoopIndexes.setVariable(loopVariable);
			}

			// set from of copyLoopIndexes to fromLoopIndexes
			copyLoopIndexes.setFrom(fromLoopIndexes);
			
			// create to for LoopIndexes
			To toLoopIndexes = BPELFactory.eINSTANCE.createTo();	
			toLoopIndexes.setVariable(sendInputVariable);
			// create part for LoopIndexes
			Part partLoopIndexes = WSDLFactory.eINSTANCE.createPart();		
			partLoopIndexes.setName(sendProsumerPartOperationData.getInputMessageData().getPartName());
			// set partLoopIndexes to toLoopIndexes
			toLoopIndexes.setPart(partLoopIndexes);
			// create query for LoopIndexes
			Query queryLoopIndexes = BPELFactory.eINSTANCE.createQuery();
			queryLoopIndexes.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryLoopIndexes.setValue(XPATH_QUERY_LOOP_INDEXES);			
			// set toLoopIndexes query to queryLoopIndexes
			toLoopIndexes.setQuery(queryLoopIndexes);		
			// set to of copyLoopIndexes to toLoopIndexes
			copyLoopIndexes.setTo(toLoopIndexes);
			// add copyLoopIndexes to assignSendInput
			assignSendInput.getCopy().add(copyLoopIndexes);		
		}
		
		// log Assign information
		LOGGER.info("Assign: "+assignSendInput.getName()+" created!");
		// log execution flow
		LOGGER.exit();
		
		return assignSendInput;
	}
	
	public static Assign createAssignProviderOperationInput(Variable sendOutputVariable,
			OperationData sendOperationData, OperationData providerOperationData,
			Variable providerInputVariable, WSDLDefinitionData providerWSDLDefinitionData){
		
		// log execution flow
		LOGGER.entry("Provider Operation name: "+providerOperationData.getName()
			+" Provider Input Variable name: "+providerInputVariable.getName()
			+" Send Output Variable name: "+sendOutputVariable.getName());
		
		// create assign
		Assign assignProviderInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignProviderInput.setName(BPELUtils.createAssignName(providerInputVariable.getName()));
		assignProviderInput.setValidate(false);
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(providerWSDLDefinitionData
				.getWsdl(), providerOperationData.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(providerInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(providerOperationData.getInputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignProviderInput
		assignProviderInput.getCopy().add(copyInitialize);	
		
		// create copy for ProviderInputMessageData
		Copy copyProviderInputMessageData = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of ProviderInputMessageData
		Part partProviderInputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
		partProviderInputMessageDataInput.setName(sendOperationData.getOutputMessageData().getPartName());
		// create from for ProviderInputMessageData
		From fromProviderInputMessageData = BPELFactory.eINSTANCE.createFrom();	
		// set partProviderInputMessageDataInput to fromProviderInputMessageData
		fromProviderInputMessageData.setPart(partProviderInputMessageDataInput);
		fromProviderInputMessageData.setVariable(sendOutputVariable);		
		// create query for toProviderInputMessageData
		Query querytoDataItem = BPELFactory.eINSTANCE.createQuery();
		querytoDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		querytoDataItem.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);	
		// set querytoDataItem to fromProviderInputMessageData
		fromProviderInputMessageData.setQuery(querytoDataItem);
		// set from of copyProviderInputMessageData to fromProviderInputMessageData
		copyProviderInputMessageData.setFrom(fromProviderInputMessageData);	
		// create to for copyProviderInputMessageData
		To toProviderInputMessageData = BPELFactory.eINSTANCE.createTo();	
		// create part for output of copyProviderInputMessageData
		Part partProviderInputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
		partProviderInputMessageDataOutput.setName(providerOperationData.getInputMessageData()
				.getPartName());
		// set partProviderInputMessageDataOutput to toProviderInputMessageData
		toProviderInputMessageData.setPart(partProviderInputMessageDataOutput);
		// set variable to toProviderInputMessageData
		toProviderInputMessageData.setVariable(providerInputVariable);
		// set to of copyProviderInputMessageData to toProviderInputMessageData		
		copyProviderInputMessageData.setTo(toProviderInputMessageData);
		// add copyProviderInputMessageData to assignProviderInput
		assignProviderInput.getCopy().add(copyProviderInputMessageData);
		
		// log Assign information
		LOGGER.info("Assign: "+assignProviderInput.getName()+" created!");
		// log execution flow
		LOGGER.exit();
		
		return assignProviderInput;
	}

	public static Assign createAssignProsumerOperationInput(Process process, 
			WSDLDefinitionData prosumerWSDLDefinitionData, OperationData sendOperationData,
			Variable sendOutputVariable, OperationData prosumerOperationData,
			Variable prosumerInputVariable, Variable choreographyIDVariable,
			Variable participantNameVariable, String receiverParticipantName, String choreographyTaskName,
			String inputMessageName, boolean isReceiverParticipantClient,
			Deque<LoopedFlowNodeData> loopedFlowNodes){
		
		// log execution flow
		LOGGER.entry("Prosumer Operation name: "+prosumerOperationData.getName(),"Prosumer Variable name: "
				+prosumerInputVariable.getName());
		
		// create assign
		Assign assignProsumerInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignProsumerInput.setName(BPELUtils.createAssignName(prosumerInputVariable.getName()));
		assignProsumerInput.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(prosumerWSDLDefinitionData
				.getWsdl(), prosumerOperationData.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(prosumerInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignReceiveInput
		assignProsumerInput.getCopy().add(copyInitialize);
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();	
		fromChoreographyId.setVariable(choreographyIDVariable);
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(prosumerInputVariable);	
		// create part for copyChoreographyId
		Part partChoreographyId = WSDLFactory.eINSTANCE.createPart();
		partChoreographyId.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partChoreographyId to toChoreographyId
		toChoreographyId.setPart(partChoreographyId);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReceiveInput
		assignProsumerInput.getCopy().add(copyChoreographyId);	
		
		// check if the receiver participant is the client participant
		if(!isReceiverParticipantClient){
			// the receiver participant is the client participant
			
			// create copy for senderParticipantName
			Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
			// create from for senderParticipantName
			From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
			fromSenderParticipantName.setVariable(participantNameVariable);
			// set from of copySenderParticipantName to fromSenderParticipantName
			copySenderParticipantName.setFrom(fromSenderParticipantName);		
			// create to for senderParticipantName
			To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
			toSenderParticipantName.setVariable(prosumerInputVariable);
			// create part for senderParticipantName
			Part partSenderParticipantName = WSDLFactory.eINSTANCE.createPart();		
			partSenderParticipantName.setName(prosumerOperationData.getInputMessageData().getPartName());
			// set partSenderParticipantName to toSenderParticipantName
			toSenderParticipantName.setPart(partSenderParticipantName);
			// create query for senderParticipantName
			Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
			queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
			// set toSenderParticipantName query to queryToSenderParticipantName
			toSenderParticipantName.setQuery(queryToSenderParticipantName);		
			// set to of copySenderParticipantName to toSenderParticipantName
			copySenderParticipantName.setTo(toSenderParticipantName);		
			// add copysenderParticipantName to assignReceiveInput
			assignProsumerInput.getCopy().add(copySenderParticipantName);		
		
			// create copy for receiverParticipantName
			Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();	
			// create from for receiverParticipantName
			From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
			fromReceiverParticipantName.setLiteral(receiverParticipantName);
			// set from of copyReceiverParticipantName to fromReceiverParticipantName
			copyReceiverParticipantName.setFrom(fromReceiverParticipantName);		
			// create to for receiverParticipantName
			To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
			toReceiverParticipantName.setVariable(prosumerInputVariable);
			// create part for receiverParticipantName
			Part partReceiverParticipantName = WSDLFactory.eINSTANCE.createPart();		
			partReceiverParticipantName.setName(prosumerOperationData.getInputMessageData().getPartName());
			// set partReceiverParticipantName to toReceiverParticipantName
			toReceiverParticipantName.setPart(partReceiverParticipantName);
			// create query for receiverParticipantName
			Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
			queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
			// set toReceiverParticipantName query to queryReceiverParticipantName
			toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
			// set to of copyReceiverParticipantName to toReceiverParticipantName
			copyReceiverParticipantName.setTo(toReceiverParticipantName);		
			// add copyReceiverParticipantName to assignReceiveInput
			assignProsumerInput.getCopy().add(copyReceiverParticipantName);			
		
			// create copy for choreographyTaskName
			Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();			
			// create from for choreographyTaskName
			From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
			fromChoreographyTaskName.setLiteral(choreographyTaskName);
			// set from of copyChoreographyTaskName to fromChoreographyTaskName
			copyChoreographyTaskName.setFrom(fromChoreographyTaskName);		
			// create to for choreographyTaskName
			To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
			toChoreographyTaskName.setVariable(prosumerInputVariable);
			// create part for choreographyTaskName
			Part partChoreographyTaskName = WSDLFactory.eINSTANCE.createPart();		
			partChoreographyTaskName.setName(prosumerOperationData.getInputMessageData().getPartName());
			// set partChoreographyTaskName to toChoreographyTaskName
			toChoreographyTaskName.setPart(partChoreographyTaskName);
			// create query for choreographyTaskName
			Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
			queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
			// set toChoreographyTaskName query to queryChoreographyTaskName
			toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
			// set to of copyChoreographyTaskName to toChoreographyTaskName
			copyChoreographyTaskName.setTo(toChoreographyTaskName);		
			// add copyChoreographyTaskName to assignReceiveInput
			assignProsumerInput.getCopy().add(copyChoreographyTaskName);		
		
			// create copy for inputMessageName
			Copy copyInputMessageName = BPELFactory.eINSTANCE.createCopy();			
			// create from for inputMessageName
			From fromInputMessageName = BPELFactory.eINSTANCE.createFrom();		
			fromInputMessageName.setLiteral(inputMessageName);
			// set from of copyOutputMessageName to fromOutputMessageName
			copyInputMessageName.setFrom(fromInputMessageName);		
			// create to for outputMessageName
			To toInputMessageName = BPELFactory.eINSTANCE.createTo();	
			toInputMessageName.setVariable(prosumerInputVariable);
			// create part for outputMessageName
			Part partInputMessageName = WSDLFactory.eINSTANCE.createPart();		
			partInputMessageName.setName(prosumerOperationData.getInputMessageData().getPartName());
			// set partInputMessageName to toInputMessageName
			toInputMessageName.setPart(partInputMessageName);
			// create query for outputMessageName
			Query queryOutputMessageName = BPELFactory.eINSTANCE.createQuery();
			queryOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			queryOutputMessageName.setValue(XPATH_QUERY_INPUT_MESSAGE_NAME);			
			// set toOutputMessageName query to queryOutputMessageName
			toInputMessageName.setQuery(queryOutputMessageName);		
			// set to of copyInputMessageName to toOutputMessageName
			copyInputMessageName.setTo(toInputMessageName);		
			// add copyChoreographyTaskName to assignReceiveInput
			assignProsumerInput.getCopy().add(copyInputMessageName);			

			// create copy for loop indexes
			Copy copyLoopIndexes = BPELFactory.eINSTANCE.createCopy();			
			// create from for loop indexes
			From fromLoopIndexes = BPELFactory.eINSTANCE.createFrom();			
			// check if loopedFlowNodes is empty
			if(!loopedFlowNodes.isEmpty()) {
				// loopedFlowNodes is not empty 			
				// create expression for fromLoopIndexes
				Expression fromExpression = BPELFactory.eINSTANCE.createExpression();
				fromExpression.setExpressionLanguage(XPATH_2_0_QUERY_LANGUAGE);
				// check if there is more than one element inside loopedFlowNodes
				if(loopedFlowNodes.size() > 1) {
					// there is more than one element inside loopedFlowNodes
					// create and initialize fromLoopIndexesExpressionBody
					StringBuilder fromLoopIndexesExpressionBody = new 
							StringBuilder(XPATH_CONCAT_OPERATOR_NAME + BRACKET_LEFT);
					// iterate over loopedFlowNodes	
					for (Iterator iterator = loopedFlowNodes.iterator(); iterator.hasNext();) {
						LoopedFlowNodeData loopedFlowNodeData = (LoopedFlowNodeData) iterator.next();
						// append the variable name of the current LoopedFlowNodeData of loopedFlowNodes
						// to fromLoopIndexesExpressionBody
						fromLoopIndexesExpressionBody.append(BPEL_VARIABLE_ACCESSOR_SYMBOL 
								+ loopedFlowNodeData.getVariableName());
						// check if there are any remaining LoopedFlowNodeData in loopedFlowNode
						if(iterator.hasNext()) {
							// there are any remaining LoopedFlowNodeData in loopedFlowNode
							// append ,',', to fromLoopIndexesExpressionBody
							fromLoopIndexesExpressionBody.append(COMMA_LABEL + QUOTATION_MARK + COMMA_LABEL 
									+ QUOTATION_MARK + COMMA_LABEL);
						}			
					}	
					// append ) to fromLoopIndexesExpressionBody
					fromLoopIndexesExpressionBody.append(BRACKET_RIGHT);
					// set fromLoopIndexesExpressionBody as body of fromExpression
					fromExpression.setBody(fromLoopIndexesExpressionBody.toString());	
					// set expression of fromLoopIndexes to fromExpression
					fromLoopIndexes.setExpression(fromExpression);	
				}
				else {				
					// there is one element inside loopedFlowNodes
					// get or create the loop variable
					Variable loopVariable = BPELUtils.getOrCreateStringVariable(process,
							loopedFlowNodes.getLast().getVariableName());
					// set loopVariable as variable of fromLoopIndexes
					fromLoopIndexes.setVariable(loopVariable);
				}
				
				// set from of copyLoopIndexes to fromLoopIndexes
				copyLoopIndexes.setFrom(fromLoopIndexes);
				
				// create to for LoopIndexes
				To toLoopIndexes = BPELFactory.eINSTANCE.createTo();	
				toLoopIndexes.setVariable(prosumerInputVariable);
				// create part for LoopIndexes
				Part partLoopIndexes = WSDLFactory.eINSTANCE.createPart();		
				partLoopIndexes.setName(prosumerOperationData.getInputMessageData().getPartName());
				// set partLoopIndexes to toLoopIndexes
				toLoopIndexes.setPart(partLoopIndexes);
				// create query for LoopIndexes
				Query queryLoopIndexes = BPELFactory.eINSTANCE.createQuery();
				queryLoopIndexes.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
				queryLoopIndexes.setValue(XPATH_QUERY_LOOP_INDEXES);			
				// set toLoopIndexes query to queryLoopIndexes
				toLoopIndexes.setQuery(queryLoopIndexes);		
				// set to of copyLoopIndexes to toLoopIndexes
				copyLoopIndexes.setTo(toLoopIndexes);
				// add copyLoopIndexes to assignProsumerInput
				assignProsumerInput.getCopy().add(copyLoopIndexes);		
			}		
			
			// create copy for inputMessageData
			Copy copyInputMessageData = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyInputMessageData
			Part partInputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
			partInputMessageDataInput.setName(sendOperationData.getOutputMessageData().getPartName());
			// create from for outputMessageData
			From fromInputMessageData = BPELFactory.eINSTANCE.createFrom();	
			fromInputMessageData.setPart(partInputMessageDataInput);
			fromInputMessageData.setVariable(sendOutputVariable);
			// create query for toDataItem
			Query querytoDataItemInputFrom = BPELFactory.eINSTANCE.createQuery();
			querytoDataItemInputFrom.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItemInputFrom.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);		
			// set querytoDataItemInputFrom to fromInputMessageData
			fromInputMessageData.setQuery(querytoDataItemInputFrom);
			// set from of copyOutputMessageData to fromOutputMessageData
			copyInputMessageData.setFrom(fromInputMessageData);	
			// create to for copyOutputMessageData
			To toInputMessageData = BPELFactory.eINSTANCE.createTo();	
			// create part for output of copyOutputMessageData
			Part partInputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
			partInputMessageDataOutput.setName(prosumerOperationData.getInputMessageData().getPartName());
			// set partOutputMessageDataOutput to toOutputMessageData
			toInputMessageData.setPart(partInputMessageDataOutput);
			// set variable to toDataItem
			toInputMessageData.setVariable(prosumerInputVariable);
			// create query for toDataItem
			Query querytoDataItemInputTo = BPELFactory.eINSTANCE.createQuery();
			querytoDataItemInputTo.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItemInputTo.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);		
			// set querytoDataItem to toDataItem
			toInputMessageData.setQuery(querytoDataItemInputTo);
			// set to of copyDataItem to toDataItem		
			copyInputMessageData.setTo(toInputMessageData);
			// add copyDataItem to assignReceiveInput
			assignProsumerInput.getCopy().add(copyInputMessageData);
					
		}
		else{
			// prosumer is the client participant
			
			// create copy for MessageData
			Copy copyMessageData = BPELFactory.eINSTANCE.createCopy();	
			// create part for input of copyMessageData
			Part partMessageDataInput = WSDLFactory.eINSTANCE.createPart();
			partMessageDataInput.setName(sendOperationData.getOutputMessageData().getPartName());
			// create from for copyMessageData
			From fromMessageData = BPELFactory.eINSTANCE.createFrom();	
			fromMessageData.setPart(partMessageDataInput);
			fromMessageData.setVariable(sendOutputVariable);
			// create query for toDataItem
			Query querytoDataItemInputFrom = BPELFactory.eINSTANCE.createQuery();
			querytoDataItemInputFrom.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItemInputFrom.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);		
			// set querytoDataItemInputFrom to fromInputMessageData
			fromMessageData.setQuery(querytoDataItemInputFrom);
			// set from of copyOutputMessageData to fromOutputMessageData
			copyMessageData.setFrom(fromMessageData);	
			// create to for copyOutputMessageData
			To toMessageData = BPELFactory.eINSTANCE.createTo();	
			// create part for output of copyOutputMessageData
			Part partMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
			partMessageDataOutput.setName(prosumerOperationData.getInputMessageData().getPartName());
			// set partOutputMessageDataOutput to toOutputMessageData
			toMessageData.setPart(partMessageDataOutput);
			// set variable to toDataItem
			toMessageData.setVariable(prosumerInputVariable);
			// create query for toDataItem
			Query querytoDataItemInputTo = BPELFactory.eINSTANCE.createQuery();
			querytoDataItemInputTo.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
			querytoDataItemInputTo.setValue(XPATH_QUERY_MESSAGE_DATA);		
			// set querytoDataItem to toDataItem
			toMessageData.setQuery(querytoDataItemInputTo);
			// set to of copyDataItem to toDataItem		
			copyMessageData.setTo(toMessageData);
			// add copyDataItem to assignReceiveInput
			assignProsumerInput.getCopy().add(copyMessageData);
			
		}
		
		// log Assing information
		LOGGER.info("Assign: "+assignProsumerInput.getName()+" created!");
		// log exectution flow
		LOGGER.exit();
		
		return assignProsumerInput;
	}
	
	public static Assign createAssignCDClientProsumerOperationInput(
			WSDLDefinitionData prosumerWSDLDefinitionData, 
			OperationData operationData, Variable operationInputVariable,
			OperationData prosumerOperationData, Variable prosumerInputVariable,
			Variable choreographyIDVariable, Variable participantNameVariable,
			String receiverParticipantName, String choreographyTaskName, String inputMessageName){
		
		// log execution flow
		LOGGER.entry("Prosumer Operation name: "+prosumerOperationData.getName()+"Prosumer Variable name: "
				+prosumerInputVariable.getName());
		
		// create assign
		Assign assignProsumerInput = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignProsumerInput.setName(BPELUtils.createAssignName(prosumerInputVariable.getName()));
		assignProsumerInput.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(prosumerWSDLDefinitionData
				.getWsdl(), prosumerOperationData.getInputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(prosumerInputVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignReceiveInput
		assignProsumerInput.getCopy().add(copyInitialize);
		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();	
		fromChoreographyId.setVariable(choreographyIDVariable);
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(prosumerInputVariable);	
		// create part for copyChoreographyId
		Part partChoreographyId = WSDLFactory.eINSTANCE.createPart();
		partChoreographyId.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partChoreographyId to toChoreographyId
		toChoreographyId.setPart(partChoreographyId);		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);		
		// set toChoreographyId query to queryToChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReceiveInput
		assignProsumerInput.getCopy().add(copyChoreographyId);	

		// create copy for senderParticipantName
		Copy copySenderParticipantName = BPELFactory.eINSTANCE.createCopy();
		// create from for senderParticipantName
		From fromSenderParticipantName = BPELFactory.eINSTANCE.createFrom();
		fromSenderParticipantName.setVariable(participantNameVariable);
		// set from of copySenderParticipantName to fromSenderParticipantName
		copySenderParticipantName.setFrom(fromSenderParticipantName);		
		// create to for senderParticipantName
		To toSenderParticipantName = BPELFactory.eINSTANCE.createTo();
		toSenderParticipantName.setVariable(prosumerInputVariable);
		// create part for senderParticipantName
		Part partSenderParticipantName = WSDLFactory.eINSTANCE.createPart();		
		partSenderParticipantName.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partSenderParticipantName to toSenderParticipantName
		toSenderParticipantName.setPart(partSenderParticipantName);
		// create query for senderParticipantName
		Query queryToSenderParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryToSenderParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToSenderParticipantName.setValue(XPATH_QUERY_SENDER_PARTICIPANT_NAME);			
		// set toSenderParticipantName query to queryToSenderParticipantName
		toSenderParticipantName.setQuery(queryToSenderParticipantName);		
		// set to of copySenderParticipantName to toSenderParticipantName
		copySenderParticipantName.setTo(toSenderParticipantName);		
		// add copysenderParticipantName to assignReceiveInput
		assignProsumerInput.getCopy().add(copySenderParticipantName);		
	
		// create copy for receiverParticipantName
		Copy copyReceiverParticipantName = BPELFactory.eINSTANCE.createCopy();	
		// create from for receiverParticipantName
		From fromReceiverParticipantName = BPELFactory.eINSTANCE.createFrom();		
		fromReceiverParticipantName.setLiteral(receiverParticipantName);
		// set from of copyReceiverParticipantName to fromReceiverParticipantName
		copyReceiverParticipantName.setFrom(fromReceiverParticipantName);		
		// create to for receiverParticipantName
		To toReceiverParticipantName = BPELFactory.eINSTANCE.createTo();	
		toReceiverParticipantName.setVariable(prosumerInputVariable);
		// create part for receiverParticipantName
		Part partReceiverParticipantName = WSDLFactory.eINSTANCE.createPart();		
		partReceiverParticipantName.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partReceiverParticipantName to toReceiverParticipantName
		toReceiverParticipantName.setPart(partReceiverParticipantName);
		// create query for receiverParticipantName
		Query queryReceiverParticipantName = BPELFactory.eINSTANCE.createQuery();
		queryReceiverParticipantName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryReceiverParticipantName.setValue(XPATH_QUERY_RECEIVER_PARTICIPANT_NAME);			
		// set toReceiverParticipantName query to queryReceiverParticipantName
		toReceiverParticipantName.setQuery(queryReceiverParticipantName);		
		// set to of copyReceiverParticipantName to toReceiverParticipantName
		copyReceiverParticipantName.setTo(toReceiverParticipantName);		
		// add copyReceiverParticipantName to assignReceiveInput
		assignProsumerInput.getCopy().add(copyReceiverParticipantName);			
	
		// create copy for choreographyTaskName
		Copy copyChoreographyTaskName = BPELFactory.eINSTANCE.createCopy();			
		// create from for choreographyTaskName
		From fromChoreographyTaskName = BPELFactory.eINSTANCE.createFrom();		
		fromChoreographyTaskName.setLiteral(choreographyTaskName);
		// set from of copyChoreographyTaskName to fromChoreographyTaskName
		copyChoreographyTaskName.setFrom(fromChoreographyTaskName);		
		// create to for choreographyTaskName
		To toChoreographyTaskName = BPELFactory.eINSTANCE.createTo();	
		toChoreographyTaskName.setVariable(prosumerInputVariable);
		// create part for choreographyTaskName
		Part partChoreographyTaskName = WSDLFactory.eINSTANCE.createPart();		
		partChoreographyTaskName.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partChoreographyTaskName to toChoreographyTaskName
		toChoreographyTaskName.setPart(partChoreographyTaskName);
		// create query for choreographyTaskName
		Query queryChoreographyTaskName = BPELFactory.eINSTANCE.createQuery();
		queryChoreographyTaskName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryChoreographyTaskName.setValue(XPATH_QUERY_CHOREOGRAPHY_TASK_NAME);			
		// set toChoreographyTaskName query to queryChoreographyTaskName
		toChoreographyTaskName.setQuery(queryChoreographyTaskName);		
		// set to of copyChoreographyTaskName to toChoreographyTaskName
		copyChoreographyTaskName.setTo(toChoreographyTaskName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignProsumerInput.getCopy().add(copyChoreographyTaskName);		
	
		// create copy for inputMessageName
		Copy copyInputMessageName = BPELFactory.eINSTANCE.createCopy();			
		// create from for inputMessageName
		From fromInputMessageName = BPELFactory.eINSTANCE.createFrom();		
		fromInputMessageName.setLiteral(inputMessageName);
		// set from of copyOutputMessageName to fromOutputMessageName
		copyInputMessageName.setFrom(fromInputMessageName);		
		// create to for outputMessageName
		To toInputMessageName = BPELFactory.eINSTANCE.createTo();	
		toInputMessageName.setVariable(prosumerInputVariable);
		// create part for outputMessageName
		Part partInputMessageName = WSDLFactory.eINSTANCE.createPart();		
		partInputMessageName.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partInputMessageName to toInputMessageName
		toInputMessageName.setPart(partInputMessageName);
		// create query for outputMessageName
		Query queryOutputMessageName = BPELFactory.eINSTANCE.createQuery();
		queryOutputMessageName.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryOutputMessageName.setValue(XPATH_QUERY_INPUT_MESSAGE_NAME);			
		// set toOutputMessageName query to queryOutputMessageName
		toInputMessageName.setQuery(queryOutputMessageName);		
		// set to of copyInputMessageName to toOutputMessageName
		copyInputMessageName.setTo(toInputMessageName);		
		// add copyChoreographyTaskName to assignReceiveInput
		assignProsumerInput.getCopy().add(copyInputMessageName);			
		
		// create copy for inputMessageData
		Copy copyInputMessageData = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyInputMessageData
		Part partInputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
		partInputMessageDataInput.setName(operationData.getInputMessageData().getPartName());
		// create from for outputMessageData
		From fromInputMessageData = BPELFactory.eINSTANCE.createFrom();	
		// set partInputMessageDataInput to fromInputMessageData
		fromInputMessageData.setPart(partInputMessageDataInput);
		fromInputMessageData.setVariable(operationInputVariable);
		// create query for fromDataItem
		Query queryFromDataItemInputTo = BPELFactory.eINSTANCE.createQuery();
		queryFromDataItemInputTo.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromDataItemInputTo.setValue(XPATH_QUERY_MESSAGE_DATA);
		// set fromInputMessageData to queryFromDataItemInputTo
		fromInputMessageData.setQuery(queryFromDataItemInputTo);
		// set from of copyOutputMessageData to fromOutputMessageData
		copyInputMessageData.setFrom(fromInputMessageData);	
		// create to for copyOutputMessageData
		To toInputMessageData = BPELFactory.eINSTANCE.createTo();	
		// create part for output of copyOutputMessageData
		Part partInputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();	
		partInputMessageDataOutput.setName(prosumerOperationData.getInputMessageData().getPartName());
		// set partOutputMessageDataOutput to toOutputMessageData
		toInputMessageData.setPart(partInputMessageDataOutput);
		// set variable to toDataItem
		toInputMessageData.setVariable(prosumerInputVariable);
		// create query for toDataItem
		Query querytoDataItemInputTo = BPELFactory.eINSTANCE.createQuery();
		querytoDataItemInputTo.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		querytoDataItemInputTo.setValue(XPATH_QUERY_INPUT_MESSAGE_DATA);		
		// set querytoDataItem to toDataItem
		toInputMessageData.setQuery(querytoDataItemInputTo);
		// set to of copyDataItem to toDataItem		
		copyInputMessageData.setTo(toInputMessageData);
		// add copyDataItem to assignReceiveInput
		assignProsumerInput.getCopy().add(copyInputMessageData);
				
		// log Assing information
		LOGGER.info("Assign: "+assignProsumerInput.getName()+" created!");
		// log execution flow
		LOGGER.exit();
		
		return assignProsumerInput;
	}	
	
	public static Condition createCDProsumerXPathConditionOutgoingDivergingExclusiveGateway(Process process, 
			String cdParticipantName, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, WSDLDefinitionData prosumerPartWSDLDefinitionData, 
			List<Choreography> choreographies, SequenceFlow sequenceFlow){

		// log execution flow
		LOGGER.entry();	
		
		// get condition body
		String conditionBody = BPMNUtils.getConditionBodyConditionExpressionOfSequenceFlow(sequenceFlow);
		// get message names contained into the body of condition
		List<String> messagesNames = BPMNUtils.getMessagesNamesFromSequenceFlowConditionBody(conditionBody);
		
		// iterate over every messageName
		for (String messageName : messagesNames) {
			// get choreography message information
			ChoreographyMessageInformation choreographyMessageInformation = BPMNUtils
					.getDataTypeChoreographyInformation(choreographies, messageName);				
			Definition wsdl = null;
			boolean isDataTypeContainedIntoInputMessage = false;
			String operationName = null;
			String prefix = null;
			String variableName = null;
			// check if cd participant is the receiving participant of the 
			// task of dataTypeChoreographyInformation
			if(choreographyMessageInformation.getReceivingParticipantName().equals(cdParticipantName)){
				// cd participant is the receiving participant of the task of dataTypeChoreographyInformation
				// wsdl is the wsdl of the CD
				wsdl = cdWSDLDefinitionData.getWsdl();		
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix
				prefix = cdWSDLDefinitionData.getPrefix();			
				// check if cd participant is the sender of the message
				if(choreographyMessageInformation.getSenderParticipantName().equals(cdParticipantName)){
					// cd participant is the sender of the message
					// data type is contained into the output message of the CD operation
					isDataTypeContainedIntoInputMessage = false;
					// set variable name
					variableName = BPELUtils.createOutputVariableName(cdParticipantName, operationName);
					
				}
				else{
					// cd participant is the receiver of the message
					// data type is contained into the input message of the CD operation
					isDataTypeContainedIntoInputMessage = true;				
					// set variable name
					variableName = BPELUtils.createInputVariableName(cdParticipantName, operationName);				
				}		
			}
			// check if cd participant is the initiating participant of the task of dataTypeChoreographyInformation and
			// it is also the sender participant
			else if(choreographyMessageInformation.getInitiatingParticipantName().equals(cdParticipantName) 
						&& choreographyMessageInformation.getSenderParticipantName().equals(cdParticipantName)){
				// cd participant is the initiating participant of the task of dataTypeChoreographyInformation and
				// it is also the sender participant
				// wsdl is the wsdl of the prosumer
				wsdl = prosumerPartWSDLDefinitionData.getWsdl();
				// set operationName to send operation name corresponding to choreography task name
				operationName = WSDLUtils.createSendOperationName(choreographyMessageInformation
						.getChoreographyMessageName());
				// set prefix
				prefix = prosumerPartWSDLDefinitionData.getPrefix();
				// set variable name
				variableName = BPELUtils.createOutputVariableName(cdParticipantName, 
						WSDLUtils.createSendOperationName(choreographyMessageInformation
								.getChoreographyMessageName()));			
				// data type is contained into the output message of the prosumer operation
				isDataTypeContainedIntoInputMessage = false;			
			}
			else if (choreographyMessageInformation.getInitiatingParticipantName().equals(cdParticipantName) 
						&& choreographyMessageInformation.getReceiverParticipantName()
						.equals(cdParticipantName)) {
				// cd participant is the initiating participant of the task of 
				// dataTypeChoreographyInformation and it is also the receiver participant		
				// wsdl is the wsdl of the sender participant
				wsdl = 	wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getWsdl();
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix 
				prefix = wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getPrefix();
				// set variable name
				variableName = BPELUtils.createOutputVariableName(choreographyMessageInformation
						.getSenderParticipantName(), operationName);						
				// data type is contained into the output message of the sender participant operation
				isDataTypeContainedIntoInputMessage = false;		
			}	
			// get data type information of WSDL
			DataTypeWSDLInformation dataTypeWSDLInformation = WSDLUtils.getDataTypeWSDLInformation(wsdl,
					operationName, choreographyMessageInformation.getDataTypeName(),
					isDataTypeContainedIntoInputMessage);
			// create messageTypeVariable
			Variable messageTypeVariable = BPELUtils.getOrCreateVariableForMessageType(process,
					variableName, prefix, dataTypeWSDLInformation.getMessageName());
			// create message variable XPath expression
			String messageVariableXPathExpression = null;
			if(dataTypeWSDLInformation.isMainElementOfMessage()){
				// create an xpath expression of the form  
				// $variableName.partName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).toString();
			}
			else{
				// create an xpath expression of the form  
				// $variableName.partName/elementName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).append(SLASH_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getElementName()).toString();			
			}
			// replace messageName with messageVariableXPath into conditionBody
			conditionBody = conditionBody.replace(messageName, messageVariableXPathExpression);			
		}		
		// convert all Left double quotation mark and Right double quotation mark into "
		conditionBody = BPELUtils.convertDoubleQuotes(conditionBody);
		// create condition		
		Condition condition = BPELFactory.eINSTANCE.createCondition();				
		condition.setBody(conditionBody);
		
		// log execution flow
		LOGGER.exit();		
		
		return condition;
	}
	
	public static Expression createCDProsumerXPathExpression(String conditionBody, Process process, 
			String cdParticipantName, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, WSDLDefinitionData prosumerPartWSDLDefinitionData, 
			List<Choreography> choreographies){

		// log execution flow
		LOGGER.entry();	

		// get message names contained into the body of condition
		List<String> messagesNames = BPMNUtils.getMessagesNamesFromSequenceFlowConditionBody(conditionBody);
		
		// iterate over every messageName
		for (String messageName : messagesNames) {
			// get choreography message information
			ChoreographyMessageInformation choreographyMessageInformation = BPMNUtils
					.getDataTypeChoreographyInformation(choreographies, messageName);				
			Definition wsdl = null;
			boolean isDataTypeContainedIntoInputMessage = false;
			String operationName = null;
			String prefix = null;
			String variableName = null;
			// check if cd participant is the receiving participant of the 
			// task of dataTypeChoreographyInformation
			if(choreographyMessageInformation.getReceivingParticipantName().equals(cdParticipantName)){
				// cd participant is the receiving participant of the task 
				// of dataTypeChoreographyInformation, wsdl is the wsdl of the CD
				wsdl = cdWSDLDefinitionData.getWsdl();		
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix
				prefix = cdWSDLDefinitionData.getPrefix();			
				// check if cd participant is the sender of the message
				if(choreographyMessageInformation.getSenderParticipantName().equals(cdParticipantName)){
					// cd participant is the sender of the message
					// data type is contained into the output message of the CD operation
					isDataTypeContainedIntoInputMessage = false;
					// set variable name
					variableName = BPELUtils.createOutputVariableName(cdParticipantName, operationName);
					
				}
				else{
					// cd participant is the receiver of the message
					// data type is contained into the input message of the CD operation
					isDataTypeContainedIntoInputMessage = true;				
					// set variable name
					variableName = BPELUtils.createInputVariableName(cdParticipantName, operationName);				
				}		
			}
			// check if cd participant is the initiating participant of the task of 
			// dataTypeChoreographyInformation and it is also the sender participant
			else if(choreographyMessageInformation.getInitiatingParticipantName().equals(cdParticipantName) 
						&& choreographyMessageInformation
						.getSenderParticipantName().equals(cdParticipantName)){
				// cd participant is the initiating participant of the task of 
				// dataTypeChoreographyInformation and it is also the sender participant
				// wsdl is the wsdl of the prosumer
				wsdl = prosumerPartWSDLDefinitionData.getWsdl();
				// set operationName to send operation name corresponding to choreography task name
				operationName = WSDLUtils.createSendOperationName(choreographyMessageInformation
						.getChoreographyMessageName());
				// set prefix
				prefix = prosumerPartWSDLDefinitionData.getPrefix();
				// set variable name
				variableName = BPELUtils.createOutputVariableName(cdParticipantName, 
						WSDLUtils.createSendOperationName(choreographyMessageInformation
								.getChoreographyMessageName()));			
				// data type is contained into the output message of the prosumer operation
				isDataTypeContainedIntoInputMessage = false;			
			}
			else if (choreographyMessageInformation.getInitiatingParticipantName().equals(cdParticipantName) 
						&& choreographyMessageInformation.getReceiverParticipantName()
						.equals(cdParticipantName)) {
				// cd participant is the initiating participant of the task of 
				// dataTypeChoreographyInformation and it is also the receiver participant		
				// wsdl is the wsdl of the sender participant
				wsdl = 	wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getWsdl();
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix 
				prefix = wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getPrefix();
				// set variable name
				variableName = BPELUtils.createOutputVariableName(choreographyMessageInformation
						.getSenderParticipantName(), operationName);						
				// data type is contained into the output message of the sender participant operation
				isDataTypeContainedIntoInputMessage = false;		
			}	
			// get data type information of WSDL
			DataTypeWSDLInformation dataTypeWSDLInformation = WSDLUtils.getDataTypeWSDLInformation(wsdl,
					operationName, choreographyMessageInformation.getDataTypeName(),
					isDataTypeContainedIntoInputMessage);
			// create messageTypeVariable
			Variable messageTypeVariable = BPELUtils.getOrCreateVariableForMessageType(process,
					variableName, prefix, dataTypeWSDLInformation.getMessageName());
			// create message variable XPath expression
			String messageVariableXPathExpression = null;
			if(dataTypeWSDLInformation.isMainElementOfMessage()){
				// create an xpath expression of the form  
				// $variableName.partName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).toString();
			}
			else{
				// create an xpath expression of the form  
				// $variableName.partName/elementName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).append(SLASH_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getElementName()).toString();			
			}
			// replace messageName with messageVariableXPath into conditionBody
			conditionBody = conditionBody.replace(messageName, messageVariableXPathExpression);			
		}		
		// convert all Left double quotation mark and Right double quotation mark into "
		conditionBody = BPELUtils.convertDoubleQuotes(conditionBody);
		
		// create expression
		Expression expression = BPELFactory.eINSTANCE.createExpression();
		expression.setBody(conditionBody);	
		expression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
		
		// log execution flow
		LOGGER.exit();		
		
		return expression;
	}	
	
	public static Expression createCDClientXPathExpression(String conditionBody, Process process, 
			String cdParticipantName, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, List<Choreography> choreographies){

		// log execution flow
		LOGGER.entry();	

		// get message names contained into the body of condition
		List<String> messagesNames = BPMNUtils.getMessagesNamesFromSequenceFlowConditionBody(conditionBody);
		
		// iterate over every messageName
		for (String messageName : messagesNames) {
			// get choreography message information
			ChoreographyMessageInformation choreographyMessageInformation = BPMNUtils
					.getDataTypeChoreographyInformation(choreographies, messageName);				
			Definition wsdl = null;
			boolean isDataTypeContainedIntoInputMessage = false;
			String operationName = null;
			String prefix = null;
			String variableName = null;
			// check if cd participant is the sender participant of the 
			// task of dataTypeChoreographyInformation
			if(choreographyMessageInformation.getSenderParticipantName().equals(cdParticipantName)){
				// cd participant is the sender participant of the task 
				// of dataTypeChoreographyInformation, wsdl is the wsdl of the CD
				wsdl = cdWSDLDefinitionData.getWsdl();		
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix
				prefix = cdWSDLDefinitionData.getPrefix();			
				// data type is contained into the input message of the CD operation
				isDataTypeContainedIntoInputMessage = true;				
				// set variable name
				variableName = BPELUtils.createInputVariableName(cdParticipantName, operationName);				
			}		
			// check if cd participant is the receiver participant of the 
			// task of dataTypeChoreographyInformation
			else if	(choreographyMessageInformation.getReceiverParticipantName().equals(cdParticipantName)) {
				// cd participant is the receiver participant of the 
				// task of dataTypeChoreographyInformation		
				// wsdl is the wsdl of the sender participant
				wsdl = 	wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getWsdl();
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix 
				prefix = wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getPrefix();
				// set variable name
				variableName = BPELUtils.createOutputVariableName(choreographyMessageInformation
						.getSenderParticipantName(), operationName);						
				// data type is contained into the output message of the sender participant operation
				isDataTypeContainedIntoInputMessage = false;		
			}	
			// get data type information of WSDL
			DataTypeWSDLInformation dataTypeWSDLInformation = WSDLUtils.getDataTypeWSDLInformation(wsdl,
					operationName, choreographyMessageInformation.getDataTypeName(),
					isDataTypeContainedIntoInputMessage);
			// create messageTypeVariable
			Variable messageTypeVariable = BPELUtils.getOrCreateVariableForMessageType(process,
					variableName, prefix, dataTypeWSDLInformation.getMessageName());
			// create message variable XPath expression
			String messageVariableXPathExpression = null;
			if(dataTypeWSDLInformation.isMainElementOfMessage()){
				// create an xpath expression of the form  
				// $variableName.partName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).toString();
			}
			else{
				// create an xpath expression of the form  
				// $variableName.partName/elementName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).append(SLASH_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getElementName()).toString();			
			}
			// replace messageName with messageVariableXPath into conditionBody
			conditionBody = conditionBody.replace(messageName, messageVariableXPathExpression);			
		}		
		// convert all Left double quotation mark and Right double quotation mark into "
		conditionBody = BPELUtils.convertDoubleQuotes(conditionBody);
		
		// create expression
		Expression expression = BPELFactory.eINSTANCE.createExpression();
		expression.setBody(conditionBody);	
		expression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
		
		// log execution flow
		LOGGER.exit();		
		
		return expression;
	}		
	
	public static Condition createCDClientXPathConditionOutgoingDivergingExclusiveGateway(Process process, 
			String cdParticipantName, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, List<Choreography> choreographies,
			SequenceFlow sequenceFlow){

		// log execution flow
		LOGGER.entry();	

		// get condition body
		String conditionBody = BPMNUtils.getConditionBodyConditionExpressionOfSequenceFlow(sequenceFlow);
		// get message names contained into the body of condition
		List<String> messagesNames = BPMNUtils.getMessagesNamesFromSequenceFlowConditionBody(conditionBody);	
		
		// iterate over every messageName		
		for (String messageName : messagesNames) {
			// get data type choreography information
			ChoreographyMessageInformation choreographyMessageInformation = BPMNUtils
					.getDataTypeChoreographyInformation(choreographies, messageName);	
			Definition wsdl = null;
			boolean isDataTypeContainedIntoInputMessage = false;
			String operationName = null;
			String prefix = null;
			String variableName = null;
			// check if cd participant is the receiving participant of the task of 
			// dataTypeChoreographyInformation
			if(choreographyMessageInformation.getReceivingParticipantName().equals(cdParticipantName)){
				// cd participant is the receiving participant of the task of dataTypeChoreographyInformation
				// wsdl is the wsdl of the CD
				wsdl = cdWSDLDefinitionData.getWsdl();		
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix
				prefix = cdWSDLDefinitionData.getPrefix();			
				// check if cd participant is the sender of the message
				if(choreographyMessageInformation.getSenderParticipantName().equals(cdParticipantName)){
					// cd participant is the sender of the message
					// data type is contained into the output message of the CD operation
					isDataTypeContainedIntoInputMessage = false;
					// set variable name
					variableName = BPELUtils.createOutputVariableName(cdParticipantName, operationName);
					
				}
				else{
					// cd participant is the receiver of the message
					// data type is contained into the input message of the CD operation
					isDataTypeContainedIntoInputMessage = true;				
					// set variable name
					variableName = BPELUtils.createInputVariableName(cdParticipantName, operationName);				
				}		
			}
			// check if cd participant is the initiating participant of the task of 
			// dataTypeChoreographyInformation and it is also the sender participant
			else if(choreographyMessageInformation.getInitiatingParticipantName().equals(cdParticipantName) 
						&& choreographyMessageInformation.getSenderParticipantName().equals(cdParticipantName)){
				// cd participant is the initiating participant of the task of 
				// dataTypeChoreographyInformation and it is also the sender participant
				// wsdl is the wsdl of the CD
				wsdl = cdWSDLDefinitionData.getWsdl();
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix
				prefix = cdWSDLDefinitionData.getPrefix();
				// set variable name
				variableName = BPELUtils.createInputVariableName(cdParticipantName, operationName);				
				// data type is contained into the output message of the prosumer operation
				isDataTypeContainedIntoInputMessage = true;			
			}
			else if (choreographyMessageInformation.getInitiatingParticipantName().equals(cdParticipantName) 
						&& choreographyMessageInformation.getReceiverParticipantName()
						.equals(cdParticipantName)) {
				// cd participant is the initiating participant of the task of 
				// dataTypeChoreographyInformation and
				// it is also the receiver participant		
				// wsdl is the wsdl of the sender participant
				wsdl = 	wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getWsdl();
				// set operationName to choreography task name
				operationName = choreographyMessageInformation.getChoreographyTaskName();
				// set prefix 
				prefix = wsdlDefinitions.get(choreographyMessageInformation.getSenderParticipantName())
						.getPrefix();
				// set variable name
				variableName = BPELUtils.createOutputVariableName(choreographyMessageInformation
						.getSenderParticipantName(), operationName);						
				// data type is contained into the output message of the sender participant operation
				isDataTypeContainedIntoInputMessage = false;		
			}	
			// get data type information of WSDL
			DataTypeWSDLInformation dataTypeWSDLInformation = WSDLUtils.getDataTypeWSDLInformation(wsdl,
					operationName, choreographyMessageInformation.getDataTypeName(),
					isDataTypeContainedIntoInputMessage);
			// create messageTypeVariable
			Variable messageTypeVariable = BPELUtils.getOrCreateVariableForMessageType(process,
					variableName, prefix, dataTypeWSDLInformation.getMessageName());
			// create message variable XPath expression
			String messageVariableXPathExpression = null;
			if(dataTypeWSDLInformation.isMainElementOfMessage()){
				// create an xpath expression of the form  
				// $variableName.partName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).toString();
			}
			else{
				// create an xpath expression of the form  
				// $variableName.partName/elementName
				messageVariableXPathExpression = new StringBuilder(BPEL_VARIABLE_ACCESSOR_SYMBOL)
						.append(messageTypeVariable.getName()).append(BPEL_MESSAGE_PART_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getPartName()).append(SLASH_SEPARATOR_SYMBOL)
						.append(dataTypeWSDLInformation.getElementName()).toString();			
			}
			// replace messageName with messageVariableXPath into conditionBody			
			conditionBody = conditionBody.replace(messageName, messageVariableXPathExpression);
			
		}			
		// convert all Left double quotation mark and Right double quotation mark into "
		conditionBody = BPELUtils.convertDoubleQuotes(conditionBody);		
		// create condition		
		Condition condition = BPELFactory.eINSTANCE.createCondition();		
		condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
		condition.setBody(conditionBody);
		
		// log execution flow
		LOGGER.exit();		
		
		return condition;
	}
		
	public static Assign createAssignCDClientReply(Process process, WSDLDefinitionData cdWSDLDefinitionData, 
			OperationData replyCDOperationData, Variable replyCDVariable,
			OperationData messageDataOperationData, Variable messageDataVariable,
			boolean isMessageDataVariableOfCDProsumer){
		
		// log execution flow
		LOGGER.entry("Reply CD Operation name: "+replyCDOperationData.getName()+
				"Reply CD Variable name: "+replyCDVariable.getName());		
		
		// create assign
		Assign assignReply = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignReply.setName(BPELUtils.createAssignName(replyCDVariable.getName()));
		assignReply.setValidate(false);	
		
		// create copy for the initialization of the variable
		Copy copyInitialize = BPELFactory.eINSTANCE.createCopy();
		// create from for copyInitialize
		From fromInitialize = BPELFactory.eINSTANCE.createFrom();
		// set from to the string representing the initialization of the message
		fromInitialize.setLiteral(WSDLUtils.generateInitializationMessageString(cdWSDLDefinitionData
				.getWsdl(), replyCDOperationData.getOutputMessageData().getElementName()));
		// set fromInitialize to copyInitialize
		copyInitialize.setFrom(fromInitialize);
		// create to for copyInitialize
		To toInitialize = BPELFactory.eINSTANCE.createTo();
		toInitialize.setVariable(replyCDVariable);
		// create part for toInitialize
		Part partToInitialize = WSDLFactory.eINSTANCE.createPart();
		partToInitialize.setName(replyCDOperationData.getOutputMessageData().getPartName());
		// set partToInitialize to toInitialize
		toInitialize.setPart(partToInitialize);
		// set toInitialize to copyInitialize		
		copyInitialize.setTo(toInitialize);
		// add copyInitialize to assignSendInput
		assignReply.getCopy().add(copyInitialize);	
			
		// get or create choreographyIDVariable
		Variable choreographyIDVariable = BPELUtils.getOrCreateStringVariable(process,
				CHOREOGRAPHY_ID_VARIABLE_NAME);				
	
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();
		// set variable for fromChoreographyId
		fromChoreographyId.setVariable(choreographyIDVariable);
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		
		// create part for output of copyChoreographyId
		Part partOutputChoreographyIdOutput = WSDLFactory.eINSTANCE.createPart();	
		partOutputChoreographyIdOutput.setName(replyCDOperationData.getInputMessageData().getPartName());	
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		// set partOutputMessageDataOutput to toChoreographyId
		toChoreographyId.setPart(partOutputChoreographyIdOutput);
		// set variable to toChoreographyId
		toChoreographyId.setVariable(replyCDVariable);
		
		// create query for queryToChoreographyId
		Query queryToChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryToChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);
		// set queryToChoreographyId to toChoreographyId
		toChoreographyId.setQuery(queryToChoreographyId);
		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);	
		
		// add copyChoreographyId to assignReply
		assignReply.getCopy().add(copyChoreographyId);		
		
		// create copy for messageData
		Copy copyMessageData = BPELFactory.eINSTANCE.createCopy();	
		// create part for input of copyOutputMessageData
		Part partOutputMessageDataInput = WSDLFactory.eINSTANCE.createPart();
		partOutputMessageDataInput.setName(messageDataOperationData.getInputMessageData().getPartName());
		// create from for outputMessageData
		From fromOutputMessageData = BPELFactory.eINSTANCE.createFrom();
		// set partOutputMessageDataInput to fromOutputMessageData
		fromOutputMessageData.setPart(partOutputMessageDataInput);
		fromOutputMessageData.setVariable(messageDataVariable);	
		// create query for fromOutputMessageData
		Query queryFromDataItem = BPELFactory.eINSTANCE.createQuery();
		queryFromDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		
		// check if message data variable belong to a CD prosumer
		if(isMessageDataVariableOfCDProsumer){
		// message data variable belongs to a CD prosumer
		// set querytoDataItem to outputMessageData	
			queryFromDataItem.setValue(XPATH_QUERY_OUTPUT_MESSAGE_DATA);			
		}
		else{
		// message data variable belong to a CD client
		// set querytoDataItem to messageData 		
			queryFromDataItem.setValue(XPATH_QUERY_MESSAGE_DATA);			
		}
		
		// set queryFromDataItem to fromOutputMessageData
		fromOutputMessageData.setQuery(queryFromDataItem);
		// set from of copyOutputMessageData to fromOutputMessageData
		copyMessageData.setFrom(fromOutputMessageData);
		
		// create part for output of copyOutputMessageData
		Part partOutputMessageDataOutput = WSDLFactory.eINSTANCE.createPart();
	    partOutputMessageDataOutput.setName(replyCDOperationData.getInputMessageData().getPartName());				
	    // create to for copyOutputMessageData
		To toOutputMessageData = BPELFactory.eINSTANCE.createTo();	
		// set partOutputMessageDataOutput to toOutputMessageData
		toOutputMessageData.setPart(partOutputMessageDataOutput);
		// set variable to toDataItem
		toOutputMessageData.setVariable(replyCDVariable);
		
		// create query for toOutputMessageData
		Query queryToDataItem = BPELFactory.eINSTANCE.createQuery();
		queryToDataItem.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryToDataItem.setValue(XPATH_QUERY_MESSAGE_DATA);
		// set queryToDataItem to toOutputMessageData
		toOutputMessageData.setQuery(queryToDataItem);	
		
		// set to of copyDataItem to toDataItem		
		copyMessageData.setTo(toOutputMessageData);
		
		// add copyDataItem to assignReply
		assignReply.getCopy().add(copyMessageData);
		
		// log Assing information
		LOGGER.info("Assign: "+assignReply.getName()+" created!");
		// log exectution flow
		LOGGER.exit();
	
		return assignReply;
	}
	
	public static void createCDProsumerLoopActivities(FlowNode flowNode, Process process,
			String cdParticipantName, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdProsumerWSDLDefinitionData,
			WSDLDefinitionData prosumerPartWSDLDefinitionData, List<Choreography> choreographies,
			Deque<org.eclipse.bpel.model.Sequence> sequences, Deque<BPELExtensibleElement> bpelElements,
			boolean createAssignLoopVariable) {
		
		// log execution flow
		LOGGER.entry("CD Prosumer loop activities for flow node: "+flowNode.getName());	
	
		// create a sequence 
		org.eclipse.bpel.model.Sequence sequenceLoop = BPELFactory.eINSTANCE.createSequence();		
		
		// check if the loop has a numeric expression
		if(BPMNUtils.getLoopNumericExpression(flowNode) != null){
			// the loop has a numeric expression	
			// create the variable for the for each counter
			Variable varForEach = BPELFactory.eINSTANCE.createVariable();
			varForEach.setName(StringUtils.deleteWhitespace(flowNode.getName()));

			// create ForEach
			ForEach forEach = BPELFactory.eINSTANCE.createForEach();
			forEach.setParallel(false);
			forEach.setCounterName(varForEach);
			forEach.setName(BPELUtils.createForEachName(flowNode.getName()));
			// create for each counter start expression
			Expression startExpression = BPELFactory.eINSTANCE.createExpression();
			startExpression.setBody(START_COUNTER_VALUE);
			startExpression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
			// set for each counter start value
			forEach.setStartCounterValue(startExpression);
			// set for each counter final value
			forEach.setFinalCounterValue(BPELUtils.createCDProsumerXPathExpression(BPMNUtils
					.getLoopNumericExpression(flowNode), process, cdParticipantName, wsdlDefinitions,
					cdProsumerWSDLDefinitionData, prosumerPartWSDLDefinitionData, choreographies));
			// create scope
			Scope scope = BPELFactory.eINSTANCE.createScope();
			// set scope as the activity of the ForEach 
			forEach.setActivity(scope);		
			// set the sequenceLoop as the activity of the scope 
			scope.setActivity(sequenceLoop);	
			// attach the ForEach just created to the last sequence of sequences
			sequences.getLast().getActivities().add(forEach);					
			// add the ForEach as the last element of bpelElements 
			bpelElements.addLast(forEach);
			
			// log for each creation
			LOGGER.info("Created forEach for the choreography task: "+flowNode.getName());
		}
		
		// check if the loop has a conditional expression
		if(BPMNUtils.getLoopConditionalExpression(flowNode) != null){
			// the loop has a conditional expression
			// create while 
			While whileLoop = BPELFactory.eINSTANCE.createWhile();
			whileLoop.setName(BPELUtils.createWhileName(flowNode.getName()));
			// create condition 
			Condition condition = BPELFactory.eINSTANCE.createCondition();
			condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
			// set condition body
			condition.setBody(BPELUtils.createCDProsumerXPathExpression(BPMNUtils
				.getLoopConditionalExpression(flowNode), process, cdParticipantName, wsdlDefinitions,
					cdProsumerWSDLDefinitionData, prosumerPartWSDLDefinitionData, choreographies).getBody());
			// set condition of whileLoop
			whileLoop.setCondition(condition);
			// set the sequenceLoop as the activity of the whileLoop
			whileLoop.setActivity(sequenceLoop);
			// attach the while just created to the last sequence of sequences
			sequences.getLast().getActivities().add(whileLoop);					
			// add the while as the last element of bpelElements 
			bpelElements.addLast(whileLoop);
			
			// log while creation
			LOGGER.info("Created While for the choreography task: "+flowNode.getName());
		}
		
		// check if the assign of the loop variable has to be created
		if(createAssignLoopVariable) {
			// the assign of the loop variable has to be created
			// create assign for loop variable 
			Assign assignLoopVariable = BPELUtils.createAssignLoopVariable(process,
					StringUtils.deleteWhitespace(flowNode.getName()));
			// add assignLoopVariable to sequenceForEach
			sequenceLoop.getActivities().add(assignLoopVariable);
		}
			
		// add to sequences the sequence sequenceForEach as last element
		sequences.addLast(sequenceLoop);	
		
		// log execution flow
		LOGGER.exit();			
		
	}
	
	public static void createCDClientLoopActivities(FlowNode flowNode, Process process,
			String cdParticipantName, Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdClientWSDLDefinitionData, List<Choreography> choreographies,
			Deque<org.eclipse.bpel.model.Sequence> sequences, Deque<BPELExtensibleElement> bpelElements) {
		
		// log execution flow
		LOGGER.entry("CD Prosumer loop activities for flow node: "+flowNode.getName());	
	
		// create a sequence 
		org.eclipse.bpel.model.Sequence sequenceLoop = BPELFactory.eINSTANCE.createSequence();		
		
		// check if the loop has a numeric expression
		if(BPMNUtils.getLoopNumericExpression(flowNode) != null){
			// the loop has a numeric expression	
			// create the variable for the for each counter
			Variable varForEach = BPELFactory.eINSTANCE.createVariable();
			varForEach.setName(StringUtils.deleteWhitespace(flowNode.getName()));

			// create ForEach
			ForEach forEach = BPELFactory.eINSTANCE.createForEach();
			forEach.setParallel(false);
			forEach.setCounterName(varForEach);
			forEach.setName(BPELUtils.createForEachName(flowNode.getName()));
			// create for each counter start expression
			Expression startExpression = BPELFactory.eINSTANCE.createExpression();
			startExpression.setBody(START_COUNTER_VALUE);
			startExpression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
			// set for each counter start value
			forEach.setStartCounterValue(startExpression);
			// set for each counter final value
			forEach.setFinalCounterValue(BPELUtils.createCDClientXPathExpression(BPMNUtils
					.getLoopNumericExpression(flowNode), process, cdParticipantName, wsdlDefinitions,
					cdClientWSDLDefinitionData, choreographies));			
			// create scope
			Scope scope = BPELFactory.eINSTANCE.createScope();
			// set scope as the activity of the ForEach 
			forEach.setActivity(scope);		
			// set the sequenceLoop as the activity of the scope 
			scope.setActivity(sequenceLoop);	
			// attach the ForEach just created to the last sequence of sequences
			sequences.getLast().getActivities().add(forEach);					
			// add the ForEach as the last element of bpelElements 
			bpelElements.addLast(forEach);
			
			// log for each creation
			LOGGER.info("Created forEach for the choreography task: "+flowNode.getName());
		}
		
		// check if the loop has a conditional expression
		if(BPMNUtils.getLoopConditionalExpression(flowNode) != null){
			// the loop has a conditional expression
			// create while 
			While whileLoop = BPELFactory.eINSTANCE.createWhile();
			whileLoop.setName(BPELUtils.createWhileName(flowNode.getName()));
			// create condition 
			Condition condition = BPELFactory.eINSTANCE.createCondition();
			condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
			// set condition body
			condition.setBody(BPELUtils.createCDClientXPathExpression(BPMNUtils
				.getLoopConditionalExpression(flowNode), process, cdParticipantName, wsdlDefinitions,
				cdClientWSDLDefinitionData, choreographies).getBody());			
			// set condition of whileLoop
			whileLoop.setCondition(condition);
			// set the sequenceLoop as the activity of the whileLoop
			whileLoop.setActivity(sequenceLoop);
			// attach the while just created to the last sequence of sequences
			sequences.getLast().getActivities().add(whileLoop);					
			// add the while as the last element of bpelElements 
			bpelElements.addLast(whileLoop);
			
			// log while creation
			LOGGER.info("Created While for the choreography task: "+flowNode.getName());
		}
					
		// add to sequences the sequence sequenceForEach as last element
		sequences.addLast(sequenceLoop);	
		
		// log execution flow
		LOGGER.exit();			
		
	}	
	
	public static void createTimerActivities(TimerEventDefinition timerEventDefinition,
			Deque<org.eclipse.bpel.model.Sequence> sequences, Deque<BPELExtensibleElement> bpelElements) {
		
		// log execution flow
		LOGGER.entry();
		
		// create a sequence 
		org.eclipse.bpel.model.Sequence sequenceLoop = BPELFactory.eINSTANCE.createSequence();	
		// create while 
		While whileLoop = BPELFactory.eINSTANCE.createWhile();
		whileLoop.setName(BPELUtils.createWhileName(timerEventDefinition.getId()));
		// create condition 
		Condition condition = BPELFactory.eINSTANCE.createCondition();
		condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
		// set condition body
		condition.setBody(QUOTATION_MARK+TRUE_VALUE+QUOTATION_MARK);
		// set condition of whileLoop
		whileLoop.setCondition(condition);
		// set the sequenceLoop as the activity of the whileLoop
		whileLoop.setActivity(sequenceLoop);
		// attach the while just created to the last sequence of sequences
		sequences.getLast().getActivities().add(whileLoop);					
		// add the while as the last element of bpelElements 
		bpelElements.addLast(whileLoop);
		
		// log while creation
		LOGGER.info("Created timer activities for the timer: "+timerEventDefinition.getId());

		// add to sequences the sequence sequenceForEach as last element
		sequences.addLast(sequenceLoop);
		
		// log execution flow
		LOGGER.exit();		
	}
	
	public static void createWaitWithForExpression(String forExpressionValue, Sequence sequence) {
		
		// log execution flow
		LOGGER.entry();	
		
		// create wait
		Wait wait = BPELFactory.eINSTANCE.createWait();
		// create waitForExpression
		Expression waitForExpression = BPELFactory.eINSTANCE.createExpression();
		waitForExpression.setBody(QUOTATION_MARK+forExpressionValue+QUOTATION_MARK);
		// set waitForExpression as for of wait
		wait.setFor(waitForExpression);
		
		// add wait to the activities of the sequence
		sequence.getActivities().add(wait);
		
		// log execution flow
		LOGGER.exit();	
	}
	
	public static String convertDoubleQuotes(String conditionBody) {
		
		// convert all Left double quotation mark and Right double quotation mark into "
		return conditionBody.replaceAll("\\u201C", "\"").replaceAll("\\u201D", "\"");
	}
		
	public static String createForEachName(String name){
		
		return new StringBuilder(FOR_EACH_PREFIX).append(StringUtils.deleteWhitespace(name)).toString();
	}

	public static String createWhileName(String name){
		
		return new StringBuilder(WHILE_PREFIX).append(StringUtils.deleteWhitespace(name)).toString();
	}	
	
	public static String createIfName(String name){
		
		return StringUtils.deleteWhitespace(name).replaceAll("[^\\p{L}\\p{Nd}]+", "");
	}
	
	public static String createCDBpelProcessTargetNamespace(String choreographyTargetNamespace,
			String cdName){
		
		return new StringBuilder(choreographyTargetNamespace).append(SLASH_SEPARATOR_SYMBOL)
				.append(CD_LABEL_NS).append(SLASH_SEPARATOR_SYMBOL).append(cdName)
				.append(SLASH_SEPARATOR_SYMBOL).append(PROCESS_LABEL_NS).toString();
	}
		
	public static String createInputVariableName(String baseName,String operationName){
		
		return new StringBuilder(StringUtils.uncapitalize(StringUtils.deleteWhitespace(baseName)))
				.append(UNDERSCORE).append(StringUtils.deleteWhitespace(operationName))
				.append(UNDERSCORE).append(INPUT_LABEL).toString();
	}	

	public static String createOutputVariableName(String baseName, String operationName){
		
		return new StringBuilder(StringUtils.uncapitalize(StringUtils.deleteWhitespace(baseName)))
				.append(UNDERSCORE).append(StringUtils.deleteWhitespace(operationName))
				.append(UNDERSCORE).append(OUTPUT_LABEL).toString();
	}

	public static String createParticipantVariableName(String participantName){
		
		return new StringBuilder(participantName).append(UNDERSCORE)
				.append(PARTICIPANT_NAME_VARIABLE_NAME_SUFFIX).toString();
	}
	
	public static String createParticipantPartnerLinkAddressVariableName(String participantName){
		
		return new StringBuilder(participantName).append(UNDERSCORE)
				.append(PARTICIPANT_PARTNER_LINK_ADDRESS_VARIABLE_NAME_SUFFIX).toString();
	}
	
	public static String createAssignName(String baseName){
		
		return new StringBuilder(ASSIGN_NAME_PREFIX).append(UNDERSCORE)
				.append(StringUtils.deleteWhitespace(baseName)).toString();
	}
	
	public static String createLoopVariableName(String baseName){
		
		return new StringBuilder(LOOP_NAME_PREFIX).append(UNDERSCORE)
				.append(StringUtils.deleteWhitespace(baseName)).toString();
	}
	
	public static String createInvokeName(String baseName,String operationName){
		
		return new StringBuilder(INVOKE_NAME_PREFIX).append(UNDERSCORE).append(StringUtils
				.uncapitalize(StringUtils.deleteWhitespace(baseName))).append(UNDERSCORE)
				.append(StringUtils.deleteWhitespace(operationName)).toString();
	}

	public static String createReceiveName(String baseName,String operationName){
		
		return new StringBuilder(RECEIVE_NAME_PREFIX).append(UNDERSCORE).append(StringUtils
				.uncapitalize(StringUtils.deleteWhitespace(baseName))).append(UNDERSCORE)
				.append(StringUtils.deleteWhitespace(operationName)).toString();
	}
	
	public static String createSequenceName(String name){
		
		return new StringBuilder(SEQUENCE_NAME_PREFIX).append(UNDERSCORE).append(name).toString();
	}	

	public static String createSequenceIfName(String name){
		
		return new StringBuilder(SEQUENCE_NAME_PREFIX).append(UNDERSCORE).append(IF_NAME_PREFIX)
				.append(UNDERSCORE).append(name).toString();
	}
	
	public static String createSequenceFlowName(String name){
		
		return new StringBuilder(SEQUENCE_NAME_PREFIX).append(UNDERSCORE).append(FLOW_NAME_PREFIX)
				.append(UNDERSCORE).append(name).toString();
	}
	
	public static String createReplyName(String name){
		
		return new StringBuilder(REPLY_NAME_PREFIX).append(UNDERSCORE).append(name).toString();
	}	
	
	public static String createPartnerLinkName(String baseName, String name){
		
		return new StringBuilder(baseName).append(UNDERSCORE).append(name).toString();
	}
	
	public static String createPartnerLinkNameFromPartnerLinkTypeName(String partnerLinkTypeName){
		
		return partnerLinkTypeName.replace(PLNKTYPE_SUFFIX, "");
	}
	
	public static String formatParticipantNameForPartnerLinkTypeName(String participantName){
		
		return StringUtils.remove(StringUtils.deleteWhitespace(participantName),HYPHEN);
	}
	
	public static String createPartnerLinkTypeName(String participantName){
		
		return new StringBuilder(StringUtils.capitalize(BPELUtils
				.formatParticipantNameForPartnerLinkTypeName(participantName))).append(PLNKTYPE_SUFFIX)
				.toString();
	}
	
	public static String createCDPartnerLinkTypeName(String participantName){
		
		return new StringBuilder(CDNAME_PARTNER_LINK_TYPE_NAME_PREFIX).append(StringUtils
				.capitalize(BPELUtils.formatParticipantNameForPartnerLinkTypeName(participantName)))
				.append(PLNKTYPE_SUFFIX).toString();
	}
	
	public static String createProcessNameDeploy(String baseName,String name){
		
		return new StringBuilder(baseName).append(COLON_LABEL).append(name).toString();
	}
			
	public static PartnerLink getPartnerLinkOrCreateIfNotExist(String partnerLinkName, String taskName,
			Process process, PartnerLinkTypeData partnerLinkTypeData, ArtifactData artifactData,
			boolean myrole){

		// log execution flow
		LOGGER.entry("Partner Link Name: "+partnerLinkName+" Partner Link type name: "
				+partnerLinkTypeData.getName()+" Role: "+partnerLinkTypeData.getRoleName()
				+" My role: "+myrole);
		
		PartnerLink ptnl = null;
		for (PartnerLink partnerLink : process.getPartnerLinks().getChildren()) {
			// check if the partner link corresponding to the partner link type in input exist
			// inside the process
			if(Utils.getLocalPartXMLQName(partnerLink.getPartnerLinkType().getName())
					.equals(partnerLinkTypeData.getName())){
				
				// a partner link with myRole exist and has the same value of the role name of 
				// partnerLinkTypeData
				// or
				// a partner link with partnerRole exist and has the same value of the role name of 
				// partnerLinkTypeData
				// or 
				// a partner link with partnerRole is needed and it exist a partner link with myRole 
				// with the same role as partnerLinkTypeData
				if((myrole && partnerLink.getMyRole() != null && (partnerLink.getMyRole().getName()
						.equals(partnerLinkTypeData.getRoleName()))) ||
				   (!myrole && partnerLink.getPartnerRole() != null && (partnerLink.getPartnerRole()
						   .getName().equals(partnerLinkTypeData.getRoleName()))) ||
				   (!myrole && partnerLink.getMyRole() != null && (partnerLink.getMyRole().getName()
						   .equals(partnerLinkTypeData.getRoleName())))){
					
					ptnl = partnerLink;
					// log Partner Link Type information
					LOGGER.info("A Partner Link corresponding to the Partner Link Type: "
							+partnerLinkTypeData.getName()+" exist!"+" Partner Link name: "+ptnl.getName());
					// log execution flow
					LOGGER.exit();
					return ptnl;
				}				
				// a partner link with myRole is needed and in the partnerlink found myRole is not defined
				if(myrole && (partnerLink.getMyRole() == null)){
					ptnl = partnerLink;
					Role role = PartnerlinktypeFactory.eINSTANCE.createRole();
					role.setName(partnerLinkTypeData.getRoleName());				
					ptnl.setMyRole(role);
					
					// log Partner Link Type information
					LOGGER.info("A Partner Link corresponding to the Partner Link Type: "
							+partnerLinkTypeData.getName()
							+" already exist! My Role set! Partner Link name: "+ptnl.getName());
					// log execution flow
					LOGGER.exit();
					
					return ptnl;
				}
				// a partner link with partnerRole is needed and in the partnerlink found partnerRole 
				// is not defined
				if(!myrole && (partnerLink.getPartnerRole() == null)){
					ptnl = partnerLink;
					Role role = PartnerlinktypeFactory.eINSTANCE.createRole();
					role.setName(partnerLinkTypeData.getRoleName());				
					ptnl.setPartnerRole(role);	
					
					// log Partner Link Type information
					LOGGER.info("A Partner Link corresponding to the Partner Link Type: "
							+partnerLinkTypeData.getName()
							+" exist! Partner Role set!"+" Partner Link name: "+ptnl.getName());
					// log execution flow
					LOGGER.exit();
					
					return ptnl;
				}
			}
		}
		// the partnerLink does not exist 
		if(ptnl == null){
			ptnl = BPELFactory.eINSTANCE.createPartnerLink();
			PartnerLinkType ptnlt = PartnerlinktypeFactory.eINSTANCE.createPartnerLinkType();
			ptnlt.setName(Utils.createXMLQNameString(artifactData.getPrefix(), partnerLinkTypeData
					.getName()));
			ptnl.setPartnerLinkType(ptnlt);
			Role role = PartnerlinktypeFactory.eINSTANCE.createRole();
			role.setName(partnerLinkTypeData.getRoleName());
			if(myrole){
				ptnl.setMyRole(role);
				ptnl.setName(partnerLinkName);
			}	
			else{
				ptnl.setPartnerRole(role);
				ptnl.setName(BPELUtils.createPartnerLinkNameFromPartnerLinkTypeName(partnerLinkTypeData
						.getName()));
			}	
			// add partnerLink to process
			process.getPartnerLinks().getChildren().add(ptnl);
		}
		
		// log Partner Link Type information
		LOGGER.info("Partner Link corresponding to the Partner Link Type: "+partnerLinkTypeData.getName()
				+" created!"+" Partner Link name: "+ptnl.getName());
		// log execution flow
		LOGGER.exit();
		
		return ptnl;
	}
	
	public static Variable getOrCreateVariableForMessageType(Process process, String variableName,
			String messagePrefix, String messageTypeName){
	
		// log execution flow
		LOGGER.entry("Variable name: "+variableName+" Message Type name: "+messageTypeName
				+" Message Prefix: "+messagePrefix);
		
		for (Variable variable : process.getVariables().getChildren()) {
			if(variable.getMessageType() != null){
				if(variable.getMessageType().getQName().getNamespaceURI().equals(messagePrefix) && 
				   variable.getMessageType().getQName().getLocalPart().equals(messageTypeName)){
					// log execution flow
					LOGGER.exit("A variable of message type "+messageTypeName+" already exists!");
					return variable;
				}
			}
		}
		// create messageVariable
		Message messageVariable = WSDLFactory.eINSTANCE.createMessage();
		messageVariable.setQName(new QName(messagePrefix, messageTypeName));
		// create variableNew
		Variable variable = BPELFactory.eINSTANCE.createVariable();
		variable.setName(StringUtils.deleteWhitespace(variableName));
		// set Message Type
		variable.setMessageType(messageVariable);
		// add variableNew to process
		process.getVariables().getChildren().add(variable);
		
		// log Variable information
		LOGGER.info("Created variable "+variableName+" for message type "+messageTypeName);
		// log execution flow
		LOGGER.exit();
		
		return variable;
	}
	
	public static Variable getOrCreateStringVariable(Process process, String variableName){
		
		// log execution flow
		LOGGER.entry();

		String variableSearchedName = StringUtils.deleteWhitespace(variableName);
		Variable stringVariable = null;
		// check if stringVariable variable already exists
		for (Variable variable : process.getVariables().getChildren()) {
			if(variable.getName().equals(variableSearchedName)){
				// log execution flow
				LOGGER.exit("A String variable with name "+variableSearchedName+" already exists!");
				return variable;
			}
		}
		// stringVariable variable doesn't exist
		// create stringVariable variable
		XSDSchema schemaForSchemas = XSDUtil.getSchemaForSchema(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
		XSDTypeDefinition xsdTypeDefinition = schemaForSchemas
				.resolveSimpleTypeDefinition(XSD_STRING_TYPE_NAME);
		xsdTypeDefinition.setTargetNamespace(XSD_PREFIX);
		stringVariable = BPELFactory.eINSTANCE.createVariable();
		stringVariable.setName(variableSearchedName);
		stringVariable.setType(xsdTypeDefinition);	
		
		// add stringVariable variable to process
		process.getVariables().getChildren().add(stringVariable);

		// log execution flow
		LOGGER.exit("Created String variable with name "+variableName);
		// log execution flow
		LOGGER.exit();
		
		return stringVariable;
	}
	
	public static Variable getVariableFromName(Process process, String variableName){
		
		// log execution flow
		LOGGER.entry("Variable name: "+variableName);

		for (Variable variable : process.getVariables().getChildren()) {
			if(variable.getName().equals(variableName)){
				// log execution flow
				LOGGER.exit("Variable "+variableName+" found!");
				return variable;
			}
		}		
		
		// log execution flow
		LOGGER.exit("Variable "+variableName+" not found!");		
		// log execution flow
		LOGGER.exit();
		
		return null;
	}
	
	public static Variable getVariableFromMessageType(Process process, String messageTypeName){
		
		// log execution flow
		LOGGER.entry("Message Type: "+messageTypeName);
		
		for (Variable variable : process.getVariables().getChildren()) {
			if(variable.getMessageType() != null){
				if(variable.getMessageType().getQName().getLocalPart().equals(messageTypeName)){
					// log execution flow
					LOGGER.exit("Variable of Message Type"+messageTypeName+" found!");	
					return variable;
				}
			}
		}		
		
		// log execution flow
		LOGGER.exit("Variable of Message Type"+messageTypeName+" not found!");			
		// log execution flow
		LOGGER.exit();	
		
		return null;
	}
	
	public static Assign createAssignChoreographyIDVariableFromCurrentDataTime(Process process){
		
		// log execution flow
		LOGGER.entry();
		
		// get or create choreographyIDVariable
		Variable choreographyIDVariable = BPELUtils
				.getOrCreateStringVariable(process, CHOREOGRAPHY_ID_VARIABLE_NAME);
		// create assign for choreographyIDVariable
		Assign assignChoreographyID = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignChoreographyID.setName(BPELUtils.createAssignName(CHOREOGRAPHY_ID_VARIABLE_NAME));		
		assignChoreographyID.setValidate(false);
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();			
		// create expression for fromChoreographyId
		Expression expression = BPELFactory.eINSTANCE.createExpression();
		expression.setExpressionLanguage(XPATH_2_0_QUERY_LANGUAGE);	
		expression.setBody(XPATH_EXPRESSION_CURRENT_DATE_TIME_AS_STRING_ONLY_NUMBERS);
		// set expression to fromChoreographyId
		fromChoreographyId.setExpression(expression);
		// set fromChoreographyId of copyChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(choreographyIDVariable);		
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);				
		// add copyChoreographyId to assignReceiveInput
		assignChoreographyID.getCopy().add(copyChoreographyId);	
			
		// log execution flow
		LOGGER.exit();
		
		return assignChoreographyID;	
	}
		
	public static Assign createAssignChoreographyIDVariableFromVariable(Process process,
			MessageData inputMessageData, Variable inputMessageVariable){
		
		// log execution flow
		LOGGER.entry("Create Assign to initialize ChoreographyID Variable with message: "
				+inputMessageData.getName());
		
		// get or create choreographyIDVariable
		Variable choreographyIDVariable = BPELUtils.getOrCreateStringVariable(process,
				CHOREOGRAPHY_ID_VARIABLE_NAME);				
		// create assign for choreographyIDVariable
		Assign assignChoreographyID = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name???
		assignChoreographyID.setName(BPELUtils.createAssignName(CHOREOGRAPHY_ID_VARIABLE_NAME));		
		assignChoreographyID.setValidate(false);		
		// create part of the message containing the choreographyID for copyChoreographyId		
		Part choreographyIdPart = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE.createPart();
		choreographyIdPart.setName(inputMessageData.getPartName());		
		// create copy for choreographyId
		Copy copyChoreographyId = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyChoreographyId
		From fromChoreographyId = BPELFactory.eINSTANCE.createFrom();
		// set choreographyIdPart to fromChoreographyId
		fromChoreographyId.setPart(choreographyIdPart);
		// set variable for fromChoreographyId
		fromChoreographyId.setVariable(inputMessageVariable);
		// create query for queryToChoreographyId
		Query queryFromChoreographyId = BPELFactory.eINSTANCE.createQuery();
		queryFromChoreographyId.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromChoreographyId.setValue(XPATH_QUERY_CHOREOGRAPHY_ID);
		// set queryFromChoreographyId to fromChoreographyId
		fromChoreographyId.setQuery(queryFromChoreographyId);
		// set from of copyChoreographyId to fromChoreographyId
		copyChoreographyId.setFrom(fromChoreographyId);
		// create to for copyChoreographyId
		To toChoreographyId = BPELFactory.eINSTANCE.createTo();
		toChoreographyId.setVariable(choreographyIDVariable);						
		// set to of copyChoreographyId to toChoreographyId
		copyChoreographyId.setTo(toChoreographyId);					
		// add copyChoreographyId to assignReceiveInput
		assignChoreographyID.getCopy().add(copyChoreographyId);	
				
		// log execution flow
		LOGGER.exit();
		
		return assignChoreographyID;
	}	
	
	public static Assign createAssignStringVariable(Process process, Variable variable, 
			String stringVariableValue){
		
		// log execution flow
		LOGGER.entry();
		
		// create assign for stringVariableName variable
		Assign assignVariable = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name??? because there will be more than one with the same name
		assignVariable.setName(BPELUtils.createAssignName(variable.getName()));			
		assignVariable.setValidate(false);		
		// create copy for stringVariableName assign
		Copy copyStringVariable = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyStringVariable
		From fromStringVariable = BPELFactory.eINSTANCE.createFrom();	
		fromStringVariable.setLiteral(stringVariableValue);
		// set fromStringVariable to copyStringVariable
		copyStringVariable.setFrom(fromStringVariable);
		// create to for stringVariableName assign
		To toStringVariableName = BPELFactory.eINSTANCE.createTo();
		toStringVariableName.setVariable(variable);	
		// set toStringVariableName to copyStringVariable
		copyStringVariable.setTo(toStringVariableName);		
		// add copyStringVariable to assignVariable
		assignVariable.getCopy().add(copyStringVariable);	
				
		// log execution flow
		LOGGER.exit();	
		
		return assignVariable;
	}
	
	public static Assign createAssignLoopVariable(Process process, String loopName){
		
		// log execution flow
		LOGGER.entry();
		
		// create or get loop variable
		Variable loopVariable = BPELUtils.getOrCreateStringVariable(process,
				BPELUtils.createLoopVariableName(loopName));
		
		// create assign for loopVariable
		Assign assignLoopVariable = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name??? because there will be more than one with the same name
		assignLoopVariable.setName(BPELUtils.createAssignName(loopVariable.getName()));			
		assignLoopVariable.setValidate(false);		
		// create copy for loopVariable assign
		Copy copyLoopVariable = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyLoopVariable
		From fromLoopVariable = BPELFactory.eINSTANCE.createFrom();			
		// create expression for fromLoopVariable
		Expression fromExpression = BPELFactory.eINSTANCE.createExpression();
		fromExpression.setExpressionLanguage(XPATH_2_0_QUERY_LANGUAGE);				
		fromExpression.setBody(XPATH_CONCAT_OPERATOR_NAME+BRACKET_LEFT+QUOTATION_MARK+loopName
				+QUOTATION_MARK+COMMA_LABEL+QUOTATION_MARK+HASH+QUOTATION_MARK+COMMA_LABEL
				+BPEL_VARIABLE_ACCESSOR_SYMBOL+loopName+BRACKET_RIGHT);
		// set fromExpression as expression of fromLoopVariable
		fromLoopVariable.setExpression(fromExpression);
		// set fromLoopVariable to copyLoopVariable
		copyLoopVariable.setFrom(fromLoopVariable);
		// create to for loopVariable assign
		To toLoopVariable = BPELFactory.eINSTANCE.createTo();
		toLoopVariable.setVariable(loopVariable);	
		// set toLoopVariable to copyLoopVariable
		copyLoopVariable.setTo(toLoopVariable);		
		// add copyStringVariable to assignVariable
		assignLoopVariable.getCopy().add(copyLoopVariable);	
				
		// log execution flow
		LOGGER.exit();	
		
		return assignLoopVariable;
	}
	
	public static Assign createAssignLoopIndexes(Process process, String loopName,
			MessageData inputMessageData, Variable inputMessageVariable) {
		
		// log execution flow
		LOGGER.entry();		

		// create assign for loopVariable
		Assign assignLoopIndexes = BPELFactory.eINSTANCE.createAssign();	
		// TODO: is it mandatory to set the name??? because there will be more than one with the same name
		assignLoopIndexes.setName(BPELUtils.createAssignName(loopName));			
		assignLoopIndexes.setValidate(false);		
		// create part of the message containing loopIndexes for choreographyLoopIndexes		
		Part choreographyLoopIndexes = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE.createPart();
		choreographyLoopIndexes.setName(inputMessageData.getPartName());		
		// create copy for loopIndexes
		Copy copyLoopIndexes = BPELFactory.eINSTANCE.createCopy();		
		// create from for copyLoopIndexes
		From fromLoopIndexes = BPELFactory.eINSTANCE.createFrom();
		// set choreographyIdPart to fromLoopIndexes
		fromLoopIndexes.setPart(choreographyLoopIndexes);
		// set variable for fromLoopIndexes
		fromLoopIndexes.setVariable(inputMessageVariable);
		// create query for queryFromLoopIndexes
		Query queryFromLoopIndexes = BPELFactory.eINSTANCE.createQuery();
		queryFromLoopIndexes.setQueryLanguage(XPATH_1_0_QUERY_LANGUAGE);
		queryFromLoopIndexes.setValue(XPATH_QUERY_LOOP_INDEXES);
		// set queryFromLoopIndexes to fromLoopIndexes
		fromLoopIndexes.setQuery(queryFromLoopIndexes);		
		// set from of copyLoopIndexes to fromLoopIndexes
		copyLoopIndexes.setFrom(fromLoopIndexes);
		// get or create the variable related to the loop
		Variable variableLoop = BPELUtils.getOrCreateStringVariable(process, BPELUtils
							.createLoopVariableName(StringUtils.deleteWhitespace(loopName)));
		// create to for copyLoopIndexes
		To toLoopIndexes = BPELFactory.eINSTANCE.createTo();
		toLoopIndexes.setVariable(variableLoop);						
		// set to of copyLoopIndexes to toLoopIndexes
		copyLoopIndexes.setTo(toLoopIndexes);					
		// add copyLoopIndexes to assignLoopIndexes
		assignLoopIndexes.getCopy().add(copyLoopIndexes);		
		
		
		// log execution flow
		LOGGER.exit();	
		
		return assignLoopIndexes;
	}
	
	public static Assign createAssignFromPartnerLinkToVariable(Process process, PartnerLink partnerLink, 
			boolean partnerLinkPartnerRole, Variable participantNamePartnerLinkAddressVariable){
		
		// log execution flow
		LOGGER.entry();	
		
		// create assign for partnerLink
		Assign assignPartnerLink = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name??? because there will be more than one with the same name
		//assignPartnerLink.setName(BPELUtils.createAssignName(partnerLink.getName()));
		assignPartnerLink.setValidate(false);
		// create copy for partnerLink
		Copy copyPartnerLink = BPELFactory.eINSTANCE.createCopy();	
		// create from for partnerLink assign
		From fromPartnerLink = BPELFactory.eINSTANCE.createFrom();
		// set partnerLink to fromPartnerLink
		fromPartnerLink.setPartnerLink(partnerLink);
		if(partnerLinkPartnerRole){
			// set partnerRole to endpointReference of fromPartnerLink
			fromPartnerLink.setEndpointReference(EndpointReferenceRole.PARTNER_ROLE_LITERAL);
		}
		else{
			// set myRole to endpointReference of fromPartnerLink
			fromPartnerLink.setEndpointReference(EndpointReferenceRole.MY_ROLE_LITERAL);			
		}
		// set fromPartnerLink to copyPartnerLink
		copyPartnerLink.setFrom(fromPartnerLink);	
		// create to for partnerLink assign
		To toPartnerLink = BPELFactory.eINSTANCE.createTo();
		// set participantNamePartnerLinkAddressVariable to toPartnerLink
		toPartnerLink.setVariable(participantNamePartnerLinkAddressVariable);
		// set toPartnerLink to copyPartnerLink
		copyPartnerLink.setTo(toPartnerLink);		
		// add copyPartnerLink to assignPartnerLink			
		assignPartnerLink.getCopy().add(copyPartnerLink);
			
		// log execution flow
		LOGGER.exit();
		
		return assignPartnerLink;
	}

	public static Assign createAssignFromSIAManagerToPartnerLink(PartnerLink partnerLink, 
			Variable participantFromVariableName, Variable participantToVariableName, 
			Variable participantToPartnerLinkAddressVariableName){
		
		// log execution flow
		LOGGER.entry();		

		// create assign for partnerLink
		Assign assignPartnerLink = BPELFactory.eINSTANCE.createAssign();
		// TODO: is it mandatory to set the name??? because there will be more than one with the same name
		//assignPartnerLink.setName(BPELUtils.createAssignName(partnerLink.getName()));
		assignPartnerLink.setValidate(false);		
		// create copy for partnerLink
		Copy copyPartnerLink = BPELFactory.eINSTANCE.createCopy();	
		// create element for copyPartnerLink
		try {
			copyPartnerLink.setElement(DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument().createElement(ELEMENT_DEFAULT_NAME));
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
		}
		// add namespace declaration of SIA manager
		copyPartnerLink.getElement().setAttribute(XMLNS_LABEL+COLON_LABEL+SIA_MANAGER_PREFIX,
				SIA_MANAGER_NS_URI);		
		// create from for partnerLink assign
		From fromPartnerLink = BPELFactory.eINSTANCE.createFrom();
		// create expression for fromPartnerLink
		Expression expression = BPELFactory.eINSTANCE.createExpression();
		expression.setExpressionLanguage(XPATH_2_0_QUERY_LANGUAGE);		
		expression.setBody(SIA_MANAGER_PREFIX+COLON_LABEL+SIA_MANAGER_GET_ADDRESS_METHOD_NAME
				+BRACKET_LEFT+BPEL_VARIABLE_ACCESSOR_SYMBOL+participantFromVariableName.getName() 
				+COMMA_LABEL+BPEL_VARIABLE_ACCESSOR_SYMBOL+participantToVariableName.getName()
				+COMMA_LABEL+BPEL_VARIABLE_ACCESSOR_SYMBOL+participantToPartnerLinkAddressVariableName
				.getName()+BRACKET_RIGHT);
		// set expression to fromPartnerLink
		fromPartnerLink.setExpression(expression);		
		// set fromPartnerLink to copyPartnerLink
		copyPartnerLink.setFrom(fromPartnerLink);	
		// create to for partnerLink assign
		To toPartnerLink = BPELFactory.eINSTANCE.createTo();
		// set partnerLink to toPartnerLink
		toPartnerLink.setPartnerLink(partnerLink);
		// set toPartnerLink to copyPartnerLink
		copyPartnerLink.setTo(toPartnerLink);		
		// add copyPartnerLink to assignPartnerLink			
		assignPartnerLink.getCopy().add(copyPartnerLink);
				
		// log execution flow
		LOGGER.exit();		
		
		return assignPartnerLink;
	}
	
	
	public static void createCDClientInitiatingChoreographyTaskActivities(ChoreographyTask task,
			boolean isFirstChoreographyTaskMainChoreography, Participant cdParticipant, String cdName,
			Process process, Deque<org.eclipse.bpel.model.Sequence> sequences,
			Deque<BPELExtensibleElement> bpelElements, Map<String, WSDLDefinitionData> wsdlDefinitions,
			WSDLDefinitionData cdClientWSDLDefinitionData, WSDLDefinitionData receiverWSDLDefinitionData,
			ArtifactData artifactData, PropertyAliasesData propertyAliasesData){
		
		// log execution flow
		LOGGER.entry("CD Client Name: "+cdName," CD Client participant name: "+cdParticipant.getName()
		+" CD Client WSDL Definition Data: "+cdClientWSDLDefinitionData," Receiving WSDL Definition Data: "
		+receiverWSDLDefinitionData," Task name: "+task.getName());
		
		LOGGER.info("Creating CD "+cdName+" Client initiating activities for choreography task "
				+task.getName());
		
		String taskName = WSDLUtils.formatOperationName(task.getName());
		String choreographyInitiatingMessageName = BPMNUtils
				.getMessageNameChoreographyTaskSentFromParticipant(task, cdParticipant);
		String choreographyReturnMessageName = BPMNUtils
				.getMessageNameChoreographyTaskSentFromParticipant(task, cdParticipant);		
		// get onMessageOperationData
		OperationData onMessageOperationData = WSDLUtils
				.getOperationDataWSDLOperation(cdClientWSDLDefinitionData.getWsdl(), taskName);
		// create onMessageCDClientInputVariable
		Variable onMessageInputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
				.createInputVariableName(cdName, taskName), cdClientWSDLDefinitionData.getPrefix() 
				, onMessageOperationData.getInputMessageData().getName());
		// create prosumerOperationData
		OperationData prosumerOperationData = WSDLUtils
				.getOperationDataWSDLOperation(receiverWSDLDefinitionData.getWsdl(), taskName);
		// get receiver participant name
		String receiverParticipantName = BPMNUtils.getReceivingParticipant(task).getName();
		
		// get partner link data for on message
		PartnerLinkTypeData onMessagePartnerLinkTypeData = BPELUtils
				.getPartnerLinkTypeData(artifactData.getArtifact(), onMessageOperationData
						.getPortTypeName());		
		// create onMessagePartnerLink
		PartnerLink onMessagePartnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(WSDLUtils
				.formatParticipantNameForWSDL(cdName), taskName, process, onMessagePartnerLinkTypeData,
				artifactData, true);
			
		// create onMessage
		OnMessage onMessage = BPELFactory.eINSTANCE.createOnMessage();		
		// create onMessageSequence
		Sequence onMessageSequence = BPELFactory.eINSTANCE.createSequence();
		onMessageSequence.setName(BPELUtils.createSequenceName(WSDLUtils
				.formatOperationName(task.getName())));
		// set onMessageSequence as the activity of onMessage
		onMessage.setActivity(onMessageSequence);					
		// set onMessagePartnerLink to onMessage
		onMessage.setPartnerLink(onMessagePartnerLink);		
		// set onMessage variable
		onMessage.setVariable(onMessageInputVariable);
					
		// create onMessageOperation
		Operation onMessageOperation = WSDLFactory.eINSTANCE.createOperation();
		onMessageOperation.setName(taskName);		
		// set onMessage operation
		onMessage.setOperation(onMessageOperation);	
		// create onMessagePortType
		PortType onMessagePortType = WSDLFactory.eINSTANCE.createPortType();
		onMessagePortType.setQName(new QName(cdClientWSDLDefinitionData.getPrefix(), onMessageOperationData
				.getPortTypeName()));
		// set onMessage portType
		onMessage.setPortType(onMessagePortType);
		
		// log onMessage creation
		LOGGER.info("Created onMessage element for operation: "+onMessageOperation.getName());
		
		// create assign to initialize choreographyID Variable
		Assign assignInitializeChoreographyIDVariable = null;
		// check if the choreography task is the first of the main choreography
		if(isFirstChoreographyTaskMainChoreography) {
			// the choreography task is the first of the main choreography
			// create assign initialize choreographyID from current date time
			assignInitializeChoreographyIDVariable = BPELUtils
					.createAssignChoreographyIDVariableFromCurrentDataTime(process);
		}
		else {
			// the choreography task is not the first of the main choreography
			// create assign to initialize choreographyID Variable from onMessageInputVariable
			assignInitializeChoreographyIDVariable = BPELUtils
					.createAssignChoreographyIDVariableFromVariable(process, 
							onMessageOperationData.getInputMessageData(), onMessageInputVariable);
		}
		// add assignInitializeChoreographyIDVariable to onMessageSequence
		onMessageSequence.getActivities().add(assignInitializeChoreographyIDVariable);
		
		// get or create participant name variable
		Variable participantNameVariable = BPELUtils.getOrCreateStringVariable(process, 
				PARTICIPANT_NAME_VARIABLE_NAME);
		// create assign participantName Variable
		Assign assignParticipantNameVariable = BPELUtils.createAssignStringVariable(process,
				participantNameVariable, cdParticipant.getName());
		// add assignParticipantNameVariable to onMessageSequence
		onMessageSequence.getActivities().add(assignParticipantNameVariable);	
			
		Variable prosumerInputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
				.createInputVariableName(receiverWSDLDefinitionData.getParticipantName(), taskName), 
				receiverWSDLDefinitionData.getPrefix(), prosumerOperationData
				.getInputMessageData().getName());
				
		Variable prosumerOutputVariable = null;
		if(prosumerOperationData.getOutputMessageData() != null){			
			prosumerOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils
					.createOutputVariableName(receiverWSDLDefinitionData.getParticipantName(), taskName), 
					receiverWSDLDefinitionData.getPrefix(), prosumerOperationData
					.getOutputMessageData().getName());
		}	

		// get choreographyID variable
		Variable choreographyIDVariable = BPELUtils.getVariableFromName(process,
				CHOREOGRAPHY_ID_VARIABLE_NAME);
						
		// create assignInvokeProsumerInput
		Assign assignInvokeProsumerInput = BPELUtils.createAssignCDClientProsumerOperationInput(
				receiverWSDLDefinitionData, onMessageOperationData, onMessageInputVariable,
				prosumerOperationData, prosumerInputVariable, choreographyIDVariable,
				participantNameVariable, receiverParticipantName, task.getName(),
				choreographyInitiatingMessageName);		
		
		// add assignInvokeProviderInput to sequence
		onMessageSequence.getActivities().add(assignInvokeProsumerInput);		
		
		// get partner link
		PartnerLinkTypeData invokeProviderPartnerLinkTypeData = BPELUtils
				.getPartnerLinkTypeData(artifactData.getArtifact(), prosumerOperationData.getPortTypeName());
		PartnerLink invokeProviderPartnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(WSDLUtils
				.formatParticipantNameForWSDL(receiverWSDLDefinitionData.getParticipantName()),
				taskName, process, invokeProviderPartnerLinkTypeData, artifactData, false);
		
		// create Activities for SIA Manager for invoke		
		// get or create variable corresponding to receiverParticipantName
		Variable invokeParticipantNameVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantVariableName(receiverParticipantName));
		// create assignInvokeParticipantNameVariable
		Assign assignInvokeParticipantNameVariable = BPELUtils.createAssignStringVariable(process, 
				invokeParticipantNameVariable, receiverParticipantName);
		// add assignInvokeParticipantNameVariable to sequence
		onMessageSequence.getActivities().add(assignInvokeParticipantNameVariable);
		// get or create variable corresponding to invokePartnerLinkVariable
		Variable invokePartnerLinkAddressVariable = BPELUtils.getOrCreateStringVariable(process, 
				BPELUtils.createParticipantPartnerLinkAddressVariableName(receiverParticipantName));	
		// create assignPartnerLinkAddressVariable
		Assign assignPartnerLinkAddressVariable = BPELUtils.createAssignFromPartnerLinkToVariable(process, 
				invokeProviderPartnerLink, true, invokePartnerLinkAddressVariable);
		// add assignPartnerLinkAddressVariable to sequence
		onMessageSequence.getActivities().add(assignPartnerLinkAddressVariable);	
		// create assignFromSIAManagerToInvokePartnerLink
		Assign assignFromSIAManagerToInvokePartnerLink = BPELUtils
				.createAssignFromSIAManagerToPartnerLink(invokeProviderPartnerLink, participantNameVariable,
						invokeParticipantNameVariable, invokePartnerLinkAddressVariable);
		// add assignFromSIAManagerToInvokePartnerLink to sequence
		onMessageSequence.getActivities().add(assignFromSIAManagerToInvokePartnerLink);		
				
		// create provider Invoke
		Invoke invokeProsumer = BPELFactory.eINSTANCE.createInvoke();
		invokeProsumer.setName(BPELUtils.createInvokeName(receiverWSDLDefinitionData.getParticipantName(),
				taskName));
		// set partner link 
		invokeProsumer.setPartnerLink(invokeProviderPartnerLink);	
		// create operation
		Operation invokeProviderOperation = WSDLFactory.eINSTANCE.createOperation();
		invokeProviderOperation.setName(prosumerOperationData.getName());	
		// set operation name
		invokeProsumer.setOperation(invokeProviderOperation);	
		// set input variable
		invokeProsumer.setInputVariable(prosumerInputVariable);
		
		// log invoke creation
		LOGGER.info("Created invoke element for operation: "+invokeProviderOperation.getName());
		
		// the receiver participant of the choreography task is a prosumer
		// create correlation set for choreographyID
		CorrelationSet correlationSetChoreographyID = BPELFactory.eINSTANCE.createCorrelationSet();
		correlationSetChoreographyID.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
		// create correlation for choreographyID
		Correlation correlationChoreographyID = BPELFactory.eINSTANCE.createCorrelation();
		correlationChoreographyID.setSet(correlationSetChoreographyID);
		correlationChoreographyID.setInitiate(CORRELATION_INITIATE_JOIN_LABEL);
		// create invokeCorrelations
		Correlations invokeCorrelations = BPELFactory.eINSTANCE.createCorrelations();
		invokeCorrelations.getChildren().add(correlationChoreographyID);
		// set invokeCorrelations to invokeProsumer
		invokeProsumer.setCorrelations(invokeCorrelations);	
		// check if the interaction is request-response
		if(prosumerOutputVariable != null){
			// set output variable
			invokeProsumer.setOutputVariable(prosumerOutputVariable);
			// set correlation pattern request-response
			correlationChoreographyID.setPattern(CorrelationPattern.REQUESTRESPONSE_LITERAL);
		}
		// add invokeProvider to sequence
		onMessageSequence.getActivities().add(invokeProsumer);

		// create propertyAliasesMessageType for Input Message
		PropertyAliasesMessageType propertyAliasesInputMessageType = new PropertyAliasesMessageType(
				prosumerOperationData.getInputMessageData().getName(), prosumerOperationData
				.getInputMessageData().getPartName());
		// add propertyAliasesMessageType to propertyAliasesData
		Utils.addPropertyAliasesData(propertyAliasesData, receiverWSDLDefinitionData.getParticipantName(),
				receiverWSDLDefinitionData.getWsdlFileName(), receiverWSDLDefinitionData.getWsdl()
				.getTargetNamespace(), propertyAliasesInputMessageType);	
		
		// log creation of propertyAlias
		LOGGER.info("Created propertyAlias for message: "+prosumerOperationData.getInputMessageData()
			.getName());		
				
		if(prosumerOutputVariable != null){		
			// create propertyAliasesMessageType for Output Message
			PropertyAliasesMessageType propertyAliasesOutputMessageType = new PropertyAliasesMessageType(
					prosumerOperationData.getOutputMessageData().getName(), prosumerOperationData
					.getOutputMessageData().getPartName());
			// add propertyAliasesMessageType to propertyAliasesData
			Utils.addPropertyAliasesData(propertyAliasesData, receiverWSDLDefinitionData
					.getParticipantName(), receiverWSDLDefinitionData.getWsdlFileName(),
					receiverWSDLDefinitionData.getWsdl().getTargetNamespace(),
					propertyAliasesOutputMessageType);			
			
			// log creation of propertyAlias
			LOGGER.info("Created propertyAlias for message: "+prosumerOperationData.getOutputMessageData()
				.getName());
			
			// create onMessageOutputVariable 
			Variable onMessageOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process, 
					BPELUtils.createOutputVariableName(cdName,taskName), cdClientWSDLDefinitionData
					.getPrefix(), onMessageOperationData.getOutputMessageData().getName());			
			
			// create assign
			Assign assignReply = BPELUtils.createAssignCDClientReply(process, cdClientWSDLDefinitionData,
					onMessageOperationData, onMessageOutputVariable, prosumerOperationData,
					prosumerOutputVariable, true);
			
			// add assignReply to sequence
			onMessageSequence.getActivities().add(assignReply);	
			
			// create reply
			Reply reply = BPELFactory.eINSTANCE.createReply();
			reply.setName(BPELUtils.createReplyName(WSDLUtils.formatOperationName(task.getName())));
			reply.setOperation(onMessageOperation);
			reply.setPartnerLink(onMessagePartnerLink);
			reply.setPortType(onMessagePortType);
			reply.setVariable(onMessageOutputVariable);
			
			// log reply creation
			LOGGER.info("Created reply element for operation: "+onMessageOperation.getName());		
	
			// add reply to onMessageSequence
			onMessageSequence.getActivities().add(reply);
		}
	
		// add onMessageSequence to sequences as the last sequence
		sequences.addLast(onMessageSequence);
		// add onMessage to the main pick of the process
		BPELUtils.getMainPickElementFromBpelElements(bpelElements).getMessages().add(onMessage);
		
		// log execution flow
		LOGGER.exit();
	}
	
	public static void createCDClientReceivingChoreographyTaskActivities(String cdName,
			List<Choreography> choreographies, ChoreographyTask task,
			List<TaskCorrelationData> tasksCorrelationData, Participant cdParticipant, Process process,
			Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdInitiatingWSDLDefinitionData, WSDLDefinitionData cdClientWSDLDefinitionData,
			Deque<BPELExtensibleElement> bpelElements, Deque<org.eclipse.bpel.model.Sequence> sequences,
			ArtifactData artifactData, PropertyAliasesData propertyAliasesData){
		
		// log execution flow
		LOGGER.entry("CD Client Participant name: "+cdParticipant.getName(),"Task name: "+task.getName());
		
		LOGGER.info("Creating CD "+cdName+" Client receiving activities for choreography task "
				+task.getName());
		
		String taskName = WSDLUtils.formatOperationName(task.getName());
		String initiatingMessageName = BPMNUtils.getMessageNameChoreographyTaskSentToParticipant(task,
				cdParticipant);
		
		// check if the choreography task is looped
		if(task.getLoopType().compareTo(ChoreographyLoopType.NONE) != 0){
			// the choreography task is looped
			
			// log choreography task information
			LOGGER.info("The choreography task: "+task.getName()+" is looped!");
													
			// create a sequence 
			org.eclipse.bpel.model.Sequence sequenceLoop = BPELFactory.eINSTANCE.createSequence();		
			// check if the loop has a numeric expression
			if(BPMNUtils.getLoopNumericExpression(task) != null){
				// the loop has a numeric expression	
				// create the variable for the for each counter
				Variable varForEach = BPELFactory.eINSTANCE.createVariable();
				varForEach.setName(StringUtils.deleteWhitespace(task.getName()));

				// create ForEach
				ForEach forEach = BPELFactory.eINSTANCE.createForEach();
				forEach.setParallel(false);
				forEach.setCounterName(varForEach);
				forEach.setName(BPELUtils.createForEachName(task.getName()));
				// create for each counter start expression
				Expression startExpression = BPELFactory.eINSTANCE.createExpression();
				startExpression.setBody(START_COUNTER_VALUE);
				startExpression.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
				// set for each counter start value
				forEach.setStartCounterValue(startExpression);
				// set for each counter final value
				forEach.setFinalCounterValue(BPELUtils.createCDClientXPathExpression(BPMNUtils
						.getLoopNumericExpression(task), process, cdParticipant.getName(), wsdlDefinitions,
						cdClientWSDLDefinitionData, choreographies));
				// create scope
				Scope scope = BPELFactory.eINSTANCE.createScope();
				// set scope as the activity of the ForEach 
				forEach.setActivity(scope);		
				// set the sequenceLoop as the activity of the scope 
				scope.setActivity(sequenceLoop);	
				// attach the ForEach just created to the last sequence of sequences
				sequences.getLast().getActivities().add(forEach);					
				// add the ForEach as the last element of bpelElements 
				bpelElements.addLast(forEach);	
				
				// log for each creation
				LOGGER.info("Created forEach for the choreography task: "+task.getName());	
			}
			
			// check if the loop has a conditional expression
			if(BPMNUtils.getLoopConditionalExpression(task) != null){
				// the loop has a conditional expression
				// create while 
				While whileLoop = BPELFactory.eINSTANCE.createWhile();
				whileLoop.setName(BPELUtils.createWhileName(task.getName()));
				// create condition 
				Condition condition = BPELFactory.eINSTANCE.createCondition();
				condition.setExpressionLanguage(XPATH_1_0_QUERY_LANGUAGE);
				// set condition body
				condition.setBody(BPELUtils.createCDClientXPathExpression(BPMNUtils
					.getLoopConditionalExpression(task), process, cdParticipant.getName(), wsdlDefinitions,
					cdClientWSDLDefinitionData, choreographies).getBody());
				// set condition of whileLoop
				whileLoop.setCondition(condition);
				// set the sequenceLoop as the activity of the whileLoop
				whileLoop.setActivity(sequenceLoop);
				// attach the while just created to the last sequence of sequences
				sequences.getLast().getActivities().add(whileLoop);					
				// add the while as the last element of bpelElements 
				bpelElements.addLast(whileLoop);		
				
				// log while creation
				LOGGER.info("Created While for the choreography task: "+task.getName());	
			}
			// add to sequences the sequence sequenceForEach as last element
			sequences.addLast(sequenceLoop);								
		}			
		// the choreography task is not looped
		
		// log choreography task information
		LOGGER.info("The choreography task: "+task.getName()+" is not looped!");		
		
		// create receive
		Receive receiveCD = BPELFactory.eINSTANCE.createReceive();
		receiveCD.setName(BPELUtils.createReceiveName(cdParticipant.getName(),taskName));
		receiveCD.setCreateInstance(false);
		// get receiveCDOperationData
		OperationData receiveCDOperationData = WSDLUtils
				.getOperationDataWSDLOperation(cdClientWSDLDefinitionData.getWsdl(), taskName);		
		// get partner link for receive
		PartnerLinkTypeData receiveCDPartnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(artifactData
				.getArtifact(), receiveCDOperationData.getPortTypeName());
		PartnerLink receiveCDPartnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(WSDLUtils.
				formatParticipantNameForWSDL(cdInitiatingWSDLDefinitionData.getParticipantName()),
				taskName, process, receiveCDPartnerLinkTypeData,artifactData,true);
				
		Variable receiveCDInputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils.
				createInputVariableName(cdClientWSDLDefinitionData.getParticipantName(), taskName), 
				cdClientWSDLDefinitionData.getPrefix(), receiveCDOperationData.getInputMessageData()
				.getName());
	
		// create receiveCDOperation
		Operation receiveCDOperation = WSDLFactory.eINSTANCE.createOperation();
		receiveCDOperation.setName(taskName);
		// create receiveCDPortType
		PortType receiveCDPortType = WSDLFactory.eINSTANCE.createPortType();
		receiveCDPortType.setQName(new QName(cdClientWSDLDefinitionData.getPrefix(), receiveCDOperationData.
				getPortTypeName()));
		// set operation to receiveCD
		receiveCD.setOperation(receiveCDOperation);
		// set partner link to receiveCD
		receiveCD.setPartnerLink(receiveCDPartnerLink);	
		// set port type to receiveCD
		receiveCD.setPortType(receiveCDPortType);		
		// set variable to receiveCD
		receiveCD.setVariable(receiveCDInputVariable);
		
		// log receive creation
		LOGGER.info("Created receive element for operation: "+receiveCDOperation.getName());		
		
		// the receiver participant of the choreography task is a prosumer
		// create correlation set for choreographyID
		CorrelationSet correlationSetChoreographyID = BPELFactory.eINSTANCE.createCorrelationSet();
		correlationSetChoreographyID.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
		// create correlation for choreographyID
		Correlation correlationChoreographyID = BPELFactory.eINSTANCE.createCorrelation();
		correlationChoreographyID.setSet(correlationSetChoreographyID);
		correlationChoreographyID.setInitiate(CORRELATION_INITIATE_JOIN_LABEL);
		// create invokeCorrelations
		Correlations invokeCorrelations = BPELFactory.eINSTANCE.createCorrelations();
		invokeCorrelations.getChildren().add(correlationChoreographyID);
		// set invokeCorrelations to receiveCD
		receiveCD.setCorrelations(invokeCorrelations);	
		
		// add to receiveCD to the last sequence of sequences
		sequences.getLast().getActivities().add(receiveCD);
		
		// create propertyAliasesMessageType for Input Message
		PropertyAliasesMessageType propertyAliasesMessageType = new PropertyAliasesMessageType(
				receiveCDOperationData.getInputMessageData().getName(), receiveCDOperationData
				.getInputMessageData().getPartName());
		// add propertyAliasesMessageType to propertyAliasesData
		Utils.addPropertyAliasesData(propertyAliasesData, cdClientWSDLDefinitionData.getParticipantName(), 
				cdClientWSDLDefinitionData.getWsdlFileName(), cdClientWSDLDefinitionData.getWsdl()
				.getTargetNamespace(), propertyAliasesMessageType);
		
		// log creation of propertyAlias
		LOGGER.info("Created propertyAlias for message: "+receiveCDOperationData.getInputMessageData()
			.getName());		
		
		// check if the choreography task is looped
		if(task.getLoopType().compareTo(ChoreographyLoopType.NONE) != 0){
			// the choreography task is looped 
			// remove the last element inside sequences and bpelElements	
			sequences.pollLast();
			bpelElements.pollLast();
		}
		
		// if the CD participant is the receiving participant 
		// of a correlated receivingTask create <reply>
		if(tasksCorrelationData != null && BPMNUtils.isTaskReceivingOfCorrelationData(tasksCorrelationData,
				task)){
			// get receiveCDOperationData
			String onMessageCDOperationName = BPMNUtils
					.getInitiatingTaskNameCorrelatedToReceivingTask(tasksCorrelationData, task);
			OperationData onMessageCDOperationData = WSDLUtils
					.getOperationDataWSDLOperation(cdClientWSDLDefinitionData.getWsdl(),
							onMessageCDOperationName);	
			Variable replyCDOutputVariable = BPELUtils.getOrCreateVariableForMessageType(process, BPELUtils.
					createOutputVariableName(cdClientWSDLDefinitionData.getParticipantName(),taskName), 
					cdClientWSDLDefinitionData.getPrefix(), onMessageCDOperationData.getOutputMessageData()
					.getName());			
						
			// create assignReply
			Assign assignReply = BPELUtils.createAssignCDClientReply(process, cdClientWSDLDefinitionData, 
					onMessageCDOperationData, replyCDOutputVariable, receiveCDOperationData,
					receiveCDInputVariable, false);
			
			// add assignReply to the last sequence of sequences
			sequences.getLast().getActivities().add(assignReply);	
										
			// create onMessagePortType
			PortType onMessagePortType = WSDLFactory.eINSTANCE.createPortType();
			onMessagePortType.setQName(new QName(cdClientWSDLDefinitionData.getPrefix(),
					onMessageCDOperationData.getPortTypeName()));		
			// get partner link for on message
			PartnerLinkTypeData onMessagePartnerLinkTypeData = BPELUtils.getPartnerLinkTypeData(artifactData.
					getArtifact(), onMessageCDOperationData.getPortTypeName());			
			PartnerLink onMessagePartnerLink = BPELUtils.getPartnerLinkOrCreateIfNotExist(WSDLUtils.
					formatParticipantNameForWSDL(cdName), taskName, process, onMessagePartnerLinkTypeData, 
					artifactData, true);	
			
			// get initiating choreography task name from tasksCorrelationData
			String initiatingTaskName = BPMNUtils
					.getInitiatingTaskNameCorrelatedToReceivingTask(tasksCorrelationData, task);
			// get operation name corresponding to initiatingTaskName inside CDClientWSDL
			String cdClientOperationName = WSDLUtils
					.getOperationNameFromWSDLDefinition(cdClientWSDLDefinitionData.getWsdl(),
							initiatingTaskName);
			
			// create onMessageOperation
			Operation onMessageOperation = WSDLFactory.eINSTANCE.createOperation();
			onMessageOperation.setName(cdClientOperationName);			
			
			// create reply
			Reply reply = BPELFactory.eINSTANCE.createReply();
			reply.setName(BPELUtils.createReplyName(WSDLUtils.formatOperationName(cdClientOperationName)));
			reply.setOperation(onMessageOperation);
			reply.setPartnerLink(onMessagePartnerLink);
			reply.setPortType(onMessagePortType);
			reply.setVariable(replyCDOutputVariable);
			
			// log reply creation
			LOGGER.info("Created reply element for operation: "+onMessageOperation.getName());			

			// add reply to onMessageSequence
			sequences.getLast().getActivities().add(reply);			
		}
		
		// log execution flow
		LOGGER.exit();
	}


}
