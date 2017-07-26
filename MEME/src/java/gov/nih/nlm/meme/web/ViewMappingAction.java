package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import gov.nih.nlm.meme.common.MapSet;
import javax.servlet.http.HttpSession;
import gov.nih.nlm.meme.beans.MappingBean;
import gov.nih.nlm.util.PaginatedList;
import java.util.ArrayList;
import gov.nih.nlm.util.PaginatedArrayList;
import gov.nih.nlm.meme.exception.MEMEException;

public class ViewMappingAction
    extends Action {
  final static int cacheSize = 500;
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws MEMEException {
    MappingActionForm mappingActionForm = (MappingActionForm) actionForm;
    HttpSession session = servletRequest.getSession();
    MappingBean mappingBean = null;
    MapSet mapset = null;
    PaginatedList mappingList = null;
    int start = 0;
    int end = cacheSize;
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
      if (servletRequest.getParameter("pageDirection") != null) {
        if ( (session != null) && (session.getAttribute("mappingList") != null)) {
          mappingList = (PaginatedList) session.getAttribute("mappingList");
        }
        if (mappingList == null) {
          return actionMapping.findForward("main");
        }
        if ("next".equals(mappingActionForm.getPageDirection())) {
          mappingList.nextPage();
          String mappingindex = mapset.getName() + "index";
          if(mappingList.isLastPage() && session.getAttribute(mappingindex) != null) {
            int index = ((Integer)session.getAttribute(mappingindex)).intValue();
            start = index * cacheSize;
            end = start + cacheSize;
            ArrayList list = mappingBean.getMappings(mapset,start,end);
            if(list.size() < cacheSize)
              session.removeAttribute(mappingindex);
            else
              session.setAttribute(mappingindex,new Integer(++index));
            mappingList.addAll(list);
          }
        }
        else if ("previous".equals(mappingActionForm.getPageDirection())) {
          mappingList.previousPage();
        }
        String[] forwards = actionMapping.findForwards();
        for (int i=0; i<forwards.length; i++) {
          if (servletRequest.getParameter(forwards[i])!=null) {
            // Return the required ActionForward instance
            return actionMapping.findForward(forwards[i]);
          }
        }
        return null;
      }
      ArrayList mappings = mappingBean.getMappings(mapset, start, end);
      if(mappings.size() == cacheSize ) {
        session.setAttribute(mapset.getName() + "index",new Integer(1));
      }
      mappingList = new PaginatedArrayList(mappings, 80);
      session.setAttribute("mappingList", mappingList);
    }
    catch (Exception e) {
      e.printStackTrace();
      MEMEException me = new MEMEException("Non MEMEException", e);
      me.setPrintStackTrace(true);
      servletRequest.setAttribute("MEMEException", me);
      throw me;
    }
    String[] forwards = actionMapping.findForwards();
    for (int i=0; i<forwards.length; i++) {
      if (servletRequest.getParameter(forwards[i])!=null) {
        // Return the required ActionForward instance
        return actionMapping.findForward(forwards[i]);
      }
    }
    return null;
  }
}
