/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ReportsClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.ActionReport;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.ReportStyle;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.ArrayList;
import java.util.Date;
import gov.nih.nlm.meme.common.SourceMetadataReport;

/**
 * This client API is used to generate concept reports
 * from the MID.  See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, generating reports
 * is as simple as instantiating {@link ReportsClient} and
 * calling either <code>getReport</code>, <code>getReports</code>,
 * or <code>getReportForAtom</code>.
 * For example,
 * <pre>
 *   // Instantiate client with default mid service
 *   ReportsClient reports = new ReportsClient();
 *
 *   // Generate a single report for concept_id 100000
 *   String single_id_report = reports.getReport(100000);
 *
 *   // Generate a single report for cui C0000039
 *   String single_cui_report = reports.getReport("C0000039");
 *
 *   // Generate a single report for atom 12345
 *   String single_atom_report = reports.getReportForAtom(12345);
 *
 *   // Generate multiple reports for concept_ids
 *   String multiple_reports = reports.getReports(
 *                             new int[] {100000, 100001, 100002} );
 *
 *   // Generate multiple reports for cuis_ids
 *   String multiple_cui_reports = reports.getReports(
 *                             new String[] {"C0000039", "C0000040"} );
 * </pre>
 *
 * As with earlier versions of the reports, you can change the display
 * options for context relationships and regular relationships.  In
 * each case there are four options.  The relationship view can
 * be set in one of these ways,
 * <pre>
 *    // Do not display relationships in report
 *    reports.setRelationshipViewMode(ReportsClient.NONE);
 *
 *    // Set to default mode (show winning rels)
 *    reports.setRelationshipViewMode(ReportsClient.DEFAULT);
 *
 *    // Set to view that groups XR rels together
 *    // This mode is like default, it shows winning rels
 *    // but it also groups any XR rels with the non-XR rels
 *    // that share concept_id_2
 *    reports.setRelationshipViewMode(ReportsClient.XR);
 *
 *    // Show all relationships
 *    reports.setRelationshipViewMode(ReportsClient.ALL);
 * </pre>
 * Once this mode is set, it is used for all reports generated after that
 * time, or until it is set to something else.  The context relationship
 * view can be changed, in one of these ways:
 * <pre>
 *    // Do not display relationships in report
 *    reports.setContextRelationshipViewMode(ReportsClient.NONE);
 *
 *    // Set to default mode (show PAR, CHD only if concept
 *    // has no rels and no CONTEXT attributes)
 *    reports.setContextRelationshipViewMode(ReportsClient.DEFAULT);
 *
 *    // Use default mode but also show SIB
 *    reports.setContextRelationshipViewMode(ReportsClient.INCLUDE_SIB);
 *
 *    // Show all cxt relationships
 *    reports.setContextRelationshipViewMode(ReportsClient.ALL);
 * </pre>
 * This class now supports all of the extended report funcationality
 * available to the <code>$MEME_HOME/bin/xreports.pl</code> Perl client.
 * In particular, through this client you now have the ability to
 * set the content type of the report and add style elements.  For example,
 * view can be changed, in one of these ways:
 * <pre>
 *    // Have the server produce an HTML-ized report
 *    reports.setContentType("text/html");
 *
 *    // Have the server produce an enscript-enabled report
 *    reports.setContentType("text/enscript");
 *
 *    // Clear all styles associated with this client
 *    reports.clearStyles();
 *
 *    // Create a style
 *    ReportStyle style = new ReportStyle.Default();
 *    style.setRegexp(".*MSH.*");
 *    style.setSections(new String[] {ReportStyle.ATOMS, ReportStyle.DEF});
 *    style.setColor("#ff0000");
 *    style.setBold(true);
 *
 *    // Add this style to the client, which will cause
 *    // it to be used for all subsequent requests.
 *    reports.addStyle(style);
 * </pre>
 *
 * <i>Note:</i> by default, concept reports will be generated
 * using the <code>editing-db</code> database.  If you
 * want to generate reports using something other than
 * <code>editing-db</code>, you can either pass the mid
 * service name to the constructor or call the {@link #setMidService(String)}
 * method.
 * <p>
 * <i>Note:</i> This class was not designed to be thread-safe.  If you
 * desire to use multiple threads, make sure that each thread instantiates
 * its own copy of the class.
 *
 * @see ClientAPI
 * @author MEME Group
 */
public class ReportsClient extends ClientAPI {

  //
  // Fields
  //

