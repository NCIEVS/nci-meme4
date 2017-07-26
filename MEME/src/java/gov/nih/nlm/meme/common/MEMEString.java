/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MEMEString
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents the
 * strings used for atom names in the MEME database.  Rougly, it
 * represents the data in the <code>string_ui</code> table.
 *
 * Most of the methods in this interface are accessors for the various
 * kinds of information associated with a string, including:<ul>
 *   <li>SUI: The string unique identifier associated with a case-sensitive
 * string</li>
 *   <li>ISUI: The string unique identifier associated with a case-insensitive
 * string</li>
 *   <li>LUI: The lexical class identifier associated with a normalized
 * representation of the string (produced by the lvg luiNorm program)</li>
 *   <li>String: The actual case-sensitive string itself</li>
 *   <li>NormalizedString: The normalized string</li>
 * </ul>
 *  <p>
 *  The other methods are used to assertain various properties of the strings.
 *  The {@link #getBaseString()}, {@link #isBaseString()},
 *  {@link #getBracketNumber()}, and {@link #isBracketString()} methods are
 *  all useful in disecting the multiple-meaning disambiguating strings which
 *  end with a number in brackets.  For example, suppose {@link #getString()}
 *  returned this string:<p> <code>This is a sample string <5></code><p> In
 *  this case, the {@link #getBaseString()} method would return the string
 *  stripped of its bracket number:<p> <code>This is a sample string</code><p>
 *  The {@link #getBracketNumber()} method would return the number in the
 *  brackets, or 5.  The {@link #isBaseString()} would return
 *  <code>false</code> since the string ends with a bracket, and
 *  {@link #isBracketString()} which is the inverse would return
 *  <code>true</code>.
 *
 * @see StringIdentifier
 *  Update: SL: For LVG2009 updated the return value for Default.getBaseString
 * @author MEME Group
 */

public interface MEMEString {

  /**
   * Returns the SUI, the unique identifier for the case-sensitive string.
   * @return the {@link StringIdentifier} SUI
   */
  public StringIdentifier getSUI();

  /**
   * Sets the SUI.
   * @param sui the {@link StringIdentifier} SUI
   */
  public void setSUI(StringIdentifier sui);

  /**
   * Returns the LUI, the unique identifier for the lexical class (the norm
   * string).
   * @return the {@link StringIdentifier} LUI
   */
  public StringIdentifier getLUI();

  /**
   * Sets the LUI.
   * @param lui the {@link StringIdentifier} LUI
   */
  public void setLUI(StringIdentifier lui);

  /**
   * Returns the ISUI, the unique identifier for the case-insensitive string.
   * @return the {@link StringIdentifier} ISUI
   */
  public StringIdentifier getISUI();

  /**
   * Sets the ISUI.
   * @param isui the {@link StringIdentifier} ISUI
   */
  public void setISUI(StringIdentifier isui);

  /**
   * Returns the{@link Language}.
   * @return the {@link Language}
   */
  public Language getLanguage();

  /**
   * Sets the {@link Language}.
   * @param lang the {@link Language}
   */
  public void setLanguage(Language lang);

  /**
   * Returns the string itself.
   * @return the {@link String} itself
   */
  public String toString();

  /**
   * Returns the string itself.
   * @return the {@link String} itself
   */
  public String getString();

  /**
   * Sets the string.
   * @param string the {@link String}
   */
  public void setString(String string);

  /**
   * Returns the normalized {@link String}.
   * @return the normalized {@link String}
   */
  public String getNormalizedString();

  /**
   * Sets the normalized {@link String}.
   * @param norm_string the normalized {@link String}
   */
  public void setNormalizedString(String norm_string);

  /**
   * Returns the string stripped of any bracket number.
       * this is equivalent to (in perl) <code>getString() =~ s/ <\d{1,}>$//;</code>.
   * @return the string stripped of any bracket number
   */
  public String getBaseString();

  /**
   * Indicates whether or not this is base string.
   *  @return <code>true</code> if this is base string, <code>false</code> otherwise
   */
  public boolean isBaseString();

  /**
   * Returns the bracket number.  In a string like "Apple <5>" this
       * would be the <code>int</code> 5. In a string with no brackets, like "Apple",
   * it returns 0.
   * @return the <code>int</code> bracket number
   */
  public int getBracketNumber();

  /**
   * Indicates whether or not this is bracket string.
   * @return <code>true</code> if this is bracket string,
   *         <code>false</code> otherwise
   */
  public boolean isBracketString();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link MEMEString} interface.
   */
  public class Default implements MEMEString {

    //
    // Fields
    //

    private StringIdentifier sui = null;
    private StringIdentifier lui = null;
    private StringIdentifier isui = null;
    private String string = null;
    private String norm_string = null;
    private Language language = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link MEMEString}.
     */
    public Default() {}

    //
    // Implementation of MEMEString interface
    //

    /**
     * Implements {@link MEMEString#getSUI()}.
     */
    public StringIdentifier getSUI() {
      return sui;
    }

    /**
     * Implements {@link MEMEString#setSUI(StringIdentifier)}.
     */
    public void setSUI(StringIdentifier sui) {
      this.sui = sui;
    }

    /**
     * Implements {@link MEMEString#getLUI()}.
     */
    public StringIdentifier getLUI() {
      return lui;
    }

    /**
     * Implements {@link MEMEString#setLUI(StringIdentifier)}.
     */
    public void setLUI(StringIdentifier lui) {
      this.lui = lui;
    }

    /**
     * Implements {@link MEMEString#getISUI()}.
     */
    public StringIdentifier getISUI() {
      return isui;
    }

    /**
     * Implements {@link MEMEString#setISUI(StringIdentifier)}.
     */
    public void setISUI(StringIdentifier isui) {
      this.isui = isui;
    }

    /**
     * Implements {@link MEMEString#getLanguage()}.
     */
    public Language getLanguage() {
      return language;
    }

    /**
     * Implements {@link MEMEString#setLanguage(Language)}.
     */
    public void setLanguage(Language language) {
      this.language = language;
    }

    /**
     * Implements {@link MEMEString#toString()}.
     */
    public String toString() {
      return string;
    }

    /**
     * Implements {@link MEMEString#getString()}.
     */
    public String getString() {
      return string;
    }

    /**
     * Implements {@link MEMEString#setString(String)}.
     */
    public void setString(String string) {
      this.string = string;
    }

    /**
     * Implements {@link MEMEString#getNormalizedString()}.
     */
    public String getNormalizedString() {
      return norm_string;
    }

    /**
     * Implements {@link MEMEString#setNormalizedString(String)}.
     */
    public void setNormalizedString(String norm_string) {
      this.norm_string = norm_string;
    }

    /**
     * Implements {@link MEMEString#getBaseString()}.
     * This default implementation looks through string and strip off the end
     * part if it matches the pattern / <\d>$/.  It should start at the end of
     * the string and look towards the beginning.  It returns the base string
     * if the characters seen do not match the pattern, otherwise it keeps
     * track of the location of the last character before the pattern and
     * return the substring of the string up to that point.
     */
    public String getBaseString() {

      char[] chars = string.toCharArray();
      boolean gt_found = false;
      boolean digit_found = false;

      for (int i = chars.length - 1; i > 0; i--) {
        if (i == chars.length - 1 && chars[i] != '>') {
          return string;
        }
        if (chars[i] == '>') {

          // set gt_found
          gt_found = true;
        } else if (gt_found && chars[i] >= '0' && chars[i] <= '9') {

          // set digit_found
          digit_found = true;
        } else if (gt_found && digit_found && chars[i] == '<') {
          if (i > 0 && chars[i - 1] == ' ') {

            // return the substring of the string
            return string.substring(0, i - 1);
          } else {
            return string;
          }
        } else {

          // matching pattern not found, no bracket string
          return string;
        }
      }
      return string;
    }

    /**
     * Implements {@link MEMEString#isBaseString()}.
     */
    public boolean isBaseString() {
      if (getBracketNumber() == 0) {
        return true;
      }
      return false;
    }

    /**
     * Implements {@link MEMEString#getBracketNumber()}.
     * This default implementation looks through string and identify the
     * / <\d>$/ pattern at the end of the string.  It should start at the end
     * of the string and look towards the beginning.  It returns 0 if the
     * characters seen do not match the pattern, otherwise it keeps track of
     * the location of the last character before the pattern and return only
     * the bracket number.
     */
    public int getBracketNumber() {

      char[] chars = string.toCharArray();
      boolean gt_found = false;
      boolean digit_found = false;
      int multiplier = 1;
      int bracket_num = 0;

      for (int i = chars.length - 1; i > 0; i--) {
        if (i == chars.length - 1 && chars[i] != '>') {

          // last character must be '>'
          return 0;
        }
        if (chars[i] == '>') {

          // set gt_found
          gt_found = true;
        } else if (gt_found && chars[i] >= '0' && chars[i] <= '9') {
          // '>' was found and the current character is a digit
          // add the digit to bracket_num and increase the multiplier
          bracket_num += (chars[i] - '0') * multiplier;
          multiplier *= 10;
          digit_found = true;
        } else if (gt_found && digit_found && chars[i] == '<') {
          // digits followed by '>' found, '<' also found
          if (i > 0 && chars[i - 1] == ' ') {

            // previous character is a ' '
            return bracket_num;
          } else {
            return 0;
          }
        } else {

          // matching pattern not found, no bracket string
          return 0;
        }
      }
      return 0;
    }

    /**
     * Implements {@link MEMEString#isBracketString()}.
     */
    public boolean isBracketString() {
      return!isBaseString();
    }

  }
}
