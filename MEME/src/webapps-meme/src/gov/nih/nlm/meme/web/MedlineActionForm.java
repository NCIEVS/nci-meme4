/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  MedlineActionForm
 *
 * Changes:
 *   04/28/2006 TTN (1-77HMD): Added MedlineService to process Medline data
 *
 *****************************************************************************/
package gov.nih.nlm.meme.web;

import javax.servlet.http.*;

import org.apache.struts.action.*;

public class MedlineActionForm extends BaseForm {
    private String context;
    private String cutoffDate;
    private String midService = "";
    private String details;
    private String stage;
    private String action;
    private String file;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setMidService(String midService) {
        this.midService = midService;
    }

    public void setCutoffDate(String cutoff_date) {
        this.cutoffDate = cutoff_date;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getCutoffDate() {
        return cutoffDate;
    }

    public String getMidService() {
        return midService;
    }

    public String getStage() {
        return stage;
    }

    public String getDetails() {
        return details;
    }

    public String getAction() {
        return action;
    }

    public String getFile() {
        return file;
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
