/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     Recipe.java
 *
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    This is the Recipe class.  It is abstract and must be
 *             subclassed.  Recipes contain logic for how sections should
 *             be built.
 *
 * Changes
 *  03/09/2007 JFW (1-DP1M5): add "insertion complete" email notification
 *  11/14/2006 BAC (1-CNG9B): small formatting changes introduced by last fix
 *  10/30/2006 BAC (1-CNG9B): fixes to email param handling
 *  
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JPanel;

/**
 * The recipe object is created by the RxWriter object and is used by Recipe
 * Runner. Recipe contains "sections" ArrayList which contains "steps"
 * ArrayList. The whole Recipe-RxSections-RxSteps graph makes a recipe.
 * 
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */
public abstract class Recipe implements Serializable {

	//
	// Constants
	//
	// Valid values for the attributes array
	public static final String RX_NAME_ATTRIBUTE = "name";

	public static final String RX_AUTHORITY_ATTRIBUTE = "authority";

	public static final String RX_SOURCE_ATTRIBUTE = "source_name";

	public static final String RX_ROOT_SOURCE_ATTRIBUTE = "root_source_name";

	public static final String RX_OLD_SOURCE_ATTRIBUTE = "old_source_name";

	public static final String RX_WORK_ID_ATTRIBUTE = "work_id";

	public static final String RX_DESCRIPTION_ATTRIBUTE = "description";

	public static final String RX_PRE_INSERT_ATTRIBUTE = "pre_insert";

	//
	// Fields
	//

	protected HashMap attributes = new HashMap();

	protected ArrayList sections = new ArrayList();

	// Flag indicating whether recipe is currently executing
	protected boolean running = false;

	// Flag indicating whether recipe is running or undoing
	protected int execution_mode = RxConstants.EM_RUN;

	// Listeners
	protected transient ArrayList listeners = new ArrayList();

	// Flattened view of the steps
	// Transient so that RxRunner rebuilds it from the sections list
	private transient ArrayList steps = null;

	// Reference to currently running step (no need to serialize)
	protected transient RxStep currently_running_step = null;

	protected transient int current_result_status = RxConstants.RS_NOT_RUN_YET;

	//
	// Constructors
	//

	/**
	 * Recipe constructor comment.
	 */
	public Recipe() {
		super();
		RxToolkit.trace("Recipe::Recipe()");
	}

	/**
	 * Constructor for loading from XML
	 */
	public Recipe(HashMap hm) {
		// get execution_mode,running
		RxToolkit.trace("Recipe::Recipe()");
		this.execution_mode = Integer.valueOf((String) hm.get("execution_mode"))
				.intValue();
		this.running = Boolean.valueOf((String) hm.get("running")).booleanValue();
		// get attributes (embedded HashMap object)
		// Note, the parser constructs the HashMap object from
		// <Entry name="...">value</Entry> tags
		RxToolkit.trace("ATTRIBUTES HASHMAP"
				+ ((HashMap) hm.get("attributes")).toString());
		this.attributes = (HashMap) hm.get("attributes");
	};

	//
	// Accessors
	//

	/**
	 * This method creates a listeners list if it doesn't already exist
	 * @return ArrayList
	 */
	private ArrayList getListeners() {
		if (listeners == null) {
			listeners = new ArrayList();
		}
		return listeners;
	}

