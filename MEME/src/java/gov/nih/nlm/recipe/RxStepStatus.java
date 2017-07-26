/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.RxStep;
 * Interface:      RunnableRxStep.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    This abstract class defines the recipe step API
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.table.TableModel;

/** 
 * This class holds status information for a step.
 */
public class RxStepStatus implements Serializable {
    
  public int current_status = RxConstants.UNTOUCHED;
  public int result_status = RxConstants.RS_NOT_RUN_YET;
  public java.util.Date start_date = null;
  public java.util.Date end_date = null;
  public long elapsed_time;
  public HashMap sub_steps = new HashMap();
  
  private RxStep parent = null;

  /**
   * Default Constructor
   */
  public RxStepStatus() {
    RxToolkit.trace("RxStepStatus::Status()");
  };
  
  /**
   * Constructor that takes the Step that this belongs to.
   */
  public RxStepStatus(RxStep parent) {
    this();
    this.parent = parent;
  }

    //
    // Accessors
    // 
    /**
     * General method to change result_status
     **/
    public void setResultStatus (int result) throws IllegalArgumentException {
      
      RxToolkit.trace("RxStepStatus::setResultStatus("+result+").");
      if (RxConstants.validResultStatus(result)) {
	result_status  = result;
      }
      else {
	RxToolkit.trace("RxStepStatus::setResultStatus illegal argument: " + 
			result + ".");
	throw new IllegalArgumentException ("Illegal argument: " + result);
      };
    }

    //
    // Public Methods
    //

    /**
     * This method logs the start time in the status
     * It takes an authority, and a descriptive name of the process
     * @param authority String
     * @param op_name String 
     */
    public void startOperation() {

      // log start time
      RxToolkit.logStartTime(parent.toString());
      start_date = new java.util.Date();
    }
    
    /**
     * This method logs the start/elapsed time of a step
     * @param op_name String
     */
    public void endOperation() {
      
      // log end/elapsed time
      RxToolkit.logElapsedTime(parent.toString());
      end_date = new java.util.Date();
      elapsed_time = RxToolkit.timeDifference(
             end_date, start_date);
    }

    /**
     * This method begins a sub-step
     * @param name String
     * @param authority String
     */
    public void startSubOperation(String name, String authority) {
      RxToolkit.logStartTime(name);

      SubOperation sub_step = new SubOperation();
      sub_step.undone = false;
      sub_step.authority = authority;
      sub_step.start_date = new java.util.Date();
      sub_step.name = name;
      sub_steps.put(name,sub_step);
      parent.checkpointRecipe();

    }

