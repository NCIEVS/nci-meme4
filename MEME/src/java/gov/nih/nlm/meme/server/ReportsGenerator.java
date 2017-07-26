/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ReportsGenerator
 *
 * Changes
 *   04/10/2006 TTN (1-AV6X1) : optimize queries to pick up the correct previous name for updated sources
 *   02/15/2006 BAC (1-79HNF): If -url params not supplied and properties file
 *     doesn't have URL props, then provide no links for those kinds of URLs
 *     in the HTML report.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.action.ActionReport;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.ReportStyle;
import gov.nih.nlm.meme.common.ReportsAtomComparator;
import gov.nih.nlm.meme.common.ReportsRelationshipRestrictor;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.common.SourceDifference.Value;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Map;
import java.sql.SQLException;
import gov.nih.nlm.meme.common.SourceMetadataReport;
import java.util.Iterator;
import gov.nih.nlm.meme.common.SourceDifference;
import java.util.Set;
import gov.nih.nlm.meme.common.Source;
import java.lang.Thread;

/**
 * Handles requests for various kinds
 * of reports.  Initially, it was implemented to generate concept reports,
 * for the {@link ReportsClient} but later was expanded to generate
 * molecular action summary reports and to generate editing report data for the
 * "action harvester".
 *
 * @author MEME Group
 */
