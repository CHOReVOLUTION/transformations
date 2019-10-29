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

import org.w3c.dom.Document;

public class ArtifactData {
	
	private String fileName;
	private String namespace;
	private String prefix;
	private Document artifact;
	
	public ArtifactData() {
		super();
	}

	public ArtifactData(String fileName, String namespace, String prefix, Document artifact) {
		super();
		this.fileName = fileName;
		this.namespace = namespace;
		this.prefix = prefix;
		this.artifact = artifact;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Document getArtifact() {
		return artifact;
	}

	public void setArtifact(Document artifact) {
		this.artifact = artifact;
	}
	
}
