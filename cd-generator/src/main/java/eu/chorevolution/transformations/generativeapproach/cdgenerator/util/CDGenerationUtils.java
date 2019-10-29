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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Correlation;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.CorrelationSets;
import org.eclipse.bpel.model.Correlations;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.PartnerLinks;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.messageproperties.MessagepropertiesFactory;
import org.eclipse.bpel.model.messageproperties.Property;
import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
import org.eclipse.bpel.model.partnerlinktype.PartnerlinktypeFactory;
import org.eclipse.bpel.model.partnerlinktype.Role;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyActivity;
import org.eclipse.bpmn2.ChoreographyLoopType;
import org.eclipse.bpmn2.ChoreographyTask;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubChoreography;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.impl.FormalExpressionImpl;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.wsdl.extensions.schema.SchemaImpl;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ArtifactData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.LoopedFlowNodeData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesDataItem;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesMessageType;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ServiceDeployBPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TimerData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLDefinitionData;



public class CDGenerationUtils {

	private static final String ADDRESS_CD_DEFAULT_HOSTADDRESS = "localhost";
	private static final String ADDRESS_CD_DEFAULT_PORT = "8080";
	private static final String ADDRESS_CD_DEFAULT = "http://"+ADDRESS_CD_DEFAULT_HOSTADDRESS+":"
			+ADDRESS_CD_DEFAULT_PORT+"/ode/processes/";
	private static final String ADDRESS_CD_PROSUMER_SIDE_DEFAULT_HOSTADDRESS = "localhost";
	private static final String ADDRESS_CD_PROSUMER_SIDE_DEFAULT_PORT = "9090";
	private static final String ADDRESS_CD_PROSUMER_SIDE_DEFAULT = "http://"
			+ADDRESS_CD_PROSUMER_SIDE_DEFAULT_HOSTADDRESS+":"+ADDRESS_CD_PROSUMER_SIDE_DEFAULT_PORT+"/";
	private static final String ARTIFACTS_NAME = "Artifacts";
	private static final String BASE_SERVICE_ARTIFACTS_FILE_NAME = "baseServiceArtifacts.wsdl";
	private static final String BASE_SERVICE_BPEL_PROCESS_FILE_NAME = "baseService.bpel";
	private static final String BASE_SERVICE_SUFFIX = "BaseService";	
	private static final String BASE_SERVICE_WSDL_FILE_NAME = "baseService.wsdl";	
	private static final String CD_PROCESS_PREFIX_NS = "cdprocess";
	private static final String CHOREOGRAPHY_ID_PROPERTY_LABEL = "chorID";
	private static final String CORRELATION_SET_CHOREOGRAPHY_ID_NAME = "CorrelationSetChoreographyID";
	private static final String DEPLOY_ACTIVE_TAG_NAME = "active";
	private static final String DEPLOY_GENERATE_ATTRIBUTE_NAME = "generate";
	private static final String DEPLOY_GENERATE_ATTRIBUTE_ALL_VALUE = "all";
	private static final String DEPLOY_INVOKE_TAG_NAME = "invoke";
	private static final String DEPLOY_PARTNERLINK_ATTRIBUTE_NAME = "partnerLink";
	private static final String DEPLOY_PROCESS_TAG_NAME = "process";
	private static final String DEPLOY_PROCESS_EVENTS_TAG_NAME = "process-events";
	private static final String DEPLOY_PROVIDE_TAG_NAME = "provide";
	private static final String DEPLOY_RETIRED_TAG_NAME = "retired";
	private static final String DEPLOY_SERVICE_PORT_ATTRIBUTE_NAME = "port";
	private static final String DEPLOY_SERVICE_TAG_NAME = "service";
	private static final String DEPLOY_TAG_NAME = "deploy";
	private static final String DEPLOY_USEPEER2PEER_ATTRIBUTE_NAME = "usePeer2Peer";
	private static final String DEPLOY_XMLNS_APACHE_ODE = "http://www.apache.org/ode/schemas/dd/2007/03";
	private static final String ECLIPSE_COMMON_COMPONENT_FILE_NAME = "org.eclipse.wst.common.component";	
	private static final String ECLIPSE_FACET_CORE_FILE_NAME =
			"org.eclipse.wst.common.project.facet.core.xml";	
	private static final String ECLIPSE_PROJECT_FILE_NAME = ".project";	
	private static final String ECLIPSE_SETTINGS_FOLDER_NAME = ".settings";
	private static final String FALSE_VALUE = "false";
	private static final String LOCATION_LABEL = "location";
	private static final String MAIN_SEQUENCE_LABEL = "MainSequence";
	private static final String MESSAGE_TYPE_ATTRIBUTE_NAME = "messageType";
	private static final String NAMESPACE_LABEL = "namespace";
	private static final String NAME_LABEL = "name";
	private static final String PART_ATTRIBUTE_NAME = "part";
	private static final String PARTNER_LINK_TYPE = "partnerLinkType";
	private static final String PICK_NAME = "SwitchInvokedOperation";
	private static final String PORT_TYPE_LABEL = "portType";
	private static final String PROPERTIES_ALIASES_NAME_LABEL = "propertiesAliases";
	private static final String PROPERTIES_NAME_LABEL = "properties";
	private static final String PROPERTIES_PREFIX = "properties";
	private static final String PROPERTY_NAME_ATTRIBUTE_NAME = "propertyName";
	private static final String PLNK_ROLE = "role";
	private static final String PLNKTYPE_NS = "http://docs.oasis-open.org/wsbpel/2.0/plnktype";
	private static final String SET_INVOCATION_ADDRESS_DEPLOY_PREFIX_NAME = "baseservice";
	private static final String SET_INVOCATION_ADDRESS_PARTNER_LINK_NAME =  "ConfigurableService";
	private static final String SET_INVOCATION_ADDRESS_SERVICE_PORT_NAME =  "ConfigurableServicePort";
	private static final String SET_INVOCATION_ADDRESS_PROCESS_NAME = "BaseService";
	private static final String SET_INVOCATION_ADDRESS_PROCESS_TNS = "http://services.chorevolution.eu/";
	private static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
	private static final String SOAP_NS_PREFIX = "soap";	
	private static final String TARGET_NAMESPACE_LABEL = "targetNamespace";
	private static final String TARGET_NS_CD_DEFAULT = "http://eu.chorevolution/cd/";
	private static final String TARGET_NS_PREFIX = "tns";	
	private static final String TRUE_VALUE = "true";	
	private static final String TYPE_LABEL = "type";	
	private static final String VPROP = "http://docs.oasis-open.org/wsbpel/2.0/varprop";
	private static final String VPROP_PROPERTY_ALIAS_TAG_NAME = "vprop:propertyAlias";
	private static final String VPROP_PROPERTY_ELEMENT_NAME = "vprop:property";
	private static final String VPROP_QUERY_CHOREOGRAPHY_ID_QUERY = "choreographyId/choreographyId";
	private static final String VPROP_QUERY_ELEMENT_NAME = "vprop:query";
	private static final String WSAM_NS = "http://www.w3.org/2007/05/addressing/metadata";
	private static final String WSAM_NS_PREFIX = "wsam";	
	private static final String WSDL_DEFINITIONS_TAG_NAME = "definitions";
	private static final String WSDL_IMPORT_TAG_NAME = "import";
	private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
	private static final String WSP_NS = "http://www.w3.org/ns/ws-policy";
	private static final String WSP_NS_PREFIX = "wsp";
	private static final String WSP1_2_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";	
	private static final String WSP1_2_NS_PREFIX = "wsp1_2";		
	private static final String WSU_NS = 
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	private static final String WSU_NS_PREFIX = "wsu";		
	private static final String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
	private static final String XMLSCHEMA_NS_PREFIX = "xsd";
	private static final String XMLNS = "xmlns";
	private static final String XMLNS_TNS = "xmlns:tns";
	private static final String XMLNS_VPROP = "xmlns:vprop";
	private static final String XMLNS_XSD = "xmlns:xsd";	
	private static final String XSD_SCHEMA_ELEMENT_TYPE_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";
	private static final String XSD_SCHEMA_ELEMENT_TYPE_LOCAL_PART = "schema";
	private static final String XSD_STRING_TYPE = "xsd:string";	
	
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(CDGenerationUtils.class);
	
	public static Definition generateCDProsumerWSDL(ChoreographyData choreographyData,
			String choreographytns, String cdParticipantName){

		// log execution flow
		LOGGER.entry("CD participant name: "+cdParticipantName);
		
		Definition definition = null;
		String cdNameWSDL = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = WSDLUtils.createCDName(cdNameWSDL);
		String targetNS = WSDLUtils.createNamespace(choreographytns,cdName);	
		org.apache.ws.commons.schema.XmlSchema xmlschema = choreographyData.getSchema();	
		xmlschema.setTargetNamespace(targetNS);
		try {
			WSDLFactory factory = WSDLFactory.newInstance();
			definition = factory.newDefinition();
		    // setName
		    definition.setQName(new QName(targetNS,Utils.createCDname(cdNameWSDL)));
		    // set namespaces
		    definition.addNamespace(TARGET_NS_PREFIX,targetNS);
		    definition.addNamespace(WSAM_NS_PREFIX,WSAM_NS);
		    definition.addNamespace(WSP_NS_PREFIX,WSP_NS);
		    definition.addNamespace(WSP1_2_NS_PREFIX,WSP1_2_NS);
		    definition.addNamespace(WSU_NS_PREFIX,WSU_NS);
		    definition.addNamespace(XMLSCHEMA_NS_PREFIX,XMLSCHEMA_NS);
			definition.addNamespace(SOAP_NS_PREFIX,SOAP_NS);
//			definition.addNamespace(TYPES_NS_PREFIX,typesNS);
			// set TNS WSDL
		    definition.setTargetNamespace(targetNS);			    
		    // get the ids of the looped choreography tasks
		    Set<String> loopedChoreographyTasks = BPMNUtils
		    		.getLoopedChoreographyTasksFromChoreographies(choreographyData.getChoreographies());	    
			// get the participant corresponding to the cd
			Participant cdParticipant = choreographyData.getParticipantFromName(cdParticipantName);
			// iterate over every choreography
			for(Choreography choreography:choreographyData.getChoreographies()){
				// iterate over choreography tasks
				for (FlowElement flowElement : choreography.getFlowElements()) {							
					if (flowElement instanceof ChoreographyTask) {						
						// check if the CD participant is the receiving participant of the task 
						if((BPMNUtils.getReceivingParticipant((ChoreographyActivity) flowElement)
								.equals(cdParticipant))){
							// log Choreography Task name
							LOGGER.info("Adding operation related to the Choreography Task: "
									+((ChoreographyActivity) flowElement).getName());						
							// create base name by concatenating the name of the initiating participant 
							// with the name of the receiving participant of the Choreography Task
							String baseName = BPMNUtils
									.createChoreographyTaskParticipantsBaseName(
											(ChoreographyTask) flowElement);							
							// get the name of the choreography input message
							String choreographyInitiatingMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// get the type name of the choreography input message
							String choreographyInitiatingMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// get the name of the choreography output message
							String choreographyReturnMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// get the type name of the choreography output message
							String choreographyReturnMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// add operation elements to the wsdl definition 
							// corresponding to the choreography task 
							WSDLUtils.addCDProsumerOperationElementsToWSDLDefinition(definition, xmlschema,
									targetNS, WSDLUtils.formatOperationName(flowElement.getName()), 
									loopedChoreographyTasks.contains(flowElement.getId()),
									choreographyInitiatingMessageName, choreographyInitiatingMessageTypeName,
									choreographyReturnMessageName, choreographyReturnMessageTypeName,
									baseName, ADDRESS_CD_DEFAULT);					
						}
					}
				}
			}	
			// create types	
			// TODO till now all the types are added but would have to be retrieved only types 
			// involved in the task
			Types types = definition.createTypes();
			Schema schema = new SchemaImpl();
			schema.setElement(XSDSchemaUtils.getElementFromXmlSchema(xmlschema));		
			schema.setElementType(new QName(XSD_SCHEMA_ELEMENT_TYPE_NAMESPACE_URI,
					XSD_SCHEMA_ELEMENT_TYPE_LOCAL_PART));
			WSDLUtils.setTargetNamespaceSchema(schema, targetNS);				
			types.addExtensibilityElement(schema);		          
		    definition.setTypes(types);	
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"WSDLException into generateProsumerWSDL see log file for details");
		}
		
		// log execution flow
		LOGGER.exit();
		
