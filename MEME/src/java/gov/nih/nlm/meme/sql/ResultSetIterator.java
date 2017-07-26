/*****************************************************************************
 * Package: gov.nih.nlm.meme.sql
 * Object:  ResultSetIterator
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.NotPersistentIfTransient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterator} over a {@link ResultSet}.
 *
 * @author MEME Group
 */
public class ResultSetIterator implements Iterator, NotPersistentIfTransient {

  //
  // Fields
  //
  private ResultSet rs = null;
  private ResultSetMapper mapper = null;
  private MEMEDataSource mds = null;
  private boolean rs_closed = false;
  private Object obj = null;

  //
  // Constructors
  //

  /**
       * Instantiates a {@link ResultSetIterator} for the specified {@link ResultSet}.
   * @param rs the {@link ResultSet} to iterate over
   * @param mds the {@link MEMEDataSource}
   * @param mapper the {@link ResultSetMapper} to map the result set to the
   */
  public ResultSetIterator(ResultSet rs, MEMEDataSource mds,
                           ResultSetMapper mapper) {
    this.rs = rs;
    this.mapper = mapper;
    this.mds = mds;
  }

  //
  // Iterator Interface
  //

  /**
   * Returns <code>true</code> if there are more elements in the result set.
   * @return <code>true</code> if there are more elements in the result set
   */
  public boolean hasNext() {
    try {
      // If the result set is closed, there are no more elements
      if (rs_closed) {
        return false;
      }

      // If the object is not null, it is the
      // next element, return true
      if (obj != null) {
        return true;
      }

      // If the object is null and the result set is open
      // try to get the next element, if it succeeds
      // set obj and return true. otherwise false
      if (rs.next()) {
        obj = mapper.map(rs, mds);
        return true;
      } else {
        rs_closed = true;
        rs.getStatement().close();
        return false;
      }

    } catch (Exception e) {
      // If there is an exception, try to close the result set
      try {
        rs_closed = true;
        rs.getStatement().close();
      } catch (Exception ee) {}
      ;

      throw new RuntimeException(
          "Unexpected error determining if iterator has next object", e);
    }
  }

  /**
   * Returns the object representing the next result set element.
   * @return the object representing the next result set element.
   */
  public Object next() {
    try {

      // If the result set is open, there are more elements
      if (!rs_closed) {

        // If the object reference is not null
        // it is the next object, set the
        // reference to null and return it
        if (obj != null) {
          Object tmp = obj;
          obj = null;
          return tmp;
        }

        // Otherwise, get the next object, if there is one
        if (rs.next()) {
          return mapper.map(rs, mds);
        }

        // otherwise just throw an exception
        else {
          throw new NoSuchElementException();
        }
      } else {
        throw new NoSuchElementException();
      }

      // Handle the error immediately
      // otherwise info about it will be lost

    } catch (MEMEException me) {
      me.setFatal(false);
      MEMEToolkit.handleError(me);
      // If there is an exception, try to close the result set
      try {
        rs_closed = true;
        rs.getStatement().close();
      } catch (SQLException e) {}
      throw new NoSuchElementException(me.getMessage());
    } catch (SQLException se) {
      MEMEToolkit.handleError(se);
      // If there is an exception, try to close the result set
      try {
        rs_closed = true;
        rs.getStatement().close();
      } catch (SQLException e) {}
      throw new NoSuchElementException(se.getMessage());
    }
  }

  /**
   * Close the underlying result set.
   */
  public void remove() {
    if (rs != null) {
      try {
        rs_closed = true;
        rs.getStatement().close();
      } catch (SQLException se) {}
    }
  }

}
