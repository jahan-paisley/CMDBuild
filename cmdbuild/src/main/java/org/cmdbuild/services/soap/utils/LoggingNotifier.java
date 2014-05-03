package org.cmdbuild.services.soap.utils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.utils.NotifierProxy.Notifier;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public class LoggingNotifier implements Notifier {

	private static final Logger logger = Log.SOAP;

	private static class Stringify {

		private final Object argument;

		public Stringify(final Object argument) {
			this.argument = argument;
		}

		@Override
		public String toString() {
			final boolean useToString = (argument instanceof String) || (argument instanceof Number)
					|| (argument instanceof Boolean);
			return useToString ? argument.toString() : ToStringBuilder.reflectionToString(argument,
					ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	@Override
	public void invoke(final String method, final Iterable<Object> arguments) {
		logger.trace("invoking '{}' with arguments:", method);
		for (final Object argument : arguments) {
			logger.trace("- {}", new Stringify(argument));
		}
	}

}