		return definition;
	}

	public static Definition generateCDClientWSDL(ChoreographyData choreographyData, String choreographytns, 
			String cdParticipantName, List<TaskCorrelationData> tasksCorrelationData){
		
		// log execution flow
		LOGGER.entry("CD participant name: "+cdParticipantName);
		
		Definition definition = null;
		String cdNameWSDL = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		String cdName = WSDLUtils.createCDName(cdNameWSDL);
		String targetNS = WSDLUtils.createNamespace(choreographytns, cdName);	
		org.apache.ws.commons.schema.XmlSchema xmlschema = choreographyData.getSchema();	
		xmlschema.setTargetNamespace(targetNS);
		
		// get first choreography task name of the main choreography
		String firstChoregraphyTaskNameMainChoreography = BPMNUtils.getFirstChoregraphyTaskName(BPMNUtils
				.getMainChoreographyFromChoreographyData(choreographyData));
		
		try {
			WSDLFactory factory = WSDLFactory.newInstance();
			definition = factory.newDefinition();
		    // setName
		    definition.setQName(new QName(targetNS,Utils.createCDname(cdNameWSDL)));
		    // set namespaces
		    definition.addNamespace(TARGET_NS_PREFIX,targetNS);
		    definition.addNamespace(WSAM_NS_PREFIX,WSAM_NS);
		    definition.addNamespace(WSP_NS_PREFIX,WSP_NS);
		    definition.addNamespace(WSP1_2_NS_PREFIX,WSP1_2_NS);
		    definition.addNamespace(WSU_NS_PREFIX,WSU_NS);
		    definition.addNamespace(XMLSCHEMA_NS_PREFIX,XMLSCHEMA_NS);
			definition.addNamespace(SOAP_NS_PREFIX,SOAP_NS);
//			definition.addNamespace(TYPES_NS_PREFIX,typesNS);
			// set TNS WSDL
		    definition.setTargetNamespace(targetNS);				    
			// get the participant corresponding to the cd
			Participant cdParticipant = choreographyData.getParticipantFromName(cdParticipantName);
			// iterate over every choreography
			for(Choreography choreography:choreographyData.getChoreographies()){
				// iterate over choreography tasks
				for (FlowElement flowElement : choreography.getFlowElements()) {
					if (flowElement instanceof ChoreographyTask) {
						// log Choreography Task name
						LOGGER.info("Adding operation related to the Choreography Task: "
								+((ChoreographyActivity) flowElement).getName());	
						String baseName = null;
						String choreographyInitiatingMessageName = null;
						String choreographyInitiatingMessageTypeName = null;
						String choreographyReturnMessageName = null;
						String choreographyReturnMessageTypeName = null;						
						// check if CD participant is the initiating participant or the receiving participant 
						// of the choreography task 
						if (BPMNUtils.getInitiatingParticipant((ChoreographyActivity) flowElement)
								.equals(cdParticipant)) {		
							// CD participant is the initiating participant of the choreography task 
							// create base name by removing spaces in the name of the participant that 
							// represent the CD Client
							baseName = StringUtils.deleteWhitespace(cdParticipantName);
							// get the type name of the choreography input message and output message in 
							// case the task is the initiating task of some task to be correlated
							if(tasksCorrelationData != null && BPMNUtils
									.isTaskInitiatingOfCorrelationData(tasksCorrelationData,
											(ChoreographyTask) flowElement)){
								choreographyInitiatingMessageName = BPMNUtils
										.getMessageNameChoreographyTaskSentFromParticipant(
												(ChoreographyTask) flowElement, cdParticipant);
								choreographyInitiatingMessageTypeName = BPMNUtils
										.getMessageTypeNameChoreographyTaskSentFromParticipant(
												(ChoreographyTask) flowElement, cdParticipant);
								choreographyReturnMessageName = BPMNUtils
										.getMessageNameSentToParticipantFromTaskName(choreographyData
												.getChoreographies(), tasksCorrelationData,
												((ChoreographyTask) flowElement).getName(), cdParticipant);
								choreographyReturnMessageTypeName = BPMNUtils
										.getMessageTypeNameSentToParticipantFromTaskName(choreographyData
												.getChoreographies(), tasksCorrelationData,
												((ChoreographyTask) flowElement).getName(), cdParticipant);
							}
							// get the type name of the choreography input message and output message 
							// in case the task is not the initiating task of some task to be correlated
							else{
								choreographyInitiatingMessageName = BPMNUtils
										.getMessageNameChoreographyTaskSentFromParticipant(
												(ChoreographyTask) flowElement, cdParticipant);
								choreographyInitiatingMessageTypeName = BPMNUtils
										.getMessageTypeNameChoreographyTaskSentFromParticipant(
												(ChoreographyTask) flowElement, cdParticipant);								
								choreographyReturnMessageName = BPMNUtils
										.getMessageNameChoreographyTaskSentToParticipant(
												(ChoreographyTask) flowElement, cdParticipant);	
								choreographyReturnMessageTypeName = BPMNUtils
										.getMessageTypeNameChoreographyTaskSentToParticipant(
												(ChoreographyTask) flowElement, cdParticipant);									
							}
						}
						else{
							// CD participant is the receiving participant of the choreography task 
							// create base name by concatenating the name of the initiating participant 
							// with the name of the receiving participant of the Choreography Task
							baseName = BPMNUtils
									.createChoreographyTaskParticipantsBaseName(
											(ChoreographyTask) flowElement);						
							// get the name of the choreography input message
							choreographyInitiatingMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);
							// get the type name of the choreography input message
							choreographyInitiatingMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// get the name of the choreography output message
							choreographyReturnMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// get the type name of the choreography output message
							choreographyReturnMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement, cdParticipant);								
						}
						// add operation elements corresponding to the choreography task to wsdl's definition					
						WSDLUtils.addCDClientOperationElementsToWSDLDefinition( definition, xmlschema,
								targetNS, WSDLUtils.formatOperationName(flowElement.getName()),
								choreographyInitiatingMessageName, choreographyInitiatingMessageTypeName, 
								choreographyReturnMessageName, choreographyReturnMessageTypeName, baseName, 
								ADDRESS_CD_DEFAULT, !firstChoregraphyTaskNameMainChoreography
									.equals(flowElement.getName()));							
					}			
				}
			}
			// create types	
			// TODO till now all the types are added but would have to be retrieved only types 
			// involved in the task
			Types types = definition.createTypes();
			Schema schema = new SchemaImpl();
			schema.setElement(XSDSchemaUtils.getElementFromXmlSchema(xmlschema));		
			schema.setElementType(new QName(XSD_SCHEMA_ELEMENT_TYPE_NAMESPACE_URI, 
					XSD_SCHEMA_ELEMENT_TYPE_LOCAL_PART));
			WSDLUtils.setTargetNamespaceSchema(schema, targetNS);				
			types.addExtensibilityElement(schema);		          
		    definition.setTypes(types);	
			}catch (WSDLException e) {
				LOGGER.error(e.getMessage(),e);
				throw new CDGeneratorException(
						"WSDLException into generateProsumerWSDL see log file for details ");
			}
		
		// log execution flow
		LOGGER.exit();		
		
		return definition;
	}
	
	public static Definition generateCDProsumerPartWSDL(ChoreographyData choreographyData,
			String choreographytns, String prosumerParticipantName){
			
		// log execution flow
		LOGGER.entry("CD participant name: "+prosumerParticipantName);
		
		String prosumerParticipantNameWSDL = WSDLUtils.formatParticipantNameForWSDL(prosumerParticipantName);		
		String targetNS = WSDLUtils.createNamespace(choreographytns, prosumerParticipantNameWSDL);	
		XmlSchema xmlschema = choreographyData.getSchema();		
		xmlschema.setTargetNamespace(targetNS);		
		Definition definition = null;
		try {
			WSDLFactory factory = WSDLFactory.newInstance();
			definition = factory.newDefinition();
		    // setName
		    definition.setQName(new QName(targetNS, prosumerParticipantNameWSDL));
		    // set namespaces
		    definition.addNamespace(TARGET_NS_PREFIX, targetNS);
		    definition.addNamespace(WSAM_NS_PREFIX,WSAM_NS);
		    definition.addNamespace(WSP_NS_PREFIX,WSP_NS);
		    definition.addNamespace(WSP1_2_NS_PREFIX,WSP1_2_NS);
		    definition.addNamespace(WSU_NS_PREFIX,WSU_NS);
		    definition.addNamespace(XMLSCHEMA_NS_PREFIX,XMLSCHEMA_NS);
			definition.addNamespace(SOAP_NS_PREFIX,SOAP_NS);
			// set TNS WSDL
		    definition.setTargetNamespace(targetNS);		    
		    // get the ids of the looped choreography tasks
		    Set<String> loopedChoreographyTasks = BPMNUtils
		    		.getLoopedChoreographyTasksFromChoreographies(choreographyData.getChoreographies());	    
		    // get cd participant
			Participant cdParticipant = choreographyData.getParticipantFromName(prosumerParticipantName);
			// iterate over every choreography
			for(Choreography choreography:choreographyData.getChoreographies()){
				// iterate over choreography tasks
				for (FlowElement flowElement : choreography.getFlowElements()) {
					if (flowElement instanceof ChoreographyTask) {
						String choreographyInitiatingMessageName = null;
						String choreographyInitiatingMessageTypeName = null;
						String choreographyReturnMessageName = null;
						String choreographyReturnMessageTypeName = null;
						// check if the CD participant is the initiating participant of the task 
						if (BPMNUtils.getInitiatingParticipant((ChoreographyActivity) flowElement)
								.equals(cdParticipant)){
							// log send operation creation
							LOGGER.info("Create send operation related to choreography task: "
									+((ChoreographyActivity) flowElement).getName());						
							// CD participant is the initiating participant of the task	
							// create a send operation for the initiating message of the choregraphy task							
							// get the message name sent from the CD participant
							choreographyInitiatingMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement, cdParticipant);		
							// get the message type name sent from the CD participant
							choreographyInitiatingMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							
							// add send operation elements definitions to wsdl definition
							WSDLUtils.addCDProsumerPartSendOperationElementsToWSDLDefinition(definition,
									xmlschema, targetNS, choreographyInitiatingMessageName,
									choreographyInitiatingMessageTypeName, prosumerParticipantName,
									ADDRESS_CD_PROSUMER_SIDE_DEFAULT, loopedChoreographyTasks
									.contains(flowElement.getId()));
							
							// get the message name received by the CD participant
							// (return message for the choreography task)
							choreographyReturnMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);
							// get the message type name received by the CD participant
							// (return message for the choreography task)
							choreographyReturnMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);							
							// check if exist a return message for the choregraphy task
							if( choreographyReturnMessageName != null){
								// log receiving operation creation
								LOGGER.info("Create receive operation related to choreography task: "
										+ ((ChoreographyActivity) flowElement).getName());							
								// a return message for the choreography task exist		
								
								// add receive operation elements definitions to wsdl defition
								WSDLUtils.addCDProsumerPartReceiveOperationElementsToWSDLDefinition(
										definition, xmlschema, targetNS, choreographyReturnMessageName, 
										choreographyReturnMessageTypeName, prosumerParticipantName, 
										ADDRESS_CD_PROSUMER_SIDE_DEFAULT);
							}					
						}
						else{
							// log operation creation
							LOGGER.info("Create operation related to choreography task: "
									+ ((ChoreographyActivity) flowElement).getName());
							// CD participant is the receiving participant of the task	
							// get the name of the choreography input message
							choreographyInitiatingMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);		
							// get the type name of the choreography input message
							choreographyInitiatingMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentToParticipant(
											(ChoreographyTask) flowElement, cdParticipant);	
							// get the name of the choreography output message
							choreographyReturnMessageName = BPMNUtils
									.getMessageNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement,cdParticipant);
							// get the type name of the choreography output message
							choreographyReturnMessageTypeName = BPMNUtils
									.getMessageTypeNameChoreographyTaskSentFromParticipant(
											(ChoreographyTask) flowElement,cdParticipant);
							
							// add operation elements corresponding to the choreography task 
							// to wsdl definition	
							WSDLUtils.addCDProsumerPartOperationElementsToWSDLDefinition(
									definition, xmlschema, 
									targetNS, WSDLUtils.formatOperationName(flowElement.getName()), 
									choreographyInitiatingMessageName,
									choreographyInitiatingMessageTypeName, choreographyReturnMessageName,
									choreographyReturnMessageTypeName, prosumerParticipantName, 
									ADDRESS_CD_PROSUMER_SIDE_DEFAULT);
						}
					}
				}
			}
			// create types	
			// TODO till now all the types are added but it has to retrieved only types involved in the task
			Types types = definition.createTypes();
			Schema schema = new SchemaImpl();
			schema.setElement(XSDSchemaUtils.getElementFromXmlSchema(xmlschema));
			schema.setElementType(new QName(XSD_SCHEMA_ELEMENT_TYPE_NAMESPACE_URI, 
					XSD_SCHEMA_ELEMENT_TYPE_LOCAL_PART));			
			WSDLUtils.setTargetNamespaceSchema(schema, targetNS);			
	        types.addExtensibilityElement(schema);		          
		    definition.setTypes(types);		
		} catch (WSDLException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"WSDLException into generateProsumerWSDL see log file for details ");
		}	
		
		// log execution flow
		LOGGER.exit();
		
		return definition;
	}	

	
	public static Document generateCDArtifactsDocument(String choreographytns,String cdName,
			String cdParticipantName, ChoreographyData choreographyData, 
			Map<String, WSDLDefinitionData> wsdlDefinitions, WSDLDefinitionData cdWSDLDefinitionData){
		
		// log execution flow
		LOGGER.entry("CD name: "+cdName+" CD participant name: "+cdParticipantName);
		
		String prosumerNameWSDL = WSDLUtils.formatParticipantNameForWSDL(cdParticipantName);
		Document artifactsDocument = null;
		String cdNameLowerCase = cdName.toLowerCase();
		try {
			// create artifacts document
			artifactsDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			// create <definitions>
			Element definitions=artifactsDocument.createElement(WSDL_DEFINITIONS_TAG_NAME);
			// add <definitions> to document
			artifactsDocument.appendChild(definitions);		
			List<Element> importElementList = new ArrayList<>();
			List<Element> partnerLinkTypeElementList = new ArrayList<>();
			// create target namespace
			String targetNS = WSDLUtils.createNamespace(choreographytns, new StringBuilder(prosumerNameWSDL)
					.append(ARTIFACTS_NAME).toString());
			// add xmlns:tns namespace
			definitions.setAttribute(XMLNS_TNS,targetNS);
			// add targetNamespace namespace
			definitions.setAttribute(TARGET_NAMESPACE_LABEL,targetNS);
			// add xmlns namespace
			definitions.setAttribute(XMLNS,WSDL_NS);
			
			// get target namespace of the WSDL corresponding to cdWSDLDefinitionData
			String tnsImportCDWSDL = cdWSDLDefinitionData.getWsdl().getTargetNamespace();
			// create <import>
			Element importItemCD=artifactsDocument.createElement(WSDL_IMPORT_TAG_NAME);		
			importItemCD.setAttribute(NAMESPACE_LABEL,tnsImportCDWSDL);
			importItemCD.setAttribute(LOCATION_LABEL,WSDLUtils.createImportLocationValue(
					cdWSDLDefinitionData.getWsdlFileName()));
			// add <import> namespace to <definitions>
			definitions.setAttribute(Utils.createXMLQNameString(XMLNS,cdNameLowerCase),tnsImportCDWSDL);		
			// add <import> to importElementList
			importElementList.add(importItemCD);	
			
			// create a <partnerLinkType> for every port 
			Iterator<?> cdPortTypes = cdWSDLDefinitionData.getWsdl().getPortTypes().entrySet().iterator();
			while (cdPortTypes.hasNext()) {
				String portTypeNameImport = ((PortType) ((Entry<?, ?>) cdPortTypes.next()).getValue())
						.getQName().getLocalPart();
				// create <partnerLinkType>
				Element cdplt=artifactsDocument.createElement(PARTNER_LINK_TYPE);		
				// set partnerLinkTypeName 
				cdplt.setAttribute(NAME_LABEL, BPELUtils.createCDPartnerLinkTypeName(portTypeNameImport));
				// set xmlns attribute of <partnerLinkType>
				cdplt.setAttribute(XMLNS,PLNKTYPE_NS);					
				// create <role>
				Element plRole=artifactsDocument.createElement(PLNK_ROLE);
				// set <role> name
				plRole.setAttribute(NAME_LABEL,WSDLUtils.createPartnerLinkRoleNameValue(portTypeNameImport));	
				// set <role> portType
				plRole.setAttribute(PORT_TYPE_LABEL,Utils.createXMLQNameString(cdNameLowerCase, 
						portTypeNameImport));
				// add <role> to <partnerLinkType>
				cdplt.appendChild(plRole);	
				// add <partnerLinkType> to partnerLinkTypeElementList
				partnerLinkTypeElementList.add(cdplt);	
			}
						
			// create partnerLinkType of other artifacts
			// iterate over wsdlDefinitions
			for (Entry<String, WSDLDefinitionData> WSDLDefinitionDataItem : wsdlDefinitions.entrySet()) {			
				String participantNameNormalized = WSDLUtils.formatParticipantNameForWSDL(
						WSDLDefinitionDataItem.getKey());
				// get target namespace of the WSDL corresponding to WSDLDefinitionDataItem
				String tnsImportWSDL = WSDLDefinitionDataItem.getValue().getWsdl().getTargetNamespace();
				// create <import>
				Element importItem=artifactsDocument.createElement(WSDL_IMPORT_TAG_NAME);		
				importItem.setAttribute(NAMESPACE_LABEL,tnsImportWSDL);
				importItem.setAttribute(LOCATION_LABEL,WSDLUtils.createImportLocationValue(
						WSDLDefinitionDataItem.getValue().getWsdlFileName()));		
				// add <import> namespace to <definitions>
				definitions.setAttribute(Utils.createXMLQNameString(XMLNS,participantNameNormalized), 
						tnsImportWSDL);
				// add <import> to importElementList
				importElementList.add(importItem);
								
				// create a <partnerLinkType> for every port 
				Iterator<?> portTypes = WSDLDefinitionDataItem.getValue().getWsdl().getPortTypes()
						.entrySet().iterator();
				while (portTypes.hasNext()) {
					String portTypeNameImport = ((PortType) ((Entry<?, ?>) portTypes.next()).getValue())
							.getQName().getLocalPart();
					// create <partnerLinkType>
					Element plt=artifactsDocument.createElement(PARTNER_LINK_TYPE);		
					// set partnerLinkTypeName
					plt.setAttribute(NAME_LABEL, BPELUtils.createPartnerLinkTypeName(portTypeNameImport));
					// set xmlns attribute of <partnerLinkType>
					plt.setAttribute(XMLNS,PLNKTYPE_NS);		
					// create <role>
					Element plRole=artifactsDocument.createElement(PLNK_ROLE);
					// set <role> name
					plRole.setAttribute(NAME_LABEL, WSDLUtils
							.createPartnerLinkRoleNameValue(portTypeNameImport));	
					// set <role> portType
					plRole.setAttribute(PORT_TYPE_LABEL, Utils
							.createXMLQNameString(participantNameNormalized, portTypeNameImport));
					// add <role> to <partnerLinkType>
					plt.appendChild(plRole);	
					// add <partnerLinkType> to partnerLinkTypeElementList
					partnerLinkTypeElementList.add(plt);					
				}				
			}
			// add every <import> into <definitions>
			for (Element element : importElementList) {
				definitions.appendChild(element);
			}			
			// add every <partnerLinkType> into <definitions>			
			for (Element element : partnerLinkTypeElementList) {
				definitions.appendChild(element);
			}	
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into generateProsumerArtifactsDocument see log file for details");
		}
		// log execution flow
		LOGGER.exit();
		
		return artifactsDocument;
	}
	
	public static void createSetInvocationAddressElements(String participantName, String cdName,
			String destinationFolderPath){
		
		// copy baseService files to the destination folder
//		Utils.copyBaseServiceFilesToFolder(destinationFolderPath);
		
		// create baseServive Bpel process
		FreeMarkerUtils.generateBaseServiceBpelProcess(participantName, 
				destinationFolderPath + BASE_SERVICE_BPEL_PROCESS_FILE_NAME);
		// create baseService Artifacts WSDL
		FreeMarkerUtils.generateBaseServiceArtifactsWSDL(destinationFolderPath 
				+ BASE_SERVICE_ARTIFACTS_FILE_NAME);
		// create base service wsdl
		FreeMarkerUtils.generateBaseServiceWSDL(cdName, destinationFolderPath 
				+ BASE_SERVICE_WSDL_FILE_NAME);
	}
	
	public static void createEclipseProjectFiles(String cdName, String destinationFolderPath){
		
		// create .settings folder
		try {
			String settingsFolderPath = new StringBuilder(destinationFolderPath).append(File.separatorChar)
					.append(ECLIPSE_SETTINGS_FOLDER_NAME).append(File.separatorChar).toString();
			FileUtils.forceMkdir(new File(settingsFolderPath));
			// create eclipse .project file
			FreeMarkerUtils.generateEclipseProjectFile(cdName, destinationFolderPath 
					+ ECLIPSE_PROJECT_FILE_NAME);
			// create eclipse org.eclipse.wst.common.component
			FreeMarkerUtils.generateEclipseCommonComponentFile(cdName, settingsFolderPath
					+ECLIPSE_COMMON_COMPONENT_FILE_NAME);
			// create org.eclipse.wst.common.project.facet.core.xml
			FreeMarkerUtils.generateEclipseFacetCoreFile(settingsFolderPath 
					+ ECLIPSE_FACET_CORE_FILE_NAME);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into createPropertiesWSDLDocument see log file for details");
		}
	}

	
	public static Document createPropertiesWSDLDocument(String baseNS){

		// log execution flow
		LOGGER.entry("Base namespace: "+baseNS);
		
		Document propertiesWSDLDocument = null;
		try {
			// create propertiesWSDL document
			propertiesWSDLDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();		
			// create <definitions>
			Element definitions = propertiesWSDLDocument.createElement(WSDL_DEFINITIONS_TAG_NAME);	
			// add name attribute 
			definitions.setAttribute(NAME_LABEL, PROPERTIES_NAME_LABEL);
			// create target namespace
			String targetNS = WSDLUtils.createNamespace(baseNS, PROPERTIES_NAME_LABEL);	
			// add targetNamespace namespace
			definitions.setAttribute(TARGET_NAMESPACE_LABEL, targetNS);
			// add xmlns namespace
			definitions.setAttribute(XMLNS, WSDL_NS);			
			// add xmlns:tns namespace
			definitions.setAttribute(XMLNS_TNS, targetNS);	
			// add xmlns:vprop namespace
			definitions.setAttribute(XMLNS_VPROP, VPROP);
			// add xmlns:xsd namespace
			definitions.setAttribute(XMLNS_XSD, XSD_SCHEMA_ELEMENT_TYPE_NAMESPACE_URI);			
			
			// create <vprop:property>
			Element vprop_Property = propertiesWSDLDocument.createElement(VPROP_PROPERTY_ELEMENT_NAME);
			// add name attribute
			vprop_Property.setAttribute(NAME_LABEL, CHOREOGRAPHY_ID_PROPERTY_LABEL);
			// add type attribute
			vprop_Property.setAttribute(TYPE_LABEL, XSD_STRING_TYPE);		
			
			// add <vprop:property> to <definitions>
			definitions.appendChild(vprop_Property);			
			
			// add <definitions> to document
			propertiesWSDLDocument.appendChild(definitions);				
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into createPropertiesWSDLDocument see log file for details");
		}	
		
		// log execution flow
		LOGGER.exit();		
		
		return propertiesWSDLDocument;
	}
	
	public static Document createPropertiesAliasesWSDLDocument(String baseNS,
			PropertyAliasesData propertyAliasesData){

		// log execution flow
		LOGGER.entry("Base namespace: "+baseNS);
		
		Document propertiesAliasesWSDLDocument = null;
		try {
			// create propertiesAliasesWSDL document
			propertiesAliasesWSDLDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
			// create <definitions>
			Element definitions = propertiesAliasesWSDLDocument.createElement(WSDL_DEFINITIONS_TAG_NAME);	
			// add name attribute 
			definitions.setAttribute(NAME_LABEL, PROPERTIES_ALIASES_NAME_LABEL);			
			// create target namespace
			String targetNS = WSDLUtils.createNamespace(baseNS, PROPERTIES_ALIASES_NAME_LABEL);				
			// add targetNamespace namespace
			definitions.setAttribute(TARGET_NAMESPACE_LABEL, targetNS);
			// add xmlns:tns namespace
			definitions.setAttribute(XMLNS_TNS, targetNS);	
			// add xmlns namespace
			definitions.setAttribute(XMLNS, WSDL_NS);				
			// add xmlns:xsd namespace
			definitions.setAttribute(XMLNS_XSD, XSD_SCHEMA_ELEMENT_TYPE_NAMESPACE_URI);		
			// add xmlns:properties namespace  
			definitions.setAttribute(Utils.createXMLQNameString(XMLNS, PROPERTIES_NAME_LABEL),
					propertyAliasesData.getPropertiesTNS());
			// add xmlns:vprop namespace
			definitions.setAttribute(XMLNS_VPROP, VPROP);
			
			// add NS participant WSDL and <import>
			for (PropertyAliasesDataItem propertyAliasesDataItem : propertyAliasesData
					.getPropertyAliasesData()) {
				// add xmlns: participant name namespace
				definitions.setAttribute(Utils.createXMLQNameString(XMLNS, WSDLUtils.formatParticipantName(
						propertyAliasesDataItem.getParticipantName())),
						propertyAliasesDataItem.getWsdlTNS());
				// add <import> for participant wsdl
				Element importItem = propertiesAliasesWSDLDocument.createElement(WSDL_IMPORT_TAG_NAME);
				// create location attribute
				importItem.setAttribute(LOCATION_LABEL, WSDLUtils
						.createImportLocationValue(propertyAliasesDataItem
						.getWsdlFileName()));
				// create namespace attribute
				importItem.setAttribute(NAMESPACE_LABEL, propertyAliasesDataItem.getWsdlTNS());
				// add importItem to definitions
				definitions.appendChild(importItem);
			}
			
			// add <vprop:propertyAlias> tags
			for (PropertyAliasesDataItem propertyAliasesDataItem : propertyAliasesData
					.getPropertyAliasesData()) {				
				for (PropertyAliasesMessageType propertyAliasesMessageType : propertyAliasesDataItem
						.getPropertyAliasesMessages()) {
					// create <vprop:propertyAlias> 
					Element vpropPropertyAlias = propertiesAliasesWSDLDocument
							.createElement(VPROP_PROPERTY_ALIAS_TAG_NAME);
					// add propertyName attribute
					vpropPropertyAlias.setAttribute(PROPERTY_NAME_ATTRIBUTE_NAME, Utils
							.createXMLQNameString(PROPERTIES_NAME_LABEL, propertyAliasesData
									.getPropertyName()));
					// add messageType attribute
					vpropPropertyAlias.setAttribute(MESSAGE_TYPE_ATTRIBUTE_NAME, Utils
							.createXMLQNameString(WSDLUtils.formatParticipantName(propertyAliasesDataItem
									.getParticipantName()), propertyAliasesMessageType
									.getMessageTypeName()));		
					// add part attribute
					vpropPropertyAlias.setAttribute(PART_ATTRIBUTE_NAME, propertyAliasesMessageType
							.getMessageTypePart());
					// create <vprop:query>
					Element vpropQuery = propertiesAliasesWSDLDocument
							.createElement(VPROP_QUERY_ELEMENT_NAME);
					vpropQuery.setTextContent(VPROP_QUERY_CHOREOGRAPHY_ID_QUERY);
					// add vpropQuery to vpropPropertyAlias
					vpropPropertyAlias.appendChild(vpropQuery);
					// add vpropPropertyAlias to definitions
					definitions.appendChild(vpropPropertyAlias);				
				}				
			}
			
			// add <definitions> to document
			propertiesAliasesWSDLDocument.appendChild(definitions);		
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into createPropertiesAliasesWSDLDocument see log file for details");
		}
		
		// log execution flow
		LOGGER.exit();	
		
		return propertiesAliasesWSDLDocument;
	}
	
	
	public static Document generateCDDeployDocument(List<PartnerLink> partnerLinks, Map<String, 
			WSDLDefinitionData> wsdlDefinitions, WSDLDefinitionData CDWSDLDefinitionData, String cdPrefix, 
			String cdName, Document artifactDocument){
		
		// log execution flow
		LOGGER.entry("CD name: "+cdName);
		
		Document deployDocument = null;
		try {
			deployDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			// create <deploy>
			Element deployElement = deployDocument.createElement(DEPLOY_TAG_NAME);
			// add xmlns namespace
			deployElement.setAttribute(XMLNS, DEPLOY_XMLNS_APACHE_ODE);
			// add <deploy> to deployDocument
			deployDocument.appendChild(deployElement);	
			// create <process>
			Element processElement = deployDocument.createElement(DEPLOY_PROCESS_TAG_NAME);
			// add cdprocess namespace attribute
			deployElement.setAttribute(Utils.createXMLQNameString(XMLNS, CD_PROCESS_PREFIX_NS), cdPrefix);			
			// set <process> name
			processElement.setAttribute(NAME_LABEL, BPELUtils.createProcessNameDeploy(CD_PROCESS_PREFIX_NS,
					cdName));
			// add <process> to <deploy>
			deployElement.appendChild(processElement);
			// create <active>
			Element activeElement = deployDocument.createElement(DEPLOY_ACTIVE_TAG_NAME);
			// set <active> to true
			activeElement.appendChild(deployDocument.createTextNode(TRUE_VALUE));
			// add <active> to <process>
			processElement.appendChild(activeElement);		
			// create <retired>
			Element retiredElement = deployDocument.createElement(DEPLOY_RETIRED_TAG_NAME);			
			// set <retired> to false
			retiredElement.appendChild(deployDocument.createTextNode(FALSE_VALUE));
			// add <retired> to <process>
			processElement.appendChild(retiredElement);	
			// create <process-events>
			Element processEventsElement = deployDocument.createElement(DEPLOY_PROCESS_EVENTS_TAG_NAME);			
			// set generate attribute of <process-events> to all
			processEventsElement.setAttribute(DEPLOY_GENERATE_ATTRIBUTE_NAME,
					DEPLOY_GENERATE_ATTRIBUTE_ALL_VALUE);
			// add <process-events> to <process>
			processElement.appendChild(processEventsElement);	
			String portTypeName = null;
			ServiceDeployBPELData serviceDeployBPELData = null;
			String prefix = null;
			Element serviceElement = null;
			// iterate over every partner link to create <provide> elements
			for (PartnerLink partnerLink : partnerLinks) {
				// myRole defined inside partnerLink
				if(partnerLink.getMyRole() != null){
					// get port type name corresponding to the partner link type and the 
					// role of the partner link
					portTypeName = BPELUtils.getPortTypeNameOfRoleOfPartnerLinkType(artifactDocument,
							partnerLink.getPartnerLinkType().getName(), partnerLink.getMyRole().getName());
					
					// get serviceDeployBPELData corresponding to portTypeName				
					serviceDeployBPELData = WSDLUtils.getServiceDeployBPELDataOfWSDLDefinition(
							CDWSDLDefinitionData.getWsdl(),CDWSDLDefinitionData.getPrefix(), portTypeName);	
									
					// check if the namespace is already defined as attribute of the deploy element 
					// and obtain the prefix
					prefix = CDGenerationUtils.getDeployElementNamespaceAttributePrefix(deployElement, 
							serviceDeployBPELData.getPortName());
					if(prefix == null){
						// namespace not defined inside deploy element 		
						// add namespace attribute to <deploy>
						deployElement.setAttribute(Utils.createXMLQNameString(XMLNS, serviceDeployBPELData
								.getPrefix()), serviceDeployBPELData.getNamespace());
						prefix = serviceDeployBPELData.getPrefix();
					}
					// create <service>
					serviceElement = deployDocument.createElement(DEPLOY_SERVICE_TAG_NAME);
					// set name attribute of <service>
					serviceElement.setAttribute(NAME_LABEL,Utils.createXMLQNameString(prefix, 
							serviceDeployBPELData.getServiceName()));
					// set port attribute of <service>
					serviceElement.setAttribute(DEPLOY_SERVICE_PORT_ATTRIBUTE_NAME,serviceDeployBPELData
							.getPortName());
					// create <provide> 
					Element provideElement = deployDocument.createElement(DEPLOY_PROVIDE_TAG_NAME);
					// set partnerLink attribute of <provide>
					provideElement.setAttribute(DEPLOY_PARTNERLINK_ATTRIBUTE_NAME, partnerLink
							.getName());
					// add <service> to <provide>
					provideElement.appendChild(serviceElement);				
					// add <provide> to <process>
					processElement.appendChild(provideElement);
				}
			}
			// iterate over every partner link to create <invoke> elements
			for (PartnerLink partnerLink : partnerLinks) {			
				// partnerRole defined inside partnerLink	
				if(partnerLink.getPartnerRole() != null){
					portTypeName = BPELUtils.getPortTypeNameOfRoleOfPartnerLinkType(artifactDocument,
							partnerLink.getPartnerLinkType().getName(), partnerLink.getPartnerRole()
							.getName());	
					
					// get serviceDeployBPELData corresponding to portTypeName
					serviceDeployBPELData = WSDLUtils.getServiceDeployBPELDataOfPortType(wsdlDefinitions,
							portTypeName);
					
					// check if the namespace is already defined as attribute of the deploy element 
					// and obtain the prefix
					prefix = CDGenerationUtils.getDeployElementNamespaceAttributePrefix(deployElement, 
							serviceDeployBPELData.getPortName());
					if(prefix == null){
						// namespace not defined inside deploy element 		
						// add namespace attribute to <deploy>
						deployElement.setAttribute(Utils.createXMLQNameString(XMLNS,serviceDeployBPELData
								.getPrefix()), serviceDeployBPELData.getNamespace());
						prefix = serviceDeployBPELData.getPrefix();
					}
					// create <service>
					serviceElement = deployDocument.createElement(DEPLOY_SERVICE_TAG_NAME);
					// set name attribute of <service>
					serviceElement.setAttribute(NAME_LABEL, Utils.createXMLQNameString(prefix, 
							serviceDeployBPELData.getServiceName()));
					// set port attribute of <service>
					serviceElement.setAttribute(DEPLOY_SERVICE_PORT_ATTRIBUTE_NAME, serviceDeployBPELData
							.getPortName());					
					// create <invoke>
					Element invokeElement = deployDocument.createElement(DEPLOY_INVOKE_TAG_NAME); 
					// set partnerLink attribute of <invoke>
					invokeElement.setAttribute(DEPLOY_PARTNERLINK_ATTRIBUTE_NAME, partnerLink
							.getName());
					// set usePeer2Peer attribute of <invoke> to false
					invokeElement.setAttribute(DEPLOY_USEPEER2PEER_ATTRIBUTE_NAME, FALSE_VALUE);					
					// add <service> to <invoke>
					invokeElement.appendChild(serviceElement);				
					// add <invoke> to <process>
					processElement.appendChild(invokeElement);	
				}
			}
			// Create Set Invocation Address process
			// create <process>
			// add SIA namespace attribute to <deploy>
			deployElement.setAttribute(Utils.createXMLQNameString(XMLNS,
					SET_INVOCATION_ADDRESS_DEPLOY_PREFIX_NAME), SET_INVOCATION_ADDRESS_PROCESS_TNS);
			Element processSIAElement = deployDocument.createElement(DEPLOY_PROCESS_TAG_NAME);	
			// add <process> to <deploy>
			deployElement.appendChild(processSIAElement);
			// set <process> name
			processSIAElement.setAttribute(NAME_LABEL, BPELUtils.createProcessNameDeploy(
					SET_INVOCATION_ADDRESS_DEPLOY_PREFIX_NAME, SET_INVOCATION_ADDRESS_PROCESS_NAME));
			// create <process-events>
			Element processEventsSIAElement = deployDocument.createElement(DEPLOY_PROCESS_EVENTS_TAG_NAME);			
			// set generate attribute of <process-events> to all
			processEventsSIAElement.setAttribute(DEPLOY_GENERATE_ATTRIBUTE_NAME,
					DEPLOY_GENERATE_ATTRIBUTE_ALL_VALUE);
			// append processEventsSIAElement on processSIAElement
			processSIAElement.appendChild(processEventsSIAElement);
			// create <provide> 
			Element provideSIAElement = deployDocument.createElement(DEPLOY_PROVIDE_TAG_NAME);			
			// set partnerLink attribute of <provide>
			provideSIAElement.setAttribute(DEPLOY_PARTNERLINK_ATTRIBUTE_NAME, 
					SET_INVOCATION_ADDRESS_PARTNER_LINK_NAME);	
			// append provideSIAElement to processSIAElement
			processSIAElement.appendChild(provideSIAElement);
			// create <service>
			Element serviceSIAElement = deployDocument.createElement(DEPLOY_SERVICE_TAG_NAME);		
			// set name attribute of <service>
			serviceSIAElement.setAttribute(NAME_LABEL, Utils.createXMLQNameString(
					SET_INVOCATION_ADDRESS_DEPLOY_PREFIX_NAME, cdName+BASE_SERVICE_SUFFIX));
			// set port attribute of <service>
			serviceSIAElement.setAttribute(DEPLOY_SERVICE_PORT_ATTRIBUTE_NAME, 
					SET_INVOCATION_ADDRESS_SERVICE_PORT_NAME);			
			// append serviceSIAElement to provideSIAElement
			provideSIAElement.appendChild(serviceSIAElement);
			
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into generateProsumerDeployDocument see log file for details ");
		}	
		
		// log execution flow
		LOGGER.exit();
		
		return deployDocument;			
	}
	
	public static String getDeployElementNamespaceAttributePrefix(Element deployElement, String namespace){
		
		String prefix = null;
		// iterate over all the attributes of the deploy element
		for (int i = 0; i < deployElement.getAttributes().getLength(); i++) {
			Node node = deployElement.getAttributes().item(i);
			// check if the attribute has namespace as value
			if(node.getNodeValue().equals(namespace)){
				// get the prefix
				prefix = Utils.getLocalPartXMLQName(node.getNodeName());
				break;
			}
		}
		return prefix;
	}
	
	public static BPELData generateCDBPELprocess(ChoreographyData choreography, String participantName){
		
		
		// Process
		Process process = BPELFactory.eINSTANCE.createProcess();
		process.setName("cdtouristagent.bpel");
		process.setTargetNamespace(TARGET_NS_CD_DEFAULT);
		// Imports 
		Import importItem = BPELFactory.eINSTANCE.createImport();
		importItem.setImportType("1");
		importItem.setLocation("1");
		importItem.setNamespace("1");
		process.getImports().add(importItem);
		Import importItem2 = BPELFactory.eINSTANCE.createImport();
		importItem2.setImportType("2");
		importItem2.setLocation("2");
		importItem2.setNamespace("2");
		process.getImports().add(importItem2);
		// Partner Link
		PartnerLink ptnl = BPELFactory.eINSTANCE.createPartnerLink();
		ptnl.setName("partnerLinkName");
		PartnerLinkType ptnlt = PartnerlinktypeFactory.eINSTANCE.createPartnerLinkType();
		ptnlt.setName("a:partnerLinkTypeName");
		ptnl.setPartnerLinkType(ptnlt);
		Role role = PartnerlinktypeFactory.eINSTANCE.createRole();
		role.setName("roleName");
//		Object portTypeName = "portTypeName";
//		role.setPortType(portTypeName);
		ptnl.setMyRole(role);
		ptnl.setPartnerRole(role);
		
		PartnerLinks partnerLinks = BPELFactory.eINSTANCE.createPartnerLinks();
		partnerLinks.getChildren().add(ptnl);
		process.setPartnerLinks(partnerLinks);
			
		// Variable
		Variable variable = BPELFactory.eINSTANCE.createVariable();
		variable.setName("variableName");
		org.eclipse.wst.wsdl.Message message = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE.createMessage();
		message.setQName(new QName("ns", "messageType"));
		variable.setMessageType(message);
		Variables variables = BPELFactory.eINSTANCE.createVariables();
		variables.getChildren().add(variable);
		
		// choreographyID variable
//		XSDSchema schemaForSchemas = XSDUtil.getSchemaForSchema(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
//		XSDTypeDefinition xsdTypeDefinition = schemaForSchemas.resolveSimpleTypeDefinition("string");
//		xsdTypeDefinition.setTargetNamespace("xsd");
//		Variable variableChorID = BPELFactory.eINSTANCE.createVariable();
//		variableChorID.setName("choreographyID");
//		variableChorID.setType(xsdTypeDefinition);
//		variables.getChildren().add(variableChorID);
							
		process.setVariables(variables);

		// create correlation sets of the process
		CorrelationSets correlationSets = BPELFactory.eINSTANCE.createCorrelationSets();	
		process.setCorrelationSets(correlationSets);		
		// create correlation set
		CorrelationSet correlationSet = BPELFactory.eINSTANCE.createCorrelationSet();
		correlationSet.setName("CorrelationSetChoreographyID");
		Property property = MessagepropertiesFactory.eINSTANCE.createProperty();
		property.setName("property:chorID");
		correlationSet.getProperties().add(property);	
		// add correlation set to the correlation sets of the process
		process.getCorrelationSets().getChildren().add(correlationSet);
		
			
		// Receive
		Receive receive = BPELFactory.eINSTANCE.createReceive();
		receive.setCreateInstance(true);
		//receive.setOperation(value);
		receive.setPartnerLink(ptnl);
		org.eclipse.wst.wsdl.PortType portType = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE.createPortType(); 
		portType.setQName(new QName("portType"));
		receive.setPortType(portType);
		receive.setVariable(variable);
		
		// create correlation set
		CorrelationSet correlationSet2 = BPELFactory.eINSTANCE.createCorrelationSet();
		correlationSet2.setName("CorrelationSetChoreographyID");
		// create correlation
		Correlation correlation = BPELFactory.eINSTANCE.createCorrelation();
		correlation.setSet(correlationSet2);
		correlation.setInitiate("join");
		// crete correlations
		Correlations correlations = BPELFactory.eINSTANCE.createCorrelations();
		correlations.getChildren().add(correlation);
		
		// set correlations to receive
		receive.setCorrelations(correlations);
		
		//process.setActivity(receive);
		
		Variable varForEach = BPELFactory.eINSTANCE.createVariable();
		varForEach.setName("varForEach");

		// Invoke
		Invoke invoke = BPELFactory.eINSTANCE.createInvoke();
		invoke.setName("Invoke_touristagent_receiveGetTripPlanRequest");
		invoke.setPartnerLink(ptnl);
		org.eclipse.wst.wsdl.Operation operation = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE
				.createOperation();
		operation.setName("operation");
		invoke.setOperation(operation);
		invoke.setInputVariable(variable);
		
		// Invoke
		Invoke invoke2 = BPELFactory.eINSTANCE.createInvoke();
		invoke2.setName("Invoke_touristagent_receiveGetTripPlanRequest");
		invoke2.setPartnerLink(ptnl);
		org.eclipse.wst.wsdl.Operation operation2 = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE
				.createOperation();
		operation2.setName("operation");
		invoke2.setOperation(operation2);
		invoke2.setInputVariable(variable);			
		
//		ForEach forEach = BPELFactory.eINSTANCE.createForEach();
//		forEach.setParallel(false);
//		forEach.setName("forEachLoop");
//		forEach.setCounterName(varForEach);
//		Expression startExpression = BPELFactory.eINSTANCE.createExpression();
//		startExpression.setBody("1");
//		startExpression.setExpressionLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
//		forEach.setStartCounterValue(startExpression);
//		Expression finalExpression = BPELFactory.eINSTANCE.createExpression();
//		finalExpression.setBody("count($cdSEADA-TRAFFIC_getRouteTrafficInformation_Input.parameters"
//				+ "/messageData/routeSegments)");	
//		finalExpression.setExpressionLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
//		forEach.setFinalCounterValue(finalExpression);
//		Scope scope = BPELFactory.eINSTANCE.createScope();		
//		forEach.setActivity(scope);
		org.eclipse.bpel.model.Sequence sequenceForEach = BPELFactory.eINSTANCE.createSequence();
		sequenceForEach.getActivities().add(invoke2);
		//scope.setActivity(sequenceForEach);	
		
		While while1 = BPELFactory.eINSTANCE.createWhile();
		Condition condition2 = BPELFactory.eINSTANCE.createCondition();
		condition2.setExpressionLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
		condition2.setBody("\'true\'");
		while1.setCondition(condition2);
		while1.setActivity(sequenceForEach);
	
			
		//process.setActivity(invoke);
		
		// Assign
		Assign assign = BPELFactory.eINSTANCE.createAssign();
		List<Copy> copies = assign.getCopy();
		Copy copy = BPELFactory.eINSTANCE.createCopy();
		From from = BPELFactory.eINSTANCE.createFrom();
	
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder;
//		try {
//			builder = dbf.newDocumentBuilder();
//	        Document doc = builder.newDocument();
//	        Element element = doc.createElement("root");
//	        element.setAttribute("expressionLanguage", "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath2.0");
//			from.setElement(element);
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Expression expression = BPELFactory.eINSTANCE.createExpression();
		expression.setExpressionLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath2.0");	
		from.setExpression(expression);
		
//		from.setLiteral(message.toString());
	
		Query query = BPELFactory.eINSTANCE.createQuery();
		query.setQueryLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
		query.setValue("choreographyId/choreographyId");

		org.eclipse.wst.wsdl.Part part2 = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE.createPart();
		part2.setName("parameters");
		from.setPart(part2);
		from.setVariable(variable);
		
		from.setQuery(query);
		
		To to = BPELFactory.eINSTANCE.createTo();
		to.setVariable(variable);
		org.eclipse.wst.wsdl.Part part = org.eclipse.wst.wsdl.WSDLFactory.eINSTANCE.createPart();
		part.setName("parameters");
		to.setPart(part);
		copy.setFrom(from);
		copy.setTo(to);
		copies.add(copy);
		
		// If
		If ifItem = BPELFactory.eINSTANCE.createIf();
		Condition condition = BPELFactory.eINSTANCE.createCondition();
		condition.setExpressionLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
		//TODO find a way to define condition body
		condition.setBody("conditionBody");
		ifItem.setCondition(condition);
		ifItem.setName("if");
		org.eclipse.bpel.model.Sequence sequence2 = BPELFactory.eINSTANCE.createSequence();
		sequence2.setName("sequence2");
		sequence2.getActivities().add(receive);
		ifItem.setActivity(sequence2);
	
		// Else
		Else elseItem = BPELFactory.eINSTANCE.createElse(); 
		elseItem.setActivity(invoke);
		ifItem.setElse(elseItem);

		// ElseIf
		ElseIf elseIf = BPELFactory.eINSTANCE.createElseIf(); 
//		elseIf.setActivity(value);
		
		
		// Flow
		Flow flow = BPELFactory.eINSTANCE.createFlow();
		flow.setName("flow");
		
		// Pick
		Pick pick = BPELFactory.eINSTANCE.createPick();
		pick.setCreateInstance(false);
		OnMessage onMessage = BPELFactory.eINSTANCE.createOnMessage();
		onMessage.setPartnerLink(ptnl);
		onMessage.setOperation(operation);
		onMessage.setPortType(portType);
		onMessage.setVariable(variable);
//		pick.getMessages().add(onMessage);
		
		Reply reply = BPELFactory.eINSTANCE.createReply();
		reply.setName("Reply");
		reply.setOperation(operation);
		reply.setPartnerLink(ptnl);
		reply.setPortType(portType);
		reply.setVariable(variable);
		
		org.eclipse.bpel.model.Sequence sequenceOnMessage = BPELFactory.eINSTANCE.createSequence();
		sequenceOnMessage.getActivities().add(reply);
		
		onMessage.setActivity(sequenceOnMessage);
		
		pick.getMessages().add(onMessage);

		//process.setActivity(assign);
		
		Wait wait = BPELFactory.eINSTANCE.createWait();
		Expression waitExpressionFor = BPELFactory.eINSTANCE.createExpression();
		waitExpressionFor.setBody("\'PT5S\'");
		wait.setFor(waitExpressionFor);

		org.eclipse.bpel.model.Sequence sequence = BPELFactory.eINSTANCE.createSequence();
		sequence.setName("sequence");
		sequence.getActivities().add(ifItem);
		sequence.getActivities().add(pick);
//		sequence.getActivities().add(receive);
		sequence.getActivities().add(assign);
//		sequence.getActivities().add(invoke);
//		sequence.getActivities().add(forEach);
		sequence.getActivities().add(while1);
		sequence.getActivities().add(wait);
				
		
		process.setActivity(sequence);
		
		sequence.getActivities().add(BPELUtils.createAssignStringVariable(process, variable,
				"SEADA-SEATSA"));
		
		sequence.getActivities().add(BPELUtils.createAssignFromSIAManagerToPartnerLink(ptnl, variable, 
				variable, variable));
	
		// Imported namespace that will be added to process tag
		HashMap<String, String> importedNamespacesData = new HashMap<>();
		importedNamespacesData.put("bpel","http://docs.oasis-open.org/wsbpel/2.0/process/executable");
		importedNamespacesData.put("ns","http://it.softeco.chorevolution.smt/cd/touristagentArtifacts");
		importedNamespacesData.put("ns0","http://it.softeco.chorevolution.smt/cd/touristagent");
		importedNamespacesData.put("touristagent","http://it.softeco.chorevolution.smt/touristagent");
		importedNamespacesData.put("poi","http://it.softeco.chorevolution.smt/poi");
		importedNamespacesData.put("tripplanner","http://it.softeco.chorevolution.smt/cd/tripplanner");
		importedNamespacesData.put("news","http://it.softeco.chorevolution.smt/news");
		importedNamespacesData.put("xsd","http://www.w3.org/2001/XMLSchema");
		BPELData bpelData = new BPELData();
		bpelData.setProcess(process);
		bpelData.setImportedNamespacesData(importedNamespacesData);
		return bpelData;
	}
	
	public static BPELData createCDProsumerBpelProcessFromChoreography(String cdParticipantName,
			String cdName, String choreographyTNS, List<String> prosumersParticipantsNames, 
			List<String> clientProsumersParticipantsNames, Choreography choreography,
			List<Choreography> choreographies, Map<String, WSDLDefinitionData> wsdlDefinitions,
			WSDLDefinitionData cdWSDLDefinitionData, WSDLDefinitionData consumerPartWSDLDefinitionData,
			ArtifactData artifactData){
		
		// log execution flow
		LOGGER.entry("CD Prosumer name: "+cdName+" CD Prosumer participant name: "+cdParticipantName);
		
		LOGGER.info("Creating "+cdName+" Prosumer Bpel process");
		
		BPELData bpelData = new BPELData();
		StartEvent startEvent = BPMNUtils.getStartEvent(choreography);
		ArrayDeque<BPELExtensibleElement> bpelElements = new ArrayDeque<>();
		ArrayDeque<org.eclipse.bpel.model.Sequence> sequences = new ArrayDeque<>();		
		Process process = BPELFactory.eINSTANCE.createProcess();
		process.setName(cdName);		
		process.setTargetNamespace(BPELUtils.createCDBpelProcessTargetNamespace(BPMNUtils
				.getTargetNamespaceChoreography(choreography), cdName));
		org.eclipse.bpel.model.Sequence mainSequence = BPELFactory.eINSTANCE.createSequence();
		mainSequence.setName(MAIN_SEQUENCE_LABEL);
		
		// create correlationSets of the process
		CorrelationSets correlationSets = BPELFactory.eINSTANCE.createCorrelationSets();	
		process.setCorrelationSets(correlationSets);		
		// create correlation set for choreographyID
		CorrelationSet choreographyIDCorrelationSet = BPELFactory.eINSTANCE.createCorrelationSet();
		choreographyIDCorrelationSet.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
		Property property = MessagepropertiesFactory.eINSTANCE.createProperty();
		property.setName(Utils.createXMLQNameString(PROPERTIES_PREFIX, CHOREOGRAPHY_ID_PROPERTY_LABEL));
		choreographyIDCorrelationSet.getProperties().add(property);	
		// add choreographyIDCorrelationSet to the correlation sets of the process
		process.getCorrelationSets().getChildren().add(choreographyIDCorrelationSet);

		// create properties TNS
		String propertiesTNS = WSDLUtils.createNamespace(BPMNUtils
				.getTargetNamespaceChoreography(choreography), PROPERTIES_NAME_LABEL);	
		// create propertyAliasesData
		PropertyAliasesData propertyAliasesData = new PropertyAliasesData(CHOREOGRAPHY_ID_PROPERTY_LABEL, 
				propertiesTNS);		
			
		// create main Pick
		Pick mainPick = BPELFactory.eINSTANCE.createPick();
		mainPick.setCreateInstance(true);
		mainPick.setName(PICK_NAME);
		// add main Pick to Sequence
		mainSequence.getActivities().add(mainPick);
		process.setActivity(mainSequence);
		bpelElements.add(process);
		// add main Pick to bpelElements
		bpelElements.add(mainPick);
		
		// create imports and importedNamespacesData
		bpelData.setImportedNamespacesData(BPELUtils
				.addWSDLImportsToCDProsumerBpelProcessAndGetImportNamespaceData(process, choreographyTNS, 
						wsdlDefinitions, cdWSDLDefinitionData, consumerPartWSDLDefinitionData, artifactData, 
						propertiesTNS));
	
		// initialize process variables
		Variables variables = BPELFactory.eINSTANCE.createVariables();
		process.setVariables(variables);
		// initialize partner links of the process
		PartnerLinks partnerLinks = BPELFactory.eINSTANCE.createPartnerLinks();
		process.setPartnerLinks(partnerLinks);	
				
		Participant cdParticipant = BPMNUtils.getParticipantFromName(choreographies, cdParticipantName);
		
		// create the set containing the ids of the flow nodes processed
		Set<String> processedFlowNodes = new HashSet<>();
	
		// create the queue containing the data of the looped flow nodes
		Deque<LoopedFlowNodeData> loopedFlowNodes = new ArrayDeque<>();
		
		// create the queue containing the data of the timers
		Deque<TimerData> timersData = new ArrayDeque<>();		

		// create the queue containing the ids of the traversed flow nodes
		Deque<String> currentPath = new ArrayDeque<>();
		// add the id of choreography to currentPath
		currentPath.addLast(choreography.getId());
		
		// TODO find out a way to manage the situation in which a start event has more than one 
		// outgoing, if possible
		CDGenerationUtils.createCDProsumerBpelProcess(processedFlowNodes, startEvent.getOutgoing().get(0)
				.getTargetRef(), choreographies, choreography, cdParticipant, cdName,
				prosumersParticipantsNames, clientProsumersParticipantsNames, process, sequences,
				bpelElements, wsdlDefinitions, cdWSDLDefinitionData, consumerPartWSDLDefinitionData,
				artifactData, propertyAliasesData, loopedFlowNodes, timersData, currentPath);
		
		Process processFinal = (Process) bpelElements.pollFirst();

		bpelData.setProcess(processFinal);
		bpelData.setPropertyAliasesData(propertyAliasesData);
		
		// log execution flow
		LOGGER.exit();
		
		return bpelData;
	}
			
	public static void createCDProsumerBpelProcess(Set<String> processedFlowNodes, FlowNode flowNode, 
			List<Choreography> choreographies, Choreography currentChoreography, Participant cdParticipant,
			String cdName, List<String> prosumersParticipantsNames,
			List<String> clientProsumersParticipantsNames, Process process,
			Deque<org.eclipse.bpel.model.Sequence> sequences, Deque<BPELExtensibleElement> bpelElements, 
			Map<String, WSDLDefinitionData> wsdlDefinitions, WSDLDefinitionData cdWSDLDefinitionData, 
			WSDLDefinitionData prosumerPartWSDLDefinitionData, ArtifactData artifactData, 
			PropertyAliasesData propertyAliasesData, Deque<LoopedFlowNodeData> loopedFlowNodes,
			Deque<TimerData> timersData, Deque<String> currentPath){
		
		// log execution flow
		LOGGER.entry("CD Prosumer Name: "+cdName+" CD Prosumer participant name: "
				+cdParticipant.getName()+" Flow Node name: "+flowNode.getName()
				+" Client Prosumer Participant "+ clientProsumersParticipantsNames );
		
		// flowNode is an end event
		if(flowNode instanceof EndEvent){
			// check if timersData is not empty and if the last element has the same
			// choreography name as the currentChoreography
			if(!timersData.isEmpty() && 
				timersData.getLast().getChoreographyName().equals(currentChoreography.getName())) {
				// timersData is not empty and the last element has the same
				// choreography name as the currentChoreography
				// create wait with for expression
				BPELUtils.createWaitWithForExpression(timersData.getLast().getTimerValue(),
						sequences.getLast());
				// remove the last element from timersData
				timersData.pollLast();
				// remove the last element from sequences
				sequences.pollLast();
				// remove the last element from bpelElements
				bpelElements.pollLast();
			}
			// check if loopedFlowNodes is not empty and if the last element has the same
			// name as the currentChoreography
			if(!loopedFlowNodes.isEmpty() && 
				loopedFlowNodes.getLast().getName().equals(currentChoreography.getName())) {
				// loopedFlowNodes is not empty and the last element has the same
				// name as currentChoreography so it has to removed
				loopedFlowNodes.pollLast();
				// remove the last element from sequences
				sequences.pollLast();
				// remove the last element from bpelElements
				bpelElements.pollLast();
			}
			// check if there is at least one element in currentPath
			if(currentPath.size() > 1) {
				// there is at least one element in currentPath
				// remove the last element of currentPath
				currentPath.removeLast();
			}
			return;
		}
		// check if flowNode has been already processed
		if(BPMNUtils.hasFlowNodeProcessed(flowNode, currentPath, processedFlowNodes)) {
			// flowNode has been already processed skip it
			return;		
		}
		// add flowNodePath to flowNodesProcessed
		processedFlowNodes.add(BPMNUtils.createFlowNodePath(flowNode, currentPath));		
		// check if flowNode is a IntermediateCatchEvent and if the timer has to be implemented
		if(flowNode instanceof IntermediateCatchEvent &&
			BPMNUtils.hasTimerToBeImplemented(flowNode, cdParticipant.getName())) {
			// get the first event definition of the IntermediateCatchEvent as a TimerEventDefinition
			TimerEventDefinition timerEventDefinition = (TimerEventDefinition) 
					((IntermediateCatchEvent) flowNode).getEventDefinitions().get(0);						
			// flowNode is a IntermediateCatchEvent			
			BPELUtils.createTimerActivities(timerEventDefinition, sequences, bpelElements);
			// add the data related to the timer to timersData
			timersData.addLast(new TimerData(currentChoreography.getName(),
					((FormalExpressionImpl) timerEventDefinition.getTimeDuration()).getBody()));
		}
		// check if flowNode is a sub-choreography
		if(flowNode instanceof SubChoreography){
			// flowNode is a sub-choreography
			
			// log sub-choreography  name
			LOGGER.info("CD Prosumer Name: "+cdName+" CD Prosumer participant name: "
					+cdParticipant.getName()+" Sub Choreography name: "+flowNode.getName()); 
						
			// check if the subchoreography is looped and if the related loop has to be implemented
			if(((SubChoreography) flowNode).getLoopType().compareTo(ChoreographyLoopType.NONE) != 0
				&& BPMNUtils.hasSubChoreographyLoopToBeImplementedIntoCDProsumer(currentChoreography, 
						(SubChoreography) flowNode, cdParticipant, choreographies)) {
				// the subchoreography is looped and the related loop has to be implemented		
				// create loop activities
				BPELUtils.createCDProsumerLoopActivities(flowNode, process, cdParticipant.getName(),
						wsdlDefinitions, cdWSDLDefinitionData, prosumerPartWSDLDefinitionData,
						choreographies, sequences, bpelElements, ((SubChoreography) flowNode)
						.getInitiatingParticipantRef().equals(cdParticipant));
			}
			// check if the subchoreography is looped 
			if(((SubChoreography) flowNode).getLoopType().compareTo(ChoreographyLoopType.NONE) != 0) {
				// check if participant is the initiating participant of the subchoreography
				if(((SubChoreography) flowNode).getInitiatingParticipantRef().equals(cdParticipant)) {
					// participant is the initiating participant of the subchoreography	
					// add the LoopedFlowNodeData (name, variableName) related to the subchoreography 
					// as last element of loopedFlowNodes
					loopedFlowNodes.addLast(new LoopedFlowNodeData(flowNode.getName(), BPELUtils
							.createLoopVariableName(StringUtils.deleteWhitespace(flowNode.getName()))));				
				}
				else {
					// participant is one of the receiver participants of the subchoreography	
					// add the LoopedFlowNodeData (name) related to the subchoreography 
					// as last element of loopedFlowNodes
					loopedFlowNodes.addLast(new LoopedFlowNodeData(flowNode.getName()));						
				}			
			}		
			// retrieve the sub-choreography 
			Choreography subChoreography = BPMNUtils
					.getChoreography(choreographies, ((SubChoreography)flowNode).getName());	
			// add the id of flowNode to the currentPath
			currentPath.addLast(flowNode.getId());		
			// get sub-choregraphy start event
			StartEvent subChoreographyStartEvent = BPMNUtils.getStartEvent(subChoreography);
			// TODO find out a way to manage the situation in which a start event has more than one outgoing  
			// if possible
			// call createBpelProcess with the targetRef of the first outgoing of subChoreographyStartEvent
			createCDProsumerBpelProcess(processedFlowNodes, subChoreographyStartEvent.getOutgoing().get(0)
					.getTargetRef(), choreographies, subChoreography, cdParticipant, cdName,
					prosumersParticipantsNames, clientProsumersParticipantsNames, process, sequences,
					bpelElements, wsdlDefinitions, cdWSDLDefinitionData, prosumerPartWSDLDefinitionData,
					artifactData, propertyAliasesData, loopedFlowNodes, timersData, currentPath);
		}
		
		// flowNode is a choreography task
		if(flowNode instanceof ChoreographyTask){
			
			// log choreography task name
			LOGGER.info("CD Prosumer Name: "+cdName+" CD Prosumer participant name: "
					+cdParticipant.getName()+" Choreography task name: "+flowNode.getName());

			boolean isProsumerClientParticipant = false;
			// cd is the receiving participant of the task			
			if(BPMNUtils.getReceivingParticipant((ChoreographyActivity) flowNode).equals(cdParticipant)){	
				// check if the initiating participant is the prosumer client participant
				isProsumerClientParticipant = clientProsumersParticipantsNames.contains(BPMNUtils
						.getInitiatingParticipant((ChoreographyActivity) flowNode).getName());
				// create CD Prosumer receiving choreography task activities						
				BPELUtils.createCDProsumerReceivingChoreographyTaskActivities(cdName, cdParticipant,
						choreographies, (ChoreographyTask) flowNode, cdWSDLDefinitionData,
						prosumerPartWSDLDefinitionData, wsdlDefinitions, process, artifactData,
						bpelElements, sequences, propertyAliasesData, isProsumerClientParticipant,
						loopedFlowNodes);
			}
			// cd is initiating participant of the task
			else{
				// get receiverParticipantName
				String receiverParticipantName = BPMNUtils.getReceivingParticipant(
						(ChoreographyTask) flowNode).getName();
				// check if the receiver participant is a prosumer
				boolean isReceiverParticipantProsumer = Utils.isParticipantProsumer(receiverParticipantName, 
						prosumersParticipantsNames);
				// check if the receiving participant is the prosumer client participant
				isProsumerClientParticipant = clientProsumersParticipantsNames
						.contains(receiverParticipantName);
				// get the WSDL of the receiving participant of the task
				WSDLDefinitionData receiverWSDLDefinitionData = wsdlDefinitions.get(receiverParticipantName);
				// create CD Prosumer initiating choreography task activities	
				BPELUtils.createCDProsumerInitiatingChoreographyTaskActivities(cdParticipant,
						choreographies, (ChoreographyTask) flowNode, isReceiverParticipantProsumer,
						isProsumerClientParticipant, cdWSDLDefinitionData, prosumerPartWSDLDefinitionData,
						wsdlDefinitions, process, artifactData, bpelElements, sequences,
						receiverWSDLDefinitionData, propertyAliasesData, loopedFlowNodes);
			}
		}
		
		// flowNode is a gateway
		if(flowNode instanceof Gateway){			
			if(((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.CONVERGING)==0){
				// flowNode is a converging gateway so remove the last element of sequences and the 
				// last element of bpelElements
				if(!(bpelElements.getLast() instanceof Process)){
					sequences.pollLast();
					bpelElements.pollLast();
				}
			}		
			// flowNode is a diverging exclusive gateway 				
			// check if it has to be implemented as an IF BPEL 
			if(flowNode instanceof ExclusiveGateway &&				
			   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0 &&
				BPMNUtils.hasExclusiveGatewayToBeImplemented(flowNode, cdParticipant)){					
					// create IF BPEL					
					If ifItem = BPELFactory.eINSTANCE.createIf();
					// check if flow node has name
					if(flowNode.getName() != null){
						// set if name
						ifItem.setName(BPELUtils.createIfName(flowNode.getName()));	
					}				
					// attach the IF BPEL just created to the last sequence of sequences
					sequences.getLast().getActivities().add(ifItem);					
					// add IF BPEL as the last element of bpelElements 
					bpelElements.addLast(ifItem);	
			}			
			// flowNode is a parallel diverging gateway 
			if(flowNode instanceof ParallelGateway &&			
				((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0){					
					// create FLOW BPEL element
					Flow flow = BPELFactory.eINSTANCE.createFlow();
					flow.setName(flowNode.getName());
					// attach the FLOW BPEL just created to the last sequence of sequences					
					sequences.getLast().getActivities().add(flow);				
					// add FLOW BPEL as the last element of bpelElements
					bpelElements.addLast(flow);
			}	
		}	
		
		// iterate over the outgoing of flowNode
		for (int i = 0; i < flowNode.getOutgoing().size(); i++){
						
			// check if it flowNode is an exclusive diverging gateway that has to be implemented  
			if(flowNode instanceof ExclusiveGateway &&				
			   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0 &&
				BPMNUtils.hasExclusiveGatewayToBeImplemented(flowNode, cdParticipant)){	
				// flowNode is a diverging exclusive gateway that has to be implemented	
				// create a BPEL sequence
				org.eclipse.bpel.model.Sequence sequenceIfEl = BPELFactory.eINSTANCE.createSequence();
				sequenceIfEl.setName(BPELUtils.createSequenceIfName(String.valueOf(i)));
				// first outgoing of a diverging exclusive gateway
				if(i == 0){
					// the last element of bpelElement is a bpel if item
					// attach as activity the sequence sequenceIfEl to it 
					((If) bpelElements.getLast()).setActivity(sequenceIfEl);
					// set if condition
					((If) bpelElements.getLast()).setCondition(BPELUtils
							.createCDProsumerXPathConditionOutgoingDivergingExclusiveGateway(process, 
									cdParticipant.getName(), wsdlDefinitions, cdWSDLDefinitionData, 
									prosumerPartWSDLDefinitionData, choreographies, flowNode.getOutgoing()
									.get(i)));				
					// create a new instance of sequences
					ArrayDeque<org.eclipse.bpel.model.Sequence> sequencesNew = new ArrayDeque<>();
					// copy all the elements of sequences into sequencesNew
					sequencesNew.addAll(sequences);
					// add to sequencesNew the sequence sequenceIfEl as last element
					sequencesNew.addLast(sequenceIfEl);
					// create a new instance of bpelElements
					ArrayDeque<BPELExtensibleElement> bpelElementsNew = new ArrayDeque<>();
					// copy all the elements of bpelElements into bpelElementsNew
					bpelElementsNew.addAll(bpelElements);				
					// create a new instance of loopedFlowNodes
					Deque<LoopedFlowNodeData> loopedFlowNodesNew = new ArrayDeque<>();
					// copy all the elements of loopedFlowNodes into loopedFlowNodesNew
					loopedFlowNodesNew.addAll(loopedFlowNodes);					
					// create a new instance of currentPath
					Deque<String> currentPathNew = new ArrayDeque<>();
					// copy all the elements of currentPath into currentPathNew			
					currentPathNew.addAll(currentPath);					
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequencesNew, bpelElementsNew, loopedFlowNodesNew and currentPathNew
					createCDProsumerBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							prosumersParticipantsNames, clientProsumersParticipantsNames, process,
							sequencesNew, bpelElementsNew, wsdlDefinitions, cdWSDLDefinitionData,
							prosumerPartWSDLDefinitionData, artifactData, propertyAliasesData,
							loopedFlowNodesNew, timersData, currentPathNew);
				}
				// outgoing, other than the first, of a diverging exclusive gateway 
				else {
					// create an elseif bpel
					ElseIf elseIf = BPELFactory.eINSTANCE.createElseIf(); 
					// set as activity of elseif bpel sequenceIfEl
					elseIf.setActivity(sequenceIfEl);					
					// set else if condition
					elseIf.setCondition(BPELUtils
							.createCDProsumerXPathConditionOutgoingDivergingExclusiveGateway(process, 
									cdParticipant.getName(), wsdlDefinitions, cdWSDLDefinitionData, 
									prosumerPartWSDLDefinitionData, choreographies,
									flowNode.getOutgoing().get(i)));					
					// attach elseif bpel to elseIf list of the last element of bpelElements
					((If) bpelElements.getLast()).getElseIf().add(elseIf);
					// create a new instance of sequences					
					ArrayDeque<org.eclipse.bpel.model.Sequence> sequencesNew = new ArrayDeque<>();
					// copy all the elements of sequences into sequencesNew
					sequencesNew.addAll(sequences);
					// add to sequencesNew the sequence sequenceIfEl as last element
					sequencesNew.addLast(sequenceIfEl);
					// create a new instance of bpelElements				
					ArrayDeque<BPELExtensibleElement> bpelElementsNew = new ArrayDeque<>();
					// copy all the elements of bpelElements into bpelElementsNew
					bpelElementsNew.addAll(bpelElements);	
					// create a new instance of loopedFlowNodes
					Deque<LoopedFlowNodeData> loopedFlowNodesNew = new ArrayDeque<>();
					// copy all the elements of loopedFlowNodes into loopedFlowNodesNew
					loopedFlowNodesNew.addAll(loopedFlowNodes);					
					// create a new instance of currentPath
					Deque<String> currentPathNew = new ArrayDeque<>();
					// copy all the elements of currentPath into currentPathNew			
					currentPathNew.addAll(currentPath);					
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequencesNew, bpelElementsNew, loopedFlowNodesNew and currentPathNew
					createCDProsumerBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							prosumersParticipantsNames, clientProsumersParticipantsNames, process,
							sequencesNew, bpelElementsNew, wsdlDefinitions, cdWSDLDefinitionData,
							prosumerPartWSDLDefinitionData, artifactData, propertyAliasesData,
							loopedFlowNodesNew, timersData, currentPathNew);
				}			
			}
			else{
				// check if flowNode is a parallel diverging gateway
				if(flowNode instanceof ParallelGateway && 
					// flowNode is a parallel diverging gateway
				   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0){
					// create a sequence
					org.eclipse.bpel.model.Sequence sequenceFlowEl = BPELFactory.eINSTANCE.createSequence();
					sequenceFlowEl.setName(BPELUtils.createSequenceFlowName(String.valueOf(i)));
					// attach sequenceFlowEl as an activity of the last element of bpelElements	
					((Flow) bpelElements.getLast()).getActivities().add(sequenceFlowEl);
					// create a new instance of sequences	
					ArrayDeque<org.eclipse.bpel.model.Sequence> sequencesNew = new ArrayDeque<>();
					// copy all the elements of sequences into sequencesNew
					sequencesNew.addAll(sequences);
					// add to sequencesNew the sequence sequenceFlowEl as last element
					sequencesNew.addLast(sequenceFlowEl);
					// create a new instance of bpelElements
					ArrayDeque<BPELExtensibleElement> bpelElementsNew = new ArrayDeque<>();
					// copy all the elements of bpelElements into bpelElementsNew
					bpelElementsNew.addAll(bpelElements);	
					// create a new instance of loopedFlowNodes
					Deque<LoopedFlowNodeData> loopedFlowNodesNew = new ArrayDeque<>();
					// copy all the elements of loopedFlowNodes into loopedFlowNodesNew
					loopedFlowNodesNew.addAll(loopedFlowNodes);						
					// create a new instance of currentPath
					Deque<String> currentPathNew = new ArrayDeque<>();
					// copy all the elements of currentPath into currentPathNew			
					currentPathNew.addAll(currentPath);								
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequencesNew, bpelElementsNew, loopedFlowNodesNew and currentPathNew
					createCDProsumerBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							prosumersParticipantsNames, clientProsumersParticipantsNames, process,
							sequencesNew, bpelElementsNew, wsdlDefinitions, cdWSDLDefinitionData,
							prosumerPartWSDLDefinitionData, artifactData, propertyAliasesData,
							loopedFlowNodesNew, timersData, currentPathNew);
				}			
				else{
					// flowNode is an event based gateway
					if(flowNode instanceof EventBasedGateway){
						// take the target reference of the i-est outgoing of the event based gateway
						// it is assumed that every outgoing of an event based gateway is either 
						// a choreography task or an end event
						// TODO check if this assumption is generally valid 
						// check if the io-est outgoing of the event based gateway is a ChoreographyTask								
							if(flowNode.getOutgoing().get(i).getTargetRef() instanceof ChoreographyTask){
								ChoreographyTask choreographyTask = (ChoreographyTask) flowNode
										.getOutgoing().get(i).getTargetRef();								
								// check if choreography task has not been processed yet	
								if(!BPMNUtils.hasFlowNodeProcessed(choreographyTask, currentPath,
										processedFlowNodes)) {
									// choreography task has not been processed yet 
									// it is assumed that every choreography task outgoing from an event
									// based gateway involves a prosumer participant as a receiver 
									// participant
									// TODO check if this assumption is generally valid 
									// check if the initiating participant is the prosumer client 
									// participant          
									boolean isProsumerClientParticipant = clientProsumersParticipantsNames
											.contains(BPMNUtils.getInitiatingParticipant(choreographyTask)
													.getName());
									// create onMessage choreography task activities
									BPELUtils.createCDProsumerOnMessageChoreographyTaskActivities(cdName,
											cdParticipant, choreographyTask, cdWSDLDefinitionData,
											prosumerPartWSDLDefinitionData, process, artifactData,
											bpelElements, sequences, propertyAliasesData,
											isProsumerClientParticipant);
									// create an empty hashset of choreography task processed ID
									HashSet<String> processedFlowNodesNew = new HashSet<>();
									// add flowNodePath to processedFlowNodesNew
									processedFlowNodesNew.add(BPMNUtils
											.createFlowNodePath(choreographyTask, currentPath));	
									// create a new instance of loopedFlowNodes
									Deque<LoopedFlowNodeData> loopedFlowNodesNew = new ArrayDeque<>();
									// copy all the elements of loopedFlowNodes into loopedFlowNodesNew
									loopedFlowNodesNew.addAll(loopedFlowNodes);																				
									// create a new instance of currentPath
									Deque<String> currentPathNew = new ArrayDeque<>();
									// copy all the elements of currentPath into currentPathNew			
									currentPathNew.addAll(currentPath);															
									// call createBpelProcess with the targetRef of the first outgoing of 
									// choreographyTask, sequences, bpelElements, loopedFlowNodesNew and
									// currentPathNew
									createCDProsumerBpelProcess(processedFlowNodesNew, choreographyTask
											.getOutgoing().get(0).getTargetRef(), choreographies,
											currentChoreography, cdParticipant, cdName,
											prosumersParticipantsNames, clientProsumersParticipantsNames,
											process, sequences, bpelElements, wsdlDefinitions,
											cdWSDLDefinitionData, prosumerPartWSDLDefinitionData,
											artifactData, propertyAliasesData, loopedFlowNodesNew,
											timersData, currentPathNew);
								}	
							}
							// check if the i-est outgoing of the event based gateway is an end event								
							if(flowNode.getOutgoing().get(i).getTargetRef() instanceof EndEvent){							
								return;
							}												
					}
					else{
						// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
						// sequences and bpelElements
						createCDProsumerBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
								.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
								prosumersParticipantsNames, clientProsumersParticipantsNames, process,
								sequences, bpelElements, wsdlDefinitions, cdWSDLDefinitionData,
								prosumerPartWSDLDefinitionData, artifactData, propertyAliasesData,
								loopedFlowNodes, timersData, currentPath);
					}
				} 
			}	
		}
		
	}

	public static BPELData createCDClientBpelProcessFromChoreography(String cdParticipantName, String cdName,
			String choreographyTNS, List<TaskCorrelationData> tasksCorrelationData,
			Choreography choreography, List<Choreography> choreographies,
			Map<String, WSDLDefinitionData> wsdlDefinitions, 
			WSDLDefinitionData cdWSDLDefinitionData, ArtifactData artifactData){
		
		// log execution flow
		LOGGER.entry("CD Client name: "+cdName+" CD Client participant name: "+cdParticipantName);
				
		LOGGER.info("Creating "+cdName+" Client Bpel process");
		
		BPELData bpelData = new BPELData();
		StartEvent startEvent = BPMNUtils.getStartEvent(choreography);
		ArrayDeque<BPELExtensibleElement> bpelElements = new ArrayDeque<>();
		ArrayDeque<org.eclipse.bpel.model.Sequence> sequences = new ArrayDeque<>();		
		Process process = BPELFactory.eINSTANCE.createProcess();
		process.setName(cdName);
		process.setTargetNamespace(BPELUtils.createCDBpelProcessTargetNamespace(BPMNUtils
				.getTargetNamespaceChoreography(choreography), cdName));
		org.eclipse.bpel.model.Sequence mainSequence = BPELFactory.eINSTANCE.createSequence();
		mainSequence.setName(MAIN_SEQUENCE_LABEL);

		// create correlationSets of the process
		CorrelationSets correlationSets = BPELFactory.eINSTANCE.createCorrelationSets();	
		process.setCorrelationSets(correlationSets);		
		// create correlation set for choreographyID
		CorrelationSet choreographyIDCorrelationSet = BPELFactory.eINSTANCE.createCorrelationSet();
		choreographyIDCorrelationSet.setName(CORRELATION_SET_CHOREOGRAPHY_ID_NAME);
		Property property = MessagepropertiesFactory.eINSTANCE.createProperty();
		property.setName(Utils.createXMLQNameString(PROPERTIES_PREFIX, CHOREOGRAPHY_ID_PROPERTY_LABEL));
		choreographyIDCorrelationSet.getProperties().add(property);	
		// add choreographyIDCorrelationSet to the correlation sets of the process
		process.getCorrelationSets().getChildren().add(choreographyIDCorrelationSet);

		// create properties TNS
		String propertiesTNS = WSDLUtils.createNamespace(BPMNUtils
				.getTargetNamespaceChoreography(choreography), PROPERTIES_NAME_LABEL);	
		// create propertyAliasesData
		PropertyAliasesData propertyAliasesData = new PropertyAliasesData(CHOREOGRAPHY_ID_PROPERTY_LABEL, 
				propertiesTNS);			
		
		// create main Pick
		Pick mainPick = BPELFactory.eINSTANCE.createPick();
		mainPick.setCreateInstance(true);
		mainPick.setName(PICK_NAME);
		// add main Pick to Sequence
		mainSequence.getActivities().add(mainPick);
		process.setActivity(mainSequence);
		bpelElements.add(process);
		// add main Pick to bpelElements
		bpelElements.add(mainPick);

		// create imports and importedNamespacesData
		bpelData.setImportedNamespacesData(BPELUtils
				.addWSDLImportsToCDClientBpelProcessAndGetImportNamespaceData(process, choreographyTNS,
						wsdlDefinitions, cdWSDLDefinitionData, artifactData));
		
		// initialize process variables
		Variables variables = BPELFactory.eINSTANCE.createVariables();
		process.setVariables(variables);
		// initialize partner links of the process
		PartnerLinks partnerLinks = BPELFactory.eINSTANCE.createPartnerLinks();
		process.setPartnerLinks(partnerLinks);	
				
		Participant cdParticipant = BPMNUtils.getParticipantFromName(choreographies, cdParticipantName);
		
		// create the set containing the ids of the flow nodes processed
		Set<String> processedFlowNodes = new HashSet<>();
		
		// create the queue containing the data of the looped subchoreographies
		Deque<LoopedFlowNodeData> loopedSubChoreographies = new ArrayDeque<>();
			
		// create the queue containing the data of the timers
		Deque<TimerData> timersData = new ArrayDeque<>();		
		
		// create the queue containing the ids of the traversed flow nodes
		Deque<String> currentPath = new ArrayDeque<>();
		// add the id of choreography to currentPath
		currentPath.addLast(choreography.getId());
		
		// TODO find out a way to manage the situation in which a start event has more than one outgoing
		// if possible
		CDGenerationUtils.createCDClientBpelProcess(processedFlowNodes, startEvent.getOutgoing().get(0)
				.getTargetRef(), choreographies, choreography, cdParticipant, cdName, tasksCorrelationData,
				process, sequences, bpelElements, wsdlDefinitions, cdWSDLDefinitionData, artifactData,
				propertyAliasesData, loopedSubChoreographies, timersData, currentPath);
		Process processFinal = (Process) bpelElements.pollFirst();

		bpelData.setProcess(processFinal);
		bpelData.setPropertyAliasesData(propertyAliasesData);
		
		// log execution flow
		LOGGER.exit();
		
		return bpelData;
	}
	
	public static void createCDClientBpelProcess(Set<String> processedFlowNodes, FlowNode flowNode, 
			List<Choreography> choreographies, Choreography currentChoreography,
			Participant cdParticipant, String cdName, List<TaskCorrelationData> tasksCorrelationData,
			Process process, Deque<org.eclipse.bpel.model.Sequence> sequences,
			Deque<BPELExtensibleElement> bpelElements, Map<String, WSDLDefinitionData> wsdlDefinitions,
			WSDLDefinitionData cdWSDLDefinitionData, ArtifactData artifactData,
			PropertyAliasesData propertyAliasesData, Deque<LoopedFlowNodeData> loopedSubChoreographies,
			Deque<TimerData> timersData, Deque<String> currentPath){
		
		// log execution flow
		LOGGER.entry("CD Client Name: "+cdName+" CD Client participant name: "
				+cdParticipant.getName()+" Flow Node name: "+flowNode.getName());
				
		// flowNode is an end event
		if(flowNode instanceof EndEvent){
			// check if timersData is not empty and if the last element has the same
			// choreography name as the currentChoreography
			if(!timersData.isEmpty() && 
				timersData.getLast().getChoreographyName().equals(currentChoreography.getName())) {
				// timersData is not empty and the last element has the same
				// choreography name as the currentChoreography
				// create wait with for expression
				BPELUtils.createWaitWithForExpression(timersData.getLast().getTimerValue(),
						sequences.getLast());
				// remove the last element from timersData
				timersData.pollLast();
				// remove the last element from sequences
				sequences.pollLast();
				// remove the last element from bpelElements
				bpelElements.pollLast();
			}			
			// check if loopedSubChoreographies is not empty and the last element has the same
			// name as currentChoreography
			if(!loopedSubChoreographies.isEmpty() && 
				loopedSubChoreographies.getLast().getName().equals(currentChoreography.getName())) {
				// loopedFlowNodes is not empty and the last element has the same
				// name as currentChoreography so it has to removed
				loopedSubChoreographies.pollLast();
				// remove the last element from sequences
				sequences.pollLast();
				// remove the last element from bpelElements
				bpelElements.pollLast();
			}
			// check if there is at least one element in currentPath
			if(currentPath.size() > 1) {
				// there is at least one element in currentPath
				// remove the last element of currentPath
				currentPath.removeLast();
			}
			return;
		}
		// check if flowNode has been already processed
		if(BPMNUtils.hasFlowNodeProcessed(flowNode, currentPath, processedFlowNodes)) {
			// flowNode has been already processed skip it
			return;		
		}
		// add flowNodePath to flowNodesProcessed
		processedFlowNodes.add(BPMNUtils.createFlowNodePath(flowNode, currentPath));		
		// check if flowNode is a IntermediateCatchEvent and if the timer has to be implemented
		if(flowNode instanceof IntermediateCatchEvent &&
			BPMNUtils.hasTimerToBeImplemented(flowNode, cdParticipant.getName())) {
			// get the first event definition of the IntermediateCatchEvent as a TimerEventDefinition
			TimerEventDefinition timerEventDefinition = (TimerEventDefinition) 
					((IntermediateCatchEvent) flowNode).getEventDefinitions().get(0);						
			// flowNode is a IntermediateCatchEvent			
			BPELUtils.createTimerActivities(timerEventDefinition, sequences, bpelElements);
			// add the data related to the timer to timersData
			timersData.addLast(new TimerData(currentChoreography.getName(),
					((FormalExpressionImpl) timerEventDefinition.getTimeDuration()).getBody()));
		}		
		// check if flowNode is a sub-choreography
		if(flowNode instanceof SubChoreography){
			
			// log sub-choreography  name
			LOGGER.info("CD Client Name: "+cdName+" CD Client participant name: "
					+cdParticipant.getName()+" Sub Choreography name: "+flowNode.getName());
			
			// flowNode is a sub-choreography
			// check if the subchoreography is looped and if the related loop has to be implemented
			if(((SubChoreography) flowNode).getLoopType().compareTo(ChoreographyLoopType.NONE) != 0
				&& BPMNUtils.hasSubChoreographyLoopToBeImplementedIntoCDClient(currentChoreography, 
						(SubChoreography) flowNode, cdParticipant, choreographies)) {
				// the subchoreography is looped and the related loop has to be implemented		
				// create loop activities
				BPELUtils.createCDClientLoopActivities(flowNode, process, cdParticipant.getName(),
						wsdlDefinitions, cdWSDLDefinitionData, choreographies, sequences, bpelElements);
				// add the LoopedFlowNodeData (name) related to the subchoreography 
				// as last element of loopedSubChoreographies
				loopedSubChoreographies.addLast(new LoopedFlowNodeData(flowNode.getName()));	
			}
			// retrieve the sub-choreography 
			Choreography subChoreography = BPMNUtils.getChoreography(choreographies,
					((SubChoreography)flowNode).getName());
			// add the id of flowNode to the currentPath
			currentPath.addLast(flowNode.getId());	
			// get sub-choregraphy start event
			StartEvent subChoreographyStartEvent = BPMNUtils.getStartEvent(subChoreography);
			// TODO find out a way to manage the situation in which a start event has more than one outgoing, 
			// if possible
			// call createBpelProcess with the targetRef of the first outgoing of subChoreographyStartEvent
			createCDClientBpelProcess(processedFlowNodes, subChoreographyStartEvent.getOutgoing().get(0)
					.getTargetRef(), choreographies, subChoreography, cdParticipant, cdName,
					tasksCorrelationData, process, sequences, bpelElements, wsdlDefinitions,
					cdWSDLDefinitionData, artifactData, propertyAliasesData, loopedSubChoreographies,
					timersData, currentPath);			
		}
		
		// flowNode is a choreography task
		if(flowNode instanceof ChoreographyTask){	
			
			// log choreography task name
			LOGGER.info("CD Client Name: "+cdName+" CD Client participant name: "
					+cdParticipant.getName()+" Choreography task name: "+flowNode.getName());
			
			// the CD Client participant is the initiating participant of the task
			if(BPMNUtils.getInitiatingParticipant((ChoreographyActivity) flowNode).equals(cdParticipant)){
				// get receiver participant WSDLDefinitionData
				WSDLDefinitionData receivingWSDLDefinitionData = wsdlDefinitions.get(BPMNUtils
						.getReceivingParticipant((ChoreographyActivity) flowNode).getName());
				// check if the choreography task is the first choreography task of the main choreography
				boolean isFirstChoreographyTaskMainChoreography = BPMNUtils
						.getFirstChoregraphyTaskName(BPMNUtils
								.getMainChoreographyFromChoreographyies(choreographies))
									.equals(flowNode.getName());
				// create CD Client initiating choreography task activities
				BPELUtils.createCDClientInitiatingChoreographyTaskActivities((ChoreographyTask) flowNode,
						isFirstChoreographyTaskMainChoreography, cdParticipant, cdName, process, sequences,
						bpelElements, wsdlDefinitions, cdWSDLDefinitionData, receivingWSDLDefinitionData,
						artifactData, propertyAliasesData);
			}
			// the CD Client participant is not the initiating participant of the task
			else{
				// get initiating cd participant WSDLDefinitionData
				WSDLDefinitionData cdInitiatingWSDLDefinitionData = wsdlDefinitions.get(BPMNUtils
						.getInitiatingParticipant((ChoreographyActivity) flowNode).getName());				
				// create CD Client receiving choreography task activities
				BPELUtils.createCDClientReceivingChoreographyTaskActivities(cdName, choreographies,
						(ChoreographyTask) flowNode, tasksCorrelationData, cdParticipant, process,
						wsdlDefinitions, cdInitiatingWSDLDefinitionData, cdWSDLDefinitionData,
						bpelElements, sequences, artifactData, propertyAliasesData);
			}				
		}	
		// flowNode is a gateway
		if(flowNode instanceof Gateway){			
			if(((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.CONVERGING)==0 &&
					flowNode instanceof ParallelGateway ){
				// flowNode is a converging gateway so remove the last element of sequences 
				// and the last element of bpelElements
				if(!(bpelElements.getLast() instanceof Process)){
					sequences.pollLast();
					bpelElements.pollLast();
				}
			}	
			// flowNode is a diverging exclusive gateway 				
			// check if it has to be implemented as an IF BPEL 
			if(flowNode instanceof ExclusiveGateway &&				
			   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0 &&
				BPMNUtils.hasExclusiveGatewayToBeImplemented(flowNode, cdParticipant)){					
					// create IF BPEL					
					If ifItem = BPELFactory.eINSTANCE.createIf();
					// check if flow node has name
					if(flowNode.getName() != null){
						// set if name
						ifItem.setName(BPELUtils.createIfName(flowNode.getName()));	
					}
					// attach the IF BPEL just created to the last sequence of sequences
					sequences.getLast().getActivities().add(ifItem);					
					// add IF BPEL as the last element of bpelElements 
					bpelElements.addLast(ifItem);	
			}						
			// flowNode is a parallel diverging gateway 
			if(flowNode instanceof ParallelGateway &&			
				((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0){					
					// create FLOW BPEL element
					Flow flow = BPELFactory.eINSTANCE.createFlow();
					flow.setName(flowNode.getName());
					// attach the FLOW BPEL just created to the last sequence of sequences					
					sequences.getLast().getActivities().add(flow);				
					// add FLOW BPEL as the last element of bpelElements
					bpelElements.addLast(flow);
			}	
		}		
		// iterate over the outgoing of flowNode
		for (int i = 0; i < flowNode.getOutgoing().size(); i++) {
			
			// flowNode is a diverging exclusive gateway 				
			// check if it has to be translated into an IF BPEL  
			if(flowNode instanceof ExclusiveGateway &&				
			   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0 &&
				BPMNUtils.hasExclusiveGatewayToBeImplemented(flowNode, cdParticipant)){	
				// create a BPEL sequence
				org.eclipse.bpel.model.Sequence sequenceIfEl = BPELFactory.eINSTANCE.createSequence();
				sequenceIfEl.setName(BPELUtils.createSequenceIfName(String.valueOf(i)));
				// first outgoing of a diverging exclusive gateway
				if(i == 0){
					// the last element of bpelElement is a bpel if item
					// attach as activity the sequence sequenceIfEl to it 
					((If) bpelElements.getLast()).setActivity(sequenceIfEl);
					// set if condition
					((If) bpelElements.getLast()).setCondition(BPELUtils
							.createCDClientXPathConditionOutgoingDivergingExclusiveGateway(process, 
									cdParticipant.getName(), wsdlDefinitions, cdWSDLDefinitionData,
									choreographies, flowNode.getOutgoing().get(i)));	
					// create a new instance of sequences, sequencesNew
					ArrayDeque<org.eclipse.bpel.model.Sequence> sequencesNew = new ArrayDeque<>();	
					sequencesNew.addAll(sequences);
					// add to sequencesNew the sequence sequenceIfEl as last element
					sequencesNew.addLast(sequenceIfEl);
					// create a new instance of bpelElements, bpelElementsNew
					ArrayDeque<BPELExtensibleElement> bpelElementsNew = new ArrayDeque<>();
					bpelElementsNew.addAll(bpelElements);
					// create a new instance of loopedSubChoreographies
					Deque<LoopedFlowNodeData> loopedSubChoreographiesNew = new ArrayDeque<>();
					// copy all the elements of loopedSubChoreographies into loopedSubChoreographiesNew
					loopedSubChoreographiesNew.addAll(loopedSubChoreographies);										
					// create a new instance of currentPath
					Deque<String> currentPathNew = new ArrayDeque<>();
					// copy all the elements of currentPath into currentPathNew			
					currentPathNew.addAll(currentPath);					
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequencesNew, bpelElementsNew, loopedSubChoreographiesNew and currentPathNew
					createCDClientBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							tasksCorrelationData, process, sequencesNew, bpelElementsNew, wsdlDefinitions,
							cdWSDLDefinitionData, artifactData, propertyAliasesData,
							loopedSubChoreographiesNew, timersData, currentPathNew);
				}
				// outgoing, other than the first, of a diverging exclusive gateway 
				else{
					// create an elseif bpel
					ElseIf elseIf = BPELFactory.eINSTANCE.createElseIf(); 
					// set as activity of elseif bpel sequenceIfEl
					elseIf.setActivity(sequenceIfEl);
					// set elseif condition
					elseIf.setCondition(BPELUtils
							.createCDClientXPathConditionOutgoingDivergingExclusiveGateway(process, 
									cdParticipant.getName(), wsdlDefinitions, cdWSDLDefinitionData,
									choreographies, flowNode.getOutgoing().get(i)));				
					// attach elseif bpel to elseIf list of the last element of bpelElements
					((If) bpelElements.getLast()).getElseIf().add(elseIf);
					// create a new instance of sequences, sequencesNew					
					ArrayDeque<org.eclipse.bpel.model.Sequence> sequencesNew = new ArrayDeque<>();	
					sequencesNew.addAll(sequences);
					// add to sequencesNew the sequence sequenceIfEl as last element
					sequencesNew.addLast(sequenceIfEl);
					// create a new instance of bpelElements, bpelElementsNew					
					ArrayDeque<BPELExtensibleElement> bpelElementsNew = new ArrayDeque<>();
					bpelElementsNew.addAll(bpelElements);
					// create a new instance of loopedSubChoreographies
					Deque<LoopedFlowNodeData> loopedSubChoreographiesNew = new ArrayDeque<>();
					// copy all the elements of loopedSubChoreographies into loopedSubChoreographiesNew
					loopedSubChoreographiesNew.addAll(loopedSubChoreographies);								
					// create a new instance of currentPath
					Deque<String> currentPathNew = new ArrayDeque<>();
					// copy all the elements of currentPath into currentPathNew			
					currentPathNew.addAll(currentPath);	
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequencesNew, bpelElementsNew, loopedSubChoreographiesNew and currentPathNew
					createCDClientBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							tasksCorrelationData,process, sequencesNew, bpelElementsNew, wsdlDefinitions,
							cdWSDLDefinitionData,artifactData, propertyAliasesData,
							loopedSubChoreographiesNew, timersData, currentPathNew);
				}		
			}
			else{
				// flowNode is a parallel diverging gateway
				if(flowNode instanceof ParallelGateway && 
				   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0){
					// create a sequence
					org.eclipse.bpel.model.Sequence sequenceFlowEl = BPELFactory.eINSTANCE.createSequence();
					sequenceFlowEl.setName(BPELUtils.createSequenceFlowName(String.valueOf(i)));
					// attach sequenceFlowEl as an activity of the last element of bpelElements	
					((Flow) bpelElements.getLast()).getActivities().add(sequenceFlowEl);
					// create a new instance of sequences, sequencesNew	
					ArrayDeque<org.eclipse.bpel.model.Sequence> sequencesNew = new ArrayDeque<>();	
					sequencesNew.addAll(sequences);
					// add to sequencesNew the sequence sequenceFlowEl as last element
					sequencesNew.addLast(sequenceFlowEl);
					// create a new instance of bpelElements, bpelElementsNew
					ArrayDeque<BPELExtensibleElement> bpelElementsNew = new ArrayDeque<>();
					bpelElementsNew.addAll(bpelElements);				
					// create a new instance of loopedSubChoreographies
					Deque<LoopedFlowNodeData> loopedSubChoreographiesNew = new ArrayDeque<>();
					// copy all the elements of loopedSubChoreographies into loopedSubChoreographiesNew
					loopedSubChoreographiesNew.addAll(loopedSubChoreographies);						
					// create a new instance of currentPath
					Deque<String> currentPathNew = new ArrayDeque<>();
					// copy all the elements of currentPath into currentPathNew			
					currentPathNew.addAll(currentPath);					
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequencesNew, bpelElementsNew, loopedSubChoreographies and currentPathNew
					createCDClientBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							tasksCorrelationData, process, sequencesNew, bpelElementsNew, wsdlDefinitions,
							cdWSDLDefinitionData, artifactData, propertyAliasesData,
							loopedSubChoreographiesNew, timersData, currentPathNew);
				}
				// flowNode is an event based gateway
				if(flowNode instanceof EventBasedGateway){
						// take the target reference of the i-est outgoing of the event based gateway
						// it is assumed that every outgoing of an event based gateway is either a
						// choreography taskor an end event
						// TODO check if this assumption is generally valid 
						// check if the i-est outgoing of the event based gateway is a ChoreographyTask
						if(flowNode.getOutgoing().get(i).getTargetRef() instanceof ChoreographyTask){
							ChoreographyTask choreographyTask = (ChoreographyTask) flowNode.getOutgoing()
									.get(i).getTargetRef();
							// check if choreography task has not been processed yet	
							if(!BPMNUtils.hasFlowNodeProcessed(choreographyTask, currentPath,
									processedFlowNodes)) {
								// choreography task has not been processed yet 
								// it is assumed that every choreography task outgoing from an event based 
								// gateway involves a prosumer client participant as a initating participant
								// TODO check if this assumption is generally valid 
								// get receiver participant WSDLDefinitionData
								WSDLDefinitionData receivingWSDLDefinitionData = wsdlDefinitions
										.get(BPMNUtils.getReceivingParticipant(choreographyTask).getName());
								// check if the choreography task is the first choreography task of 
								// the main choreography
								boolean isFirstChoreographyTaskMainChoreography = BPMNUtils
										.getFirstChoregraphyTaskName(BPMNUtils
												.getMainChoreographyFromChoreographyies(choreographies))
													.equals(flowNode.getName());
								// create CD Client initiating choreography task activities
								BPELUtils
									.createCDClientInitiatingChoreographyTaskActivities(choreographyTask,
										isFirstChoreographyTaskMainChoreography,
										cdParticipant, cdName, process, sequences, bpelElements,
										wsdlDefinitions, cdWSDLDefinitionData, receivingWSDLDefinitionData,
										artifactData, propertyAliasesData);
								// create an empty hashset of choreography task processed ID
								HashSet<String> processedFlowNodesNew = new HashSet<>();
								// add flowNodePath to processedFlowNodesNew
								processedFlowNodesNew.add(BPMNUtils
										.createFlowNodePath(choreographyTask, currentPath));	
								// create a new instance of loopedSubChoreographies
								Deque<LoopedFlowNodeData> loopedSubChoreographiesNew = new ArrayDeque<>();
								// copy all the elements of loopedFlowNodes into loopedFlowNodesNew
								loopedSubChoreographiesNew.addAll(loopedSubChoreographies);									
								// create a new instance of currentPath
								Deque<String> currentPathNew = new ArrayDeque<>();
								// copy all the elements of currentPath into currentPathNew			
								currentPathNew.addAll(currentPath);								
								// call createBpelProcess with the targetRef of the first outgoing of
								// choreographyTask, sequences, bpelElements, loopedSubChoreographiesNew and
								// currentPathNew
								createCDClientBpelProcess(processedFlowNodesNew, choreographyTask
										.getOutgoing().get(0).getTargetRef(), choreographies,
										currentChoreography,cdParticipant, cdName, tasksCorrelationData,
										process, sequences, bpelElements, wsdlDefinitions,
										cdWSDLDefinitionData, artifactData, propertyAliasesData,
										loopedSubChoreographiesNew, timersData, currentPathNew);
							}
						}					
						// check if the i-est outgoing of the event based gateway is an end event						
						if(flowNode.getOutgoing().get(i).getTargetRef() instanceof EndEvent){
							return;
						}						
				}	
				else{					
					// call createBpelProcess with the targetRef of the i-est outgoing of flowNode,
					// sequences and bpelElements
					createCDClientBpelProcess(processedFlowNodes, flowNode.getOutgoing().get(i)
							.getTargetRef(), choreographies, currentChoreography, cdParticipant, cdName,
							tasksCorrelationData, process, sequences, bpelElements, wsdlDefinitions,
							cdWSDLDefinitionData, artifactData, propertyAliasesData,
							loopedSubChoreographies, timersData, currentPath);
				} 
			}	
		}
	}	
	
}
