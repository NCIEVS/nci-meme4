/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  IntegrityViolationException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.integrity.ViolationsVector;

/**
 * Generically represents an exception caused by a failure while
 * checking integrity constraints.  This type of exception can happen
 * when processing an action and should be used to stop the action from
 * proceeding.
 *
 * @author MEME Group
 */
public class IntegrityViolationException extends ActionException {

  //
  // Fields
  //
  private ViolationsVector vv = null;

  /**
   * Instantiates an {@link IntegrityViolationException} with the specified {@link ViolationsVector}.
   * @param vv the specified {@link ViolationsVector}
   */
  public IntegrityViolationException(ViolationsVector vv) {
    super("Failed to pass integrity checks.");
    setDetail("violations_vector", vv);
    this.vv = vv;
    setInformAdministrator(false);
    setPrintStackTrace(false);
  }

  /**
   * Returns the {@link ViolationsVector}.
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector getViolationsVector() {
    return vv;
  }

}
