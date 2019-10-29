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
package eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyTask;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Participant;

import eu.chorevolution.modelingnotations.chorarch.Adapter;
import eu.chorevolution.modelingnotations.chorarch.BindingComponent;
import eu.chorevolution.modelingnotations.chorarch.BusinessComponent;
import eu.chorevolution.modelingnotations.chorarch.ChorArchModel;
import eu.chorevolution.modelingnotations.chorarch.ChorarchFactory;
import eu.chorevolution.modelingnotations.chorarch.ClientAppComponent;
import eu.chorevolution.modelingnotations.chorarch.Component;
import eu.chorevolution.modelingnotations.chorarch.ConsumerCoordinationDelegate;
import eu.chorevolution.modelingnotations.chorarch.ConsumerInterface;
import eu.chorevolution.modelingnotations.chorarch.ProsumerCoordinationDelegate;
import eu.chorevolution.modelingnotations.chorarch.ProviderInterface;
import eu.chorevolution.modelingnotations.chorarch.RestServiceComponent;
import eu.chorevolution.modelingnotations.chorarch.SecurityFilter;
import eu.chorevolution.modelingnotations.chorarch.ThingComponent;
import eu.chorevolution.modelingnotations.chorarch.WebServiceComponent;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.ComponentData;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.InterfaceDependency;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.util.Bpmn2ChoreographyLoader;
import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.util.ChoreographyArchitectureGeneratorUtils;

public class ChoreographyArchitectureGenerator {
	private ChoreographyArchitectureGeneratorRequest bpmn2ChoreoArchGeneratorRequest;
	private Bpmn2ChoreographyLoader bpmn2ChoreographyLoader;

	private List<ChorArchModel> chorArchModels;

	private Map<Participant, Component> providerComponents;
	private Map<Participant, Component> clientComponents;
	private Map<Participant, Component> consumerCoordinationComponents;
	private Map<Participant, Component> bindingComponents;
	private Map<Participant, Component> securityComponents;
	private Map<Participant, Component> adapterComponents;
	private Map<Participant, Component> prosumerCoordinationComponents;

	private List<InterfaceDependency> interfaceDependencies;

	public ChoreographyArchitectureGeneratorResponse generate(
			final ChoreographyArchitectureGeneratorRequest bpmn2ChoreoArchGeneratorRequest)
			throws ChoreographyArchitectureGeneratorException {

		// load the BPMN2 Choreography model
		this.bpmn2ChoreoArchGeneratorRequest = bpmn2ChoreoArchGeneratorRequest;

		bpmn2ChoreographyLoader = new Bpmn2ChoreographyLoader(this.bpmn2ChoreoArchGeneratorRequest.getBpmn2Content());

		chorArchModels = new ArrayList<ChorArchModel>();

		providerComponents = new HashMap<Participant, Component>();
		clientComponents = new HashMap<Participant, Component>();
		consumerCoordinationComponents = new HashMap<Participant, Component>();
		bindingComponents = new HashMap<Participant, Component>();
		securityComponents = new HashMap<Participant, Component>();
		prosumerCoordinationComponents = new HashMap<Participant, Component>();
		adapterComponents = new HashMap<Participant, Component>();

		interfaceDependencies = new ArrayList<InterfaceDependency>();

		createComponents();
		
		for (Choreography choreography : bpmn2ChoreographyLoader.getChoreographies()){
			createProviderDependencies(choreography);
		}
		
		for (Choreography choreography : bpmn2ChoreographyLoader.getChoreographies()){
			createProsumerDependencies(choreography);
		}

		ChoreographyArchitectureGeneratorResponse bpmn2ChoreoArchTransformatorResponse = new ChoreographyArchitectureGeneratorResponse();
		ChorArchModel chorArchModel = ChorarchFactory.eINSTANCE.createChorArchModel();
		//chorArchModel.setChoreographyID("...");
		//chorArchModel.setChoreographyName("...");
		chorArchModel.getComponents().addAll(providerComponents.values());
		chorArchModel.getComponents().addAll(prosumerCoordinationComponents.values());
		chorArchModel.getComponents().addAll(consumerCoordinationComponents.values());
		chorArchModel.getComponents().addAll(securityComponents.values());
		chorArchModel.getComponents().addAll(bindingComponents.values());
		chorArchModel.getComponents().addAll(clientComponents.values());
		chorArchModel.getComponents().addAll(adapterComponents.values());

		chorArchModels.add(chorArchModel);

		bpmn2ChoreoArchTransformatorResponse.setChoreographyArchitecture(
				ChoreographyArchitectureGeneratorUtils.getChoreoArchContent(chorArchModel));

		return bpmn2ChoreoArchTransformatorResponse;
	}
	
