package gov.nih.nlm.mrd.client;

import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.client.ClientAPI;
import gov.nih.nlm.meme.common.MRDConcept;
import gov.nih.nlm.meme.common.MRDAtom;
import gov.nih.nlm.meme.common.MRDRelationship;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.ReleaseTarget;
import gov.nih.nlm.umls.jekyll.JekyllKit;

/**
 * {@link ConceptReportClient} wrapper for handling "Concept report" events.
 * 
 * @author MEME Group
 */

public abstract class ConceptReportClient extends ClientAPI {

	protected String name;

	//
	// MID Service
	//
	protected String mid_service = null;

	/**
	 * Instantiates a {@link ConceptReportClient} connected to "mrd-db".
	 * 
	 * @throws MEMEException
	 *           if the required properties are not set, or if the protocol
	 *           handler cannot be instantiated
	 */
	public ConceptReportClient() throws MEMEException {
		this("mrd-db");
	}

	/**
	 * Instantiates a {@link ConceptReportClient} connected to the specified mid
	 * service.
	 * 
	 * @param service
	 *          the mid service
	 * @throws MEMEException
	 *           if the required properties are not set, or if the protocol
	 *           handler cannot be instantiated
	 */
	public ConceptReportClient(String service) throws MEMEException {
		super();
		this.mid_service = service;
	}

	/**
	 * Sets the mid service.
	 * 
	 * @param mid_service
	 *          the mid service
	 */
	public void setMidService(String mid_service) {
		if (!mid_service.equals(this.mid_service)) {
			this.mid_service = mid_service;
		}
	}

	/**
	 * Returns the mid service.
	 * 
	 * @return the mid service
	 */
	public String getMidService() {
		return mid_service;
	}

	/**
	 * Sets the concept generator name.
	 * 
	 * @param name
	 *          the concept generator name
	 */
	public void setConceptGenerator(String name) {
		this.name = name;
	}

	public synchronized static String getDataSource(String data_source) {
		if (data_source.endsWith("-db")) {
			try {
				return MIDServices.getService(data_source);
			} catch (Exception ex) {
				ex.printStackTrace(JekyllKit.getLogWriter());
				return "";
			}
		} else {
			return data_source;
		}
	}

	/**
	 * Returns the {@link MRDConcept}
	 * 
	 * @param inputValue
	 *          the CUI or CID or AUI
	 * @param idType
	 *          inputValue indicator
	 * @return the {@link MRDConcept}
	 * @throws MEMEException
	 *           if failed to get target
	 */
	public MRDConcept getMRDConcept(String inputValue, String idType)
			throws MEMEException {
		// Prepare request document
		MEMEServiceRequest request = getServiceRequest();
		String function = null;
		if (idType.equalsIgnoreCase("cui")) {
			function = "cuiConceptReport";
		} else if (idType.equalsIgnoreCase("aui")) {
			function = "auiConceptReport";
		} else if (idType.equalsIgnoreCase("conceptId")) {
			function = "conceptIdConceptReport";
		}

		request.addParameter(new Parameter.Default("function", function));
		request.addParameter(new Parameter.Default(idType, inputValue));

		request.setMidService(getDataSource("mrd-db"));

		// Issue request
		request = getRequestHandler().processRequest(request);

		// Handle exceptions
		Exception[] exceptions = request.getExceptions();
		if (exceptions.length > 0) {
			throw (MEMEException) exceptions[0];
		}

		if (request.getReturnValue("ConceptReport") != null) {
			return (MRDConcept) request.getReturnValue("ConceptReport").getValue();
		} else {
			throw new MEMEException("No Concepts found");
		}

	}

