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
package eu.chorevolution.transformations.generativeapproach.choreographyspecificationgenerator.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import eu.chorevolution.datamodel.Choreography;
import eu.chorevolution.modelingnotations.chorarch.ChorArchModel;
import eu.chorevolution.modelingnotations.chorarch.Component;
import eu.chorevolution.modelingnotations.chorarch.ConsumerInterface;
import eu.chorevolution.modelingnotations.chorarch.Interface;
import eu.chorevolution.modelingnotations.chorarch.ProviderInterface;
import eu.chorevolution.modelingnotations.chorarch.impl.ChorarchPackageImpl;
import eu.chorevolution.transformations.generativeapproach.choreographyspecificationgenerator.ChoreographySpecificationGeneratorException;

/**
 * This class provides static utility methods for manipulate a ChorArch model
 */
public class ChoreographySpecificationGeneratorUtils {

	private static final String TEMPFILE_SUFFIX = "choreographyspecificationgenerator";
	private static final String CHOREOARCH_FILE_EXTENSION = ".chorarch";
	private static final String CHOREOSPEC_FILE_EXTENSION = ".choreospec";

	public static ChorArchModel loadChorArchModel(final byte[] choreographyArchitectureContent)
			throws ChoreographySpecificationGeneratorException {
		File choreographyArchitectureFile;
		try {
			choreographyArchitectureFile = File.createTempFile(TEMPFILE_SUFFIX,
					CHOREOARCH_FILE_EXTENSION);
			IOUtils.write(choreographyArchitectureContent, FileUtils.openOutputStream(choreographyArchitectureFile));
		} catch (IOException e1) {
			throw new ChoreographySpecificationGeneratorException(
					"Internal Error while creating the Choreography Architecture");
		}

		URI choreographyArchitectureURI = URI.createURI(choreographyArchitectureFile.toURI().toString());

		ChorarchPackageImpl.init();
		Resource resource = new XMIResourceFactoryImpl().createResource(choreographyArchitectureURI);

		try {
			// load the resource
			resource.load(null);

		} catch (IOException e) {
			throw new ChoreographySpecificationGeneratorException(
					"Error to load the resource: " + resource.getURI().toFileString());
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(choreographyArchitectureFile);
		}

		return (ChorArchModel) resource.getContents().get(0);
	}

	public static byte[] getChoreographySpecificationContent(final Choreography choreographySpecification)
			throws ChoreographySpecificationGeneratorException {

		File choreoArchFile;
		try {
			choreoArchFile = FileUtils.getTempDirectory().createTempFile(TEMPFILE_SUFFIX, CHOREOSPEC_FILE_EXTENSION);
		} catch (IOException e1) {
			throw new ChoreographySpecificationGeneratorException(
					"Internal Error while creating the Choreography Specification Model");
		}
		byte[] choreoArchContent;
		try {
			 choreoArchContent = choreographySpecification.getXML().getBytes(Charset.defaultCharset());
		} catch (JAXBException | XMLStreamException e) {
			throw new ChoreographySpecificationGeneratorException(
					"Internal Error while reading the Choreography Specification Model");
		}
/*
		try {
			choreoArchContent = choreographySpecification.getXML().getBytes(Charset.defaultCharset());
			FileUtils.writeStringToFile(choreoArchFile, choreographySpecification.getXML(), Charset.defaultCharset());

			choreoArchContent = FileUtils.readFileToByteArray(choreoArchFile);
			

		} catch (IOException | JAXBException | XMLStreamException e) {
			throw new ChoreographySpecificationGeneratorException(
					"Internal Error while reading the Choreography Specification Model");
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(choreoArchFile);
		}
*/
		return choreoArchContent;

	}

	public static List<ConsumerInterface> getRequiredInterface(Component component) {
		List<ConsumerInterface> requiredInterfaces = new ArrayList<ConsumerInterface>();
		for (Interface componentInterface : component.getInterfaces()) {
			if (componentInterface instanceof ConsumerInterface
					&& !((ConsumerInterface) componentInterface).getServiceRequired().isEmpty()) {
				requiredInterfaces.add((ConsumerInterface) componentInterface);
			}

		}
		return requiredInterfaces;
	}

	public static List<Interface> getProvidedInterface(Component component) {
		List<Interface> providedInterfaces = new ArrayList<Interface>();
		for (Interface componentInterface : component.getInterfaces()) {
			if (componentInterface instanceof ProviderInterface
					&& !((ProviderInterface) componentInterface).getServiceProvided().isEmpty()) {
				providedInterfaces.add(componentInterface);
			}

		}
		return providedInterfaces;
	}

	public static Component getComponentHasInterface(List<Component> components, Interface requiredInterface,
			Component excludeComponent) throws ChoreographySpecificationGeneratorException {
		for (Component component : components) {
			if (!component.equals(excludeComponent) && getProvidedInterface(component).contains(requiredInterface)) {
				return component;
			}
		}

		throw new ChoreographySpecificationGeneratorException(
				"None Choreography Architecture Component founded that has a provided Interface for the input required Interface");

	}

	private static String createXMLFile(final Choreography choreographySpecification) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Choreography.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(choreographySpecification, stringWriter);
		return stringWriter.toString();
	}

}
