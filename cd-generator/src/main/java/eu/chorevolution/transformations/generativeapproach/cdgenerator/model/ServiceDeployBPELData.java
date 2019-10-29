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

public class ServiceDeployBPELData {
	
	private String prefix;
	private String namespace;
	private String serviceName;
	private String portName;
	
	public ServiceDeployBPELData(String prefix,String namespace,String serviceName, String portName) {
		super();
		this.prefix = prefix;
		this.namespace = namespace;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	@Override
	public String toString() {
		return "ServiceDeployBPELData [prefix=" + prefix + ", namespace=" + namespace + ", serviceName=" 
				+ serviceName+ ", portName=" + portName + "]";
	}
	
	
	
}
