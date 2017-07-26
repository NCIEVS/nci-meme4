package gov.nih.nlm.mrd.beans;

import gov.nih.nlm.meme.beans.ClientBean;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.client.FullMRConceptReportClient;
import gov.nih.nlm.mrd.client.ConceptReportClient;

import java.text.SimpleDateFormat;


public class ConceptBean extends ClientBean {

  //
  // Fields
  //
  private ConceptReportClient conceptReportClient = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ConceptBean}.
   * @throws MEMEException if failed to construct this class
   */
  public ConceptBean() throws MEMEException {
    super();
    conceptReportClient = new FullMRConceptReportClient("");
  }

  //
  // Methods
  //

  /**
   * Returns a fully configured {@link ConceptReportClient}.
   * @return a fully configured {@link ConceptReportClient}
   */
  public ConceptReportClient getConceptReportClient() {
    configureClient(conceptReportClient);
    conceptReportClient.setMidService(getMidService());
    return conceptReportClient;
  }

  /**
   * Sets the generator class name.
   * @param class_name the generator class name
   */
  public void setConceptGenerator(String class_name) throws MEMEException {
    try {
    	conceptReportClient = (ConceptReportClient) Class.forName(class_name).newInstance();
    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Class could not be loaded.", e);
      ie.setDetail("class_name", class_name);
      throw ie;
    }
  }

  /**
   * Returns the standard {@link SimpleDateFormat}.
   * @return the standard {@link SimpleDateFormat}
   */
  public SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat("MM/dd/yyyy");
  }

  /**
   * Returns the HTML code <code>SELECTED</code>
   * if the specified host matches the client bean host.
   * @param host the  host to check
   * @return the SELECTED code or ""
   */
  public String getHostSelectedFlag(String host) {
    if (host.equals(getHost())) {
      return "SELECTED";
    } else {
      return "";
    }
  }

}
