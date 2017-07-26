/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  TransactionAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Represents a non operation action.
 *
 * @author MEME Group
 */

public class TransactionAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  private Identifier transaction_id = null;
  private String mode = null;
  private boolean force = false;

  /**
   * Instantiates a {@link UndoTransactionAction} with no parameter.
   */
  private TransactionAction() {}

  //
  // Methods
  //

  /**
   * Returns an action that will undo the specified transaction identifier.
   * If it is already undone, this will likely throw an exception.
   * @param transaction_id the transaction id
   * @return an action to undo a transaction
   */
  public static TransactionAction newUndoTransactionAction(
      Identifier transaction_id, boolean force) {
    TransactionAction uta = new TransactionAction();
    uta.transaction_id = transaction_id;
    uta.mode = "UNDO";
    uta.force = force;
    return uta;
  }

  /**
   * Returns an action that will undo the specified transaction identifier.
   * If it is already undone, this will likely throw an exception.
   * @param transaction_id the transaction id
   * @return an action to undo a transaction
   */
  public static TransactionAction newUndoTransactionAction(Identifier
      transaction_id) {
    TransactionAction uta = new TransactionAction();
    uta.transaction_id = transaction_id;
    uta.mode = "UNDO";
    return uta;
  }

  /**
   * Returns an action that will undo the specified transaction identifier.
   * If it is already undone, this will likely throw an exception.
   * @param transaction_id the transaction id
   * @return an action to undo a transaction
   */
  public static TransactionAction newRedoTransactionAction(Identifier
      transaction_id) {
    TransactionAction rta = new TransactionAction();
    rta.transaction_id = transaction_id;
    rta.mode = "REDO";
    return rta;
  }

  /**
   * Returns an action that will undo the specified transaction identifier.
   * If it is already undone, this will likely throw an exception.
   * @param transaction_id the transaction id
   * @return an action to undo a transaction
   */
  public static TransactionAction newRedoTransactionAction(
      Identifier transaction_id, boolean force) {
    TransactionAction rta = new TransactionAction();
    rta.transaction_id = transaction_id;
    rta.mode = "REDO";
    rta.force = force;
    return rta;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {

    if (mode.equals("UNDO")) {
      CallableStatement cstmt = null;
      try {
        String undo_process =
            "{? = call MEME_BATCH_ACTIONS.macro_undo("
            + "transaction_id => ?, "
            + "authority => ?, force => ?)}";
        cstmt = mds.prepareCall(undo_process);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, (transaction_id == null) ? 0 : transaction_id.intValue());
        cstmt.setString(3, getAuthority() == null ? "" :
                        getAuthority().toString());
        cstmt.setString(4, force ? "Y" : "N");
        cstmt.execute();
        int transaction_id = cstmt.getInt(1);
        if (transaction_id < 0) {
          cstmt.close();
          throw new DataSourceException("Bad return value from macro_undo");
        }

        //
        // In this case, we don't actually care about the id of this thing.
        //
        setIdentifier(new Identifier.Default(transaction_id));

        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        DataSourceException ds = new DataSourceException(
            "Error while undoing transaction.", this, se);
        throw ds;

      }
      finally {
        try {
          MEMEToolkit.logComment(mds.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }

    else if (mode.equals("REDO")) {
      CallableStatement cstmt = null;
      try {
        String redo_process =
            "{? = call MEME_BATCH_ACTIONS.macro_redo("
            + "transaction_id => ?, "
            + "authority => ?, force => ?)}";
        cstmt = mds.prepareCall(redo_process);
        cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setInt(2, (transaction_id == null) ? 0 : transaction_id.intValue());
        cstmt.setString(3, getAuthority() == null ? "" :
                        getAuthority().toString());
        cstmt.setString(4, force ? "Y" : "N");
        cstmt.execute();
        int transaction_id = cstmt.getInt(1);
        if (transaction_id < 0) {
          cstmt.close();
          throw new DataSourceException("Bad return value from macro_redo");
        }

        //
        // In this case, we don't actually care about the id of this thing.
        //
        setIdentifier(new Identifier.Default(transaction_id));

        cstmt.close();

      } catch (SQLException se) {
        try {
          cstmt.close();
        } catch (SQLException e) {}
        DataSourceException ds = new DataSourceException(
            "Error while redoing transaction.", this, se);
        throw ds;

      }
      finally {
        try {
          MEMEToolkit.logComment(mds.flushBuffer());
        } catch (DataSourceException dse) {}
      }
    }
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    TransactionAction la = null;
    if (mode.equals("UNDO"))
      la = TransactionAction.newRedoTransactionAction(transaction_id);
    else
      la = TransactionAction.newUndoTransactionAction(transaction_id);
    la.setUndoActionOf(this);
    la.force = force;
    return la;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    // Nothing for here
  }

}