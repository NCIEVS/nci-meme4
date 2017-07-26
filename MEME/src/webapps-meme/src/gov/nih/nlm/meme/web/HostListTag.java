/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  HostListTag
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
 * This is a custom tag for a host list widget.
 *
 * @author Brian Carlsen
 */
public class HostListTag extends TagSupport {

  //
  // Name of the host list widget (for referencing)
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
   * Handle tag, write host list widget.
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
          String[] hosts_pre = MIDServices.getHostServicesList();
          String[] hosts = new String[hosts_pre.length+2];
          for (int i=0; i < hosts_pre.length; i++)
            hosts[i] = MIDServices.getService(hosts_pre[i]);
          hosts[hosts.length-2] = "localhost";
          hosts[hosts.length-1] = initial_value;
          Arrays.sort(hosts);
          String prev_host = "";
          for (int i=0; i < hosts.length; i++) {
            if (!hosts[i].equals(prev_host)) {
              String host = initial_value.equals(hosts[i]) ? "SELECTED" : "";
              pw.println("\t  <OPTION value=\"" + hosts[i] + "\" "  + host + ">" + hosts[i] + "</OPTION>");
            }
            prev_host = hosts[i];
          }
          pw.println("\t</SELECT>");
       } catch (Exception ex) {
         JspTagException jte =
             new JspTagException("Error handling host list tag");
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