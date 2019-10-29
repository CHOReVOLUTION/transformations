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

public class ProjectionData {

	private String participantName;
	private String cdName;
	private byte[] bpmn;
	private byte[] types;
		
	public ProjectionData() {
		super();
	}

	public ProjectionData(String participantName, String cdName, byte[] bpmn, byte[] types) {
		super();
		this.participantName = participantName;
		this.cdName = cdName;
		this.bpmn = bpmn;
		this.types = types;
	}

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	public String getCdName() {
		return cdName;
	}

	public void setCdName(String cdName) {
		this.cdName = cdName;
	}

	public byte[] getBpmn() {
		return bpmn;
	}

	public void setBpmn(byte[] bpmn) {
		this.bpmn = bpmn;
	}

	public byte[] getTypes() {
		return types;
	}

	public void setTypes(byte[] types) {
		this.types = types;
	}
	
}
