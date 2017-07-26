package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.beans.MappingBean;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.MappingClient;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.exception.MEMEException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class UpdateMapSetAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws
      MEMEException {
    MapSetActionForm mapSetActionForm = (MapSetActionForm) actionForm;
    HttpSession session = servletRequest.getSession();
    MappingBean mappingBean = null;
    MapSet mapset = null;
    try {
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
      AuxiliaryDataClient aux = mappingBean.getAuxiliaryDataClient();
      MappingClient mapping = mappingBean.getMappingClient();
      mapping.setDescription(mapset, mapSetActionForm.getDescription());
      mapping.setFromSource(mapset,
                            aux.getSource(mapSetActionForm.getFromSource()));
      mapping.setToSource(mapset, aux.getSource(mapSetActionForm.getToSource()));
      mapping.setMapSetSource(mapset,
                              aux.getSource(mapSetActionForm.getMapSetSource()));
      mapping.setAttribute(mapset,mapSetActionForm.getMapSetComplexity(),"MTH_MAPSETCOMPLEXITY");
      mapping.setAttribute(mapset, mapSetActionForm.getFromComplexity(),
                           "MTH_MAPFROMCOMPLEXITY");
      mapping.setAttribute(mapset,
                           ("true".equals(mapSetActionForm.getIsFromExhaustive()) ?
                            "Y" : "N"),
                           "MTH_MAPFROMEXHAUSTIVE");
      mapping.setAttribute(mapset, mapSetActionForm.getToComplexity(),
                           "MTH_MAPTOCOMPLEXITY");
      mapping.setAttribute(mapset,
                           ("true".equals(mapSetActionForm.getIsToExhaustive()) ?
                            "Y" : "N"),
                           "MTH_MAPTOEXHAUSTIVE");
      mapping.setAttribute(mapset, mapSetActionForm.getMAPSETSEPARATORCODE(),
                           "MAPSETSEPARATORCODE");
      mapping.setAttribute(mapset, mapSetActionForm.getUMLSMAPSETSEPARATOR(),
                           "MTH_UMLSMAPSETSEPARATOR");
      mapping.setAttribute(mapset, mapSetActionForm.getMAPSETXRTARGETID(),
                           "MAPSETXRTARGETID");
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
