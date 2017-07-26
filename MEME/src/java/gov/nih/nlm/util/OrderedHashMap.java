/************************************************************************
 * Package:     gov.nih.nlm.util
 * Object:      OrderedHashMap.java
 ***********************************************************************/
package gov.nih.nlm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A {@link HashMap} that maintains the order in which elements were added.
 *
 * @author Stephanie Halbeisen
 */
public class OrderedHashMap extends HashMap {

  //
  // Private Fields
  //
  private ArrayList keys;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link OrderedHashMap}.
   */
  public OrderedHashMap() {
    super();
    keys = new ArrayList();
  }

  /**
   * Instantiates an empty {@link OrderedHashMap} with the specified capacity.
   * @param capacity the initial capacity
   */
  public OrderedHashMap(int capacity) {
    super(capacity);
    keys = new ArrayList(capacity);
  }

  /**
   * Instantiates an empty {@link OrderedHashMap} with the specified capacity
   * and load factor.
   * @param capacity the initial capacity
   * @param load_factor the load factor
   */
  public OrderedHashMap(int capacity, float load_factor) {
    super(capacity, load_factor);
    keys = new ArrayList(capacity);
  }

  /**
   * Instantiates an {@link OrderedHashMap} from an existing {@link Map}.
   * @param map the map
   */
  public OrderedHashMap(Map map) {
    super(map);
    keys = new ArrayList(map.keySet());
  }

  //
  // Methods
  //

  /**
   * Adds a key/value pair.
   * @param key the key
   * @param value the value
   * @return {@link Object}
   */
  public Object put(Object key, Object value) {
    if (super.get(key) == null) {
      keys.add(key);
    }
    return super.put(key, value);
  }

  /**
   * Adds all elements from the specified {@link Map}.
   * @param map the {@link Map}
   */
  public void putAll(Map map) {
    Iterator iter = map.keySet().iterator();
    while (iter.hasNext()) {
      Object next = iter.next();
      if (super.get(next) == null) {
        keys.add(next);
      }
    }
    super.putAll(map);
  }

  /**
   * Returns the {@link Object} key for a specified index.
   * @param index an index into the ordered list
   * @return the key as an {@link Object}
   */
  public Object getKey(int index) {
    return keys.get(index);
  }

  /**
   * Returns the {@link Object} value for a specified index.
   * @param index an index into the ordered list
   * @return the value as an {@link Object}
   */
  public Object getValue(int index) {
    return super.get(keys.get(index));
  }

  /**
   * Removes the mapping for the specified key.
   * @param key the key
   * @return the mapping
   */
  public Object remove(Object key) {
    keys.remove(keys.indexOf(key));
    return super.remove(key);
  }

  /**
   * Clears the map.
   */
  public void clear() {
    super.clear();
    keys = new ArrayList();
  }

  /**
   * Returns the key set as an ordered {@link List}.
   * @return the key set as an ordered {@link List}
   */
  public List orderedKeySet() {
    return keys;
  }

}
