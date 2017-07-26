/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ITestSuite.java
 *
 * Changes
 *   01/13/2006 BAC (1-73T75): add getClient methods
 *   
 *****************************************************************************/
package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.exception.MEMEException;

/**
 * Generically represents a test suite set.  The set provides information
 * about what it tests as well as implementing the actual validaiton check.
 * Implementations of this class are {@link Runnable}.
 */
public interface ITestSuite
    extends Runnable {

  /**
   * Returns a boolean value to indicate if this test suite passed
   * @return <code>true</code>if the test passed; <code>false</code> otherwise
   */
  public boolean isPassed();

  /**
   * This test failed
   */
  public void thisTestFailed();

  /**
   * Set the name of the test suite
   * @param name
   */
  public void setName(String name);

  /**
   * Returns the test suite name.
   * @return the test suite name
   */
  public String getName();

  /**
   * Set the test suite description
   * @param desc
   */
  public void setDescription(String desc);

  /**
   * Returns the test suite description.
   * @return the test suite description
   */
  public String getDescription();

  /**
   * Sets the starting concept id used for testing.
   * @param concept_id the starting concept id
   */
  public void setConceptId(int concept_id);

  /**
   * Returns the starting concept id used for testing.
   * @return the starting concept id used for testing
   **/
  public int getConceptId();

  /**
   * Returns the log.  Should be called after {@link #run()}.
   * @return the log
   */
  public String getLog();

  /**
   * Adds a new line to the log.
   * @param line the line to add
   */
  public void addToLog(String line);

  /**
   * Add the exception to the log.
   * @param e
   */
  public void addToLog(MEMEException e);

  /**
   * Clear the log buffer
   */
  public void clearLog();

  /**
   * Configures the client with the specified host, port, and mid service.
   * @param host the specified host
   * @param port the specified port
   * @param mid_service the specified mid service
   */
  public void configureClient(String host, int port, String mid_service) throws
      MEMEException;

  /**
   * Returns the {@link EditingClient} used by the test suite.
   * @return the {@link EditingClient} used by the test suite
   */
  public EditingClient getClient();

  /**
   * Returns the {@link EditingClient} used by the test suite.
   * @param mid_service
   * @return the {@link EditingClient} used by the test suite
   */
  public EditingClient getClient(String mid_service);
}
  