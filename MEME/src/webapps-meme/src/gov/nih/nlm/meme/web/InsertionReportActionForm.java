/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  InsertionProblemActionForm
 * 08/31/2006 TTN(1-C4PMN): source insertion report web application
 *
 *****************************************************************************/
package gov.nih.nlm.meme.web;

import javax.servlet.http.*;

import org.apache.struts.action.*;

public class InsertionReportActionForm extends WorkActivityLogActionForm {
    private String tableCounts;
    public String getTableCounts() {
        return tableCounts;
    }

    public String getType() {
        return type;
    }

    public String getView() {
        return view;
    }

    private String type;
    private String view;

    public void setTableCounts(String tableCounts) {
        this.tableCounts = tableCounts;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setView(String view) {
        this.view = view;
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
