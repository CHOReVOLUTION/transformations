/*
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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer;

import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.xml.sax.SAXParseException;

public class SAXParseDiagnostic implements Diagnostic
{
	protected SAXParseException exception;
	protected int severity;
	
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int FATAL_ERROR = 3;
	
	SAXParseDiagnostic(SAXParseException exception, int severity)
	{
		this.exception = exception;
		this.severity = severity;
	}

	public int getColumn() {
		return exception.getColumnNumber();
	}

	public int getLine() {
		return exception.getLineNumber();
	}

	public String getLocation() {
		return exception.getPublicId();
	}

	public String getMessage() {
		return exception.getLocalizedMessage();
	}
	
	public int getSeverity() {
		return severity;
	}
}