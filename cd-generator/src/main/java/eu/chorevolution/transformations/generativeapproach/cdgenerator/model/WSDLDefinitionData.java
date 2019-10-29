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

import javax.wsdl.Definition;

public class WSDLDefinitionData {
	
	private String participantName;
	private String prefix;
	private String wsdlFileName;
	private Definition wsdl;

	public WSDLDefinitionData() {
		super();
	}

	public WSDLDefinitionData(String participantName,String prefix,String wsdlFileName,Definition wsdl) {
		super();
		this.participantName = participantName;
		this.prefix = prefix;
		this.wsdlFileName = wsdlFileName;
		this.wsdl = wsdl;
	}
	
	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getWsdlFileName() {
		return wsdlFileName;
	}

	public void setWsdlFileName(String wsdlFileName) {
		this.wsdlFileName = wsdlFileName;
	}
		
	public Definition getWsdl() {
		return wsdl;
	}

	public void setWsdl(Definition wsdl) {
		this.wsdl = wsdl;
	}

	@Override
	public String toString() {
		return "WSDLDefinitionData [participantName=" + participantName + ", prefix=" + prefix 
				+ ", wsdlFileName="+ wsdlFileName +"]";
	}

	
	
}
