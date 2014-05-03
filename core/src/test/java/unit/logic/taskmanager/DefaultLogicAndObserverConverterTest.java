package unit.logic.taskmanager;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter.ObserverFactory;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.services.event.Observer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultLogicAndObserverConverterTest {

	private DefaultLogicAndObserverConverter converter;
	private ObserverFactory factory;

	@Before
	public void setUp() throws Exception {
		factory = mock(ObserverFactory.class);
		converter = new DefaultLogicAndObserverConverter(factory);
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance().build();

		final Observer created = mock(Observer.class);
		when(factory.create(task)) //
				.thenReturn(created);

		// when
		converter.from(task).toObserver();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task);
		inOrder.verifyNoMoreInteractions();
	}

}
