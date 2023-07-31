package com.github.albfernandez.qunittestrunner.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public final class FileUtils {
	private static final Logger LOGGER = Logger.getLogger(FileUtils.class);

	public static final FilenameFilter QUNIT_TEST_RECIPE_FILENAME_FILTER = buildQUnitTestRecipeFilenameFilter();

	public static final FileFilter IS_DIRECTORY_FILE_FILTER = new DirectoryFileFilter();

	public static URI findResource(String resource, boolean preferClasspath) throws FileNotFoundException {
		LOGGER.debug("Searching for resource: " + resource + ", preferClasspath: " + preferClasspath);
		URI resourceStream = null;
		if (preferClasspath) {
			resourceStream = getClassPathResource(resource);
			if (resourceStream == null) {
				resourceStream = findNonClasspathResource(resource);
			} else {

				LOGGER.debug("Resource found on classpath: " + resource);
			}
		} else {

			try {
				resourceStream = findNonClasspathResource(resource);

			} catch (FileNotFoundException fnfe) {
				LOGGER.debug("Resource not found as file, will now check classpath.");
			}
			if (resourceStream == null) {
				resourceStream = getClassPathResource(resource);
			}
		}
		return resourceStream;
	}

	private static URI findNonClasspathResource(String resource) throws FileNotFoundException {
		URI resourceStream = null;
		LOGGER.debug("Searching for resource: " + resource + " as an absolute file path.");
		File resourceFile = new File(resource);
		if (resourceFile.exists()) {
			LOGGER.debug("Resource found using absolute path: " + resource);
			resourceStream = resourceFile.toURI();
		} else {

			LOGGER.debug("Resource not found using absolute path: " + resource + ", will try as relative path instead");
			File currentDir = new File(".");
			resourceFile = new File(currentDir, resource);
			if (resourceFile.exists()) {
				LOGGER.debug("Resource found using relative path: " + resource);
				resourceStream = resourceFile.toURI();
			} else {

				LOGGER.debug("Resource not found using relative path: " + resource);
				throw new FileNotFoundException(resource);
			}
		}
		return resourceStream;
	}

	public static File getClasspathResourceAsFile(String resource) {
		File file;
		URL url = FileUtils.class.getResource(resource);

		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			file = new File(url.getPath());
		}
		return file;
	}

	public static File getNonClasspathResourceAsFile(String resource) throws FileNotFoundException {
		File file = new File(resource);
		if (!file.exists()) {
			LOGGER.error("Cannot find non-classpath resource: " + resource);
			throw new FileNotFoundException(resource);
		}
		return file;
	}

	public static InputStream getClassPathResourceAsInputStream(String resource) throws FileNotFoundException {
		LOGGER.debug("Searching classpath for resource: " + resource);
		InputStream inputStream = FileUtils.class.getResourceAsStream(resource);
		LOGGER.debug("Resource " + resource + ((inputStream == null) ? " not found" : " found"));
		if (inputStream == null) {
			LOGGER.error("Could not find resource on classpath: " + resource);
			throw new FileNotFoundException(resource);
		}
		return inputStream;
	}

	public static URI getClassPathResource(String resource) throws FileNotFoundException {
		LOGGER.debug("Searching classpath for resource: " + resource);
		URI uri = null;
		try {
			URL url = FileUtils.class.getResource(resource);
			LOGGER.debug("Resource " + resource + ((url == null) ? " not found" : " found"));
			if (url == null) {
				throw new FileNotFoundException(resource);
			}
			uri = url.toURI();
		} catch (URISyntaxException urise) {
			LOGGER.error(urise.getMessage());
		}

		return uri;
	}

	public static Reader getClassPathResourceAsReader(String resource) throws FileNotFoundException {
		return new InputStreamReader(getClassPathResourceAsInputStream(resource));
	}

	public static List<File> scanRecursivelyForFiles(String baseDirectoryPath, FilenameFilter filenameFilter,
			boolean preferClasspath) throws FileNotFoundException {
		LOGGER.debug("Searching recursively for files in :" + baseDirectoryPath);
		URI baseDirectory = findResource(baseDirectoryPath, preferClasspath);
		LOGGER.debug("File is" + baseDirectory.getRawPath());
		List<File> directoriesToScan = listSubDirs(new File(baseDirectory));
		List<File> matchedFiles = new ArrayList<>();

		for (File directory : directoriesToScan) {
			File[] matchedFilesInDirectory = directory.listFiles(QUNIT_TEST_RECIPE_FILENAME_FILTER);
			matchedFiles.addAll(Arrays.asList(matchedFilesInDirectory));
		}
		return matchedFiles;
	}

	public static FilenameFilter buildRegexpMatchFilenameFilter(final String regexp) {
		return new FilenameFilter() {
			private final String testRecipeFileRegexpString = regexp;
			Pattern regexpMatcher = Pattern.compile(this.testRecipeFileRegexpString);

			public boolean accept(File dir, String name) {
				if (this.regexpMatcher.matcher(name).matches()) {
					return true;
				}

				return false;
			}
		};
	}

	private static List<File> listSubDirs(File dir) throws FileNotFoundException {
		LOGGER.debug("Searching for sub directories of: " + dir.getAbsolutePath());
		if (!dir.exists()) {
			LOGGER.error("Specified directory doesn't exist: " + dir.getAbsolutePath());
			throw new FileNotFoundException(dir.getAbsolutePath());
		}
		List<File> subDirs = new ArrayList<>();
		subDirs.add(dir);
		File[] childDirs = dir.listFiles(IS_DIRECTORY_FILE_FILTER);
		for (File childDir : childDirs) {
			subDirs.addAll(listSubDirs(childDir));
		}
		return subDirs;
	}

	private static FilenameFilter buildQUnitTestRecipeFilenameFilter() {
		return buildRegexpMatchFilenameFilter(".*TestRecipe\\.js");
	}


	private static class DirectoryFileFilter implements FileFilter {
		public DirectoryFileFilter() {
			super();
		}

		public boolean accept(File file) {
			return file.isDirectory() && !"".equals(file.getName());
		}
	}
}
