package unit.logic.taskmanager;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.taskmanager.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.LogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.LogicAndSchedulerConverter.LogicAsSourceConverter;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DefaultSchedulerFacadeTest {

	private SchedulerService schedulerService;
	private LogicAndSchedulerConverter converter;

	private DefaultSchedulerFacade schedulerFacade;

	@Before
	public void setUp() throws Exception {
		schedulerService = mock(SchedulerService.class);
		converter = mock(LogicAndSchedulerConverter.class);
		schedulerFacade = new DefaultSchedulerFacade(schedulerService, converter);
	}

	@Test
	public void scheduleCreatedOnlyIfTaskIsActive() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.build();

		// when
		schedulerFacade.create(task);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduleCreated() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.create(task);

		// then
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).add(eq(job), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedTrigger, instanceOf(RecurringTrigger.class));
		assertThat(RecurringTrigger.class.cast(capturedTrigger).getCronExpression(), endsWith("cron expression"));
	}

	@Test
	public void secondsAddedToSpecifiedCronExpression() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.withCronExpression("<actual cron expression>") //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.create(task);

		// then
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).add(eq(job), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedTrigger, instanceOf(RecurringTrigger.class));
		assertThat(RecurringTrigger.class.cast(capturedTrigger).getCronExpression(),
				equalTo("0 <actual cron expression>"));
	}

	@Test
	public void scheduleDeletedOnlyIfTaskIsActive() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.build();

		// when
		schedulerFacade.delete(task);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduleDeleted() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.delete(task);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).remove(job);
		inOrder.verifyNoMoreInteractions();
	}

}
