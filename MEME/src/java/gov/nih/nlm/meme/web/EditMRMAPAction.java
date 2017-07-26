package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import org.apache.commons.beanutils.PropertyUtils;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.beans.MappingBean;
import gov.nih.nlm.meme.exception.MEMEException;

import gov.nih.nlm.meme.web.MappingActionForm;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.struts.util.LabelValueBean;
import gov.nih.nlm.meme.common.MapSet;

public class EditMRMAPAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws MEMEException {
    try {

      MappingActionForm mapForm = (MappingActionForm) actionForm;
      HttpSession session = servletRequest.getSession();
      MappingBean mappingBean = null;
      if ( (session != null) && (session.getAttribute("mappingBean") != null)) {
        mappingBean = (MappingBean) session.getAttribute("mappingBean");
        PropertyUtils.copyProperties(mappingBean, mapForm);
      }
      if (mappingBean == null) {
        mappingBean = new MappingBean();
        if(mapForm.getHost() != null)
          mappingBean.setHost(mapForm.getHost());
        if(mapForm.getPort() != null)
          mappingBean.setPort(mapForm.getPort());
        if(mapForm.getMidService() != null)
          mappingBean.setMidService(mapForm.getMidService());
      }
      ArrayList mapsets = mappingBean.getMapSets();
      ArrayList beanCollection = new ArrayList(mapsets.size());
      for(Iterator iter = mapsets.iterator(); iter.hasNext();) {
        MapSet mapset = (MapSet)iter.next();
        beanCollection.add(new LabelValueBean(mapset.getName(),mapset.getIdentifier().toString()));
      }
      session.setAttribute("mappings", beanCollection);
      session.setAttribute("mappingBean", mappingBean);
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