	/**
	 * This method determines if a particular type of section is in the sessions
	 * vector
	 * @param c String
	 * @return boolean
	 */
	public boolean containsInstanceOf(String c) {
		Iterator iter = sections.iterator();
		while (iter.hasNext()) {
			if (iter.next().getClass().equals(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Accessor method for getting attributes
	 * @return Object
	 */
	public Object getAttribute(Object key) {
		if (attributes.get(key) != null) {
			RxToolkit.trace("true");
			RxToolkit.trace(key.toString());
			RxToolkit.trace((attributes.get(key)).getClass().toString());
			RxToolkit.trace((attributes.get(key)).toString());
		} else {
			RxToolkit.trace("false");
		}
		return attributes.get(key);
	}

	/**
	 * Accessor method for getting an attribute. if null, use default.
	 * @return Object
	 */
	public Object getAttribute(Object key, Object def) {
		if (attributes.get(key) != null) {
			RxToolkit.trace("true");
			RxToolkit.trace(key.toString());
			RxToolkit.trace((attributes.get(key)).getClass().toString());
			RxToolkit.trace((attributes.get(key)).toString());
		} else {
			RxToolkit.trace("false");
			return def;
		}
		return attributes.get(key);
	}

	/**
	 * Accessor method for getting attributes
	 * @param key Object
	 * @param value Object
	 */
	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	/**
	 * Accessor method for setting recipe name
	 * @param name String
	 */
	public void setName(String name) {
		setAttribute(RX_NAME_ATTRIBUTE, name);
	}

	/**
	 * Accessor method for getting recipe name
	 * @return String
	 */
	public String getName() {
		String name = (String) getAttribute(RX_NAME_ATTRIBUTE);
		if (name == null)
			name = "";
		return name;
	}

	/**
	 * Accessor method for setting recipe's default authority
	 * @param name String
	 */
	public void setAuthority(String name) {
		setAttribute(RX_AUTHORITY_ATTRIBUTE, name);
	}

	/**
	 * Accessor method for getting recipe's default authority
	 * @return String
	 */
	public String getAuthority() {
		String name = (String) getAttribute(RX_AUTHORITY_ATTRIBUTE);
		if (name == null)
			name = "";
		return name;
	}

	/**
	 * Accessor method for getting recipe's default ENG authority
	 * @return String
	 */
	public String getENGAuthority() {
		String name = (String) getAttribute(RX_AUTHORITY_ATTRIBUTE);
		if (name == null)
			name = "";
		return RxConstants.ENG_PREFIX + name;
	}

	/**
	 * Accessor method for setting recipe description
	 * @param dsc String
	 */
	public void setDescription(String dsc) {
		setAttribute(RX_DESCRIPTION_ATTRIBUTE, dsc);
	}

	/**
	 * Accessor method for getting recipe description
	 * @return String
	 */
	public String getDescription() {
		String dsc = (String) getAttribute(RX_DESCRIPTION_ATTRIBUTE);
		if (dsc == null)
			dsc = "";
		return dsc;
	}

	/**
	 * Accessor method for setting recipe name
	 * @param work_id int
	 */
	public void setWorkID(int work_id) {
		setAttribute(RX_WORK_ID_ATTRIBUTE, new Integer(work_id));
	}

	/**
	 * Accessor method for getting recipe name
	 * @return String
	 */
	public int getWorkID() {
		Integer work_id = (Integer) getAttribute(RX_WORK_ID_ATTRIBUTE);
		if (work_id == null)
			return 0;
		return work_id.intValue();
	}

	/**
	 * Accessor method to add a step that can be run before insertion
	 * @param step RxStep
	 */
	public void addPreInsertStep(RxStep step) {
		ArrayList pre_insert = (ArrayList) getAttribute(RX_PRE_INSERT_ATTRIBUTE);
		if (pre_insert == null) {
			pre_insert = new ArrayList();
			setAttribute(RX_PRE_INSERT_ATTRIBUTE, pre_insert);
		}
		if (!pre_insert.contains(step))
			pre_insert.add(step);
	}

	/**
	 * Accessor method to remove pre-insert step
	 * @param step RxStep
	 */
	public void removePreInsertStep(RxStep step) {
		ArrayList pre_insert = (ArrayList) getAttribute(RX_PRE_INSERT_ATTRIBUTE);
		if (pre_insert == null)
			return;
		pre_insert.remove(step);
	}

	/**
	 * Accessor method to get an ArrayList of pre-insert merge steps
	 * @return ArrayList
	 */
	public ArrayList getPreInsertSteps() {
		ArrayList pre_insert = (ArrayList) getAttribute(RX_PRE_INSERT_ATTRIBUTE);
		if (pre_insert == null)
			pre_insert = new ArrayList();
		return pre_insert;
	}

	/**
	 * Accessor method for getting the name of the recipe as an HTML file.
	 * @return String
	 */
	public String getHTMLFilename() {
		return "RECIPE." + getName().toLowerCase() + ".html";
	}

	/**
	 * Accessor method for getting the name of the recipe shell script.
	 * @return String
	 */
	public String getShellScriptName() {
		return getName().toLowerCase() + ".csh";
	}

	/**
	 * This method returns the sections ArrayList as an array.
	 * @return Object []
	 */
	public Object[] getSections() {
		RxToolkit.trace("Recipe::getSections()");
		return sections.toArray();
	};

	//
	// Standard Recipe Methods
	//

	/**
	 * This method returns the section after the one passed in. If there is no
	 * next section, it throws an exception. SectionNotFoundException is thrown if
	 * more sections are allowed, EndOfRecipe is thrown if no more sections are
	 * allowed.
	 * @return RxSection
	 * @param current RxSection
	 */
	public RxSection getSectionAfter(RxSection current)
			throws SectionNotFoundException, EndOfRecipeException {
		RxToolkit.trace("RxRecipe::getSectionAfter(" + current + ")");
		int index = sections.indexOf(current);
		RxToolkit.trace("RxRecipe::getSectionAfter(" + current + ") - " + index
				+ "," + sections.size());
		if (index == sections.size() - 1) {
			if (allowsMoreSections()) {
				throw new SectionNotFoundException();
			} else {
				throw new EndOfRecipeException();
			}
		}
		return (RxSection) sections.get(index + 1);
	};

	/**
	 * This method returns the section before the current one If there is no
	 * earlier section, it throws an exception.
	 * @param current RxSection
	 * @return RxSection
	 */
	public RxSection getSectionBefore(RxSection current)
			throws SectionNotFoundException {
		RxToolkit.trace("RxRecipe::getSectionBefore(" + current + ")");
		int index = sections.indexOf(current);
		if (index < 1) {
			throw new SectionNotFoundException();
		}
		;

		return (RxSection) sections.get(index - 1);
	};

	/**
	 * This method returns the first section
	 * @return RxSection
	 */
	public RxSection getFirstSection() throws IndexOutOfBoundsException {
		RxToolkit.trace("RxRecipe::getFirstSection()");
		return (RxSection) sections.get(0);
	}

	/**
	 * This method returns the last section
	 * @return RxSection
	 */
	public RxSection getLastSection() throws IndexOutOfBoundsException {
		RxToolkit.trace("RxRecipe::getLastSection()");
		return (RxSection) sections.get(sections.size() - 1);
	}

	/**
	 * This method returns the index of a section
	 * @param current RxSection
	 * @return int
	 */
	public int indexOf(RxSection current) {
		return sections.indexOf(current);
	}

	/**
	 * Returns true if the recipe contains no sections
	 * @return boolean
	 */
	public boolean isEmpty() {
		RxToolkit.trace("Recipe::isEmpty()");
		return sections.isEmpty();
	}

	/**
	 * Returns true if the recipe contains the object RxSection passed in
	 * @param section RxSection
	 * @return boolean
	 */
	public boolean contains(RxSection section) {
		RxToolkit.trace("RxSection::contains()");
		return sections.contains(section);
	}

	/**
	 * This method inserts a RxSection at the end of the sections array
	 * @param section RxSection
	 */
	public void addRxSection(RxSection section) {
		RxToolkit.trace("Recipe::addRxSection(" + section + ")");
		if (sections.indexOf(section) == -1)
			sections.add(section);
		else
			RxToolkit.reportError("Cannot add section that already exists.");
	};

	/**
	 * This method inserts a RxSection at the end of the sections array
	 * @param index int
	 * @param section RxSection
	 */
	public void addRxSection(int index, RxSection section) {
		RxToolkit.trace("Recipe::addRxSection(" + index + "," + section + ")");
		sections.add(index, section);
	};

	/**
	 * This method deletes the section at the index
	 * @param index int
	 */
	public void deleteSection(int index) {
		RxToolkit.trace("Recipe::deleteSection(" + index + ")");
		sections.remove(index);
	};

	/**
	 * This method deletes section matching the one passed in
	 * @param section RxSection
	 */
	public void deleteSection(RxSection section) throws SectionNotFoundException {
		RxToolkit.trace("Recipe::deleteSection(" + section + ")");
		int index = sections.indexOf(section);
		if (index == -1) {
			throw new SectionNotFoundException("Section (" + section
					+ ") not found in section (" + section + ")");
		}
		sections.remove(index);
	};

	/**
	 * This method copies the sections ArrayList to the one passed in
	 * @param sections ArrayList
	 */
	public void copySections(ArrayList sections) {
		RxToolkit.trace("Recipe::copySections(" + sections + ")");
		sections = this.sections;
	};

	/**
	 * This method makes a call to save the recipe
	 */
	public void checkpoint() {
		RxToolkit.trace("Recipe::checkpoint()");
		String error;
		try {
			RxToolkit.save(this);
		} catch (Exception e) {
			RxToolkit.trace("Recipe::checkpoint() -> " + "Exception caught");
			error = "Step failed: \n" + e + ".\n"
					+ "Execution of recipe was force stopped.";
			RxToolkit.reportError(error);
		}
	};

	/**
	 * Get HTML representation of recipe, name
	 * @return String
	 */
	public String toHTML() {
		RxToolkit.trace("Recipe::toHTML()");

		String header = "<html>\n    <head>\n" + "      <title>RECIPE -- "
				+ getName() + "</title>\n" + "    </head>\n" + "    <body>";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		String footer = "        <p>\n"
				+ "         <!-- These comments are used by the What's new Generator -->\n"
				+ "         <!-- Changed On: " + sdf.format(new java.util.Date())
				+ " -->\n" + "         <!-- Changed by: MEME Team -->\n"
				+ "         <!-- Change Note: This page moved to NLM. -->\n"
				+ "         <!-- Fresh for: 1 month -->\n" + "    </body>\n"
				+ "</html>\n";

		StringBuffer body = new StringBuffer();

		Iterator iter = sections.iterator();
		while (iter.hasNext()) {

			RxSection _section = (RxSection) iter.next();
			RxToolkit.trace("NewSourceRecipe::toHTML - section.toHTML(" + _section
					+ ")");

			body.append("        " + _section.typeToHTML() + "\n");
			body.append(_section.toHTML());

		}

		StringBuffer html_doc = new StringBuffer();
		html_doc.append(header);
		html_doc.append(body.toString());
		html_doc.append(footer);

		return html_doc.toString();

	}; // end Recipe.toHTML()

	/**
	 * Get a .csh script representation of the recipe.
	 * @return String
	 */
	public String toShellScript() {
		RxToolkit.trace("Recipe::toShellScript()");

		String header = "#!/bin/csh -f\n#\n"
				+ "# File:    "
				+ getAuthority().toLowerCase()
				+ ".csh\n"
				+ "# Author:  Brian Carlsen\n"
				+ "#\n"
				+ "#          This script should be run from the directory\n"
				+ "#          containing the SRC files.\n\n"
				+ "###################################################################\n"
				+ "#\n"
				+ "# Configuration\n"
				+ "#\n"
				+ "###################################################################\n"
				+ "set mode=real\n"
				+ "set db=oa_mid2004\n"
				+ "set authority="
				+ getAuthority()
				+ "\n"
				+ "set new_source="
				+ getAttribute(RX_SOURCE_ATTRIBUTE)
				+ "\n"
				+ "set old_source=\""
				+ getAttribute(RX_OLD_SOURCE_ATTRIBUTE, "")
				+ "\"\n"
				+ "set work_id=0\n\n"
				+ "#\n"
				+ "#Set email flags\n"
				+ "#\n"
				+ "set subject_flag='-subject=\"Insertion Error - '$new_source'\"'\n"
				+ "#set to=<your email>\n"
				+ "#set from=$to\n"
				+ "if ($?to == 1) then\n"
				+ "   set to_from_flags=\"-to $to -from $from\"\n"
				+ "else\n"
				+ "   set to_from_flags=\n"
				+ "endif\n\n"
				+ "if ($?MEME_HOME == 0) then\n"
				+ "    echo '$MEME_HOME must be set'\n"
				+ "    exit 1\n"
				+ "endif\n"
				+ "\n"
				+ "if ($?LVG_HOME == 0) then\n"
				+ "    echo '$LVG_HOME must be set'\n"
				+ "    exit 1\n"
				+ "endif\n"
				+ "\n"
				+ "if ($?ORACLE_HOME == 0) then\n"
				+ "    echo '$ORACLE_HOME must be set'\n"
				+ "    exit 1\n"
				+ "endif\n"
				+ "\n"
				+ "if ($#argv == 0) then\n"
				+ "    echo \"Usage: $0 <database>\"\n"
				+ "    echo \"Usage: $0 [-t] <test database> <editing database>\"\n"
				+ "    exit 1\n"
				+ "else if ($#argv == 1) then\n"
				+ "    set mode=run\n"
				+ "    set db=$1\n"
				+ "    set real_db=$1\n"
				+ "    set host=`$MIDSVCS_HOME/bin/midsvcs.pl -s insertion-meme-server-host`\n"
				+ "    set port=`$MIDSVCS_HOME/bin/midsvcs.pl -s insertion-meme-server-port`\n"
				+ "    set qual=real\n"
				+ "else if ($#argv == 3 && \"$argv[1]\" == \"-t\") then\n"
				+ "    set mode=test\n"
				+ "    set real_db=$3\n"
				+ "    set db=$2\n"
				+ "    set host=`$MIDSVCS_HOME/bin/midsvcs.pl -s test-insertion-meme-server-host`\n"
				+ "    set port=`$MIDSVCS_HOME/bin/midsvcs.pl -s test-insertion-meme-server-port`\n"
				+ "    set qual=test\n"
				+ "else\n"
				+ "    echo \"Usage: $0 <database>\"\n"
				+ "    echo \"Usage: $0 [-t] <test database> <editing database>\"\n"
				+ "    exit 1\n"
				+ "endif \n"
				+ "\n"
				+ "set ct=`ls |fgrep -c .src`\n"
				+ "if ($ct == 0) then\n"
				+ "    echo \"You must run this script from the directory containing the src files\"\n"
				+ "    exit 1\n"
				+ "endif\n"
				+ "\n"
				+ "if ((! -e sources.src) || (! -e termgroups.src)) then\n"
				+ "    echo \"The files sources.src and termgroups.src must exist\"\n"
				+ "    exit 1\n"
				+ "endif\n"
				+ "\n"
				+ "###################################################################\n"
				+ "# Get username/password and work_id\n"
				+ "###################################################################\n"
				+ "set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`\n\n"
				+ "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! /tmp/work_id\n"
				+ "    set feedback off\n"
				+ "    set serveroutput on size 100000\n"
				+ "    exec dbms_output.put_line ( -\n"
				+ "         meme_utility.new_work( -\n"
				+ "	      authority => '$new_source', -\n"
				+ "              type => 'INSERTION', -\n"
				+ "	      description => 'Source insertion of $new_source'));\n"
				+ "EOF\n"
				+ "if ($status != 0) then\n"
				+ "    echo \"Error obtaining work_id\"\n"
				+ "    cat /tmp/work_id\n"
				+ "    exit 1\n"
				+ "endif\n"
				+ "set work_id=`tail -1 /tmp/work_id`\n"
				+ "\\rm -f /tmp/work_id\n"
				+ "\n"
				+ "#################################################################\n"
				+ "# \n"
				+ "#  Start RECIPE\n"
				+ "#\n"
				+ "#################################################################\n"
				+ "\n"
				+ "set la_status = `$MEME_HOME/bin/dump_table.pl -u $user -d $db -q \"select status from system_status where system='log_actions'\"`\n"
				+ "set recipe_start_t=`perl -e 'print time'`\n"
				+ "echo \"--------------------------------------------------------------\"\n"
				+ "echo \"Starting $0 ... `/bin/date`\"\n"
				+ "echo \"--------------------------------------------------------------\"\n"
				+ "echo \"MEME_HOME:  $MEME_HOME\"\n"
				+ "echo \"mode:       $mode\"\n"
				+ "echo \"db:         $db\"\n"
				+ "echo \"authority:  $authority\"\n"
				+ "echo \"new_source: $new_source\"\n"
				+ "echo \"old_source: $old_source\"\n"
				+ "echo \"work_id:    $work_id\"\n"
				+ "echo \"host:       $host\"\n"
				+ "echo \"port:       $port\"\n"
				+ "echo \"la_status:  $la_status\"\n"
				+ "echo \"time:       $recipe_start_t\"\n"
				+ "if ($?to == 1) then\n"
				+ "    echo \"email:      $to\"\n"
				+ "endif\n"
				+ "echo \"\"\n\n"
				+ "#\n# Indicate start of insertion\n#\n"
				+ "$ORACLE_HOME/bin/sqlplus -s $user@$real_db <<EOF > /dev/null \n"
				+ " UPDATE system_status SET status='OFF' WHERE system='log_actions';\n"
				+ "EOF\n\n" + "foreach sab (`/bin/cut -d\\| -f 1 sources.src`)\n "
				+ "  $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF > /dev/null \n"
				+ "     UPDATE sims_info SET ${qual}_insertion_start = sysdate\n"
				+ "     WHERE source = '$sab';\n" + "EOF\n" + "end\n\n";

		String footer = "##############################################################\n"
				+ "# Cleanup\n"
				+ "##############################################################\n"
				+ "echo \"    Cleaning up .src files ... `/bin/date`\"\n"
				+ "/bin/mv -f attributes.src.bak attributes.src\n"
				+ "if ($mode != \"test\") then\n"
				+ "    /bin/rm -f strings.src stringtab.src\n"
				+ "endif\n\n"
				+ "echo \"    Removing temporary indexes ... `/bin/date`\"\n"
				+ "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF\n"
				+ "    whenever sqlerror exit -1\n"
				+ "    set serveroutput on size 100000\n"
				+ "    set feedback off\n"
				+ "    exec MEME_SOURCE_PROCESSING.drop_insertion_indexes( -\n"
				+ "        authority => '$authority', work_id => $work_id)\n"
				+ "EOF\n\n"
				+ "#\n# Indicate end of insertion, compute stats\n#\n"
				+ "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF > /dev/null \n"
				+ "    UPDATE system_status SET status='OFF' WHERE system='$la_status';\n"
				+ "    exec dbms_stats.gather_schema_stats( -\n"
				+ "        ownname => 'MTH', degree => 8, options => 'GATHER STALE');\n"
				+ "    exec dbms_stats.gather_schema_stats( -\n"
				+ "        ownname => 'MEOW', degree => 8, options => 'GATHER STALE');\n"
				+ "EOF\n\n"
				+ "foreach sab (`/bin/cut -d\\| -f 1 sources.src`)\n "
				+ "  $ORACLE_HOME/bin/sqlplus -s $user@$real_db <<EOF > /dev/null \n"
				+ "    UPDATE sims_info SET ${qual}_insertion_end = sysdate\n"
				+ "    WHERE source = '$sab';\n"
				+ "EOF\n"
				+ "end\n\n"
				+ "$MEME_HOME/bin/log_operation.pl $db $authority \"$new_source Insertion\" "		
				+ "  \"Done inserting $new_source\" $work_id 0 $recipe_start_t >> /dev/null\n\n"
				+ "set subject_flag='-subject=\"Insertion Complete - '$new_source'\"'\n"
				+ "if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Insertion finished.\"' \n\n"
				+ "echo \"--------------------------------------------------------------\"\n"
				+ "echo \"Finished $0 ... `/bin/date`\"\n"
				+ "echo \"--------------------------------------------------------------\"\n";

		StringBuffer body = new StringBuffer();

		Iterator iter = sections.iterator();
		while (iter.hasNext()) {

			RxSection _section = (RxSection) iter.next();

			body
					.append("#################################################################\n");
			body.append("# " + _section.typeToHTML() + "\n");
			body
					.append("#################################################################\n");
			body.append(_section.toShellScript());
			body.append("\n");

		}

		StringBuffer html_doc = new StringBuffer();
		html_doc.append(header);
		html_doc.append(body.toString());
		html_doc.append(footer);

		return html_doc.toString();

	}; // end Recipe.toHTML()

	//
	// Methods for running recipe
	//

	/**
	 * This method flattens the steps out into an object array.
	 * @return ArrayList
	 */
	public ArrayList flattenSteps() {
		RxToolkit.trace("Recipe::flattenSteps()");

		if (steps == null) {
			ArrayList tmp_steps = new ArrayList();
			for (int i = 0; i < sections.size(); i++) {
				tmp_steps.addAll(((RxSection) sections.get(i)).getSteps());
				RxToolkit.trace("Recipe::flattenSteps - " + sections.get(i) + " : "
						+ tmp_steps.size());
			}
			steps = tmp_steps;
		}
		RxToolkit.trace("Recipe::flattenSteps(" + steps.size() + ")");
		return steps;
	}

	/**
	 * This produces an object array of headers for each section
	 * @return ArrayList
	 */
	public ArrayList sectionHeaders() {
		RxToolkit.trace("Recipe::sectionHeaders()");
		ArrayList section_headers = new ArrayList();
		for (int i = 0; i < sections.size(); i++) {
			RxSection s = (RxSection) sections.get(i);
			for (int j = 0; j < s.getSteps().size(); j++) {
				section_headers.add(s.toString());
			}
		}
		return section_headers;
	}

	/**
	 * This method returns the execution mode
	 * @return int
	 */
	public int getExecutionMode() {
		return execution_mode;
	}

	/**
	 * This method returns the steps vector
	 * @return ArrayList
	 */
	public ArrayList getSteps() {
		return flattenSteps();
	};

	/**
	 * This method returns true if the Recipe is in run mode.
	 * @return boolean
	 */
	public boolean inRunMode() {
		if (execution_mode == RxConstants.EM_RUN) {
			return true;
		} else {
			return false;
		}
	};

	/**
	 * This method returns true if the Recipe is in undo mode.
	 * @return boolean
	 */
	public boolean inUndoMode() {
		if (execution_mode == RxConstants.EM_UNDO) {
			return true;
		} else {
			return false;
		}
	};

	/**
	 * This method determines what a step instruction should change to upon
	 * execution mode change
	 * @param old_si int
	 * @param new_mode int
	 * @return int
	 */
	public int mapStepInstruction(int old_si, int new_mode) {

		// This method assumes the step instructions have been set correctly
		// If si is SI_NONE then it should change, otherwise not.
		if (new_mode == RxConstants.EM_RUN) {
			switch (old_si) {
			case RxConstants.SI_NONE:
				RxToolkit.trace("\tRxConstants::mapStepInstruction. " + old_si + " -> "
						+ RxConstants.stepInstructionToString(RxConstants.SI_RUN));
				return RxConstants.SI_RUN;
			default:
				RxToolkit.trace("\tRxConstants::mapStepInstruction. " + old_si + " -> "
						+ RxConstants.stepInstructionToString(RxConstants.SI_NONE));
				return RxConstants.SI_NONE;
			}
		} else if (new_mode == RxConstants.EM_UNDO) {
			switch (old_si) {
			case RxConstants.SI_NONE:
				RxToolkit.trace("\tRxConstants::mapStepInstruction. " + old_si + " -> "
						+ RxConstants.stepInstructionToString(RxConstants.SI_UNDO));
				return RxConstants.SI_UNDO;
			default:
				RxToolkit.trace("\tRxConstants::mapStepInstruction. " + old_si + " -> "
						+ RxConstants.stepInstructionToString(RxConstants.SI_NONE));
				return RxConstants.SI_NONE;
			}
		}

		return RxConstants.SI_NONE;

	}

	/**
	 * This method returns true if the Recipe is in undo mode.
	 * @return boolean
	 */
	protected boolean isRunning() {
		return running;
	};

	//
	// Event Generation
	//

	/**
	 * Adds a listener
	 * @param rrl RxListener
	 */
	public void addRxListener(RxListener rrl) {
		RxToolkit.trace("Recipe::addRxListener().");
		getListeners().add(rrl);
	}

	/**
	 * Removes a listener
	 * @param rrl RxListener
	 */
	public void removeRxListner(RxListener rrl) {
		RxToolkit.trace("Recipe::removeRxListener().");
		getListeners().remove(rrl);
	}

	/**
	 * This is fired when the execution_mode value is changed
	 */
	protected void fireExecutionModeChanged() {
		RxToolkit.trace("Recipe::fireExecutionModeChanged().");
		Iterator i = getListeners().iterator();
		while (i.hasNext()) {
			((RxListener) i.next()).executionModeChanged(new RxEvent(this));
		}
		checkpoint();
	}

	/**
	 * When a steps instruction is changed, this is fired with the step
	 * @param step RxStep
	 */
	protected void fireStepInstructionChanged(RxStep step) {
		RxToolkit.trace("Recipe::fireStepInstructionChanged().");
		Iterator i = getListeners().iterator();
		while (i.hasNext()) {
			((RxListener) i.next()).stepInstructionChanged(new RxEvent(step));
		}
		checkpoint();
	}

	/**
	 * When a steps instruction mask is changed, this is fired with the step
	 * @param step RxStep
	 */
	protected void fireStepInstructionMaskChanged(RxStep step) {
		RxToolkit.trace("Recipe::fireStepInstructionMaskChanged().");
		Iterator i = getListeners().iterator();
		while (i.hasNext()) {
			((RxListener) i.next()).stepInstructionMaskChanged(new RxEvent(step));
		}
		checkpoint();
	}

	/**
	 * When a steps result status mask is changed, this is fired with the step
	 * @param step RxStep
	 */
	protected void fireResultStatusChanged(RxStep step) {
		RxToolkit.trace("Recipe::fireResultStatusChanged().");
		Iterator i = getListeners().iterator();
		while (i.hasNext()) {
			((RxListener) i.next()).resultStatusChanged(new RxEvent(step));
		}
		checkpoint();
	}

	/**
	 * When the run() method completes this is fired.
	 */
	protected void fireRecipeExecutionCompleted() {
		RxToolkit.trace("Recipe::fireRecipeExecutionCompleted().");
		Iterator i = getListeners().iterator();
		while (i.hasNext()) {
			((RxListener) i.next()).recipeExecutionCompleted(new RxEvent(this));
		}
	}

	//
	// Inner classes
	//

	/**
	 * Inner class defining the RxSection's own View. This inner class should be
	 * "overridden" by subclasses
	 */
	protected class View extends JPanel {
		public View() {
			super();
		};
	};

	//
	// Abstract methods
	//

	/**
	 * This abstract method returns the type of work this recipe does.
	 * @return String
	 */
	public abstract String getWorkType();

	/**
	 * This abstract method returns HTML which is used as a section header when
	 * rendering this recipe as an HTML document.
	 * @return String
	 */
	public abstract String typeToHTML();

	/**
	 * This method generates a view
	 * @return JPanel
	 */
	public abstract javax.swing.JPanel getView();

	/**
	 * This abstract method returns a String [] containing class names of the
	 * sections that can be inserted This list is presented to the user when
	 * inserting a new section
	 * @return String []
	 */
	public abstract String[] getPossibleInsertSections(RxSection current)
			throws SectionNotAllowedException;

	/**
	 * This method returns a String [] of class names of the sections that can
	 * come "next".
	 * @return String []
	 */
	public abstract String[] getPossibleNextSections(RxSection current);

	/**
	 * Abstract method returns true if more sections are allowed to be added
	 * @return boolean
	 */
	public abstract boolean allowsMoreSections();

	/**
	 * This static method returns a name describing the recipe type. This is used
	 * by anything that dynamically loads the class. To see a string
	 * representation of what the class is. Although not abstract this method
	 * should be overloaded
	 * @return String
	 */
	public static String typeToString() {
		return "Generic Recipe, this method should be overridden by subclasses.";
	};

}