  /**
   * Used to indicate that no relationships should be shown.
   */
  public final static int NONE = 0;

  /**
   * Used to indicate that the default view should be used.
   */
  public final static int DEFAULT = 1;

  /**
   * Used to indicate that all relationships should be shown.
   */
  public final static int ALL = 3;

  /**
   * Used to indicate that winning relationships should be
   * shown and any rels matching XR should also be shown.
   */
  public final static int XR = 2;

  /**
   * Used to indicate that the default view for context
   * relationships should be used with the addition of SIB rels.
   */
  public final static int INCLUDE_SIB = 2;

  /**
   * Used to indicate the color red.
   */
  public final static String RED = "#FB0000";

  /**
   * Used to indicate the color green.
   */
  public final static String GREEN = "#00FB00";

  /**
   * Used to indicate the color blue.
   */
  public final static String BLUE = "#0000FB";

  // Fields to track relationship and context relationship view options.
  private int rel_opt = DEFAULT;
  private int cxt_rel_opt = DEFAULT;
  private int max_r_rel_count = -1;

  private String mid_service = null;

  // Fields use to handle HTML/styled content type
  private String content_type = "text/plain";
  private ArrayList styles = new ArrayList();
  private boolean include_or_exclude_lats = false;
  private String[] selected_languages = new String[0];

  //
  // Constructors
  //

  /**
       * Instantiates a {@link ReportsClient} connected to the specified mid service.
   * Valid mid service names can be found by calling
   * {@link gov.nih.nlm.meme.MIDServices}.getDbServicesList().
   * @param mid_service a valid MID service name
   * @throws MEMEException if the required properties are not sset
   *         or if the protocol handler cannot be instantiated
   */
  public ReportsClient(String mid_service) throws MEMEException {
    this();
    this.mid_service = mid_service;
  }

  /**
   * Instantiates a {@link ReportsClient} connected to the default mid service.
   * @throws MEMEException if the required properties are not set
   *         or if the protocol handler cannot be instantiated
   */
  public ReportsClient() throws MEMEException {
    super();
    mid_service = "editing-db";
  }

  //
  // Methods
  //

  /**
   * Sets the mid service.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Determines whether languages are included or excluded to read.
   * @return <code>true</code>if include; <code>false</code>otherwise.
   */
  public boolean includeOrExcludeLanguages() {
    return include_or_exclude_lats;
  }

  /**
   * Returns selected languages when reading an atom.
   * @return selected languages.
   */
  public String[] getReadLanguages() {
    return selected_languages;
  }

  /**
   * Sets any languages to exclude when reading an atom.
   * @param lats A languages to exclude.
   */
  public void setReadLanguagesToExclude(String[] lats) {
    this.selected_languages = lats;
    include_or_exclude_lats = false;
  }

  /**
   * Sets any languages to include when reading an atom.
   * @param lats A languages to include.
   */
  public void setReadLanguagesToInclude(String[] lats) {
    this.selected_languages = lats;
    include_or_exclude_lats = true;
  }

  /**
   * Sets the relationships view mode. This should be one of:
   * <ul>
   *   <li>{@link #NONE}</li>
   *   <li>{@link #DEFAULT}</li>
   *   <li>{@link #XR}</li>
   *   <li>{@link #ALL}</li>
   * </ul>
   * @param mode the relationships view mode
   */
  public void setRelationshipViewMode(int mode) {
    rel_opt = mode;
  }

  /**
   * Sets the context relationships view mode. This should be one of:
   * <ul>
   *   <li>{@link #NONE}</li>
   *   <li>{@link #DEFAULT}</li>
   *   <li>{@link #INCLUDE_SIB}</li>
   *   <li>{@link #ALL}</li>
   * </ul>
   * @param mode the context relationships view mode
   */
  public void setContextRelationshipViewMode(int mode) {
    cxt_rel_opt = mode;
  }

  /**
   * Sets the max reviewed relationship count.
   * @param max_r_rel_count the maximum reviewed relationship count
   */
  public void setMaxReviewedRelationshipCount(int max_r_rel_count) {
    this.max_r_rel_count = max_r_rel_count;
  }

  /**
   * Returns the content type.
   * @return the content type.
   */
  public String getContentType() {
    return content_type;
  }

  /**
   * Sets the content type.
   * Allowable values are "text/plain", "text/html", "text/enscript"
   * @param content_type the MIME content type
   */
  public void setContentType(String content_type) {
    this.content_type = content_type;
  }

