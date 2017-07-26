
package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.beans.ContentViewBean;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.util.PaginatedArrayList;
import gov.nih.nlm.util.PaginatedList;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewContentViewMembersAction
    extends Action {
  final static int cacheSize = 500;
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws MEMEException {
	  ContentViewActionForm contentViewActionForm = (ContentViewActionForm) actionForm;
    HttpSession session = servletRequest.getSession();
    ContentViewBean contentViewBean = null;
    ContentView contentView = null;
    PaginatedList cvmList = null;
    int start = 0;
    int end = cacheSize;
    try {
      if ( (session != null) && (session.getAttribute("cv_bean") != null)) {
        contentViewBean = (ContentViewBean) session.getAttribute("cv_bean");
      }
      if (contentViewBean == null) {
        contentViewBean = new ContentViewBean();
        session.setAttribute("cv_bean", contentViewBean);
      }
      if ( (session != null) && (session.getAttribute("contentView") != null)) {
        contentView = (ContentView) session.getAttribute("contentView");
      }
      if (contentView == null) {
    	  if( contentViewActionForm.getContentViewId() != 0) {
    		  contentView = contentViewBean.getContentViewClient().getContentView(contentViewActionForm.getContentViewId());
    		  session.setAttribute("contentView",contentView);
    	  } else 
    		  return actionMapping.findForward("main");
      }
      if (servletRequest.getParameter("pageDirection") != null) {
        if ( (session != null) && (session.getAttribute("cvmList") != null)) {
          cvmList = (PaginatedList) session.getAttribute("cvmList");
        }
        if (cvmList == null) {
          return actionMapping.findForward("main");
        }
        if ("next".equals(contentViewActionForm.getPageDirection())) {
          cvmList.nextPage();
          String cvIndex = contentView.getIdentifier().toString() + "index";
          if(cvmList.isLastPage() && session.getAttribute(cvIndex) != null) {
            List list = new ArrayList();
            int index = ((Integer)session.getAttribute(cvIndex)).intValue();
            boolean fetch = true;
            do {
	            start = index * cacheSize;
	            end = start + cacheSize;
	            ContentViewMember[] cvms = contentViewBean.getContentViewClient().getContentViewMembers(contentView,start,end); 
	            if(cvms.length < cacheSize) {
	              session.removeAttribute(cvIndex);
	              fetch = false;
	            } else
	              session.setAttribute(cvIndex,new Integer(++index));
	            for(int i=0; i< cvms.length; i++) {
	            	if(cvms[i].getAtom() != null) {
	            		list.add(cvms[i].getAtom());
	            	}
	            }
            } while(list.isEmpty() && fetch);
            cvmList.addAll(list);
          }
        }
        else if ("previous".equals(contentViewActionForm.getPageDirection())) {
          cvmList.previousPage();
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
      List list = new ArrayList();
      int index = 0;
      boolean fetch = true;
      do {
          start = index * cacheSize;
          end = start + cacheSize;
          ContentViewMember[] cvms = contentViewBean.getContentViewClient().getContentViewMembers(contentView,start,end); 
          if(cvms.length < cacheSize)
        	  fetch = false;
          else
        	  session.setAttribute(contentView.getIdentifier().toString() + "index",new Integer(++index));
          for(int i=0; i< cvms.length; i++) {
          	if(cvms[i].getAtom() != null) {
          		list.add(cvms[i].getAtom());
          	}
          }
      } while(list.isEmpty() && fetch);
      cvmList = new PaginatedArrayList(list, 80);
      session.setAttribute("cvmList", cvmList);
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
