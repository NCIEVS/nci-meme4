/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SearchParameter
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.HashMap;

/**
 * Generically represents a search criterion.  Implementations define
 * whether they are single or multiple value searches and whether they
 * are approximate, exact, or range-based searches.  Also supported
 * is a generic key/value attribute mechanism for supplying additional
 * needed information not otherwise supported.   In practice, this
 * class is used to build SQL queries to search for and build
 * objecst from the database.
 * The various <i>Finder</i>
 *
 * @author MEME Group
 */

public interface SearchParameter {

  /**
   * Returns the name of the search parameter.  Typically
   * this is the field to be searched.
   * @return the name of the search parameter
   */
  public String getName();

  /**
   * Returns <code>true</code> if this search parameter is
   * used to find things given a range of identifiers.
   * If this returns <code>true</code>, then {@link #getStartId()}
   * and {@link #getEndId()} should return non-null values.
   * @return <code>true</code> if this is a range search; <code>false</code>
   * otherwise
   */
  public boolean isRangeSearch();

  /**
   * Returns the starting {@link Identifier} in a range search.
   * @return the starting {@link Identifier} in a range search
   */
  public Identifier getStartId();

  /**
   * Returns the ending {@link Identifier} in a range search.
   * @return the ending {@link Identifier} in a range search
   */
  public Identifier getEndId();

  /**
   * Returns <code>true</codE> if this search parameter is
   * used to find things that have a certain identifier.
   * If this returns <code>true</code>, then {@link #getValue()}
   * should return a non-null value.
   * @return <code>true</code> if this is a singel value search;
   * <code>false</code> otherwise
   */
  public boolean isSingleValueSearch();

  /**
   * Returns <code>true</code> if this search parameter is
   * used to find things that have an identifier from a list
   * of identifiers..
   * @return If this returns <code>true</code>, then {@link #getValues()}
   * should return a non-null value
   */
  public boolean isMultipleValueSearch();

  /**
   * Returns <code>true</code> if this search parameter is
   * used to find things that approximately have a certain
   * identifier. For example, an approxmiate word search.
   * @return If this returns <code>true</code>, then {@link #getValue()}
   * should return a non-null value
   */
  public boolean isApproximateValueSearch();

  /**
   * Returns the {@link Identifier} used by a single value or approximiate
   * search.
   * @return the {@link Identifier}
   */
  public Identifier getValue();

  /**
   * Returns the {@link Identifier} used by a single value or approximiate
   * search.
   * @return An {@link Identifier}
   */
  public Identifier[] getValues();

  /**
   * Indicates whether or not this parameter is negated.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isNegated();

  /**
   * Sets the flag indicating whether or not this parameter is negated.
   * @param is_negated the "is negated" flag value
   */
  public void setIsNegated(boolean is_negated);

  /**
   * Returns an attribute associated with a key.
   * @param key the attribute key
   * @return an attribute associated with a key
   */
  public Object getAttribute(String key);

  /**
   * Sets an attribute associated with a key.
   * @param key the key
   * @param value the value
   */
  public void setAttribute(String key, Object value);

  /**
   * This class is a default implementation of {@link SearchParameter}.
   */
  public abstract class Default implements SearchParameter {

    //
    // Fields
    //
    protected Identifier start_range = null;
    protected Identifier end_range = null;
    protected Identifier value = null;
    protected String name = null;
    protected HashMap attributes = new HashMap();
    protected boolean is_negated = false;

    //
    // Methods
    //

    /**
     * Returns the name of the search parameter.  Typically
     * this is the field to be searched.
     * @return the name of the search parameter.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     */
    public boolean isRangeSearch() {
      return false;
    }

    /**
     * Returns the start identifier.
     * @return the start identifier
     */
    public Identifier getStartId() {
      return start_range;
    }

