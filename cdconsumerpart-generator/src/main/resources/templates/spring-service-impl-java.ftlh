package ${generatingJavaData.businessPackageName}.impl.service;

import org.springframework.stereotype.Service;

<#list generatingJavaData.receivingOperations as receivingOperation>
import ${generatingJavaData.basePackageName}.${receivingOperation.inputBusinessMessageType};
<#if receivingOperation.requestResponse>
import ${generatingJavaData.basePackageName}.${receivingOperation.outputBusinessMessageType};
</#if>
</#list>  
<#list generatingJavaData.initiatingSendOperations as initiatingSendOperation>
import ${generatingJavaData.basePackageName}.${initiatingSendOperation.inputBusinessMessageType};
</#list> 
<#list generatingJavaData.initiatingReceiveOperations as initiatingReceiveOperation>
import ${generatingJavaData.basePackageName}.${initiatingReceiveOperation.outputBusinessMessageType};
</#list>    

import ${generatingJavaData.businessPackageName}.ChoreographyInstanceMessages;
import ${generatingJavaData.businessPackageName}.${generatingJavaData.simpleName}Service;

import ${generatingJavaData.modelPackageName}.ChoreographyLoopIndexes;

@Service
public class ${generatingJavaData.simpleName}ServiceImpl implements ${generatingJavaData.simpleName}Service {

<#list generatingJavaData.receivingOperations as receivingOperation>
<#if receivingOperation.requestResponse>
	@Override
	public ${receivingOperation.outputBusinessMessageType} ${receivingOperation.name}(${receivingOperation.inputBusinessMessageType} parameter, String choreographyTaskName, String senderParticipantName) {
		/**
		*	TODO Add your business logic upon the reception of ${receivingOperation.inputBusinessMessageType} message from senderParticipantName
		*	     and reply with ${receivingOperation.outputBusinessMessageType}
		*/
		return null;
	}     
<#else>
	@Override
	public void ${receivingOperation.name}(${receivingOperation.inputBusinessMessageType} parameter, String choreographyTaskName, String senderParticipantName) {
		/**
		*	TODO Add your business logic upon the reception of ${receivingOperation.inputBusinessMessageType} message from senderParticipantName
		*/
	}     
</#if>

</#list>
<#list generatingJavaData.initiatingSendOperations as initiatingSendOperation>
    @Override
<#if initiatingSendOperation.loopIndexes>
	public ${initiatingSendOperation.inputBusinessMessageType} ${initiatingSendOperation.name?replace("send", "create")}(ChoreographyInstanceMessages choreographyInstanceMessages, String choreographyTaskName, String receiverParticipantName, ChoreographyLoopIndexes choreographyLoopIndexes) {
<#else>
	public ${initiatingSendOperation.inputBusinessMessageType} ${initiatingSendOperation.name?replace("send", "create")}(ChoreographyInstanceMessages choreographyInstanceMessages, String choreographyTaskName, String receiverParticipantName) {
</#if>    
    	${initiatingSendOperation.inputBusinessMessageType} result = new ${initiatingSendOperation.inputBusinessMessageType}();
    	/**
		 *	TODO write the code that generates ${initiatingSendOperation.inputBusinessMessageType} message 
		 *	that has to be sent to receiverParticipantName within the interaction belonging to choreographyTaskName
		 *	You can exploit the following API from choreographyInstanceMessages
		 *	1. Get a Message using its name 
		 *		- example: 
		 *			EcoRoutesRequest message = (EcoRoutesRequest) choreographyInstanceMessages.getMessage("EcoRoutesRequest");
		 *		A null value is returned if no message has been found.
		 *	2. Get a list of messages using a message name
		 *		- example: 
		 *			List<EcoRoutesRequest> messages = choreographyInstanceMessages.getMessages("EcoRoutesRequest");
		 *		A null value is returned if no message has been found. 
		 *	3. Get a message using its name and the name of the sender participant
		 *		- example: 
		 *			EcoRoutesRequest message = (EcoRoutesRequest) choreographyInstanceMessages.getMessageSentFromParticipant("EcoRoutesRequest","ND");
		 *		A null value is returned if no message has been found. 
		 *	4. Get a list of messages using a message name and a sender participant name
		 *		- example: 
		 *			List<EcoRoutesRequest> messages = choreographyInstanceMessages.getMessagesSentFromParticipant("EcoRoutesRequest","ND"); 
		 *		A null value is returned if no message has been found.
		 *	5. Get a message using its name, the name of the sender participant and the name of the task in which 
		 *		the message has been exchanged
		 *		- example: 
		 *			EcoRoutesRequest message = (EcoRoutesRequest) choreographyInstanceMessages.getMessageSentFromParticipant("EcoRoutesRequest","ND","Get Eco Routes");	
		 *		A null value is returned if no message has been found.
		 *	6. Get a list of messages using a message name, the name of the sender participant and the name of the task in which 
		 *		the messages has been exchanged
		 *		- example: 
		 *			List<EcoRoutesRequest> messages = choreographyInstanceMessages.getMessagesSentFromParticipant("EcoRoutesRequest","ND","Get Eco Routes");
		 *		A null value is returned if no message has been found.
		 *	7. Get a message using its name and the name of the receiver participant
		 *		- example: 
		 *			RoutesRequest message = (RoutesRequest) choreographyInstanceMessages.getMessageReceivedParticipant("RoutesRequest", "DTS-GOOGLE");
		 *		A null value is returned if no message has been found. 
		 *	8. Get a message using its name, the name of the receiver participant and the name of the task in which 
		 *		the message has been exchanged
		 *		- example: 
		 *			RoutesRequest message = (RoutesRequest) choreographyInstanceMessages.getMessageReceivedFromParticipant("RoutesRequest", "DTS-GOOGLE", "Routes Request");;	
		 *		A null value is returned if no message has been found.
		 *	9. Get a list of messages using a message name and a receiver participant name
		 *		- example: 
         *			List<RoutesRequest> messages = choreographyInstanceMessages.getMessagesReceivedFromParticipant("RoutesRequest", "DTS-GOOGLE");
		 *	10. Get a list of messages using a message name, the name of the receiver participant and the name of the task in which 
		 *		the messages has been exchanged
		 *		- example: 
		 *			List<RoutesRequest> messages = choreographyInstanceMessages.getMessagesReceivedFromParticipant("RoutesRequest", "DTS-GOOGLE", "Routes Request");
		 *		A null value is returned if no message has been found.
		 */	
    	return result;
    }
    
</#list>
<#list generatingJavaData.initiatingReceiveOperations as initiatingReceiveOperation>
	@Override    
    public void ${initiatingReceiveOperation.name}(${initiatingReceiveOperation.outputBusinessMessageType} parameter, String choreographyTaskName, String senderParticipantName) {
    	/**
		*	TODO Add your business logic upon the reception of (${initiatingReceiveOperation.outputBusinessMessageType} message from senderParticipantName 
		*		 within the interaction belonging to choreographyTaskName
		*/
    }
    
</#list>

}
