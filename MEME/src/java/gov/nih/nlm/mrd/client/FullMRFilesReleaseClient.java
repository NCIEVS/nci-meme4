/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.client
 * Object:  FullMRFilesReleaseClient
 * Changes:
 *   03/08/2007 TTN (1-DKB57): Add Finish Release method
 *   01/06/2006 TTN (1-73ETH): Add main method to use from command line
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.client;

import gov.nih.nlm.meme.exception.*;
import gov.nih.nlm.meme.MEMEToolkit;
import java.util.Vector;
import java.util.Arrays;
import gov.nih.nlm.meme.common.StageStatus;

/**
 * Represents a {@link ReleaseClient} capable of generating and running
 * QA for a full set of MR files.
 *
 * @author  MEME Group
 */
public class FullMRFilesReleaseClient
    extends ReleaseClient {
  /**
   * Instantiates a {@link FullMRFilesReleaseClient}.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public FullMRFilesReleaseClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates a {@link FullMRFilesReleaseClient} pointing to the
   * specified mid service.
   * @param service the mid service name to use
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public FullMRFilesReleaseClient(String service) throws MEMEException {
    super();
    this.mid_service = service;
    setReleaseGenerator("FullMRFilesReleaseGenerator");
  }

  public static void main(String[] args) {
    try {
      if (args.length < 4) {
        System.err.println(
            "Usage: FullMRFilesReleaseClient <database> <release> <stage> <target list>");
        return;
      }
      String release = args[1];
      String stage = args[2];
      String target = args[3];
      ReleaseClient rc = new FullMRFilesReleaseClient();
      rc.setMidService(args[0]);
      String[] handlers = target.split(",");
      if (handlers != null) {
        for (int i = 0; i < handlers.length; i++) {
          String dependencies = rc.getTarget(release, handlers[i]).
              getDependencies();
          if (dependencies != null) {
            String[] dep_targets = dependencies.split(",");
            for (int j = 0; j < dep_targets.length; j++) {
              if ( (target.indexOf(dep_targets[j]) == -1)) {
                StageStatus[] status = rc.getTarget(release,
                    dep_targets[j]).getStageStatus();
                for (int k = 0; k < status.length; k++) {
                  if (status[k].getName().equals(stage) &&
                      (status[k].getCode() & StageStatus.FINISHED) !=
                      StageStatus.FINISHED) {
                    throw new BadValueException("You must " + stage + " " +
                                                dep_targets[j] + " before " +
                                                handlers[i]);
                  }
                }
              }
            }
          }
        }
        rc.activateTargetHandlers(handlers);
        Vector active = new Vector(Arrays.asList(handlers));
        String[] targets = rc.getTargetNames(release);
        Vector deactive = new Vector(targets.length);
        for (int i = 0; i < targets.length; i++) {
          if (!active.contains(targets[i])) {
            deactive.add(targets[i]);
          }
        }
        rc.deactivateTargetHandlers( (String[]) deactive.toArray(new String[0]));
      }
      rc.doProcess(stage, release);
    }
    catch (MEMEException e) {
      MEMEToolkit.handleError(e);
      System.exit(1);
    }
  }

}
