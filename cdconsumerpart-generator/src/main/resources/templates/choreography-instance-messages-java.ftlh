package ${generatingJavaData.businessPackageName};

import java.util.List;

public interface ChoreographyInstanceMessages<T> {

	T getMessageSentFromParticipant(String messageName, String senderParticipantName, String taskName);
	List<T> getMessagesSentFromParticipant(String messageName, String senderParticipantName, String taskName);
	T getMessageSentFromParticipant(String messageName, String senderParticipantName);
	List<T> getMessagesSentFromParticipant(String messageName, String senderParticipantName);		
	T getMessageReceivedFromParticipant(String messageName, String receiverParticipantName, String taskName);
	List<T> getMessagesReceivedFromParticipant(String messageName, String receiverParticipantName, String taskName);
	T getMessageReceivedParticipant(String messageName, String receiverParticipantName);
	List<T> getMessagesReceivedFromParticipant(String messageName, String receiverParticipantName);	
	T getMessage(String messageName);
	List<T> getMessages(String messageName);		

}
