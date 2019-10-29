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

import java.util.ArrayList;
import java.util.List;

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl.Utility;

@SuppressWarnings("serial")
public class GeneratingJavaData implements java.io.Serializable {

	private String basePackageName;
	private String portName;
	private String cdName;
	private List<OperationData> initiatingSendOperations = new ArrayList<>();
	private List<OperationData> initiatingReceiveOperations = new ArrayList<>();
	private List<OperationData> receivingOperations = new ArrayList<>();

	public String getBasePackageName() {
		return basePackageName;
	}

	public void setBasePackageName(String basePackageName) {
		this.basePackageName = basePackageName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getCdName() {
		return cdName;
	}

	public void setCdName(String cdName) {
		this.cdName = cdName;
	}

	public String getWebServicePackageName() {
		return basePackageName + ".webservices";

	}

	public String getBusinessPackageName() {
		return basePackageName + ".business";
	}
	
	public String getModelPackageName() {
		return basePackageName + ".model";
	}

	public String getJavaName() {
		return portName + "Impl";
	}

	public String getSimpleName() {
		return Utility.deleteSpecialChar(cdName);
	}

	public List<OperationData> getInitiatingSendOperations() {
		return initiatingSendOperations;
	}

	public void setInitiatingSendOperations(List<OperationData> initiatingSendOperations) {
		this.initiatingSendOperations = initiatingSendOperations;
	}

	public void addInitiatingSendOperation(OperationData operationData) {
		initiatingSendOperations.add(operationData);
	}

	public List<OperationData> getReceivingOperations() {
		return receivingOperations;
	}

	public void setReceivingOperations(List<OperationData> receivingOperations) {
		this.receivingOperations = receivingOperations;
	}

	public void addReceivingOperation(OperationData operationData) {
		receivingOperations.add(operationData);
	}

	public List<OperationData> getInitiatingReceiveOperations() {
		return initiatingReceiveOperations;
	}
	
	public void addInitiatingReceiveOperation(OperationData operationData) {
		initiatingReceiveOperations.add(operationData);
	}

	public void setInitiatingReceiveOperations(List<OperationData> initiatingReceiveOperations) {
		this.initiatingReceiveOperations = initiatingReceiveOperations;
	}
	
}
