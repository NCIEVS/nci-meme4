package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

public class UploadAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws Exception {
    ViewReportActionForm theForm = (ViewReportActionForm) actionForm;

    //retrieve the file representation
    FormFile file = theForm.getTheFile();

    String type = theForm.getType();

    try {
      //retrieve the file data
      InputStream stream = file.getInputStream();
      //write the file to the file specified
      SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
      String path = servletRequest.getSession().getServletContext().getRealPath(theForm.getFilePath());
      //String path = theForm.getFilePath();
      String file_name = type + "." + dateformat.format(new Date()) +  ".txt";
      OutputStream bos = new FileOutputStream(new File(path,file_name));
      int bytesRead = 0;
      byte[] buffer = new byte[8192];
      while ( (bytesRead = stream.read(buffer, 0, 8192)) != -1) {
        bos.write(buffer, 0, bytesRead);
      }
      bos.close();
      //close the stream
      stream.close();
    }
    catch (FileNotFoundException fnfe) {
      MEMEException me = new ExternalResourceException("FileNotFoundException",fnfe);
      throw me;
    }
    catch (IOException ioe) {
      MEMEException me = new ExternalResourceException("IOException",ioe);
      servletRequest.setAttribute("MEMEException", me);
      throw me;
    }

    //destroy the temporary file created
    file.destroy();

    //return a forward to display.jsp
    return actionMapping.findForward(type);
  }

}
