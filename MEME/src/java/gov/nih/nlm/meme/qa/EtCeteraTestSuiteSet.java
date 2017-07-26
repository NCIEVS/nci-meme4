/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  EtCeteraTestSuiteSet.java
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.etc.DBLockTest;
import gov.nih.nlm.meme.qa.etc.RelConcurrencyCheck;
import gov.nih.nlm.meme.qa.etc.RealTimeCUIAssignmentTest;

/**
 * Represents a set of miscellaneous test suites.  Currently this involves
 * a {@link DBLockTest} and a {@link RelConcurrencyCheck}.
 */
public class EtCeteraTestSuiteSet  extends TestSuiteSet {

  /**
   * Instantiates an empty {@link EtCeteraTestSuiteSet}.
   */
  public EtCeteraTestSuiteSet() { }

  /**
   * Returns the test suite name.
   * @return the test suite name
   */
  public String getName() {
    return "EtCeteraTestSuiteSet";
  }

  /**
   * Returns the test suite description.
   * @return the test suite description
   */
  public String getDescription() {
    return "This contains tests that do not fit into other categories such as Concurrency and Deadlock checks";
  }

  /**
   * Returns the {@link ITestSuite}s that are part of this set.
   * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
        new RelConcurrencyCheck(), // [0]
        new DBLockTest()           // [1]
        //new RealTimeCUIAssignmentTest()
    };
  }
}
