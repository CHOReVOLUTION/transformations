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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyActivity;
import org.eclipse.bpmn2.ChoreographyLoopType;
import org.eclipse.bpmn2.ChoreographyTask;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubChoreography;
import org.eclipse.bpmn2.impl.FormalExpressionImpl;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyMessageInformation;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ProjectionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;


public class BPMNUtils {
	
	private static final String BPMN_EXTENSION = ".bpmn";  
	private static final String HASH_SYMBOL = "#";

	private static final List<String> XPATH_OPERATORS = new ArrayList<String>(Arrays
			.asList("|","+","-","*",
			"div","=","!=","<","<=",">",">=","or","and","mod","count","count-not-empty","id","concat",
			"substring-before","substring-after","substring","normalize-space","translate","lower-case",
			"upper-case","ends-with","boolean","not","sum","floor","ceiling","round","avg","min","max"));
	
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(BPMNUtils.class);	
	
	public static ChoreographyData readChoreographyData(ProjectionData projectionData)
			throws CDGeneratorException {
		
		// log execution flow
		LOGGER.entry("CD name: "+projectionData.getCdName()," CD Participant name: "
				+projectionData.getParticipantName());
		
		// load and extract choreography contained in the BPMN model.
		String tempFolderPath = Utils.createTemporaryFolderFromMillisAndGetPath();
		File bpmnFile = new File(tempFolderPath+File.separator+projectionData.getCdName()+BPMN_EXTENSION);
		ChoreographyData choreographyData = null;
		try {
			FileUtils.writeByteArrayToFile(bpmnFile, projectionData.getBpmn());
			// load and extract choreographies contained in the BPMN model.
			List<Choreography> choreographies = BPMNUtils.loadChoreographiesFromBPMN2Model(bpmnFile);					
			XmlSchema schema = XSDSchemaUtils.getXmlSchema(projectionData.getTypes());
			choreographyData = new ChoreographyData(choreographies, schema);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Exception in readChoreographyData see log file for details");
		}
						
		Utils.deleteFolder(tempFolderPath);
		
		// log execution flow
		LOGGER.exit();
		
		return choreographyData;
	}
	
	public static ChoreographyData readChoreographyData(File bpmnFile) throws CDGeneratorException {
			
		// log execution flow
		LOGGER.entry();
		
		// load and extract choreographies contained in the BPMN model.
		List<Choreography> choreographies = BPMNUtils.loadChoreographiesFromBPMN2Model(bpmnFile);
		
		XmlSchema schema = null;
		// get the container from the first choreography in order to retrive the imports
		List<Import> imports = ((Definitions) choreographies.get(0).eContainer()).getImports();
		
		if (!imports.isEmpty()) {
			// TODO for now we assume that there is only one import
			try {
				schema = XSDSchemaUtils.getXmlSchema(FileUtils.readFileToByteArray(new File(bpmnFile
						.getParent(), imports.get(0).getLocation())));
			} catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
				throw new CDGeneratorException("Exception in readChoreographyData see log file for details");
			}
		}
		ChoreographyData choreographyData = new ChoreographyData(choreographies, schema);

		// log execution flow
		LOGGER.exit();
		
