/************************************************************************
 *
 * Object:      RelSemantics.java
 *
 * Change History:
 *  01/24/2002: First version
 *  05/15/2002: Added "concept_id_1" field
 *  07/22/2002: Added "mapped_to" and "mapped_from" relationship
 *              attributes
 *  09/17/2002: Class name has been changes from "UMLSRelationship"
 *              to "RelSemantics".
 *
 ***********************************************************************/

package gov.nih.nlm.umls.jekyll.relae;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.umls.jekyll.JekyllKit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class RelSemantics {

  //
  // Static Fields
  //
  static Vector rel_values = new Vector();
  static {
    rel_values.add("BT");
    rel_values.add("NT");
    rel_values.add("RT");
    rel_values.add("LK");
    rel_values.add("RT?");
    rel_values.add("XR");
  }

  private static Hashtable rel_names = new Hashtable();
  static {
    rel_names.put("BT", "is broader than");
    rel_names.put("NT", "is narrower than");
    rel_names.put("RT", "is related to");
    rel_names.put("LK", "is like");
    rel_names.put("RT?", "maybe related to");
    rel_names.put("XR", "is not related to");
  }

  // the following is a set of relas for NLM03 project
  static HashMap attr_values = new HashMap();
  static {
    attr_values.put("isa", "NT");
    attr_values.put("inverse_isa", "BT");
    attr_values.put("tradename_of", "NT");
    attr_values.put("has_tradename", "BT");
    attr_values.put("ingredient_of", "RT");
    attr_values.put("has_ingredient", "RT");
    attr_values.put("contains", "RT");
    attr_values.put("contained_in", "RT");
    attr_values.put("consists_of", "RT");
    attr_values.put("constitutes", "RT");
    attr_values.put("has_scd", null);
    attr_values.put("scd_of", null);
    attr_values.put("has_dose_form", "RT");
    attr_values.put("dose_form_of", "RT");
    attr_values.put("has_form", "BT");
    attr_values.put("form_of", "NT");
    attr_values.put("mapped_to", null);
    attr_values.put("mapped_from", null);
  }

  private static Hashtable inverse_relas = new Hashtable();
  static {
    inverse_relas.put("isa", "inverse_isa");
    inverse_relas.put("inverse_isa", "isa");
    inverse_relas.put("tradename_of", "has_tradename");
    inverse_relas.put("has_tradename", "tradename_of");
    inverse_relas.put("ingredient_of", "has_ingredient");
    inverse_relas.put("has_ingredient", "ingredient_of");
    inverse_relas.put("contains", "contained_in");
    inverse_relas.put("contained_in", "contains");
    inverse_relas.put("consists_of", "constitutes");
    inverse_relas.put("constitutes", "consists_of");
    inverse_relas.put("has_scd", "scd_of");
    inverse_relas.put("scd_of", "has_scd");
    inverse_relas.put("has_dose_form", "dose_form_of");
    inverse_relas.put("dose_form_of", "has_dose_form");
    inverse_relas.put("has_form", "form_of");
    inverse_relas.put("form_of", "has_form");
    inverse_relas.put("mapped_to", "mapped_from");
    inverse_relas.put("mapped_from", "mapped_to");

  }

  private static String[] attributes = null;
  // Source of SAB and SL with which all relas are created
  private static String current_source = null;

  //
  // Static Methods
  //
  public static String[] getRelationshipNames() {
    return (String[]) rel_values.toArray(new String[0]);
  }

  public static String[] getRelationshipAttributes() {
    String[] attrs = (String[]) attr_values.keySet().toArray(new String[0]);
    Arrays.sort(attrs);
    return attrs;
  }

  public static String getCorrespondingRel(String attr) {
    return (String) attr_values.get(attr);
  }

  public static String getLongForm(String rel_name) {
    return (String) rel_names.get(rel_name);
  }

  /**
   * Returns current value for SAB and SL.
   *
   * @return String
   */
  static String getCurrentSAB_SL() {
    return current_source;
  }

  /**
   * Sets value for SAB and SL.
   * 
   * @param source
   */
  static void setCurrentSAB_SL(String source) {
      current_source = source;
  }
  
  /**
   * TODO: is there MEME4 method?
   * <br>
   * NOTE: there's <code>mth.srstre2</code> table, and
   * there are <code>meow.srstre2_<release> tables</code>.
   * Which one to use?
   *
   * @param concept_1 Concept
   * @param concept_2 Concept
   * @return String[]
   */
  public static String[] getAttributesRestrBySTYs(Concept concept_1,
                                                  Concept concept_2) {
//    Connection conn = null;
//    Statement stmt = null;
//    ResultSet rs = null;
//    Vector v = new Vector();
//    StringBuffer sb = new StringBuffer(500);
//
//    if (concept_1 == null && concept_2 == null) {
//      return null;
//    }
//
//    ConceptSemanticType[] STYs_1 = concept_1.getSemanticTypes();
//    ConceptSemanticType[] STYs_2 = concept_2.getSemanticTypes();
//
//    try {
//      // get a logical connection
//      conn = OraclePortal.getConnection();
//
//      // create a statement
//      stmt = conn.createStatement();
//
//      // nested loop is implemented to accommodate multiple STYs
//      for (int i = 0; i < STYs_1.length; i++) {
//        for (int k = 0; k < STYs_2.length; k++) {
//          sb.append("SELECT rel");
//          sb.append(" FROM mth.srstre2");
//          sb.append(" WHERE sty1 = '");
//          sb.append(STYs_1[i]);
//
//          sb.append("'");
//          sb.append(" AND sty2 = '");
//          sb.append(STYs_2[k]);
//          sb.append("'");
//
//          // populate a resultset
//          rs = stmt.executeQuery(sb.toString());
//
//          while (rs.next()) {
//            String rel = rs.getString("REL");
//            // avoiding duplicates
//            if (!v.contains(rel)) {
//              v.add(rel);
//            }
//          }
//
//          // closing ResultSet object
//          rs.close();
//          rs = null;
//
//          // clear StringBuffer
//          sb.delete(0, sb.length());
//
//          // accommodating assumption that "isa" should be present
//          // if two STYs are the same.
//          if (!v.contains("isa") && STYs_1[i].equals(STYs_2[k])) {
//            v.add("isa");
//          }
//        }
//      }
//    }
//    catch (SQLException ex) {
//      System.err.println("failed to retrieve relationship attributes");
//      System.err.println(ex.getMessage());
//    }
//    finally {
//      try {
//        // closing the ResultSet object
//        if (rs != null) {
//          rs.close();
//          rs = null;
//        }
//
//        // closing Statement object
//        stmt.close();
//        stmt = null;
//
//        // closing logical connection
//        conn.close();
//        conn = null;
//      }
//      catch (SQLException ex) {}
//    }
//
//    return (String[]) v.toArray(new String[0]);
    return new String[0];
  } // getAttributesRestrBySTYs()

  public static String[] getAllAttributes() throws Exception {
    if (attributes == null) {
      attributes = JekyllKit.getAuxDataClient().
          getValidRelationshipAttributes();
    }

    return attributes;
  } // getAllAttributes()

  /**
   * Used in <code>RelPane</code> object.
   * TODO: need to replace this with MEME4 method.
   * For now, we use Hashtable as workaround.
   *
   * @param attr String
   * @return String
   */
    public static String getInvertedAttribute(String attr) {
          return (String) inverse_relas.get(attr);
    }
//  public static String getInvertedAttribute(String attr) {
//    Connection conn = null;
//    Statement stmt = null;
//    ResultSet rs = null;
//    String inverted_attr = attr;
//
//    StringBuffer sb = new StringBuffer(500);
//    sb.append("SELECT inverse_rel_attribute");
//    sb.append(" FROM mth.inverse_rel_attributes");
//    sb.append(" WHERE relationship_attribute = '");
//    sb.append(attr);
//    sb.append("'");
//
//    try {
//      // get a logical connection
//      conn = OraclePortal.getConnection();
//
//      // create a statement
//      stmt = conn.createStatement();
//
//      // populate a resultset
//      rs = stmt.executeQuery(sb.toString());
//
//      while (rs.next()) {
//        inverted_attr = rs.getString("INVERSE_REL_ATTRIBUTE");
//      }
//    }
//    catch (SQLException ex) {
//      System.err.println("Failed to invert relationship attribute: " + attr);
//      System.err.println("Message: " + ex.getMessage());
//    }
//    finally {
//      try {
//        // closing ResultSet object
//        rs.close();
//        rs = null;
//
//        // closing Statement object
//        stmt.close();
//        stmt = null;
//
//        // closing logical connection
//        conn.close();
//        conn = null;
//      }
//      catch (SQLException ex) {}
//    }
//
//    return inverted_attr;
//  } // getInvertedAttribute()

//  public static String get_Current_SRDEF_RELA() {
//    Connection conn = null;
//    Statement stmt = null;
//    ResultSet rs = null;
//    String current_table = null;
//
//    StringBuffer sb = new StringBuffer(500);
//    sb.append("SELECT object_name");
//    sb.append(" FROM all_objects");
//    sb.append(" WHERE object_name like 'SRDEF_RELA_%'");
//    sb.append(" AND created =");
//    sb.append(" (SELECT MAX(created)");
//    sb.append(" FROM all_objects");
//    sb.append(" WHERE object_type = 'TABLE'");
//    sb.append(" AND owner = 'MEOW'");
//    sb.append(" AND object_name like 'SRDEF_RELA_%')");
//
//    try {
//      // get a logical connection
//      conn = OraclePortal.getConnection();
//
//      // create a statement
//      stmt = conn.createStatement();
//
//      // populate a resultset
//      rs = stmt.executeQuery(sb.toString());
//
//      while (rs.next()) {
//        current_table = rs.getString("OBJECT_NAME");
//      }
//    }
//    catch (SQLException ex) {
//      System.err.println("failed to retrieve current SRDEF_RELA% table");
//      System.err.println(ex.getMessage());
//    }
//    finally {
//      try {
//        // closing ResultSet object
//        rs.close();
//        rs = null;
//
//        // closing Statement object
//        stmt.close();
//        stmt = null;
//
//        // closing logical connection
//        conn.close();
//        conn = null;
//      }
//      catch (SQLException ex) {}
//    }
//
//    return "meow." + current_table;
//  } // get_Current_SRDEF_RELA()

}
