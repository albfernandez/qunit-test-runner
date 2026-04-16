package com.github.albfernandez.qunittestrunner.examples;

import org.junit.jupiter.api.Test;

import com.github.albfernandez.qunittestrunner.QUnitTestRunner;

public class Example1Test {
	
	public Example1Test() {
		super();
	}
	
	@Test
	public void test1() throws Exception {
		QUnitTestRunner qunitTestRunner = new QUnitTestRunner();
		String testFile = "src/test/resources/example1/";
		String reportDirectory = "target/reports/example1";
		String pattern = ".*TestRecipe\\.js";
		qunitTestRunner.runTests("", pattern, testFile, reportDirectory);
		
	}

}
