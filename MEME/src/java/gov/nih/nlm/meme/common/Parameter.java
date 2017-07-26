/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Parameter
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a Parameter.
 *
 * @author MEME Group
 */

public interface Parameter {

  /**
   * Returns the name.
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the value.
   * @return the value
   */
  public Object getValue();

  /**
   * Returns the value as an <code>int</code>.
   * @return the value as an <code>int</code>
   */
  public int getInt();

  /**
   * Returns the value as an <code>byte</code>.
   * @return the value as an <code>byte</code>
   */
  public byte getByte();

  /**
   * Returns the value as an <code>short</code>.
   * @return the value as an <code>short</code>
   */
  public short getShort();

  /**
   * Returns the value as an <code>long</code>.
   * @return the value as an <code>long</code>
   */
  public long getLong();

  /**
   * Returns the value as an <code>float</code>.
   * @return the value as an <code>float</code>
   */
  public float getFloat();

  /**
   * Returns the value as an <code>double</code>.
   * @return the value as an <code>double</code>
   */
  public double getDouble();

  /**
   * Returns the value as an <code>boolean</code>.
   * @return the value as an <code>boolean</code>
   */
  public boolean getBoolean();

  /**
   * Returns the value as an <code>char</code>.
   * @return the value as an <code>char</code>
   */
  public char getChar();

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(Object value);

  /**
   * Sets the value
   * @param value the value
   */
  public void setValue(int value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(byte value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(short value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(long value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(float value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(double value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(boolean value);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(char value);

  /**
   * Indicatess whether or not object should be treated as a primitive.
   * @return <code>true</code> if it is primitive,
   * <codE>false</code> otherwise
   */
  public boolean isPrimitive();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link Parameter} interface.
   */
  public class Default implements Parameter {

    //
    // Fields
    //

    private String name = null;
    private Object value = null;
    private boolean primitive = false;

    //
    // Constructors
    //

    /**
     * No-argument constructor
     */
    public Default() {};

    /**
     * Instantiates a {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     * @param primitive the "primitive" flag
     */
    public Default(String name, Object value, boolean primitive) {
      this.name = name;
      this.value = value;
      this.primitive = primitive;
    }

    /**
     * Instantiates a non-primitive {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, Object value) {
      this(name, value, false);
    }

    /**
     * Instantiates an <codE>int</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, int value) {
      this(name, new Integer(value), true);
    }

    /**
     * Instantiates an <code>byte</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, byte value) {
      this(name, new Byte(value), true);
    }

    /**
     * Instantiates an <code>short</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, short value) {
      this(name, new Short(value), true);
    }

    /**
     * Instantiates an <code>long</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, long value) {
      this(name, new Long(value), true);
    }

    /**
     * Instantiates an <code>float</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, float value) {
      this(name, new Float(value), true);
    }

    /**
     * Instantiates an <code>double</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, double value) {
      this(name, new Double(value), true);
    }

    /**
     * Instantiates an <code>boolean</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, boolean value) {
      this(name, new Boolean(value), true);
    }

    /**
     * Instantiates an <code>char</code> {@link Parameter.Default}.
     * @param name the name
     * @param value the value
     */
    public Default(String name, char value) {
      this(name, new Character(value), true);
    }

    //
    // Overridden Object methods
    //

    /**
     * Returns <code>int</code> hashcode.
     * @return <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    /**
     * Returns a string representation.
     * @return a string representation
     */
    public String toString() {
      return "[" + name + "=" + value + "]";
    }

    /**
     * Equality function.
     * @param obj object ot compare to
     * @return <code>true</code> if the objects are equal; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
      if (obj instanceof Parameter) {
        Parameter p = (Parameter) obj;
        return getName().equals(p.getName()) &&
            getValue().equals(p.getValue());
      }
      return false;
    }

    //
    // Implementation of Parameter interface
    //

    /**
     * Implements {@link Parameter#getName()}.
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * Implements {@link Parameter#setName(String)}.
     * @param name the name
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Implements {@link Parameter#getValue()}.
     * @return the value
     */
    public Object getValue() {
      return value;
    }

    /**
     * Implements {@link Parameter#getInt()}.
     * @return the value
     */
    public int getInt() {
      return ( (Integer) value).intValue();
    }

    /**
     * Implements {@link Parameter#getByte()}.
     * @return the value
     */
    public byte getByte() {
      return ( (Byte) value).byteValue();
    }

    /**
     * Implements {@link Parameter#getShort()}.
     * @return the value
     */
    public short getShort() {
      return ( (Short) value).shortValue();
    }

    /**
     * Implements {@link Parameter#getLong()}.
     * @return the value
     */
    public long getLong() {
      return ( (Long) value).longValue();
    }

    /**
     * Implements {@link Parameter#getFloat()}.
     * @return the value
     */
    public float getFloat() {
      return ( (Float) value).floatValue();
    }

    /**
     * Implements {@link Parameter#getDouble()}.
     * @return the value
     */
    public double getDouble() {
      return ( (Double) value).doubleValue();
    }

    /**
     * Implements {@link Parameter#getBoolean()}.
     * @return the value
     */
    public boolean getBoolean() {
      return ( (Boolean) value).booleanValue();
    }

    /**
     * Implements {@link Parameter#getChar()}.
     * @return the value
     */
    public char getChar() {
      return ( (Character) value).charValue();
    }

    /**
     * Implements {@link Parameter#setValue(Object)}.
     * @param value the value
     */
    public void setValue(Object value) {
      this.value = value;
      this.primitive = false;
    }

    /**
     * Implements {@link Parameter#setValue(int)}.
     * @param value the value
     */
    public void setValue(int value) {
      this.value = new Integer(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(byte)}.
     * @param value the value
     */
    public void setValue(byte value) {
      this.value = new Byte(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(short)}.
     * @param value the value
     */
    public void setValue(short value) {
      this.value = new Short(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(long)}.
     * @param value the value
     */
    public void setValue(long value) {
      this.value = new Long(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(float)}.
     * @param value the value
     */
    public void setValue(float value) {
      this.value = new Float(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(double)}.
     * @param value the value
     */
    public void setValue(double value) {
      this.value = new Double(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(boolean)}.
     * @param value the value
     */
    public void setValue(boolean value) {
      this.value = new Boolean(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#setValue(char)}.
     * @param value the value
     */
    public void setValue(char value) {
      this.value = new Character(value);
      this.primitive = true;
    }

    /**
     * Implements {@link Parameter#isPrimitive()}.
     * @return <code>true</code> if primitive, <code>false</code> otherwise
     */
    public boolean isPrimitive() {
      return primitive;
    }

  }
}
