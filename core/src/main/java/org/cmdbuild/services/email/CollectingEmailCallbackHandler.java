package org.cmdbuild.services.email;

import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.email.Email;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class CollectingEmailCallbackHandler implements EmailCallbackHandler {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CollectingEmailCallbackHandler> {

		private Predicate<Email> predicate;

		private Builder() {
			// use factory method
		}

		@Override
		public CollectingEmailCallbackHandler build() {
			validate();
			return new CollectingEmailCallbackHandler(this);
		}

		private void validate() {
			Validate.notNull(predicate, "invalid predicate");
		}

		public Builder withPredicate(final Predicate<Email> predicate) {
			this.predicate = predicate;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Predicate<Email> predicate;
	private final Collection<Email> emails;

	public CollectingEmailCallbackHandler(final Builder builder) {
		this.predicate = builder.predicate;
		this.emails = Lists.newArrayList();
	}

	@Override
	public boolean apply(final Email input) {
		return predicate.apply(input);
	}

	@Override
	public void accept(final Email email) {
		emails.add(email);
	}

	public Iterable<Email> getEmails() {
		return emails;
	}

}
