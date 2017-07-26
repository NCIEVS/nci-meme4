<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="java.util.*, gov.nih.nlm.meme.client.*"
         errorPage= "IFrameErrorPage.jsp" %>

<HTML>
<HEAD>
</HEAD>
<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.ActionSequenceBean" />
<jsp:setProperty name="bean" property="*" />

<%
  final AdminClient client = bean.getAdminClient();
  client.getRequestHandler().setHost(bean.getHost());
  client.getRequestHandler().setPort(Integer.parseInt(bean.getPort()));
  bean.setSessionId(bean.getSessionId());
  int progress = 0;
  if (bean.getSessionId().equals("-1")) {
    client.initiateSession();
    bean.setSessionId(client.getSessionId());
    final String sequence = bean.getSequence();
    Thread t = new Thread(
      new Runnable() {
        public void run() {
          try {
            client.runActionSequence("sequence_"+sequence);
          } catch (Exception e) {}
        };
      }
    );
    t.start();
  } else if (bean.getTerminate()) {
    client.setSessionId(bean.getSessionId());
    client.terminateSession();
    return;
  } else {
    progress = client.getSessionProgress(bean.getSessionId());
  }

%>
<BODY onLoad="parent.setProgress(<%= bean.getUsers() %>,'<%= progress %>','<%= bean.getSessionId() %>')">
</BODY>
</HTML>