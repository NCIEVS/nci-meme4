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

import gov.nih.nlm.meme.common.Atom;
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
        if(mappingBean.getHost() != null)
        	mapForm.setHost(mappingBean.getHost());
        if(mappingBean.getPort() != null)
        	mapForm.setPort(mappingBean.getPort());
        if(mappingBean.getMidService() != null)
        	mapForm.setMidService(mappingBean.getMidService());
      }
      ArrayList mapsets = mappingBean.getMapSets();
      ArrayList beanCollection = new ArrayList(mapsets.size());
      for(Iterator iter = mapsets.iterator(); iter.hasNext();) {
    	  MapSet mapset = (MapSet)iter.next();
          Atom[] atoms = mapset.getAtoms();
          for(int i=0; i<atoms.length; i++) {
          	if(atoms[i].getTermgroup().getTermType().equals("XM")  &&
          		atoms[i].isReleasable()) {
                  beanCollection.add(new LabelValueBean(atoms[i].getString(),mapset.getIdentifier().toString()));
          	}
          }
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

