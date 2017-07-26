package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.common.*;
import gov.nih.nlm.meme.exception.*;
import gov.nih.nlm.meme.xml.*;

public class MedlineClient extends ClientAPI {

    private String midService;

    /**
     * MedlineClient
     * @throws MEMEException if the required properties are not set,
     *         or if the protocol handler cannot be instantiated.
     */
    public MedlineClient() throws MEMEException {
        super();
    }

    /**
     * MedlineClient
     *
     * @param midService String
     * @throws MEMEException if the required properties are not set,
     *         or if the protocol handler cannot be instantiated.
     */
    public MedlineClient(String midService) throws MEMEException {
        this();
        this.midService = midService;
    }

    /**
     * Request that the server download the MEDLINE baseline files to the
     * $MRD_HOME/Medline server directory.
     * @throws MEMEException if failed to download the files
     */
    public void downloadMedlineBaseline() throws MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();

        request.addParameter(new Parameter.Default("function",
                "downloadMedlineBaseline"));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }
    }

    /**
     * Requests that the server parses the MEDLINE Baseline XML files
     * in the $MRD_HOME/Medline server directory.
     * @param context either the release name or MID
     * @throws MEMEException if failed to parse the files
     */
    public void parseMedlineBaseline(String context) throws MEMEException {
        parseMedlineBaseline(context,null);
    }

    /**
     * Requests that the server parses the MEDLINE Baseline XML files
     * in the $MRD_HOME/Medline server directory.
     * @param context either the release name or MID
     * @param cutoff_date the last date to process medline data
     * @throws MEMEException if failed to parse the files
     */
    public void parseMedlineBaseline(String context, String cutoff_date) throws MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();

        request.addParameter(new Parameter.Default("function",
                "parseMedlineBaseline"));
        request.addParameter(new Parameter.Default("context", context));
        request.addParameter(new Parameter.Default("cutoff_date", cutoff_date));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }
    }
    /**
     * Requests that the server process the MEDLINE baseline data in the
     * $MRD_HOME/Medline server directory.
     * @param context either the release name or MID
     * @throws MEMEException if failed to process the data
     */
    public void processMedlineBaseline(String context) throws MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();

        request.addParameter(new Parameter.Default("function",
                "processMedlineBaseline"));

        request.addParameter(new Parameter.Default("context", context));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }
    }

    /**
     * Requests that the server downloads, parse, and process
     * the MEDLINE update XML files (in the $MRD_HOME/Medline/update server
     * directory).
     * @param context the release name or MID
     * @param cutoff_date the last date to process medline data
     * @throws MEMEException if failed to process the data
     */
    public void updateMedline(String context, String cutoff_date) throws MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();

        request.addParameter(new Parameter.Default("function", "updateMedline"));
        request.addParameter(new Parameter.Default("context", context));
        request.addParameter(new Parameter.Default("cutoff_date", cutoff_date));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }
    }

    /**
     * Requests that the server downloads, parse, and process
     * the MEDLINE update XML files (in the $MRD_HOME/Medline/update server
     * directory).
     * @param context the release name
     * @throws MEMEException if failed to process the data
     */
    public void updateMedline(String context) throws MEMEException {
        updateMedline(context, null);
    }

    /**
     * Returns the status of all Medline processing stages.
     * @return the status of all Medline processing stages
     * @throws MEMEException if failed to get status
     */
    public StageStatus[] getMedlineStatus() throws MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();
        request.addParameter(new Parameter.Default("function",
                "getMedlineStatus"));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }

        // Process and return response
        return (StageStatus[]) request.getReturnValue("status").getValue();
    }

    /**
     * Requests that the server delete the specified MEDLINE Update XML file
     * from the $MRD_HOME/Medline/update server directory.  This is used when
     * a parsing error is encountered in an update file.
     * @param file_name the file name to delete
     * @throws MEMEException if failed to get status
     */
    public void deleteUpdateMedlineXML(String file_name) throws MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();

        request.addParameter(new Parameter.Default("function",
                "deleteUpdateMedlineXML"));
        request.addParameter(new Parameter.Default("file_name", file_name));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }

    }

    /**
     * Returns the status of specified Medline processing stage.
     * @param stage_name the stage name
     * @return the status of specified Medline processing stage
     * @throws MEMEException if failed to get status
     */
    public StageStatus getMedlineStageStatus(String stage_name) throws
            MEMEException {
        // Prepare request document
        MEMEServiceRequest request = getServiceRequest();

        request.addParameter(new Parameter.Default("function",
                "getMedlineStageStatus"));
        request.addParameter(new Parameter.Default("stage_name", stage_name));

        // Issue request
        request = getRequestHandler().processRequest(request);

        // Handle exceptions
        Exception[] exceptions = request.getExceptions();
        if (exceptions.length > 0) {
            throw (MEMEException) exceptions[0];
        }

        // Process and return response
        return (StageStatus) request.getReturnValue("status").getValue();
    }

    /**
     * Requests that the server remove the log file for the
     * specified MEDLINE processing stage.
     * @param stage the stage name
     * @throws MEMEException if failed to process the request
     */
    public void clearMedlineStatus(String stage) throws MEMEException {
      // Prepare request document
      MEMEServiceRequest request = getServiceRequest();

      request.addParameter(new Parameter.Default("function", "clearMedlineStatus"));
      request.addParameter(new Parameter.Default("stage", stage));

      // Issue request
      request = getRequestHandler().processRequest(request);

      // Handle exceptions
      Exception[] exceptions = request.getExceptions();
      if (exceptions.length > 0) {
        throw (MEMEException) exceptions[0];
      }
    }


    protected MEMEServiceRequest getServiceRequest() {
      MEMEServiceRequest request = super.getServiceRequest();
      request.setService("MedlineService");
      request.setMidService(midService);
      request.setNoSession(true);
      return request;
  }

    public void setMidService(String midService) {
        this.midService = midService;
    }

    public String getMidService() {
        return midService;
    }
}
