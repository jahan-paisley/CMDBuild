package org.cmdbuild.data.store.email;

import static org.cmdbuild.data.store.email.EmailConstants.PROCESS_ID_ATTRIBUTE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Groupable;

public class EmailOwnerGroupable implements Groupable {

	public static EmailOwnerGroupable of(final Long owner) {
		return new EmailOwnerGroupable(owner);
	}

	private final Long owner;

	private EmailOwnerGroupable(final Long owner) {
		Validate.notNull(owner, "owner's id cannot be null");
		Validate.isTrue(owner > 0, "owner's id must be greater than zero");
		this.owner = owner;
	}

	@Override
	public String getGroupAttributeName() {
		return PROCESS_ID_ATTRIBUTE;
	}

	@Override
	public Object getGroupAttributeValue() {
		return owner;
	}

}