    /**
     * This method ends sub-step
     * @param name String
     * @param results TableModel
     */
    public void endSubOperation(String name, TableModel results) {
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null) {
	RxToolkit.reportError("The name used for startSubOperation must\n"+
				"must be the same as for endSubOperation:\n"+
				name);
	return;
      }
      sub_step.table_results = results;
      sub_step.end_date = new java.util.Date();
      sub_step.successful = true;
      sub_step.elapsed_time = RxToolkit.timeDifference(
	   sub_step.end_date, sub_step.start_date);
      RxToolkit.logElapsedTime(name);
      parent.checkpointRecipe();
    }

    /**
     * This method ends sub-step
     * @param name String
     * @param results String
     */
    public void endSubOperation(String name, String results) {
      RxToolkit.trace("RxStep::endSubOperation()");
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null) {
	RxToolkit.reportError("The name used for startSubOperation must\n"+
				"must be the same as for endSubOperation:\n"+
				name);
	return;
      }
      sub_step.string_results = results;
      sub_step.end_date = new java.util.Date();
      sub_step.successful = true;
      sub_step.elapsed_time = RxToolkit.timeDifference(
	   sub_step.end_date, sub_step.start_date);
      RxToolkit.logElapsedTime(name);
      parent.checkpointRecipe();
    }

    /**
     * This method informs the Status object that a suboperation hs
     * been undone
     * @param name String
     */
    public void undoSubOperation(String name) {
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null) {
	RxToolkit.reportError("An attempt was made to mark a non-existent" +
				"sub-operation as undone: " +name);
	return;
      }
      sub_step.undone = true;
      sub_step.successful = true;
      parent.checkpointRecipe();
    }

    /**
     * This method informs the Status object that a suboperation has
     * been successfully undone
     * @param name String
     * @param results TableModel
     */
    public void endUndoSubOperation(String name, TableModel results) {
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null) {
	RxToolkit.reportError("An attempt was made to mark a non-existent" +
				"sub-operation as undone: " +name);
	return;
      }
      sub_step.undone = true;
      sub_step.successful = true;
      sub_step.table_results = results;
      sub_step.end_date = new java.util.Date();
      sub_step.elapsed_time = RxToolkit.timeDifference(
	   sub_step.end_date, sub_step.start_date);
      RxToolkit.logElapsedTime(name);
      parent.checkpointRecipe();
    }

    /**
     * This method informs the Status object that a suboperation has
     * been successfully undone
     * @param name String
     * @param results String
     */
    public void endUndoSubOperation(String name, String results) {
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null) {
	RxToolkit.reportError("An attempt was made to mark a non-existent" +
				"sub-operation as undone: " +name);
	return;
      }
      sub_step.undone = true;
      sub_step.successful = true;
      sub_step.string_results = results;
      sub_step.end_date = new java.util.Date();
      sub_step.elapsed_time = RxToolkit.timeDifference(
	   sub_step.end_date, sub_step.start_date);
      RxToolkit.logElapsedTime(name);
      parent.checkpointRecipe();
    }

    /**
     * This method informs the Status object that an undo suboperation has
     * been initiated 
     * @param name String
     */
    public void startUndoSubOperation(String name, String authority) {
      RxToolkit.logStartTime(name);
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null) {
	RxToolkit.reportError("An attempt was made to undo a non-existent" +
				"sub-operation: " +name);
	return;
      }
      sub_step.undone = true;
      sub_step.successful = false;
      sub_step.start_date = new java.util.Date();
      parent.checkpointRecipe();
    }

    /**
     * This returns true if a sub_step was successful in its operation
     *  and has not been undone
     * @param name String
     * @return boolean
     */
    public boolean wasSubOperationSuccessful (String name) {
      SubOperation sub_step = (SubOperation)sub_steps.get(name);
      if (sub_step == null)
	return false;
      if(sub_step.successful && !sub_step.undone)
        RxToolkit.trace("wasSubOperationSuccessful true");
      return sub_step.successful && !sub_step.undone;
    }


    /**
     * This method returns an HTML representation of the step status
     * @return String
     */
    public String toHTML() {
      RxToolkit.trace("RxStep::toHTML");
      // If something to report, report it in <TT> font
      StringBuffer sub_step_text = new StringBuffer();

      Set sub_step_set = sub_steps.keySet();
      Iterator iter = sub_step_set.iterator();
      int ct = 0;
      while (iter.hasNext()) {
	if (ct++ == 0) {
	  sub_step_text.append("<center><table BORDER=0 WIDTH=\"90%\">");
	}
        SubOperation sub_step = (SubOperation)sub_steps.get(iter.next());
	sub_step_text.append(sub_step.toHTML());
      }
      if (ct > 0)
	sub_step_text.append("</table></center>");

      return sub_step_text.toString();
    }

    
    //
    // Inner (inner) classes
    //

    /**
     * Inner class to hold sub-operation info
     */
    public class SubOperation implements Serializable {

      //
      // Fields
      //
      public java.util.Date start_date = null;
      public java.util.Date end_date = null;
      public long elapsed_time = 0;
      public String authority;
      public String name;
      public TableModel table_results = null;
      public String string_results = null;
      public boolean successful = false;
      public boolean undone = false;

      /**
       * Constructor
       */
      public SubOperation () {};

      /**
       * 
       *
       * 
       * 
       */
      public void setSubOperation(int work_id, int step_id, HashMap sub_operations) throws Exception {
        java.sql.Statement stmt =
	      RxToolkit.getSQLConnection().createStatement();
	String query;
        int ct;
        ResultSet rs = stmt.executeQuery(
          "SELECT count(*) as ct FROM rx_sub_steps"+
          "WHERE step_id = " + step_id);
        if (rs.next()) {
 	  ct = rs.getInt(1);
        } else { 
	  throw new Exception ("Counting source_classes_atoms failed");
        };

        if(ct > 0) {
	  query = "UPDATE rx_sub_steps SET work_id = " + work_id + 
		  ", step_id = " + step_id + 
		  ", start_date = '" + this.start_date + "'" +
	          ", end_date = '" + this.end_date + "'" +
		  ", elapsed_time = " + this.elapsed_time + 
		  ", authority = '" + this.authority + "'" +
		  ", name = '" + this.name + "'" +
		  ", success = '" + this.successful + "'" +
		  ", undone = '" + this.undone + "'" +
		  ", results = '" + this.string_results + "')";
        } else {
   	  query = "INSERT into rx_sub_steps " + 
		  "(work_id, step_id, start_date, " +
	  	  "end_date, elapsed_time, authority, name, " +
		  "success, undone, results) " +
	  	  "VALUES " + work_id + ", " + step_id + ", '" + 
		  this.start_date +"', '" + this.end_date + "', " + 
		  this.elapsed_time + ", '" + this.authority + "', '" + 
		  this.name + "', '" + this.successful + "', '" + 
		  this.undone + "', '" + this.string_results + "'";
        }
	RxToolkit.logComment(query, true);
	stmt.executeUpdate(query);
        RxToolkit.getSQLConnection().commit();
      };

      public HashMap getSubOperations(int step_id) throws Exception{
        java.sql.Statement stmt =
	      RxToolkit.getSQLConnection().createStatement();
	String query;
	
	HashMap hm = new HashMap();
	query = "SELECT work_id, step_id, start_date, end_date, elapsed_time, authority, name, successful, undone, results FROM rx_sub_steps WHERE step_id = "+ step_id;
        ResultSet rs = stmt.executeQuery(query); 
	while (rs.next()) {
	  hm.put(new Integer(rs.getInt(2)),  new SubOperation());
        }
        RxToolkit.getSQLConnection().commit();
	RxToolkit.flushBuffer();
	return hm;
      };


      public String toHTML() {
	StringBuffer step_text = new StringBuffer();
	step_text.append("<tr><td><tt>name : " + this.name + "</tt></td></tr>"); 
	step_text.append("<tr><td><tt>authority : "+this.authority +"</tt></td></tr>"); 
	step_text.append("<tr><td><tt>elapsed_time : "+RxToolkit.timeToString(this.elapsed_time)+"</tt></td></tr>"); 
	step_text.append("<tr><td><tt>successful : "+this.successful+"</tt></td></tr>"); 
	step_text.append("<tr><td><tt>undone : "+this.undone +"</tt></td></tr>"); 
	if(table_results != null) {
	  int column_count = table_results.getColumnCount();
	  int row_count = table_results.getRowCount();

	  step_text.append("<tr><td><tt>results : </tt></td></tr>"); 
	  step_text.append("<table BORDER=0 CELLSPACING=0 CELLPADDING=5 COLS="+column_count+"><tr>");
	  for(int x=0; x<column_count; x++){
	    step_text.append("<td><tt>" + table_results.getColumnName(x) + "</tt></td>");
          }
	  step_text.append("</tr>");


	  for(int y=0; y<row_count; y++)   {
	    step_text.append("<tr>");
            for(int x=0; x<column_count; x++){
	      step_text.append("<td><tt>" + table_results.getValueAt(y,x) + "</tt></td>"); 
            }
	    step_text.append("</tr>");
          }
	  step_text.append("</table>\n");
	}
	  
        if(string_results != null) {
	  step_text.append("<tr><td><tt>results : "+ this.string_results +"</tt></td></tr>"); 
        }
	step_text.append("<tr><td><tt></tt></td></tr>"); 

        RxToolkit.trace(step_text.toString());
	return step_text.toString();
      };

    }

}
