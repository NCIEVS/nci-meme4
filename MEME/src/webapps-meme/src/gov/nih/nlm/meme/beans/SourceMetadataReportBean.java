package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.client.ReportsClient;

public class SourceMetadataReportBean
    extends ClientBean {
  //
  // Fields
  //

  private ReportsClient rc = null;

  //
  // Constructors
  //

  /**
   * SourceMetadataReportBean
   * @throws MEMEException if failed to construct this class.
   */
  public SourceMetadataReportBean() throws MEMEException {
    super();
    rc = new ReportsClient();
  }

  //
  // Methods
  //

  /**
   * Returns a fully configured {@link AuxiliaryDataClient}.
   * @return a fully configured {@link AuxiliaryDataClient}
   */
  public ReportsClient getReportsClient() {
    configureClient(rc);
    rc.setMidService(getMidService());
    return rc;
  }

  /**
   * Sets the mid service.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    super.setMidService(mid_service);
    if (rc != null) {
      try {
        rc = new ReportsClient(mid_service);
      }
      catch (MEMEException me) {
        throw new RuntimeException("Unexcepted Error", me);
      }
    }
  }
}
