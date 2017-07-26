/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Warning
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represent a warning.
 *
 * @author MEME Group
 */

public interface Warning {

  /**
   * Returns the code of the warning.
   * @return the code of the warning
   */
  public String getCode();

  /**
   * Sets the code of the warning.
   * @param code the code of the warning
   */
  public void setCode(String code);

  /**
   * Returns the location of the warning.
   * @return the location of the warning
   */
  public String getLocation();

  /**
   * Sets the location of the warning.
   * @param location the location of the warning
   */
  public void setLocation(String location);

  /**
   * Returns the method of the warning.
   * @return the method of the warning
   */
  public String getMethod();

  /**
   * Sets the method of the warning.
   * @param method the method of the warning
   */
  public void setMethod(String method);

  /**
   * Returns the message of the warning.
   * @return the message of the warning
   */
  public String getMessage();

  /**
   * Sets the message of the warning.
   * @param message the message of the warning
   */
  public void setMessage(String message);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link Warning} interface.
   */
  public class Default implements Warning {

    //
    // Fields
    //

    private String code;
    private String location;
    private String method;
    private String message;

    //
    // Constructors
    //

    /**
     * Instantiates a {@link Warning.Default}.
     */
    public Default() {};

    /**
     * Instantiates a {@link Warning.Default}.
     * @param code the code
     * @param location the location
     * @param method the method
     * @param message the message
     */
    public Default(String code, String location, String method, String message) {
      this.code = code;
      this.location = location;
      this.method = method;
      this.message = message;
    }

    //
    // Implementation of Warning interface
    //

    /**
     * Implements {@link Warning#getCode()}.
     * @return the code
     */
    public String getCode() {
      return code;
    }

    /**
     * Implements {@link Warning#setCode(String)}.
     * @param code the code
     */
    public void setCode(String code) {
      this.code = code;
    }

    /**
     * Implements {@link Warning#getLocation()}.
     * @return the location
     */
    public String getLocation() {
      return location;
    }

    /**
     * Implements {@link Warning#setLocation(String)}.
     * @param location the location
     */
    public void setLocation(String location) {
      this.location = location;
    }

    /**
     * Implements {@link Warning#getMethod()}.
     * @return the method
     */
    public String getMethod() {
      return method;
    }

    /**
     * Implements {@link Warning#setMethod(String)}.
     * @param method the method
     */
    public void setMethod(String method) {
      this.method = method;
    }

    /**
     * Implements {@link Warning#getMessage()}.
     * @return the message
     */
    public String getMessage() {
      return message;
    }

    /**
     * Implements {@link Warning#setMessage(String)}.
     * @param message the message
     */
    public void setMessage(String message) {
      this.message = message;
    }

  }
}
