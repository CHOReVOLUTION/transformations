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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class NamespaceResolver implements NamespaceContext {
	
	HashMap<String, String> map = new HashMap();

	public void addBinding(String prefix, String uri) {
		this.map.put(prefix, uri);
	}

	public void addBindings(Map _map) {
		this.map.putAll(_map);
	}

	public String getNamespaceURI(String prefix) {
		if (prefix.equals("")) {
			return "";
		}
		if (prefix.equals("xml")) {
			return "http://www.w3.org/XML/1998/namespace";
		}
		if (prefix.equals("xmlns")) {
			return "http://www.w3.org/2000/xmlns/";
		}
		
		if (this.map.containsKey(prefix)) {
			return this.map.get(prefix);
		}
		return "";
	}

	public boolean hasBindings() {
		return !this.map.isEmpty();
	}

	public String getPrefix(String namespaceURI) {
		return null;
	}

	public Iterator getPrefixes(String namespaceURI) {
		return null;
	}

	public String getNamespaceAt(int rowIndex) {
		return (String) ((Map.Entry) this.map.entrySet().toArray()[rowIndex]).getValue();
	}

	public String getPrefixAt(int rowIndex) {
		return (String) ((Map.Entry) this.map.entrySet().toArray()[rowIndex]).getKey();
	}

	public int size() {
		return this.map.size();
	}
}
