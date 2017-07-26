/*****************************************************************************
 *
 * Package: gov.nih.nlm.util
 * Object:  ObjectXMLSerializer
 *****************************************************************************/
package gov.nih.nlm.util;

import gov.nih.nlm.swing.SwingToolkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is used to provide an XML serialization scheme for ANY java
 * object. This class is thread-safe. It uses only local variables and some
 * static HashMap. Any multi-threaded application can create one instance of
 * this class and then safely call the toXML and fromXML methods in overlapping
 * threads without worrying that it will cause conflicts.
 * 
 * @author MEME Group
 */
public class ObjectXMLSerializer extends DefaultHandler {

  //
  // Static fields
  //

  private static Map primitive_types = new HashMap();

  private static Map primitive_wrapper_map = new HashMap();

  private static Map primitive_valueOf_methods = new HashMap();

  private static Map primitive_toString_methods = new HashMap();

  private static Map fields_as_hash_map = new HashMap();

  private static Map fields_as_array = new HashMap();

  private static Map primitive_defaults = new HashMap();

  private static Map object_attributes = new HashMap();

  private static Set primitive_classes = new HashSet();

  //
  // Used to compare field names
  //
  private final static Comparator FIELD_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof Field) || !(o2 instanceof Field)) {
        return 0;
      } else {
        Field f1 = (Field) o1;
        Field f2 = (Field) o2;
        // sort on name
        return f1.getName().compareTo(f2.getName());
      }
    }

    public boolean equals(Object o) {
      return false;
    }
  };

  //
  // Static initializer
  //
  static {
    // Map the primitive types to their corresponding class objects
    primitive_types.put("int", Integer.TYPE);
    primitive_types.put("boolean", Boolean.TYPE);
    primitive_types.put("char", Character.TYPE);
    primitive_types.put("long", Long.TYPE);
    primitive_types.put("float", Float.TYPE);
    primitive_types.put("short", Short.TYPE);
    primitive_types.put("byte", Byte.TYPE);
    primitive_types.put("double", Double.TYPE);

    // Map the primitive wrapper types to their corresponding full class types
    primitive_wrapper_map.put(Integer.TYPE, Integer.class);
    primitive_wrapper_map.put(Boolean.TYPE, Boolean.class);
    primitive_wrapper_map.put(Character.TYPE, Character.class);
    primitive_wrapper_map.put(Long.TYPE, Long.class);
    primitive_wrapper_map.put(Float.TYPE, Float.class);
    primitive_wrapper_map.put(Short.TYPE, Short.class);
    primitive_wrapper_map.put(Byte.TYPE, Byte.class);
    primitive_wrapper_map.put(Double.TYPE, Double.class);

    // Map primitive types to String representations of default values.
    primitive_defaults.put(Integer.TYPE, "0");
    primitive_defaults.put(Long.TYPE, "0");
    primitive_defaults.put(Short.TYPE, "0");
    primitive_defaults.put(Byte.TYPE, "0");
    primitive_defaults.put(Float.TYPE, "0.0");
    primitive_defaults.put(Double.TYPE, "0.0");
    primitive_defaults.put(Boolean.TYPE, "false");
    primitive_defaults.put(Character.TYPE, "");

    // List field names that must appear as sub-elements of the
    // Object tag since they are important attributes of Object tag
    object_attributes.put("id", "id");
    object_attributes.put("idref", "idref");
    object_attributes.put("class", "class");
    object_attributes.put("name", "name");
    object_attributes.put("length", "length");

    // Find methods to convert from string value to primitive type.
    Class[] signature = new Class[] { String.class };
    try {
      primitive_valueOf_methods.put("java.lang.Integer", Integer.class
          .getMethod("valueOf", signature));
      primitive_valueOf_methods.put("java.lang.Boolean", Boolean.class
          .getMethod("valueOf", signature));
      primitive_valueOf_methods.put("java.lang.Long", Long.class.getMethod(
          "valueOf", signature));
      primitive_valueOf_methods.put("java.lang.Short", Short.class.getMethod(
          "valueOf", signature));
      primitive_valueOf_methods.put("java.lang.Float", Float.class.getMethod(
          "valueOf", signature));
      primitive_valueOf_methods.put("java.lang.Double", Double.class.getMethod(
          "valueOf", signature));
      primitive_valueOf_methods.put("java.lang.Byte", Byte.class.getMethod(
          "valueOf", signature));
      primitive_valueOf_methods.put("java.lang.String",
          ObjectXMLSerializer.class.getMethod("StringValueOf", signature));
      primitive_valueOf_methods.put("java.lang.Character",
          ObjectXMLSerializer.class.getMethod("CharacterValueOf", signature));
      primitive_valueOf_methods.put("java.util.Date", ObjectXMLSerializer.class
          .getMethod("DateValueOf", signature));
      primitive_valueOf_methods.put("java.sql.Date", ObjectXMLSerializer.class
          .getMethod("DateValueOf", signature));
      primitive_valueOf_methods.put("java.sql.Timestamp",
          ObjectXMLSerializer.class.getMethod("DateValueOf", signature));

      signature = new Class[] {};
      primitive_toString_methods.put("java.lang.Integer", Integer.class
          .getMethod("toString", signature));
      primitive_toString_methods.put("java.lang.Boolean", Boolean.class
          .getMethod("toString", signature));
      primitive_toString_methods.put("java.lang.Long", Long.class.getMethod(
          "toString", signature));
      primitive_toString_methods.put("java.lang.Short", Short.class.getMethod(
          "toString", signature));
      primitive_toString_methods.put("java.lang.Float", Float.class.getMethod(
          "toString", signature));
      primitive_toString_methods.put("java.lang.Double", Double.class
          .getMethod("toString", signature));
      primitive_toString_methods.put("java.lang.Byte", Byte.class.getMethod(
          "toString", signature));
      primitive_toString_methods.put("java.lang.String", String.class
          .getMethod("toString", signature));
      primitive_toString_methods.put("java.lang.Character", Character.class
          .getMethod("toString", signature));
      primitive_toString_methods.put("java.util.Date", Date.class.getMethod(
          "getTime", signature));
      primitive_toString_methods.put("java.sql.Date", Date.class.getMethod(
          "getTime", signature));
      primitive_toString_methods.put("java.sql.Timestamp", Date.class.getMethod(
          "getTime", signature));

      //
      // List classes which are treated like primitives or exceptions
      //
      primitive_classes.add("java.lang.Integer");
      primitive_classes.add("java.lang.Boolean");
      primitive_classes.add("java.lang.Long");
      primitive_classes.add("java.lang.Short");
      primitive_classes.add("java.lang.Float");
      primitive_classes.add("java.lang.Double");
      primitive_classes.add("java.lang.Byte");
      primitive_classes.add("java.lang.Character");

      primitive_classes.add("java.lang.String");
      primitive_classes.add("java.util.Date");
      primitive_classes.add("java.sql.Timestamp");
      primitive_classes.add("java.sql.Date");

    } catch (NoSuchMethodException nsme) {
      SwingToolkit.reportError(
          "Method valueOf for one of the primitive type wrapper classes was not found. "
              + "Most likely this is the result of a bad JVM.", true);
    } catch (SecurityException se) {
      SwingToolkit
          .reportError(
              "Method valueOf for one of the primitive type wrapper classes was not accessible. "
                  + "Most likely this is the result of a bad JVM.", true);
    }
  }

  //
  // Class Fields
  //

  public HashMap objects_seen_read = new HashMap();

  private Stack keys = new Stack();

  private Stack object_stack = new Stack();

  private HashMap fields_map = new HashMap();

  private String chars_data = "";

  private String id = "";

  /**
   * Instantiates an empty {@link ObjectXMLSerializer}.
   */
  public ObjectXMLSerializer() {
  };

  /**
   * Clears the various class fields for the next round. This should be called
   * between subsequent serializations or de-serializations.
   */
  public void clear() {
    objects_seen_read = new HashMap();
    keys = new Stack();
    object_stack = new Stack();
    fields_map = new HashMap();
    chars_data = "";
    id = "";
  }

  /**
   * The {@link java.lang.Character} class does not have a
   * <code>valueOf(String)</code> method, this method takes its place in
   * reconstructing primitive values.
   * 
   * @param data
   *          a {@link String}
   * @return the {@link Character} in the first position of the specified
   *         {@link String}
   */
  public static Character CharacterValueOf(String data) {
    Character character = null;
    if (data == null || data.equals("")) {
      character = new Character((char) 0);
    } else {
      character = new Character(data.charAt(0));
    }
    return character;
  }

  /**
   * The {@link java.util.Date} classes do not have a
   * <code>valueOf(String)</code> method, this method takes its place in
   * reconstructing primitive values. Here, dates are represented as the string
   * value of the long obtained by calling {@link Date#getTime()}.
   * 
   * @param data
   *          a {@link String} representation of a date
   * @return the {@link Date} corresponding to that string
   */
  public static Date DateValueOf(String data) {
    Date d = new Date();
    d.setTime(Long.valueOf(data).longValue());
    return d;
  }

  /**
   * The {@link java.lang.String} classes do not have a
   * <code>valueOf(String)</code> method, this method takes its place in
   * reconstructing primitive values.
   * 
   * @param data
   *          the input {@link String}
   * @return the output {@link String}
   */
  public static String StringValueOf(String data) {
    String str = new String("");
    ;
    if (data != null) {
      str = new String(data);
    }
    return str;
  }

  //
  // XMLSerializer Implementation
  //

  /**
   * Converts an object to an XML representation.
   * 
   * @param obj
   *          an object to convert
   * @return a XML representation of the object
   * @throws Exception
   *           if conversion fails
   */
  public String toXML(Object obj) throws Exception {
    return toXML(obj, "");
  }

  /**
   * Converts an object to an XML representation with the specified level of
   * indenting.
   * 
   * @param obj
   *          an object to convert
   * @param indent
   *          a {@link String} containing space characters
   * @return a XML representation of the object
   * @throws Exception
   *           if conversion fails
   */
  public String toXML(Object obj, String indent) throws Exception {
    clear();
    Context context = new Context();
    context.ref_counter = 0;
    context.indent = indent;
    return writeObject(obj, "", context);
  }

  /**
   * Creates an object from the XML representation indicated by the specified
   * file name.
   * 
   * @param file
   *          an XML file name
   * @return the object represented by the file
   * @throws Exception
   *           if conversion fails
   */
  public Object fromXML(String file) throws Exception {

    return fromXML(file, true);
  }

  /**
   * Creates an object from the XML representation indicated by the specified
   * file name.
   * 
   * @param file
   *          an XML file name
   * @param validating
   *          a flag indicating whether or not a validating parser should be
   *          used
   * @return the object represented by the file
   * @throws Exception
   *           if conversion fails
   */
  public Object fromXML(String file, boolean validating) throws Exception {

    clear();
    FileReader xml_reader = null;
    try {
      xml_reader = new FileReader(new File(file));
    } catch (Exception e) {
      Exception te = new Exception("Failed to parse XML document.", e);
      throw te;
    }

    return fromXML(xml_reader, validating);
  }

  /**
   * Creates an object from the XML representation indicated by the specified
   * {@link Reader}.
   * 
   * @param reader
   *          a {@link Reader} object
   * @return the object represented by the file
   * @throws Exception
   *           if conversion fails
   */
  public Object fromXML(Reader reader) throws Exception {

    return fromXML(reader, true);
  }

  /**
   * Creates an object from the XML representation indicated by the specified
   * {@link Reader}.
   * 
   * @param reader
   *          an object {@link Reader}
   * @param validating
   *          a flag indicating whether or not a validating parser should be
   *          used
   * @return the object represented by the file
   * @throws Exception
   *           if conversion fails
   */
  public Object fromXML(Reader reader, boolean validating) throws Exception {
    clear();
    Object obj = null;
    // Use the default (non-validating) parser
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      // Parse the input
      SAXParser saxParser = factory.newSAXParser();
      object_stack.clear();
      saxParser.parse(new InputSource(reader), this);
      obj = ((FieldInfo) object_stack.pop()).getValue();
    } catch (Exception e) {
      e.printStackTrace();
      Exception xpe = new Exception("Failed to parse XML document.", e);
      throw xpe;
    }
    return obj;
  }

  /**
   * Returns the top object on the object {@link Stack}.
   * 
   * @return the top object on the object {@link Stack}
   * @throws SAXException
   *           if nothing is on the stack
   */
  protected Object getObject() throws SAXException {
    if (!object_stack.empty()) {
      if (object_stack.size() == 1) {
        return ((FieldInfo) object_stack.pop()).getValue();
      } else {
        throw new SAXException("The Object Stack has more than one object ");
      }
    } else {
      throw new SAXException("The Object Stack is Empty");
    }
  }

  /**
   * Clears the object stack.
   */
  protected void clearObject() {
    object_stack.clear();
  }

  //
  // Methods
  //
  // Implemention of SAXParser

  /**
   * Handles opening tags.
   * 
   * @param namespace_uri
   *          The namespace URI
   * @param simple_name
   *          the simple name of the element
   * @param qualified_name
   *          the fully qualified name of the allement
   * @param attrs
   *          The element attributes
   * @throws SAXException
   *           if anything goes wrong
   */
  public void startElement(String namespace_uri, String simple_name, // simple
                                                                      // name
                                                                      // (localName)
      String qualified_name, // qualified name
      Attributes attrs) throws SAXException {

    //
    // Get the element name
    //
    String element = simple_name;
    if ("".equals(element)) {
      element = qualified_name;

      //
      // Reset the character data
      //
    }
    chars_data = "";

    try {

      //
      // Obtain Object tag attributes
      //
      Class c = null;
      String name = attrs.getValue("name");
      String idref = attrs.getValue("idref");
      id = attrs.getValue("id");
      String length = attrs.getValue("length");
      String class_name = attrs.getValue("class");

      //
      // Handle the "O" tag
      //
      if (element.equals("O")) {

        //
        // Determine the class name.
        //
        if (attrs.getValue("class") != null) {
          class_name = attrs.getValue("class");
        } else if (idref == null) {
          if (((FieldInfo) object_stack.peek()).getValue().getClass().isArray()) {
            c = ((FieldInfo) object_stack.peek()).getValue().getClass()
                .getComponentType();
            class_name = c.getName();
          } else {
            class_name = ((Field) fields_map.get(name)).getType().getName();
          }
        }

        //
        // Handle ID refrences
        //
        if (idref != null) {
          object_stack.push(new FieldInfo(name, objects_seen_read.get(idref),
              false));

        } else {

          //
          // Load the class
          //
          try {
            c = forName(class_name);
            if (c.isInterface()) {
              class_name = c.getName().concat("$Default");
              c = forName(class_name);
            }
          } catch (Exception x) {
            Exception re = new Exception("Class " + class_name
                + " could not be loaded.", x);
            throw re;
          }

          //
          // Handle Arrays
          //
          if (length != null) {

            //
            // Create a new instance of the array with the correct length.
            //
            Object o = Array.newInstance(c, Integer.valueOf(length).intValue());

            //
            // Map the array to its id
            //
            objects_seen_read.put(id, o);
            if (primitive_wrapper_map.get(c) != null) {
              class_name = ((Class) primitive_wrapper_map.get(c)).getName();

              //
              // Construct FieldInfo for the array
              //
            }
            object_stack.push(new FieldInfo(name, o, false));

            //
            // Cache the object with the id.
            //
            objects_seen_read.put(id, o);
          }

          //
          // Handle special case: Map
          //
          else if (class_name.equals(HashMap.class.getName())
              || class_name.equals(Hashtable.class.getName())
              || class_name.equals(Properties.class.getName())) {
            Object map = forceNewInstance(Class.forName(class_name));
            object_stack.push(new FieldInfo(name, map, false));
            objects_seen_read.put(id, map);
          }

          //
          // Handle special case: primitive object type
          //
          else if (primitive_classes.contains(class_name)) {
            Object o = forceNewInstance(c);
            object_stack.push(new FieldInfo(name, o, false));
            objects_seen_read.put(id, o);

            //
            // Handle normal object
            //
          } else {

            //
            // Handle the object case
            //
            Object o = forceNewInstance(c);

            //
            // o should not be null here
            //

            //
            // Get all of its fields.
            //
            fields_map = getAllFieldsAsHashMap(c);

            //
            // Set null to all object fields
            // And the default value to primitive type field
            //
            List field_list = new ArrayList(fields_map.values());
            Collections.sort(field_list, FIELD_COMPARATOR);
            Iterator iterator = field_list.iterator();
            while (iterator.hasNext()) {
              Field f = (Field) iterator.next();
              Object f_ob = null;
              if (!Modifier.isFinal(f.getModifiers()) && !excludeField(f)) {

                //
                // Get the data for this field unless
                // it is a field called id, then it must
                // be handled elsewhere
                //
                String data = null;
                if (!object_attributes.containsKey(f.getName())) {
                  data = attrs.getValue(f.getName());

                  //
                  // Handle id refrences encoded as tag attributes
                  //
                }
                if (data != null && data.startsWith("#")) {
                  if (objects_seen_read.get(data.substring(1)) != null) {
                    f_ob = objects_seen_read.get(data.substring(1));
                  } else if (data.substring(1).equals(id)) {
                    f_ob = o;
                  }
                }

                //
                // Set the default values to primitive fields
                //
                if (f_ob == null && isPrimitive(f)) {

                  Class field_class = null;
                  try {
                    field_class = f.getType();
                  } catch (NullPointerException npe) {
                    // If we catch a null pointer exception
                    // it means we are using an incompatable version
                    // of this class (see getFieldType comments).

                    // The most flexible thing to do is to just keep
                    // going!
                    npe.printStackTrace();
                  }

                  if (data == null) {
                    data = (String) primitive_defaults.get(field_class);
                  }
                  if (data != null) {
                    if (primitive_wrapper_map.get(field_class) != null) {
                      field_class = (Class) primitive_wrapper_map
                          .get(field_class);
                    }
                    //
                    // For all primitive types, dynamically call the valueOf
                    // method which takes a string.
                    //
                    f_ob = getPrimitiveObjectForString(field_class, data);
                  }
                }

                //
                // Set the field value
                //
                try {
                  if (f_ob != null)
                    f.set(o, f_ob);
                } catch (IllegalAccessException iae) {
                  Exception re = new Exception("Field " + f.getName()
                      + " of object " + o.getClass().getName()
                      + " is not accessible.", iae);
                  throw re;
                }
              }
            } // end of while

            object_stack.push(new FieldInfo(name, o, false));

            //
            // Cache the object with the id.
            //
            objects_seen_read.put(id, o);

          }
        }

        //
        // Handle primitive variables
        //
      } else if (element.equals("V")) {
        Object f_ob = null;
        FieldInfo f_info = (FieldInfo) object_stack.peek();
        Object o = f_info.getValue();

        if (o.getClass().isArray()) {

          //
          // Determine default value
          //
          String data = attrs.getValue("value");
          f_ob = getPrimitiveObjectForString(o.getClass().getComponentType(),
              data);

          //
          // Set the value for this element of the array
          //
          Array.set(o, Integer.valueOf(name).intValue(), f_ob);

        } else {
          Field f = (Field) fields_map.get(name);
          Class field_class = null;
          String data = attrs.getValue("value");
          try {
            field_class = f.getType();
          } catch (NullPointerException npe) {
            // If we catch a null pointer exception
            // it means we are using an incompatable version
            // of this class (see getFieldType comments).

            // The most flexible thing to do is to just keep going!
            npe.printStackTrace();
          }

          if (primitive_wrapper_map.containsKey(field_class)) {
            field_class = (Class) primitive_wrapper_map.get(field_class);

            //
            // Instantiate primitive object
            //
          }
          f_ob = getPrimitiveObjectForString(field_class, data);

          try {
            // Set the field value in the object
            // This will throw an exception if there is a problem
            if (f_ob != null)
              f.set(o, f_ob);
          } catch (IllegalAccessException iae) {
            Exception re = new Exception("Field " + f.getName() + " of object "
                + o.getClass().getName() + " is not accessible.", iae);
            throw re;
          } catch (NullPointerException npe) {
            // keep going
            npe.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SAXException(e);
    }
  }

  /**
   * Implementation of {@link DefaultHandler} API.
   * 
   * @param chars
   *          characters from within an XML tag
   * @param start
   *          start index
   * @param length
   *          number of chars in this segment
   */
  public void characters(char[] chars, int start, int length) {
    String s = new String(chars, start, length);
    chars_data = chars_data.concat(s);
  }

  /**
   * Implementation of {@link DefaultHandler} API.
   * 
   * @param namespace_uri
   *          the namespace URI
   * @param simple_name
   *          the simple name
   * @param qualified_name
   *          the qualified name
   * @throws SAXException
   *           if failed to parse object
   */
  public void endElement(String namespace_uri, String simple_name, // simple
                                                                    // name
      String qualified_name // qualified name
  ) throws SAXException {

    String element = simple_name;
    if ("".equals(element)) {
      element = qualified_name;

    }

    try {

      //
      // Handle object tags
      //
      if (element.equals("O")) {

        //
        // Get top element from object stack
        //
        FieldInfo field_info = (FieldInfo) object_stack.pop();

        //
        // Get the get the name and instance
        //
        String name = field_info.getName();
        Object obj = field_info.getValue();

        //
        // If we are not working with the top-level element
        //
        if (!name.equals("")) {

          //
          // Handle object primitive types.
          //
          if (id != null && isPrimitive(obj.getClass())) {
            obj = getPrimitiveObjectForString(obj.getClass(), chars_data);
            objects_seen_read.put(id, obj);
          }

          //
          // Get parent object from stack
          //
          field_info = (FieldInfo) object_stack.pop();
          Object f_ob = field_info.getValue();

          //
          // Handle array element
          //
          if (f_ob.getClass().isArray()) {
            Array.set(f_ob, Integer.valueOf(name).intValue(), obj);

          }

          //
          // Handle null values for HashMap, Hashtable, Properties
          //
          else if (field_info.getValue().getClass().equals(HashMap.class)
              || field_info.getValue().getClass().equals(Hashtable.class)
              || field_info.getValue().getClass().equals(Properties.class)) {
            Map map = (Map) f_ob;
            if (name.equals("key")) {
              if (obj.equals("__NULL__") && obj instanceof String) {
                obj = null;
              }
              keys.push(obj);
            } else if (name.equals("value")) {
              if (obj.equals("__NULL__") && obj instanceof String) {
                obj = null;
              }
              map.put(keys.pop(), obj);
            }

          } else {
            fields_map = getAllFieldsAsHashMap(f_ob.getClass());
            Field f = (Field) fields_map.get(name);

            try {
              // Set the field value in the object
              // This will throw an exception if there is a problem
              f.set(f_ob, obj);
            } catch (IllegalAccessException iae) {
              Exception re = new Exception("Field " + f.getName()
                  + " of object " + field_info.getValue().getClass().getName()
                  + " is not accessible.", iae);
              throw re;
            }
          }
        }
        object_stack.push(field_info);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SAXException(e);
    }

  }

  /**
   * This private method gets a class name. The normal Class.forName method will
   * not return things like "int" or "double" so we use a static hashmap
   * (declared above) to make it work.
   * 
   * @param class_name
   *          An object {@link String} representation of the name of class.
   * @return An object {@link Class} represented by that name.
   * @throws Exception
   *           if the class name cannot be found.
   */
  private Class forName(String class_name) throws Exception {
    if (primitive_types.get(class_name) != null) {
      return (Class) primitive_types.get(class_name);
    } else {
      return Class.forName(class_name);
    }
  };

  /**
   * Returns the string value of an "object primitive".
   * 
   * @param o
   *          object whose string values is sought
   * @return the string value of an "object primitive"
   * @throws Exception
   *           if failed to get primitive string value
   */
  private String getPrimitiveStringValue(Object o) throws Exception {

    Method m = null;
    final Class[] signature = new Class[] {};
    if (Primitive.class.isAssignableFrom(o.getClass())) {
      m = o.getClass().getMethod("getPrimitiveValue", signature);
    } else if (primitive_toString_methods.containsKey(o.getClass().getName())) {
      m = (Method) primitive_toString_methods.get(o.getClass().getName());
    }
    String s = null;
    try {
      s = String.valueOf(m.invoke(o, new Object[] {}));
    } catch (Exception x) {
      Exception re = new Exception(
          "Attempt to invoke valueOf method of class failed ("
              + o.getClass().getName() + ")", x);
      throw re;
    }
    return s;
  }

  /**
   * Returns the (primitive) object matching the string.
   * 
   * @param c
   *          the {@link Class}
   * @param s
   *          the string representation of the object
   * @return the (primitive) object matching the string
   * @throws Exception
   *           if failed to get primitive object for string
   */
  private Object getPrimitiveObjectForString(Class c, String s)
      throws Exception {

    final Class[] signature = new Class[] { String.class };
    Method m = null;
    if (Primitive.class.isAssignableFrom(c)) {
      m = c.getMethod("newInstance", signature);
    } else if (primitive_wrapper_map.containsKey(c)) {
      m = (Method) primitive_valueOf_methods.get(((Class) primitive_wrapper_map
          .get(c)).getName());
    } else {
      m = (Method) primitive_valueOf_methods.get(c.getName());
    }
    Object obj = null;
    try {
      obj = m.invoke(null, new Object[] { s });
    } catch (Exception x) {
      Exception re = new Exception(
          "Attempt to invoke valueOf method of class failed (" + c.getName()
              + ")", x);
      throw re;
    }
    return obj;
  }

  /**
   * Returns true if the class is primitive or in special classes
   * 
   * @param c
   *          a potentially primitive {@link Class}
   * @return true if the class is primitive or in special classes
   */
  private boolean isPrimitive(Class c) {
    if (c.isPrimitive() || primitive_classes.contains(c.getName())) {
      return true;
    }
    if (Primitive.class.isAssignableFrom(c)) {
      return true;
    }
    return false;
  }

  /**
   * Returns true if the field is primitive or in special classes
   * 
   * @param f
   *          a potentially primitive {@link Field}
   * @return true if the class is primitive or in special classes
   */
  private boolean isPrimitive(Field f) {
    if (f.getType().isPrimitive()
        || primitive_classes.contains(f.getType().getName())) {
      return true;
    }
    if (Primitive.class.isAssignableFrom(f.getType())) {
      return true;
    }
    return false;
  }

  /**
   * Indicates whether or not the class name is required when writing out an
   * object based on the discrepancy between the field class name and the
   * instance class name
   * 
   * @param field_class
   *          an object {@link Class}
   * @param instance_class
   *          an object {@link Class}
   * @return <code>true</code> if class name is needed; <code>false</code>
   *         otherwise
   */
  private boolean isClassNameNeeded(Class field_class, Class instance_class) {
    return !(field_class.equals(instance_class)
        || instance_class.equals(primitive_wrapper_map.get(field_class)) || (field_class
        .isInterface() && instance_class.getName().equals(
        field_class.getName() + "$Default")));
  }

  /**
   * Gets all fields in Class c and its superclasses and puts them into a
   * HashMap.
   * 
   * @param c
   *          An object {@link Class} to get the fields for.
   * @return An object {@link HashMap} of field names mapped to Field objects.
   * @throws Exception
   *           if conversion fails.
   */
  private HashMap getAllFieldsAsHashMap(Class c) throws Exception {

    // First, look it up in the cache.
    if (fields_as_hash_map.containsKey(c)) {
      return (HashMap) fields_as_hash_map.get(c);
    }

    // Else construct it from the array
    Field fields[] = getAllFieldsAsArray(c);
    HashMap newhm = new HashMap();
    for (int i = 0; i < fields.length; i++) {
      newhm.put(fields[i].getName(), fields[i]);
    }

    // Add it to the cache
    fields_as_hash_map.put(c, newhm);

    // Return the fields
    return newhm;
  }

  /**
   * Gets all fields in Class c and its superclasses and returns them as an
   * array of Field objects.
   * 
   * @param c
   *          An object {@link Class} to get the fields for.
   * @return An array of object {@link Field} in that Class.
   * @throws Exception
   *           if there is an error looking up the fields.
   */
  private Field[] getAllFieldsAsArray(Class c) throws Exception {

    // First look it up in the cache
    if (fields_as_array.containsKey(c)) {
      return (Field[]) fields_as_array.get(c);
    }
    ;

    // Else construct it by reflectinf
    Field[] fields = {};

    // Call it recursively until we reach null or Object
    if ((c.getSuperclass() != null)
        && (!c.getSuperclass().getName().equals("java.lang.Object"))) {
      fields = getAllFieldsAsArray(c.getSuperclass());
    }

    Field additional[] = null;
    Field newfields[] = null;
    try {
      // Add fields for this level
      additional = c.getDeclaredFields();
      newfields = new Field[additional.length + fields.length];

    } catch (SecurityException se) {
      Exception re = new Exception("Declared fields of class " + c.getName()
          + " were inaccessible.", se);
      throw re;
    }

    // Put fields array into newfields
    System.arraycopy(fields, 0, newfields, 0, fields.length);

    // Put additional fields into new fields
    System
        .arraycopy(additional, 0, newfields, fields.length, additional.length);

    // must set permission to access private and protected fields
    for (int i = 0; i < newfields.length; i++) {
      final Field field = newfields[i];
      try {
        AccessController.doPrivileged(new PrivilegedExceptionAction() {
          public Object run() throws SecurityException {
            field.setAccessible(true);
            return null;
          }
        });
      } catch (Exception e) {
        Exception re = new Exception("Attempt to make field + "
            + field.getName() + " accessible failed.", e);
        throw re;
      }
    }

    // Add fields to the cache
    fields_as_array.put(c, newfields);

    // Return fields
    return newfields;
  }

  /**
   * This method creates the XML representation of a recipe. It is called
   * recursively in order to write out all of the fields of a recipe.
   * 
   * @param o
   *          An object {@link Object} to convert to XML.
   * @param name
   *          An object {@link String} whereas if this is a recursive call, the
   *          name of a field of the parent object; "" otherwise.
   * @param context
   *          An object {@link Context} wrapper for useful parameters.
   * @return An object {@link String} representation of the object.
   * @throws Exception
   *           if conversion fails.
   */
  private String writeObject(Object o, String name, Context context)
      throws Exception {
    // Assume document size of at least 5000 bytes
    StringBuffer line = new StringBuffer(5000);
    writeObject(o, name, context, line, true);
    return line.toString();
  }

  /**
   * This method creates the XML representation of a recipe and appends it to a
   * StringBuffer. It is called recursively in order to write out all of the
   * fields of a recipe.
   * 
   * @param o
   *          An object {@link Object} to convert to XML.
   * @param name
   *          An object {@link String} whereas if this is a recursive call, the
   *          name of a field of the parent object; "" otherwise.
   * @param context
   *          An object {@link Context} wrapper for useful parameters.
   * @param line
   *          An object {@link StringBuffer} used to store the document.
   * @param write_class_name
   *          A <code>booleanM</code> representation of class name.
   * @throws Exception
   *           if conversion fails.
   */
  private void writeObject(Object o, String name, Context context,
      StringBuffer line, boolean write_class_name) throws Exception {
    String indent = context.indent;
    //
    // The object should not be null
    //
    if (o == null) {
      throw new NullPointerException();
    }

    //
    // Check if we've seen the object already
    //
    Integer idref = (Integer) context.objects_seen_write.get(o);

    //
    // If so, just write an idref tag & return it.
    //
    if (idref != null) {
      line.append("<O name=\"");
      line.append(name);
      line.append("\" ");
      line.append("idref=\"");
      line.append(idref);
      line.append("\"");
      line.append("></O>\n");
      return;
    }

    //
    // Increment the ref counter, this id will apply to the next object seen
    //
    int id = ++context.ref_counter;

    //
    // cache our object with the id
    //
    //System.out.println("name=" + name + ", class=" + o.getClass().getName());
    context.objects_seen_write.put(o, new Integer(id));

    //
    // Handle case where the Object is primitive
    //
    if (isPrimitive(o.getClass())) {
      line.append("<O name=\"");
      line.append(name);
      line.append("\" ");
      if (write_class_name) {
        line.append("class=\"").append(o.getClass().getName()).append("\" ");
      }
      line.append("id=\"");
      line.append(id);
      line.append("\">");

      //
      // Obtain string value
      //
      String s = getPrimitiveStringValue(o);
      boolean needToChange = false;
      int numChars = s.length();
      int i = 0;
      while (i < numChars && !needToChange) {
        char c = s.charAt(i);
        switch (c) {
        case '<':
        case '>':
        case ':':
        case '&':
        case '\t':
        case '"':
        case '\0':
          i++;
          needToChange = true;
          break;
        default:
          i++;
          break;
        }
      }
      if (needToChange) {
        line.append("<![CDATA[");
        line.append(s);
        line.append("]]>");
      } else {
        line.append(s);
      }
      line.append("</O>\n");
      return;
    }

    //
    // HashMap is a special case convert to key,value pairs and write them out.
    //
    else if (o.getClass().equals(HashMap.class)
        || o.getClass().equals(Hashtable.class)
        || o.getClass().equals(Properties.class)) {
      line.append("<O name=\"");
      line.append(name);
      line.append("\" ");
      if (write_class_name) {
        line.append("class=\"").append(o.getClass().getName()).append("\" ");
      }
      line.append("id=\"");
      line.append(id);
      line.append("\">\n");

      context.indent = indent + " ";
      Map m = (Map) o;
      Iterator iter = m.keySet().iterator();
      while (iter.hasNext()) {
        Object key = iter.next();
        Object value = m.get(key);
        if (key == null) {
          writeObject("__NULL__", "key", context, line, true);
        } else {
          writeObject(key, "key", context, line, true);
        }
        if (value == null) {
          writeObject("__NULL__", "value", context, line, true);
        } else {
          writeObject(value, "value", context, line, true);
        }
      }
      line.append("</O>\n");
      context.indent = indent;
      return;
    }

    //
    // Arrays are written out with their contents as object
    // tags where the field name is the array index.
    //
    else if (o.getClass().isArray()) {
      line.append("<O name=\"");
      line.append(name);
      line.append("\" ");
      line.append("id=\"");
      line.append(id);
      line.append("\" ");
      line.append(" class=\"");
      line.append(o.getClass().getComponentType().getName());
      line.append("\"");
      line.append(" length=\"");
      line.append(Array.getLength(o));
      line.append("\">\n");

      context.indent = indent + " ";
      // Iterate through array elements.
      for (int i = 0; i < Array.getLength(o); i++) {
        Object obj = Array.get(o, i);

        //
        // Write as primitives if class names are not needed
        // and the array type is primitive.
        //
        if (obj != null
            && isPrimitive(o.getClass().getComponentType())
            && !isClassNameNeeded(o.getClass().getComponentType(), obj
                .getClass())) {
          writePrimitive(obj, String.valueOf(i), context.indent, false, line);

          //
          // Otherwise write objects
          //
        } else {
          //
          // Ignore null elements objects
          //
          if (obj != null) {
            //
            // Recursively write this object tag
            //
            // The write_class_name flag should be false
            // if the object in this array represents the
            // component type of the array
            //
            boolean wcn = isClassNameNeeded(o.getClass().getComponentType(),
                obj.getClass());
            writeObject(obj, String.valueOf(i), context, line, wcn);
          }
        }
      }
      line.append("</O>\n");
      context.indent = indent;
      return;
    }

    //
    // o is just an Object -
    // it is not an array, it is not null, and it is not an idref
    //
    line.append("<O name=\"");
    line.append(name);
    line.append("\" ");
    line.append("id=\"");
    line.append(id);
    line.append("\" ");
    if (write_class_name) {
      line.append(" class=\"");
      line.append(o.getClass().getName());
      line.append("\" ");
    }

    Object obj = null;

    //
    // Get fields for the object
    //
    Field[] fields = getAllFieldsAsArray(o.getClass());
    List obj_fields = new ArrayList(fields.length);

    //
    // Throw any exceptions that this produces
    //
    for (int i = 0; i < fields.length; i++) {

      //
      // Obtain instance reference
      //
      Object instance = null;
      try {
        instance = fields[i].get(o);
      } catch (IllegalAccessException iae) {
        Exception re = new Exception("Field " + fields[i].getName()
            + " of object " + ((o != null) ? o.getClass().getName() : "")
            + " is not accessible.", iae);
        throw re;
      }

      //
      // If this is a normal field (not final and not (transient
      // and unserializable)) then write it out.
      //
      // We have a problem with the backtrace field
      // of the java.lang.Exception object, so if we encounter it
      // just ignore it (starting jdk1.3).
      //
      if ((!Modifier.isFinal(fields[i].getModifiers()))
          && ((!Modifier.isTransient(fields[i].getModifiers())) || (!(instance instanceof NotPersistentIfTransient)))
          &&
          // Exception exception
          (!(Modifier.isTransient(fields[i].getModifiers())
              && o.getClass().getName().indexOf("Exception") != -1 && fields[i]
              .getName().equals("backtrace")))) {

        //
        // The field has already been marked as accessible
        // If field is Primitive call writePrimitive
        //
        final Field field = fields[i];

        /***********************************************************************
         * instance = null; try { instance = field.get(o); } catch
         * (IllegalAccessException iae) { ReflectionException re = new
         * ReflectionException( "Field of object is not accessible.", field,
         * iae); re.setDetail("object", o); re.setDetail("class", (o != null) ?
         * o.getClass().getName() : ""); re.setDetail("field",field.getName());
         * throw re; }
         **********************************************************************/

        //
        // Write as an attribute if the field class is primitive
        // and no class name is required.
        //
        if (instance != null && isPrimitive(field)
            && !isClassNameNeeded(field.getType(), instance.getClass())
            && !object_attributes.containsKey(field.getName())) {
          writePrimitive(instance, field.getName(), indent + " ", true, line);
        }
        //
        // If field has already seen and the field name doesn't match an
        // attribute of the object
        // write it as an attribute.
        //
        else if ((context.objects_seen_write.get(instance) != null)
            && !object_attributes.containsKey(field.getName())
            && instance != null) {
          String attr_value = "#"
              + (Integer) context.objects_seen_write.get(instance);
          writePrimitive(attr_value, field.getName(), indent + " ", true, line);
        }

        //
        // Otherwise just get the field
        //
        else {
          obj_fields.add(field);
        }

      } // end if not transient and final

    } // end for

    line.append(">");

    //
    // Sort the remaining object fields
    //
    Collections.sort(obj_fields, FIELD_COMPARATOR);

    boolean found = false;
    for (Iterator i = obj_fields.iterator(); i.hasNext();) {
      Field field = (Field) i.next();

      try {
        obj = field.get(o);
      } catch (IllegalAccessException iae) {
        Exception re = new Exception("Field " + field.getName() + " of object "
            + ((o != null) ? o.getClass().getName() : "")
            + " is not accessible.", iae);
        throw re;
      }

      //
      // Primitives may be left if they have names like "id"
      //
      if (obj != null && isPrimitive(field)
          && !isClassNameNeeded(field.getType(), obj.getClass())) {
        if (!found) {
          line.append("\n");
        }
        found = true;
        writePrimitive(obj, field.getName(), indent + " ", false, line);

        //
        // Write it if object is not null
        //
      } else if (obj != null) {

        if (!found) {
          line.append("\n");
        }
        found = true;

        boolean wcn = isClassNameNeeded(field.getType(), obj.getClass());
        context.indent = indent + " ";
        writeObject(obj, field.getName(), context, line, wcn);
        context.indent = indent;
      }
    }
    line.append("</O>\n");
    return;
  };

  /**
   * This method creates the XML representation of a Primitive field and appends
   * it to a StringBuffer.
   * 
   * @param o
   *          An object {@link Object} representation of a primitive to convert
   *          to XML
   * @param name
   *          An object {@link String} representation of field name of this
   *          primitive object
   * @param indent
   *          An object {@link String} for indenting
   * @param attr
   *          True if the object is an attribute
   * @param line
   *          An object {@link StringBuffer} containing the document
   * @throws Exception
   *           if failed to write primitive
   */
  private void writePrimitive(Object o, String name, String indent,
      boolean attr, StringBuffer line) throws Exception {

    if (o instanceof Boolean && ((Boolean) o).booleanValue() == false) {
      // Default value do nothing
    } else if (o instanceof Character && ((Character) o).charValue() == '\0') {
      // Default value do nothing
    } else if (primitive_defaults.get(o.getClass()) != null
        && o.toString().equals(primitive_defaults.get(o.getClass()))) {
      // Default value do nothing
    } else {

      String s = getPrimitiveStringValue(o);
      boolean needToChange = false;
      int numChars = s.length();
      int i = 0;
      while (i < numChars && !needToChange) {
        char c = s.charAt(i);
        switch (c) {
        case '<':
        case '>':
        case ':':
        case '&':
        case '\t':
        case '\n':
        case '\r':
        case '"':
        case '\0':
          i++;
          needToChange = true;
          break;
        default:
          i++;
          break;
        }
      }

      if (needToChange) {
        s = XMLEntityEncoder.encode(s);

      }
      if (attr) {
        line.append(" ").append(name).append("=\"").append(s).append("\"");
      } else {
        line.append("<V name=\"").append(name).append("\" value=\"").append(s)
            .append("\" />\n");
      }
    }
  };



  /**
   * This method is responsible for finding a constructor and forcing it to
   * create an intitial object. It uses default values for the various
   * constructor parameters. If no constructor could be found, or if calling the
   * constructor raises an exception, then we should just bail.
   * 
   * @param c
   *          An object {@link Class} to convert to XML.
   * @return An object {@link Object} representation of converted document.
   * @throws Exception
   *           if conversion fails.
   */
  public Object forceNewInstance(Class c) throws Exception {
    // System.err.println(c.getName());

    // first try to find an empty constructor
    try {
      Constructor constructor = c.getDeclaredConstructor(new Class[] {});
      constructor.setAccessible(true);
      return constructor.newInstance(new Object[] {});
    } catch (NoSuchMethodException exc) {
      // This means there is no no-argument constructor
      // don't bail yet if that is the case.
    } catch (Exception e) {
      Exception re = new Exception("Constructor " + c.getName() + " failed.", e);
      throw re;
    }

    Constructor[] constructors = c.getDeclaredConstructors();
    AccessibleObject.setAccessible(constructors, true);
    for (int i = 0; i < constructors.length; i++) {
      Class[] paramTypes = constructors[i].getParameterTypes();
      if (paramTypes.length == 0) {
        // already tried the zero argument constructor
        continue;
      }
      Object[] params = new Object[paramTypes.length];
      for (int j = paramTypes.length - 1; j >= 0; j--) {
        Class type = paramTypes[j];

        if (type == Byte.TYPE || type == Byte.class) {
          params[j] = new Byte((byte) 0);
        } else if (type == Short.TYPE || type == Short.class) {
          params[j] = new Short((short) 0);
        } else if (type == Integer.TYPE || type == Integer.class) {
          params[j] = new Integer(0);
        } else if (type == Long.TYPE || type == Long.class) {
          params[j] = new Long(0);
        } else if (type == Character.TYPE || type == Character.class) {
          params[j] = new Character((char) 0);
        } else if (type == Boolean.TYPE || type == Boolean.class) {
          params[j] = new Boolean(true);
        } else if (type == Float.TYPE || type == Float.class) {
          params[j] = new Float(0);
        } else if (type == Double.TYPE || type == Double.class) {
          params[j] = new Double(0);
        } else if (type == String.class) {
          params[j] = "0";
        } else if (type == c) {

          // If the type is the same as the class, we have
          // an infinite recursion problem, make it null
          params[j] = null;
        } else {

          // setting object parms to null may present a problem
          // so instead we try to force a new instance of that type.
          params[j] = forceNewInstance(paramTypes[j]);
        }
      }

      try {
        // This will throw an exception if the instance cannot be made
        return constructors[i].newInstance(params);
      } catch (Exception e) {
        Exception re = new Exception("Constructor " + c.getName() + "failed.",
            e);
        throw re;
      }

    }

    // If we get here, then no constructors were found
    Exception re = new Exception("No suitable constructor for " + c.getName()
        + " could be found.");
    throw re;

  }

  /**
   * Helper method to determine which fields should not be set when re-creating
   * an instance. We have problems with these fields .*Exception.*.backtrace
   * java.lang.Integer.perThreadBuffer (1.4.2)
   * 
   * @param f
   *          an object {@link Field}
   * @return <code>true</code> if exclude field; <code>false</code>
   *         otherwise
   */
  private boolean excludeField(Field f) {
    return (f.getDeclaringClass().getName().equals("java.lang.Integer") && f
        .getName().equals("perThreadBuffer"))
        || (f.getDeclaringClass().getName().indexOf("Exception") != -1 && f
            .getName().equals("backtrace"));
  }

  //
  // Inner classes
  //

  /**
   * This class represents ifnormation about a field, including the field's
   * name, Object value, and a boolean flag indicating whether or not it is
   * prmitive.
   */
  private class FieldInfo {

    //
    // Fields
    //
    private String name = "";

    private Object value = null;

    /**
     * Constructor
     * 
     * @param nm
     *          An object {@link String} representation of name.
     * @param val
     *          An object {@link Object} representation of value.
     * @param prim
     *          A <code>boolean</code> representation of primitive.
     * @return An object {@link FieldInfo}.
     */
    public FieldInfo(String nm, Object val, boolean prim) {
      name = nm;
      value = val;
    };

    /**
     * Default Constructor
     */
    public FieldInfo() {
    };

    /**
     * Returns the Object value for this field.
     * 
     * @return An object {@link Object} representation of value of the field.
     */
    public Object getValue() {
      return value;
    };

    /**
     * Returns the name of the field.
     * 
     * @return An object {@link String} representation of name of the field.
     */
    public String getName() {
      return name;
    };

    /**
     * Sets the value for the field.
     * 
     * @param o
     *          An object {@link Object} representation of value of the field.
     */
    public void setValue(Object o) {
      value = o;
    };

    /**
     * Sets the name of the field.
     * 
     * @param n
     *          An object {@link String} representation of name of the field.
     */
    public void setName(String n) {
      name = n;
    };

  } // end inner FieldInfo class

  /**
   * This private inner class is a container for information that the parsing
   * and writing routines need. Using this mechanism allows ALL variables to be
   * local to the methods, thus enabling this object to be thread-safe without
   * requiring multiple instances of it to exist.
   */
  private class Context {

    // Fields
    public StringBuffer line;

    public IdentityHashMap objects_seen_write = new IdentityHashMap();

    public HashMap objects_seen_read = new HashMap();

    public int ref_counter = 0;

    public DocumentBuilder builder;

    public DocumentBuilderFactory factory;

    public String indent = "";

    public boolean validating = true;
  }

  //
  // Main method (for testing)
  //

  /**
   * The main method performs a self-QA test
   * 
   * @param argv
   *          An array of argument.
   */
  public static void main(String[] argv) {
    try {

      System.out.println("-----------------------------------------------");
      System.out.println("Starting ... " + new Date());
      System.out.println("-----------------------------------------------");

      // Make a serializer
      ObjectXMLSerializer oxs = new ObjectXMLSerializer();

      // First, create an object.
      ArrayList ar = new ArrayList();

      // 1. String
      ar.add(new String("Test String \" end"));

      // 2. String[]
      String[] strs = new String[5];
      for (int i = 0; i < strs.length; i++) {
        strs[i] = String.valueOf(i);
      }
      ar.add(strs);

      // 3. Date
      ar.add(new Date());

      // 4. int[]
      int[] intarray = new int[5];
      for (int i = 0; i < intarray.length; i++) {
        intarray[i] = i;
      }
      ar.add(intarray);

      // 5. test primitive
      TestPrimitive t = new TestPrimitive();
      ar.add(t);

      // 6. Properties
      Properties p = new Properties();
      p.setProperty("propertiestest", "value");
      ar.add(p);

      // 7. HashMap
      HashMap hm = new HashMap();
      hm.put("hashmaptest", "value");
      ar.add(hm);

      // 8. Properties
      Hashtable ht = new Hashtable();
      ht.put("hashtabletest", "value");
      ar.add(ht);

      System.out.println("\n\tStart with an arraylist:");
      System.out.println("\t-------------------------\n\t  " + ar);
      System.out.println("\n\tStart with an arraylist:");
      System.out.println("\t-------------------------\n\t  " + ar);

      // Serialize the arraylist.
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(
          "arraylist.xml")));
      System.out.println("\n\tArraylist document:\n\t-------------------");
      String doc = oxs.toXML(ar, "");
      System.out.println(doc);
      out.write(doc);
      out.close();

      System.out.println("\n\tTry reading the document: arraylist.xml");
      System.out.println("\t--------------------------------------------");

      //
      // Read arraylist back in
      //
      Object obj = oxs.fromXML("arraylist.xml", false);
      ArrayList ar2 = (ArrayList) obj;

      // 1.
      String string2 = (String) ar2.get(0);
      System.out.println("1. String \t" + string2);

      // 2.
      String[] strs2 = (String[]) ar2.get(1);
      System.out.println("2. String[]");
      System.out.println("\tlength = " + strs2.length);
      System.out.println("\tstrs2[0] = " + strs2[0]);
      System.out.println("\tstrs2[1] = " + strs2[1]);

      // 3.
      Date date2 = (Date) ar2.get(2);
      System.out.println("3. Date \t" + date2);

      // 4.
      int[] i2 = (int[]) ar2.get(3);
      System.out.println("4. int[]");
      System.out.println("\tlength = " + i2.length);
      for (int i = 0; i < i2.length; i++) {
        System.out.println("\ti2[" + i + "] = " + i2[i]);

      }

      // 5. test primitive
      System.out.println("6. TestPrimitive");
      t = (TestPrimitive) ar2.get(4);
      System.out.println(t.getPrimitiveValue());

      // 6. Properties
      System.out.println("6. Properties");
      p = (Properties) ar2.get(5);
      System.out.println(p.toString());

      // 7. HashMap
      System.out.println("7. HashMap");
      hm = (HashMap) ar2.get(6);
      System.out.println(hm.toString());

      // 8. Hashtable
      System.out.println("8. Hashtable");
      ht = (Hashtable) ar2.get(7);
      System.out.println(ht.toString());

      // Cleanup
      File f = new File("arraylist.xml");
      f.delete();

      System.out.println("-----------------------------------------------");
      System.out.println("Finished ... " + new Date());
      System.out.println("-----------------------------------------------");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Objects should implement this if they are to be ignored by the serializer
   * and are marked as transient members.
   */
  public interface NotPersistentIfTransient {
  }

  /**
   * Anything implementing this method should also have a static
   * <code>newInstance(String)</code> method.
   */
  public interface Primitive {

    /**
     * Returns the value as a {@link String}. The static
     * <code>newInstance</code> method takes this string value and produces a
     * populated object.
     * 
     * @return a {@link String} representation
     */
    public String getPrimitiveValue();

  }

  private static class TestPrimitive implements Primitive {
    private Date d;

    private Date d2;

    public TestPrimitive() {
      d = new Date();
      d2 = new Date();
    }

    public String getPrimitiveValue() {
      return d.getTime() + "|" + d2.getTime();
    }

    public static TestPrimitive newInstance(String s) {
      String[] ss = FieldedStringTokenizer.split(s, "|");
      TestPrimitive t = new TestPrimitive();
      t.d = new Date(Long.parseLong(ss[0]));
      t.d2 = new Date(Long.parseLong(ss[1]));
      return t;
    }

  }

}
