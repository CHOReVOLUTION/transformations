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
import eu.chorevolution.modelingnotations.adapter.ChoreographyComplexItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyDataItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyMessage;
import eu.chorevolution.modelingnotations.adapter.ChoreographySimpleItem;
import eu.chorevolution.modelingnotations.adapter.DataItem;
import eu.chorevolution.modelingnotations.adapter.DataItemRelation;
import eu.chorevolution.modelingnotations.adapter.EnumerationItemRelation;
import eu.chorevolution.modelingnotations.adapter.MessageRelation;
import eu.chorevolution.modelingnotations.adapter.OccurencesType;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.AdapterModelsUtility;

public class ResponseTranslationData {

	private String adElementName;
	private List<DataItemInfo> bcElements;

	private String elementPath;

	private String transformationRule;

	private List<ResponseTranslationData> innerElements;
	private Map<String, String> enumerationMapping; // key is AD, value is BC

	private boolean hasMultipleOccurrences;

	public ResponseTranslationData() {
	}

	public ResponseTranslationData(AdapterModel adapterModel) {
		// This constructor builds the translation data tree from the root
		bcElements = new ArrayList<>();
		innerElements = new ArrayList<>();
		enumerationMapping = new HashMap<>();
		this.adElementName = "rootChoreographyDataItem";
		this.bcElements.add(new DataItemInfo("rootDataItem", "", OccurencesType.ONE));
		ChoreographyMessage returnMessage = AdapterModelsUtility.getChoreographyReturnMessage(adapterModel); // Assuming
																												// a
																												// single
																												// operation
																												// with
																												// a
																												// single
																												// input
																												// message
		MessageRelation inputMessageRelation = AdapterModelsUtility.findChoreographyMessageRelation(returnMessage,
				adapterModel);
		List<ChoreographyDataItem> choreographyDataItems = AdapterModelsUtility
				.getChoreographyMessageInnerDataItems(returnMessage);
		for (ChoreographyDataItem item : choreographyDataItems) {
			this.innerElements.add(new ResponseTranslationData(inputMessageRelation, item, ""));
		}
	}

	public ResponseTranslationData(MessageRelation messageRelation, ChoreographyDataItem choreographyDataItem,
			String elementPath) {
		// This constructor builds the translation data tree from any inner node
		this.bcElements = new ArrayList<>();
		this.innerElements = new ArrayList<>();
		this.enumerationMapping = new HashMap<>();
		if (choreographyDataItem != null) {
			EList<DataItem> dataItems = AdapterModelsUtility.getRelatedDataItems(choreographyDataItem, messageRelation);
			for (DataItem currentItem : dataItems) {
				this.bcElements.add(new DataItemInfo(currentItem.getName(),
						AdapterModelsUtility.getRootPath(messageRelation.getMessage(), currentItem),
						currentItem.getMaxOccurs()));
			}
			this.adElementName = choreographyDataItem.getName();
			this.elementPath = elementPath.isEmpty() ? choreographyDataItem.getName()
					: elementPath + "." + choreographyDataItem.getName();
			this.transformationRule = AdapterModelsUtility.getTransformationRule(choreographyDataItem, messageRelation);
			this.hasMultipleOccurrences = choreographyDataItem.getMaxOccurs().equals(OccurencesType.UNBOUNDED);
			if (choreographyDataItem instanceof ChoreographyComplexItem) {
				ChoreographyComplexItem item = (ChoreographyComplexItem) choreographyDataItem;
				EList<ChoreographyDataItem> innerItems = item.getHasChoreographyDataItems();
				if (innerItems != null) {
					for (ChoreographyDataItem innerItem : innerItems) {
						this.innerElements
								.add(new ResponseTranslationData(messageRelation, innerItem, this.elementPath));
					}
				}
			} else {
				ChoreographySimpleItem item = (ChoreographySimpleItem) choreographyDataItem;
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

	public String getAdElementName() {
		return adElementName;
	}

	public void setAdElementName(String adElementName) {
		this.adElementName = adElementName;
	}

	public List<DataItemInfo> getBcElements() {
		return this.bcElements;
	}

	public String getElementPath() {
		return elementPath;
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

	public List<ResponseTranslationData> getInnerElements() {
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
