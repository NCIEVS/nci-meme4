/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ChangeAtomTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Dec 23, 2003: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 *
 * This test MolecularChangeAtomAction
 */
public class ChangeAtomActionTest
    extends TestSuite {

  public ChangeAtomActionTest() {
    setName("ChangeAtomActionTest");
    setDescription("This test the MolecularChangeAtomAction");
    setConceptId(115);
  }

  /**
   * Perform tests on tobereleased and status
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());
      Atom test_atom = test_concept.getAtoms()[0];

      unitTest(date_format, client, test_atom, 'Y', 'R');
      unitTest(date_format, client, test_atom, 'N', 'R');
      unitTest(date_format, client, test_atom, 'N', 'N');
      unitTest(date_format, client, test_atom, 'Y', 'N');

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
   * This is the unit test for this class
   * @param date_format
   * @param client
   * @param test_atom
   * @param tbr
   * @param status
   * @throws MEMEException
   */
  private void unitTest(SimpleDateFormat date_format, EditingClient client,
                        Atom test_atom, char tbr, char status) throws
      MEMEException {
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog(
        "    Case Status = " + status + "; tobereleased = " + tbr + "... " +
        date_format.format(timestamp));

    test_atom = client.getAtom(test_atom.getIdentifier());
    char previous_tbr = test_atom.getTobereleased();
    char previous_status = test_atom.getStatus();

    test_atom.setTobereleased(tbr);
    test_atom.setStatus(status);

    MolecularChangeAtomAction action = new MolecularChangeAtomAction(
        test_atom);
    client.processAction(action);

    test_atom = client.getAtom(test_atom.getIdentifier());
    if (test_atom.getTobereleased() != tbr)
      throw new MEMEException("Atom tobereleased did not change to " + tbr);
    if (test_atom.getStatus() != status)
      throw new MEMEException("Atom status did not change to " + status);

    client.processUndo(action);

    test_atom = client.getAtom(test_atom.getIdentifier());
    if (test_atom.getTobereleased() != previous_tbr)
      throw new MEMEException("Atom tobereleased did not undo to " +
                              previous_tbr);
    if (test_atom.getStatus() != previous_status)
      throw new MEMEException("Atom status did not undo to " + previous_status);

    timestamp.setTime(System.currentTimeMillis());
    addToLog("      success... " + date_format.format(timestamp));
  }
}