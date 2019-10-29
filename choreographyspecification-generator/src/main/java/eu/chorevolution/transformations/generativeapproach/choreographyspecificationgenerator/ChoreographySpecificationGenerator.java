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
package eu.chorevolution.transformations.generativeapproach.choreographyspecificationgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.chorevolution.datamodel.Choreography;
import eu.chorevolution.datamodel.ChoreographyService;
import eu.chorevolution.datamodel.DeployableService;
import eu.chorevolution.datamodel.ExistingService;
import eu.chorevolution.datamodel.PackageType;
import eu.chorevolution.datamodel.ServiceDependency;
import eu.chorevolution.datamodel.ServiceGroup;
import eu.chorevolution.datamodel.ServiceType;
import eu.chorevolution.modelingnotations.chorarch.Adapter;
import eu.chorevolution.modelingnotations.chorarch.AdditionalComponent;
import eu.chorevolution.modelingnotations.chorarch.BindingComponent;
import eu.chorevolution.modelingnotations.chorarch.BusinessComponent;
import eu.chorevolution.modelingnotations.chorarch.ChorArchModel;
import eu.chorevolution.modelingnotations.chorarch.ClientAppComponent;
import eu.chorevolution.modelingnotations.chorarch.Component;
import eu.chorevolution.modelingnotations.chorarch.ConsumerCoordinationDelegate;
import eu.chorevolution.modelingnotations.chorarch.ConsumerInterface;
import eu.chorevolution.modelingnotations.chorarch.ProsumerCoordinationDelegate;
import eu.chorevolution.modelingnotations.chorarch.SecurityFilter;
import eu.chorevolution.transformations.generativeapproach.choreographyspecificationgenerator.util.ChoreographySpecificationGeneratorUtils;

public class ChoreographySpecificationGenerator {

	private static final int DEPLOYABLE_SERVICE_NUMBER_OF_INSTANCES = 1;

	private ChorArchModel choreographyArchitectureModel;
	private Choreography choreographySpecification;
	private ServiceGroup serviceGroup;

	public ChoreographySpecificationGeneratorResponse generate(
			final ChoreographySpecificationGeneratorRequest choreographySpecificationGeneratorRequest)
			throws ChoreographySpecificationGeneratorException {
		// load the Choreography Architecture model

		this.choreographySpecification = new Choreography();
		this.serviceGroup = new ServiceGroup();
		List<ServiceGroup> serviceGroups = new ArrayList<ServiceGroup>();
		serviceGroups.add(serviceGroup);

		List<ChoreographyService> choreographyServices = new ArrayList<ChoreographyService>();
		serviceGroup.setServices(choreographyServices);

		this.choreographySpecification.setServiceGroups(serviceGroups);

		choreographyArchitectureModel = ChoreographySpecificationGeneratorUtils
				.loadChorArchModel(choreographySpecificationGeneratorRequest.getChoreographyArchitectureContent());

		// generate Choreography Specification model
		createChoreographySpecification();

		// create the result
		ChoreographySpecificationGeneratorResponse choreographySpecificationGeneratorResponse = new ChoreographySpecificationGeneratorResponse();
		choreographySpecificationGeneratorResponse.setChoreographySpecification(
				ChoreographySpecificationGeneratorUtils.getChoreographySpecificationContent(choreographySpecification));

		return choreographySpecificationGeneratorResponse;
	}

	

	private void createChoreographySpecification() throws ChoreographySpecificationGeneratorException {
		Map<Component, ChoreographyService> allServiceSpecification = new HashMap<Component, ChoreographyService>();
		for (Component component : choreographyArchitectureModel.getComponents()) {

			if (component instanceof BusinessComponent && !(component instanceof ClientAppComponent)) {
				// legacy service
				ExistingService legacyServiceSpec = new ExistingService();
				legacyServiceSpec.setName(component.getName());
				legacyServiceSpec.setRoles(component.getRoles());
				legacyServiceSpec.setUrl(((BusinessComponent) component).getUri());
				allServiceSpecification.put(component, legacyServiceSpec);
				serviceGroup.getServices().add(legacyServiceSpec);
			} else if (component instanceof AdditionalComponent) {
				// deployable service
				DeployableService deployableServiceSpec = new DeployableService();
				deployableServiceSpec.setName(component.getName());

				deployableServiceSpec.setRoles(component.getRoles());
				deployableServiceSpec.setPackageUrl(((AdditionalComponent) component).getLocation());

				// TODO for now we set COORDINATION_DELEGATE service Type
				// checking
				// if the COORDINATION_DELEGATE
				// service type is correct for the binding component, for the
				// rest component is correct
				deployableServiceSpec.setInstances(DEPLOYABLE_SERVICE_NUMBER_OF_INSTANCES);

				if (component instanceof ConsumerCoordinationDelegate) {
					deployableServiceSpec.setServiceType(ServiceType.COORDINATION_DELEGATE);
					deployableServiceSpec.setPackageType(PackageType.WAR);
				} else if (component instanceof ProsumerCoordinationDelegate) {
					deployableServiceSpec.setServiceType(ServiceType.COORDINATION_DELEGATE);
					deployableServiceSpec.setPackageType(PackageType.ODE);
				} else if (component instanceof BindingComponent) {
					deployableServiceSpec.setServiceType(ServiceType.BINDING_COMPONENT);
					deployableServiceSpec.setPackageType(PackageType.WAR);
				} else if (component instanceof SecurityFilter) {
					if (((SecurityFilter) component).isGlobal()){
						deployableServiceSpec.setServiceType(ServiceType.GLOBAL_SECURITY_FILTER);
					}else{
						deployableServiceSpec.setServiceType(ServiceType.SECURITY_FILTER);
					}
					
					deployableServiceSpec.setPackageType(PackageType.WAR);
				} else if (component instanceof Adapter) {
					deployableServiceSpec.setServiceType(ServiceType.PROTOCOL_ADAPTER);
					deployableServiceSpec.setPackageType(PackageType.WAR);
				} else {
					deployableServiceSpec.setServiceType(ServiceType.GENERIC_SERVICE);
					deployableServiceSpec.setPackageType(PackageType.WAR);
				}

				deployableServiceSpec.setDependencies(new ArrayList<ServiceDependency>());
				allServiceSpecification.put(component, deployableServiceSpec);
				serviceGroup.getServices().add(deployableServiceSpec);
			}
		}

		// set all dependencies
		for (Map.Entry<Component, ChoreographyService> entry : allServiceSpecification.entrySet()) {
			Component componentDependency = entry.getKey();

			for (ConsumerInterface requiredInterface : ChoreographySpecificationGeneratorUtils
					.getRequiredInterface(componentDependency)) {
				Component providedInterface = ChoreographySpecificationGeneratorUtils.getComponentHasInterface(
						choreographyArchitectureModel.getComponents(), requiredInterface.getServiceRequired().get(0),
						componentDependency);

				ServiceDependency serviceDependency = new ServiceDependency();
				serviceDependency.setServiceSpecName(providedInterface.getName());
				// TODO check the set .get(0) seams is not correct
				if (!providedInterface.getRoles().isEmpty()) {
					serviceDependency.setServiceSpecRole(providedInterface.getRoles().get(0));
				}

				if (entry.getValue() instanceof DeployableService) {
					DeployableService s = (DeployableService) entry.getValue();
					s.getDependencies().add(serviceDependency);
				}

			}

		}

	}
}
