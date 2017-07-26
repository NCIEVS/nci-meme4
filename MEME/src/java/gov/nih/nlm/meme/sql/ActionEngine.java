/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  ActionEngine
 * Changes
 *   03/01/2006 BAC (1-AIKFN): logAction should handle exceptional case without
 *      passing the action object or raw exception to the ActionException
 *      that will be thrown.  Instead it can reference info about these things 
 *      as details.
 *   02/09/2006 TTN (1-78NQ9): do not log the AC BatchMolecularTransaction
 *   01/19/2006 TTN (1-739BX): add the enclosed exception
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.AtomicInsertAction;
import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MEMEDataSourceAction;
import gov.nih.nlm.meme.action.MacroMolecularAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptElement;
import gov.nih.nlm.meme.common.ISUI;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.LUI;
import gov.nih.nlm.meme.common.MEMEString;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SUI;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.xml.ObjectXMLSerializer;
import gov.nih.nlm.util.SystemToolkit;
import oracle.sql.CLOB;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Generically represents a means of performing the various types of actions.
 * The {@link Default} inner class provides the reference impelmentation.
 *
 * @author MEME Group
 */
public interface ActionEngine {

  /**
   * Processes the specified {@link MolecularAction}.
   * @param action the {@link MolecularAction} to process
   * @throws ActionException if failed to process molecular action
   * @throws StaleDataException if in-memory data is too old
   */
  public void processAction(MolecularAction action) throws ActionException,
      StaleDataException;

  /**
   * Processes the specified {@link BatchMolecularTransaction}.
   * @param transaction the {@link BatchMolecularTransaction} to perform
   * @throws ActionException if failed to process batch molecular transaction
   */
  public void processAction(BatchMolecularTransaction transaction) throws
      ActionException;

  /**
   * Processes the specified {@link MacroMolecularAction}.
   * @param action the {@link MacroMolecularAction} to perform
   * @throws ActionException if failed to process macro molecular action
   */
  public void processAction(MacroMolecularAction action) throws ActionException;

  /**
   * Processes the specified {@link MEMEDataSourceAction}.
   * @param action the {@link MEMEDataSourceAction} to perform
   * @throws ActionException if failed to process MID data source action
   * @throws DataSourceException if failed to process action
   */
  public void processAction(MEMEDataSourceAction action) throws ActionException,
      DataSourceException;

  /**
   * Logs the specified action.
   * @param la the {@link LoggedAction} to log
   * @throws ActionException if failed to logged action
   */
  public void logAction(LoggedAction la) throws ActionException;

  //
  // Inner Classes
  //

  /**
   * Reference {@link ActionEngine} imlementation.
   */
  public class Default implements ActionEngine {

    //
    // Fields
    //

    //
    // Data source to perform actions in
    //
    protected MEMEDataSource data_source = null;

    //
    // Standard date format
    //
    final protected DateFormat dateformat = MEMEToolkit.getDateFormat();

    //
    // Constructors
    //

    /**
     * Instantiates an {@link ActionEngine.Default}.
     */
    public Default() {}

    /**
     * Instantiates a {@link ActionEngine.Default} connected
     * to the specified {@link MEMEDataSource}
         * @param data_source the {@link MEMEDataSource} in which to perform actions
     */
    public Default(MEMEDataSource data_source) {
      this.data_source = data_source;
    }

    //
    // Private methods
    //

    /**
     * Performs the specified {@link AtomicInsertAction}.
     * @param action the {@link AtomicInsertAction} to perform
     * @throws ActionException if failed to perform atomic insert
     */
    private void atomicInsert(AtomicInsertAction action) throws ActionException {
      //
      // Handle individual core data types
      //
      if (action.getAffectedTable().equals("C")) {
        atomicInsertForClass(action);
      } else if (action.getAffectedTable().equals("R")) {
        atomicInsertForRelationship(action);
      } else if (action.getAffectedTable().equals("A")) {
        atomicInsertForAttribute(action);
      } else if (action.getAffectedTable().equals("CS")) {
        atomicInsertForConcept(action);
      }
    }

