package unit.services.event;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.event.FilteredObserver;
import org.cmdbuild.services.event.FilteredObserver.Builder;
import org.cmdbuild.services.event.Observer;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.base.Predicate;

public class FilteredObserverTest {

	@Test(expected = NullPointerException.class)
	public void missingDelegateThrowsException() {
		// given
		final Builder builder = FilteredObserver.newInstance();

		// when
		builder.build();
	}

	@Test
	public void missingFilterIsNotAProblem() {
		// given
		final Observer delegate = mock(Observer.class);
		final Builder builder = FilteredObserver.newInstance() //
				.withDelegate(delegate);

		// when
		builder.build();
	}

	@Test
	public void whenNoFilterIsSpecifiedDelegateMethodsAreSurelyCalled() {
		// given
		final Observer delegate = mock(Observer.class);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.build();
		final CMCard actual = mock(CMCard.class);
		final CMCard next = mock(CMCard.class);
		final CMCard previous = mock(CMCard.class);

		// when
		element.afterCreate(actual);
		element.beforeUpdate(actual, next);
		element.afterUpdate(previous, actual);
		element.beforeDelete(actual);

		// then
		final InOrder inOrder = inOrder(delegate);
		inOrder.verify(delegate).afterCreate(actual);
		inOrder.verify(delegate).beforeUpdate(actual, next);
		inOrder.verify(delegate).afterUpdate(previous, actual);
		inOrder.verify(delegate).beforeDelete(actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void afterCreateMethodOnDelegateNotCalledIfFilterDoesNotApplyToCard() {
		// given
		final CMCard actual = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(false);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.afterCreate(actual);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void afterCreateMethodOnDelegateCalledIfFilterAppliesToCard() {
		// given
		final CMCard actual = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(true);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.afterCreate(actual);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verify(delegate).afterCreate(actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void beforeUpdateMethodOnDelegateNotCalledIfFilterDoesNotApplyToCard() {
		// given
		final CMCard actual = mock(CMCard.class);
		final CMCard next = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(false);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.beforeUpdate(actual, next);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void beforeUpdateMethodOnDelegateCalledIfFilterAppliesToCard() {
		// given
		final CMCard actual = mock(CMCard.class);
		final CMCard next = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(true);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.beforeUpdate(actual, next);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verify(delegate).beforeUpdate(actual, next);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void afterUpdateMethodOnDelegateNotCalledIfFilterDoesNotApplyToCard() {
		// given
		final CMCard previous = mock(CMCard.class);
		final CMCard actual = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(false);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.afterUpdate(previous, actual);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void afterUpdateMethodOnDelegateCalledIfFilterAppliesToCard() {
		// given
		final CMCard previous = mock(CMCard.class);
		final CMCard actual = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(true);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.afterUpdate(previous, actual);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verify(delegate).afterUpdate(previous, actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void beforeDeleteMethodOnDelegateNotCalledIfFilterDoesNotApplyToCard() {
		// given
		final CMCard actual = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(false);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.beforeDelete(actual);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void beforeDeleteMethodOnDelegateCalledIfFilterAppliesToCard() {
		// given
		final CMCard actual = mock(CMCard.class);
		final Observer delegate = mock(Observer.class);
		final Predicate<CMCard> filter = mock(Predicate.class);
		when(filter.apply(actual)) //
				.thenReturn(true);
		final FilteredObserver element = FilteredObserver.newInstance() //
				.withDelegate(delegate) //
				.withFilter(filter) //
				.build();

		// when
		element.beforeDelete(actual);

		// then
		final InOrder inOrder = inOrder(delegate, filter);
		inOrder.verify(filter).apply(actual);
		inOrder.verify(delegate).beforeDelete(actual);
		inOrder.verifyNoMoreInteractions();
	}

}
