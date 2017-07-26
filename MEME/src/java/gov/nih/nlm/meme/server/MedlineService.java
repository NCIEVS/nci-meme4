package gov.nih.nlm.meme.server;

/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MedlineService
 *
 * Changes:
 *   06/19/2006 TTN (1-77HMD): fix data source type cast in MedlineService to process Medline data
 *   04/28/2006 TTN (1-77HMD): Added MedlineService to process Medline data
 *
 *****************************************************************************/



import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.StageStatus;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.mrd.sql.MRDDataSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles requests related to map sets or mappings.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class MedlineService implements MEMEApplicationService {

    //
    // Implementation of MEMEApplicationService interface
    //

    /**
     * Receives requests from the {@link MEMEApplicationServer}
     * Handles the request based on the "function" parameter.
     * @param context the {@link SessionContext}
     * @throws MEMEException if failed to process the request
     */
	public void processRequest(SessionContext context) throws MEMEException{

        // Get Service Request and function parameter
        MEMEServiceRequest request = context.getServiceRequest();
        final MEMEDataSource data_source = (MEMEDataSource) context.
                                           getDataSource();
        String function = (String) request.getParameter("function").getValue();
        if (function.equals("clearMedlineStatus")) {
            clearMedlineStatus((String) request.getParameter("stage").getValue());
        } else if (function.equals("downloadMedlineBaseline")) {
            downloadMedlineBaseline();
        } else if (function.equals("parseMedlineBaseline")) {
            parseMedlineBaseline(data_source,
                                 (String) request.getParameter("context").
                                 getValue(),
                                 (String) request.getParameter("cutoff_date").
                                 getValue());
        } else if (function.equals("processMedlineBaseline")) {
            processMedlineBaseline(data_source,
                                   (String) request.getParameter("context").
                                   getValue());
        } else if (function.equals("updateMedline")) {
            updateMedline(data_source,
                          (String) request.getParameter("context").
                          getValue(),
                          (String) request.getParameter("cutoff_date").
                          getValue());
        } else if (function.equals("getMedlineStageStatus")) {
            request.addReturnValue(
                    new Parameter.Default("status",
                                          getMedlineStageStatus((String)
                    request.getParameter("stage_name").getValue())));
        } else if (function.equals("getMedlineStatus")) {
            request.addReturnValue(
                    new Parameter.Default("status", getMedlineStatus()));
        } else if (function.equals("deleteUpdateMedlineXML")) {
            deleteUpdateMedlineXML((String) request.getParameter("file_name").
                                   getValue());
        }

    }

    /**
     * Downloads the Medline Baseline files from NLM machine to $MEDLINE_DIR.
     * @throws MEMEException if failed to download the files
     */
    public void downloadMedlineBaseline() throws MEMEException {
        ServerToolkit.getThread(new Runnable() {
            public void run() {
                final String medline_dir = ServerToolkit.getProperty(
                        "env.MEDLINE_DIR");
                final String meme_home = ServerToolkit.getProperty(
                        "env.MEME_HOME");
                PrintWriter writer = null;
                try {
                    final File file = new File(medline_dir,
                                               "get_baseline.log");
                    writer = new PrintWriter(new BufferedWriter(new FileWriter(
                            file)));
                    try {
                        ServerToolkit.exec(
                                new String[] {
                                meme_home + "/bin/get_baseline.pl"},
                                new String[0],
                                new File(medline_dir),
                                writer);
                    } catch (ExecException exece) {
                        writer.write(
                                "Error downloading Medline baseline XML files");
                        exece.printStackTrace(writer);
                        ServerToolkit.handleError(exece);
                    }
                    writer.close();
                } catch (IOException ioe) {
                    ServerToolkit.handleError(ioe);
                }
            }
        }).start();

    }

    /**
     * Parses the Medline Baseline XML files by calling $MRD_HOME/bin/medline_parser.pl.
     * @param data_source the data source name
     * @param cutoff_date the last date to process medline data
     * @param context either the release name or MID
     * @throws MEMEException if failed to parse the files
     */
    public void parseMedlineBaseline(MEMEDataSource data_source, String context,
                                     String cutoff_date) throws
            MEMEException {
    	final String service = data_source.getDataSourceName();
        DateFormat dateformat = new SimpleDateFormat(
                "MM/dd/yyyy");
        if (cutoff_date == null) {
            cutoff_date = dateformat.format(Calendar.getInstance().
                                            getTime());
            if (!"MID".equals(context)) {
                cutoff_date = dateformat.format(((MRDDataSource)data_source).
                                                getReleaseInfo(context).
                                                getReleaseDate());
            }
        }
        final String release_date = cutoff_date;
        ServerToolkit.getThread(new Runnable() {
            public void run() {
                final String medline_dir = ServerToolkit.getProperty(
                        "env.MEDLINE_DIR");
                final String meme_home = ServerToolkit.getProperty(
                        "env.MEME_HOME");
                PrintWriter writer = null;
                try {
                    File file = new File(medline_dir,
                                         "coc_headings.dat");
                    file.delete();
                    file = new File(medline_dir,
                                    "coc_subheadings.dat");
                    file.delete();
                    file = new File(medline_dir,
                                    "medline_parser.log");
                    writer = new PrintWriter(new BufferedWriter(new FileWriter(
                            file)));
                    try {
                        ServerToolkit.exec(new String[] {
                                           meme_home +
                                           "/bin/medline_parser.pl",
                                           "-db=" + service,
                                           "-release_date=" + release_date,
                                           "-i",
                                           "medline*.xml"}
                                           ,
                                           new String[] {"MEME_HOME=" +
                                           ServerToolkit.getProperty(
                                ServerConstants.MEME_HOME),
                                           "ORACLE_HOME=" +
                                           ServerToolkit.
                                           getProperty(ServerConstants.
                                ORACLE_HOME)}
                                           ,
                                           new File(medline_dir),
                                           writer);
                    } catch (ExecException exece) {
                        writer.write("ERROR parsing Medline baseline XML files");
                        exece.printStackTrace(writer);
                        ServerToolkit.handleError(exece);
                    }
                    writer.close();
                } catch (IOException ioe) {
                    ServerToolkit.handleError(ioe);
                }
            }
        }).start();
    }

    /**
     * Process the Medline Baseline data by calling $MRD_HOME/bin/process_medline_data.csh.
     * @param data_source the data source name
     * @param context either the release name or MID
     * @throws MEMEException if failed to process the data
     */
    public void processMedlineBaseline(MEMEDataSource data_source, String context) throws
            MEMEException {
    	final String service = data_source.getDataSourceName();
        final String db_mode = (!"MID".equals(context) ? "-mrd" : "");
        ServerToolkit.getThread(new Runnable() {
            public void run() {
                final String meme_home = ServerToolkit.getProperty(
                        ServerConstants.
                        MEME_HOME);
                final String medline_dir = ServerToolkit.getProperty(
                        "env.MEDLINE_DIR");
                PrintWriter writer = null;
                try {
                    File file = new File(medline_dir,
                                         "process_medline_data.log");
                    writer = new PrintWriter(new BufferedWriter(new FileWriter(
                            file)));
                    try {
                        file = new File(medline_dir + "/update");
                        if (file.exists()) {
                            ServerToolkit.exec(
                                    new String[] {"/bin/rm", "-r", "-f",
                                    medline_dir + "/update"});
                        }
                        if(db_mode.equals("-mrd")) {
                            ServerToolkit.exec(
                                    new String[] {
                                    meme_home + "/bin/process_medline_data.csh",
                                    db_mode, "-i", service},
                                    new String[0],
                                    new File(medline_dir), writer);
                        } else {
                            ServerToolkit.exec(
                                    new String[] {
                                    meme_home + "/bin/process_medline_data.csh",
                                    "-i", service},
                                    new String[0],
                                    new File(medline_dir), writer);
                        }
                    } catch (ExecException exece) {
                        writer.write("ERROR process Medline baseline XML files");
                        exece.printStackTrace(writer);
                        ServerToolkit.handleError(exece);
                    }
                    writer.close();
                } catch (IOException ioe) {
                    ServerToolkit.handleError(ioe);
                }
            }
        }).start();
    }

    /**
     * Downloads, parses and process the Medline Update XML files by calling
     * $MRD_HOME/bin/update_medline_data.pl
     * @param data_source the data source name
     * @param context either the release name or MID
     * @param cutoff_date the last date to process medline data
     * @throws MEMEException if failed to process the data
     */
    public void updateMedline(MEMEDataSource data_source, String context,
                              String cutoff_date) throws
            MEMEException {
        DateFormat dateformat = new SimpleDateFormat(
                "MM/dd/yyyy");
        if (cutoff_date == null) {
            cutoff_date = dateformat.format(Calendar.getInstance().
                                            getTime());
            if (!"MID".equals(context)) {
                cutoff_date = dateformat.format(((MRDDataSource)data_source).
                                                getReleaseInfo(context).
                                                getReleaseDate());
            }
        }
    	final String service = data_source.getDataSourceName();
        final String release_date = cutoff_date;
        final String db_mode = (!"MID".equals(context) ? "-mrd" : "");
        ServerToolkit.getThread(new Runnable() {
            public void run() {
                final String meme_home = ServerToolkit.getProperty(
                        ServerConstants.
                        MEME_HOME);
                final String medline_dir = ServerToolkit.getProperty(
                        "env.MEDLINE_DIR");
                PrintWriter writer = null;
                try {
                    File file = new File(medline_dir + "/update");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    file = new File(medline_dir + "/update",
                                    "update_medline_data.log");
                    writer = new PrintWriter(new BufferedWriter(new FileWriter(
                            file)));
                    try {
                        ServerToolkit.exec(
                                new String[] {
                                meme_home + "/bin/update_medline_data.pl",
                                db_mode,
                                "-db=" + service,
                                "-start_date=" + release_date,
                                "-release_date=" + release_date,
                        },
                                new String[0],
                                new File(medline_dir + "/update"), writer);
                    } catch (ExecException exece) {
                        writer.write(
                                "ERROR processing Medline update XML files");
                        exece.printStackTrace(writer);
                        ServerToolkit.handleError(exece);
                    }
                    writer.close();
                } catch (IOException ioe) {
                    ServerToolkit.handleError(ioe);
                }
            }
        }).start();
    }

    /**
     * Returns the {@link StageStatus} of the specified Meldine  processing stage.
     * @param stage_name the stage name
     * @return the {@link StageStatus} of the specified Meldine  processing stage
     * @throws MEMEException if failed to get status
     */
    public StageStatus getMedlineStageStatus(String stage_name) throws
            MEMEException {
        final String medline_dir = ServerToolkit.getProperty("env.MEDLINE_DIR");
        StageStatus stage = null;
        if (stage_name.equals("download")) {
            try {
                File file = new File(medline_dir,
                                     "get_baseline.log");
                stage = new StageStatus("download");
                stage.setCode(StageStatus.NONE);
                if (file.exists()) {
                    stage.setCode(StageStatus.RUNNING);
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        if (line.indexOf("Error") != -1) {
                            stage.setCode(StageStatus.ERROR);
                        }
                        if (line.indexOf("Finished") != -1) {
                            stage.setCode(StageStatus.FINISHED);
                        }
                        sb.append(line).append("\n");
                    }
                    stage.setLog(sb.toString());
                    stage.setEndTime(new Date(file.lastModified()));
                }
                return stage;
            } catch (IOException e) {
                ExternalResourceException ere = new ExternalResourceException(
                        "Failed to read the download log file", e);
                ere.setDetail("file", medline_dir + "/get_baseline.log");
                throw ere;
            }
        }
        if (stage_name.equals("parse")) {
            try {
                final File file = new File(medline_dir,
                                           "medline_parser.log");
                stage = new StageStatus("parse");
                stage.setCode(StageStatus.NONE);
                if (file.exists()) {
                    stage.setCode(StageStatus.RUNNING);
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        if (line.indexOf("ERROR") != -1) {
                            stage.setCode(StageStatus.ERROR);
                        }
                        if (line.indexOf("Finished") != -1) {
                            stage.setCode(StageStatus.FINISHED);
                        }
                        sb.append(line).append("\n");
                    }
                    stage.setLog(sb.toString());
                    stage.setEndTime(new Date(file.lastModified()));
                }
                return stage;
            } catch (IOException e) {
                ExternalResourceException ere = new ExternalResourceException(
                        "Failed to read the parse log file", e);
                ere.setDetail("file", medline_dir + "/medline_parser.log");
                throw ere;
            }
        }
        if (stage_name.equals("process")) {
            try {
                final File file = new File(medline_dir,
                                           "process_medline_data.log");
                stage = new StageStatus("process");
                stage.setCode(StageStatus.NONE);
                if (file.exists()) {
                    stage.setCode(StageStatus.RUNNING);
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        if (line.indexOf("ORA-") != -1 ||
                            line.indexOf("Error") != -1) {
                            stage.setCode(StageStatus.ERROR);
                        }
                        if (line.indexOf("Finished") != -1) {
                            stage.setCode(StageStatus.FINISHED);
                        }
                        sb.append(line).append("\n");
                    }
                    stage.setLog(sb.toString());
                    stage.setEndTime(new Date(file.lastModified()));
                }
                return stage;
            } catch (IOException e) {
                ExternalResourceException ere = new ExternalResourceException(
                        "Failed to read the process log file", e);
                ere.setDetail("file", medline_dir + "/process_medline_data.log");
                throw ere;
            }
        }
        if (stage_name.equals("update")) {
            try {
                final File file = new File(medline_dir + "/update",
                                           "update_medline_data.log");
                stage = new StageStatus("update");
                stage.setCode(StageStatus.NONE);
                if (file.exists()) {
                    stage.setCode(StageStatus.RUNNING);
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        if (line.indexOf("ORA-") != -1 ||
                            line.indexOf("ERROR") != -1) {
                            stage.setCode(StageStatus.ERROR);
                        }
                        if (line.indexOf("FINISHED") != -1) {
                            stage.setCode(StageStatus.FINISHED);
                        }
                        sb.append(line).append("\n");
                    }
                    stage.setLog(sb.toString());
                    stage.setEndTime(new Date(file.lastModified()));
                }
                return stage;
            } catch (IOException e) {
                ExternalResourceException ere = new ExternalResourceException(
                        "Failed to read the update log file", e);
                ere.setDetail("file",
                              medline_dir + "/update/update_medline_data.log");
                throw ere;
            }
        }
        return stage;
    }

    /**
     * Returns all {@link StageStatus} for Meldine processing stages.
     * @return all {@link StageStatus} for Meldine processing stages
     * @throws MEMEException if failed to get status
     */
    public StageStatus[] getMedlineStatus() throws MEMEException {
        final String[] stages = new String[] {
                                "download", "parse", "process", "update"};
        final StageStatus[] status = new StageStatus[stages.length];
        for (int i = 0; i < stages.length; i++) {
            status[i] = getMedlineStageStatus(stages[i]);
        }
        return status;
    }

    /**
     * remove the Medline process log file
     * @param stage An object {@link String} representation of stage.
     * @throws MEMEException if failed to process the request.
     */
    public void clearMedlineStatus(String stage) throws MEMEException {
        final Map stage_files_map = new HashMap(4);
        final String medline_dir = ServerToolkit.getProperty("env.MEDLINE_DIR");
        stage_files_map.put("download",
                            new String[] {
                            medline_dir + "/get_baseline.log",
                            medline_dir + "/medline_parser.log",
                            medline_dir + "/process_medline_data.log",
                            medline_dir + "/update/update_medline_data.log"});
        stage_files_map.put("parse",
                            new String[] {
                            medline_dir + "/medline_parser.log",
                            medline_dir + "/process_medline_data.log",
                            medline_dir + "/update/update_medline_data.log"});
        stage_files_map.put("process",
                            new String[] {
                            medline_dir + "/process_medline_data.log",
                            medline_dir + "/update/update_medline_data.log"});
        stage_files_map.put("update",
                            new String[] {
                            medline_dir + "/update/update_medline_data.log"});
        if (stage_files_map.containsKey(stage)) {
            final String[] files = (String[]) stage_files_map.get(stage);
            for (int i = 0; i < files.length; i++) {
                File file = new File(files[i]);
                file.delete();
            }
        } else {
            BadValueException bve = new BadValueException("Invalid stage value");
            bve.setDetail("stage", stage);
            throw bve;
        }
    }

    /**
     * Deletes the xml file from medline update
     * @param file_name An object {@link String} representation of file name.
     * @throws MEMEException if failed to get status.
     */
    public void deleteUpdateMedlineXML(String file_name) throws MEMEException {
        final String medline_dir = ServerToolkit.getProperty("env.MEDLINE_DIR");
        try {
            final File file = new File(medline_dir + "/update", file_name);
            file.delete();
        } catch (Exception e) {
            ExternalResourceException ere = new ExternalResourceException(
                    "Failed to delete the xml file", e);
            ere.setDetail("file",
                          medline_dir + "/update/" + file_name);
            throw ere;

        }

    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     */
    public boolean requiresSession() {
        return false;
    }

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>
     */
    public boolean isRunning() {
        return false;
    }

    /**
     * Returns <code>false</code>.
     * @param context the {@link SessionContext}
     * @return <code>false</code>
     */
    public boolean isReEntrant(SessionContext context) {
      return false;
    }

}