	/**
	 * Returns the array of {@link MRDAtom}
	 * 
	 * @param inputValue
	 *          code or tty
	 * @param idType
	 *          inputValue indicator
	 * @return the {@link MRDAtom}
	 * @throws MEMEException
	 *           if failed to get target
	 */
	public MRDAtom[] getMRDAtoms(String inputValue, String idType, String source,
			int count) throws MEMEException {
		// Prepare request document
		MEMEServiceRequest request = getServiceRequest();

		String function = null;

		if (idType.equalsIgnoreCase("code")) {
			function = "codeConceptReport";
		} else if (idType.equalsIgnoreCase("tty")) {
			function = "ttyConceptReport";

			if (source != null)
				request.addParameter(new Parameter.Default("source", source));
		}

		request.addParameter(new Parameter.Default("count", count));
		request.addParameter(new Parameter.Default("function", function));
		request.addParameter(new Parameter.Default(idType, inputValue));

		request.setMidService(getDataSource("mrd-db"));

		// Issue request
		request = getRequestHandler().processRequest(request);

		// Handle exceptions
		Exception[] exceptions = request.getExceptions();
		if (exceptions.length > 0) {
			throw (MEMEException) exceptions[0];
		}

		if (idType.equalsIgnoreCase("tty")
				&& request.getReturnValue("TtyData") != null) {
			return (MRDAtom[]) request.getReturnValue("TtyData").getValue();
		} else if (idType.equalsIgnoreCase("code")
				&& request.getReturnValue("CodeData") != null) {
			return (MRDAtom[]) request.getReturnValue("CodeData").getValue();
		} else {
			throw new MEMEException("No Atoms found");
		}

	}

	/**
	 * Returns the array of {@link MRDRelationship}
	 * 
	 * @param inputValue
	 *          the rel or rela
	 * @param idType
	 *          inputValue indicator
	 * @return the array of {@link MRDRelationship}
	 * @throws MEMEException
	 *           if failed to get target
	 */
	public MRDRelationship[] getMRDRels(String inputValue, String idType,
			String source, String rela, String rel, int count) throws MEMEException {
		// Prepare request document
		MEMEServiceRequest request = getServiceRequest();

		String function = null;

		if (idType.equalsIgnoreCase("rel")) {
			function = "relConceptReport";

			if (rela != null)
				request.addParameter(new Parameter.Default("rela", rela));
		} else if (idType.equalsIgnoreCase("rela")) {
			function = "relaConceptReport";

			if (rela != null)
				request.addParameter(new Parameter.Default("rel", rel));

		}

		if (source != null)
			request.addParameter(new Parameter.Default("source", source));

		request.addParameter(new Parameter.Default("count", count));
		request.addParameter(new Parameter.Default("function", function));
		request.addParameter(new Parameter.Default(idType, inputValue));

		request.setMidService(getDataSource("mrd-db"));

		// Issue request
		request = getRequestHandler().processRequest(request);

		// Handle exceptions
		Exception[] exceptions = request.getExceptions();
		if (exceptions.length > 0) {
			throw (MEMEException) exceptions[0];
		}

		if (request.getReturnValue("RelationsData") != null) {
			return (MRDRelationship[]) request.getReturnValue("RelationsData")
					.getValue();
		} else {
			throw new MEMEException("No Concepts found");
		}

	}

	/**
	 * Get a list of valid <code>source</code> values.
	 * 
	 * @return a array of valid <code>source</code> values.
	 * @throws MEMEException
	 */
	public String[] getSources() throws MEMEException {

		// Prepare request document
		MEMEServiceRequest request = getServiceRequest();
		// request.setService("MEMERelaEditorService");
		request.setMidService(getDataSource("mrd-db"));
		// request.setNoSession(true);
		request.addParameter(new Parameter.Default("function", "getSources"));

		// Issue request
		request = getRequestHandler().processRequest(request);

		Exception[] exceptions = request.getExceptions();

		if (exceptions.length > 0) {
			throw (MEMEException) exceptions[0];
		}

		return (String[]) request.getReturnValue("sources").getValue();

	}

	/**
	 * Returns the {@link MEMEServiceRequest}.
	 * 
	 * @return the {@link MEMEServiceRequest}
	 */
	protected MEMEServiceRequest getServiceRequest() {

		// Prepare request document
		MEMEServiceRequest request = new MEMEServiceRequest();
		request.setService("ConceptReportGenerator");
		request.setInitiateSession(true);
		request.setNoSession(false);
		request.setTerminateSession(false);
		request.setTimeout(0);

		return request;
	}
}
