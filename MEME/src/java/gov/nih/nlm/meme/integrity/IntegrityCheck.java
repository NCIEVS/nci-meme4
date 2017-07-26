/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  IntegrityCheck
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Generically represents an integrity check.
 *
 * @author MEME Group
 */

public interface IntegrityCheck {

  //
  // Fields
  //

  public final static String TERMGROUP_TYPE = "TERMGROUP";
  public final static String SOURCE_TYPE = "SOURCE";
  public final static String STY_TYPE = "STY";

  //
  // Methods
  //

  /**
   * Returns the cloned object.
   * @return the cloned object
   */
  public Object clone();

  /**
   * Returns the check name.
   * @return the check name
   */
  public String getName();

  /**
   * Sets the check name.
   * @param ic_name the check name
   */
  public void setName(String ic_name);

  /**
   * Indicates whether or not the check is active.
       * @return <code>true</code> if the check is active; <code>false</code> otherwise
   */
  public boolean isActive();

  /**
   * Sets the flag indicating whether or not the check is active.
   * @param ic_status a <code>boolean</code> flag indicating whether or not the check is active
   */
  public void setIsActive(boolean ic_status);

  /**
   * Indicates whether or not the violation indicate a fatal problem.
   * @return <code>true</code> if the violation indicate fatal problem;
   * <code>false</code> otherwise
   */
  public boolean isFatal();

  /**
   * Sets the flag indicating whether or not a violation of this check should
   * be considiered fatal
       * @param ic_type a <code>boolean</code> flag indicating if the check is fatal
   */
  public void setIsFatal(boolean ic_type);

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
   * Returns the short description.
   * @return the short description
   */
  public String getShortDescription();

  /**
   * Sets the short description.
   * @param ic_short_dsc the short descrption
   */
  public void setShortDescription(String ic_short_dsc);

  /**
   * Returns the description.
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * @param ic_long_dsc the description
   */
  public void setDescription(String ic_long_dsc);

  //
  // Inner Classes
  //

  /**
   * Default implementation`
   */
  public class Default implements IntegrityCheck {

    //
    // Fields
    //

    private String ic_name;
    private boolean ic_status;
    private boolean ic_type;
    private Date timestamp;
    private String ic_short_dsc;
    private String ic_long_dsc;

    //
    // Constructors
    //

    /**
     * Instantiates an empty {@link IntegrityCheck.Default} implementation.
     */
    public Default() {};

    /**
     * Instantiates an {@link IntegrityCheck.Default} implementation with the specified name.
     * @param ic_name integrity check name
     */
    public Default(String ic_name) {
      this.ic_name = ic_name;
    }

    //
    // Overriden Object Methods
    //

    /**
     * Returns an integer hashcode.
     * @return an integer hashcode
     */
    public int hashCode() {
      return getName().hashCode();
    }

    /**
     * Indicates whether or not the specified {@link Object} is equal
     * to this one.
     * @param o  the {@link Object} to compare to
     * @return <code>true</code> if the objects are equal, <code>false</code> otherwise
     */
    public boolean equals(Object o) {
      if (o instanceof IntegrityCheck) {
        return getName().equals( ( (IntegrityCheck) o).getName());
      }
      return false;
    }

    //
    // Methods
    //

    /**
     * Clones the check.
     * @return the clone
     */
    public Object clone() {
      IntegrityCheck ic = new IntegrityCheck.Default();
      ic.setName(getName());
      ic.setIsActive(isActive());
      ic.setIsFatal(isFatal());
      ic.setTimestamp(getTimestamp());
      ic.setShortDescription(getShortDescription());
      ic.setDescription(getDescription());
      return ic;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
      return ic_name;
    }

    /**
     * Sets the name.
     * @param ic_name the name
     */
    public void setName(String ic_name) {
      this.ic_name = ic_name;
    }

