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

import org.eclipse.bpmn2.CallChoreography;
import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyActivity;
import org.eclipse.bpmn2.ChoreographyTask;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SubChoreography;
import org.eclipse.bpmn2.impl.Bpmn2FactoryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.util.Bpmn2ChoreographyProjectorUtils;

public class Bpmn2ChoreographyProjector {
	private static Logger logger = LoggerFactory.getLogger(Bpmn2ChoreographyProjector.class);

	private Bpmn2ChoreographyLoader bpmn2ChoreographyLoader;

	/**
	 * this property is a reference of BPMN participantUsedToBpmnProjection used
	 * to get all activities where this participant is involved
	 */
	private Participant participantUsedToBpmnProjection;

	public Bpmn2ChoreographyProjectorResponse project(
			final Bpmn2ChoreographyProjectorRequest bpmn2ChoreographyProjectorRequest)
			throws Bpmn2ChoreographyProjectorException {
		//
		DocumentRoot documentRoot = Bpmn2ChoreographyProjectorUtils
				.getBpmn2DocumentRoot(bpmn2ChoreographyProjectorRequest.getBpmn2Content());
		// load the BPMN2 Choreography model
		bpmn2ChoreographyLoader = new Bpmn2ChoreographyLoader(documentRoot);
		this.participantUsedToBpmnProjection = Bpmn2ChoreographyProjectorUtils.getParticipant(
				bpmn2ChoreographyLoader.getChoreographies(),
				bpmn2ChoreographyProjectorRequest.getParticipantUsedToBpmn2Projection());

		for (Choreography choreography : bpmn2ChoreographyLoader.getChoreographies()) {
			performProjection(choreography);
		}

		postProcessingProjection();

		// generate the result
		Bpmn2ChoreographyProjectorResponse bpmn2ChoreographyProjectorResponse = new Bpmn2ChoreographyProjectorResponse();
		// byte[] projectetContent =
		// Bpmn2ChoreographyProjectorUtils.getByteArray(EcoreUtil.copy(bpmn2ChoreographyLoader.getDocumentRoot()));
		byte[] projectetContent = Bpmn2ChoreographyProjectorUtils
				.getByteArray(bpmn2ChoreographyLoader.getDocumentRoot());
		bpmn2ChoreographyProjectorResponse.setBpmn2Content(projectetContent);

		return bpmn2ChoreographyProjectorResponse;
	}

	private void performProjection(Choreography choreography) throws Bpmn2ChoreographyProjectorException {
		// List<SequenceFlow>
		// changeExclusiveWithEventBaseGateway(choreography);

		deleteAllUnnecessaryChoreographyActivity(choreography);

		while (changeExclusiveWithEventBaseGateway(choreography) || deleteAllEmptyDivergingGateway(choreography)
				|| deleteAllParallelGatewayEmptyTransitions(choreography) || serializeAllDivergingGateway(choreography)
				|| serializeAllConverginGateway(choreography) || removeEmptyParallelGatewayFlows(choreography)
				|| removeNotUsedSequenceFlow(choreography) || removeAllEmptyEndEvent(choreography)
				|| collapseEventBasedGateway(choreography) || removeEmptyCycle(choreography)) {
			// leave empty
		}

		// remove this method: fix issue wp5 projection of Stapp
		fixIssueDoNotRemoveExclusiveGatewayWP5(choreography);

		// remove this method: fix issue wp4 projection of SEADA-SEARP,
		// SEADA-SEATSA and ND
		fixIssueDoNotRemoveEventBasedGatewayWP4(choreography);

	}

