<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<jsp:useBean id="medlineproperty_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<%  AuxiliaryDataClient aux_client = medlineproperty_bean.getAuxiliaryDataClient();
   MetaProperty property = null;
   String category = request.getParameter("category");
   MetaProperty[] properties = aux_client.getMetaPropertiesByKeyQualifier(category);
   int rank = 0;
   for(int i=0; i<properties.length; i++) {
	int tmp = Integer.parseInt(properties[i].getExample());
	if(tmp > rank) {
      		rank = tmp;
	}
	if(properties[i].getIdentifier().toString().equals(request.getParameter("rowid"))) {
		property = properties[i];
	}
   }
   if("Delete".equals(request.getParameter("command"))) {
	aux_client.removeMetaProperty(property);
   } else if("Insert".equals(request.getParameter("command"))) {
	property = new MetaProperty();
	property.setKey(request.getParameter("key"));
	property.setKeyQualifier(request.getParameter("category"));
	property.setValue(request.getParameter("value"));
	property.setExample(String.valueOf(++rank));
	aux_client.addMetaProperty(property);
   } else if("Update".equals(request.getParameter("command"))) {
	aux_client.removeMetaProperty(property);
	property.setKey(request.getParameter("key"));
	property.setValue(request.getParameter("value"));
	aux_client.addMetaProperty(property);
   } else if("UpdateAll".equals(request.getParameter("command"))) {
	String[] rowids = request.getParameterValues("pattern");
	for(int i = 0; i < rowids.length; i++) {
		for(int j=0; j<properties.length; j++) {
			if(properties[j].getIdentifier().toString().equals(rowids[i])) {
				aux_client.removeMetaProperty(properties[j]);
				properties[j].setExample(String.valueOf(i));
				aux_client.addMetaProperty(properties[j]);
			}
		}
	}
   }
%>
    <jsp:forward page="mrd/controller">
      <jsp:param name="state" value="ListMedlineProperty" />
    </jsp:forward>


