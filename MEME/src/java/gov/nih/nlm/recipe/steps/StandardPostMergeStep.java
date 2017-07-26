/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     StandardPostMergeStep.java
 *
 * Changes
 * 03/29/2013 PM: Added "end" for the foreach loop of atom ordering. 
 * 02/24/2009 BAC (1-GCLNT): Parallelize some operations
 *   10/17/2007 JFW (1-FJ4OX): Add set_atom_ordering capability
 *   11/19/2006 BAC (1-COX53): Added newline to lrr step in set_ranks
 *   11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *   11/14/2006 BAC (1-COX53): Fix to set_ranks formatting error introduced by last fix
 *   11/03/2006 BAC (1-COX53): During load_src.csh for classes_atoms.src
 *      last_release_rank was loaded as a value of 5 to ensure "new" version things
 *      would outrank "old" version things.  Here, we set it back to 0 for any
 *      that did not inherit values as a result of safe-replacement.
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This step contains functionality to incorporate all of the "standard" post
 * merge steps that do not really require parameters. As of 4/10/2000 These are:
 * . add words to index tables . set preference . mthtm.pl . matrix initializer
 * . generate report table . clone RT? rels to C level XS rels . handle 'NEC in
 * <source>' atoms . assign CUIs
 * @author Brian Carlsen
 * @version 1.0
 */

public class StandardPostMergeStep extends RxStep {

	//
	// Fields
	//
	protected static HashMap activities_dsc = new HashMap();
	static {
		activities_dsc.put("RT_TO_XS", "Clone RT? rels to Concept level XS");
		activities_dsc.put("SY_TO_XS", "Clone SY rels to Concept level XS");
		activities_dsc.put("NEC_IN", "Handle 'NEC in' atoms");
		activities_dsc.put("ADD_WORDS", "Update String/Word Indexes");
		activities_dsc.put("REPORT_TABLES", "Generate standard report tables");
		activities_dsc.put("SET_RANKS", "Set Core Table Ranks");
		activities_dsc
				.put("MTHTM", "Delete unneeded MTH/TM atoms, insert new ones");
		activities_dsc.put("MATRIX_INITIALIZER", "Run matrix initializer");
		activities_dsc.put("ASSIGN_CUIS", "Assign CUIs");
		activities_dsc.put("REPARTITION", "Repartition MID");
	};
	protected HashMap activities = new HashMap();
	protected String comments;
	protected String mthtm_tid;
	protected String rt_to_xs_tid;
	protected String sy_to_xs_tid;
	protected String atom_ordering;

	/**
	 * StandardPostMergeStep default constructor
	 */
	public StandardPostMergeStep() {
		super();
		RxToolkit.trace("StandardPostMergeStep::StandardPostMergeStep()");
		resetValues();
	};

	/**
	 * This method resets the internal values to defaults
	 */
	public void resetValues() {
		RxToolkit.trace("StandardPostMergeStep::resetValues()");
		activities.put("RT_TO_XS", "false");
		activities.put("SY_TO_XS", "false");
		activities.put("NEC_IN", "true");
		activities.put("ADD_WORDS", "true");
		activities.put("REPORT_TABLES", "true");
		activities.put("SET_RANKS", "true");
		activities.put("MTHTM", "true");
		activities.put("MATRIX_INITIALIZER", "true");
		activities.put("ASSIGN_CUIS", "false");
		activities.put("REPARTITION", "true");
		comments = "";
		atom_ordering = "DEPTH-FIRST,CODE";
	};

	/**
	 * Create & return an instance of inner class View
	 * 
	 * @return RxStep.View
	 */
	public RxStep.View constructView() {
		RxToolkit.trace("StandardPostMergeStep::constructView()");
		return new StandardPostMergeStep.View();
	};

	/**
	 * This method generates some kind of online help and then returns. Most
	 * simply it will produce a dialog box.
	 */
	public void getHelp() {
		RxToolkit.trace("StandardPostMergeStep::getHelp()");
		RxToolkit.notifyUser("Help is not currently available.");
	};

