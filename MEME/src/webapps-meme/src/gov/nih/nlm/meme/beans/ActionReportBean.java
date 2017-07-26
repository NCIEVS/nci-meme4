/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  ActionReportBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.client.FinderClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.MEMEException;

/**
 * {@link FinderClient} wrapper for "Action Harvester" used to to find {@link MolecularAction}s.
 *
 * @author MEME Group
 */

public class ActionReportBean extends ActionHarvesterBean {

  //
  // Fields
  //

  private FinderClient client = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ActionReportBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public ActionReportBean() throws MEMEException {
    super();
    client = new FinderClient();
  }

  //
  // Methods
  //

  /**
   * Returns all actions matching the specified set of parameters (<B>SERVER CALL</b>).
   * @return all actions matching the specified set of parameters
   * @throws MEMEException if failed to get actions.
   */
  public MolecularAction[] getActions() throws MEMEException {

    configureClient(client);
    client.setMidService(getMidService());

    if (start_date != null || end_date != null)
      client.restrictByDateRange(start_date, end_date);
    if (concept_id > 0)
      client.restrictByConcept(new Concept.Default(concept_id));
    if (recursive != null)
      client.setRecursive(true);
    client.setMaxResultCount(row_count==null ? -1 : Integer.valueOf(row_count).intValue());
    if (transaction_id > 0)
      client.restrictByTransaction(new MolecularTransaction(transaction_id));
    if (worklist != null) {
      Worklist wl = new Worklist();
      wl.setName(worklist);
      client.restrictByWorklist(wl);
    }
    if (authority != null)
      client.setAuthority(new Authority.Default(authority));

    if (molecular_action != null) {
      MolecularAction ma = new MolecularAction();
      ma.setActionName(molecular_action);
      client.restrictByActionType(ma);
    }

    if (core_table != null)
      try {
      client.restrictByCoreDataType(Class.forName(
        "gov.nih.nlm.meme.common." + core_table));
      } catch (Exception e) {}

    return client.findMolecularActions();
  }

}