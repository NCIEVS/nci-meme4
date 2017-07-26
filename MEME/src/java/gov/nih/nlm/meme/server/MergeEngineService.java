/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MergeEngineService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MergeFactAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.NextIdentifierAction;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MergeFact;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.ApplicationException;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.ActionEngine;
import gov.nih.nlm.meme.sql.MIDActionEngine;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Handles requests related to merge sets.
 *
 * @author MEME Group
 */
public class MergeEngineService implements MEMEApplicationService {

  //
  // Fields
  //
  public boolean is_running = false;
  public String currently_running_set = null;

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    // Get service request, data source, and function parameter
    MEMEServiceRequest request = context.getServiceRequest();
    String function = (String) request.getParameter("function").getValue();

    // This function processes a merge set.
    // It requires two parameters:
    //  1. a merge set name
    //  2. an authority
    //  3. a work_id
    if (function.equals("process_merge_set")) {
      processMergeSet(context);
    } else if (function.equals("request_log")) {
      StringBuffer log = (StringBuffer) context.get("log");
      request.addReturnValue(
          new Parameter.Default("log", log.toString()));
    } else if (function.equals("terminate")) {
      // do nothing
    }

  } // end processRequest

  /**
   * Processes a merge set.
   * @param context the {@link SessionContext}
   * @throws MEMEException if anything goes wrong.
   */
  private void processMergeSet(SessionContext context) throws MEMEException {

    MEMEServiceRequest request = context.getServiceRequest();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    MIDActionEngine action_engine = (MIDActionEngine) data_source.
        getActionEngine();

    // Keep track of concept_id pairs that have already
    // produced demotions
    HashSet known_to_fail = new HashSet();

    String merge_set =
        (String) request.getParameter("merge_set").getValue();
    Authority authority = data_source.getAuthority(
        (String) request.getParameter("authority").getValue());
    int work_id =
        (int) request.getParameter("work_id").getInt();

    // Make sure only one merge set is running at a time
    synchronized (this) {
      if (is_running) {
        ApplicationException ae = new ApplicationException(
            "Failed to process merge set. Another merge " +
            "set is currently running.");
        ae.setDetail("merge_set", currently_running_set);
        throw ae;
      }
      is_running = true;
      currently_running_set = merge_set;
    }

    // Wrap with a try so we can set
    // is_running=false if anything goes wrong
    StringBuffer log = null;
    try {

      // Get a transaction_id
      NextIdentifierAction nia =
          NextIdentifierAction.newSetNextIdentifierAction(MolecularTransaction.class);
      data_source.getActionEngine().processAction(nia);
      Identifier transaction_id = nia.getNextIdentifier();

      // Get merge set from data source
      SearchParameter[] params = new SearchParameter[2];
      params[0] = new SearchParameter.Single("merge_set", merge_set);
      params[1] = new SearchParameter.Single("status", "R");
      Iterator iter = data_source.findMergeFacts(params);

      // Get merge set's total count to update progress
      String query = "SELECT COUNT(*) AS CT FROM mom_merge_facts" +
          " WHERE merge_set = ? AND status = 'R'";
      PreparedStatement ps = data_source.prepareStatement(query);
      int fact_count = 0;
      ps.setString(1, merge_set);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        fact_count = rs.getInt("CT");
      }
      ps.close();

      // Create StringBuffer to use as a log
      // Add it to the session context
      log = new StringBuffer(1000);
      context.put("log", log);

      // Get an action engine
      ActionEngine engine = data_source.getActionEngine();

      // Process the facts in order
      log.append("-------------------------------------------------\n")
          .append("Starting merge engine ...").append(new Date())
          .append("\n-------------------------------------------------\n")
          .append("Database:  ")
          .append(context.getDataSource().getDataSourceName())
          .append("\n")
          .append("Merge Set: ").append(merge_set).append("\n")
          .append("Authority: ").append(authority).append("\n")
          .append("Work ID:   ").append(work_id).append("\n")
          .append("Trans ID:  ").append(transaction_id).append("\n\n");
      //Arrays.sort(facts);
      int p_cid_1 = 0, p_cid_2 = 0;
      String p_status = "";

      int ctr = 0;
      int progress_ctr = 0;
      while (iter.hasNext()) {

        MergeFact fact = (MergeFact) iter.next();

        MEMEToolkit.trace("Process Fact - " + fact);

        // Get concept ids
        int concept_id_1 =
            fact.getAtom().getConcept().getIdentifier().intValue();
        int concept_id_2 =
            fact.getConnectedAtom().getConcept().getIdentifier().intValue();

        // If concept ids are the same as before
        // then the status will be the same, reuse it
        // and go on to the next case.
        if (p_cid_1 != 0 &&
            ( (p_cid_1 == concept_id_1 && p_cid_2 == concept_id_2) ||
             (p_cid_2 == concept_id_1 && p_cid_1 == concept_id_2))) {

          fact.setStatus(p_status);
        }

        // If the concept ids are equal, we have a P
        else if (concept_id_1 == concept_id_2) {
          // fact is already merged
          fact.setStatus("P");
          log.append("    ").append(concept_id_1).append(" ")
              .append("ALREADY MERGED\n");
        }

        // If the concept_ids have already failed, then just fail
        else if (known_to_fail.contains(concept_id_1 + "|" + concept_id_2)) {
          fact.setStatus("F");
          log.append("    ").append(concept_id_1).append(" into ")
              .append(concept_id_2).append(" ALREADY FAILED\n");
        } else if (known_to_fail.contains(concept_id_2 + "|" + concept_id_1)) {
          fact.setStatus("F");
          log.append("    ").append(concept_id_2).append(" into ")
              .append(concept_id_1).append(" ALREADY FAILED\n");
        }

        // Otherwise, build the action and try
        // to run it, we wind up with either a D or an M
        else {

          // Create a merge action and populate it
          MolecularMergeAction mma = null;

          // Normalize the concept_ids, i.e.
          // Merge higher concept_id into lower concept_id
          if (concept_id_1 > concept_id_2) {
            mma = new MolecularMergeAction(
                fact.getAtom().getConcept(),
                fact.getConnectedAtom().getConcept());
          } else {
            mma = new MolecularMergeAction(
                fact.getConnectedAtom().getConcept(),
                fact.getAtom().getConcept());
          }

          mma.setAuthority(authority);
          mma.setDeleteDuplicateSemanticTypes(false);
          mma.setIntegrityVector(fact.getIntegrityVector());
          mma.setChangeStatus(fact.getChangeStatus());
          mma.setWorkIdentifier(new Identifier.Default(work_id));
          mma.setTransactionIdentifier(transaction_id);

          log.append("    ")
              .append(mma.getSourceIdentifier()).append(" into ")
              .append(mma.getTargetIdentifier()).append(" ... ")
              .append(new Date()).append("  ");

          MEMEToolkit.trace("    " +
                            mma.getSourceIdentifier() + " into " +
                            mma.getTargetIdentifier() + " ... " +
                            new Date() + "  ");

          // Process the merge
          try {
            engine.processAction(mma);

            // merge was successful
            fact.setStatus("M");
            fact.setAction(mma);
            log.append("SUCCESS\n");

          } catch (IntegrityViolationException ive) {
            // merge failed for integrity violations
            fact.setStatus("D");
            fact.setViolationsVector(ive.getViolationsVector());
            known_to_fail.add(
                mma.getSourceIdentifier().toString() + "|" +
                mma.getTargetIdentifier().toString());

            log.append("FAILED\n      ")
                .append(ive.getViolationsVector().toString())
                .append("\n");

            //
            // insert demotion?!
            //
            if (fact.demoteIfMergeFails()) {
              String author = authority.toString();
              if (author.startsWith("ENG-")) {
                author = author.substring(4);
              }
              Relationship demotion = new Relationship.Default();
              demotion.setName("RT");
              demotion.setLevel('P');
              demotion.setSource(new Source.Default(author));
              demotion.setSourceOfLabel(new Source.Default(author));
              demotion.setTobereleased('n');
              demotion.setReleased('N');
              demotion.setAtom(fact.getAtom());
              demotion.setConcept(fact.getAtom().getConcept());
              demotion.getConcept().setReadTimestamp(null);
              demotion.setRelatedAtom(fact.getConnectedAtom());
              demotion.setRelatedConcept(fact.getConnectedAtom().getConcept());
              demotion.getRelatedConcept().setReadTimestamp(null);
              demotion.setStatus('D');
              MolecularInsertRelationshipAction mia =
                  new MolecularInsertRelationshipAction(demotion);
              mia.setAuthority(new Authority.Default(author));
              mia.setWorkIdentifier(new Identifier.Default(work_id));
              mia.setTransactionIdentifier(transaction_id);
              engine.processAction(mia);
              fact.setAction(mia);
            }
          }

        }

        // Update the fact status
        MergeFactAction mfa = MergeFactAction.newSetMergeFactAction(fact);
        action_engine.processAction(mfa);
        p_cid_1 = concept_id_1;
        p_cid_2 = concept_id_2;
        p_status = fact.getStatus();

        // Update the progress
        // each time the progress monitor changes.
        ctr++;
        if (Math.floor( ( (double) ctr / (double) fact_count) * 100) >
            progress_ctr) {
          progress_ctr = (int) (Math.floor( ( (double) ctr /
                                             (double) fact_count) * 100));
          context.updateProgress("Merge set " + progress_ctr + "% complete.",
                                 progress_ctr);
        }

      } // end while loop

    } catch (MEMEException me) {
      is_running = false;
      throw me;
    } catch (Exception e) {
      is_running = false;
      MEMEException me = new MEMEException("Unexpected Exception");
      me.setEnclosedException(e);
      throw me;
    }

    // We are finished, another merge set may go
    is_running = false;

    log.append("-------------------------------------------------\n")
        .append("Finished merge engine ...").append(new Date())
        .append("\n-------------------------------------------------\n");

    request.addReturnValue(
        new Parameter.Default("log", log.toString()));
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return true;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }

}
