/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SourceMetadataReport
 *
 * Changes
 *
 * 06/09/2006 TTN (1-BFPDH) : add midService field to represent it on web report
 *
 *****************************************************************************/
package gov.nih.nlm.meme.common;



public class SourceMetadataReport {
  private String midService;
  private SourceDifference[] sourceDifferences;
  private SourceDifference[] attributeNameDifferences;
  private SourceDifference[] termgroupDifferences;
  private SourceDifference[] relationshipAttributeDifferences;
  private SourceDifference[] suppressibleDifferences;

  public SourceDifference[] getSourceDifferences() {
    return sourceDifferences;
  }


  public SourceDifference[] getAttributeNameDifferences() {
    return attributeNameDifferences;
  }

  public SourceDifference[] getTermgroupDifferences() {
    return termgroupDifferences;
  }

  public SourceDifference[] getRelationshipAttributeDifferences() {
    return relationshipAttributeDifferences;
  }

  public void setSourceDifferences(SourceDifference[] sourceDifferences) {
    this.sourceDifferences = sourceDifferences;
  }

  public void setAttributeNameDifferences(SourceDifference[] attributeNameDifferences) {
    this.attributeNameDifferences = attributeNameDifferences;
  }

  public void setTermgroupDifferences(SourceDifference[] termgroupDifferences) {
    this.termgroupDifferences = termgroupDifferences;
  }

  public void setRelationshipAttributeDifferences(SourceDifference[] relationshipAttributeDifferences) {
    this.relationshipAttributeDifferences = relationshipAttributeDifferences;
  }


	public String getMidService() {
		return midService;
	}

	public void setMidService(String midService) {
		this.midService = midService;
	}


	public SourceDifference[] getSuppressibleDifferences() {
		return suppressibleDifferences;
	}


	public void setSuppressibleDifferences(
			SourceDifference[] suppressibleDifferences) {
		this.suppressibleDifferences = suppressibleDifferences;
	}

}
