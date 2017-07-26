/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  TestSuiteSet.java
 *
 * Changes:
 * 03/22/2007 BAC (1-D0BIJ): Removed ICTestSuiteSet from list of all sets
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa;

/**
 * Generically represents a collection of {@link ITestSuite}s.
 */
abstract public class TestSuiteSet {

  /**
   * Instantiates an empty {@link TestSuiteSet}.
   */
  public TestSuiteSet() {}

  /**
   * Returns the test suite set name.
   * @return the test suite set name
   */
  public abstract String getName();

  /**
   * Returns the test suite set description.
   * @return the test suite set description
   */
  public abstract String getDescription();

  /**
   * Returns the {@link ITestSuite}s.
   * @return the {@link ITestSuite}s
   */
  public abstract ITestSuite[] getTestSuites();

  /**
   * Returns all of the available {@link TestSuiteSet}s.
   * @return all of the available {@link TestSuiteSet}s
   */
  public static TestSuiteSet[] getTestSuiteSets() {
    return new TestSuiteSet[] {
        new SequenceTestSuiteSet(),
        new ActionTestSuiteSet(),
        //new ICTestSuiteSet(),  --handled by JUnit now
        new EtCeteraTestSuiteSet(),
        new UITestSuiteSet(),
        new ClientTestSuiteSet(),
        new CommonTestSuiteSet()
    };
  };
}