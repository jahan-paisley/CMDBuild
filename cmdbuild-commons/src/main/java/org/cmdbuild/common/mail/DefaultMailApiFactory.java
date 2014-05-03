package org.cmdbuild.common.mail;

/**
 * Default Mail API factory.
 */
public class DefaultMailApiFactory extends MailApiFactory {

	@Override
	public MailApi createMailApi() {
		return new DefaultMailApi(getConfiguration());
	}

}
