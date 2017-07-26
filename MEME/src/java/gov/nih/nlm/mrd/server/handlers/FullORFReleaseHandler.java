/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRXReleaseHandler.java
 *
 * Changes:
 *   02/03/2006 BAC (1-74H2V): Call MMSToolkit.fakeMRDOCSuppressRows().  Fixes
 *      problem where output MRRANK has SUPPRESS alwasys set to N
 *  
 ***********************************************************************/
package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.swing.SwingToolkit;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * Handler for "ORF" target.
 *
 * @author TTN, BAC
 */
public class FullORFReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullORFReleaseHandler}.
   */
  public FullORFReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Cleans up "Build URI" METAO directory and re-creates it.
   * @throws MEMEException if anything goes wrong
   */
  public void prepare() throws MEMEException {
    try {
      ServerToolkit.logCommentToBuffer("Prepare METAO directory", true, log);
      File file = new File(release.getBuildUri() + File.separator + "METAO");
      if (file.exists()) {
        ServerToolkit.exec(new String[] {"/bin/rm", "-r", "-f",
                           release.getBuildUri() + File.separator + "METAO"});
      }
      file.mkdir();
    } catch (ExecException exece) {
      throw exece;
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to make the output folder", e);
      throw ere;
    }
  }

  /**
   * Uses Metamorphosys to generate a "keep everything" ORF subset of the
   * entire set of release files in "Build URI" METAO directory.
   * @throws MEMEException if anything goes wrong
   */
  public void generate() throws MEMEException {
    SwingToolkit.setProperty(SwingToolkit.VIEW,"false");
    File ap_config = new File(release.getBuildUri() +
                              "/MMSYS/config/mmsys.a.prop");
    File user_config = new File(release.getBuildUri() +
                                "/MMSYS/config/mmsys.a.prop");
    try {
      ServerToolkit.logCommentToBuffer(
          "Configure properties objects for MMSYS run", true, log);
      Properties ap = new Properties();
      ap.load(new FileInputStream(ap_config));
      Properties up = new Properties();
      up.load(new FileInputStream(user_config));

      //
      // Override user configuration settings
      //

      // Output handler
      up.setProperty("mmsys_output_stream",
                     "gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream");

      // Input handler
      up.setProperty("mmsys_input_stream",
                     "gov.nih.nlm.mms.RichMRMetamorphoSysInputStream");

      // keep all sources
      up.setProperty(
          "gov.nih.nlm.mms.filters.SourcesToRemoveFilter.remove_selected_sources",
          "true");
      up.setProperty(
          "gov.nih.nlm.mms.filters.SourcesToRemoveFilter.selected_sources", "");
      up.setProperty(
          "gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.remove_utf8",
          "false");

      ServerToolkit.logCommentToBuffer("Load MMSYS classes", true, log);
      URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {new URL(
          "file:" + release.getBuildUri() +
          "/MMSYS/lib/mms.jar"), new URL(
          "file:" + release.getBuildUri() +
          "/MMSYS/"),
          new URL("file:" + release.getBuildUri() +
                  "/MMSYS/lib/objects.jar")});
      ServerToolkit.logCommentToBuffer("Set ApplicationConfiguration", true,
                                       log);
      Class cl = ucl.loadClass("gov.nih.nlm.mms.ApplicationConfiguration");
      Constructor constr = cl.getConstructor(new Class[] {ap.getClass()});
      Object ac = constr.newInstance(new Object[] {ap});
      String path = release.getBuildUri() + "/MMSYS/config";
      cl.getMethod("setConfigDirectory", new Class[] {String.class}).invoke(ac,
          new Object[] {path});

      ServerToolkit.logCommentToBuffer("Set UserConfiguration", true, log);
      cl = ucl.loadClass("gov.nih.nlm.mms.UserConfiguration");
      constr = cl.getConstructor(new Class[] {up.getClass()});
      Object uc = constr.newInstance(new Object[] {up});
      path = release.getBuildUri() + "/META";
      cl.getMethod("setSourcePaths", new Class[] {String[].class}).invoke(uc,
          new Object[] {new String[] {path}
      });
      path = release.getBuildUri() + "/METAO";
      cl.getMethod("setSubsetDirectory", new Class[] {String.class}).invoke(uc,
          new Object[] {path});

      ServerToolkit.logCommentToBuffer(
          "Set release.dat file location in MMSToolkit", true, log);
      cl = ucl.loadClass("gov.nih.nlm.mms.MMSToolkit");
      path = release.getBuildUri() + "/release.dat";
      cl.getMethod("setReleaseDatLocation", new Class[] {String.class}).invoke(null,
          new Object[] {path});
      cl.getMethod("fakeMRDOCSuppressRows", new Class[0]).invoke(null,
          new Object[0]);

      ServerToolkit.logCommentToBuffer("Set MetamorphoSys", true, log);
      cl = ucl.loadClass("gov.nih.nlm.mms.MetamorphoSys");
      constr = cl.getConstructor(new Class[] {ac.getClass(), uc.getClass()});
      Object mms = constr.newInstance(new Object[] {ac, uc});
      cl.getMethod("initializeConfigurables", null).invoke(mms, null);
      ServerToolkit.logCommentToBuffer("Begin Subsetting Operation", true, log);
      PrintStream out = System.out;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintStream stdout = new PrintStream(bos);
      System.setOut(stdout);
      cl.getMethod("subset", null).invoke(mms, null);
      System.setOut(out);
      stdout.close();
      ServerToolkit.logCommentToBuffer(bos.toString(),false,log);
      ServerToolkit.logCommentToBuffer("Finished Subsetting Operation", true,
                                       log);

    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to generate the release data", this);
      dev.setEnclosedException(e);
      throw dev;
    }
  }

  /**
   * Handles publishing of target files, which involves doing nothing.
   * @throws MEMEException if failed to publish
   */

  public boolean publish() throws MEMEException {
    return true;
  }

  /**
   * Returns empty file list.
   * @return empty file list
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {};
  }
}
