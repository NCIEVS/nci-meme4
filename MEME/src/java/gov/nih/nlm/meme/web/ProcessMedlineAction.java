package gov.nih.nlm.meme.web;

/*****************************************************************************
*
* Package: gov.nih.nlm.meme.web
* Object:  ProcessMedlineAction
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

public class ProcessMedlineAction extends Action {
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
            MedlineClient client = medlineBean.getMedlineClient();
            if("clearLog".equals(medlineActionForm.getAction())) {
                    client.clearMedlineStatus(medlineActionForm.getStage());
            }
            else if("Start".equals(medlineActionForm.getAction())) {
                    if("download".equals(medlineActionForm.getStage())) {
                            client.downloadMedlineBaseline();
                    } if("parse".equals(medlineActionForm.getStage())) {
                            client.parseMedlineBaseline(medlineActionForm.getContext(), medlineActionForm.getCutoffDate());
                    } if("process".equals(medlineActionForm.getStage())) {
                            client.processMedlineBaseline(medlineActionForm.getContext());
                    } if("update".equals(medlineActionForm.getStage())) {
                            client.updateMedline(medlineActionForm.getContext(), medlineActionForm.getCutoffDate());
                    }
            }
            else if ("delete".equals(medlineActionForm.getAction())) {
                    client.deleteUpdateMedlineXML(medlineActionForm.getFile());
            }
        } catch (MEMEException e) {
            servletRequest.setAttribute("MEMEException", e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            MEMEException me = new MEMEException("Non MEMEException", e);
            servletRequest.setAttribute("MEMEException", me);
            throw me;
        }
        return actionMapping.findForward("success");
    }
}
