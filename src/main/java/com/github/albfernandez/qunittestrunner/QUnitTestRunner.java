package com.github.albfernandez.qunittestrunner;

import com.github.albfernandez.qunittestrunner.file.FileUtils;
import com.github.albfernandez.qunittestrunner.logging.RhinoLogger;
import com.github.albfernandez.qunittestrunner.results.QUnitTestAssertion;
import com.github.albfernandez.qunittestrunner.results.QUnitTestExecution;
import com.github.albfernandez.qunittestrunner.results.QUnitTestModule;
import com.github.albfernandez.qunittestrunner.results.QUnitTestResults;
import com.github.albfernandez.qunittestrunner.transformer.JUnitXMLQUnitTestResultsTransformer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class QUnitTestRunner {
	private static final Logger LOGGER = Logger.getLogger(QUnitTestRunner.class);

	private static final String QUNIT_FILE_FILENAME = "/META-INF/javascript/qunit.js";

	private static final String ENV_RHINO_JS_FILE_FILENAME = "/META-INF/javascript/env.rhino.1.2.js";

	private static final String QUNIT_RHINO_BINDINGS_FILE_FILENAME = "/META-INF/javascript/qunit-rhino-binding.js";

	private static final String LOAD_FILES_JS_IMPLEMENTATION_FILENAME = "/META-INF/javascript/load-files-implementation.js";

	private static final String PRINT_METHOD_JS_IMPLEMENTATION_FILENAME = "/META-INF/javascript/print-method-implementation.js";

	public void runTests(String qUnitPath, String qunitTestRecipeFilePattern, String baseDirectory,
			String reportDirectory) throws QUnitTestRunnerRuntimeException {
		LOGGER.debug("QUnitTestRunner: Entering runTests");

		validateInputParameters(qUnitPath, qunitTestRecipeFilePattern, baseDirectory, reportDirectory);

		List<QUnitTestExecutionHandler> testExecutionHandlers = new ArrayList<>();
		List<File> testRecipeFiles = loadQUnitTestRecipeFiles(baseDirectory, qunitTestRecipeFilePattern);

		for (File testRecipeFile : testRecipeFiles) {
			QUnitTestExecutionHandler testExecutionHandler = executeTestRecipeFile(testRecipeFile);
			testExecutionHandlers.add(testExecutionHandler);
		}

		writeReportFiles(reportDirectory, testExecutionHandlers);
		LOGGER.debug("QUnitTestRunner: Exiting runTests");
	}

	private void validateInputParameters(String qUnitPath, String qunitTestFilePattern, String baseDirectory,
			String reportDirectory) {
		try {
			if (qUnitPath != null) {
				FileUtils.findResource(qUnitPath, false);

			}

		} catch (FileNotFoundException fnfe) {
			throw new QUnitTestRunnerRuntimeException(fnfe);
		}
	}

	private List<File> loadQUnitTestRecipeFiles(String baseDirectory, String qunitTestRecipeFilePattern) {
		LOGGER.debug("Searching for test recipe files in: " + baseDirectory);
		FilenameFilter qunitTestRecipeFilenameFilter = (qunitTestRecipeFilePattern == null)
				? FileUtils.QUNIT_TEST_RECIPE_FILENAME_FILTER
				: FileUtils.buildRegexpMatchFilenameFilter(qunitTestRecipeFilePattern);

		List<File> testRecipeFiles = null;
		try {
			testRecipeFiles = FileUtils.scanRecursivelyForFiles(baseDirectory, qunitTestRecipeFilenameFilter, false);

			if (testRecipeFiles.isEmpty()) {
				LOGGER.warn("No QUnit test files were found.");
			}
		} catch (FileNotFoundException fnfe) {
			throw new QUnitTestRunnerRuntimeException(fnfe);
		}
		return testRecipeFiles;
	}

	private QUnitTestExecutionHandler executeTestRecipeFile(File testRecipeFile) {
		QUnitTestExecutionHandler testExecutionHandler = null;
		try {
			Context context = Context.enter();

			context.setOptimizationLevel(-1);
			ScriptableObject scriptableObject = context.initStandardObjects();

			ScriptableObject.defineClass((Scriptable) scriptableObject, QUnitTestExecutionHandler.class);
			addPrintMethodToContext(context, scriptableObject);

			Reader qunitReader = FileUtils.getClassPathResourceAsReader(QUNIT_FILE_FILENAME);
			Reader qunitRhinoBindingsReader = FileUtils.getClassPathResourceAsReader(QUNIT_RHINO_BINDINGS_FILE_FILENAME);
			Reader envRhinoJsReader = FileUtils.getClassPathResourceAsReader(ENV_RHINO_JS_FILE_FILENAME);
			context.evaluateReader(scriptableObject, qunitReader, QUNIT_FILE_FILENAME, 1, null);
			context.evaluateReader( scriptableObject, qunitRhinoBindingsReader,
					QUNIT_RHINO_BINDINGS_FILE_FILENAME, 1, null);
			context.evaluateReader(scriptableObject, envRhinoJsReader, ENV_RHINO_JS_FILE_FILENAME, 1, null);

			Reader reader = createFileReader(testRecipeFile);
			String[] filesInRecipeFile = getFilesFromTestRecipeFile(context, scriptableObject, reader);
			List<File> filesToExecute = buildCompleteFileExecutionList(testRecipeFile.getParent(), filesInRecipeFile);

			Map<File, Throwable> fileExecutionExceptions = executeJavaScriptFiles(context, scriptableObject, filesToExecute);

			testExecutionHandler = (QUnitTestExecutionHandler) scriptableObject.get("testExecutionHandler", scriptableObject);

			QUnitTestResults testResults = testExecutionHandler.getTestResults();
			QUnitTestModule fileLoadingExceptionsTestModule = new QUnitTestModule(".javascript.load.results");
			QUnitTestExecution fileLoadingTestExecution = new QUnitTestExecution(".javascript.load.results");
			Set<File> filesWithExecptions = fileExecutionExceptions.keySet();
			for (File file : filesWithExecptions) {
				QUnitTestAssertion assertion = new QUnitTestAssertion(false, (fileExecutionExceptions.get(file)).getMessage());
				fileLoadingTestExecution.addAssertion(assertion);
			}
			fileLoadingExceptionsTestModule.addTestExecution(fileLoadingTestExecution);
			testResults.addQUnitTestModule(fileLoadingExceptionsTestModule);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		} finally {

			Context.exit();
		}
		return testExecutionHandler;
	}

	private void writeReportFiles(String reportDirectory, List<QUnitTestExecutionHandler> testExecutionHandlers) {
		File reportDirectoryFile = null;
		try {
			reportDirectoryFile = FileUtils.getNonClasspathResourceAsFile(reportDirectory);
		} catch (FileNotFoundException fnfe) {
			LOGGER.debug("Report directory not found: " + reportDirectory);
			File currentDir = new File(".");
			reportDirectoryFile = new File(currentDir, reportDirectory);
			reportDirectoryFile.mkdirs();
			LOGGER.debug("Report directory created: " + reportDirectoryFile.getAbsolutePath());
		}

		for (QUnitTestExecutionHandler testExecutionHandler : testExecutionHandlers) {
			try {
				writeResultsToDisk(reportDirectory, testExecutionHandler);
			} catch (IOException ioe) {
				throw new QUnitTestRunnerRuntimeException(ioe);
			}
		}
	}

	private Reader createFileReader(File file) {
		Reader fileReader = null;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException fnfe) {
			LOGGER.error(fnfe.getMessage());
		}
		return fileReader;
	}

	private void addPrintMethodToContext(Context context, Scriptable scope) throws Exception {
		LOGGER.debug("Adding 'print' method to Rhino Context");

		ScriptableObject.defineClass(scope, RhinoLogger.class);

		Reader printMethodReader = FileUtils.getClassPathResourceAsReader(PRINT_METHOD_JS_IMPLEMENTATION_FILENAME);
		Object result = context.evaluateReader(scope, printMethodReader, PRINT_METHOD_JS_IMPLEMENTATION_FILENAME, 1, null);
	}

	private String[] getFilesFromTestRecipeFile(Context context, Scriptable scope, Reader recipeFileReader)
			throws IOException {
		Reader loadFilesImplementationFileReader = FileUtils
				.getClassPathResourceAsReader(LOAD_FILES_JS_IMPLEMENTATION_FILENAME);
		Object result = context.evaluateReader(scope, loadFilesImplementationFileReader, LOAD_FILES_JS_IMPLEMENTATION_FILENAME, 1, null);
		NativeArray testRecipeFiles = (NativeArray) context.evaluateReader(scope, recipeFileReader, "testRecipeFiles",	1, null);
		return convertNativeArrayToStringArray(testRecipeFiles);
	}

	private String[] convertNativeArrayToStringArray(NativeArray nativeArray) {
		NativeArray arr = nativeArray;
		String[] array = new String[(int) arr.getLength()];
		for (Object o : arr.getIds()) {
			int index = ((Integer) o).intValue();
			array[index] = (String) arr.get(index, null);
		}
		return array;
	}

	private List<File> buildCompleteFileExecutionList(String baseDir, String[] filesInRecipeFile) {
		List<File> filesToExecute = new ArrayList<>();
		for (int i = 0; i < filesInRecipeFile.length; i++) {
			String fileName = filesInRecipeFile[i];
			File fileToExecute = new File(baseDir, fileName);
			filesToExecute.add(fileToExecute);
		}
		return filesToExecute;
	}

	private Map<File, Throwable> executeJavaScriptFiles(Context context, Scriptable scope, List<File> filesToExecute)
			throws IOException {
		Map<File, Throwable> exceptions = new HashMap<>();
		for (File file : filesToExecute) {
			Reader fileReader = createFileReader(file);
			try {
				context.evaluateReader(scope, fileReader, file.getName(), 1, null);

			} catch (Exception e) {
				LOGGER.error("Exception encountered whilst executing file: " + file.getAbsolutePath(), e);
				exceptions.put(file, e);
			}
			LOGGER.debug("Executed file: " + file.getName());
		}
		return exceptions;
	}

	private void writeResultsToDisk(String reportDirectory, QUnitTestExecutionHandler testExecutionHandler)
			throws IOException {
		QUnitTestResults testResults = testExecutionHandler.getTestResults();
		JUnitXMLQUnitTestResultsTransformer jUnitXMLQUnitTestResultsTransformer = new JUnitXMLQUnitTestResultsTransformer();
		String[] results = jUnitXMLQUnitTestResultsTransformer.transform(testResults);

		File reportDirectoryFile = new File(reportDirectory);
		if (!reportDirectoryFile.exists()) {
			LOGGER.debug("Report directory does not exist yet" + reportDirectoryFile.getAbsolutePath());
			reportDirectoryFile.createNewFile();
			LOGGER.debug("Created report directory:" + reportDirectoryFile.getAbsolutePath());
		}
		for (int i = 0; i < results.length; i++) {
			String result = results[i];
			String fileName = "TEST-qunit-" + i + ".xml";
			File file = new File(reportDirectory, fileName);
			Files.write(file.toPath(), result.getBytes(StandardCharsets.UTF_8));
		}
	}
}
