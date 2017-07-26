/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  DBLockTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Jan 6, 2003: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.etc;

import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * This class test the database locking scheme
 */
public class DBLockTest extends TestSuite {
  private int concept_id_2 = 198;

  public DBLockTest() {
    setName("DBLockTest");
    setDescription("This test the db locking schemes");
    setConceptId(199);
  }

  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      // setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());
      Concept test_concept2 = client.getConcept(concept_id_2);
      Atom new_atom = TestSuiteUtils.createAtom("QA Atom", client);
      new_atom.setConcept(test_concept);
      test_concept.addAtom(new_atom);
      Thread[] thread_array = new Thread[20];

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog("    Case 1, action1 inserts a new atom while action2 change the concept status..."
          + date_format.format(timestamp));
      addToLog("      action 2 should finish before action 1");
      thread_array[0] = new AddAtomAction(client,
          (MolecularInsertAtomAction) new MolecularInsertAtomAction(new_atom),
          "case 1 Action 1");
      thread_array[1] = new ChangeConceptAction(client, test_concept2,
          "case 1 Action 2");
      try {
        thread_array[0].start();
        thread_array[1].start();
      } catch (Exception e) {
        e.printStackTrace();
        throw new MEMEException(
            "runtime error for db lock scheme concurrency test");
      }
      // wait for threads to finish
      try {
        thread_array[0].join();
        thread_array[1].join();
      } catch (Exception e) {
      }

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 1 is complete..." + date_format.format(timestamp));
      addToLog("    case 2, stale data test..." + date_format.format(timestamp));
      thread_array[0] = new ChangeConceptAction(client, test_concept2,
          "case 2 Action 1");
      thread_array[1] = new ChangeConceptAction(client, test_concept2,
          "case 2 Action 2");
      try {
        thread_array[0].start();
        thread_array[1].start();
      } catch (Exception e) {
        e.printStackTrace();
        throw new MEMEException(
            "runtime error for db lock scheme concurrency test");
      }
      // wait for threads to finish
      try {
        thread_array[0].join();
        thread_array[1].join();
      } catch (Exception e) {
      }
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 2 is complete..." + date_format.format(timestamp));

      addToLog("    case 3, deadlock test" + date_format.format(timestamp));
      addToLog("      Create concept-level relationships among 10 concepts, spun off 10 threads to insert relationships to the 11th concept");
      // create concept-level relationship among these 10 concepts
      int num_of_concepts = 10;
      int concept_offset = 100;

