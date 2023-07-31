package com.github.albfernandez.qunittestrunner;

import org.apache.log4j.Logger;
import org.mozilla.javascript.ScriptableObject;

import com.github.albfernandez.qunittestrunner.results.QUnitTestAssertion;
import com.github.albfernandez.qunittestrunner.results.QUnitTestExecution;
import com.github.albfernandez.qunittestrunner.results.QUnitTestModule;
import com.github.albfernandez.qunittestrunner.results.QUnitTestResults;

public class QUnitTestExecutionHandler extends ScriptableObject {
	private static final Logger LOGGER = Logger.getLogger(QUnitTestExecutionHandler.class);

	private static final long serialVersionUID = 438270592527335646L;

	private QUnitTestResults testResults = new QUnitTestResults();

	private QUnitTestModule currentModule = null;

	private QUnitTestExecution currentTestExecution = null;

	public QUnitTestResults getTestResults() {
		return this.testResults;
	}

	public void jsFunction_begin() {
		this.testResults = new QUnitTestResults();
	}

	public void jsFunction_moduleStart(String name) {
		this.currentModule = new QUnitTestModule(name);
		this.testResults.addQUnitTestModule(this.currentModule);
	}

	public void jsFunction_moduleDone(String name, int failures, int totalAssertions) {
		LOGGER.debug("jsFunction_moduleDone" + name + "," + failures + "," + totalAssertions);
	}

	public void jsFunction_testStart(String name) {
		this.currentTestExecution = new QUnitTestExecution(name);
		this.currentModule.addTestExecution(this.currentTestExecution);
	}

	public void jsFunction_testDone(String name, int failures, int totalAssertions) {
		LOGGER.debug("jsFunction_testDone:" + name + "," + failures + "," + totalAssertions);
	}

	public void jsFunction_log(boolean passed, String message) {
		QUnitTestAssertion assertion = new QUnitTestAssertion(passed, message);
		this.currentTestExecution.addAssertion(assertion);
	}

	public void jsFunction_done() {
		LOGGER.debug("jsFunction_done");
	}

	public String getClassName() {
		return "QUnitTestExecutionHandler";
	}
}
