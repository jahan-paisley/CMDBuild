package unit.logic.taskmanager;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.taskmanager.DefaultSynchronousEventFacade;
import org.cmdbuild.logic.taskmanager.LogicAndObserverConverter;
import org.cmdbuild.logic.taskmanager.LogicAndObserverConverter.LogicAsSourceConverter;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.services.event.Observer;
import org.cmdbuild.services.event.ObserverCollector;
import org.cmdbuild.services.event.ObserverCollector.IdentifiableObserver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DefaultSynchronousEventFacadeTest {

	private DefaultSynchronousEventFacade synchronousEventFacade;
	private ObserverCollector observerCollector;
	private LogicAndObserverConverter converter;

	@Before
	public void setUp() throws Exception {
		observerCollector = mock(ObserverCollector.class);
		converter = mock(LogicAndObserverConverter.class);
		synchronousEventFacade = new DefaultSynchronousEventFacade(observerCollector, converter);
	}

	@Test(expected = NullPointerException.class)
	public void taskCannotBeAddedIfIdIsMissing() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance().build();

		// when
		synchronousEventFacade.create(task);
	}

	@Test
	public void taskAddedOnlyIfTaskIsActive() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance().withId(42L).build();

		// when
		synchronousEventFacade.create(task);

		// then
		final InOrder inOrder = inOrder(observerCollector, converter);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void taskAdded() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(42L) //
				.withActiveStatus(true) //
				.build();
		final Observer observer = mock(Observer.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toObserver()) //
				.thenReturn(observer);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		synchronousEventFacade.create(task);

		// then
		final ArgumentCaptor<IdentifiableObserver> captor = ArgumentCaptor.forClass(IdentifiableObserver.class);
		final InOrder inOrder = inOrder(observerCollector, converter, logicAsSourceConverter);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toObserver();
		inOrder.verify(observerCollector).add(captor.capture());
		inOrder.verifyNoMoreInteractions();

		final IdentifiableObserver captured = captor.getValue();
		assertThat(captured.getIdentifier(), equalTo("42"));
	}

	@Test(expected = NullPointerException.class)
	public void taskCannotBeRemovedIfIdIsMissing() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance().build();

		// when
		synchronousEventFacade.delete(task);
	}

	@Test
	public void taskDeletedOnlyIfTaskIsActive() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance().withId(42L).build();

		// when
		synchronousEventFacade.delete(task);

		// then
		final InOrder inOrder = inOrder(observerCollector, converter);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void taskDeleted() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(42L) //
				.withActiveStatus(true) //
				.build();

		// when
		synchronousEventFacade.delete(task);

		// then
		final ArgumentCaptor<IdentifiableObserver> captor = ArgumentCaptor.forClass(IdentifiableObserver.class);
		final InOrder inOrder = inOrder(observerCollector, converter);
		inOrder.verify(observerCollector).remove(captor.capture());
		inOrder.verifyNoMoreInteractions();

		final IdentifiableObserver captured = captor.getValue();
		assertThat(captured.getIdentifier(), equalTo("42"));
	}

}