  /**
   * Adds a new style into style list.
   * @param style a {@link ReportStyle} to add
   */
  public void addStyle(ReportStyle style) {
    style.setContentType(getContentType());
    styles.add(style);
  }

  /**
   * Removes the specified style.
   * @param style the {@link ReportStyle}
   */
  public void removeStyle(ReportStyle style) {
    styles.remove(style);
  }

  /**
   * Clears the style list.
   */
  public void clearStyles() {
    styles.clear();
  }

  /**
   * Returns an {@link ActionReport} for a specified molecule id (<B>SERVER CALL</B>).
   * @param molecule_id the <code>int</code> molecule id
   * @return the {@link ActionReport}
   * @throws MEMEException if anything goes wrong
   */
  public ActionReport getActionReport(int molecule_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("function", "action_report"));
    request.addParameter(new Parameter.Default("molecule_id", molecule_id));

    // Add Style parameters
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    ActionReport report =
        (ActionReport) request.getReturnValue("action_report").getValue();
    return report;
  }

  /**
   * Returns an action report for a specified molecule id (<B>SERVER CALL</B>).
   * @param molecule_id the <code>int</code> molecule id
   * @return an action report for a specified molecule id
   * @throws MEMEException if anything goes wrong
   */
  public String getActionReportDocument(int molecule_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("function",
                                               "action_report_document"));
    request.addParameter(new Parameter.Default("molecule_id", molecule_id));

    // Add Style parameters
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report =
        (String) request.getReturnValue("action_report_document").getValue();
    return report;
  }

  /**
       * Returns raw editing report data for a "days ago" lookup (<B>SERVER CALL</B>).
   * This data is generated by
   * calling the <code>$MEME_HOME/bin/editing.csh</code> script.  This data
   * is used by the action harvester to display a summary report of recent
   * editing activity.
   * @param days_ago an <code>int</code> number of days before today to
   *        generate report data for
   * @return a {@link String}<code>[][]</code> matrix of editing report data
   * @throws MEMEException if anything goes wrong
   */
  public String[][] getEditingReportData(int days_ago) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("function", "editing_report"));
    request.addParameter(new Parameter.Default("days_ago", days_ago));

    // Add Style parameters
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String[][] report =
        (String[][]) request.getReturnValue("editing_report").getValue();
    return report;
  }

  /**
       * Returns raw editing report data for a "worklist" lookup (<B>SERVER CALL</B>).
   * This data is generated by calling the
   * <code>$MEME_HOME/bin/editing.csh</code> script and is used by the action
   * harvester to display a summary report of recent editing activity.
   * @param worklist the worklist name to generate data for
   * @param start_date the create date of the worklist
   * @param end_date the stamp date of the worklist (or the current date)
   * @return a {@link String}<code>[][]</code> matrix of editing report data
   * @throws MEMEException if anything goes wrong
   */
  public String[][] getEditingReportData(String worklist,
                                         Date start_date, Date end_date) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("function", "editing_report"));
    request.addParameter(new Parameter.Default("worklist", worklist));
    request.addParameter(new Parameter.Default("start_date", start_date));
    request.addParameter(new Parameter.Default("end_date", end_date));

    // Add Style parameters
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String[][] report =
        (String[][]) request.getReturnValue("editing_report").getValue();
    return report;
  }

  //
  // getReport(s) for concept_id(s)
  //

  /**
       * Returns a concept report for the specified concept id (<B>SERVER CALL</B>).
   * @param concept_id the <code>int</code> concept id
   * @return a report for the specified concept id
   * @throws MEMEException if anything goes wrong
   */
  public String getReport(int concept_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add concept parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("concept_id", concept_id));
    request.addParameter(new Parameter.Default("rel_opt", rel_opt));
    request.addParameter(new Parameter.Default("cxt_rel_opt", cxt_rel_opt));
    if (max_r_rel_count != -1) {
      request.addParameter(
          new Parameter.Default("max_r_rel_count", max_r_rel_count));

      // Add Style parameters
    }
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report = (String) request.getReturnValue("report").getValue();
    return report;
  }

  /**
       * Returns concept reports for the specified concept id (<B>SERVER CALL</B>)s.
   * @param concept_ids the concept ids
   * @return concept reports for the specified concept ids.
   * @throws MEMEException if anything goes wrong
   */
  public String getReports(int[] concept_ids) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    request.setNoSession(true);

    // Add concept parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("concept_ids", concept_ids));
    request.addParameter(new Parameter.Default("rel_opt", rel_opt));
    request.addParameter(new Parameter.Default("cxt_rel_opt", cxt_rel_opt));
    if (max_r_rel_count != -1) {
      request.addParameter(
          new Parameter.Default("max_r_rel_count", max_r_rel_count));

      // Add Style parameters
    }
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report = (String) request.getReturnValue("report").getValue();
    return report;
  }

  //
  // getReport(s) for cui(s)
  //

  /**
   * Returns a concept report for the specified CUI value (<B>SERVER CALL</B>).
   * @param cui a {@link String} representation of a CUI
   * @return a concept report for the specified CUI value
   * @throws MEMEException if anything goes wrong
   */
  public String getReport(String cui) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add concept parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("cui", cui));
    request.addParameter(new Parameter.Default("rel_opt", rel_opt));
    request.addParameter(new Parameter.Default("cxt_rel_opt", cxt_rel_opt));
    if (max_r_rel_count != -1) {
      request.addParameter(
          new Parameter.Default("max_r_rel_count", max_r_rel_count));

      // Add Style parameters
    }
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report = (String) request.getReturnValue("report").getValue();
    return report;
  }

  /**
   * Returns concept reports for the specified CUI values (<B>SERVER CALL</B>).
   * @param cuis a {@link String} representation of a set of CUIs
   * @return concept reports for the specified CUI values.
   * @throws MEMEException if anything goes wrong
   */
  public String getReports(String[] cuis) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    request.setNoSession(true);

    // Add concept parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("cuis", cuis));
    request.addParameter(new Parameter.Default("rel_opt", rel_opt));
    request.addParameter(new Parameter.Default("cxt_rel_opt", cxt_rel_opt));
    if (max_r_rel_count != -1) {
      request.addParameter(
          new Parameter.Default("max_r_rel_count", max_r_rel_count));

      // Add Style parameters
    }
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report = (String) request.getReturnValue("report").getValue();
    return report;
  }

  //
  // getReport(s)ForAtom(s)
  //

  /**
   * Returns a concept report for the specified atom id (<B>SERVER CALL</B>).
   * @param atom_id the <code>int</code> atom id
   * @return a concept report for the specified atom id
   * @throws MEMEException if anything goes wrong
   */
  public String getReportForAtom(int atom_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add concept parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("atom_id", atom_id));
    request.addParameter(new Parameter.Default("rel_opt", rel_opt));
    request.addParameter(new Parameter.Default("cxt_rel_opt", cxt_rel_opt));
    if (max_r_rel_count != -1) {
      request.addParameter(
          new Parameter.Default("max_r_rel_count", max_r_rel_count));

      // Add Style parameters
    }
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report = (String) request.getReturnValue("report").getValue();
    return report;
  }

  /**
   * Returns concept reports for the specified atom ids (<B>SERVER CALL</B>).
   * @param atom_ids an <code>int[]</code> of atom ids
   * @return concept reports for the specified atom ids
   * @throws MEMEException if anything goes wrong
   */
  public String getReportsForAtoms(int[] atom_ids) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    request.setNoSession(true);

    // Add concept parameters
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));
    request.addParameter(new Parameter.Default("atom_ids", atom_ids));
    request.addParameter(new Parameter.Default("rel_opt", rel_opt));
    request.addParameter(new Parameter.Default("cxt_rel_opt", cxt_rel_opt));
    if (max_r_rel_count != -1) {
      request.addParameter(
          new Parameter.Default("max_r_rel_count", max_r_rel_count));

      // Add Style parameters
    }
    request.addParameter(new Parameter.Default("content_type", content_type));
    for (int i = 0; i < styles.size(); i++) {
      request.addParameter( ( (ReportStyle) styles.get(i)).getParameter(i));
    }

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    String report = (String) request.getReturnValue("report").getValue();
    return report;
  }

  /**
   * Returns an {@link SourceMetadataReport} .
   * @return the {@link ActionReport}
   * @throws MEMEException if anything goes wrong
   */
  public SourceMetadataReport getSourceMetadataReport() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ReportsGenerator");
    request.setMidService(mid_service);
    MEMEToolkit.trace("mid_service= " + mid_service);
    request.setNoSession(true);

    // Add parameters
    request.addParameter(new Parameter.Default("function", "source_metadata_report"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    SourceMetadataReport report =
        (SourceMetadataReport) request.getReturnValue("source_metadata_report").getValue();
    return report;
  }
}
