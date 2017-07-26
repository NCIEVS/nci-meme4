/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ClientTestCenter
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa;



public class ClientTestCenter {

  public static void main(String[] args) {

    TestSuiteSet[] sets = TestSuiteSet.getTestSuiteSets();
    ITestSuite[] client_suite = sets[5].getTestSuites();

    client_suite[13].run();
    //runThisTestSuiteSet(client_suite);
    //runAllTestSets(sets);
  }

  static float runThisTestSuiteSet(ITestSuite[] tests) {
    int pass_rate = 0;
    for(int i = 0; i < tests.length; i++) {
      tests[i].run();
      if (tests[i].isPassed())
        pass_rate++;
    }
    return (float) (pass_rate/tests.length)*100;
  }

  static void runAllTestSets(TestSuiteSet[] set) {
    float pass_rate = 0;
    for(int i = 0; i < set.length; i++){
      pass_rate = runThisTestSuiteSet(set[i].getTestSuites());
      System.out.println(set[i].getName() + " has the pass rate of " + pass_rate + "%");
    }
  }

}