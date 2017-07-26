/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ReportStyle
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generically represents a style which is to be applied
 * to an HTML or enscript-enabled concept report.  Typically this
 * kind of style is used to highlight some section of the report
 * where lines match a particular regular expression.  The actual
 * style can consist of a background shading, a color, or bold, underlined,
 * or italicized text.
 *
 * @see gov.nih.nlm.meme.client.ReportsClient
 * @see gov.nih.nlm.meme.server.ReportsGenerator
 *
 * @author MEME Group
 */

public interface ReportStyle {

  /**
   * Returns the regular expression.
   * @return the regular expression
   */
  public String getRegexp();

  /**
   * Sets the regular expression.
   * @param regexp the the regular expression
   */
  public void setRegexp(String regexp);

  /**
   * Returns the report section names.
   * @return the report section names
   */
  public String[] getSections();

  /**
   * Adds the specified section names.
   * @param sections the section names to add
   */
  public void setSections(String[] sections);

  /**
   * Returns the color.
   * @return the color
   */
  public String getColor();

  /**
   * Sets the color.
   * @param color the color
   */
  public void setColor(String color);

  /**
   * Returns the shade value.
   * @return the shade value
   */
  public double getShade();

  /**
   * Sets the shade value.
   * @param shade the shade value
   */
  public void setShade(double shade);

  /**
   * Indicates whether or not this style includes <b>bold</b> text.
   * @return <code>true</code> if bold, <code>false</code>
   * otherwise
   */
  public boolean isBold();

  /**
   * Sets the "bold" flag.
   * @param bold the "bold" flag value
   */
  public void setBold(boolean bold);

  /**
   * Indicates whether or not this sytle includes <u>underline</u> text.
   * @return <code>true</code> if underline, <code>false</code>
   * otherwise
   */
  public boolean isUnderline();

  /**
   * Sets the "underline" flag.
   * @param underline the value of the "underline" flag
   */
  public void setUnderline(boolean underline);

  /**
   * Indicates whether or not this style includes <i>italic</i> text.
   * @return <code>true</code> if italics, <code>false</code>
   * otherwise
   */
  public boolean isItalics();

  /**
   * Sets the "italics" flag.
   * @param italics the "italics" flag
   */
  public void setItalics(boolean italics);

  /**
   * Returns the content type.
   * @return the content type
   */
  public String getContentType();

  /**
   * Sets the content type.
   * @param content_type the content type
   */
  public void setContentType(String content_type);

  /**
   * Returns the {@link Parameter} for this style.
   * @param number the parameter index
   * @return the {@link Parameter} for this style
   */
  public Parameter getParameter(int number);

  /**
   * Returns formatting start tags based on lines of the report.
   *
   * When processing a report, this method is called
   * before each line is written out to apply style
   * tags to the line.
   * @param line line to be written to apply tags.
   * @return the start tag.
   */
  public String getStartTag(String line);

