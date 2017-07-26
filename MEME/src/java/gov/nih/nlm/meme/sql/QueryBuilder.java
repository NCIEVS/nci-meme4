/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  QueryBuilder
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.SearchParameter;

import java.util.List;

/**
 * Builds query conditions from a search parameter list.
 *
 * @author MEME Group
 */
public class QueryBuilder {

  /**
   * Builds query.
   * @param query the {@link StringBuffer}
   * @param params An array of object {@link SearchParameter}
   * @param list the {@link List}
   */
  public void build(StringBuffer query, SearchParameter[] params, List list) {

    for (int i = 0; i < params.length; i++) {
      // Extract parameters
      String name = params[i].getName();
      Identifier value = params[i].getValue();
      Identifier start = params[i].getStartId();
      Identifier end = params[i].getEndId();
      Identifier[] values = params[i].getValues();

      // Ignore if name=1
      if (name.equals("1")) {
        continue;
      }

      // Single value means equality test should be used
      if (params[i].isSingleValueSearch()) {
        query.append(" AND ").append(name).append(" ");
        if (value != null) {
          if (params[i].isNegated()) {
            query.append("!");
          }
          query.append("= ?");
          list.add(value);
        } else {
          query.append("IS ");
          if (params[i].isNegated()) {
            query.append("NOT ");
          }
          query.append("NULL");
        }
      }

      // Range search means a between test should be used
      // It is possible that start/end identifiers are
      // null, in which case it is just a < or > search.
      else if (params[i].isRangeSearch()) {
        if (params[i].isNegated()) {
          query.append(" NOT(");
        }
        if (start != null) {
          query.append(" AND ").append(name).append(" >=?");
          list.add(start);
        }
        if (end != null) {
          query.append(" AND ").append(name).append(" <=?");
          list.add(end);
        }
        if (params[i].isNegated()) {
          query.append(")");
        }
      }

      // Approximate search means a like test
      // It is not clear where the % should bo
      // For now   name = 'value%' makes the most sense.
      else if (params[i].isApproximateValueSearch()) {
        query.append(" AND ").append(name);
        if (params[i].isNegated()) {
          query.append(" NOT ");
          //query.append(" LIKE '").append(value).append("'");
        }
        query.append(" LIKE ?");
        list.add(value);
      }

      // Multiple value search means a set membership test.
      else if (params[i].isMultipleValueSearch()) {
        query.append(" AND ").append(name);
        if (params[i].isNegated()) {
          query.append(" NOT");
        }
        query.append(" IN (");
        for (int j = 0; j < values.length; j++) {
          if (j > 0) {
            query.append(",");
          }
          query.append("?");
          list.add(values[j]);
        }
        query.append(")");
      }
    }
  }

}
