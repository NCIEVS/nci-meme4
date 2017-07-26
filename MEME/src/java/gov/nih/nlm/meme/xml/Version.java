/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  Version
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <b>History:</b>
 * <dl>
 * <dt>08/15/2005:</dt>
 * <dd>Better handling of wrapper classes.  This will help support jdk1.5.
 * </dd>
 * <dt>06/17/2005:</dt>
 * <dd>Updated javadoc comments.
 * </dd>
 * <dt>01/25/2005:</dt>
 * <dd>{@link MedlineHandler} only prints "PMID not found" messages for records
 * that are not being ignored. {@link ObjectXMLSerializer} updated to serialize
 * Properties and Hashtable objects correctly.  Formatting was standardized and
 * javadocs were checked.
 * </dd>
 * <dt>01/25/2005:</dt>
 * <dd>
 * {@link MASRequestSerializer} now supports "reconnect requests".  It has
 * a <code>copy</code> method for turning one request into another.
 * {@link MedlineHandler} uses patterns for massaging poorly formatted dates.
 * Data comes from <code>meme_properties</code>
 * {@link ObjectXMLSerializer} does a better job with fields that
 * used to be there.
 * </dd>
 * <dt>12/13/2004:</dt>
 * <dd>{@link ObjectXMLSerializer} has more sophisticated mechanism for
 * tracking primitives.
 * </dd>
 * <dt>11/03/2004:</dt>
 * <dd>Minor documentation changes.
 *
 *     {@link MedlineHandler} updated to handle new dates found in
 *     the MEDLINE info.
 *
 * </dd>
 * <dt>07/09/2004:</dt>
 * <dd>{@link MedlineHandler} handles additional bad dates in the PubDate field
 * of the medline XML docs.
 * </dd>
 * <dt>03/17/2004:</dt>
     *           <dd>{@link ObjectXMLSerializer} bug fix to handle special characters
 *           in Var tags.  Also the serializer now handles java.sql.Date and no
 *           longer treats NativeIdentifier as a primitive.
 *           </dd>
 * <dt>02/06/2004:</dt>
 *           <dd>{@link MASRequestSerializer} handles SAXExceptions better.
 *           {@link ObjectXMLSerializer} was overhauled to more consistently
 *           deal with classes we want to treat as primitives.  We also added
 *           support for treating native identifiers as primitives.  A great
 *           deal of redundant (or useless) code was removed.
 *           </dd>
 * <dt>01/20/2004:</dt>
 *           <dd>{@link MedlineHandler} employs a more aggressive algorithm
 *           for matching MSH main heading string in the Medline files.
 *           </dd>
 * <dt>11/07/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} will not allow a field of
 *           a class called "id" to be set by the id attribute in an Object tag
 *           (same for idref, name, and length).  This is a bug fix.
 *           </dd>
 * <dt>08/26/2003:</dt>
 *           <dd>{@link MedlineHandler} supports UTF-8.
 *           {@link ObjectXMLSerializer} ignores the perThreadBuffer
 *           variable of the java 1.4.2 Integer class for parsing.
 *           </dd>
 * <dt>08/01/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} was updated to deal with a problem
 *           in serializing exceptions having to do with not being able to
 *           find valid constructors for interface types.
 *           </dd>
 * <dt>06/19/2003:</dt>
 *           <dd>{@link MedlineHandler} has still more additional improperly
 *           formatted date handlers.
 *           </dd>
 * <dt>06/06/2003:</dt>
 *           <dd>
 *           {@link MedlineHandler} handles additional improperly formatted
 *           dates. {@link MEMEServiceRequest} now tracks the writer from
     *           the socket, allowing services to directly write their own responses.
     *           {@link CGIStyleMEMEServiceRequest} supports multiple CGI parameters
 *           with the same name (as a String[] parameter).
 *           </dd>
 * <dt>05/21/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} reports better details when
 *           throwing reflection exceptions.
 *           </dd>
 * <dt>05/16/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} was updated to report better
 *           exceptions when classes cannot be instantiated.
 *           </dd>
 * <dt>05/09/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} now deals with '\r'.
 *           {@link CGIStyleMEMEServiceRequest} supports multiple CGI params
 *           with the same name (important for graphing application).
 *           </dd>
 * <dt>04/10/2003:</dt>
     *           <dd>{@link ObjectXMLSerializer} now properly handles tab characters
 *           within attributes by converting them to &#x9;.
 *           </dd>
 * <dt>04/01/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} was updated slightly to provide
 *           better error messages. {@link MASRequestSerializer} clears
 *           the {@link ObjectXMLSerializer} data structures between the
 *           Server and Client sections of the document.  This is necessary
 *           because there are two distinct &lt;Object&gt; documents
 *           within the &lt;MASRequest&gt; document itself.
 *           {@link CGIStyleMEMEServiceRequest} was given get/set methods
 *           to provide direct access to the requests socket
 *           output stream.  This allows cgi sytle requests to
 *           directly return binary data, like images.
 *           </dd>
 * <dt>03/19/2003:</dt>
 *           <dd>{@link ObjectXMLSerializer} made more efficient by expanding
 *           what is treated as "primtive", since primitives are written out as
 *           tag attributes instead of sub-Object tags.  We now consider a certain
 *           set of common objects to be <i>primitive</i> as well as
 *           all idrefs.
 *           </dd>
 * <dt>11/13/2002:</dt>
 *           <dd>Small changes to the MedlineHandler to deal with the
 *                NLM vs NLM-MED problem.  Also, we discovered a problem
 *                with respect to synchronizing actions to the MRD.  The
 *                operations_queue table contains XML fragments that may have
 *                CDATA sections within.  These document fragments are placed
 *                within larger XML documents (in CDATA sections) if an action is fully read
 *                and then transported via the {@link MEMEServiceRequest}
 *                mechanism.  I believe this problem can be worked around
 *                with the use of {@link gov.nih.nlm.util.XMLEntityEncoder}
 *                on the inner block with CDATA fragments.
 *           </dd>
 * <dt>08/28/2002:</dt>
 *           <dd>The {@link MASRequestSerializer} and
 *               {@link ObjectXMLSerializer} were upgraded to support
 *               SAX parsing instead of DOM parsing. This version also
 *               brings with it the addition of {@link MedlineHandler}.
 *           </dd>
 * </dl>
 *
 * @author  MEME Group
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 4;
  private final static double version = 0.0;
  private final static String version_authority = "MEME Group";
  private final static String version_date = "28-Aug-2002 00:00:00";
  private final static String package_name = "gov.nih.nlm.meme.xml";

  //
  // Methods
  //

  /**
   * Returns the package current release.
   * @return An <code>int</code> representation of package current release.
   */
  public static int getRelease() {
    return release;
  }

  /**
   * Returns the package current version.
   * @return A <code>double</code> representation of package current version.
   */
  public static double getVersion() {
    return version;
  }

  /**
   * Returns the package current version authority.
   * @return An object {@link String} representation of package current version
   * authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * @return An object {@link Date} representation of package current version
   * date.
   */
  public static Date getDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    Date date = null;
    try {
      date = formatter.parse(version_date);
    } catch (Exception e) {
      BadValueException bve = new BadValueException(
          "Badly formatted version date.");
      bve.setDetail("version_date", version_date);
      MEMEToolkit.handleError(bve);
    }
    return date;
  }

  /**
   * Returns the package current package name.
   * @return An object {@link String} representation of package current name.
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * @return An object {@link String} representation of package current
   * version information.
   */
  public static String getVersionInformation() {
    return getPackage() + ": " + getRelease() + "." + getVersion() + ", "
        + getDate() + " (" + getAuthority() + ")";
  }

  //
  // Main
  //

  /**
   * This can be used to print package version information
   * on the command line.
   * @param argv An array of argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.xml.Version.getVersionInformation());
  }
}
