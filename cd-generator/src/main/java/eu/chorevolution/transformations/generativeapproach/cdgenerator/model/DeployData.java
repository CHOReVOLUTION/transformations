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

public class DeployData {

	private String partnerLinkName;
	private String serviceNamePrefix;
	private String serviceName;
	private String portName;
	
	public DeployData() {
		super();
	}

	public DeployData(String partnerLinkName, String serviceNamePrefix, String serviceName,
			String portName) {
		super();
		this.partnerLinkName = partnerLinkName;
		this.serviceNamePrefix = serviceNamePrefix;
		this.serviceName = serviceName;
		this.portName = portName;
	}

	public String getPartnerLinkName() {
		return partnerLinkName;
	}

	public void setPartnerLinkName(String partnerLinkName) {
		this.partnerLinkName = partnerLinkName;
	}

	public String getServiceNamePrefix() {
		return serviceNamePrefix;
	}

	public void setServiceNamePrefix(String serviceNamePrefix) {
		this.serviceNamePrefix = serviceNamePrefix;
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
	
	
	
}
