/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  MGV_ADHOC_ICTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Nov 19, 2003: 1st Version.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 *
 *This class test the MGV_ADHOC integrity check for merging 2 concepts.
 * where one contains a releasable, current version MDR atom
 * and the other has a chemical semantic type.
 *
 *  * Truth Table for test cases
 * ------------------------------------------
 * C1 R1 S1 C2 R2 S2 V
 * N  Y  Y  Y  N  N  Y
 * Y  N  N  N  Y  Y  Y
 * N  N  N  Y  Y  Y  N
 * Y  Y  Y  N  N  Y  N
 *
 * notes:
 * C - is chemical
 * R - is Releasable
 * S - Source is MDR
 *
 */

public class MGV_ADHOC_ICTest
    extends TestSuite {

  private int concept_id_2 = 102;

  public MGV_ADHOC_ICTest() {
    setName("MGV_ADHOC_ICTest");
    setDescription(
        "This test suite test the MGV_ADHOC integrity check for merging 2 concepts"
        + " where one contains a releasable, current version MDR atom "
        + "and the other has a chemical semantic type.");
    setConceptId(101);
  }

  /**
   * Perform the test
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    addToLog("  Helpful notes if an error occurs");
    addToLog("  c - is chemical");
    addToLog("  r - is releasable");
    addToLog("  s - source is MDR");
    addToLog("  test case format:");
    addToLog("  C1 R1 S1 C2 R2 S2");
    addToLog("    ");

    try {
      //setup
      EditingClient client = getClient();
      EnforcableIntegrityVector integrity_vector = new
          EnforcableIntegrityVector();
      integrity_vector.addIntegrityCheck(client.getIntegrityCheck("MGV_ADHOC"),
                                         "E");
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();

      Concept concept1 = client.getConcept(this.getConceptId());
      Concept concept2 = client.getConcept(concept_id_2);
      Atom atom1 = concept1.getAtoms()[0];
      Atom atom2 = concept2.getAtoms()[0];

      //create chemical attributes
      ConceptSemanticType sty1 = new ConceptSemanticType();
      sty1.setTobereleased('Y');
      ConceptSemanticType sty2 = new ConceptSemanticType();
      sty2.setTobereleased('Y');

      Source src1 = new Source.Default();
      Source src2 = new Source.Default();

      concept1.addAttribute(sty1);
      concept2.addAttribute(sty2);

      atom1.setSource(src1);
      atom2.setSource(src2);

      unitTest(date_format, integrity_vector, concept1, concept2, sty1, sty2,
               atom1, atom2, src1, src2, true, 'Y', "MDR", true, 'N', "MTH", true);
      unitTest(date_format, integrity_vector, concept1, concept2, sty1, sty2,
               atom1, atom2, src1, src2, true, 'N', "MTH", false, 'Y', "MDR", true);
      unitTest(date_format, integrity_vector, concept1, concept2, sty1, sty2,
               atom1, atom2, src1, src2, false, 'N', "MTH", true, 'Y', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, sty1, sty2,
               atom1, atom2, src1, src2, true, 'Y', "MDR", false, 'N', "MDR", false);


      addToLog(this.getName() +" passed");
    }
    catch (MEMEException e) {
      thisTestFailed();
        addToLog(e);
        e.setPrintStackTrace(true);
        e.printStackTrace();
    }
  }

  /**
   * Unit test for this class
   * @param date_format
   * @param integrity_vector
   * @param concept1
   * @param concept2
   * @param sty1
   * @param sty2
   * @param atom1
   * @param atom2
   * @param src1
   * @param src2
   * @param isChemical1
   * @param isReleasable1
   * @param source1
   * @param isChemical2
   * @param isReleasable2
   * @param source2
   * @param fatal
   * @throws MEMEException
   */
  private void unitTest(SimpleDateFormat date_format,
                        EnforcableIntegrityVector integrity_vector,
                        Concept concept1, Concept concept2,
                        ConceptSemanticType sty1, ConceptSemanticType sty2,
                        Atom atom1, Atom atom2, Source src1, Source src2,
                        boolean isChemical1,
                        char isReleasable1, String source1, boolean isChemical2,
                        char isReleasable2, String source2, boolean fatal) throws
      MEMEException {
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog("    Case c1 = " + isChemical1 + "; r1 = " +
             isReleasable1 + "; s1 = " + source1 + "; c2 = " +
             isChemical2 + "; r2 = " + isReleasable2 + "; s2 = " +
             source2 + "; isFatal= " + fatal + "..." +
             date_format.format(timestamp));

    sty1.setIsChemical(isChemical1);
    atom1.setTobereleased(isReleasable1);
    src1.setStrippedSourceAbbreviation(source1);
    sty2.setIsChemical(isChemical2);
    atom2.setTobereleased(isReleasable2);
    src2.setStrippedSourceAbbreviation(source2);

    ViolationsVector violation = integrity_vector.applyMergeInhibitors(
        concept1, concept2);
    if (violation.isFatal() != fatal)
      throw new MEMEException("Integrity check failed");
    timestamp.setTime(System.currentTimeMillis());
    addToLog("      success ..." + date_format.format(timestamp));

  }
}