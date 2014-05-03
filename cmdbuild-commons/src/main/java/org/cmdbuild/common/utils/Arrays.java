package org.cmdbuild.common.utils;

import org.apache.commons.lang3.ArrayUtils;

public class Arrays {

	private Arrays() {
		// prevents instantiation
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] append(final T[] original, final T element) {
		return (T[]) ArrayUtils.add(original, element);
	}

	public static <T> T[] addDistinct(final T[] original, final T element) {
		if (element == null) {
			return original;
		}
		for (final T origElement : original) {
			if (element.equals(origElement)) {
				return original;
			}
		}
		return append(original, element);
	}

}
