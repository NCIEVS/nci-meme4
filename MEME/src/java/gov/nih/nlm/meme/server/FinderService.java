/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.serverall
 * Object:  FinderService
 * Version Information
 * 03/06/2007 3.12.4 SL (1-DNO15) : Adding a new function find_concepts_by_ndc to retrieve the NDC Concepts
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Handles requests for finding data.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class FinderService implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Returns a concept list from the {@link Iterator} with
   * the specified number of elements.
   * @param iterator a {@link Concept} {@link Iterator}.
   * @param max_result_count the max number of results to appear in the list
   * @return a concept list from the {@link Iterator}
   */
  private List getConceptList(Iterator iterator, int max_result_count) {
    final List list = new ArrayList();
    int ctr = 0;
    while (iterator.hasNext()) {
      if (++ctr > max_result_count) {
        iterator.remove();
        break;
      }
      final Concept c = (Concept) iterator.next();
      c.clearAtoms();
      if (c.getPreferredAtom() == null) {
        Atom a = new Atom.Default();
        a.setString("Missing Preferred Name");
        c.setPreferredAtom(a);
      }
      c.getPreferredAtom().setConcept(c);
      list.add(c);
    }
    return list;
  }

  /**
   * Receives requests from the {@link MEMEApplicationServer}
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    // Get Service Request and function parameter
    final MEMEServiceRequest request = context.getServiceRequest();
    final MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    final String function = (String) request.getParameter("function").getValue();
    final int max_result_count = request.getParameter("max_result_count").
        getInt();

    // Define common variables
    final List sps = new ArrayList();
    Iterator iterator = null;

    // Find Molecular Actions
    if (function.equals("find_molecular_actions")) {

      int concept_id = request.getParameter("concept_id").getInt();
      if (concept_id > 0) {
        sps.add(new SearchParameter.Single("concept_id",
                                           new Identifier.Default(concept_id)));

      }
      String worklist = (String) request.getParameter("worklist").getValue();
      if (worklist != null) {
        sps.add(new SearchParameter.Single("worklist", worklist));

      }
      String core_table = (String) request.getParameter("core_table").getValue();
      if (core_table != null) {
        sps.add(new SearchParameter.Single("core_table", core_table));

      }
      int transaction_id = request.getParameter("transaction_id").getInt();
      if (transaction_id > 0) {
        sps.add(new SearchParameter.Single("transaction_id",
                                           new Identifier.Default(
            transaction_id)));

      }
      String molecular_action =
          (String) request.getParameter("molecular_action").getValue();
      if (molecular_action != null) {
        sps.add(new SearchParameter.Single("molecular_action", molecular_action));

      }
      Authority authority =
          (Authority) request.getParameter("authority").getValue();
      if (authority != null) {
        sps.add(new SearchParameter.Single("authority", authority.toString()));

      }
      Date start_date = (Date) request.getParameter("start_date").getValue();
      Date end_date = (Date) request.getParameter("end_date").getValue();
      if (start_date != null || end_date != null) {
        Identifier start_range = null;
        if (start_date != null) {
          start_range = new Identifier.Default(
              MEMEToolkit.getDateFormat().format(start_date));
        }
        Identifier end_range = null;
        if (end_date != null) {
          end_range = new Identifier.Default(
              MEMEToolkit.getDateFormat().format(end_date));
        }
        sps.add(new SearchParameter.Range("timestamp", start_range, end_range));
      }

      int rownum = request.getParameter("max_result_count").getInt();
      if (rownum > 0) {
        sps.add(new SearchParameter.Single("rownum",
                                           new Identifier.Default(rownum)));

      }
      boolean recursive = request.getParameter("recursive").getBoolean();
      if (recursive) {
        sps.add(new SearchParameter.Single("recursive",
                                           new Identifier.Default(String.
            valueOf(recursive))));

      }
      ArrayList actions = new ArrayList();

      iterator = data_source.findMolecularActions(
          (SearchParameter[]) sps.toArray(new SearchParameter[0]));

      int ctr = 0;
      while (iterator.hasNext()) {
        if (max_result_count != -1 && ++ctr > max_result_count) {
          iterator.remove();
          break;
        }
        MolecularAction ma = (MolecularAction) iterator.next();
        ma.clearSubActions();
        actions.add(ma);
      }

      // Return value
      request.addReturnValue(
          new Parameter.Default("find_molecular_actions",
                                (MolecularAction[]) actions.toArray(new
          MolecularAction[] {})));

    } else {

      // Other function

      SemanticType[] stys =
          (SemanticType[]) request.getParameter("semantic_types").getValue();
      Source[] sources =
          (Source[]) request.getParameter("sources").getValue();
      boolean releasable = true;
      if (request.getParameter("releasable") != null) {
        releasable = request.getParameter("releasable").getBoolean();
      }
      boolean chemical =
          request.getParameter("chemical").getBoolean();
      boolean non_chemical =
          request.getParameter("non_chemical").getBoolean();

      if (stys != null) {
        String[] ids = new String[stys.length];
        for (int i = 0; i < stys.length; i++) {
          ids[i] = stys[i].getValue();
        }
        sps.add(new SearchParameter.Multiple("semantic_type", ids));
      }

      if (sources != null) {
        String[] srcs = new String[sources.length];
        for (int i = 0; i < sources.length; i++) {
          srcs[i] = sources[i].getSourceAbbreviation();
        }
        sps.add(new SearchParameter.Multiple("source", srcs));
      }

      if (releasable) {
        sps.add(new SearchParameter.Single("tobereleased", "Y"));

      }
      if (chemical) {
        sps.add(new SearchParameter.Single("chemical_semantic_type", "1"));

      }
      if (non_chemical) {
        sps.add(new SearchParameter.Single("non_chemical_semantic_type", "1"));

      }
      Ticket ticket = Ticket.getEmptyTicket();
      ticket.setReadConcept(true);
      ticket.setReadAtoms(true);
      ticket.setReadAtomNames(true);
      ticket.setCalculatePreferredAtom(true);

      if (function.equals("find_concepts_by_code")) {
        Code code = (Code) request.getParameter("code").getValue();
        if (code != null) {
          SearchParameter sp =
              new SearchParameter.Single("code", code.toString());
          sps.add(sp);

          iterator = data_source.findConceptsFromString(
              (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

          List concepts = getConceptList(iterator, max_result_count);

          // Return value
          request.addReturnValue(
              new Parameter.Default("concepts_by_code",
                                    (Concept[]) concepts.toArray(new Concept[] {})));
        }
         }else if (function.equals("find_concepts_by_ndc")) {
          Code code = (Code) request.getParameter("code").getValue();
          if (code != null) {
        	 // First the get first character. if it started by 0 then create a approximate attribute_value search parameter
        	  //character 
        	 
            SearchParameter sp = new SearchParameter.Single("code", code.toString());
            
            sps.add(sp);

            iterator = data_source.findNDCConceptsFromCode(code.toString(),ticket);

            List concepts = getConceptList(iterator, max_result_count);

            // Return value
            request.addReturnValue(
                new Parameter.Default("concepts_by_ndc",
                                      (Concept[]) concepts.toArray(new Concept[] {})));
          }
     }else if (function.equals("exact_string")) {
        String string = (String) request.getParameter("string").getValue();
        if (string != null) {
          SearchParameter sp =
              new SearchParameter.Single("lowercase_string", string.toLowerCase());
          sps.add(sp);

          iterator = data_source.findConceptsFromString(
              (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

          List concepts = getConceptList(iterator, max_result_count);

          // Return value
          request.addReturnValue(
              new Parameter.Default("exact_string",
                                    (Concept[]) concepts.toArray(new Concept[] {})));
        }

      } else if (function.equals("norm_string")) {
        String string = (String) request.getParameter("string").getValue();
        if (string != null) {
          String norm_string = MIDServices.getLuiNormalizedString(string);
          if (norm_string != null) {
            SearchParameter sp =
                new SearchParameter.Single("norm_string", norm_string);
            sps.add(sp);

            iterator = data_source.findConceptsFromString(
                (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

            List concepts = getConceptList(iterator, max_result_count);

            // Return value
            request.addReturnValue(
                new Parameter.Default("norm_string",
                                      (Concept[]) concepts.toArray(new Concept[] {})));
          }
        }

      } else if (function.equals("all_word")) {
        String[] words = (String[]) request.getParameter("words").getValue();
        if (words.length > 0) {
          SearchParameter sp = new SearchParameter.Multiple("words", words);
          sp.setAttribute("any_or_all", "all");
          sps.add(sp);

          iterator = data_source.findConceptsFromWords(
              (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

          List concepts = getConceptList(iterator, max_result_count);

          // Return value
          request.addReturnValue(
              new Parameter.Default("all_word",
                                    (Concept[]) concepts.toArray(new Concept[] {})));
        }

      } else if (function.equals("all_norm_word")) {

        final String[] norm_words =
            (String[]) request.getParameter("norm_words").getValue();

        final List param_norm_words = new ArrayList();
        for (int i = 0; i < norm_words.length; i++) {
          final String norm_word = MIDServices.getLuiNormalizedString(
              norm_words[i]);
          if (!norm_word.equals("")) {
            param_norm_words.add(norm_word);
          }
        }
        if (param_norm_words.size() == 0) {
          param_norm_words.add("");

        }
        if (norm_words.length > 0) {
          SearchParameter sp =
              new SearchParameter.Multiple(
              "norm_words", (String[]) param_norm_words.toArray(new String[0]));
          sp.setAttribute("any_or_all", "all");
          sps.add(sp);

          iterator = data_source.findConceptsFromWords(
              (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

          List concepts = getConceptList(iterator, max_result_count);

          // Return value
          request.addReturnValue(
              new Parameter.Default("all_norm_word",
                                    (Concept[]) concepts.toArray(new Concept[] {})));
        }

      } else if (function.equals("any_word")) {
        String[] words = (String[]) request.getParameter("words").getValue();
        if (words.length > 0) {
          SearchParameter sp = new SearchParameter.Multiple("words", words);
          sp.setAttribute("any_or_all", "any");
          sps.add(sp);

          iterator = data_source.findConceptsFromWords(
              (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

          List concepts = getConceptList(iterator, max_result_count);

          // Return value
          request.addReturnValue(
              new Parameter.Default("any_word",
                                    (Concept[]) concepts.toArray(new Concept[] {})));
        }

      } else if (function.equals("any_norm_word")) {

        final String[] norm_words = (String[]) request.getParameter(
            "norm_words").getValue();
        final List param_norm_words = new ArrayList();
        for (int i = 0; i < norm_words.length; i++) {
          final String norm_word = MIDServices.getLuiNormalizedString(
              norm_words[i]);
          if (!norm_word.equals("")) {
            param_norm_words.add(norm_word);
          }
        }
        if (param_norm_words.size() == 0) {
          param_norm_words.add("");

        }
        if (norm_words.length > 0) {
          SearchParameter sp =
              new SearchParameter.Multiple(
              "norm_words", (String[]) param_norm_words.toArray(new String[0]));
          sp.setAttribute("any_or_all", "any");
          sps.add(sp);

          iterator = data_source.findConceptsFromWords(
              (SearchParameter[]) sps.toArray(new SearchParameter[0]), ticket);

          List concepts = getConceptList(iterator, max_result_count);

          // Return value
          request.addReturnValue(
              new Parameter.Default("any_norm_word",
                                    (Concept[]) concepts.toArray(new Concept[] {})));
        }
      }
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
