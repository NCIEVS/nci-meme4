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

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerConstants;
import gov.nih.nlm.swing.SwingConstants;
import gov.nih.nlm.swing.SwingToolkit;

import java.io.File;

/**
 * Handler for "Optimization" target.
 *
 * @author BAC
 */
public class FullOptimizationReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullOptimizationReleaseHandler}.
   */
  public FullOptimizationReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Cleans up "Build URI" META2 directory and re-creates it.
   * @throws MEMEException if anything goes wrong
   */
  @Override
	public void prepare() throws MEMEException {
    try {
      MEMEToolkit.logCommentToBuffer("Prepare META2 directory", true, log);
      File file = new File(release.getBuildUri() + File.separator + "META2");
      if (file.exists()) {
        MEMEToolkit.exec(new String[] {"/bin/rm", "-r", "-f",
                           release.getBuildUri() + File.separator + "META2"});
      }
      file.mkdir();
      
      MEMEToolkit.logCommentToBuffer("Prepare METAACTIVE2 directory", true, log);
      file = new File(release.getBuildUri() + File.separator + "METAACTIVE2");
      if (file.exists()) {
          MEMEToolkit.exec(new String[] {"/bin/rm", "-r", "-f",
                             release.getBuildUri() + File.separator + "METAACTIVE2"});  
      }
      file.mkdir();

    } catch (ExecException exece) {
      throw exece;
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to make the output folder(s) - META2 or METAACTIVE2", e);
      throw ere;
    }
  }

  /**
   * Runs the $MRD_HOME/bin/optimize_rrf.csh script to create
   * optimized RRF files from META in META2.  Files that do not require
   * optimizing are simply linked back to META.
   * @throws MEMEException if anything goes wrong
   */
  @Override
	public void generate() throws MEMEException {
    SwingToolkit.setProperty(SwingConstants.VIEW,"false");
    try {
    	//
    	// Create optimization in META2
    	//
      MEMEToolkit.logCommentToBuffer("Begin Optimizing Operation", true, log);
	  String mrd_home = SwingToolkit.getProperty(ServerConstants.MRD_HOME);
	  MEMEToolkit.exec(new String[] {
					mrd_home + "/bin/optimize_rrf.csh",
					release.getBuildUri() + "/META",
					release.getBuildUri() + "/META2"
			}, new String[0], new File(release.getBuildUri(), "/META2"));
	  
	  MEMEToolkit.exec(new String[] {
				mrd_home + "/bin/optimize_rrf.csh",
				release.getBuildUri() + "/METAACTIVE",
				release.getBuildUri() + "/METAACTIVE2"
		}, new String[0], new File(release.getBuildUri(), "/METAACTIVE2"));
	  
      MEMEToolkit.logCommentToBuffer("Finished Optimizing Operation", true,log);

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
  @Override
  public boolean publish() throws MEMEException {
    return true;
  }

  /**
   * Returns empty file list.
   * @return empty file list
   * @throws MEMEException if failed to get file list
   */
  @Override
	public String[] getFiles() throws MEMEException {
    return new String[] {};
  }
}
