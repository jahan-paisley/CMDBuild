package org.cmdbuild.common.mail;

import org.cmdbuild.common.Builder;

class DefaultFetchedMail implements FetchedMail {

	public static class DefaultFetchedMailBuilder implements Builder<DefaultFetchedMail> {

		private String id;
		private String folder;
		private String subject;

		private DefaultFetchedMailBuilder() {
			// prevents instantiation
		}

		@Override
		public DefaultFetchedMail build() {
			return new DefaultFetchedMail(this);
		}

		public DefaultFetchedMailBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DefaultFetchedMailBuilder withFolder(final String folder) {
			this.folder = folder;
			return this;
		}

		public DefaultFetchedMailBuilder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

	}

	public static DefaultFetchedMailBuilder newInstance() {
		return new DefaultFetchedMailBuilder();
	}

	private final String id;
	private final String folder;
	private final String subject;

	public DefaultFetchedMail(final DefaultFetchedMailBuilder builder) {
		this.id = builder.id;
		this.folder = builder.folder;
		this.subject = builder.subject;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFolder() {
		return folder;
	}

	@Override
	public String getSubject() {
		return subject;
	}

}
