/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ViewWorkLogAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import org.apache.struts.action.*;
import org.apache.struts.util.LabelValueBean;
import org.apache.commons.beanutils.PropertyUtils;

import javax.servlet.http.*;

import gov.nih.nlm.meme.beans.WorkLogBean;
import gov.nih.nlm.meme.exception.MEMEException;
import java.util.ArrayList;
import gov.nih.nlm.meme.action.WorkLog;
import java.util.Iterator;
import java.text.SimpleDateFormat;

public class ViewWorkLogAction
    extends Action {
  /**
   *
   * @param mapping The ActionMapping used to select this instance
   * @param form The optional ActionForm bean for this request (if any)
   * @param request The HTTP request we are processing
   * @param response The HTTP response we are creating
   *
   * @exception Exception if the application business logic throws
   *  an exception
   */
  public ActionForward execute(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    try {
      WorkActivityLogActionForm workForm = (WorkActivityLogActionForm) form;
      HttpSession session = request.getSession();
      WorkLogBean workBean = null;
      if ( (session != null) && (session.getAttribute("worklogBean") != null)) {
        workBean = (WorkLogBean) session.getAttribute("worklogBean");
        PropertyUtils.copyProperties(workBean, workForm);
      }
      if (workBean == null) {
        workBean = new WorkLogBean();
        if(workForm.getMidService() != null)
          workBean.setMidService(workForm.getMidService());
        workBean.setRange(30);
      }
      SimpleDateFormat formatter = new SimpleDateFormat(
          "dd-MMM-yyyy hh:mm:ss");
      ArrayList logs = workBean.getWorkLogs();
      ArrayList beanCollection = new ArrayList(logs.size());
      for (Iterator iter = logs.iterator(); iter.hasNext(); ) {
        WorkLog work = (WorkLog) iter.next();
        beanCollection.add(new LabelValueBean(work.getType() + " - " +
                                              work.getDescription()
                                              + "(" +
                                              formatter.format(work.
            getTimestamp()) +
                                              ")",
                                              work.getIdentifier().toString()));
      }
      session.setAttribute("worklogs", beanCollection);
      session.setAttribute("worklogBean", workBean);
    }
    catch (MEMEException e) {
      request.setAttribute("MEMEException", e);
      throw e;
    }
    catch (Exception e) {
      MEMEException me = new MEMEException("Non MEMEException", e);
      request.setAttribute("MEMEException", me);
      throw me;
    }
    return mapping.findForward("success");
  }

}
