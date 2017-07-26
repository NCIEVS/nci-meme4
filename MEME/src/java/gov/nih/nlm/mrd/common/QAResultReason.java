/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAResultReason
 *
 * Author:  BAC, TTN
 *
 * History:
 *
 *   08/13/2002 4.1.0.: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.Iterator;
import java.util.Map;

public class QAResultReason extends QAReason.Default {

  /**
   * Indicates whether or not this reason applies to the specified
   * {@link QAResultReason}.
   * @param qaresult the qaresult
   * @param macro_map a map of source type macro to list of sources
   * @return <code>true</code> if it does; <code>false</code> otherwise
   */
  public boolean appliesTo(QAResult qaresult, Map macro_map) {
    //
    // Compare names
    //
    if ("=".equals(getNameOperator()) && !getName().equals(qaresult.getName())) {
      return false;
    }

    if ("=~".equals(getNameOperator()) &&
        qaresult.getName().indexOf(getName()) == -1) {
      if (!qaresult.getName().matches(getName())) {
        return false;
      }
    }
    if ("in".equals(getNameOperator())) {
      String[] names = FieldedStringTokenizer.split(getName(), ",");
      boolean found = false;
      for (int i = 0; i < names.length; i++) {
        if (names[i].equals(qaresult.getName())) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }
    if ("in =~".equals(getNameOperator())) {
      String[] names = FieldedStringTokenizer.split(getName(), ",");
      boolean found = false;
      for (int i = 0; i < names.length; i++) {
        if (qaresult.getName().matches(names[i])) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }

    //
    // Compare values
    //
    if ("=".equals(getValueOperator()) && !getValue().equals(qaresult.getValue())) {
      return false;
    }
    if ("in".equals(getValueOperator())) {
      String[] values = FieldedStringTokenizer.split(getValue(), ",");
      boolean found = false;
      for (int i = 0; i < values.length; i++) {
        if (macro_map.containsKey(values[i])) {
          Source[] srcs = (Source[]) macro_map.get(values[i]);
          for (int j = 0; j < srcs.length; j++) {
            if (srcs[j].getStrippedSourceAbbreviation().equals(qaresult.
                getValue())) {
              found = true;
            }
          }
        } else if (values[i].equals(qaresult.getValue())) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }

    if ("=~".equals(getValueOperator()) &&
        qaresult.getValue().indexOf(getValue()) == -1) {
      String value = getValue();
      for (Iterator iter = macro_map.keySet().iterator(); iter.hasNext(); ) {
        String macro = (String) iter.next();
        if (value.indexOf(macro) != -1) {
          StringBuffer list = new StringBuffer("(");
          Source[] srcs = (Source[]) macro_map.get(macro);
          for (int j = 0; j < srcs.length - 1; j++) {
            list.append(srcs[j].getStrippedSourceAbbreviation()).append("|");
          }
          list.append(srcs[srcs.length - 1].getStrippedSourceAbbreviation()).
              append(")");
          value = value.replaceAll(macro, list.toString());
        }
      }
      if (!qaresult.getValue().matches(value)) {
        return false;
      }
    }

    if ("in =~".equals(getValueOperator())) {
      String[] values = FieldedStringTokenizer.split(getValue(), ",");
      boolean found = false;
      for (int i = 0; i < values.length; i++) {
        boolean inmacro = false;
        for (Iterator iter = macro_map.keySet().iterator(); iter.hasNext(); ) {
          String macro = (String) iter.next();
          if (values[i].indexOf(macro) != -1) {
            inmacro = true;
            Source[] srcs = (Source[]) macro_map.get(macro);
            for (int j = 0; j < srcs.length; j++) {
              if (qaresult.getValue().matches(values[i].replaceAll(macro,
                  srcs[j].getStrippedSourceAbbreviation()))) {
                found = true;
              }
            }
          }
        }
        if (!inmacro && qaresult.getValue().matches(values[i])) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }
    //
    // Compare counts
    //
    if ("=".equals(getCountOperator()) && getCount() != qaresult.getCount()) {
      return false;
    }
    if ("<".equals(getCountOperator()) && getCount() < qaresult.getCount()) {
      return false;
    }
    if (">".equals(getCountOperator()) && getCount() > qaresult.getCount()) {
      return false;
    }

    return true;
  }

}
