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
package eu.chorevolution.transformations.generativeapproach.gidlgenerator;

public class GIDLGeneratorRequest implements java.io.Serializable {
	private static final long serialVersionUID = -2266130452223752463L;
	private byte[] wsdlContent;
	private String name;

	public GIDLGeneratorRequest() {
		super();
	}

	public GIDLGeneratorRequest(byte[] wsdlContent, String name) {
		super();
		this.wsdlContent = wsdlContent;
		this.setName(name);
	}

	public byte[] getWsdlContent() {
		return wsdlContent;
	}

	public void setWsdlContent(byte[] wsdlContent) {
		this.wsdlContent = wsdlContent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
