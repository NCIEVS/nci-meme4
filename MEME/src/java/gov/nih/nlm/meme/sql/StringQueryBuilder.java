/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  StringQueryBuilder
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.SearchParameter;

import java.util.List;

/**
 * Builds query conditions for string related parameters.
 *
 * @author MEME Group
 */
public class StringQueryBuilder extends QueryBuilder {

  /**
   * Builds additional query conditions for string related {@link SearchParameter}s.
   * @param query the {@link StringBuffer} query so far
   * @param params the {@link SearchParameter}s
   * @param list the bind variable {@link List} for the query so far
   */
  public void build(StringBuffer query, SearchParameter[] params, List list) {

    // First, go through and expand string fields
    // to perform _pre lookups.  Also lowercase_string
    // is really a LOWER(string) lookup, replace
    // the parameter.
    for (int i = 0; i < params.length; i++) {
      // Extract parameters
      final String name = params[i].getName();
      final Identifier value = params[i].getValue();

      // Built query according to search condition
      if (params[i].isSingleValueSearch()) {

        // Used for computing substrings
        int len = (value.toString().length() > 9) ? 10 :
            value.toString().length();

        if (name.endsWith("string")) {
          query.append(" AND ").append(name).append("_pre = ?");
          list.add(value.toString().substring(0, len));
        }

        if (name.equals("lowercase_string")) {
          SearchParameter param =
              new SearchParameter.Single("LOWER(String)", value.toString());
          params[i] = param;
        }
      }
    }

    // Use superclass algorithm to complete query.
    super.build(query, params, list);
  }
}
