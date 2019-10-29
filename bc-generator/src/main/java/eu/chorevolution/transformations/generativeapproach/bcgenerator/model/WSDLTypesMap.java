/*
* Copyright 2017 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.bcgenerator.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import eu.chorevolution.modelingnotations.gidl.ComplexType;
import eu.chorevolution.modelingnotations.gidl.Data;
import eu.chorevolution.modelingnotations.gidl.DataType;
import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.modelingnotations.gidl.OccurrencesTypes;
import eu.chorevolution.modelingnotations.gidl.Operation;
import eu.chorevolution.modelingnotations.gidl.SimpleType;

public class WSDLTypesMap {

	private Map<String, WSDLType> wsdlItems;

	private WSDLType requestType;
	private WSDLType responseType;

	public WSDLTypesMap(GIDLModel gidlModel) {
		wsdlItems = new HashMap<>();

		Operation operation = gidlModel.getHasInterfaces().get(0).getHasOperations().get(0);

		Data inputData = operation.getInputData().get(0);
		Data outputData = operation.getOutputData().get(0);

		DataType requestBaseDataType = inputData.getHasDataType().get(0);
		DataType responseBaseDataType = outputData.getHasDataType().get(0);

		// Workaround for adding a fake root element
		this.requestType = new WSDLType();
		requestType.setComplex(true);
		requestType.setEnumeration(false);
		requestType.setMaxOccurs("one");
		requestType.setMinOccurs("one");
		requestType.setName(operation.getName());
		requestType.setType(operation.getName());
		requestType.getInnerItems().add(recursiveBuilder(requestBaseDataType));
		wsdlItems.put(requestType.getType(), requestType);

		this.responseType = new WSDLType();
		responseType.setComplex(true);
		responseType.setEnumeration(false);
		responseType.setMaxOccurs("one");
		responseType.setMinOccurs("one");
		responseType.setName(operation.getName() + "Response");
		responseType.setType(operation.getName() + "Response");
		responseType.getInnerItems().add(recursiveBuilder(responseBaseDataType));
		wsdlItems.put(responseType.getType(), responseType);

//		this.requestType = recursiveBuilder(requestBaseDataType);
//		this.responseType = recursiveBuilder(responseBaseDataType);

	}

	private WSDLType recursiveBuilder(DataType dataType) {
		WSDLType wsdlType = new WSDLType();
		wsdlType.setName(dataType.getName());
		wsdlType.setMaxOccurs(getOccurrencesWSDLValue(dataType.getMaxOccurs()));
		wsdlType.setMinOccurs(getOccurrencesWSDLValue(dataType.getMinOccurs()));

		if (dataType instanceof ComplexType) {
			ComplexType complexType = (ComplexType) dataType;
			EList<DataType> innerItems = complexType.getHasDataType();
			
			if (innerItems != null) {
				for (DataType item : innerItems) {
					wsdlType.getInnerItems().add(recursiveBuilder(item));
				}
			}

			wsdlType.setType(dataType.getName());
			wsdlType.setComplex(true);
			wsdlType.setEnumeration(false);
			this.wsdlItems.put(wsdlType.getType(), wsdlType);

		} else {
			SimpleType simpleType = (SimpleType) dataType;

			wsdlType.setEnumeration(false);
			wsdlType.setComplex(false);
			wsdlType.setType(simpleType.getType().getName());
		}

		return wsdlType;
	}

	private String getOccurrencesWSDLValue(OccurrencesTypes occurencesType) {
		if (occurencesType==null) {
			return "1";
		}
		switch (occurencesType) {
			case ONE:
				return "1";
			case ZERO:
				return "0";
			case UNBOUNDED:
				return "unbounded";
		}
		return "";
	}


	public Map<String, WSDLType> getWsdlItems() {
		return wsdlItems;
	}

	public WSDLType getRequestType() {
		return requestType;
	}

	public WSDLType getResponseType() {
		return responseType;
	}
	
}
