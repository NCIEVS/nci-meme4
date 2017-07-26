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

public class InsertionProblemActionForm extends ActionForm {
    private String authority;
    private String date;
    private String feedback;
    private String origin;
    private String problem;
    private String recurrence;
    private String solution;
    private String identifier;
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setIdentifier(String workId) {
        this.identifier = workId;
    }

    public String getDate() {
        return date;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getOrigin() {
        return origin;
    }

    public String getProblem() {
        return problem;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public String getSolution() {
        return solution;
    }

    public String getIdentifier() {
        return identifier;
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
