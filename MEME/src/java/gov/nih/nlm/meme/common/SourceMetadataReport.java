package gov.nih.nlm.meme.common;



public class SourceMetadataReport {
  private SourceDifference[] sourceDifferences;
  private SourceDifference[] attributeNameDifferences;
  private SourceDifference[] termgroupDifferences;
  private SourceDifference[] relationshipAttributeDifferences;

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

}
