/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteCoreData
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.ByNameAttributeRestrictor;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.NonSourceAssertedRestrictor;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Test suite for CoreData
 */
public class TestSuiteCoreData extends TestSuite {

  public TestSuiteCoreData() {
    setName("TestSuiteCoreData");
    setDescription("Test Suite for CoreData");
  }

  /**
   * Perform Test Suite CoreData
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    CoreData cd = new CoreData.Default();

    addToLog("    1. Test CoreData: setSource(), getSource() ... "
             + date_format.format(timestamp));

    Source src = new Source.Default();
    src.setSourceAbbreviation("MSH2001");
    cd.setSource(src);
    if (cd.getSource().equals(src))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test CoreData: setGenerated(), isGenerated() ... "
             + date_format.format(timestamp));

    cd.setGenerated(true);
    if (cd.isGenerated())
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test CoreData: setDead(), isDead() ... "
             + date_format.format(timestamp));

    cd.setDead(true);
    if (cd.isDead())
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.1. Test CoreData: setStatus(), isReviewed() ... "
             + date_format.format(timestamp));

    cd.setStatus(CoreData.FV_STATUS_REVIEWED);
    if (cd.isReviewed())
      addToLog("    4.1. Test Passed");
    else {
      addToLog("    4.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.2. Test CoreData: setStatus(), isApproved() ... "
             + date_format.format(timestamp));

    cd.setStatus(CoreData.FV_STATUS_REVIEWED);
    if (cd.isApproved())
      addToLog("    4.2. Test Passed");
    else {
      addToLog("    4.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.3. Test CoreData: setStatus(), isUnreviewed() ... "
             + date_format.format(timestamp));

    cd.setStatus(CoreData.FV_STATUS_UNREVIEWED);
    if (cd.isUnreviewed())
      addToLog("    4.3. Test Passed");
    else {
      addToLog("    4.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.4. Test CoreData: setStatus(), needsReview() ... "
             + date_format.format(timestamp));

    cd.setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
    if (cd.needsReview())
      addToLog("    4.4. Test Passed");
    else {
      addToLog("    4.4. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.5. Test CoreData: setStatus(), isDemoted() ... "
             + date_format.format(timestamp));

    cd.setStatus(CoreData.FV_STATUS_DEMOTED);
    if (cd.isDemoted())
      addToLog("    4.5. Test Passed");
    else {
      addToLog("    4.5. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.6. Test CoreData: getStatus() ... "
             + date_format.format(timestamp));

    if (cd.getStatus() == CoreData.FV_STATUS_DEMOTED)
      addToLog("    4.6. Test Passed");
    else {
      addToLog("    4.6. Test Failed");
      thisTestFailed();
    }

    addToLog("    4.7. Test CoreData: setStatus(), isEmbryo() ... "
             + date_format.format(timestamp));

    cd.setStatus(CoreData.FV_STATUS_EMBRYO);
    if (cd.isEmbryo())
      addToLog("    4.7. Test Passed");
    else {
      addToLog("    4.7. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test CoreData: setAuthority(), getAuthority() ... "
             + date_format.format(timestamp));

    Authority authority = new Authority.Default("AUTHOR");
    cd.setAuthority(authority);
    if (cd.getAuthority().equals(authority))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test CoreData: setTimestamp(), getTimestamp() ... "
             + date_format.format(timestamp));

    cd = new CoreData.Default();
    java.util.Date today = new java.util.Date();
    cd.setTimestamp(today);
    if (cd.getTimestamp().equals(today))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test CoreData: setInsertionDate(), getInsertionDate() ... "
             + date_format.format(timestamp));

    java.util.Date insertion_date = new java.util.Date();
    cd.setInsertionDate(insertion_date);
    if (cd.getInsertionDate().equals(insertion_date))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8.1. Test CoreData: setReleased(), wasReleasedAsApproved() ... "
             + date_format.format(timestamp));

    cd.setReleased(CoreData.FV_RELEASED_AS_APPROVED);
    if (cd.wasReleasedAsApproved())
      addToLog("    8.1. Test Passed");
    else {
      addToLog("    8.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    8.2. Test CoreData: setReleased(), wasReleasedAsUnreviewed() ... "
             + date_format.format(timestamp));

    cd.setReleased(CoreData.FV_RELEASED_AS_UNREVIEWED);
    if (cd.wasReleasedAsUnreviewed())
      addToLog("    8.2. Test Passed");
    else {
      addToLog("    8.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    8.3. Test CoreData: setReleased(), wasReleased() ... "
             + date_format.format(timestamp));

    cd.setReleased(CoreData.FV_RELEASED_AS_UNREVIEWED);
    if (cd.wasReleased())
      addToLog("    8.3. Test Passed");
    else {
      addToLog("    8.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    8.4. Test CoreData: getReleased() ... "
             + date_format.format(timestamp));

    if (cd.getReleased() == CoreData.FV_RELEASED_AS_UNREVIEWED)
      addToLog("    8.4. Test Passed");
    else {
      addToLog("    8.4. Test Failed");
      thisTestFailed();
    }


    addToLog("    9.1. Test CoreData: setTobereleased(), isReleasable() ... "
             + date_format.format(timestamp));

    cd.setTobereleased(CoreData.FV_RELEASABLE);
    if (cd.isReleasable())
      addToLog("    9.1. Test Passed");
    else {
      addToLog("    9.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    9.2. Test CoreData: setTobereleased(), isWeaklyReleasable() ... "
             + date_format.format(timestamp));

    cd.setTobereleased(CoreData.FV_WEAKLY_RELEASABLE);
    if (cd.isWeaklyReleasable())
      addToLog("    9.2. Test Passed");
    else {
      addToLog("    9.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    9.3. Test CoreData: setTobereleased(), isWeaklyUnreleasable() ... "
             + date_format.format(timestamp));

    cd.setTobereleased(CoreData.FV_WEAKLY_UNRELEASABLE);
    if (cd.isWeaklyUnreleasable())
      addToLog("    9.3. Test Passed");
    else {
      addToLog("    9.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    9.4. Test CoreData: setTobereleased(), isUnreleasable() ... "
             + date_format.format(timestamp));

    cd.setTobereleased(CoreData.FV_UNRELEASABLE);
    if (cd.isUnreleasable())
      addToLog("    9.4. Test Passed");
    else {
      addToLog("    9.4. Test Failed");
      thisTestFailed();
    }

    addToLog("    9.5. Test CoreData: getTobereleased() ... "
             + date_format.format(timestamp));

    if (cd.getTobereleased() == CoreData.FV_UNRELEASABLE)
      addToLog("    9.5. Test Passed");
    else {
      addToLog("    9.5. Test Failed");
      thisTestFailed();
    }

    addToLog("    10. Test CoreData: setLastAction(), getLastAction() ... "
             + date_format.format(timestamp));

    LoggedAction action = null;
    cd.setLastAction(action);
    if (cd.getLastAction() == null)
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
      thisTestFailed();
    }

    addToLog("    11. Test CoreData: setSuppressible(), isSuppressible() ... "
             + date_format.format(timestamp));

    cd.setSuppressible("E");
    if (cd.isSuppressible())
      addToLog("    11. Test Passed");
    else {
      addToLog("    11. Test Failed");
      thisTestFailed();
    }

    addToLog("    12.1. Test CoreData: setLevel(), isMTHAsserted() ... "
             + date_format.format(timestamp));

    cd.setLevel(CoreData.FV_MTH_ASSERTED);
    if (cd.isMTHAsserted())
      addToLog("    12.1. Test Passed");
    else {
      addToLog("    12.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    12.2. Test CoreData: setLevel(), isSourcesserted() ... "
             + date_format.format(timestamp));

    cd.setLevel(CoreData.FV_SOURCE_ASSERTED);
    if (cd.isSourceAsserted())
      addToLog("    12.2. Test Passed");
    else {
      addToLog("    12.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    12.3. Test CoreData: setLevel(), isAtomLevel() ... "
             + date_format.format(timestamp));

    cd.setLevel(CoreData.FV_SOURCE_ASSERTED);
    if (cd.isAtomLevel())
      addToLog("    12.3. Test Passed");
    else {
      addToLog("    12.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    12.4. Test CoreData: getLevel() ... "
             + date_format.format(timestamp));

    if (cd.getLevel() == CoreData.FV_SOURCE_ASSERTED)
      addToLog("    12.4. Test Passed");
    else {
      addToLog("    12.4. Test Failed");
      thisTestFailed();
    }

    addToLog("    12.5. Test CoreData: setLevel(), isConceptLevel() ... "
             + date_format.format(timestamp));

    cd.setLevel(CoreData.FV_MTH_ASSERTED);
    if (cd.isConceptLevel())
      addToLog("    12.5. Test Passed");
    else {
      addToLog("    12.5. Test Failed");
      thisTestFailed();
    }

    addToLog("    13.1. Test CoreData: setSrcIdentifier(), getSrcIdentifier() ... "
             + date_format.format(timestamp));

    Identifier identifier = new Identifier.Default("MY_IDENTITY");
    cd.setSrcIdentifier(identifier);
    if (cd.getSrcIdentifier().equals(identifier))
      addToLog("    13.1. Test Passed");
    else {
      addToLog("    13.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    13.2. Test CoreData: setIdentifier(), getIdentifier() ... "
             + date_format.format(timestamp));

    cd.setIdentifier(identifier);
    if (cd.getIdentifier().equals(identifier))
      addToLog("    13.2. Test Passed");
    else {
      addToLog("    13.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    13.3. Test CoreData: setNativeIdentifier(), getNativeIdentifier() ... "
             + date_format.format(timestamp));

    NativeIdentifier ni = new NativeIdentifier("CODE", "SOURCE_AUI", "MTH", "", "");
    cd.setNativeIdentifier(ni);
    if (cd.getNativeIdentifier().equals(ni))
      addToLog("    13.3. Test Passed");
    else {
      addToLog("    13.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    13.4. Test CoreData: setSourceIdentifier(), getSourceIdentifier() ... "
             + date_format.format(timestamp));

    cd.setSourceIdentifier(identifier);
    if (cd.getSourceIdentifier().equals(identifier))
      addToLog("    13.4. Test Passed");
    else {
      addToLog("    13.4. Test Failed");
      thisTestFailed();
    }

    addToLog("    14. Test CoreData: setRank(), getRank() ... "
             + date_format.format(timestamp));

    Rank rank = new Rank.Default(12345);
    cd.setRank(rank);
    if (cd.getRank().equals(rank))
      addToLog("    14. Test Passed");
    else {
      addToLog("    14. Test Failed");
      thisTestFailed();
    }

    addToLog("    15.1. Test CoreData: clearAttribute() ... "
             + date_format.format(timestamp));

    cd.clearAttributes();

    addToLog("    15.2. Test CoreData: addAttribute(), getAttribute() ... "
             + date_format.format(timestamp));

    Attribute[] x_attributes = new Attribute[10];
    x_attributes[0] = new Attribute.Default(1041);
    x_attributes[0].setName("SEMANTIC_TYPE");
    cd.addAttribute(x_attributes[0]);
    x_attributes[1] = new Attribute.Default(1042);
    x_attributes[1].setName("SOS");
    cd.addAttribute(x_attributes[1]);
    x_attributes[2] = new Attribute.Default(1043);
    x_attributes[2].setName("SRC");
    cd.addAttribute(x_attributes[2]);
    Attribute[] y_attributes = cd.getAttributes();
    boolean same_array = true;
    for (int i = 0; i < y_attributes.length; i++) {
      if (! (x_attributes[i].equals(y_attributes[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    15.2. Test Passed");
    else {
      addToLog("    15.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    15.3. Test CoreData: getAttributesByName() ... "
             + date_format.format(timestamp));

    Attribute[] attrs = cd.getAttributesByName("SEMANTIC_TYPE");
    if (attrs.length > 0)
      addToLog("    15.3. Test Passed");
    else {
      addToLog("    15.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    15.4. Test CoreData: getAttributesByNames() ... "
             + date_format.format(timestamp));

    String[] names = new String[] { "SEMANTIC_TYPE", "SOS", "SRC", "CONTEXT", };
    attrs = cd.getAttributesByNames(names);
    boolean found = false;
    for (int i = 0; i < attrs.length; i++) {
      addToLog("          attrs["+i+"]= " + attrs[i].getName());
      found = true;
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }
    if (found)
      addToLog("    15.4. Test Passed");
    else {
      addToLog("    15.4. Test Failed");
      thisTestFailed();
    }

    addToLog("    15.5. Test CoreData: getRestrictedAttributes() ... "
             + date_format.format(timestamp));

    attrs = cd.getRestrictedAttributes(new ByNameAttributeRestrictor(names));
    found = false;
    for (int i = 0; i < attrs.length; i++) {
      addToLog("          attrs["+i+"]= " + attrs[i].getName());
      found = true;
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }
    if (found)
      addToLog("    15.5. Test Passed");
    else {
      addToLog("    15.5. Test Failed");
      thisTestFailed();
    }

    addToLog("    15.6. Test CoreData: gettFormattedContexts() ... "
             + date_format.format(timestamp));

    attrs = cd.getFormattedContexts();
    for (int i = 0; i < attrs.length; i++) {
      addToLog("          attrs["+i+"]= " + attrs[i].getName());
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }

    addToLog("    15.7. Test CoreData: removeAttribute(), getAttribute() ... "
             + date_format.format(timestamp));

    cd.removeAttribute(x_attributes[1]);
    y_attributes = cd.getAttributes();
    same_array = true;
    for (int i = 0; i < y_attributes.length; i++) {
      if (! (x_attributes[i].equals(y_attributes[i]))) {
        same_array = false;
      }
    }
    if (!same_array)
      addToLog("    15.7. Test Passed");
    else {
      addToLog("    15.7. Test Failed");
      thisTestFailed();
    }

    Comparator oc = new Comparator() {
      public int compare(Object object1, Object object2) {
        return ( (Comparable) object1).compareTo(object2);
      }
    };

    addToLog("    15.8. Test CoreData: getAttributes(), getSortedAttributes() ... "
             + date_format.format(timestamp));

    if (! (cd.getAttributes().equals(cd.getSortedAttributes(oc))))
      addToLog("    15.8. Test Passed");
    else {
      addToLog("    15.8. Test Failed");
      thisTestFailed();
    }

    addToLog("    16. Test CoreData: setClusterIdentifier(), getClusterIdentifier() ... "
             + date_format.format(timestamp));

    cd.setClusterIdentifier(identifier);
    if (cd.getClusterIdentifier().equals(identifier))
      addToLog("    16. Test Passed");
    else {
      addToLog("    16. Test Failed");
      thisTestFailed();
    }

    addToLog("    17.1. Test CoreData: clearRelationships(), addRelationship(), getRelationships() ... "
             + date_format.format(timestamp));

    cd.clearRelationships();
    Relationship[] x_relationships = new Relationship[10];
    x_relationships[0] = new Relationship.Default(1041);
    x_relationships[0].setName("RT");
    cd.addRelationship(x_relationships[0]);
    x_relationships[1] = new Relationship.Default(1042);
    x_relationships[1].setName("NT");
    cd.addRelationship(x_relationships[1]);
    x_relationships[2] = new Relationship.Default(1043);
    x_relationships[2].setName("SFO");
    cd.addRelationship(x_relationships[2]);
    Relationship[] y_relationships = cd.getRelationships();
    same_array = true;
    for (int i = 0; i < y_relationships.length; i++) {
      if (! (x_relationships[i].equals(y_relationships[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    17.1. Test Passed");
    else {
      addToLog("    17.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    17.2. Test CoreData: getRestrictedRelationships() ... "
             + date_format.format(timestamp));

    Relationship[] rels = cd.getRestrictedRelationships(new NonSourceAssertedRestrictor());
    found = false;
    for (int i = 0; i < rels.length; i++) {
      addToLog("          rels["+i+"]= " + rels[i].getName());
      found = true;
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }
    if (found)
      addToLog("    17.2. Test Passed");
    else {
      addToLog("    17.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    17.3 Test CoreData: removeRelationship(), getRelationships() ... "
             + date_format.format(timestamp));

    cd.removeRelationship(x_relationships[1]);
    y_relationships = cd.getRelationships();
    same_array = true;
    for (int i = 0; i < y_relationships.length; i++) {
      if (! (x_relationships[i].equals(y_relationships[i]))) {
        same_array = false;
      }
    }
    if (! (same_array))
      addToLog("    17.3. Test Passed");
    else {
      addToLog("    17.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    17.4. Test CoreData: getRelationships(), getSortedRelationships() ... "
             + date_format.format(timestamp));

    oc = new Comparator() {
      public int compare(Object object1, Object object2) {
        return ( (Comparable) object1).compareTo(object2);
      }
    };

    if (! (cd.getRelationships().equals(cd.getSortedRelationships(oc))))
      addToLog("    17.4. Test Passed");
    else {
      addToLog("    17.4. Test Failed");
      thisTestFailed();
    }

    addToLog("");

    if (this.isPassed())
      addToLog("    All tests passed");
    else
      addToLog("    At least one test did not complete successfully");

      //
      // Main Footer
      //

    addToLog("");

    addToLog("-------------------------------------------------------");
    addToLog("Finished TestSuiteCoreData at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}