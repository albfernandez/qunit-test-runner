package com.github.albfernandez.qunittestrunner.results;

import java.util.ArrayList;
import java.util.List;














public class QUnitTestResults
{
  private List<QUnitTestModule> testModules = new ArrayList<QUnitTestModule>();
  
  public void addQUnitTestModule(QUnitTestModule testModule) {
    this.testModules.add(testModule);
  }






  
  public List<QUnitTestModule> getTestModules() {
    return this.testModules;
  }
}

