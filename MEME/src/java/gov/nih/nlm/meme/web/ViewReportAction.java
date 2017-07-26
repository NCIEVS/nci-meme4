package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.util.LabelValueBean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;

public class ViewReportAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws
      Exception {
    ViewReportActionForm reportForm = (ViewReportActionForm) actionForm;
    String type = reportForm.getType();
    Set paths = servletRequest.getSession().getServletContext().
        getResourcePaths("/StatusReports/");
    if (paths == null) {
      MEMEException me = new MissingDataException("Invalid path /StatusReports");
      servletRequest.setAttribute("MEMEException", me);
      throw me;
    }
    Vector beanCollection = new Vector(paths.size());
    for (Iterator iter = paths.iterator(); iter.hasNext(); ) {
      String path = (String) iter.next();
      if (path.indexOf(type) != -1) {
        beanCollection.add(new LabelValueBean(path.substring(path.lastIndexOf(
            "/") + 1), path));
      }
    }
    Collections.sort(beanCollection, new Comparator() {
      public int compare(Object o1, Object o2) {
        return o2.toString().compareTo(o1.toString());
      }
    });
    if (reportForm.getReportID() == null && beanCollection.size() > 0) {
      reportForm.setReportID( ( (LabelValueBean) beanCollection.get(0)).
                             getValue());
    }
    if ("status".equals(type) && reportForm.getReportID() != null) {
      for (Iterator iter = beanCollection.iterator(); iter.hasNext(); ) {
        LabelValueBean bean = (LabelValueBean) iter.next();
        if (bean.getValue().equals(reportForm.getReportID()) &&
            iter.hasNext()) {
          servletRequest.setAttribute("previous",
                                      ( (LabelValueBean) iter.next()).
                                      getValue());
          break;
        }
      }
    }
    servletRequest.setAttribute("options", beanCollection);
    return actionMapping.findForward("success");
  }

}
