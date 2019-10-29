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

public class PropertyAliasesData {
	
	private String propertyName;
	private String propertiesTNS;
	private List<PropertyAliasesDataItem> propertyAliasesData;
	
	public PropertyAliasesData() {
		super();
	}

	public PropertyAliasesData(String propertyName, String propertiesTNS) {
		super();
		this.propertyName = propertyName;
		this.propertiesTNS = propertiesTNS;
		this.propertyAliasesData = new ArrayList<>();
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertiesTNS() {
		return propertiesTNS;
	}

	public void setPropertiesTNS(String propertiesTNS) {
		this.propertiesTNS = propertiesTNS;
	}

	public List<PropertyAliasesDataItem> getPropertyAliasesData() {
		return propertyAliasesData;
	}

	public void setPropertyAliasesData(List<PropertyAliasesDataItem> propertyAliasesData) {
		this.propertyAliasesData = propertyAliasesData;
	}

	public void addPropertyAliasesDataItem(PropertyAliasesDataItem propertyAliasesDataItem){
		this.propertyAliasesData.add(propertyAliasesDataItem);
	}
}
