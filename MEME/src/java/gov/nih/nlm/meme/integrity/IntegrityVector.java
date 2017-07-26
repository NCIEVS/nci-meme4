/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  IntegrityVector
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Generically represents a vector of {@link IntegrityCheck}s used by an application to
 * validate content.  This class includes a default implementation
 * of the interface.
 *
 * @author MEME Group
 */
public interface IntegrityVector {

  //
  // Fields
  //
  public final static String ENFORCE = "E";
  public final static String WARN = "W";
  public final static String CHECK = "C";
  public final static String NONE = "N";

  //
  // Methods
  //

  /**
   * Returns the cloned object.
   * @return the cloned object
   */
  public Object clone();

  /**
   * Returns the {@link IntegrityCheck}s.
   * @return the {@link IntegrityCheck}s
   */
  public IntegrityCheck[] getChecks();

  /**
   * Returns the code for the specified {@link IntegrityCheck}.
   * @param check the {@link IntegrityCheck}
   * @return the <code>int</code> code for the specified check
   */
  public String getCodeForCheck(IntegrityCheck check);

  /**
   * Adds the {@link IntegrityCheck}.
   * @param check the {@link IntegrityCheck}
   * @param code the code for the check
   */
  public void addIntegrityCheck(IntegrityCheck check, String code);

  /**
   * Removes the {@link IntegrityCheck}.
   * @param check the {@link IntegrityCheck} to remove
   */
  public void removeIntegrityCheck(IntegrityCheck check);

  //
  // Inner Classes
  //

  /**
   * Default implementation of {@link IntegrityVector} interface.
   */
  public class Default implements IntegrityVector {

    //
    // Fields
    //

    protected HashMap checks = new HashMap();

    //
    // Constructors
    //

    /**
     * Instantiates an empty {@link IntegrityVector.Default}.
     */
    public Default() {}

    //
    // Overriden Object Methods
    //

    /**
     * Returns an <code>int</code> hashcode
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    /**
     * Indicates whether or not the specified {@link Object} is equal to
     * this {@link IntegrityVector}.
     * @param o the {@link Object} to compare to
     * @return <code>true</code> if the objects are equal; <code>false</code>
     * otherwise
     */
    public boolean equals(Object o) {
      return toString().equals(o.toString());
    }

    /**
     * Returns a string representation.
     * In fact, the representation returned is the one used the MID to
     * represent vectors.
     * @return a {@link String} representation
     */
    public String toString() {
      StringBuffer sb = new StringBuffer(100);
      Iterator iter = checks.keySet().iterator();
      while (iter.hasNext()) {
        IntegrityCheck check = (IntegrityCheck) iter.next();
        sb.append("<");
        sb.append(check.getName());
        sb.append(":");
        String code = (String) checks.get(check);
        sb.append(code);
        sb.append(">");

      }
      return sb.toString();
    }

    //
    // Methods
    //

    /**
     * Returns a clone.
     * @return a clone
     */
    public Object clone() {
      IntegrityVector iv = new IntegrityVector.Default();
      IntegrityCheck[] ic = getChecks();
      for (int i = 0; i < ic.length; i++) {
        iv.addIntegrityCheck( (IntegrityCheck) ic[i].clone(),
                             getCodeForCheck(ic[i]));
      }
      return iv;
    }

    /**
     * Returns the {@link IntegrityCheck}s in this vector.
     * @return the {@link IntegrityCheck}s in this vector
     */
    public IntegrityCheck[] getChecks() {
      IntegrityCheck[] ic = new IntegrityCheck[checks.size()];
      Iterator iter = checks.keySet().iterator();
      int i = 0;
      while (iter.hasNext()) {
        IntegrityCheck check = (IntegrityCheck) iter.next();
        ic[i] = check;
        i++;
      }
      return ic;
    }

    /**
     * Returns the code for the{@link IntegrityCheck}.
     * @param check the {@link IntegrityCheck}.
     * @return the <code>int</code> code for the check
     */
    public String getCodeForCheck(IntegrityCheck check) {
      String code = (String) checks.get(check);
      return ( (code != null) ? code : NONE);
    }

    /**
     * Adds the {@link IntegrityCheck}.
     * @param check the {@link IntegrityCheck}
     * @param code the <code>int</code> code for the check
     */
    public void addIntegrityCheck(IntegrityCheck check, String code) {
      checks.put(check, code);
    }

    /**
     * Removes the {@link IntegrityCheck}.
     * @param check the {@link IntegrityCheck}
     */
    public void removeIntegrityCheck(IntegrityCheck check) {
      checks.remove(check);
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
      MEMEToolkit.trace("Starting test of IntegrityVector ..." + new Date());
      MEMEToolkit.trace(
          "-------------------------------------------------------");

      boolean failed = false;

      String test_message = "Testing IntegrityVector-";
      String test_result = null;

      // Create a IntegrityVector object to work with
      IntegrityVector iv = new IntegrityVector.Default();

      IntegrityCheck ic = new IntegrityCheck.Default();
      ic.setName("IC1");
      ic.setIsActive(true);
      ic.setIsFatal(true);
      ic.setShortDescription("Short description");
      ic.setDescription("Description of atom.");

      iv.addIntegrityCheck(ic, "C");

      IntegrityCheck[] checks = iv.getChecks();

      if (checks.length > 0) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result +
                        "addIntegrityCheck()/getChecks() number of IC=" +
                        checks.length);

      if (iv.getCodeForCheck(ic).equals("C")) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result +
                        "getCodeForCheck(). code= " + iv.getCodeForCheck(ic));

      IntegrityVector iv_clone = (IntegrityVector) iv.clone();
      IntegrityCheck[] clone_checks = iv_clone.getChecks();

      if (clone_checks.length > 0) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result +
                        "clone()/getChecks() number of IC=" +
                        clone_checks.length);

      iv.removeIntegrityCheck(ic);
      checks = iv.getChecks();
      if (checks.length == 0) {
        test_result = " PASSED: ";
      } else {
        test_result = " FAILED: ";
        failed = true;
      }
      MEMEToolkit.trace(test_message + test_result +
                        "removeIntegrityCheck() number of IC=" + checks.length);

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
      MEMEToolkit.trace("Finished test of IntegrityVector ..." + new Date());
      MEMEToolkit.trace(
          "-------------------------------------------------------");

    }
  }

}
