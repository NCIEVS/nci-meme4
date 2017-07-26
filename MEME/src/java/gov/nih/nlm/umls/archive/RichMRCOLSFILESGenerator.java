/*****************************************************************************
 * Package: gov.nih.nlm.umls.archive
 * Object:  RichMRCOLSFILESGenerator.java
 *
 * CHANGES:
 *   08/22/2007: First Version
 ****************************************************************************/
package gov.nih.nlm.umls.archive;

import gov.nih.nlm.umls.io.InputStreamFileStatisticsConfiguration;
import gov.nih.nlm.umls.io.RichMRMetathesaurusOutputStream;
import gov.nih.nlm.umls.meta.Language;
import gov.nih.nlm.umls.meta.MetaDescription;
import gov.nih.nlm.umls.meta.impl.MetaDescriptionImpl;
import gov.nih.nlm.util.FieldedStringTokenizer;
import gov.nih.nlm.util.FileStatistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to generate MRCOLS.RRF and MRFILES.RRF for
 * a comparable RRF data set.
 * @author Brian Carlsen
 */
public class RichMRCOLSFILESGenerator {

	/**
	 * Application entry point.
	 * @param s command line args
	 */
	public static void main (String[] s){
		try {
			
			/**
			 * Create generator and write fil/col stats.
			 */
			RichMRCOLSFILESGenerator gen = new
			  RichMRCOLSFILESGenerator();
			gen.writeFileColumnStats(s[0]);
						
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

  /**
   * Write the column and file statistics.
   * @param dir Location of input and output files
   * @throws IOException if anything goes wrong
   */
  private void writeFileColumnStats(String dir) throws IOException {
		final List<String> files = getFileNames(dir);
		
		final FileInputStream fis = new FileInputStream(
				new File(System.getProperty("stats.config.file")));
		final InputStreamFileStatisticsConfiguration config =
			new InputStreamFileStatisticsConfiguration(fis);

		//
		// Open each file in turn and gather stats
		//
		final List<String> change_files = new ArrayList<String>();
		for (String file : files) {
			if (file.startsWith("CHANGE/"))
				change_files.add(FieldedStringTokenizer.split(file,"/")[1]);
			System.out.println("    Generating Stats for " + file);
			final BufferedReader in = new BufferedReader(new FileReader(new File(dir,file)));
			String line = null;
			final FileStatistics fs = config.getFileStatistics(file);
			fs.setCharsEqualsBytes(false);
			while ((line = in.readLine()) != null) {
			  fs.processLine(line);
			}
			in.close();
		}
		final String[] l_change_files = change_files.toArray(new String[0]);
		
		//
		// write column stats
		//
		System.out.println("    Generate MRCOLS and MRFILES");
		RichMRMetathesaurusOutputStream out = 
			new RichMRMetathesaurusOutputStream(config) {
			  public void open (String dir) throws IOException {
			  	mr_dir = dir;
			  	mr_targets.put("MRCOLS",openTargetFile("MRCOLS.RRF"));
			  	mr_targets.put("MRFILES",openTargetFile("MRFILES.RRF"));
			  	change_files = l_change_files;
			  	writeFileColumnStats();
			  	((BufferedWriter)(mr_targets.get("MRCOLS"))).close();
			  	((BufferedWriter)(mr_targets.get("MRFILES"))).close();
			  }
		  };
		out.open(dir);
  }
  
  /**
   * Return file names for MRFILES/MRCOLS.
   * @param dir directory where the ORF files are
   * @return the list of tracked MR files
   * @throws IOException if anything goes wrong
   */
  private List<String> getFileNames(String dir) throws IOException {
  	List<String> files = new ArrayList<String>();
  	//
  	// Get file list from config file.
  	//
  	FileReader fr = new FileReader(
				new File(System.getProperty("stats.config.file")));
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] tokens = FieldedStringTokenizer.split(line,"|");
			//
			// If file exists, add it to list
			//
			if (tokens[1].equals("MRFILES") &&
					new File(dir,tokens[0]).exists()) {
				files.add(tokens[0]);
				//
				// Initialize language for MRXW files
				//
				if (tokens[0].startsWith("MRXW.")) {
					Language.initialize( new MetaDescription[] {
							new MetaDescriptionImpl("LAT|" +
									FieldedStringTokenizer.split(tokens[0],".")[1] +
									"|expanded_form|X") });
				}
			}
		}
		return files;
  }
}

