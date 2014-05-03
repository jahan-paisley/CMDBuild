package org.cmdbuild.data.store.email;

public class EmailConstants {

	private EmailConstants() {
		// prevents instantiation
	}

	public static final String EMAIL_CLASS_NAME = "Email";

	public static final String PROCESS_ID_ATTRIBUTE = "Activity";
	public static final String EMAIL_STATUS_ATTRIBUTE = "EmailStatus";
	public static final String FROM_ADDRESS_ATTRIBUTE = "FromAddress";
	public static final String TO_ADDRESSES_ATTRIBUTE = "ToAddresses";
	public static final String CC_ADDRESSES_ATTRIBUTE = "CcAddresses";
	public static final String SUBJECT_ATTRIBUTE = "Subject";
	public static final String CONTENT_ATTRIBUTE = "Content";
	public static final String NOTIFY_WITH = "NotifyWith";

}
