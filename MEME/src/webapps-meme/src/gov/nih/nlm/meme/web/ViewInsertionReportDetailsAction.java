/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ViewInsertionReportDetailsAction
 * 08/31/2006 TTN(1-C4PMN): source insertion report web application
 *
 *****************************************************************************/
package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.beans.WorkLogBean;
import gov.nih.nlm.meme.exception.MEMEException;
import java.util.ArrayList;
import gov.nih.nlm.meme.action.WorkLog;
import java.util.Iterator;

public class ViewInsertionReportDetailsAction extends Action {
        /**
         *
         * @param mapping The ActionMapping used to select this instance
         * @param form The optional ActionForm bean for this request (if any)
         * @param request The HTTP request we are processing
         * @param response The HTTP response we are creating
         *
         * @exception MEMEException if the application business logic throws
         *  an exception
         */
        public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws MEMEException {
          try {
            WorkActivityLogActionForm workForm = (WorkActivityLogActionForm) form;
            HttpSession session = request.getSession();
            WorkLogBean workBean = null;
            if ( (session != null) && (session.getAttribute("insertionReportBean") != null)) {
              workBean = (WorkLogBean) session.getAttribute("insertionReportBean");
              workBean.setWork_id(workForm.getWork_id());
            }
            if (workBean == null) {
              return mapping.findForward("main");
            }
            ArrayList activityList = workBean.getActivities();
            session.setAttribute("activityList", activityList);
            WorkLog log = workBean.getWorkLog();
            session.setAttribute("worklog", log);
            session.setAttribute("insertionReportBean", workBean);
          }
          catch (MEMEException e) {
            request.setAttribute("MEMEException", e);
            throw e;
          }
          catch (Exception e) {
            MEMEException me = new MEMEException("Non MEMEException", e);
            me.setPrintStackTrace(true);
            request.setAttribute("MEMEException", me);
            throw me;
          }
          return mapping.findForward("success");
        }

}
