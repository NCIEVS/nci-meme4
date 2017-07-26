package gov.nih.nlm.meme.web;

import javax.servlet.http.*;

import org.apache.struts.action.*;

public class ContentViewActionForm extends ActionForm {
	private String host;

	private int contentViewId;

	private String midService;

	private String port;

	private String pageDirection;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setMidService(String midService) {
		this.midService = midService;
	}

	/**
	 * @return the contentViewId
	 */
	public int getContentViewId() {
		return contentViewId;
	}

	/**
	 * @param contentViewId
	 *            the contentViewId to set
	 */
	public void setContentViewId(int contentViewId) {
		this.contentViewId = contentViewId;
	}

	public String getMidService() {
		return midService;
	}

	public String getPort() {
		return port;
	}

	public void setPageDirection(String pageDirection) {
		this.pageDirection = pageDirection;
	}

	public String getPageDirection() {
		return pageDirection;
	}

	public ActionErrors validate(ActionMapping actionMapping,
			HttpServletRequest httpServletRequest) {
		/** @todo: finish this method, this is just the skeleton. */
		return null;
	}

	public void reset(ActionMapping actionMapping,
			HttpServletRequest servletRequest) {
	}
}
