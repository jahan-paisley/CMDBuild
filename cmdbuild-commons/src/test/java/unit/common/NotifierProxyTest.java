package unit.common;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.cmdbuild.common.utils.NotifierProxy;
import org.cmdbuild.common.utils.NotifierProxy.Notifier;
import org.junit.Before;
import org.junit.Test;

public class NotifierProxyTest {

	public static interface Custom {

		public void foo();

		void bar(String s);

		void baz(String s, int i);

	}

	private static abstract class ForwardingCustom implements Custom {

		private final Custom delegate;

		protected ForwardingCustom(final Custom delegate) {
			this.delegate = delegate;
		}

		@Override
		public void foo() {
			delegate.foo();
		}

		@Override
		public void bar(final String s) {
			delegate.bar(s);
		}

		@Override
		public void baz(final String s, final int i) {
			delegate.baz(s, i);
		}

	}

	private Custom delegate;
	private Notifier notifier;
	private NotifierProxy.Builder<Custom> wellFormedBuilder;
	private Custom proxy;

	@Before
	public void setUp() throws Exception {
		delegate = mock(Custom.class);
		notifier = mock(Notifier.class);

		wellFormedBuilder = NotifierProxy.<Custom> newInstance() //
				.withType(Custom.class) //
				.withDelegate(new ForwardingCustom(delegate) {
				}) //
				.withNotifier(notifier);

		proxy = wellFormedBuilder.build().get();
	}

	@Test(expected = NullPointerException.class)
	public void typeMustBeSpecified() throws Exception {
		// given
		wellFormedBuilder.withType(null);

		// when
		wellFormedBuilder.build();
	}

	@Test(expected = NullPointerException.class)
	public void delegateMustBeSpecified() throws Exception {
		// given
		wellFormedBuilder.withDelegate(null);

		// when
		wellFormedBuilder.build();
	}

	@Test(expected = NullPointerException.class)
	public void notifierMustBeSpecified() throws Exception {
		// given
		wellFormedBuilder.withNotifier(null);

		// when
		wellFormedBuilder.build();
	}

	@Test
	public void methodWithNoArgumentsSuccessfullyNotified() throws Exception {
		// when
		proxy.foo();

		// then
		verify(delegate).foo();
		verify(notifier).invoke(eq("foo"), eq(Collections.emptyList()));
	}

	@Test
	public void methodWithOneArgumentSuccessfullyNotified() throws Exception {
		// when
		proxy.bar("test");

		// then
		verify(delegate).bar(eq("test"));
		verify(notifier).invoke(eq("bar"), eq(asList((Object) "test")));
	}

	@Test
	public void methodWithTwoArgumentsSuccessfullyNotified() throws Exception {
		// when
		proxy.baz("test", 42);

		// then
		verify(delegate).baz(eq("test"), eq(42));
		verify(notifier).invoke(eq("baz"), eq(asList((Object) "test", (Object) 42)));
	}

}
