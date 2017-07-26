/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ConceptMappingService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.action.ConceptMappingAction;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles requests related to concept mappings.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class ConceptMappingService implements MEMEApplicationService {

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

    // Get Service Request and function parameter
    MEMEServiceRequest request = context.getServiceRequest();
    String function = (String) request.getParameter("function").getValue();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    //
    // ConceptMapping
    //

    if (function.equals("manage_concept_mapping")) {
      if (request.getParameter("command") != null ||
          request.getParameter("param") != null) {

        String command = (String) request.getParameter("command").getValue();
        ConceptMappingAction cma = null;
        ConceptMapping cm = (ConceptMapping) request.getParameter("param").
            getValue();
        if (command.equals("ADD")) {
          cma = ConceptMappingAction.newAddConceptMappingAction(cm);
        } else if (command.equals("REMOVE")) {
          cma = ConceptMappingAction.newRemoveConceptMappingAction(cm);

        }
        data_source.getActionEngine().processAction(cma);
        request.addReturnValue(new Parameter.Default("id", cm.getIdentifier()));
      }
    }

    if (function.equals("get_concept_mappings")) {
      Ticket ticket = Ticket.getMappingTicket();
      ConceptMapping[] cms = data_source.getConceptMappings(ticket);
      request.addReturnValue(new Parameter.Default("get_concept_mappings", cms));
    } else if (function.equals("get_concept_mappings_by_concept")) {
      Ticket ticket = Ticket.getMappingTicket();
      Concept concept = null;
      if (request.getParameter("concept").getValue() != null) {
        concept = (Concept) request.getParameter("concept").getValue();
      }
      ConceptMapping[] cms = data_source.getConceptMappings(concept, ticket);
      request.addReturnValue(new Parameter.Default(
          "get_concept_mappings_by_concept", cms));
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
