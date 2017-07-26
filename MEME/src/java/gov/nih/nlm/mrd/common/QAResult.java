/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAResult
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Generically represents a result in a QA set.
 *
 * @author  MRD Group
 */
public class QAResult {

  // Fields
  private String name;
  private String value;
  private ArrayList reasons;
  private long count;
  private int code;

  /**
   * Instantiates a {@link QAResult}.
   */
  public QAResult() {
    reasons = new ArrayList();
  }

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the name.
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value.
   * @param value the value.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns the value.
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the values.
   * @return the values
   */
  public String[] getValues() {
    return FieldedStringTokenizer.split(getValue(), "~");
  }

  /**
   * Sets the count.
   * @param count the count.
   */
  public void setCount(long count) {
    this.count = count;
  }

  /**
   * Assigns source type flags
   * @param source_type the source type map
   */
  public void assignRefersToCode(Map source_type) {
    for (Iterator iter = source_type.keySet().iterator(); iter.hasNext(); ) {
      Integer type = (Integer) iter.next();
      Source[] sources = (Source[]) source_type.get(type);
      for (int i = 0; i < sources.length; i++) {
        if (getValue().indexOf(sources[i].getStrippedSourceAbbreviation()) !=
            -1) {
          code = code | type.intValue();
        }
      }
    }
  }

  /**
   * Returns the count.
   * @return the count
   */
  public long getCount() {
    return count;
  }

  /**
   * Returns the {@link QAReason}s.
   * @return the {@link QAReason}s
   */
  public QAReason[] getReasons() {
    return (QAReason[]) reasons.toArray(new QAReason[] {});
  }

  /**
   * Returns the {@link QAReason} values
   * @return the {@link QAReason} values
   */
  public String getReasonsAsString() {
    StringBuffer sb = new StringBuffer();
    QAReason[] reasons = getReasons();
    Arrays.sort(reasons);
    for (int i = 0; i < reasons.length; i++) {
      sb.append(reasons[i].getReason());
    }
    return sb.toString();
  }

  /**
   * Adds the specified {@link QAReason}.
   * @param reason the {@link QAReason} to add
   */
  public void addReason(QAReason reason) {
    reasons.add(reason);
  }

  /**
   * Sets the {@link QAReason}s.
   * @param reasons the {@link QAReason}s
   */
  public void setReasons(QAReason[] reasons) {
    this.reasons.clear();
    for (int i = 0; i < reasons.length; i++) {
      this.reasons.add(reasons[i]);
    }
  }

  /**
   * Indicates whether or not this result is associated with an update source.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean refersToUpdateSource() {
    return ( (code & SourceType.UPDATE) == SourceType.UPDATE);
  }

  /**
   * Indicates whether or not this result is associated with an new source.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean refersToNewSource() {
    return ( (code & SourceType.NEW) == SourceType.NEW);
  }

  /**
   * Indicates whether or not this result is associated with an obsolete source.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean refersToObsoleteSource() {
    return ( (code & SourceType.OBSOLETE) == SourceType.OBSOLETE);
  }

}