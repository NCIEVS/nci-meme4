package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

public class MapSetActionForm
    extends ActionForm {
  private String MAPSETSEPARATORCODE;
  private String MAPSETXRTARGETID;
  private String UMLSMAPSETSEPARATOR;
  private String description;
  private String fromComplexity;
  private String fromSource;
  private String isFromExhaustive;
  private String isToExhaustive;
  private String mapSetComplexity;
  private String mapSetSource;
  private String toComplexity;
  private String toSource;
  public String getMAPSETSEPARATORCODE() {
    return MAPSETSEPARATORCODE;
  }

  public void setMAPSETSEPARATORCODE(String MAPSETSEPARATORCODE) {
    this.MAPSETSEPARATORCODE = MAPSETSEPARATORCODE;
  }

  public String getMAPSETXRTARGETID() {
    return MAPSETXRTARGETID;
  }

  public void setMAPSETXRTARGETID(String MAPSETXRTARGETID) {
    this.MAPSETXRTARGETID = MAPSETXRTARGETID;
  }

  public String getUMLSMAPSETSEPARATOR() {
    return UMLSMAPSETSEPARATOR;
  }

  public void setUMLSMAPSETSEPARATOR(String UMLSMAPSETSEPARATOR) {
    this.UMLSMAPSETSEPARATOR = UMLSMAPSETSEPARATOR;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setToSource(String toSource) {
    this.toSource = toSource;
  }

  public void setToComplexity(String toComplexity) {
    this.toComplexity = toComplexity;
  }

  public void setMapSetSource(String mapSetSource) {
    this.mapSetSource = mapSetSource;
  }

  public void setMapSetComplexity(String mapSetComplexity) {
    this.mapSetComplexity = mapSetComplexity;
  }

  public void setIsToExhaustive(String isToExhaustive) {
    this.isToExhaustive = isToExhaustive;
  }

  public void setIsFromExhaustive(String isFromExhaustive) {
    this.isFromExhaustive = isFromExhaustive;
  }

  public void setFromSource(String fromSource) {
    this.fromSource = fromSource;
  }

  public void setFromComplexity(String fromComplexity) {
    this.fromComplexity = fromComplexity;
  }

  public String getFromComplexity() {
    return fromComplexity;
  }

  public String getFromSource() {
    return fromSource;
  }

  public String getIsFromExhaustive() {
    return isFromExhaustive;
  }

  public String getIsToExhaustive() {
    return isToExhaustive;
  }

  public String getMapSetComplexity() {
    return mapSetComplexity;
  }

  public String getMapSetSource() {
    return mapSetSource;
  }

  public String getToComplexity() {
    return toComplexity;
  }

  public String getToSource() {
    return toSource;
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
