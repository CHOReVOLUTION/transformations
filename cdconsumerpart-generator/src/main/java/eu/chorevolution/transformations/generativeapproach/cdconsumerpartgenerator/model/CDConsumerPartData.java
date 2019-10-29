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

@SuppressWarnings("serial")
public class CDConsumerPartData implements java.io.Serializable {

	private static final String WSDL_EXTENSION = ".wsdl";
	
	private String name;
	private String groupId;
	private String artifactId;
	private String wsdlname;
	private String packagename;

	public CDConsumerPartData() {
	}

	public CDConsumerPartData(String name, String groupId, String artifactId, String wsdlname, String packagename) {
		this.name = name;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.wsdlname = wsdlname + WSDL_EXTENSION;
		this.packagename = packagename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getWsdlname() {
		return wsdlname;
	}

	public void setWsdlname(String wsdlname) {
		if (wsdlname.endsWith(WSDL_EXTENSION)) {
			this.wsdlname = wsdlname;
		} else {
			this.wsdlname = wsdlname + WSDL_EXTENSION;	
		}
		
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

}
