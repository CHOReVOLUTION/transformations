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
package eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.CDConsumerPartGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.CDConsumerPartData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.GeneratingJavaData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.OperationData;
import eu.chorevolution.transformations.generativeapproach.cdconsumerpartgenerator.model.WSDLInfo;

public class Utility {

	private final static Logger LOGGER = LoggerFactory.getLogger(Utility.class);

	private static final String NAME_LABEL = "name";
	private static final String COLON = ":";
	private static final String TYPE_LABEL = "type";
	private final static String TARGZ_EXTENSION = ".tar.gz";

	public static byte[] createTarGzOfDirectory(String directoryPath, String destination, String tarGzName) {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		byte[] bytes = null;
		String tarGzPath = new StringBuilder(destination).append(File.separatorChar).append(tarGzName).append(TARGZ_EXTENSION).toString();
		try {
			File targz = new File(tarGzPath);
			fOut = new FileOutputStream(targz);
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);
			addFileToTarGz(tOut, directoryPath, "");
			tOut.finish();
			tOut.close();
			gzOut.close();
			bOut.close();
			fOut.close();
			bytes = FileUtils.readFileToByteArray(targz);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into createTarGzOfDirectory see log file for details ");
		}
		return bytes;
	}

	private static void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base) {
		File f = new File(path);
		String entryName = base + f.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
		try {
			tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			tOut.putArchiveEntry(tarEntry);
			if (f.isFile()) {
				IOUtils.copy(new FileInputStream(f), tOut);
				tOut.closeArchiveEntry();
			} else {
				tOut.closeArchiveEntry();
				File[] children = f.listFiles();
				if (children != null) {
					for (File child : children) {
						addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into addFileToTarGz see log file for details ");
		}
	}

	public static String createTemporaryFolderFromMillisAndGetPath() {

		String systemTempDirectoryPath = FileUtils.getTempDirectoryPath();
		if (systemTempDirectoryPath.charAt(systemTempDirectoryPath.length() - 1) != File.separatorChar) {
			systemTempDirectoryPath = systemTempDirectoryPath + File.separatorChar;
		}
		String destDir = new StringBuilder(systemTempDirectoryPath + System.currentTimeMillis()).toString().replaceAll("\\s", "_");
		try {
			FileUtils.forceMkdir(new File(destDir));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into createDestinationFolderAndGetPath see log file for details ");
		}
		return destDir;
	}

	public static void deleteFolder(String path) {

		try {
			FileUtils.forceDelete(new File(path));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			// throw new CDConsumerPartGeneratorException("Exception into
			// deleteFolder see log file for details ");
		}

	}

	public static String createArtifactName(String name) {

		return StringUtils.lowerCase(StringUtils.remove(StringUtils.deleteWhitespace(name), "-"));
	}

	public static String createName(String name) {

		return StringUtils.remove(StringUtils.deleteWhitespace(name), "-");
	}

	public static void createProjectFolders(String destinationRootPath) {

		String resoucesFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "resources";
		String javaFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "java";
		String metainfFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "META-INF";
		String springFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "spring";
		try {
			FileUtils.forceMkdir(new File(resoucesFolderPath));
			FileUtils.forceMkdir(new File(javaFolderPath));
			FileUtils.forceMkdir(new File(metainfFolderPath));
			FileUtils.forceMkdir(new File(springFolderPath));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into createProjectFolders see log file for details ");
		}
	}

	public static String createJavaPackageFolder(String projectFolderRoot, String packageName) {

		String packageNamePath = packageName.replace(".", File.separator);
		String sourcePath = projectFolderRoot + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "java" + File.separatorChar + packageNamePath;
		try {
			FileUtils.forceMkdir(new File(sourcePath));
			return sourcePath;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CDConsumerPartGeneratorException("Exception into createProjectFolders see log file for details ");
		}
	}

	public static String deleteSpecialChar(String s) {
		StringBuilder result = new StringBuilder();
		boolean firstChar = true;

		for (char ch : s.toCharArray()) {
			if (firstChar) {
				firstChar = false;
				if (Character.isJavaIdentifierStart(ch)) {
					result.append(ch);
				}
				continue;
			}
			if (Character.isJavaIdentifierPart(ch)) {
				result.append(ch);
			}
		}
		return result.toString();
	}

	public static GeneratingJavaData createGeneratingJavaData(CDConsumerPartData cdConsumerPartData, WSDLInfo wsdlInfo) {
		GeneratingJavaData result = new GeneratingJavaData();
		result.setBasePackageName(cdConsumerPartData.getPackagename());
		result.setCdName(cdConsumerPartData.getName());
		result.setPortName(wsdlInfo.getPortName());
		PortType portType = (PortType) wsdlInfo.getDefinition().getAllPortTypes().get(wsdlInfo.getDefinition().getAllPortTypes().keySet().toArray()[0]);
		Types types = wsdlInfo.getDefinition().getTypes();
		Schema schema = (Schema) types.getExtensibilityElements().get(0);
		for (Operation operation : (List<Operation>) portType.getOperations()) {
			Part inputPart = (Part) operation.getInput().getMessage().getParts().get(operation.getInput().getMessage().getParts().keySet().toArray()[0]);
			Part outputPart = (Part) operation.getOutput().getMessage().getParts().get(operation.getOutput().getMessage().getParts().keySet().toArray()[0]);
			String inputMessageType = getMessageElementNameType(schema, inputPart.getElementName().getLocalPart());
			String outputMessageType = getMessageElementNameType(schema, outputPart.getElementName().getLocalPart());

			OperationData operationData = new OperationData();
			operationData.setName(operation.getName());
			operationData.setInputMessageType(StringUtils.capitalize(inputMessageType));
			operationData.setOutputMessageType(StringUtils.capitalize(outputMessageType));

			if (operation.getName().startsWith("send")) {
				result.addInitiatingSendOperation(operationData);
				String inputBusinessMessageType = getMessageDataNameTypeFromMessageElementType(schema, outputMessageType, "inputMessageData");
				operationData.setInputBusinessMessageType(StringUtils.capitalize(inputBusinessMessageType));

			} else if (operation.getName().startsWith("receive")) {
				result.addInitiatingReceiveOperation(operationData);
				//it is correct to use inputMessageType because the outputMessageData is referred to the related choreography task 
				String outputBusinessMessageType = getMessageDataNameTypeFromMessageElementType(schema, inputMessageType, "outputMessageData");
				operationData.setOutputBusinessMessageType(StringUtils.capitalize(outputBusinessMessageType));

			} else {
				result.addReceivingOperation(operationData);
				String inputBusinessMessageType = getMessageDataNameTypeFromMessageElementType(schema, inputMessageType, "inputMessageData");
				operationData.setInputBusinessMessageType(StringUtils.capitalize(inputBusinessMessageType));
				String outputBusinessMessageType = getMessageDataNameTypeFromMessageElementType(schema, outputMessageType, "outputMessageData");
				operationData.setOutputBusinessMessageType(StringUtils.capitalize(outputBusinessMessageType));
				
			}
		}
		return result;

	}

	private static String getMessageElementNameType(Schema schema, String messageElementName) {

		Node node = schema.getElement().getFirstChild();
		while (node != null) {
			if (node.getAttributes() != null && node.getAttributes().getNamedItem(NAME_LABEL).getNodeValue().equals(messageElementName)) {
				if (node.getAttributes().getNamedItem(TYPE_LABEL) != null) {
					if (node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue().contains(COLON))
						return StringUtils.split(node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue(), COLON)[1];
					else
						return node.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue();
				}
			}
			node = node.getNextSibling();
		}
		return null;
	}

	private static String getMessageDataNameTypeFromMessageElementType(Schema schema, String messageType, String messageDataName) {
		try {
			Node node = schema.getElement().getFirstChild();
			while (node != null) {
				if (node.getAttributes() != null && node.getAttributes().getNamedItem(NAME_LABEL).getNodeValue().equals(messageType)) {
					Node sequence = node.getFirstChild();
					while (sequence != null) {
						if (sequence.getNodeName().contains("sequence")) {
							break;
						}
						sequence = sequence.getNextSibling();
					}

					sequence = sequence.getFirstChild();
					while (sequence != null) {
						if (sequence.getAttributes() != null && sequence.getAttributes().getNamedItem(NAME_LABEL).getNodeValue().equals(messageDataName)) {
							if (sequence.getAttributes().getNamedItem(TYPE_LABEL) != null) {
								if (sequence.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue().contains(COLON))
									return StringUtils.split(sequence.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue(), COLON)[1];
								else
									return sequence.getAttributes().getNamedItem(TYPE_LABEL).getNodeValue();
							}

						}
						sequence = sequence.getNextSibling();
					}
				}
				node = node.getNextSibling();
			}
		} catch (DOMException e) {
			e.printStackTrace();
		}
		return "";
	}


}
