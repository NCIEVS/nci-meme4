/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  UITestSuiteSet
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.ui.TestSuiteAUI;
import gov.nih.nlm.meme.qa.ui.TestSuiteAtuiC;
import gov.nih.nlm.meme.qa.ui.TestSuiteAtuiS;
import gov.nih.nlm.meme.qa.ui.TestSuiteAtuiSG;
import gov.nih.nlm.meme.qa.ui.TestSuiteAtuiSG2;
import gov.nih.nlm.meme.qa.ui.TestSuiteAtuiTBR;
import gov.nih.nlm.meme.qa.ui.TestSuiteRuiC;
import gov.nih.nlm.meme.qa.ui.TestSuiteRuiP;
import gov.nih.nlm.meme.qa.ui.TestSuiteRuiS;
import gov.nih.nlm.meme.qa.ui.TestSuiteRuiSG;
import gov.nih.nlm.meme.qa.ui.TestSuiteRuiTBR;

/**
 * Represents a set of test suites designed to test the complete functionality
 * of the molecular actions.
 */
public class UITestSuiteSet extends TestSuiteSet {

  /**
   * Instantiates an empty {@link UITestSuiteSet}.
   */
  public UITestSuiteSet() { }

  /**
   * Returns the test set name.
   * @return the test set name
   */
  public String getName() {
    return "UITestSuiteSet";
  }

  /**
   * Returns the test set description.
   * @return the test set description
   */
  public String getDescription() {
    return "QA Tests for UIs";
  }

  /**
   * Returns the {@link ITestSuite}s that are part of this set.
   * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
        new TestSuiteAUI(),              // 0
        new TestSuiteAtuiC(),            // 1
        new TestSuiteAtuiS(),            // 2
        new TestSuiteAtuiSG(),           // 3
        new TestSuiteAtuiSG2(),          // 4
        new TestSuiteAtuiTBR(),          // 5
        new TestSuiteRuiC(),             // 6
        new TestSuiteRuiP(),             // 7
        new TestSuiteRuiS(),             // 8
        new TestSuiteRuiSG(),            // 9
        new TestSuiteRuiTBR()            // 10
        // SRC tests no longer used
        //  new TestSuiteAtuiSRC(),
        //  new TestSuiteAtuiTbrSrc(),
        //  new TestSuiteRuiSRC(),
        //  new TestSuiteRuiTbrSrc()
        // NLM03 tests no longer used
        //  new TestSuiteRuiNlm03(),
    };
  }

}