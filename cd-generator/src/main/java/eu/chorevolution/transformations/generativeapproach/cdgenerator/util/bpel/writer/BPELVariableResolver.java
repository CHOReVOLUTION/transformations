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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.emf.ecore.EObject;


/**
 * Base implementation of VariableResolver. This resolves all variables
 * as defined in the BPEL specifications.
 */
public class BPELVariableResolver implements VariableResolver {
	
	/**
	 * @see org.eclipse.bpel.model.resource.VariableResolver#getVariable(org.eclipse.emf.ecore.EObject,
	 *  java.lang.String)
	 */
	public Variable getVariable(EObject eObject, String variableName) {
		EObject container = eObject.eContainer();
		while (container != null) {
			if (container instanceof OnEvent) {
				Variable variable = ((OnEvent)container).getVariable();
				if (variable != null && variable.getName().equals(variableName)) {
					return variable;
				}
			} else if (container instanceof Catch) {
				Variable variable = ((Catch)container).getFaultVariable();
				if (variable != null && variable.getName().equals(variableName)) {
					return variable;
				}
			} else if (container instanceof ForEach) {
				Variable variable = ((ForEach)container).getCounterName();
				if (variable != null && variable.getName().equals(variableName)) {
					return variable;
				}
			} else {
				Variables variables = null;
				if (container instanceof Process) 
					variables = ((Process)container).getVariables();				
				else if (container instanceof Scope) 
					variables = ((Scope)container).getVariables();
				
				if (variables != null) {
					
					List<Object> list = new ArrayList<Object>();
					
					// check all BPEL variables if anyone has the correct variable name					
					list.addAll(variables.getChildren());
					list.addAll(variables.getExtensibilityElements());
					
					for (Object n : list) {
						if (n instanceof Variable) {						
							Variable variable = (Variable) n;
							String name = variable.getName();
							if (name != null && name.equals(variableName)) {
								return variable;
							}
						}
					}
				}
			}
			container = container.eContainer();	
		}	
		
		return null;	
	}
}
