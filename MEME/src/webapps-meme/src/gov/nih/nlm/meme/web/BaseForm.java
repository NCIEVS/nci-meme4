package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

public class BaseForm extends ActionForm {
    private String host;
    private String port;
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public ActionErrors validate(ActionMapping actionMapping,
                                 HttpServletRequest httpServletRequest) {
            /** @todo: finish this method, this is just the skeleton.*/
        return null;
    }

    public void reset(ActionMapping actionMapping,
                      HttpServletRequest servletRequest) {
    }
}
