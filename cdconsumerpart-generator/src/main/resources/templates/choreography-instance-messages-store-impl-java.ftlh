package ${generatingJavaData.businessPackageName}.impl.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ${generatingJavaData.businessPackageName}.ChoreographyInstanceMessagesStore;
import ${generatingJavaData.modelPackageName}.MessagesData;

public class ChoreographyInstanceMessagesStoreImpl<T> implements ChoreographyInstanceMessagesStore<T> {

	private static Logger logger = LoggerFactory.getLogger(ChoreographyInstanceMessagesStoreImpl.class);

	// messageName, messagesData
	private HashMap<String, List<MessagesData<T>>> messages;

	public ChoreographyInstanceMessagesStoreImpl(HashMap<String, List<MessagesData<T>>> messages) {
		
		this.messages = messages;
	}
	
	@Override
	public T getMessageSentFromParticipant(String messageName, String senderParticipantName, String taskName) {

		logger.info("Get Message " + messageName +" sent from "+senderParticipantName +" for task "+taskName);
		List<T> messagesRetrieved = getMessagesSentFromParticipant(messageName, senderParticipantName, taskName);
		if(messagesRetrieved != null){
			return messagesRetrieved.get(0);
		}
		logger.error("No message "+messageName+" sent from "+senderParticipantName +" for task "+taskName+" found!");
		return null;	
	}
	
	@Override	
	public List<T> getMessagesSentFromParticipant(String messageName, String senderParticipantName, String taskName) {

		logger.info("Get Messages " + messageName +" sent from "+senderParticipantName +" for task "+taskName);
		if(messages.containsKey(messageName)){
			MessagesData<T> messagesData = getMessagesDataSentFromParticipant(messageName, senderParticipantName, taskName);
			if(messagesData != null){
				return messagesData.getMessages();		
			}			
		}
		logger.error("No messages "+messageName+" sent from "+senderParticipantName +" for task "+taskName+" found!");
		return null;
	}
	
	@Override
	public T getMessageSentFromParticipant(String messageName, String senderParticipantName) {

		logger.info("Get Message " + messageName +" sent from "+senderParticipantName);
		List<T> messagesRetrieved = getMessagesSentFromParticipant(messageName, senderParticipantName);		
		if(messagesRetrieved != null){
			return messagesRetrieved.get(0);
		}
		logger.error("No message "+messageName+" sent from "+senderParticipantName +" found!");
		return null;	
	}

	@Override
	public List<T> getMessagesSentFromParticipant(String messageName, String senderParticipantName) {
		
		logger.info("Get Messages " + messageName +" sent from "+senderParticipantName);
		if(messages.containsKey(messageName)){
			MessagesData<T> messagesData = getMessagesDataSentFromParticipant(messageName, senderParticipantName);
			if(messagesData != null){
				return messagesData.getMessages();		
			}			
		}		
		logger.error("No messages "+messageName+" sent from "+senderParticipantName +" found!");
		return null;
	}

	@Override
	public T getMessageReceivedFromParticipant(String messageName, String receiverParticipantName, String taskName) {
		
		logger.info("Get Message " + messageName +" received from "+receiverParticipantName +" for task "+taskName);
		List<T> messagesRetrieved = getMessagesReceivedFromParticipant(messageName, receiverParticipantName, taskName);
		if(messagesRetrieved != null){
			return messagesRetrieved.get(0);
		}
		logger.error("No message "+messageName+" received from "+receiverParticipantName +" for task "+taskName+" found!");
		return null;
	}

	@Override
	public List<T> getMessagesReceivedFromParticipant(String messageName, String receiverParticipantName,
			String taskName) {

		logger.info("Get Messages " + messageName +" received from "+receiverParticipantName +" for task "+taskName);
		if(messages.containsKey(messageName)){
			MessagesData<T> messagesData = getMessagesDataReceivedFromParticipant(messageName, receiverParticipantName, taskName);
			if(messagesData != null){
				return messagesData.getMessages();		
			}			
		}
		logger.error("No messages "+messageName+" received from "+receiverParticipantName +" for task "+taskName+" found!");
		return null;
	}

	@Override
	public T getMessageReceivedParticipant(String messageName, String receiverParticipantName) {

		logger.info("Get Message " + messageName +" received from "+receiverParticipantName);
		List<T> messagesRetrieved = getMessagesReceivedFromParticipant(messageName, receiverParticipantName);		
		if(messagesRetrieved != null){
			return messagesRetrieved.get(0);
		}
		logger.error("No message "+messageName+" sent from "+receiverParticipantName +" found!");
		return null;	
	}

