/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ChangeAttributeActionTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Nov 24, 2003: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 *
 *This test the MolecularChangeAttributeAction
 */
public class ChangeAttributeActionTest
    extends TestSuite {

  public ChangeAttributeActionTest() {
    setName("ChangeAttributeActionTest");
    setDescription("This test the MolecularChangeAttributeAction");
    setConceptId(131);
  }

  /**
   * This method contains unit tests for status, suppressible, and tobereleased fields of attributes
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());
      Atom test_atom = test_concept.getAtoms()[0];
      Attribute test_attr = TestSuiteUtils.createAttribute("Test Attribute",
          test_atom, client);
      test_attr.setConcept(test_concept);
      test_atom.addAttribute(test_attr);
      MolecularInsertAttributeAction insert_attr_action = new
          MolecularInsertAttributeAction(test_attr);
      client.processAction(insert_attr_action);
      test_attr = client.getAtom(test_atom).getAttributesByName(test_attr.
          getName())[0];

      unitTest(date_format, client, test_concept, test_attr, 'R', "E", 'Y');
      unitTest(date_format, client, test_concept, test_attr, 'N', "N", 'N');
      unitTest(date_format, client, test_concept, test_attr, 'N', "N", 'Y');
      unitTest(date_format, client, test_concept, test_attr, 'N', "E", 'N');
      unitTest(date_format, client, test_concept, test_attr, 'N', "E", 'Y');
      unitTest(date_format, client, test_concept, test_attr, 'R', "N", 'N');
      unitTest(date_format, client, test_concept, test_attr, 'R', "N", 'Y');
      unitTest(date_format, client, test_concept, test_attr, 'R', "E", 'N');

      client.processUndo(insert_attr_action);

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
   * This method contain the unit test for MolecularChangeAttributeAction
   * @param date_format
   * @param client
   * @param test_concept
   * @param test_attr
   * @param status
   * @param suppressible
   * @param tobereleased
   * @throws MEMEException
   */
  private void unitTest(SimpleDateFormat date_format, EditingClient client,
                        Concept test_concept, Attribute test_attr, char status,
                        String suppressible, char tobereleased) throws
      MEMEException {
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog(
        "    Status = " + status + "; Suppressible = " + suppressible +
        "; tobereleased = " + tobereleased + ";... " +
        date_format.format(timestamp));

    test_attr = client.getAttribute(test_attr.getIdentifier().intValue());

    char pre_status = test_attr.getStatus();
    String pre_suppressible = test_attr.getSuppressible();
    char pre_tobereleased = test_attr.getTobereleased();

    test_attr.setStatus(status);
    test_attr.setSuppressible(suppressible);
    test_attr.setTobereleased(tobereleased);

    MolecularChangeAttributeAction action = new
        MolecularChangeAttributeAction(test_attr);

    client.processAction(action);
    test_attr = client.getAttribute(test_attr.getIdentifier().intValue());

    if (test_attr.getStatus() != status)
      throw new MEMEException("Status did not change to " + status);
    if (!test_attr.getSuppressible().equals(suppressible))
      throw new MEMEException("Suppressible did not change to " + suppressible);
    if (test_attr.getTobereleased() != tobereleased)
      throw new MEMEException("ToBeReleased did not change to " + tobereleased);

    addToLog("     undo...");
    client.processUndo(action);
    test_attr = client.getAttribute(test_attr.getIdentifier().intValue());

    if (test_attr.getStatus() != pre_status)
      throw new MEMEException("Status did not undo to " + pre_status);
    if (!test_attr.getSuppressible().equals(pre_suppressible))
      throw new MEMEException("Suppressible did not undo to " +
                              pre_suppressible);
    if (test_attr.getTobereleased() != pre_tobereleased)
      throw new MEMEException("ToBeReleased did not undo to " +
                              pre_tobereleased);

    timestamp.setTime(System.currentTimeMillis());
    addToLog("      success... " + date_format.format(timestamp));
  }
}