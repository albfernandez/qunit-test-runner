//load("qunit.js");
//This file configures QUnit to write it's results back to an object which will then reside in the Rhino Context.

var testExecutionHandler = new QUnitTestExecutionHandler();

/**
 * We configure QUnit here so that it writes results back to the QunitTestResults above.
 */
QUnit.init();
QUnit.config.blocking = false;
QUnit.config.autorun = true;
QUnit.config.updateRate = 0;
QUnit.begin = function() {
  var msg = '*** BEGIN: ***';
  testExecutionHandler.begin();
};
QUnit.moduleStart = function(name) {
  var msg = '*** MODULE START: name(' + name + ') ***';
  testExecutionHandler.moduleStart(name);
};
QUnit.moduleDone = function(name, failures, total) {
  var msg = '*** MODULE DONE : name(' + name + ') failures(' + failures + ') total(' + total + ') ***';
  testExecutionHandler.moduleDone(name, failures, total);
};
QUnit.testStart = function(name) {
  var msg = '*** TEST START: name(' + name + ') ***';
  testExecutionHandler.testStart(name);
};
QUnit.testDone = function(name, failures, total) {
  var msg = '*** TEST FINISHED: name(' + name + ') failures(' + failures + ') total(' + total + ') ***';
  testExecutionHandler.testDone(name, failures, total);
};
QUnit.log = function(result, message) {
  var msg = result ? 'PASS' : 'FAIL' + message;
  testExecutionHandler.log(result, message);
};
QUnit.done = function(failures, total) {
  var msg = '*** QUNIT DONE: failures(' + failures + ') total(' + total + ') ***';
  testExecutionHandler.done(failures, total);
};

//load("myLib.js");
//load("myLibTest.js");