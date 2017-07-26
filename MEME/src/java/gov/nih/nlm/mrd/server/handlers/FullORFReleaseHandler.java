/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRXReleaseHandler.java
 *
 * Changes:
 *   02/03/2006 BAC (1-74H2V): Call MMSToolkit.fakeMRDOCSuppressRows().  Fixes
 *      problem where output MRRANK has SUPPRESS alwasys set to N
 *    07/12/2006 SL (1-BNKYE) Commenting out the fakeMRDOCSuppressRows call
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
import java.io.FileOutputStream;
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
    try {
      ServerToolkit.logCommentToBuffer(
          "Configure properties objects for MMSYS run", true, log);

			//
			// Override user configuration settings
			//
			ServerToolkit.logCommentToBuffer(
					"Configure properties objects for MMSYS run", true, log);
			Properties subsetConfig = new Properties();
			subsetConfig.load(new FileInputStream(new File(new File(new File(
					new File(release.getBuildUri(), "MMSYS"), "config"), release
					.getName()), "user.a.prop")));
			subsetConfig.setProperty("mmsys_output_stream",
					"gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream");
			subsetConfig.setProperty("mmsys_input_stream",
					"gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysInputStream");
			// keep all sources, assume default config for all other filters
			subsetConfig
					.setProperty(
							"gov.nih.nlm.umls.mmsys.filter.SourceListFilter.remove_selected_sources",
							"true");
			subsetConfig
					.setProperty(
							"gov.nih.nlm.umls.mmsys.filter.SourceListFilter.selected_sources",
							"");
			// re-write config file
			subsetConfig.store(new FileOutputStream(new File(new File(release
					.getBuildUri(), "log"), "mmsys.prop")), "MRD configuration");

			// To run MetamorphoSys in batch subsetting mode,
			// it is now way easier to just invoke Java directly.
			// MRD does not use plugin framework so configuring it would be difficult
			// TODO: shouldn't hardcode "solaris" in the java command
      ServerToolkit.logCommentToBuffer("Begin Subsetting Operation", true, log);
      try {
      ServerToolkit.exec(new String[] {
					release.getBuildUri() + "/MMSYS/jre/solaris/bin/java",
					"-Djava.awt.headless=true",
					"-Djpf.boot.config=" + release.getBuildUri()
							+ "/MMSYS/etc/subset.boot.properties",
					"-Dlog4j.configuration=etc/subset.log4j.properties",
					"-Dscript_type=.sh", "-Dfile.encoding=UTF-8", "-Xms600M",
					"-Xmx1400M", "-Dinput.uri=" + release.getBuildUri() + "/META",
					"-Doutput.uri=" + release.getBuildUri() + "/METAO",
					"-Dmmsys.config.uri=" + release.getBuildUri() + "/log/mmsys.prop",
					"org.java.plugin.boot.Boot"
			}, new String[] {
					"CLASSPATH=" + release.getBuildUri() + "/MMSYS:"
							+ release.getBuildUri() + "/MMSYS/lib/jpf-boot.jar"
			}, new File(release.getBuildUri(), "/MMSYS"));
      ServerToolkit.logCommentToBuffer("Finished Subsetting Operation", true,
          log);
      } catch (ExecException exece) {
				// If fails as solaris, try as linux
        ServerToolkit.exec(new String[] {
  					release.getBuildUri() + "/MMSYS/jre/linux/bin/java",
  					"-Djava.awt.headless=true",
  					"-Djpf.boot.config=" + release.getBuildUri()
  							+ "/MMSYS/etc/subset.boot.properties",
  					"-Dlog4j.configuration=etc/subset.log4j.properties",
  					"-Dscript_type=.sh", "-Dfile.encoding=UTF-8", "-Xms600M",
  					"-Xmx1400M", "-Dinput.uri=" + release.getBuildUri() + "/META",
  					"-Doutput.uri=" + release.getBuildUri() + "/METAO",
  					"-Dmmsys.config.uri=" + release.getBuildUri() + "/log/mmsys.prop",
  					"org.java.plugin.boot.Boot"
  			}, new String[] {
  					"CLASSPATH=" + release.getBuildUri() + "/MMSYS:"
  							+ release.getBuildUri() + "/MMSYS/lib/jpf-boot.jar"
  			}, new File(release.getBuildUri(), "/MMSYS"));
			}
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
