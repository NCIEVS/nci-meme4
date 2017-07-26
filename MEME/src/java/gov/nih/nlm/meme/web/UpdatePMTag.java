/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  UpdatePMTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class UpdatePMTag extends TagSupport {

  protected String name = null;
  protected String progress = null;
  protected String message = "Process Monitor";
  protected int max = 100;
  protected String mode = "javascript";
  protected boolean isexpression = false;

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the progress.
   * @param progress the progress
   */
  public void setProgress(String progress) {
    this.progress = progress;
  }

  /**
   * Sets the message
   * @param message the message
   */
  public void SetMessage(String message) {
    this.message = message;
  }

  /**
   * Sets the mode.
   * @param mode the mode
   */
  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Sets the max value.
   * @param max the max value
   */
  public void setMax(int max) {
    this.max = max;
  }

  /**
   * Sets the flag indicating whether or not the message is a JavaScript
   * expression.
   * @param isexpression
   */
  public void setIsexpression(boolean isexpression) {
    this.isexpression = isexpression;
  }

  /**
  * Handle tag, write Update the proress monitor.
  * @return code indicating what to do next
  * @throws JspException anything goes wrong
  */
 public int doStartTag() throws JspException {
   try {
     JspWriter pw = pageContext.getOut();
     if (mode.equals("javascript"))
       pw.println("<script language=\"JavaScript\">");

     if(!isexpression)
       pw.println("myInt = \"" + progress + "\";");
     else
       pw.println("myInt = "+progress+";");
     //verify progress is an integer
     pw.println("if(isNaN(parseInt(myInt)))");
     pw.println("  alert(\"The progress param \" + myInt +\" in <meme:updatepm> is not an integer\");");
     //verify progress range
     pw.println("if(myInt < 0) myInt = 0;");
     pw.println("if (myInt > " + max + ") myInt = " + max + ";");
     pw.println("for(var cell=0; cell < myInt;cell++)");
     pw.println("  document.getElementById(\"" + name +
                "_progress\" + cell).style.backgroundColor='blue';");
     //update message
     pw.println("document.getElementById(\"message_"+name+"\").innerHTML='"+message+" - '+myInt+'% complete';");
   if (mode.equals("javascript"))
       pw.println("</script>");
   }
   catch (Exception ex) {
     JspTagException jte =
         new JspTagException("Error handling update progress monitor tag");
     jte.initCause(ex);
     throw jte;
   }
   return SKIP_BODY;
 }

 /**
  * Handles ending of tag.
  * @return code indicating what to do next
  * @throws JspException if anything goes wrong
  */
 public int doEndTag() throws JspException {
   return EVAL_PAGE;
 }

}
