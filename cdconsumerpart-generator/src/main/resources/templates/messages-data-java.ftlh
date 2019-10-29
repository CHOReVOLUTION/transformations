package ${generatingJavaData.modelPackageName};

import java.util.List;

public class MessagesData<T> {
	
	private String senderParticipantName;
	private String receivingParticipantName;	
	private String taskName;
	private List<T> messages;

	public MessagesData() {
		super();
	}

	public MessagesData(String senderParticipantName, String receivingParticipantName, String taskName, List<T> messages) {
		super();
		this.senderParticipantName = senderParticipantName;
		this.receivingParticipantName = receivingParticipantName;
		this.taskName = taskName;
		this.messages = messages;
	}

	public String getSenderParticipantName() {
		return senderParticipantName;
	}
	public void setSenderParticipantName(String senderParticipantName) {
		this.senderParticipantName = senderParticipantName;
	}
	public String getReceivingParticipantName() {
		return receivingParticipantName;
	}
	public void setReceivingParticipantName(String receivingParticipantName) {
		this.receivingParticipantName = receivingParticipantName;
	}	
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public List<T> getMessages() {
		return messages;
	}
	public void setMessages(List<T> messages) {
		this.messages = messages;
	}
	
	public void storeMessage(T message){
		messages.add(message);
	}
	
}