public class ReportsGenerator
    implements MEMEApplicationService, ServerThread {

  //
  // Constants
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
   * Used to indicate that winning relationships should be shown and any rels
   * matching XR should also be shown.
   */
  public final static int XR = 2;

  /**
   * Used to indicate that the default view for context relationships should
   * be used with the addition of SIB rels.
   */
  public final static int INCLUDE_SIB = 2;

  //
  // Fields
  //

  private MEMEApplicationServer server = null;
  private Thread thread = null;
  private static SourceMetadataReport sourceMetadataReport;
  private static boolean cacheReport;

  private static HashMap rel_name_map = new HashMap();
  static {
    rel_name_map.put("RT", "REL");
    rel_name_map.put("NT", "NRW");
    rel_name_map.put("SY", "NSY");
    rel_name_map.put("BT", "BRD");
    rel_name_map.put("LK", "LIK");
    rel_name_map.put("XR", "NOT");
    rel_name_map.put("XS", "NSY");
  }

  private static String[] rel_tag_map = new String[6];
  static {
    rel_tag_map[0] = "DEMOTED RELATED CONCEPT(S)";
    rel_tag_map[1] = "XR(S) AND CORRESPONDING RELATIONSHIP(S)";
    rel_tag_map[2] = "NEEDS REVIEW RELATED CONCEPT(S)";
    rel_tag_map[3] = "REVIEWED RELATED CONCEPT(S)";
    rel_tag_map[4] = "UNREVIEWED/SUPPLEMENTARY CHEMICAL RELATED CONCEPT";
    rel_tag_map[5] = "ALL RELATIONSHIP(S)";
  }

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    MEMEServiceRequest request = context.getServiceRequest();
    MEMEToolkit.trace("ReportsGenerator.processRequest() - " + request);

    String[] selected_languages = new String[0];
    boolean include_or_exclude = false;

    if (request.getParameter("selected_languages") != null) {
      selected_languages = (String[]) request.getParameter("selected_languages").
          getValue();

    }
    if (request.getParameter("include_or_exclude") != null) {
      include_or_exclude = request.getParameter("include_or_exclude").
          getBoolean();
    }
    Ticket ticket = Ticket.getReportsTicket();
    if (include_or_exclude) {
      ticket.setReadLanguagesToInclude(selected_languages);
    }
    else {
      ticket.setReadLanguagesToExclude(selected_languages);

    }
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    // If no function, this is the old reports code
    // for backwards compatability
    String function = null;
    if (request.getParameter("function") != null) {
      function = (String) request.getParameter("function").getValue();
    }
    else {
      function = "concept_report";

    }
    if (function.equals("action_report")) {
      ActionReport report = null;
      if (request.getParameter("molecule_id") != null) {
        int molecule_id = request.getParameter("molecule_id").getInt();
        report = getActionReport(molecule_id, context);
      }
      request.addReturnValue(new Parameter.Default("action_report", report));

    }
    else if (function.equals("action_report_document")) {
      String report = null;
      if (request.getParameter("molecule_id") != null) {
        int molecule_id = request.getParameter("molecule_id").getInt();
        report = getActionReportDocument(molecule_id, context);
      }
      request.addReturnValue(new Parameter.Default("action_report_document",
          report));

    }
    else if (function.equals("editing_report")) {

      int days_ago = 0;
      String worklist = null;
      Date start_date = null;
      Date end_date = null;
      if (request.getParameter("days_ago") != null) {
        days_ago = request.getParameter("days_ago").getInt();
      }
      else if (request.getParameter("worklist") != null) {
        worklist = (String) request.getParameter("worklist").getValue();
        start_date = (Date) request.getParameter("start_date").getValue();
        end_date = (Date) request.getParameter("end_date").getValue();
      }

      String mid_service = request.getMidService();

      String[][] editing_report =
          getEditingReportData(days_ago, worklist, start_date, end_date,
                               mid_service);

      request.addReturnValue(
          new Parameter.Default("editing_report", editing_report));

    }
    else if (function.equals("concept_report")) {

      //
      // Must be concept report
      //

      String report = null;
      String line_end = System.getProperty("line.separator");

      // Concept id report
      if (request.getParameter("concept_id") != null) {
        int concept_id = request.getParameter("concept_id").getInt();
        report = getReport(data_source.getConcept(concept_id, ticket), context,
                           0);

        // CUI report
      }
      else if (request.getParameter("cui") != null) {
        String scui = (String) request.getParameter("cui").getValue();
        CUI cui = new CUI(scui);
        report = getReport(data_source.getConcept(cui, ticket), context, 0);

        // Atom id report
      }
      else if (request.getParameter("atom_id") != null) {
        int atom_id = request.getParameter("atom_id").getInt();
        int concept_id = data_source.getAtomWithName(atom_id, null).getConcept().
            getIdentifier().intValue();
        report = getReport(data_source.getConcept(concept_id, ticket), context,
                           atom_id);

        // Multiple concept id reports
      }
      else if (request.getParameter("concept_ids") != null) {
        StringBuffer reports = new StringBuffer(10000);
        int[] concept_ids = (int[]) request.getParameter("concept_ids").
            getValue();
        for (int i = 0; i < concept_ids.length; i++) {
          reports.append(getReport(data_source.getConcept(concept_ids[i],
              ticket), context, 0));
          reports.append(line_end);
          reports.append(
              "_____________________________________________________");
          reports.append(line_end);
        }
        report = reports.toString();

        // Multiple CUI reports
      }
      else if (request.getParameter("cuis") != null) {
        StringBuffer reports = new StringBuffer(10000);
        String[] cuis = (String[]) request.getParameter("cuis").getValue();
        for (int i = 0; i < cuis.length; i++) {
          reports.append(getReport(data_source.getConcept(new CUI(cuis[i]),
              ticket), context, 0));
          reports.append(line_end);
          reports.append(
              "_____________________________________________________");
          reports.append(line_end);
        }
        report = reports.toString();

        // Multiple atom id reports
      }
      else if (request.getParameter("atom_ids") != null) {
        StringBuffer reports = new StringBuffer(10000);
        int[] atom_ids = (int[]) request.getParameter("atom_ids").getValue();
        for (int i = 0; i < atom_ids.length; i++) {
          int concept_id = data_source.getAtomWithName(atom_ids[i], null).
              getConcept().getIdentifier().intValue();
          reports.append(getReport(data_source.getConcept(concept_id, ticket),
                                   context, atom_ids[i]));
          reports.append(line_end);
          reports.append(
              "_____________________________________________________");
          reports.append(line_end);
        }
        report = reports.toString();

      }
      else {
        throw new MEMEException("Badly formatted request (2)");
      }

      // Sets the return value for concept report
      request.addReturnValue(new Parameter.Default("report", report));
    }
    else if (function.equals("source_metadata_report")) {
      if (!cacheReport) {
        throw new DataSourceException("SourceMetadataReport is not ready yet. Please try again later");
      }
      request.addReturnValue(new Parameter.Default("source_metadata_report",
          sourceMetadataReport));
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
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }

  //
  // Private Methods
  //

  /**
   * Returns an action report for the specified molecule id.
   *
   * @param molecule_id the molecule id
   * @param context the {@link SessionContext}
   * @return the {@link ActionReport}
   * @throws MEMEException if failed to produced an action report
   */
  private ActionReport getActionReport(int molecule_id, SessionContext context) throws
      MEMEException {

    Ticket ticket = Ticket.getActionsTicket();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    MolecularAction ma = data_source.getFullMolecularAction(molecule_id);

    if (ma == null) {
      throw new MEMEException("No data for molecular action " + molecule_id +
                              ".");
    }

    ActionReport report = new ActionReport();
    report.setMolecularAction(ma);

    // find source concept whether dead or alive
    Concept source = null;
    try {
      source = data_source.getConcept(ma.getSourceIdentifier().intValue(),
                                      ticket);
      report.setSource(source);
    }
    catch (DataSourceException dse) {
      // do nothing
    }
    if (source == null) {
      try {
        source = data_source.getDeadConcept(ma.getSourceIdentifier().intValue());
        report.setSource(source);
      }
      catch (DataSourceException dse) {
        // do nothing
      }
    }

    // find target concept whether dead or alive
    Concept target = null;
    if (ma.getTargetIdentifier() != null &&
        ma.getTargetIdentifier().intValue() != 0) {
      try {
        target = data_source.getConcept(ma.getTargetIdentifier().intValue(),
                                        ticket);
        report.setTarget(target);
      }
      catch (DataSourceException dse) {
        // do nothing
      }
      if (target == null) {
        try {
          target = data_source.getDeadConcept(ma.getTargetIdentifier().intValue());
          report.setTarget(target);
        }
        catch (DataSourceException dse) {
          // do nothing
        }
      }
    }

    // read from sub actions
    AtomicAction[] actions = ma.getAtomicActions();
    for (int i = 0; i < actions.length; i++) {
      String affected_table = actions[i].getAffectedTable();
      report.addAtomicAction(actions[i]);
      if (affected_table.equals("C")) {
        int atom_id = actions[i].getRowIdentifier().intValue();
        Atom atom = null;
        try {
          atom = data_source.getAtomWithName(atom_id, null);
          report.addAtom(atom);
        }
        catch (DataSourceException dse) {
          atom = data_source.getDeadAtom(atom_id);
          report.addAtom(atom);
        }
      }
      else if (affected_table.equals("R")) {
        int rel_id = actions[i].getRowIdentifier().intValue();
        Relationship rel = null;
        try {
          rel = data_source.getRelationship(rel_id, ticket);
          report.addRelationship(rel);
        }
        catch (Exception e) {
          rel = data_source.getDeadRelationship(rel_id);
          report.addRelationship(rel);
        }
      }
      else if (affected_table.equals("A")) {
        int attr_id = actions[i].getRowIdentifier().intValue();
        Attribute attr = null;
        try {
          attr = data_source.getAttribute(attr_id, ticket);
          report.addAttribute(attr);
        }
        catch (Exception e) {
          attr = data_source.getDeadAttribute(attr_id);
          report.addAttribute(attr);
        }
      }
    }

    return report;
  }

  /**
   * Returns the action report document for the specified molecule id.
   *
   * @param molecule_id the molecule id
   * @param context the {@link SessionContext}
   * @return the action report document for the specified molecule id
   * @throws MEMEException if failed to produced an action report document
   */
  private String getActionReportDocument(int molecule_id,
                                         SessionContext context) throws
      MEMEException {

    Ticket ticket = Ticket.getActionsTicket();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    MolecularAction ma = data_source.getFullMolecularAction(molecule_id);

    if (ma == null) {
      return "No data for molecular action " + molecule_id + ".";
    }

    String line_end = System.getProperty("line.separator");

    // find source concept whether dead or alive
    Concept source = null;
    try {
      source = data_source.getConcept(ma.getSourceIdentifier().intValue(),
                                      ticket);
    }
    catch (DataSourceException dse) {
      // do nothing
    }
    if (source == null) {
      try {
        source = data_source.getDeadConcept(ma.getSourceIdentifier().intValue());
      }
      catch (DataSourceException dse) {
        // do nothing
      }
    }

    // find target concept whether dead or alive
    Concept target = null;
    if (ma.getTargetIdentifier() != null &&
        ma.getTargetIdentifier().intValue() != 0) {
      try {
        target = data_source.getConcept(ma.getTargetIdentifier().intValue(),
                                        ticket);
      }
      catch (DataSourceException dse) {
        // do nothing
      }
      if (target == null) {
        try {
          target = data_source.getDeadConcept(ma.getTargetIdentifier().intValue());
        }
        catch (DataSourceException dse) {
          // do nothing
        }
      }
    }

    StringBuffer report = new StringBuffer(1000);
    StringBuffer rows = new StringBuffer(500);

    // place holder of source and target href
    StringBuffer source_href = new StringBuffer(500);
    StringBuffer target_href = new StringBuffer(500);

    boolean show_affected_rows = false;

    // contains service db and unedited-db
    String[] dbs = new String[] {
        data_source.getServiceName(),
        MIDServices.getService("unedited-db")};

    String new_value = null;

    // The ff flag is used whether or not to display the affected table name
    boolean c_flag = false;
    boolean r_flag = false;
    boolean a_flag = false;
    boolean cs_flag = false;

    // The ff string buffer is used as a place holder of the affected table
    StringBuffer c_rows = new StringBuffer(500);
    StringBuffer r_rows = new StringBuffer(500);
    StringBuffer a_rows = new StringBuffer(500);
    StringBuffer cs_rows = new StringBuffer(500);

    // read from sub actions
    AtomicAction[] actions = ma.getAtomicActions();
    for (int i = 0; i < actions.length; i++) {
      String affected_table = actions[i].getAffectedTable();
      String atomic_action = actions[i].getActionName();
      String action_name = actions[i].getActionName();
      if (affected_table.equals("C")) {
        if (!c_flag) {
          c_rows.append("</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;");
          c_rows.append("<B>").append("Atom").append("(s)").append("</B><BR>");
          c_flag = true;
        }
        int atom_id = actions[i].getRowIdentifier().intValue();
        Atom atom = null;
        try {
          atom = data_source.getAtomWithName(atom_id, null);
        }
        catch (DataSourceException dse) {
          atom = data_source.getDeadAtom(atom_id);
        }
        c_rows.append(
            "</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        if (atom == null) {
          c_rows.append("Atom data could not be found<BR>");
        }
        else {
          c_rows.append(atom).append(" (").append(action_name).append(")<BR>");

        }
      }
      else if (affected_table.equals("R")) {
        if (!r_flag) {
          r_rows.append("</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;")
              .append("<B>").append("Relationship").append("(s)")
              .append("</B><BR>");
          r_flag = true;
        }
        int rel_id = actions[i].getRowIdentifier().intValue();
        Relationship rel = null;
        try {
          rel = data_source.getRelationship(rel_id, ticket);
        }
        catch (Exception e) {
          rel = data_source.getDeadRelationship(rel_id);
        }
        r_rows.append(
            "</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        if (rel == null) {
          r_rows.append("Relationship data could not be found<BR>");
        }
        else if (rel_id > 0) {
          String concept = rel.getConcept().getIdentifier().toString();
          String related_concept = rel.getRelatedConcept().getIdentifier().
              toString();
          r_rows.append(mapHref(concept, dbs, line_end))
              .append(" --> ")
              .append(rel.getName())
              .append(" --> ")
              .append(mapHref(related_concept, dbs, line_end))
              .append(" (").append(action_name).append(")<BR>");
        }
        else {
          source_href.append(rel.getConcept().getIdentifier().toString());
        }

      }
      else if (affected_table.equals("A")) {
        if (!a_flag) {
          a_rows.append("</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;")
              .append("<B>").append("Attribute").append("(s)")
              .append("</B><BR>");
          a_flag = true;
        }
        int attr_id = actions[i].getRowIdentifier().intValue();
        Attribute attr = null;
        try {
          attr = data_source.getAttribute(attr_id, ticket);
        }
        catch (Exception e) {
          attr = data_source.getDeadAttribute(attr_id);
        }
        a_rows.append(
            "</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        if (attr == null) {
          a_rows.append("Attribute data could not be found<BR>");
        }
        else {
          a_rows.append("<I>").append(attr.getName()).append("</I>: ")
              .append(attr.getValue()).append(" (").append("action_name").
              append(")<BR>");

        }
      }
      else if (affected_table.equals("CS")) {
        if (!cs_flag) {
          cs_rows.append("</TT><TT>&nbsp;&nbsp;&nbsp;&nbsp;");
          cs_rows.append("<B>").append("Concept").append("</B> ")
              .append(action_name).append(")<BR>");
          cs_flag = true;
        }
      }

      if (!atomic_action.equals("I") && !atomic_action.equals("D")) {
        new_value = actions[i].getNewValue();
      }
    }

    // sort and append affected rows
    rows.append(a_rows).append(c_rows).append(cs_rows).append(r_rows);

    // map source_href
    if (ma.getSourceIdentifier().intValue() > 0) {
      source_href.append(mapHref(ma.getSourceIdentifier().toString(), dbs,
                                 line_end))
          .append("<BR>");
    }
    else {
      source_href.append(ma.getSourceIdentifier().toString());

      // map target_href
    }
    if (ma.getTargetIdentifier().intValue() > 0) {
      target_href.append(mapHref(ma.getTargetIdentifier().toString(), dbs,
                                 line_end))
          .append("<BR>");
    }
    else {
      target_href.append(ma.getTargetIdentifier().toString());

      //
      // map action report
      //

    }
    String action_name = ma.getActionName();
    report.append("<TABLE ALIGN=CENTER WIDTH=90%<TR><TD>");
    report.append("<TT><FONT size=+1><B>");
    if (action_name.startsWith("MOLECULAR")) {
      report.append(action_name.substring(10));
    }
    else {
      report.append(action_name);
    }
    report.append("</B></FONT><BR>");
    report.append("</TT><TT>");

    if (action_name.equals("MOLECULAR_INSERT") ||
        action_name.equals("MOLECULAR_DELETE")) {
      if (action_name.equals("MOLECULAR_INSERT")) {
        report.append("into");
      }
      else {
        report.append("from");
      }
      if (source != null) {
        report.append("&nbsp;&nbsp;")
            .append(source.getPreferredAtom().toString())
            .append(source_href.toString());
      }
      else {
        report.append("&nbsp;&nbsp;<span id=\"red\">[Source concept information is unavailable]</span>");
      }
      show_affected_rows = true;

    }
    else
    if (action_name.startsWith("MOLECULAR_CHANGE") ||
        action_name.equals("MOLECULAR_CONCEPT_APPROVAL")) {
      if (source != null) {
        report.append("&nbsp;&nbsp;Source: ")
            .append(source.getPreferredAtom().toString())
            .append(source_href.toString());
      }
      else {
        report.append("&nbsp;&nbsp;Source: <span id=\"red\">[Source concept information is unavailable]</span>");
      }
      if (action_name.startsWith("MOLECULAR_CHANGE")) {
        report.append("</TT><TT>&nbsp;&nbsp;New Value: ")
            .append(new_value).append("<BR>");
      }
      show_affected_rows = true;
    }
    else
    if (action_name.equals("MOLECULAR_MERGE") ||
        action_name.equals("MOLECULAR_SPLIT") ||
        action_name.equals("MOLECULAR_MOVE")) {
      if (source != null) {
        report.append("&nbsp;&nbsp;Source: ")
            .append(source.getPreferredAtom().toString())
            .append(source_href.toString());
      }
      else {
        report.append("&nbsp;&nbsp;Source: <span id=\"red\">[Source concept information is unavailable]</span>");
      }
      if (target != null) {
        report.append("&nbsp;&nbsp;Target: ")
            .append(target.getPreferredAtom().toString())
            .append(target_href.toString());
      }
      else {
        report.append("&nbsp;&nbsp;Target: <span id=\"red\">[Target concept information is unavailable]</span>");
      }

      if (!action_name.equals("MOLECULAR_MERGE")) {
        show_affected_rows = true;
      }
    }

    // display affected rows
    if (show_affected_rows) {
      report.append("</TT><TT><BR>");
      report.append("</TT><TT>");
      report.append("<FONT size=+1><B>AFFECTED ROWS:</B></FONT><BR>");
      report.append(rows.toString());
    }

    // end table tag
    report.append("</TT></TD></TR></TABLE>");

    MEMEToolkit.trace(report.toString());
    return report.toString();
  }

  /**
   * This method maps href.
   * @param identifier the identifier to link
   * @param dbs the list of databases
   * @param line_end line separator
   * @return a formatted href
   */
  private String mapHref(String identifier, String[] dbs, String line_end) {
    StringBuffer sb = new StringBuffer(500);
    sb.append(" (");
    for (int i = 0; i < dbs.length; i++) {
      sb.append("<A HREF=\"")
          .append(MEMEToolkit.getProperty("meme.app.reports.mid.url"))
          .append("?action=searchbyconceptid&db=")
          .append(dbs[i])
          .append("&arg=")
          .append(identifier)
          .append("\"")
          .append(line_end)
          .append(
              "OnMouseOver=\"window.status='See action report.'; return true;")
          .append("\"")
          .append(line_end)
          .append("OnMouseOut=\"window.status=''; return true;")
          .append("\"")
          .append(line_end)
          .append("target=\"_blank\">")
          .append(identifier)
          .append("</A>");
      if (i == 0) {
        sb.append("/");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  /**
   * Returns an editing report based on the number of past days, a worklist,
   * and astart and end date.  Makes use of $MEME_HOME/bin/editing.csh
   *
   * @param days_ago number of days ago
   * @param worklist worklist
   * @param start_date start {@link Date}
   * @param end_date end {@link Date}
   * @param mid_service the mid service
   * @return the report data
   * @throws MEMEException if failed to produce editing report data
   */
  private String[][] getEditingReportData(int days_ago, String worklist,
                                          Date start_date, Date end_date,
                                          String mid_service) throws
      MEMEException {

    String command =
        ServerToolkit.getProperty(ServerConstants.MEME_HOME) +
        "/bin/editing.csh";
    String[] cmdarray = null;
    if (worklist != null) {
      cmdarray = new String[5];
      cmdarray[1] = mid_service;
      cmdarray[2] = MEMEToolkit.getDateFormat().format(start_date);
      cmdarray[3] = MEMEToolkit.getDateFormat().format(end_date);
      cmdarray[4] = worklist;
    }
    else {
      cmdarray = new String[3];
      cmdarray[1] = mid_service;
      cmdarray[2] = String.valueOf(days_ago);
    }
    cmdarray[0] = command;

    String[] env = new String[] {
        "MEME_HOME=" +
        ServerToolkit.getProperty(ServerConstants.MEME_HOME),
        "ORACLE_HOME=" +
        ServerToolkit.getProperty(ServerConstants.ORACLE_HOME)};

    String report =
        ServerToolkit.exec(cmdarray, env, false,
                           ServerConstants.USE_INPUT_STREAM, false);

    /*/ Sample report
     String report2 =
    "SQL*Plus: Release 8.1.7.0.0 - Production on Tue Oct 29 17:37:27 2002\n" +
    "\n" +
    "(c) Copyright 2000 Oracle Corporation.  All rights reserved.\n" +
    "\n" +
    "\n" +
    "Connected to:\n" +
    "Oracle8i Enterprise Edition Release 8.1.6.3.0 - Production\n" +
    "AUTHORITY           |   ACTIONS|  CONCEPTS|    SPLITS|    MERGES|  APPROVES|      RELS|      STYS\n" +
    "--------------------|----------|----------|----------|----------|----------|----------|----------\n" +
    "E-LAD               |       490|       122|         4|        19|       100|        22|         6\n" +
    "E-LKW               |      1248|       838|         0|       419|       420|         1|         2\n" +
    "E-TPW               |        20|        17|         0|         9|         9|         0|         0\n" +
    "E-VHW               |        70|        34|         0|         0|        35|        35|         0\n" +
    "MAINTENANC          |       719|       546|         0|         0|         0|         0|         0\n" +
    "MAINTENANCE         |         1|         1|         0|         0|         0|         0|         0\n" +
    "\n" +
    "SQL> SQL> \n";
    */
   //
   // Determine the sizes of multidimensional arrays and store pipe separated
   // editing data into string buffer.
   //

   int array_x = 0;
    int array_y = 0;
    int counter = 0;

    boolean dash_line = false;
    boolean begin_flag = false;
    boolean empty_line = false;

    StringBuffer sb = new StringBuffer(500);

    StringTokenizer st = new StringTokenizer(report, "| \n", true);
    while (st.hasMoreTokens()) {
      String token = st.nextToken();

      if (!begin_flag) {
        if (token.startsWith("--")) {
          dash_line = true; // dashes line found.
          continue; // skip dashes line.
        }
        else if (!dash_line) {
          continue; // skip unwanted lines.
        }
      }

      // increment counter when pipe separator is found and
      // only if array_y is not yet set.
      // this will determine the size of array_y.
      if (token.equals("|") && array_y == 0) {
        counter++;

      }
      if (dash_line && token.equals("\n")) {
        // compute the size of array_y
        if (array_y == 0) {
          array_y = counter + 1;

        }
        begin_flag = true; // editing data found.
        dash_line = false; // end of dash line
        continue; // start on next line
      }

      if (!begin_flag) {
        continue; // editing data not yet found.
      }

      if (token.equals("\n")) {
        if (empty_line) {
          break; // end of record has been reached.
        }
        array_x++; // compute the size of array_x.
        empty_line = true; // notify that this is the end of the line.
      }
      else {
        empty_line = false; // current line is not an empty line.

      }
      if (token.equals(" ")) {
        continue; // skip spaces.
      }

      if (token.equals("\n")) {
        sb.append("|"); // append pipe instead
        continue; // and continue to the next token.
      }

      // append editing data.
      if (token.startsWith("\t")) {
        sb.append(token.substring(1));
      }
      else {
        sb.append(token);
      }
    }

    MEMEToolkit.trace("array_x=" + array_x);
    MEMEToolkit.trace("array_y=" + array_y);
    MEMEToolkit.trace("sb=" + sb.toString());

    // Create multidimensional string array
    String[][] editing_data = new String[array_x][array_y];

    //
    // Assign values to multidimensional string array
    //

    int index_x = 0;
    int index_y = 0;

    st = new StringTokenizer(sb.toString(), "|", false);
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      editing_data[index_x][index_y] = token;
      index_y++;
      if (index_y >= array_y) {
        index_x++;
        index_y = 0;
      }
    }
    return editing_data;
  }

  /**
   * Returns a concept report.
   *
   * Here is a simple sample concept report
   * <pre>
   * CN# 3007875  Other complications of procedures NEC in MDR33
   * CUI C0869287    Concept Status is Reviewed
   * STY Pathologic Function   R
   * ATOMS
   *             R []  Other complications of procedures NEC in MDR33 [MTH/PN/NOCODE]
   *
   * REVIEWED RELATED CONCEPT(S)
   * [BRD]  Unspecified complication of procedure NEC in MDR33 [MTH/PN||E-LAD|E-LAD]
   *
   * Concept last approved in MEME by E-LAD on 22-mar-2001 10:47:10.
   * Last MEME action: MOLECULAR_CHANGE_STATUS, performed by MDR40 on 19-sep-2001 16:45:08
   *
   * Versions: common (4.00), common2meme (9.00), molecular_actions (11.0),
   * Reports (7.00), CONCEPTpp.pl (1.2).
   *
   * This report ran against mid service: oc_testsrc.
   * </pre>
   *
   * @param concept the {@link Concept}.
   * @param context the {@link SessionContext}
   * @param atom_id the atom id
   * @return the report
   * @throws DataSourceException if failed to load data source
   */
  private String getReport(Concept concept, SessionContext context, int atom_id) throws
      DataSourceException {

    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    //
    // Determine content type
    //
    String content_type = "text/plain";
    Parameter ct_param = context.getServiceRequest().getParameter(
        "content_type");
    if (ct_param != null) {
      content_type = (String) ct_param.getValue();

      //
      // Acquire style arguments
      //
    }
    ArrayList styles = new ArrayList();
    if (!content_type.equals("text/plain")) {
      for (int i = 0; ; i++) {
        Parameter style_param = context.getServiceRequest().getParameter(
            "style" + i);
        MEMEToolkit.trace("style_param = " + style_param);
        if (style_param != null) {
          ReportStyle style = new ReportStyle.Default(style_param);
          style.setContentType(content_type);
          styles.add(style);
        }
        else {
          break;
        }
      }
    }
    ReportStyle[] style_list = (ReportStyle[]) styles.toArray(new ReportStyle[0]);

    //
    // Determine max relationship count
    //
    int max_r_rel_count = -1;
    Parameter mrc_param = context.getServiceRequest().getParameter(
        "max_r_rel_count");
    if (mrc_param != null) {
      max_r_rel_count = mrc_param.getInt();

      //
      // Prepare string buffers
      //
    }
    StringBuffer report = new StringBuffer(10000);
    StringBuffer work = new StringBuffer(500);

    //
    // Prepares separators for atom reports
    // and link parameters for HTML reports
    //
    String sep_begin = null;
    String sep_end = null;
    String line_end = System.getProperty("line.separator");

    String meme_home = null;
    String report_db = null;
    String report_mid_service = null;

    if (content_type.equals("text/html")) {
      report.append("<div style=\"font-family: monospace\">").append(line_end);
      sep_begin = "<b>";
      sep_end = "</b>";
      line_end = "<br>" + line_end;
      Parameter mh_param = context.getServiceRequest().getParameter("meme_home");
      if (mh_param != null) {
        meme_home = (String) mh_param.getValue();
      }
      report_db = data_source.getDataSourceName();
      report_mid_service = data_source.getServiceName();
    }
    else if (content_type.equals("text/enscript")) {
      sep_begin = "&#x0;font{Courier-Bold07}";
      sep_end = "&#x0;font{default}";
    }
    else {
      sep_begin = "---------------------------------------" +
          "----------------------------------------" + line_end;
      sep_end = "---------------------------------------" +
          "----------------------------------------" + line_end;
    }

    //
    // Determine view options for relationships and context relationships
    //
    boolean include_sib = false;
    Parameter parameter = context.getServiceRequest().getParameter(
        "cxt_rel_opt");
    int cxt_rel_opt = ReportsGenerator.DEFAULT;
    if (parameter != null) {
      cxt_rel_opt = parameter.getInt();

    }
    parameter = context.getServiceRequest().getParameter("rel_opt");
    int rel_opt = ReportsGenerator.DEFAULT;
    if (parameter != null) {
      rel_opt = parameter.getInt();

    }
    if (cxt_rel_opt == ReportsGenerator.INCLUDE_SIB ||
        cxt_rel_opt == ReportsGenerator.ALL) {
      include_sib = true;

    }
    MEMEToolkit.trace("MEME_HOME=" + meme_home);
    MEMEToolkit.trace("REPORT_DB=" + report_db);
    MEMEToolkit.trace("REPORT_MID_SERVICE=" + report_mid_service);

    //
    // Determine the link URLs
    //
    final String standard_params =
        "db=" + report_db + "&service=" + report_mid_service +
        "&MEME_HOME=" + meme_home;

    String url_mid_for_concept_id = null;
    Parameter url_param = context.getServiceRequest().getParameter(
        "url_mid_for_concept_id");
    if (url_param != null) {
      url_mid_for_concept_id = (String) url_param.getValue();
    }
    else {
      url_mid_for_concept_id =
        MEMEToolkit.getProperty("meme.app.reports.mid.url");
      if (url_mid_for_concept_id != null)
        url_mid_for_concept_id += "?" +
          standard_params + "&action=searchbyconceptid&arg=";

    }
    String url_mid_for_code = null;
    url_param = context.getServiceRequest().getParameter("url_mid_for_code");
    if (url_param != null) {
      url_mid_for_code = (String) url_param.getValue();
    }
    else {
      url_mid_for_code =
          MEMEToolkit.getProperty("meme.app.reports.mid.url");
      if (url_mid_for_code != null)
        url_mid_for_code += "?" +
          standard_params + "&action=searchbycode&arg=";

    }
    /**
     url_param = context.getServiceRequest().getParameter("url_mid_for_cui");
         String url_mid_for_cui = null;
         if (url_param != null) {
      url_mid_for_cui = (String) url_param.getValue();
         } else {
        url_mid_for_cui =
          MEMEToolkit.getProperty("meme.app.reports.mid.url") + "?" +
          standard_params + "&action=searchbycui&arg=";

         }
     **/

    String url_release_for_cui = null;
    url_param = context.getServiceRequest().getParameter("url_release_for_cui");
    if (url_param != null) {
      url_release_for_cui = (String) url_param.getValue();
    }
    else {
      url_release_for_cui =
          MEMEToolkit.getProperty("meme.app.reports.release.url");
      if (url_release_for_cui != null)
        url_release_for_cui += "?" +
          "action=searchbycui&arg=";

    }
    String url_release_for_sty = null;
    url_param = context.getServiceRequest().getParameter("url_release_for_sty");
    if (url_param != null) {
      url_release_for_sty = (String) url_param.getValue();
    }
    else {
      url_release_for_sty =
          MEMEToolkit.getProperty("meme.app.reports.release.url");
      if (url_release_for_sty != null )
        url_release_for_sty += "?" +
          "action=searchbysty&arg=";

      //
      // Optain matrix initializer integrity vector
      //
    }
    IntegrityCheck[] checks = null;
    EnforcableIntegrityVector vector =
        (EnforcableIntegrityVector) data_source.getApplicationVector(
            "MATRIX_INITIALIZER");
    ViolationsVector vv = vector.applyDataConstraints(concept);
    checks = vv.getChecks();

    //
    // START Section
    //
    work.append("As of ");
    work.append(MEMEToolkit.getDateFormat().format(new Date()));
    if (checks.length > 0) {
      work.append(", this entry has the following problems/issues: ");
    }
    else {
      work.append(", this entry had no problems/issues.");

    }
    applyStyles(content_type, report, work.toString(), style_list);
    work.setLength(0);
    report.append(line_end);
    //
    // Apply integrity checks
    //
    if (checks != null) {
      for (int i = 0; i < checks.length; i++) {
        work.setLength(0);
        String[] lines = MEMEToolkit.splitString(checks[i].getDescription(), 65);
        for (int j = 0; j < lines.length; j++) {
          if (lines[j] == null) {
            break;
          }
          if (j == 0) {
            work.append("  - ");
          }
          else {
            work.append("    ");
          }
          work.append(lines[j]);
        }
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }
    }

    //
    // Begin report
    //
    applyStyles(content_type, report, "......................................."
                + "........................................", style_list);
    work.setLength(0);
    report.append(line_end);

    //
    // Concept information
    // CN Section
    //
    work.append("CN# ").append(concept.getIdentifier().toString()).append("  ");
    work.append(concept.getPreferredAtom().toString());
    applyStyles(content_type, report, work.toString(), style_list);
    work.setLength(0);
    report.append(line_end);

    MEMEToolkit.trace("ReportsGenerator.getReport() - Write CUI(s).");
    /* Write CUI(s) and status */
    CUI[] cuis = concept.getCUIs();
    work.setLength(0);
    work.append("CUI ");
    if (cuis.length > 0) {
      if (content_type.equals("text/html")) {
        applyStyles(content_type, report, work.toString(), style_list);
        // hyperlinked to the last release version of the concept
        if (url_release_for_cui != null) {
          report.append("<a href=\"")
              .append(url_release_for_cui)
              .append(cuis[0].toString());
          report.append("\">");
        }
        applyStyles(content_type, report, cuis[0].toString(), style_list);
        if (url_release_for_cui != null) {
          report.append("</a>");
        }
      }
      else {
        work.append(cuis[0].toString());
        applyStyles(content_type, report, work.toString(), style_list);
      }
    }
    work.setLength(0);

    //
    // Concept status
    //
    work.append("    Concept Status ");
    if (concept.isReviewed()) {
      work.append("is Reviewed");
    }
    else if (concept.isUnreviewed()) {
      work.append("is Unreviewed/Supplementary Chemical");
    }
    else if (concept.isEmbryo()) {
      work.append("is Embryo");
    }
    else if (concept.needsReview()) {
      work.append("needs Review");
    }
    applyStyles(content_type, report, work.toString(), style_list);
    work.setLength(0);
    report.append(line_end);

    //
    // Remaining CUIs
    //

    for (int i = 1; i < cuis.length; i++) {
      work.append("CUI ");
      if (content_type.equals("text/html")) {
        applyStyles(content_type, report, work.toString(), style_list);
        // hyperlinked to the last release version of the concept
        if (url_release_for_cui != null) {
          report.append("<a href=\"")
              .append(url_release_for_cui)
              .append(cuis[i].toString());
          report.append("\">");
        }
        applyStyles(content_type, report, cuis[i].toString(), style_list);
        if (url_release_for_cui != null) {
          report.append("</a>");
        }
      }
      else {
        work.append(cuis[i].toString());
        applyStyles(content_type, report, work.toString(), style_list);
      }
      work.setLength(0);
      report.append(line_end);
    }

    //
    // STY Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write STY(s).");
    ConceptSemanticType[] stys = concept.getSemanticTypes();
    Arrays.sort(stys, Concept.Default.ATTRIBUTE_VALUE_COMPARATOR);

    for (int i = 0; i < stys.length; i++) {
      if (i == 0 || (i > 0 && (!stys[i].getValue().equals(stys[i - 1].getValue())))) {
        work.append("STY ");
        if (content_type.equals("text/html")) {
          // hyperlinked STY's
          applyStyles(content_type, report, work.toString(), style_list);
          if (url_release_for_sty != null) {
            report.append("<a href=\"")
                .append(url_release_for_sty);
            report.append(stys[i].getValue());
            report.append("\">");
          }
          applyStyles(content_type, report, stys[i].getValue(), style_list);
          if (url_release_for_sty != null) {
            report.append("</a>");
          }
        }
        else {
          work.append(stys[i].getValue());
          applyStyles(content_type, report, work.toString(), style_list);
        }
        work.setLength(0);
        if (stys[i].isApproved()) {
          work.append("   R");
        }
        else {
          work.append("   N");
        }
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }
    }

    //
    // SCT Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write SCT(s).");
    Attribute[] scts = concept.getAttributesByName(Attribute.SYNTACTIC_CATEGORY);
    Arrays.sort(scts, Concept.Default.ATTRIBUTE_VALUE_COMPARATOR);
    for (int i = 0; i < scts.length; i++) {
      report.append("SCT ");
      if (scts[i].isUnreleasable()) {
        report.append("{");

      }
      report.append(scts[i].getValue());

      if (scts[i].isUnreleasable()) {
        report.append("}");

      }
      report.append(line_end);
    }

    //
    // NH Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write NH.");
    if (concept.isNonHuman()) {
      report.append("NH").append(line_end);

      //
      // DEF Section
      //
    }
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write DEF(s).");
    Attribute[] defs = concept.getAttributesByName(Attribute.DEFINITION);
    Arrays.sort(defs, Concept.Default.SOURCE_COMPARATOR);
    for (int i = 0; i < defs.length; i++) {
      if (defs[i].getAtom() != null &&
          defs[i].getAtom().getIdentifier().intValue() == atom_id) {
        report.append(sep_begin);

      }
      work.append("DEF ");
      if (defs[i].isReleasable()) {
        work.append("[Release] ").append(defs[i].getSource().
                                         getSourceAbbreviation());
      }
      else {
        work.append("[Do Not Release] ").append(defs[i].getSource().
                                                getSourceAbbreviation());

      }
      applyStyles(content_type, report, work.toString(), style_list);
      work.setLength(0);
      report.append(line_end);

      StringBuffer def_line = new StringBuffer(
          defs[i].getValue().length() + 40);

      if (defs[i].getAtom() == null) {
        def_line.append("Concept DEF");
      }
      else {
        def_line.append(defs[i].getAtom().getTermgroup());
      }
      def_line.append("|");
      def_line.append(defs[i].getValue());

      String[] lines = MEMEToolkit.splitString(def_line.toString(), 65);
      for (int j = 0; j < lines.length; j++) {
        if (lines[j] == null) {
          break;
        }
        if (j == 0) {
          work.append("  - ");
        }
        else {
          work.append("    ");
        }
        work.append(lines[j]);
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }

      if (defs[i].getAtom() != null &&
          defs[i].getAtom().getIdentifier().intValue() == atom_id) {
        report.append(sep_end);
      }
    }

    //
    // SOS Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write SOS(s).");
    Attribute[] sos = concept.getAttributesByName(Attribute.SOS);
    Arrays.sort(sos, Concept.Default.SOURCE_COMPARATOR);
    for (int i = 0; i < sos.length; i++) {
      work.setLength(0);
      work.append("SOS ");
      if (sos[i].isReleasable()) {
        work.append("[Release] ").append(sos[i].getSource().
                                         getSourceAbbreviation());
      }
      else {
        work.append("[Do Not Release]").append(sos[i].getSource().
                                               getSourceAbbreviation());
      }
      applyStyles(content_type, report, work.toString(), style_list);
      work.setLength(0);
      report.append(line_end);

      StringBuffer sos_line = new StringBuffer(
          sos[i].getValue().length() + 40);

      if (sos[i].getAtom() == null) {
        sos_line.append("Concept SOS");
      }
      else {
        sos_line.append(sos[i].getAtom().getTermgroup());
      }
      sos_line.append("|");
      sos_line.append(sos[i].getValue());

      String[] lines = MEMEToolkit.splitString(sos_line.toString(), 65);
      for (int j = 0; j < lines.length; j++) {
        if (lines[j] == null) {
          break;
        }
        if (j == 0) {
          work.append("  - ");
        }
        else {
          work.append("    ");
        }
        work.append(lines[j]);
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }
    }

    //
    // RxNorm status
    //
    Attribute[] rns = concept.getAttributesByName("RX_NORM_STATUS");
    boolean rns_u = false;
    boolean rns_h = false;
    boolean rns_r = false;
    for (int i = 0; i < rns.length; i++) {
      if (rns[i].getValue().equals("U")) {
        rns_u = true;
      }
      if (rns[i].getValue().equals("H")) {
        rns_h = true;
      }
      if (rns[i].getValue().equals("R")) {
        rns_r = true;
      }
    }
    if (rns_r) {
      applyStyles(content_type, report, "RxNorm Status: R", style_list);
      report.append(line_end);
    }
    else if (rns_u) {
      applyStyles(content_type, report, "RxNorm Status: U", style_list);
      report.append(line_end);
    }
    else if (rns_h) {
      applyStyles(content_type, report, "RxNorm Status: H", style_list);
      report.append(line_end);
    }

    //
    // ATOMS Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write ATOMS.");

    //
    // Determine atom lexical tags
    //
    HashMap atom_lt_map = new HashMap();
    Attribute[] lts = concept.getAttributesByName(Attribute.LEXICAL_TAG);
    for (int i = 0; i < lts.length; i++) {
      atom_lt_map.put(lts[i].getAtom().getLUI(), lts[i].getValue());
    }

    //
    // Determine which atoms are connected to demotions
    //
    HashSet demoted_atoms = new HashSet();
    Relationship[] demotions = concept.getDemotions();
    for (int i = 0; i < demotions.length; i++) {
      demoted_atoms.add(demotions[i].getAtom());
    }

    //
    // Determine atom rx cui
    //
    HashMap atom_rxcui_map = new HashMap();
    Attribute[] rxcuis = concept.getAttributesByName("RXCUI");
    // Add releasable ones first
    for (int i = 0; i < rxcuis.length; i++) {
      if (rxcuis[i].isReleasable())
        atom_rxcui_map.put(rxcuis[i].getAtom().getIdentifier(),
            rxcuis[i].getValue());
    }
    // Add in unreleasable ones
    for (int i = 0; i < rxcuis.length; i++) {
      if (!atom_rxcui_map.containsKey(rxcuis[i].getAtom().getIdentifier()))
        atom_rxcui_map.put(rxcuis[i].getAtom().getIdentifier(),
            rxcuis[i].getValue());
    }

    //
    // Get atoms from concept
    //
    Atom[] sorted_atoms = concept.getSortedAtoms(new ReportsAtomComparator(
        concept));
    if (sorted_atoms.length > 0) {
      applyStyles(content_type, report, "ATOMS", style_list);
    }
    report.append(line_end);

    StringIdentifier prev_lui = null;
    StringIdentifier prev_sui = null;

    //
    // Write atoms
    //
    for (int i = 0; i < sorted_atoms.length; i++) {
      work.setLength(0);

      if (sorted_atoms[i].getIdentifier().intValue() == atom_id) {
        report.append(sep_begin);

        //
        // Determine flags
        //
      }
      work.append(" ");
      if (demoted_atoms.contains(sorted_atoms[i])) {
        work.append("D");
      }
      else {
        work.append(" ");
      }
      if (sorted_atoms[i].getAuthority().toString().startsWith("ENG-")) {
        work.append("M");
      }
      else {
        work.append(" ");
      }
      if (sorted_atoms[i].isSuppressible()) {
        work.append(sorted_atoms[i].getSuppressible());
      }
      else {
        work.append(" ");
      }
      if (sorted_atoms[i].isAmbiguous()) {
        work.append("A");
      }
      else {
        work.append(" ");

        //
        // Determine atom status
        //
      }
      applyStyles(content_type, report, work.toString(), style_list);
      work.setLength(0);
      if (sorted_atoms[i].isUnreleasable() &&
          !sorted_atoms[i].isWeaklyUnreleasable()) {
        work.append("     NEVER");
      }
      else if (sorted_atoms[i].isApproved()) {
        work.append("         R");
      }
      else if (sorted_atoms[i].needsReview()) {
        work.append("         N");
      }
      else if (sorted_atoms[i].isUnreviewed()) {
        work.append("         U");
      }
      else {
        work.append("          ");
      }

      //
      // Determine indention level and new LUI tag ([])
      //

      if (i > 0) {
        if (prev_lui.toString().equals(sorted_atoms[i].getLUI().toString())) {
          work.append("    ");
          if (prev_sui.toString().equals(sorted_atoms[i].getSUI().toString())) {
            work.append("    ");
          }
          else {
            work.append("  ");
          }
        }
        else {
          work.append(" [");
          if (atom_lt_map.containsKey(sorted_atoms[i].getLUI())) {
            work.append(" ");
            work.append(atom_lt_map.get(sorted_atoms[i].getLUI()));
          }
          work.append("]  ");
        }
      }
      else {
        work.append(" [");
        if (atom_lt_map.containsKey(sorted_atoms[i].getLUI())) {
          work.append(" ");
          work.append(atom_lt_map.get(sorted_atoms[i].getLUI()));
        }
        work.append("]  ");
      }

      if (sorted_atoms[i].isUnreleasable()) {
        work.append("{");
      }
      work.append(sorted_atoms[i].toString());
      work.append(" [");
      work.append(sorted_atoms[i].getTermgroup());
      work.append("/");
      if (content_type.equals("text/html")) {
        applyStyles(content_type, report, work.toString(), style_list);
        // hyperlinked the code
        if (url_mid_for_code != null) {
          report.append("<a href=\"")
              .append(url_mid_for_code)
              .append(sorted_atoms[i].getCode());
          report.append("\">");
        }
        applyStyles(content_type, report,
                    sorted_atoms[i].getCode().toString(), style_list);
        if (url_mid_for_code != null) {
          report.append("</a>");
        }
        work.setLength(0);
      }
      else {
        work.append(sorted_atoms[i].getCode());
      }
      work.append("]");

      //
      // Write MUI if MSH (or MSH translation).
      //
      if ("MSH".equals(sorted_atoms[i].getSource().getSourceFamilyAbbreviation()) &&
          sorted_atoms[i].getSourceConceptIdentifier() != null) {
        work.append(" ");
        work.append(sorted_atoms[i].getSourceConceptIdentifier().toString());
      }

      // Write RXCUI
      if (atom_rxcui_map.containsKey(sorted_atoms[i].getIdentifier())) {
        work.append(" ");
        work.append(atom_rxcui_map.get(sorted_atoms[i].getIdentifier()));
      }

      if (sorted_atoms[i].isUnreleasable()) {
        work.append("}");

      }
      applyStyles(content_type, report, work.toString(), style_list);
      work.setLength(0);
      report.append(line_end);

      if (sorted_atoms[i].getIdentifier().intValue() == atom_id) {
        report.append(sep_end);

      }
      prev_lui = sorted_atoms[i].getLUI();
      prev_sui = sorted_atoms[i].getSUI();

    }

    MEMEToolkit.trace("report=" + report.toString());

    //
    // LEXICAL RELATIONSHIPS section
    //
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write LEXICAL RELATIONSHIPS.");
    String atom1, atom2;
    Relationship[] lex_rels = concept.getLexicalRelationships();
    Arrays.sort(lex_rels, Concept.Default.SOURCE_COMPARATOR);
    if (lex_rels.length > 0) {
      report.append(line_end);
      applyStyles(content_type, report, "LEXICAL RELATIONSHIPS", style_list);
      report.append(line_end);
    }

    //
    // Write lexical rels
    //
    for (int i = 0; i < lex_rels.length; i++) {

      if (lex_rels[i].getAtom().getIdentifier().intValue() == atom_id) {
        report.append(sep_begin);

      }
      work.setLength(0);
      work.append(" ");
      if (lex_rels[i].isUnreleasable()) {
        work.append("{ ");

      }
      atom1 = lex_rels[i].getAtom().toString();
      atom2 = lex_rels[i].getRelatedAtom().toString();

      //
      // Write the shorter atom first
      //
      if (atom1.length() > atom2.length()) {
        work.append(lex_rels[i].getRelatedAtom());
      }
      else {
        work.append(lex_rels[i].getAtom());

      }
      work.append(" [SFO] / [LFO] ");

      //
      // Write the longer atom second
      //
      if (atom1.length() > atom2.length()) {
        work.append(lex_rels[i].getAtom());
      }
      else {
        work.append(lex_rels[i].getRelatedAtom());

      }
      work.append(" {");
      if (atom1.length() > atom2.length()) {
        work.append(lex_rels[i].getRelatedAtom().getSource().toString()).append(
            ",")
            .append(lex_rels[i].getAtom().getSource().toString());
      }
      else {
        work.append(lex_rels[i].getAtom().getSource().toString()).append(",")
            .append(lex_rels[i].getRelatedAtom().getSource().toString());
      }
      work.append("}");
      if (lex_rels[i].isUnreleasable()) {
        work.append(" }");
      }
      applyStyles(content_type, report, work.toString(), style_list);
      work.setLength(0);
      report.append(line_end);

      if (lex_rels[i].getAtom().getIdentifier().intValue() == atom_id) {
        report.append(sep_end);

      }
    }

    //
    // ATOM NOTE Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write ATOM NOTE(S).");
    Attribute[] atom_notes = concept.getAttributesByName(Attribute.ATOM_NOTE);
    Arrays.sort(atom_notes, Concept.Default.SOURCE_COMPARATOR);
    if (atom_notes.length > 0) {
      report.append(line_end);
      applyStyles(content_type, report, "ATOM NOTES(S)", style_list);
      report.append(line_end);
    }
    for (int i = 0; i < atom_notes.length; i++) {
      work.setLength(0);
      StringBuffer line = new StringBuffer(
          atom_notes[i].getValue().length() + 40);
      line.append("[");
      line.append(atom_notes[i].getAtom());
      line.append("] ");
      line.append(atom_notes[i].getValue());
      String[] lines = MEMEToolkit.splitString(line.toString(), 65);
      for (int j = 0; j < lines.length; j++) {
        if (lines[j] == null) {
          break;
        }
        if (j == 0) {
          work.append("  - ");
        }
        else {
          work.append("    ");
        }
        work.append(lines[j]);
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }
    }

    //
    // LEGACY CODE Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write LEGACY CODE(S).");
    Attribute[] legacy_codes = concept.getAttributesByNames(new String[] {
        "SNOMEDID", "CTV3ID"});
    Arrays.sort(legacy_codes, Concept.Default.ATTRIBUTE_VALUE_COMPARATOR);
    if (legacy_codes.length > 0) {
      report.append(line_end);
      applyStyles(content_type, report, "LEGACY CODE(S)", style_list);
      report.append(line_end);
    }
    for (int i = 0; i < legacy_codes.length; i++) {
      work.setLength(0);
      StringBuffer line = new StringBuffer(
          legacy_codes[i].getValue().length() + 40);
      line.append("[");
      line.append(legacy_codes[i].getAtom());
      line.append("] ");
      line.append(legacy_codes[i].getName());
      line.append(": ");
      line.append(legacy_codes[i].getValue());
      String[] lines = MEMEToolkit.splitString(line.toString(), 65);
      for (int j = 0; j < lines.length; j++) {
        if (lines[j] == null) {
          break;
        }
        if (j == 0) {
          work.append("  - ");
        }
        else {
          work.append("    ");
        }
        work.append(lines[j]);
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }
    }
    if (legacy_codes.length > 0) {
      report.append(line_end);

      //
      // SNOMED Concept Status Section
      //
    }
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write SNOMEDCT Concept Status.");
    Attribute[] atts = concept.getAttributesByName("CONCEPTSTATUS");
    if (atts.length > 0) {
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].getSource() != null && atts[i].getAtom() != null &&
            atts[i].getSource().getStrippedSourceAbbreviation().equals(
                "SNOMEDCT")) {
          if (atts[i].getAtom() != null &&
              !atts[i].getValue().equals("0")) {
            work.append("SNOMEDCT Concept ");
            work.append(atts[i].getAtom().getSourceConceptIdentifier());
            work.append(" Status: ");
            work.append(atts[i].getValue());
            applyStyles(content_type, report, work.toString(), style_list);
            work.setLength(0);
            report.append(line_end);
          }
        }
      }
    }

    //
    // CONCEPT NOTE Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write CONCEPT NOTE(s).");
    Attribute[] concept_notes = concept.getAttributesByName(Attribute.
        CONCEPT_NOTE);
    Arrays.sort(concept_notes, Concept.Default.ATTRIBUTE_VALUE_COMPARATOR);
    if (concept_notes.length > 0) {
      report.append(line_end);
      applyStyles(content_type, report, "CONCEPT NOTES(S)", style_list);
      report.append(line_end);
    }
    for (int i = 0; i < concept_notes.length; i++) {
      StringBuffer line = new StringBuffer(
          concept_notes[i].getValue().length() + 40);
      line.append(concept_notes[i].getValue());
      work.setLength(0);
      String[] lines = MEMEToolkit.splitString(line.toString(), 65);
      for (int j = 0; j < lines.length; j++) {
        if (lines[j] == null) {
          break;
        }
        if (j == 0) {
          work.append("  - ");
        }
        else {
          work.append("    ");
        }
        work.append(lines[j]);
        applyStyles(content_type, report, work.toString(), style_list);
        work.setLength(0);
        report.append(line_end);
      }
    }

    //
    // EZ/RN:EC NUMBER(S) Section
    //
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write EZ/RN:EC NUMBER(s).");
    String[] attr_names = {
        Attribute.RN, Attribute.EZ, Attribute.EC};
    Attribute[] ec_nos = concept.getAttributesByNames(attr_names);
    if (ec_nos.length > 0) {
      report.append(line_end);
      applyStyles(content_type, report, "EZ/RN:EC NUMBER(S)", style_list);
      report.append(line_end);
    }
    for (int i = 0; i < ec_nos.length; i++) {
      work.append("   ").append(ec_nos[i].getName());
      work.append(" ").append(ec_nos[i].getValue());
      work.append("   ").append("{");
      work.append(ec_nos[i].getSource());
      work.append("}");
      applyStyles(content_type, report, work.toString(), style_list);
      work.setLength(0);
      report.append(line_end);
    }

    // Relationship variables
    int rel_ctr = 0; // Holds the count of the number of relationships shown
    int r_rel_ctr = 0; // Holds the count of the number of reviewed relationships
    boolean show_tag; // Flag to determine if related concept tag must be printed
    int rels_total = 0; // Total number of relationships
    int cxt_rel_ctr = 0; // Holds the count of the number of cxt rels shown
    int start_index = 0; // Holds the starting index for RELATIONSHIP section headers
    int offset = 1;

    //
    // ATOM RELATIONSHIPS Section (for atom reports only)
    //
    Relationship[] rels = null;
    ReportsRelationshipRestrictor rrr = new ReportsRelationshipRestrictor(
        concept);

    if (atom_id != 0) {
      MEMEToolkit.trace(
          "ReportsGenerator.getReport() - Write ATOM RELATIONSHIP(s).");

      // Get the total relationships
      rels = concept.getSortedRelationships(rrr);
      rels_total = rels.length;

      show_tag = true;
      for (int i = 0; i < rels.length; i++) {

        // if rel_opt is NONE, skip
        if (rel_opt == NONE) {
          break;
        }

        // Exclude lexical rels, concept level rels
        // and relationships that do not match the atom id
        if (rels[i].getName().equals("SFO/LFO")) {
          continue;
        }
        if (!rels[i].isSourceAsserted()) {
          continue;
        }
        if (rels[i].isSourceAsserted() &&
            rels[i].getAtom().getIdentifier().intValue() != atom_id) {
          continue;
        }

        // write header
        if (show_tag) {
          report.append(line_end);
          report.append(sep_begin);
          applyStyles(content_type, report, "ATOM RELATIONSHIP(S)", style_list);
          report.append(line_end);
          show_tag = false;
        }

        // increment counter
        rel_ctr++;

        reportRelationship(rels[i], content_type, meme_home,
                           report_db, report_mid_service, report, style_list,
                           url_mid_for_concept_id);

        report.append(line_end);
      }
      if (!show_tag) { // atom relationships found
        report.append(sep_end);
      }
    }

    //
    // ATOM CONTEXT RELATIONSHIPS
    //
    ContextRelationship[] cxt_rels = null;
    Attribute[] contexts = concept.getFormattedContexts();
    if (atom_id != 0) {
      MEMEToolkit.trace(
          "ReportsGenerator.getReport() - Write ATOM CONTEXT RELATIONSHIP(s).");

      //
      // Write atom context relationships
      // if (a) there are no contexts, (b) cxt_rel_opt is ALL,
      // (c) the number of rels shown so far is zero,
      // (d) the rel_opt is NONE, or (e) the count of rels to show later is 0
      //
      if ( (contexts.length == 0) || cxt_rel_opt == ReportsGenerator.ALL ||
          rel_ctr == 0 || rel_opt == NONE || rels_total == 0) {

        cxt_rels = concept.getSortedContextRelationships(
            Concept.Default.DEFAULT_CONTEXT_RELATIONSHIP_COMPARATOR);
      }
      else {
        cxt_rels = new ContextRelationship[0];

      }
      show_tag = true;
      for (int i = 0; i < cxt_rels.length; i++) {

        if (cxt_rel_opt == NONE) {
          break;
        }

        // Exclude is SIB (and we are not showing SIBs)
        // or if the atom id does not match
        if (cxt_rels[i].getName().equals("SIB") && !include_sib) {
          continue;
        }
        if (cxt_rels[i].getAtom().getIdentifier().intValue() != atom_id) {
          continue;
        }

        // write header
        if (show_tag) {
          report.append(line_end);
          report.append(sep_begin);
          applyStyles(content_type, report, "ATOM CONTEXT RELATIONSHIP(S)",
                      style_list);
          report.append(line_end);
          show_tag = false;
        }

        // increment counter
        cxt_rel_ctr++;

        reportRelationship(cxt_rels[i], content_type, meme_home,
                           report_db, report_mid_service, report, style_list,
                           url_mid_for_concept_id);

        report.append(line_end);
      }
      if (!show_tag) { // atom context relationships found
        report.append(sep_end);

      }
    } // end if (atom_id != 0)

    //
    // Before context relationships section, we need to get
    // the relationships so we know if there are any
    //
    if (rel_opt == ReportsGenerator.ALL) {
      // If the atoms section above was triggered
      // then rels is already set to getSortedRelationships,
      // don't bother doing it again
      if (rels == null) {
        rels = concept.getSortedRelationships(rrr);
      }
      start_index = 5;
      offset = 0;
    }
    else {
      rels = concept.getRestrictedRelationships(rrr);

    }
    rels_total = rels.length;

    //
    // CONTEXT RELATIONSHIP Section
    //
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write CONTEXT RELATIONSHIP(s).");
    if (cxt_rels == null) {

      //
      // Write context relationships
      // if (a) there are no contexts, (b) cxt_rel_opt is ALL,
      // (d) the rel_opt is NONE, or (e) the count of rels to show later is 0
      //
      if ( (contexts.length == 0) || cxt_rel_opt == ReportsGenerator.ALL ||
          rel_opt == NONE || rels_total == 0) {

        cxt_rels = concept.getSortedContextRelationships(
            Concept.Default.DEFAULT_CONTEXT_RELATIONSHIP_COMPARATOR);
      }
      else {
        cxt_rels = new ContextRelationship[0];
      }
    }

    show_tag = true;
    for (int i = 0; i < cxt_rels.length; i++) {
      if (cxt_rel_opt == ReportsGenerator.NONE) {
        break;
      }

      // Exclude is SIB (and we are not showing SIBs)
      // or if the relationship matches the atom id
      if (cxt_rels[i].getName().equals("SIB") && !include_sib) {
        continue;
      }
      if (cxt_rels[i].getAtom().getIdentifier().intValue() == atom_id) {
        continue;
      }

      // write header
      if (show_tag) {
        report.append(line_end);
        applyStyles(content_type, report, "CONTEXT RELATIONSHIP(S)", style_list);
        report.append(line_end);
        show_tag = false;
      }

      // increment counter
      cxt_rel_ctr++;

      reportRelationship(cxt_rels[i], content_type, meme_home,
                         report_db, report_mid_service, report, style_list,
                         url_mid_for_concept_id);

      report.append(line_end);
    }

    //
    // RELATIONSHIPS Section
    //
    // All of the requisite variables have been set
    // including start_index, offset, and rels
    //
    for (int tag_ctr = start_index;
         tag_ctr < rel_tag_map.length - offset;
         tag_ctr++) {

      if (rel_opt == ReportsGenerator.NONE) {
        break;
      }

      show_tag = true; // Reset show_tag flag for every related concept
      for (int i = 0; i < rels.length; i++) {

        // Determines which related concepts must exclude
        boolean matches_demotion =
            rrr.hasSameRelatedConceptAsDemotion(rels[i]);
        boolean matches_xr = rrr.hasSameRelatedConceptAsXR(rels[i]);

        if (tag_ctr == 0) { // DEMOTED RELATED CONCEPT(S)
          if (!matches_demotion) {
            continue;
          }
        }
        else if (tag_ctr == 1) { // XR(S) AND CORRESPONDING RELATIONSHIP(S)
          if (rel_opt != ReportsGenerator.XR) {
            break;
          }
          if (!matches_xr) {
            continue;
          }
        }
        else if (tag_ctr == 2) { // NEEDS REVIEW RELATED CONCEPT(S)
          if (!rels[i].needsReview() || matches_demotion) {
            continue;
          }
        }
        else if (tag_ctr == 3) { // REVIEWED RELATED CONCEPT(S)
          if (!rels[i].isReviewed() || matches_demotion) {
            continue;
          }
          r_rel_ctr++;
        }
        else if (tag_ctr == 4) { // UNREVIEWED/SUPPLEMENTARY CHEMICAL RELATED CONCEPT
          if (!rels[i].isUnreviewed() || matches_demotion) {
            continue;
          }
        }

        // We are in XR mode but not in XR section and
        // relationship matches an XR.
        if (rel_opt == ReportsGenerator.XR && tag_ctr != 1 && matches_xr) {
          continue;
        }

        // Exclude SFO/LFO, self-referential
        // and source asserted rels matching the atom id
        if (rels[i].getName().equals("SFO/LFO")) {
          continue;
        }
        if (concept.getIdentifier().equals(rels[i].getRelatedConcept().
                                           getIdentifier()) &&
            rel_opt != ReportsGenerator.ALL) {
          continue;
        }
        if (rels[i].isSourceAsserted() &&
            rels[i].getAtom().getIdentifier().intValue() == atom_id) {
          continue;
        }

        // write header
        if (show_tag) {
          report.append(line_end);
          applyStyles(content_type, report, rel_tag_map[tag_ctr], style_list);
          report.append(line_end);
          show_tag = false;
        }

        // If writing a non-demotion that matches a demotion then
        // print the status
        if (matches_demotion && rels[i].isReviewed()) {
          report.append("R ");
        }
        else if (matches_demotion && rels[i].needsReview()) {
          report.append("N ");

          // increment counter
        }
        rel_ctr++;

        //
        // Stop reporting reviewed relationships if max rel count is set
        //
        if (max_r_rel_count != -1 && tag_ctr == 3 &&
            r_rel_ctr > max_r_rel_count) {
          continue;
        }
        else {
          reportRelationship(
              rels[i], content_type, meme_home,
              report_db, report_mid_service, report, style_list,
              url_mid_for_concept_id);
          report.append(line_end);
        }
      }
    }

    //
    // Write count of context relationships not shown
    //
    int hidden_cxts_count = concept.getContextRelationships().length -
        cxt_rel_ctr;
    if (hidden_cxts_count > 0) {
      report.append(line_end);
      report.append("There are ");
      report.append(hidden_cxts_count);
      report.append(" context relationships not shown here.");
      report.append(line_end);
    }

    //
    // Write count of regular relationships not shown
    //
    int hidden_rels_count = rels_total - (rel_ctr + lex_rels.length);
    if (max_r_rel_count != -1 && r_rel_ctr > max_r_rel_count) {
      hidden_rels_count += r_rel_ctr - max_r_rel_count;
    }
    if (hidden_rels_count > 0) {
      report.append(line_end);
      report.append("There are ");
      report.append(hidden_rels_count);
      report.append(" relationships not shown here.");
      report.append(line_end);
    }
    if (max_r_rel_count != -1 && r_rel_ctr > max_r_rel_count) {
      report.append("Reviewed relationships truncated at ")
          .append(max_r_rel_count)
          .append(".")
          .append(line_end);

      //
      // CONTEXT Section
      //
    }
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write CONTEXTS.");
    Arrays.sort(contexts, Concept.Default.ATTRIBUTE_VALUE_COMPARATOR);
    if (contexts.length > 0) {
      report.append(line_end);
      applyStyles(content_type, report, "CONTEXTS", style_list);
      report.append(line_end);
    }
    for (int i = 0; i < contexts.length; i++) {
      if (contexts[i].getAtom().getIdentifier().intValue() == atom_id) {
        report.append(sep_begin);
      }
      applyStyles(content_type, report, contexts[i].getValue(), style_list);
      if (contexts[i].getAtom().getIdentifier().intValue() == atom_id) {
        report.append(sep_end);
      }
    }

    //
    // ATX Section
    //
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write ATX.");
    Attribute[] atxs = concept.getAttributesByName(Attribute.ATX_REL);
    Arrays.sort(atxs, Concept.Default.SOURCE_COMPARATOR);
    for (int i = 0; i < atxs.length; i++) {
      if (i == 0 ||
          (i > 0 && (!atxs[i].getValue().equals(atxs[i - 1].getValue())))) {
        report.append(line_end);
        report.append("ATX ");
        report.append(atxs[i].getValue());
      }
    }
    if (atxs.length > 0) {
      report.append(line_end);

      //
      // IMN Section
      //
    }
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write Internal MeSH Note.");
    Attribute[] imns = concept.getAttributesByName(Attribute.IMN);
    Arrays.sort(imns, Concept.Default.SOURCE_COMPARATOR);
    for (int i = 0; i < imns.length; i++) {
      if (i == 0 ||
          (i > 0 && (!imns[i].getValue().equals(imns[i - 1].getValue())))) {
        report.append(line_end);
        report.append("IMN ");
        report.append(imns[i].getValue());
      }
    }
    if (imns.length > 0) {
      report.append(line_end);

      //
      // RO Section
      //
    }
    MEMEToolkit.trace("ReportsGenerator.getReport() - Write Record Originator.");
    /* Write any Record Originator */
    Attribute[] ros = concept.getAttributesByName(Attribute.RO);
    Arrays.sort(ros, Concept.Default.SOURCE_COMPARATOR);
    for (int i = 0; i < ros.length; i++) {
      if (i == 0 || (i > 0 && (!ros[i].getValue().equals(ros[i - 1].getValue())))) {
        report.append(line_end);
        report.append("RO ");
        report.append(ros[i].getValue());
        report.append("|");
      }
    }
    if (ros.length > 0) {
      report.append(line_end);

      //
      // Last Release Section
      //
    }
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write Last Release Information.");

    Calendar calendar = Calendar.getInstance();
    LoggedAction approval = concept.getApprovalAction();
    report.append(line_end);
    report.append("Concept was ");
    if (concept.getEditingAuthority() != null) {
      report.append("last approved on ");
      report.append(MEMEToolkit.getDateFormat().format(concept.
          getEditingTimestamp()));
      report.append(" by ");
      report.append(concept.getEditingAuthority());
    }
    else if (concept.wasReleased()) {
      report.append("released as ");
      if (concept.wasReleasedAsUnreviewed()) {
        report.append("Unapproved");
      }
      else {
        report.append("Approved");
      }
      report.append(" in Meta");
      report.append(calendar.get(Calendar.YEAR));
    }
    report.append(".").append(line_end);

    //
    // Last Action Section
    //
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write Last Action Information.");
    LoggedAction last_action =
        data_source.getLastMolecularAction(concept);
    if (last_action != null &&
        (approval == null ||
         !approval.getIdentifier().equals(last_action.getIdentifier()))) {
      report.append("Last Action was ");
      report.append(last_action.getActionName());
      report.append(" performed by ");
      report.append(last_action.getAuthority());
      report.append(" on ");
      report.append(MEMEToolkit.getDateFormat().format(last_action.getTimestamp()));
      report.append(".").append(line_end);
    }

    //
    // Version Information
    //
    MEMEToolkit.trace(
        "ReportsGenerator.getReport() - Write Version Information.");
    report.append(line_end);
    report.append("MEME Server Version: ");
    report.append(gov.nih.nlm.meme.Version.getRelease());
    report.append(".");
    report.append(gov.nih.nlm.meme.Version.getVersion());
    report.append(line_end);

    //
    // Dabase info
    //
    report.append("This report ran against: ");
    report.append(data_source.getDataSourceName());
    report.append(line_end).append(line_end);

    if (content_type.equals("text/html")) {
      report.append("</div>").append(line_end);
    }
    // Return the report
    return report.toString();
  }

  /**
   * Adds an entry to the report for the specified {@link Relationship}.
   * @param rels the relationship
   * @param content_type the content type
   * @param meme_home $MEME_HOME (for links)
   * @param report_db the database (for links)
   * @param report_mid_service the mid service (for links)
   * @param report the report to append do
   * @param style_list the styles
   * @param url_mid_for_concept_id used for links on rels
   */
  private void reportRelationship(Relationship rels,
                                  String content_type,
                                  String meme_home,
                                  String report_db,
                                  String report_mid_service,
                                  StringBuffer report,
                                  ReportStyle[] style_list,
                                  String url_mid_for_concept_id) {

    StringBuffer sb = new StringBuffer(10000);
    String rel_name;

    sb.append("[");
    rel_name = (String) rel_name_map.get(rels.getName());
    if (rel_name == null) {
      sb.append(rels.getName());
    }
    else {
      sb.append(rel_name);
    }
    sb.append("]  ");

    if (rels.isUnreleasable() ||
        rels.getName().equals("XR") ||
        rels.getName().equals("XS") ||
        (rels.isAtomLevel() &&
         (rels.getAtom().isUnreleasable() ||
          rels.getRelatedAtom().isUnreleasable()))
        ) {
      sb.append("{");

    }
    sb.append(rels.getRelatedConcept().getPreferredAtom());
    sb.append(" [");
    sb.append(rels.getRelatedConcept().getPreferredAtom().getTermgroup());
    sb.append("|");
    if (rels.getAttribute() != null) {
      sb.append(rels.getAttribute());
    }
    sb.append("|");
    sb.append(rels.getSource());
    sb.append("|");
    sb.append(rels.getAuthority());
    sb.append("]");
    sb.append(" {");

    if (content_type.equals("text/html")) {
      applyStyles(content_type, report, sb.toString(), style_list);
      // hyperlinked the concept that do not refer to the current concept
      if (url_mid_for_concept_id != null) {
        report.append("<a href=\"")
            .append(url_mid_for_concept_id)
            .append(rels.getRelatedConcept().getIdentifier().toString());
        report.append("#report\">");
      }
      applyStyles(content_type, report,
                  rels.getRelatedConcept().getIdentifier().toString(),
                  style_list);
      if (url_mid_for_concept_id != null) {
        report.append("</a>");
      }
      sb.setLength(0);
    }
    else {
      sb.append(rels.getRelatedConcept().getIdentifier().toString());

    }
    sb.append("}");

    // Print relationship_level
    if (rels.isSourceAsserted()) {
      sb.append(" S");
    }
    else if (rels.isMTHAsserted()) {
      sb.append(" C");
    }
    else {
      sb.append(" P");

    }
    if (rels.isUnreleasable() ||
        rels.getName().equals("XR") ||
        rels.getName().equals("XS") ||
        (rels.isAtomLevel() &&
         (rels.getAtom().isUnreleasable() ||
          rels.getRelatedAtom().isUnreleasable()))
        ) {
      sb.append("}");
      if (rels.isWeaklyUnreleasable()) {
        sb.append(" n");
      }
      else if (rels.isUnreleasable()) {
        sb.append(" NEVER");
      }
    }

    applyStyles(content_type, report, sb.toString(), style_list);

  }

  /**
   * Applies {@link ReportStyle} start and end tags to the lines of the report.
   * @param content_type the content type
   * @param report the report
   * @param line the line to apply styles to
   * @param style_list the {@link ReportStyle}s to apply
   */
  private void applyStyles(String content_type, StringBuffer report,
                           String line, ReportStyle[] style_list) {

    // Apply each style
    for (int j = 0; j < style_list.length; j++) {
      report.append(style_list[j].getStartTag(line));

      // write line from report
      // Replace <, >, & with character entities!

    }
    if (content_type.equals("text/html")) {
      for (int i = 0; i < line.length(); i++) {
        char token = line.charAt(i);
        if (token == '<') {
          report.append("&lt;");
        }
        else if (token == '>') {
          report.append("&gt;");
        }
        else if (token == '&') {
          report.append("&amp;");
        }
        else if (token == ' ' && line.length() > i + 1 &&
                 line.charAt(i + 1) == ' ') {
          report.append("&nbsp;");
        }
        else if (token == '\n') {
          report.append("<br>").append(System.getProperty("line.separator"));
        }
        else if (token == '\r') {
          report.append("");
        }
        else {
          report.append(token);
        }
      }
    }
    else {
      report.append(line);
    }

    // apply styles in reverse direction
    for (int j = style_list.length - 1; j >= 0; j--) {
      report.append(style_list[j].getEndTag());
    }
  }

  //
  // Implementation of Initializable interface
  //

  /**
   * Initialize component.
   * @param context the {@link InitializationContext}
   * @throws InitializationException if initialization failed
   */
  public void initialize(InitializationContext context) throws
      InitializationException {

    // get reference to server
    server = (MEMEApplicationServer) context;

    // add server hook
    context.addHook(this);

    cacheReport = false;

  }

  /**
   * Puts the source metadata report into cache.
   * @throws DataSourceException if failed to cache source metadata report
   */
  public void run() {
    try {
      final MIDDataSource data_source = ServerToolkit.newMIDDataSource();
      MEMEToolkit.logComment("  Cache source metadata report", true);

      String query = "SELECT source FROM sims_info " +
          "WHERE insert_meta_version IS NULL " +
          "AND source IN " +
          "(SELECT current_name FROM source_version WHERE previous_name IS NULL)";
      String source = null;

      data_source.setSortAreaSize(400000000);
      data_source.setHashAreaSize(400000000);

      try {
        sourceMetadataReport = new SourceMetadataReport();
        Statement stmt = data_source.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        final ArrayList newSources = new ArrayList();
        // Read new sources
        while (rs.next()) {
          source = rs.getString("source");
          newSources.add(data_source.getSource(source));
        }
        query =
            "SELECT a.source FROM sims_info a, source_version b, sims_info c " +
            "WHERE a.insert_meta_version IS NULL " +
            "AND a.source = b.current_name AND b.previous_name = c.source " +
            "AND c.remove_meta_version IS NULL";
        rs = stmt.executeQuery(query);
        final ArrayList updateSources = new ArrayList();
        // Read update sources
        while (rs.next()) {
          source = rs.getString("source");
          updateSources.add(data_source.getSource(source));
        }
        query =
            "SELECT c.source FROM sims_info a, source_version b, sims_info c, source_rank d " +
            "WHERE a.insert_meta_version IS NULL " +
            "AND a.source = b.current_name AND b.previous_name IS NOT NULL " +
            "AND b.source = d.stripped_source " +
            "AND c.source = d.source " +
            "AND c.remove_meta_version IS NULL " +
            "AND c.insert_meta_version IS NOT NULL ";
        rs = stmt.executeQuery(query);
        final ArrayList obsoleteSources = new ArrayList();
        // Read obsolete sources
        while (rs.next()) {
          source = rs.getString("source");
          obsoleteSources.add(data_source.getSource(source));
        }
        query =
            "SELECT source FROM sims_info " +
            "WHERE remove_meta_version IS NULL " +
            "AND source IN " +
            "(SELECT previous_name FROM source_version " +
            "WHERE current_name IS NULL)";
        rs = stmt.executeQuery(query);
        final ArrayList oldSources = new ArrayList();
        // Read old sources
        while (rs.next()) {
          source = rs.getString("source");
          oldSources.add(data_source.getSource(source));
        }
        query =
            "SELECT source FROM sims_info " +
            "WHERE insert_meta_version IS NOT NULL " +
            "AND source IN (SELECT current_name FROM source_version)";
        rs = stmt.executeQuery(query);
        final ArrayList unchangedSources = new ArrayList();
        // Read unchanged sources
        while (rs.next()) {
          source = rs.getString("source");
          unchangedSources.add(data_source.getSource(source));
        }
        // Close statement
        stmt.close();

        SourceDifference[] sourceDifferences = new SourceDifference[5];
        SourceDifference sourceDifference = new SourceDifference();
        sourceDifference.setName("RSAB");
        Set values = new HashSet();
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.add(newSource.getRootSourceAbbreviation());
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.add(updateSource.getRootSourceAbbreviation());
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.remove(obsoleteSource.getRootSourceAbbreviation());
        }
        for (Iterator iterator = unchangedSources.iterator(); iterator.hasNext(); ) {
          Source unchangedSource = (Source) iterator.next();
          values.remove(unchangedSource.getRootSourceAbbreviation());
        }
        ArrayList valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setNewValues( (Value[]) valueList.toArray(new Value[0]));
        values = new HashSet();
        for (Iterator iterator = oldSources.iterator(); iterator.hasNext(); ) {
          Source oldSource = (Source) iterator.next();
          values.add(oldSource.getRootSourceAbbreviation());
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.add(obsoleteSource.getRootSourceAbbreviation());
        }
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.remove(newSource.getRootSourceAbbreviation());
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.remove(updateSource.getRootSourceAbbreviation());
        }

        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setOldValues( (Value[]) valueList.toArray(new Value[0]));
        sourceDifferences[0] = sourceDifference;
        sourceDifference = new SourceDifference();
        sourceDifference.setName("VSAB");
        values = new HashSet();
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.add(newSource.getSourceAbbreviation());
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.add(updateSource.getSourceAbbreviation());
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setNewValues( (Value[]) valueList.toArray(new Value[0]));
        values = new HashSet();
        for (Iterator iterator = oldSources.iterator(); iterator.hasNext(); ) {
          Source oldSource = (Source) iterator.next();
          values.add(oldSource.getSourceAbbreviation());
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.add(obsoleteSource.getSourceAbbreviation());
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setOldValues( (Value[]) valueList.toArray(new Value[0]));
        values = new HashSet();
        for (Iterator iterator = unchangedSources.iterator(); iterator.hasNext(); ) {
          Source unchangedSource = (Source) iterator.next();
          values.add(unchangedSource.getSourceAbbreviation());
        }
        sourceDifferences[1] = sourceDifference;
        sourceDifference = new SourceDifference();
        sourceDifference.setName("SF");
        values = new HashSet();
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.add(newSource.getSourceFamilyAbbreviation());
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.add(updateSource.getSourceFamilyAbbreviation());
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.remove(obsoleteSource.getSourceFamilyAbbreviation());
        }
        for (Iterator iterator = unchangedSources.iterator(); iterator.hasNext(); ) {
          Source unchangedSource = (Source) iterator.next();
          values.remove(unchangedSource.getSourceFamilyAbbreviation());
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setNewValues( (Value[]) valueList.toArray(new Value[0]));
        values = new HashSet();
        for (Iterator iterator = oldSources.iterator(); iterator.hasNext(); ) {
          Source oldSource = (Source) iterator.next();
          values.add(oldSource.getSourceFamilyAbbreviation());
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.add(obsoleteSource.getSourceFamilyAbbreviation());
        }
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.remove(newSource.getSourceFamilyAbbreviation());
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.remove(updateSource.getSourceFamilyAbbreviation());
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setOldValues( (Value[]) valueList.toArray(new Value[0]));
        sourceDifferences[2] = sourceDifference;
        sourceDifference = new SourceDifference();
        sourceDifference.setName("LAT");
        values = new HashSet();
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          if (newSource.getLanguage() == null) {
            values.add(null);
          }
          else {
            values.add(newSource.getLanguage().getAbbreviation());
          }
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          if (updateSource.getLanguage() == null) {
            values.add(null);
          }
          else {
            values.add(updateSource.getLanguage().getAbbreviation());
          }
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          if (obsoleteSource.getLanguage() == null) {
            values.remove(null);
          }
          else {
            values.remove(obsoleteSource.getLanguage().getAbbreviation());
          }
        }
        for (Iterator iterator = unchangedSources.iterator(); iterator.hasNext(); ) {
          Source unchangedSource = (Source) iterator.next();
          if (unchangedSource.getLanguage() == null) {
            values.remove(null);
          }
          else {
            values.remove(unchangedSource.getLanguage().getAbbreviation());
          }
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setNewValues( (Value[]) valueList.toArray(new Value[0]));
        values = new HashSet();
        for (Iterator iterator = oldSources.iterator(); iterator.hasNext(); ) {
          Source oldSource = (Source) iterator.next();
          if (oldSource.getLanguage() == null) {
            values.add(null);
          }
          else {
            values.add(oldSource.getLanguage().getAbbreviation());
          }
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          if (obsoleteSource.getLanguage() == null) {
            values.add(null);
          }
          else {
            values.add(obsoleteSource.getLanguage().getAbbreviation());
          }
        }
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          if (newSource.getLanguage() == null) {
            values.remove(null);
          }
          else {
            values.remove(newSource.getLanguage().getAbbreviation());
          }
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          if (updateSource.getLanguage() == null) {
            values.remove(null);
          }
          else {
            values.remove(updateSource.getLanguage().getAbbreviation());
          }
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setOldValues( (Value[]) valueList.toArray(new Value[0]));
        sourceDifferences[3] = sourceDifference;
        sourceDifference = new SourceDifference();
        sourceDifference.setName("VASB,SF,LAT");
        values = new HashSet();
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.add(newSource.getSourceAbbreviation() + "," +
                     newSource.getSourceFamilyAbbreviation() + "," +
                     (newSource.getLanguage() == null ? "" :
                      newSource.getLanguage().getAbbreviation()));
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.add(updateSource.getSourceAbbreviation() + "," +
                     updateSource.getSourceFamilyAbbreviation() + "," +
                     (updateSource.getLanguage() == null ? "" :
                      updateSource.getLanguage().getAbbreviation()));
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.remove(obsoleteSource.getSourceAbbreviation() + "," +
                        obsoleteSource.getSourceFamilyAbbreviation() + "," +
                        (obsoleteSource.getLanguage() == null ? "" :
                         obsoleteSource.getLanguage().getAbbreviation()));
        }
        for (Iterator iterator = unchangedSources.iterator(); iterator.hasNext(); ) {
          Source unchangedSource = (Source) iterator.next();
          values.remove(unchangedSource.getSourceAbbreviation() + "," +
                        unchangedSource.getSourceFamilyAbbreviation() + "," +
                        (unchangedSource.getLanguage() == null ? "" :
                         unchangedSource.getLanguage().getAbbreviation()));
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setNewValues( (Value[]) valueList.toArray(new Value[0]));
        values = new HashSet();
        for (Iterator iterator = oldSources.iterator(); iterator.hasNext(); ) {
          Source oldSource = (Source) iterator.next();
          values.add(oldSource.getSourceAbbreviation() + "," +
                     oldSource.getSourceFamilyAbbreviation() + "," +
                     (oldSource.getLanguage() == null ? "" :
                      oldSource.getLanguage().getAbbreviation()));
        }
        for (Iterator iterator = obsoleteSources.iterator(); iterator.hasNext(); ) {
          Source obsoleteSource = (Source) iterator.next();
          values.add(obsoleteSource.getSourceAbbreviation() + "," +
                     obsoleteSource.getSourceFamilyAbbreviation() + "," +
                     (obsoleteSource.getLanguage() == null ? "" :
                      obsoleteSource.getLanguage().getAbbreviation()));
        }
        for (Iterator iterator = newSources.iterator(); iterator.hasNext(); ) {
          Source newSource = (Source) iterator.next();
          values.remove(newSource.getSourceAbbreviation() + "," +
                        newSource.getSourceFamilyAbbreviation() + "," +
                        (newSource.getLanguage() == null ? "" :
                         newSource.getLanguage().getAbbreviation()));
        }
        for (Iterator iterator = updateSources.iterator(); iterator.hasNext(); ) {
          Source updateSource = (Source) iterator.next();
          values.remove(updateSource.getSourceAbbreviation() + "," +
                        updateSource.getSourceFamilyAbbreviation() + "," +
                        (updateSource.getLanguage() == null ? "" :
                         updateSource.getLanguage().getAbbreviation()));
        }
        valueList = new ArrayList(values.size());
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
          Value value = new Value();
          value.setValue( (String) iterator.next());
          valueList.add(value);
        }
        sourceDifference.setOldValues( (Value[]) valueList.toArray(new Value[0]));
        sourceDifferences[4] = sourceDifference;
        sourceMetadataReport.setSourceDifferences(sourceDifferences);

        ThreadGroup tg = new ThreadGroup("Differences");
        Thread[] th = new Thread[3];
        th[1] = new Thread(tg,new Runnable() {
          public void run() {
            // MEMEToolkit.logComment("  Starting AttributeName Differences", true);
            StringBuffer sb = new StringBuffer();
            /*
              1. flag 1 - atns that have current source name and inserted before current source's real insertion date
              2. flag 2 - atns that have current source name and inserted after current source's real insertion date
              3. flag 2 - atns that have previous source name
                 current_source count = flag 1 + flag 2  (2)
                 previous_source count = flag 1 + flag 2 (3)
            */
            sb.append(" SELECT b.source, attribute_name, sum(ct) ct")
            .append(" FROM ")
            .append(" (SELECT source, attribute_name, flag, count(*) ct FROM ")
            .append("   (SELECT /*+ parallel(a) */ a.source, a.attribute_name, DECODE(is_current, ")
            .append("        'Y', DECODE(a.released, 'N', 2, 1), 2) flag   ")
            .append("    FROM attributes a,  ")
            .append("    (SELECT src.source, DECODE(src.source, c.current_name, 'Y','N') is_current ")
            .append("     FROM source_rank src, source_version c, sims_info si ")
            .append("     WHERE src.stripped_source = c.source ")
            .append("       AND src.source = si.source ")
            .append("       AND si.remove_meta_version IS NULL ) src ")
            .append("    WHERE a.source = src.source  ")
            .append("      AND NOT (a.tobereleased IN ('N','n') ")
            .append("           AND a.released = 'N')")
            .append("      ) ")
            .append("  GROUP BY source, attribute_name, flag ) a , ")
            .append(" (SELECT 1 flag, current_name as current_name, current_name as source ")
            .append("  FROM source_version ")
            .append("  WHERE current_name IS NOT NULL ")
            .append("  UNION ")
            .append("  SELECT 2 flag, current_name as current_name, current_name as source ")
            .append("  FROM source_version ")
            .append("  WHERE current_name IS NOT NULL ")
            .append("  UNION ")
            .append("  SELECT DECODE((SELECT count(*) FROM source_version WHERE current_name=d.source) ")
            .append("                ,0,2,1) flag, a.source as current_name, b.source as source")
            .append("  FROM source_rank a, sims_info b, source_rank c, sims_info d ")
            .append("  WHERE b.insert_meta_version IS NOT NULL ")
            .append("  AND b.remove_meta_version IS NULL ")
            .append("  AND b.source NOT IN (SELECT current_name FROM source_version WHERE current_name IS NOT NULL) ")
            .append("  AND b.source = c.source ")
            .append("  AND a.stripped_source = c.stripped_source ")
            .append("  AND a.source = d.source ")
            .append("  AND NVL(d.insert_meta_version, b.insert_meta_version) = b.insert_meta_version ")
            .append("  AND d.remove_meta_version IS NULL ")
            .append("  ) b ")
            .append(" WHERE a.flag = b.flag ")
            .append(" AND a.source = b.current_name ")
            .append(" GROUP BY b.source, attribute_name ");
           String query = sb.toString();
            String source = null;
            try {
              // MEMEToolkit.logComment("  Creating Statement AttributeName Differences", true);
              Statement stmt = data_source.createStatement();
              // MEMEToolkit.logComment("  Executing Statement AttributeName Differences", true);
              ResultSet rs = stmt.executeQuery(query);
              Map oldValues = new HashMap();
              Map newValues = new HashMap();
              // MEMEToolkit.logComment("  Processed Statement AttributeName Differences", true);
              // Read attribute names
              while (rs.next()) {
                source = rs.getString("source");
                String key = rs.getString("attribute_name");
                Source tempSource = data_source.getSource(source);
                if (oldSources.contains(tempSource) ||
                      obsoleteSources.contains(tempSource) ||
                      unchangedSources.contains(tempSource)) {
                  Value value = new Value();
                  value.setValue(key);
                  if (oldValues.containsKey(key)) {
                    value = (Value) oldValues.get(key);
                  }
                  value.setCount(tempSource.getSourceAbbreviation(),
                                 rs.getInt("ct"));
                  value.setSourceAbbreviation(tempSource.getSourceAbbreviation());
                  oldValues.put(key, value);
                }
                if ( newSources.contains(tempSource) ||
                      updateSources.contains(tempSource) ||
                      unchangedSources.contains(tempSource) ) {
                  Value value = new Value();
                  value.setValue(key);
                  if (newValues.containsKey(key)) {
                    value = (Value) newValues.get(key);
                  }
                  value.setCount(tempSource.getSourceAbbreviation(),
                                 rs.getInt("ct"));
                  value.setSourceAbbreviation(tempSource.getSourceAbbreviation());
                  newValues.put(key, value);
                }
              }
              // Close statement
              stmt.close();
              SourceDifference sourceDifference = new SourceDifference();
              sourceDifference.setName("ATN");
              sourceDifference.setNewValues( (Value[]) newValues.values().
                                            toArray(new
                  Value[0]));
              sourceDifference.setOldValues( (Value[]) oldValues.values().
                                            toArray(new
                  Value[0]));
              sourceMetadataReport.setAttributeNameDifferences(new
                  SourceDifference[] {
                  sourceDifference});
              // MEMEToolkit.logComment("  Finished AttributeName Differences", true);
            }
            catch (SQLException se) {
              // MEMEToolkit.logComment("  SQLException AttributeName Differences", true);
              DataSourceException dse = new DataSourceException(
                  "Failed to look up source info.", query, se);
              dse.setDetail("query", query);
              MEMEToolkit.handleError(dse);
            }
            catch (BadValueException bve) {
              // MEMEToolkit.logComment("  BadValueException AttributeName Differences", true);
              DataSourceException dse = new DataSourceException(
                  "Failed to source metadata report.", source, bve);
              MEMEToolkit.handleError(dse);
            }
            catch (DataSourceException dse) {
              // MEMEToolkit.logComment("  DataSourceException AttributeName Differences", true);
              MEMEToolkit.handleError(dse);
            }
            catch (Exception e) {
              e.printStackTrace();
              BadValueException bve = new BadValueException(
                  "Invalid source value");
              bve.setEnclosedException(e);
              bve.setDetail("source", source);
              MEMEToolkit.handleError(bve);
            }
          }
        });

        th[0] = new Thread(tg, new Runnable() {
          public void run() {
            // MEMEToolkit.logComment("  Starting Termgroup Differences", true);
            StringBuffer sb = new StringBuffer();
            /*
              1. flag 1 - ttys that have current source name and inserted before current source's real insertion date
              2. flag 2 - ttys that have current source name and inserted after current source's real insertion date
              3. flag 2 - ttys that have previous source name
                 current_source count = flag 1 + flag 2  (2)
                 previous_source count = flag 1 + flag 2 (3)
            */
            sb.append(" SELECT b.source, tty, sum(ct) ct")
            .append(" FROM ")
            .append(" (SELECT source, tty, flag, count(*) ct FROM ")
            .append("   (SELECT /*+ parallel(a) */ a.source, a.tty, DECODE(is_current, ")
            .append("        'Y', DECODE(a.released, 'N', 2, 1), 2) flag   ")
            .append("    FROM classes a,  ")
            .append("    (SELECT src.source, DECODE(src.source, c.current_name, 'Y','N') is_current ")
            .append("     FROM source_rank src, source_version c, sims_info si ")
            .append("     WHERE src.stripped_source = c.source ")
            .append("       AND src.source = si.source ")
            .append("       AND si.remove_meta_version IS NULL ) src ")
            .append("    WHERE a.source = src.source  ")
            .append("      AND NOT (a.tobereleased IN ('N','n') ")
            .append("           AND a.released = 'N')")
            .append("      ) ")
            .append("  GROUP BY source, tty, flag ) a , ")
            .append(" (SELECT 1 flag, current_name as current_name, current_name as source ")
            .append("  FROM source_version ")
            .append("  WHERE current_name IS NOT NULL ")
            .append("  UNION ")
            .append("  SELECT 2 flag, current_name as current_name, current_name as source ")
            .append("  FROM source_version ")
            .append("  WHERE current_name IS NOT NULL ")
            .append("  UNION ")
            .append("  SELECT DECODE((SELECT count(*) FROM source_version WHERE current_name=d.source) ")
            .append("                ,0,2,1) flag, a.source as current_name, b.source as source")
            .append("  FROM source_rank a, sims_info b, source_rank c, sims_info d ")
            .append("  WHERE b.insert_meta_version IS NOT NULL ")
            .append("  AND b.remove_meta_version IS NULL ")
            .append("  AND b.source NOT IN (SELECT current_name FROM source_version WHERE current_name IS NOT NULL) ")
            .append("  AND b.source = c.source ")
            .append("  AND a.stripped_source = c.stripped_source ")
            .append("  AND a.source = d.source ")
            .append("  AND NVL(d.insert_meta_version, b.insert_meta_version) = b.insert_meta_version ")
            .append("  AND d.remove_meta_version IS NULL ")
            .append("  ) b ")
            .append(" WHERE a.flag = b.flag ")
            .append(" AND a.source = b.current_name ")
            .append(" GROUP BY b.source, tty ");
           String query = sb.toString();
            String source = null;
            try {
              // MEMEToolkit.logComment("  Creating Statement Termgroup Differences", true);
              Statement stmt = data_source.createStatement();
              // MEMEToolkit.logComment("  Executing Statement Termgroup Differences", true);
              ResultSet rs = stmt.executeQuery(query);
              // MEMEToolkit.logComment("  Processed Statement Termgroup Differences", true);
              Map oldValues = new HashMap();
              Map newValues = new HashMap();
              // Read termgroups
              while (rs.next()) {
                source = rs.getString("source");
                String key = rs.getString("tty");
                Source tempSource = data_source.getSource(source);
                if ( oldSources.contains(tempSource) ||
                      obsoleteSources.contains(tempSource) ||
                      unchangedSources.contains(tempSource) ) {
                  Value value = new Value();
                  value.setValue(key);
                  if (oldValues.containsKey(key)) {
                    value = (Value) oldValues.get(key);
                  }
                  value.setCount(tempSource.getSourceAbbreviation(),
                                 rs.getInt("ct"));
                  value.setSourceAbbreviation(tempSource.getSourceAbbreviation());
                  oldValues.put(key, value);
                }
                if ( newSources.contains(tempSource) ||
                      updateSources.contains(tempSource) ||
                      unchangedSources.contains(tempSource) ) {
                  Value value = new Value();
                  value.setValue(key);
                  if (newValues.containsKey(key)) {
                    value = (Value) newValues.get(key);
                  }
                  value.setCount(tempSource.getSourceAbbreviation(),
                                 rs.getInt("ct"));
                  value.setSourceAbbreviation(tempSource.getSourceAbbreviation());
                  newValues.put(key, value);
                }
              }
              // Close statement
              stmt.close();
              SourceDifference sourceDifference = new SourceDifference();
              sourceDifference.setName("TTY");
              sourceDifference.setNewValues( (Value[]) newValues.values().
                                            toArray(new
                  Value[0]));
              sourceDifference.setOldValues( (Value[]) oldValues.values().
                                            toArray(new
                  Value[0]));
              sourceMetadataReport.setTermgroupDifferences(new SourceDifference[] {
                  sourceDifference});
              // MEMEToolkit.logComment("  Finished Termgroup Differences", true);
            }
            catch (SQLException se) {
              DataSourceException dse = new DataSourceException(
                  "Failed to look up source info.", query, se);
              dse.setDetail("query", query);
              se.printStackTrace();
              MEMEToolkit.handleError(dse);
            }
            catch (BadValueException bve) {
              DataSourceException dse = new DataSourceException(
                  "Failed to source metadata report.", source, bve);
              bve.printStackTrace();
              MEMEToolkit.handleError(dse);
            }
            catch (DataSourceException dse) {
              dse.printStackTrace();
              MEMEToolkit.handleError(dse);
            }
            catch (Exception e) {
              e.printStackTrace();
              BadValueException bve = new BadValueException(
                  "Invalid source value");
              bve.setEnclosedException(e);
              bve.setDetail("source", source);
              MEMEToolkit.handleError(bve);
            }
          }
        });
        th[2] = new Thread(tg, new Runnable() {
          public void run() {
            // MEMEToolkit.logComment("  Starting RelationshipAttribute Differences", true);
            StringBuffer sb = new StringBuffer();
            /*
              1. flag 1 - relas that have current source name and inserted before current source's real insertion date
              2. flag 2 - relas that have current source name and inserted after current source's real insertion date
              3. flag 2 - relas that have previous source name
                 current_source count = flag 1 + flag 2  (2)
                 previous_source count = flag 1 + flag 2 (3)
            */
            sb.append(" SELECT b.source, relationship_attribute, sum(ct) ct")
            .append(" FROM ")
            .append(" (SELECT source, relationship_attribute, flag, count(*) ct FROM ")
            .append("   (SELECT /*+ parallel(a) */ a.source, a.relationship_attribute, DECODE(is_current, ")
            .append("        'Y', DECODE(a.released, 'N', 2, 1), 2) flag   ")
            .append("    FROM (SELECT rui, relationship_attribute, source, released FROM relationships " )
            .append("          WHERE NOT (tobereleased IN ('N','n') ")
            .append("           AND released = 'N')")
            .append("          UNION ALL " )
            .append("          SELECT /*+ USE_HASH(r,i) */ inverse_rui as rui, inverse_rel_attribute as relationship_attribute, source, released " )
            .append("          FROM relationships r, inverse_rel_attributes a, inverse_relationships_ui i " )
            .append("          WHERE r.relationship_attribute = a.relationship_attribute " )
            .append("            AND r.rui = i.rui " )
            .append("            AND NOT (r.tobereleased IN ('N','n') ")
            .append("           AND r.released = 'N')")
            .append("          UNION ALL " )
            .append("          SELECT rui, relationship_attribute, source, released FROM context_relationships " )
            .append("          WHERE NOT (tobereleased IN ('N','n') ")
            .append("           AND released = 'N')")
            .append("          UNION ALL " )
            .append("          SELECT /*+ USE_HASH(r,i) */ inverse_rui as rui, inverse_rel_attribute as relationship_attribute, source, released " )
            .append("          FROM context_relationships r, inverse_rel_attributes a, inverse_relationships_ui i " )
            .append("          WHERE r.relationship_attribute = a.relationship_attribute " )
            .append("            AND NOT (r.tobereleased IN ('N','n') ")
            .append("           AND r.released = 'N')")
            .append("            AND r.rui = i.rui) a , " )
            .append("          (SELECT src.source, DECODE(src.source, c.current_name, 'Y','N') is_current ")
            .append("           FROM source_rank src, source_version c, sims_info si ")
            .append("           WHERE src.stripped_source = c.source ")
            .append("             AND src.source = si.source ")
            .append("             AND si.remove_meta_version IS NULL ) src ")
            .append("    WHERE a.source = src.source )")
            .append("  GROUP BY source, relationship_attribute, flag ) a , ")
            .append(" (SELECT 1 flag, current_name as current_name, current_name as source ")
            .append("  FROM source_version ")
            .append("  WHERE current_name IS NOT NULL ")
            .append("  UNION ")
            .append("  SELECT 2 flag, current_name as current_name, current_name as source ")
            .append("  FROM source_version ")
            .append("  WHERE current_name IS NOT NULL ")
            .append("  UNION ")
            .append("  SELECT DECODE((SELECT count(*) FROM source_version WHERE current_name=d.source) ")
            .append("                ,0,2,1) flag, a.source as current_name, b.source as source")
            .append("  FROM source_rank a, sims_info b, source_rank c, sims_info d ")
            .append("  WHERE b.insert_meta_version IS NOT NULL ")
            .append("  AND b.remove_meta_version IS NULL ")
            .append("  AND b.source NOT IN (SELECT current_name FROM source_version WHERE current_name IS NOT NULL) ")
            .append("  AND b.source = c.source ")
            .append("  AND a.stripped_source = c.stripped_source ")
            .append("  AND a.source = d.source ")
            .append("  AND NVL(d.insert_meta_version, b.insert_meta_version) = b.insert_meta_version ")
            .append("  AND d.remove_meta_version IS NULL ")
            .append("  ) b ")
            .append(" WHERE a.flag = b.flag ")
            .append(" AND a.source = b.current_name ")
            .append(" GROUP BY b.source, relationship_attribute ");

            String query = sb.toString();
            String source = null;
            try {
              // MEMEToolkit.logComment("  Creating Statement RelationshipAttribute Differences", true);
              Statement stmt = data_source.createStatement();
              // MEMEToolkit.logComment("  Executing Statement RelationshipAttribute Differences", true);
              ResultSet rs = stmt.executeQuery(query);
              Map oldValues = new HashMap();
              Map newValues = new HashMap();
              // MEMEToolkit.logComment("  Processed Satement RelationshipAttribute Differences", true);
              // Read relationship attributes
              while (rs.next()) {
                source = rs.getString("source");
                String key = rs.getString("relationship_attribute");
                Source tempSource = data_source.getSource(source);
                if ( oldSources.contains(tempSource) ||
                      obsoleteSources.contains(tempSource) ||
                      unchangedSources.contains(tempSource) ) {
                  Value value = new Value();
                  value.setValue(key);
                  if (oldValues.containsKey(key)) {
                    value = (Value) oldValues.get(key);
                  }
                  value.setCount(tempSource.getSourceAbbreviation(),
                                 rs.getInt("ct"));
                  value.setSourceAbbreviation(tempSource.getSourceAbbreviation());
                  oldValues.put(key, value);
                }
                if ( newSources.contains(tempSource) ||
                      updateSources.contains(tempSource) ||
                      unchangedSources.contains(tempSource) ) {
                  Value value = new Value();
                  value.setValue(key);
                  if (newValues.containsKey(key)) {
                    value = (Value) newValues.get(key);
                  }
                  value.setCount(tempSource.getSourceAbbreviation(),
                                 rs.getInt("ct"));
                  value.setSourceAbbreviation(tempSource.getSourceAbbreviation());
                  newValues.put(key, value);
                }
              }
              // Close statement
              stmt.close();
              SourceDifference sourceDifference = new SourceDifference();
              sourceDifference.setName("RELA");
              sourceDifference.setNewValues( (Value[]) newValues.values().
                                            toArray(new
                  Value[0]));
              sourceDifference.setOldValues( (Value[]) oldValues.values().
                                            toArray(new
                  Value[0]));
              sourceMetadataReport.setRelationshipAttributeDifferences(new
                  SourceDifference[] {
                  sourceDifference});
              // MEMEToolkit.logComment("  Finished RelationshipAttribute Differences", true);
            }
            catch (SQLException se) {
              DataSourceException dse = new DataSourceException(
                  "Failed to look up source info.", query, se);
              dse.setDetail("query", query);
              MEMEToolkit.handleError(dse);
            }
            catch (BadValueException bve) {
              DataSourceException dse = new DataSourceException(
                  "Failed to source metadata report.", source, bve);
              MEMEToolkit.handleError(dse);
            }
            catch (DataSourceException dse) {
              MEMEToolkit.handleError(dse);
            }
            catch (Exception e) {
              e.printStackTrace();
              BadValueException bve = new BadValueException(
                  "Invalid source value");
              bve.setEnclosedException(e);
              bve.setDetail("source", source);
              MEMEToolkit.handleError(bve);
            }
          }
        });
        for(int i = 0; i < th.length; i++ ) {
          th[i].start();
        }

        for(int i = 0 ; i < th.length ; i++ ) {
          try {
            // MEMEToolkit.logComment("  Join th[" + i + "] isAlive = " + th[i].isAlive(), true);
            th[i].join();
            // MEMEToolkit.logComment("  Return join th[" + i + "] isAlive = " + th[i].isAlive(), true);
          }catch (InterruptedException inte) {inte.printStackTrace();}
        }
        // MEMEToolkit.logComment(" Active Thread Count in Group = " + tg.activeCount(), true);
        cacheReport = true;
        MEMEToolkit.logComment("  Finished cache source metadata report", true);
      }
      catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to look up source info.", query, se);
        dse.setDetail("query", query);
        throw dse;
      }
      catch (BadValueException bve) {
        DataSourceException dse = new DataSourceException(
            "Failed to source metadata report.", source, bve);
        throw dse;
      }
    }
    catch (MEMEException e) {
      MEMEToolkit.handleError(e);
    }
  }

  /**
   * Stops the thread.
   */
  public void stop() {
    thread.interrupt();
  }

  /**
   * Starts the thread.
   */
  public void start() {
    // start
    thread = new Thread(this);
    thread.start();
  }
}
