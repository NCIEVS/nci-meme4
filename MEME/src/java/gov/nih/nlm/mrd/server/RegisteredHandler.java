/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server
 * Object:      RegisteredHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.common.Authority;

import java.sql.Date;

/**
 * Generically represents a handler registered to perform MRD tasks.
 * @author Stephanie Halbeisen
 */
public interface RegisteredHandler {

  /**
   * Returns the process name.
   * @return the process name
   */
  public String getProcess();

  /**
   * Sets process name
   * @param process the process name
   */
  public void setProcess(String process);

  /**
   * Returns the type.
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   * @param type the type
   */
  public void setType(String type);

  /**
   * Indicates whether or not the handler is active.
   * @return <code>true</code> if the target is active, <code>false</code> otherwise
   */
  public boolean isActive();

  /**
   * Sets the flag indicating whether or not the handler is active.
   * @param isActive <code>true</code> if the target is active, <code>false</code> otherwise
   */
  public void setIsActive(boolean isActive);

  /**
   * Returns the {@link Authority}.
   * @return the {@link Authority}
   */
  public Authority getAuthority();

  /**
   * Set the {@link Authority}.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority);

  /**
   * Returns the timestamp.
   * @return the timestamp
   */
  public Date getTimestamp();

  /**
   * Sets the timestamp.
   * @param timestamp the timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Returns the handler dependencies.
   * @return the handler dependencies
   */
  public String getDependencies();

  /**
   * Sets the handler dependencies.
   * @param dependencies the handler dependencies
   */
  public void setDependencies(String dependencies);

  /**
   * Default implementation.
   */
  public class Default implements RegisteredHandler {

    // Fields
    private String process = null, type = null, dependencies;
    private boolean isActive;
    private Authority authority;
    private Date timestamp;

    /**
     * Returns the process name.
     * @return the process name
     */
    public String getProcess() {
      return process;
    }

    /**
     * Sets the process name.
     * @param process the process name
     */
    public void setProcess(String process) {
      this.process = process;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public String getType() {
      return type;
    }

    /**
     * Sets the type.
     * @param type the type
     */
    public void setType(String type) {
      this.type = type;
    }

    /**
     * Indicates whether or not the handler is active.
     * @return <code>true</code> if the target is active, <code>false</code> otherwise
     */
    public boolean isActive() {
      return isActive;
    }

    /**
     * Sets the flag indicating whether or not the handler is active.
     * @param isActive <code>true</code> if the target is active, <code>false</code> otherwise
     */
    public void setIsActive(boolean isActive) {
      this.isActive = isActive;
    }

    /**
     * Returns the {@link Authority}.
     * @return the {@link Authority}
     */
    public Authority getAuthority() {
      return authority;
    }

    /**
     * Sets the {@link Authority}.
     * @param authority the {@link Authority}
     */
    public void setAuthority(Authority authority) {
      this.authority = authority;
    }

    /**
     * Returns the timestamp.
     * @return the timestamp
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Sets the timestamp.
     * @param timestamp the timestamp
     */
    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }

    /**
     * Returns the handler dependencies.
     * @return the handler dependencies
     */
    public String getDependencies() {
      return dependencies;
    }

    /**
     * Sets the handler dependencies.
     * @param dependencies the handler dependencies
     */
    public void setDependencies(String dependencies) {
      this.dependencies = dependencies;
    }
  }
}
