/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  TestCenter.java
 *
 * Author:  tkao
 *
 * History:
 *   12/07/2006 BAC (1-D0BIJ): Updated to run all test suites from
 *   main method.
 *   Nov 10, 2003: 1st Version.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa;

public class TestCenter {

	/**
	 * Application entry point.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Run a test suite set
			if (args.length == 1) {
				int i = Integer.parseInt(args[0]);
				TestSuiteSet set = TestSuiteSet.getTestSuiteSets()[i];
				runTestSuiteSet(set);
			}
			
			// Run a test suite
			else if (args.length == 2) {
				int i = Integer.parseInt(args[0]);
				int j = Integer.parseInt(args[1]);
				TestSuiteSet.getTestSuiteSets()[i].getTestSuites()[j].run();
			} 
			
			// Run everything
			else {

				TestSuiteSet[] sets = TestSuiteSet.getTestSuiteSets();
				int ct = 0;
				for (TestSuiteSet set : sets) {
					runTestSuiteSet(set);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

  /**
   * Helper method for running a test suite.
   * @param set TODO
   * @param tests
   * @return
   */
	static float runTestSuiteSet(TestSuiteSet set) {
		int fail_ct = 0;
		for (ITestSuite suite : set.getTestSuites()) {
			try {
				Thread.sleep(2000);
			} catch (Exception e) { }
			suite.run();
			System.out.println("TEST SUITE " + suite.getName()
					+ (suite.isPassed() ? " PASSED" : (" FAILED" + ++fail_ct)) + "\n");
		}
		System.out.println("TEST SUITE SET " + set.getName()
				+ (fail_ct == 0 ? " PASSED" : (" FAILED " + fail_ct )) + "\n");
		return fail_ct / (set.getTestSuites().length);
	}

	/**
	 * Helper method for running test suite sets.
	 * @param set
	 */
	static void runAllTestSets(TestSuiteSet[] sets) {
		float pass_rate = 0;
		for (int i = 0; i < sets.length; i++) {
			pass_rate = runTestSuiteSet(sets[i]);
			System.out.println(sets[i].getName() + " has the pass rate of "
					+ pass_rate + "%");
		}
	}

}