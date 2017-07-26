/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  SequenceTestSuiteSet.java
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.sequence.TestSequence1;
import gov.nih.nlm.meme.qa.sequence.TestSequence2;

/**
 * Represents a set of test suites designed to test molecular action sequences.
 * @see TestSequence1
 * @see TestSequence2
 */
public class SequenceTestSuiteSet extends TestSuiteSet {

  /**
   * Instantiates an empty {@link SequenceTestSuiteSet}.
   */
  public SequenceTestSuiteSet() { }

  /**
   * Returns the test set name.
   * @return the test set name
   */
  public String getName() {
    return "SequenceTestSuiteSet";
  }

  /**
   * Returns the test set description.
   * @return the test set description
   */
  public String getDescription() {
    return "This test action sequences";
  }

  /**
  * Returns the {@link ITestSuite}s that are part of this set.
  * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
        new TestSequence1(),
        new TestSequence2()
    };
  }
}