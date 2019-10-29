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

import eu.chorevolution.modelingnotations.adapter.OccurencesType;

public class DataItemInfo {

	private String name;
	private String fromRootPath;
	private OccurencesType occurrences;

	public DataItemInfo(String name, String fromRootPath, OccurencesType occurrences) {
		this.name = name;
		this.fromRootPath = fromRootPath;
		this.occurrences = occurrences;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFromRootPath() {
		return fromRootPath;
	}

	public void setFromRootPath(String fromRootPath) {
		this.fromRootPath = fromRootPath;
	}

	public OccurencesType getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(OccurencesType occurrences) {
		this.occurrences = occurrences;
	}

}
