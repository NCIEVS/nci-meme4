package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.beans.AdminClientBean;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionMessage;

public class ChangePasswordAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws
      MEMEException {
    LogonActionForm logonActionForm = (LogonActionForm) actionForm;
    try {
      AdminClientBean adminBean = new AdminClientBean();
      ActionMessages messages = new ActionMessages();
      if (logonActionForm.getDbaUsername() != null) {
        adminBean.getAdminClient().changePasswordForUser(logonActionForm.
            getDbaUsername(),
            logonActionForm.getDbaPassword(),
            logonActionForm.getUsername(),
            logonActionForm.getPassword());
        ActionMessage message = new ActionMessage(
            "message.password.update.success", logonActionForm.getUsername() + "'s");
        messages.add(ActionMessages.GLOBAL_MESSAGE, message);
      } else {
        adminBean.getAdminClient().changePassword(logonActionForm.getUsername(),
                                                  logonActionForm.
                                                  getOldPassword(),
                                                  logonActionForm.getPassword());
        ActionMessage message = new ActionMessage(
            "message.password.update.success", "your");
        messages.add(ActionMessages.GLOBAL_MESSAGE, message);
      }
      saveMessages(servletRequest, messages);
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