    /**
     * Performs the specified {@link AtomicInsertAction}.
     * @param action the {@link AtomicInsertAction} to perform
     * @throws ActionException if failed to perform atomic insert for class
     */
    private void atomicInsertForClass(AtomicInsertAction action) throws
        ActionException {

      //
      // Extract atom to inser
      //
      final Atom atom = (Atom) action.getElementToInsert();

      //
      // Look for matching SUI
      //
      final SearchParameter lat_sp =
          new SearchParameter.Single("language",
                                     atom.getLanguage() == null ? "ENG" :
                                     atom.getLanguage().getAbbreviation());
      final SearchParameter str_sp =
          new SearchParameter.Single("string", atom.toString());
      Iterator iterator = null;
      try {
        iterator = data_source.findStrings(new SearchParameter[] {str_sp,
                                           lat_sp});
      } catch (DataSourceException dse) {
        throw new ActionException(
            "Failed to find matching string.", action, dse);
      }
      boolean found = false;
      while (iterator.hasNext()) {
        final MEMEString string = (MEMEString) iterator.next();
        atom.setSUI(string.getSUI());
        atom.setISUI(string.getISUI());
        atom.setLUI(string.getLUI());
        atom.setNormalizedString(string.getNormalizedString());
        found = true;
      }
      if (!found) {
        atom.setSUI(null);
        atom.setISUI(null);
        atom.setLUI(null);
      }

      //
      // Look for matching ISUI, if needed
      //
      if (atom.getISUI() == null) {
        final SearchParameter lstr_sp = new SearchParameter.Single(
            "lowercase_string", atom.toString().toLowerCase());
        try {
          iterator = data_source.findStrings(new SearchParameter[] {lstr_sp,
                                             lat_sp});
        } catch (DataSourceException dse) {
          throw new ActionException(
              "Failed to find matching string.", action, dse);
        }
        found = false;
        while (iterator.hasNext()) {
          final MEMEString string = (MEMEString) iterator.next();
          atom.setISUI(string.getISUI());
          atom.setLUI(string.getLUI());
          atom.setNormalizedString(string.getNormalizedString());
          iterator.remove();
          found = true;
          break;
        }
        if (!found) {
          atom.setSUI(null);
          atom.setISUI(null);
          atom.setLUI(null);
        }
      }

      //
      // Look for matching LUI if needed
      //
      if (atom.getLUI() == null) {
        String norm_string = null;
        try {
          norm_string =
              MIDServices.getLuiNormalizedString(atom.getBaseString());
        } catch (MidsvcsException me) {
          ActionException ae = new ActionException(
              "Failed to get lui normalized string.", action, me);
          ae.setDetail("name", atom.toString());
          throw ae;
        }
        if (norm_string == null) {
          norm_string = "";
        }
        atom.setNormalizedString(norm_string);
        final SearchParameter nstr_sp = new SearchParameter.Single(
            "norm_string", norm_string);
        try {
          iterator = data_source.findStrings(new SearchParameter[] {nstr_sp,
                                             lat_sp});
        } catch (DataSourceException dse) {
          throw new ActionException(
              "Failed to find matching string.", action, dse);
        }
        found = false;
        while (iterator.hasNext()) {
          final MEMEString string = (MEMEString) iterator.next();
          atom.setLUI(string.getLUI());
          iterator.remove();
          found = true;
          break;
        }
        if (!found) {
          atom.setSUI(null);
          atom.setISUI(null);
          atom.setLUI(null);
        }
      }

      //
      // If needed, create new SUI, ISUI, LUI
      //
      if (atom.getSUI() == null) {
        int max_id = 0;
        try {

          max_id = data_source.getNextIdentifierForType(SUI.class).intValue();
          String prefix = data_source.getValueByCode("ui_prefix", "SUI");
          int length = Integer.parseInt(data_source.getValueByCode("ui_length",
              "SUI"));
          atom.setSUI(new SUI(
              prefix +
              "0000000000".substring(0, length - String.valueOf(max_id).length()) +
              max_id));
          if (atom.getISUI() == null) {
            max_id = data_source.getNextIdentifierForType(ISUI.class).intValue();
            prefix = data_source.getValueByCode("ui_prefix", "ISUI");
            length = Integer.parseInt(data_source.getValueByCode("ui_length",
                "ISUI"));
            atom.setISUI(new ISUI(
                prefix +
                "0000000000".substring(0,
                                       length - String.valueOf(max_id).length()) +
                max_id));
            if (atom.getLUI() == null) {
              max_id = data_source.getNextIdentifierForType(LUI.class).intValue();
              prefix = data_source.getValueByCode("ui_prefix", "LUI");
              length = Integer.parseInt(data_source.getValueByCode("ui_length",
                  "LUI"));
              atom.setLUI(new LUI(
                  prefix +
                  "0000000000".substring(0,
                                         length - String.valueOf(max_id).length()) +
                  max_id));
            }
          }
        } catch (DataSourceException dse) {
          throw new ActionException("Failed to get next id.", action, dse);
        } catch (BadValueException bve) {
          throw new ActionException("Failed to get next id.", action, bve);
        }

        //
        // String_ui insert
        //
        try {
          addString(atom);
        } catch (DataSourceException dse) {
          throw new ActionException("Failed to add string.", action, dse);
        }
      }

      //
      // Obtain word index data
      //
      try {
        atom.setNormalizedIndexWords(
            MIDServices.getLuiNormalizedWords(atom.getBaseString()));
        atom.setIndexWords(
            MIDServices.getWords(atom.getBaseString()));
      } catch (MidsvcsException me) {
        ActionException ae = new ActionException(
            "Failed to get index data.", action, me);
        ae.setDetail("name", atom.toString());
        throw ae;
      }

      //
      // Insert into word_index, normwrd, normstr
      //
      // There is a small dependency problem where we
      // do not yet have the atom_id.  so we look it up.
      //
      try {
        final Identifier id = data_source.getMaxIdentifierForType(Atom.class);
        atom.setIdentifier(new Identifier.Default(id.intValue() + 1));
        addIndexesForAtom(atom);
      } catch (DataSourceException dse) {
        throw new ActionException("Failed to add indexes.", action, dse);
      }

      //
      // Make aproc_insert_classes call
      //
      final String insert_classes =
          "{? = call MEME_APROCS.aproc_insert_classes("
          + "atomic_action_id => ?, "
          + "atom_id => ?, "
          + "source_atom_id => ?, "
          + "origin => 'Local', "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?, "
          + "l_atom_name => ?, "
          + "l_source => ?, "
          + "l_termgroup => ?, "
          + "l_code => ?, "
          + "l_aui => ?, "
          + "l_sui => ?, "
          + "l_isui => ?, "
          + "l_lui => ?, "
          + "l_generated => ?, "
          + "l_last_release_cui => ?, "
          + "l_last_assigned_cui => ?, "
          + "l_status => ?, "
          + "l_concept_id => ?, "
          + "l_tobereleased => ?, "
          + "l_released => ?, "
          + "l_last_release_rank => 0, "
          + "l_suppressible => ?, "
          + "l_source_aui => ?, "
          + "l_source_cui => ?, "
          + "l_source_dui => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(insert_classes);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getIdentifier() == null ? 0 :
                     action.getIdentifier().intValue());
        cstmt.setInt(3, atom.getIdentifier() == null ? 0 :
                     atom.getIdentifier().intValue());
        cstmt.setInt(4, atom.getSrcIdentifier() == null ? 0 :
                     atom.getSrcIdentifier().intValue());
        cstmt.setInt(5, (action.getParent() != null &&
                         action.getParent().getIdentifier() == null) ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(6, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(7, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.setString(8, atom.getString() == null ? "" :
                        atom.getString());
        cstmt.setString(9, (atom.getSource() == null ||
                            atom.getSource().getSourceAbbreviation() == null) ?
                        "" :
                        atom.getSource().getSourceAbbreviation());
        cstmt.setString(10, atom.getTermgroup() == null ? "" :
                        atom.getTermgroup().toString());
        cstmt.setString(11, atom.getCode() == null ? "" :
                        atom.getCode().toString());
        cstmt.setString(12, atom.getAUI() == null ? "" :
                        atom.getAUI().toString());
        cstmt.setString(13, atom.getSUI() == null ? "" :
                        atom.getSUI().toString());
        cstmt.setString(14, atom.getISUI() == null ? "" :
                        atom.getISUI().toString());
        cstmt.setString(15, atom.getLUI() == null ? "" :
                        atom.getLUI().toString());
        cstmt.setString(16, atom.isGenerated() ? "Y" : "N");
        cstmt.setString(17, atom.getLastReleaseCUI() == null ? "" :
                        atom.getLastReleaseCUI().toString());
        cstmt.setString(18, atom.getLastAssignedCUI() == null ? "" :
                        atom.getLastAssignedCUI().toString());
        cstmt.setString(19, String.valueOf(atom.getStatus()));
        cstmt.setInt(20, atom.getConcept() == null ? 0 :
                     atom.getConcept().getIdentifier().intValue());
        cstmt.setString(21, String.valueOf(atom.getTobereleased()));
        cstmt.setString(22, String.valueOf(atom.getReleased()));
        cstmt.setString(23, atom.getSuppressible());
        cstmt.setString(24, atom.getSourceIdentifier() == null ? "" :
                        atom.getSourceIdentifier().toString());
        cstmt.setString(25, atom.getSourceConceptIdentifier() == null ? "" :
                        atom.getSourceConceptIdentifier().toString());
        cstmt.setString(26, atom.getSourceDescriptorIdentifier() == null ? "" :
                        atom.getSourceDescriptorIdentifier().toString());
        cstmt.execute();

        int atom_id = cstmt.getInt(1);

        if (atom_id < 1) {
          throw new SQLException();
        }

        atom.setIdentifier(new Identifier.Default(atom_id));
        action.setRowIdentifier(atom_id);
        cstmt.close();
        MEMEToolkit.trace(
            "ActionEngine.atomicInsertForClass(AtomicInsertAction)... completed.");

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}

        ActionException ae = new ActionException(
            "Failed to perform atomic insert action.", action, se);
        ae.setDetail("l_atom_name", atom.getString() == null ? "" :
                     atom.getString());
        ae.setDetail("l_source", atom.getSource() == null ? "" :
                     atom.getSource().toString());
        ae.setDetail("l_termgroup", atom.getTermgroup() == null ? "" :
                     atom.getTermgroup().toString());
        ae.setDetail("l_code", atom.getCode() == null ? "" :
                     atom.getCode().toString());
        ae.setDetail("l_aui", atom.getAUI() == null ? "" :
                     atom.getAUI().toString());
        ae.setDetail("l_sui", atom.getSUI() == null ? "" :
                     atom.getSUI().toString());
        ae.setDetail("l_isui", atom.getISUI() == null ? "" :
                     atom.getISUI().toString());
        ae.setDetail("l_lui", atom.getLUI() == null ? "" :
                     atom.getLUI().toString());
        ae.setDetail("l_generated",
                     atom.isGenerated() ? "Y" : "N");
        ae.setDetail("l_last_release_cui",
                     atom.getLastReleaseCUI() == null ? "" :
                     atom.getLastReleaseCUI().toString());
        ae.setDetail("l_last_assigned_cui",
                     atom.getLastAssignedCUI() == null ? "" :
                     atom.getLastAssignedCUI().toString());
        ae.setDetail("l_status", new Character(atom.getStatus()));
        ae.setDetail("l_concept_id",
                     atom.getConcept().getIdentifier() == null ? new Integer(0) :
                     new Integer(atom.getConcept().getIdentifier().intValue()));
        ae.setDetail("l_tobereleased", new Character(atom.getTobereleased()));
        ae.setDetail("l_released", new Character(atom.getReleased()));
        ae.setDetail("l_last_release_rank",
                     atom.getRank() == null ? new Integer(0) :
                     new Integer(atom.getRank().intValue()));
        ae.setDetail("l_suppressible", atom.getSuppressible());
        ae.setDetail("l_source_aui", atom.getAUI() == null ? "" :
                     atom.getAUI().toString());
        ae.setDetail("l_source_cui",
                     atom.getSourceConceptIdentifier() == null ? "" :
                     atom.getSourceConceptIdentifier().toString());
        ae.setDetail("l_source_dui",
                     atom.getSourceDescriptorIdentifier() == null ? "" :
                     atom.getSourceDescriptorIdentifier().toString());
        throw ae;

      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs the specified {@link AtomicInsertAction}.
     * @param action the {@link AtomicInsertAction} to perform
     * @throws ActionException if failed to perform atomic insert for
     * relationship
     */
    private void atomicInsertForRelationship(AtomicInsertAction action) throws
        ActionException {
      //
      // extract relationship to insert
      //
      final Relationship rel = (Relationship) action.getElementToInsert();

      //
      // Call aproc_insert_rel
      //
      final String insert_rel =
          "{? = call MEME_APROCS.aproc_insert_rel("
          + "atomic_action_id => ?, "
          + "relationship_id => ?, "
          + "source_rel_id => ?, "
          + "origin => 'Local', "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?, "
          + "l_concept_id_1 => ?, "
          + "l_concept_id_2 => ?, "
          + "l_atom_id_1 => ?, "
          + "l_relationship_name => ?, "
          + "l_relationship_attribute => ?, "
          + "l_atom_id_2 => ?, "
          + "l_source_of_label => ?, "
          + "l_source => ?, "
          + "l_status => ?, "
          + "l_generated => ?, "
          + "l_level => ?, "
          + "l_released => ?, "
          + "l_tobereleased => ?, "
          + "l_suppressible => ?, "
          + "l_sg_id_1 => ?, "
          + "l_sg_type_1 => ?, "
          + "l_sg_qualifier_1 => ?, "
          + "l_sg_id_2 => ?, "
          + "l_sg_type_2 => ?, "
          + "l_sg_qualifier_2 => ?, "
          + "l_source_rui => ?, "
          + "l_relationship_group => ?)}";

      //
      // Determine Native identifier values
      //
      String l_sg_id_1 = "";
      String l_sg_type_1 = "";
      String l_sg_qualifier_1 = "";
      String l_sg_id_2 = "";
      String l_sg_type_2 = "";
      String l_sg_qualifier_2 = "";
      if (rel.getNativeIdentifier() != null) {
        l_sg_id_1 = rel.getNativeIdentifier().toString();
        l_sg_type_1 = rel.getNativeIdentifier().getType();
        l_sg_qualifier_1 = rel.getNativeIdentifier().getQualifier() == null ?
            null : rel.getNativeIdentifier().getQualifier().toString();
      }
      if (rel.getRelatedNativeIdentifier() != null) {
        l_sg_id_2 = rel.getRelatedNativeIdentifier().toString();
        l_sg_type_2 = rel.getRelatedNativeIdentifier().getType();
        l_sg_qualifier_2 = rel.getRelatedNativeIdentifier().getQualifier() == null ?
            null : rel.getRelatedNativeIdentifier().getQualifier().toString();
      }

      //
      // Call aproc
      //
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(insert_rel);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getIdentifier() == null ? 0 :
                     action.getIdentifier().intValue());
        cstmt.setInt(3, rel.getIdentifier() == null ? 0 :
                     rel.getIdentifier().intValue());
        cstmt.setInt(4, rel.getSrcIdentifier() == null ? 0 :
                     rel.getSrcIdentifier().intValue());
        cstmt.setInt(5, (action.getParent() != null &&
                         action.getParent().getIdentifier() == null) ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(6, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(7, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.setInt(8, (rel.getConcept() != null &&
                         rel.getConcept().getIdentifier() == null) ? 0 :
                     rel.getConcept().getIdentifier().intValue());
        cstmt.setInt(9, (rel.getRelatedConcept() != null &&
                         rel.getRelatedConcept().getIdentifier() == null) ? 0 :
                     rel.getRelatedConcept().getIdentifier().intValue());
        cstmt.setInt(10, (rel.getAtom() == null ||
                          rel.getAtom().getIdentifier() == null) ? 0 :
                     rel.getAtom().getIdentifier().intValue());
        cstmt.setString(11, rel.getName() == null ? "" : rel.getName());
        cstmt.setString(12, rel.getAttribute() == null ? "" :
                        rel.getAttribute());
        cstmt.setInt(13, (rel.getRelatedAtom() == null ||
                          rel.getRelatedAtom().getIdentifier() == null) ? 0 :
                     rel.getRelatedAtom().getIdentifier().intValue());
        cstmt.setString(14,
                        (rel.getSourceOfLabel() == null ||
                         rel.getSourceOfLabel().getSourceAbbreviation() == null) ?
                        "" :
                        rel.getSourceOfLabel().getSourceAbbreviation());
        cstmt.setString(15,
                        (rel.getSource() == null ||
                         rel.getSource().getSourceAbbreviation() == null) ? "" :
                        rel.getSource().getSourceAbbreviation());
        cstmt.setString(16, String.valueOf(rel.getStatus()));
        cstmt.setString(17, rel.isGenerated() ? "Y" : "N");
        cstmt.setString(18, String.valueOf(rel.getLevel()));
        cstmt.setString(19, String.valueOf(rel.getReleased()));
        cstmt.setString(20, String.valueOf(rel.getTobereleased()));
        cstmt.setString(21, rel.getSuppressible());
        cstmt.setString(22, l_sg_id_1);
        cstmt.setString(23, l_sg_type_1);
        cstmt.setString(24, l_sg_qualifier_1);
        cstmt.setString(25, l_sg_id_2);
        cstmt.setString(26, l_sg_type_2);
        cstmt.setString(27, l_sg_qualifier_2);
        cstmt.setString(28,
                        rel.getSourceIdentifier() == null ? "" :
                        rel.getSourceIdentifier().toString());
        cstmt.setString(29,
                        rel.getGroupIdentifier() == null ? "" :
                        rel.getGroupIdentifier().toString());
        cstmt.execute();

        int relationship_id = cstmt.getInt(1);

        //
        // Handle error case
        //
        if (relationship_id < 1) {
          throw new SQLException();
        }

        rel.setIdentifier(new Identifier.Default(relationship_id));
        action.setRowIdentifier(relationship_id);
        cstmt.close();
        MEMEToolkit.trace(
            "ActionEngine.atomicInsertForRelationship(AtomicInsertAction)...completed.");

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        ActionException ae = new ActionException(
            "Failed to perform atomic insert action.", action, se);
        ae.setDetail("l_concept_id_1",
                     rel.getConcept().getIdentifier() == null ? new Integer(0) :
                     new Integer(rel.getConcept().getIdentifier().intValue()));
        ae.setDetail("l_concept_id_2",
                     rel.getRelatedConcept().getIdentifier() == null ?
                     new Integer(0) :
                     new Integer(rel.getRelatedConcept().getIdentifier().
                                 intValue()));
        ae.setDetail("l_atom_id_1",
                     (rel.getAtom() == null || rel.getAtom().getIdentifier() == null) ?
                     new Integer(0) :
                     new Integer(rel.getAtom().getIdentifier().intValue()));
        ae.setDetail("l_relationship_name", rel.getName() == null ? "" :
                     rel.getName());
        ae.setDetail("l_relationship_attribute",
                     rel.getAttribute() == null ? "" : rel.getAttribute());
        ae.setDetail("l_atom_id_2",
                     (rel.getRelatedAtom() == null ||
                      rel.getRelatedAtom().getIdentifier() == null) ?
                     new Integer(0) :
                     new Integer(rel.getRelatedAtom().getIdentifier().intValue()));
        ae.setDetail("l_source_of_label",
                     rel.getSourceOfLabel() == null ? "" :
                     rel.getSourceOfLabel().toString());
        ae.setDetail("l_source", rel.getSource() == null ? "" :
                     rel.getSource().toString());
        ae.setDetail("l_status", new Character(rel.getStatus()));
        ae.setDetail("l_generated", rel.isGenerated() ? "Y" : "N");
        ae.setDetail("l_level", new Character(rel.getLevel()));
        ae.setDetail("l_released", new Character(rel.getReleased()));
        ae.setDetail("l_tobereleased", new Character(rel.getTobereleased()));
        ae.setDetail("l_suppressible", rel.getSuppressible());
        ae.setDetail("l_sg_id_1", l_sg_id_1);
        ae.setDetail("l_sg_type_1", l_sg_type_1);
        ae.setDetail("l_sg_qualifier_1", l_sg_qualifier_1);
        ae.setDetail("l_sg_id_2", l_sg_id_2);
        ae.setDetail("l_sg_type_2", l_sg_type_2);
        ae.setDetail("l_sg_qualifier_2", l_sg_qualifier_2);
        ae.setDetail("l_source_rui", rel.getRUI() == null ? "" :
                     rel.getRUI().toString());
        ae.setDetail("l_relationship_group",
                     rel.getGroupIdentifier() == null ? "" :
                     rel.getGroupIdentifier().toString());
        throw ae;

      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic insert for an {@link Attribute}.
     * @param action the {@link AtomicInsertAction} to perform
         * @throws ActionException if failed to perform atomic insert for attribute.
     */
    private void atomicInsertForAttribute(AtomicInsertAction action) throws
        ActionException {

      //
      // extract attribute to insert
      //
      final Attribute attr = (Attribute) action.getElementToInsert();

      //
      // Handle "long attribute" if length of attribute_value > 100
      //
      String l_attribute_value = null;
      if (attr.getValue() != null && attr.getValue().length() > 100) {
        int string_id = 0;
        try {
          string_id = data_source.getNextIdentifierForType(String.class).
              intValue();
        } catch (DataSourceException dse) {
          throw new ActionException("Failed to get next id.", action, dse);
        }
        l_attribute_value = "<>Long_Attribute<>:" + string_id;
        int row_sequence = 1;
        int start = 0;
        int end = 0;
        final String value = attr.getValue();
        final String insert_stringtab = "INSERT INTO stringtab "
            + "(string_id, row_sequence, text_total, text_value) "
            + "VALUES (?,?,?,?)";
        PreparedStatement pstmt = null;
        try {
          pstmt = data_source.prepareStatement(insert_stringtab);
          do {
            end = value.length() < start + 1786 ? value.length() :
                (start + 1786);
            pstmt.setInt(1, string_id);
            pstmt.setInt(2, row_sequence++);
            pstmt.setInt(3, value.length());
            pstmt.setString(4, value.substring(start, end));
            pstmt.executeUpdate();
            start = end;
          } while (start < value.length());
          pstmt.close();
        } catch (SQLException se) {
          try {
            pstmt.close();
          } catch (SQLException e) {}
          ActionException ae = new ActionException(
              "Failed to insert row into stringtab.", action, se);
          throw ae;
        }

      } else {
        l_attribute_value = attr.getValue();
      }

      //
      // Compute MD5 hash for attribute value (use UTF8 character encoding)
      //
      String hash = null;
      try {
        if (attr.getValue() != null) {
          hash = SystemToolkit.md5(attr.getValue());
        } else {
          hash = "d41d8cd98f00b204e9800998ecf8427e";
        }
      } catch (NoSuchAlgorithmException nsae) {
        ActionException ae = new ActionException(
            "Failed to compute the MD5 hash due to no such algorithm exception.",
            action, nsae);
        throw ae;
      } catch (UnsupportedEncodingException uee) {
        ActionException ae = new ActionException(
            "Failed to compute the MD5 hash due to unsupported encoding exception.",
            action, uee);
        throw ae;
      }

      //
      // Call aproc_insert_attribute
      //
      final String insert_attribute =
          "{? = call MEME_APROCS.aproc_insert_attribute("
          + "atomic_action_id => ?, "
          + "attribute_id => ?, "
          + "source_attribute_id => ?, "
          + "origin => 'Local', "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?, "
          + "l_level => ?, "
          + "l_concept_id => ?, "
          + "l_atom_id => ?, "
          + "l_attribute_name => ?, "
          + "l_source => ?, "
          + "l_status => ?, "
          + "l_attribute_value => ?, "
          + "l_generated => ?, "
          + "l_released => ?, "
          + "l_tobereleased => ?, "
          + "l_suppressible => ?, "
          + "l_sg_id => ?, "
          + "l_sg_type => ?, "
          + "l_sg_qualifier => ?, "
          + "l_source_atui => ?, "
          + "l_hashcode => ?)} ";

      //
      // Determine native identifier values
      //
      String l_sg_id = "";
      String l_sg_type = "";
      String l_sg_qualifier = "";
      if (attr.getNativeIdentifier() != null) {
        l_sg_id = attr.getNativeIdentifier().toString();
        l_sg_type = attr.getNativeIdentifier().getType();
        l_sg_qualifier = attr.getNativeIdentifier().getQualifier() == null ?
            null : attr.getNativeIdentifier().getQualifier().toString();
      }

      //
      // Call aproc
      //
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(insert_attribute);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getIdentifier() == null ? 0 :
                     action.getIdentifier().intValue());
        cstmt.setInt(3, attr.getIdentifier() == null ? 0 :
                     attr.getIdentifier().intValue());
        cstmt.setInt(4, attr.getSrcIdentifier() == null ? 0 :
                     attr.getSrcIdentifier().intValue());
        cstmt.setInt(5, (action.getParent() != null &&
                         action.getParent().getIdentifier() == null) ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(6, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(7, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.setString(8, String.valueOf(attr.getLevel()));
        cstmt.setInt(9, (attr.getConcept() == null ||
                         attr.getConcept().getIdentifier() == null) ? 0 :
                     attr.getConcept().getIdentifier().intValue());
        cstmt.setInt(10, (attr.getAtom() == null ||
                          attr.getAtom().getIdentifier() == null) ? 0 :
                     attr.getAtom().getIdentifier().intValue());
        cstmt.setString(11, attr.getName() == null ? "" : attr.getName());
        cstmt.setString(12, (attr.getSource() == null ||
                             attr.getSource().getSourceAbbreviation() == null) ?
                        "" :
                        attr.getSource().getSourceAbbreviation());
        cstmt.setString(13, String.valueOf(attr.getStatus()));
        cstmt.setString(14, l_attribute_value);
        cstmt.setString(15, attr.isGenerated() ? "Y" : "N");
        cstmt.setString(16, String.valueOf(attr.getReleased()));
        cstmt.setString(17, String.valueOf(attr.getTobereleased()));
        cstmt.setString(18, attr.getSuppressible());
        cstmt.setString(19, l_sg_id);
        cstmt.setString(20, l_sg_type);
        cstmt.setString(21, l_sg_qualifier);
        cstmt.setString(22,
                        attr.getSourceIdentifier() == null ? "" :
                        attr.getSourceIdentifier().toString());
        cstmt.setString(23, hash);
        cstmt.execute();

        int attribute_id = cstmt.getInt(1);
        if (attribute_id < 1) {
          throw new SQLException();
        }

        attr.setIdentifier(new Identifier.Default(attribute_id));
        action.setRowIdentifier(attribute_id);
        cstmt.close();
      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        ActionException ae = new ActionException(
            "Failed to perform atomic insert action.", action, se);
        ae.setDetail("l_level", new Character(attr.getLevel()));
        ae.setDetail("l_concept_id",
                     attr.getConcept().getIdentifier() == null ? new Integer(0) :
                     new Integer(attr.getConcept().getIdentifier().intValue()));
        ae.setDetail("l_atom_id",
                     (attr.getAtom() == null || attr.getAtom().getIdentifier() == null) ?
                     new Integer(0) :
                     new Integer(attr.getAtom().getIdentifier().intValue()));
        ae.setDetail("l_attribute_name", attr.getName() == null ? "" :
                     attr.getName());
        ae.setDetail("l_source", attr.getSource() == null ? "" :
                     attr.getSource().toString());
        ae.setDetail("l_status", new Character(attr.getStatus()));
        ae.setDetail("l_attribute_value", l_attribute_value);
        ae.setDetail("l_generated", attr.isGenerated() ? "Y" : "N");
        ae.setDetail("l_released", new Character(attr.getReleased()));
        ae.setDetail("l_tobereleased", new Character(attr.getTobereleased()));
        ae.setDetail("l_suppressible", attr.getSuppressible());
        ae.setDetail("l_sg_id", l_sg_id);
        ae.setDetail("l_sg_type", l_sg_type);
        ae.setDetail("l_sg_qualifier", l_sg_qualifier);
        ae.setDetail("l_source_atui", attr.getATUI() == null ? "" :
                     attr.getATUI().toString());

        throw ae;

      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic insert for a {@link Concept}.
     * @param action the {@link AtomicInsertAction} to perform
     * @throws ActionException if failed to perform atomic insert for concept.
     */
    private void atomicInsertForConcept(AtomicInsertAction action) throws
        ActionException {

      //
      // Extract concept to insert
      //
      final Concept concept = (Concept) action.getElementToInsert();

      //
      // Call aproc_insert_cs
      //
      final String insert_cs =
          "{? = call MEME_APROCS.aproc_insert_cs("
          + "atomic_action_id => ?, "
          + "concept_id => ?, "
          + "source_concept_id => ?, "
          + "origin => 'Local', "
          + "source => 'MTH', "
          + "status => ?, "
          + "tobereleased => ?, "
          + "released => ?, "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(insert_cs);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getIdentifier() == null ? 0 :
                     action.getIdentifier().intValue());
        cstmt.setInt(3, concept.getIdentifier() == null ? 0 :
                     concept.getIdentifier().intValue());
        cstmt.setInt(4, concept.getSrcIdentifier() == null ? 0 :
                     concept.getSrcIdentifier().intValue());
        cstmt.setString(5, "N");
        cstmt.setString(6, "Y");
        cstmt.setString(7, "N");
        cstmt.setInt(8, (action.getParent() != null &&
                         action.getParent().getIdentifier() == null) ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(9, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(10, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.execute();

        int concept_id = cstmt.getInt(1);
        if (concept_id < 1) {
          throw new SQLException();
        }

        concept.setIdentifier(new Identifier.Default(concept_id));
        action.setRowIdentifier(concept_id);
        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        ActionException ae = new ActionException(
            "Failed to perform atomic insert action.", action, se);
        ae.setDetail("source", concept.getSource() == null ? "" :
                     concept.getSource().toString());
        ae.setDetail("status", new Character(concept.getStatus()));
        ae.setDetail("tobereleased", new Character(concept.getTobereleased()));
        ae.setDetail("released", new Character(concept.getReleased()));
        throw ae;

      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    //
    // Process Batch Insert
    //

    /**
     * Performs process batch insert atom.
     * @param transaction the {@link BatchMolecularTransaction} to perform
     * @throws ActionException if failed to process batch insert atom
     */
    private void processBatchInsertAtom(BatchMolecularTransaction transaction) throws
        ActionException {

      PreparedStatement pstmt = null;
      try {
        pstmt = data_source.prepareStatement("select * from " +
                                             transaction.getTableName());
        final ResultSet rs = pstmt.executeQuery();
        final SourceAtomMapper mapper = new SourceAtomMapper();
        while (rs.next()) {
          final Atom atom = mapper.map(rs, data_source);
          populateConceptStatus(atom);
          final MolecularInsertAtomAction miaa = new MolecularInsertAtomAction(
              atom);
          miaa.setAuthority(transaction.getAuthority());
          miaa.setTransactionIdentifier(transaction.getIdentifier());
          miaa.setWorkIdentifier(transaction.getWorkIdentifier());
          data_source.getActionEngine().processAction(miaa);
          transaction.addSubAction(miaa);
        }
        pstmt.close();
      } catch (Exception e) {
        try {
          pstmt.close();
        } catch (SQLException se) {}
        ActionException ae = new ActionException(
            "Failed to perform process batch insert atom action.", transaction,
            e);
        throw ae;
      }
    }

    /**
     * Used to look up the status of a concept.  This is a shortcut used
     * to avoid having to read all concept information before performing an
     * action.
     * @param e a {@ConceptElement}
     * @throws SQLException if anything goes wrong
     */
    private void populateConceptStatus(ConceptElement e) throws SQLException {
      // Look up status of atom's concept.
      PreparedStatement status_stmt = null;
      try {
        status_stmt = data_source.prepareStatement(
            "SELECT status FROM concept_status " +
            "WHERE concept_id = ?");
        status_stmt.setInt(1, e.getConcept().getIdentifier().intValue());
        final ResultSet status_rs = status_stmt.executeQuery();
        while (status_rs.next()) {
          e.getConcept().setStatus(status_rs.getString("STATUS").charAt(0));
        }
        status_stmt.close();
      } catch (SQLException se) {
        try {
          status_stmt.close();
        } catch (SQLException se2) {}
        throw se;
      }
    }

    /**
     * Performs process batch insert relationship.
     * @param transaction the {@link BatchMolecularTransaction} to perfrom
     * @throws ActionException if failed to process batch insert relationship
     */
    private void processBatchInsertRelationship(BatchMolecularTransaction
                                                transaction) throws
        ActionException {
      PreparedStatement pstmt = null;
      try {
        pstmt = data_source.prepareStatement("SELECT * FROM " +
                                             transaction.getTableName());
        final ResultSet rs = pstmt.executeQuery();
        final SourceRelationshipMapper srm = new SourceRelationshipMapper();
        while (rs.next()) {
          final Relationship rel = srm.map(rs, data_source);
          if (rel.isSourceAsserted()) {
            populateConceptStatus(rel);
          }
          final MolecularInsertRelationshipAction mira = new
              MolecularInsertRelationshipAction(rel);
          mira.setAuthority(transaction.getAuthority());
          mira.setTransactionIdentifier(transaction.getIdentifier());
          mira.setWorkIdentifier(transaction.getWorkIdentifier());
          data_source.getActionEngine().processAction(mira);
          transaction.addSubAction(mira);
        }
        pstmt.close();
      } catch (Exception e) {
        try {
          pstmt.close();
        } catch (SQLException se) {}
        ActionException ae = new ActionException(
            "Failed to perform process batch insert relationship action.",
            transaction, e);
        throw ae;
      }
    }

    /**
     * Performs process batch insert attribute.
     * @param transaction the {@link BatchMolecularTransaction} to perform
     * @throws ActionException if failed to process batch insert attribute
     */
    private void processBatchInsertAttribute(BatchMolecularTransaction
                                             transaction) throws
        ActionException {

      PreparedStatement pstmt = null;
      try {
        pstmt = data_source.prepareStatement("SELECT * FROM " +
                                             transaction.getTableName());
        final ResultSet rs = pstmt.executeQuery();
        final SourceAttributeMapper mapper = new SourceAttributeMapper();
        while (rs.next()) {
          final Attribute attr = mapper.map(rs, data_source);
          populateConceptStatus(attr);
          final MolecularInsertAttributeAction miaa = new
              MolecularInsertAttributeAction(attr);
          miaa.setAuthority(transaction.getAuthority());
          miaa.setTransactionIdentifier(transaction.getIdentifier());
          miaa.setWorkIdentifier(transaction.getWorkIdentifier());
          data_source.getActionEngine().processAction(miaa);
          transaction.addSubAction(miaa);
        }
        pstmt.close();
      } catch (Exception e) {
        try {
          pstmt.close();
        } catch (SQLException se) {}
        ActionException ae = new ActionException(
            "Failed to perform process batch insert attribute action.",
            transaction, e);
        throw ae;
      }
    }

    //
    // Process Macro Insert
    //

    /**
     * Performs process macro insert atom.
     * @param action the {@link BatchMolecularTransaction} to  perform
     * @throws ActionException if failed to process macro insert atom
     */
    private void processMacroInsertAtom(MacroMolecularAction action) throws
        ActionException {

      //
      // Set the transaction_id for this action
      //
      int transaction_id = 0;
      try {
        transaction_id = data_source.getNextIdentifierForType(
            MolecularTransaction.class).intValue();
      } catch (DataSourceException dse) {
        ActionException ae = new ActionException(
            "Failed to get next transaction identifier.", action, dse);
        throw ae;
      }
      action.setTransactionIdentifier(new Identifier.Default(transaction_id));

      //
      // Configure ticket
      //
      final Ticket ticket = Ticket.getEmptyTicket();
      ticket.setDataTypeQuery(Atom.class,
                              "SELECT * FROM " + action.getTableName() +
                              " WHERE atom_id = ?");
      ticket.setMapDataType(Atom.class, true);
      ticket.addDataMapper(Atom.class, new SourceAtomMapper());

      //
      // Open a query over the table with the atoms
      //
      PreparedStatement pstmt = null;
      try {
        pstmt = data_source.prepareStatement("select atom_id from " +
                                             action.getTableName());
        final ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          final Atom atom = data_source.getAtomWithName(rs.getInt("ATOM_ID"),
              ticket);

          //
          // Use molecular atoms to insert
          //
          final MolecularInsertAtomAction miaa = new MolecularInsertAtomAction(
              atom);
          miaa.setAuthority(action.getAuthority());
          miaa.setTransactionIdentifier(action.getTransactionIdentifier());
          miaa.setWorkIdentifier(action.getWorkIdentifier());
          data_source.getActionEngine().processAction(miaa);
          action.addSubAction(miaa);
        }
        pstmt.close();
      } catch (Exception e) {
        try {
          pstmt.close();
        } catch (SQLException se) {}
        ActionException ae = new ActionException(
            "Failed to perform process macro insert atom action.", action, e);
        throw ae;
      }
    }

    //
    // Process Batch Approve Concept
    //

    /**
     * Performs process batch approve concept.
     * @param transaction the {@link BatchMolecularTransaction} to perform
     * @throws ActionException if failed to process batch approve concept
     */
    private void processBatchApproveConcept(BatchMolecularTransaction
                                            transaction) throws ActionException {

      //
      // Get data source as MID Data source
      //
      final MIDDataSource mds = (MIDDataSource) data_source;

      PreparedStatement pstmt = null;
      try {

        //
        // Open query for concepts to approve
        //
        pstmt = data_source.prepareStatement(
            "select concept_id from " + transaction.getTableName());

        //
        // Get approval integrity vector
        //
        final EnforcableIntegrityVector eiv =
            (EnforcableIntegrityVector) mds.getApplicationVector("APPROVAL");

        //
        // Iterate through result set, build concept list
        //
        final ResultSet rs = pstmt.executeQuery();
        final List concepts = new ArrayList();
        while (rs.next()) {
          final Concept concept = new Concept.Default(rs.getInt("CONCEPT_ID"));
          concepts.add(concept);
        }
        pstmt.close();

        //
        // Iterate through concepts, approve each one in turn
        //
        final Iterator iter = concepts.iterator();
        while (iter.hasNext()) {
          final Concept concept = (Concept) iter.next();
          final MolecularApproveConceptAction maca = new
              MolecularApproveConceptAction(concept);
          maca.setSource(concept);
          maca.setAuthority(transaction.getAuthority());
          maca.setTransactionIdentifier(transaction.getIdentifier());
          maca.setWorkIdentifier(transaction.getWorkIdentifier());
          maca.setIntegrityVector(eiv);

          try {
            data_source.getActionEngine().processAction(maca);
          } catch (IntegrityViolationException ive) {
            maca.setStatus('V');
          } catch (ActionException ae) {
            maca.setStatus('E');
            ae.setFatal(false);
            MEMEToolkit.handleError(ae);
          } catch (Exception e) {
            maca.setStatus('E');
            MEMEException me = new MEMEException("Unexpected Error");
            me.setEnclosedException(e);
            me.setFatal(false);
            MEMEToolkit.handleError(me);
          }
          transaction.addSubAction(maca);
        }
      } catch (Exception e) {
        try {
          pstmt.close();
        } catch (SQLException sqe) {}
        ActionException ae = new ActionException(
            "Failed to perform process batch approve concept action.",
            transaction, e);
        throw ae;
      }
    }

    //
    // Protected Methods
    //

    /**
     * Add indexes for atom.
     * @param atom the {@link Atom}.
     * @throws DataSourceException if failed to add indexes for atom.
     */
    protected void addIndexesForAtom(Atom atom) throws DataSourceException {

      PreparedStatement pstmt = null;

      final String insert_wi_str =
          "INSERT INTO word_index (atom_id, word) VALUES (?,?)";
      final String[] index_words = atom.getIndexWords();
      try {
        pstmt = data_source.prepareStatement(insert_wi_str);
        for (int i = 0; i < index_words.length; i++) {
          // Insert into word_index
          pstmt.setInt(1, atom.getIdentifier().intValue());
          pstmt.setString(2, index_words[i].toLowerCase());
          pstmt.executeUpdate();
        }
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (SQLException sqe) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to word_index.", this, se);
        dse.setDetail("insert", insert_wi_str);
        throw dse;
      }

      final String insert_nw_str =
          "INSERT INTO normwrd (normwrd_id, normwrd) VALUES (?,?)";
      String[] norm_index_words = atom.getNormalizedIndexWords();
      try {
        pstmt = data_source.prepareStatement(insert_nw_str);
        for (int i = 0; i < norm_index_words.length; i++) {
          // Insert into normwrd
          pstmt.setInt(1, atom.getIdentifier().intValue());
          pstmt.setString(2, norm_index_words[i]);
          pstmt.executeUpdate();
        }
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (SQLException sqe) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to normwrd.", this, se);
        dse.setDetail("insert", insert_nw_str);
        throw dse;
      }

      final String insert_ns_str =
          "INSERT INTO normstr (normstr_id, normstr) VALUES (?,?)";
      try {
        pstmt = data_source.prepareStatement(insert_ns_str);
        // Insert into normstr
        pstmt.setInt(1, atom.getIdentifier().intValue());
        pstmt.setString(2, atom.getNormalizedString());
        pstmt.executeUpdate();
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (SQLException sqe) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to normstr.", this, se);
        dse.setDetail("insert", insert_ns_str);
        throw dse;
      }
    }

    /**
     * Inserts a new molecular action.
     * @param ma the {@link MolecularAction}.
     * @throws DataSourceException if failed to add molecular action.
     */
    protected void addMolecularAction(MolecularAction ma) throws
        DataSourceException {

      final String insert_str = "INSERT INTO molecular_actions ( "
          + "transaction_id, molecule_id, "
          + "authority, timestamp, molecular_action, source_id, target_id, "
          + "undone, undone_by, undone_when, status, elapsed_time, work_id) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement pstmt = null;
      try {
        pstmt = data_source.prepareStatement(insert_str);
        pstmt.setInt(1, ma.getTransactionIdentifier() == null ? 0 :
                     ma.getTransactionIdentifier().intValue());
        pstmt.setInt(2, ma.getIdentifier().intValue());
        pstmt.setString(3,
                        ma.getAuthority() == null ? "" :
                        ma.getAuthority().toString());
        pstmt.setString(4, dateformat.format(ma.getTimestamp()));
        pstmt.setString(5, ma.getActionName());
        pstmt.setInt(6, ma.getSource().getIdentifier().intValue());
        pstmt.setInt(7, ma.getTarget() == null ? 0 :
                     ma.getTarget().getIdentifier().intValue());
        pstmt.setString(8, ma.isUndone() ? "Y" : "N");
        pstmt.setString(9, ma.getUndoneAuthority() == null ? "" :
                        ma.getUndoneAuthority().toString());
        pstmt.setString(10, ma.getUndoneTimestamp() == null ? "" :
                        dateformat.format(ma.getUndoneTimestamp()));
        pstmt.setString(11, String.valueOf(ma.getStatus()));
        pstmt.setString(12, String.valueOf(ma.getElapsedTime()));
        pstmt.setInt(13, ma.getWorkIdentifier() == null ? 0 :
                     ma.getWorkIdentifier().intValue());
        pstmt.executeUpdate();
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (SQLException sqe) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to molecular_actions.", ma, se);
        dse.setDetail("query", insert_str);
        throw dse;
      }
    }

    /**
     * Adds a string.
     * @param atom the {@link Atom}
     * @throws DataSourceException if failed to add string
     */
    protected void addString(Atom atom) throws DataSourceException {

      int len1 = atom.getString().length() < 10 ? atom.getString().length() :
          10;
      int len2 = atom.getNormalizedString().length() <
          10 ? atom.getNormalizedString().length() : 10;

      final String insert_str = "INSERT INTO string_ui (lui, sui, "
          + "string_pre, norm_string_pre, language, base_string, "
          + "string, norm_string, isui, lowercase_string_pre) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      PreparedStatement pstmt = null;
      try {
        pstmt = data_source.prepareStatement(insert_str);
        pstmt.setString(1, atom.getLUI().toString());
        pstmt.setString(2, atom.getSUI().toString());
        pstmt.setString(3, atom.getString().substring(0, len1));
        pstmt.setString(4, atom.getNormalizedString().substring(0, len2));
        pstmt.setString(5, (atom.getLanguage() == null) ? "ENG" :
                        atom.getLanguage().getAbbreviation());
        pstmt.setString(6, atom.isBaseString() ? "Y" : "N");
        pstmt.setString(7, atom.getString());
        pstmt.setString(8, atom.getNormalizedString());
        pstmt.setString(9, atom.getISUI().toString());
        pstmt.setString(10, atom.getString().substring(0, len1).toLowerCase());
        pstmt.executeUpdate();
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (SQLException sqe) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to string_ui.");
        dse.setEnclosedException(se);
        dse.setDetail("query", insert_str);
        dse.setDetail("atom", atom.getString());
        dse.setDetail("sui", atom.getSUI());
        dse.setDetail("lui", atom.getLUI());
        dse.setDetail("isui", atom.getISUI());
        throw dse;
      }
    }

    /**
     * Sets preferred atom.
     * @param concept the {@link Concept}.
     * @throws DataSourceException if failed to set preferred atom.
     */
    protected void setPreferredAtom(Concept concept) throws DataSourceException {
      final String call_str =
          "{? = call MEME_RANKS.set_preferred_id("
          + "concept_id => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(call_str);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, concept.getIdentifier().intValue());
        cstmt.execute();
        if (cstmt.getInt(1) != 0) {
          DataSourceException dse =
              new DataSourceException("Set preferred atom failed.");
          dse.setDetail("call_str", call_str);
          throw dse;
        }
        cstmt.close();
      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException sqe) {}
        DataSourceException dse = new DataSourceException(
            "Set preferred atom failed.", this, se);
        dse.setDetail("call", call_str);
        throw dse;
      }
    }

    /**
     * Performs atomic delete.
     * @param action the {@link AtomicAction} to perform
     * @throws ActionException if failed to perform atomic delete
     */
    protected void atomicDelete(AtomicAction action) throws ActionException {
      final String change_dead =
          "{? = call MEME_APROCS.aproc_change_dead("
          + "table_name => ?, "
          + "row_id => ?, "
          + "old_dead => ?, "
          + "new_dead => ?, "
          + "action_status => ?, "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(change_dead);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setString(2, action.getAffectedTable() == null ? "" :
                        action.getAffectedTable());
        cstmt.setInt(3, action.getRowIdentifier() == null ? 0 :
                     action.getRowIdentifier().intValue());
        cstmt.setString(4, action.getOldValue() == null ? "" :
                        action.getOldValue());
        cstmt.setString(5, action.getNewValue() == null ? "" :
                        action.getNewValue());
        cstmt.setString(6, String.valueOf(action.getStatus()));
        cstmt.setInt(7, action.getParent().getIdentifier() == null ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(8, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(9, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic delete action.");
        }

        cstmt.close();
      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic delete action.", action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic change status.
     * @param action the {@link AtomicAction} to perform
     * @throws ActionException if failed to perform atomic change status
     */
    protected void atomicChangeStatus(AtomicAction action) throws
        ActionException {
      final String change_status =
          "{? = call MEME_APROCS.aproc_change_status("
          + "table_name => ?, "
          + "row_id => ?, "
          + "old_status => ?, "
          + "new_status => ?, "
          + "action_status => ?, "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(change_status);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setString(2, action.getAffectedTable());
        cstmt.setInt(3, action.getRowIdentifier() == null ? 0 :
                     action.getRowIdentifier().intValue());
        cstmt.setString(4, action.getOldValue() == null ? "" :
                        action.getOldValue());
        cstmt.setString(5, action.getNewValue() == null ? "" :
                        action.getNewValue());
        cstmt.setString(6, String.valueOf(action.getStatus()));
        cstmt.setInt(7, action.getParent().getIdentifier() == null ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(8, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(9, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic change status action.");
        }

        cstmt.close();
      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic status action.", action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic change tobereleased.
     * @param action the {@link AtomicAction} to perform
     * @throws ActionException if failed to perform atomic change tobereleased
     */
    protected void atomicChangeTobereleased(AtomicAction action) throws
        ActionException {
      final String change_tbr =
          "{? = call MEME_APROCS.aproc_change_tbr("
          + "table_name => ?, "
          + "row_id => ?, "
          + "old_tobereleased => ?, "
          + "new_tobereleased => ?, "
          + "action_status => ?, "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(change_tbr);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setString(2, action.getAffectedTable() == null ? "" :
                        action.getAffectedTable());
        cstmt.setInt(3, action.getRowIdentifier() == null ? 0 :
                     action.getRowIdentifier().intValue());
        cstmt.setString(4, action.getOldValue() == null ? "" :
                        action.getOldValue());
        cstmt.setString(5, action.getNewValue() == null ? "" :
                        action.getNewValue());
        cstmt.setString(6, String.valueOf(action.getStatus()));
        cstmt.setInt(7, action.getParent().getIdentifier() == null ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(8, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(9, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic change tobereleased action.");
        }

        cstmt.close();
      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic action change tobereleased action.",
            action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic change field.
     * @param action the {@link AtomicAction} to perform
     * @throws ActionException if failed to perform atomic change field
     */
    protected void atomicChangeField(AtomicAction action) throws
        ActionException {
      final String change_field =
          "{? = call MEME_APROCS.aproc_change_field("
          + "row_id => ?, "
          + "table_name => ?, "
          + "field_name => ?, "
          + "old_value => ?, "
          + "new_value => ?, "
          + "action_status => ?, "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(change_field);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getRowIdentifier() == null ? 0 :
                     action.getRowIdentifier().intValue());
        cstmt.setString(3, action.getAffectedTable() == null ? "" :
                        action.getAffectedTable());
        cstmt.setString(4, action.getField() == null ? "" :
                        action.getField());
        cstmt.setString(5, action.getOldValue() == null ? "" :
                        action.getOldValue());
        cstmt.setString(6, action.getNewValue() == null ? "" :
                        action.getNewValue());
        cstmt.setString(7, String.valueOf(action.getStatus()));
        cstmt.setInt(8, action.getParent().getIdentifier() == null ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(9, action.getAuthority().toString() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(10, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic change field action.");
        }

        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic change field action.", action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic change concept id.
     * @param action the {@link AtomicAction}
     * @throws ActionException if failed to perform atomic change concept id
     */
    protected void atomicChangeConceptId(AtomicAction action) throws
        ActionException {
      if (action.getAffectedTable().equals("C")) {
        final String change_concept_id =
            "{? = call MEME_APROCS.aproc_change_concept_id("
            + "atom_id => ?, "
            + "old_concept_id => ?, "
            + "new_concept_id => ?, "
            + "action_status => ?, "
            + "molecule_id => ?, "
            + "authority => ?, "
            + "timestamp => ?)}";
        CallableStatement cstmt = null;
        try {
          cstmt = data_source.prepareCall(change_concept_id);
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setInt(2, action.getRowIdentifier() == null ? 0 :
                       action.getRowIdentifier().intValue());
          cstmt.setString(3, action.getOldValue() == null ? "" :
                          action.getOldValue());
          cstmt.setString(4, action.getNewValue() == null ? "" :
                          action.getNewValue());
          cstmt.setString(5, String.valueOf(action.getStatus()));
          cstmt.setInt(6, action.getParent().getIdentifier() == null ? 0 :
                       action.getParent().getIdentifier().intValue());
          cstmt.setString(7, action.getAuthority() == null ? "" :
                          action.getAuthority().toString());
          cstmt.setString(8, action.getTimestamp() == null ? "" :
                          dateformat.format(action.getTimestamp()));
          cstmt.execute();

          if (cstmt.getInt(1) != 0) {
            cstmt.close();
            throw new ActionException(
                "Failed to perform atomic change concept id action.");
          }

          cstmt.close();

        } catch (SQLException se) {
          try {
            cstmt.close();
          } catch (SQLException e) {}
          throw new ActionException(
              "Failed to perform atomic change concept id action.", action, se);
        } finally {
          try {
            MEMEToolkit.logComment(data_source.flushBuffer());
          } catch (DataSourceException dse) {}
        }

      } else if (action.getAffectedTable().equals("A") ||
                 action.getAffectedTable().equals("R")) {

        final String change_id =
            "{? = call MEME_APROCS.aproc_change_id("
            + "table_name => ?, "
            + "row_id => ?, "
            + "old_concept_id => ?, "
            + "new_concept_id => ?, "
            + "action_status => ?, "
            + "molecule_id => ?, "
            + "authority => ?, "
            + "timestamp => ?)}";
        CallableStatement cstmt = null;
        try {
          cstmt = data_source.prepareCall(change_id);
          cstmt.registerOutParameter(1, Types.INTEGER);
          cstmt.setString(2, action.getAffectedTable() == null ? "" :
                          action.getAffectedTable());
          cstmt.setInt(3, action.getRowIdentifier() == null ? 0 :
                       action.getRowIdentifier().intValue());
          cstmt.setString(4, action.getOldValue() == null ? "" :
                          action.getOldValue());
          cstmt.setString(5, action.getNewValue() == null ? "" :
                          action.getNewValue());
          cstmt.setString(6, String.valueOf(action.getStatus()));
          cstmt.setInt(7, action.getParent().getIdentifier() == null ? 0 :
                       action.getParent().getIdentifier().intValue());
          cstmt.setString(8, action.getAuthority() == null ? "" :
                          action.getAuthority().toString());
          cstmt.setString(9, action.getTimestamp() == null ? "" :
                          dateformat.format(action.getTimestamp()));
          cstmt.execute();

          if (cstmt.getInt(1) != 0) {
            cstmt.close();
            throw new ActionException(
                "Failed to perform atomic change id action.");
          }

          cstmt.close();

        } catch (SQLException se) {
          try {
            cstmt.close();
          } catch (SQLException e) {}
          throw new ActionException(
              "Failed to perform atomic change id action.", action, se);
        } finally {
          try {
            MEMEToolkit.logComment(data_source.flushBuffer());
          } catch (DataSourceException dse) {}
        }

      }
    }

    /**
     * Performs atomic change atom id.
     * @param action the {@link AtomicAction} to perform
     * @throws ActionException if failed to perform atomic change atom id
     */
    protected void atomicChangeAtomId(AtomicAction action) throws
        ActionException {
      final String change_atom_id =
          "{? = call MEME_APROCS.aproc_change_atom_id("
          + "row_id => ?, "
          + "table_name => ?, "
          + "old_atom_id => ?, "
          + "new_atom_id => ?, "
          + "action_status => ?, "
          + "molecule_id => ?, "
          + "authority => ?, "
          + "timestamp => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(change_atom_id);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getRowIdentifier() == null ? 0 :
                     action.getRowIdentifier().intValue());
        cstmt.setString(3, action.getAffectedTable() == null ? "" :
                        action.getAffectedTable());
        cstmt.setInt(4, action.getOldValueAsInt());
        cstmt.setInt(5, action.getNewValueAsInt());
        cstmt.setString(6, String.valueOf(action.getStatus()));
        cstmt.setInt(7, action.getParent().getIdentifier() == null ? 0 :
                     action.getParent().getIdentifier().intValue());
        cstmt.setString(8, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.setString(9, action.getTimestamp() == null ? "" :
                        dateformat.format(action.getTimestamp()));
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic change atom id action.");
        }

        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic change atom id action.", action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic undo.
     * @param action the {@link AtomicAction} to undo
     * @throws ActionException if failed to perform atomic undo
     */
    protected void atomicUndo(AtomicAction action) throws ActionException {
      final String undo =
          "{? = call MEME_APROCS.aproc_undo("
          + "atomic_action_id => ?, "
          + "authority => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(undo);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getIdentifier() == null ? 0 :
                     action.getIdentifier().intValue());
        cstmt.setString(3, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic undo action.");
        }

        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic undo action.", action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Performs atomic redo.  This method is local in this class.
     * @param action the {@link AtomicAction} to redo
     * @throws ActionException if failed to perform atomic redo
     */
    protected void atomicRedo(AtomicAction action) throws ActionException {
      final String redo =
          "{? = call MEME_APROCS.aproc_redo("
          + "atomic_action_id => ?, "
          + "authority => ?)}";
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall(redo);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, action.getIdentifier() == null ? 0 :
                     action.getIdentifier().intValue());
        cstmt.setString(3, action.getAuthority() == null ? "" :
                        action.getAuthority().toString());
        cstmt.execute();

        if (cstmt.getInt(1) != 0) {
          cstmt.close();
          throw new ActionException(
              "Failed to perform atomic redo action.");
        }

        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        throw new ActionException(
            "Failed to perform atomic redo action.", action, se);
      } finally {
        try {
          MEMEToolkit.logComment(data_source.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    /**
     * Processes atomic action.
     * @param action the {@link AtomicAction} to perform
     * @throws ActionException if failed to process atomic action
     */
    protected void processAction(AtomicAction action) throws ActionException {
      final String action_name = action.getActionName();

      //
      // Determine which action to call
      //
      try {
        if (action_name.equals("D")) {
          atomicDelete(action);
        }
        if (action_name.equals("T")) {
          atomicChangeTobereleased(action);
        }
        if (action_name.equals("S")) {
          atomicChangeStatus(action);
        }
        if (action_name.equals("I")) {
          atomicInsert( (AtomicInsertAction) action);
        }
        if (action_name.equals("CF")) {
          atomicChangeField(action);
        }
        if (action_name.equals("C")) {
          atomicChangeConceptId(action);
        }
        if (action_name.equals("A")) {
          atomicChangeAtomId(action);
        }
      } catch (MEMEException me) {
        throw new ActionException(
            "Failed to process action.", action, me);
      }
    }

    /**
     * Implements {@link ActionEngine#processAction(MolecularAction)}.
     * @param action the {@link MolecularAction} to perform
     * @throws ActionException if failed to process molecular action
     * @throws IntegrityViolationException if failed due to integrity violation
     * @throws StaleDataException if failed due to process lock
     */
    public void processAction(MolecularAction action) throws ActionException,
        IntegrityViolationException, StaleDataException {

      try {

        //
        // Perform the action
        //
        try {

          //
          // Perform atomic actions in database
          //
          final AtomicAction[] actions = action.getAtomicActions();
          for (int i = 0; i < actions.length; i++) {
            actions[i].setAuthority(action.getAuthority());
            actions[i].setTimestamp(action.getTimestamp());
            actions[i].setParent(action);
            processAction(actions[i]);
          }
        } catch (ActionException ae) {
          rollback(action);
          throw ae;
        }

        //
        // Determine whether or not to recompute preferred atom ids
        //
        boolean should_recompute = false;
        final AtomicAction[] atomic_actions = action.getAtomicActions();
        for (int i = 0; i < atomic_actions.length; i++) {
          if ( (atomic_actions[i].getActionName().equals("T") ||
                atomic_actions[i].getActionName().equals("C") ||
                atomic_actions[i].getActionName().equals("I") ||
                atomic_actions[i].getActionName().equals("D")) &&
              atomic_actions[i].getAffectedTable().equals("C")) {
            should_recompute = true;
          }
        }

        //
        // Recompute preferred atom ids if necessary
        //
        try {
          if (action.getSource() != null && should_recompute) {
            setPreferredAtom(action.getSource());
          }
          if (action.getTarget() != null &&
              should_recompute) {
            setPreferredAtom(action.getTarget());
          }
        } catch (DataSourceException dse) {
          rollback(action);
          throw new ActionException("Failed to set preferred atom.",
                                    action, dse);
        }

        //
        // Assign cuis if necessary
        //
        if (action.getAssignCuis()) {
          data_source.assignCuis(action.getSource(), action.getTarget());

          //
          // If this molecular action is undoing another one
          // Mark the original action as undone.
          //
        }
        if (action.getUndoActionOf() != null) {
          String update_ma =
              "UPDATE molecular_actions SET undone = 'Y', " +
              "undone_by = ?, undone_when = ? " +
              "WHERE molecule_id = ?";
          PreparedStatement pstmt = null;
          try {
            pstmt = data_source.prepareStatement(update_ma);
            pstmt.setString(1, action.getAuthority().toString());
            pstmt.setString(2, dateformat.format(action.getTimestamp()));
            pstmt.setInt(3, action.getUndoActionOf().getIdentifier().intValue());
            pstmt.executeUpdate();
            pstmt.close();
          } catch (SQLException se) {
            try {
              pstmt.close();
            } catch (SQLException se2) {}
            rollback(action);
            ActionException ae = new ActionException(
                "Failed to mark action as undone.", action.getUndoActionOf(),
                se);
            ae.setDetail("molecule_id",
                         new Integer(action.getUndoActionOf().getIdentifier().
                                     intValue()));
            ae.setDetail("authority", action.getAuthority().toString());
            throw ae;
          }

        }
      } catch (IntegrityViolationException ioe) {
        throw ioe;
      } catch (ActionException ac) {
        throw ac;
      } catch (Exception e) {
        rollback(action);
        throw new ActionException("Failed to perform molecular action.", action,
                                  e);
      }

      // Log the action
      long elapsed_time = (new Date().getTime()) -
          action.getTimestamp().getTime();
      action.setElapsedTime(elapsed_time);
      logAction(action);

    }

    /**
     * Rolls back the action, restores autocommit state
     * and handles exceptions.
     * @param action the {@link LoggedAction}
     * @throws ActionException if failed to rollback molecular action
     */
    protected void rollback(LoggedAction action) throws ActionException {
      try {
        data_source.rollback();
        data_source.restoreAutoCommit();
      } catch (DataSourceException dse) {
        throw new ActionException("Failed to restore auto commit.",
                                  action, dse);
      } catch (SQLException se) {
        throw new ActionException("Failed to rollback.", action, se);
      }
    }

    /*
     * PROCESS MOLECULAR TRANSACTION
     */

    /**
         * Implements {@link ActionEngine#processAction(BatchMolecularTransaction)}.
     * @param transaction the {@link BatchMolecularTransaction} to perform
     * @throws ActionException if failed to process batch molecular transaction
     */
    public void processAction(BatchMolecularTransaction transaction) throws
        ActionException {
      try {

        //
        // Handle Insert actions
        //
        if (transaction.getActionName().equals("I")) {

          //
          // Set the id for this transaction
          //
          int transaction_id = 0;
          try {
            transaction_id = data_source.getNextIdentifierForType(
                MolecularTransaction.class).intValue();
          } catch (DataSourceException dse) {
            ActionException ae =
                new ActionException(
                "Failed to perform process action.", transaction, dse);
            throw ae;
          }
          transaction.setIdentifier(new Identifier.Default(transaction_id));

          //
          // Process batch insert action
          //
          if (transaction.getCoreDataType().equals("C")) {
            processBatchInsertAtom(transaction);
          } else if (transaction.getCoreDataType().equals("R")) {
            processBatchInsertRelationship(transaction);
          } else if (transaction.getCoreDataType().equals("A")) {
            processBatchInsertAttribute(transaction);
          }

        }

        //
        // Handle batch concept approval
        //
        else if (transaction.getActionName().equals("AC")) {

          //
          // Set the id for this transaction
          //
          int transaction_id = 0;
          try {
            transaction_id = data_source.getNextIdentifierForType(
                MolecularTransaction.class).intValue();
          } catch (DataSourceException dse) {
            ActionException ae = new ActionException(
                "Failed to perform process action.", transaction, dse);
            throw ae;
          }
          transaction.setIdentifier(new Identifier.Default(transaction_id));

          //
          // Process batch approve concept
          //
          processBatchApproveConcept(transaction);

        }

        //
        // Handle all other action cases
        //
        else {
          final String batch_action =
              "{? = call MEME_BATCH_ACTIONS.batch_action("
              + "action => ?, "
              + "id_type => ?, "
              + "authority => ?, "
              + "table_name => ?, "
              + "work_id => ?, "
              + "status => ?, "
              + "new_value => ?, "
              + "set_preferred_flag => ?, "
              + "action_field => ?)}";
          CallableStatement cstmt = null;
          try {
            cstmt = data_source.prepareCall(batch_action);
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setString(2, transaction.getActionName() == null ? "" :
                            transaction.getActionName());
            cstmt.setString(3, transaction.getCoreDataType() == null ? "" :
                            transaction.getCoreDataType());
            cstmt.setString(4, transaction.getAuthority() == null ? "" :
                            transaction.getAuthority().toString());
            cstmt.setString(5, transaction.getTableName() == null ? "" :
                            transaction.getTableName());
            cstmt.setInt(6, transaction.getWorkIdentifier() == null ? 0 :
                         transaction.getWorkIdentifier().intValue());
            cstmt.setString(7, String.valueOf(transaction.getStatus()));
            cstmt.setString(8, transaction.getNewValue() == null ? "" :
                            transaction.getNewValue());
            cstmt.setString(9, transaction.getRankFlag() ? "Y" : "N");
            cstmt.setString(10, transaction.getActionField() == null ? "" :
                            transaction.getActionField());
            cstmt.execute();
            int transaction_id = cstmt.getInt(1);

            if (transaction_id < 0) {
              cstmt.close();
              ActionException ae = new ActionException("Batch action failed.");
              throw ae;
            }

            transaction.setIdentifier(new Identifier.Default(transaction_id));
            cstmt.close();

          } catch (SQLException se) {
            try {
              cstmt.close();
            } catch (SQLException e) {}
            ActionException ae = new ActionException(
                "Failed to perform process action.", transaction, se);
            throw ae;

          } finally {
            try {
              MEMEToolkit.logComment(data_source.flushBuffer());
            } catch (DataSourceException dse) {}
          }
        }
      } catch (ActionException ae) {
        throw ae;
      } catch (Exception e) {
        ActionException ae = new ActionException(
            "Failed to perform batch action.", transaction, e);
        throw ae;
      }

      //
      // Log the action if not AC action
      //
      if(!transaction.getActionName().equals("AC")) {
        long elapsed_time = (new Date().getTime()) -
            transaction.getTimestamp().getTime();
        transaction.setElapsedTime(elapsed_time);
        logAction(transaction);
      }
    }

    /**
     * Implements {@link ActionEngine#processAction(MacroMolecularAction)}.
     * @param action the {@link MacroMolecularAction} to perform
     * @throws ActionException if failed to process macro molecular action
     */
    public void processAction(MacroMolecularAction action) throws
        ActionException {
      try {
        if (action.getActionName().equals("I")) {

          //
          // We process a macro insert atom like this because
          // we do not have LUI,SUI assignments yet.  All other data
          // types can be processed simply by calling the corresponding MBE method
          //
          if (action.getCoreDataType().equals("C")) {
            processMacroInsertAtom(action);
          }

        } else if (action.getActionName().equals("AC")) {
          throw new ActionException("This action is illegal");

        }

        //
        // Handle other cases
        //
        else {

          final String macro_action =
              "{? = call MEME_BATCH_ACTIONS.macro_action("
              + "action => ?, "
              + "id_type => ?, "
              + "authority => ?, "
              + "table_name => ?, "
              + "work_id => ?, "
              + "status => ?, "
              + "new_value => ?, "
              + "set_preferred_flag => ?, "
              + "action_field => ?)}";
          CallableStatement cstmt = null;
          try {
            cstmt = data_source.prepareCall(macro_action);
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setString(2, action.getActionName() == null ? "" :
                            action.getActionName());
            cstmt.setString(3, action.getCoreDataType() == null ? "" :
                            action.getCoreDataType());
            cstmt.setString(4, action.getAuthority() == null ? "" :
                            action.getAuthority().toString());
            cstmt.setString(5, action.getTableName() == null ? "" :
                            action.getTableName());
            cstmt.setInt(6, action.getWorkIdentifier() == null ? 0 :
                         action.getWorkIdentifier().intValue());
            cstmt.setString(7, String.valueOf(action.getStatus()));
            cstmt.setString(8, action.getNewValue() == null ? "" :
                            action.getNewValue());
            cstmt.setString(9, action.getRankFlag() ? "Y" : "N");
            cstmt.setString(10, action.getActionField() == null ? "" :
                            action.getActionField());
            cstmt.execute();
            int transaction_id = cstmt.getInt(1);

            if (transaction_id < 0) {
              cstmt.close();
              ActionException ae = new ActionException("Macro action failed.");
              throw ae;
            }
            action.setTransactionIdentifier(transaction_id);
            cstmt.close();
            MEMEToolkit.trace(
                "ActionEngine.processAction(MacroMolecularAction)... completed.");

          } catch (SQLException se) {
            try {
              cstmt.close();
            } catch (SQLException e) {}
            ActionException ae = new ActionException(
                "Failed to perform process action.", action, se);
            throw ae;

          } finally {
            try {
              MEMEToolkit.logComment(data_source.flushBuffer());
            } catch (DataSourceException dse) {}
          }
        }
      } catch (ActionException ae) {
        throw ae;
      } catch (Exception e) {
        ActionException ae = new ActionException(
            "Failed to perform macro action.", action, e);
        throw ae;
      }

      //
      // Log the action
      //
      long elapsed_time = (new Date().getTime()) -
          action.getTimestamp().getTime();
      action.setElapsedTime(elapsed_time);
      logAction(action);

    }

    /**
     * Implements {@link ActionEngine#processAction(MEMEDataSourceAction)}.
     * @param action the {@link MEMEDataSourceAction} to perform
     * @throws ActionException if failed to process MEME data source action
     * @throws DataSourceException if failed to process action
     */
    public void processAction(MEMEDataSourceAction action) throws
        ActionException, DataSourceException {

      //
      // perform action
      //
      try {
        if (action.getAuthority() == null) {
          action.setAuthority(data_source.getAuthority("MTH"));
        }
        action.performAction(data_source);
      } catch (Exception e) {
        rollback(action);
        throw new ActionException(
            "Failed to perform MEME Data Source action", action, e);
      }

      //
      // Log the action
      //
      action.setElapsedTime(new Date().getTime() -
                            action.getTimestamp().getTime());
      logAction(action);

    }

    /**
     * Implements {@link ActionEngine#logAction(LoggedAction)}.
     * @param la the {@link LoggedAction}
     * @throws ActionException if failed to add the event
     */
    public void logAction(LoggedAction la) throws ActionException {
      try {

        if (!la.isImplied() &&
            !data_source.getSystemStatus("log_actions").equals("OFF")) {

          //
          // Insert into action_log
          //
          final String insert =
              "INSERT INTO action_log " +
              " (action_id,transaction_id, work_id, undo_action_id, " +
              "  elapsed_time, action, synchronize, authority, " +
              "  timestamp, document) " +
              " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, empty_clob()) ";
          PreparedStatement pstmt = null;
          pstmt = data_source.prepareStatement(insert);
          int action_id = data_source.getNextIdentifierForType(LoggedAction.class).
              intValue();

          // For actions that do not already have other identifiers
          // this is their identifier
          if (la.getIdentifier() == null) {
            la.setIdentifier(action_id);
          }
          int transaction_id = 0; // get id
          int work_id = 0; // get id
          LoggedAction mt = null;

          //
          // If parent is a transaction, get transaction id
          //
          if ( (la.getParent() != null) &&
              (la.getParent()instanceof Activity ||
               la.getParent()instanceof MolecularTransaction)) {
            mt = la.getParent();
            transaction_id = mt.getIdentifier().intValue();
            if (mt.getParent() != null && mt.getParent()instanceof WorkLog) {
              work_id = mt.getParent().getIdentifier().intValue();
            }
          }

          //
          // If parent is a work log, get work id.
          //
          if (la.getParent() != null && la.getParent()instanceof WorkLog) {
            work_id = la.getParent().getIdentifier().intValue();

            //
            // If action is a transaction, get transaction id
            //
          }
          if (la instanceof MolecularTransaction) {
            transaction_id = la.getIdentifier().intValue();

          }
          pstmt.setInt(1, action_id);
          pstmt.setInt(2, transaction_id);
          pstmt.setInt(3, work_id);
          pstmt.setInt(4, 0);
          pstmt.setLong(5, la.getElapsedTime());
          pstmt.setString(6, la.getActionName());
          pstmt.setString(7, "Y");
          pstmt.setString(8,
                          (la.getAuthority() == null) ? "" :
                          la.getAuthority().toString());
          pstmt.setTimestamp(9,
                             new java.sql.Timestamp(la.getTimestamp().getTime()));
          pstmt.executeUpdate();
          final String query =
              "SELECT document FROM action_log WHERE action_id = "
              + action_id
              + "FOR UPDATE";
          pstmt.close();
          final Statement stmt = data_source.createStatement();
          final ResultSet rset = stmt.executeQuery(query);
          rset.next();
          final CLOB clob = (CLOB) rset.getObject("document");

          //
          // Serialize action (without parent info and undo action of)
          //
          final LoggedAction parent = la.getParent();
          la.setParent(null);
          //final LoggedAction undo_of = la.getUndoActionOf();
          //la.setUndoActionOf(null);
          final ObjectXMLSerializer serializer = new ObjectXMLSerializer();
          data_source.setClob(clob, serializer.toXML(la));
          la.setParent(parent);
          //la.setUndoActionOf(undo_of);
          stmt.close();
        }
      } catch (Exception e) {
        rollback(la);
        ActionException ae = new ActionException(
            "Failed to add action to log.");
        ae.setDetail("action", la.getActionName());
        ae.setDetail("type", la.getClass().getName());
        ae.setDetail("orig_message", e.getMessage());
        throw ae;
      }

      //
      // If this molecular action is undoing another one
      // Mark the original action as undone.
      //
      if (la.getUndoActionOf() != null) {
        final String update_log =
            "UPDATE action_log SET undo_action_id = ? " +
            "WHERE action_id = ?";
        PreparedStatement pstmt = null;
        try {
          pstmt = data_source.prepareStatement(update_log);
          pstmt.setInt(1, la.getIdentifier().intValue());
          pstmt.setInt(2, la.getUndoActionOf().getIdentifier().intValue());
          pstmt.executeUpdate();
          pstmt.close();
        } catch (SQLException se) {
          try {
            pstmt.close();
          } catch (SQLException se2) {}
          rollback(la);
          ActionException ae = new ActionException(
              "Failed to mark action as undone.", la.getUndoActionOf(), se);
          ae.setDetail("action_id",
                       new Integer(la.getUndoActionOf().getIdentifier().
                                   intValue()));
          ae.setDetail("authority", la.getAuthority().toString());
          throw ae;
        }

      }

      //
      // commit
      //
      try {
        data_source.commit();
      } catch (SQLException se) {
        rollback(la);
        throw new ActionException("Failed to commit.", la, se);
      }

    }

    //
    // Other private and protected methods
    //


    /**
     * Drops the table with name table_name
     * @param table_name the {@link String} representation of table name
     * @throws SQLException if failed to drop the table
     */
    protected void dropTable(String table_name) throws SQLException {
      final StringBuffer call = new StringBuffer();
      call.append("{call MEME_UTILITY.drop_it(type => 'table', name => '");
      call.append(table_name);
      call.append("')}");
      final CallableStatement drop = data_source.prepareCall(call.toString());
      drop.execute();
      drop.close();
    }

    /**
     * Truncates the table with name table_name.
     * @param table_name the {@link String} representation of table name
     * @throws SQLException if failed to truncate the table
     */
    protected void truncateTable(String table_name) throws SQLException {
      CallableStatement cstmt = null;
      try {
        cstmt = data_source.prepareCall("{call MEME_SYSTEM.truncate('" +
                                        table_name + "')}");
        cstmt.execute();
        cstmt.close();
      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException se2) {}
        throw se;
      }
    }


  } // end inner class

}
