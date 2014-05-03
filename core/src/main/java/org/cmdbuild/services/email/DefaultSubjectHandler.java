package org.cmdbuild.services.email;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.slf4j.Logger;

public class DefaultSubjectHandler implements SubjectHandler {

	private static final Logger logger = Log.EMAIL;

	private static final int ACTIVITY_ID_GROUP = 1;
	private static final int REAL_SUBJECT_GROUP = 3;

	private static final String REGEX_PATTERN = "[^\\[]*\\[\\s*(\\d+)\\s*\\](\\s+)?(.*)";
	private static final String FORMAT_PATTERN_WITH_ID = "[%d] %s";
	private static final String FORMAT_PATTERN_WITHOUT_ID = "%s";

	@Override
	public ParsedSubject parse(final String subject) {
		logger.debug("parsing subject '{}'", subject);
		final Pattern pattern = Pattern.compile(REGEX_PATTERN);
		final Matcher matcher = pattern.matcher(defaultIfBlank(subject, EMPTY));
		matcher.find();
		return new ParsedSubject() {

			@Override
			public boolean hasExpectedFormat() {
				return matcher.matches();
			}

			@Override
			public Long getEmailId() {
				Validate.isTrue(hasExpectedFormat(), "invalid format");
				return Long.parseLong(get(ACTIVITY_ID_GROUP));
			}

			@Override
			public String getRealSubject() {
				Validate.isTrue(hasExpectedFormat(), "invalid format");
				return get(REAL_SUBJECT_GROUP);
			}

			private String get(final int group) {
				return matcher.group(group);
			}

		};
	}

	@Override
	public CompiledSubject compile(final Email email) {
		logger.debug("compiling subject for email with id '{}'", email.getId());
		return new CompiledSubject() {

			@Override
			public String getSubject() {
				final String subject;
				if (email.getId() == null) {
					subject = String.format(FORMAT_PATTERN_WITHOUT_ID, email.getSubject());
				} else {
					subject = String.format(FORMAT_PATTERN_WITH_ID, email.getId(), email.getSubject());
				}
				return subject;
			}

		};
	}
}
