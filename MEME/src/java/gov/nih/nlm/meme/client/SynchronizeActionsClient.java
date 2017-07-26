/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client.SynchronizeActionsClient
 * Object:  AdminService
 *
 * Changes
 *   01/11/2006 BAC (1-739BX): work to get it running properly
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MEMEDataSourceAction;
import gov.nih.nlm.meme.action.MIDDataSourceAction;
import gov.nih.nlm.meme.action.MacroMolecularAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularSplitAction;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.ObjectXMLSerializer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;

public class SynchronizeActionsClient {

  /**
   * Instantiates an empty {@link SynchronizeActionsClient}.
   */
  public SynchronizeActionsClient() {

  }

  /**
   * DO NOT REMOVE THIS METHOD (used by $MEME_HOME/bin/sync.pl)
   *
   * @param args
   *          An array of string argument.
   */
  public static void main(String[] args) {

    //
    // Usage {"synchronizeActions", "mid", "dir", "force", "clean" }
    //
    if (args.length != 5) {
      MEMEToolkit.setProperty(MEMEConstants.DEBUG, "true");
      System.out.println("PARAMETERS:");
      System.out.println("[0] request type");
      System.out.println("[1] database");
      System.out.println("[2] directory");
      System.out.println("[3] force param (true/false)");
      System.out.println("[4] clean param (true/false)");
      System.exit(1);
    }

    //
    // Extract common arguments
    //
    String database = args[1];
    String dir = args[2];
    boolean force = Boolean.valueOf(args[3]).booleanValue();
    boolean clean = Boolean.valueOf(args[4]).booleanValue();

    if (args[0].equals("synchronizeActions")) {
      try {
        SynchronizeActionsClient sac = new SynchronizeActionsClient();
        sac.synchronizeActions(database, dir, force, clean);
      }
      catch (Throwable t) {
        t.printStackTrace();
        System.err.println("Failed while synchronizing actions");
        System.exit(1);
      }
      System.exit(0);
    }
  }

  /**
   * Reads actions from one MID and applies them to another MID.
   *
   * @param from
   *          the {@link MIDDatasource} to read actions from
   * @param to
   *          the {@link MIDDataSource} to apply actions to
   * @throws ActionException
   *           if anything goes wrong
   * @throws DataSourceException
   *           if anything goes wrong
   */
  public void synchronizeActions(String db, String dir,
                                 boolean force, boolean clean) throws
      MEMEException, IOException {

    //
    // Create Client
    //
    ActionClient ac = new ActionClient(db);
    CoreDataClient cdc = new CoreDataClient(db);
    ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    //
    // Action loop
    //
    File f = new File(dir);
    if (!f.exists()) {
      throw new IOException("Specified directory does not exist");
    }
    if (!f.isDirectory()) {
      throw new IOException("Specified directory is not a directory!");
    }

    String[] files = f.list();
    Arrays.sort(files);

    int progress = 0;
    int total = files.length;
    int ct = 0;
    for (int i = 0; i < files.length; i++) {
      System.out.println("Processing Action: " + files[i]);
      File action_file = new File(dir, files[i]);
      LoggedAction action =
          (LoggedAction) serializer.fromXML(action_file.getPath());

      try {

        //
        // Configure client properly
        //
        ac.setAuthority(action.getAuthority());

        //
        // Perform the action appropriately
        //
        System.out.println("    Sending action to server: " + action);
        if (action instanceof BatchMolecularTransaction) {
          BatchMolecularTransaction ma = (BatchMolecularTransaction) action;
          ac.setWorkIdentifier(ma.getWorkIdentifier());
          ac.processAction( (BatchMolecularTransaction) action);
        }
        else if (action instanceof MacroMolecularAction) {
          MacroMolecularAction ma = (MacroMolecularAction) action;
          ac.setWorkIdentifier(ma.getWorkIdentifier());
          ac.processAction( (MacroMolecularAction) action);
        }
        else if (action instanceof MIDDataSourceAction) {
          ac.processAction( (MIDDataSourceAction) action);
        }
        else if (action instanceof MEMEDataSourceAction) {
          ac.processAction( (MEMEDataSourceAction) action);
        }
        else if (action instanceof MolecularAction) {
          MolecularAction ma = (MolecularAction) action;
          ac.setIntegrityVector(ma.getIntegrityVector());
          ac.setChangeStatus(ma.getChangeStatus());
          ac.setWorkIdentifier(ma.getWorkIdentifier());
          ac.setTransactionIdentifier(ma.getTransactionIdentifier());
          if (ma.getUndoActionOf() != null) {
            ac.processUndo( (MolecularAction) (action.getUndoActionOf()));
          }
          else {
            System.out.println("    Re-reading concepts: " +
                               ma.getSourceIdentifier() +
                               ", " + ma.getTargetIdentifier());
            if (ma.getSource() != null &&
                ! (ma instanceof MolecularInsertConceptAction)) {
              ma.setSource(cdc.getConcept(ma.getSource()));
              cdc.populateRelationships(ma.getSource());
            }
            if (ma.getTarget() != null && ! (ma instanceof MolecularSplitAction)) {
              ma.setTarget(cdc.getConcept(ma.getTarget()));
              cdc.populateRelationships(ma.getTarget());
            }
            action.clearSubActions();
            ac.processAction( (MolecularAction) action);
          }
        }
      }
      catch (MEMEException e) {
        if (force) {
          e.printStackTrace(System.out);
          System.out.println("    Action Failed: " + action.getIdentifier());
          System.out.println("FORCE is enabled, continue processing");
        }
        else {
          throw e;
        }
      }

      //
      // Remove action
      //
      if (clean) {
        System.out.println("    Removing action file: " + action_file);
        action_file.delete();
      }

      //
      // Track progress
      //
      int pct = ( (int) (ct++ / (total * 1.0))) * 100;
      if (pct > progress) {
        progress++;
        System.out.println(ct + " actions processed (" + progress + "%).");
      }
    }
  }
}