	// Change Exclusive Gateway in Event Base Gateway if each Choreography
	// Activity connected by the outgoing transition does not have the
	// projected participant as initiating participant
	private boolean changeExclusiveWithEventBaseGateway(Choreography choreography) {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof ExclusiveGateway
					&& ((ExclusiveGateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING) {

				ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
				boolean change = true;
				for (SequenceFlow sequenceFlow : exclusiveGateway.getOutgoing()) {
					if ((sequenceFlow.getTargetRef() instanceof ChoreographyActivity
							&& ((ChoreographyActivity) sequenceFlow.getTargetRef()).getInitiatingParticipantRef()
									.equals(participantUsedToBpmnProjection))
							|| (sequenceFlow.getTargetRef() instanceof Gateway
									&& !(sequenceFlow.getTargetRef() instanceof EventBasedGateway)
									&& !(((Gateway) sequenceFlow.getTargetRef())
											.getGatewayDirection() == GatewayDirection.CONVERGING))) {
						change = false;
						break;

					}

				}

				if (change) {
					EventBasedGateway eventBasedGateway = Bpmn2FactoryImpl.eINSTANCE.createEventBasedGateway();
					eventBasedGateway.setName(exclusiveGateway.getName());
					eventBasedGateway.setGatewayDirection(exclusiveGateway.getGatewayDirection());
					for (SequenceFlow incoming : new ArrayList<SequenceFlow>(exclusiveGateway.getIncoming())) {
						incoming.setTargetRef(eventBasedGateway);
					}
					for (SequenceFlow outgoing : new ArrayList<SequenceFlow>(exclusiveGateway.getOutgoing())) {
						outgoing.setSourceRef(eventBasedGateway);
					}
					// delete the Exclusive Gateway
					EcoreUtil.delete(exclusiveGateway, true);
					choreography.getFlowElements().add(eventBasedGateway);
					isDeletionPerformed = true;
				}

			}
		}
		return isDeletionPerformed;
	}

