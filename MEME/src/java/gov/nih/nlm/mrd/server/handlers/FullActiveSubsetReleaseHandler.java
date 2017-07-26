/************************************************************************
* Package:     gov.nih.nlm.mrd.server.handlers
* Object:      FullActiveReleaseHandler.java
*
* Changes:
*   08/12 initial version MAJ
*      
*    
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
* Handler for "Active" target.
*
* @author MAJ
*/
public class FullActiveSubsetReleaseHandler extends ReleaseHandler.Default {
	  /**
	   * Instantiates a {@link FullActiveSubsetReleaseHandler}.
	   */
	  public FullActiveSubsetReleaseHandler() {
	    setProcess("RELEASE");
	    setType("Full");
	  }

	  /**
	   * Cleans up "Build URI" Active directory and re-creates it.
	   * @throws MEMEException if anything goes wrong
	   */
	  public void prepare() throws MEMEException {
	    try {
	      ServerToolkit.logCommentToBuffer("Prepare Meta Active directory", true, log);
	      File file = new File(release.getBuildUri() + File.separator + "METAACTIVE");
	      if (file.exists()) {
	        ServerToolkit.exec(new String[] {"/bin/rm", "-r", "-f",
	                           release.getBuildUri() + File.separator + "METAACTIVE"});
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
	   * Uses Metamorphosys to generate a "keep everything" Active subset of the
	   * entire set of release files in "Build URI" METAACTIVE directory.
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
				// Open user.d.prop - this is the "Active Subset" default configuration.
				subsetConfig.load(new FileInputStream(new File(new File(new File(
						new File(release.getBuildUri(), "MMSYS"), "config"), release
						.getName()), "user.d.prop")));
				//subsetConfig.setProperty("mmsys_output_stream",
				//		"gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream");
				subsetConfig.setProperty("mmsys_input_stream",
						"gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysInputStream");
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
						"-Doutput.uri=" + release.getBuildUri() + "/METAACTIVE",
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
	  					"-Doutput.uri=" + release.getBuildUri() + "/METAACTIVE",
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

