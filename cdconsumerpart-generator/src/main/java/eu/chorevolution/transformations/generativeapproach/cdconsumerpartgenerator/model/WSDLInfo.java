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

import javax.wsdl.Definition;

@SuppressWarnings("serial")
public class WSDLInfo implements java.io.Serializable {

	private String portName;
	private String serviceName;
	private String targetNS;
	private Definition definition;

	public WSDLInfo() {
	}

	public WSDLInfo(String portName, String serviceName, String targetNS) {
		this.portName = portName;
		this.serviceName = serviceName;
		this.targetNS = targetNS;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getTargetNS() {
		return targetNS;
	}

	public void setTargetNS(String targetNS) {
		this.targetNS = targetNS;
	}

	public Definition getDefinition() {
		return definition;
	}

	public void setDefinition(Definition definition) {
		this.definition = definition;
	}

}