		return choreographyData;
	}	

	public static Choreography getMainChoreographyFromChoreographyData(ChoreographyData choreographyData){
		
		// log execution flow
		LOGGER.entry();
		for (Choreography choreographyItem1 : choreographyData.getChoreographies()) {
			// check if choreographyItem1 is referenced inside the others choreographies
			boolean isChoregraphyReferenced = false;
			for (Choreography choreographyItem2 : choreographyData.getChoreographies()) {
				if(choreographyItem2 != choreographyItem1 && 
				   BPMNUtils.isChoreographyRefencedInsideChoreography(choreographyItem2,
						   choreographyItem1.getName())){
					isChoregraphyReferenced = true;
				}			
			}
			// choreographyItem1 is not referenced in all the others choreographies 
			if(!isChoregraphyReferenced)
				// log execution flow
				LOGGER.exit();
				return choreographyItem1;
		}
		
		throw new CDGeneratorException("No main choreography found see log file for details.");
	}
	
	public static Choreography getMainChoreographyFromChoreographyies(List<Choreography> choreographies) {
		
		// log execution flow
		LOGGER.entry();
		for (Choreography choreographyItem1 : choreographies) {
			// check if choreographyItem1 is referenced inside the others choreographies 
			boolean isChoregraphyReferenced = false;
			for (Choreography choreographyItem2 : choreographies) {
				if(choreographyItem2 != choreographyItem1 && 
				   BPMNUtils.isChoreographyRefencedInsideChoreography(choreographyItem2,
						   choreographyItem1.getName())){
					isChoregraphyReferenced = true;
				}			
			}
			// choreographyItem1 is not referenced in all the others choreographies 
			if(!isChoregraphyReferenced)
				// log execution flow
				LOGGER.exit();
				return choreographyItem1;
		}
		
		throw new CDGeneratorException("No main choreography found see log file for details.");		
	}
	
	public static boolean isChoreographyRefencedInsideChoreography(Choreography choreography,
			String choreographyName){
		
		// log execution flow
		LOGGER.entry();
		// iterate over every sub-choregraphy
		// to check is the choregraphy with choreographyName
		// is referenced as a sub-choreography
		for (FlowElement flowElement : choreography.getFlowElements()) {
			// flowNode is a sub-choreography
			if(flowElement instanceof SubChoreography){
				if(((SubChoreography)flowElement).getName().equals(choreographyName)){
					// log execution flow
					LOGGER.exit();
					return true;
				}	
			}
		}
		// log execution flow
		LOGGER.exit();
		return false;
	}
	
	public static Choreography getChoreography(List<Choreography> choreographies, String choreographyName) {
		
		// log execution flow
		LOGGER.entry();
		for (Choreography choreography : choreographies) {
			if(choreography.getName().equals(choreographyName)){
				LOGGER.info("Choreography: "+choreographyName+" found!");			
				// log execution flow
				LOGGER.exit();				
				return choreography;
			}	
		}
		LOGGER.info("NO Choreography: "+choreographyName+" found!");
		throw new CDGeneratorException("NO Choreography: "+choreographyName
				+" found see log file for details.");
	}
	
	public static boolean isChoreographyLooped(List<Choreography> choreographies, String choreographyName) {
		
		// log execution flow
		LOGGER.entry();		
		for (Choreography choreography : choreographies) {
			// iterate over flow elements 
			for (FlowElement flowElement : choreography.getFlowElements()) {
				// check if the flow element is a sub choreography named choreographyName and looped
				if (flowElement instanceof SubChoreography && flowElement.getName().equals(choreographyName)
						&& ((SubChoreography) flowElement).getLoopType()
						.compareTo(ChoreographyLoopType.NONE) != 0) {
					// log execution flow
					LOGGER.exit();	
					return true;
				}
			}		
		}	
		// log execution flow
		LOGGER.exit();	
		return false;
	}
		
	public static Set<String> getLoopedChoreographyTasksFromChoreographies(
			List<Choreography> choreographies) {
		
		// log execution flow
		LOGGER.entry();
				
		// create the set containing the ids of the flow nodes processed
		Set<String> processedFlowNodes = new HashSet<>();
		// get the main choreography
		Choreography choreography = BPMNUtils.getMainChoreographyFromChoreographyies(choreographies);
		// get the start event of the main choregraphy
		StartEvent startEvent = BPMNUtils.getStartEvent(choreography);
		// create the set containing the ids of the looped choreography tasks
		Set<String> loopedChoreographyTasks = new HashSet<>();
		// create the set containing the names of the looped subchoreographies
		Set<String> loopedSubChoreographies = new HashSet<>();
		
		// TODO find out a way to manage the situation in which a start event has more than one 
		// outgoing, if possible
		BPMNUtils.getLoopedChoreographyTasks(processedFlowNodes, startEvent.getOutgoing().get(0)
				.getTargetRef(), choreography, choreographies, loopedChoreographyTasks,
				loopedSubChoreographies);
		
		return loopedChoreographyTasks;
	}
	
	public static void getLoopedChoreographyTasks(Set<String> processedFlowNodes, FlowNode flowNode,
			Choreography currentChoreography, List<Choreography> choreographies,
			Set<String> loopedChoreographyTasks, Set<String> loopedSubChoreographies) {
		
		// log execution flow
		LOGGER.entry();
		
		// flowNode is an end event
		if(flowNode instanceof EndEvent){			
			// remove the current choreography from the set of
			// the looped subchoreographies if present
			loopedSubChoreographies.remove(currentChoreography.getName());
			return;
		}
		// check if flowNode has been already processed
		if(processedFlowNodes.contains(flowNode.getId())){
			// flowNode has been already processed skip it	
			return;
		}
		// add flowNode to flowNodesProcessed
		processedFlowNodes.add(flowNode.getId());
		// flowNode is a sub-choreography
		if(flowNode instanceof SubChoreography){
			// check if the subchoreography is looped
			if(((SubChoreography) flowNode).getLoopType().compareTo(ChoreographyLoopType.NONE) != 0) {
				// the subchoreography is looped
				// add the subchoreography name to the set of looped sub-choreographies
				loopedSubChoreographies.add(flowNode.getName());
			}
			// retrieve the sub-choreography 
			Choreography subChoreography = BPMNUtils
					.getChoreography(choreographies, ((SubChoreography)flowNode).getName());
			// get sub-choregraphy start event
			StartEvent subChoreographyStartEvent = BPMNUtils.getStartEvent(subChoreography);	
			// TODO find out a way to manage the situation in which a start event has more than one outgoing  
			// if possible
			// call getLoopedChorTasks with the targetRef of the first outgoing of subChoreographyStartEvent
			getLoopedChoreographyTasks(processedFlowNodes, subChoreographyStartEvent.getOutgoing().get(0)
					.getTargetRef(), subChoreography, choreographies, loopedChoreographyTasks,
					loopedSubChoreographies);
		}
		// flowNode is a choreography task
		if(flowNode instanceof ChoreographyTask){
			// check if the choreography task is looped or it is contained in a 
			// looped sub-choreography
			if(((ChoreographyTask) flowNode).getLoopType().compareTo(ChoreographyLoopType.NONE) != 0 || 
					!loopedSubChoreographies.isEmpty()) {
				// the choreography task is looped or it is contained in a looped sub-choreography 
				// add the choreography task to the set of looped choreography task
				loopedChoreographyTasks.add(flowNode.getId());
			}
		}		
		// iterate over the outgoing of flowNode
		for (int i = 0; i < flowNode.getOutgoing().size(); i++){
			// check if flowNode is a diverging gateway 
			if(flowNode instanceof Gateway &&				
			   ((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.DIVERGING)==0 ) {
				// flowNode is a diverging gateway
				// create a new instance of loopedSubChoreographies
				Set<String> loopedSubChoreographiesNew = new HashSet<>();
				// copy all the elements of loopedSubChoreographies into loopedSubChoreographiesNew
				loopedSubChoreographiesNew.addAll(loopedSubChoreographies);
				// call getLoopedChorTasks on the target ref of the i-est outgoing of flowNode,
				// using loopedSubChoreographiesNew
				getLoopedChoreographyTasks(processedFlowNodes, flowNode.getOutgoing().get(i).getTargetRef(),
						currentChoreography, choreographies, loopedChoreographyTasks,
						loopedSubChoreographiesNew);
			}
			else {
				// call getLoopedChorTasks on the target ref of the i-est outgoing of flowNode
				getLoopedChoreographyTasks(processedFlowNodes, flowNode.getOutgoing().get(i).getTargetRef(),
						currentChoreography, choreographies, loopedChoreographyTasks,
						loopedSubChoreographies);
			}		
		}
	}
		
	public static Participant getParticipantFromName(ChoreographyData choreographyData, String name)
			throws CDGeneratorException {

		// log execution flow
		LOGGER.entry();
		for (Choreography choreography : choreographyData.getChoreographies()) {
			for (Participant participant : choreography.getParticipants()) {
				if (participant.getName().equals(name)){
					LOGGER.info("Participant: " +name+" found.");
					// log execution flow
					LOGGER.exit();	
					return participant;
				}	
			}			
		}
		LOGGER.info("Participant: " +name+" not found.");
		throw new CDGeneratorException("Participant: " +name+" not found see log file for details.");
	}

	public static Participant getParticipantFromName(Choreography choreography, String name)
			throws CDGeneratorException {

		// log execution flow
		LOGGER.entry();
		for (Participant participant : choreography.getParticipants()) {
			if (participant.getName().equals(name)){
				LOGGER.info("Participant "+name+" found inside choreography "+choreography.getName());
				// log execution flow
				LOGGER.exit();				
				return participant;
			}	
		}
		LOGGER.info("Participant "+name+" not found inside choreography "+choreography.getName());
		throw new CDGeneratorException("Participant "+name+" not found inside choreography "
				+choreography.getName()+" see log file for details.");
	}

	public static Participant getParticipantFromName(List<Choreography> choreographies, String name)
			throws CDGeneratorException {

		// log execution flow
		LOGGER.entry();
		for (Choreography choreography : choreographies) {
			for (Participant participant : choreography.getParticipants()) {
				if (participant.getName().equals(name)){
					LOGGER.info("Participant: "+name+" found.");				
					// log execution flow
					LOGGER.exit();
					return participant;
				}	
			}			
		}
		LOGGER.info("Participant: "+name+" not found.");
		throw new CDGeneratorException("Participant: "+name+" not found see log file for details.");
	}	
	
	public static Participant getInitiatingParticipant(ChoreographyActivity choreographyActivity)
			throws CDGeneratorException {
		
		// log execution flow
		LOGGER.entry();
		if (choreographyActivity.getInitiatingParticipantRef() != null){
			LOGGER.info("Initiating participant found in the choreography activity: "
					+ choreographyActivity.getName());	
			// log execution flow
			LOGGER.exit();			
			return choreographyActivity.getInitiatingParticipantRef();
		}
		LOGGER.info("No Initiating participant found in the choreography activity: "
				+ choreographyActivity.getName());
		throw new CDGeneratorException("No Initiating participant found in the choreography activity: " 
				+ choreographyActivity.getName()+" not found see log file for details.");
	}

	public static Participant getReceivingParticipant(ChoreographyActivity choreographyActivity)
			throws CDGeneratorException {
		
		// log execution flow
		LOGGER.entry();			
		for (Participant participant : choreographyActivity.getParticipantRefs()) {
			if (!participant.equals(choreographyActivity.getInitiatingParticipantRef())) {
				LOGGER.info("Receiving participant found in the choreography activity: "
						+ choreographyActivity.getName());			
				// log execution flow
				LOGGER.exit();				
				return participant;
			}
		}
		LOGGER.info("No Receiving participant found in the choreography activity: "
				+ choreographyActivity.getName());
		throw new CDGeneratorException("No Receiving participant found in the choreography activity: " 
				+ choreographyActivity.getName()+" not found see log file for details.");
	}

	
    	public static StartEvent getStartEvent(Choreography choreography) throws CDGeneratorException {
    	
		// log execution flow
		LOGGER.entry();	
        // we assume that the choreography has only one start event
        for (FlowElement flowElement : choreography.getFlowElements()) {
            if (flowElement instanceof StartEvent) {
				LOGGER.info("Start event found in the BPMN2 Choreography: " + choreography.getName());			
				// log execution flow
				LOGGER.exit();         	
                return (StartEvent) flowElement;
            }
        }
		LOGGER.info("No start event found in the BPMN2 Choreography: " + choreography.getName());	        
        throw new CDGeneratorException("No start event found in the BPMN2 Choreography: " 
        		+ choreography.getName() +" not found see log file for details.");
    }
    
    public static String getFirstChoregraphyTaskName(Choreography choreography){
	   
		// log execution flow
		LOGGER.entry();	
		String firstChoreographyTaskName = ((ChoreographyTask)BPMNUtils.getStartEvent(choreography)
				.getOutgoing().get(0).getTargetRef()).getName();
		// we assume that a start has only one outgoing, whose target is a choreography task
		LOGGER.info("First choregraphy task name: " + firstChoreographyTaskName);	
		return firstChoreographyTaskName;
   	}
	
   	public static String getMessageNameChoreographyTaskSentFromParticipant(ChoreographyTask choreographyTask, 
		   Participant participant){

	   // log execution flow
	   LOGGER.entry();		   
	   for (MessageFlow messageFlow : choreographyTask.getMessageFlowRef()) {
		   if(messageFlow.getSourceRef().equals(participant)){
			   LOGGER.info("The message sent from participant: "+participant.getName()
			   		+" within choreography task "+choreographyTask.getName()+" is "
					+messageFlow.getMessageRef().getName());			   
			   // log execution flow
			   LOGGER.exit();			   
			   return messageFlow.getMessageRef().getName();
		   }
	   }		
	   LOGGER.info("No message sent from participant: "+participant.getName()+" within choreography task "
			   +choreographyTask.getName()+" found.");	        
	   // log execution flow
	   LOGGER.exit();
	   return null;
   	}
 
   	public static String getMessageNameChoreographyTaskSentToParticipant(ChoreographyTask choreographyTask, 
		   Participant participant){

	   // log execution flow
	   LOGGER.entry();
	   for (MessageFlow messageFlow : choreographyTask.getMessageFlowRef()) {
		   if(messageFlow.getTargetRef().equals(participant)){
			   LOGGER.info("The message sent to participant: "+participant.getName()
			   		   +" within choreography task "+choreographyTask.getName()+" is "
					   +messageFlow.getMessageRef().getName());				   
			   // log execution flow
			   LOGGER.exit();	
			   return messageFlow.getMessageRef().getName();
		   }
	   }		
	   LOGGER.info("No message sent to participant: "+participant.getName()+" within choreography task "
			   +choreographyTask.getName()+" found.");	        
	   // log execution flow
	   LOGGER.exit();
	   return null;
   	}

   	public static String getMessageTypeNameChoreographyTaskSentFromParticipant(
		   ChoreographyTask choreographyTask, Participant participant){

	   // log execution flow
	   LOGGER.entry();	   
	   for (MessageFlow messageFlow : choreographyTask.getMessageFlowRef()) {
		   if(messageFlow.getSourceRef().equals(participant)){
			   LOGGER.info("The message type name of the message sent from participant: "+participant
					   .getName()+" within choreography task "+choreographyTask.getName()+" is "
					   +BPMNUtils.getMessageTypeName(messageFlow.getMessageRef()));			   
			   // log execution flow
			   LOGGER.exit();
			   return BPMNUtils.getMessageTypeName(messageFlow.getMessageRef());
		   }
	   }		
	   LOGGER.info("No message type name of message sent from participant: "+participant.getName()
	   		+" within choreography task "+choreographyTask.getName()+" found.");	        
	   // log execution flow
	   LOGGER.exit();
	   return null;
   	}   
   
   	public static String getMessageTypeNameChoreographyTaskSentToParticipant(
		   ChoreographyTask choreographyTask, Participant participant){

	   // log execution flow
	   LOGGER.entry();	 	   
	   for (MessageFlow messageFlow : choreographyTask.getMessageFlowRef()) {
		   if(messageFlow.getTargetRef().equals(participant)){
			   LOGGER.info("The message type name of the message sent to participant: "+participant.getName()
		   		+" within choreography task "+choreographyTask.getName()+" is "+BPMNUtils
		   		.getMessageTypeName(messageFlow.getMessageRef()));			   
			   // log execution flow
			   LOGGER.exit();
			   return BPMNUtils.getMessageTypeName(messageFlow.getMessageRef());
		   }
	   }		
	   LOGGER.info("No message type name of message sent to participant: "+participant.getName()
  		+" within choreography task "+choreographyTask.getName()+" found.");
	   // log execution flow
	   LOGGER.exit();
	   return null;
   	}   
   
   	public static ChoreographyMessageInformation getDataTypeChoreographyInformation(
		   List<Choreography> choreographies, String choreographyMessageName){

	   // log execution flow
	   LOGGER.entry();
	   ChoreographyMessageInformation choreographyMessageInformation = new ChoreographyMessageInformation();
	   choreographyMessageInformation.setChoreographyMessageName(choreographyMessageName);	   
	   boolean messageFound = false;
	   // iterate over every choreography	   
	   for (Iterator<Choreography> iterator = choreographies.iterator();
			   iterator.hasNext() && !messageFound;){
		   // get the choregraphy
		   Choreography choreography = (Choreography) iterator.next();
		   for (Iterator<FlowElement> iterator2 = choreography.getFlowElements().iterator();
				   iterator2.hasNext() && !messageFound;){
			   // get the flow element
			   FlowElement flowElement = (FlowElement) iterator2.next();
			   // check if flowElement is a choreography task
			   if(flowElement instanceof ChoreographyTask){
				   // flowElement is a choreography task
				   // iterate over task messages
				   for (MessageFlow messageFlow : ((ChoreographyTask) flowElement).getMessageFlowRef()){
					   // check if the message is the searched one
					   if(messageFlow.getMessageRef().getName().equals(choreographyMessageName)){
						   // the message is the searched one
						   // set choreographyMessageInformation
						   choreographyMessageInformation.setChoreographyTaskName(flowElement.getName()); 
						   choreographyMessageInformation.setInitiatingParticipantName(BPMNUtils
								   .getInitiatingParticipant((ChoreographyActivity) flowElement).getName());
						   choreographyMessageInformation.setReceivingParticipantName(BPMNUtils
								   .getReceivingParticipant((ChoreographyActivity) flowElement).getName());						   
						   choreographyMessageInformation.setDataTypeName(BPMNUtils
								   .getMessageTypeName(messageFlow.getMessageRef()));						   
						   choreographyMessageInformation
						   .setSenderParticipantName(((Participant) messageFlow.getSourceRef()).getName());
						   choreographyMessageInformation
						   .setReceiverParticipantName(((Participant) messageFlow.getTargetRef()).getName());
						   messageFound = true;
					   }
				   }				   
			   }			
		   }
	   }	 
	   // log execution flow
	   LOGGER.exit();
	   return choreographyMessageInformation;
   	}
   
   	public static String getMessageTypeName(Message message){
	   
	   return StringUtils.split(((org.eclipse.emf.ecore.impl.BasicEObjectImpl)message.getItemRef()
			   .getStructureRef()).eProxyURI().toString(),
			   HASH_SYMBOL)[1];	   
   	}
   
   	public static String getDataTypeNameConditionExpressionOfSequenceFlow(SequenceFlow sequenceFlow){
	   
	   return StringUtils.split(((org.eclipse.emf.ecore.impl.BasicEObjectImpl)(
				(FormalExpression) sequenceFlow.getConditionExpression())
				.getEvaluatesToTypeRef().getStructureRef()).eProxyURI().toString(), 
			   HASH_SYMBOL)[1];
   	}
   
   	public static String getConditionBodyConditionExpressionOfSequenceFlow(SequenceFlow sequenceFlow){
	
	   return ((FormalExpressionImpl) sequenceFlow.getConditionExpression()).getBody();
   	}
   
   	public static boolean isTaskInitiatingOfCorrelationData(List<TaskCorrelationData> tasksCorrelationData, 
		   ChoreographyTask task){
	   
	   // log execution flow
	   LOGGER.entry();   
	   for (TaskCorrelationData taskCorrelationData : tasksCorrelationData) {
		   if(taskCorrelationData.getInitiatingTaskName().equals(task.getName())){
			   // log execution flow
			   LOGGER.exit();
			   return true;
		   }	   
	   }
	   // log execution flow
	   LOGGER.exit();
	   return false;
   	}
   
   	public static boolean isTaskReceivingOfCorrelationData(List<TaskCorrelationData> tasksCorrelationData, 
		   ChoreographyTask task){

	   // log execution flow
	   LOGGER.entry(); 	   
	   for (TaskCorrelationData taskCorrelationData : tasksCorrelationData) {
		   if(taskCorrelationData.getReceivingTaskName().equals(task.getName())){
			   // log execution flow
			   LOGGER.exit();
			   return true;
		   }
	   }
	   // log execution flow
	   LOGGER.exit();
	   return false;
   	} 
   
   	public static String getInitiatingTaskNameCorrelatedToReceivingTask(
		   List<TaskCorrelationData> tasksCorrelationData, ChoreographyTask choreographyTask){
	
	   // log execution flow
	   LOGGER.entry(); 
	   for (TaskCorrelationData taskCorrelationData : tasksCorrelationData) {
		   if(taskCorrelationData.getReceivingTaskName().equals(choreographyTask.getName())){
			   LOGGER.info("The initiating choreography task correlated to choreography task "
					   +choreographyTask.getName()+" is "+taskCorrelationData.getInitiatingTaskName());
			   // log execution flow
			   LOGGER.exit();
			   return taskCorrelationData.getInitiatingTaskName();
		   }   
	   }
	   LOGGER.info("No initiating choreography task correlated to choreography task "
			   +choreographyTask.getName()+" found.");	        
	   throw new CDGeneratorException("No initiating choreography task correlated to choreography task "
			   +choreographyTask.getName()+" found not found see log file for details.");
   	}    
         
   	public static boolean isParticipantInvolvedInReceivingCorrelatedTask(List<Choreography> choreographies, 
		   List<TaskCorrelationData> tasksCorrelationData,Participant participant){

	   // log execution flow
	   LOGGER.entry();
	   for (TaskCorrelationData taskCorrelationData : tasksCorrelationData) {
		   ChoreographyTask task = BPMNUtils.getChoreographyTaskFromName(choreographies, taskCorrelationData
				   .getReceivingTaskName());
		   if(BPMNUtils.getReceivingParticipant(task).equals(participant)){
			   // log execution flow
			   LOGGER.exit();
			   return true;
		   }	   
	   }
	   // log execution flow
	   LOGGER.exit();
	   return false;	   
   	}
   
   	public static ChoreographyTask getChoreographyTaskFromName(Choreography choreography,
		   String choreographyTaskName){
	   
	   // log execution flow
	   LOGGER.entry();	   
	   for (FlowElement flowElement : choreography.getFlowElements()) {
		   if (flowElement instanceof ChoreographyTask) {
			   if(flowElement.getName().equals(choreographyTaskName)){
				   LOGGER.info("Choreography task "+choreographyTaskName+" found into choreography "
						   +choreography.getName());
				   // log execution flow
				   LOGGER.exit();					
				   return (ChoreographyTask) flowElement;
			   }
		   }
	   }
	   LOGGER.info("No Choreography task "+choreographyTaskName+" found into choreography "
			   +choreography.getName());	        
	   throw new CDGeneratorException("No Choreography task "+choreographyTaskName
			   +" found into choreography "+choreography.getName()+" see log file for details.");
   	}
   	
   	public static ChoreographyTask getChoreographyTaskFromName(List<Choreography> choreographies, 
   			String choreographyTaskName){
 	   
 	   // log execution flow
 	   LOGGER.entry();	   
 	   
 	   for (Choreography choreography : choreographies) {
 	 	   for (FlowElement flowElement : choreography.getFlowElements()) {
 	 		   if (flowElement instanceof ChoreographyTask) {
 	 			   if(flowElement.getName().equals(choreographyTaskName)){
 	 				   LOGGER.info("Choreography task "+choreographyTaskName+" found into choreography "
 	 						   +choreography.getName());
 	 				   
 	 				   // log execution flow
 	 				   LOGGER.exit();					
 	 				   
 	 				   return (ChoreographyTask) flowElement;
 	 			   }
 	 		   }
 	 	   }	
 	   }
 	   
 	   LOGGER.info("No Choreography task "+choreographyTaskName+" found into choreography!");	        
 	   throw new CDGeneratorException("No Choreography task "+choreographyTaskName+" found!");
    	}
   
   	public static String createChoreographyTaskParticipantsBaseName(ChoreographyTask choreographyTask){
	   
	   return StringUtils.deleteWhitespace(new StringBuilder(BPMNUtils
			   .getInitiatingParticipant(choreographyTask).getName())
			   .append(BPMNUtils.getReceivingParticipant(choreographyTask).getName()).toString());
   	}
   
   	public static String getMessageNameSentToParticipantFromTaskName(List<Choreography> choreographies, 
		   List<TaskCorrelationData> tasksCorrelationData, String initiatingTaskName,
		   Participant participant){
	   
	   // log execution flow
	   LOGGER.entry();	   
	   for (TaskCorrelationData taskCorrelationData : tasksCorrelationData) {
		   if(taskCorrelationData.getInitiatingTaskName().equals(initiatingTaskName)){
			   ChoreographyTask receivingTask = BPMNUtils.getChoreographyTaskFromName(choreographies, 
					   taskCorrelationData.getReceivingTaskName());
			   for (MessageFlow messageFlow : receivingTask.getMessageFlowRef()) {
				   if(messageFlow.getTargetRef().equals(participant)){
					   LOGGER.info("The message sent from participant "+participant.getName()+" is "
							   +messageFlow.getMessageRef().getName());
					   // log execution flow
					   LOGGER.exit();						   
					   return messageFlow.getMessageRef().getName();
				   }
			   }
		   }
	   }	   
	   LOGGER.info("No message sent from participant "+participant.getName()+" found.");	        
	   throw new CDGeneratorException("No message sent from participant "+participant.getName()
	   		+" found see log file for details.");
   	}
   
   	public static String getMessageTypeNameSentToParticipantFromTaskName(List<Choreography> choreographies, 
		   List<TaskCorrelationData> tasksCorrelationData, String initiatingTaskName,
		   Participant participant){

	   // log execution flow
	   LOGGER.entry();		   
	   
	   for (TaskCorrelationData taskCorrelationData : tasksCorrelationData) {
		   if(taskCorrelationData.getInitiatingTaskName().equals(initiatingTaskName)){
			   ChoreographyTask receivingTask = BPMNUtils.getChoreographyTaskFromName(choreographies, 
					   taskCorrelationData.getReceivingTaskName());
			   for (MessageFlow messageFlow : receivingTask.getMessageFlowRef()) {
				   if(messageFlow.getTargetRef().equals(participant)){
					   LOGGER.info("The message type name of the message sent from participant "
							   +participant.getName()+" is "+BPMNUtils.getMessageTypeName(messageFlow
							   .getMessageRef()));
					   // log execution flow
					   LOGGER.exit();	
					   return BPMNUtils.getMessageTypeName(messageFlow.getMessageRef());
				   }
			   }
		   }
	   }	 
	   
	   LOGGER.info("The message type name of the message sent from participant "
			   +participant.getName()+" found.");	        
	   
	   throw new CDGeneratorException("The message type name of the message sent from participant "
			   +participant.getName()+" found see log file for details.");
   	} 
   
   	public static List<String> getMessagesNamesFromSequenceFlowConditionBody(
		   String sequenceFlowConditionBody){

	   // log execution flow
	   LOGGER.entry();
	   
	   // replace all ( and ) with a space
	   sequenceFlowConditionBody = sequenceFlowConditionBody.replaceAll("\\(", " ").replaceAll("\\)", " ");
	   // create the list of messages names
	   List<String> messagesNames = new ArrayList<>();   
	   // remove all the xPathOperators from sequenceFlowConditionBody
	   for (String xPathOperator : XPATH_OPERATORS) {
		   sequenceFlowConditionBody = StringUtils.remove(sequenceFlowConditionBody, " "+xPathOperator+" ");
		   sequenceFlowConditionBody = StringUtils.remove(sequenceFlowConditionBody, " "+xPathOperator);
		   sequenceFlowConditionBody = StringUtils.remove(sequenceFlowConditionBody, xPathOperator+" ");
	   }
	   // remove all digits from sequenceFlowConditionBody
	   sequenceFlowConditionBody = sequenceFlowConditionBody.replaceAll("\\d","");
	   // split sequenceFlowConditionBody by space
	   String[] sequenceFlowConditionBodyItems =  StringUtils.split(sequenceFlowConditionBody, " ");
	   // iterate over sequenceFlowConditionBodyItems
	   for (int i = 0; i < sequenceFlowConditionBodyItems.length; i++) {
		   // check if sequenceFlowConditionBodyItems[i] is not a space and it is not a number
		   if(!sequenceFlowConditionBodyItems[i].equals(" ") && 
				   !StringUtils.isNumeric(sequenceFlowConditionBodyItems[i]) &&
				   !StringUtils.startsWith(sequenceFlowConditionBodyItems[i], "'")){
			   // sequenceFlowConditionBodyItems[i] is not a space and it is not a number and 
			   // it doesn't start with ' add to messagesNames the first string resulting from the split 
			   // of sequenceFlowConditionBodyItems[i] by / character
			   messagesNames.add(sequenceFlowConditionBodyItems[i].split("/")[0]);
		   }
	   }
	   
	   // log execution flow
	   LOGGER.exit();
	   
	   return messagesNames;
   	}
      
   	public static boolean isParticipantInvolvedInitiatingOutgoingTasksDivergingExclusiveGateway(
		   ExclusiveGateway exclusiveGateway, Participant participant){

	   // log execution flow
	   LOGGER.entry();
	   
	   for (int i = 0; i < exclusiveGateway.getOutgoing().size(); i++) {
			// take the i-est outgoing of flowNode
			ChoreographyTask task = (ChoreographyTask) exclusiveGateway.getOutgoing().get(i).getTargetRef();
			if(BPMNUtils.getInitiatingParticipant(task).equals(participant)){
				// log execution flow
				LOGGER.exit(true);				
				return true;
			}	
	   }
	   
	   // log execution flow
	   LOGGER.exit(false);	 
	   
	   return false;
   	}
   						
   	public static boolean hasDivergingExclusiveGatewayAllChoreographyTasksInAllItsOutgoings(
		   ExclusiveGateway exclusiveGateway){
	   
   		// log execution flow
   		LOGGER.entry();
   		
   		for (int i = 0; i < exclusiveGateway.getOutgoing().size(); i++) {
   			if(!(exclusiveGateway.getOutgoing().get(i).getTargetRef() instanceof ChoreographyTask)){
				// log execution flow
				LOGGER.exit(false);				
				return false;
			}	
   		}
   		// log execution flow
   		LOGGER.exit(true);
	   
   		return true;
   }	    
   
  	public static boolean hasExclusiveGatewayToBeImplemented(FlowNode flowNode, Participant participant){
  		
  		// log execution flow
  		LOGGER.entry("CD Participant name "+participant.getName(),"Flow Node name "+flowNode.getName());
   
	    // iterate over outgoing of an exclusive gateway
  		for (int i = 0; i < flowNode.getOutgoing().size(); i++) {	
  			// if the i-est outgoing is a choreography task and the initiating participant is the 
  			// participant in input 
  			if((flowNode.getOutgoing().get(i).getTargetRef() instanceof ChoreographyTask) &&
  					BPMNUtils.getInitiatingParticipant((ChoreographyActivity) flowNode.getOutgoing().get(i)
					   .getTargetRef()).equals(participant)){	   
  					
  					// log execution flow
  					LOGGER.exit(true);			   
  					
  					return true;
  			}
  			// if the i-est outgoing is neither a choreography task nor a converging gateway
  			// it can be either an exclusive diverging or a parallel diverging gateway  		   
  			if(!(flowNode.getOutgoing().get(i).getTargetRef() instanceof ChoreographyTask) &&
			   !(((Gateway) flowNode).getGatewayDirection().compareTo(GatewayDirection.CONVERGING)==0)){
  				// recursive call to
  				// check there is a task for which the participant in input is the initiating participant
  				boolean participantInitiatingOfATask = BPMNUtils.hasExclusiveGatewayToBeImplemented(flowNode
	  				   .getOutgoing().get(i).getTargetRef(), participant);
  				// a task for which, the participant in input is the initiating participant, has been found
  				if(participantInitiatingOfATask){  			   
  					// log execution flow
  					LOGGER.exit(true);  			   
  					
  					return true;
	  		   }	   
		   }
	   }
	   
	   // log execution flow
	   LOGGER.exit(false);
	   
	   // from the exclusive gateway in input    
	   // no task has the participant in input as initiating participant
		   return false;	   
   	}
   
  	public static boolean isParticipantReceiverOfSubChoreography(SubChoreography subChoreography,
  			Participant participant) {
  		
   		// log execution flow
   		LOGGER.entry();		
  		
   		// iterate over the participant of a subchoreography
   		for (Participant participantItem : subChoreography.getParticipantRefs()) {
   			// check if the participantItem is different from the initiating participant
   			// and if it is the same as participant
			if(!participantItem.equals(subChoreography.getInitiatingParticipantRef()) &&
					participant.equals(participant)) {
				// the participantItem is different from the initiating participant
	   			// and it is the same as participant
	   	   		// log execution flow
	   	   		LOGGER.exit(true);

	   	   		return true; 					
			}
		}
   		
   		// log execution flow
   		LOGGER.exit(false);

   		return false; 
  	}
  	
  	
  	public static boolean hasSubChoreographyLoopToBeImplementedIntoCDProsumer(
  			Choreography currentChoreography, SubChoreography subChoreography,
  			Participant participant, List<Choreography> choreographies) {
  		
   		// log execution flow
   		LOGGER.entry();
  		
   		// check if participant is the initiating participant of the subchoreography
   		if(subChoreography.getInitiatingParticipantRef().equals(participant)) {
   			// participant is the initiating participant of the subchoreography
   	   		// log execution flow
   	   		LOGGER.exit(true);
   		   
   	   		return true;    			
   		}
   		else {
   			// participant is one of the receivers participants of the subchoreography 	
   	   		// check if the current choreography contains only the subchoreography 
   			// apart from gateways or events
   	   		// get the start event of the current choreography
   	   		StartEvent startEventCurrentChoreography = BPMNUtils.getStartEvent(currentChoreography);   		
   	   		FlowNode flowNode = null;
   	   		// check if the target ref of the first outgoing of the start event is a subchoreography
   	   		if(startEventCurrentChoreography.getOutgoing().get(0)
   	   				.getTargetRef() instanceof SubChoreography) {
   	   			// the target ref of the first outgoing of the start event is a subchoreography
   	   			// set flowNode to the target ref of the first outgoing of the start event
   	   			flowNode = startEventCurrentChoreography.getOutgoing().get(0).getTargetRef();
   	   		}
   	   		// check if the target ref of the first outgoing of the start event is a gateway or an event
   	   		if(startEventCurrentChoreography.getOutgoing().get(0).getTargetRef() instanceof Gateway ||
   			   startEventCurrentChoreography.getOutgoing().get(0).getTargetRef() instanceof Event) {
   	   			// the target ref of the first outgoing of the start event is a gateway or an event
   	   			// set flowNode to the first outgoing of the target ref of the first outgoing 
   	   			// of the target ref of the start event 
   	   			flowNode = startEventCurrentChoreography.getOutgoing().get(0).getTargetRef()
   	   					.getOutgoing().get(0).getTargetRef();
   	   		}  
   	   		// check if flowNode is not null and if it has the same name of subChoreography
   	   		if(flowNode != null && flowNode instanceof SubChoreography && 
   	   		   flowNode.getName().equals(subChoreography.getName())) {
   	   			// flowNode is not null and if it has the same name of subChoreography
   	   			// check if the target ref of the first outgoing of flowNode is an EndEvent
   	   			if(flowNode.getOutgoing().get(0).getTargetRef() instanceof EndEvent) {
   	   				// the target ref of the first outgoing of flowNode is an EndEvent
   	   				
	   	   	   		// log execution flow
	   	   	   		LOGGER.exit(false);
	
	   	   	   		return false;    	   				
   	   			}
   	   			// check if the target ref of the first outgoing of flowNode is a Gateway or an Event and
   	   			// if the target ref of the first outgoing of the target ref of the first outgoing 
   	   			// of flowNode is an EndEvent
   	   			if((flowNode.getOutgoing().get(0).getTargetRef() instanceof Gateway || 
   	   				flowNode.getOutgoing().get(0).getTargetRef() instanceof Event) &&
   	   				flowNode.getOutgoing().get(0).getTargetRef().getOutgoing().get(0)
   	   				.getTargetRef() instanceof EndEvent) {
   	   	   			// the target ref of the first outgoing of flowNode is a Gateway or an Event and
   	   	   			// if the target ref of the first outgoing of the target ref of the first outgoing 
   	   	   			// of flowNode is an EndEvent  	  
   	   				
	   	   	   		// log execution flow
	   	   	   		LOGGER.exit(false);
	
	   	   	   		return false; 	   				
   	   			}  	   			
   	   		}
   	   		
   		}   		
   		// log execution flow
   		LOGGER.exit(true);
	   
   		return true;   	   		
  	}
  	
 	public static boolean hasSubChoreographyLoopToBeImplementedIntoCDClient(Choreography currentChoreography,
  			SubChoreography subChoreography, Participant participant, List<Choreography> choreographies) {
  		
   		// log execution flow
   		LOGGER.entry();
  		
   		// check if participant is the initiating participant of the subchoreography
   		if(subChoreography.getInitiatingParticipantRef().equals(participant)) {
   			// participant is the initiating participant of the subchoreography
   	   		// log execution flow
   	   		LOGGER.exit(false);
   		   
   	   		return false;    			
   		}
   		else {
   		   			
   			// participant is one of the receivers participants of the subchoreography 	
   	   		// check if the current choreography contains only the subchoreography 
   			// apart from gateways or events
   	   		// get the start event of the current choreography
   	   		StartEvent startEventCurrentChoreography = BPMNUtils.getStartEvent(currentChoreography);   		
   	   		FlowNode flowNode = null;
   	   		// check if the target ref of the first outgoing of the start event is a subchoreography
   	   		if(startEventCurrentChoreography.getOutgoing().get(0)
   	   				.getTargetRef() instanceof SubChoreography) {
   	   			// the target ref of the first outgoing of the start event is a subchoreography
   	   			// set flowNode to the target ref of the first outgoing of the start event
   	   			flowNode = startEventCurrentChoreography.getOutgoing().get(0).getTargetRef();
   	   		}
   	   		// check if the target ref of the first outgoing of the start event is a gateway or an event
   	   		if(startEventCurrentChoreography.getOutgoing().get(0).getTargetRef() instanceof Gateway ||
   			   startEventCurrentChoreography.getOutgoing().get(0).getTargetRef() instanceof Event) {
   	   			// the target ref of the first outgoing of the start event is a gateway or an event
   	   			// set flowNode to the first outgoing of the target ref of the first outgoing 
   	   			// of the target ref of the start event 
   	   			flowNode = startEventCurrentChoreography.getOutgoing().get(0).getTargetRef()
   	   					.getOutgoing().get(0).getTargetRef();
   	   		}  
   	   		// check if flowNode is not null and if it has the same name of subChoreography
   	   		if(flowNode != null && flowNode instanceof SubChoreography && 
   	   		   flowNode.getName().equals(subChoreography.getName())) {
   	   			// flowNode is not null and if it has the same name of subChoreography
   	   			// check if the target ref of the first outgoing of flowNode is an EndEvent
   	   			if(flowNode.getOutgoing().get(0).getTargetRef() instanceof EndEvent) {
   	   				// the target ref of the first outgoing of flowNode is an EndEvent
   	   				
	   	   	   		// log execution flow
	   	   	   		LOGGER.exit(false);
	
	   	   	   		return false;    	   				
   	   			}
   	   			// check if the target ref of the first outgoing of flowNode is a Gateway or an Event and
   	   			// if the target ref of the first outgoing of the target ref of the first outgoing 
   	   			// of flowNode is an EndEvent
   	   			if((flowNode.getOutgoing().get(0).getTargetRef() instanceof Gateway || 
   	   				flowNode.getOutgoing().get(0).getTargetRef() instanceof Event) &&
   	   				flowNode.getOutgoing().get(0).getTargetRef().getOutgoing().get(0)
   	   				.getTargetRef() instanceof EndEvent) {
   	   	   			// the target ref of the first outgoing of flowNode is a Gateway or an Event and
   	   	   			// if the target ref of the first outgoing of the target ref of the first outgoing 
   	   	   			// of flowNode is an EndEvent  	  
   	   				
	   	   	   		// log execution flow
	   	   	   		LOGGER.exit(false);
	
	   	   	   		return false; 	   				
   	   			}  	   			
   	   		}	
   		}   		
   		// log execution flow
   		LOGGER.exit(true);
	   
   		return true;   	   		
  	}

  	public static boolean hasTimerToBeImplemented(FlowNode flowNode, String cdParticipantName) {
  		
   		// log execution flow
   		LOGGER.entry();
  		
   		// check if the target ref of flowNode is either a subchoreography or a choreography task
   		if(flowNode.getOutgoing().get(0).getTargetRef() instanceof ChoreographyTask ||
   		   flowNode.getOutgoing().get(0).getTargetRef() instanceof SubChoreography) {
   			// the target ref of flowNode is either a subchoreography or a choreography task	
   			// check if the initating participant of the the target ref of flowNode 
   			// is the participant related to the CD
   			if(BPMNUtils.getInitiatingParticipant((ChoreographyActivity) flowNode.getOutgoing().get(0)
   					.getTargetRef()).getName().equals(cdParticipantName)){
   	   			// the initating participant of the the target ref of flowNode 
   	   			// is the participant related to the CD   				
   		   		// log execution flow
   		   		LOGGER.exit(true);
   			   
   		   		return true; 	
   			}
   		}
   	   		   		
   		// log execution flow
   		LOGGER.exit(false);
	   
   		return false;   	   		
  	}

  	public static List<FlowNode> getFlowNodes(FlowNode flow){
		
		List<FlowNode> nodes = new ArrayList<>();
		traverseChoreographyFlows(flow, nodes);
		return nodes;
	}
	    
    public static void traverseChoreographyFlows(FlowNode flow, List<FlowNode> nodes) {
    	
    	if(flow instanceof EndEvent || nodes.contains((FlowNode) flow))
    		return;    		
    	for (SequenceFlow outgoing : flow.getOutgoing()) {
			nodes.add(flow);
			traverseChoreographyFlows(outgoing.getTargetRef(), nodes);
		}
    }
    
    public static String getTargetNamespaceChoreography(Choreography choreography){
    	
    	return ((Definitions) choreography.eContainer()).getTargetNamespace();
    }
    
    public static String getTargetNamespaceFromChoreographyData(ChoreographyData choreographyData){
    	
		// get the container from the first choreography in order to retrive the target namespace
    		return ((Definitions) choreographyData.getChoreographies().get(0).eContainer())
    				.getTargetNamespace();
    }
        
	private static Choreography loadBPMNModel(File bpmnFile) throws CDGeneratorException {
		
		URI bpmnURI = URI.createURI(bpmnFile.toURI().toString());
		Choreography choreography = null;

		// register the BPMN2ResourceFactory in Factory registry
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		reg.getExtensionToFactoryMap().put("bpmn", new Bpmn2ResourceFactoryImpl());
		reg.getExtensionToFactoryMap().put("bpmn2", new Bpmn2ResourceFactoryImpl());

		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(bpmnURI);

		try {
			resource.load(null);
			EObject root = resource.getContents().get(0);
			Definitions definitions;

			if (root instanceof DocumentRoot) {
				definitions = ((DocumentRoot) root).getDefinitions();
			} else {
				definitions = (Definitions) root;
			}

			for (EObject definition : definitions.eContents()) {
				if (definition instanceof Choreography) {
					choreography = (Choreography) definition;
				}
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Error to load the resource " + resource.getURI().toFileString());
		}
		if (choreography == null) {
			throw new CDGeneratorException("No choreography found in the model " + resource.getURI()
				.toFileString());
		}
		return choreography;
	}

	private static List<Choreography> loadChoreographiesFromBPMN2Model(File bpmnFile){

		// log execution flow
		LOGGER.entry();
		List<Choreography> choreographies = new ArrayList<Choreography>();		
		// register the BPMN2ResourceFactory in Factory registry
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		reg.getExtensionToFactoryMap().put("bpmn", new Bpmn2ResourceFactoryImpl());
		reg.getExtensionToFactoryMap().put("bpmn2", new Bpmn2ResourceFactoryImpl());		
		URI bpmnURI = URI.createURI(bpmnFile.toURI().toString());		
		// load the resource and resolve
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(bpmnURI);
		
		try {
			// load the resource
			resource.load(null);
			DocumentRoot documentRoot = (DocumentRoot) resource.getContents().get(0);			
	        // get all the choreographies of the BPMN2 Model 
	        for (EObject definition : documentRoot.getDefinitions().eContents()) {
	            if (definition instanceof Choreography) {
	                Choreography choreography = (Choreography) definition;
	                choreographies.add(choreography);
	            }
	        }
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into loadChoreographiesFromBPMN2Mode see log file for details ");
		}
		// log execution flow
		LOGGER.exit();		
		return choreographies;
	}

	public static String getLoopIndexName(SubChoreography s) {
		
		// log execution flow
		LOGGER.entry();		
		if (s.getExtensionValues().size() > 0) {
			ExtensionAttributeValue eav = s.getExtensionValues().get(0);
			for (FeatureMap.Entry aValue : eav.getValue()) {
				AnyTypeImpl ati = (AnyTypeImpl) aValue.getValue();
				if (((String) ati.getAnyAttribute().getValue(0)).trim().equals("loopindexname")) {
					AnyTypeImpl val = (AnyTypeImpl) ati.getAny().get(0).getValue();
					// log index name found
					LOGGER.info("Loop index name found!");	
					// log execution flow
					LOGGER.exit();
					return (String) val.getMixed().get(0).getValue();
				}
			}
		}
		// log index name found
		LOGGER.info("Loop index name not found!");	
		// log execution flow
		LOGGER.exit();
		return null;
	}

	public static String getLoopMaximumIterationsNumber(SubChoreography s) {
		
		// log execution flow
		LOGGER.entry();	
		if (s.getExtensionValues().size() > 0) {
			ExtensionAttributeValue eav = s.getExtensionValues().get(0);
			for (FeatureMap.Entry aValue : eav.getValue()) {
				AnyTypeImpl ati = (AnyTypeImpl) aValue.getValue();
				if (((String) ati.getAnyAttribute().getValue(0)).trim()
						.equals("loopmaximumiterationsnumber")) {
					AnyTypeImpl val = (AnyTypeImpl) ati.getAny().get(0).getValue();
					// log index name found
					LOGGER.info("Loop Maximum Iterations Number found!");	
					// log execution flow
					LOGGER.exit();
					return (String) val.getMixed().get(0).getValue();
				}
			}
		}
		// log index name found
		LOGGER.info("Loop Maximum Iterations Number not found!");	
		// log execution flow
		LOGGER.exit();		
		return null;
	}	
	
	public static String getLoopNumericExpression(FlowElement node) {
		
		// log execution flow
		LOGGER.entry();	
		if (node.getExtensionValues().size() > 0) {
			ExtensionAttributeValue eav = node.getExtensionValues().get(0);
			for (FeatureMap.Entry aValue : eav.getValue()) {
				AnyTypeImpl ati = (AnyTypeImpl) aValue.getValue();
				if (((String) ati.getAnyAttribute().getValue(0)).trim()
						.equals("loopnumericexpression")) {
					AnyTypeImpl val = (AnyTypeImpl) ati.getAny().get(0).getValue();					
					if(val.getMixed().get(0).getValue().equals("")) {
						// log index name found
						LOGGER.info("Loop Numeric Expression not found!");	
						// log execution flow
						LOGGER.exit();		
						return null;
					}
					else {
						// log index name found
						LOGGER.info("Loop Numeric Expression found!");	
						// log execution flow
						LOGGER.exit();
						return (String) val.getMixed().get(0).getValue();
					}
				}
			}
		}
		// log index name found
		LOGGER.info("Loop Numeric Expression not found!");	
		// log execution flow
		LOGGER.exit();		
		return null;
	}
	
	public static String getLoopConditionalExpression(FlowElement node) {
		
		// log execution flow
		LOGGER.entry();	
		if (node.getExtensionValues().size() > 0) {
			ExtensionAttributeValue eav = node.getExtensionValues().get(0);
			for (FeatureMap.Entry aValue : eav.getValue()) {
				AnyTypeImpl ati = (AnyTypeImpl) aValue.getValue();
				if (((String) ati.getAnyAttribute().getValue(0)).trim()
						.equals("loopconditionalexpression")) {		
					AnyTypeImpl val = (AnyTypeImpl) ati.getAny().get(0).getValue();
					if(val.getMixed().get(0).getValue().equals("")) {
						// log index name found
						LOGGER.info("Loop Conditional Expression not found!");	
						// log execution flow
						LOGGER.exit();		
						return null;					
					}
					else {
						// log index name found
						LOGGER.info("Loop Conditional Expression found!");	
						// log execution flow
						LOGGER.exit();
						return (String) val.getMixed().get(0).getValue();
					}
				}
			}
		}
		// log index name found
		LOGGER.info("Loop Conditional Expression not found!");	
		// log execution flow
		LOGGER.exit();		
		return null;
	}
	
	public static boolean hasFlowNodeProcessed(FlowNode flowNode, Deque<String> currentPath,
			Set<String> processedFlowNodes) {
		
		// log execution flow
		LOGGER.entry();	
		
		// create path of the visited flow nodes  
		String flowNodeWithPath = "";
		for (String pathItem : currentPath) {
			// check if pathItem is the first element of currentPath
			if(currentPath.getFirst().equals(pathItem)) {
				// pathItem is the first element of currentPath
				flowNodeWithPath += pathItem;
			}
			else {
				// pathItem is not the first element of currentPath
				flowNodeWithPath += HASH_SYMBOL+pathItem;
			}
		}		
		// add the id of flowNode to the path of the visited flow nodes  
		flowNodeWithPath+=HASH_SYMBOL+flowNode.getId();
		// check if the flowNode has been processed
		if(processedFlowNodes.contains(flowNodeWithPath)) {

			// log execution flow
			LOGGER.exit();		
			
			// the flowNode has been processed				
			return true;
		}

		// log execution flow
		LOGGER.exit();		
		
		// the flowNode has not been processed			
		return false;
	}

	public static String createFlowNodePath(FlowNode flowNode, Deque<String> currentPath) {
		
		// log execution flow
		LOGGER.entry();	
		
		// create path of the visited flow nodes  
		String flowNodeWithPath = "";
		for (String pathItem : currentPath) {
			// check if pathItem is the first element of currentPath
			if(currentPath.getFirst().equals(pathItem)) {
				// pathItem is the first element of currentPath
				flowNodeWithPath += pathItem;
			}
			else {
				// pathItem is not the first element of currentPath
				flowNodeWithPath += HASH_SYMBOL+pathItem;
			}
		}
		// add the id of flowNode to the path of the visited flow nodes  
		flowNodeWithPath+=HASH_SYMBOL+flowNode.getId();
		
		// log execution flow
		LOGGER.exit();	
		
		return flowNodeWithPath;
	}
	
}
