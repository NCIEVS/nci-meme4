/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  ActionDetailsBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.meme.exception.MEMEException;

/**
 * Container for "Action Harvester" function to view a molecular action
 * report.  Makes use of the {@link ReportsClient} and tracks a molecule id
 * parameter.
 *
 * @author MEME Group
 */

public class ActionDetailsBean extends ClientBean {

  //
  // Fields
  //

  private ReportsClient client = null;
  private int molecule_id = 0;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link ActionDetailsBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public ActionDetailsBean() throws MEMEException {
    super();
    client = new ReportsClient();
  }

  //
  // Methods
  //

  /**
   * Returns the molecule id.
   * @return the molecule id
   */
  public int getMoleculeId() {
    return molecule_id;
  }

  /**
   * Set the molecule id.
   * @param molecule_id the molecule id
   */
  public void setMoleculeId(int molecule_id) {
    if (molecule_id > 0) {
      this.molecule_id = molecule_id;
    }
  }

  /**
   * Uses a {@link ReportsClient} to return the action report document
   * for the molecule id specified by {@link #setMoleculeId(int)} (<B>SERVER CALL</B>).
   * This report contains details of what a molecular action did to change the
   * MID.
   * @return the action report document for the specified molecule id
   * @throws MEMEException if failed to get action report.
   */
  public String getActionReport() throws MEMEException {
    configureClient(client);
    client.setMidService(getMidService());
    return client.getActionReportDocument(molecule_id);
  }

}