/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  CoreDataService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles requests for core data.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class CoreDataService implements MEMEApplicationService {

  //
  // Private methods
  //

  /**
   * Returns the relationship count.
   * @param concept the {@link Concept}
   * @param data_source the {@link MIDDataSource}
   * @return the relationship count
   * @throws DataSourceException if failed to count relationships
   */
  private int getRelationshipCount(Concept concept,
                                   MIDDataSource data_source) throws
      DataSourceException {
    return data_source.getRelationshipCount(concept);
  }

  /**
   * Returns the context relationship count.
   * @param concept the{@link Concept}
   * @param data_source the {@link MIDDataSource}
   * @return the context relationship count
   * @throws DataSourceException if failed to count context relationships
   */
  private int getContextRelationshipCount(Concept concept,
                                          MIDDataSource data_source) throws
      DataSourceException {
    return data_source.getContextRelationshipCount(concept);
  }

  /**
   * Populates the relationship.
   * @param concept the {@link Concept}
   * @param data_source the {@link MIDDataSource}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @throws DataSourceException if failed to populate relationships
   * @throws BadValueException if failed due to invalid data value
   */
  private void populateRelationships(Concept concept, MIDDataSource data_source,
                                     int start, int end) throws
      DataSourceException, BadValueException {
    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadRelationships(true);
    ticket.setReadRelationshipNames(true);
    ticket.setReadConcept(true);
    //ticket.setReadAtoms(true);
    if (start != -1 && end != -1) {
      ticket.setDataTypeStart(Relationship.class, start);
      ticket.setDataTypeEnd(Relationship.class, end);
    }
    data_source.populateConcept(concept, ticket);
    return;
  }

  /**
   * Populates the context relationship.
   * @param concept the {@link Concept}
   * @param data_source the {@link MIDDataSource}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @throws DataSourceException if failed to populate context relationships
   * @throws BadValueException if failed due to invalid data value
   */
  private void populateContextRelationships(Concept concept,
                                            MIDDataSource data_source,
                                            int start, int end) throws
      DataSourceException, BadValueException {
    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadContextRelationships(true);
    ticket.setReadRelationshipNames(true);
    ticket.setReadConcept(true);
    //ticket.setReadAtoms(true);
    if (start != -1 && end != -1) {
      ticket.setDataTypeStart(ContextRelationship.class, start);
      ticket.setDataTypeEnd(ContextRelationship.class, end);
    }
    data_source.populateConcept(concept, ticket);
    return;
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
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();
    String function = (String) request.getParameter("function").getValue();

    String[] selected_languages = null;
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
    ticket.setReadActions(false);
    if (include_or_exclude) {
      ticket.setReadLanguagesToInclude(selected_languages);
    } else {
      ticket.setReadLanguagesToExclude(selected_languages);

    }
    if (function.equals("get_concept")) {
      ticket.setReadRelationships(false);
      ticket.setReadContextRelationships(false);
      if (request.getParameter("concept") != null) {
        int concept_id = request.getParameter("concept").getInt();
        Concept concept = data_source.getConcept(concept_id, ticket);
        request.addReturnValue(new Parameter.Default("get_concept", concept));
      } else
      if (request.getParameter("cui") != null) {
        CUI cui = (CUI) request.getParameter("cui").getValue();
        Concept concept = data_source.getConcept(cui, ticket);
        request.addReturnValue(new Parameter.Default("get_concept", concept));
      }
    } else if (function.equals("get_atom")) {
      if (request.getParameter("atom") != null) {
        int atom_id = request.getParameter("atom").getInt();
        Atom atom = data_source.getAtom(atom_id, ticket);
        request.addReturnValue(new Parameter.Default("get_atom", atom));
      }
    } else if (function.equals("get_attribute")) {
      if (request.getParameter("attribute") != null) {
        int attr_id = request.getParameter("attribute").getInt();
        Attribute attribute = data_source.getAttribute(attr_id, ticket);
        request.addReturnValue(new Parameter.Default("get_attribute", attribute));
      }
    } else if (function.equals("get_dead_atom")) {
      if (request.getParameter("dead_atom") != null) {
        int atom_id = request.getParameter("dead_atom").getInt();
        Atom atom = data_source.getDeadAtom(atom_id);
        request.addReturnValue(new Parameter.Default("get_dead_atom", atom));
      }
    } else if (function.equals("get_dead_attribute")) {
      if (request.getParameter("dead_attribute") != null) {
        int attr_id = request.getParameter("dead_attribute").getInt();
        Attribute attr = data_source.getDeadAttribute(attr_id);
        request.addReturnValue(new Parameter.Default("get_dead_attribute", attr));
      }
    } else if (function.equals("get_dead_concept")) {
      if (request.getParameter("dead_concept") != null) {
        int concept_id = request.getParameter("dead_concept").getInt();
        Concept concept = data_source.getDeadConcept(concept_id);
        request.addReturnValue(new Parameter.Default("get_dead_concept",
            concept));
      }
    } else if (function.equals("get_dead_relationship")) {
      if (request.getParameter("dead_relationship") != null) {
        int rel_id = request.getParameter("dead_relationship").getInt();
        Relationship rel = data_source.getDeadRelationship(rel_id);
        request.addReturnValue(new Parameter.Default("get_dead_relationship",
            rel));
      }
    } else if (function.equals("get_dead_cxt_rel")) {
      if (request.getParameter("dead_cxt_rel") != null) {
        int rel_id = request.getParameter("dead_cxt_rel").getInt();
        ContextRelationship cxt_rel = data_source.getDeadContextRelationship(
            rel_id);
        request.addReturnValue(new Parameter.Default("get_dead_cxt_rel",
            cxt_rel));
      }
    } else if (function.equals("get_relationship")) {
      if (request.getParameter("relationship") != null) {
        int rel_id = request.getParameter("relationship").getInt();
        Relationship rel = data_source.getRelationship(rel_id, ticket);
        request.addReturnValue(new Parameter.Default("get_relationship", rel));
      }
    } else if (function.equals("get_inverse_rel")) {
      if (request.getParameter("relationship") != null) {
        int rel_id = request.getParameter("relationship").getInt();
        Relationship rel = data_source.getInverseRelationship(rel_id, ticket);
        request.addReturnValue(new Parameter.Default("get_inverse_rel", rel));
      }
    } else if (function.equals("get_rels_count")) {
      if (request.getParameter("concept") != null) {
        Concept concept = (Concept) request.getParameter("concept").getValue();
        int count = getRelationshipCount(concept, data_source);
        request.addReturnValue(new Parameter.Default("get_rels_count", count));
      }
    } else if (function.equals("get_cxt_rel")) {
      if (request.getParameter("cxt_rel") != null) {
        int cxt_rel_id = request.getParameter("cxt_rel").getInt();
        ContextRelationship cxt_rel =
            data_source.getContextRelationship(cxt_rel_id, ticket);
        request.addReturnValue(new Parameter.Default("get_cxt_rel", cxt_rel));
      }
    } else if (function.equals("get_cxt_rels_count")) {
      if (request.getParameter("concept") != null) {
        Concept concept = (Concept) request.getParameter("concept").getValue();
        int count = getContextRelationshipCount(concept, data_source);
        request.addReturnValue(new Parameter.Default("get_cxt_rels_count",
            count));
      }
    } else if (function.equals("pop_rels")) {
      int start = -1;
      int end = -1;
      if (request.getParameter("start") != null) {
        start = (int) request.getParameter("start").getInt();
      }
      if (request.getParameter("end") != null) {
        end = (int) request.getParameter("end").getInt();
      }
      if (request.getParameter("concept_id") != null) {
        int concept_id = (int) request.getParameter("concept_id").getInt();
        Concept concept = new Concept.Default(concept_id);
        if (start == 0) {
          int size = getRelationshipCount(concept, data_source);
          request.addReturnValue(new Parameter.Default("size", size));
        }
        populateRelationships(concept, data_source, start, end);
        Relationship[] rels = concept.getRelationships();
        concept.clearRelationships();
        Atom[] atoms = concept.getAtoms();
        for (int i = 0; i < atoms.length; i++) {
          atoms[i].clearRelationships();
        }
        request.addReturnValue(new Parameter.Default("pop_rels", rels));
      }
    } else if (function.equals("pop_cxt_rels")) {
      int start = -1;
      int end = -1;
      if (request.getParameter("start") != null) {
        start = (int) request.getParameter("start").getInt();
      }
      if (request.getParameter("end") != null) {
        end = (int) request.getParameter("end").getInt();
      }
      if (request.getParameter("concept_id") != null) {
        int concept_id = (int) request.getParameter("concept_id").getInt();
        Concept concept = new Concept.Default(concept_id);
        if (start == 0) {
          int size = getContextRelationshipCount(concept, data_source);
          request.addReturnValue(new Parameter.Default("size", size));
        }
        populateContextRelationships(concept, data_source, start, end);
        ContextRelationship[] cxt_rels = concept.getContextRelationships();
        concept.clearContextRelationships();
        Atom[] atoms = concept.getAtoms();
        for (int i = 0; i < atoms.length; i++) {
          atoms[i].clearContextRelationships();
        }
        request.addReturnValue(new Parameter.Default("pop_cxt_rels", cxt_rels));
      }
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