	/*
	 * - create component for client participants
	 * - create component for prosumer participants
	 * - create component for provider participants
	 */
	private void createComponents() throws ChoreographyArchitectureGeneratorException {

		// create component for client participants
		for (ComponentData componentData : bpmn2ChoreoArchGeneratorRequest.getClientParticipants()) {
			ClientAppComponent clientComponent = createClientComponent(componentData);
			
			BindingComponent bindingComponent = createBindingComponent(componentData);
			
			SecurityFilter securityComponent = createSecurityComponent(componentData, true);
			
			ProsumerCoordinationDelegate prosumerCoordinationComponent = createProsumerCoordinationComponent(componentData);
			
			Adapter adapterComponent = createAdapterComponent(componentData);

			// set dependencies between components
			createClientComponentChain(clientComponent, bindingComponent, securityComponent, adapterComponent, prosumerCoordinationComponent);
			
		}

		// create component for prosumer participants
		for (ComponentData componentData : bpmn2ChoreoArchGeneratorRequest.getProsumerParticipants()) {
			ConsumerCoordinationDelegate consumerCoordinationDelegate = createConsumerCoordinationComponent(componentData);

			ProsumerCoordinationDelegate prosumerCoordinationDelegate = createProsumerCoordinationComponent(componentData);

			// set dependencies between components
			createConsumerCoordinationComponentChain(consumerCoordinationDelegate, prosumerCoordinationDelegate);

		}

		// create component for provider participants
		for (ComponentData componentData : bpmn2ChoreoArchGeneratorRequest.getProviderParticipants()) {
			BusinessComponent providerComponent;
			
			if (componentData.getBindingComponentData()==null){
				providerComponent = createProviderComponent(componentData, WebServiceComponent.class);
			}else{
				providerComponent = createProviderComponent(componentData, RestServiceComponent.class);
			}
			
			BindingComponent bindingComponent = createBindingComponent(componentData);
			
			SecurityFilter securityComponent = createSecurityComponent(componentData, false);
			
			Adapter adapterComponent = createAdapterComponent(componentData);
			
			// set dependencies between components
			createProviderComponentChain(providerComponent, bindingComponent, securityComponent, adapterComponent);

		}

	}
	
	private void createProviderDependencies(final Choreography choreography)
			throws ChoreographyArchitectureGeneratorException {
		for (FlowElement flowElement : choreography.getFlowElements()) {
			if (flowElement instanceof ChoreographyTask) {
				ChoreographyTask choreographyTask = (ChoreographyTask) flowElement;
				Participant initiatingParticipant = ChoreographyArchitectureGeneratorUtils.getInitiatingParticipant(choreographyTask);
				Participant receivingParticipant = ChoreographyArchitectureGeneratorUtils.getReceivingParticipant(choreographyTask);

				InterfaceDependency interfaceDependency = new InterfaceDependency(initiatingParticipant,receivingParticipant);

				if (providerComponents.containsKey(receivingParticipant)
						&& !interfaceDependencies.contains(interfaceDependency)) {
					interfaceDependencies.add(interfaceDependency);
					
					// get provider Component
					Component businessComponent;
					
					if (adapterComponents.containsKey(receivingParticipant)){
						businessComponent = adapterComponents.get(receivingParticipant);
					}else if (securityComponents.containsKey(receivingParticipant)){
						businessComponent = securityComponents.get(receivingParticipant);
					}else if (bindingComponents.containsKey(receivingParticipant)){
						businessComponent = bindingComponents.get(receivingParticipant);
					}else{
						businessComponent = providerComponents.get(receivingParticipant);
					}

					// Coordination component
					Component coordinationComponent = prosumerCoordinationComponents.get(initiatingParticipant);
					createComponentDependency(businessComponent, coordinationComponent);
				}
			}
		}
	}

