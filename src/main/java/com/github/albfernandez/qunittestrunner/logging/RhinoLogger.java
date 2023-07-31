package com.github.albfernandez.qunittestrunner.logging;

import org.apache.log4j.Logger;
import org.mozilla.javascript.ScriptableObject;

public class RhinoLogger extends ScriptableObject {
	private static final Logger LOGGER = Logger.getLogger(RhinoLogger.class);

	private static final long serialVersionUID = 438270592527335646L;

	public static void debug(String message) {
		LOGGER.debug(message);
	}

	public String getClassName() {
		return "RhinoLogger";
	}
}
