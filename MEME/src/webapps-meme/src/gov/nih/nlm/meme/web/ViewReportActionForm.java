package gov.nih.nlm.meme.web;

import javax.servlet.http.*;

import org.apache.struts.action.*;
import org.apache.struts.upload.*;

public class ViewReportActionForm
    extends ActionForm {
  private String reportID;
  private String date;
  private String type;
  /**
   * The value of the text the user has sent as form data
   */
  protected String theText;

  /**
   * The value of the embedded query string parameter
   */
  protected String queryParam;
  private String release;

  /**
   * Whether or not to write to a file
   */
  protected boolean writeFile;

  /**
   * The file that the user has uploaded
   */
  protected FormFile theFile;

  /**
   * The file path to write to
   */
  protected String filePath;

  public String getReportID() {
    return reportID;
  }

  public void setReportID(String reportID) {
    this.reportID = reportID;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * Retrieve the value of the text the user has sent as form data
   */
  public String getTheText() {
    return theText;
  }

  /**
   * Set the value of the form data text
   */
  public void setTheText(String theText) {
    this.theText = theText;
  }

  /**
   * Retrieve the value of the query string parameter
   */
  public String getQueryParam() {
    return queryParam;
  }

  /**
   * Set the value of the query string parameter
   */
  public void setQueryParam(String queryParam) {
    this.queryParam = queryParam;
  }

  /**
   * Retrieve a representation of the file the user has uploaded
   */
  public FormFile getTheFile() {
    return theFile;
  }

  /**
   * Set a representation of the file the user has uploaded
   */
  public void setTheFile(FormFile theFile) {
    this.theFile = theFile;
  }

  /**
   * Set whether or not to write to a file
   */
  public void setWriteFile(boolean writeFile) {
    this.writeFile = writeFile;
  }

  /**
   * Get whether or not to write to a file
   */
  public boolean getWriteFile() {
    return writeFile;
  }

  /**
   * Set the path to write a file to
   */
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public void setDate(String date) {
    this.date = date;
  }

  /**
   * Get the path to write a file to
   */
  public String getFilePath() {
    return filePath;
  }

  public String getRelease() {
    return release;
  }

  public String getDate() {
    return date;
  }

  public ActionErrors validate(ActionMapping actionMapping,
                               HttpServletRequest httpServletRequest) {
    /** @todo: finish this method, this is just the skeleton.*/
    return null;
  }

  public void reset(ActionMapping actionMapping,
                    HttpServletRequest servletRequest) {
  }
}
