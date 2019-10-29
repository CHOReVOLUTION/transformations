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

public class OperationData {
	
	private String name;
	private String portTypeName;
	private String targetNS;
	private MessageData inputMessageData;
	private MessageData outputMessageData;
	
	public OperationData() {
		super();
		inputMessageData = new MessageData();
		outputMessageData = null;
	}

	public OperationData(String name, String portTypeName, String targetNS, MessageData inputMessageData,
			MessageData outputMessageData) {
		super();
		this.name = name;
		this.portTypeName = portTypeName;
		this.targetNS = targetNS;
		this.inputMessageData = inputMessageData;
		this.outputMessageData = outputMessageData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPortTypeName() {
		return portTypeName;
	}

	public void setPortTypeName(String portTypeName) {
		this.portTypeName = portTypeName;
	}

	public String getTargetNS() {
		return targetNS;
	}

	public void setTargetNS(String targetNS) {
		this.targetNS = targetNS;
	}

	public MessageData getInputMessageData() {
		return inputMessageData;
	}

	public void setInputMessageData(MessageData inputMessageData) {
		this.inputMessageData = inputMessageData;
	}

	public MessageData getOutputMessageData() {
		return outputMessageData;
	}

	public void setOutputMessageData(MessageData outputMessageData) {
		this.outputMessageData = outputMessageData;
	}

	@Override
	public String toString() {
		if(outputMessageData == null)
			return "OperationData [name=" + name + ", portTypeName=" + portTypeName + ", targetNS=" 
				+ targetNS+ ", inputMessageData=" + inputMessageData.toString() +"]";
		else
			return "OperationData [name=" + name + ", portTypeName=" + portTypeName + ", targetNS=" 
				+ targetNS+ ", inputMessageData=" + inputMessageData.toString() + ", outputMessageData=" 
				+ outputMessageData.toString() + "]";
	}
	
	

}
