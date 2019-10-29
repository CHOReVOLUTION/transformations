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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.modelingnotations.adapter.ChoreographyDataItem;
import eu.chorevolution.modelingnotations.adapter.ComplexItem;
import eu.chorevolution.modelingnotations.adapter.DataItem;
import eu.chorevolution.modelingnotations.adapter.DataItemRelation;
import eu.chorevolution.modelingnotations.adapter.EnumerationItemRelation;
import eu.chorevolution.modelingnotations.adapter.Message;
import eu.chorevolution.modelingnotations.adapter.MessageRelation;
import eu.chorevolution.modelingnotations.adapter.OccurencesType;
import eu.chorevolution.modelingnotations.adapter.SimpleItem;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.AdapterModelsUtility;

public class RequestTranslationData {

	private List<DataItemInfo> adElements;
	private String bcElementName;

	private String elementPath;

	private String transformationRule;

	private List<RequestTranslationData> innerElements;
	private Map<String, String> enumerationMapping; // key is AD, value is BC

	private boolean hasMultipleOccurrences;

	public RequestTranslationData() {
	}

	public RequestTranslationData(AdapterModel adapterModel) {
		// This constructor builds the translation data tree from the root
		adElements = new ArrayList<>();
		innerElements = new ArrayList<>();
		enumerationMapping = new HashMap<>();
		this.adElements.add(new DataItemInfo("rootChoreographyDataItem", "", OccurencesType.ONE));
		this.bcElementName = "rootDataItem";
		Message inputMessage = AdapterModelsUtility.getInputMessage(adapterModel); // Assuming
																					// a
																					// single
																					// operation
																					// with
																					// a
																					// single
																					// input
																					// message
		MessageRelation inputMessageRelation = AdapterModelsUtility.findMessageRelation(inputMessage, adapterModel);
		List<DataItem> dataItems = AdapterModelsUtility.getMessageInnerDataItems(inputMessage);
		for (DataItem item : dataItems) {
			this.innerElements.add(new RequestTranslationData(inputMessageRelation, item, ""));
		}
	}

	public RequestTranslationData(MessageRelation messageRelation, DataItem dataItem, String elementPath) {
		// This constructor builds the translation data tree from any inner node
		this.adElements = new ArrayList<>();
		this.innerElements = new ArrayList<>();
		this.enumerationMapping = new HashMap<>();
		if (dataItem != null) {
			EList<ChoreographyDataItem> choreographyDataItems = AdapterModelsUtility
					.getRelatedChoreographyDataItems(dataItem, messageRelation);
			for (ChoreographyDataItem currentItem : choreographyDataItems) {
				this.adElements.add(new DataItemInfo(currentItem.getName(),
						AdapterModelsUtility.getRootPath(messageRelation.getChoreographyMessage(), currentItem),
						currentItem.getMaxOccurs()));
			}
			this.bcElementName = dataItem.getName();
			this.elementPath = elementPath.isEmpty() ? dataItem.getName() : elementPath + "." + dataItem.getName();
			this.transformationRule = AdapterModelsUtility.getTransformationRule(dataItem, messageRelation);
			this.hasMultipleOccurrences = dataItem.getMaxOccurs().equals(OccurencesType.UNBOUNDED);
			if (dataItem instanceof ComplexItem) {
				ComplexItem item = (ComplexItem) dataItem;
				EList<DataItem> innerItems = item.getHasDataItems();
				if (innerItems != null) {
					for (DataItem innerItem : innerItems) {
						this.innerElements
								.add(new RequestTranslationData(messageRelation, innerItem, this.elementPath));
					}
				}
			} else {
				SimpleItem item = (SimpleItem) dataItem;
				DataItemRelation dataItemRelation = AdapterModelsUtility.findDataItemRelation(messageRelation, item);
				if (dataItemRelation != null) {
					EList<EnumerationItemRelation> enumerationItemRelations = dataItemRelation
							.getHasEnumerationItemsRelations();
					if (enumerationItemRelations != null) {
						for (EnumerationItemRelation enumerationItemRelation : enumerationItemRelations) {
							String adValue = AdapterModelsUtility
									.getEnumerationItemValue(enumerationItemRelation.getChoreographyEnumerationItem());
							String bcValue = AdapterModelsUtility
									.getEnumerationItemValue(enumerationItemRelation.getEnumerationItem());
							this.enumerationMapping.put(adValue, bcValue);
						}
					}
				}
			}
		}
	}

	public List<DataItemInfo> getAdElements() {
		return adElements;
	}

	public String getBcElementName() {
		return bcElementName;
	}

	public void setBcElementName(String bcElementName) {
		this.bcElementName = bcElementName;
	}

	public String getElementPath() {
		return this.elementPath;
	}

	public void setElementPath(String elementPath) {
		this.elementPath = elementPath;
	}

	public String getTransformationRule() {
		return transformationRule;
	}

	public void setTransformationRule(String transformationRule) {
		this.transformationRule = transformationRule;
	}

	public List<RequestTranslationData> getInnerElements() {
		return innerElements;
	}

	public boolean hasInnerElements() {
		return !innerElements.isEmpty();
	}

	public boolean isEnumeration() {
		return !enumerationMapping.isEmpty();
	}

	public Map<String, String> getEnumerationMapping() {
		return enumerationMapping;
	}

	public boolean isHasMultipleOccurrences() {
		return hasMultipleOccurrences;
	}

	public void setHasMultipleOccurrences(boolean hasMultipleOccurrences) {
		this.hasMultipleOccurrences = hasMultipleOccurrences;
	}
}
