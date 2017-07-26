package gov.nih.nlm.meme.web;

import org.apache.struts.validator.ValidatorActionForm;
import org.apache.struts.action.*;
import org.apache.struts.upload.FormFile;

public class MappingsFileInputActionForm extends ValidatorActionForm {
  private FormFile theFile;
  private String destDir;
	  
  public String getDestDir() {
		return destDir;
  }
  public void setDestDir(String destDir) {
		this.destDir = destDir;
  }
	/**
	   * @return Returns the theFile.
	   */
  public FormFile getTheFile() {
	    return theFile;
  }
	  /**
	   * @param theFile The FormFile to set.
	   */
  public void setTheFile(FormFile theFile) {
	    this.theFile = theFile;
  }
}