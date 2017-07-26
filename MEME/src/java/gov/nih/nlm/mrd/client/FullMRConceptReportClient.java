package gov.nih.nlm.mrd.client;

import gov.nih.nlm.meme.exception.MEMEException;

/**
 *
 * @author  MEME Group
 */

public class FullMRConceptReportClient extends ConceptReportClient {

  /**
   * Instantiates a {@link FullMRConceptReportClient}.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public FullMRConceptReportClient() throws MEMEException {
    this("mrd-db");
    setConceptGenerator("ConceptReportGenerator");
  }

  /**
   * Instantiates a {@link FullMRConceptReportClient} pointing to the
   * specified mid service.
   * @param service the mid service name to use
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public FullMRConceptReportClient(String service) throws MEMEException {
    super();
    this.mid_service = service;
  }
}
