/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  DeleteInsetionProblemAction
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
import java.util.ArrayList;
import java.util.Iterator;

public class DeleteInsetionProblemAction extends Action {
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) {
        InsertionProblemActionForm insertionProblemActionForm = (
                InsertionProblemActionForm) actionForm;
        HttpSession session = servletRequest.getSession();
        ArrayList problems = null;
        if ( (session != null) && (session.getAttribute("problems") != null)) {
          problems = (ArrayList) session.getAttribute("problems");
        }
        if (problems == null) {
          return actionMapping.findForward("main");
        }
        for(Iterator iterator = problems.iterator(); iterator.hasNext();) {
            InsertionProblemActionForm problem = (InsertionProblemActionForm)iterator.next();
            if(problem.getProblem().equals(insertionProblemActionForm.getProblem()))
                iterator.remove();
        }
        return actionMapping.findForward("success");
    }
}