	private void createProsumerDependencies(final Choreography choreography)
			throws ChoreographyArchitectureGeneratorException {
		for (FlowElement flowElement : choreography.getFlowElements()) {
			if (flowElement instanceof ChoreographyTask) {
				ChoreographyTask choreographyTask = (ChoreographyTask) flowElement;
				Participant initiatingParticipant = ChoreographyArchitectureGeneratorUtils.getInitiatingParticipant(choreographyTask);
				Participant receivingParticipant = ChoreographyArchitectureGeneratorUtils.getReceivingParticipant(choreographyTask);

				InterfaceDependency interfaceDependency = new InterfaceDependency(initiatingParticipant, receivingParticipant);
				
				if (prosumerCoordinationComponents.containsKey(initiatingParticipant)
						&& prosumerCoordinationComponents.containsKey(receivingParticipant)
						&& !interfaceDependencies.contains(interfaceDependency)) {
					interfaceDependencies.add(interfaceDependency);

					// get initiating Component
					Component initiatingCoordinationComponent = prosumerCoordinationComponents.get(initiatingParticipant);
					
					// get receiving Component
					Component receivingCoordinationComponent = prosumerCoordinationComponents.get(receivingParticipant);
					createComponentDependency(initiatingCoordinationComponent, receivingCoordinationComponent);				}
			}
		}
	}

	private ClientAppComponent createClientComponent (ComponentData componentData) throws ChoreographyArchitectureGeneratorException{
		if (componentData == null){
			return null;
		}
		
		ClientAppComponent clientComponent = ChorarchFactory.eINSTANCE.createClientAppComponent();
		clientComponent.setName(componentData.getName());
		
		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		clientComponents.put(bpmn2Participant, clientComponent);
		
		return clientComponent;
	}
	
	private Adapter createAdapterComponent (ComponentData componentData) throws ChoreographyArchitectureGeneratorException{
		if (componentData.getAdapterComponentData() == null){
			return null;
		}
		
		Adapter adapter = ChorarchFactory.eINSTANCE.createAdapter();
		adapter.setName(componentData.getAdapterComponentData().getName());
		adapter.setLocation(componentData.getAdapterComponentData().getLocation());
		adapter.getRoles().add(componentData.getParticipantName());
		
		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		adapterComponents.put(bpmn2Participant, adapter);
		
		return adapter;
	}
	
	
	private BindingComponent createBindingComponent (ComponentData componentData) throws ChoreographyArchitectureGeneratorException{
		if (componentData.getBindingComponentData() == null){
			return null;
		}
		
		BindingComponent bindingComponent = ChorarchFactory.eINSTANCE.createBindingComponent();
		bindingComponent.setName(componentData.getBindingComponentData().getName());
		bindingComponent.setLocation(componentData.getBindingComponentData().getLocation());
		bindingComponent.getRoles().add(componentData.getParticipantName());
		
		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		bindingComponents.put(bpmn2Participant, bindingComponent);
		
		return bindingComponent;
	}
	
	private SecurityFilter createSecurityComponent (ComponentData componentData, boolean clientSide) throws ChoreographyArchitectureGeneratorException{
		if (componentData.getSecurityComponentData() == null){
			return null;
		}
		
		SecurityFilter securityComponent = ChorarchFactory.eINSTANCE.createSecurityFilter();
		securityComponent.setName(componentData.getSecurityComponentData().getName());
		securityComponent.setLocation(componentData.getSecurityComponentData().getLocation());
		securityComponent.setGlobal(clientSide);
		securityComponent.getRoles().add(componentData.getParticipantName());
		
		
		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		securityComponents.put(bpmn2Participant, securityComponent);
		
		return securityComponent;
	}
	
	private ProsumerCoordinationDelegate createProsumerCoordinationComponent (ComponentData componentData) throws ChoreographyArchitectureGeneratorException{
		if (componentData == null){
			return null;
		}
		
		ProsumerCoordinationDelegate prosumerCoordinationComponent = ChorarchFactory.eINSTANCE.createProsumerCoordinationDelegate();
		prosumerCoordinationComponent.setName(componentData.getName());
		prosumerCoordinationComponent.setLocation(componentData.getLocation());
		prosumerCoordinationComponent.getRoles().add(componentData.getParticipantName());

		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		prosumerCoordinationComponents.put(bpmn2Participant, prosumerCoordinationComponent);
		
		return prosumerCoordinationComponent;
	}

	private ConsumerCoordinationDelegate createConsumerCoordinationComponent (ComponentData componentData) throws ChoreographyArchitectureGeneratorException{
		if (componentData.getConsumerComponentData() == null){
			return null;
		}
		
		ConsumerCoordinationDelegate consumerCoordinationComponent = ChorarchFactory.eINSTANCE.createConsumerCoordinationDelegate();
		consumerCoordinationComponent.setName(componentData.getConsumerComponentData().getName());
		consumerCoordinationComponent.setLocation(componentData.getConsumerComponentData().getLocation());
		consumerCoordinationComponent.getRoles().add(componentData.getParticipantName());
		
		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		consumerCoordinationComponents.put(bpmn2Participant, consumerCoordinationComponent);
		
		return consumerCoordinationComponent;
	}
	
