package com.github.albfernandez.qunittestrunner.results;

import java.util.ArrayList;
import java.util.List;

public class QUnitTestExecution {
	private String name;
	private List<QUnitTestAssertion> assertions = new ArrayList<QUnitTestAssertion>();

	public QUnitTestExecution(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void addAssertion(QUnitTestAssertion assertion) {
		this.assertions.add(assertion);
	}

	public List<QUnitTestAssertion> getAssertions() {
		return this.assertions;
	}

	public int getTotalAssertions() {
		return this.assertions.size();
	}

	public int getTotalPassedAssertions() {
		int totalPassedAssertions = 0;
		for (QUnitTestAssertion assertion : this.assertions) {
			if (assertion.isPassed()) {
				totalPassedAssertions++;
			}
		}
		return totalPassedAssertions;
	}
}
