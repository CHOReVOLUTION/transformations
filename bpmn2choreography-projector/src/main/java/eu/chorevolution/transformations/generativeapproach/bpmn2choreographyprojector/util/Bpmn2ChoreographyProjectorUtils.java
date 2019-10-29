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
package eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyActivity;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

import eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.Bpmn2ChoreographyProjectorException;

/**
 * This class provides static utility methods for manipulate a BPMN model
 * 
 */
public final class Bpmn2ChoreographyProjectorUtils {
	private static final String TEMPFILE_SUFFIX = "bpmn2choreographyprojector";
	private static final String BPMN2_FILE_EXTENSION = ".bpmn2";

	private Bpmn2ChoreographyProjectorUtils() {
		// Do not instantiate
	}

	public static DocumentRoot getBpmn2DocumentRoot(byte[] bpmn2Content) throws Bpmn2ChoreographyProjectorException {

		File bpmn2TempFile;
		try {
			bpmn2TempFile = File.createTempFile(TEMPFILE_SUFFIX, BPMN2_FILE_EXTENSION);
			IOUtils.write(bpmn2Content, FileUtils.openOutputStream(bpmn2TempFile));
		} catch (IOException e) {
			throw new Bpmn2ChoreographyProjectorException("Internal Error while creating temporary file", e);
		}

		URI bpmnURI = URI.createURI(bpmn2TempFile.toURI().toString(), true);

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
			throw new Bpmn2ChoreographyProjectorException(
					"Internal Error while loading the resource " + bpmn2TempFile.getAbsolutePath(), e);
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(bpmn2TempFile);
		}