	/**
	 * This method will be overridden by the subclasses' method. It returns the
	 * HTML representation of the step.
	 * @return String
	 */
	public String toHTML() {
		RxToolkit.trace("StandardPostMergeStep::toHTML()");
		StringBuffer step_text = new StringBuffer();

		step_text
				.append("            <table ALIGN=CENTER WIDTH=90% BORDER=1 CELLSPACING=1 CELLPADDING=2>\n");
		Iterator iter = activities_dsc.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (Boolean.valueOf((String) activities.get(entry.getKey()))
					.booleanValue()) {
				step_text.append("              <tr>\n");
				step_text.append("                <td><b>");
				step_text.append(entry.getValue());
				step_text.append("</b></td></tr>\n");
			}
		}

		if (!atom_ordering.equals("")) {
			step_text.append("              <tr>\n");
			step_text.append("                <td><b>");
			step_text.append("Atom ordering: " + atom_ordering);
			step_text.append("</b></td></tr>\n");
		}
		step_text.append("              </table>\n");

		if (!comments.equals("")) {
			step_text.append("            Comment: " + comments + "\n");

		}

		return step_text.toString();
	};

	/**
	 * This method generates code for a shell script to perform the post-insert
	 * merge operation
	 */
	public String toShellScript() {
		StringBuffer body = new StringBuffer(500);

		//
		// If section contains a ResolveSTYStep, end it
		//
		if (parent.containsInstanceOf(ResolveSTYStep.class.getName())) {
			body
					.append("#\n# Wait for resolve STYs and update releasability\n#\n"
							+ "set ef = 0\n"
							+ "wait\n"
							+ "if ($status != 0) then\n"
							+ "    set ef = 1\n"
							+ "endif\n\n"
							+ "if (`grep -c ORA- /tmp/t.sty.$$.log` > 0) then\n"
							+ "    echo \"Error resolving STYs\"\n"
							+ "    cat /tmp/t.$$.log\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error resolving STYs\"'\n"
							+ "    set ef = 1\n" + "endif\n");
		}

		//
		// If section contains an UpdateReleasabilityStep, end it
		//
		if (parent.containsInstanceOf(UpdateReleasabilityStep.class.getName())) {
			body
					.append("foreach value (`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q \"select a.high_source,a.low_source,b.stripped_source from source_source_rank a, source_rank b where a.low_source=b.source and a.stripped_source=b.stripped_source\"`)\n"
							+ "	    set ns=`echo $value | perl -ne 'split /\\|/; print \"$_[0]\\n\";'`\n"
							+ "	    set os=`echo $value | perl -ne 'split /\\|/; print \"$_[1]\\n\";'`\n"
							+ "	    set ss=`echo $value | perl -ne 'chop;split /\\|/; print \"$_[2]\\n\";'`\n"
							+ "	    if (`grep -c ORA- /tmp/t.ur.$ss.$$.log` > 0) then\n"
							+ "	        echo \"Error updating releasability\"\n"
							+ "	        cat /tmp/t.ur.$ss.$$.log\n"
							+ "	        if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error updating releasability\"'\n"
							+ "	        set ef = 1;\n"
							+ "	    endif\n\n"
							+ "end\n\n"
							+ "if ($ef == 1) then\n" + "    exit 1\n" + "endif\n");
		}

		if (activities.get("RT_TO_XS").equals("true")) {

			body.append("#\n# Clone RT? Relationships to XS\n#\n");
			body
					.append("echo \"    Clone RT? Relationships to XS ...`/bin/date`\"\n\n");
			body
					.append("$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n"
							+ "    set serveroutput on size 100000\n"
							+ "    set feedback off\n"
							+ "    WHENEVER SQLERROR EXIT -1\n"
							+ " \n"
							+ "    exec MEME_UTILITY.drop_it('table','t1')\n"
							+ "    CREATE TABLE t1 AS\n"
							+ "    SELECT  concept_id_1,concept_id_2, \n"
							+ "      0 as atom_id_1, 0 as atom_id_2,\n"
							+ "      'XS' as relationship_name,relationship_attribute,\n"
							+ "      source, source_of_label, 'N' as status,\n"
							+ "      'Y' as generated_status, 'C' as relationship_level,\n"
							+ "      released,tobereleased, 0 as relationship_id, \n"
							+ "      suppressible, sg_id_1, sg_type_1, sg_qualifier_1,\n"
							+ "      sg_id_2, sg_type_2, sg_qualifier_2\n"
							+ "      FROM relationships \n"
							+ "    WHERE relationship_id in\n"
							+ "      (SELECT relationship_id FROM source_relationships\n"
							+ "       WHERE relationship_name = 'RT?'); \n"
							+ "\n"
							+ "    -- Delete self-referential\n"
							+ "    DELETE FROM t1 WHERE concept_id_1=concept_id_2; \n"
							+ "\n"
							+ "    -- Delete where C level rel already exists\n"
							+ "    DELETE FROM t1 a WHERE EXISTS \n"
							+ "    (SELECT 1 FROM relationships b \n"
							+ "     WHERE relationship_level='C'\n"
							+ "     AND a.concept_id_1=b.concept_id_1 \n"
							+ "     AND a.concept_id_2=b.concept_id_2);\n"
							+ "    DELETE FROM t1 a WHERE EXISTS \n"
							+ "    (SELECT 1 FROM relationships b \n"
							+ "     WHERE relationship_level='C'\n"
							+ "     AND a.concept_id_2=b.concept_id_1 \n"
							+ "     AND a.concept_id_1=b.concept_id_2);\n"
							+ "\n"
							+ "    -- Set rela\n"
							+ "    UPDATE t1 SET relationship_attribute='', \n"
							+ "                  source_of_label='MTH',\n"
							+ "EOF\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error cloning relationships\"\n"
							+ "    cat /tmp/t.$$.log\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error cloning relationships\"' \n"
							+ "    exit 1\n"
							+ "endif\n"
							+ "\n"
							+ "    $MEME_HOME/bin/insert.pl -w $work_id -host=$host -port=$port -rels t1 $db $authority >&! insert.xs.log\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error inserting relationships\"\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error inserting relationships\"' \n"
							+ "    exit 1\n" + "endif\n" + "\n\n");
		}

		if (activities.get("SY_TO_XS").equals("true")) {

			body.append("#\n# Clone SY Relationships to XS\n#\n");
			body
					.append("echo \"    Clone SY Relationships to XS ...`/bin/date`\"\n\n");
			body
					.append("$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n"
							+ "    set serveroutput on size 100000\n"
							+ "    set feedback off\n"
							+ "    WHENEVER SQLERROR EXIT -1\n"
							+ " \n"
							+ "    exec MEME_UTILITY.drop_it('table','t1')\n"
							+ "    CREATE TABLE t1 AS\n"
							+ "    SELECT  concept_id_1,concept_id_2, \n"
							+ "      0 as atom_id_1, 0 as atom_id_2,\n"
							+ "      'XS' as relationship_name,relationship_attribute,\n"
							+ "      source, source_of_label, 'N' as status,\n"
							+ "      'Y' as generated_status, 'C' as relationship_level,\n"
							+ "      released,tobereleased, 0 as relationship_id, \n"
							+ "      suppressible, sg_id_1, sg_type_1, sg_qualifier_1,\n"
							+ "      sg_id_2, sg_type_2, sg_qualifier_2\n"
							+ "      FROM relationships \n"
							+ "    WHERE relationship_id in\n"
							+ "      (SELECT relationship_id FROM source_relationships\n"
							+ "       WHERE relationship_name = 'SY'); \n"
							+ "\n"
							+ "    -- Delete self-referential\n"
							+ "    DELETE FROM t1 WHERE concept_id_1=concept_id_2; \n"
							+ "\n"
							+ "    -- Delete where C level rel already exists\n"
							+ "    DELETE FROM t1 a WHERE EXISTS \n"
							+ "    (SELECT 1 FROM relationships b \n"
							+ "     WHERE relationship_level='C'\n"
							+ "     AND a.concept_id_1=b.concept_id_1 \n"
							+ "     AND a.concept_id_2=b.concept_id_2);\n"
							+ "    DELETE FROM t1 a WHERE EXISTS \n"
							+ "    (SELECT 1 FROM relationships b \n"
							+ "     WHERE relationship_level='C'\n"
							+ "     AND a.concept_id_2=b.concept_id_1 \n"
							+ "     AND a.concept_id_1=b.concept_id_2);\n"
							+ "\n"
							+ "    -- Set rela\n"
							+ "    UPDATE t1 SET relationship_attribute='', \n"
							+ "                  source_of_label='MTH',\n"
							+ "EOF\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error cloning relationships\"\n"
							+ "    cat /tmp/t.$$.log\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error cloning relationships\"' \n"
							+ "    exit 1\n"
							+ "endif\n"
							+ "\n"
							+ "    $MEME_HOME/bin/insert.pl -w $work_id -host=$host -port=$port -rels t1 $db $authority >&! insert.sy-xs.log\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error inserting relationships\"\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error inserting relationships\"' \n"
							+ "    exit 1\n" + "endif\n" + "\n\n");
		}

		if (activities.get("NEC_IN").equals("true") && 1 == 2) {

			body.append("#\n# Resolve atoms like '%NEC in $old_source'\n#\n");
			body.append("echo \"    Resolve 'NEC in' atoms ...`/bin/date`\"\n\n");
			body
					.append("$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n"
							+ "    set serveroutput on size 100000\n"
							+ "    set feedback off\n"
							+ "    WHENEVER SQLERROR EXIT -1\n"
							+ " \n"
							+ "    exec MEME_UTILITY.drop_it('table','t_${new_source}_1');\n"
							+ "    CREATE TABLE t_${new_source}_1 AS\n"
							+ "    SELECT concept_id,a.atom_id,atom_name,termgroup,\n"
							+ "      source,code,status,generated_status,released,\n"
							+ "      tobereleased, suppressible, isui \n"
							+ "    FROM classes a, atoms WHERE 1=0;\n"
							+ "\n"
							+ "    -- Get atoms based on 'NEC' and 'not elsewhere classified' \n"
							+ "    INSERT INTO t_${new_source}_1\n"
							+ "    SELECT /*+ RULE */ concept_id, atom_id,\n"
							+ "       pre || ' NEC' || post || ' in $new_source',\n"
							+ "       'MTH/PN','MTH','NOCODE', 'R','Y','N','Y','N', isui \n"
							+ "    FROM\n"
							+ "      (SELECT distinct /*+ RULE */ concept_id,\n"
							+ "             b.atom_id, \n"
							+ "  	        substr(atom_name,0,INSTR(atom_name,' NEC')-1) as pre,\n"
							+ "  	        substr(atom_name,INSTR(atom_name,' NEC')+4) as post\n"
							+ "       FROM classes a, atoms b\n"
							+ "       WHERE a.source = '$new_source'\n"
							+ "	 AND tobereleased in ('Y','y')\n"
							+ "	 AND a.atom_id = b.atom_id\n"
							+ "         AND (atom_name like '% NEC' OR\n"
							+ "	      atom_name like '% NEC (%)')\n"
							+ "       UNION\n"
							+ "       SELECT distinct /*+ RULE */ concept_id,\n"
							+ "             b.atom_id, \n"
							+ "  	        substr(atom_name,0,INSTR(lower(atom_name),' not elsewhere classified')-1) as pre,\n"
							+ "		'' as post\n"
							+ "       FROM classes a, atoms b\n"
							+ "       WHERE a.source = '$new_source'\n"
							+ "	 AND tobereleased in ('Y','y')\n"
							+ "	 AND a.atom_id = b.atom_id\n"
							+ "         AND lower(atom_name) like '% not elsewhere classified'\n"
							+ "      );\n"
							+ "\n"
							+ "    -- Remove atoms that already exist\n"
							+ "    DELETE FROM t_${new_source}_1 WHERE atom_name IN\n"
							+ "     (SELECT atom_name FROM atoms);\n"
							+ "\n"
							+ "    -- Keep only cases where underlying atom is ambiguous!\n"
							+ "    DELETE FROM t_${new_source}_1 WHERE isui IN\n"
							+ "    (SELECT isui FROM t_${new_source}_1\n"
							+ "     MINUS SELECT isui FROM classes \n"
							+ "     WHERE tobereleased in ('Y','y')\n"
							+ "     GROUP BY isui HAVING count(DISTINCT concept_id)>1);\n"
							+ "\n"
							+ "    -- Find atoms to delete\n"
							+ "    exec MEME_UTILITY.drop_it('table','t_${new_source}_2');\n"
							+ "    CREATE TABLE t_${new_source}_2 AS\n"
							+ "    SELECT /*+ RULE */ b.atom_id as row_id \n"
							+ "    FROM \n"
							+ "	 (SELECT DISTINCT atom_id+0 as atom_id, atom_name||'' as atom_name \n"
							+ "          FROM atoms WHERE atom_name LIKE '% NEC %in $old_source') b, \n"
							+ "      classes c \n"
							+ "    WHERE b.atom_id = c.atom_id;\n"
							+ "\n"
							+ "    exec dbms_output.put_line(meme_batch_actions.macro_action( -\n"
							+ "       action => 'D', id_type => 'C', authority => '$authority', -\n"
							+ "       table_name => 't_${new_source}_2', work_id => $work_id,- \n"
							+ "       status => 'R', set_preferred_flag => 'Y'));\n"
							+ "\n"
							+ "EOF\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error fixing NEC atoms\"\n"
							+ "    cat /tmp/t.$$.sql\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error fixing NEC atoms\"' \n"
							+ "    exit 1\n"
							+ "endif\n"
							+ "\n"
							+ "$MEME_HOME/bin/insert.pl -w $work_id -host=$host -port=$port -atoms t_${new_source}_1 $db $authority >&! /tmp/t.$$.log\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error inserting new NEC atoms\"\n"
							+ "    cat /tmp/t.$$.log\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error inserting new NEC atoms\"' \n"
							+ "    exit 1\n"
							+ "endif\n"
							+ "\n"
							+ "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n"
							+ "    set serveroutput on size 100000\n"
							+ "    set feedback off\n"
							+ "    WHENEVER SQLERROR EXIT -1\n"
							+ "    exec MEME_UTILITY.drop_it('table','t_${new_source}_1');\n"
							+ "    exec MEME_UTILITY.drop_it('table','t_${new_source}_2');\n"
							+ "\n" + "EOF\n");
		}

		if (activities.get("ADD_WORDS").equals("true")) {
			body.append("#\n# ");
			body.append(activities_dsc.get("ADD_WORDS"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("ADD_WORDS"));
			body.append(" ...`/bin/date`\"\n");
			body
					.append("$MEME_HOME/bin/add_words.csh -w $work_id $db >&! add_words.log &\n\n\n");
		}

		if (activities.get("REPORT_TABLES").equals("true")) {
			// We want to generate report tables for each source
			// that appears in the sources.src file
			// accessible through RxToolkit.getFromRanksFile(RxConstants.SOURCE)

			body.append("#\n# ");
			body.append(activities_dsc.get("REPORT_TABLES"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("REPORT_TABLES"));
			body.append(" ... `/bin/date`\"");
			body
					.append("\n"
							+ "foreach value (`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q \"select a.stripped_source from source_source_rank a\"`)\n"
							+ "\n"
							+ "     set ss=`echo $value | perl -ne 'chop; split /\\|/; print \"$_[0]\\n\";'`\n"
							+ "     echo \"    Generate standard report tables - $ss ... `/bin/date`\"\n"
							+ "     $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! counts.$ss.rpt &\n"
							+ "          WHENEVER SQLERROR EXIT -2\n"
							+ "          set serveroutput on size 100000\n"
							+ "          set feedback off\n"
							+ "          ALTER session SET sort_area_size=200000000;\n"
							+ "          ALTER session SET hash_area_size=200000000;\n"
							+ "          exec MEME_SOURCE_PROCESSING.report_tables( -\n"
							+ "              root_source => '$ss', -\n"
							+ "              authority => '$authority', -\n"
							+ "              work_id => '$work_id' );\n"
							+ "EOF\n"
							+ " \n"
							+ "end\n\n" + "\n");

			/** KAL removed comment block and for loop 02/27/06 **/
		}

		if (activities.get("SET_RANKS").equals("true")) {
			body.append("#\n# ");
			body.append(activities_dsc.get("SET_RANKS"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("SET_RANKS"));
			body.append(" ...`/bin/date`\"\n");
			body
					.append("$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.ranks.$$.log &\n"
							+ "    WHENEVER SQLERROR EXIT -2\n"
							+ "    set serveroutput on size 100000\n"
							+ "    set feedback off\n"
							+ "    ALTER session SET sort_area_size=200000000;\n"
							+ "    ALTER session SET hash_area_size=200000000;\n"
							+ "    \n"
							+ "    -- Reset last release rank values from original source_classes_atoms load\n"
							+ "    update classes set last_release_rank=0 where last_release_rank=5;\n"
							+ "    \n"
							+ "    exec MEME_RANKS.set_ranks( -\n"
							+ "      classes_flag => 'Y', -\n"
							+ "      attributes_flag => 'N', -\n"
							+ "      relationships_flag => 'N', work_id => $work_id );\n"
							+ "    COMMIT;\n"
							+ "    exec MEME_RANKS.set_preference(work_id => $work_id);\n"
							+ "EOF\n\n");
		}

		if (!atom_ordering.equals("")) {
			body.append("#\n");
			body.append("# Set atom ordering: " + atom_ordering + "\n");
			body.append("#\n");
			body
					.append("foreach value (`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q \"select high_source from source_source_rank\"`)\n"
							+ "    set ns=`echo $value | perl -ne 'split /\\|/; print \"$_[0]\\n\";'`\n"
							+ "    echo \"    Set atom ordering ($ns, "
							+ atom_ordering
							+ ") ...`/bin/date`\"\n"
							+ "\n"
							+ "    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF  >&! /tmp/t.ar.$ns.$$.log &\n"
							+ "        WHENEVER SQLERROR EXIT -2\n"
							+ "        set serveroutput on size 100000\n"
							+ "        set feedback off\n"
							+ "        ALTER session SET sort_area_size=200000000;\n"
							+ "        ALTER session SET hash_area_size=200000000;\n"
							+ "    \n"
							+ "        exec MEME_SOURCE_PROCESSING.set_atom_ordering( -\n"
							+ "          source => '$ns', -\n"
							+ "          ordering => '"
							+ atom_ordering
							+ "', -\n"
							+ "          authority => '$authority', work_id => $work_id);\n"
							+ "EOF\n"
							+ "end\n\n");
		}

		if (activities.get("MTHTM").equals("true")) {
			body.append("#\n# ");
			body.append(activities_dsc.get("MTHTM"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("MTHTM"));
			body.append(" ...`/bin/date`\"\n");
			body
					.append("$MEME_HOME/bin/mthtm.pl -w $work_id -host=$host -port=$port -d MERGED -i -t MTH/TM -s R $db $authority >&! mthtm.log &\n\n");
		}

		//
		// Wait for parallel processes
		//
		body.append("#\n" + "# Wait for parallel processes\n" + "#\n"
				+ "set ef = 0\n" + "wait\n" + "if ($status != 0) then\n"
				+ "    set ef = 1\n" + "endif\n\n");

		if (activities.get("REPORT_TABLES").equals("true")) {

			body
					.append("#\n"
							+ "# Error check report tables\n"
							+ "#\n"
							+ "foreach f (counts.*.rpt)\n"
							+ "    if (`grep -c ORA- $f` > 0) then\n"
							+ "        cat $f\n"
							+ "        echo \"Error generating counts $f\"\n"
							+ "        set ef = 1\n"
							+ "    endif\n"
							+ "end\n"
							+ "if ($ef == 0) then\n"
							+ "    foreach value (`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q \"select a.stripped_source from source_source_rank a\"`)\n"
							+ "\n"
							+ "        set ss=`echo $value | perl -ne 'chop; split /\\|/; print \"$_[0]\\n\";'`\n"
							+ "        set sst=`echo $ss | perl -pe 's/-//g; s/\\.//g;'`\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_demotions -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_demotions\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_merges -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_merges\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_need_review -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_need_review\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_replaced -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_replaced\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_sample -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_sample\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_nosty -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_nosty\n"
							+ "        $MEME_HOME/bin/dump_table.pl -d $db -t ${sst}_bad_merge -u MTH | $EMS_HOME/bin/make-checklist.pl -d $db -m 100 -c chk_${sst}_bad_merge\n"							
							+ "end\n" 
							+ "endif\n\n");
		}
		if (activities.get("SET_RANKS").equals("true")) {
			body
					.append("#\n"
							+ "# Error check set ranks\n"
							+ "#\n"
							+ "if (`grep -c ORA- /tmp/t.ranks.$$.log` > 0) then\n"
							+ "    echo \"Error calculating ranks\"\n"
							+ "    cat /tmp/t.ranks.$$.log\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error calculating ranks\"'\n"
							+ "    set ef = 1\n" 
							+ "endif\n\n");
		}

		if (!atom_ordering.equals("")) {
			body
					.append("#\n"
							+ "# Error check atom ordering\n"
							+ "#\n"
							+ "foreach value (`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q \"select high_source from source_source_rank\"`)\n"
							+ "    set ns=`echo $value | perl -ne 'split /\\|/; print \"$_[0]\\n\";'`\n"
							+ "    if (`grep -c ORA- /tmp/t.ar.$ns.$$.log` > 0) then\n"
							+ "        echo \"Error setting atom ordering\"\n"
							+ "        cat /tmp/t.ar.$ns.$$.log\n"
							+ "        if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error setting atom_ordering\"'\n"
							+ "        set ef = 1\n" 
							+ "    endif\n" 
							+ "end\n\n");
		}

		if (activities.get("MTHTM").equals("true")) {
			body
					.append("if (`grep -c ERROR mthtm.log` > 0) then\n"
							+ "    echo \"Error making MTH/TM atoms\"\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error making MTH/TM atoms\"'\n"
							+ "    set ef = 1\n" 
							+ "endif\n\n");
		}

		body.append("if ($ef == 1) then\n" + "    exit\n" + "endif\n\n");

		if (activities.get("MATRIX_INITIALIZER").equals("true")) {
			body.append("#\n# ");
			body.append(activities_dsc.get("MATRIX_INITIALIZER"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("MATRIX_INITIALIZER"));
			body.append(" ...`/bin/date`\"\n");
			body
					.append("$MEME_HOME/bin/matrixinit.pl -w $work_id -I $db >&! matrixinit.log\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error initializing matrix\"\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error initializing matrix\"' \n"
							+ "    exit 1\n" + "endif\n\n\n");
		}

		if (activities.get("ASSIGN_CUIS").equals("true")) {
			body.append("#\n# ");
			body.append(activities_dsc.get("ASSIGN_CUIS"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("ASSIGN_CUIS"));
			body.append(" ...`/bin/date`\"\n");
			body
					.append("$MEME_HOME/bin/assign_cuis.pl -w $work_id -new C $db >&! assign_cuis.log\n"
							+ "if ($status != 0) then\n"
							+ "    echo \"Error assigning CUIs\"\n"
							+ "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error assigning CUIs\"' \n"
							+ "    exit 1\n" + "endif\n\n\n");
		}

		if (activities.get("REPARTITION").equals("true")) {
			body.append("#\n# ");
			body.append(activities_dsc.get("REPARTITION"));
			body.append("\n#\n");
			body.append("echo \"    ");
			body.append(activities_dsc.get("REPARTITION"));
			body.append(" ...`/bin/date`\"\n");
			body.append("$EMS_HOME/bin/chemconcepts.pl -d $db\n"
					+ "$EMS_HOME/bin/batchpartition.pl -d $db >&! batch.partition.log &\n");
		}

		return body.toString();
	}

	/**
	 * This method returns an HTML representation of the step type for use in
	 * rendering the section header for a recipe step
	 * @return String
	 */
	public String typeToHTML() {
		return "<h3>" + typeToString() + "</h3>";
	};

	/**
	 * This method returns an string representation of the step type for use in
	 * rendering the step in JLists and JTables.
	 * @return String
	 */
	public String toString() {
		return typeToString();
	};

	/**
	 * This method returns a descriptive name for the type of step.
	 * @return String
	 */
	public static String typeToString() {
		RxToolkit.trace("StandardPostMergeStep::typeToString()");
		return "Standard Post Merge Steps";
	};

	/**
	 * Inner class returned by getView();
	 */
	public class View extends RxStep.View {

		//
		// View Fields
		//
		HashMap jactivities = new HashMap();
		JTextArea jcomments = new JTextArea();
		JComboBox jatom_ordering = new JComboBox();

		/**
		 * Constructor
		 */
		public View() {
			super();
			RxToolkit.trace("StandardPostMergeStep.View::View()");
			initialize();
		};

		/**
		 * This sets up the JPanel
		 */
		private void initialize() {
			RxToolkit.trace("StandardPostMergeStep.View::initialize()");

			setLayout(new BorderLayout());
			JPanel data_panel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = GridBagConstraints.RELATIVE;
			c.insets = RxConstants.GRID_INSETS;

			// Create an RxStep.DataChangeListener.
			DataChangeListener dcl = new DataChangeListener();

			// Add options
			Iterator iter = activities_dsc.entrySet().iterator();
			c.gridy = 0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			data_panel.add(new JLabel(typeToString()), c);
			c.gridwidth = 1;

			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				JCheckBox box = new JCheckBox((String) entry.getValue());
				jactivities.put(entry.getKey(), box);
				c.gridy++;
				box.addActionListener(dcl);
				data_panel.add(box, c);
			}

			c.gridy++;

			data_panel.add(new JLabel("Atom ordering:"), c);
			jatom_ordering.addItem("DEPTH-FIRST,CODE");
			jatom_ordering.addItem("DEPTH-FIRST,SCUI");
			jatom_ordering.addItem("DEPTH-FIRST,SDUI");
			jatom_ordering.addItem("DEPTH-FIRST,NONE");
			jatom_ordering.addItem("STRING");
			jatom_ordering.addItem("CODE");
			jatom_ordering.addItem("NONE");
			jatom_ordering.setSelectedIndex(0);
			jatom_ordering.setEditable(false);
			jatom_ordering.addActionListener(dcl);
			data_panel.add(jatom_ordering, c);

			c.gridy++;
			data_panel.add(new JLabel("Comments:"), c);
			c.gridy++;
			c.gridwidth = GridBagConstraints.REMAINDER;
			jcomments.setRows(3);
			jcomments.getDocument().addDocumentListener(dcl);
			JScrollPane comment_scroll = new JScrollPane();
			comment_scroll.setViewportView(jcomments);
			data_panel.add(comment_scroll, c);

			add(data_panel);

		};

		//
		// Implementation of RxStep.View methods
		//

		/**
		 * Set the focus
		 */
		public void setFocus() {
			((JCheckBox) jactivities.values().iterator().next()).requestFocus();
		}

		/**
		 * This takes values from the step and displays them.
		 */
		public void getValues() {
			RxToolkit.trace("StandardPostMergeStep.View::getValues()");
			Iterator iter = jactivities.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				((JCheckBox) entry.getValue()).setSelected(Boolean.valueOf(
						(String) activities.get(entry.getKey())).booleanValue());
			}
			jatom_ordering.setSelectedItem(atom_ordering);
			jcomments.setText(comments);
			has_data_changed = false;
		}

		/**
		 * This method is overridden by subclasses. It takes a step and puts the
		 * values from the GUI.
		 */
		public void setValues() {
			RxToolkit.trace("StandardPostMergeStep.View::setValues()");
			Iterator iter = jactivities.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				activities.put(entry.getKey(), String.valueOf(((JCheckBox) entry
						.getValue()).isSelected()));
			}
			atom_ordering = jatom_ordering.getSelectedItem().toString();
			comments = jcomments.getText();
			has_data_changed = false;
		};

		/**
		 * This method is overridden by subclasses It validates the input with
		 * respect to the underlying step
		 */
		public boolean checkUserEntry() {
			RxToolkit.trace("StandardPostMergeStep.View::checkUserEntry()");
			return true;
		};

	}

}
