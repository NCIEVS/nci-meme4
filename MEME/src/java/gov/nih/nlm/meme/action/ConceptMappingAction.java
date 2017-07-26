/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  ConceptMappingAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a concept mapping action.
 *
 * @author MEME Group
 */

public class ConceptMappingAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private ConceptMapping cm = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ConceptMappingAction}
   */
  private ConceptMappingAction() {}

  /**
   * Instantiates a {@link ConceptMappingAction} with
   * the specified concept mapping.
   * @param cm an object {@link ConceptMapping}
   */
  private ConceptMappingAction(ConceptMapping cm) {
    this.cm = cm;
  }

  //
  // Methods
  //

  /**
   * Set the action mode.
   * @param mode the action mode
   */
  private void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Performs a new add content view action
   * @param cm an object {@link ConceptMapping}
   * @return an object {@link ConceptMappingAction}
   */
  public static ConceptMappingAction newAddConceptMappingAction(ConceptMapping
      cm) {
    ConceptMappingAction cma = new ConceptMappingAction(cm);
    cma.setMode("ADD");
    return cma;
  }

  /**
   * Performs a new remove concept mapping action
   * @param cm an object {@link ConceptMapping}
   * @return an object {@link ConceptMappingAction}
   */
  public static ConceptMappingAction newRemoveConceptMappingAction(
      ConceptMapping cm) {
    ConceptMappingAction cma = new ConceptMappingAction(cm);
    cma.setMode("REMOVE");
    return cma;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addConceptMapping(cm);
    else if (mode.equals("REMOVE"))
      mds.removeConceptMapping(cm);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    ConceptMappingAction cma = null;
    if (mode.equals("ADD"))
      cma = newRemoveConceptMappingAction(cm);
    else if (mode.equals("REMOVE"))
      cma = newAddConceptMappingAction(cm);

    cma.setUndoActionOf(this);
    return cma;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("REMOVE")) {
        cm = mds.getConceptMapping(cm.getIdentifier().intValue());
      }
    } catch (BadValueException bve) {
      DataSourceException dse =
          new DataSourceException("Failed to get initial state.");
      throw dse;
    }
  }

}