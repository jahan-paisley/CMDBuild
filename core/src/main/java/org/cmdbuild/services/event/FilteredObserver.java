package org.cmdbuild.services.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;

import com.google.common.base.Predicate;

public class FilteredObserver extends ForwardingObserver {

	private static class CardForFilterResolver implements Observer {

		private CMCard card;

		public CMCard get() {
			return card;
		}

		@Override
		public void afterCreate(final CMCard current) {
			this.card = current;
		}

		@Override
		public void beforeUpdate(final CMCard current, final CMCard next) {
			this.card = current;
		}

		@Override
		public void afterUpdate(final CMCard previous, final CMCard current) {
			this.card = current;
		}

		@Override
		public void beforeDelete(final CMCard current) {
			this.card = current;
		}

	}

	public static class Builder implements org.cmdbuild.common.Builder<FilteredObserver> {

		private Observer delegate;
		private Predicate<CMCard> filter;
		private Observer proxy;

		private Builder() {
			// use factory method
		}

		@Override
		public FilteredObserver build() {
			validate();
			if (filter != null) {
				final Object proxy = Proxy.newProxyInstance( //
						Builder.class.getClassLoader(), //
						new Class<?>[] { Observer.class }, //
						new InvocationHandler() {

							@Override
							public Object invoke(final Object proxy, final Method method, final Object[] args)
									throws Throwable {
								final CardForFilterResolver cardForFilterResolver = new CardForFilterResolver();
								method.invoke(cardForFilterResolver, args);
								final CMCard card = cardForFilterResolver.get();

								return filter.apply(card) ? method.invoke(delegate, args) : null;
							}

						});
				this.proxy = Observer.class.cast(proxy);
			} else {
				this.proxy = delegate;
			}
			return new FilteredObserver(this);
		}

		private void validate() {
			Validate.notNull(delegate, "missing delegate");
		}

		public Builder withDelegate(final Observer delegate) {
			this.delegate = delegate;
			return this;
		}

		public Builder withFilter(final Predicate<CMCard> filter) {
			this.filter = filter;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private FilteredObserver(final Builder builder) {
		super(builder.proxy);
	}

}
