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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.CDGeneratorException;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesData;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesDataItem;
import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.PropertyAliasesMessageType;

public class Utils {
	
	private static final String	BASE_SERVICE_ARTIFACTS_FILE_NAME = "BaseServiceArtifacts.wsdl";
	private static final String	BASE_SERVICE_FILE_NAME = "BaseService.bpel";	
	private static final String BASE_SERVICE_RELATIVE_PATH = "baseService/";
	private static final String CD_PREFIX = "cd";
	private static final String COLON_LABEL = ":";
	private static final String TARGZ_EXTENSION = ".tar.gz";
	private static final String XML_FILE_EXTENSION = ".xml";
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(Utils.class);
	
	public static byte[] createTarGzOfDirectory(String directoryPath,String destination, String tarGzName){

		// log execution flow
		LOGGER.entry();	
	    FileOutputStream fOut = null;
	    BufferedOutputStream bOut = null;
	    GzipCompressorOutputStream gzOut = null;
	    TarArchiveOutputStream tOut = null;	 
	    byte[] bytes = null;
	    String tarGzPath = new StringBuilder(destination).append(File.separatorChar).append(tarGzName)
	    		.append(TARGZ_EXTENSION).toString();
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
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into createTarGzOfDirectory see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
        return bytes;
	}
	 
