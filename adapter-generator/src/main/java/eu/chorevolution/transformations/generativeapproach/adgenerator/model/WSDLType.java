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
import java.util.List;

public class WSDLType {

	private String name;
	private String type;
	private String minOccurs;
	private String maxOccurs;

	private boolean complex;
	private boolean enumeration;

	private List<WSDLType> innerItems;

	private List<String> enumerationValues;

	public WSDLType() {
		this.innerItems = new ArrayList<>();
		this.enumerationValues = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMinOccurs() {
		return minOccurs;
	}

	public void setMinOccurs(String minOccurs) {
		this.minOccurs = minOccurs;
	}

	public String getMaxOccurs() {
		return maxOccurs;
	}

	public void setMaxOccurs(String maxOccurs) {
		this.maxOccurs = maxOccurs;
	}

	public boolean isComplex() {
		return complex;
	}

	public void setComplex(boolean complex) {
		this.complex = complex;
	}

	public boolean isEnumeration() {
		return enumeration;
	}

	public void setEnumeration(boolean enumeration) {
		this.enumeration = enumeration;
	}

	public List<WSDLType> getInnerItems() {
		return innerItems;
	}

	public void setInnerItems(List<WSDLType> innerItems) {
		this.innerItems = innerItems;
	}

	public List<String> getEnumerationValues() {
		return enumerationValues;
	}

	public void setEnumerationValues(List<String> enumerationValues) {
		this.enumerationValues = enumerationValues;
	}
	
}
