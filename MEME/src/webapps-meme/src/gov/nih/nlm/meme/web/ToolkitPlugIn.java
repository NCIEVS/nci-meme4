/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ToolkitPlugIn
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.client.ClientToolkit;

import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

public class ToolkitPlugIn implements PlugIn {
  public ToolkitPlugIn() {
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Logging output for this plug in instance.
   */
  private Log log = LogFactory.getLog(this.getClass());

  /**
   * The web application resource path of our
   * properties file.
   */
  private String pathname = "/WEB-INF/classes/meme.prop";

  public String getPathname() {
      return (this.pathname);
  }

  public void setPathname(String pathname) {
      this.pathname = pathname;
  }

  public void destroy() {
    log.info("Finalizing toolkit plug in");
  }

  public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
    Properties props = new Properties();
    try {
      props.load(servlet.getServletContext().getResourceAsStream(pathname));
      ClientToolkit.initialize(props);
    } catch (Exception e) {
      log.error("Initializing ClientToolkit", e);
      throw new ServletException(e);
    }
  }

  private void jbInit() throws Exception {
  }
}
