/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  SourceType
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.common;

/**
 * Represents a type of source: UPDATE, NEW, or OBSOLETE.
 * @author TTN, BAC
 */
public interface SourceType {

  public static final int UPDATE = 1;

  public static final int NEW = 2;

  public static final int OBSOLETE = 4;

}