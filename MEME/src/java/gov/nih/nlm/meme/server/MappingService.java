/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MappingService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.Mapping;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MappingMapper;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles requests related to map sets or mappings.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class MappingService implements MEMEApplicationService {

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

    //
    // Return range (start to end-1) of mappings for a specified concept_id
    //
    if (function.equals("get_mappings")) {
      int concept_id = request.getParameter("concept_id").getInt();
      int start = request.getParameter("start").getInt();
      int end = request.getParameter("end").getInt();
      request.addReturnValue(
          new Parameter.Default("mappings",
                                getMappings(context.getDataSource(), concept_id,
                                            start, end)));
    }

    //
    // Return number of mappings for map set with specified concept_id
    //
    else if (function.equals("get_mapping_count")) {
      int concept_id = request.getParameter("concept_id").getInt();
      request.addReturnValue(new Parameter.Default(
          "mapping_count",
          context.getDataSource().getMapSet(concept_id, Ticket.getMappingTicket()).
          getMappings().length));
    }

    //
    // Return all map sets
    //
    else if (function.equals("get_map_sets")) {
      request.addReturnValue(new Parameter.Default(
          "map_sets", context.getDataSource().getMapSets()));
    }

    //
    // Return fully populated map set for specified concept id (including all mappings)
    //
    else if (function.equals("get_map_set")) {
      int concept_id = request.getParameter("concept_id").getInt();
      request.addReturnValue(new Parameter.Default(
          "map_set",
          context.getDataSource().getMapSet(concept_id, Ticket.getMappingTicket())));
    }

    //
    // Return map set info for specified concept id (without mappings).
    //
    else if (function.equals("get_map_set_no_mappings")) {
      int concept_id = request.getParameter("concept_id").getInt();
      MapSet[] map_sets = context.getDataSource().getMapSets();
      for (int i = 0; i < map_sets.length; i++) {
        if (map_sets[i].getIdentifier().intValue() == concept_id) {
          request.addReturnValue(new Parameter.Default(
              "map_set",
              map_sets[i]));
          break;
        }
      }
    }
  }

  /**
   * Return mappings
   * @param mds an object {@link MEMEDataSource}
   * @param concept_id the concept id
   * @param start the start index
   * @param end the end index
   * @return an array of object {@link Mapping}
   * @throws MEMEException if failed to get mappings
   */
  private Mapping[] getMappings(
      MEMEDataSource mds, int concept_id, int start, int end) throws
      MEMEException {

    Ticket ticket = Ticket.getMappingTicket();
    ticket.clearDataMappers(Attribute.class);
    MappingMapper mm = new MappingMapper();
    if (start != -1 && end != -1) {
      mm.setStart(start);
      mm.setEnd(end);
    }
    ticket.addDataMapper(Attribute.class, mm);
    ticket.setDataTypeOrderBy(Attribute.class, "ORDER BY attribute_name");
    ticket.setDataTypeRestriction(Attribute.class,
        "AND attribute_name LIKE 'XMAP%' AND tobereleased IN ('Y','y')");
    MapSet mapset = new MapSet.Default(concept_id);
    mds.populateConcept(mapset, ticket);

    return mapset.getMappings();

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