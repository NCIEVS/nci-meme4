/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MEMERelaEditorService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.RelationshipAttributeAction;
import gov.nih.nlm.meme.action.RelationshipNameAction;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.sql.MEMEConnection;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;

/**
 * This class services requests for the {@link gov.nih.nlm.meme.client.MEMERelaEditorKit}.
 *
 * @author MEME Group
 */
public class MEMERelaEditorService implements MEMEApplicationService {

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
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();
    String function = (String) request.getParameter("function").getValue();

    int transaction_id = 0;

    if (function.equals("processRelationship") ||
        function.equals("insertRelationship") ||
        function.equals("changeRelationshipTBR")) {

      MEMEConnection mid = (MEMEConnection) context.getDataSource();
      try {
        // Get transaction_id
        MEMEToolkit.trace("\tAssigning next transaction_id... ");
        CallableStatement cstmt = null;
        cstmt = mid.prepareCall(
            "{ ? = call MEME_UTILITY.get_next_id('TRANSACTIONS') }");
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.execute();
        transaction_id = cstmt.getInt(1);
        MEMEToolkit.trace("\tdone. transaction_id=(" + transaction_id + ").");
        cstmt.close();
        mid.commit();
      } catch (SQLException se) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to get next id.", mid, se);
        dse.setDetail("type", "transaction");
        throw dse;
      }
    }

    if (function.equals("getRelationshipNames")) {

      // Return an array of valid relationship names
      request.addReturnValue(new Parameter.Default(
          "rel_names", data_source.getRelationshipNames()));

    } else if (function.equals("getRelationshipAttributes")) {

      // Return an array of valid relationship attribute values
      // is null in this list?
      request.addReturnValue(new Parameter.Default(
          "rela_attributes", data_source.getRelationshipAttributes()));

    } else if (function.equals("insertRelationshipAttribute")) {

      String rela = (String) request.getParameter("rela").getValue();
      String inverse = (String) request.getParameter("rela").getValue();
      int rank = request.getParameter("rank").getInt();
      RelationshipAttributeAction raa =
          RelationshipAttributeAction.newAddRelationshipAttributeAction(rela,
          inverse, rank);
      data_source.getActionEngine().processAction(raa);

      try {
        data_source.commit();
      } catch (Exception e) {}

    } else if (function.equals("removeRelationshipAttribute")) {

      String rela = (String) request.getParameter("rela").getValue();
      RelationshipAttributeAction raa =
          RelationshipAttributeAction.newRemoveRelationshipAttributeAction(rela);
      data_source.getActionEngine().processAction(raa);

      try {
        data_source.commit();
      } catch (Exception e) {}

    } else if (function.equals("insertRelationshipName")) {

      String name = (String) request.getParameter("name").getValue();
      String inverse_name = (String) request.getParameter("inverse_name").
          getValue();
      boolean weak = request.getParameter("weak").getBoolean();
      String long_name = (String) request.getParameter("long_name").getValue();
      String inverse_long_name = (String) request.getParameter(
          "inverse_long_name").getValue();
      String release_name = (String) request.getParameter("release_name").
          getValue();
      String inverse_release_name = (String) request.getParameter(
          "inverse_release_name").getValue();

      RelationshipNameAction rna =
          RelationshipNameAction.newAddRelationshipNameAction(name,
          inverse_name, weak, long_name, inverse_long_name, release_name,
          inverse_release_name);
      data_source.getActionEngine().processAction(rna);

      try {
        data_source.commit();
      } catch (Exception e) {}

    } else if (function.equals("removeRelationshipName")) {

      String name = (String) request.getParameter("name").getValue();
      RelationshipNameAction rna =
          RelationshipNameAction.newRemoveRelationshipNameAction(name);
      data_source.getActionEngine().processAction(rna);

      try {
        data_source.commit();
      } catch (Exception e) {}

    } else if (function.equals("getSources")) {

      // Return an array of valid source names
      Source[] sources = data_source.getSources();
      String[] source_abbs = new String[sources.length];
      for (int i = 0; i < sources.length; i++) {
        source_abbs[i] = sources[i].getSourceAbbreviation();
      }
      request.addReturnValue(new Parameter.Default("sources", source_abbs));

    } else if (function.equals("processRelationship")) {

      // Process an existing relationship.  This functionality
      // finds a matching relationship (based on either cui_[12]
      // of atom_id_[12] passed in) and updates the
      // relationship_name, relationship_attribute, etc. fields
      // where they have changed.

      // Prepare connection
      MEMEConnection mid = (MEMEConnection) context.getDataSource();

      // Extract relevant parameters

      String pr_cui_1 = null;
      String pr_cui_2 = null;
      int pr_atom_id_1 = 0;
      int pr_atom_id_2 = 0;
      int pr_concept_id_1 = 0;
      int pr_concept_id_2 = 0;

      if (request.getParameter("pr_cui_1") != null) {
        pr_cui_1 = (String) request.getParameter("pr_cui_1").getValue();
      }
      if (request.getParameter("pr_cui_2") != null) {
        pr_cui_2 = (String) request.getParameter("pr_cui_2").getValue();
      }
      if (request.getParameter("pr_atom_id_1") != null) {
        pr_atom_id_1 = request.getParameter("pr_atom_id_1").getInt();
      }
      if (request.getParameter("pr_atom_id_2") != null) {
        pr_atom_id_2 = request.getParameter("pr_atom_id_2").getInt();
      }
      if (request.getParameter("pr_concept_id_1") != null) {
        pr_concept_id_1 = request.getParameter("pr_concept_id_1").getInt();
      }
      if (request.getParameter("pr_concept_id_2") != null) {
        pr_concept_id_2 = request.getParameter("pr_concept_id_2").getInt();

      }

      String pr_rel_name = (String) request.getParameter("pr_rel_name").
          getValue();
      String pr_rel_attr = (String) request.getParameter("pr_rel_attr").
          getValue();
      String pr_source = (String) request.getParameter("pr_source").getValue();
      String pr_source_of_label = (String) request.getParameter(
          "pr_source_of_label").getValue();
      String pr_authority = (String) request.getParameter("pr_authority").
          getValue();
      int pr_rel_id = request.getParameter("pr_rel_id").getInt();

      MEMEToolkit.trace("\tDisplaying arguments value...");
      MEMEToolkit.trace("\t\tpr_cui_1= " + pr_cui_1);
      MEMEToolkit.trace("\t\tpr_cui_2= " + pr_cui_2);
      MEMEToolkit.trace("\t\tpr_atom_id_1= " + pr_atom_id_1);
      MEMEToolkit.trace("\t\tpr_atom_id_2= " + pr_atom_id_2);
      MEMEToolkit.trace("\t\tpr_concept_id_1= " + pr_concept_id_1);
      MEMEToolkit.trace("\t\tpr_concept_id_2= " + pr_concept_id_2);
      MEMEToolkit.trace("\t\tpr_relationship_name= " + pr_rel_name);
      MEMEToolkit.trace("\t\tpr_relationship_attribute= " + pr_rel_attr);
      MEMEToolkit.trace("\t\tpr_source= " + pr_source);
      MEMEToolkit.trace("\t\tpr_source_of_label= " + pr_source_of_label);
      MEMEToolkit.trace("\t\tpr_authority= " + pr_authority);
      MEMEToolkit.trace("\t\tpr_relationship_id= " + pr_rel_id);

      // Determine which function to use based on parameter passing
      // return the relationship_id
      if (pr_cui_1 != null && pr_cui_2 != null) {
        request.addReturnValue(new Parameter.Default("process_rel",
            processRelationshipBetweenCUIs(mid,
                                           pr_cui_1, pr_cui_2, pr_rel_name,
                                           pr_rel_attr, pr_source,
                                           pr_source_of_label, pr_authority,
                                           pr_rel_id, transaction_id)));
      } else if (pr_atom_id_1 > 0 && pr_atom_id_2 > 0) {
        request.addReturnValue(new Parameter.Default("process_rel",
            processRelationshipBetweenAtoms(mid,
                                            pr_atom_id_1, pr_atom_id_2,
                                            pr_rel_name, pr_rel_attr, pr_source,
                                            pr_source_of_label, pr_authority,
                                            pr_rel_id, transaction_id)));
      } else if (pr_concept_id_1 > 0 && pr_concept_id_2 > 0) {
        request.addReturnValue(new Parameter.Default("process_rel",
            processRelationshipBetweenConcepts(mid,
                                               pr_concept_id_1, pr_concept_id_2,
                                               pr_rel_name, pr_rel_attr,
                                               pr_source,
                                               pr_source_of_label, pr_authority,
                                               pr_rel_id, transaction_id)));

      }

    } else if (function.equals("insertRelationship")) {

      // Insert a relationship.  This will either be a
      // CUI=>CUI or an atom=>atom or a concept=> concept relationship based
      // on whether cui_[12] or atom_id_[12] or concept_id[12] parameters
      // were passed in.

      // Prepare connection
      MEMEConnection mid = (MEMEConnection) context.getDataSource();

      // Extract relevant parameters
      String ir_cui_1 = null;
      String ir_cui_2 = null;
      int ir_atom_id_1 = 0;
      int ir_atom_id_2 = 0;
      int ir_concept_id_1 = 0;
      int ir_concept_id_2 = 0;

      if (request.getParameter("ir_cui_1") != null) {
        ir_cui_1 = (String) request.getParameter("ir_cui_1").getValue();
      }
      if (request.getParameter("ir_cui_2") != null) {
        ir_cui_2 = (String) request.getParameter("ir_cui_2").getValue();
      }
      if (request.getParameter("ir_atom_id_1") != null) {
        ir_atom_id_1 = request.getParameter("ir_atom_id_1").getInt();
      }
      if (request.getParameter("ir_atom_id_2") != null) {
        ir_atom_id_2 = request.getParameter("ir_atom_id_2").getInt();
      }
      if (request.getParameter("ir_concept_id_1") != null) {
        ir_concept_id_1 = request.getParameter("ir_concept_id_1").getInt();
      }
      if (request.getParameter("ir_concept_id_2") != null) {
        ir_concept_id_2 = request.getParameter("ir_concept_id_2").getInt();

      }
      String ir_rel_name = (String) request.getParameter("ir_rel_name").
          getValue();
      String ir_rel_attr = (String) request.getParameter("ir_rel_attr").
          getValue();
      String ir_source = (String) request.getParameter("ir_source").getValue();
      String ir_source_of_label = (String) request.getParameter(
          "ir_source_of_label").getValue();
      String ir_authority = (String) request.getParameter("ir_authority").
          getValue();

      MEMEToolkit.trace("\tDisplaying arguments value...");
      MEMEToolkit.trace("\t\tir_cui_1= " + ir_cui_1);
      MEMEToolkit.trace("\t\tir_cui_2= " + ir_cui_2);
      MEMEToolkit.trace("\t\tir_atom_id_1= " + ir_atom_id_1);
      MEMEToolkit.trace("\t\tir_atom_id_2= " + ir_atom_id_2);
      MEMEToolkit.trace("\t\tir_concept_id_1= " + ir_concept_id_1);
      MEMEToolkit.trace("\t\tir_concept_id_2= " + ir_concept_id_2);
      MEMEToolkit.trace("\t\tir_relationship_name= " + ir_rel_name);
      MEMEToolkit.trace("\t\tir_relationship_attribute= " + ir_rel_attr);
      MEMEToolkit.trace("\t\tir_source= " + ir_source);
      MEMEToolkit.trace("\t\tir_source_of_label= " + ir_source_of_label);
      MEMEToolkit.trace("\t\tir_authority= " + ir_authority);

      // Determine which function to use, based on parameters
      // passed in.  Return the relationship_id of the relationship
      // inserted.
      if (ir_cui_1 != null && ir_cui_2 != null) {
        request.addReturnValue(new Parameter.Default("insert_rel",
            insertRelationshipBetweenCUIs(mid,
                                          ir_cui_1, ir_cui_2, ir_rel_name,
                                          ir_rel_attr, ir_source,
                                          ir_source_of_label, ir_authority,
                                          transaction_id)));
      } else if (ir_atom_id_1 > 0 && ir_atom_id_2 > 0) {
        request.addReturnValue(new Parameter.Default("insert_rel",
            insertRelationshipBetweenAtoms(mid,
                                           ir_atom_id_1, ir_atom_id_2,
                                           ir_rel_name, ir_rel_attr, ir_source,
                                           ir_source_of_label, ir_authority,
                                           transaction_id)));
      } else if (ir_concept_id_1 > 0 && ir_concept_id_2 > 0) {
        request.addReturnValue(new Parameter.Default("insert_rel",
            insertRelationshipBetweenConcepts(mid,
                                              ir_concept_id_1, ir_concept_id_2,
                                              ir_rel_name, ir_rel_attr,
                                              ir_source,
                                              ir_source_of_label, ir_authority,
                                              transaction_id)));

      }

    } else if (function.equals("changeRelationshipTBR")) {

      // Change the releasability of a relationship.

      // Prepare connection
      MEMEConnection mid = (MEMEConnection) context.getDataSource();

      // Extract relevant parameters
      int crtbr_rel_id = request.getParameter("crtbr_rel_id").getInt();
      String crtbr_authority = (String) request.getParameter("crtbr_authority").
          getValue();
      String crtbr_tbr = (String) request.getParameter("crtbr_tbr").getValue();

      MEMEToolkit.trace("\tDisplaying arguments value...");
      MEMEToolkit.trace("\t\tcrtbr_rel_id= " + crtbr_rel_id);
      MEMEToolkit.trace("\t\tcrtbr_authority= " + crtbr_authority);
      MEMEToolkit.trace("\t\tcrtbr_tbr= " + crtbr_tbr);

      request.addReturnValue(new Parameter.Default("change_rel_tbr",
          changeRelationshipTBR(mid, crtbr_rel_id, crtbr_authority, crtbr_tbr,
                                transaction_id)));

    } else {

      // Illegal function value.
      BadValueException bve = new BadValueException(
          "Bad request parameter value.");
      bve.setDetail("function", function);
      throw bve;
    }

  } // end processRequest

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
  // Private methods
  //

  /**
   * Format a call to the full <code>processRelationship</code> that has
   * <code>atom_id_1 = 0</code> and <code>atom_id_2 = 0</code>.
   * @param mid the {@link MEMEConnection}
   * @param pr_cui_1 is the possible CUI that this relationship connects
   * @param pr_cui_2 is the possible CUI that this relationship connects
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param pr_source is the source asserting the relationship
   * @param pr_source_of_label is the source asserting the nature of the relationship
   * @param pr_authority is the one responsible for the Update/Insert actions
   * @param pr_rel_id is the ID of the relationships to be processed
   * @param pr_transaction_id the transaction id being processed
   * @return <code>relationship_id</code>.
   * @throws MEMEException if process relationship between cuis failed
   */
  private int processRelationshipBetweenCUIs(MEMEConnection mid,
                                             String pr_cui_1, String pr_cui_2,
                                             String pr_rel_name,
                                             String pr_rel_attr,
                                             String pr_source,
                                             String pr_source_of_label,
                                             String pr_authority, int pr_rel_id,
                                             int pr_transaction_id) throws
      MEMEException {

    // Parameter validation
    if (pr_cui_1 == null || pr_cui_2 == null || pr_rel_name == null ||
        pr_source == null || pr_source_of_label == null ||
        pr_authority == null || pr_rel_id < 0) {
      throw new BadValueException("Bad request parameter value.");
    }

    return processRelationship(mid, pr_cui_1, pr_cui_2, 0, 0, 0, 0, pr_rel_name,
                               pr_rel_attr, pr_source, pr_source_of_label,
                               pr_authority, pr_rel_id,
                               pr_transaction_id);
  }

  /**
   * Format a call to the full <code>processRelationship</code> that has
   * <code>cui_1 = null</code> and <code>cui_2 = null</code>.
   * @param mid the {@link MEMEConnection}
       * @param pr_atom_id_1 is the possible atom id that this relationship connects
       * @param pr_atom_id_2 is the possible atom id that this relationship connects
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param pr_source is the source asserting the relationship
   * @param pr_source_of_label is the source asserting the nature of the relationship
   * @param pr_authority is the one responsible for the Update/Insert actions
   * @param pr_rel_id is the ID of the relationships to be processed
   * @param pr_transaction_id the transaction id being processed
   * @return <code>relationship_id</code>
   * @throws MEMEException if process relationship between atoms failed
   */
  private int processRelationshipBetweenAtoms(MEMEConnection mid,
                                              int pr_atom_id_1,
                                              int pr_atom_id_2,
                                              String pr_rel_name,
                                              String pr_rel_attr,
                                              String pr_source,
                                              String pr_source_of_label,
                                              String pr_authority,
                                              int pr_rel_id,
                                              int pr_transaction_id) throws
      MEMEException {

    // Parameter validation
    if (pr_atom_id_1 < 0 || pr_atom_id_2 < 0 || pr_rel_name == null ||
        pr_source == null || pr_source_of_label == null ||
        pr_authority == null || pr_rel_id < 0) {
      throw new BadValueException("Bad request parameter value.");
    }

    return processRelationship(mid, null, null, pr_atom_id_1, pr_atom_id_2, 0,
                               0, pr_rel_name,
                               pr_rel_attr, pr_source, pr_source_of_label,
                               pr_authority, pr_rel_id,
                               pr_transaction_id);

  }

  /**
   * Format a call to the full <code>processRelationship</code> that has
   * <code>concept_id_1 != null</code> and <code>concept_id_2 != null</code>.
   * @param mid the {@link MEMEConnection}
   * @param pr_concept_id_1 is the possible concept id that this relationship connects
   * @param pr_concept_id_2 is the possible concept id that this relationship connects
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param pr_source is the source asserting the relationship
   * @param pr_source_of_label is the source asserting the nature of the relationship
   * @param pr_authority is the one responsible for the Update/Insert actions
   * @param pr_rel_id is the ID of the relationships to be processed
   * @param pr_transaction_id the transaction id being processed
   * @return <code>relationship_id</code>
   * @throws MEMEException if process relationship between concepts failed
   */
  private int processRelationshipBetweenConcepts(MEMEConnection mid,
                                                 int pr_concept_id_1,
                                                 int pr_concept_id_2,
                                                 String pr_rel_name,
                                                 String pr_rel_attr,
                                                 String pr_source,
                                                 String pr_source_of_label,
                                                 String pr_authority,
                                                 int pr_rel_id,
                                                 int pr_transaction_id) throws
      MEMEException {

    // Parameter validation
    if (pr_concept_id_1 < 0 || pr_concept_id_2 < 0 || pr_rel_name == null ||
        pr_source == null || pr_source_of_label == null ||
        pr_authority == null || pr_rel_id < 0) {
      throw new BadValueException("Bad request parameter value.");
    }

    return processRelationship(mid, null, null, 0, 0, pr_concept_id_1,
                               pr_concept_id_2, pr_rel_name,
                               pr_rel_attr, pr_source, pr_source_of_label,
                               pr_authority, pr_rel_id,
                               pr_transaction_id);

  }

  /**
   * Inserts a new source-level relationship or
   * updates an existing one so its fields match the parameters.<p>
   *
   * This procedure must "fake" a <code>MOLECULAR_CHANGE_FIELD</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * It is an update if a source-level relationship match <code>relationship_id</code>. Otherwise,
   * it is an insert if no source-level relationship match <code>relationship_id</code>.<p>
   *
   * @param mid the {@link MEMEConnection}
   * @param pr_cui_1 is the possible CUI that this relationship connects
   * @param pr_cui_2 is the possible CUI that this relationship connects
       * @param pr_atom_id_1 is the possible atom id that this relationship connects
       * @param pr_atom_id_2 is the possible atom id that this relationship connects
   * @param pr_concept_id_1 is the possible concept id that this relationship connects
   * @param pr_concept_id_2 is the possible concept id that this relationship connects
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param pr_source is the source asserting the relationship
   * @param pr_source_of_label is the source asserting the nature of the relationship
   * @param pr_authority is the one responsible for the Update/Insert actions
   * @param pr_rel_id is the ID of the relationships to be processed
   * @param pr_transaction_id the transaction id being processed
   * @return <code>relationship_id</code>
   * @throws MEMEException if program validation failed
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  private int processRelationship(MEMEConnection mid,
                                  String pr_cui_1, String pr_cui_2,
                                  int pr_atom_id_1, int pr_atom_id_2,
                                  int pr_concept_id_1, int pr_concept_id_2,
                                  String pr_rel_name, String pr_rel_attr,
                                  String pr_source,
                                  String pr_source_of_label,
                                  String pr_authority, int pr_rel_id,
                                  int pr_transaction_id) throws MEMEException {

    // Variable declaration
    String l_rel_name = null;
    String l_rel_attr = null;
    String l_source = null;
    String l_source_of_label = null;
    String l_molecular_action = "MOLECULAR_INSERT";

    int l_molecule_id;

    int l_rel_id = 0;
    int l_rel_count = 0;
    int l_concept_id = 0;

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    java.util.Date timestamp = new java.util.Date();
    java.sql.Timestamp l_timestamp = new java.sql.Timestamp(timestamp.getTime());

    CallableStatement cstmt = null;
    Statement stmt = null;
    ResultSet rs = null;

    try {
      // Commit any uncommited transaction and turn auto commit off
      mid.commit();
      mid.setAutoCommit(false);
    } catch (SQLException se) {
      throw new DataSourceException("Failed to set commit mode.", mid, se);
    }

    // Get molecule_id
    MEMEToolkit.trace("\tAssigning next molecule_id... ");
    try {
      cstmt = mid.prepareCall(
          "{ ? = call MEME_UTILITY.get_next_id('MOLECULAR_ACTIONS') }");
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.execute();
      l_molecule_id = cstmt.getInt(1);
      MEMEToolkit.trace("\tdone. molecule_id=(" + l_molecule_id + ").");
      cstmt.close();
    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get next id.", mid, se);
      dse.setDetail("type", "molecule");
      throw dse;
    }

    try {
      // Get source-level relationship row
      MEMEToolkit.trace("\tSearching for source-level relationships row...");
      stmt = mid.createStatement();
      StringBuffer sqlSelect = new StringBuffer(500);

      sqlSelect.append("SELECT * FROM relationships WHERE relationship_id = ");
      sqlSelect.append(pr_rel_id);
      rs = stmt.executeQuery(sqlSelect.toString());

      while (rs.next()) {
        l_rel_count++;
        l_molecular_action = "MOLECULAR_CHANGE_FIELD";
        l_rel_name = rs.getString("RELATIONSHIP_NAME");
        l_rel_attr = rs.getString("RELATIONSHIP_ATTRIBUTE");
        l_source = rs.getString("SOURCE");
        l_source_of_label = rs.getString("SOURCE_OF_LABEL");
        l_rel_id = rs.getInt("RELATIONSHIP_ID");
        l_concept_id = rs.getInt("CONCEPT_ID_1");
      }

      stmt.close();

      // Verify single source-level relationship
      if (l_rel_count > 1) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        BadValueException bve = new BadValueException("Too much data.");
        bve.setDetail("relationship_id", String.valueOf(l_rel_id));
        bve.setDetail("relationship_count", String.valueOf(l_rel_count));
        throw bve;
      }

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get relationship.", mid, se);
      dse.setDetail("relationship_id", String.valueOf(l_rel_id));
      throw dse;
    }

    if (l_molecular_action.equals("MOLECULAR_INSERT")) {
      MEMEToolkit.trace("\tSource-level relationship not found.");
      MEMEToolkit.trace("\tPerforming Molecular Insert.");
      if (pr_cui_1 != null && pr_cui_2 != null) {
        return insertRelationshipBetweenCUIs(mid,
                                             pr_cui_1, pr_cui_2, pr_rel_name,
                                             pr_rel_attr, pr_source,
                                             pr_source_of_label, pr_authority,
                                             pr_transaction_id);
      } else if (pr_atom_id_1 > 0 && pr_atom_id_2 > 0) {
        return insertRelationshipBetweenAtoms(mid,
                                              pr_atom_id_1, pr_atom_id_2,
                                              pr_rel_name, pr_rel_attr,
                                              pr_source,
                                              pr_source_of_label, pr_authority,
                                              pr_transaction_id);
      } else if (pr_concept_id_1 > 0 && pr_concept_id_2 > 0) {
        return insertRelationshipBetweenConcepts(mid,
                                                 pr_concept_id_1,
                                                 pr_concept_id_2, pr_rel_name,
                                                 pr_rel_attr, pr_source,
                                                 pr_source_of_label,
                                                 pr_authority,
                                                 pr_transaction_id);
      }
    }

    MEMEToolkit.trace("\tSource-level relationship found.");

    // Fix source and source_of_label value
    fixSource(mid, pr_source);
    fixSource(mid, pr_source_of_label);

    try {
      // Insert into molecular_action
      MEMEToolkit.trace("\tInserting new molecular_actions row... ");

      stmt = mid.createStatement();
      StringBuffer sqlInsertMA = new StringBuffer(500);

      sqlInsertMA.append(
          "INSERT INTO molecular_actions (transaction_id,molecule_id, ");
      sqlInsertMA.append(
          "authority, timestamp, molecular_action, source_id, target_id, ");
      sqlInsertMA.append(
          "undone, undone_by, undone_when, status, elapsed_time, work_id) ");
      sqlInsertMA.append("VALUES ( ");
      sqlInsertMA.append(pr_transaction_id);
      sqlInsertMA.append(",");
      sqlInsertMA.append(l_molecule_id);
      sqlInsertMA.append(",'");
      sqlInsertMA.append(pr_authority);
      sqlInsertMA.append("','");
      sqlInsertMA.append(formatter.format(timestamp));
      sqlInsertMA.append("','");
      sqlInsertMA.append(l_molecular_action);
      sqlInsertMA.append("',");
      sqlInsertMA.append(l_concept_id);
      sqlInsertMA.append(",0,'N','','','R',1,0)");

      stmt.executeUpdate(sqlInsertMA.toString());
      stmt.close();
      MEMEToolkit.trace("\tdone. molecule_id=(" + l_molecule_id + ").");

      if (l_molecular_action.equals("MOLECULAR_CHANGE_FIELD")) {
        // Verify relationship_name and
        // Call MEME_APROCS.aproc_change_field
        if (!l_rel_name.equals(pr_rel_name)) {
          MEMEToolkit.trace("\tUpdating relationship_name field... ");
          cstmt = mid.prepareCall(
              "{ ? = call MEME_APROCS.aproc_change_field("
              + "row_id => ?,"
              + "table_name => 'R',"
              + "field_name => 'relationship_name',"
              + "old_value => ?,"
              + "new_value => ?,"
              + "action_status => 'R',"
              + "molecule_id => ?,"
              + "authority => ?,"
              + "timestamp => ?) }");
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setInt(2, pr_rel_id);
          cstmt.setString(3, l_rel_name);
          cstmt.setString(4, pr_rel_name);
          cstmt.setInt(5, l_molecule_id);
          cstmt.setString(6, pr_authority);
          cstmt.setTimestamp(7, l_timestamp);
          cstmt.execute();
          MEMEToolkit.trace("\tdone.");
          if (cstmt.getInt(1) < 0) {
            try {
              mid.rollback();
            } catch (Exception e) {}
            throw new DataSourceException("Updating relationship_name failed "
                                          + "(see meme_error).");
          }
          cstmt.close();
        }
        // Verify relationship_attribute field
        if ( (l_rel_attr == null && pr_rel_attr != null) ||
            (l_rel_attr != null && !l_rel_attr.equals(pr_rel_attr)) ||
            (l_rel_attr != null && pr_rel_attr == null)) {
          MEMEToolkit.trace("\tUpdating relationship_attribute field... ");
          cstmt = mid.prepareCall(
              "{ ? = call MEME_APROCS.aproc_change_field("
              + "row_id => ?,"
              + "table_name => 'R',"
              + "field_name => 'relationship_attribute',"
              + "old_value => ?,"
              + "new_value => ?,"
              + "action_status => 'R',"
              + "molecule_id => ?,"
              + "authority => ?,"
              + "timestamp => ?) }");
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setInt(2, pr_rel_id);
          cstmt.setString(3, l_rel_attr);
          cstmt.setString(4, pr_rel_attr);
          cstmt.setInt(5, l_molecule_id);
          cstmt.setString(6, pr_authority);
          cstmt.setTimestamp(7, l_timestamp);
          cstmt.execute();
          MEMEToolkit.trace("\tdone.");
          if (cstmt.getInt(1) < 0) {
            try {
              mid.rollback();
            } catch (Exception e) {}
            throw new DataSourceException(
                "Updating relationship_attribute failed "
                + "(see meme_error).");
          }
          cstmt.close();
        }
        // Verify source field
        if (!l_source.equals(pr_source)) {
          MEMEToolkit.trace("\tUpdating source field... ");
          cstmt = mid.prepareCall(
              "{ ? = call MEME_APROCS.aproc_change_field("
              + "row_id => ?,"
              + "table_name => 'R',"
              + "field_name => 'source',"
              + "old_value => ?,"
              + "new_value => ?,"
              + "action_status => 'R',"
              + "molecule_id => ?,"
              + "authority => ?,"
              + "timestamp => ?) }");
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setInt(2, pr_rel_id);
          cstmt.setString(3, l_source);
          cstmt.setString(4, pr_source);
          cstmt.setInt(5, l_molecule_id);
          cstmt.setString(6, pr_authority);
          cstmt.setTimestamp(7, l_timestamp);
          cstmt.execute();
          MEMEToolkit.trace("\tdone.");
          if (cstmt.getInt(1) < 0) {
            try {
              mid.rollback();
            } catch (Exception e) {}
            throw new DataSourceException("Updating source failed "
                                          + "(see meme_error).");
          }
          cstmt.close();
        }
        // Verify source_of_label field
        if (!l_source_of_label.equals(pr_source_of_label)) {
          MEMEToolkit.trace("\tUpdating source_of_label field... ");
          cstmt = mid.prepareCall(
              "{ ? = call MEME_APROCS.aproc_change_field("
              + "row_id => ?,"
              + "table_name => 'R',"
              + "field_name => 'source_of_label',"
              + "old_value => ?,"
              + "new_value => ?,"
              + "action_status => 'R',"
              + "molecule_id => ?,"
              + "authority => ?,"
              + "timestamp => ?) }");
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setInt(2, pr_rel_id);
          cstmt.setString(3, l_source_of_label);
          cstmt.setString(4, pr_source_of_label);
          cstmt.setInt(5, l_molecule_id);
          cstmt.setString(6, pr_authority);
          cstmt.setTimestamp(7, l_timestamp);
          cstmt.execute();
          MEMEToolkit.trace("\tdone.");
          if (cstmt.getInt(1) < 0) {
            try {
              mid.rollback();
            } catch (Exception e) {}
            throw new DataSourceException("Updating source_of_label failed "
                                          + "(see meme_error).");
          }
          cstmt.close();
        }
      } else {
        try {
          mid.rollback();
        } catch (Exception e) {}
        throw new BadValueException("Illegal action name.");
      }

      // Commit work
      mid.commit();

      return pr_rel_id;

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      throw new DataSourceException("Molocular action failed.", mid, se);
    }
  }

  /**
   * Format a call to the full <code>insertRelationship</code> that has
   * <code>atom_id_1 = 0</code> and <code>atom_id_2 = 0</code>.
   * @param mid the {@link MEMEConnection}
   * @param ir_cui_1 is the possible CUI that this relationship connects
   * @param ir_cui_2 is the possible CUI that this relationship connects
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param ir_source is the source asserting the relationship
   * @param ir_source_of_label is the source asserting the nature of the relationship
   * @param ir_authority is the one responsible for the Update/Insert actions
   * @param ir_transaction_id the transaction id being processed
   * @return <code>relationship_id</code>
   * @throws MEMEException if insert relationship between cuis failed
   */
  private int insertRelationshipBetweenCUIs(MEMEConnection mid,
                                            String ir_cui_1, String ir_cui_2,
                                            String ir_rel_name,
                                            String ir_rel_attr,
                                            String ir_source,
                                            String ir_source_of_label,
                                            String ir_authority,
                                            int ir_transaction_id) throws
      MEMEException {

    // Parameter validation
    if (ir_cui_1 == null || ir_cui_2 == null || ir_rel_name == null ||
        ir_source == null || ir_source_of_label == null ||
        ir_authority == null) {
      throw new BadValueException("Bad request parameter found.");
    }

    return insertRelationship(mid, ir_cui_1, ir_cui_2, 0, 0, 0, 0, ir_rel_name,
                              ir_rel_attr, ir_source, ir_source_of_label,
                              ir_authority, ir_transaction_id);

  }

  /**
   * Format a call to the full <code>insertRelationship</code> that has
   * <code>cui_1 = null</code> and <code>cui_2 = null</code>.
   *
   * @param mid the {@link MEMEConnection}.
       * @param ir_atom_id_1 is the possible atom id that this relationship connects.
       * @param ir_atom_id_2 is the possible atom id that this relationship connects.
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param ir_source is the source asserting the relationship.
   * @param ir_source_of_label is the source asserting the nature of the relationship.
   * @param ir_authority is the one responsible for the Update/Insert actions.
   * @param ir_transaction_id the transaction id being processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if insert relationship between atoms failed.
   */
  private int insertRelationshipBetweenAtoms(MEMEConnection mid,
                                             int ir_atom_id_1, int ir_atom_id_2,
                                             String ir_rel_name,
                                             String ir_rel_attr,
                                             String ir_source,
                                             String ir_source_of_label,
                                             String ir_authority,
                                             int ir_transaction_id) throws
      MEMEException {

    // Parameter validation
    if (ir_atom_id_1 < 0 || ir_atom_id_2 < 0 || ir_rel_name == null ||
        ir_source == null || ir_source_of_label == null ||
        ir_authority == null) {
      throw new BadValueException("Bad request parameter found.");
    }

    return insertRelationship(mid, null, null, ir_atom_id_1, ir_atom_id_2, 0, 0,
                              ir_rel_name,
                              ir_rel_attr, ir_source, ir_source_of_label,
                              ir_authority, ir_transaction_id);

  }

  /**
   * Format a call to the full <code>insertRelationship</code> that has
   * <code>concept_id_1 != null</code> and <code>concept_id_2 != null</code>.
   *
   * @param mid the {@link MEMEConnection}
   * @param ir_concept_id_1 is the possible concept id that this relationship connects
   * @param ir_concept_id_2 is the possible concept id that this relationship connects
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param ir_source is the source asserting the relationship
   * @param ir_source_of_label is the source asserting the nature of the relationship
   * @param ir_authority is the one responsible for the Update/Insert actions
   * @param ir_transaction_id the transaction id being processed
   * @return <code>relationship_id</code>
   * @throws MEMEException if insert relationship between concepts failed
   */
  private int insertRelationshipBetweenConcepts(MEMEConnection mid,
                                                int ir_concept_id_1,
                                                int ir_concept_id_2,
                                                String ir_rel_name,
                                                String ir_rel_attr,
                                                String ir_source,
                                                String ir_source_of_label,
                                                String ir_authority,
                                                int ir_transaction_id) throws
      MEMEException {

    // Parameter validation
    if (ir_concept_id_1 < 0 || ir_concept_id_2 < 0 || ir_rel_name == null ||
        ir_source == null || ir_source_of_label == null ||
        ir_authority == null) {
      throw new BadValueException("Bad request parameter found.");
    }

    return insertRelationship(mid, null, null, 0, 0, ir_concept_id_1,
                              ir_concept_id_2, ir_rel_name,
                              ir_rel_attr, ir_source, ir_source_of_label,
                              ir_authority, ir_transaction_id);

  }

  /**
   * Inserts a new source level relationship.
   *
   * <p>This procedure must "fake" a <code>MOLECULAR_INSERT</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * The following defaults are to be assumed for the unspecified
   * <code>relationships</code> fields:<br>
   *    <ul><code>relationship_level</code> = "S"</ul>
   *    <ul><code>status</code> = "R"</ul>
   *    <ul><code>atom_id_1</code> = <code>atom_id_2</code> = 0</ul>
   *    <ul><code>generated</code> = "N"</ul>
   *    <ul><code>dead</code> = "N"</ul>
   *    <ul><code>released</code> = "N"</ul>
   *    <ul><code>tobereleased</code> = "Y"</ul>
   *    <ul><code>suppressible</code> = "N"</ul><p>
   *
   * @param mid the {@link MEMEConnection}
   * @param ir_cui_1 is the possible CUI that this relationship connects
   * @param ir_cui_2 is the possible CUI that this relationship connects
       * @param ir_atom_id_1 is the possible atom id that this relationship connects
       * @param ir_atom_id_2 is the possible atom id that this relationship connects
   * @param ir_concept_id_1 is the possible concept id that this relationship connects
   * @param ir_concept_id_2 is the possible concept id that this relationship connects
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>
   * @param ir_source is the source asserting the relationship
   * @param ir_source_of_label is the source asserting the nature of the relationship
   * @param ir_authority is the one responsible for the Update/Insert actions
   * @return <code>relationship_id</code>
   * @throws MEMEException if program validation failed
   * @param ir_transaction_id the transaction id being processed
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  private int insertRelationship(MEMEConnection mid,
                                 String ir_cui_1, String ir_cui_2,
                                 int ir_atom_id_1, int ir_atom_id_2,
                                 int ir_concept_id_1, int ir_concept_id_2,
                                 String ir_rel_name, String ir_rel_attr,
                                 String ir_source,
                                 String ir_source_of_label, String ir_authority,
                                 int ir_transaction_id) throws MEMEException {

    int l_molecule_id;

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    java.util.Date timestamp = new java.util.Date();
    java.sql.Timestamp l_timestamp = new java.sql.Timestamp(timestamp.getTime());

    CallableStatement cstmt = null;
    Statement stmt = null;
    ResultSet rs = null;

    try {
      // Commit any uncommited transaction and turn auto commit off
      mid.commit();
      mid.setAutoCommit(false);
    } catch (SQLException se) {
      throw new DataSourceException("Failed to set commit mode.", mid, se);
    }

    try {
      // Get molecule_id
      MEMEToolkit.trace("\tAssigning next molecule_id... ");
      cstmt = mid.prepareCall(
          "{ ? = call MEME_UTILITY.get_next_id('MOLECULAR_ACTIONS') }");
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.execute();
      l_molecule_id = cstmt.getInt(1);
      MEMEToolkit.trace("\tdone. molecule_id=(" + l_molecule_id + ").");
      cstmt.close();
    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to assign next id.", mid, se);
      dse.setDetail("type", "molecule");
      throw dse;
    }

    int l_atom_id_1 = 0;
    int l_atom_id_2 = 0;
    int l_concept_id_1 = 0;
    int l_concept_id_2 = 0;
    String l_level = "S"; // source asserted

    // Map atom ids
    if (ir_cui_1 != null && ir_cui_2 != null) {
      // Get atom ids from valid cuis
      MEMEToolkit.trace("\tBadValueException atom_id_1... ");
      l_atom_id_1 = getAtomFromCUI(mid, ir_cui_1);
      MEMEToolkit.trace("\tdone. atom_id_1=(" + l_atom_id_1 + ").");
      MEMEToolkit.trace("\tMapping atom_id_2... ");
      l_atom_id_2 = getAtomFromCUI(mid, ir_cui_2);
      MEMEToolkit.trace("\tdone. atom_id_2=(" + l_atom_id_2 + ").");
    } else if (ir_atom_id_1 > 0 && ir_atom_id_2 > 0) {
      l_atom_id_1 = ir_atom_id_1;
      l_atom_id_2 = ir_atom_id_2;
    }

    // Map concept ids
    if (ir_concept_id_1 > 0 && ir_concept_id_2 > 0) {
      // l_atom_id_1 and l_atom_id_2 must be 0 at this point
      l_concept_id_1 = ir_concept_id_1;
      l_concept_id_2 = ir_concept_id_2;
      l_level = "C"; // mth asserted
    } else {
      // Get concept ids from valid atoms
      MEMEToolkit.trace("\tMapping concept_id_1... ");
      l_concept_id_1 = getConceptFromAtom(mid, l_atom_id_1);
      MEMEToolkit.trace("\tdone. concept_id_1=(" + l_concept_id_1 + ").");

      MEMEToolkit.trace("\tMapping concept_id_2... ");
      l_concept_id_2 = getConceptFromAtom(mid, l_atom_id_2);
      MEMEToolkit.trace("\tdone. concept_id_2=(" + l_concept_id_2 + ").");
    }

    // Fix source and source_of_label value
    // this makes sure that the source/source_of_label exist in source_rank
    // and inserts them if they do not
    fixSource(mid, ir_source);
    fixSource(mid, ir_source_of_label);

    // Insert into molecular_action
    String l_molecular_action = "MOLECULAR_INSERT";
    MEMEToolkit.trace("\tInserting new molecular_actions row... ");

    try {
      stmt = mid.createStatement();
      StringBuffer sqlInsertMA = new StringBuffer(500);
      sqlInsertMA = new StringBuffer(500);

      sqlInsertMA.append(
          "INSERT INTO molecular_actions (transaction_id,molecule_id, ");
      sqlInsertMA.append(
          "authority, timestamp, molecular_action, source_id, target_id, ");
      sqlInsertMA.append(
          "undone, undone_by, undone_when, status, elapsed_time, work_id) ");
      sqlInsertMA.append("VALUES ( ");
      sqlInsertMA.append(ir_transaction_id);
      sqlInsertMA.append(",");
      sqlInsertMA.append(l_molecule_id);
      sqlInsertMA.append(",'");
      sqlInsertMA.append(ir_authority);
      sqlInsertMA.append("','");
      sqlInsertMA.append(formatter.format(timestamp));
      sqlInsertMA.append("','");
      sqlInsertMA.append(l_molecular_action);
      sqlInsertMA.append("',");
      sqlInsertMA.append(l_concept_id_1);
      sqlInsertMA.append(",0,'N','','','R',1,0)");

      stmt.executeUpdate(sqlInsertMA.toString());
      MEMEToolkit.trace("\tdone. molecule_id=(" + l_molecule_id + ").");
      stmt.close();

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      throw new DataSourceException("Failed to insert into molecular action.",
                                    mid, se);
    }

    if (l_level.equals("C")) {
      // Relationship is concept level
      int l_rel_id = 0;
      try {
        // Check and see if there is already a matching C level relationship
        MEMEToolkit.trace("\tSearching for a matching C level...");
        stmt = mid.createStatement();
        StringBuffer sqlSelect = new StringBuffer(500);

        sqlSelect.append("SELECT relationship_id FROM relationships ");
        sqlSelect.append("WHERE concept_id_1 = ");
        sqlSelect.append(l_concept_id_1);
        sqlSelect.append(" AND concept_id_2 = ");
        sqlSelect.append(l_concept_id_2);
        sqlSelect.append(" AND relationship_level = 'C'");
        sqlSelect.append(" UNION ");
        sqlSelect.append("SELECT relationship_id FROM relationships ");
        sqlSelect.append("WHERE concept_id_1 = ");
        sqlSelect.append(l_concept_id_2);
        sqlSelect.append(" AND concept_id_2 = ");
        sqlSelect.append(l_concept_id_1);
        sqlSelect.append(" AND relationship_level = 'C'");
        rs = stmt.executeQuery(sqlSelect.toString());

        while (rs.next()) {
          l_rel_id = rs.getInt("RELATIONSHIP_ID");
          // Delete if there is already a matching C level relationship
          // for this concept level relationship
          MEMEToolkit.trace("\tdone. relationship_id=(" + l_rel_id + ")");
          MEMEToolkit.trace(
              "\tDeleting a match C level relationship for this concept level relationship.");
          cstmt = mid.prepareCall(
              "{ ? = call MEME_APROCS.aproc_change_dead("
              + "table_name => 'R',"
              + "row_id => ?,"
              + "old_dead => 'N',"
              + "new_dead => 'Y',"
              + "action_status => 'R',"
              + "molecule_id => ?,"
              + "authority => ?,"
              + "timestamp => ?) }");
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setInt(2, l_rel_id);
          cstmt.setInt(3, l_molecule_id);
          cstmt.setString(4, ir_authority);
          cstmt.setTimestamp(5, l_timestamp);
          cstmt.execute();
          MEMEToolkit.trace("\tdone.");
          if (cstmt.getInt(1) < 0) {
            try {
              mid.rollback();
            } catch (Exception e) {}
            throw new DataSourceException(
                "Deleting matching C level relationship failed "
                + "(see meme_error).");
          }
          cstmt.close();
        }
        stmt.close();

      } catch (SQLException se) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to process matching C level relationship.", mid, se);
        dse.setDetail("relationship_id", String.valueOf(l_rel_id));
        throw dse;
      }
    }

    int l_rel_id = 0;
    try {
      // Insert source-level relationships row
      MEMEToolkit.trace("\tInserting new source-level relationship row... ");
      cstmt = mid.prepareCall(
          "{ ? = call MEME_APROCS.aproc_insert_rel("
          + "source_rel_id => 0,"
          + "origin => 'Local',"
          + "molecule_id => ?,"
          + "authority => ?,"
          + "timestamp => ?,"
          + "l_concept_id_1 => ?,"
          + "l_concept_id_2 => ?,"
          + "l_atom_id_1 => ?,"
          + "l_relationship_name => ?,"
          + "l_relationship_attribute => ?,"
          + "l_atom_id_2 => ?,"
          + "l_source_of_label => ?,"
          + "l_source => ?,"
          + "l_status => 'R',"
          + "l_generated => 'N',"
          + "l_level => ?,"
          + "l_released => 'N',"
          + "l_tobereleased => 'n',"
          + "l_suppressible => 'N') }");
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.setInt(2, l_molecule_id);
      cstmt.setString(3, ir_authority);
      cstmt.setTimestamp(4, l_timestamp);
      cstmt.setInt(5, l_concept_id_1);
      cstmt.setInt(6, l_concept_id_2);
      cstmt.setInt(7, l_atom_id_1);
      cstmt.setString(8, ir_rel_name);
      cstmt.setString(9, ir_rel_attr);
      cstmt.setInt(10, l_atom_id_2);
      cstmt.setString(11, ir_source_of_label);
      cstmt.setString(12, ir_source);
      cstmt.setString(13, l_level);
      MEMEToolkit.trace("\tdone.");
      cstmt.execute();

      l_rel_id = cstmt.getInt(1);
      cstmt.close();
      if (l_rel_id < 1) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        throw new DataSourceException("Inserting relationships row failed "
                                      + "(see meme_error).");
      }

      // Insert into sg_relationships
      MEMEToolkit.trace("\tSetting sg_* fields ... ");

      // update srelationships for CUI
      if (ir_cui_1 != null && ir_cui_2 != null) {
        stmt = mid.createStatement();
        StringBuffer update_query = new StringBuffer(500);
        update_query = new StringBuffer(500);
        update_query.append("UPDATE relationships ");
        update_query.append("SET sg_id_1 = '").append(ir_cui_1).append("', ");
        update_query.append(" sg_type_1='CUI', sg_qualifier_1 = '', ");
        update_query.append(" sg_id_2 = '").append(ir_cui_2).append("', ");
        update_query.append(" sg_type_2='CUI', sg_qualifier_2 = '' ");
        update_query.append("WHERE relationship_id = ").append(l_rel_id);

        stmt.executeUpdate(update_query.toString());
        stmt.close();
      }

      MEMEToolkit.trace("\tdone.");

      // Commit work
      mid.commit();

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      throw new DataSourceException("Molecular action failed.", mid, se);
    }

    return l_rel_id;
  }

  /**
   * Changes the relationship's tobereleased value.
   *
   * @param mid the {@link MEMEConnection}.
   * @param crtbr_rel_id is the ID of the relationships to be processed.
   * @param crtbr_authority is the one responsible for the change actions.
   * @param crtbr_tbr is the new value of tobereleased.
   * @param transaction_id the transaction id being processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getTobereleased()
   */
  private int changeRelationshipTBR(MEMEConnection mid,
                                    int crtbr_rel_id,
                                    String crtbr_authority,
                                    String crtbr_tbr,
                                    int transaction_id) throws MEMEException {

    // Parameter validation
    if (crtbr_rel_id < 0 || crtbr_authority == null || crtbr_tbr == null) {
      throw new BadValueException("Bad request parameter found.");
    }

    int l_molecule_id;
    int l_rel_count = 0;
    int l_concept_id = 0;
    String l_tobereleased = "Y";
    String l_molecular_action = "MOLECULAR_CHANGE_TOBERELEASED";

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    java.util.Date timestamp = new java.util.Date();
    java.sql.Timestamp l_timestamp = new java.sql.Timestamp(timestamp.getTime());

    CallableStatement cstmt = null;
    Statement stmt = null;
    ResultSet rs = null;

    try {
      // Commit any uncommited transaction and turn auto commit off
      mid.commit();
      mid.setAutoCommit(false);
    } catch (SQLException se) {
      throw new DataSourceException("Failed to set commit mode.", mid, se);
    }

    try {
      // Get molecule_id
      MEMEToolkit.trace("\tAssigning next molecule_id... ");
      cstmt = mid.prepareCall(
          "{ ? = call MEME_UTILITY.get_next_id('MOLECULAR_ACTIONS') }");
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.execute();
      l_molecule_id = cstmt.getInt(1);
      MEMEToolkit.trace("\tdone. molecule_id=(" + l_molecule_id + ").");
      cstmt.close();
    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to assign next id.", mid, se);
      dse.setDetail("type", "molecule");
      throw dse;
    }

    try {
      // Get source-level relationship row
      MEMEToolkit.trace(
          "\tSearching relationships rows which relationship_id = "
          + crtbr_rel_id + ".");
      stmt = mid.createStatement();
      StringBuffer sqlSelect = new StringBuffer(500);

      sqlSelect.append("SELECT concept_id_1, tobereleased "
                       + "FROM relationships WHERE relationship_id = ");
      sqlSelect.append(crtbr_rel_id);
      rs = stmt.executeQuery(sqlSelect.toString());

      while (rs.next()) {
        l_rel_count++;
        l_concept_id = rs.getInt("CONCEPT_ID_1");
        l_tobereleased = rs.getString("TOBERELEASED");
      }

      stmt.close();

      // Verify single source-level relationship
      if (l_rel_count > 1) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Too much data.");
        dse.setDetail("relationship_id", String.valueOf(crtbr_rel_id));
        dse.setDetail("relationship_count", String.valueOf(l_rel_count));
        throw dse;
      }

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get relationship.", mid, se);
      dse.setDetail("relationship_id", String.valueOf(crtbr_rel_id));
      throw dse;
    }

    try {
      // Insert into molecular_action
      MEMEToolkit.trace("\tInserting new molecular_actions row... ");

      stmt = mid.createStatement();
      StringBuffer sqlInsertMA = new StringBuffer(500);

      sqlInsertMA.append(
          "INSERT INTO molecular_actions (transaction_id,molecule_id, ");
      sqlInsertMA.append(
          "authority, timestamp, molecular_action, source_id, target_id, ");
      sqlInsertMA.append(
          "undone, undone_by, undone_when, status, elapsed_time, work_id) ");
      sqlInsertMA.append("VALUES ( ");
      sqlInsertMA.append(transaction_id);
      sqlInsertMA.append(",");
      sqlInsertMA.append(l_molecule_id);
      sqlInsertMA.append(",'");
      sqlInsertMA.append(crtbr_authority);
      sqlInsertMA.append("','");
      sqlInsertMA.append(formatter.format(timestamp));
      sqlInsertMA.append("','");
      sqlInsertMA.append(l_molecular_action);
      sqlInsertMA.append("',");
      sqlInsertMA.append(l_concept_id);
      sqlInsertMA.append(",0,'N','','','R',1,0)");

      stmt.executeUpdate(sqlInsertMA.toString());
      stmt.close();
      MEMEToolkit.trace("\tdone. molecule_id=(" + l_molecule_id + ").");

      MEMEToolkit.trace("\tUpdating relationship's tobereleased field... ");
      cstmt = mid.prepareCall(
          "{ ? = call MEME_APROCS.aproc_change_tbr("
          + "table_name => 'R',"
          + "row_id => ?,"
          + "old_tobereleased => ?,"
          + "new_tobereleased => ?,"
          + "action_status => 'R',"
          + "molecule_id => ?,"
          + "authority => ?,"
          + "timestamp => ?) }");
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.setInt(2, crtbr_rel_id);
      cstmt.setString(3, l_tobereleased);
      cstmt.setString(4, crtbr_tbr);
      cstmt.setInt(5, l_molecule_id);
      cstmt.setString(6, crtbr_authority);
      cstmt.setTimestamp(7, l_timestamp);
      cstmt.execute();
      MEMEToolkit.trace("\tdone.");
      if (cstmt.getInt(1) < 0) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        throw new DataSourceException(
            "Updating relationship's tobereleased failed "
            + "(see meme_error).");
      }
      cstmt.close();

      // Commit work
      mid.commit();

      return crtbr_rel_id;

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      throw new DataSourceException("Molecular action failed.", mid, se);
    }

  }

  /**
       * Fixes the valid value of <code>source</code> and <code>source_of_label</code>
   * which does not exist in <code>source_rank</code>.
   * @param mid the {@link MEMEConnection}
   * @param fS_source the source value to fix
   * @throws MEMEException if program failed
   */
  private void fixSource(MEMEConnection mid,
                         String fS_source) throws MEMEException {

    int l_count = 0;
    Statement stmt = null;

    try {
      stmt = mid.createStatement();
      ResultSet rs =
          stmt.executeQuery(
          "SELECT COUNT(*) AS ct FROM source_rank WHERE source = '"
          + fS_source + "'");

      while (rs.next()) {
        l_count = rs.getInt("CT");
      }
      stmt.close();

    } catch (SQLException se) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get source rank data.", mid, se);
      dse.setDetail("source", fS_source);
      throw dse;
    }

    if (l_count == 0 && fS_source.substring(0, 3).equals("NLM")) {
      // Insert into source_rank
      MEMEToolkit.trace("\tInserting new source_rank row... ");

      try {
        stmt = mid.createStatement();
        StringBuffer sqlInsertSR = new StringBuffer(500);

        sqlInsertSR.append("INSERT INTO source_rank (source, rank, ");
        sqlInsertSR.append(
            "restriction_level, normalized_source, stripped_source, notes) ");
        sqlInsertSR.append("SELECT '");
        sqlInsertSR.append(fS_source);
        sqlInsertSR.append("' ,");
        sqlInsertSR.append(
            "rank, restriction_level, normalized_source, stripped_source, notes ");
        sqlInsertSR.append("FROM source_rank WHERE source = 'NLM'");

        stmt.executeUpdate(sqlInsertSR.toString());
        stmt.close();

        MEMEToolkit.trace("\tdone. source=(" + fS_source + ").");

        // Commit work !! don't commit here!!!!!
        // mid.commit();

      } catch (SQLException se) {
        try {
          mid.rollback();
        } catch (Exception e) {}
        throw new DataSourceException("Failed to insert into source rank.", mid,
                                      se);
      }

    } else if (l_count == 0) {
      try {
        mid.rollback();
      } catch (Exception e) {}
      BadValueException bve = new BadValueException("Illegal source value.");
      bve.setDetail("source", fS_source);
      throw bve;
    }
  }

  /**
   * Returns an atom id for the specified cui.
   * @param mid the {@link MEMEConnection}.
   * @param p_cui the cui
   * @return the atom id
   * @throws MEMEException if failed to get atom id
   */
  private int getAtomFromCUI(
      MEMEConnection mid, String p_cui) throws MEMEException {

    int l_atom_id = 0;
    try {
      ResultSet rs = null;
      Statement stmt = mid.createStatement();
      StringBuffer sqlSelect = new StringBuffer(500);
      sqlSelect = new StringBuffer(500);

      sqlSelect.append(
          "SELECT TO_NUMBER(SUBSTR(max_rank, INSTR(max_rank,'/')+1)) ");
      sqlSelect.append("AS atom FROM ");
      sqlSelect.append(
          "(SELECT MAX(rank||'/'||LPAD(atom_id,10,0)) AS max_rank ");
      sqlSelect.append("FROM classes WHERE last_release_cui = '");
      sqlSelect.append(p_cui);
      sqlSelect.append("')");

      rs = stmt.executeQuery(sqlSelect.toString());

      while (rs.next()) {
        l_atom_id = rs.getInt("ATOM");
      }
      stmt.close();

      if (l_atom_id < 1) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("atom_id", String.valueOf(l_atom_id));
        throw dse;
      }

      sqlSelect = new StringBuffer(500);
      sqlSelect.append("SELECT NVL(MIN(new_atom_id), ");
      sqlSelect.append(l_atom_id);
      sqlSelect.append(") AS new_atom FROM mom_safe_replacement, classes");
      sqlSelect.append(" WHERE old_atom_id = ");
      sqlSelect.append(l_atom_id);
      sqlSelect.append(" AND new_atom_id = atom_id");

      stmt = mid.createStatement();
      rs = stmt.executeQuery(sqlSelect.toString());

      while (rs.next()) {
        l_atom_id = rs.getInt("NEW_ATOM");
      }

      stmt.close();

      if (l_atom_id < 1) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("atom_id", String.valueOf(l_atom_id));
        throw dse;
      }

    } catch (SQLException se) {
      throw new DataSourceException("Failed to get atom from CUI.", mid, se);
    }

    return l_atom_id;
  }

  /**
   * Returns a concept id for the specified atom id.
   * @param mid the {@link MEMEConnection}.
   * @param p_atom the atom id
   * @return the concept id.
   * @throws MEMEException if failed to get concept id.
   */
  private int getConceptFromAtom(
      MEMEConnection mid, int p_atom) throws MEMEException {
    int l_concept_id = 0;
    try {
      Statement stmt = mid.createStatement();
      ResultSet rs =
          stmt.executeQuery("SELECT concept_id FROM classes WHERE atom_id = "
                            + p_atom);

      while (rs.next()) {
        l_concept_id = rs.getInt("CONCEPT_ID");
      }
      stmt.close();

      if (l_concept_id < 1) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("atom_id", String.valueOf(p_atom));
        throw dse;
      }
    } catch (SQLException se) {
      throw new DataSourceException("Failed to get concept id.", mid, se);
    }
    return l_concept_id;
  }
}
