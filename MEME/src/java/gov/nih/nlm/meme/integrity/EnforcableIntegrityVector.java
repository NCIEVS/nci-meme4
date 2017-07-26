/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  EnforcableIntegrityVector
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a vector used by an application for enforcing {@link IntegrityCheck}s.
 * It contains methods for applying the various {@link DataConstraint}
 * and {@link MergeInhibitor} to concepts used by the application.
 *
 * @author MEME Group
 */

public class EnforcableIntegrityVector extends IntegrityVector.Default {

  //
  // Methods
  //

  /**
   * Overrides checks from specified vector.
   * @param vector the {@link IntegrityVector}
   */
  public void applyOverrideVector(IntegrityVector vector) {
    //
    // Get checks
    //
    IntegrityCheck[] ic = vector.getChecks();

    //
    // Override vector with new codes
    //
    for (int i = 0; i < ic.length; i++) {
      addIntegrityCheck(ic[i], vector.getCodeForCheck(ic[i]));
    }
  }

  /**
   * Apply {@link MergeInhibitor}s to the pair of concepts and return the {@link ViolationsVector}.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector applyMergeInhibitors(Concept source, Concept target) {
    //
    // Create a violations vector
    //
    ViolationsVector vv = new ViolationsVector();

    //
    // Iterate over checks
    //
    Collection collection = checks.keySet();
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();

      //
      // Apply merge inhibitors
      // Add failed checks to violations vector
      //
      if (o instanceof MergeInhibitor) {
        String code = (String) checks.get(o);
        MergeInhibitor mi = (MergeInhibitor) o;
        if (mi.isActive()) {
          boolean failed = mi.validate(source, target);
          if (failed) {
            vv.addIntegrityCheck(mi, code);
          }
        }
      }
    }
    return vv;
  }

  /**
   * Apply {@link MoveInhibitor}s to the pair of concepts and return the {@link ViolationsVector}.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms an array of object {@link Atom}
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector applyMoveInhibitors(Concept source, Concept target,
                                              Atom[] source_atoms) {
    //
    // Create a violations vector
    //
    ViolationsVector vv = new ViolationsVector();

    //
    // Iterate over checks
    //
    Collection collection = checks.keySet();
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();

      //
      // Apply move inhibitors
      // Add failed checks to violations vector
      //
      if (o instanceof MoveInhibitor) {
        String code = (String) checks.get(o);
        MoveInhibitor mi = (MoveInhibitor) o;
        if (mi.isActive()) {
          boolean failed = mi.validate(source, target, source_atoms);
          if (failed) {
            vv.addIntegrityCheck(mi, code);
          }
        }
      }
    }
    return vv;
  }

  /**
   * Apply fatal {@link MergeInhibitor}s to the pair of concepts and return the {@link ViolationsVector}.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector applyFatalMergeInhibitors(Concept source,
      Concept target) {
    //
    // Create violations vector
    //
    ViolationsVector vv = new ViolationsVector();

    //
    // Iterate through checks
    //
    Collection collection = checks.keySet();
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();

      //
      // Apply fatal merge inhibitors
      // Add failed checks to violations vector
      //
      if (o instanceof MergeInhibitor) {
        String code = (String) checks.get(o);
        MergeInhibitor mi = (MergeInhibitor) o;
        if (mi.isFatal() && mi.isActive()) {
          boolean failed = mi.validate(source, target);
          if (failed) {
            vv.addIntegrityCheck(mi, code);
          }
        }
      }
    }
    return vv;
  }

  /**
   * Apply fatal {@link MoveInhibitor}s to the pair of concepts and return the {@link ViolationsVector}.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms an array of object {@link Atom}
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector applyFatalMoveInhibitors(Concept source,
      Concept target,
      Atom[] source_atoms) {
    //
    // Create violations vector
    //
    ViolationsVector vv = new ViolationsVector();

    //
    // Iterate over checks
    //
    Collection collection = checks.keySet();
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();
      //
      // Apply fatal move inhibitors
      // Add failed checks to violations vector
      //
      if (o instanceof MoveInhibitor) {
        String code = (String) checks.get(o);
        MoveInhibitor mi = (MoveInhibitor) o;
        if (mi.isFatal() && mi.isActive()) {
          boolean failed = mi.validate(source, target, source_atoms);
          if (failed) {
            vv.addIntegrityCheck(mi, code);
          }
        }
      }
    }
    return vv;
  }

  /**
   * Apply {@link DataConstraint}s to the specified concepts and return the {@link ViolationsVector}.
   * @param source the source {@link Concept}
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector applyDataConstraints(Concept source) {
    //
    // Create a violations vector
    //
    ViolationsVector vv = new ViolationsVector();

    //
    // Iterate over checks
    //
    Collection collection = checks.keySet();
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();
      //
      // Apply data constraints.
      // Add failed checks to violations vector
      //
      if (o instanceof DataConstraint) {
        String code = (String) checks.get(o);
        DataConstraint dc = (DataConstraint) o;
        if (dc.isActive()) {
          boolean failed = dc.validate(source);
          MEMEToolkit.trace(
              "EnforcableIntegrityVector.applyDataConstraints() -"
              + dc.getName() + ", failed=" + failed);
          if (failed) {
            vv.addIntegrityCheck(dc, code);
          }
        }
      }
    }
    return vv;
  }

  /**
   * Only runs the check if they are fatal and returns the validation vector.
   * @param source the {@link Concept}
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector applyFatalDataConstraints(Concept source) {
    //
    // Create violations vector
    //
    ViolationsVector vv = new ViolationsVector();

    //
    // Iterate over checks
    //
    Collection collection = checks.keySet();
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();
      //
      // Apply fatal data constraints.
      // Add failed checks to violations vector
      //
      if (o instanceof DataConstraint) {
        String code = (String) checks.get(o);
        DataConstraint dc = (DataConstraint) o;
        if (dc.isFatal() && dc.isActive()) {
          boolean failed = dc.validate(source);
          if (failed) {
            vv.addIntegrityCheck(dc, code);
          }
        }
      }
    }
    return vv;
  }

}