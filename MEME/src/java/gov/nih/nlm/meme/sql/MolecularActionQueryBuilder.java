/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MolecularActionQueryBuilder
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.SearchParameter;

import java.util.List;

/**
 * Builds a query for searching for molecular actions.
 *
 * @author MEME Group
 */
public class MolecularActionQueryBuilder extends QueryBuilder {

  /**
   * Builds molecular action query.
   * @param query the {@link StringBuffer}
   * @param params An array of object {@link SearchParameter}
   * @param list the {@link List}
   */
  public void build(StringBuffer query, SearchParameter[] params, List list) {

    MEMEToolkit.trace("MolecularActionQueryBuilder.build() ... ");

    String worklist = null;
    int concept_id = 0;
    int rownum = 0;

    // Start building up a query
    query.append("SELECT * FROM ")
        .append("(SELECT DISTINCT molecule_id FROM molecular_actions WHERE 1=1");

    // Determine if recursive search
    boolean recursive = false;
    for (int i = 0; i < params.length; i++) {
      if (params[i].getName().equals("recursive")) {
        recursive = true;
        break;
      }
    }

    for (int i = 0; i < params.length; i++) {
      String name = params[i].getName();

      // concept id lookup
      if (name.equals("concept_id")) {
        concept_id = params[i].getValue().intValue();
        if (!recursive) {
          query.append(" AND molecule_id IN ")
              .append("(SELECT molecule_id FROM molecular_actions")
              .append(" WHERE source_id = ?");
          list.add(String.valueOf(concept_id));
          query.append(" UNION ")
              .append("SELECT molecule_id FROM molecular_actions")
              .append(" WHERE target_id = ?)");
          list.add(String.valueOf(concept_id));
        }
      }

      // worklist lookup
      else if (name.equals("worklist")) {
        worklist = params[i].getValue().toString();
        if (!recursive) {
          query.append(" AND molecule_id IN ")
              .append("(SELECT molecule_id FROM molecular_actions a,")
              .append(" classes b, ").append(worklist).append(" c")
              .append(" WHERE source_id = b.concept_id")
              .append(" AND b.atom_id = c.atom_id")
              .append(" UNION")
              .append(" SELECT molecule_id FROM molecular_actions a,")
              .append(" classes b, ").append(worklist).append(" c")
              .append(" WHERE target_id = b.concept_id")
              .append(" AND b.atom_id = c.atom_id)");
        }
      }

      // core table clause
      else if (name.equals("core_table")) {
        String table_name = params[i].getValue().toString();
        query.append(" AND molecule_id IN ")
            .append("(SELECT molecule_id FROM atomic_actions")
            .append(" WHERE table_name = ?)");
        list.add(table_name);
      }

      // counter clause
      else if (name.equals("rownum")) {
        rownum = params[i].getValue().intValue();
      }

      else if (name.equals("recursive")) {
        // do nothing, we just avoid the current name
        // to be passed as one of the field in the query.
      }

      // any other parameters
      else {
        super.build(query, new SearchParameter[] {params[i]}
                    , list);
      }
    } // end for

    // Deal w/ recursive
    if (recursive == true) {
      if (worklist != null) {
        /**
                 query.append(" START WITH source_id IN ")
          .append("(SELECT concept_id FROM classes a, ")
          .append(worklist).append(" b ")
          .append("WHERE a.atom_id = b.atom_id)");
         **/
        query.append(" START WITH molecule_id IN ")
            .append("(SELECT molecule_id ")
            .append(" FROM molecular_actions a, classes b, ")
            .append(worklist).append(" c ")
            .append(
            " WHERE source_id = b.concept_id AND b.atom_id = c.atom_id ")
            .append(" UNION SELECT molecule_id ")
            .append(" FROM molecular_actions a, classes b, ")
            .append(worklist).append(" c ")
            .append(
            " WHERE target_id = b.concept_id AND b.atom_id = c.atom_id)");
      } else
      if (concept_id != 0) {
        query.append(" START WITH ? IN (source_id, target_id)");
        list.add(String.valueOf(concept_id));
      }
      query.append(" CONNECT BY source_id = PRIOR target_id ")
          .append("AND timestamp > PRIOR timestamp ")
          .append("AND molecular_action NOT LIKE 'MACRO%'");
    }

    query.append(")");

    // Wrap up
    if (rownum != 0) {
      query.append(" WHERE rownum <= ? ");
      list.add(String.valueOf(rownum));
    }

    query.append(" ORDER BY molecule_id");

  } // end method
} // end class