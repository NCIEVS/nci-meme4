/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  LoggedAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents an action that has been logged.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.LoggedAction instead
 */

public interface LoggedAction extends gov.nih.nlm.meme.action.LoggedAction {

  /**
   * This class serves as a default abstract implementation of the
   * interface, implementations should extend it.
   */

  public abstract class Default extends gov.nih.nlm.meme.action.LoggedAction.
      Default implements LoggedAction {

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link LoggedAction}.
     */
    public Default() {
      super();
    }

  }

}
