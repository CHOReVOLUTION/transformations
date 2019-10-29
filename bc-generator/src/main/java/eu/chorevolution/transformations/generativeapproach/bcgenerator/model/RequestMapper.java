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
package eu.chorevolution.transformations.generativeapproach.bcgenerator.model;

import java.util.HashMap;
import java.util.Map;

public class RequestMapper {

	private String root;
	private String requestWrapperTypeName;
	private String requestWrapperParameterName;
	private String requestTypeName;

	private Map<String, RequestElementInfo> requestElements; //Name-type

	public RequestMapper(){
		this.requestElements = new HashMap<>();
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getRequestWrapperTypeName() {
		return requestWrapperTypeName;
	}

	public void setRequestWrapperTypeName(String requestWrapperTypeName) {
		this.requestWrapperTypeName = requestWrapperTypeName;
	}

	public String getRequestWrapperParameterName() {
		return requestWrapperParameterName;
	}

	public void setRequestWrapperParameterName(String requestWrapperParameterName) {
		this.requestWrapperParameterName = requestWrapperParameterName;
	}

	public String getRequestTypeName() {
		return requestTypeName;
	}

	public void setRequestTypeName(String requestTypeName) {
		this.requestTypeName = requestTypeName;
	}

	public Map<String, RequestElementInfo> getRequestElements() {
		return requestElements;
	}
}
