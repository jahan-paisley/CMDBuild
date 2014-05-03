package org.cmdbuild.common.template;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Function;

public class Functions {

	private static class SimpleEvalFunction implements Function<String, String> {

		private final TemplateResolver templateResolver;

		public SimpleEvalFunction(final TemplateResolver templateResolver) {
			Validate.notNull(templateResolver, "missing template resolver");
			this.templateResolver = templateResolver;
		}

		@Override
		public String apply(final String input) {
			return templateResolver.resolve(input);
		}

	}

	public static Function<String, String> simpleEval(final TemplateResolver templateResolver) {
		return new SimpleEvalFunction(templateResolver);
	}

	private Functions() {
		// prevents instantiation
	}

}
