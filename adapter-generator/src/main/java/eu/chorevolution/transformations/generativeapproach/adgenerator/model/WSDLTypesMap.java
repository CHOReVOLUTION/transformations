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
package eu.chorevolution.transformations.generativeapproach.adgenerator.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.modelingnotations.adapter.ChoreographyComplexItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyDataItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyEnumerationItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyMessage;
import eu.chorevolution.modelingnotations.adapter.ChoreographySimpleItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyTask;
import eu.chorevolution.modelingnotations.adapter.OccurencesType;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.AdapterModelsUtility;

public class WSDLTypesMap {

	private Map<String, WSDLType> wsdlItems;

	public WSDLTypesMap(AdapterModel adapterModel) {
		wsdlItems = new HashMap<>();

		ChoreographyTask task = adapterModel.getHasChoreographyTasks().get(0);
		for (ChoreographyMessage message : task.getHasChoreographyMessages()) {
			recursiveBuilder(message.getHasChoreographyDataItem());
		}
	}

	private WSDLType recursiveBuilder(ChoreographyDataItem choreographyDataItem) {
		WSDLType wsdlType = new WSDLType();
		wsdlType.setName(choreographyDataItem.getName());
		wsdlType.setMaxOccurs(getOccurrencesWSDLValue(choreographyDataItem.getMaxOccurs()));
		wsdlType.setMinOccurs(getOccurrencesWSDLValue(choreographyDataItem.getMinOccurs()));

		if (choreographyDataItem instanceof ChoreographyComplexItem) {
			ChoreographyComplexItem complexItem = (ChoreographyComplexItem) choreographyDataItem;
			EList<ChoreographyDataItem> innerItems = complexItem.getHasChoreographyDataItems();
			
			if (innerItems != null) {
				for (ChoreographyDataItem item : innerItems) {
					wsdlType.getInnerItems().add(recursiveBuilder(item));
				}
			}

			wsdlType.setType(complexItem.getTypeName());
			wsdlType.setComplex(true);
			wsdlType.setEnumeration(false);
			this.wsdlItems.put(wsdlType.getType(), wsdlType);

		} else {
			ChoreographySimpleItem simpleItem = (ChoreographySimpleItem) choreographyDataItem;
			EList<ChoreographyEnumerationItem> enumerationList = simpleItem.getHasChoreographyEnumerationItems();

			if (enumerationList != null && !enumerationList.isEmpty()) {
				wsdlType.setEnumeration(true);
				wsdlType.getEnumerationValues().addAll(AdapterModelsUtility.getSimpleItemEnumerationValues(simpleItem));
				this.wsdlItems.put(wsdlType.getName(), wsdlType);
			} else {
				wsdlType.setEnumeration(false);
			}

			wsdlType.setComplex(false);
			wsdlType.setType(simpleItem.getType().getName());
		}

		return wsdlType;
	}

	private String getOccurrencesWSDLValue(OccurencesType occurencesType) {
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
	
}
