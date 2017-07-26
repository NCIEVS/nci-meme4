package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.beans.MappingBean;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.exception.MEMEException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewMapSetAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws
      MEMEException {
    MappingActionForm mappingActionForm = (MappingActionForm) actionForm;
    HttpSession session = servletRequest.getSession();
    MappingBean mappingBean = null;
    MapSet mapset = null;
    if ( (session != null) && (session.getAttribute("mappingBean") != null)) {
      mappingBean = (MappingBean) session.getAttribute("mappingBean");
    }
    if (mappingBean == null) {
      return actionMapping.findForward("main");
    }
    try {
      mapset = mappingBean.getMapSet(mappingActionForm.getMapsetId());
      session.setAttribute("mapset", mapset);
      session.setAttribute("MAPSETSEPARATORCODE",mapset.getValue("MAPSETSEPARATORCODE"));
      session.setAttribute("UMLSMAPSETSEPARATOR",mapset.getValue("UMLSMAPSETSEPARATOR"));
      session.setAttribute("MAPSETXRTARGETID",mapset.getValue("MAPSETXRTARGETID"));
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
