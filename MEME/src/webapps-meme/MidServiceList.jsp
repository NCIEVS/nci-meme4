<!--
  This page provides a list of MEME application server hosts.
  Use it in a JSP page with this directive:

  < %@include file="/MidServiceList.jsp" %>

  It assumes that there is a bean called "bean" with a getMidService method.
-->
  <OPTION value="" <%= bean.getMidService().equals(MIDServices.getService("")) ? "SELECTED" : "" %>>&lt;default&gt;</OPTION>
  <% String[] mid_services = MIDServices.getDbServicesList();
     for (int i=0; i < mid_services.length; i++)
       mid_services[i] = MIDServices.getService(mid_services[i]);
     Arrays.sort(mid_services);
     String prev_mid_service = "";
     for (int i=0; i < mid_services.length; i++) {
       if (!mid_services[i].equals(prev_mid_service)) {
  %>
  <OPTION value="<%= mid_services[i] %>" <%= bean.getMidService().equals(mid_services[i]) ? "SELECTED" : "" %> ><%= mid_services[i] %></OPTION>
  <%
       }
       prev_mid_service = mid_services[i];
    }
  %>