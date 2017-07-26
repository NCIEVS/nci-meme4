<!--
  This page provides a list of MEME application server hosts.
  Use it in a JSP page with this directive:

  < %@include file="/HostList.jsp" %>

  It assumes that there is a bean called "bean" with a getHost method.
-->
  <%
    String[] hosts_pre = MIDServices.getHostServicesList();
    String[] hosts = new String[hosts_pre.length+2];
    for (int i=0; i < hosts_pre.length; i++)
      hosts[i] = MIDServices.getService(hosts_pre[i]);
    hosts[hosts.length-2] = "localhost";
    hosts[hosts.length-1] = bean.getHost();
    Arrays.sort(hosts);
    String prev_host = "";
    for (int i=0; i < hosts.length; i++) {
      if (!hosts[i].equals(prev_host)) {
  %>
  <OPTION value="<%= hosts[i] %>" <%= bean.getHost().equals(hosts[i]) ? "SELECTED" : "" %> ><%= hosts[i] %></OPTION>
  <%
      }
      prev_host = hosts[i];
    }
  %>