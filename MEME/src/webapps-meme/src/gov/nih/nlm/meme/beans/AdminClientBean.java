/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  AdminClientBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.exception.MEMEException;

/**
 * {@link AdminClient} wrapper used to perform administrative tasks.
 *
 * @author MEME Group
 */

 public class AdminClientBean extends ClientBean {

  //
  // Fields
  //

  private AdminClient ac = null;
  private String action = "";
  private String action_status = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link AdminClientBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public AdminClientBean() throws MEMEException {
    super();
    ac = new AdminClient();
  }

  //
  // Methods
  //

  /**
   * Returns a fully configured {@link AdminClient}.
   * @return a fully configured {@link AdminClient}
   */
  public AdminClient getAdminClient() {
    configureClient(ac);
    ac.setMidService(getMidService());
    return ac;
  }

  /**
   * Returns the action to perform.
   * @return the action to perform
   */
  public String getAction() {
    return action;
  }

  /**
   * Returns the status message regarding the state of the action.
   * @return the status message regarding the state of the action
   */
  public String getActionStatus() {
    return action_status != null ? action_status : "Connected.";
  }

  /**
   * Sets the action to perform and performs the action.
   * @param action the actions to perform
   * @throws MEMEException if anything goes wrong
   */
  public void setAction(String action) throws MEMEException {
    this.action = action;
    performAction();
  }

  /**
   * Performs the action (<B>SERVER CALL</b>).
   * @throws MEMEException if anything goes wrong
   */
  public void performAction() throws MEMEException {
    configureClient(ac);
    ac.setMidService(getMidService());
    if (action.equals("disable_editing")) {
      ac.disableEditing();
      action_status = "Editing was successfully disabled.";
    } else
    if (action.equals("enable_editing")) {
      ac.enableEditing();
      action_status = "Editing was successfully enabled.";
    }
    if (action.equals("disable_integrity")) {
      ac.disableIntegritySystem();
      action_status = "Integrity system was successfully disabled.";
    } else
    if (action.equals("enable_integrity")) {
      ac.enableIntegritySystem();
      action_status = "Integrity system was successfully enabled.";
    }
    if (action.equals("disable_validate_atomic_action")) {
      ac.disableAtomicActionValidation();
      action_status = "Validate atomic action was successfully disabled.";
    } else
    if (action.equals("enable_validate_atomic_action")) {
      ac.enableAtomicActionValidation();
      action_status = "Validate atomic action was successfully enabled.";
    }
    if (action.equals("disable_validate_molecular_action")) {
      ac.disableMolecularActionValidation();
      action_status = "Validate molecular action was successfully disabled.";
    } else
    if (action.equals("enable_validate_molecular_action")) {
      ac.enableMolecularActionValidation();
      action_status = "Validate molecular action was successfully enabled.";
    } else
    if (action.equals("refresh_caches")) {
      ac.refreshCaches();
      MIDServices.refreshCache();
      action_status = "Caches were successfully refreshed.";
    }
  }

  /**
   * Sets the "head or tail" value.  Used to restrict log requests to
   * the beginning or end portion of the log.  A positive number indicates
   * the "head" of the log, a negative number indicates the "tail" of the log.
   * @param value the "head or tail" value
   */
  public void setHeadOrTail(String value) {
    int x = Integer.valueOf(value).intValue();
    if (x < 0) ac.setTail(-1 * x);
    else if (x > 0) ac.setHead(x);
  }

}