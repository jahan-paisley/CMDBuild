package org.cmdbuild.common.utils.guava;

import com.google.common.base.Function;

public final class Functions {

	private Functions() {
		// prevents instantiation
	}

	public static Function<String, String> trim() {
		return StringFunctions.TRIM;
	}

	private enum StringFunctions implements Function<String, String> {

		TRIM {

			@Override
			protected String doApply(final String input) {
				return input.trim();
			}

		}, //
		;

		@Override
		public String apply(final String input) {
			return doApply(input);
		}

		protected abstract String doApply(final String input);

	}

}
