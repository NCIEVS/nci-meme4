package gov.nih.nlm.meme.web;
/*****************************************************************************
*
* Package: gov.nih.nlm.meme.web
* Object:  ViewMedlineProcessAction
*
* Changes:
*   06/19/2006 TTN (1-77HMD): fix data source type cast in MedlineService to process Medline data
*   04/28/2006 TTN (1-77HMD): Added MedlineService to process Medline data
*
*****************************************************************************/

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import gov.nih.nlm.meme.web.MedlineActionForm;
import org.apache.struts.action.Action;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.exception.MEMEException;
import java.util.HashMap;
import gov.nih.nlm.meme.beans.MedlineClientBean;
import gov.nih.nlm.meme.common.StageStatus;

public class ViewMedlineProcessAction extends Action {
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
                medlineBean = new MedlineClientBean();
            }
            if(medlineActionForm.getHost() != null) {
                medlineBean.setHost(medlineActionForm.getHost());
            }
            if(medlineActionForm.getPort() != null) {
                medlineBean.setPort(medlineActionForm.getPort());
            }
            if(medlineActionForm.getMidService() != null && !medlineActionForm.getMidService().equals("")) {
                medlineBean.setMidService(medlineActionForm.getMidService());
            }
            HashMap params = new HashMap();
            params.put("host", medlineBean.getHost());
            params.put("port", medlineBean.getPort());
            params.put("midService", medlineBean.getMidService());

            session.setAttribute("medlineBean", medlineBean);
            session.setAttribute("linkParams", params);

            StageStatus[] stageStatus = medlineBean.getStageStatus();
            StringBuffer options = new StringBuffer();
            boolean enable = true;
            boolean prev;
            for (int j = 0; j < stageStatus.length; j++) {
                int code = stageStatus[j].getCode();
                if (enable) {
                    options.append(stageStatus[j].toString()).append(",");
                }
                prev = enable;
                enable = false;
                if ((code & StageStatus.FINISHED) == StageStatus.FINISHED) {
                    enable = prev && true;
                }
            }
            session.setAttribute("options",options.toString());
            if(medlineActionForm.getContext() == null) {
                medlineActionForm.setContext("MID");
            }
        } catch (MEMEException e) {
            servletRequest.setAttribute("MEMEException", e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            MEMEException me = new MEMEException("Non MEMEException", e);
            me.setPrintStackTrace(true);
            servletRequest.setAttribute("MEMEException", me);
            throw me;
        }
        return actionMapping.findForward("success");
    }
}
