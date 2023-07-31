package com.github.albfernandez.qunittestrunner.file;

import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {
	
	
	@Test
	public void test1() throws Exception {
		String resource = "/META-INF/javascript/qunit.js";
		InputStream is = FileUtils.getClassPathResourceAsInputStream(resource);
		Assertions.assertNotNull(is);
	}

}
