/**
 * Defines the print function. This particular
 * implementation simply delegates the logging call
 * to the RhinoLogger which allows us to control the logging
 * on the Java side of things.
 */
print = function(message) {
  com.github.albfernandez.qunittestrunner.logging.RhinoLogger.debug(message);
}