    /**
     * Returns the end identifier.
     * @return the end identifier
     */
    public Identifier getEndId() {
      return end_range;
    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     */
    public boolean isSingleValueSearch() {
      return false;
    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     */
    public boolean isMultipleValueSearch() {
      return false;
    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     */
    public boolean isApproximateValueSearch() {
      return false;
    }

    /**
     * Indicates whether or not this parameter is negated.
     * @return A <code>boolean</code>
     */
    public boolean isNegated() {
      return is_negated;
    };

    /**
     * Sets the flag indicating whether or not this parameter is negated.
     * @param is_negated A <code>boolean</code>
     */
    public void setIsNegated(boolean is_negated) {
      this.is_negated = is_negated;
    }

    /**
     * Returns the search value.
     * @return the search value
     */
    public Identifier getValue() {
      return value;
    }

    /**
     * Returns multiple search values.
     * @return multiple search values
     */
    public Identifier[] getValues() {
      return new Identifier[0];
    }

    /**
     * Returns the attribute associated with the key.
     * @param key the key
     * @return the attribute associated with the key
     */
    public Object getAttribute(String key) {
      return attributes.get(key);
    }

    /**
     * Sets an attribute associated with a key.
     * @param key the key
     * @param value the value
     */
    public void setAttribute(String key, Object value) {
      attributes.put(key, value);
    }
  }

  /**
   * This class is a default implementation of {@link SearchParameter}
   * used for range searches.
   */
  public class Range extends Default {

    //
    // Constructors
    //

    /**
     * Instantiates a {@link SearchParameter} for
     * a range search.
     * @param name the name of the parameter
     * @param start The start identifier
     * @param end The end identifier
     */
    public Range(String name, Identifier start, Identifier end) {
      this.name = name;
      start_range = start;
      end_range = end;
    }

    /**
     * Returns <code>true</code>.
     * @return <code>true</code>
     */
    public boolean isRangeSearch() {
      return true;
    }

  }

  /**
   * This class is a default implementation of {@link SearchParameter}
   * used for single value searches
   */
  public class Single extends Default {

    //
    // Constructors
    //

    /**
     * Constructs a {@link SearchParameter} for
     * a single value search.
     * @param name the name of the parameter
     * @param value the value to search for
     */
    public Single(String name, Identifier value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Constructs a {@link SearchParameter} for
     * a single value search.
     * @param name the name of the parameter
     * @param value the value to search for
     */
    public Single(String name, String value) {
      this.name = name;
      if (value != null) {
        this.value = new Identifier.Default(value);
      } else {
        this.value = null;
      }
    }

    /**
     * Returns <code>true</code>.
     * @return <code>true</code>
     */
    public boolean isSingleValueSearch() {
      return true;
    }

  }

  /**
   * This class is a default implementation of {@link SearchParameter}.
   * used for single value searches
   */
  public class Approximate extends Default {

    //
    // Constructors
    //

    /**
     * Constructs a {@link SearchParameter} for
     * a approximate value search.
     * @param name the name of the parameter
     * @param value the value to search for
     */
    public Approximate(String name, Identifier value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Constructs a {@link SearchParameter} for
     * a approximate value search.
     * @param name the name of the parameter
     * @param value the value to search for
     */
    public Approximate(String name, String value) {
      this.name = name;
      this.value = new Identifier.Default(value);
    }

    /**
     * Returns <code>true</code>.
     * @return <code>true</code>
     */
    public boolean isApproximateValueSearch() {
      return true;
    }

  }

  /**
   * This class is a default implementation of {@link SearchParameter}
   * used for multiple value searches.
   */
  public class Multiple extends Default {

    //
    // Fields
    //
    private Identifier[] values = null;

    //
    // Constructors
    //

    /**
     * Instantiates a {@link SearchParameter} for
     * a multiple value search.
     * @param name the name of the parameter
     * @param values the values to search for
     */
    public Multiple(String name, Identifier[] values) {
      this.name = name;
      this.values = values;
    }

    /**
     * Instantiates a {@link SearchParameter} for
     * a multiple value search.
     * @param name the name of the parameter
     * @param values the values to search for
     */
    public Multiple(String name, String[] values) {
      this.name = name;
      this.values = new Identifier[values.length];
      for (int i = 0; i < values.length; i++) {
        this.values[i] = new Identifier.Default(values[i]);
      }
    }

    /**
     * Returns <code>true</code>.
     * @return <code>true</code>
     */
    public boolean isMultipleValueSearch() {
      return true;
    }

    /**
     * Returns the list of values.
     * @return the list of values
     */
    public Identifier[] getValues() {
      return values;
    }

  }

}
