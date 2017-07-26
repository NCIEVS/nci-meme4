/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ContextFormatter
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.Arrays;

/**
     * Implements a function for formatting the microsyntax CONTEXT attribute values
 * into a pretty-printed form.
 *
 * @author MEME Group
 */

public class ContextFormatter {

  //
  // Methods
  //

  /**
   * Returns the formatted context value.
   * @param context the context in micro-syntax form
   * @param termgroup the termgroup value associated with the concept
   * @param code the code value associated with the context
   * @return the formatted context value
   */
  public static String formatContext(String context, String termgroup,
                                     String code) {

    FieldedStringTokenizer st = new FieldedStringTokenizer(context, "\t");
    String[] parts = new String[st.countTokens()];
    String id, ancestor, self, child, sibling = null;

    // Extracts context into parts
    int parts_ctr = 0;
    while (st.hasMoreTokens()) {
      parts[parts_ctr] = st.nextToken().toString();
      parts_ctr++;
    }

    // Assigns context parts into it's particular data holder
    if (parts.length == 6) {
      id = parts[0];
      ancestor = parts[2];
      self = parts[3];
      child = parts[4];
      sibling = parts[5] + '~' + self;

    } else if (parts.length == 5) {
      id = parts[0];
      ancestor = parts[2];
      self = parts[3];
      child = parts[4];

    } else if (parts.length == 4 || parts.length == 3) {
      // Current context separator is ":"
      FieldedStringTokenizer st2 = new FieldedStringTokenizer(parts[0], ":");
      String[] subparts = new String[st2.countTokens()];

      // Extracts current context subparts
      int subparts_ctr = 0;
      while (st2.hasMoreTokens()) {
        subparts[subparts_ctr] = st2.nextToken().toString();
        subparts_ctr++;
      }

      // Assigns context parts and subparts into it's particular data holder
      id = subparts[0];
      ancestor = subparts[subparts_ctr - 1];
      self = parts[1];
      child = parts[2];

    } else {
      return "Improperly formatted context: " + context;
    }

    // Write termgroup, id, code
    StringBuffer sb = new StringBuffer(5000);
    sb.append("   ");
    sb.append(termgroup);
    sb.append(id);
    sb.append("/");
    sb.append(code);
    sb.append("\n");

    // Extracts ancestors
    int indent_ctr = 0;
    st = new FieldedStringTokenizer(ancestor, "~");
    while (st.hasMoreTokens()) {
      sb.append("   ");
      for (int i = 0; i < indent_ctr; i++) {
        sb.append("  ");
      }
      indent_ctr++;
      // Write ancestor
      sb.append(st.nextToken()).toString();
      sb.append("\n");
    }

    indent_ctr--;
    if (sibling == null) {
      sb.append("   ");
      for (int i = 0; i < indent_ctr; i++) {
        sb.append("  ");
      }
      // Writes self
      sb.append(" <");
      sb.append(self);
      sb.append(">");
      sb.append("\n");
      if (child != null) {
        // Extracts and writes child
        FieldedStringTokenizer st_child = new FieldedStringTokenizer(child, "~");
        while (st_child.hasMoreTokens()) {
          sb.append("   ");
          for (int i = 0; i < indent_ctr; i++) {
            sb.append("  ");
          }
          sb.append("    ");
          String current_child = st_child.nextToken().toString();
          if (current_child.endsWith("^")) {
            current_child = current_child.substring(0,
                current_child.length() - 1) + " +";
          }
          sb.append(current_child);
          sb.append("\n");
        }
      }
      return sb.toString();
    }

    // Store sibling into temporary array for the purpose of sorting
    int sib_ctr = 0;
    st = new FieldedStringTokenizer(sibling, "~");
    String[] sib_array = new String[st.countTokens()];
    while (st.hasMoreTokens()) {
      sib_array[sib_ctr] = st.nextToken().toString();
      sib_ctr++;
    }

    // Sort sibling in ascending order
    Arrays.sort(sib_array, String.CASE_INSENSITIVE_ORDER);

    // Put sorted array into it's original string data holder
    sibling = null;
    for (int i = 0; i < sib_array.length; i++) {
      if (sibling != null) {
        sibling = sibling + '~' + sib_array[i];
      } else {
        sibling = sib_array[i];
      }
    }

    // Extracts and writes sibling
    String current_sibling = null;
    st = new FieldedStringTokenizer(sibling, "~");

    while (st.hasMoreTokens()) {
      sb.append("   ");
      for (int i = 0; i < indent_ctr; i++) {
        sb.append("  ");
      }
      current_sibling = st.nextToken().toString();
      if (current_sibling.equals(self)) {
        sb.append(" <");
      } else {
        sb.append("  ");

      }
      if (current_sibling.endsWith("^")) {
        current_sibling = current_sibling.substring(0,
            current_sibling.length() - 1) + " +";
      }
      sb.append(current_sibling);
      if (current_sibling.equals(self)) {
        sb.append(">");
        sb.append("\n");
        if (child != null) {
          // Extracts and writes child
          FieldedStringTokenizer st_child = new FieldedStringTokenizer(child,
              "~");
          while (st_child.hasMoreTokens()) {
            sb.append("   ");
            for (int i = 0; i < indent_ctr; i++) {
              sb.append("  ");
            }
            sb.append("    ");
            String current_child = st_child.nextToken().toString();
            if (current_child.endsWith("^")) {
              current_child = current_child.substring(0,
                  current_child.length() - 1) + " +";
            }
            sb.append(current_child);
            sb.append("\n");
          }
        }
      } else {
        sb.append("\n");
      }
    }

    return sb.toString();

  }
}
