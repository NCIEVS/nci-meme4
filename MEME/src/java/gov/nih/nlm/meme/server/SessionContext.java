/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  SessionContext
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.client.ClientProgressEvent;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.meme.xml.ObjectXMLSerializer;

import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for all the session information.
 *
 * @author MEME Group
 */
public class SessionContext extends HashMap {

  //
  // Fields
  //

  private MEMEDataSource session_data_source = null;
  private MEMEApplicationServer server = null;
  private Date last_use = new Date();
  private boolean is_running = false;
  private long timeout = 0;
  private String session_id = null;
  private Map request_by_thread = new HashMap();
  private Map ds_by_thread = new HashMap();
  private MEMEServiceRequest latest_request = null;

  //
  // Methods
  //

  /**
   * Returns the service request.
   * @return the service request
   */
  public MEMEServiceRequest getServiceRequest() {
    return (MEMEServiceRequest) request_by_thread.get(Thread.currentThread());
  }

  /**
   * Removes the service request.
   */
  public void removeServiceRequest() {
    request_by_thread.remove(Thread.currentThread());
  }

  /**
   * Sets the {@link MEMEServiceRequest}.
   * @param request the {@link MEMEServiceRequest}
   */
  public void setServiceRequest(MEMEServiceRequest request) {
    session_id = request.getSessionId();
    timeout = request.getTimeout();
    request_by_thread.put(Thread.currentThread(), request);
    latest_request = request;
  }

  /**
   * Returns the {@link MEMEDataSource} for this request.
   * @return the {@link MEMEDataSource} for this request
   */
  public MEMEDataSource getDataSource() {
    return (MEMEDataSource) ds_by_thread.get(Thread.currentThread());
  }

  /**
   * Returns the default {@link MEMEDataSource} for this session.
   * @return the default {@link MEMEDataSource} for this session
   */
  public MEMEDataSource getSessionDataSource() {
    return session_data_source;
  }

  /**
   * Sets the {@link MEMEDataSource} for this request.
   * @param data_source the {@link MEMEDataSource} for this request
   */
  public void setDataSource(MEMEDataSource data_source) {
    ds_by_thread.put(Thread.currentThread(), data_source);
  }

  /**
   * Removes the {@link MEMEDataSource} for this request.
   */
  public void removeDataSource() {
    ds_by_thread.remove(Thread.currentThread());
  }

  /**
   * Sets the default {@link MEMEDataSource} for the session.
   * @param data_source the default {@link MEMEDataSource} for the session
   */
  public void setSessionDataSource(MEMEDataSource data_source) {
    session_data_source = data_source;
  }

  /**
   * Returns the {@link MEMEApplicationServer}.
   * @return the {@link MEMEApplicationServer}
   */
  public MEMEApplicationServer getServer() {
    return this.server;
  }

  /**
   * Sets the {@link MEMEApplicationServer}.
   * @param server the {@link MEMEApplicationServer}
   */
  public void setServer(MEMEApplicationServer server) {
    this.server = server;
  }

  /**
   * Returns the last use.
   * @return thes last use
   */
  public Date getLastUse() {
    return last_use;
  }

  /**
   * Sets the last use.
   * @param last_use the last use
   */
  public void setLastUse(Date last_use) {
    this.last_use = last_use;
  }

  /**
   * Frees resources associated with terminated session.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void terminateSession() throws BadValueException {
    if (session_data_source != null) {
      ServerToolkit.returnDataSource(session_data_source);
    }
    session_data_source = null;
  }

  /**
   * Sets the flag indicating whether or not this session is currently running a request.
   * @param is_running <code>true</code> if so, <code>false</code> otherwise
   */
  public void setIsRunning(boolean is_running) {
    this.is_running = is_running;
  }

  /**
   * Indicates whether or not this session is currently running a request.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isRunning() {
    return is_running;
  }

  /**
   * Indicates whether or not this session is expired.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isExpired() {
    // if timeout is not set, then use default
    if (timeout == 0) {
      timeout = Long.valueOf(ServerToolkit.getProperty(
          ServerConstants.MEME_SESSION_TIMEOUT)).longValue();
    }

    // Return true, if the value returned by subtracting the current and last use time
    // exceeds the allowable time for a session.
    if (timeout != -1 && (new Date().getTime() - last_use.getTime()) > timeout) {
      return true;
    }
    return false;
  }

  /**
   * Returns the session id.
   * @return the session id
   */
  public String getSessionId() {
    return session_id;
  }

  /**
   * Updates the progress monitor being displayed in the client.  Sends
   * a {@link ClientProgressEvent} across the socket connection to the client.
   * @param message the message to be displayed in the progress monitor
   * @param progress the progress value
   * @throws MEMEException if failed to update the progress
   */
  public void updateProgress(String message, int progress) throws MEMEException {
    put("progress", new Integer(progress));
    put("progress_message", message);
    ClientProgressEvent cpe = new ClientProgressEvent(message, progress);
    try {
      Writer writer = getServiceRequest().getWriter();
      ObjectXMLSerializer oxs = new ObjectXMLSerializer();
      String doc = oxs.toXML(cpe);
      writer.write("HTTP/1.1 200 PROGRESS\n");
      writer.write("Content-Length: ");
      writer.write(String.valueOf(doc.getBytes("UTF-8").length));
      writer.write(";\nContent-Type: text/xml;\n\n");
      writer.write(doc);
      writer.write("\n");
      writer.flush();
    } catch (MEMEException me) {
      throw me;
    } catch (Exception e) {
      ExternalResourceException ere =
          new ExternalResourceException("Error sending progress report", e);
      throw ere;
    }
  }

  /**
   * Returns the latest request to be processed.
   * @return the latest request to be processed
   */
  public MEMEServiceRequest getLatestRequest() {
    return latest_request;
  }
}
