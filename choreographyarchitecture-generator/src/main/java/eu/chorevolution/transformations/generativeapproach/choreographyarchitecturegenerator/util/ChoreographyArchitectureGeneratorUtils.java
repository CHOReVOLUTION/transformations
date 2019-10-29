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
package eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyActivity;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubChoreography;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import eu.chorevolution.modelingnotations.chorarch.ChorArchModel;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.ChoreographyArchitectureGeneratorException;

/**
 * This class provides static utility methods for manipulate a CLTS model
 */
public class ChoreographyArchitectureGeneratorUtils {
    private static final String TEMPFILE_SUFFIX = "choreographyarchitecturegenerator";
    private static final String BPMN2_FILE_EXTENSION = ".bpmn2";

    private ChoreographyArchitectureGeneratorUtils() {
        // Do not instantiate
    }

    public static DocumentRoot getBpmn2DocumentRoot(final byte[] bpmnContent)
            throws ChoreographyArchitectureGeneratorException {

        File bpmnFile;
        try {
            bpmnFile = File.createTempFile(TEMPFILE_SUFFIX, BPMN2_FILE_EXTENSION);
            IOUtils.write(bpmnContent, FileUtils.openOutputStream(bpmnFile));
        } catch (IOException e1) {
            throw new ChoreographyArchitectureGeneratorException("Internal Error while creating the BPMN2 Model");
        }

        URI bpmnURI = URI.createURI(bpmnFile.toURI().toString());

        // register the BPMN2ResourceFactory in Factory registry
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        reg.getExtensionToFactoryMap().put("bpmn", new Bpmn2ResourceFactoryImpl());
        reg.getExtensionToFactoryMap().put("bpmn2", new Bpmn2ResourceFactoryImpl());

        // load the resource and resolve
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.createResource(bpmnURI);

        try {
            // load the resource
            resource.load(null);
        } catch (IOException e) {
            throw new ChoreographyArchitectureGeneratorException("Error to load the BPMN2 Choreography Model");
        } finally {
            FileDeleteStrategy.FORCE.deleteQuietly(bpmnFile);
        }

        if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof DocumentRoot) {
            return (DocumentRoot) resource.getContents().get(0);
        }