		if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof DocumentRoot) {
			return (DocumentRoot) resource.getContents().get(0);
		}

		throw new Bpmn2ChoreographyProjectorException(
				"BPMN2 model is loaded but not contain a BPMN2 DocumentRoot " + bpmn2TempFile.getAbsolutePath());
	}

	public static byte[] getByteArray(DocumentRoot bpmn2DocumentRoot) throws Bpmn2ChoreographyProjectorException {
		File bpmn2TempFile;
		try {
			bpmn2TempFile = File.createTempFile(TEMPFILE_SUFFIX, BPMN2_FILE_EXTENSION);
		} catch (IOException e) {
			throw new Bpmn2ChoreographyProjectorException("Internal Error while creating temporary file", e);
		}

		// create resource from the Model
		URI fileUriTempModelNormalized = URI.createURI(bpmn2TempFile.toURI().toString());
		Resource resourceModelNormalized = new Bpmn2ResourceFactoryImpl().createResource(fileUriTempModelNormalized);
		// add model in model resourceModel
		resourceModelNormalized.getContents().add(bpmn2DocumentRoot);
		
		Map<Object, Object> saveOptions =  new HashMap<Object, Object>();
		saveOptions.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.FALSE);

		try {
			resourceModelNormalized.save(saveOptions);
		} catch (IOException e) {
			throw new Bpmn2ChoreographyProjectorException(
					"Internal Error while saving BPMN2 " + bpmn2TempFile.getAbsolutePath(), e);
		}

		try {
			return FileUtils.readFileToByteArray(bpmn2TempFile);
		} catch (IOException e) {
			throw new Bpmn2ChoreographyProjectorException(
					"Internal Error while reading temporary file " + bpmn2TempFile.getAbsolutePath(), e);
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(bpmn2TempFile);
		}

	}

	public static StartEvent getStartEvent(final Choreography choreography) throws Bpmn2ChoreographyProjectorException {
		// we assume that the choreography has only one start event
		for (FlowElement flowElement : choreography.getFlowElements()) {
			if (flowElement instanceof StartEvent) {
				return (StartEvent) flowElement;
			}
		}
		throw new Bpmn2ChoreographyProjectorException(
				"None start event founded in the BPMN2 Choreography: " + choreography.getName());
	}

	public static EndEvent getEndEvent(final Choreography choreography) throws Bpmn2ChoreographyProjectorException {
		// we assume that the choreography has only one end event
		for (FlowElement flowElement : choreography.getFlowElements()) {
			if (flowElement instanceof EndEvent) {
				return (EndEvent) flowElement;
			}
		}
		throw new Bpmn2ChoreographyProjectorException(
				"None end event founded in the BPMN2 Choreography: " + choreography.getName());
	}

	public static Participant getParticipant(final List<Choreography> choreographies, final String participantName)
			throws Bpmn2ChoreographyProjectorException {
		for (Choreography choreography : choreographies) {
			for (Participant participant : choreography.getParticipants()) {
				if (participant.getName().equals(participantName)) {
					return participant;
				}
			}
		}

		throw new Bpmn2ChoreographyProjectorException(participantName + " BPMN2 Participant not found");
	}

	public static boolean hasParticipant(final ChoreographyActivity choreographyActivity,
			final Participant participant) {

		for (Participant participantRef : choreographyActivity.getParticipantRefs()) {
			if (participantRef.equals(participant)) {
				return true;
			}
		}

		return false;
	}

	// checking if the participant is used in the choreography
	public static boolean isParticipantUsed(final List<Choreography> choreographies, final Participant participant) {
		for (Choreography choreography : choreographies) {
			for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
				if (flowElement instanceof ChoreographyActivity
						&& hasParticipant((ChoreographyActivity) flowElement, participant)) {
					return true;
				}
			}
		}
		return false;
	}

	// checking if a Diverging Gateway is empty. A Diverging Gateway is empty if
	// his outgoing transition directly reach the same Converging Gateway. The
	// Converging Gateway has the same class of the Diverging Gateway. The set
	// of outgoing transition of the Diverging Gateway is equals to the set of
	// incoming transition of the Converging Gateway
	public static boolean isDirectlyConnetedToConvergingGateway(final Gateway divergingGateway)
			throws Bpmn2ChoreographyProjectorException {

		// throw an exception if the Diverging Gateway has not outgoing
		// transitions
		if (divergingGateway.getOutgoing().isEmpty()) {
			throw new Bpmn2ChoreographyProjectorException(
					"BPMN2 Diverging Gateway <" + divergingGateway.getName() + "> has not outgoing transitions");
		}

		FlowNode convergingGateway = divergingGateway.getOutgoing().get(0).getTargetRef();

		// check if the target flow is a Converging Converging and the
		// Converging Gateway has the same class of the Diverging Gateway
		if (!(convergingGateway instanceof Gateway)
				|| (convergingGateway instanceof Gateway
						&& ((Gateway) convergingGateway).getGatewayDirection() != GatewayDirection.CONVERGING)
				|| (!divergingGateway.getClass().equals(convergingGateway.getClass())
						&& divergingGateway.getClass().equals(EventBasedGateway.class))) {
			return false;
		}

		// check if the set of outgoing transition of the Diverging Gateway is
		// equals to the set of incoming transition of the Converging Gateway
		return divergingGateway.getOutgoing().containsAll(convergingGateway.getIncoming())
				&& convergingGateway.getIncoming().containsAll(divergingGateway.getOutgoing());

	}

	// checking if a Diverging Gateway is empty. A Diverging Gateway is empty if
	// his outgoing transition directly reach the same End Gateway. The set
	// of outgoing transition of the Diverging Gateway is equals to the set of
	// incoming transition of the End Gateway
	public static boolean isDirectlyConnetedToEndEvent(final Gateway divergingGateway)
			throws Bpmn2ChoreographyProjectorException {

		// throw an exception if the Diverging Gateway has not outgoing
		// transitions
		if (divergingGateway.getOutgoing().isEmpty()) {
			throw new Bpmn2ChoreographyProjectorException(
					"BPMN2 Diverging Gateway <" + divergingGateway.getName() + "> has not outgoing transitions");
		}

		FlowNode endEvent = divergingGateway.getOutgoing().get(0).getTargetRef();

		// check if the target flow is a End Event
		if (!(endEvent instanceof EndEvent)) {
			return false;
		}

		// check if the set of outgoing transition of the Diverging Gateway is
		// equals to the set of incoming transition of the End Event
		return divergingGateway.getOutgoing().containsAll(endEvent.getIncoming())
				&& endEvent.getIncoming().containsAll(divergingGateway.getOutgoing());
	}

	public static boolean isEmptyChoreography(Choreography choreography) {
		for (FlowElement flowElement : choreography.getFlowElements()) {
			/*if (flowElement instanceof ChoreographyTask) {
				return false;
			}*/
			
			if (flowElement instanceof ChoreographyActivity) {
				return false;
			}
		}
		return true;
	}

	public static Choreography getOneNonEmptyChoreography(List<Choreography> choreographies) {
		for (Choreography choreography : choreographies) {
			for (FlowElement flowElement : choreography.getFlowElements()) {
				/*if (flowElement instanceof ChoreographyTask) {
					return choreography;
				}*/
				if (flowElement instanceof ChoreographyActivity) {
					return choreography;
				}
			}
		}
		return null;
	}

	public static List<Participant> allUsefullParticipant(List<Choreography> choreographies) {
		List<Participant> usefullParticipants = new ArrayList<Participant>();

		for (Choreography choreography : choreographies) {
			for (FlowElement flowElement : choreography.getFlowElements()) {
				if (flowElement instanceof ChoreographyActivity) {
					ChoreographyActivity choreographyActivity = (ChoreographyActivity) flowElement;
					for (Participant participant : choreographyActivity.getParticipantRefs()) {
						// note please attention on the condition &&
						// !participant.equals(choreographyActivity.getInitiatingParticipantRef())
						// for now not remove the participant if it is the
						// initiating
						// if
						// (!participant.equals(choreographyActivity.getInitiatingParticipantRef())){
						if (!usefullParticipants.contains(participant)) {
							usefullParticipants.add(participant);
						}
						// }
					}
				}
			}
		}

		return usefullParticipants;
	}
}
