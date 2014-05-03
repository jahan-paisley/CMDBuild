package org.cmdbuild.common.mail;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import java.security.Security;

class DefaultMailApi implements MailApi {

	private final Configuration configuration;

	@SuppressWarnings("restriction")
	public DefaultMailApi(final Configuration configuration) {
		this.configuration = configuration;

		final MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	@Override
	public NewMail newMail() {
		return new DefaultNewMail(configuration);
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return new DefaultSelectFolder(configuration, folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return new DefaultSelectMail(configuration, mail);
	}

}