      for (int i = 0; i < num_of_concepts - 1; i++) {
        for (int j = i + 1; j < num_of_concepts; j++) {
          // refresh the concept to avoid stale data error
          Concept iconcept = client.getConcept(concept_offset + i);
          Concept jconcept = client.getConcept(concept_offset + j);
          Relationship rel = TestSuiteUtils.createConceptLevelRelationship(
              client, iconcept, jconcept);
          client.processAction(new MolecularInsertRelationshipAction(rel));
        }
      }
      // create relationship to the the 11th concept
      for (int i = 0; i < num_of_concepts; i++) {
        // make sure all concepts are refreshed before action
        int target_concept_id = concept_offset + num_of_concepts + 1;
        int source_concept_id = concept_offset + i;
        Concept target_concept = client.getConcept(target_concept_id);
        Concept source_concept = client.getConcept(source_concept_id);
        Relationship rel = TestSuiteUtils.createConceptLevelRelationship(
            client, source_concept, target_concept);

        thread_array[i] = new InsertRelAction(
            client,
            (MolecularInsertRelationshipAction) new MolecularInsertRelationshipAction(
                rel), "case 3.1 add rel btween " + source_concept_id + " and "
                + target_concept_id);
        thread_array[i].start();
      }
      // wait til all the threads are complete
      try {
        for (int i = 0; i < num_of_concepts; i++) {
          thread_array[i].join();
        }
      } catch (Exception e) {
      }
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Merge 10 concepts to 5..."
          + date_format.format(timestamp));
      // merge test
      for (int i = 0; i < num_of_concepts; i = i + 2) {
        int source_concept_id = concept_offset + i;
        int target_concept_id = concept_offset + i + 1;
        thread_array[i] = new MergeAction(client,
            (MolecularMergeAction) new MolecularMergeAction(client
                .getConcept(source_concept_id), client
                .getConcept(target_concept_id)), "case 3.2 merge concepts "
                + source_concept_id + " to " + target_concept_id);
        thread_array[i].start();

        // approve the target concept to test locking scheme, more specifically
        // whether the server lock the concept in order of execution
        thread_array[i + 1] = new ApproveConceptAction(client,
            new MolecularApproveConceptAction(client
                .getConcept(target_concept_id)),
            "case 3.2.1 approve concept action on " + target_concept_id);
        thread_array[i + 1].start();

      }

      // wait til all the threads are complete
      try {
        for (int i = 0; i < num_of_concepts; i = i + 1) {
          thread_array[i].join();
        }
      } catch (Exception e) {
      }

      // cleanup
      BatchMolecularTransaction batch_action = new BatchMolecularTransaction(
          client.getTransaction().getIdentifier().intValue());
      client.setWorkIdentifier(new Identifier.Default(0));
      batch_action.setAuthority(client.getAuthority("L-QA"));
      client.processUndo(batch_action);
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 3 is complete..." + date_format.format(timestamp));

      addToLog(this.getName() + " passed");
    } catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

  /**
   * Inner class used in a Thread to process MolecularApproveConceptAction
   */
  class ApproveConceptAction extends Thread {
    EditingClient client = null;

    String name = null;

    MolecularApproveConceptAction action = null;

    /**
     * Constructor for the inner class
     * 
     * @param client
     * @param action
     * @param name
     */
    ApproveConceptAction(EditingClient client,
        MolecularApproveConceptAction action, String name) {
      this.client = client;
      this.action = action;
      this.name = name;
    }

    /**
     * Process the action
     */
    public void run() {
      try {
        client.processAction(action);
        addToLog("        " + name);
      } catch (MEMEException e) {
        e.setPrintStackTrace(true);
        e.printStackTrace();
      }
    }
  }

  /**
   * Inner class used in a Thread to process MolecularInsertAtomAction
   */
  class AddAtomAction extends Thread {
    EditingClient client = null;

    String name = null;

    MolecularInsertAtomAction action = null;

    /**
     * Constructor for the inner class
     * 
     * @param client
     * @param action
     * @param name
     */
    AddAtomAction(EditingClient client, MolecularInsertAtomAction action,
        String name) {
      this.client = client;
      this.name = name;
      this.action = action;
    }

    /**
     * Process the action
     */
    public void run() {
      try {
        client.processAction(action);
        addToLog("        " + name);
      } catch (MEMEException e) {
        e.setPrintStackTrace(true);
        e.printStackTrace();
      }
    }
  }

  /**
   * 
   * Inner class used in a Thread to process MolecularChangeConceptAction
   */
  class ChangeConceptAction extends Thread {
    EditingClient client = null;

    Concept test_concept = null;

    String name = null;

    /**
     * Constructor for the inner class
     * 
     * @param client
     * @param test_concept
     */
    ChangeConceptAction(EditingClient client, Concept test_concept, String name) {
      this.client = client;
      this.test_concept = test_concept;
      this.name = name;
    }

    /**
     * Process the action
     */
    public void run() {
      try {
        test_concept.setStatus('R');
        MolecularChangeConceptAction action = new MolecularChangeConceptAction(
            test_concept);
        client.processAction(action);
        addToLog("        " + name);
      } catch (MEMEException e) {
        e.setPrintStackTrace(true);
        e.printStackTrace();
      }
    }
  }

  /**
   * 
   * Inner class used in Threads to process MolecularInsertRelationshipAction
   */
  class InsertRelAction extends Thread {
    EditingClient client = null;

    MolecularInsertRelationshipAction action = null;

    String name = null;

    /**
     * Constructor for the inner class
     * 
     * @param client
     * @param test_concept
     */
    InsertRelAction(EditingClient client,
        MolecularInsertRelationshipAction action, String name) {
      this.client = client;
      this.action = action;
      this.name = name;
    }

    /**
     * Process the action
     */
    public void run() {
      try {
        client.processAction(action);
        addToLog("        " + name);
      } catch (MEMEException e) {
        e.setPrintStackTrace(true);
        e.printStackTrace();
      }
    }
  }

  /**
   * 
   * Inner class used in Threads to process MolecularMergeAction
   */
  class MergeAction extends Thread {
    EditingClient client = null;

    MolecularMergeAction action = null;

    String name = null;

    /**
     * Constructor for the inner class
     * 
     * @param client
     * @param test_concept
     */
    MergeAction(EditingClient client, MolecularMergeAction action, String name) {
      this.client = client;
      this.action = action;
      this.name = name;
    }

    /**
     * Process the action
     */
    public void run() {
      try {
        client.processAction(action);
        addToLog("        " + name);
      } catch (MEMEException e) {
        e.setPrintStackTrace(true);
        e.printStackTrace();
      }
    }
  }

}