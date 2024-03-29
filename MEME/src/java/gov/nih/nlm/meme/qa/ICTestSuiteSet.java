/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ICTestSuiteSet.java
 *
 * 12/07/2006 BAC (1-D0BIJ): Remove DT_I8 test.
 * 06/07/2006 RBE (1-BEYL1): Added MGV_SCUI & MGV_SDUI
 * 
 *****************************************************************************/
package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.ic.DT_I10Test;
import gov.nih.nlm.meme.qa.ic.DT_I11Test;
import gov.nih.nlm.meme.qa.ic.DT_I12Test;
import gov.nih.nlm.meme.qa.ic.DT_I13Test;
import gov.nih.nlm.meme.qa.ic.DT_I2Test;
import gov.nih.nlm.meme.qa.ic.DT_I3BTest;
import gov.nih.nlm.meme.qa.ic.DT_I3Test;
import gov.nih.nlm.meme.qa.ic.DT_I4Test;
import gov.nih.nlm.meme.qa.ic.DT_I8Test;
import gov.nih.nlm.meme.qa.ic.DT_M1Test;
import gov.nih.nlm.meme.qa.ic.DT_M3Test;
import gov.nih.nlm.meme.qa.ic.DT_MM2Test;
import gov.nih.nlm.meme.qa.ic.DT_MM3Test;
import gov.nih.nlm.meme.qa.ic.DT_PN2Test;
import gov.nih.nlm.meme.qa.ic.DT_PN3Test;
import gov.nih.nlm.meme.qa.ic.DT_PN4Test;
import gov.nih.nlm.meme.qa.ic.MGV_A4Test;
import gov.nih.nlm.meme.qa.ic.MGV_ADHOCTest;
import gov.nih.nlm.meme.qa.ic.MGV_B2Test;
import gov.nih.nlm.meme.qa.ic.MGV_BTest;
import gov.nih.nlm.meme.qa.ic.MGV_CTest;
import gov.nih.nlm.meme.qa.ic.MGV_DTest;
import gov.nih.nlm.meme.qa.ic.MGV_ETest;
import gov.nih.nlm.meme.qa.ic.MGV_FTest;
import gov.nih.nlm.meme.qa.ic.MGV_GTest;
import gov.nih.nlm.meme.qa.ic.MGV_H1Test;
import gov.nih.nlm.meme.qa.ic.MGV_H2Test;
import gov.nih.nlm.meme.qa.ic.MGV_ITest;
import gov.nih.nlm.meme.qa.ic.MGV_JTest;
import gov.nih.nlm.meme.qa.ic.MGV_KTest;
import gov.nih.nlm.meme.qa.ic.MGV_MMTest;
import gov.nih.nlm.meme.qa.ic.MGV_MTest;
import gov.nih.nlm.meme.qa.ic.MGV_MUITest;
import gov.nih.nlm.meme.qa.ic.MGV_PNTest;
import gov.nih.nlm.meme.qa.ic.MGV_RX1Test;
import gov.nih.nlm.meme.qa.ic.MGV_RX2Test;
import gov.nih.nlm.meme.qa.ic.MGV_RXCUITest;
import gov.nih.nlm.meme.qa.ic.MGV_SCUITest;
import gov.nih.nlm.meme.qa.ic.MGV_SDUITest;
import gov.nih.nlm.meme.qa.ic.MGV_STYTest;
import gov.nih.nlm.meme.qa.ic.MVS_RX3Test;

/**
 * Represents a set of test suites designed to validate the integrity checks.
 */
public class ICTestSuiteSet extends TestSuiteSet{

  /**
   * Instantiates an empty {@link ICTestSuiteSet}.
   */
  public ICTestSuiteSet() { }

  /**
   * Returns the test set name.
   * @return the test set name
   */
  public String getName() {
    return "ICTestSuiteSet";
  }

  /**
   * Returns the test set description.
   * @return the test set description
   */
  public String getDescription() {
    return "This test the Integrity Checks";
  }

  /**
   * Returns the {@link ITestSuite}s that are part of this set.
   * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
        new DT_M1Test(),		// 0
        new DT_M3Test(),		// 1
        new DT_MM2Test(),		// 2
        new DT_MM3Test(),		// 3
        new DT_PN2Test(),		// 4
        new DT_PN3Test(),		// 5
        new DT_PN4Test(),		// 6 
        new DT_I10Test(),		// 7
        new DT_I11Test(),		// 8
        new DT_I12Test(),		// 9
        new DT_I13Test(),		// 10
        new DT_I2Test(),		// 11
        new DT_I3Test(),		// 12
        new DT_I3BTest(),		// 13
        new DT_I4Test(),		// 14
        //new DT_I8Test(),		
        new MGV_A4Test(),		// 15
        new MGV_ADHOCTest(),	// 16
        new MGV_BTest(),		// 17
        new MGV_B2Test(),		// 18
        new MGV_CTest(),		// 19
        new MGV_DTest(),		// 20
        new MGV_ETest(),		// 21
        new MGV_FTest(),		// 22
        new MGV_GTest(),		// 23
        new MGV_H1Test(),		// 24
        new MGV_H2Test(),		// 25
        new MGV_ITest(),		// 26
        new MGV_JTest(),		// 27
        new MGV_KTest(),		// 28
        new MGV_MTest(),		// 29
        new MGV_MMTest(),		// 30
        new MGV_MUITest(),		// 31
        new MGV_PNTest(),		// 32
        new MGV_RXCUITest(),	// 33
        //new MGV_STYTest(),		//
        new MGV_RX1Test(),		// 34
        new MGV_RX2Test(),		// 35
        new MVS_RX3Test(),		// 36
        new MGV_SCUITest(),		// 37
        new MGV_SDUITest() // 38
    };
  }
}