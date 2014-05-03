package org.cmdbuild.common.utils;

import static java.util.Arrays.asList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

import org.apache.commons.lang3.Validate;

public class NotifierProxy<T> {

	public static interface Notifier {

		void invoke(String method, Iterable<Object> arguments);

	}

	public static class Builder<T> implements org.cmdbuild.common.Builder<NotifierProxy<T>> {

		private Class<T> type;
		private T delegate;
		private Notifier notifier;

		@Override
		public NotifierProxy<T> build() {
			validate();
			return new NotifierProxy<T>(this);
		}

		private void validate() {
			Validate.notNull(type, "invalid type");
			Validate.notNull(delegate, "invalid delegate");
			Validate.notNull(notifier, "invalid notifier");
		}

		public Builder<T> withType(final Class<T> type) {
			setType(type);
			return this;
		}

		public void setType(final Class<T> type) {
			this.type = type;
		}

		public Builder<T> withDelegate(final T delegate) {
			setDelegate(delegate);
			return this;
		}

		public void setDelegate(final T delegate) {
			this.delegate = delegate;
		}

		public Builder<T> withNotifier(final Notifier notifier) {
			setNotifier(notifier);
			return this;
		}

		public void setNotifier(final Notifier notifier) {
			this.notifier = notifier;
		}

	}

	public static <T> Builder<T> newInstance() {
		return new Builder<T>();
	}

	private static final Iterable<Object> NO_ARGUMENTS = Collections.emptyList();

	private final Class<T> type;
	private final T delegate;
	private final Notifier notifier;

	public NotifierProxy(final Builder<T> builder) {
		this.type = builder.type;
		this.delegate = builder.delegate;
		this.notifier = builder.notifier;
	}

	public T get() {
		final Object proxy = Proxy.newProxyInstance( //
				type.getClassLoader(), //
				new Class<?>[] { type }, //
				new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
						notifier.invoke(method.getName(), (args == null) ? NO_ARGUMENTS : asList(args));
						return method.invoke(delegate, args);
					}

				});
		return type.cast(proxy);
	}

}
