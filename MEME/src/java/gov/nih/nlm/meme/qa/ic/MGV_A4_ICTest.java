/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  MGV_A4_ICTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Nov 17, 2003: 1st Version.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * This test suite test the MGV_A4 integrity check for merging 2 concepts
 *
 * Truth table to validate MGV_A4
 * ------------------------------------------------------------------------
 * tbr - to be released
 * lrc - last released cui is not null
 * same - if lrc1=lrc2
 *
 * tbr1 lrc1 tbr2 lrc2 same validate
 * ---------------------------------
 *  y    y    y    y    n     y
 *  n    n    n    n    y     n
 *  n    n    n    y    n     n
 *  n    n    y    n    y     n
 *  n    n    y    y    n     n
 *  n    y    n    n    n     n
 *  n    y    n    y    n     n
 *  n    y    y    n    n     n
 *  n    y    y    y    n     n
 *  y    n    n    y    n     n
 *  y    n    y    y    n     n
 *  y    y    n    y    n     n
 *  y    y    y    y    n     n
 *  n    y    n    y    y     n
 *  n    y    y    y    y     n
 *  y    n    n    n    y     n
 *  y    n    y    n    y     n
 *  y    y    n    y    y     n
 *  y    y    y    y    y     n
 *  y    y    n    n    n     n
 *  y    y    y    n    n     n
 *
 */
public class MGV_A4_ICTest
    extends TestSuite {

  private int concept_id_2 = 102;

  public MGV_A4_ICTest() {
    setName("MGV_A4_ICTest");
    setDescription(
        "This test suite test the MGV_A4 integrity check for merging 2 concepts");
    setConceptId(101);
  }

  /**
   * Perform the test
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    addToLog("  Helpful notes if an error occurs");
    addToLog("  tbr - to be released");
    addToLog("  lcr - last cui released is not null");
    addToLog("  same - if lcr1 = lcr2");
    addToLog("  test case format:");
    addToLog("  tbr1 lrc1 tbr2 lrc2 same");
    addToLog("    ");
    try {
      EditingClient client = getClient();
      EnforcableIntegrityVector integrity_vector = new
          EnforcableIntegrityVector();
      integrity_vector.addIntegrityCheck(client.getIntegrityCheck("MGV_A4"),
                                         "E");
      Concept concept1 = client.getConcept(this.getConceptId());
      Concept concept2 = client.getConcept(concept_id_2);
      Atom atom1 = concept1.getAtoms()[0];
      Atom atom2 = concept2.getAtoms()[0];
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();

      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', new CUI("C0000001"), 'y', new CUI("C0000002"), true);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', null, 'n', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', null, 'n', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', null, 'y', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', null, 'y', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', new CUI("C0000001"), 'n', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', new CUI("C0000001"), 'n', new CUI("C0000002"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', new CUI("C0000001"), 'y', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', new CUI("C0000001"), 'y', new CUI("C0000002"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', null, 'n', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', null, 'y', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', new CUI("C0000001"), 'n', new CUI("C0000002"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', new CUI("C0000001"), 'y', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', new CUI("C0000001"), 'n', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'n', new CUI("C0000001"), 'y', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', null, 'n', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', null, 'y', null, false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', new CUI("C0000001"), 'n', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', new CUI("C0000001"), 'y', new CUI("C0000001"), false);
      unitTest(date_format, atom1, atom2, integrity_vector, concept1, concept2,
               'y', new CUI("C0000001"), 'n', null, false);

      addToLog(this.getName() + " passed");
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
   * @param atom1
   * @param atom2
   * @param integrity_vector
   * @param concept1
   * @param concept2
   * @param tbr1
   * @param cui1
   * @param tbr2
   * @param cui2
   * @param fatal
   * @throws MEMEException
   */
  private void unitTest(SimpleDateFormat date_format, Atom atom1, Atom atom2,
                        EnforcableIntegrityVector integrity_vector,
                        Concept concept1, Concept concept2, char tbr1,
                        CUI cui1, char tbr2, CUI cui2, boolean fatal) throws
      MEMEException {
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog("    Case tbr1= " + tbr1 + "; cui1 = " + cui1 +
             "; tbr2 = " + tbr2 + "; cui2 = " + cui2 + "..." +
             date_format.format(timestamp));
    atom1.setTobereleased(tbr1);
    atom1.setLastReleaseCUI(cui1);
    atom2.setTobereleased(tbr2);
    atom2.setLastReleaseCUI(cui2);
    ViolationsVector violation = integrity_vector.applyMergeInhibitors(
        concept1, concept2);
    if (violation.isFatal() != fatal)
      throw new MEMEException("Integrity check failed");
    timestamp.setTime(System.currentTimeMillis());
    addToLog("      success ..." + date_format.format(timestamp));
  }
}