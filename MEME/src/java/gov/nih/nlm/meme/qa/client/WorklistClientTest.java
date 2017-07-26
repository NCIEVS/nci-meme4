/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  WorklistClientTest
 *
 * 02/14/2006 RBE (1-79GGX): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.client.WorklistClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.PasswordAuthentication;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;
import gov.nih.nlm.meme.sql.DataSourceConstants;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

/**
 * Test suite for Worklist
 */
public class WorklistClientTest extends TestSuite {

	public WorklistClientTest() {
		setName("WorklistClientTest");
		setDescription("Test Suite for Worklist");
	}

	/**
	 * Perform Test Suite Worklist
	 */
	public void run() {

		TestSuiteUtils.printHeader(this);

		//
		// Initial Setup
		//
		SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
		Date timestamp = new Date(System.currentTimeMillis());

		WorklistClient client = null;

		//
		// This configuration cycle will only work in a server environment
		// where EMS_HOME and MIDSVCS_HOME and SAFEBOX_HOME are configured
		// properly.
		//
		// It is needed to acquire the "default" EMS user as the default
		// MID user is no longer allowed to create tables in the EMS user space.
		//
		try {
			client = new WorklistClient("");
			String user = null;
			String pwd = null;
			if (MEMEToolkit.getProperty("env.WORKLIST_USER") != null &&
					MEMEToolkit.getProperty("env.WORKLIST_PWD") != null) {
				user = MEMEToolkit.getProperty("env.WORKLIST_USER");
				pwd = MEMEToolkit.getProperty("env.WORKLIST_PWD");
				addToLog("   USER/PWD config info found in ENV_FILE");
			} 
			// If not specified, cleverly find login info.
			else {
				addToLog("   USER/PWD config info NOT found in ENV_FILE, computing it ...");
				// Get user from $EMS_HOME/etc/ems.config
				Properties prop = new Properties();
				String ems_home = MEMEToolkit.getProperty("env.EMS_HOME");
				try {
					File file = new File(new File(ems_home, "etc"), "ems.config");
					prop.load(new FileInputStream(file));
				} catch (IOException ioe) {
					throw new ExternalResourceException("Failed to open ems config file",
							ioe);
				}
				user = prop.getProperty("ORACLE_USER");

				try {
					String[] cmd = new String[] {
							MEMEToolkit.getProperty("env.MIDSVCS_HOME")
									+ "/bin/get-oracle-pwd.pl", "-d",
							System.getProperty("meme.mid.service.default"), "-u", user };
					addToLog("      " + Arrays.toString(cmd));
					Process p = Runtime.getRuntime().exec(cmd);

					//
					// Read from input stream
					//
					BufferedReader in = new BufferedReader(new InputStreamReader(p
							.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
						String[] user_pwd = FieldedStringTokenizer.split(line, "/");
						pwd = user_pwd[1];
					}

					//
					// Wait for process to complete, throw exception if bad return value
					//
					p.waitFor();
					if (p.exitValue() != 0) {
						throw new Exception("Bad Return Value - " + cmd);
					}
					p.destroy();
					addToLog("   USER/PWD config successfully computed ...");
				} catch (Exception e) {
					throw new ExternalResourceException(
							"Failed to open ems user password", e);
				}
			}
			client.setAuthentication(new PasswordAuthentication(user,pwd.toCharArray()));

			//
			// 1.1. Test setMidService(String), getMidService()
			//
			addToLog("    1.1. Test setMidService(String), " + "getMidService() ... "
					+ date_format.format(timestamp));

			client.setMidService("");
			if (client.getMidService().equals(""))
				addToLog("    1.1. Test Passed");
			else {
				addToLog("    1.1. Test Failed");
				thisTestFailed();
			}

			//
			// 2.1. Test setSessionId(String), getSessionId()
			//
			addToLog("    2.1. Test setSessionId(String), " + "getSessionId() ... "
					+ date_format.format(timestamp));

			client.setSessionId(null);
			if (client.getSessionId() == null)
				addToLog("    2.1. Test Passed");
			else {
				addToLog("    2.1. Test Failed");
				thisTestFailed();
			}

			//
			// 3.1. Test getCurrentWorklists()
			//
			addToLog("    3.1. Test getCurrentWorklists() ... "
					+ date_format.format(new Date()));

			Worklist[] worklists = client.getCurrentWorklists();
			for (int i = 0; i < worklists.length; i++) {
				addToLog("            Worklist[" + i + "] = " + worklists[i].getName());
				if (i > 5) {
					addToLog("          >>> Loop terminated. Only few records displayed.");
					break;
				}
			}

			//
			// 4.1. Test getWorklists()
			//
			addToLog("    4.1. Test getWorklists() ... "
					+ date_format.format(new Date()));

			worklists = client.getWorklists();
			for (int i = 0; i < worklists.length; i++) {
				addToLog("            Worklist[" + i + "] = " + worklists[i].getName());
				if (i > 5) {
					addToLog("          >>> Loop terminated. Only few records displayed.");
					break;
				}
			}

			//
			// 5.1. Test getWorklistNames()
			//
			addToLog("    5.1. Test getWorklistNames() ... "
					+ date_format.format(new Date()));

			String[] worklist_names = client.getWorklistNames();
			for (int i = 0; i < worklist_names.length; i++) {
				addToLog("            Worklist Name[" + i + "] = " + worklist_names[i]);
				if (i > 5) {
					addToLog("          >>> Loop terminated. Only few records displayed.");
					break;
				}
			}

			//
			// 6.1. Test getChecklists()
			//
			addToLog("    6.1. Test getChecklists() ... "
					+ date_format.format(new Date()));

			Checklist[] checklists = client.getChecklists();
			for (int i = 0; i < checklists.length; i++) {
				addToLog("            Checklist[" + i + "] = "
						+ checklists[i].getName());
				if (i > 5) {
					addToLog("          >>> Loop terminated. Only few records displayed.");
					break;
				}
			}

			//
			// 7.1. Test getChecklistNames()
			//
			addToLog("    7.1. Test getChecklistNames() ... "
					+ date_format.format(new Date()));

			String[] checklist_names = client.getChecklistNames();
			for (int i = 0; i < checklist_names.length; i++) {
				addToLog("            Checklist Name[" + i + "] = "
						+ checklist_names[i]);
				if (i > 5) {
					addToLog("          >>> Loop terminated. Only few records displayed.");
					break;
				}
			}

			//
			// 8.1. Test getWorklistAndChecklistNames()
			//
			addToLog("    8.1. Test getWorklistAndChecklistNames() ... "
					+ date_format.format(new Date()));

			String[] wc_names = client.getWorklistAndChecklistNames();
			for (int i = 0; i < wc_names.length; i++) {
				addToLog("            Name[" + i + "] = " + wc_names[i]);
				if (i > 5) {
					addToLog("          >>> Loop terminated. Only few records displayed.");
					break;
				}
			}

			//
			// 9.1. Test addAtomWorklist(AtomWorklist)
			//
			addToLog("    9.1. Test addAtomWorklist(AtomWorklist) ... "
					+ date_format.format(new Date()));

			AtomWorklist aw = client.getAtomWorklist(worklists[worklists.length-1].getName());
			aw.setName("wrk_atom_worklist");
			Atom atom = new Atom.Default(12345);
			atom.setConcept(new Concept.Default(1058572));
			aw.clear();
			aw.add(new Atom[] { atom });
			client.addAtomWorklist(aw);

			addToLog("    aw.getName()=" + aw.getName());
			addToLog("    client.getAtomWorklist(aw.getName()).getName()="
					+ client.getAtomWorklist(aw.getName()).getName());
			if (client.getAtomWorklist(aw.getName()).getName().equals(aw.getName()))
				addToLog("    9.1. Test Passed");
			else {
				addToLog("    9.1. Test Failed");
				thisTestFailed();
			}

			//
			// 10.1. Test worklistExists()
			//
			addToLog("    10.1. Test worklistExists() ... "
					+ date_format.format(new Date()));

			if (client.worklistExists(aw.getName()))
				addToLog("    10.1. Test Passed");
			else {
				addToLog("    10.1. Test Failed");
				thisTestFailed();
			}

			//
			// 11.1. Test addConceptWorklist(ConceptWorklist)
			//
			addToLog("    11.1. Test addConceptWorklist(ConceptWorklist) ... "
					+ date_format.format(new Date()));

			ConceptWorklist cw = client
					.getConceptWorklist(worklists[worklists.length-1].getName());
			cw.setName("wrk_concept_worklist");
			cw.clear();
			client.addConceptWorklist(cw);

			if (client.getConceptWorklist(cw.getName()).equals(cw))
				addToLog("    11.1. Test Passed");
			else {
				addToLog("    11.1. Test Failed");
				thisTestFailed();
			}

			//
			// 12.1. Test worklistExists()
			//
			addToLog("    12.1. Test worklistExists() ... "
					+ date_format.format(new Date()));

			if (client.worklistExists(cw.getName()))
				addToLog("    12.1. Test Passed");
			else {
				addToLog("    12.1. Test Failed");
				thisTestFailed();
			}

			//
			// 13.1. Test addAtomChecklist(AtomChecklist)
			//      
			addToLog("    13.1. Test addAtomChecklist(AtomChecklist) ... "
					+ date_format.format(new Date()));

			AtomChecklist ac = client.getAtomChecklist("chk_testconcepts");
			ac.setName("chk_atom_checklist");
			ac.clear();
			client.addAtomChecklist(ac);

			if (client.getAtomChecklist(ac.getName()).equals(ac))
				addToLog("    13.1. Test Passed");
			else {
				addToLog("    13.1. Test Failed");
				thisTestFailed();
			}

			//
			// 14.1. Test checklistExists()
			//      
			addToLog("    14.1. Test checklistExists() ... "
					+ date_format.format(new Date()));

			if (client.checklistExists("chk_atom_checklist"))
				addToLog("    14.1. Test Passed");
			else {
				addToLog("    14.1. Test Failed");
				thisTestFailed();
			}

			//
			// 15.1. Test addConceptChecklist(ConceptChecklist)
			//      
			addToLog("    15.1. Test addConceptChecklist(ConceptChecklist) ... "
					+ date_format.format(new Date()));

			ConceptChecklist cc = client.getConceptChecklist("chk_testconcepts");
			cc.setName("chk_concept_checklist");
			cc.clear();
			client.addConceptChecklist(cc);

			//
			// 16.1. Test checklistExists()
			//      
			addToLog("    16.1. Test checklistExists() ... "
					+ date_format.format(new Date()));

			if (client.checklistExists("chk_concept_checklist"))
				addToLog("    16.1. Test Passed");
			else {
				addToLog("    16.1. Test Failed");
				thisTestFailed();
			}

			//
			// 17.1. Test getAtomWorklist(String)
			//
			addToLog("    17.1. Test getAtomWorklist(String) ... "
					+ date_format.format(new Date()));

			aw = client.getAtomWorklist("wrk_atom_worklist");
			if (aw.getName().equals("wrk_atom_worklist"))
				addToLog("    17.1. Test Passed");
			else {
				addToLog("    17.1. Test Failed");
				thisTestFailed();
			}

			//
			// 18.1. Test getAtomChecklist(String)
			//
			addToLog("    18.1. Test getAtomChecklist(String) ... "
					+ date_format.format(new Date()));

			ac = client.getAtomChecklist("chk_atom_checklist");
			if (ac.getName().equals("chk_atom_checklist"))
				addToLog("    18.1. Test Passed");
			else {
				addToLog("    18.1. Test Failed");
				thisTestFailed();
			}

			//
			// 19.1. Test getConceptWorklist(String)
			//
			addToLog("    19.1. Test getConceptWorklist(String) ... "
					+ date_format.format(new Date()));

			cw = client.getConceptWorklist("wrk_concept_worklist");
			if (cw.getName().equals("wrk_concept_worklist"))
				addToLog("    19.1. Test Passed");
			else {
				addToLog("    19.1. Test Failed");
				thisTestFailed();
			}

			//
			// 20.1. Test getConceptChecklist(String)
			//
			addToLog("    20.1. Test getConceptChecklist(String) ... "
					+ date_format.format(new Date()));

			cc = client.getConceptChecklist("chk_concept_checklist");
			if (cc.getName().equals("chk_concept_checklist"))
				addToLog("    20.1. Test Passed");
			else {
				addToLog("    20.1. Test Failed");
				thisTestFailed();
			}

			//
			// 21.1. Test stampWorklist(String, Authority)
			//            
			addToLog("    21.1. Test stampWorklist(String, Authority) ... "
					+ date_format.format(new Date()));

			MolecularTransaction mt = client.stampWorklist(aw.getName(),
					new Authority.Default("MTH"));
			addToLog("     Action Name = " + mt.getActionName());
			// addToLog(" Trans ID = " + mt.getTransactionIdentifier());
			addToLog("     Work ID = " + mt.getWorkIdentifier());

			//
			// 22.1. Test removeWorklist(Worklist)
			//      
			addToLog("    22.1. Test removeWorklist(Worklist) ... "
					+ date_format.format(new Date()));

			client.removeWorklist("wrk_atom_worklist");
			try {
				client.getAtomWorklist("wrk_atom_worklist");
				addToLog("    22.1. Test Failed");
				thisTestFailed();
			} catch (MissingDataException mde) {
				addToLog("    22.1. Test Passed");
			}

			//
			// 22.2. Test removeWorklist(Worklist)
			//      
			addToLog("    22.2. Test removeWorklist(Worklist) ... "
					+ date_format.format(new Date()));

			client.removeWorklist("wrk_concept_worklist");
			try {
				client.getConceptWorklist("wrk_concept_worklist");
				addToLog("    22.2. Test Failed");
				thisTestFailed();
			} catch (MissingDataException mde) {
				addToLog("    22.2. Test Passed");
			}

			//
			// 23.1. Test removeChecklist(Checklist)
			//            
			addToLog("    23.1. Test removeChecklist(Checklist) ... "
					+ date_format.format(new Date()));

			client.removeChecklist("chk_atom_checklist");
			try {
				client.getAtomChecklist("chk_atom_checklist");
				addToLog("    23.1. Test Failed");
				thisTestFailed();
			} catch (MissingDataException mde) {
				addToLog("    23.1. Test Passed");
			}

			//
			// 23.2. Test removeChecklist(Checklist)
			//            
			addToLog("    23.2. Test removeChecklist(Checklist) ... "
					+ date_format.format(new Date()));

			client.removeChecklist("chk_concept_checklist");
			try {
				client.getConceptChecklist("chk_concept_checklist");
				addToLog("    23.2. Test Failed");
				thisTestFailed();
			} catch (MissingDataException mde) {
				addToLog("    23.2. Test Passed");
			}

		} catch (MEMEException me) {
			thisTestFailed();
			addToLog(me);
			me.setPrintStackTrace(true);
			me.printStackTrace();
		}

		addToLog("");

		if (this.isPassed())
			addToLog("    All tests passed");
		else
			addToLog("    At least one test did not complete successfully");

		//
		// Main Footer
		//

		addToLog("");

		addToLog("-------------------------------------------------------");
		addToLog("Finished WorklistClientTest at "
				+ date_format.format(new Date(System.currentTimeMillis())));
		addToLog("-------------------------------------------------------");

	}

}