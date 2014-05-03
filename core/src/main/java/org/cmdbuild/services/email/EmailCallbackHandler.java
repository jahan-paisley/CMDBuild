package org.cmdbuild.services.email;

import org.cmdbuild.model.email.Email;

import com.google.common.base.Predicate;

/**
 * Handler for {@link Email} reception.
 */
public interface EmailCallbackHandler extends Predicate<Email> {

	void accept(Email email);

}