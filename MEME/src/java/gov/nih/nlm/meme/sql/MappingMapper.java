/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MappingMapper
 *
 *****************************************************************************/

package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MapObject;
import gov.nih.nlm.meme.common.Mapping;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class contains overriden method of
 * {@link AttributeMapper#map(ResultSet, MEMEDataSource)} to implement
 * mappings of "XMAP", "XMAPTO" and "XMAPFROM" attributes.
 *
 * @author MEME Group
 */

public class MappingMapper extends AttributeMapper.Default {

  //
  // Fields
  //

  private Map to = new HashMap();
  private Map from = new HashMap();
  private Source from_source = null;
  private Source to_source = null;
  private int start = -1;
  private int end = -1;
  private int ct = 0;
  private boolean keep_going = false;
  private final static Attribute END_ATTRIBUTE = new Attribute.Default();
  static {
    END_ATTRIBUTE.setName("END OF MAPPINGS");
  }

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MappingMapper}.  By default, this mapper is
   * configured to connect atom and concept objects.
   */
  public MappingMapper() {
    this(true);
  }

  /**
       * Instantiate a {@link MappingMapper} and indicate whether or not this mapper
   * should populate atom and concept fields
   * @param handle_id <code>true</code> if the mapping function should look up
   * the atom and concept, <code>false</code> otherwise
   */
  public MappingMapper(boolean handle_id) {
    super(handle_id);
  }

  //
  // Methods
  //

  /**
   * Sets the start
   * @param start an <code>int</code> represent the start of data type
   */
  public void setStart(int start) {
    this.start = start;
  }

  /**
   * Sets the end
   * @param end an <code>int</code> represent the end of data type
   */
  public void setEnd(int end) {
    this.end = end;
  }

  /**
   * Return <code>true</code> if is restricted; <code>false</code> otherwise.
   * @return restricted value
   */
  public boolean isRestricted() {
    return (start != -1 || end != -1);
  }

  /**
   * Maps the {@link ResultSet} to a {@link Mapping} or {@link Attribute}. Used
   * for map set attributes.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Attribute}
   * @throws SQLException if sql process failed.
   * @throws DataSourceException mapping failed.
   * @throws BadValueException if failed due to invalid values
   */
  public Attribute map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      DataSourceException, BadValueException {

    Attribute attr = null;
    do {
      keep_going = false;
      attr = mapHelper(rs, mds);
      ct++;
    } while (keep_going);
    return attr;

  }

  /**
   * Mapping helper use to map set attributes.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Attribute}
   * @throws SQLException if sql process failed.
   * @throws DataSourceException mapping failed.
   * @throws BadValueException if failed due to invalid values
   */
  private Attribute mapHelper(ResultSet rs, MEMEDataSource mds) throws
      SQLException,
      DataSourceException, BadValueException {

    Attribute attr = null;

    if (rs.getString("ATTRIBUTE_NAME").equals("XMAP") &&
        rs.getString("TOBERELEASED").toLowerCase().equals("y")) {

      // if in restricted mode skip XMAP attributes not in range
      if (isRestricted() && (ct < start || ct >= end)) {
        keep_going = rs.next();
        return END_ATTRIBUTE;
      }

      Mapping mapping = new Mapping.Default();
      populate(rs, mds, mapping);

      String av = rs.getString("ATTRIBUTE_VALUE");
      //
      // (MAPSUBSETID, MAPRANK, FROMID, REL, RELA, TOID
      //  MAPRULE, MAPTYPE, MAPATN, MAPATV, MAPSID, MAPRES)
      //
      String[] values = FieldedStringTokenizer.split(av, "~");

      //
      // Set "map to" object
      //
      if (to.containsKey(values[5])) {
        mapping.setTo( (MapObject) to.get(values[5]));
      } else {
        MapObject mo = new MapObject.Default();
        mo.setMapObjectIdentifier(new Identifier.Default(values[5]));
        if (to_source != null) {
          mo.setMapObjectSource(to_source);
        }
        mapping.setTo(mo);
        to.put(mo.getMapObjectIdentifier().toString(), mo);
      }

      //
      // Set "map from" object
      //
      if (from.containsKey(values[2])) {
        mapping.setFrom( (MapObject) from.get(values[2]));
      } else {
        MapObject mo = new MapObject.Default();
        mo.setMapObjectIdentifier(new Identifier.Default(values[2]));
        if (from_source != null) {
          mo.setMapObjectSource(from_source);
        }
        mapping.setFrom(mo);
        from.put(mo.getMapObjectIdentifier().toString(), mo);
      }
      attr = mapping;
    } else if (rs.getString("ATTRIBUTE_NAME").equals("XMAPTO") &&
               rs.getString("TOBERELEASED").toLowerCase().equals("y")) {

      String av = rs.getString("ATTRIBUTE_VALUE");
      String[] values = FieldedStringTokenizer.split(av, "~");
      MapObject mo = null;
      if (to.containsKey(values[0])) {
        mo = (MapObject) to.get(values[0]);
      } else {

        // if in restricted mode and XMAP for this XMAPTO was not in
        // range, then skip this attribute
        if (isRestricted()) {
          keep_going = rs.next();
          return END_ATTRIBUTE;
        }

        mo = new MapObject.Default();
        mo.setMapObjectIdentifier(new Identifier.Default(values[0]));
        if (to_source != null) {
          mo.setMapObjectSource(to_source);
        }
        to.put(mo.getMapObjectIdentifier().toString(), mo);
      }
      populate(rs, mds, mo);
      attr = mo;
    } else if (rs.getString("ATTRIBUTE_NAME").equals("XMAPFROM") &&
               rs.getString("TOBERELEASED").toLowerCase().equals("y")) {
      String av = rs.getString("ATTRIBUTE_VALUE");
      String[] values = FieldedStringTokenizer.split(av, "~");
      MapObject mo = null;
      if (from.containsKey(values[0])) {
        mo = (MapObject) from.get(values[0]);
      } else {

        // if in restricted mode and XMAP for this XMAPFROM was not in
        // range, then skip this attribute
        if (isRestricted()) {
          keep_going = rs.next();
          return END_ATTRIBUTE;
        }

        mo = new MapObject.Default();
        mo.setMapObjectIdentifier(new Identifier.Default(values[0]));
        if (from_source != null) {
          mo.setMapObjectSource(from_source);
        }
        from.put(mo.getMapObjectIdentifier().toString(), mo);
      }
      populate(rs, mds, mo);
      attr = mo;
    } else if (rs.getString("ATTRIBUTE_NAME").equals("FROMVSAB") &&
               rs.getString("TOBERELEASED").toLowerCase().equals("y")) {
      from_source = mds.getSource(rs.getString("ATTRIBUTE_VALUE"));
      Iterator iter = from.values().iterator();
      while (iter.hasNext()) {
        ( (MapObject) iter.next()).setMapObjectSource(from_source);
      }
      attr = null;
    } else if (rs.getString("ATTRIBUTE_NAME").equals("TOVSAB") &&
               rs.getString("TOBERELEASED").toLowerCase().equals("y")) {
      to_source = mds.getSource(rs.getString("ATTRIBUTE_VALUE"));
      Iterator iter = to.values().iterator();
      while (iter.hasNext()) {
        ( (MapObject) iter.next()).setMapObjectSource(to_source);
      }
      attr = null;
    }
    return attr;
  }
}
