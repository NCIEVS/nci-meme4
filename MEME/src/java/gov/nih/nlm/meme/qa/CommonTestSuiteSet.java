/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  CommonTestSuiteSet
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.common.TestSuiteAtom;
import gov.nih.nlm.meme.qa.common.TestSuiteAtomChecklist;
import gov.nih.nlm.meme.qa.common.TestSuiteAtomCluster;
import gov.nih.nlm.meme.qa.common.TestSuiteAtomWorklist;
import gov.nih.nlm.meme.qa.common.TestSuiteAttribute;
import gov.nih.nlm.meme.qa.common.TestSuiteAuthority;
import gov.nih.nlm.meme.qa.common.TestSuiteChecklist;
import gov.nih.nlm.meme.qa.common.TestSuiteCluster;
import gov.nih.nlm.meme.qa.common.TestSuiteComponentMetaData;
import gov.nih.nlm.meme.qa.common.TestSuiteConcept;
import gov.nih.nlm.meme.qa.common.TestSuiteConceptChecklist;
import gov.nih.nlm.meme.qa.common.TestSuiteConceptCluster;
import gov.nih.nlm.meme.qa.common.TestSuiteConceptMapping;
import gov.nih.nlm.meme.qa.common.TestSuiteConceptSemanticType;
import gov.nih.nlm.meme.qa.common.TestSuiteConceptWorklist;
import gov.nih.nlm.meme.qa.common.TestSuiteContextPath;
import gov.nih.nlm.meme.qa.common.TestSuiteContextRelationship;
import gov.nih.nlm.meme.qa.common.TestSuiteCoocurrence;
import gov.nih.nlm.meme.qa.common.TestSuiteCoreData;
import gov.nih.nlm.meme.qa.common.TestSuiteEditorPreferences;
import gov.nih.nlm.meme.qa.common.TestSuiteIdentifier;
import gov.nih.nlm.meme.qa.common.TestSuiteLanguage;
import gov.nih.nlm.meme.qa.common.TestSuiteLocator;
import gov.nih.nlm.meme.qa.common.TestSuiteMEMEString;
import gov.nih.nlm.meme.qa.common.TestSuiteMapObject;
import gov.nih.nlm.meme.qa.common.TestSuiteMapSet;
import gov.nih.nlm.meme.qa.common.TestSuiteMergedFact;
import gov.nih.nlm.meme.qa.common.TestSuiteMeshEntryTerm;
import gov.nih.nlm.meme.qa.common.TestSuiteMetaCode;
import gov.nih.nlm.meme.qa.common.TestSuiteMetaProperty;
import gov.nih.nlm.meme.qa.common.TestSuiteNativeIdentifier;
import gov.nih.nlm.meme.qa.common.TestSuiteParameter;
import gov.nih.nlm.meme.qa.common.TestSuitePasswordAuthenticator;
import gov.nih.nlm.meme.qa.common.TestSuiteRank;
import gov.nih.nlm.meme.qa.common.TestSuiteRelationship;
import gov.nih.nlm.meme.qa.common.TestSuiteReportStyle;
import gov.nih.nlm.meme.qa.common.TestSuiteSafeReplacementFact;
import gov.nih.nlm.meme.qa.common.TestSuiteSource;
import gov.nih.nlm.meme.qa.common.TestSuiteTermgroup;
import gov.nih.nlm.meme.qa.common.TestSuiteWarning;

/**
 * Represents a set of test suites designed to test the complete functionality
 * of the molecular actions.
 */
public class CommonTestSuiteSet extends TestSuiteSet {

  /**
   * Instantiates an empty {@link CommonTestSuiteSet}.
   */
  public CommonTestSuiteSet() { }

  /**
   * Returns the test set name.
   * @return the test set name
   */
  public String getName() {
    return "CommonTestSuiteSet";
  }

  /**
   * Returns the test set description.
   * @return the test set description
   */
  public String getDescription() {
    return "QA Tests for Commons";
  }

  /**
   * Returns the {@link ITestSuite}s that are part of this set.
   * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
        new TestSuiteAtom(),                 // 0
        new TestSuiteAtomChecklist(),        // 1
        new TestSuiteAtomCluster(),          // 2
        new TestSuiteAtomWorklist(),         // 3
        new TestSuiteAttribute(),            // 4
        new TestSuiteAuthority(),            // 5
        new TestSuiteChecklist(),            // 6
        new TestSuiteCluster(),              // 7
        new TestSuiteComponentMetaData(),    // 8
        new TestSuiteConcept(),              // 9
        new TestSuiteConceptChecklist(),     // 10
        new TestSuiteConceptCluster(),       // 11
        new TestSuiteConceptMapping(),       // 12
        new TestSuiteConceptSemanticType(),  // 13
        new TestSuiteConceptWorklist(),      // 14
        new TestSuiteContextPath(),          // 15
        new TestSuiteContextRelationship(),  // 16
        new TestSuiteCoocurrence(),          // 17
        new TestSuiteCoreData(),             // 18
        new TestSuiteCoreData(),             // 19 - DUMMY because TestSuiteDoseFormAtom was removed
        new TestSuiteEditorPreferences(),    // 20
        new TestSuiteIdentifier(),           // 21
        new TestSuiteLanguage(),             // 22
        new TestSuiteLocator(),              // 23
        new TestSuiteMergedFact(),           // 24
        new TestSuiteMapObject(),            // 25
        new TestSuiteMapSet(),               // 26
        new TestSuiteMEMEString(),           // 27
        new TestSuiteMEMEString(),           // 28
        new TestSuiteMeshEntryTerm(),        // 29
        new TestSuiteMetaCode(),             // 30
        new TestSuiteMetaProperty(),         // 31
        new TestSuiteNativeIdentifier(),     // 32
        new TestSuiteParameter(),            // 33
        new TestSuitePasswordAuthenticator(),// 34
        new TestSuiteRank(),                 // 35
        new TestSuiteRelationship(),         // 36
        new TestSuiteReportStyle(),          // 37
        new TestSuiteReportStyle(),          // 38
        new TestSuiteCoreData(),             // 39 - DUMMY because TestSuiteRxNormAtom was removed
        new TestSuiteSafeReplacementFact(),  // 40
        new TestSuiteTermgroup(),            // 41
        new TestSuiteSource(),               // 42
        new TestSuiteWarning()
    };
  }

}