	public static byte[] createTarGzOfDirectoryContent(String directoryPath,String destination,
			String tarGzName){
		
		// log execution flow
		LOGGER.entry();
	    FileOutputStream fOut = null;
	    BufferedOutputStream bOut = null;
	    GzipCompressorOutputStream gzOut = null;
	    TarArchiveOutputStream tOut = null;	 
	    byte[] bytes = null;
	    String tarGzPath = new StringBuilder(destination).append(File.separatorChar).append(tarGzName)
	    		.append(TARGZ_EXTENSION).toString();
        try {
        	File targz = new File(tarGzPath);
			fOut = new FileOutputStream(targz);
	        bOut = new BufferedOutputStream(fOut);
	        gzOut = new GzipCompressorOutputStream(bOut);
	        tOut = new TarArchiveOutputStream(gzOut);
	        File dir = new File(directoryPath);
	        for (File file : dir.listFiles()) {
	        	addFileToTarGz(tOut, file.getAbsolutePath(), "");
			}
	        tOut.finish();	 
	        tOut.close();
	        gzOut.close();
	        bOut.close();
	        fOut.close();
	        bytes = FileUtils.readFileToByteArray(targz);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into createTarGzOfDirectoryContent see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
        return bytes;		
	}
	
	
	private static void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base){
		
		// log execution flow
		LOGGER.entry();
	    File f = new File(path);
	    String entryName = base + f.getName();
	    TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);	 
	    try {
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
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException("Exception into addFileToTarGz see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
	}
	
	
	public static String createTemporaryFolderFromMillisAndGetPath(){

		// log execution flow
		LOGGER.entry();
		String destDir = new StringBuilder(FileUtils.getTempDirectoryPath()+File.separatorChar+System
				.currentTimeMillis()).toString().replaceAll("\\s", "_");
		try {
			FileUtils.forceMkdir(new File(destDir));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into createDestinationFolderAndGetPath see log file for details");
		}
		// log execution flow
		LOGGER.exit();
		return destDir;
	}
	
	public static void deleteFolder(String path){

		// log execution flow
		LOGGER.entry();
		try {
			FileUtils.forceDelete(new File(path));
		} catch (IOException e) {
			// TODO verify if this exception has to be thrown
			LOGGER.error(e.getMessage(),e);
//			throw new CDGeneratorException("Exception into deleteFolder see log file for details ");
		}
		// log execution flow
		LOGGER.exit();
	}
	
	public static String createCDname(String participantName){
		
		return new StringBuilder(CD_PREFIX).append(WSDLUtils
				.formatParticipantNameForWSDL(participantName)).toString();
	}
	
	public static String getLocalPartXMLQName(String qname){
		
		return StringUtils.split(qname,COLON_LABEL)[1]; 
	}
	
	public static String createXMLQNameString(String prefix,String localPart){
		
		return new StringBuilder(prefix).append(COLON_LABEL).append(localPart).toString(); 
	}

	public static void writeXMLDocumentToFile(Document xmlDocument, String destination, String fileName){
		
		// log execution flow
		LOGGER.entry();
		try {
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");            
	        DOMSource source = new DOMSource(xmlDocument);
			String xmlFilePath = new StringBuilder(destination).append(File.separator).append(fileName)
					.append(XML_FILE_EXTENSION).toString();
	        StreamResult sr = new StreamResult(new File(xmlFilePath));
	        transformer.transform(source, sr);   
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			LOGGER.error(e.getMessage(),e);
			throw new CDGeneratorException(
					"Exception into writeXMLDocumentToFile see log file for details ");
		}
		// log execution flow
		LOGGER.exit();

	}
	
	public static boolean isParticipantProsumer(String participantName,
			List<String> prosumersParticipantsNames){
		
		return prosumersParticipantsNames.contains(participantName);
	}
	
	public static void addPropertyAliasesData(PropertyAliasesData propertyAliasesData,
			String participantName, String wsdlFileName, String wsdlTNS,
			PropertyAliasesMessageType propertyAliasesMessageType){

		// log execution flow
		LOGGER.entry();
		for (PropertyAliasesDataItem propertyAliasesDataItem : propertyAliasesData
				.getPropertyAliasesData()) {
			if(propertyAliasesDataItem.getParticipantName().equals(participantName)){
				// check if it already exists a property alias for the part of the message
				for (PropertyAliasesMessageType propertyAliasesMessageTypeListItem : propertyAliasesDataItem
						.getPropertyAliasesMessages()) {
					if(propertyAliasesMessageTypeListItem.getMessageTypeName()
							.equals(propertyAliasesMessageType.getMessageTypeName()) &&
							propertyAliasesMessageTypeListItem.getMessageTypePart()
							.equals(propertyAliasesMessageType.getMessageTypePart())){
						// a property alias for the part of the message already exists
						// it is not necessary to add propertyAliasesMessageType to the list
						return;
					}					
				}
				// a property alias for the part of the message doesn't exist
				// add propertyAliasesMessageType to the list			
				propertyAliasesDataItem.addPropertyAliasesMessage(propertyAliasesMessageType);
				return;
			}
		}	
		PropertyAliasesDataItem propertyAliasesDataItem = new PropertyAliasesDataItem(participantName,
				wsdlFileName, wsdlTNS, propertyAliasesMessageType);
		propertyAliasesData.addPropertyAliasesDataItem(propertyAliasesDataItem);
		// log execution flow
		LOGGER.exit();
	}
	
	public static void copyBaseServiceFilesToFolder(String folderPath){

		// log execution flow
		LOGGER.entry();
		File sourceCodeFile = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation()
				.getPath());
		File baseServiceBpelDestFile = new File(folderPath + BASE_SERVICE_FILE_NAME);
		File baseServiceArtifactsDestFile = new File(folderPath + BASE_SERVICE_ARTIFACTS_FILE_NAME);
		// check if the method is executed inside a jar
		if(sourceCodeFile.isFile()) { 
			// the method is executed inside a jar
			try {
				// get the source code jar file
				JarFile sourceCodeJar = new JarFile(Utils.class.getProtectionDomain().getCodeSource()
						.getLocation().getPath());
				// get all the entries inside source code jar file
				Enumeration<JarEntry> entries = sourceCodeJar.entries();
				// iterate over source code jar entries
				while(entries.hasMoreElements()) {
					// get jar entry
					JarEntry jarEntry = (JarEntry) entries.nextElement();
					// check if the jar entry name starts with the baseService relative path
					// and if is different from baseService relative path
					if (jarEntry.getName().startsWith(BASE_SERVICE_RELATIVE_PATH) && !jarEntry.getName()
							.equals(BASE_SERVICE_RELATIVE_PATH)){
						// get the input stream of jar entry for the source code jar
						InputStream jarIS = sourceCodeJar.getInputStream(jarEntry);
						File file = null;
						// check which binding file is being processed as jar entry
						if(jarEntry.getName().contains(BASE_SERVICE_FILE_NAME)){
							file = baseServiceBpelDestFile;
						}
						if(jarEntry.getName().contains(BASE_SERVICE_ARTIFACTS_FILE_NAME)){
							file = baseServiceArtifactsDestFile;
						}
						// copy the input stream to the file
						FileUtils.copyInputStreamToFile(jarIS, file);
					}
				}
				sourceCodeJar.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw new CDGeneratorException(
						"Exception into copyBaseServiceFiles see log file for details ");
			}
		}
		else{
			// the method is not executed inside a jar
			File baseServiceBpelSrcFile = new File(Utils.class.getClassLoader()
					.getResource("baseService/BaseService.bpel").getPath());
			File baseServiceArtifactsSrcFile = new File(Utils.class.getClassLoader()
					.getResource("baseService/BaseServiceArtifacts.wsdl").getPath());
			try {
				// copy baseServiceBpelSrcFile to baseServiceBpelDestFile
				FileUtils.copyFile(baseServiceBpelSrcFile, baseServiceBpelDestFile);
				// copy baseServiceArtifactsSrcFile to baseServiceArtifactsDestFile
				FileUtils.copyFile(baseServiceArtifactsSrcFile, baseServiceArtifactsDestFile);				
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw new CDGeneratorException(
						"Exception into copyBaseServiceFiles see log file for details ");
			}
		}
		// log execution flow
		LOGGER.exit();
	}
	
	public static Object getKeyFromValueIntoMap(Map<?, ?> map, Object object){

		// log execution flow
		LOGGER.entry();

	    for(Object key:map.keySet()){
	        if(map.get(key).equals(object)) {
				// log key found
				LOGGER.info("Key found for the object "+object+" inside the map"+map+" !");		        		
		    		// log execution flow
		    		LOGGER.exit();
		    		
	            return key;
	        }
	    }
		// log key not found
		LOGGER.info("Key not found for the object "+object+" inside the map"+map+" !");		        		
    		// log execution flow
    		LOGGER.exit();		
	    return null;
	}
}
