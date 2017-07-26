/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  SemanticTypeQueryBuilder
 *
 *****************************************************************************/

package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.SearchParameter;

import java.util.List;

/**
 * This dynamicaly builds an STY query from a list of {@link SearchParameter}s.
 *
 * @author MEME Group
 */

public class SemanticTypeQueryBuilder {

  /**
   * Builds query clauses for the specified {@link SearchParameter}s.
   * @param query a {@link StringBuffer} containing the query so far
   * @param params the {@link SearchParameter}s
   * @param list the list of bind values for the query so far
   */
  public void build(StringBuffer query, SearchParameter[] params, List list) {

    for (int i = 0; i < params.length; i++) {
      // Extract parameters
      final String name = params[i].getName();
      final Identifier value = params[i].getValue();
      final Identifier[] values = params[i].getValues();

      if (!name.equals("semantic_type") &&
          !name.equals("chemical_semantic_type") &&
          !name.equals("non_chemical_semantic_type")) {

        // disallow other name
        continue;
      }

      if (name.equals("chemical_semantic_type") ||
          name.equals("non_chemical_semantic_type")) {
        query.append(" AND concept_id IN ")
            .append("(SELECT concept_id FROM attributes a, semantic_types b ")
            .append("WHERE attribute_name||'' = 'SEMANTIC_TYPE' ")
            .append("AND attribute_value = semantic_type");

        if (name.equals("chemical_semantic_type")) {
          query.append(" AND editing_chem = 'Y')");
        } else if (name.equals("non_chemical_semantic_type")) {
          query.append(" AND editing_chem = 'N')");
        }
      }

      if (name.equals("semantic_type")) {
        query.append(" AND concept_id IN ")
            .append("(SELECT concept_id FROM attributes ")
            .append("WHERE attribute_name||'' = 'SEMANTIC_TYPE' ")
            .append("AND attribute_value ");

        // Single value means equality test should be used
        if (params[i].isSingleValueSearch()) {
          if (value != null) {
            if (params[i].isNegated()) {
              query.append("!");
            }
            query.append("= ?");
            list.add(value.toString());
          } else {
            query.append("IS ");
            if (params[i].isNegated()) {
              query.append("NOT ");
            }
            query.append("NULL");
          }
        }

        // Multiple value search means a set membership test.
        else if (params[i].isMultipleValueSearch()) {
          if (params[i].isNegated()) {
            query.append("NOT ");
          }
          query.append("IN (");
          for (int j = 0; j < values.length; j++) {
            if (j > 0) {
              query.append(",");
            }
            query.append("?");
            list.add(values[j]);
          }
          query.append(")");
        }
        query.append(")");
      }
      params[i] = new SearchParameter.Single("1", "1");
    }
  }
}
