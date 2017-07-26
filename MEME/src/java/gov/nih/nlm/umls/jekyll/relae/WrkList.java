/**
 * WrkList.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.util.*;

import gov.nih.nlm.umls.jekyll.JekyllKit;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptCluster;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.BySourceRestrictor;

public class WrkList {

  //
  // Private Fields
  //
  private Object[][] clusters;
  private boolean is_worklist = true;

  private ConceptWorklist cw = null;
  private ConceptChecklist cc = null;
  private Date worklist_creation_date = null;

  //
  // Constructors
  //
  public WrkList(String worklist) {
    try {
      if (worklist.startsWith("wrk")) {
        cw = JekyllKit.getWorklistClient().getConceptWorklist(worklist);
        worklist_creation_date = cw.getCreationDate();
      }
      else {
        cc = JekyllKit.getWorklistClient().getConceptChecklist(worklist);
        worklist_creation_date = cc.getCreationDate();
        is_worklist = false;
      }

      initWorklist();
    }
    catch (Exception ex) {
      if (ex.getMessage().startsWith(
          "Not a valid worklist/checklist for Rela editing.")) {
        MEMEToolkit.notifyUser(ex.getMessage());
      }
      else {
        ex.printStackTrace();
        MEMEToolkit.notifyUser("Failed to load a worklist/checklist.\n"
                               + "\nLog file may contain more information.");
      }

      clusters = null;
    }
  }

  //
  // Methods
  //

  private void initWorklist() throws Exception {
    ConceptCluster[] c_clusters = null;

    if (is_worklist) {
      c_clusters = (ConceptCluster[]) cw.getClusters().toArray(new
          ConceptCluster[
          0]);
    }
    else {
      c_clusters = (ConceptCluster[]) cc.getClusters().toArray(new
          ConceptCluster[
          0]);
    }

    clusters = new Object[c_clusters.length][5];
    int bkg_color = 0; // 0 for default (white) color, 1 for gray color
    for (int i = 0; i < c_clusters.length; i++) {
      if (c_clusters[i].getConcepts().length > 2) {
        throw new Exception("Not a valid worklist/checklist for Rela editing.");
      }

      Concept[] concepts = c_clusters[i].getConcepts();
      bkg_color = (bkg_color == 0) ? 1 : 0; // toggle background color

      clusters[i][0] = new Integer(c_clusters[i].getIdentifier().intValue());
      clusters[i][1] = JekyllKit.getCoreDataClient().getConcept(concepts[0]);
      if (concepts.length == 1) {
        clusters[i][2] = null;
        clusters[i][3] = new Integer(checkStatus(c_clusters[i].getConcepts()[0],
                                                 null));
      }
      else {
        clusters[i][2] = JekyllKit.getCoreDataClient().getConcept(concepts[1]);
        clusters[i][3] = new Integer(checkStatus(c_clusters[i].getConcepts()[0],
                                                 c_clusters[i].getConcepts()[1]));
      }
      clusters[i][4] = new Integer(bkg_color);
    } // clusters loop

  } // initWorklist()

  // 0 is for unreviewed, 1 is for reviewed
  private int checkStatus(Concept concept_1, Concept concept_2) throws
      Exception {
    int reviewed = 0;

    if (JekyllKit.getCoreDataClient().getRelationshipCount(concept_1) > 1000) {
      MEMEToolkit.notifyUser("There are more than 1000 relationships"
                             + "\nfor the cluster of concepts:"
                             + concept_1.getIdentifier().toString()
                             + " and "
                             +
                             ( (concept_2 == null) ? "0" :
                              concept_2.getIdentifier().toString())
                             + "\nThe check whether cluster has been reviewed"
                             + "\nbefore will not be performed.");
      return reviewed;
    }

    JekyllKit.getCoreDataClient().populateRelationships(concept_1);
    Relationship[] rels = concept_1.getRestrictedRelationships(new
        BySourceRestrictor(RelSemantics.getCurrentSAB_SL()));

    for (int i = 0; i < rels.length; i++) {
      if (rels[i].getRelatedConcept().equals(concept_2)) {
        if (rels[i].getTimestamp().after(worklist_creation_date)) {
          reviewed = 1;
        }
      }
    }

    return reviewed;
  } // checkStatus()

  public Vector getData() {
    if (clusters == null) {
      return null;
    }

    Vector v = new Vector();

    for (int i = 0; i < clusters.length; i++) {
      v.add( ( (Concept) clusters[i][1]).getIdentifier());
      if (clusters[i][2] == null) {
        v.add(new Integer(0));
      }
      else {
        v.add( ( (Concept) (clusters[i][2])).getIdentifier());
      }
    } // for loop

    return v;
  }

  /**
   * Returns true if <code>WrkList</code> object has
   * no data, false otherwise.
   *
   * @return true if empty
   */
  public boolean isEmpty() {
    if (getData() == null) {
      return true;
    }
    else {
      return false;
    }
  } // isEmpty()

  // 1 pair has been reviewed by editor
  // o otherwise
  public boolean isReviewed(int index) {
    if (index % 2 != 0) {
      index -= 1;
    }
    index = index / 2;
    if ( ( (Integer) clusters[index][3]).intValue() == 1) {
      return true;
    }
    else {
      return false;
    }
  }

  // 1 for gray color
  // o for default color
  public boolean isBkgGray(int index) {
    if (index % 2 != 0) {
      index -= 1;
    }
    index = index / 2;
    if ( ( (Integer) clusters[index][4]).intValue() == 1) {
      return true;
    }
    else {
      return false;
    }
  } // isBkgGray()

  public int find(int cluster_id) {
    int index = -1;

    for (int i = 0; i < clusters.length; i++) {
      if ( ( (Integer) clusters[i][0]).intValue() == cluster_id) {
        index = i;
        break;
      }
    }

    return index;
  } // find()

  public void setReviewedStatus(int index) {
    if (index % 2 != 0) {
      index -= 1;
    }
    index = index / 2;
    clusters[index][3] = new Integer(1);
  } // setReviewedStatus()

  public Concept[] getConcepts(int index) {
    if (index % 2 != 0) {
      index -= 1;
    }

    index = index / 2;
    Concept concept_1 = (Concept) clusters[index][1];
    Concept concept_2 = (clusters[index][2] == null) ? null :
        (Concept) clusters[index][2];

    return new Concept[] {
        concept_1, concept_2};
  } // getConcepts()

  public static Relationship[] getNLM_Rels(Concept concept_1, Concept concept_2) {
    Relationship[] rels = concept_1.getRelationships();

    Vector v = new Vector();
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].getSource().toString().startsWith("NLM") &&
          ( (concept_2 == null) || (rels[i].getRelatedConcept() == concept_2))) {
        v.add(rels[i]);
      }
    }

    return (Relationship[]) v.toArray(new Relationship[0]);
  } // getNLM_Rels()

  // if the this worklist object is populated.
  public Relationship[] getNLM_Rels(int index) {
    if (index % 2 != 0) {
      index -= 1;
    }
    index = index / 2;

    Concept concept_1 = (Concept) clusters[index][1];
    Concept concept_2 = (Concept) clusters[index][2];

    Relationship[] rels = concept_1.getRelationships();

    Vector v = new Vector();
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].getSource().toString().startsWith("NLM") &&
          ( (concept_2 == null) || (rels[i].getRelatedConcept() == concept_2))) {
        v.add(rels[i]);
      }
    }

    return (Relationship[]) v.toArray(new Relationship[0]);
  } // getNLM_Rels()

//  public Relationship getOther_Rels(Concept concept_1, Concept concept_2) {
//
//  } // getOther_Rels()

}
