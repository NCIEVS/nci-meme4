/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  AuxiliaryDataService
 *
 * 06/19/2006 RBE (1-BIC23) : Bug fixes
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import java.util.HashMap;

import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.ApplicationVectorAction;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.EditorPreferencesAction;
import gov.nih.nlm.meme.action.IntegrityCheckAction;
import gov.nih.nlm.meme.action.LanguageAction;
import gov.nih.nlm.meme.action.MetaCodeAction;
import gov.nih.nlm.meme.action.MetaPropertyAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.NextIdentifierAction;
import gov.nih.nlm.meme.action.OverrideVectorAction;
import gov.nih.nlm.meme.action.SemanticTypeAction;
import gov.nih.nlm.meme.action.SourceAction;
import gov.nih.nlm.meme.action.TermgroupAction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.ISUI;
import gov.nih.nlm.meme.common.LUI;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SUI;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MIDActionEngine;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles requests for auxiliary data.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class AuxiliaryDataService implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}.
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    //
    // Get Service Request and function parameter
    //
    MEMEServiceRequest request = context.getServiceRequest();
    String function = (String) request.getParameter("function").getValue();
    MEMEDataSource data_source = context.getDataSource();

    //
    // Edit source data
    //
    if (function.equals("manage_source")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        SourceAction sa = null;
        if (command.equals("ADDS")) {
          sa = SourceAction.newAddSourcesAction(
              (Source[]) request.getParameter("param").getValue());
        } else {
          Source source = (Source) request.getParameter("param").getValue();
          if (command.equals("ADD")) {
            sa = SourceAction.newAddSourceAction(source);
          } else if (command.equals("REMOVE")) {
            sa = SourceAction.newRemoveSourceAction(source);
          } else if (command.equals("SET")) {
            sa = SourceAction.newSetSourceAction(source);
          }
        }

        data_source.getActionEngine().processAction(sa);

      }
    }

    //
    // Return source data
    //
    if (function.equals("get_sources")) {
      request.addReturnValue(new Parameter.Default("sources",
          data_source.getSources()));
    }

    //
    // Edit termgroup data
    //
    if (function.equals("manage_termgroup")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        TermgroupAction ta = null;
        if (command.equals("ADDS")) {
          ta = TermgroupAction.newAddTermgroupsAction(
              (Termgroup[]) request.getParameter("param").getValue());
        } else {
          Termgroup termgroup = (Termgroup) request.getParameter("param").
              getValue();
          if (command.equals("ADD")) {
            ta = TermgroupAction.newAddTermgroupAction(termgroup);
          } else if (command.equals("REMOVE")) {
            ta = TermgroupAction.newRemoveTermgroupAction(termgroup);
          } else if (command.equals("SET")) {
            ta = TermgroupAction.newSetTermgroupAction(termgroup);
          }
        }

        data_source.getActionEngine().processAction(ta);

      }
    }

    //
    // Return termgroup data
    //
    if (function.equals("get_termgroups")) {
      request.addReturnValue(new Parameter.Default("termgroups",
          data_source.getTermgroups()));
    }

    //
    // Edit sty data
    //
    if (function.equals("manage_sty")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        SemanticType sty = (SemanticType) request.getParameter("param").
            getValue();

        SemanticTypeAction sta = null;
        if (command.equals("ADD")) {
          sta = SemanticTypeAction.newAddSemanticTypeAction(sty);
        } else if (command.equals("REMOVE")) {
          sta = SemanticTypeAction.newRemoveSemanticTypeAction(sty);

        }
        data_source.getActionEngine().processAction(sta);
        request.addReturnValue(new Parameter.Default("id",
            sty.getTypeIdentifier()));

      }
    }

    //
    // Return STY data
    //
    if (function.equals("get_semantic_types")) {
      request.addReturnValue(new Parameter.Default("semantic_types",
          data_source.getValidSemanticTypes()));
    }

    //
    // Edit language data
    //
    if (function.equals("manage_language")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        Language language =
            (Language) request.getParameter("param").getValue();
        LanguageAction la = null;

        if (command.equals("ADD")) {
          la = LanguageAction.newAddLanguageAction(language);
        } else if (command.equals("REMOVE")) {
          la = LanguageAction.newRemoveLanguageAction(language);
        } else if (command.equals("SET")) {
          la = LanguageAction.newSetLanguageAction(language);

        }
        data_source.getActionEngine().processAction(la);

      }
    }

    //
    // Return language data
    //
    if (function.equals("get_language")) {
      if (request.getParameter("lat") != null) {
        String lat = (String) request.getParameter("lat").getValue();
        request.addReturnValue(new Parameter.Default("language",
            data_source.getLanguage(lat)));
      }
    }

    if (function.equals("get_languages")) {
      request.addReturnValue(new Parameter.Default("languages",
          data_source.getLanguages()));
    }

    //
    // Get max identifier
    //
    if (function.equals("get_max_id")) {
      if (request.getParameter("param") != null) {
        String table = (String) request.getParameter("param").getValue();

        Class c = null;
        if (table.equals(Ticket.class.getName())) {
          c = Ticket.class;
        } else if (table.equals(Atom.class.getName())) {
          c = Atom.class;
        } else if (table.equals(Concept.class.getName())) {
          c = Concept.class;
        } else if (table.equals(Relationship.class.getName())) {
          c = Relationship.class;
        } else if (table.equals(ContextRelationship.class.getName())) {
          c = ContextRelationship.class;
        } else if (table.equals(Attribute.class.getName())) {
          c = Attribute.class;
        } else if (table.equals(MolecularAction.class.getName())) {
          c = MolecularAction.class;
        } else if (table.equals(AtomicAction.class.getName())) {
          c = AtomicAction.class;
        } else if (table.equals(MolecularTransaction.class.getName())) {
          c = MolecularTransaction.class;
        } else if (table.equals(CUI.class.getName())) {
          c = CUI.class;
        } else if (table.equals(LUI.class.getName())) {
          c = LUI.class;
        } else if (table.equals(SUI.class.getName())) {
          c = SUI.class;
        } else if (table.equals(ISUI.class.getName())) {
          c = ISUI.class;
        } else if (table.equals(WorkLog.class.getName())) {
          c = WorkLog.class;
        } else {
          throw new MEMEException("Server: Invalid class name for this method.",
                                  table);
        }

        request.addReturnValue(new Parameter.Default("max_id",
            data_source.getMaxIdentifierForType(c)));
      }
    }

    //
    // Get "next" identifier
    //
    if (function.equals("get_next_id")) {
      if (request.getParameter("param") != null) {
        String table = (String) request.getParameter("param").getValue();

        Class c = null;
        if (table.equals(Ticket.class.getName())) {
          c = Ticket.class;
        } else if (table.equals(Atom.class.getName())) {
          c = Atom.class;
        } else if (table.equals(Concept.class.getName())) {
          c = Concept.class;
        } else if (table.equals(Relationship.class.getName())) {
          c = Relationship.class;
        } else if (table.equals(ContextRelationship.class.getName())) {
          c = ContextRelationship.class;
        } else if (table.equals(Attribute.class.getName())) {
          c = Attribute.class;
        } else if (table.equals(MolecularAction.class.getName())) {
          c = MolecularAction.class;
        } else if (table.equals(AtomicAction.class.getName())) {
          c = AtomicAction.class;
        } else if (table.equals(MolecularTransaction.class.getName())) {
          c = MolecularTransaction.class;
        } else if (table.equals(CUI.class.getName())) {
          c = CUI.class;
        } else if (table.equals(LUI.class.getName())) {
          c = LUI.class;
        } else if (table.equals(SUI.class.getName())) {
          c = SUI.class;
        } else if (table.equals(ISUI.class.getName())) {
          c = ISUI.class;
        } else if (table.equals(WorkLog.class.getName())) {
          c = WorkLog.class;
        } else if (table.equals(MapSet.class.getName())) {
          c = MapSet.class;
        } else {
          throw new MEMEException("Server: Invalid class name for this method.",
                                  table);
        }

        NextIdentifierAction nia =
            NextIdentifierAction.newSetNextIdentifierAction(c);
        data_source.getActionEngine().processAction(nia);
        request.addReturnValue(new Parameter.Default("next_id",
            nia.getNextIdentifier()));
      }
    }

    //
    // Return  work logs
    //
    if (function.equals("get_work_log")) {
      WorkLog wl = null;
      if (request.getParameter("param") != null) {
        int work_id = request.getParameter("param").getInt();
        wl = data_source.getWorkLog(work_id);
      }
      request.addReturnValue(new Parameter.Default("get_work_log", wl));
    }

    if (function.equals("get_work_logs")) {
      request.addReturnValue(new Parameter.Default("get_work_logs",
          (WorkLog[]) data_source.getWorkLogs()));
    }

    //
    // Return activity logs
    //
    if (function.equals("get_activity_log")) {
      Activity activity = null;
      if (request.getParameter("param") != null) {
        int transaction_id = request.getParameter("param").getInt();
        activity = data_source.getActivityLog(new MolecularTransaction(
            transaction_id));
      }
      request.addReturnValue(new Parameter.Default("get_activity_log", activity));
    }

    if (function.equals("get_activity_logs")) {
      Activity[] activities = null;
      if (request.getParameter("param") != null) {
        int work_id = request.getParameter("param").getInt();
        activities = data_source.getActivityLogs(new WorkLog(work_id));
      }
      request.addReturnValue(new Parameter.Default("get_activity_logs",
          activities));
    }

    //
    // Edit meta codes (e.g. code_map data)
    //
    if (function.equals("manage_metacode")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        MetaCode mcode =
            (MetaCode) request.getParameter("param").getValue();
        MetaCodeAction mca = null;

        if (command.equals("ADD")) {
          mca = MetaCodeAction.newAddMetaCodeAction(mcode);
        } else if (command.equals("REMOVE")) {
          mca = MetaCodeAction.newRemoveMetaCodeAction(mcode);

        }
        data_source.getActionEngine().processAction(mca);
        request.addReturnValue(new Parameter.Default("id", mcode.getIdentifier()));

      }
    }

    //
    // Return meta codes (e.g. code_map data)
    //
    if (function.equals("get_metacode")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        String code = (String) request.getParameter("param1").getValue();
        String type = (String) request.getParameter("param2").getValue();
        request.addReturnValue(new Parameter.Default("get_metacode",
            data_source.getMetaCode(code, type)));
      }
    }
    if (function.equals("get_metacodes")) {
      request.addReturnValue(new Parameter.Default("get_metacodes",
          data_source.getMetaCodes()));
    }
    if (function.equals("get_metacode_types")) {
      request.addReturnValue(new Parameter.Default("get_metacode_types",
          data_source.getMetaCodeTypes()));
    }
    if (function.equals("get_metacodes_by_type")) {
      if (request.getParameter("param") != null) {
        String type = (String) request.getParameter("param").getValue();
        request.addReturnValue(new Parameter.Default("get_metacodes_by_type",
            data_source.getMetaCodesByType(type)));
      }
    }

    //
    // Edit meta properties(e.g. meme_properties data)
    //
    if (function.equals("manage_meta_property")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        MetaProperty meta_prop =
            (MetaProperty) request.getParameter("param").getValue();
        MetaPropertyAction mpa = null;

        if (command.equals("ADD")) {
          mpa = MetaPropertyAction.newAddMetaPropertyAction(meta_prop);
        } else if (command.equals("REMOVE")) {
          mpa = MetaPropertyAction.newRemoveMetaPropertyAction(meta_prop);

        }
        data_source.getActionEngine().processAction(mpa);
        request.addReturnValue(new Parameter.Default("id",
            meta_prop.getIdentifier()));

      }
    }

    //
    // Return meta properties(e.g. meme_properties data)
    //
    if (function.equals("get_meta_property")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null &&
          request.getParameter("param3") != null) {
        String key = (String) request.getParameter("param1").getValue();
        String key_qualifier = (String) request.getParameter("param2").getValue();
        String value = (String) request.getParameter("param3").getValue();
        String description = (String) request.getParameter("param4").getValue();//naveen UMLS-60 added description parameter to getMetaProperty method 
        request.addReturnValue(new Parameter.Default("get_meta_property",
            data_source.getMetaProperty(key, key_qualifier, value, description)));
      }
    }
    if (function.equals("get_meta_properties")) {
      request.addReturnValue(new Parameter.Default("get_meta_properties",
          data_source.getMetaProperties()));
    }
    if (function.equals("get_meta_prop_key_qualifiers")) {
      request.addReturnValue(new Parameter.Default(
          "get_meta_prop_key_qualifiers",
          data_source.getMetaPropertyKeyQualifiers()));
    }
    if (function.equals("get_meta_props_by_key_qualifier")) {
      if (request.getParameter("param") != null) {
        String key_qualifier = (String) request.getParameter("param").getValue();
        request.addReturnValue(new Parameter.Default(
            "get_meta_props_by_key_qualifier",
            data_source.getMetaPropertiesByKeyQualifier(key_qualifier)));
      }
    }

    //
    // Get codes and values
    //
    if (function.equals("get_code_by_value")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        String type = (String) request.getParameter("param1").getValue();
        String value = (String) request.getParameter("param2").getValue();
        request.addReturnValue(new Parameter.Default("code_by_value",
            data_source.getCodeByValue(type, value)));
      }
    }

    if (function.equals("get_value_by_code")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        String type = (String) request.getParameter("param1").getValue();
        String code = (String) request.getParameter("param2").getValue();
        request.addReturnValue(new Parameter.Default("value_by_code",
            data_source.getValueByCode(type, code)));
      }
    }

    //
    // Get status values
    //
    if (function.equals("status_for_atoms")) {
      request.addReturnValue(new Parameter.Default("status_for_atoms",
          ( (MIDDataSource) data_source).getValidStatusValuesForAtoms()));
    }
    if (function.equals("status_for_attributes")) {
      request.addReturnValue(new Parameter.Default("status_for_attributes",
          ( (MIDDataSource) data_source).getValidStatusValuesForAttributes()));
    }
    if (function.equals("status_for_concepts")) {
      request.addReturnValue(new Parameter.Default("status_for_concepts",
          ( (MIDDataSource) data_source).getValidStatusValuesForConcepts()));
    }

    if (function.equals("status_for_relationships")) {
      request.addReturnValue(new Parameter.Default("status_for_relationships",
          ( (MIDDataSource) data_source).getValidStatusValuesForRelationships()));
    }

    //
    // Get level values
    //
    if (function.equals("level_for_attributes")) {
      request.addReturnValue(new Parameter.Default("level_for_attributes",
          ( (MIDDataSource) data_source).getValidLevelValuesForAttributes()));
    }
    if (function.equals("level_for_relationships")) {
      request.addReturnValue(new Parameter.Default("level_for_relationships",
          ( (MIDDataSource) data_source).getValidLevelValuesForRelationships()));
    }

    //
    // get "released" values
    //
    if (function.equals("valid_released")) {
      request.addReturnValue(new Parameter.Default("valid_released",
          ( (MIDDataSource) data_source).getValidReleasedValues()));
    }

    //
    // get "tobereleased" values
    //
    if (function.equals("valid_tobereleased")) {
      request.addReturnValue(new Parameter.Default("valid_tobereleased",
          ( (MIDDataSource) data_source).getValidTobereleasedValues()));
    }

    //
    // Edit editor preferences
    //
    if (function.equals("manage_ep")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        EditorPreferences ep =
            (EditorPreferences) request.getParameter("param").getValue();
        EditorPreferencesAction epa = null;

        if (command.equals("ADD")) {
          epa = EditorPreferencesAction.newAddEditorPreferencesAction(ep);
        } else if (command.equals("REMOVE")) {
          epa = EditorPreferencesAction.newRemoveEditorPreferencesAction(ep);
        } else if (command.equals("SET")) {
          epa = EditorPreferencesAction.newSetEditorPreferencesAction(ep);

        }
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(epa);
      }
    }

    //
    // Return editor preferences
    //
    if (function.equals("get_editor_preferences")) {
      request.addReturnValue(new Parameter.Default("editor_preferences",
          ( (MIDDataSource) data_source).getEditorPreferences()));
    }

    //
    // Return rel,rela inverses
    //
    if (function.equals("inverse_rel_name")) {
    	request.addReturnValue(new Parameter.Default("inverse_rel_name",
          new HashMap(data_source.getInverseRelationshipNameMap())));   	    
    }
    if (function.equals("inverse_rel_attr")) {
    	request.addReturnValue(new Parameter.Default("inverse_rel_attr",
          new HashMap(data_source.getInverseRelationshipAttributeMap())));   	    
    }

    //
    // Return an authority
    //
    if (function.equals("get_authority")) {
      Authority authority = null;
      if (request.getParameter("param") != null) {
        String auth = (String) request.getParameter("param").getValue();
        authority = data_source.getAuthority(auth);
      }
      request.addReturnValue(new Parameter.Default("authority", authority));
    }

    //
    // Return molecular actions
    //
    if (function.equals("get_molecular_action")) {
      MolecularAction ma = null;
      if (request.getParameter("param") != null) {
        int molecule_id = request.getParameter("param").getInt();
        ma = data_source.getMolecularAction(molecule_id);
      }
      request.addReturnValue(new Parameter.Default("molecular_action", ma));
    }
    if (function.equals("get_full_molecular_action")) {
      MolecularAction ma = null;
      if (request.getParameter("param") != null) {
        int molecule_id = request.getParameter("param").getInt();
        ma = data_source.getFullMolecularAction(molecule_id);
      }
      request.addReturnValue(new Parameter.Default("full_molecular_action", ma));
    }

    //
    // Return atomic actions
    //
    if (function.equals("get_atomic_action")) {
      AtomicAction aa = null;
      if (request.getParameter("param") != null) {
        int atomic_action_id = request.getParameter("param").getInt();
        aa = data_source.getAtomicAction(atomic_action_id);
      }
      request.addReturnValue(new Parameter.Default("atomic_action", aa));
    }

    //
    // Edit integrity vectors
    //
    if (function.equals("add_application_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        String application = (String) request.getParameter("param1").getValue();
        IntegrityVector iv = (IntegrityVector) request.getParameter("param2").
            getValue();
        ApplicationVectorAction iva =
            ApplicationVectorAction.newAddApplicationVectorAction(application,
            iv);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(iva);
      }
    }
    if (function.equals("add_check_to_application_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null &&
          request.getParameter("param3") != null) {
        String application = (String) request.getParameter("param1").getValue();
        IntegrityCheck ic = (IntegrityCheck) request.getParameter("param2").
            getValue();
        String code = (String) request.getParameter("param3").getValue();
        ApplicationVectorAction iva =
            ApplicationVectorAction.newAddCheckToApplicationVectorAction(
            application, ic, code);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(iva);
      }
    }
    if (function.equals("set_application_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        String application = (String) request.getParameter("param1").getValue();
        IntegrityVector iv = (IntegrityVector) request.getParameter("param2").
            getValue();
        ApplicationVectorAction iva =
            ApplicationVectorAction.newSetApplicationVectorAction(application,
            iv);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(iva);
      }
    }
    if (function.equals("remove_application_vector")) {
      if (request.getParameter("param") != null) {
        String application = (String) request.getParameter("param").getValue();
        ApplicationVectorAction iva =
            ApplicationVectorAction.newRemoveApplicationVectorAction(
            application);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(iva);
      }
    }
    if (function.equals("remove_check_from_application_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        String application = (String) request.getParameter("param1").getValue();
        IntegrityCheck ic = (IntegrityCheck) request.getParameter("param2").
            getValue();
        ApplicationVectorAction iva =
            ApplicationVectorAction.newRemoveCheckFromApplicationVectorAction(
            application, ic);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(iva);
      }
    }

    //
    // Return integrity vectors
    //
    if (function.equals("get_application_vector")) {
      EnforcableIntegrityVector eiv = null;
      if (request.getParameter("param") != null) {
        String app = (String) request.getParameter("param").getValue();
        eiv = ( (MIDDataSource) data_source).getApplicationVector(app);
      }
      request.addReturnValue(new Parameter.Default("application_vector", eiv));
    }
    if (function.equals("get_applications_with_vectors")) {
      String[] apps = ( (MIDDataSource) data_source).getApplicationsWithVectors();
      request.addReturnValue(new Parameter.Default("applications_with_vectors",
          apps));
    }

    //
    // Edit override vectors
    //
    if (function.equals("add_override_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        int ic_level = request.getParameter("param1").getInt();
        IntegrityVector iv = (IntegrityVector) request.getParameter("param2").
            getValue();
        OverrideVectorAction ova =
            OverrideVectorAction.newAddOverrideVectorAction(ic_level, iv);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ova);
      }
    }
    if (function.equals("add_check_to_override_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null &&
          request.getParameter("param3") != null) {
        int ic_level = request.getParameter("param1").getInt();
        IntegrityCheck ic = (IntegrityCheck) request.getParameter("param2").
            getValue();
        String code = (String) request.getParameter("param3").getValue();
        OverrideVectorAction ova =
            OverrideVectorAction.newAddCheckToOverrideVectorAction(ic_level, ic,
            code);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ova);
      }
    }
    if (function.equals("set_override_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        int ic_level = request.getParameter("param1").getInt();
        IntegrityVector iv = (IntegrityVector) request.getParameter("param2").
            getValue();
        OverrideVectorAction ova =
            OverrideVectorAction.newSetOverrideVectorAction(ic_level, iv);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ova);
      }
    }
    if (function.equals("remove_override_vector")) {
      if (request.getParameter("param") != null) {
        int ic_level = request.getParameter("param").getInt();
        OverrideVectorAction ova =
            OverrideVectorAction.newRemoveOverrideVectorAction(ic_level);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ova);
      }
    }
    if (function.equals("remove_check_from_override_vector")) {
      if (request.getParameter("param1") != null &&
          request.getParameter("param2") != null) {
        int ic_level = request.getParameter("param1").getInt();
        IntegrityCheck ic = (IntegrityCheck) request.getParameter("param2").
            getValue();
        OverrideVectorAction ova =
            OverrideVectorAction.newRemoveCheckFromOverrideVectorAction(
            ic_level, ic);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ova);
      }
    }

    //
    // Return override vector data
    //
    if (function.equals("get_override_vector")) {
      IntegrityVector iv = null;
      if (request.getParameter("param") != null) {
        int editor_level = request.getParameter("param").getInt();
        iv = ( (MIDDataSource) data_source).getOverrideVector(editor_level);
      }
      request.addReturnValue(new Parameter.Default("override_vector", iv));
    }
    if (function.equals("get_levels_with_override_vectors")) {
      int[] levels = ( (MIDDataSource) data_source).
          getLevelsWithOverrideVectors();
      request.addReturnValue(new Parameter.Default(
          "levels_with_override_vectors", levels));
    }

    //
    // Edit integrity checks
    //
    if (function.equals("manage_ic")) {
      if (request.getParameter("param") != null ||
          request.getParameter("command") != null) {

        String command = (String) request.getParameter("command").getValue();
        IntegrityCheck ic = (IntegrityCheck) request.getParameter("param").
            getValue();

        IntegrityCheckAction ica = null;
        if (command.equals("ADD")) {
          ica = IntegrityCheckAction.newAddIntegrityCheckAction(ic);
        } else if (command.equals("REMOVE")) {
          ica = IntegrityCheckAction.newRemoveIntegrityCheckAction(ic);
        } else if (command.equals("SET")) {
          ica = IntegrityCheckAction.newSetIntegrityCheckAction(ic);
        } else if (command.equals("ACTIVATE")) {
          ica = IntegrityCheckAction.newSetActivateIntegrityCheckAction(ic);
        } else if (command.equals("DEACTIVATE")) {
          ica = IntegrityCheckAction.newSetDeactivateIntegrityCheckAction(ic);

          // Process action
        }
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ica);
      }
    }

    //
    // Return integrity check data
    //
    if (function.equals("get_integrity_check")) {
      IntegrityCheck ic = null;
      if (request.getParameter("param") != null) {
        String ic_name = (String) request.getParameter("param").getValue();
        ic = ( (MIDDataSource) data_source).getIntegrityCheck(ic_name);
      }
      request.addReturnValue(new Parameter.Default("integrity_check", ic));
    }
    if (function.equals("get_integrity_checks")) {
      IntegrityCheck[] checks = ( (MIDDataSource) data_source).
          getIntegrityChecks();
      request.addReturnValue(new Parameter.Default("integrity_checks", checks));
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
   * @param context the {@link SessionContext}
   * @return <code>false</code>
   */
  public boolean isReEntrant(SessionContext context) {
    return false;
  }

}
