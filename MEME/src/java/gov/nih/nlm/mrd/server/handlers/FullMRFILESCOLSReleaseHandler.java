/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRFILESCOLSReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.util.ColumnStatistics;
import gov.nih.nlm.util.FileStatistics;
import gov.nih.nlm.util.SystemToolkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Handler for "MRFILESCOLS" target.
 *
 * @author TTN, BAC
 */
public class FullMRFILESCOLSReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRFILESCOLSReleaseHandler}.
   */
    public FullMRFILESCOLSReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Compute and write <code>MRCOLS.RRF</code> and <code>MRFILES.RRF</code>
   * to "Build URI" META direcgtory.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {
	  
	  /*
	   * added 02/27/07
	   * copy MRCOLS and MRFILES from METASUBSET to META
	   * MRCOLS and MRFILES were generated in FULLMetamorphSysRleaseHandler.generate()
	   */
      String from_dir_name = release.getBuildUri() + File.separator + "METASUBSET";
      String to_dir_name = release.getBuildUri() + File.separator + "META";
  
      File from_file = null;
      File to_file = null;
      	  
	  try{
		    
		  from_file = new File(from_dir_name, "MRCOLS.RRF");
		  to_file = new File(to_dir_name, "MRCOLS.RRF");
		  ServerToolkit.logCommentToBuffer("Prepare METAO directory", true, log);
		  
		  SystemToolkit.copy(from_file, to_file);
		  
	      from_file = new File(from_dir_name, "MRFILES.RRF");
	      to_file = new File(to_dir_name, "MRFILES.RRF");   
          SystemToolkit.copy(from_file, to_file); 
          
      } catch (IOException ioe) {
          ExternalResourceException ere = new ExternalResourceException(
              "Failed to copy MRFILES/MRCOLS", ioe);
          ere.setDetail("file", from_dir_name + "MRCOLS.RRF/MRFILES.RRF");
          throw ere;
      }
      
  }

  /**
   * Returns the file list.
   * @return the file list
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {
        "MRFILES", "MRCOLS"};
  }
}