package gov.nih.nlm.meme.web;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

public class MappingsFileInputAction extends Action {
	public ActionForward execute(ActionMapping mapping,  ActionForm form,   HttpServletRequest request, HttpServletResponse response) throws Exception{
		MappingsFileInputActionForm myForm = (MappingsFileInputActionForm)form;
        // Process the FormFile
		HttpSession session = request.getSession();
		ActionMessages actionMessages = new ActionMessages();
	    if ( session == null || session.getAttribute("MappingsLogonActionForm") == null || myForm == null) {
	    	actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
        	        new ActionMessage("error.accessDenied"));
        	 request.setAttribute("error", actionMessages);
        	 saveMessages(request, actionMessages);
        	return mapping.findForward("main");
	      }
	    MappingsLogonActionForm mlaf = (MappingsLogonActionForm) session.getAttribute("MappingsLogonActionForm");
	    //if (session != null && session.getAttribute("userAuthenticated") != null && session.getAttribute("userAuthenticated").equals("AUTHENTICATED")) {
	    if (mlaf != null && mlaf.isAuthenticated()) {
	    	session.setAttribute("FileName", null);
	        FormFile myFile = myForm.getTheFile();
	        String destDir = myForm.getDestDir();
	        String fileName = null;
	        if (myFile != null) {
	        	fileName = myFile.getFileName();
	        }

	        if (myFile == null || fileName == null || fileName.equals(""))  {
		    	actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
	        	        new ActionMessage("error.noUploadFile"));
	        	 request.setAttribute("error", actionMessages);
	        	 saveMessages(request, actionMessages);
	        	 return mapping.findForward("fileinput");
		    }
		    if (destDir == null) {
		    	actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
	        	        new ActionMessage("error.noDestDir"));
	        	 request.setAttribute("error", actionMessages);
	        	 saveMessages(request, actionMessages);
	        	 return mapping.findForward("fileinput");
		    }
	        String path = request.getRealPath("/");
	        String dirName =  path + "../www/Mappings/" + destDir + "/";
	        try {
	        	//File file= new File(dirName + fileName);
	        	FileOutputStream fos = new FileOutputStream(dirName + fileName);
	        	DataOutputStream dos=new DataOutputStream(fos);
	   	       	dos.write(myFile.getFileData());
	        } catch (IOException e) {
	        	request.setAttribute("MEMEException", e);
		        throw e;
	        	//session.setAttribute("error", "Unknown exception please contact OCCS support");
	        	//return mapping.findForward("main");
	        } 
	        session.setAttribute("FileName", fileName);
	        session.setAttribute("destination", destDir);
		    return mapping.findForward("success");
		    } else {
		    	actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
	        	        new ActionMessage("error.accessDenied"));
	        	 request.setAttribute("error", actionMessages);
	        	 saveMessages(request, actionMessages);
		    	return mapping.findForward("main");
		    }
	}
}
