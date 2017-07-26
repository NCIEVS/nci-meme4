/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ViewInsertionReportAction
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
import org.apache.commons.beanutils.PropertyUtils;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.beans.WorkLogBean;
import org.apache.struts.util.LabelValueBean;
import gov.nih.nlm.meme.exception.MEMEException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import gov.nih.nlm.meme.action.WorkLog;
import java.util.Iterator;

public class ViewInsertionReportAction extends Action {
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse)  throws
            MEMEException {
        try {
        WorkActivityLogActionForm workActivityLogActionForm = (
                WorkActivityLogActionForm) actionForm;
        HttpSession session = servletRequest.getSession();
        WorkLogBean insertionReportBean = null;
        if ( (session != null) && (session.getAttribute("insertionReportBean") != null)) {
          insertionReportBean = (WorkLogBean) session.getAttribute("insertionReportBean");
          PropertyUtils.copyProperties(insertionReportBean, workActivityLogActionForm);
        }
        if (insertionReportBean == null) {
          insertionReportBean = new WorkLogBean();
          if(workActivityLogActionForm.getMidService() != null && !"".equals(workActivityLogActionForm.getMidService()))
            insertionReportBean.setMidService(workActivityLogActionForm.getMidService());
          insertionReportBean.setRange(30);
        }
        SimpleDateFormat formatter = new SimpleDateFormat(
            "dd-MMM-yyyy hh:mm:ss");
        ArrayList logs = insertionReportBean.getWorkLogs();
        ArrayList beanCollection = new ArrayList(logs.size());
        for (Iterator iter = logs.iterator(); iter.hasNext(); ) {
            WorkLog work = (WorkLog) iter.next();
            if (work.getType().equals("INSERTION")) {
                beanCollection.add(new LabelValueBean(work.getType() + " - " +
                        work.getDescription()
                        + "(" +
                        formatter.format(work.
                                         getTimestamp()) +
                        ")",
                        work.getIdentifier().toString()));
            }
        }
        session.setAttribute("worklogs", beanCollection);
        session.setAttribute("insertionReportBean", insertionReportBean);
        session.removeAttribute("problems");
      }
      catch (MEMEException e) {
        servletRequest.setAttribute("MEMEException", e);
        throw e;
      }
      catch (Exception e) {
          e.printStackTrace();
        MEMEException me = new MEMEException("Non MEMEException", e);
        me.setPrintStackTrace(true);
        servletRequest.setAttribute("MEMEException", me);
        throw me;
      }
      return actionMapping.findForward("success");
    }
}
