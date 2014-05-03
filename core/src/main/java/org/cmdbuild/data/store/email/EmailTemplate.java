package org.cmdbuild.data.store.email;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.model.email.EmailConstants;

public class EmailTemplate implements Storable {

	public static class Builder implements org.cmdbuild.common.Builder<EmailTemplate> {

		private Long id;
		private String name;
		private String description;
		private String from;
		private String to;
		private String cc;
		private String bcc;
		private String subject;
		private String body;

		private Builder() {
			// use static method
		}

		@Override
		public EmailTemplate build() {
			validate();
			return new EmailTemplate(this);
		}

		private void validate() {
			Validate.isTrue(isNotBlank(name), "invalid name");
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withFrom(final String from) {
			this.from = from;
			return this;
		}

		public Builder withTo(final String to) {
			this.to = to;
			return this;
		}

		public Builder withCc(final String cc) {
			this.cc = cc;
			return this;
		}

		public Builder withBcc(final String bcc) {
			this.bcc = bcc;
			return this;
		}

		public Builder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public Builder withBody(final String body) {
			this.body = body;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String name;
	private final String description;
	private final String from;
	private final String to;
	private final String cc;
	private final String bcc;
	private final String subject;
	private final String body;

	private EmailTemplate(final Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.description = builder.description;
		this.from = builder.from;
		this.to = builder.to;
		this.cc = builder.cc;
		this.bcc = builder.bcc;
		this.subject = builder.subject;
		this.body = builder.body;
	}

	@Override
	public String getIdentifier() {
		return this.getName();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	/**
	 * Read the "TO" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	public List<String> getToAddresses() {
		return Arrays.asList(getTo().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	public String getCc() {
		return cc;
	}

	/**
	 * Read the "CC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	public List<String> getCCAddresses() {
		return Arrays.asList(getCc().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	public String getBcc() {
		return bcc;
	}

	/**
	 * Read the "BCC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	public List<String> getBCCAddresses() {
		return Arrays.asList(getBcc().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
