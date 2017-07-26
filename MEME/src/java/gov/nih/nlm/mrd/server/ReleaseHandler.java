/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server
 * Object:      ReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Generically represents a handler for generating a release file.
 * @author Stephanie Halbeisen
 */
public interface ReleaseHandler extends RegisteredHandler {

  /**
   * Takes the data stored in the different MRD tables and treats then in a
   * way that they are close to some MR file format. This method gets only
   * implemented if necessary, that is, if there are no MRD tables containing
   * the MR data directly.
   * @throws MEMEException if failed to prepare
   */
  void prepare() throws MEMEException;

  /**
   * Writes one or more release files.
   * @throws MEMEException if failed to generate
   */
  void generate() throws MEMEException;

  /**
   * Feeds back information about the release to the MID (<i>no longer used</i>).
   * Until now, no relase handler is implementing this method.
   * @throws MEMEException if failed to feedback
   */
  void feedback() throws MEMEException;

  /**
   * Returns the first n lines of the target file (for example: head -100 MRCON)
   * @param lines the number of lines
   * @return the target
   * @throws MEMEException if failed to feedback
   */
  String preview(int lines) throws MEMEException;

  /**
   * Publishes the target files.
   * @return a flag indicating whether or not the target was successfully published
   * @throws MEMEException if failed to feedback
   */
  boolean publish() throws MEMEException;

  /**
   * Returns the list of files.
   * @return the list of files
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException;

  /**
   * Returns target name.
   * @return target name
   */
  public String getTargetName();

  /**
   * Sets the {@link MRDDataSource}.
   * @param data_source the {@link MRDDataSource}
   */
  public void setDataSource(MRDDataSource data_source);

  /**
   * Sets the {@link ReleaseInfo}.
   * @param release the {@link ReleaseInfo}
   */
  public void setReleaseInfo(ReleaseInfo release);

  /**
   * Sets the log buffer.
   * @param log the log buffer
   */
  void setLog(StringBuffer log);

  /**
   * Default implementation.
   */
  abstract class Default extends RegisteredHandler.Default implements
      ReleaseHandler {

    //Fields
    protected StringBuffer log;
    protected MRDDataSource data_source = null;
    protected ReleaseInfo release = null;

    /**
     * Empty default implementation.
     * @throws MEMEException if failed to prepare
     */
    public void prepare() throws MEMEException { }

    /**
     * Empty default implementation.
     * @throws MEMEException if failed to generate.
     */
    public void generate() throws MEMEException { }

    /**
     * Empty default implementation.
     * @throws MEMEException if failed to feedback
     */
    public void feedback() throws MEMEException {
    }

    /**
     * Returns null.
     * @param lines the number of lines
     * @return null
     * @throws MEMEException if failed to preview
     */
    public String preview(int lines) throws MEMEException {
      return null;
    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     * @throws MEMEException if failed to publish
     */
    public boolean publish() throws MEMEException {
      return false;
    }

    /**
     * Returns the target name.
     * @return the target name
     * @throws MEMEException if failed to feedback
     */
    public String[] getFiles() throws MEMEException {
      return new String[] {getTargetName()};
    }

    /**
     * Returns the target name, assumes that the class name is something
     * like "FullXXXXReleaseHandler". This allows subclasses to not bother
     * implementing this method if they are properly named.
     * @return the target name
     */
    public String getTargetName() {
      String name = getClass().getName();
      return name.substring(name.indexOf("Full") + 4,
                            name.indexOf("ReleaseHandler"));
    }

    /**
     * Sets the log buffer.
     * @param log the log buffer
     */
    public void setLog(StringBuffer log) {
      this.log = log;
    }

    /**
     * Sets the {@link MRDDataSource}.
     * @param data_source the {@link MRDDataSource}
     */
    public void setDataSource(MRDDataSource data_source) {
      this.data_source = data_source;
    }

    /**
     * Sets the {@link ReleaseInfo}.
     * @param release the {@link ReleaseInfo}
     */
    public void setReleaseInfo(ReleaseInfo release) {
      this.release = release;
    }

    /**
     * Drops the {@link MRDDataSource} table with the specified table name.
     * @param table_name the table name
     * @throws DataSourceException if failed to drop the table
     */
    protected void dropTable(String table_name) throws DataSourceException {
      StringBuffer call = new StringBuffer();
      try {
        call.append("{call MEME_UTILITY.drop_it(type => 'table', name => '");
        call.append(table_name);
        call.append("')}");
        CallableStatement drop = data_source.prepareCall(call.toString());
        drop.execute();
        drop.close();
      } catch (SQLException e) {
        DataSourceException me =
            new DataSourceException("Failed to drop table.", this, e);
        me.setDetail("statement", call.toString());
        throw me;
      }
    }

    /**
     * Computes an MD5 digest for a local file.
     * @param path the path
     * @return the digest
     * @throws ExecException if computation fails
     */
    protected static String localDigest(String path) throws ExecException {
      String digest = ServerToolkit.exec(
          new String[] {
          "/bin/sh", "-c", "/bin/cat " + path + " | " + ServerToolkit.getProperty("env.PATH_TO_MD5")}
          ,
          new String[] {}
          ,
          true,
          ServerConstants.USE_INPUT_STREAM,
          false);
      return " " + digest;
    }

    /**
     * Computes an MD5 digest for a remote file.
     * @param path the path
     * @param remotehost the remote host
     * @return the digest
     * @throws ExecException if computation fails
     */
    protected static String remoteDigest(String path, String remotehost) throws
        ExecException {

      String digest = ServerToolkit.exec(
          // PATH_TO_MD5 is converted by build script
          new String[] {
          "/bin/rsh", remotehost,
          "/bin/cat " + path + " | " + ServerToolkit.getProperty("env.PATH_TO_MD5")}
          ,
          new String[] {}
          ,
          true,
          ServerConstants.USE_INPUT_STREAM,
          false);
      return " " + digest;
    }
  }
}