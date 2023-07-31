package com.github.albfernandez.qunittestrunner.transformer;

import com.github.albfernandez.qunittestrunner.results.QUnitTestResults;

public interface QUnitTestResultsTransformer {
	String[] transform(QUnitTestResults paramQUnitTestResults);
}
