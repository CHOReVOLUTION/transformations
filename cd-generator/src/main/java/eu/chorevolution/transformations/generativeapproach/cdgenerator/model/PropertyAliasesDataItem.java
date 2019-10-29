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

import java.util.ArrayList;
import java.util.List;

public class PropertyAliasesDataItem {
	
	private String participantName;
	private String wsdlFileName;
	private String wsdlTNS;
	private List<PropertyAliasesMessageType> propertyAliasesMessages;
		
	public PropertyAliasesDataItem() {
		super();
	}

	public PropertyAliasesDataItem(String participantName, String wsdlFileName, String wsdlTNS,
			List<PropertyAliasesMessageType> propertyAliasesMessages) {
		super();
		this.participantName = participantName;
		this.wsdlFileName = wsdlFileName;
		this.wsdlTNS = wsdlTNS;
		this.propertyAliasesMessages = propertyAliasesMessages;
	}
	
	public PropertyAliasesDataItem(String participantName, String wsdlFileName, String wsdlTNS,
			PropertyAliasesMessageType propertyAliasesMessage) {
		super();
		this.participantName = participantName;
		this.wsdlFileName = wsdlFileName;
		this.wsdlTNS = wsdlTNS;	
		this.propertyAliasesMessages = new ArrayList<>();
		this.propertyAliasesMessages.add(propertyAliasesMessage);
	}	

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	public String getWsdlFileName() {
		return wsdlFileName;
	}

	public void setWsdlFileName(String wsdlFileName) {
		this.wsdlFileName = wsdlFileName;
	}

	public String getWsdlTNS() {
		return wsdlTNS;
	}

	public void setWsdlTNS(String wsdlTNS) {
		this.wsdlTNS = wsdlTNS;
	}

	public List<PropertyAliasesMessageType> getPropertyAliasesMessages() {
		return propertyAliasesMessages;
	}

	public void setPropertyAliasesMessages(List<PropertyAliasesMessageType> propertyAliasesMessages) {
		this.propertyAliasesMessages = propertyAliasesMessages;
	}

	public void addPropertyAliasesMessage(PropertyAliasesMessageType propertyAliasesMessage){
		this.propertyAliasesMessages.add(propertyAliasesMessage);
	}
	
	
}