	// all choreography activities that they haven't as
	// initiating and receiving the participant used to projection. the removal
	// of one choreography activity occurs changing the target flow node for
	// each incoming transition with the target flow node of the outgoing
	// transition.
	// We assume that a Choreography activity has only one outgoing transition
	// and has infinite incoming transition
	private boolean deleteAllUnnecessaryChoreographyActivity(Choreography choreography)
			throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;
		// delete all choreography activities that they haven't as initiating
		// and receiving the participant used to projection
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof ChoreographyActivity) {

				ChoreographyActivity choreographyActivity = (ChoreographyActivity) flowElement;
				if (!Bpmn2ChoreographyProjectorUtils.hasParticipant(choreographyActivity,
						participantUsedToBpmnProjection)) {
					// delete this activity
					logger.info("   Delete \"" + choreographyActivity.getName() + "\" on " + choreography.getName()
							+ " Choreography");

					// get outgoing transition: We assume that there is only one
					// outgoing transition
					if (choreographyActivity.getOutgoing().isEmpty()) {
						throw new Bpmn2ChoreographyProjectorException("BPMN2 Choreography Activity <"
								+ choreographyActivity.getName() + "> has not outgoing transition");
					} else if (choreographyActivity.getOutgoing().size() > 1) {
						throw new Bpmn2ChoreographyProjectorException("BPMN2 Choreography Activity <"
								+ choreographyActivity.getName() + "> has more than one outgoing transition");
					}

					SequenceFlow outgoingTransition = choreographyActivity.getOutgoing().get(0);

					// for each incoming transitions, change the target Flow
					// Node with the target Flow node of the outgoing transition
					for (SequenceFlow incomingTransition : new ArrayList<SequenceFlow>(
							choreographyActivity.getIncoming())) {
						incomingTransition.setTargetRef(outgoingTransition.getTargetRef());
					}
					// delete outgoing transition
					EcoreUtil.delete(outgoingTransition);

					// delete Message Flow is the Choreography Activity is a
					// ChoreographyTask
					if (choreographyActivity instanceof ChoreographyTask) {
						for (MessageFlow messageFlow : new ArrayList<MessageFlow>(
								((ChoreographyTask) choreographyActivity).getMessageFlowRef())) {
							EcoreUtil.delete(messageFlow);
						}
					}

					// delete the Choreography Activity
					EcoreUtil.delete(choreographyActivity);

					// Cheking if the participants can be removed
					for (Participant participant : new ArrayList<Participant>(
							choreographyActivity.getParticipantRefs())) {
						if (!Bpmn2ChoreographyProjectorUtils
								.isParticipantUsed(bpmn2ChoreographyLoader.getChoreographies(), participant)) {
							EcoreUtil.delete(participant);
						}
					}

					isDeletionPerformed = true;
				}
			}
		}

		return isDeletionPerformed;
	}

	// delete all empty Diverging Gateway. A Diverging Gateway is empty if its
	// outgoing transition directly reach the same Converging Gateway. The
	// Converging Gateway has the same class of the Diverging Gateway. The set
	// of outgoing transition of the Diverging Gateway is equals to the set of
	// incoming transition of the Converging Gateway
	private boolean deleteAllEmptyDivergingGateway(Choreography choreography)
			throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			// get all Diverging Gateway
			if (flowElement instanceof Gateway
					&& ((Gateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING) {
				Gateway divergingGateway = (Gateway) flowElement;
				if (Bpmn2ChoreographyProjectorUtils.isDirectlyConnetedToConvergingGateway(divergingGateway)) {
					// get the related Converging Gateway
					Gateway convergingGateway = (Gateway) divergingGateway.getOutgoing().get(0).getTargetRef();

					// delete all outgoing sequenceFlow of the Diverging Gateway
					for (SequenceFlow sequenceFlow : new ArrayList<SequenceFlow>(divergingGateway.getOutgoing())) {
						EcoreUtil.delete(sequenceFlow);
					}

					// for each incoming transitions of the Diverging Gateway,
					// change the target Flow Element with the target Flow
					// Element of the outgoing transition of the Converging
					// Gateway
					for (SequenceFlow incomingTransition : new ArrayList<SequenceFlow>(
							divergingGateway.getIncoming())) {
						incomingTransition.setTargetRef(convergingGateway.getOutgoing().get(0).getTargetRef());
					}
					// delete the outgoing transition of the targetFlowNode
					EcoreUtil.delete(convergingGateway.getOutgoing().get(0));
					// delete the Diverging Gateway
					EcoreUtil.delete(divergingGateway);
					// delete the Converging Gateway
					EcoreUtil.delete(convergingGateway);

					isDeletionPerformed = true;
				}
			}
		}

		return isDeletionPerformed;
	}

	// delete all outgoing transition of the Parallel Diverging Gateway that
	// they are directly connected to a Parallel Converging Gateway
	private boolean deleteAllParallelGatewayEmptyTransitions(Choreography choreography)
			throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			// get all Parallel Diverging Gateway
			if (flowElement instanceof ParallelGateway
					&& ((ParallelGateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING) {

				Gateway divergingGateway = (ParallelGateway) flowElement;

				for (SequenceFlow sequenceFlow : new ArrayList<SequenceFlow>(divergingGateway.getOutgoing())) {
					if (sequenceFlow.getTargetRef() instanceof ParallelGateway
							&& ((ParallelGateway) sequenceFlow.getTargetRef())
									.getGatewayDirection() == GatewayDirection.CONVERGING
							&& divergingGateway.getClass().equals(sequenceFlow.getTargetRef().getClass())) {
						// delete the transition
						EcoreUtil.delete(sequenceFlow);

						isDeletionPerformed = true;
					}
				}

			}
		}
		return isDeletionPerformed;

	}

	// serialize all Diverging Gateway. A Diverging Gateway can be serialized if
	// it has only one incoming transition and only one outgoing transition.
	// Note that: we can serialize an Exclusive Gateway or a Complex Gateway
	// only if they have a
	// target transition connected to an End Event or a Choreography Activity.
	// In the case of the target transition is a Choreography Activity, the
	// Choreography Activity must not have the projected participant as
	// initiating participant
	// We can also serialize Parallel Gateway independently of the targetFlow of
	// the outgoing transition.
	private boolean serializeAllDivergingGateway(Choreography choreography) throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if ((flowElement instanceof ParallelGateway
					&& ((Gateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING
					&& ((Gateway) flowElement).getIncoming().size() == 1
					&& ((Gateway) flowElement).getOutgoing().size() == 1)
					|| (flowElement instanceof Gateway && !(flowElement instanceof ParallelGateway)
							&& ((Gateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING
							&& ((Gateway) flowElement).getIncoming().size() == 1
							&& ((Gateway) flowElement).getOutgoing().size() == 1
							&& (((Gateway) flowElement).getOutgoing().get(0).getTargetRef() instanceof EndEvent
									|| ((Gateway) flowElement).getOutgoing().get(0)
											.getTargetRef() instanceof EventBasedGateway
									|| (((Gateway) flowElement).getOutgoing().get(0)
											.getTargetRef() instanceof ChoreographyActivity)
											&& !((ChoreographyActivity) ((Gateway) flowElement).getOutgoing().get(0)
													.getTargetRef()).getInitiatingParticipantRef()
															.equals(participantUsedToBpmnProjection)))) {

				((Gateway) flowElement).getIncoming().get(0)
						.setTargetRef(((Gateway) flowElement).getOutgoing().get(0).getTargetRef());

				// delete the outgoing transition
				EcoreUtil.delete(((Gateway) flowElement).getOutgoing().get(0), true);

				// delete the Diverging Gateway
				EcoreUtil.delete(flowElement, true);

				isDeletionPerformed = true;
			}
		}
		return isDeletionPerformed;
	}

	// serialize all Converging Gateway. A Converging Gateway can be serialized
	// if he has only one incoming transition and only one outgoing transition
	private boolean serializeAllConverginGateway(Choreography choreography) throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof Gateway
					&& ((Gateway) flowElement).getGatewayDirection() == GatewayDirection.CONVERGING
					&& ((Gateway) flowElement).getIncoming().size() == 1
					&& ((Gateway) flowElement).getOutgoing().size() == 1) {

				((Gateway) flowElement).getIncoming().get(0)
						.setTargetRef(((Gateway) flowElement).getOutgoing().get(0).getTargetRef());

				// delete the outgoing transition
				EcoreUtil.delete(((Gateway) flowElement).getOutgoing().get(0));
				// delete the Converging Gateway
				EcoreUtil.delete(flowElement);

				isDeletionPerformed = true;
			}
		}
		return isDeletionPerformed;
	}

	// collapse event based gateway. AnEvent Based Gateway can be always
	// collapsed, changing the source of each outgoing transition of the target
	// event based gateway.
	private boolean collapseEventBasedGateway(Choreography choreography) {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof SequenceFlow
					&& ((SequenceFlow) flowElement).getSourceRef() instanceof EventBasedGateway
					&& ((SequenceFlow) flowElement).getTargetRef() instanceof EventBasedGateway) {

				EventBasedGateway sourceBasedGateway = (EventBasedGateway) ((SequenceFlow) flowElement).getSourceRef();
				EventBasedGateway targetBasedGateway = (EventBasedGateway) ((SequenceFlow) flowElement).getTargetRef();
				for (SequenceFlow sequenceFlow : new ArrayList<SequenceFlow>(targetBasedGateway.getOutgoing())) {
					sequenceFlow.setSourceRef(sourceBasedGateway);
				}

				// delete the target EventBasedGateway
				EcoreUtil.delete(targetBasedGateway);

				// delete the sequence flow
				EcoreUtil.delete(flowElement);

				isDeletionPerformed = true;
			}
		}
		return isDeletionPerformed;
	}

	// remove empty parallel gateway flows. A Parallel Gateway Flow is empty is
	// the sourceRef of the flow is a Converging Parallel Gateway and the
	// targetFlowRef is a End Event
	// note that: removing empty parallel gateway flows it may be that we could
	// have the Parallel Gateway without outgoing transition then it must be
	// removed.
	// We create create an End Event for each incoming transition and we connect
	// each incoming to the new End Event
	private boolean removeEmptyParallelGatewayFlows(Choreography choreography)
			throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;

		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof SequenceFlow
					&& ((SequenceFlow) flowElement).getSourceRef() instanceof ParallelGateway
					&& ((ParallelGateway) ((SequenceFlow) flowElement).getSourceRef())
							.getGatewayDirection() == GatewayDirection.DIVERGING
					&& ((SequenceFlow) flowElement).getTargetRef() instanceof EndEvent) {

				// delete the End Event
				EcoreUtil.delete(((SequenceFlow) flowElement).getTargetRef());

				// delete the sequence flow
				EcoreUtil.delete(flowElement);

				isDeletionPerformed = true;
			}
		}

		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof ParallelGateway
					&& ((Gateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING
					&& ((Gateway) flowElement).getOutgoing().isEmpty()) {

				ParallelGateway parallelGateway = (ParallelGateway) flowElement;

				// delete all outgoing sequenceFlow of the Diverging Gateway
				for (SequenceFlow sequenceFlow : new ArrayList<SequenceFlow>(parallelGateway.getIncoming())) {
					EndEvent endEvent = Bpmn2FactoryImpl.eINSTANCE.createEndEvent();
					sequenceFlow.setTargetRef(endEvent);
					choreography.getFlowElements().add(endEvent);
				}

				// delete the sequence flow
				EcoreUtil.delete(flowElement);

				isDeletionPerformed = true;
			}
		}

		return isDeletionPerformed;
	}

	// remove not used sequence flow. A sequence flow is not used if the
	// sourceRef of the sequence flow is equals to the targetRef of the sequence
	// flow
	private boolean removeNotUsedSequenceFlow(Choreography choreography) {
		boolean isDeletionPerformed = false;

		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof SequenceFlow && ((SequenceFlow) flowElement).getSourceRef()
					.equals(((SequenceFlow) flowElement).getTargetRef())) {
				// delete the sequence flow
				EcoreUtil.delete(flowElement);

				isDeletionPerformed = true;
			}
		}

		return isDeletionPerformed;
	}

	// remove all empty end event. An End Event is empty if he has zero incoming
	// flow and zero outgoing sequence flow
	private boolean removeAllEmptyEndEvent(Choreography choreography) throws Bpmn2ChoreographyProjectorException {
		boolean isDeletionPerformed = false;
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof EndEvent && ((EndEvent) flowElement).getIncoming().isEmpty()
					&& ((EndEvent) flowElement).getOutgoing().isEmpty()) {
				// delete the outgoing transition
				EcoreUtil.delete(flowElement);
				isDeletionPerformed = true;
			}
		}

		return isDeletionPerformed;
	}

	private boolean removeEmptyCycle(Choreography choreography) {
		boolean isDeletionPerformed = false;

		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof ExclusiveGateway
					&& ((Gateway) flowElement).getGatewayDirection() == GatewayDirection.CONVERGING
					&& ((Gateway) flowElement).getOutgoing().get(0).getTargetRef() instanceof Gateway
					&& ((Gateway) ((Gateway) flowElement).getOutgoing().get(0).getTargetRef())
							.getGatewayDirection() == GatewayDirection.DIVERGING) {
				ExclusiveGateway convergingGateway = (ExclusiveGateway) flowElement;
				Gateway divergingGateway = (Gateway) ((Gateway) flowElement).getOutgoing().get(0).getTargetRef();
				boolean isLoop = true;
				EndEvent endEvent = null;
				List<SequenceFlow> sequenceFlowInvolvedToTheLoop = new ArrayList<SequenceFlow>();
				for (SequenceFlow sequenceFlow : divergingGateway.getOutgoing()) {
					if (!sequenceFlow.equals(((Gateway) flowElement).getOutgoing().get(0))) {
						if (sequenceFlow.getTargetRef() instanceof EndEvent) {
							endEvent = (EndEvent) sequenceFlow.getTargetRef();
						} else if (!sequenceFlow.getTargetRef().equals(convergingGateway)) {
							isLoop = false;
							break;
						} else {
							sequenceFlowInvolvedToTheLoop.add(sequenceFlow);
						}
					}
				}

				if (isLoop && endEvent != null) {
					for (SequenceFlow sequenceFlow : new ArrayList<SequenceFlow>(divergingGateway.getOutgoing())) {
						EcoreUtil.delete(sequenceFlow);
					}
					divergingGateway.getIncoming().get(0).setTargetRef(endEvent);
					// EcoreUtil.delete(((Gateway)
					// flowElement).getOutgoing().get(0));
					EcoreUtil.delete(divergingGateway);
					isDeletionPerformed = true;
				}
			}
		}
		return isDeletionPerformed;
	}

	// remove this method
	private void fixIssueDoNotRemoveExclusiveGatewayWP5(Choreography choreography) {
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof ExclusiveGateway
					&& ((ExclusiveGateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING
					&& ((ExclusiveGateway) flowElement).getName() != null
					&& ((ExclusiveGateway) flowElement).getName().equals("Trip Accepted?")
					&& participantUsedToBpmnProjection.getName().equals("Trip Planner")) {

				ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;

				EndEvent endEvent = null;
				for (SequenceFlow outgoing : new ArrayList<SequenceFlow>(exclusiveGateway.getOutgoing())) {
					if (outgoing.getTargetRef() instanceof EndEvent) {
						endEvent = (EndEvent) outgoing.getTargetRef();
					}
					EcoreUtil.delete(outgoing);
				}
				exclusiveGateway.getIncoming().get(0).setTargetRef(endEvent);

				// delete the sequence flow
				EcoreUtil.delete(flowElement);

			}
		}
	}

	// remove this method
	private void fixIssueDoNotRemoveEventBasedGatewayWP4(Choreography choreography) {
		// remove that has 2 outgoing transition: "new route needed" and
		// "destination reached"
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof EventBasedGateway
					&& ((EventBasedGateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING
					&& ((EventBasedGateway) flowElement).getName() == null
					&& (participantUsedToBpmnProjection.getName().equals("SEADA-SEARP")
							|| participantUsedToBpmnProjection.getName().equals("SEADA-SEATSA")
							|| participantUsedToBpmnProjection.getName().equals("ND"))
					&& ((((EventBasedGateway) flowElement).getOutgoing().size() == 2
							&& (((EventBasedGateway) flowElement).getOutgoing().get(0).getName()
									.equals("new route needed")
									|| ((EventBasedGateway) flowElement).getOutgoing().get(0).getName()
											.equals("destination reached"))
							&& (((EventBasedGateway) flowElement).getOutgoing().get(1).getName()
									.equals("new route needed")
									|| ((EventBasedGateway) flowElement).getOutgoing().get(1).getName()
											.equals("destination reached")))
							|| (((EventBasedGateway) flowElement).getOutgoing().size() == 3
									&& (((EventBasedGateway) flowElement).getOutgoing().get(0).getName() == null
											|| ((EventBasedGateway) flowElement).getOutgoing().get(0).getName()
													.equals("new route needed")
											|| ((EventBasedGateway) flowElement).getOutgoing().get(0).getName()
													.equals("destination reached"))
									&& (((EventBasedGateway) flowElement).getOutgoing().get(1).getName() == null
											|| ((EventBasedGateway) flowElement).getOutgoing().get(1).getName()
													.equals("new route needed")
											|| ((EventBasedGateway) flowElement).getOutgoing().get(1).getName()
													.equals("destination reached"))
									&& (((EventBasedGateway) flowElement).getOutgoing().get(2).getName() == null
											|| ((EventBasedGateway) flowElement).getOutgoing().get(2).getName()
													.equals("new route needed")
											|| ((EventBasedGateway) flowElement).getOutgoing().get(2).getName()
													.equals("destination reached"))))) {

				EventBasedGateway eventBasedGateway = (EventBasedGateway) flowElement;

				EndEvent endEvent = null;
				for (SequenceFlow outgoing : new ArrayList<SequenceFlow>(eventBasedGateway.getOutgoing())) {
					if (outgoing.getTargetRef() instanceof EndEvent) {
						endEvent = (EndEvent) outgoing.getTargetRef();
					}
					EcoreUtil.delete(outgoing);
				}
				eventBasedGateway.getIncoming().get(0).setTargetRef(endEvent);

				// delete the sequence flow
				EcoreUtil.delete(flowElement);

			}
		}

		// remove that has 2 outgoing transition: "new route needed" and
		// "destination reached"
		for (FlowElement flowElement : new ArrayList<FlowElement>(choreography.getFlowElements())) {
			if (flowElement instanceof EventBasedGateway
					&& ((EventBasedGateway) flowElement).getGatewayDirection() == GatewayDirection.DIVERGING
					&& ((EventBasedGateway) flowElement).getName() == null
					&& (participantUsedToBpmnProjection.getName().equals("SEADA-SEARP")
							|| participantUsedToBpmnProjection.getName().equals("SEADA-SEATSA")
							|| participantUsedToBpmnProjection.getName().equals("ND"))
					&& ((EventBasedGateway) flowElement).getOutgoing().size() == 2
					&& (((EventBasedGateway) flowElement).getOutgoing().get(0).getName().equals("route not accepted")
							|| ((EventBasedGateway) flowElement).getOutgoing().get(0).getName()
									.equals("route accepted"))
					&& (((EventBasedGateway) flowElement).getOutgoing().get(1).getName().equals("route not accepted")
							|| ((EventBasedGateway) flowElement).getOutgoing().get(1).getName()
									.equals("route accepted"))) {

				EventBasedGateway eventBasedGateway = (EventBasedGateway) flowElement;

				if (participantUsedToBpmnProjection.getName().equals("SEADA-SEARP")) {
					EndEvent endEvent = null;
					for (SequenceFlow outgoing : new ArrayList<SequenceFlow>(eventBasedGateway.getOutgoing())) {
						if (outgoing.getTargetRef() instanceof EndEvent) {
							endEvent = (EndEvent) outgoing.getTargetRef();
						}
						EcoreUtil.delete(outgoing);
					}
					eventBasedGateway.getIncoming().get(0).setTargetRef(endEvent);

					// delete the sequence flow
					EcoreUtil.delete(flowElement);
				} else {
					for (SequenceFlow outgoing : new ArrayList<SequenceFlow>(eventBasedGateway.getOutgoing())) {
						if (outgoing.getName().equals("route not accepted")) {
							EcoreUtil.delete(outgoing);
						}

					}
				}
			}
		}
	}

	private void postProcessingProjection() {

		Choreography nonEmptyChoreography = Bpmn2ChoreographyProjectorUtils
				.getOneNonEmptyChoreography(bpmn2ChoreographyLoader.getChoreographies());

		for (Choreography choreography : new ArrayList<Choreography>(bpmn2ChoreographyLoader.getChoreographies())) {
			if (!choreography.equals(nonEmptyChoreography)) {
				// move all participants in one Choreography that cannot be
				// removed
				nonEmptyChoreography.getParticipants().addAll(choreography.getParticipants());

				// check if the choreography is empty, in that case the
				// choreography will be removed
				if (Bpmn2ChoreographyProjectorUtils.isEmptyChoreography(choreography)) {
					EcoreUtil.delete(choreography);
				}
			}
		}

		// get list of Participant used in all Choreography Task
		List<Participant> usefullParticipants = Bpmn2ChoreographyProjectorUtils
				.allUsefullParticipant(bpmn2ChoreographyLoader.getChoreographies());

		// remove non used participant
		for (Participant participant : new ArrayList<Participant>(nonEmptyChoreography.getParticipants())) {
			if (!usefullParticipants.contains(participant)) {
				nonEmptyChoreography.getParticipants().remove(participant);
			}
		}

		for (FlowElement flowElement : nonEmptyChoreography.getFlowElements()) {
			if (flowElement instanceof ChoreographyTask) {
				ChoreographyTask choreographyTask = (ChoreographyTask) flowElement;
				for (Participant participant : choreographyTask.getParticipantRefs()) {
					if (!usefullParticipants.contains(participant)) {
						nonEmptyChoreography.getParticipants().remove(participant);
					}
				}
			}
		}

		// remove reference of participant not used in Sub/Call Choreography
		// Task
		for (Choreography choreography : bpmn2ChoreographyLoader.getChoreographies()) {
			for (FlowElement flowElement : choreography.getFlowElements()) {
				if (flowElement instanceof SubChoreography || flowElement instanceof CallChoreography) {
					ChoreographyActivity choreographyActivity = (ChoreographyActivity) flowElement;
					for (Participant participant : new ArrayList<Participant>(
							choreographyActivity.getParticipantRefs())) {

						if (!usefullParticipants.contains(participant)) {
							choreographyActivity.getParticipantRefs().remove(participant);
						}
					}
				}
			}
		}
	}
}
