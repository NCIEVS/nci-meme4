<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="gov.nih.nlm.meme.MEMEToolkit" %>
<%@ page import="gov.nih.nlm.meme.common.Authority" %>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>
<%@ page import="gov.nih.nlm.mrd.web.LocalResponseWrapper" %>
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<%  String state = "ReleaseSummary";
    if("prepare".equals(request.getParameter("action"))) {
    ReleaseClient client = release_bean.getReleaseClient();
    ReleaseInfo releaseinfo = new ReleaseInfo();
    SimpleDateFormat dateformat = release_bean.getDateFormat();
    releaseinfo.setName(request.getParameter("release"));
    releaseinfo.setPreviousReleaseInfo(client.getReleaseInfo(request.getParameter("previous_release")));
    releaseinfo.setPreviousMajorReleaseInfo(client.getReleaseInfo(request.getParameter("previous_major_release")));
    releaseinfo.setDescription(request.getParameter("description"));
    if(! (request.getParameter("authority") == null || "".equals(request.getParameter("authority"))))
    	releaseinfo.setAuthority(new Authority.Default(request.getParameter("authority")));
    releaseinfo.setGeneratorClass("gov.nih.nlm.mrd.client." + request.getParameter("generator"));
    if(! (request.getParameter("administrator") == null || "".equals(request.getParameter("administrator"))))
    	releaseinfo.setAdministrator(new Authority.Default(request.getParameter("administrator")));
    releaseinfo.setBuildHost(request.getParameter("build_host"));
    releaseinfo.setBuildUri(request.getParameter("build_uri"));
    releaseinfo.setReleaseHost(request.getParameter("release_host"));
    releaseinfo.setReleaseUri(request.getParameter("release_uri"));
    releaseinfo.setDocumentationHost(request.getParameter("documentation_host"));
    releaseinfo.setDocumentationUri(request.getParameter("documentation_uri"));
    releaseinfo.setStartDate(dateformat.parse(request.getParameter("start_date")));
    releaseinfo.setEndDate(new java.util.Date());
    releaseinfo.setMEDStartDate(dateformat.parse(request.getParameter("med_start_date")));
    releaseinfo.setMBDStartDate(dateformat.parse(request.getParameter("mbd_start_date")));
    releaseinfo.setReleaseDate(dateformat.parse(request.getParameter("release_date")));
    releaseinfo.setIsBuilt(false);
    releaseinfo.setIsPublished(false);
    release_bean.setReleaseGenerator("gov.nih.nlm.mrd.client."+ request.getParameter("generator"));
    client.prepareRelease(releaseinfo);
    } else if("update".equals(request.getParameter("action"))) {
 	ReleaseClient rc = release_bean.getReleaseClient();
    	ReleaseInfo releaseinfo = rc.getReleaseInfo(request.getParameter("release"));
    	SimpleDateFormat dateformat = release_bean.getDateFormat();
    	releaseinfo.setDescription(request.getParameter("description"));
    	if(! (request.getParameter("authority") == null || "".equals(request.getParameter("authority"))))
    		releaseinfo.setAuthority(new Authority.Default(request.getParameter("authority")));
    	if(! (request.getParameter("administrator") == null || "".equals(request.getParameter("administrator"))))
    		releaseinfo.setAdministrator(new Authority.Default(request.getParameter("administrator")));
    	releaseinfo.setBuildHost(request.getParameter("build_host"));
    	releaseinfo.setBuildUri(request.getParameter("build_uri"));
    	releaseinfo.setReleaseHost(request.getParameter("release_host"));
    	releaseinfo.setReleaseUri(request.getParameter("release_uri"));
    	releaseinfo.setDocumentationHost(request.getParameter("documentation_host"));
    	releaseinfo.setDocumentationUri(request.getParameter("documentation_uri"));
    	releaseinfo.setMEDStartDate(dateformat.parse(request.getParameter("med_start_date")));
    	releaseinfo.setMBDStartDate(dateformat.parse(request.getParameter("mbd_start_date")));
    	releaseinfo.setReleaseDate(dateformat.parse(request.getParameter("release_date")));
    	rc.setReleaseInfo(releaseinfo);
    } else if("delete".equals(request.getParameter("action"))) {
	state = "ReleaseManager";
  	ReleaseClient rc = release_bean.getReleaseClient();
  	ReleaseInfo releaseinfo = rc.getReleaseInfo(request.getParameter("release"));
  	rc.removeReleaseInfo(releaseinfo);
    } else if("finish".equals(request.getParameter("action"))) {
     	ReleaseClient rc = release_bean.getReleaseClient();
    	ReleaseInfo releaseinfo = rc.getReleaseInfo(request.getParameter("release"));
    	String[] targets = rc.getTargetNames(releaseinfo.getName());
    	for (int i=0; i < targets.length;i++) {
	   	    StageStatus[] stagestatus = rc.getTargetStatus(releaseinfo.getName(),targets[i]).getStageStatus();
	   	    for(int j=0; j < stagestatus.length; j++) {
		        if("validate".equals(stagestatus[j].getName()) && ((stagestatus[j].getCode() & StageStatus.FINISHED) == StageStatus.FINISHED) &&
	        			((stagestatus[j].getCode() & StageStatus.ERROR) != StageStatus.ERROR)) {
		            HttpServletResponse hres = (HttpServletResponse) response;
		            // Create a wrapper object that "catches" output
		            LocalResponseWrapper lrw = new LocalResponseWrapper(hres);
	   				RequestDispatcher dispatcher =
	   	  	  			getServletContext().getRequestDispatcher(response.encodeURL("/mrd/controller?state=ManageTargets&action=QAReport&release=" + releaseinfo.getName() + "&handlers=" + targets[i]));
	   				if (dispatcher != null) {
	   	          		dispatcher.include(request,lrw);
	   				}
		        }
	   	    }
    	}
   		rc.finishRelease(releaseinfo);
    }
    response.sendRedirect("controller?state=" + state + "&release=" + request.getParameter("release"));
%>
