/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMetaMorphoSysReleaseHandler.java
 * 
 * 08/29/2007  HY (1-F4HG9): creation folder for MRFILES/MRCOLS to be META
 * 07/17/2007  SL ( 1-EIJUU):  Unable to create the mmsys.zip removed brower.sh code.
 * 06/09/2006 TTN (1-BFPC3): add medline_info entries to release.dat
 * 
 **********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerConstants;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.swing.SwingToolkit;
import gov.nih.nlm.util.ColumnStatistics;
import gov.nih.nlm.util.FileStatistics;
import gov.nih.nlm.util.SystemToolkit;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

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
			ServerToolkit.logCommentToBuffer("Clean and prepare "
					+ release.getBuildUri() + "/MMSYS", true, log);
			// Clean and remove $META_RELEASE/MMSYS
			ServerToolkit.exec(new String[] {
					"/bin/rm", "-r", "-f", release.getBuildUri() + "/MMSYS"
			});
			// Make a "MMSYS" directory
			ServerToolkit.exec(new String[] {
					"/bin/mkdir", release.getBuildUri() + "/MMSYS"
			});

			// Clean and remove $META_RELEASE/METASUBSET
			ServerToolkit.exec(new String[] {
					"/bin/rm", "-r", "-f", release.getBuildUri() + "/METASUBSET"
			});
			// Make a "MMSYS" directory
			ServerToolkit.exec(new String[] {
					"/bin/mkdir", release.getBuildUri() + "/METASUBSET"
			});

			// Copy mmsys.zip from $MRD_HOME to $META_RELEASE/MMSYS
			ServerToolkit.logCommentToBuffer("Copying zip file to "
					+ release.getBuildUri() + "/MMSYS", true, log);
			ServerToolkit.exec(new String[] {
					"/bin/cp", "-f", mrd_home + "/mmsys.zip", release.getBuildUri()
			}, new String[] {
				"MRD_HOME=" + mrd_home
			});

			// Unpack mmsys.zip
			ServerToolkit.logCommentToBuffer("Unzipping " + release.getBuildUri()
					+ "/mmsys.zip", true, log);
			ServerToolkit.exec(new String[] {
					"unzip", "-o", "-d", release.getBuildUri() + "/MMSYS",
					release.getBuildUri() + "/mmsys.zip"
			});
			// Remove $META_RELEASE/mmsys.zip
			ServerToolkit.logCommentToBuffer("Deleting " + release.getBuildUri()
					+ "/mmsys.zip", true, log);
			File file1 = new File(release.getBuildUri(), "mmsys.zip");
			file1.delete();
			// At this point $META_RELEASE/MMSYS contains the MetamorphoSys
			// distribution, including scripts, plugins, config, etc.
			// it is $MMSYS_HOME for make_config.csh
		} catch (ExecException exece) {
			throw exece;
		} catch (Exception e) {
			DeveloperException dev =
					new DeveloperException("Failed to prepare the release data", this);
			dev.setEnclosedException(e);
			throw dev;
		}
	}

	/**
	 * Builds current config files and incorporates into new mmsys.zip
	 * @throws MEMEException if failed to generate.
	 */
	public void generate() throws MEMEException {
		/*
		 * 02/22/07 -- block 1 added from FullORFRleaseHandler.java (for move subset
		 * generation from MetaMorphoSys "validate" to MetamorphoSys "build"
		 */
		SwingToolkit.setProperty(SwingToolkit.VIEW, "false");

		// end of block 1
		try {

			/*
			 * 02/22/07 -- block 2 added from FullORFRleaseHandler.java (for move
			 * subset generation from MetaMorphoSys "validate" to MetamorphoSys
			 * "build"
			 */
			DecimalFormat formatter = new DecimalFormat("0.00");
			FileColumnStatisticsHandler f_handler =
					new FileColumnStatisticsHandler("MRFILES", data_source);

			FileColumnStatisticsHandler c_handler =
					new FileColumnStatisticsHandler("MRCOLS", data_source);
			String[] files;
			StringBuffer query = new StringBuffer();
			try {
				query.append("SELECT key, value, description ").append(
						"FROM mrd_properties ").append("WHERE key_qualifier = 'MRFILES'")
						.append("  AND expiration_date IS NULL");
				MEMEToolkit.trace("Query: " + query.toString());
				Statement stmt = data_source.createStatement();
				ResultSet rset = stmt.executeQuery(query.toString());
				Vector v = new Vector();
				while (rset.next()) {
					v.addElement(rset.getString("key"));
				}
				files = new String[v.size()];
				v.copyInto(files);
			} catch (SQLException e) {
				DataSourceException dse =
						new DataSourceException("Failded to get MRFILES values. ");
				dse.setDetail("statement", query.toString());
				dse.setEnclosedException(e);
				throw dse;
			}

			StringBuffer mrcols = new StringBuffer();
			StringBuffer mrfiles = new StringBuffer();

			for (int i = 0; i < files.length; i++) {

				if (files[i].equals("MRCOLS") || files[i].equals("MRFILES")) {
					continue;
				}

				FileStatistics stats = data_source.getFileStatistics(files[i]);
				// construct a mrfiles line from stats
				StringBuffer mrfiles_line = new StringBuffer();
				mrfiles_line.append(stats.getFileName()).append(".RRF").append("|")
						.append(stats.getDescription()).append("|");
				ColumnStatistics[] col_stats = stats.getAllColumnStatistics();
				for (int j = 0; j < col_stats.length - 1; j++) {
					mrfiles_line.append(col_stats[j].getColumnName()).append(",");
				}
				mrfiles_line.append(col_stats[col_stats.length - 1].getColumnName())
						.append("|").append(col_stats.length).append("|").append(
								stats.getLineCount()).append("|").append(stats.getByteCount())
						.append("|");
				mrfiles.append(f_handler.processLine(mrfiles_line.toString()));
				mrfiles.append("\n");
				for (int j = 0; j < col_stats.length; j++) {
					// construct mrcols line from col_stats
					StringBuffer mrcols_line = new StringBuffer();
					mrcols_line.append(col_stats[j].getColumnName()).append("|").append(
							col_stats[j].getDescription()).append("|").append("|").append(
							col_stats[j].getMinLength()).append("|").append(
							formatter.format(col_stats[j].getAverageLength())).append("|")
							.append(col_stats[j].getMaxLength()).append("|").append(
									col_stats[j].getFileName()).append(".RRF").append("|")
							.append(col_stats[j].getDataType()).append("|");
					mrcols.append(c_handler.processLine(mrcols_line.toString()));
					mrcols.append("\n");
				}
			}

			// now build a mrcols line for MRCOLS and pass it to f_handler
			// and c_handler, then append the line to mrcols (stringbuffer)
			// with \n
			FileStatistics stats = c_handler.getFileStatistics();
			ColumnStatistics[] col_stats = stats.getAllColumnStatistics();
			for (int j = 0; j < col_stats.length; j++) {
				// construct mrcols line from col_stats
				StringBuffer mrcols_line = new StringBuffer();
				mrcols_line.append(col_stats[j].getColumnName()).append("|").append(
						col_stats[j].getDescription()).append("|").append("|").append(
						col_stats[j].getMinLength()).append("|").append(
						formatter.format(col_stats[j].getAverageLength())).append("|")
						.append(col_stats[j].getMaxLength()).append("|").append(
								col_stats[j].getFileName()).append(".RRF").append("|").append(
								col_stats[j].getDataType()).append("|");
				c_handler.processLine(mrcols_line.toString());
			}
			StringBuffer mrfiles_line = new StringBuffer();
			mrfiles_line.append(stats.getFileName()).append(".RRF").append("|")
					.append(stats.getDescription()).append("|");
			for (int j = 0; j < col_stats.length - 1; j++) {
				mrfiles_line.append(col_stats[j].getColumnName()).append(",");
			}
			mrfiles_line.append(col_stats[col_stats.length - 1].getColumnName())
					.append("|").append(col_stats.length).append("|").append(
							stats.getLineCount()).append("|").append(stats.getByteCount())
					.append("|");
			f_handler.processLine(mrfiles_line.toString());

			// then build a mrfiles line for MRFILES and pass it
			// to f_handler and c_handler, then append the line to
			// mrfiles (string buffer) with \n.
			stats = f_handler.getFileStatistics();

			col_stats = stats.getAllColumnStatistics();
			for (int j = 0; j < col_stats.length; j++) {
				// construct mrcols line from col_stats
				StringBuffer mrcols_line = new StringBuffer();
				mrcols_line.append(col_stats[j].getColumnName()).append("|").append(
						col_stats[j].getDescription()).append("|").append("|").append(
						col_stats[j].getMinLength()).append("|").append(
						formatter.format(col_stats[j].getAverageLength())).append("|")
						.append(col_stats[j].getMaxLength()).append("|").append(
								col_stats[j].getFileName()).append(".RRF").append("|").append(
								col_stats[j].getDataType()).append("|");
				c_handler.processLine(mrcols_line.toString());
			}
			mrfiles_line = new StringBuffer();
			mrfiles_line.append(stats.getFileName()).append("|").append(
					stats.getDescription()).append("|");
			for (int j = 0; j < col_stats.length - 1; j++) {
				mrfiles_line.append(col_stats[j].getColumnName()).append(",");
			}
			mrfiles_line.append(col_stats[col_stats.length - 1].getColumnName())
					.append("|").append(col_stats.length).append("|").append(
							stats.getLineCount()).append("|").append(stats.getByteCount())
					.append("|");
			f_handler.processLine(mrfiles_line.toString());

			stats = c_handler.getFileStatistics();
			col_stats = stats.getAllColumnStatistics();
			for (int j = 0; j < col_stats.length; j++) {
				// construct mrcols line from col_stats
				StringBuffer mrcols_line = new StringBuffer();
				mrcols_line.append(col_stats[j].getColumnName()).append("|").append(
						col_stats[j].getDescription()).append("|").append("|").append(
						col_stats[j].getMinLength()).append("|").append(
						formatter.format(col_stats[j].getAverageLength())).append("|")
						.append(col_stats[j].getMaxLength()).append("|").append(
								col_stats[j].getFileName()).append(".RRF").append("|").append(
								col_stats[j].getDataType()).append("|");
				mrcols.append(mrcols_line.toString());
				mrcols.append("\n");
			}
			mrfiles_line = new StringBuffer();
			mrfiles_line.append(stats.getFileName()).append(".RRF").append("|")
					.append(stats.getDescription()).append("|");
			for (int j = 0; j < col_stats.length - 1; j++) {
				mrfiles_line.append(col_stats[j].getColumnName()).append(",");
			}
			mrfiles_line.append(col_stats[col_stats.length - 1].getColumnName())
					.append("|").append(col_stats.length).append("|").append(
							stats.getLineCount()).append("|").append(stats.getByteCount())
					.append("|");
			mrfiles.append(mrfiles_line.toString());
			mrfiles.append("\n");

			// then build a mrfiles line for MRFILES and pass it
			// to f_handler and c_handler, then append the line to
			// mrfiles (string buffer) with \n.
			stats = f_handler.getFileStatistics();

			col_stats = stats.getAllColumnStatistics();
			for (int j = 0; j < col_stats.length; j++) {
				// construct mrcols line from col_stats
				StringBuffer mrcols_line = new StringBuffer();
				mrcols_line.append(col_stats[j].getColumnName()).append("|").append(
						col_stats[j].getDescription()).append("|").append("|").append(
						col_stats[j].getMinLength()).append("|").append(
						formatter.format(col_stats[j].getAverageLength())).append("|")
						.append(col_stats[j].getMaxLength()).append("|").append(
								col_stats[j].getFileName()).append(".RRF").append("|").append(
								col_stats[j].getDataType()).append("|");
				mrcols.append(mrcols_line.toString());
				mrcols.append("\n");
			}
			mrfiles_line = new StringBuffer();
			mrfiles_line.append(stats.getFileName()).append(".RRF").append("|")
					.append(stats.getDescription()).append("|");
			for (int j = 0; j < col_stats.length - 1; j++) {
				mrfiles_line.append(col_stats[j].getColumnName()).append(",");
			}
			mrfiles_line.append(col_stats[col_stats.length - 1].getColumnName())
					.append("|").append(col_stats.length).append("|").append(
							stats.getLineCount()).append("|").append(stats.getByteCount())
					.append("|");
			mrfiles.append(mrfiles_line.toString());
			mrfiles.append("\n");

			MEMEToolkit.trace("mrcols = " + mrcols);
			MEMEToolkit.trace("mrfiles = " + mrfiles);

			String dir_name = release.getBuildUri() + File.separator + "META";
			File file = new File(dir_name, "MRCOLS.RRF");
			MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator
					+ file.getName(), true, log);
			PrintWriter out =
					new PrintWriter(new BufferedWriter(new FileWriter(file)));
			out.print(mrcols);
			out.close();
			try {
				SystemToolkit.sort(dir_name + File.separator + file.getName());
			} catch (IOException ioe) {
				ExternalResourceException ere =
						new ExternalResourceException("Failed to sort the target file", ioe);
				ere.setDetail("file", file.getName());
				throw ere;
			}
			File file_MRFILES = new File(dir_name, "MRFILES.RRF");
			MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator
					+ file_MRFILES.getName(), true, log);
			out = new PrintWriter(new BufferedWriter(new FileWriter(file_MRFILES)));
			out.print(mrfiles);
			out.close();
			try {
				SystemToolkit.sort(dir_name + File.separator + file_MRFILES.getName());
			} catch (IOException ioe) {
				ExternalResourceException ere =
						new ExternalResourceException("Failed to sort the target file", ioe);
				ere.setDetail("file", file_MRFILES.getName());
				throw ere;
			}

			// --- end of block 2

			// Make $META_RELEASE/release.dat
			ServerToolkit.logCommentToBuffer("Creating " + release.getBuildUri()
					+ "/release.dat", true, log);
			File file_r = new File(release.getBuildUri() + "/release.dat");
			PrintWriter writer = new PrintWriter(new FileWriter(file_r));
			writer.println("umls.release.name=" + release.getName());
			writer.println("umls.release.description=" + release.getDescription());
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
			writer.println("umls.release.date="
					+ dateformat.format(release.getReleaseDate()));
			writer.println("umls.lvg.version=" + ServerToolkit.getLVGVersion());
			MetaProperty[] medline_properties =
					data_source.getMetaPropertiesByKeyQualifier("MEDLINE_INFO");
			for (int i = 0; i < medline_properties.length; i++) {
				writer.println(medline_properties[i].getKey() + "="
						+ medline_properties[i].getValue());
			}

			// Get mmsys.build.date, mmsys.version
			// from config/<release>/release.dat			
			Properties configProps = new Properties();
			File configReleaseDat =
					new File(
							release.getBuildUri() + "/MMSYS/config/" + release.getName(),
							"release.dat");
			if (!configReleaseDat.exists())
				throw new IOException(configReleaseDat.getAbsolutePath()
						+ " does not exist.");
			configProps.load(new FileInputStream(configReleaseDat));
			writer.println("mmsys.build.date="
					+ configProps.getProperty("mmsys.build.date"));
			writer.println("mmsys.version="
					+ configProps.getProperty("mmsys.version"));
			writer.flush();
			writer.close();

			// Copy release.dat to $META_RELEASE/MMSYS
			// Used by make_config.csh to determine the current release version
			ServerToolkit.exec(new String[] {
					"/bin/cp", "-f", release.getBuildUri() + "/release.dat",
					release.getBuildUri() + "/MMSYS"
			});

			// Copy release.dat to $META_RELEASE/META
			// Used by MetamorphoSys to know the release version of the input stream
			// This is the release.dat file that winds up in the mmsys.zip file.
			ServerToolkit.exec(new String[] {
					"/bin/cp", "-f", release.getBuildUri() + "/release.dat",
					release.getBuildUri() + "/META"
			});

			//
			// Run make_config.csh
			// TODO: need to make NET/ dir -- not automatic!
			//
			SwingToolkit.setProperty(SwingToolkit.VIEW, "false");
			String mrd_home = ServerToolkit.getProperty(ServerConstants.MRD_HOME);
			String oracle_home =
					ServerToolkit.getProperty(ServerConstants.ORACLE_HOME);
			ServerToolkit.logCommentToBuffer("Making config file "
					+ dateformat.format(release.getReleaseDate()) + " "
					+ release.getName() + " " + data_source.getDataSourceName(), true,
					log);
			ServerToolkit.exec(new String[] {
					mrd_home + "/bin/make_config.csh", release.getBuildUri() + "/META",
					release.getBuildUri() + "/NET", release.getBuildUri() + "/MMSYS"
			}, new String[] {
					"MRD_HOME=" + mrd_home,
					"META_RELEASE=" + release.getBuildUri() + "/META",
					"MMSYS_DIR=" + release.getBuildUri() + "/MMSYS",
					"ORACLE_HOME=" + oracle_home
			}, new File(mrd_home, "bin"));

			// Pack mmsys.zip from the MMSYS/ directory (includes all files, scripts,
			// etc).
			ServerToolkit.logCommentToBuffer("Creating zip file", true, log);
			ServerToolkit.exec(new String[] {
					"zip", "-r", release.getBuildUri() + "/mmsys.zip", "."
			}, new String[] {
					"MRD_HOME=" + mrd_home,
					"META_RELEASE=" + release.getBuildUri() + "/META"
			}, new File(release.getBuildUri(), "/MMSYS"));

			/*
			 * 02/22/07 added from FullORFRleaseHandler.java override the
			 * UserConfiguration settings
			 */

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
					"gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream");
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
			try {
				// Try as solaris
				ServerToolkit.exec(new String[] {
						release.getBuildUri() + "/MMSYS/jre/solaris/bin/java",
						"-Djava.awt.headless=true",
						"-Djpf.boot.config=" + release.getBuildUri()
								+ "/MMSYS/etc/subset.boot.properties",
						"-Dlog4j.configuration=etc/subset.log4j.properties",
						"-Dscript_type=.sh", "-Dfile.encoding=UTF-8", "-Xms600M",
						"-Xmx1400M", "-Dinput.uri=" + release.getBuildUri() + "/META",
						"-Doutput.uri=" + release.getBuildUri() + "/METASUBSET",
						"-Dmmsys.config.uri=" + release.getBuildUri() + "/log/mmsys.prop",
						"org.java.plugin.boot.Boot"
				}, new String[] {
						"MRD_HOME=" + mrd_home,
						"META_RELEASE=" + release.getBuildUri() + "/META",
						"CLASSPATH=" + release.getBuildUri() + "/MMSYS:"
								+ release.getBuildUri() + "/MMSYS/lib/jpf-boot.jar"
				}, new File(release.getBuildUri(), "/MMSYS"));
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
						"-Doutput.uri=" + release.getBuildUri() + "/METASUBSET",
						"-Dmmsys.config.uri=" + release.getBuildUri() + "/log/mmsys.prop",
						"org.java.plugin.boot.Boot"
				}, new String[] {
						"MRD_HOME=" + mrd_home,
						"META_RELEASE=" + release.getBuildUri() + "/META",
						"CLASSPATH=" + release.getBuildUri() + "/MMSYS:"
								+ release.getBuildUri() + "/MMSYS/lib/jpf-boot.jar"
				}, new File(release.getBuildUri(), "/MMSYS"));
			}
		} catch (ExecException exece) {
			throw exece;
		} catch (Exception e) {
			DeveloperException dev =
					new DeveloperException("Failed to generate the release data", this);
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
			ServerToolkit.exec(new String[] {
					"/bin/rcp",
					release.getBuildUri() + "/mmsys.zip",
					release.getReleaseHost() + ":" + release.getReleaseUri() + "/MASTER/"
							+ "mmsys." + date + ".zip"
			}, new String[] {}, true, ServerConstants.USE_INPUT_STREAM, false);
			ServerToolkit.exec(new String[] {
					"/bin/rsh", release.getReleaseHost(),
					"/bin/rm -f " + release.getReleaseUri() + "/MASTER/mmsys.zip"
			}, new String[] {}, true, ServerConstants.USE_INPUT_STREAM, false);
			ServerToolkit.exec(new String[] {
					"/bin/rsh",
					release.getReleaseHost(),
					"/bin/ln -s " + release.getReleaseUri() + "/MASTER/mmsys." + date
							+ ".zip " + release.getReleaseUri() + "/MASTER/mmsys.zip"
			}, new String[] {}, true, ServerConstants.USE_INPUT_STREAM, false);
			String local_digest =
					localDigest(new File(release.getBuildUri(), "mmsys.zip").getPath());
			ServerToolkit.logCommentToBuffer("MD5 digest " + release.getBuildUri()
					+ target_name + " - " + local_digest, true, log);
			String remote_digest =
					remoteDigest(release.getReleaseUri() + "/MASTER/mmsys.zip", release
							.getReleaseHost());

			ServerToolkit.logCommentToBuffer("MD5 digest " + release.getReleaseUri()
					+ "/MASTER/" + target_name + " - " + remote_digest, true, log);

			if (!local_digest.equals(remote_digest)) {
				throw new Exception("Remote digest does not match local digest.");
			}
		} catch (Exception e) {
			DeveloperException dev =
					new DeveloperException("Failed to publish the target", this);
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
