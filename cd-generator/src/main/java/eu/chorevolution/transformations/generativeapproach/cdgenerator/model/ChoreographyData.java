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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ws.commons.schema.XmlSchema;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.Participant;

public class ChoreographyData {

	private List<Choreography> choreographies;
	private Map<String, Participant> participants = new HashMap<>();
	private XmlSchema schema;
	
	public ChoreographyData(List<Choreography> choreographies,  XmlSchema schema) {
		super();
		this.choreographies = choreographies;
		for (Choreography choreography : choreographies) {
			for (Participant element : choreography.getParticipants()) {
				participants.put(element.getName(), element);		
			}		
		}
		this.setSchema(schema);
	}

	public List<Choreography> getChoreographies() {
		return choreographies;
	}

	public void setChoreographies(List<Choreography> choreographies) {
		this.choreographies = choreographies;
	}

	public Map<String, Participant> getParticipants() {
		return participants;
	}
	
	public Participant getParticipantFromName(String name) {
		return participants.get(name);		
	}
	
	public void setParticipants(Map<String, Participant> participants) {
		this.participants = participants;
	}

	public XmlSchema getSchema() {
		return schema;
	}

	public void setSchema(XmlSchema schema) {
		this.schema = schema;
	}
	
}