  /**
   * Returns the formatting end tags.  This method only returns
   * a value if the start tag method returned a value.
   * @return the end tag.
   */
  public String getEndTag();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link ReportStyle} interface.
   */
  public class Default implements ReportStyle {

    //
    // Constants
    //

    /**
     * Represents the section that starts the report.
     */
    public final static String START_SECTION = "As of";

    /**
     * Represents the section that ends the report.
     */
    public final static String END_SECTION = "MEME Server Version";

    /**
     * Represents the section that names the concept_id.
     */
    public final static String CN_SECTION = "CN";

    /**
     * Represents the section that lists CUIs.
     */
    public final static String CUI_SECTION = "CUI";

    /**
     * Represents the section that lists STYs.
     */
    public final static String STY_SECTION = "STY";

    /**
     * Represents the section that lists definitions.
     */
    public final static String DEF_SECTION = "DEF";

    /**
     * Represents the section that lists atoms.
     */
    public final static String ATOMS_SECTION = "ATOMS";

    /**
     * Represents the section that lists lecgacy codes.
     */
    public final static String LEGACY_CODE_SECTION = "LEGACY CODE";

    /**
     * Represents the section that lists contexts.
     */
    public final static String CONTEXTS_SECTION = "CONTEXTS";

    /**
     * Represents the section that lists demotions.
     */
    public final static String DEMOTED_RELATED_CONCEPT_SECTION =
        "DEMOTED RELATED CONCEPT";

    /**
     * Represents the section that lists demotions.
     */
    public final static String LEXICAL_RELATIONSHIPS_SECTION =
        "LEXICAL RELATIONSHIPS";

    /**
     * Represents the section that lists reviewed relationships.
     */
    public final static String REVIEWED_RELATED_CONCEPT_SECTION =
        "REVIEWED RELATED CONCEPT";

    /**
     * Represents the section that lists reviewed relationships.
     */
    public final static String NEEDS_REVIEW_RELATED_CONCEPT_SECTION =
        "NEEDS REVIEW RELATED CONCEPT";

    /**
     * Represents the section that lists all relationships.
     */
    public final static String ALL_RELATIONSHIP_SECTION =
        "ALL RELATIONSHIP";

    /**
     * Represents the section that lists context relationships.
     */
    public final static String CONTEXT_RELATIONSHIP_SECTION =
        "CONTEXT RELATIONSHIP";

    /**
     * Represents the section that lists atom-specific relationships.
     */
    public final static String ATOM_RELATIONSHIP_SECTION =
        "ATOM RELATIONSHIP";

    /**
     * Represents the section that lists atom-specific context relationships.
     */
    public final static String ATOM_CONTEXT_RELATIONSHIP_SECTION =
        "ATOM CONTEXT RELATIONSHIP";

    /**
     * Used to apply styles to all sections of the report.
     */
    public final static String ALL_SECTIONS = "";

    /**
     * This is the list of all sections.  Whenever one of
     * these headers is encountered, the previous section ends.
     */
    public final static String[] ALL_SECTIONS_LIST =
        new String[] {
        START_SECTION,
        CN_SECTION,
        CUI_SECTION,
        STY_SECTION,
        DEF_SECTION,
        ATOMS_SECTION,
        CONTEXTS_SECTION,
        LEXICAL_RELATIONSHIPS_SECTION,
        LEGACY_CODE_SECTION,
        DEMOTED_RELATED_CONCEPT_SECTION,
        REVIEWED_RELATED_CONCEPT_SECTION,
        NEEDS_REVIEW_RELATED_CONCEPT_SECTION,
        ALL_RELATIONSHIP_SECTION,
        ATOM_RELATIONSHIP_SECTION,
        ATOM_CONTEXT_RELATIONSHIP_SECTION,
        CONTEXT_RELATIONSHIP_SECTION};

    //
    // Fields
    //

    private String regexp = ".*";
    private Pattern pattern = Pattern.compile(".*");

    private String[] sections = null;
    private String color = null;
    private double shade = 0.0;
    private boolean bold = false;
    private boolean underline = false;
    private boolean italics = false;

    private String content_type;

    // this flag is true if we are in a section in the sections array
    private boolean in_section = false;

    // this flag is true if getStartTag returned something other than ""
    private boolean inside_container = false;

    //
    // Constructors
    //

    /**
     * Instantiates a {@link ReportStyle.Default} from the
     * specified {@link Parameter}.
     * @param p the {@link Parameter}
     */
    public Default(Parameter p) {
      // This has a value like this:
      // regexp:sections:color:shade:bold:underline:italics
      String[] fields = FieldedStringTokenizer.split( (String) p.getValue(),
          ":");
      regexp = ( (fields[0].equals("")) ? null : fields[0]);
      pattern = Pattern.compile(regexp, Pattern.DOTALL);

      sections = FieldedStringTokenizer.split(fields[1], ",");
      color = ( (fields[2].equals("")) ? null : fields[2]);
      try {
        shade = Double.parseDouble(fields[3]);
      } catch (Exception e) {
        shade = 0.0;
      }
      bold = Boolean.valueOf(fields[4]).booleanValue();
      underline = Boolean.valueOf(fields[5]).booleanValue();
      italics = Boolean.valueOf(fields[6]).booleanValue();
    }

    /**
     * Instantiates an empty {@link ReportStyle.Default}.
     */
    public Default() {}

    //
    // Implementation of Object class
    //

    /**
     * Returns a {@link String} representation.
     * @return a {@link String} representation.
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      if (regexp != null) {
        sb.append(regexp);
      }
      sb.append(":");
      if (sections != null) {
        for (int i = 0; i < sections.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(sections[i]);
        }
      }
      sb.append(":");
      if (color != null) {
        sb.append(color);
      }
      sb.append(":");
      sb.append(shade);
      sb.append(":");
      sb.append(bold);
      sb.append(":");
      sb.append(underline);
      sb.append(":");
      sb.append(italics);
      return sb.toString();
    }

    /**
     * Returns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    /**
     * Equality function based on string representations.
     * @param object objecgt to compare to
     * @return <code>true</code> if equal,
     * <code>false</code> otherwise
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof ReportStyle))) {
        return false;
      }
      return toString().equals(object.toString());
    }

    //
    // Accessor Methods
    //

    /**
     * Implements {@link ReportStyle#getRegexp()}.
     * @return regular expression
     */
    public String getRegexp() {
      return regexp;
    }

    /**
     * Implements {@link ReportStyle#setRegexp(String)}.
     * @param regexp the regular expression
     */
    public void setRegexp(String regexp) {
      this.regexp = regexp;
      pattern = Pattern.compile(regexp, Pattern.DOTALL);
    }

    /**
     * Implements {@link ReportStyle#getSections()}.
     * @return all sections
     */
    public String[] getSections() {
      return sections;
    }

    /**
     * Implements {@link ReportStyle#setSections(String[])}.
     * @param sections all sections
     */
    public void setSections(String[] sections) {
      this.sections = sections;

    }

    /**
     * Implements {@link ReportStyle#getColor()}.
     * @return the color
     */
    public String getColor() {
      return color;
    }

    /**
     * Implements {@link ReportStyle#setColor(String)}.
     * @param color the color
     */
    public void setColor(String color) {
      this.color = color;
    }

    /**
     * Implements {@link ReportStyle#getShade()}.
     * @return the shade value
     */
    public double getShade() {
      return shade;
    }

    /**
     * Implements {@link ReportStyle#setShade(double)}.
     * @param shade the shade value
     */
    public void setShade(double shade) {
      this.shade = shade;
    }

    /**
     * Implements {@link ReportStyle#isBold()}.
     * @return <code>true</code> if bold is set; <code>false</code>
     * otherwise
     */
    public boolean isBold() {
      return bold;
    }

    /**
     * Implements {@link ReportStyle#setBold(boolean)}.
     * @param bold the "bold" flag value
     */
    public void setBold(boolean bold) {
      this.bold = bold;
    }

    /**
     * Implements {@link ReportStyle#isUnderline()}.
     * @return <code>true</code> if underline is set; <code>false</code>
     * otherwise
     */
    public boolean isUnderline() {
      return underline;
    }

    /**
     * Implements {@link ReportStyle#setUnderline(boolean)}.
     * @param underline the "underline" flag value
     */
    public void setUnderline(boolean underline) {
      this.underline = underline;
    }

    /**
     * Implements {@link ReportStyle#isItalics()}.
     * @return <code>true</code> if italics is set; <code>false</code>
     * otherwise
     */
    public boolean isItalics() {
      return italics;
    }

    /**
     * Implements {@link ReportStyle#setItalics(boolean)}.
     * @param italics the "italics" flag value
     */
    public void setItalics(boolean italics) {
      this.italics = italics;
    }

    /**
     * Implements {@link ReportStyle#getContentType()}.
     * @return the content type
     */
    public String getContentType() {
      return content_type;
    }

    /**
     * Implements {@link ReportStyle#setContentType(String)}.
     * @param content_type the content type
     */
    public void setContentType(String content_type) {
      this.content_type = content_type;
    }

    //
    // Methods
    //

    /**
     * Implements {@link ReportStyle#getParameter(int)}.
     * @param number the parameter index
     * @return the {@link Parameter} for this {@link ReportStyle}
     */
    public Parameter getParameter(int number) {
      String name = "style" + number;
      return new Parameter.Default(name, toString());
    }

    /**
     * Implements {@link ReportStyle#getStartTag(String)}.
     * @param line the line to be written to apply tags
     * @return the start tag
     */
    public String getStartTag(String line) {
      StringBuffer tag = new StringBuffer(100);

      // First, we must check to see if the line
      // is the beginning of a section we care about
      if (sections != null && sections.length > 0) {

        // If we are in a section it ends with the next
        // section header
        if (in_section) {
          in_section = !sectionCheck(line, ALL_SECTIONS_LIST);

          // If we are not in a section, matching header puts us in one
        }
        if (!in_section) {
          in_section = sectionCheck(line, sections);

        }
      }
      // if we are in a section we care about
      // or if we are not screening for sections
      if (in_section || sections.length == 0) {

        // Now, see if the current line
        // matches the regex pattern.
        Matcher m = pattern.matcher(line);
        MEMEToolkit.trace("Pattern=" + regexp);
        MEMEToolkit.trace("Line=" + line);
        if (m.matches()) {
          // Now apply style based on content-type
          if (content_type.equals("text/html")) {

            if (bold) {
              tag.append("<b>");
            }
            if (underline) {
              tag.append("<u>");
            }
            if (italics) {
              tag.append("<i>");
            }
            if (color != null) {
              tag.append("<span style=\"color:");
              tag.append(color);
              tag.append(";\">");
            }
            if (shade != 0.0) {
              // scale to 256
              // convert to hex
              String hex = Integer.toHexString( (int) (shade * 256));
              tag.append("<span style=\"background-color:");
              tag.append("#").append(hex).append(hex).append(hex).append(";\">");
            }

            inside_container = true;
            return tag.toString();

          } else if (content_type.equals("text/enscript")) {

            MEMEToolkit.trace("getStartTag()...");

            if (bold) {
              tag.append("&#x0;font{Courier-Bold07}");
              // underline does nothing
            }
            if (italics) {
              tag.append("&#x0;font{Courier-Oblique07}");
            }
            if (color != null) {
              // here we have to take
              // something like #112233
              // and produce red, green, blue values
              double red =
                  Math.round( (Integer.parseInt(color.substring(1, 3), 16) /
                               2.55)) / 100;
              double green =
                  Math.round( (Integer.parseInt(color.substring(3, 5), 16) /
                               2.55)) / 100;
              double blue =
                  Math.round( (Integer.parseInt(color.substring(5), 16) / 2.55)) /
                  100;
              tag.append("&#x0;color{");
              tag.append(red).append(" ");
              tag.append(green).append(" ");
              tag.append(blue).append("}");
            }
            if (shade != 0.0) {
              tag.append("&#x0;shade{").append(shade).append("}");
            }

            inside_container = true;
            return tag.toString();
          }
        }
      }

      // If we make it this far, we did not return a start tag.
      inside_container = false;
      return "";
    }

    /**
     * Implements {@link ReportStyle#getEndTag()}.
     * @return the end tag
     */
    public String getEndTag() {
      StringBuffer tag = new StringBuffer(100);
      // if a start tag was produced, produce the corresponding end tag
      if (inside_container) {
        // Now apply style based on content-type
        if (content_type.equals("text/html")) {

          if (shade != 0.0) {
            tag.append("</span>");
          }
          if (color != null) {
            tag.append("</span>");
          }
          if (italics) {
            tag.append("</i>");
          }
          if (underline) {
            tag.append("</u>");
          }
          if (bold) {
            tag.append("</b>");

          }
          MEMEToolkit.trace("getEndTag()...");
        } else if (content_type.equals("text/enscript")) {
          if (shade != 0.0) {
            tag.append("&#x0;shade{1.0}");
          }
          if (color != null) {
            tag.append("&#x0;color{0.0 0.0 0.0}");
          }
          if (italics) {
            tag.append("&#x0;font{default}");
            // underline does nothing
          }
          if (bold) {
            tag.append("&#x0;font{default}");
          }
        }
        return tag.toString();
      }
      return "";
    }

    /**
     * Helper method to determine if a line from the report
     * is in one of the sections named in the sections param.
     * @param line the line from the report.
     * @param sections list of section.
     * @return A <code>boolean</code> representation of section
     * check value.
     */
    private boolean sectionCheck(String line, String[] sections) {
      for (int i = 0; i < sections.length; i++) {
        if (line.startsWith(sections[i])) {
          return true;
        }
      }
      return false;
    }

  }
}
