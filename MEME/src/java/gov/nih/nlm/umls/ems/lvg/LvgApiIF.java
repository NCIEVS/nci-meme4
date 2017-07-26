/************************************************************************
 *
 * Object:  LvgApiIF
 *
 * Author: suresh@nlm.nih.gov
 *
 * History: 
 *
 * 5/21/2002: First release - Suresh
 *
 * Interface to the LVG Api objects and their methods
 */

package gov.nih.nlm.umls.ems.lvg;

import gov.nih.nlm.nls.lvg.Api.LuiNormApi;
import gov.nih.nlm.nls.lvg.Api.NormApi;
import gov.nih.nlm.nls.lvg.Api.WordIndApi;

import java.util.Vector;

public class LvgApiIF {
	// types supported
	public static final int NORM = 1;

	public static final int LUINORM = 2;

	public static final int WORDIND = 3;

	public LvgApiIF(LVGServer s, String dir) {
	}

	// public methods
	public Vector norm(String q) {
		NormApi api = new NormApi();
		try {
			Vector v = api.Mutate(q);
			api.CleanUp();
			return v;
		} catch (Exception e) {
		}
		return null;
	}

	public Vector luinorm(String q) {
		Vector v = new Vector(1);
		try {
			LuiNormApi api = new LuiNormApi();
			v.add(api.Mutate(q));
			api.CleanUp();
			return v;
		} catch (Exception e) {
			System.err.println("My ERROR: " + e);
		}
		return null;
	}

	public Vector wordind(String q) {
		WordIndApi api = new WordIndApi();
		try {
			return api.Mutate(q);
		} catch (Exception e) {
		}
		return null;
	}

	public Vector derivationalVariant(String q) {
		// LvgCmdApi api = new LvgCmdApi("-f:d -CR:o", lvgProp, propHash);
		// return api.Mutate(q);
		return null;
	}

	public void cleanup() {	}

}
