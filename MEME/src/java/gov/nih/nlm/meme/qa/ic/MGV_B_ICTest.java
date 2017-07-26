/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  MGV_B_ICTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Nov 21, 2003: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * The MGV_B class validates those merging two concepts that
 * both contain releasable atoms from the same source and that source is
 * listed in ic_single.
 *
 * Truth Table for test cases
 * ------------------------------------------
 * R1 R2 S I V
 * Y  Y  Y Y Y
 * Y  Y  Y N N
 * Y  Y  N Y N
 * Y  Y  N N N
 * Y  N  Y Y N
 * Y  N  Y N N
 * Y  N  N Y N
 * Y  N  N N N
 * N  Y  Y Y N
 * N  Y  Y N N
 * N  Y  N Y N
 * N  Y  N N N
 * N  N  Y Y N
 * N  N  Y N N
 * N  N  N Y N
 * N  N  N N N
 *
 * notes:
 * C - is chemical
 * R - is Releasable
 * S - Source is the same
 * I - source is in the ic_single table
 *
 */
public class MGV_B_ICTest
    extends TestSuite {

  private int concept_id_2 = 121;

  public MGV_B_ICTest() {
    setName("MGV_B_ICTest");
    setDescription(
        "The MGV_B class validates those merging two concepts that"
        +
        " both contain releasable atoms from the same source and that source is "
        + "listed in ic_single.");
    setConceptId(120);
  }

  /**
   * Perform the test
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    addToLog("  Helpful notes if an error occurs");
    addToLog("  R - is releasable");
    addToLog("  S - source is the same");
    addToLog("  I - source is in the ic_single table");
    addToLog("  test case format:");
    addToLog("  R1 R2 S I");
    addToLog("    ");

    try {
      //setup
      EditingClient client = getClient();
      EnforcableIntegrityVector integrity_vector = new
          EnforcableIntegrityVector();
      integrity_vector.addIntegrityCheck(client.getIntegrityCheck("MGV_B"),
                                         "E");
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();

      Concept concept1 = client.getConcept(this.getConceptId());
      Concept concept2 = client.getConcept(concept_id_2);
      Atom atom1 = concept1.getAtoms()[0];
      Atom atom2 = concept2.getAtoms()[0];

      Source src1 = new Source.Default();
      src1.setSourceAbbreviation("MTH");
      Source src2 = new Source.Default();
      src2.setSourceAbbreviation("MTH");

      atom1.setTobereleased('Y');
      atom2.setTobereleased('Y');

      atom1.setSource(src1);
      atom2.setSource(src2);

      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MTH", 'Y', "MTH", true);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MDR", 'Y', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MTH", 'Y', "M11", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MTH", 'Y', "M11", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "OTHER", 'Y', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MTH", 'N', "MTH", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MDR", 'N', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MTH", 'N', "M11", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'Y', "MDR", 'N', "OTHER", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "MTH", 'Y', "MTH", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "MDR", 'Y', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "MTH", 'Y', "M11", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "MDR", 'Y', "OTHER", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "MTH", 'N', "MTH", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "MDR", 'N', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "M11", 'N', "MDR", false);
      unitTest(date_format, integrity_vector, concept1, concept2, atom1, atom2,
               src1, src2, 'N', "OTHER", 'N', "MDR", false);

      addToLog(this.getName() + " passed");
    }
    catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

  private void unitTest(SimpleDateFormat date_format,
                        EnforcableIntegrityVector integrity_vector,
                        Concept concept1, Concept concept2, Atom atom1,
                        Atom atom2, Source src1, Source src2, char tbr1,
                        String abbr1, char tbr2, String abbr2, boolean fatal) throws
      MEMEException {
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog("    Case tbr1 = " + tbr1 + "; src1 = " + src1 +
             "; tbr2 = " + tbr2 + "; src2 = " + src2 + "..." +
             date_format.format(timestamp));
    atom1.setTobereleased(tbr1);
    atom2.setTobereleased(tbr2);
    src1.setSourceAbbreviation(abbr1);
    src2.setSourceAbbreviation(abbr2);
    ViolationsVector violation = integrity_vector.applyMergeInhibitors(
        concept1, concept2);
    if (violation.isFatal() != fatal)
      throw new MEMEException("Integrity check failed");

    timestamp.setTime(System.currentTimeMillis());
    addToLog("      2.a success ..." + date_format.format(timestamp));

  }
}