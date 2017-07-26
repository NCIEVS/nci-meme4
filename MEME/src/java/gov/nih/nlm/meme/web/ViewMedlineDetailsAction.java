package gov.nih.nlm.meme.web;
/*****************************************************************************
*
* Package: gov.nih.nlm.meme.web
* Object:  ViewMedlineDetailsAction
*
* Changes:
*   04/28/2006 TTN (1-77HMD): Added MedlineService to process Medline data
*
*****************************************************************************/

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.beans.MedlineClientBean;
import gov.nih.nlm.meme.client.MedlineClient;
import gov.nih.nlm.meme.common.StageStatus;

public class ViewMedlineDetailsAction extends Action {
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws
            MEMEException {
        try {
            MedlineActionForm medlineActionForm = (MedlineActionForm)
                                                  actionForm;
            HttpSession session = servletRequest.getSession();
            MedlineClientBean medlineBean = null;
            if ((session != null) && (session.getAttribute("medlineBean") != null)) {
                medlineBean = (MedlineClientBean) session.getAttribute(
                        "medlineBean");
            }
            if (medlineBean == null) {
                return actionMapping.findForward("main");
            }
            String log = null;
            MedlineClient mc = medlineBean.getMedlineClient();
            if (("View Log").equals(medlineActionForm.getDetails())) {
                StageStatus stagestatus = mc.getMedlineStageStatus(
                        medlineActionForm.getStage());
                int code = stagestatus.getCode();
                log = stagestatus.getLog();
                if ((code & StageStatus.ERROR) == StageStatus.ERROR) {
                    log = log.replaceAll("(ERROR parsing )(medline.*xml)", "$1<A href=\"processMedline.do?action=delete&file=$2\" onclick=\"confirmDelete('$2',this.href);return false;\">$2</A>");
                    log = log + "<blockquote><i> Click <a href=\"meme/controller?state=EditMedlineProperty&command=Insert\">here</a> to add a new pattern.</i></blockquote>";
                }
            }
            session.setAttribute("log", log);
        } catch (MEMEException e) {
            servletRequest.setAttribute("MEMEException", e);
            throw e;
        } catch (Exception e) {
            MEMEException me = new MEMEException("Non MEMEException", e);
            servletRequest.setAttribute("MEMEException", me);
            throw me;
        }
        return actionMapping.findForward("success");
    }
}
