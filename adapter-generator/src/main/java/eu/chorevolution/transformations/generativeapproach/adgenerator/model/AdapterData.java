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

@SuppressWarnings("serial")
public class AdapterData implements java.io.Serializable {

	private static final String WSDL_EXTENSION = ".wsdl";
	
	private String name;
	private String roleName;
	private String groupId;
	private String artifactId;
	private String wsdlname;
	private String packagename;
	private String servicename;
	private String bcTargetNamespace;
	private String adTargetNamespace;
	private String serviceLocationURI;

	public AdapterData() {
	}

	public AdapterData(String name, String roleName, String groupId, String artifactId, String wsdlname, String packagename, String servicename, String bcTargetNamespace, String adTargetNamespace, String serviceLocationURI) {
		this.name = name;
		this.roleName = roleName;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.wsdlname = wsdlname + WSDL_EXTENSION;
		this.packagename = packagename;
		this.servicename = servicename;
		this.bcTargetNamespace = bcTargetNamespace;
		this.adTargetNamespace = adTargetNamespace;
		this.serviceLocationURI = serviceLocationURI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
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

	public String getServicename() {
		return servicename;
	}

	public void setServicename(String servicename) {
		this.servicename = servicename;
	}

	public String getBcTargetNamespace() {
		return bcTargetNamespace;
	}

	public void setBcTargetNamespace(String bcTargetNamespace) {
		this.bcTargetNamespace = bcTargetNamespace;
	}

	public String getAdTargetNamespace() {
		return adTargetNamespace;
	}

	public void setAdTargetNamespace(String adTargetNamespace) {
		this.adTargetNamespace = adTargetNamespace;
	}

	public String getServiceLocationURI() {
		return serviceLocationURI;
	}

	public void setServiceLocationURI(String serviceLocationURI) {
		this.serviceLocationURI = serviceLocationURI;
	}

}
