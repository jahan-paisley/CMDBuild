package org.cmdbuild.model;

public abstract class AbstractEmail {

	private String fromAddress;
	private String toAddresses;
	private String ccAddresses;
	private String bccAddresses;
	private String subject;
	private String content;
	private String notifyWith;

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(final String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(final String toAddresses) {
		this.toAddresses = toAddresses;
	}

	public String getCcAddresses() {
		return ccAddresses;
	}

	public void setCcAddresses(final String ccAddresses) {
		this.ccAddresses = ccAddresses;
	}

	public String getBccAddresses() {
		return bccAddresses;
	}

	public void setBccAddresses(final String bccAddresses) {
		this.bccAddresses = bccAddresses;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	/**
	 * Return the name of the Email template to use to notify a received email
	 * in answer to this email
	 * 
	 * @return
	 */
	public String getNotifyWith() {
		return notifyWith;
	}

	/**
	 * Set the name of the Email template to use to notify a received email in
	 * answer to this email
	 * 
	 * @param notifyWith
	 */
	public void setNotifyWith(final String notifyWith) {
		this.notifyWith = notifyWith;
	}
}
