package com.github.albfernandez.qunittestrunner.results;

public class QUnitTestAssertion {
	private boolean passed;
	private String message;

	public QUnitTestAssertion(boolean passed, String message) {
		this.passed = passed;
		this.message = message;
	}

	public boolean isPassed() {
		return this.passed;
	}

	public String getMessage() {
		return this.message;
	}
}


