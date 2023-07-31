package com.github.albfernandez.qunittestrunner;

import org.apache.tools.ant.Task;

public class QUnitTestRunnerTask extends Task {
	private String qUnitPath;
	private String qUnitTestRecipeFilePattern = ".*TestRecipe\\.js";

	private String baseDirectory = "test";

	private String reportDirectory = "target/reports";

	private QUnitTestRunner qUnitTestRunner = null;

	public void setQUnitPath(String qUnitPath) {
		this.qUnitPath = qUnitPath;
	}

	public void setQunitTestRecipeFilePattern(String qUnitTestFilePattern) {
		this.qUnitTestRecipeFilePattern = qUnitTestFilePattern;
	}

	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public void setReportDirectory(String reportDirectory) {
		this.reportDirectory = reportDirectory;
	}

	public void execute() {
		System.out.println("QUnitTestRunnerTask: Started.");
		this.qUnitTestRunner = new QUnitTestRunner();
		this.qUnitTestRunner.runTests(this.qUnitPath, this.qUnitTestRecipeFilePattern, this.baseDirectory,
				this.reportDirectory);
		System.out.println("QUnitTestRunnerTask: Finished.");
	}
}
