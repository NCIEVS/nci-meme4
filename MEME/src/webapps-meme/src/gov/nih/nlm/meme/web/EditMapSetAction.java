package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.common.MapSet;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.beans.MappingBean;

public class EditMapSetAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws MEMEException {
    MapSetActionForm mapSetActionForm = (MapSetActionForm) actionForm;
    HttpSession session = servletRequest.getSession();
    MappingBean mappingBean = null;
    MapSet mapset = null;
    if ( (session != null) && (session.getAttribute("mappingBean") != null)) {
      mappingBean = (MappingBean) session.getAttribute("mappingBean");
    }
    if (mappingBean == null) {
      return actionMapping.findForward("main");
    }
    if ( (session != null) && (session.getAttribute("mapset") != null)) {
      mapset = (MapSet) session.getAttribute("mapset");
    }
    if (mapset == null) {
      return actionMapping.findForward("main");
    }
    try {
      mapSetActionForm.setMAPSETSEPARATORCODE(mapset.getValue("MAPSETSEPARATORCODE"));
      mapSetActionForm.setMTH_UMLSMAPSETSEPARATOR(mapset.getValue("MTH_UMLSMAPSETSEPARATOR"));
      mapSetActionForm.setMAPSETXRTARGETID(mapset.getValue("MAPSETXRTARGETID"));
      mapSetActionForm.setMAPSETVERSION(mapset.getValue("MAPSETVERSION"));
      AuxiliaryDataClient aux = mappingBean.getAuxiliaryDataClient();
      session.setAttribute("sources", aux.getSourceAbbreviations());
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
