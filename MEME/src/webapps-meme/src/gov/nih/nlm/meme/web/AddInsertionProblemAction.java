/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  AddInsertionProblemAction
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
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.PropertyUtils;
import gov.nih.nlm.meme.exception.MEMEException;

public class AddInsertionProblemAction extends Action {
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws MEMEException {
        try {
            InsertionProblemActionForm problemForm = (
                    InsertionProblemActionForm) actionForm;
            ArrayList problems = new ArrayList();
            HttpSession session = servletRequest.getSession();
            if (session != null && session.getAttribute("problems") != null) {
                problems = (ArrayList) session.getAttribute("problems");
            }
            InsertionProblemActionForm problem = new InsertionProblemActionForm();
            PropertyUtils.copyProperties(problem, problemForm);
            problems.add(problem);
            session.setAttribute("problems", problems);
        }
        catch (Exception e) {
          MEMEException me = new MEMEException("Non MEMEException", e);
          servletRequest.setAttribute("MEMEException", me);
          throw me;
        }
        return actionMapping.findForward("success");
    }
}
