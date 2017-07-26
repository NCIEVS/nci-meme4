/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  XMLSerializer
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.exception.XMLParseException;

import java.io.Reader;

/**
 * Generically represents a class that can convert objects to XML and back.
 *
 * @author  MEME Group
 */
public interface XMLSerializer {

  //
  // Methods
  //

  /**
   * This method generically converts an object to an XML document.
   * The implementation should call {@link #toXML(Object,String)} with a
   * default indent value.
   * @param object An object {@link Object} to serialize.
   * @return An object {@link String} containing an XML representation of that
   * document.
   * @throws ReflectionException if conversion fails.
   */
  public String toXML(Object object) throws ReflectionException;

  /**
   * This method generically converts an object to an XML document.
   * @param object An object {@link Object} representation of an object to
   * serialize.
   * @param indent An object {@link String} used for indenting.
   * @return An object {@link String} containing an XML representation of that
   * document.
   * @throws ReflectionException if conversion fails.
   */
  public String toXML(Object object, String indent) throws ReflectionException;

  /**
   * This method generically takes a file name, parses the file and produces a
   * Java Object reprsented by that XML file.  The implementation should call
       * {@link #fromXML(String,boolean)} and pass it a default value for validating.
   * @param string An object {@link String} representation of file name of an
   * XML file.
   * @return The Object represented by the document in that file
   * @throws ReflectionException if conversion fails.
   * @throws XMLParseException if conversion fails.
   */
  public Object fromXML(String string) throws ReflectionException,
      XMLParseException;

  /**
   * This method generically takes a Reader, parses it and produces a Java
   * Object reprsented by the document in the Reader.  The implementation
   * should call {@link #fromXML(Reader,boolean)} and pass it a default value
   * for validating.
   * @param reader An object {@link Reader} containing an XML document.
   * @return An object {@link Object} represented by the document in that file.
   * @throws ReflectionException if conversion fails.
   * @throws XMLParseException if conversion fails.
   */
  public Object fromXML(Reader reader) throws ReflectionException,
      XMLParseException;

  /**
   * This method generically takes a file name, parses the file and produces a
   * Java Object reprsented by that XML file.  The parser used to parse the
   * file is validating if the flag is <code>true</code>, and non-validating if
   * <code>false</code>.
   * @param string An object {@link String} representation of the file name of
   * an XML file.
   * @param validating A <code>boolean</code> representation of flag indicating
   * whether or not the parser should validate.
   * @return An object {@link Object} represented by the document in that file.
   * @throws ReflectionException if conversion fails.
   * @throws XMLParseException if conversion fails.
   */
  public Object fromXML(String string, boolean validating) throws
      ReflectionException, XMLParseException;

  /**
   * This method generically takes a Reader, parses it and produces a Java
   * Object represented by the document in the Reader.  The parser used to
   * parse the file is validating if the flag is <code>true</code>, and
   * non-validating if <code>false</code>.
   * @param reader An object {@link Reader} containing an XML document.
   * @param validating A <code>boolean</code> representation of flag indicating
   * whether or not the parser should validate.
   * @return An object {@link Object} represented by the document in that file.
   * @throws ReflectionException if conversion fails.
   * @throws XMLParseException if conversion fails.
   */
  public Object fromXML(Reader reader, boolean validating) throws
      ReflectionException, XMLParseException;

}
