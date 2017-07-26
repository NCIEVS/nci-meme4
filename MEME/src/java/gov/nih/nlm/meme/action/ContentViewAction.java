/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  ContentViewAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a content view action.
 *
 * @author MEME Group
 */

public class ContentViewAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private ContentView cv = null;
  private ContentView old_cv = null;
  private ContentViewMember cvm = null;
  private ContentViewMember[] cvms = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ContentViewAction} with
   * the specified content view.
   * @param cv an object {@link ContentView}
   */
  private ContentViewAction(ContentView cv) {
    this.cv = cv;
  }

  /**
   * Instantiates a {@link ContentViewAction} with
   * the specified content view member.
   * @param cvm an object {@link ContentViewMember}
   */
  private ContentViewAction(ContentViewMember cvm) {
    this.cvm = cvm;
  }

  /**
   * Instantiates a {@link ContentViewAction} with
   * the specified content view members.
   * @param cvms an array of object {@link ContentViewMember}
   */
  private ContentViewAction(ContentViewMember[] cvms) {
    this.cvms = cvms;
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
   * Performs a new add content view action
   * @param cv an object {@link ContentView}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newAddContentViewAction(ContentView cv) {
    ContentViewAction cva = new ContentViewAction(cv);
    cva.setMode("ADD");
    return cva;
  }

  /**
   * Performs a new add content view member action
   * @param cvm an object {@link ContentViewMember}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newAddContentViewMemberAction(
      ContentViewMember cvm) {
    ContentViewAction cva = new ContentViewAction(cvm);
    cva.setMode("ADD_MEMBER");
    return cva;
  }

  /**
   * Performs a new add content view members action
   * @param cvms an array of object {@link ContentViewMember}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newAddContentViewMembersAction(
      ContentViewMember[] cvms) {
    ContentViewAction cva = new ContentViewAction(cvms);
    cva.setMode("ADD_MEMBERS");
    return cva;
  }

  /**
   * Performs a new generate content view members action
   * @param cv an object {@link ContentView}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newGenerateContentViewMembersAction(
      ContentView cv) {
    ContentViewAction cva = new ContentViewAction(cv);
    cva.setMode("GENERATE");
    return cva;
  }

  /**
   * Performs a new remove content view action
   * @param cv an object {@link ContentView}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newRemoveContentViewAction(ContentView cv) {
    ContentViewAction cva = new ContentViewAction(cv);
    cva.setMode("REMOVE");
    return cva;
  }

  /**
   * Performs a new remove content view member action
   * @param cvm an object {@link ContentViewMember}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newRemoveContentViewMemberAction(
      ContentViewMember cvm) {
    ContentViewAction cva = new ContentViewAction(cvm);
    cva.setMode("REMOVE_MEMBER");
    return cva;
  }

  /**
   * Performs a new remove content view members action
   * @param cv an object {@link ContentView}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newRemoveContentViewMembersAction(ContentView
      cv) {
    ContentViewAction cva = new ContentViewAction(cv);
    cva.setMode("REMOVE_CV_MEMBERS");
    return cva;
  }

  /**
   * Performs a new remove content view member action
   * @param cvms an array object {@link ContentViewMember}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newRemoveContentViewMembersAction(
      ContentViewMember[] cvms) {
    ContentViewAction cva = new ContentViewAction(cvms);
    cva.setMode("REMOVE_MEMBERS");
    return cva;
  }

  /**
   * Performs a new set content view action
   * @param cv an object {@link ContentView}
   * @return an object {@link ContentViewAction}
   */
  public static ContentViewAction newSetContentViewAction(ContentView cv) {
    ContentViewAction cva = new ContentViewAction(cv);
    cva.setMode("SET");
    return cva;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addContentView(cv);
    else if (mode.equals("ADD_MEMBER"))
      mds.addContentViewMember(cvm);
    else if (mode.equals("ADD_MEMBERS"))
      mds.addContentViewMembers(cvms);
    else if (mode.equals("GENERATE"))
      mds.generateContentViewMembers(cv);
    else if (mode.equals("REMOVE"))
      mds.removeContentView(cv);
    else if (mode.equals("REMOVE_MEMBER"))
      mds.removeContentViewMember(cvm);
    else if (mode.equals("REMOVE_CV_MEMBER"))
      mds.removeContentViewMembers(cv);
    else if (mode.equals("REMOVE_MEMBERS"))
      mds.removeContentViewMembers(cvms);
    else if (mode.equals("SET"))
      mds.setContentView(cv);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    ContentViewAction cva = null;
    if (mode.equals("ADD"))
      cva = newRemoveContentViewAction(cv);
    else if (mode.equals("ADD_MEMBER"))
      cva = newRemoveContentViewMemberAction(cvm);
    else if (mode.equals("ADD_MEMBERS"))
      cva = newRemoveContentViewMembersAction(cvms);
    else if (mode.equals("GENERATE"))
      return new NonOperationAction();
    else if (mode.equals("REMOVE"))
      cva = newAddContentViewAction(cv);
    else if (mode.equals("REMOVE_MEMBER"))
      cva = newAddContentViewMemberAction(cvm);
    else if (mode.equals("REMOVE_MEMBERS"))
      cva = newAddContentViewMembersAction(cvms);
    else if (mode.equals("REMOVE_CV_MEMBER"))
      cva = newAddContentViewMembersAction(cv.getMembers());
    else if (mode.equals("SET"))
      cva = newSetContentViewAction(old_cv);

    cva.setUndoActionOf(this);
    return cva;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      old_cv = mds.getContentView(cv.getIdentifier());
    else if (mode.equals("REMOVE"))
      cv = mds.getContentView(cv.getIdentifier());
      //else if (mode.equals("REMOVE_MEMBER"))
      // Do nothing. All CVM info already available
      //else if (mode.equals("REMOVE_MEMBERS"))
      // Do nothing. All CVM info already available
    else if (mode.equals("REMOVE_CV_MEMBER"))
      cv.setMembers(mds.getContentViewMembers(cv));
  }

}