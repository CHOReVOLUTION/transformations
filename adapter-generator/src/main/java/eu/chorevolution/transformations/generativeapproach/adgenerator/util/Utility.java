/*
* Copyright 2017 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.adgenerator.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chorevolution.transformations.generativeapproach.adgenerator.ADGeneratorException;

public class Utility {

	private final static Logger LOGGER = LoggerFactory.getLogger(Utility.class);

	private final static String TARGZ_EXTENSION = ".tar.gz";
	private final static String ZIP_EXTENSION = ".zip";

	public static void createZipOfDirectory(String directoryPath, String destination, String zipName) {
 
        try {
            FileOutputStream fos = new FileOutputStream(destination + File.separatorChar + zipName + ZIP_EXTENSION);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(directoryPath);
			zipDirectory(fileToZip, fileToZip.getName(), zipOut);
	        zipOut.close();
	        fos.close();
		} catch (IOException e) {
			throw new ADGeneratorException("Exception into createZipfDirectory see log file for details ");
		}

	}
	
	private static void zipDirectory(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
            	zipDirectory(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


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
			throw new ADGeneratorException("Exception into createTarGzOfDirectory see log file for details ");
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
			throw new ADGeneratorException("Exception into addFileToTarGz see log file for details ");
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
			throw new ADGeneratorException("Exception into createDestinationFolderAndGetPath see log file for details ");
		}
		return destDir;
	}

	public static void deleteFolder(String path) {

		try {
			FileUtils.forceDelete(new File(path));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			// throw new ADGeneratorException("Exception into
			// deleteFolder see log file for details ");
		}

	}

	public static String createArtifactName(String name) {
		if (name.startsWith("ad")) {
			name = name.substring(2);
		}
		return StringUtils.lowerCase(StringUtils.remove(StringUtils.deleteWhitespace(name), "-"));
	}

	public static String createName(String name) {

		return StringUtils.remove(StringUtils.deleteWhitespace(name), "-");
	}

	public static void createProjectFolders(String destinationRootPath) {

		String resoucesFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "resources";
		String javaFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "java";
		String metainfFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "META-INF";
		String webinfFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF";
		String springFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "spring";
		String integrationFolderPath = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "spring" + File.separatorChar + "integration";
		String serviceWDSLFolderPAth = destinationRootPath + File.separatorChar + "src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "wsdl";
		try {
			FileUtils.forceMkdir(new File(resoucesFolderPath));
			FileUtils.forceMkdir(new File(javaFolderPath));
			FileUtils.forceMkdir(new File(metainfFolderPath));
			FileUtils.forceMkdir(new File(webinfFolderPath));
			FileUtils.forceMkdir(new File(springFolderPath));
			FileUtils.forceMkdir(new File(integrationFolderPath));
			FileUtils.forceMkdir(new File(serviceWDSLFolderPAth));
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ADGeneratorException("Exception into createProjectFolders see log file for details ");
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
			throw new ADGeneratorException("Exception into createProjectFolders see log file for details ");
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

}
