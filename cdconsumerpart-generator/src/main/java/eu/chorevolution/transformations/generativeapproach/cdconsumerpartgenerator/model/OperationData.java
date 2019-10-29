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
package eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model;

@SuppressWarnings("serial")
public class OperationData implements java.io.Serializable {

	private String name;
	private String inputMessageType;
	private String outputMessageType;
	private String inputBusinessMessageType;
	private String outputBusinessMessageType;
	private boolean requestResponse;
	
	private static final String SEND_REQUEST_TYPE_WITH_LOOP = "SendRequestTypeWithLoop";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInputMessageType() {
		return inputMessageType;
	}

	public void setInputMessageType(String inputMessageType) {
		this.inputMessageType = inputMessageType;
	}

	public String getOutputMessageType() {
		return outputMessageType;
	}

	public void setOutputMessageType(String outputMessageType) {
		this.outputMessageType = outputMessageType;
	}

	public String getInputBusinessMessageType() {
		return inputBusinessMessageType;
	}

	public void setInputBusinessMessageType(String inputBusinessMessageType) {
		this.inputBusinessMessageType = inputBusinessMessageType;
	}

	public boolean isRequestResponse() {
		return requestResponse;
	}

	public String getOutputBusinessMessageType() {
		return outputBusinessMessageType;
	}

	public void setOutputBusinessMessageType(String outputBusinessMessageType) {
		requestResponse = !((outputBusinessMessageType == null || "".equals(outputBusinessMessageType)));
		this.outputBusinessMessageType = outputBusinessMessageType;
	}
	
	public boolean isLoopIndexes() {
		return SEND_REQUEST_TYPE_WITH_LOOP.equalsIgnoreCase(inputMessageType);
	}

}
