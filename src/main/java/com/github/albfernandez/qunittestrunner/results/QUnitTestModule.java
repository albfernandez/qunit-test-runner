package com.github.albfernandez.qunittestrunner.results;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.ScriptableObject;

public class QUnitTestModule extends ScriptableObject {
	private static final long serialVersionUID = 438270592527335643L;
	private String name;
	private List<QUnitTestExecution> testExecutions = new ArrayList<QUnitTestExecution>();

	public QUnitTestModule(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public List<QUnitTestExecution> getTestExecutions() {
		return this.testExecutions;
	}

	public void setTestExecutions(List<QUnitTestExecution> testExecutions) {
		this.testExecutions = testExecutions;
	}

	public void addTestExecution(QUnitTestExecution testExecution) {
		this.testExecutions.add(testExecution);
	}

	public int getTotalAssertions() {
		int totalAssertions = 0;
		for (QUnitTestExecution execution : this.testExecutions) {
			totalAssertions += execution.getTotalAssertions();
		}
		return totalAssertions;
	}

	public int getTotalPassedAssertions() {
		int totalPassedAssertions = 0;
		for (QUnitTestExecution execution : this.testExecutions) {
			totalPassedAssertions += execution.getTotalPassedAssertions();
		}
		return totalPassedAssertions;
	}

	public String getClassName() {
		return "QUnitTestModule";
	}
}
