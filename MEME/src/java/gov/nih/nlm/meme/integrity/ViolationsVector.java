/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  ViolationsVector
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import java.util.Iterator;

/**
 * Generically represents an {@link IntegrityVector}
 * that contains violation codes corresponding to the
 * result of applying the vector of checks to a concept or pair of concepts.
 *
 * @see EnforcableIntegrityVector
 *
 * @author MEME Group
 */
public class ViolationsVector extends IntegrityVector.Default {

  //
  // Fields
  //

  public final static String VIOLATION = "V";
  public final static String WARNING = "W";
  public final static String PASSED = "P";

  //
  // Constructor
  //

  /**
   * Instantiates an empty {@link ViolationsVector}.
   */
  public ViolationsVector() {
    super();
  }

  //
  // Implementation of IntegrityVector
  //

  /**
   * Adds an {@link IntegrityCheck} with the specified code.
   * @param check the {@link IntegrityCheck}
   * @param code the <code>int</code> code
   */
  public void addIntegrityCheck(IntegrityCheck check, String code) {
    // this method transforms ic codes to violation codes
    //int vcode = 0;
    String vcode = code;
    if (code.equals(ENFORCE)) {
      vcode = VIOLATION;
    }
    if (code.equals(WARN)) {
      vcode = WARNING;
    }
    if (code.equals(CHECK)) {
      vcode = PASSED;
    }
    super.addIntegrityCheck(check, vcode);
  }

  /**
   * Removes the {@link IntegrityCheck}.
   * @param check the {@link IntegrityCheck}
   */
  public void removeIntegrityCheck(IntegrityCheck check) {}

  //
  // Methods
  //

  /**
   * Indicates whether or not the specified {@link IntegrityCheck} produced
   * a warning when applied.
   * @param check the {@link IntegrityCheck}
   * @return <code>true</code> if integrity check has a warning,
   * <code>false</code> otherwise
   */
  public boolean isWarning(IntegrityCheck check) {
    return ( (String) checks.get(check)).equals(WARNING);
  }

  /**
   * Indicates whether or not the specified {@link IntegrityCheck} produced
   * a violation when applied.
   * @param check the {@link IntegrityCheck}
   * @return <code>true</code> if integrity check has a violation,
   * <code>false</code> otherwise
   */
  public boolean isViolation(IntegrityCheck check) {
    return ( (String) checks.get(check)).equals(VIOLATION);
  }

  /**
   * Indicates whether or not any of {@link IntegrityCheck}s produced
   * fatal violations.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFatal() {
    Iterator iter = checks.keySet().iterator();
    while (iter.hasNext()) {
      IntegrityCheck ic = (IntegrityCheck) iter.next();
      if (ic.isFatal() && isViolation(ic)) {
        return true;
      }
    }
    return false;
  }

  /**
       * Returns a {@link ViolationsVector} containing checks with a warning status.
   * @return a {@link ViolationsVector} containing checks with a warning status
   */
  public ViolationsVector getWarnings() {
    ViolationsVector vv = new ViolationsVector();
    Iterator iter = checks.keySet().iterator();
    while (iter.hasNext()) {
      IntegrityCheck ic = (IntegrityCheck) iter.next();
      if (isWarning(ic)) {
        vv.addIntegrityCheck(ic, WARNING);
      }
    }
    return vv;
  }

  /**
       * Returns a {@link ViolationsVector} containing checks with a violation status.
       * @return a {@link ViolationsVector} containing checks with a violation status
   */
  public ViolationsVector getViolations() {
    ViolationsVector vv = new ViolationsVector();
    Iterator iter = checks.keySet().iterator();
    while (iter.hasNext()) {
      IntegrityCheck ic = (IntegrityCheck) iter.next();
      if (isViolation(ic)) {
        vv.addIntegrityCheck(ic, VIOLATION);
      }
    }
    return vv;
  }
}
