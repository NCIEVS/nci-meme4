/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  LanguageAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a language action.
 *
 * @author MEME Group
 */

public class LanguageAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private Language language = null;
  private Language old_language = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link LanguageAction}
   */
  private LanguageAction() {}

  /**
   * Instantiates a {@link LanguageAction} with
   * the specified language.
   * @param language an object {@link Language}
   */
  private LanguageAction(Language language) {
    this.language = language;
  }

  //
  // Methods
  //

  /**
   * Set the action mode.
   * @param mode the action mode
   */
  private void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Performs a new add language action
   * @param language an object {@link Language}
   * @return an object {@link LanguageAction}
   */
  public static LanguageAction newAddLanguageAction(Language language) {
    LanguageAction la = new LanguageAction(language);
    la.setMode("ADD");
    return la;
  }

  /**
   * Performs a new remove language action
   * @param language an object {@link Language}
   * @return an object {@link LanguageAction}
   */
  public static LanguageAction newRemoveLanguageAction(Language language) {
    LanguageAction la = new LanguageAction(language);
    la.setMode("REMOVE");
    return la;
  }

  /**
   * Performs a new set language action
   * @param language an object {@link Language}
   * @return an object {@link LanguageAction}
   */
  public static LanguageAction newSetLanguageAction(Language language) {
    LanguageAction la = new LanguageAction(language);
    la.setMode("SET");
    return la;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addLanguage(language);
    else if (mode.equals("REMOVE"))
      mds.removeLanguage(language);
    else if (mode.equals("SET"))
      mds.setLanguage(language);
  }

  /**
   * Return the inverse action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    LanguageAction la = null;
    if (mode.equals("ADD"))
      la = newRemoveLanguageAction(language);
    else if (mode.equals("REMOVE"))
      la = newAddLanguageAction(language);
    else if (mode.equals("SET"))
      la = newSetLanguageAction(old_language);

    la.setUndoActionOf(this);
    return la;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("SET"))
        old_language = mds.getLanguage(language.getAbbreviation());
      else if (mode.equals("REMOVE"))
        language = mds.getLanguage(language.getAbbreviation());
    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to initialize state.", language, bve);
      throw dse;
    }
  }

}