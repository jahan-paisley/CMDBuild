package org.cmdbuild.common.template.engine;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Map;

public class Engines {

	private static class EmptyStringOnNullEngine extends ForwardingEngine {

		public EmptyStringOnNullEngine(final Engine delegate) {
			super(delegate);
		}

		@Override
		public Object eval(final String expression) {
			return defaultIfNull(super.eval(expression), EMPTY);
		}

	}

	private static class MapEngine implements Engine {

		private final Map<String, ?> map;

		public MapEngine(final Map<String, ?> map) {
			this.map = map;
		}

		@Override
		public Object eval(final String expression) {
			return map.get(expression);
		}

	}

	private static class NullOnErrorEngine extends ForwardingEngine {

		public NullOnErrorEngine(final Engine delegate) {
			super(delegate);
		}

		@Override
		public Object eval(final String expression) {
			try {
				return super.eval(expression);
			} catch (final Throwable e) {
				// TODO log
				return null;
			}
		}

	}

	public static Engine emptyStringOnNull(final Engine delegate) {
		return new EmptyStringOnNullEngine(delegate);
	}

	public static Engine map(final Map<String, ?> map) {
		return new MapEngine(map);
	}

	public static Engine nullOnError(final Engine delegate) {
		return new NullOnErrorEngine(delegate);
	}

	private Engines() {
		// prevents instantiation
	}

}
