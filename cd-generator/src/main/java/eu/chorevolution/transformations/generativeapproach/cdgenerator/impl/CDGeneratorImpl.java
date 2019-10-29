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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.wsdl.Definition;

import org.apache.commons.io.FileUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGenerator;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ArtifactData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.CD;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ChoreographyData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.ProjectionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.TaskCorrelationData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.WSDLDefinitionData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPELUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.BPMNUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.CDGenerationUtils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.Utils;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.util.WSDLUtils;

public class CDGeneratorImpl implements CDGenerator {

	
	private static final String ARTIFACTS_PREFIX = "artifacts";
	private static final String ARTIFACTS_SUFFIX = "Artifacts";
	private static final String DEPLOY_FILE_NAME = "deploy";
	private static final String PROPERTIES_FILE_NAME = "properties";
	private static final String PROPERTIES_ALIASES_FILE_NAME = "propertiesAliases";
	
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(CDGeneratorImpl.class);
	
	@Override
	public CD generateCDProsumer(List<String> prosumersParticipantsNames,
			List<String> clientProsumersParticipantsNames, ProjectionData projectionData,
			List<WSDLData> wsdlData) throws CDGeneratorException {
		
		// log execution flow
		LOGGER.entry("CD Prosumer Participant name: "+projectionData.getParticipantName());
		// log cdParticipantName
		LOGGER.info("Creating CD Prosumer for Participant: "+ projectionData.getParticipantName());		
		
		String cdProsumerParticipantName = projectionData.getParticipantName();
		String cdProsumerParticipantNameNormalized = WSDLUtils
				.formatParticipantNameForWSDL(cdProsumerParticipantName);
		String cdName = projectionData.getCdName();
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String destination = Utils.createTemporaryFolderFromMillisAndGetPath();
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
		String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);		
		CD cd = new CD();
		cd.setName(cdName);
		try {
			// create destination folder
			FileUtils.forceMkdir(new File(destinationFolder));
			// get destinationFolderAbsolutePath
			String destinationFolderAbsolutePath = new File(destinationFolder).getAbsolutePath()
					.replace(File.separatorChar+".","");
			
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdProsumerParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdWSDLDefinitionData = wsdlDefinitions.remove(cdProsumerParticipantName);	
			
			// create WSDL prosumer part
			Definition prosumerPartWSDLDefinition = CDGenerationUtils
					.generateCDProsumerPartWSDL(choreographyData, choreographytns,
							cdProsumerParticipantName);
			// write WSDL prosumer part to file
			WSDLUtils.writeWSDLDefinitionToFile(prosumerPartWSDLDefinition, destinationFolder, 
					cdProsumerParticipantNameNormalized);
			// set cd WSDL consumer part
			cd.setProsumerwsdl(WSDLUtils.readWSDLtoByteArray(destinationFolder,
					cdProsumerParticipantNameNormalized));
			
			// create prosumerPartWSDLDefinitionData
			WSDLDefinitionData prosumerPartWSDLDefinitionData = new WSDLDefinitionData();
			prosumerPartWSDLDefinitionData.setPrefix(cdProsumerParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setParticipantName(projectionData.getParticipantName());
			prosumerPartWSDLDefinitionData.setWsdlFileName(cdProsumerParticipantNameNormalized);
			prosumerPartWSDLDefinitionData.setWsdl(prosumerPartWSDLDefinition);
			
			// create list of WSDLDefinitionData corresponding to the wsdl of 
			// all artifacts(including prosumer part) 
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);
			// add WSDLDefinitionData of consumer part to wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.put(cdProsumerParticipantName, prosumerPartWSDLDefinitionData);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdProsumerParticipantName, choreographyData, wsdlDefinitionsAllArtifacts,
					cdWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);
			
			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);
						
			// set WSDL file name of cdWSDLDefinitionData
			cdWSDLDefinitionData.setWsdlFileName(cdName);
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdWSDLDefinitionData.getWsdl(), destinationFolder, cdName);
			
			// write all the WSDL including cd prosumer part WSDL
			WSDLUtils
				.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts, destinationFolder);
			
			// create artifactData
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
											
			BPELData bpelData = CDGenerationUtils.createCDProsumerBpelProcessFromChoreography(
					cdProsumerParticipantName, cdName, choreographytns, prosumersParticipantsNames,
					clientProsumersParticipantsNames, BPMNUtils
					.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitions, cdWSDLDefinitionData, 
					prosumerPartWSDLDefinitionData, artifactData);
					
			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);			
			
			// write bpel process to file
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			// create deploy
			Document deployDocument = CDGenerationUtils.generateCDDeployDocument(bpelData.getProcess()
					.getPartnerLinks().getChildren(), wsdlDefinitionsAllArtifacts, 
					cdWSDLDefinitionData, bpelData.getProcess().getTargetNamespace(), cdName, artifactsWSDL);
					
			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);				

			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdProsumerParticipantName, cdName,
					destinationFolder);		

			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
			cd.setArtifact(Utils.createTarGzOfDirectoryContent(destinationFolderAbsolutePath, destination,
					cdName));
			
			// delete destination folder
			Utils.deleteFolder(destination);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("IOException into generateCDProsumer see log file for details ");
		}
		
		// log execution flow
		LOGGER.exit();
		return cd;
	}

	@Override
	public CD generateCDClient(List<TaskCorrelationData> tasksCorrelationData, ProjectionData projectionData, 
			List<WSDLData> wsdlData) throws CDGeneratorException {
		
		// log execution flow
		LOGGER.entry("CD Client Participant name: "+projectionData.getParticipantName());
		// log cdParticipantName
		LOGGER.info("Creating CD Client for Participant: "+ projectionData.getParticipantName());	
		String cdClientParticipantName = projectionData.getParticipantName();
		String cdName = projectionData.getCdName();
		String artifactsFileName = new StringBuilder(cdName).append(ARTIFACTS_SUFFIX).toString();
		String destination = Utils.createTemporaryFolderFromMillisAndGetPath();
		String destinationFolder = new StringBuilder(destination).append(File.separatorChar).append(cdName)
				.append(File.separatorChar).toString();
		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);		
		String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
		CD cd = new CD();
		cd.setName(cdName);
		try {
			// create destinationFolder
			FileUtils.forceMkdir(new File(destinationFolder));
			// get destinationFolderAbsolutePath
			String destinationFolderAbsolutePath = new File(destinationFolder).getAbsolutePath()
					.replace(File.separatorChar+".","");	
			// convert all the WSDLData into WSDLDefinitionData
			HashMap<String, WSDLDefinitionData> wsdlDefinitions = WSDLUtils
					.getWSDLDefinitionDataOfWSDLs(wsdlData, cdClientParticipantName, cdName);
			// get WSDLDefinitionData of the CD and remove it from wsdlDefinitions
			WSDLDefinitionData cdClientWSDLDefinitionData = wsdlDefinitions.remove(cdClientParticipantName);	
			
			// create list of WSDLDefinitionData corresponding to the wsdl of all artifacts
			HashMap<String, WSDLDefinitionData> wsdlDefinitionsAllArtifacts = new HashMap<>();
			// copy all the elements of wsdlDefinitions into wsdlDefinitionsAllArtifacts
			wsdlDefinitionsAllArtifacts.putAll(wsdlDefinitions);
			
			// create artifacts WSDL
			Document artifactsWSDL = CDGenerationUtils.generateCDArtifactsDocument(choreographytns, cdName, 
					cdClientParticipantName, choreographyData, wsdlDefinitionsAllArtifacts,
					cdClientWSDLDefinitionData);
			WSDLUtils.writeWSDLDocumentToFile(artifactsWSDL, destinationFolder, artifactsFileName);

			// create properties WSDL
			Document propertiesWSDL = CDGenerationUtils.createPropertiesWSDLDocument(choreographytns);
			WSDLUtils.writeWSDLDocumentToFile(propertiesWSDL, destinationFolder, PROPERTIES_FILE_NAME);			
						
			// write WSDL cd to file
			WSDLUtils.writeWSDLDefinitionToFile(cdClientWSDLDefinitionData.getWsdl(), destinationFolder,
					cdName);
			
			// write all the WSDL including cd consumer part WSDL
			WSDLUtils
				.writeWSDLDefinitionsToDestinationFolder(wsdlDefinitionsAllArtifacts, destinationFolder);

			// create artifactData
			ArtifactData artifactData = new ArtifactData();
			artifactData.setArtifact(artifactsWSDL);
			artifactData.setFileName(artifactsFileName);
			artifactData.setNamespace(WSDLUtils.getTargetNamespace(artifactsWSDL));
			artifactData.setPrefix(ARTIFACTS_PREFIX);
			
			BPELData bpelData = CDGenerationUtils.createCDClientBpelProcessFromChoreography(
					cdClientParticipantName, cdName, choreographytns, tasksCorrelationData, 
					BPMNUtils.getMainChoreographyFromChoreographyData(choreographyData), 
					choreographyData.getChoreographies(), wsdlDefinitionsAllArtifacts,
					cdClientWSDLDefinitionData, artifactData);

			// create properties Aliases WSDL
			Document propertiesAliasesWSDL = CDGenerationUtils
					.createPropertiesAliasesWSDLDocument(choreographytns, bpelData.getPropertyAliasesData());
			WSDLUtils.writeWSDLDocumentToFile(propertiesAliasesWSDL, destinationFolder,
					PROPERTIES_ALIASES_FILE_NAME);				
			
			// write bpel process to file
			BPELUtils.writeBPELprocessToFile(bpelData, destinationFolder, cdName);

			// create deploy
			Document deployDocument = CDGenerationUtils.generateCDDeployDocument(bpelData.getProcess()
					.getPartnerLinks().getChildren(), wsdlDefinitions, cdClientWSDLDefinitionData, 
					bpelData.getProcess().getTargetNamespace(), cdName, artifactsWSDL);

			Utils.writeXMLDocumentToFile(deployDocument, destinationFolder, DEPLOY_FILE_NAME);
			
			// create set invocation address elements
			CDGenerationUtils.createSetInvocationAddressElements(cdClientParticipantName, cdName,
					destinationFolder);
	
			// create eclipse project files 
			CDGenerationUtils.createEclipseProjectFiles(cdName, destinationFolder);	
			
			cd.setArtifact(Utils.createTarGzOfDirectoryContent(destinationFolderAbsolutePath, destination,
					cdName));
			
			// delete destination folder
			Utils.deleteFolder(destination);
		}catch (IOException e){
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("IOException into generateCDClient see log file for details ");			
		}	
		// log execution flow
		LOGGER.exit();		
		return cd;
	}

	@Override
	public byte[] generateCDWSDL(ProjectionData projectionData) {

		// log execution flow
		LOGGER.entry("CD participant name: "+projectionData.getParticipantName());
		// log cdParticipantName
		LOGGER.info("Creating CD WSDL for Participant: "+ projectionData.getParticipantName());
		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);
		String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
		Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDProsumerWSDL(choreographyData,
				choreographytns, projectionData.getParticipantName());		
		// log execution flow
		LOGGER.exit();	
		return WSDLUtils.getWSDLByteArrayFromDefinition(prosumerWSDLdefinition);
	}

	@Override
	public byte[] generateCDClientWSDL(ProjectionData projectionData,
			List<TaskCorrelationData> tasksCorrelationData) {

		// log execution flow
		LOGGER.entry("CD Client participant name: "+projectionData.getParticipantName());
		// log cdParticipantName
		LOGGER.info("Creating CD Client WSDL for Participant: "+projectionData.getParticipantName());		
		ChoreographyData choreographyData = BPMNUtils.readChoreographyData(projectionData);
		String choreographytns = BPMNUtils.getTargetNamespaceFromChoreographyData(choreographyData);
		Definition prosumerWSDLdefinition = CDGenerationUtils.generateCDClientWSDL(choreographyData,
				choreographytns, projectionData.getParticipantName(), tasksCorrelationData);
		// log execution flow
		LOGGER.exit();	
		return WSDLUtils.getWSDLByteArrayFromDefinition(prosumerWSDLdefinition);
	}
	
}