	private <T extends BusinessComponent> BusinessComponent createProviderComponent (ComponentData componentData, Class<T> businessComponentClass) throws ChoreographyArchitectureGeneratorException{
		if (componentData == null){
			return null;
		}
		BusinessComponent businessComponent;
		
		if (businessComponentClass.isAssignableFrom(WebServiceComponent.class)){
			businessComponent = ChorarchFactory.eINSTANCE.createWebServiceComponent();
		}else if (businessComponentClass.isAssignableFrom(RestServiceComponent.class)){
			businessComponent = ChorarchFactory.eINSTANCE.createRestServiceComponent();
		}else if (businessComponentClass.isAssignableFrom(ThingComponent.class)){
			businessComponent = ChorarchFactory.eINSTANCE.createThingComponent();
		}else{
			throw new ChoreographyArchitectureGeneratorException("Error creating '"+componentData.getName()+"' Business Component"); 
		}
		
		businessComponent.setName(componentData.getName());
		businessComponent.getRoles().add(componentData.getParticipantName());
		businessComponent.setUri(componentData.getLocation());
		
		Participant bpmn2Participant = ChoreographyArchitectureGeneratorUtils.getParticipant(bpmn2ChoreographyLoader.getChoreographies(), componentData.getParticipantName());
		providerComponents.put(bpmn2Participant, businessComponent);
		return businessComponent;
	}

	private void createComponentDependency (Component providerComponent, Component consumerComponent){
		ProviderInterface providerInterface = ChorarchFactory.eINSTANCE.createProviderInterface();
		providerComponent.getInterfaces().add(providerInterface);
				
		ConsumerInterface consumerInterface= ChorarchFactory.eINSTANCE.createConsumerInterface();
		consumerComponent.getInterfaces().add(consumerInterface);
		
		providerInterface.getServiceProvided().add(consumerInterface);
		
	}
	
	private void createClientComponentChain(
			ClientAppComponent clientComponent,
			BindingComponent bindingComponent,
			SecurityFilter securityComponent,
			Adapter adapterComponent,
			ProsumerCoordinationDelegate prosumerCoordinationComponent) throws ChoreographyArchitectureGeneratorException{
		/*
		if (securityComponent == null && bindingComponent == null){
			// connect the prosumer coordination component to the client component 
			createComponentDependency(prosumerCoordinationComponent, clientComponent);
		}else if (securityComponent != null && bindingComponent == null){
			// connect the prosumer coordination component to the security component 
			createComponentDependency(prosumerCoordinationComponent, securityComponent);
			// connect the security component to the client component
			createComponentDependency(securityComponent, clientComponent);
		}else if (securityComponent == null && bindingComponent != null){
			// connect the prosumer coordination component to the binding component 
			createComponentDependency(prosumerCoordinationComponent, bindingComponent);
			// connect the binding component to the client component
			createComponentDependency(bindingComponent, clientComponent);
		}else if (securityComponent != null && bindingComponent != null){
			// connect the prosumer coordination component to the security component 
			createComponentDependency(prosumerCoordinationComponent, securityComponent);
			// connect the security component to the binding component
			createComponentDependency(securityComponent, bindingComponent);
			// connect the binding component to the client component
			createComponentDependency(bindingComponent, clientComponent);
		}*/
		
		if (bindingComponent == null && securityComponent == null && adapterComponent == null){
			// connect the client client component to the coordination component 
			createComponentDependency(prosumerCoordinationComponent, clientComponent);
		} else if (bindingComponent == null && securityComponent == null && adapterComponent != null){
			// connect the client client component to the adapter component 
			createComponentDependency(adapterComponent, clientComponent);
			// connect the adapter component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, adapterComponent);			
		} else if (bindingComponent == null && securityComponent != null && adapterComponent == null){
			// connect the client client component to the security component 
			createComponentDependency(securityComponent, clientComponent);
			// connect the security component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, securityComponent);
		} else if (bindingComponent == null && securityComponent != null && adapterComponent != null){
			// connect the client client component to the security component 
			createComponentDependency(securityComponent, clientComponent);
			// connect the security component to the adapter component
			createComponentDependency(adapterComponent, securityComponent);	
			// connect the adapter component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, adapterComponent);
		} else if (bindingComponent != null && securityComponent == null && adapterComponent == null){
			// connect the client component to the binding component 
			createComponentDependency(bindingComponent, clientComponent);
			// connect the binding component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, bindingComponent);
		} else if (bindingComponent != null && securityComponent == null && adapterComponent != null){
			// connect the client component to the binding component 
			createComponentDependency(bindingComponent, clientComponent);
			// connect the binding component to the adapter component
			createComponentDependency(adapterComponent, bindingComponent);
			// connect the adapter component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, adapterComponent);
		} else if (bindingComponent != null && securityComponent != null && adapterComponent == null){
			// connect the client component to the binding component 
			createComponentDependency(bindingComponent, clientComponent);
			// connect the binding component to the security component 
			createComponentDependency(securityComponent, bindingComponent);		
			// connect the binding component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, securityComponent);
		} else if (bindingComponent != null && securityComponent != null && adapterComponent != null){
			// connect the client component to the binding component 
			createComponentDependency(bindingComponent, clientComponent);
			// connect the binding component to the security component 
			createComponentDependency(securityComponent, bindingComponent);	
			// connect the security component to the adapter component
			createComponentDependency(adapterComponent, securityComponent);	
			// connect the adapter component to the coordination component
			createComponentDependency(prosumerCoordinationComponent, adapterComponent);
		}        
		
	}

