package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.beans.AdminClientBean;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.exception.MEMEException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionErrors;

public class MappingsLogonAction extends Action {
	public ActionForward execute(ActionMapping mapping,  ActionForm form,   HttpServletRequest request, HttpServletResponse response) throws Exception{
		MappingsLogonActionForm myForm = (MappingsLogonActionForm)form;
        // Process the FormFile
        if (myForm == null) {
        	return mapping.findForward("main");
        }
		String userId = myForm.getUsername();
        String passwd = myForm.getPassword();
        HttpSession session = request.getSession();
        ActionMessages actionMessages = new ActionMessages();
        try {
	        AdminClientBean adminBean = new AdminClientBean();
	       
	         EditorPreferences ep = adminBean.getAdminClient().authenticate(userId,passwd);
	        if (ep == null || ep.getEditorLevel() != 5) {
	        	myForm.setAuthenticated(false);
	        	actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
	        	        new ActionMessage("error.authenticationFailed", userId));
	        	        request.setAttribute("error", actionMessages);
	        	 saveMessages(request, actionMessages);
	        	return mapping.findForward("main");
	        }
	        
		    myForm.setAuthenticated(true);
	    
      }
      catch (MEMEException e) {
    	  actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
    			  new ActionMessage("error.exceptionReceived", userId,e.toString()));
      	 request.setAttribute("error", actionMessages);
      	 saveMessages(request, actionMessages);
      	 myForm.setAuthenticated(false);
      	return mapping.findForward("main");
      }
	    return mapping.findForward("success");
	}
}
