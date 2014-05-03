package unit.logic.taskmanager;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.JobFactory;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.scheduler.Job;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultLogicAndSchedulerConverterTest {

	private DefaultLogicAndSchedulerConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new DefaultLogicAndSchedulerConverter();
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance().build();

		final Job job = mock(Job.class);
		final JobFactory<ReadEmailTask> factory = mock(JobFactory.class);
		when(factory.create(task)) //
				.thenReturn(job);
		converter.register(ReadEmailTask.class, factory);

		// when
		converter.from(task).toJob();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance().build();

		final Job job = mock(Job.class);
		final JobFactory<StartWorkflowTask> factory = mock(JobFactory.class);
		when(factory.create(task)) //
				.thenReturn(job);
		converter.register(StartWorkflowTask.class, factory);

		// when
		converter.from(task).toJob();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task);
		inOrder.verifyNoMoreInteractions();
	}

}