	private void createConsumerCoordinationComponentChain(
			ConsumerCoordinationDelegate consumerCoordinationComponent,
			ProsumerCoordinationDelegate prosumerCoordinationComponent) throws ChoreographyArchitectureGeneratorException{
		// connect the prosumer coordination component to the consumer coordination component 
		createComponentDependency(consumerCoordinationComponent, prosumerCoordinationComponent);
	}
	
	private void createProviderComponentChain(
			BusinessComponent providerComponent, 
			BindingComponent bindingComponent, 
			SecurityFilter securityComponent,
			Adapter adapterComponent)throws ChoreographyArchitectureGeneratorException{
		
		/*
		if (securityComponent != null && bindingComponent == null){
			// connect the security component to the provider component
			createComponentDependency(providerComponent, securityComponent);
		}else if (securityComponent == null && bindingComponent != null){
			// connect the binding component to the provider component
			createComponentDependency(providerComponent, bindingComponent);
		}else if (securityComponent != null && bindingComponent != null){
			// connect the security component to the binding component
			createComponentDependency(bindingComponent, securityComponent);
			// connect the binding component to the provider component
			createComponentDependency(providerComponent, bindingComponent);
		}
		*/
		
		
		
		if (bindingComponent == null && securityComponent == null && adapterComponent != null){
			// connect the adapter component to the provider component
			createComponentDependency(providerComponent, adapterComponent);			
		} else if (bindingComponent == null && securityComponent != null && adapterComponent == null){
			// connect the security component to the provider component
			createComponentDependency(providerComponent, securityComponent);
		} else if (bindingComponent == null && securityComponent != null && adapterComponent != null){
			// connect the adapter component to the security component
			createComponentDependency(securityComponent, adapterComponent);	
			// connect the security component to the provider component
			createComponentDependency(providerComponent, securityComponent);
		} else if (bindingComponent != null && securityComponent == null && adapterComponent == null){
			// connect the binding component to the provider component
			createComponentDependency(providerComponent, bindingComponent);
		} else if (bindingComponent != null && securityComponent == null && adapterComponent != null){
			// connect the adapter component to the binding component
			createComponentDependency(bindingComponent, adapterComponent);
			// connect the binding component to the provider component
			createComponentDependency(providerComponent, bindingComponent);
		} else if (bindingComponent != null && securityComponent != null && adapterComponent == null){
			// connect the security component to the binding component
			createComponentDependency(bindingComponent, securityComponent);			
			// connect the binding component to the provider component
			createComponentDependency(providerComponent, bindingComponent);
		} else if (bindingComponent != null && securityComponent != null && adapterComponent != null){
			// connect the adapter component to the security component
			createComponentDependency(securityComponent, adapterComponent);
			// connect the security component to the binding component
			createComponentDependency(bindingComponent, securityComponent);
			// connect the binding component to the provider component
			createComponentDependency(providerComponent, bindingComponent);
		}        
		
	}

}
