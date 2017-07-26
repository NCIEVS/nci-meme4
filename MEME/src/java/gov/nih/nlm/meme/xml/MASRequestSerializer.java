/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  MASRequestSerializer
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.ComponentMetaData;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.PasswordAuthentication;
import gov.nih.nlm.meme.common.PasswordAuthenticator;
import gov.nih.nlm.meme.common.Warning;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.exception.XMLParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Used to map
 * {@link <a href="/MEME/Data/MASRequest.dtd">
 *  MEME service request documents</a>} to
 * {@link MEMEServiceRequest} objects and vice-versa using the public
 * {@link #fromXML(String)} or {@link #fromXML(Reader)},
 * and {@link #toXML(Object)} methods.
 *
 * Internally, this class implements a validating SAX parser.  Typically
 * an instance must be informed of the location of the DTD via a call
 * to {@link #setSystemId(String)} method before one
 * of the <code>fromXML()</code> methods is called. For example,<p>
 *
 * <pre>
 * String file = "my_file.xml";
 * MEMERequestSerializer mrs = new MEMERequestSerializer();
 * mrs.setSystemId("url of MASRequest.dtd");
 * MEMEServiceRequest request = (MEMEServiceRequest)mrs.fromXML(file);
 * </pre>
 *
 * Alternatively the system id could be supplied to the serializer
 * via the second constructor.
 *
 * @author MEME Group
 */
public class MASRequestSerializer extends ObjectXMLSerializer implements
    XMLSerializer {

  //
  // Fields
  //

  private HashSet active = new HashSet();
  private HashMap elements = new HashMap();
  private MEMEServiceRequest service_request;
  private String chars_data = "";
  private final static String[] ind_array = {
      "  ", "    ", "      "};

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MASRequestSerializer} without a system ID.
   */
  public MASRequestSerializer() {
    super();
  }

  /**
   * Instantiates a {@link MASRequestSerializer} with the specified system ID.
   * @param system_id the system id
   */
  public MASRequestSerializer(String system_id) {
    this();
  }

  //
  // Methods
  //

  /**
   * Sets the system id.
   * This class implements a validating parser.  When reading from a Reader,
   * we need to know the SystemID so we can locate the DTD.  This accessor
   * method provides a hook to set the system id.
       * @param url a {@link String} URL (for files use <code>file:/</code> notation)
   */
  public void setSystemId(String url) {

  }

  /**
   * Returns the object represented in the contents of the named XML file.
   * This method takes a filename containing a MASRequest document,
   * wraps a {@link FileReader} around it and passes it to
       * {@link #fromXML(Reader,boolean)} instructing it to use a validating parser.
   * @param file the file name
   * @return the {@link Object} represented by the file
   * @throws ReflectionException if conversion fails
   * @throws XMLParseException if conversion fails
   */
  public Object fromXML(String file) throws ReflectionException,
      XMLParseException {
    return fromXML(file, true);
  }

  /**
   * Parses and (optionally) validates the specified XML file and
   * returns the {@link Object} represented by it.
   * This method takes a filename containing a MEME service request document,
   * wraps a {@link FileReader} around it and passes it to
   * {@link #fromXML(Reader,boolean)}.
   * @param file the file name
   * @param validating a flag indicating whether or not to validate
   * @return the {@link Object} represented by the file
   * @throws ReflectionException if conversion fails
   * @throws XMLParseException if conversion fails
   */
  public Object fromXML(String file, boolean validating) throws
      ReflectionException, XMLParseException {

    //MEMEToolkit.trace("MASRequestSerializer.fromXML() -Location 3");

    FileReader xml_reader = null;
    try {
      xml_reader = new FileReader(new File(file));
    } catch (IOException e) {
      XMLParseException xpe = new XMLParseException(
          "Failed open XML document.", this, e);
      xpe.setDetail("file_name", file);
      throw xpe;
    }

    return fromXML(xml_reader, validating);
  }

  /**
   * Parses and validates the contents of the specified {@link Reader}.
   * This method takes a Reader containing a MEME service request document
   * and parses it using a validating parser, sending the document root to
   * {@link #fromXML(Reader,boolean)}.
   * @param input_reader a {@link Reader} containing an XML document
   * @return the {@link Object} represented by the document
   * @throws ReflectionException if conversion fails
   * @throws XMLParseException if conversion fails
   */
  public Object fromXML(Reader input_reader) throws ReflectionException,
      XMLParseException {
    return fromXML(input_reader, true);
  }

  /**
   * Pazses and (optionally) validates the contents of the specified {@link Reader}.
   * This method takes a Reader containing a MEME service request document
   * and parses it.
   * @param input_reader a {@link Reader} containing an XML document
   * @param validating a flag indicating whether or not to validate
   * @return the {@link Object} represented by the document
   * @throws ReflectionException if conversion fails
   * @throws XMLParseException if conversion fails
   */
  public Object fromXML(Reader input_reader, boolean validating) throws
      ReflectionException, XMLParseException {
    clear();
    service_request = new MEMEServiceRequest();
    //
    // Use the default (non-validating) parser
    //
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser sax_parser = null;
    try {
      //
      // Create parser and parse input source
      //
      sax_parser = factory.newSAXParser();
      sax_parser.parse(new InputSource(input_reader), this);
    } catch (ParserConfigurationException pce) {
      XMLParseException xpe = new XMLParseException(
          "Failed to configure XML parser.", this, pce);
      throw xpe;
    } catch (SAXException se) {
      XMLParseException xpe = new XMLParseException(
          "Failed to parse XML document: " + se.getMessage(), this,
          se.getException());
      throw xpe;
    } catch (IOException ioe) {
      XMLParseException xpe = new XMLParseException(
          "Failed I/O error while parsing XML document.", this, ioe);
      xpe.setDetail("input_source", input_reader.toString());
      throw xpe;
    }
    return service_request;

  }

  /**
   * This method converts a {@link MEMEServiceRequest} object to
   * an XML {@link String} representation.
   * @param input_object a {@link MEMEServiceRequest}
   * @return the XML {@link String} representation
   * @throws ReflectionException if conversion fails
   */
  public String toXML(Object input_object) throws ReflectionException {
    return toXML(input_object, "");
  };

  /**
   * This method converts a {@link MEMEServiceRequest} object to
   * a String representation of MEME service request.
   * @param input_object a {@link MEMEServiceRequest}
   * @return the XML {@link String} representation
   * @param indent an indent {@link String}
   * @throws ReflectionException if conversion fails
   */
  public String toXML(Object input_object, String indent) throws
      ReflectionException {

    if (input_object == null) {
      throw new NullPointerException();
    } else if (! (input_object instanceof MEMEServiceRequest)) {
      throw new ReflectionException(
          "Object to serialize must be an instance of MEMEServiceRequest.", this, null);
    }

    MEMEServiceRequest service_request = (MEMEServiceRequest) input_object;

    StringBuffer sb = new StringBuffer(5000);

    //
    // XML Header
    //
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n");

    //
    // We are not using a validating parser
    //
    //sb.append("<!DOCTYPE MASRequest SYSTEM \"");
    //sb.append(system_id);
    //sb.append("MASRequest.dtd\">\n\n");
    //sb.append("<!DOCTYPE MASRequest SYSTEM \"MASRequest.dtd\">\n\n");
    //sb.append("<!DOCTYPE MASRequest SYSTEM \"M.dtd\">\n\n");

    sb.append("<MASRequest>\n");

    //
    // ConnectionInformation
    //
    sb.append(ind_array[0]);
    sb.append("<ConnectionInformation>\n");

    if (service_request.getSessionId() != null ||
        service_request.initiateSession() ||
        service_request.noSession() ||
        service_request.terminateSession()) {
      //
      // ConnectionInformation:Session
      //
      sb.append(ind_array[1]);
      sb.append("<Session");
      if (service_request.getSessionId() != null) {
        sb.append(" id=\"");
        sb.append(service_request.getSessionId());
        sb.append("\"");
      }
      if (service_request.initiateSession()) {
        sb.append(" initiate=\"true\"");
      }
      if (service_request.noSession()) {
        sb.append(" nosession=\"true\"");
      }
      if (service_request.terminateSession()) {
        sb.append(" terminate=\"true\"");
      }
      sb.append(" />\n");
    }

    if (service_request.getMidService() != null) {
      //
      // ConnectionInformation:DataSource
      //
      sb.append(ind_array[1]);
      sb.append("<DataSource service=\"");
      sb.append(service_request.getMidService());
      sb.append("\" />\n");
    }

    if (service_request.getAuthentication() != null) {
      //
      // ConnectionInformation:Authentication
      //
      Authentication authentication = service_request.getAuthentication();
      if (authentication instanceof PasswordAuthentication) {
        PasswordAuthenticator passwordAuthenticator =
            new PasswordAuthenticator();
        authentication.provideAuthentication(passwordAuthenticator);
        service_request.authenticate(passwordAuthenticator);
        sb.append(ind_array[1]);
        sb.append("<Authentication mode=\"user:password\">\n");
        sb.append(ind_array[2]);
        sb.append("<Username>");
        sb.append(passwordAuthenticator.getUsername());
        sb.append("</Username>\n");
        sb.append(ind_array[2]);
        sb.append("<Password>");
        sb.append(passwordAuthenticator.getPassword());
        sb.append("</Password>\n");
        sb.append(ind_array[1]);
        sb.append("</Authentication>\n");
      }
    }

    if (service_request.getTimeout() != MEMEServiceRequest.NO_TIMEOUT) {
      //
      // ConnectionInformation:Timeout
      //
      sb.append(ind_array[1]);
      sb.append("<Timeout units=\"seconds\" value=\"");
      sb.append(service_request.getTimeout());
      sb.append("\" />\n");
    }

    if (service_request.getIdlePriority() != MEMEServiceRequest.NO_IDLE) {
      //
      // ConnectionInformation:Idle
      //
      sb.append(ind_array[1]);
      sb.append("<Idle priority=\"");
      sb.append(service_request.getIdlePriority());
      sb.append("\" />\n");
    }

    sb.append(ind_array[0]);
    sb.append("</ConnectionInformation>\n");

    ComponentMetaData[] software_versions = service_request.getCurrentSoftware();
    if (software_versions != null && software_versions.length > 0) {
      //
      // SoftwareVersion
      //
      sb.append(ind_array[0]);
      sb.append("<SoftwareVersions>\n");
      sb.append(super.toXML(service_request.getCurrentSoftware(), ind_array[1]));
      sb.append(ind_array[0]);
      sb.append("</SoftwareVersions>\n");
    }

    //
    // ServiceParameter
    //
    sb.append(ind_array[0]);
    sb.append("<ServiceParameters>\n");

    //
    // ServiceParameter:Service
    //
    sb.append(ind_array[1]);
    sb.append("<Service>");
    sb.append(service_request.getService());
    sb.append("</Service>\n");

    Parameter[] parameters = service_request.getParameters();
    if (parameters != null && parameters.length > 0) {
      //
      // ServiceParameter:Parameter
      //
      sb.append(ind_array[1]);
      sb.append("<Parameter>\n");
      sb.append(super.toXML(service_request.getParameters(), ind_array[2]));
      sb.append(ind_array[1]);
      sb.append("</Parameter>\n");
    }

    sb.append(ind_array[0]);
    sb.append("</ServiceParameters>\n");

    //
    // ClientResponse
    //
    sb.append(ind_array[0] + "<ClientResponse>\n");

    ComponentMetaData[] required_software = service_request.getRequiredSoftware();
    if (required_software != null && required_software.length > 0) {
      //
      // ClientResponse:RequiredSoftwareUpdates
      //
      sb.append(ind_array[1]);
      sb.append("<RequiredSoftwareUpdates>\n");
      sb.append(super.toXML(service_request.getRequiredSoftware(), ind_array[2]));
      sb.append(ind_array[1]);
      sb.append("</RequiredSoftwareUpdates>\n");
    }

    Exception[] exceptions = service_request.getExceptions();
    if (exceptions != null && exceptions.length > 0) {
      //
      // ClientResponse:Exception
      //
      sb.append(ind_array[1]);
      sb.append("<Exception>\n");
      sb.append(super.toXML(service_request.getExceptions(), ind_array[2]));
      sb.append(ind_array[1]);
      sb.append("</Exception>\n");
    }

    Warning[] warnings = service_request.getWarnings();
    if (warnings != null && warnings.length > 0) {
      //
      // ClientResponse:Warning
      //
      sb.append(ind_array[1]);
      sb.append("<Warning>\n");
      sb.append(super.toXML(service_request.getWarnings(), ind_array[2]));
      sb.append(ind_array[1]);
      sb.append("</Warning>\n");
    }

    Parameter[] return_values = service_request.getReturnValues();
    if (return_values != null && return_values.length > 0) {
      //
      // ClientResponse:ReturnValue
      //
      sb.append(ind_array[1]);
      sb.append("<ReturnValue>\n");
      sb.append(super.toXML(service_request.getReturnValues(), ind_array[2]));
      sb.append(ind_array[1]);
      sb.append("</ReturnValue>\n");
    }

    sb.append(ind_array[0]);
    sb.append("</ClientResponse>\n");
    sb.append("</MASRequest>\n");

    return sb.toString();
  }

  //
  // SAX API
  //

  /**
   * Handles start tags.
   * @param namespace_uri the namespace
   * @param s_name the simple name of the tag
   * @param q_name the qualified name of the tag
   * @param attrs the tag attributes
   * @throws SAXException if the start tag cannot be handled
   */
  public void startElement(String namespace_uri,
                           String s_name, // simple name (localName)
                           String q_name, // qualified name
                           Attributes attrs) throws SAXException {

    //
    // Obtain element name
    //
    String element = s_name;
    if ("".equals(element)) {
      element = q_name;

    }
    chars_data = "";

    //
    // Mark element as active in a hash.
    //
    active.add(element);
    try {
      if (element.equals("Object") || element.equals("Var")) {
        super.startElement(namespace_uri, s_name, q_name, attrs);
      }
      //
      // ConnectionInformation
      //
      if (element.equals("Session")) {
        if (attrs.getValue("id") != null) {
          service_request.setSessionId(attrs.getValue("id"));
        }
        if (attrs.getValue("initiate") != null) {
          service_request.setInitiateSession(Boolean.valueOf(attrs.getValue(
              "initiate")).booleanValue());
        }
        if (attrs.getValue("nosession") != null) {
          service_request.setNoSession(Boolean.valueOf(attrs.getValue(
              "nosession")).booleanValue());
        }
        if (attrs.getValue("terminate") != null) {
          service_request.setTerminateSession(Boolean.valueOf(attrs.getValue(
              "terminate")).booleanValue());
        }
      }
      if (element.equals("DataSource")) {
        //
        // The default for service should be "editing-db"
        //
        String service = attrs.getValue("service");
        service_request.setMidService(service);
      }
      if (element.equals("Authentication")) {
        if ("user:password".equals(attrs.getValue("mode"))) {
          elements.put("mode", "user:password");
        }
      }
      if (element.equals("Timeout")) {
        service_request.setTimeout
            (Long.parseLong(attrs.getValue("value")));
      }
      if (element.equals("Idle")) {
        service_request.setIdlePriority
            (Byte.parseByte(attrs.getValue("priority")));
      }

      //
      // SoftwareVersion
      //
      if (element.equals("SoftwareVersions")) {
        clearObject();
      }

      //
      // ServiceParameter
      //
      if (element.equals("Parameter")) {
        clearObject();
      }

      //
      // ClientResponse
      //
      if (element.equals("ClientResponse")) {
        //
        // Must clear object xml serialzer data structures
        //
        clear();
      }
      if (element.equals("RequiredSoftwareUpdates")) {
        clearObject();
      }
      if (element.equals("Exception")) {
        clearObject();
      }
      if (element.equals("Warning")) {
        clearObject();
      }
      if (element.equals("ReturnValue")) {
        clearObject();
      }
    } catch (Exception e) {
      throw new SAXException(e);
    }

  }

  /**
   * Handles characters between start and end tags.  Remember,
   * in a SAX parser, characters between tags may be buffered and so
   * not sent all at once but in sections.
   * @param chars the characters
   * @param start the start position
   * @param length the length
   */
  public void characters(char[] chars, int start, int length) {
    if (active.contains("Object") || active.contains("Var")) {
      super.characters(chars, start, length);
    } else {
      String s = new String(chars, start, length);
      chars_data = chars_data.concat(s);
    }
  }

  /**
   * Handles end tags.
   * @param namespace_uri the namespace
   * @param s_name the simple tag name
   * @param q_name the qualified name of the tag
   * @throws SAXException if the end tag cannot be handled
   */
  public void endElement(String namespace_uri,
                         String s_name, // simple name
                         String q_name // qualified name
                         ) throws SAXException {

    //
    // Obtain element name
    //
    String element = s_name;
    if ("".equals(element)) {
      element = q_name;

    }
    try {
      if (element.equals("Object") || element.equals("Var")) {
        super.endElement(namespace_uri, s_name, q_name);
      }
      if (element.equals("Username")) {
        if (active.contains("Authentication") &&
            "user:password".equals(elements.get("mode"))) {
          elements.put("Username", chars_data);
        }
      }
      if (element.equals("Password")) {
        if (active.contains("Authentication") &&
            "user:password".equals(elements.get("mode"))) {
          String password = chars_data;
          Authentication authentication = new PasswordAuthentication
              ( (String) elements.get("Username"), password.toCharArray());
          service_request.setAuthentication(authentication);
        }
      }

      //
      // SoftwareVersion
      //
      if (element.equals("SoftwareVersions")) {
        ComponentMetaData[] svCMD = (ComponentMetaData[]) getObject();
        for (int k = 0; k < svCMD.length; k++) {
          service_request.addCurrentSoftware(svCMD[k]);
        }
      }

      //
      // ServiceParameter
      //
      if (element.equals("Service")) {
        service_request.setService(chars_data);
      }
      if (element.equals("Parameter")) {
        Parameter[] parameter = (Parameter[]) getObject();
        for (int k = 0; k < parameter.length; k++) {
          service_request.addParameter(parameter[k]);
        }
      }

      //
      // ClientResponse
      //
      if (element.equals("RequiredSoftwareUpdates")) {
        ComponentMetaData[] rsCMD = (ComponentMetaData[]) getObject();
        for (int k = 0; k < rsCMD.length; k++) {
          service_request.addRequiredSoftware(rsCMD[k]);
        }
      }

      //
      // Exceptions and warnings
      //
      if (element.equals("Exception")) {
        Exception[] exception = (Exception[]) getObject();
        for (int k = 0; k < exception.length; k++) {
          service_request.addException(exception[k]);
        }
      }
      if (element.equals("Warning")) {
        Warning[] warning = (Warning[]) getObject();
        for (int k = 0; k < warning.length; k++) {
          service_request.addWarning(warning[k]);
        }
      }

      //
      // Return values
      //
      if (element.equals("ReturnValue")) {
        Parameter[] parameter = (Parameter[]) getObject();
        for (int k = 0; k < parameter.length; k++) {
          service_request.addReturnValue(parameter[k]);
        }
      }
      //
      // Unmark element as active
      //
      active.remove(element);
    } catch (Exception e) {
      throw new SAXException(e);
    }

  }

}
