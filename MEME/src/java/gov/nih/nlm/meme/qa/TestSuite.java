/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  TestSuite.java
 *
 * Changes
 *   01/13/2006 BAC (1-73T75): configureClient is more explicit in its 
 *     configuring.
 *     
 *****************************************************************************/

package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.MEMEToolkit;

/**
 * Default implementation of {@link ITestSuite}.
 */
abstract public class TestSuite
    implements ITestSuite {

  /**
   * State variables
   */
  private boolean passed = true;
  private String name = null;
  private String description = null;
  private int concept_id = 100;
  private StringBuffer Log = new StringBuffer();
  private EditingClient client = null;

  /**
   * Instantiates a {@link TestSuite}.
   */
  public TestSuite() {}

  /**
   * Returns a boolean value to indicate if this test suite passed
   * @return <code>true</code>if the test passed; <code>false</code> otherwise
   */
  public boolean isPassed() {
    return passed;
  }

  /**
   * This test failed
   */
  public void thisTestFailed() {
    this.passed = false;
  }

  /**
   * Set the name of the test suite
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the test suite name.
   * @return the test suite name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the test suite description
   * @param desc
   */
  public void setDescription(String desc) {
    this.description = desc;
  }

  /**
   * Returns the test suite description.
   * @return the test suite description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the starting concept id used for testing.
   * @return the starting concept id used for testing
   */
  public int getConceptId() {
    return concept_id;
  }

  /**
   * Sets the starting concept id used for testing.
   * @param concept_id the starting concept id
   */
  public void setConceptId(int concept_id) {
    this.concept_id = concept_id;
  }

  /**
   * Returns the log.  Should be called after {@link #run()}.
   * @return the log
   */
  public String getLog() {
    return Log.toString();
  }

  /**
   * Adds a new line to the log.
   * @param line the line to add
   */
  public void addToLog(String line) {
    Log.append(line + "\n");
    System.out.println(line);
  }

  /**
   * Add the exception to the log.
   * @param e
   */
  public void addToLog(MEMEException e) {
    addToLog(e.getMessage());
    for (int i = 0; i < e.getStackTrace().length; i++)
      addToLog(e.getStackTrace()[i].toString());
  }

  /**
   * Clear the log buffer
   */
  public void clearLog() {
    Log.setLength(0);
  }

  /**
   * Configures the client with the specified host, port, and mid service.
   * @param host the specified host
   * @param port the specified port
   * @param mid_service the specified mid service
   */
  public void configureClient(String host, int port, String mid_service) throws
      MEMEException {
    EditingClient ec = getClient();
    MEMEToolkit.setProperty("meme.client.server.host",host);
    MEMEToolkit.setProperty("meme.client.server.port",String.valueOf(port));
    ec.setMidService(mid_service);
    ec.getRequestHandler().setHost(host);
    ec.getRequestHandler().setPort(port);
    // for debugging
    //Log.append("host = ").append(host);
    //Log.append(" port = ").append(port);
    //Log.append(" midService = ").append(mid_service).append("\n\n");
  }

  /**
   * Returns the {@link EditingClient} used by the test suite.
   * @return the {@link EditingClient} used by the test suite
   */
  public EditingClient getClient() {
    return getClient("");
  }

  /**
   * Returns the {@link EditingClient} used by the test suite.
   * @param mid_service
   * @return the {@link EditingClient} used by the test suite
   */
  public EditingClient getClient(String mid_service) {
    //create the client on the first call
    if (client == null) {
      try {
        if (mid_service == null)
          mid_service = "";
        client = TestSuiteUtils.getClient(mid_service);
      }
      catch (MEMEException e) {
        addToLog(e);
      }
    }
    return client;
  }

}