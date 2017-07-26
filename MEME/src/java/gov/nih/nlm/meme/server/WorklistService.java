/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  WorklistService
 *
 *****************************************************************************/
package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.action.ChecklistAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.NextIdentifierAction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.action.WorklistAction;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.sql.MIDActionEngine;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Handles requests relating to worklists or checklists.  This class
 * contains functionality for finding worklists and checklists
 * based on a variety of criteria.  Additionally it can be used
 * to create, destroy, and stamp worklists and checklists.
 *
 * @author MEME Group
 */
public class WorklistService implements MEMEApplicationService {

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
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();
    String function = (String) request.getParameter("function").getValue();


    if (function.equals("current_worklists")) {
      request.addReturnValue(
          new Parameter.Default("current_worklists",
                                getCurrentWorklists(data_source)));

    } else if (function.equals("worklists")) {
      request.addReturnValue(
          new Parameter.Default("worklists", getWorklists(data_source)));

    } else if (function.equals("worklist_names")) {
      request.addReturnValue(
          new Parameter.Default("worklist_names", getWorklistNames(data_source)));

    } else if (function.equals("checklists")) {
      request.addReturnValue(
          new Parameter.Default("checklists", getChecklists(data_source)));

    } else if (function.equals("checklist_names")) {
      request.addReturnValue(
          new Parameter.Default("checklist_names",
                                getChecklistNames(data_source)));

    } else if (function.equals("atom_worklist")) {
      String name = (String) request.getParameter("atom_worklist").getValue();
      if (name != null) {
        AtomWorklist atom_worklist = data_source.getAtomWorklist(name);
        request.addReturnValue(new Parameter.Default("atom_worklist",
            atom_worklist));
      }

    } else if (function.equals("atom_checklist")) {
      String name = (String) request.getParameter("atom_checklist").getValue();
      if (name != null) {
        AtomChecklist atom_checklist = data_source.getAtomChecklist(name);
        request.addReturnValue(new Parameter.Default("atom_checklist",
            atom_checklist));
      }

    } else if (function.equals("concept_worklist")) {
      String name = (String) request.getParameter("concept_worklist").getValue();
      if (name != null) {
        ConceptWorklist concept_worklist = data_source.getConceptWorklist(name);
        request.addReturnValue(new Parameter.Default("concept_worklist",
            concept_worklist));
      }

    } else if (function.equals("concept_checklist")) {
      String name = (String) request.getParameter("concept_checklist").getValue();
      if (name != null) {
        ConceptChecklist concept_checklist = data_source.getConceptChecklist(
            name);
        request.addReturnValue(new Parameter.Default("concept_checklist",
            concept_checklist));
      }

    } else if (function.equals("add_atom_worklist")) {
      AtomWorklist worklist = (AtomWorklist) request.getParameter(
          "atom_worklist").getValue();
      if (worklist != null) {
        WorklistAction wa = WorklistAction.newAddAtomWorklistAction(worklist);
        ( (MIDActionEngine) (data_source.getActionEngine())).processAction(wa);
      }

    } else if (function.equals("add_concept_worklist")) {
      ConceptWorklist worklist = (ConceptWorklist) request.getParameter(
          "concept_worklist").getValue();
      if (worklist != null) {
        WorklistAction wa = WorklistAction.newAddConceptWorklistAction(worklist);
        ( (MIDActionEngine) (data_source.getActionEngine())).processAction(wa);
      }

    } else if (function.equals("add_atom_checklist")) {
      AtomChecklist checklist = (AtomChecklist) request.getParameter(
          "atom_checklist").getValue();
      if (checklist != null) {
        ChecklistAction ca = ChecklistAction.newAddAtomChecklistAction(
            checklist);
        ( (MIDActionEngine) (data_source.getActionEngine())).processAction(ca);
      }

    } else if (function.equals("add_concept_checklist")) {
      ConceptChecklist checklist = (ConceptChecklist) request.getParameter(
          "concept_checklist").getValue();
      if (checklist != null) {
        ChecklistAction ca = ChecklistAction.newAddConceptChecklistAction(
            checklist);
        ( (MIDActionEngine) (data_source.getActionEngine())).processAction(ca);
      }

    } else if (function.equals("worklist_exist")) {
      String worklist_name = (String) request.getParameter("worklist_name").
          getValue();
      if (worklist_name != null) {
        boolean worklist_exist = data_source.worklistExists(worklist_name);
        request.addReturnValue(new Parameter.Default("worklist_exist",
            worklist_exist));
      }

    } else if (function.equals("checklist_exist")) {
      String checklist_name = (String) request.getParameter("checklist_name").
          getValue();
      if (checklist_name != null) {
        boolean checklist_exist = data_source.checklistExists(checklist_name);
        request.addReturnValue(new Parameter.Default("checklist_exist",
            checklist_exist));
      }

    } else if (function.equals("remove_worklist")) {
      String worklist_name = (String) request.getParameter("worklist_name").
          getValue();
      if (worklist_name != null) {
        WorklistAction wa = WorklistAction.newRemoveWorklistAction(
            worklist_name);
        ( (MIDActionEngine) (data_source.getActionEngine())).processAction(wa);
      }

    } else if (function.equals("remove_checklist")) {
      String checklist_name = (String) request.getParameter("checklist_name").
          getValue();
      if (checklist_name != null) {
        ChecklistAction ca = ChecklistAction.newRemoveChecklistAction(
            checklist_name);
        ( (MIDActionEngine) (data_source.getActionEngine())).processAction(ca);
      }

    } else if (function.equals("stamp_worklist")) {

      // Extract parameter
      Authority auth = (Authority) request.getParameter("auth").getValue();
      String worklist_name = (String) request.getParameter("worklist_name").
          getValue();

      // Define and set MolecularTransaction
      MolecularTransaction transaction = new MolecularTransaction();
      NextIdentifierAction nia =
          NextIdentifierAction.newSetNextIdentifierAction(MolecularTransaction.class);
      data_source.getActionEngine().processAction(nia);
      Identifier transaction_id = nia.getNextIdentifier();
      transaction.setIdentifier(transaction_id);

      // Define and set WorkLog
      WorkLog worklog = data_source.newWorkLog(auth,
                                               "MAINTENANCE",
                                               "Stamp worklist " +
                                               worklist_name);
      worklog.addSubAction(transaction);

      if (worklist_name != null) {
        ConceptWorklist worklists = data_source.getConceptWorklist(
            worklist_name);
        Concept[] concepts = worklists.getConcepts();
        EnforcableIntegrityVector eiv =
            (EnforcableIntegrityVector) data_source.getApplicationVector(
            "APPROVAL");
        for (int i = 0; i < concepts.length; i++) {
          MolecularApproveConceptAction maca = new
              MolecularApproveConceptAction(concepts[i]);
          maca.setSource(concepts[i]);
          maca.setTransactionIdentifier(transaction_id);
          maca.setWorkIdentifier(worklog.getIdentifier());
          maca.setAuthority(auth);
          maca.setIntegrityVector(eiv);
          data_source.getActionEngine().processAction(maca);
          transaction.addSubAction(maca);
        }
      }

      // Return value
      request.addReturnValue(new Parameter.Default("transaction", transaction));
    }

  } // end processRequest

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
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

  //
  // Private Methods
  //

  /**
   * Returns all current {@link Worklist}s.
   * @param data_source the {@link MIDDataSource}
   * @return all current {@link Worklist}s
   * @throws DataSourceException if failed to get current worklists
   */
  private Worklist[] getCurrentWorklists(MIDDataSource data_source) throws
      DataSourceException {
    String name = "to_char(nvl(stamp_date, sysdate), 'yyyy')";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
    Identifier year = new Identifier.Default(sdf.format(new Date()));
    SearchParameter sp = new SearchParameter.Range(name, year, null);
    List worklists = new ArrayList();
    Iterator iterator = data_source.findWorklists(new SearchParameter[] {sp});
    while (iterator.hasNext()) {
      worklists.add( (Worklist) iterator.next());

    }
    return (Worklist[]) worklists.toArray(new Worklist[0]);
  }

  /**
   * Returns all {@link Worklist}s.
   * @param data_source the {@link MIDDataSource}
   * @return all {@link Worklist}s
   * @throws DataSourceException if failed to get worklists
   */
  private Worklist[] getWorklists(MIDDataSource data_source) throws
      DataSourceException {
    SearchParameter sp = new SearchParameter.Single("1", "1");
    List worklists = new ArrayList();
    Iterator iterator = data_source.findWorklists(new SearchParameter[] {sp});
    while (iterator.hasNext()) {
      worklists.add( (Worklist) iterator.next());

    }
    return (Worklist[]) worklists.toArray(new Worklist[0]);
  }

  /**
   * Returns all worklist names.
   * @param data_source the {@link MIDDataSource}
   * @return all worklist names
   * @throws DataSourceException if failed to get worklist name
   */
  private String[] getWorklistNames(MIDDataSource data_source) throws
      DataSourceException {
    SearchParameter sp = new SearchParameter.Single("1", "1");
    List worklist_names = new ArrayList();
    Iterator iterator = data_source.findWorklistNames(sp);
    while (iterator.hasNext()) {
      worklist_names.add( (String) iterator.next());

    }
    return (String[]) worklist_names.toArray(new String[0]);
  }

  /**
   * Returns all {@link Checklist}s.
   * @param data_source the {@link MIDDataSource}
   * @return all {@link Checklist}s
   * @throws DataSourceException if failed to get checklists
   */
  private Checklist[] getChecklists(MIDDataSource data_source) throws
      DataSourceException {
    SearchParameter sp = new SearchParameter.Single("1", "1");
    List checklists = new ArrayList();
    Iterator iterator = data_source.findChecklists(new SearchParameter[] {sp});
    while (iterator.hasNext()) {
      checklists.add( (Checklist) iterator.next());

    }
    return (Checklist[]) checklists.toArray(new Checklist[0]);
  }

  /**
   * Returns all checklist names.
   * @param data_source the {@link MIDDataSource}
   * @return all checklist names
   * @throws DataSourceException if failed to get checklist names
   */
  private String[] getChecklistNames(MIDDataSource data_source) throws
      DataSourceException {
    SearchParameter sp = new SearchParameter.Single("1", "1");
    List checklist_names = new ArrayList();
    Iterator iterator = data_source.findChecklistNames(sp);
    while (iterator.hasNext()) {
      checklist_names.add( (String) iterator.next());

    }
    return (String[]) checklist_names.toArray(new String[0]);
  }

}