	@Override
	public List<T> getMessagesReceivedFromParticipant(String messageName, String receivingParticipantName) {
		
		logger.info("Get Messages " + messageName +" received from "+receivingParticipantName);
		if(messages.containsKey(messageName)){
			MessagesData<T> messagesData = getMessagesDataReceivedFromParticipant(messageName, receivingParticipantName);
			if(messagesData != null){
				return messagesData.getMessages();		
			}			
		}		
		logger.error("No messages "+messageName+" received from "+receivingParticipantName +" found!");
		return null;
	}	
	
	@Override
	public T getMessage(String messageName) {
		
		logger.info("Get Message " + messageName);	
		List<T> messagesRetrieved = getMessages(messageName);
		if(messagesRetrieved != null){
			return messagesRetrieved.get(0);
		}
		logger.error("No messages "+messageName+" found!");
		return null;
	}

	@Override
	public List<T> getMessages(String messageName) {
		
		logger.info("Get Messages " + messageName);
		if(messages.containsKey(messageName)){
			List<T> messagesRetrieved = new ArrayList<>();
			for (MessagesData<T> messageData : messages.get(messageName)) {
				messagesRetrieved.addAll(messageData.getMessages());
			}			
			return messagesRetrieved;
		}
		logger.error("No messages "+messageName+" found!");			
		return null;
	}	

	@Override
	public void storeMessage(String senderParticipantName, String receivingParticipantName, String messageName, String taskName, T message) {

		logger.info("Storing message " + messageName + " sent from " + senderParticipantName+" for task "+taskName);
		// check if an entry for the message exists
		if(messages.containsKey(messageName)){
			// an entry for the message exists
			// get messages data
			MessagesData<T> messagesData = getMessagesData(senderParticipantName, receivingParticipantName, messageName, taskName);					
			if(messagesData == null){
				// no message data found
				// create message data
				messagesData = new MessagesData<T>(senderParticipantName, receivingParticipantName, taskName, new ArrayList<>());	
				// add message data to message entry
				messages.get(messageName).add(messagesData);
			}
			// store message 
			messagesData.storeMessage(message);
		}
		else{
			// an entry for the message doesn't exist		
			// create messages data list
			List<MessagesData<T>> messagesDataList = new ArrayList<>();
			// create messages data
			MessagesData<T> messagesData = new MessagesData<T>(senderParticipantName, receivingParticipantName, taskName, new ArrayList<>());
			// store message inside messages data
			messagesData.storeMessage(message);
			// add messages data to messages data list
			messagesDataList.add(messagesData);
			// add the entry just created for the message
			messages.put(messageName, messagesDataList);	
		}	
	}
	
	private MessagesData<T> getMessagesData(String senderParticipantName, String receivingParticipantName, String messageName, String taskName){
		
		for (MessagesData<T> messageData : messages.get(messageName)) {
			if(messageData.getSenderParticipantName().equals(senderParticipantName) &&
					messageData.getReceivingParticipantName().equals(receivingParticipantName) &&
					messageData.getTaskName().equals(taskName)){
				return messageData;
			}
		}
		return null;
	}	
	
	private MessagesData<T> getMessagesDataSentFromParticipant(String messageName, String senderParticipantName, String taskName){
		
		for (MessagesData<T> messageData : messages.get(messageName)) {
			if(messageData.getSenderParticipantName().equals(senderParticipantName) && messageData.getTaskName().equals(taskName)){
				return messageData;
			}
		}
		return null;
	}

	private MessagesData<T> getMessagesDataSentFromParticipant(String messageName, String senderParticipantName){
		
		for (MessagesData<T> messageData : messages.get(messageName)) {
			if(messageData.getSenderParticipantName().equals(senderParticipantName)){
				return messageData;
			}
		}
		return null;
	}
	
	private MessagesData<T> getMessagesDataReceivedFromParticipant(String messageName, String receivingParticipantName, String taskName){
		
		for (MessagesData<T> messageData : messages.get(messageName)) {
			if(messageData.getReceivingParticipantName().equals(receivingParticipantName) && messageData.getTaskName().equals(taskName)){
				return messageData;
			}
		}
		return null;
	}

	private MessagesData<T> getMessagesDataReceivedFromParticipant(String messageName, String receivingParticipantName){
		
		for (MessagesData<T> messageData : messages.get(messageName)) {
			if(messageData.getReceivingParticipantName().equals(receivingParticipantName)){
				return messageData;
			}
		}
		return null;
	}

}
