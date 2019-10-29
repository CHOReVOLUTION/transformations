/*
  * Copyright 2015 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.gidlgenerator.util;

import eu.chorevolution.modelingnotations.gidl.OccurrencesTypes;

public class TreeNodeDataType {
	private String type;
	private String content;
	private String isMappedWith;
	private OccurrencesTypes maxOccurrences;
	private OccurrencesTypes minOccurrences;
	private String complexOrSimpleType;
	
	
	public TreeNodeDataType(String type, String content) {
		this.type = type;
		this.content = content;
		this.isMappedWith = null;
		this.maxOccurrences = OccurrencesTypes.ONE;//default is 1
		this.minOccurrences = OccurrencesTypes.ONE;
		this.complexOrSimpleType = "simpleType";
	}


	public String getIsMappedWith() {
		return isMappedWith;
	}


	public void setIsMappedWith(String isMappedWith) {
		this.isMappedWith = isMappedWith;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}



	public OccurrencesTypes getMaxOccurrences() {
		return maxOccurrences;
	}


	public void setMaxOccurrences(OccurrencesTypes maxOccurrences) {
		this.maxOccurrences = maxOccurrences;
	}


	public OccurrencesTypes getMinOccurrences() {
		return minOccurrences;
	}


	public void setMinOccurrences(OccurrencesTypes minOccurrences) {
		this.minOccurrences = minOccurrences;
	}


	public String getComplexOrSimpleType() {
		return complexOrSimpleType;
	}


	public void setComplexOrSimpleType(String complexOrSimpleType) {
		this.complexOrSimpleType = complexOrSimpleType;
	}
	
	
	
}
