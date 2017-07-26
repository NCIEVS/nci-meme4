<%@ page contentType="text/html;charset=utf-8" errorPage= "IFrameErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Vector" %>
<%@ page import="gov.nih.nlm.mrd.web.*" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.io.*" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<%
  ReleaseClient rc = release_bean.getReleaseClient();
  ReleaseInfo release_info = rc.getReleaseInfo(request.getParameter("release"));
  String[] handlers = request.getParameterValues("handlers");
  if (handlers != null) {
    rc.activateTargetHandlers(handlers);
    Vector active = new Vector(Arrays.asList(handlers));
    String[] targets = rc.getTargetNames(release_info.getName());
    Vector deactive = new Vector(targets.length);
    for(int i=0; i < targets.length; i++)
      if(!active.contains(targets[i]))
        deactive.add(targets[i]);
    rc.deactivateTargetHandlers((String[])deactive.toArray(new String[0]));
  }

  //
  // Handle QA Report case
  //
  if("QAReport".equals(request.getParameter("action"))) {
    if(handlers != null) {
      for(int i=0; i< handlers.length; i++) {
      StageStatus[] status = rc.getTargetStatus(release_info.getName(),handlers[i]).getStageStatus();
      for(int j=0; j < status.length; j++) {
        if("validate".equals(status[j].getName()) && ((status[j].getCode() & StageStatus.FINISHED) == StageStatus.FINISHED) ) {
        String name = release_info.getDocumentationUri() + "/qa_" +
                      handlers[i] + ".rpt.html"; // qareport file
        //System.out.println(name);
	RequestDispatcher dispatcher =
  	  getServletContext().getRequestDispatcher(response.encodeURL("/TargetDetails.jsp?release=" +
              release_info.getName() + "&target=" + handlers[i] + "&type=html&detail=View QAReport"));
	if (dispatcher != null) {
  	  //System.out.println("Dispatching ....");
          // Downcast request objects
          HttpServletRequest hreq = (HttpServletRequest) request;
          HttpServletResponse hres = (HttpServletResponse) response;

          // Create a wrapper object that "catches" output
          LocalResponseWrapper lrw = new LocalResponseWrapper(hres);

          //System.out.println("do filter include");

          dispatcher.include(request, lrw);

          //System.out.println("Now post-process");

          // Now post-process the output
          String result = lrw.toString();

          // Save the HTML to file
	  PrintWriter pw =
          new PrintWriter(
            new OutputStreamWriter(
              new FileOutputStream(name,false),"UTF-8"));
	      pw.println(result);
	      pw.close();
              //System.out.println("Return ....");
	    }
        name = release_info.getDocumentationUri() + "/qa_" +
                      handlers[i] + ".xml"; // qareport file
        //System.out.println(name);
	dispatcher =
  	  getServletContext().getRequestDispatcher(response.encodeURL("/TargetDetails.jsp?release=" +
              release_info.getName() + "&target=" + handlers[i] + "&type=xml&detail=View QAReport"));
	if (dispatcher != null) {
  	  //System.out.println("Dispatching ....");
          // Downcast request objects
          HttpServletRequest hreq = (HttpServletRequest) request;
          HttpServletResponse hres = (HttpServletResponse) response;

          // Create a wrapper object that "catches" output
          LocalResponseWrapper lrw = new LocalResponseWrapper(hres);

          //System.out.println("do filter include");

          dispatcher.include(request, lrw);

          //System.out.println("Now post-process");

          // Now post-process the output
          String result = lrw.toString();

          // Save the xml to file
	  PrintWriter pw =
          new PrintWriter(
            new OutputStreamWriter(
              new FileOutputStream(name,false),"UTF-8"));
	      pw.println(result);
	      pw.close();
              //System.out.println("Return ....");
	    }
    }
    }
  }
    }
  }  else if("clearLog".equals(request.getParameter("action"))) {
	rc.clearStatus(request.getParameter("release"),request.getParameter("target"),request.getParameter("stage"));
  }
  else if(!"null".equals(request.getParameter("action"))) {
      rc.doProcess(request.getParameter("action"),request.getParameter("release"));
  }
%>
