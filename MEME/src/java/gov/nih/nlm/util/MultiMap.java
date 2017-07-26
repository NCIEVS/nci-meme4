/************************************************************************
 * Package:     gov.nih.nlm.util
 * Object:      MultiMap.java
 * Changes
 *  06/05/2006 BAC (1-BECAZ): add clear method
 *  04/10/2006 TTN (1-AV6X1) : use synchronizedMap
 *
 ***********************************************************************/
package gov.nih.nlm.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a {@link Map} that uses two keys.
 * For use in an environment where you want to cache symmetric sets of data
 * but with different top-level keys.  e.g. cache the source lists from
 * two different databases.
 */
public class MultiMap {

  //
  // Default top-key to use
  //

  //
  // Data
  //
  private Map data = Collections.synchronizedMap(new HashMap());

  /**
   * Instantiates a {@link MultiMap}.
   */
  public MultiMap() {
    super();

  }

  /**
   * Clears the map using the specified "top" key.
   * @param top_key the top key
   */
  public void clear(Object top_key) {
    Map m = (Map) data.get(top_key);
    if (m != null) m.clear();
  }

  /**
   * Puts the key/value pair into the map using the specified "top" key.
   * @param top_key the top key
   * @param key the key
   * @param value the value
   */
  public void put(Object top_key, Object key, Object value) {
    Map m = (Map) data.get(top_key);
    if (m == null) {
      m = Collections.synchronizedMap(new HashMap());
      data.put(top_key, m);
    }
    m.put(key, value);
  }

  /**
   * Returns the value for the key, using the specified "top" key.
   * @param top_key the top key
   * @param key the key
   * @return the value for the key, using the default "top" key
   */
  public Object get(Object top_key, Object key) {
    Map m = (Map) data.get(top_key);
    if (m == null) {
      m = Collections.synchronizedMap(new HashMap());
      data.put(top_key, m);
    }
    return m.get(key);
  }

  /**
   * Removes the entry for the specified keys.
   * @param top_key the top key
   * @param key the key
   */
  public void remove(Object top_key, Object key) {
    Map m = (Map) data.get(top_key);
    if (m == null) {
      return;
    }
    m.remove(key);
  }

  /**
   * Returns the value for the specified "top" key.
   * @param tk the top key
   * @return the value for the key, using the default "top" key
   */
  public Collection values(Object tk) {
    Map m = (Map) data.get(tk);
    if (m == null) {
      return new HashSet();
    }
    return m.values();
  }

  /**
   * Returns the key set for the specified "top" key.
   * @param tk the top key
   * @return the entry set for the key, using the default "top" key
   */
  public Set keySet(Object tk) {
    Map m = (Map) data.get(tk);
    if (m == null) {
      return new HashSet();
    }
    return m.keySet();
  }

  /**
   * Returns the entry set for the specified "top" key.
   * @param tk the top key
   * @return the entry set for the key, using the default "top" key
   */
  public Set entrySet(Object tk) {
    Map m = (Map) data.get(tk);
    if (m == null) {
      return new HashSet();
    }
    return m.entrySet();
  }

  /**
   * Determines whether or not it contains the specified key.
   * @param tk the top key
   * @param key the key to check
   * @return <code>true</code> if it contains the key;
   * <code>false</code> otherwise
   */
  public boolean containsKey(Object tk, Object key) {
    Map m = (Map) data.get(tk);
    if (m == null) {
      return false;
    }
    return m.containsKey(key);
  }

  /**
   * Returns the Map for the key.
   * @param tk the top key
   * @return the Map for the key, using the default "top" key
   */
  public Map getMap(Object tk) {
    return (Map) data.get(tk);
  }

}
