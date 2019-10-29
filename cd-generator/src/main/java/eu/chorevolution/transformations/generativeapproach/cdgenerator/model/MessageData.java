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

public class MessageData {
	
	private String name;
	private String partName;
	private String elementName;
	private String elementTypeName;
	
	public MessageData() {
		super();
	}

	public MessageData(String name, String partName, String elementName,String elementTypeName) {
		super();
		this.name = name;
		this.partName = partName;
		this.setElementName(elementName);
		this.setElementTypeName(elementTypeName);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getElementTypeName() {
		return elementTypeName;
	}

	public void setElementTypeName(String elementTypeName) {
		this.elementTypeName = elementTypeName;
	}

	@Override
	public String toString() {
		return "MessageData [name=" + name + ", partName=" + partName + ", elementName=" + elementName
				+ ", elementTypeName=" + elementTypeName + "]";
	}


		
}
