/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ComponentMetaData
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * <B>NOT USED</B>
 *
 * Represents a releasable software component.  The {@link
 * <a href="../xml/MEMEServiceRequest.html">
 * <code>MEMEServiceRequest</code></a>} object has fields for current versions
 * of software components and required upgrades.  If the
 * ComponentMetaData represents a current version of a releasable component,
 * the (name, release, and version) fields will have values.  If it represents
 * a required upgrade, the (name, server, username, directory, file, and
 * password) fields will have values. In this case, the object will contain
 * all the information necessary for a client application to download the
 * required components and install them.
 *
 * @author MEME Group
 */

public class ComponentMetaData {

  //
  // Fields
  //

  private String name = "";
  private int release = 1;
  private double version = 1.0;
  private String server, username, directory, file = "";
  private char[] password = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link ComponentMetaData}.
   */
  public ComponentMetaData() {};

  /**
   * Instantiates a {@link ComponentMetaData} with the specified
   * name and version information.
   * @param name the name of a releasable software component
   * @param release an <code>int</code> representation of MEME release that
   * this software component belongs to
   * @param version A <code>double</code> representation of version (within
   * the release) of this component
   */
  public ComponentMetaData(String name, int release, double version) {
    this.name = name;
    this.release = release;
    this.version = version;
  }

  /**
   * Instantiates a {@link ComponentMetaData} with the specified
   * information.
   * This constructor is used to represent required software upgrades and
   * provide enough information to a client application to download the
   * necessary component.
   * @param name the name of a releasable software component.
   * @param server the domain name/IP number of an FTP server where the component is located.
   * @param username the FTP username
   * @param password the FTP password as a <code>char[]</code>
   * @param directory the remote directory containing the component
   * @param file the remote filename containing the releasable component
   */
  public ComponentMetaData(String name, String server, String username,
                           char[] password, String directory, String file) {
    this.name = name;
    this.server = server;
    this.username = username;
    this.password = password;
    this.directory = directory;
    this.file = file;
  }

  //
  // Methods
  //

  /**
   * Returns the releasable software component name.
   * @return the component name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the component name.
   * @param name the component name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the MEME release number.
   * @return an <code>int</code> representation of the MEME release number
   */
  public int getRelease() {
    return release;
  }

  /**
   * Sets the MEME release number.
       * @param release an <code>int</code> representation of the MEME release number
   */
  public void setRelease(int release) {
    this.release = release;
  }

  /**
   * Returns the component version number.
   * @return a <code>double</code> representation of the version number
   */
  public double getVersion() {
    return version;
  }

  /**
   * Sets the component version number.
   * @param version a <code>double</code> representation of version number
   */
  public void setVersion(double version) {
    this.version = version;
  }

  /**
   * Returns the server (domain name or IP number) containing the component.
   * @return the server containing the component
   */
  public String getServer() {
    return server;
  }

  /**
   * Sets the server name.
   * @param server the domain name/IP number of the FTP server
   * containing this component.
   */
  public void setServer(String server) {
    this.server = server;
  }

  /**
   * Returns the FTP username.
   * @return the FTP username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the FTP username.
   * @param username the FTP username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Returns the remote directory containing the component.
   * @return the remote directory containing the component
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Sets the remote directory.
   * @param directory the remote directory containing the component
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Returns the remote filename of the component.
   * @return the remote filename of the component
   */
  public String getFile() {
    return file;
  }

  /**
   * Sets the remote filename.
   * @param file the remote filename of the component
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * Returns the FTP password.
   * @return the FTP password as a <code>char[]</code>
   */
  public char[] getPassword() {
    return (char[]) password;
  }

  /**
   * Sets the FTP password.
   * @param password the FTP password as a <code>char[]</code>
   */
  public void setPassword(char[] password) {
    this.password = password;
  }

  /**
   * Clears the password.
   */
  public void clearPassword() {
    for (int i = 0; i < password.length; i++) {
      password[i] = (char) 0;
    }
  }

}
