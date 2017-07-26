/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  WorkActivityLogActionForm
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import org.apache.struts.action.*;
import javax.servlet.http.*;

public class WorkActivityLogActionForm extends ActionForm {
  private int range;
  private String state;
  private int work_id;
  private String midService;

  public String getMidService() {
    return midService;
  }
  public void setMidService(String midService) {
    this.midService = midService;
  }
  public int getRange() {
    return range;
  }
  public void setRange(int range) {
    this.range = range;
  }
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }
  public int getWork_id() {
    return work_id;
  }
  public void setWork_id(int work_id) {
    this.work_id = work_id;
  }
  public ActionErrors validate(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {
    /**@todo: finish this method, this is just the skeleton.*/
    return null;
  }
  public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {
  }
}
