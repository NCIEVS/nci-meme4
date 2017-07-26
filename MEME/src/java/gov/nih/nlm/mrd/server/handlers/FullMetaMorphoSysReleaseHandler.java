/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMetaMorphoSysReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerConstants;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.swing.SwingToolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handler for "MetaMorphoSys" target.
 *
 * @author Tun Tun Naing, Brian Carlsen
 */
public class FullMetaMorphoSysReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates {@link FullMetaMorphoSysReleaseHandler}.
   */
  public FullMetaMorphoSysReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Cleans up and copies current mmsys.zip into place.
   * @throws MEMEException if failed to prepare
   */
  public void prepare() throws MEMEException {
    try {
      String mrd_home = ServerToolkit.getProperty(ServerConstants.MRD_HOME);
      ServerToolkit.logCommentToBuffer("Copying zip file to " +
                                       release.getBuildUri(), true, log);
      ServerToolkit.exec(new String[] {
                         "/bin/cp", "-f", mrd_home + "/mmsys.zip",
                         release.getBuildUri()}
                         ,
                         new String[] {"MRD_HOME=" + mrd_home});
      ServerToolkit.logCommentToBuffer("Cleaning up " + release.getBuildUri() +
                                       "/MMSYS", true, log);
      ServerToolkit.exec(new String[] {
                         "/bin/rm", "-r", "-f",
                         release.getBuildUri() + "/MMSYS"});

      ServerToolkit.logCommentToBuffer("Unzipping " + release.getBuildUri() +
                                       "/mmsys.zip", true, log);
      ServerToolkit.exec(new String[] {
                         "unzip", "-o", "-d", release.getBuildUri(),
                         release.getBuildUri() + "/mmsys.zip"});
      ServerToolkit.logCommentToBuffer("Deleting " + release.getBuildUri() +
                                       "/mmsys.zip", true, log);
      File file = new File(release.getBuildUri() + "/mmsys.zip");
      file.delete();
    } catch (ExecException exece) {
      throw exece;
    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to prepare the release data", this);
      dev.setEnclosedException(e);
      throw dev;
    }
  }

  /**
   * Builds current config files and incorporates into new mmsys.zip
   * @throws MEMEException if failed to generate.
   */
  public void generate() throws MEMEException {
    try {
      SwingToolkit.setProperty(SwingToolkit.VIEW,"false");
      String mrd_home = ServerToolkit.getProperty(ServerConstants.MRD_HOME);
      String oracle_home = ServerToolkit.getProperty(ServerConstants.
          ORACLE_HOME);
      SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
      ServerToolkit.logCommentToBuffer("Making config file " +
                                       dateformat.format(release.getReleaseDate()) +
                                       " " +
                                       release.getName() + " " +
                                       data_source.getDataSourceName(), true,
                                       log);
      ServerToolkit.exec(new String[] {mrd_home + "/bin/make_config.csh",
                         dateformat.format(release.getReleaseDate()),
                         release.getDescription(),
                         release.getName(),
                         data_source.getDataSourceName(),
                         release.getBuildUri() + "/META",
                         release.getBuildUri() +"/MMSYS"},
                         new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META",
                         "MMSYS_DIR=" + release.getBuildUri() + "/MMSYS",
                         "ORACLE_HOME=" + oracle_home}
                         ,
                         new File(mrd_home + File.separator + "bin"));
     /* ServerToolkit.logCommentToBuffer("Copying properties file", true, log);
      File file = new File(mrd_home + "/bin/mmsys.a.prop");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/mmsys.a.prop"));
      file = new File(mrd_home + "/bin/mmsys.b.prop");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/mmsys.b.prop"));
      file = new File(mrd_home + "/bin/mmsys.c.prop");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/mmsys.c.prop"));
      file = new File(mrd_home + "/bin/mmsys.prop.sav");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/mmsys.prop.sav"));
      ServerToolkit.logCommentToBuffer("Copying mrcolsfiles", true, log);
      file = new File(mrd_home + "/bin/mrcolsfiles.dat");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/mrcolsfiles.dat"));
      ServerToolkit.logCommentToBuffer("Copying mrpluscolsfiles", true, log);
      file = new File(mrd_home + "/bin/mrpluscolsfiles.dat");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/mrpluscolsfiles.dat"));
      ServerToolkit.logCommentToBuffer("Copying types", true, log);
      file = new File(mrd_home + "/bin/att_types.dat");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/att_types.dat"));
      file = new File(mrd_home + "/bin/rel_types.dat");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/rel_types.dat"));
      file = new File(mrd_home + "/bin/snomed_rela_map.dat");
      file.renameTo(new File(release.getBuildUri() +
                             "/MMSYS/config/snomed_rela_map.dat"));
*/                             
      ServerToolkit.logCommentToBuffer("Creating " + release.getBuildUri() +
                                       "/release.dat", true, log);
      File file = new File(release.getBuildUri() + "/release.dat");
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      writer.println("umls.release.name=" + release.getName());
      writer.println("umls.release.description=" + release.getDescription());
      writer.println("umls.release.date=" +
                     dateformat.format(release.getReleaseDate()));
      writer.println("umls.lvg.version=" + ServerToolkit.getLVGVersion());
      File input_file = new File(release.getBuildUri() +
                                 "/MMSYS/config/timestamp.dat");
      BufferedReader input = new BufferedReader(new FileReader(input_file));
      String tempStr;
      if ( (tempStr = input.readLine()) != null) {
        writer.println("mmsys.build.date=" + tempStr);
      }
      input.close();
      input_file = new File(release.getBuildUri() + "/MMSYS/config/version.dat");
      input = new BufferedReader(new FileReader(input_file));
      if ( (tempStr = input.readLine()) != null) {
        writer.println("mmsys.version=" + tempStr);
      }
      input.close();
      writer.close();
      ServerToolkit.logCommentToBuffer("Creating zip file", true, log);
      ServerToolkit.exec(new String[] {
                         "zip", "-r", "mmsys.zip", "MMSYS"}
                         , new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META"}
                         ,
                         new File(release.getBuildUri()));
      ServerToolkit.exec(new String[] {
                         "zip", "-r", "mmsys.zip", "linux_mmsys.sh",
                         "solaris_mmsys.sh",
                         "macintosh_mmsys.sh", "macintosh_mmsys.command"}
                         , new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META"}
                         ,
                         new File(release.getBuildUri()));
      ServerToolkit.exec(new String[] {
                         "zip", "-r", "mmsys.zip", "linux_browser.sh",
                         "solaris_browser.sh",
                         "macintosh_browser.sh", "macintosh_browser.command"}
                         , new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META"}
                         ,
                         new File(release.getBuildUri()));
      ServerToolkit.exec(new String[] {
                         "zip", "-r", "mmsys.zip", "linux_mrcxt_builder.sh",
                         "solaris_mrcxt_builder.sh",
                         "macintosh_mrcxt_builder.sh",
                         "macintosh_mrcxt_builder.command"}
                         , new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META"}
                         ,
                         new File(release.getBuildUri()));
      ServerToolkit.exec(new String[] {
                         "zip", "-r", "mmsys.zip", "windows_mmsys.bat",
                         "windows_browser.bat",
                         "windows_mrcxt_builder.bat"}
                         , new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META"}
                         ,
                         new File(release.getBuildUri()));
      ServerToolkit.exec(new String[] {
                         "zip", "-r", "mmsys.zip", "release.dat"}
                         , new String[] {"MRD_HOME=" + mrd_home,
                         "META_RELEASE=" + release.getBuildUri() + "/META"}
                         ,
                         new File(release.getBuildUri()));
    } catch (ExecException exece) {
      throw exece;
    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to generate the release data", this);
      dev.setEnclosedException(e);
      throw dev;
    }
  }

  /**
   * Publishes mmsys.zip to "release Host/URI".
   * @return flag indicating whether or not publish was successful
   * @throws MEMEException if anything goes wrong
   */
  public boolean publish() throws MEMEException {
    String target_name = "MetaMorphoSys";
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    String date = dateformat.format(new Date());
    try {
      ServerToolkit.exec(
          new String[] {
          "/bin/rcp", release.getBuildUri() + "/mmsys.zip",
          release.getReleaseHost() + ":" +
          release.getReleaseUri() + "/MASTER/" + "mmsys." + date +
          ".zip"}
          ,
          new String[] {}
          ,
          true,
          ServerConstants.USE_INPUT_STREAM,
          false);
      ServerToolkit.exec(
          new String[] {
          "/bin/rsh", release.getReleaseHost(),
          "/bin/rm -f " + release.getReleaseUri() +
          "/MASTER/mmsys.zip"}
          ,
          new String[] {}
          ,
          true,
          ServerConstants.USE_INPUT_STREAM,
          false);
      ServerToolkit.exec(
          new String[] {
          "/bin/rsh", release.getReleaseHost(),
          "/bin/ln -s " + release.getReleaseUri() + "/MASTER/mmsys." +
          date + ".zip " + release.getReleaseUri() +
          "/MASTER/mmsys.zip"}
          ,
          new String[] {}
          ,
          true,
          ServerConstants.USE_INPUT_STREAM,
          false);
      String local_digest =
          localDigest(new File(release.getBuildUri(), "mmsys.zip").
                      getPath());
      ServerToolkit.logCommentToBuffer(
          "MD5 digest " + release.getBuildUri() + target_name +
          " - " + local_digest, true, log);
      String remote_digest =
          remoteDigest(release.getReleaseUri() + "/MASTER/mmsys.zip",
                       release.getReleaseHost());

      ServerToolkit.logCommentToBuffer(
          "MD5 digest " + release.getReleaseUri() + "/MASTER/" +
          target_name +
          " - " + remote_digest, true, log);

      if (!local_digest.equals(remote_digest)) {
        throw new Exception("Remote digest does not match local digest.");
      }
    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to publish the target", this);
      dev.setEnclosedException(e);
      throw dev;
    }
    return true;
  }

  /**
   * Returns the list of files.
   * @return the list of files
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {};
  }
}
