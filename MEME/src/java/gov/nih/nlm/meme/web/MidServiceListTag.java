/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  MidServiceListTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.MIDServices;

import java.util.Arrays;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This is a custom tag for a mid service list.
 *
 * @author Brian Carlsen
 */
public class MidServiceListTag extends TagSupport {

  //
  // Name of the mid serve list
  //
  protected String name = null;
  protected String initial_value = null;
  protected boolean submit = false;

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) { this.name = name; }

  /**
   * Sets the initial value.
   * @param value the initial value
   */
  public void setInitialValue(String value) { this.initial_value = value; }

  /**
   * Sets the flag indicating whether or not this is a auto-submit widget.
   * @param submit <code>true</code> if so, <code>false</code> otherwise
   */
  public void setSubmit(boolean submit) { this.submit = submit; }

  /**
   * Handle tag, write mid service list.
   * @return code indicating what to do next
   * @throws JspException if anything goes wrong
   */
  public int doStartTag() throws JspException {
       try {
          JspWriter pw = pageContext.getOut();
          pw.print("<SELECT name=\"");
          pw.print(name);
          pw.print("\" ");
          if (submit)
            pw.print("onChange='this.form.submit();'");
          pw.println(">");
          pw.println("\t  <OPTION value=\"\">&lt;default&gt;</OPTION>");
          String[] mid_services = MIDServices.getDbServicesList();
          for (int i=0; i < mid_services.length; i++)
            mid_services[i] = MIDServices.getService(mid_services[i]);
          Arrays.sort(mid_services);
          String prev_mid_service = "";
          for (int i=0; i < mid_services.length; i++) {
            if (!mid_services[i].equals(prev_mid_service)) {
              String mid_service = initial_value.equals(mid_services[i]) ? "SELECTED" : "";
              pw.println("\t  <OPTION value=\"" + mid_services[i] + "\" " + mid_service + ">" + mid_services[i] + "</OPTION>");
            }
            prev_mid_service = mid_services[i];
          }
          pw.println("\t</SELECT>");
       } catch (Exception ex) {
         JspTagException jte =
             new JspTagException("Error handling mid service list tag");
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