        throw new ChoreographyArchitectureGeneratorException("BPMN2 model is loaded but not contain a BPMN2 DocumentRoot");

    }

    public static byte[] getChoreoArchContent(final ChorArchModel chorarchModel)
            throws ChoreographyArchitectureGeneratorException {

        File choreoArchFile;
        try {
            choreoArchFile = File.createTempFile(TEMPFILE_SUFFIX, BPMN2_FILE_EXTENSION);
        } catch (IOException e1) {
            throw new ChoreographyArchitectureGeneratorException(
                    "Internal Error while creating the Choreography Architecture Model");
        }

        // create resource from the Model
        URI fileUriTempModelNormalized = URI.createFileURI(choreoArchFile.getAbsolutePath());
        Resource resourceModelNormalized = new XMIResourceFactoryImpl().createResource(fileUriTempModelNormalized);
        // add model in model resourceModel
        resourceModelNormalized.getContents().add(chorarchModel);

        byte[] choreoArchContent;

        try {
            resourceModelNormalized.save(Collections.EMPTY_MAP);
            choreoArchContent = FileUtils.readFileToByteArray(choreoArchFile);

        } catch (IOException e) {
            throw new ChoreographyArchitectureGeneratorException(
                    "Internal Error while reading the Choreography Architecture Model");
        } finally {
        	FileDeleteStrategy.FORCE.deleteQuietly(choreoArchFile);
        }

        return choreoArchContent;

    }

    public static Participant getParticipant(final List<Choreography> choreographies,
            final String participantName) throws ChoreographyArchitectureGeneratorException {
        for (Choreography choreography : choreographies) {
            for (Participant participant : choreography.getParticipants()) {
                if (participant.getName().equals(participantName)) {
                    return participant;
                }
            }
        }

        throw new ChoreographyArchitectureGeneratorException(participantName + " BPMN2 Participant not found");
    }
    
    public static Participant getInitiatingParticipant(ChoreographyActivity choreographyActivity)
            throws ChoreographyArchitectureGeneratorException {
        if (choreographyActivity.getInitiatingParticipantRef() != null)
            return choreographyActivity.getInitiatingParticipantRef();
        throw new ChoreographyArchitectureGeneratorException(
                "None Initiating participant founded in the choreography activity: " + choreographyActivity.getName());

    }

    public static Participant getReceivingParticipant(ChoreographyActivity choreographyActivity)
            throws ChoreographyArchitectureGeneratorException {
        for (Participant participant : choreographyActivity.getParticipantRefs()) {
            if (!participant.equals(choreographyActivity.getInitiatingParticipantRef())) {
                return participant;
            }
        }
        throw new ChoreographyArchitectureGeneratorException(
                "None target participant founded in the choreography activity: " + choreographyActivity.getName());
    }
    
    
    
    public static StartEvent getStartEvent(final Choreography choreography)
            throws ChoreographyArchitectureGeneratorException {
        // we assume that the choreography has only one start event
        for (FlowElement flowElement : choreography.getFlowElements()) {
            if (flowElement instanceof StartEvent) {
                return (StartEvent) flowElement;
            }
        }
        throw new ChoreographyArchitectureGeneratorException(
                "None start event founded in the BPMN2 Choreography: " + choreography.getName());
    }

    public static EndEvent getEndEvent(final Choreography choreography)
            throws ChoreographyArchitectureGeneratorException {
        // we assume that the choreography has only one end event
        for (FlowElement flowElement : choreography.getFlowElements()) {
            if (flowElement instanceof EndEvent) {
                return (EndEvent) flowElement;
            }
        }
        throw new ChoreographyArchitectureGeneratorException(
                "None end event founded in the BPMN2 Choreography: " + choreography.getName());
    }

    /**
     * This method try to detect in the {@link Choreography} the first
     * acceptable element. This element will be used during the transformation
     * when the transformator needed to collapse the sub-choreography
     *
     * @param choreography
     *            the {@link Choreography} that refers the
     *            {@link SubChoreography}
     * @return {@link FlowElement} the first acceptable element used when the
     *         transformator collapse the sub-choreography
     * @throws Bpmn2ChoreographyProjectorException
     *             a {@link Bpmn2ChoreographyProjectorException} is thrown if
     *             none first acceptable element is founded in the
     *             sub-choreography
     */
    public static FlowElement getFirstAcceptableFlowElement(final Choreography choreography)
            throws ChoreographyArchitectureGeneratorException {
        // we assume that the start event of the choreography has only one
        // outgoing transition
        try {
            return getStartEvent(choreography).getOutgoing().get(0).getTargetRef();
        } catch (ChoreographyArchitectureGeneratorException e) {
            throw new ChoreographyArchitectureGeneratorException(
                    "None first acceptable element is founded in the BPMN2 Choreography: "
                            + choreography.getName());
        }
    }

    /**
     * This method try to detect in the {@link Choreography} the last acceptable
     * element. This element will be used during the transformation when the
     * transformator needed to collapse the sub-choreography
     *
     * @param choreography
     *            the {@link Choreography} that refers the
     *            {@link SubChoreography}
     * @return {@link FlowElement} the last acceptable element used when the
     *         transformator collapse the sub-choreography
     * @throws Bpmn2ChoreographyProjectorException
     *             a {@link Bpmn2ChoreographyProjectorException} is thrown if
     *             none last acceptable element is founded in the
     *             sub-choreography
     */
    public static FlowElement getLastAcceptableFlowElement(final Choreography choreography)
            throws ChoreographyArchitectureGeneratorException {
        // we assume that the end event of the choreography has only one
        // incoming transition
        try {
            return getEndEvent(choreography).getIncoming().get(0).getSourceRef();
        } catch (ChoreographyArchitectureGeneratorException e) {
            throw new ChoreographyArchitectureGeneratorException(
                    "None last accettable element is founded in the BPMN2 Choreography: "
                            + choreography.getName());
        }
    }

}
