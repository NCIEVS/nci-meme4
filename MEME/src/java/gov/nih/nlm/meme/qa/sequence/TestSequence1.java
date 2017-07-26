/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  TestSequence1.java
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa.sequence;

import gov.nih.nlm.meme.action.MolecularDeleteAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Performs a sequence of molecular actions designed to test insertion and deletion.
 */
public class TestSequence1
    extends TestSuite {

  /**
   * Instantiates a {@link TestSequence1}.
   */
  public TestSequence1() {
    setName("TestSequence1");
    setDescription("This performs a basic test atom insertion and deletion");
    setConceptId(161);
  }

  /**
   * Perform a test of insert, delete, and undo.
   * @param client the {@link EditingClient}
   * @param test_concept the test {@link Concept}
   * @throws MEMEException if anything goes wrong
   */
  private void testInsertDeleteUndo(EditingClient client,
                                    Concept test_concept) throws MEMEException {

    //
    // Get timestamp, log start of insert
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog("    0 Insert a new atom to concept... " + date_format.format(timestamp));

    //
    // Get and configure new atom
    //
    Atom test_atom = TestSuiteUtils.createAtom("Test Atom", client);
    test_atom.setConcept(test_concept);
    test_concept.addAtom(test_atom);

    //
    // Insert new atom
    //
    MolecularInsertAtomAction insert_atom_action = new
        MolecularInsertAtomAction(test_atom);
    client.processAction(insert_atom_action);

    //
    // Validate insertion (check that atom is there).
    //
    Concept current_concept = client.getConcept(test_concept.getIdentifier());

    if (!current_concept.contains(test_atom))
      throw new MEMEException("Insert atom action failed");

    //
    // Log Result
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("      0.a Atom insertion is successful... " +
             date_format.format(timestamp));

    //
    // Log start of delete test
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("    1 Delete the newly created atom... " +
             date_format.format(timestamp));


    //
    // delete the atom
    //
    MolecularDeleteAtomAction delete_atom_action = new
        MolecularDeleteAtomAction(test_atom);
    client.processAction(delete_atom_action);

    //
    // Verify deletion, check that atom is gone.
    //
    current_concept = client.getConcept(test_concept.getIdentifier());
    if (current_concept.contains(test_atom))
      throw new MEMEException("Delete atom action failed");

    //
    // Log delete result
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("      1.a Atom deletion is successful... " +
             date_format.format(timestamp));

    //
    // Log start of undo delete
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("    2 Undo step 1... " + date_format.format(timestamp));

    //
    // undo the delete atom action
    //
    client.processUndo(delete_atom_action);

    //
    // Verify undo delete, atom should be there
    //
    current_concept = client.getConcept(test_concept.getIdentifier());

    if (!current_concept.contains(test_atom))
      throw new MEMEException("Undo delete atom action failed");

    //
    // Log undo delete result
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("      2.a Undo step 1 is successful... " +
             date_format.format(timestamp));


    //
    // Log undo insert start
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("    3 Undo step 0... " + date_format.format(timestamp));

    //
    // undo the insert atom action
    //
    client.processUndo(insert_atom_action);

    //
    // Verify undo insert, atom should be gone
    //
    current_concept = client.getConcept(test_concept.getIdentifier());

    if (current_concept.contains(test_atom))
      throw new MEMEException("Undo insert atom action failed");

    //
    // Log result
    //
    timestamp.setTime(System.currentTimeMillis());
    addToLog("      3.a Undo step 0 is successful... " +
             date_format.format(timestamp));
  }

  /**
   * Performs the test.
   */
  public void run(){
    EditingClient client = null;
    TestSuiteUtils.printHeader(this);
    try {
      //
      // Get an editing client
      //
      client = getClient();

      //
      //Get a concept
      //
      Concept test_concept = client.getConcept(this.getConceptId());

      //
      //test insert delete undo atom
      //
      testInsertDeleteUndo(client, test_concept);

      addToLog(this.getName() + " passed");

    } catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

}