/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  ActionSequenceBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.exception.MEMEException;

/**
 * {@link AdminClient} wrapper used to perform "action sequences".
 *
 * @author MEME Group
 */

public class ActionSequenceBean extends ClientBean {

  //
  // Fields
  //

  private String sequence = null;
  private int users = 0;
  private String session_id = null;
  private boolean terminate = false;
  private AdminClient client = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link ActionSequenceBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public ActionSequenceBean() throws MEMEException {
    super();
    client = new AdminClient();
  }

  //
  // Methods
  //

  /**
   * Returns the sequence name.
   * @return the sequence name
   */
  public String getSequence() {
    return sequence;
  }

  /**
   * Sets the sequence name.
   * @param sequence the sequence name
   */
  public void setSequence(String sequence) {
    if (sequence != null) {
      this.sequence = sequence;
    }
  }

  /**
   * Returns the number of users to simulate.
   * @return the number of users to simulate
   */
  public int getUsers() {
    return users;
  }

  /**
   * Sets the number of users to simulate.
   * @param users the number of users to simulate
   */
  public void setUsers(int users) {
    this.users = users;
  }

  /**
   * Returns the session id.
   * @return the session id
   */
  public String getSessionId() {
    return session_id;
  }

  /**
   * Sets the session id.
   * @param session_id the session id
   */
  public void setSessionId(String session_id) {
    if (session_id != null) {
      this.session_id = session_id;
    }
  }

  /**
   * Indicates whether or not to terminate the session.
   * @return <code>true</code> if the session should be terminated,
   * <code>false</code> otherwise
   */
  public boolean getTerminate() {
    return terminate;
  }

  /**
   * Sets the value indicating whether or not to terminate the session.
   * @param terminate <code>true</code> if the session should be terminated,
   * <code>false</code> otherwise
   */
  public void setTerminate(boolean terminate) {
    this.terminate = terminate;
  }

  /**
   * Returns the fully configured {@link AdminClient}
   * used to perform the action sequence.
   * @return the {@link AdminClient}
   */
  public AdminClient getAdminClient() {
    configureClient(client);
    client.setMidService(getMidService());
    return client;
  }

}