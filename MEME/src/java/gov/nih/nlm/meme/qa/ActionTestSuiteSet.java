/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ActionTestSuiteSet.java
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.action.MolecularApproveConceptActionTest;
import gov.nih.nlm.meme.qa.action.MolecularChangeAtomActionTest;
import gov.nih.nlm.meme.qa.action.MolecularChangeAttributeActionTest;
import gov.nih.nlm.meme.qa.action.MolecularChangeConceptActionTest;
import gov.nih.nlm.meme.qa.action.MolecularChangeRelationshipActionTest;
import gov.nih.nlm.meme.qa.action.MolecularDeleteAtomActionTest;
import gov.nih.nlm.meme.qa.action.MolecularDeleteAttributeActionTest;
import gov.nih.nlm.meme.qa.action.MolecularDeleteConceptActionTest;
import gov.nih.nlm.meme.qa.action.MolecularDeleteRelationshipActionTest;
import gov.nih.nlm.meme.qa.action.MolecularInsertAtomActionTest;
import gov.nih.nlm.meme.qa.action.MolecularInsertAttributeActionTest;
import gov.nih.nlm.meme.qa.action.MolecularInsertConceptActionTest;
import gov.nih.nlm.meme.qa.action.MolecularInsertRelationshipActionTest;
import gov.nih.nlm.meme.qa.action.MolecularMergeActionTest;
import gov.nih.nlm.meme.qa.action.MolecularMoveActionTest;
import gov.nih.nlm.meme.qa.action.MolecularSplitActionTest;
//import gov.nih.nlm.meme.qa.action.RecipeTestSuite;

/**
 * Represents a set of test suites designed to test the complete functionality
 * of the molecular actions.
 */
public class ActionTestSuiteSet
    extends TestSuiteSet {

  /**
   * Instantiates an empty {@link ActionTestSuiteSet}.
   */
  public ActionTestSuiteSet() { }

  /**
   * Returns the test set name.
   * @return the test set name
   */
  public String getName() {
    return "ActionTestSuiteSets";
  }

  /**
   * Returns the test set description.
   * @return the test set description
   */
  public String getDescription() {
    return "QA Tests for Molecular Actions";
  }

  /**
   * Returns the {@link ITestSuite}s that are part of this set.
   * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
        new MolecularApproveConceptActionTest(),			// 0
        new MolecularChangeAtomActionTest(),					// 1
        new MolecularChangeAttributeActionTest(),			// 2
        new MolecularChangeConceptActionTest(),				// 3
        new MolecularChangeRelationshipActionTest(),	// 4
        new MolecularDeleteAtomActionTest(),					// 5
        new MolecularDeleteAttributeActionTest(),			// 6
        new MolecularDeleteConceptActionTest(),				// 7
        new MolecularDeleteRelationshipActionTest(),	// 8	
        new MolecularInsertAtomActionTest(),					// 9
        new MolecularInsertAttributeActionTest(),			// 10
        new MolecularInsertConceptActionTest(),				// 11
        new MolecularInsertRelationshipActionTest(),	// 12
        new MolecularMergeActionTest(),								// 13
        new MolecularMoveActionTest(),								// 14
        new MolecularSplitActionTest(),								// 15
    };
  }

}