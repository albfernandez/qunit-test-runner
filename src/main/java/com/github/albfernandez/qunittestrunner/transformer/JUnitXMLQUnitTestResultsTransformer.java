package com.github.albfernandez.qunittestrunner.transformer;

import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.albfernandez.qunittestrunner.results.QUnitTestAssertion;
import com.github.albfernandez.qunittestrunner.results.QUnitTestExecution;
import com.github.albfernandez.qunittestrunner.results.QUnitTestModule;
import com.github.albfernandez.qunittestrunner.results.QUnitTestResults;

public class JUnitXMLQUnitTestResultsTransformer implements QUnitTestResultsTransformer {
	private static final Logger LOGGER = Logger.getLogger(JUnitXMLQUnitTestResultsTransformer.class);

	public String[] transform(QUnitTestResults testResults) {
		List<QUnitTestModule> testModules = testResults.getTestModules();
		String[] fileContents = new String[testModules.size()];

		for (int i = 0; i < testModules.size(); i++) {
			QUnitTestModule testModule = testModules.get(i);
			Document moduleDocument = buildModuleReport(testModule);
			fileContents[i] = getStringFromDocument(moduleDocument);
		}
		return fileContents;
	}

	private Document buildModuleReport(QUnitTestModule testModule) {
		List<QUnitTestExecution> testExecutions = testModule.getTestExecutions();
		int noOfTests = testModule.getTotalAssertions();
		int noOfFailures = noOfTests - testModule.getTotalPassedAssertions();

		Document document = null;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.newDocument();
			Element rootElement = document.createElement("testsuite");
			rootElement.setAttribute("errors", "0");
			rootElement.setAttribute("failures", String.valueOf(noOfFailures));
			rootElement.setAttribute("hostname", "TBC_random_hostname");
			rootElement.setAttribute("name", testModule.getName());
			rootElement.setAttribute("tests", String.valueOf(noOfTests));
			rootElement.setAttribute("time", "0");

			document.appendChild(rootElement);

			for (QUnitTestExecution testExecution : testExecutions) {
				List<QUnitTestAssertion> assertions = testExecution.getAssertions();
				for (QUnitTestAssertion assertion : assertions) {
					Element testCase = document.createElement("testcase");
					rootElement.appendChild(testCase);
					testCase.setAttribute("classname", testModule.getName());
					testCase.setAttribute("name", testExecution.getName() + ": " + assertion.getMessage());
					testCase.setAttribute("time", "0");
					if (!assertion.isPassed()) {
						Element failure = document.createElement("failure");
						failure.setAttribute("message", "Assertion error: " + assertion.getMessage());
						testCase.appendChild(failure);
					}

				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return document;
	}

	public String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {

			ex.printStackTrace();
			return null;
		}
	}
}
