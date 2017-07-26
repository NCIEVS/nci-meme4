/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  WorklistLookupBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.WorklistClient;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.MEMEException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link WorklistClient} wrapper for looking up worklist names.
 *
 * @author MEME Group
 */

 public class WorklistLookupBean extends ClientBean {

  //
  // Fields
  //

  private WorklistClient client = null;
  private SimpleDateFormat year_format = new SimpleDateFormat("yyyy");

  //
  // Constructors
  //

  /**
   * Instantiates a {@link WorklistLookupBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public WorklistLookupBean() throws MEMEException {
    super();
    client = new WorklistClient();
  }

  //
  // Methods
  //

  /**
   * Returns all current worklists (<B>SERVER CALL</b>).
   * @return all current worklists
   * @throws MEMEException if failed to get worklists
   */
  public Worklist[] getWorklists() throws MEMEException {
    configureClient(client);
    client.setMidService(getMidService());
    return client.getCurrentWorklists();
  }

  /**
   * Returns the year of the specified date.
   * @param date the {@link Date}
   * @return the year of the specified date
   */
  public int getYear(Date date) {
    return Integer.valueOf(year_format.format(date)).intValue();
  }

}