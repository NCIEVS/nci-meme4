/*****************************************************************************
 * Package: gov.nih.nlm.umls.archive
 * Object:  OriginalMRCOLSFILESGenerator.java
 *
 * CHANGES:
 *   08/22/2007: First Version
 ****************************************************************************/
package gov.nih.nlm.umls.archive;

import gov.nih.nlm.umls.io.InputStreamFileStatisticsConfiguration;
import gov.nih.nlm.umls.io.OriginalMRMetathesaurusOutputStream;
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
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to generate MRCOLS and MRFILES for
 * a comparable ORF data set.
 * @author Brian Carlsen
 */
public class OriginalMRCOLSFILESGenerator {

	/**
	 * Application entry point.
	 * @param s command line args
	 */
	public static void main (String[] s){
		try {
			
			/**
			 * Create generator and write fil/col stats.
			 */
			OriginalMRCOLSFILESGenerator gen = new
			  OriginalMRCOLSFILESGenerator();
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
		OriginalMRMetathesaurusOutputStream out = 
			new OriginalMRMetathesaurusOutputStream(config) {
			  public void open (String dir) throws IOException {
			  	mr_dir = dir;
			  	mr_targets.put("MRCOLS",openTargetFile("MRCOLS"));
			  	mr_targets.put("MRFILES",openTargetFile("MRFILES"));
			  	change_files = l_change_files;
			  	writeFileColumnStats();
			  	((BufferedWriter)(mr_targets.get("MRCOLS"))).close();
			  	((BufferedWriter)(mr_targets.get("MRFILES"))).close();
			  }
			  protected void writeFileColumnStats() throws IOException {

			    //
			    // Iterate through file statistics for all files
			    //
			    final StringBuilder mrcols_static = new StringBuilder(200);
			    final StringBuilder mrfiles_static = new StringBuilder(200);
			    for(int i = 0; i < target_files.length; i++) {
			    	if (target_files[i].equals("MRCUI"))
			    		continue;
			      final FileStatistics file_stats = config.getFileStatistics(target_files[i]);
			      //
			      // Build up dummy in-memory versions of MRCOLS MRFILES
			      //
			      if(file_stats.getFileName().startsWith("MRCOLS") ||
			         file_stats.getFileName().startsWith("MRFILES")) {
			        continue;
			      } else {
			        mrfiles_static.append(getFileTextFromFileStatistics(file_stats));
			        mrcols_static.append(getColumnTextFromFileStatistics(file_stats));
			      }
			    }

			    final Language[] lats = Language.getLanguages();
			    for(int i = 0; i < lats.length; i++) {
			      final FileStatistics stats = config.getFileStatistics("MRXW." + lats[i]);
			      mrfiles_static.append(getFileTextFromFileStatistics(stats));
			      mrcols_static.append(getColumnTextFromFileStatistics(stats));
			    }

			    for (int i = 0; i < change_files.length; i++) {
			      final FileStatistics stats = config.getFileStatistics("CHANGE/" +
			          change_files[i]);
			      mrfiles_static.append(getFileTextFromFileStatistics(stats));
			      mrcols_static.append(getColumnTextFromFileStatistics(stats));
			    }

			    FileStatistics stats = config.getFileStatistics("AMBIG.SUI");
			    mrfiles_static.append(getFileTextFromFileStatistics(stats));
			    mrcols_static.append(getColumnTextFromFileStatistics(stats));

			    stats = config.getFileStatistics("AMBIG.LUI");
			    mrfiles_static.append(getFileTextFromFileStatistics(stats));
			    mrcols_static.append(getColumnTextFromFileStatistics(stats));

			    stats = config.getFileStatistics("MRDOC");
			    mrfiles_static.append(getFileTextFromFileStatistics(stats));
			    mrcols_static.append(getColumnTextFromFileStatistics(stats));

			    //
			    // Iterate through, making guessing contents for MRFILES/COLS
			    // When a revised guess matches an old one, we have converged
			    // then write MRFILES/COLS
			    //
			    StringBuilder mrcols = new StringBuilder(200);
			    StringBuilder mrfiles = new StringBuilder(200);
			    StringBuilder mrcols_new = new StringBuilder(200);
			    StringBuilder mrfiles_new = new StringBuilder(200);
			    try {

			      while(true) {

			        //
			        // Start with static data for MRCOLS
			        //
				mrcols = new StringBuilder(200);
				mrcols.append(mrcols_new);
				mrcols_new = new StringBuilder(200);
				mrcols_new.append(mrcols_static);

			        //
			        // Start with static data for MRFILES
			        //
				mrfiles = new StringBuilder(200);
				mrfiles.append(mrfiles_new);
				mrfiles_new = new StringBuilder(200);
				mrfiles_new.append(mrfiles_static);

			        //
			        // Compute MRFILES line for MRFILES
			        //
				FileStatistics mrfiles_stats = config.getFileStatistics("MRFILES");
				String[] mrfiles_lines =
				  FieldedStringTokenizer.split(mrfiles.toString(), "\n");
				for(int i = 0; i < mrfiles_lines.length; i++) {
			          if (mrfiles_lines[i].length() > 0)
			  	    mrfiles_stats.processLine(mrfiles_lines[i]);
				}

			        //
			        // Compute MRFILES line for MRCOLS
			        //
				FileStatistics mrcols_stats = config.getFileStatistics("MRCOLS");
				String[] mrcols_lines =
				  FieldedStringTokenizer.split(mrcols.toString(), "\n");
				for(int i = 0; i < mrcols_lines.length; i++) {
			          if (mrcols_lines[i].length() > 0)
				    mrcols_stats.processLine(mrcols_lines[i]);
				}

			        //
			        // Append MRFILES,MRCOLS entries to MRFILES
			        //
				mrfiles_new.append(getFileTextFromFileStatistics(mrfiles_stats));
				mrfiles_new.append(getFileTextFromFileStatistics(mrcols_stats));
				mrcols_new.append(getColumnTextFromFileStatistics(mrcols_stats));
				mrcols_new.append(getColumnTextFromFileStatistics(mrfiles_stats));

			        //
			        // If everything is the same, we are done.
			        //
				if(mrfiles.toString().equals(mrfiles_new.toString()) &&
				   mrcols.toString().equals(mrcols_new.toString()))
				  break;
			      }

			      //
			      // Split MRCOLS/MRFILES into lines
			      //
			      String[] mrfiles_output =
				FieldedStringTokenizer.split(mrfiles.toString(), "\n");
			      String[] mrcols_output =
				FieldedStringTokenizer.split(mrcols.toString(), "\n");

			      //
			      // Sort MRCOLS/MRFILES
			      //
			      Arrays.sort(mrfiles_output);
			      Arrays.sort(mrcols_output);

			      //
			      // Write MRFILES and MRCOLS
			      //  (start at index 1 because FST.split causes a blank entry)
			      //
			      BufferedWriter mrfiles_target = (BufferedWriter)mr_targets.get("MRFILES");
			      BufferedWriter mrcols_target = (BufferedWriter)mr_targets.get("MRCOLS");
			      for (int i = 0; i < mrfiles_output.length; i++)
			        if (mrfiles_output[i].length() > 0) {
			          mrfiles_target.write(mrfiles_output[i]);
			          mrfiles_target.newLine();
			        }

			      for (int i = 0; i < mrcols_output.length; i++)
			        if (mrcols_output[i].length() > 0) {
			          mrcols_target.write(mrcols_output[i]);
			          mrcols_target.newLine();
			        }
			    } catch (Exception e) {
			      //
			      // Exception here means something weird happened
			      //
			      IOException ioe = new IOException(
			       "Problem processing lines for FileStatistics ");
			      ioe.initCause(e);
			      throw ioe;
			    }
			  }		  };
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

