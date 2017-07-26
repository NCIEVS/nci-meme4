/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  EditingReportBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.meme.exception.MEMEException;

/**
 * {@link ReportsClient} wrapper for generating editing report data.
 *
 * @author MEME Group
 */

public class EditingReportBean extends ActionHarvesterBean {

  //
  // Fields
  //

  private ReportsClient client = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link EditingReportBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public EditingReportBean() throws MEMEException {
    super();
    client = new ReportsClient();
  }

  //
  // Methods
  //

  /**
   * Returns editing report data for the start and end dates specified (<B>SERVER CALL</b>).
   * @return the editing report data for the start and end dates specified
   * @throws MEMEException if failed to produce editing report data
   */
  public String[][] getEditingReportData() throws MEMEException {

    configureClient(client);
    client.setMidService(getMidService());
    String[][] report = null;
    if (getWorklist() != "") {
      String worklist = getWorklist();
      try {
        start_date = MEMEToolkit.getDateFormat().parse(getStartDate());
        end_date = MEMEToolkit.getDateFormat().parse(getEndDate());
      } catch (Exception e) {
        // do nothing
      }
      report = client.getEditingReportData(worklist, start_date, end_date);
    } else {
      report = client.getEditingReportData(getDaysAgo());
    }

    return report;
  }

}