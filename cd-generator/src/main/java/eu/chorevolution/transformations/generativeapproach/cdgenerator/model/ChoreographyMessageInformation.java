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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.model;

public class ChoreographyMessageInformation {

	private String choreographyMessageName;
	private String dataTypeName;
	private String choreographyTaskName;
	private String initiatingParticipantName;
	private String receivingParticipantName;
	private String senderParticipantName;
	private String receiverParticipantName;
	
	public ChoreographyMessageInformation() {
		super();
	}

	public String getChoreographyMessageName() {
		return choreographyMessageName;
	}

	public void setChoreographyMessageName(String choreographyMessageName) {
		this.choreographyMessageName = choreographyMessageName;
	}	
	
	public String getDataTypeName() {
		return dataTypeName;
	}

	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

	public String getChoreographyTaskName() {
		return choreographyTaskName;
	}

	public void setChoreographyTaskName(String choreographyTaskName) {
		this.choreographyTaskName = choreographyTaskName;
	}

	public String getInitiatingParticipantName() {
		return initiatingParticipantName;
	}

	public void setInitiatingParticipantName(String initiatingParticipantName) {
		this.initiatingParticipantName = initiatingParticipantName;
	}

	public String getReceivingParticipantName() {
		return receivingParticipantName;
	}

	public void setReceivingParticipantName(String receivingParticipantName) {
		this.receivingParticipantName = receivingParticipantName;
	}

	public String getSenderParticipantName() {
		return senderParticipantName;
	}

	public void setSenderParticipantName(String senderParticipantName) {
		this.senderParticipantName = senderParticipantName;
	}

	public String getReceiverParticipantName() {
		return receiverParticipantName;
	}

	public void setReceiverParticipantName(String receiverParticipantName) {
		this.receiverParticipantName = receiverParticipantName;
	}
	
}
