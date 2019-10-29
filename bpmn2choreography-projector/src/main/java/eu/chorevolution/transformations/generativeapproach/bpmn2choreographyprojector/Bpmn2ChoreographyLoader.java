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
package eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * This class load a BPMN model
 */
public class Bpmn2ChoreographyLoader {
	
    private DocumentRoot documentRoot;

    /**
     * this property contains all the choreography containd in the BPMN file
     */
    private List<Choreography> choreographies;

    public Bpmn2ChoreographyLoader(final DocumentRoot bpmnContent)
            throws Bpmn2ChoreographyProjectorException {
        this.documentRoot = bpmnContent; // Bpmn2ChoreographyProjectorUtils.getBpmn2DocumentRoot(bpmnContent);
        
        this.choreographies = new ArrayList<Choreography>();

        // get all choreography in the BPMN2 Model and set the Default
        // choreography
        for (EObject definition : documentRoot.getDefinitions().eContents()) {
            if (definition instanceof Choreography) {
                Choreography choreography = (Choreography) definition;
                choreographies.add(choreography);

            }
        }

        // throw Bpmn2ChoreographyProjectorException
        if (choreographies.isEmpty()) {
            throw new Bpmn2ChoreographyProjectorException("None choreography founded in the BPMN model");
        }

        
        // remove the graphical editor inside the document root
        deleteBPMNDiagram(documentRoot);
    }

    public List<Choreography> getChoreographies() {
        return choreographies;
    }


    public DocumentRoot getDocumentRoot() {
        return documentRoot;
    }
    
    private void deleteBPMNDiagram(DocumentRoot documentRoot) {
        for (BPMNDiagram bpmnDiagram : new ArrayList<BPMNDiagram>(
        		documentRoot.getDefinitions().getDiagrams())) {
            EcoreUtil.delete(bpmnDiagram);
        }
    }

}
