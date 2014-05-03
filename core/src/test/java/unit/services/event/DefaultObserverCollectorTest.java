package unit.services.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.event.DefaultObserverCollector;
import org.cmdbuild.services.event.ObserverCollector.IdentifiableObserver;
import org.junit.Before;
import org.junit.Test;

public class DefaultObserverCollectorTest {

	private static final CMCard DUMMY_CARD = mock(CMCard.class);

	private DefaultObserverCollector observerCollector;

	@Before
	public void setUp() {
		observerCollector = new DefaultObserverCollector();
	}

	@Test
	public void twoElementsAddedThenBothAreInvoked() throws Exception {
		// given
		final IdentifiableObserver foo = identifiableObserver("foo");
		final IdentifiableObserver bar = identifiableObserver("bar");
		observerCollector.add(foo);
		observerCollector.add(bar);

		// when
		observerCollector.allInOneObserver().afterCreate(DUMMY_CARD);

		// then
		verify(foo).getIdentifier();
		verify(bar).getIdentifier();
		verify(foo).afterCreate(DUMMY_CARD);
		verify(bar).afterCreate(DUMMY_CARD);
		verifyNoMoreInteractions(foo, bar);
	}

	@Test
	public void twoElementsAddedThenOneRemovedThenOnlyOneIsInvoked() throws Exception {
		// given
		final IdentifiableObserver foo = identifiableObserver("foo");
		final IdentifiableObserver bar = identifiableObserver("bar");
		observerCollector.add(foo);
		observerCollector.add(bar);
		observerCollector.remove(bar);

		// when
		observerCollector.allInOneObserver().afterCreate(DUMMY_CARD);

		// then
		verify(foo).getIdentifier();
		verify(bar, times(2)).getIdentifier();
		verify(foo).afterCreate(DUMMY_CARD);
		verifyNoMoreInteractions(foo, bar);
	}

	private IdentifiableObserver identifiableObserver(final String identifier) {
		final IdentifiableObserver mock = mock(IdentifiableObserver.class, identifier);
		when(mock.getIdentifier()) //
				.thenReturn(identifier);
		return mock;
	}

}
