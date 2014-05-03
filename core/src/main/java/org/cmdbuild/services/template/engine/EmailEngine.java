package org.cmdbuild.services.template.engine;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.model.email.Email;

public class EmailEngine implements Engine {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<EmailEngine> {

		private Email email;

		private Builder() {
			// use factory method
		}

		@Override
		public EmailEngine build() {
			validate();
			return new EmailEngine(this);
		}

		private void validate() {
			Validate.notNull(email, "missing email");
		}

		public Builder withEmail(final Email email) {
			this.email = email;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static enum Element {
		from {

			@Override
			Object eval(final Email email) {
				return email.getFromAddress();
			}

		},
		to() {

			@Override
			Object eval(final Email email) {
				return email.getToAddresses();
			}

		}, //
		cc() {

			@Override
			Object eval(final Email email) {
				return email.getCcAddresses();
			}

		}, //
		bcc() {

			@Override
			Object eval(final Email email) {
				return email.getBccAddresses();
			}

		}, //
		date() {

			@Override
			Object eval(final Email email) {
				return email.getDate().toString();
			}

		}, //
		subject {

			@Override
			Object eval(final Email email) {
				return email.getSubject();
			}

		},
		content {

			@Override
			Object eval(final Email email) {
				return email.getContent();
			}

		},
		undefined() {

			@Override
			Object eval(final Email email) {
				return EMPTY;
			}

		}, //
		;

		private static Element of(final String expression) {
			for (final Element value : values()) {
				if (value.name().equals(expression)) {
					return value;
				}
			}
			throw new IllegalArgumentException("element not found");
		}

		abstract Object eval(Email email);

	}

	private final Email email;

	private EmailEngine(final Builder builder) {
		this.email = builder.email;
	}

	@Override
	public Object eval(final String expression) {
		return Element.of(expression).eval(email);
	}

}