    /**
     * Indicates whether or not the check is active.
         * @return <code>true</code> if the check is active, <code>false<code> otherwise
     */
    public boolean isActive() {
      return ic_status;
    }

    /**
     * Sets the flag indiciating whether or not the check is active.
         * @param ic_status a <code>boolean</code> flag indicating whether or not the
     * check is active
     */
    public void setIsActive(boolean ic_status) {
      this.ic_status = ic_status;
    }

    /**
     * Indicates whether or not the check is fatal.
         * @return <code>true</code> if the check is fatal, <code>false</code> otherwise
     */
    public boolean isFatal() {
      return ic_type;
    }

    /**
     * Sets the flag indiciating whether or not the check is fata.
     * @param ic_type a <code>boolean</code> flag indicating whether or not the
     * check is fatal
     */
    public void setIsFatal(boolean ic_type) {
      this.ic_type = ic_type;
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
     * Returns the short description.
     * @return the short description
     */
    public String getShortDescription() {
      return ic_short_dsc;
    }

    /**
     * Sets the short description.
     * @param ic_short_dsc the short description
     */
    public void setShortDescription(String ic_short_dsc) {
      this.ic_short_dsc = ic_short_dsc;
    }

    /**
     * Returns the description.
     * @return the description
     */
    public String getDescription() {
      return ic_long_dsc;
    }

    /**
     * Sets the description.
     * @param ic_long_dsc the description
     */
    public void setDescription(String ic_long_dsc) {
      this.ic_long_dsc = ic_long_dsc;
    }

    /**
     * Self-qa test.
     * @param args command line arguments
     */
    public static void main(String[] args) {

      try {
        MEMEToolkit.initialize(null, null);
      } catch (InitializationException ie) {
        MEMEToolkit.handleError(ie);
      }
      MEMEToolkit.setProperty(MEMEConstants.DEBUG, "true");

      //
      // Main Header
      //

      MEMEToolkit.trace(
          "-------------------------------------------------------");
      MEMEToolkit.trace("Starting test of IntegrityCheck ..." + new Date());
      MEMEToolkit.trace(
          "-------------------------------------------------------");

      // Create a IntegrityCheck object to work with
      IntegrityCheck ic = new IntegrityCheck.Default();

      boolean failed = false;

      String test_message = "Testing IntegrityCheck-";
      String test_result = null;

      ic.setName("IC1");
      if (ic.getName().equals("IC1")) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result + "set/getName().");

      ic.setIsActive(true);
      if (ic.isActive()) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result + "set/isActive().");

      ic.setIsFatal(true);
      if (ic.isFatal()) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result + "set/isFatal().");

      ic.setTimestamp(new Date());
      if (ic.getTimestamp() != null) {
        test_result = " PASSED: ";
        if (ic.isActive()) {
          MEMEToolkit.trace(">>>>>>>Activation date= " + ic.getTimestamp());
        } else {
          MEMEToolkit.trace(">>>>>>>Deactivation date= " + ic.getTimestamp());
        }
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result + "set/getTimestamp().");

      ic.setShortDescription("Short description");
      if (ic.getShortDescription().equals("Short description")) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result +
                        "set/getShortDescription().");

      ic.setDescription("Description of atom.");
      if (ic.getDescription().equals("Description of atom.")) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result + "set/getDescription().");

      Object ic_clone = ic.clone();
      if (ic.clone().equals(ic_clone)) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result + "clone().");

      //
      // Main Footer
      //

      MEMEToolkit.trace("");

      if (failed) {
        MEMEToolkit.trace("AT LEAST ONE TEST DID NOT COMPLETE SUCCESSFULLY");
      } else {
        MEMEToolkit.trace("ALL TESTS PASSED");

      }
      MEMEToolkit.trace("");

      MEMEToolkit.trace(
          "-------------------------------------------------------");
      MEMEToolkit.trace("Finished test of IntegrityCheck ..." + new Date());
      MEMEToolkit.trace(
          "-------------------------------------------------------");

    }
  }

}
