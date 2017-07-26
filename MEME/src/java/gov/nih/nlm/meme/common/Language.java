/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Language
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents the language of an {@link Atom}.
 *
 * @author MEME Group
 */

public interface Language extends Comparable {

  /**
   * Returns the {@link String} representation.
   * @return the {@link String} representation
   */
  public String toString();

  /**
   * Returns the abbreviation.
   * @return the abbreviation
   */
  public String getAbbreviation();

  /**
   * Sets the ISO abbreviation.
   * @param iso_lat the ISO abbreviation
   */
  public void setISOAbbreviation(String iso_lat);

  /**
   * Returns the ISO abbreviation.
   * @return the ISO abbreviation
   */
  public String getISOAbbreviation();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Language} interface.
   */
  public class Default implements Language {

    //
    // Fields
    //

    private String language = null;
    private String lat = null;
    private String iso_lat = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Language}.
     */
    public Default() {}

    /**
     * Instantiates a default {@link Language} with
     * the specified full language name and abbreviation.
     * @param language the full language name
     * @param lat language abbreviation
     */
    public Default(String language, String lat) {
      this.language = language;
      this.lat = lat;
    }

    //
    // Implementation of Object class
    //

    /**
     * Returns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return (lat + language).hashCode();
    }

    /**
     * Implements an equality function based on all fields.
     * @param object the {@link Language} to compare to
     * @return <code>true</code> if objects are equal,
     *         <code>false</code> otherwise
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof Language))) {
        return false;
      }
      return (lat + language).equals(object.toString());
    }

    //
    // Implementation of Comparable interface
    //

    /**
     * Implements a comparison function based on abbreviation.
     * @param object the {@link Language} to compare to
     * @return an <code>int</code> indicating the relative ordering
     */
    public int compareTo(Object object) {
      if (! (object instanceof Language)) {
        return 0;
      }
      return lat.compareTo( ( (Language) object).getAbbreviation());
    }

    //
    // Methods
    //

    /**
     * Implements {@link Language#toString()}.
     */
    public String toString() {
      return language;
    }

    /**
     * Implements {@link Language#getAbbreviation()}.
     */
    public String getAbbreviation() {
      return lat;
    }

    /**
     * Implements {@link Language#getISOAbbreviation()}.
     */
    public void setISOAbbreviation(String iso_lat) {
      this.iso_lat = iso_lat;
    }

    /**
     * Implements {@link Language#getISOAbbreviation()}.
     */
    public String getISOAbbreviation() {
      return iso_lat;
    }

  }
}
