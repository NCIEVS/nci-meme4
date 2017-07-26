/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ClearPMTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class ClearPMTag extends TagSupport {

  protected String name = null;
  protected String message = "Process Monitor";
  protected int max = 100;
  protected String mode = "javascript";

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
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
   * Sets the message.
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
  * Handle tag, write "clear progress monitor" widget.
  * @return code indicating what to do next
  * @throws JspException if anything goes wrong
  */
 public int doStartTag() throws JspException {
   try {
     JspWriter pw = pageContext.getOut();
     if (mode.equals("javascript"))
       pw.println("<script language=\"JavaScript\">");

     pw.println("  document.getElementById(\"message_" + name +
                  "\").innerHTML='" + message + "';");

     pw.println("for(var cell=0; cell < "+max+";cell++)");
     pw.println("  document.getElementById(\"" + name +
                "_progress\" + cell).style.backgroundColor='transparent';");
   if (mode.equals("javascript"))
       pw.println("</script>");
   }
   catch (Exception ex) {
     JspTagException jte =
         new JspTagException("Error handling clear progress monitor tag");
     jte.initCause(ex);
     throw jte;
   }
   return SKIP_BODY;
 }

 /**
  * Handles ending of tag.
  * @return a code indicating what to do next
  * @throws JspException if anything goes wrong
  */
 public int doEndTag() throws JspException {
   return EVAL_PAGE;
 }

}
