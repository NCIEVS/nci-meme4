/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  WordQueryBuilder
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.SearchParameter;

import java.util.List;

/**
 * Builds query conditions for word-based index lookups.
 *
 * @author MEME Group
 */
public class WordQueryBuilder extends QueryBuilder {

  /**
   * Builds query conditions for word-based index lookups.
   * @param query the query so far
   * @param params the word-based {@link SearchParameter}s
   * @param list the {@link List} of bind values for the query so far
   */
  public void build(StringBuffer query, SearchParameter[] params, List list) {
    for (int i = 0; i < params.length; i++) {
      // Built query according to search condition
      if (params[i].isMultipleValueSearch() &&
          (params[i].getName().equals("words") ||
           params[i].getName().equals("norm_words"))) {

        // Extract values
        final Identifier[] values = params[i].getValues();

        String table = null;
        String id_field = null;
        String word_field = null;

        // Map table, id_field, and word_field
        if (params[i].getName().equals("words")) {
          table = "word_index";
          id_field = "atom_id";
          word_field = "word";
        } else if (params[i].getName().equals("norm_words")) {
          table = "normwrd";
          id_field = "normwrd_id";
          word_field = "normwrd";
        }
        if (table != null) {
          query.append(" AND ");
          if (params[i].getAttribute("any_or_all").equals("all")) {
            query.append("atom_id IN (SELECT ");

            if (values.length > 1) {
              query.append("a.");
            }
            query.append(id_field).append(" FROM ").append(table);

            if (values.length > 1) {
              query.append(" a");
              for (int j = 1; j < values.length; j++) {
                query.append(", ").append(table).append(" b").append(j);
              }
            }

            query.append(" WHERE ");
            // Select as many words as there are
            if (values.length > 1) {
              query.append("a.");
              for (int j = 1; j < values.length; j++) {
                if (j > 1) {
                  query.append(" AND b").append(j - 1).append(".");
                }
                query.append(id_field).append(" = ").append("b")
                    .append(j).append(".").append(id_field);
              }
              query.append(" AND a.");
            }

            query.append(word_field).append(" = ?");
            list.add(values[0].toString());

            if (values.length > 1) {
              for (int j = 1; j < values.length; j++) {
                query.append(" AND b").append(j).append(".")
                    .append(word_field).append(" = ?");
                list.add(values[j].toString());
              }
            }
            query.append(")");

          } else if (params[i].getAttribute("any_or_all").equals("any")) {
            query.append("atom_id IN ")
                .append("(SELECT ").append(id_field).append(" FROM ")
                .append(table).append(" WHERE ").append(word_field).append(
                " = ?");
            list.add(values[0].toString());

            // Union select as many words as there are
            if (values.length > 1) {
              for (int j = 1; j < values.length; j++) {
                query.append(" UNION SELECT ")
                    .append(id_field).append(" FROM ").append(table)
                    .append(" WHERE ").append(word_field).append(" = ?");
                list.add(values[j].toString());
              }
            }
            query.append(")");
          }
        }
        params[i] = new SearchParameter.Single("1", "1");
      }
    }

    // Use superclass algorithm to complete query.
    super.build(query, params, list);
  }
}
