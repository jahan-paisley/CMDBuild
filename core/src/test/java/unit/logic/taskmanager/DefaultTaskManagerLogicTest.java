package unit.logic.taskmanager;

import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.task.TaskStore;
import org.cmdbuild.data.store.task.TaskVisitor;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.LogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
import org.cmdbuild.logic.taskmanager.SynchronousEventFacade;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DefaultTaskManagerLogicTest {

	private static final class DummyStorableTask extends org.cmdbuild.data.store.task.Task {

		public static Builder<DummyStorableTask> newInstance() {
			return new Builder<DummyStorableTask>() {

				@Override
				public DummyStorableTask doBuild() {
					return new DummyStorableTask(this);
				}

			};
		}

		private DummyStorableTask(final Builder<? extends org.cmdbuild.data.store.task.Task> builder) {
			super(builder);
		}

		@Override
		public void accept(final TaskVisitor visitor) {
			// nothing to do
		}

		@Override
		protected Builder<? extends org.cmdbuild.data.store.task.Task> builder() {
			throw new UnsupportedOperationException();
		}

	}

	private static final org.cmdbuild.data.store.task.Task DUMMY_STORABLE_TASK = DummyStorableTask.newInstance() //
			.build();

	private static final Task DUMMY_TASK = mock(Task.class);

	private static final long ID = 42L;
	private static final long ANOTHER_ID = 123L;
	private static final String DESCRIPTION = "the description";
	private static final String NEW_DESCRIPTION = "new description";
	private static final boolean ACTIVE_STATUS = true;
	private static final String CRON_EXPRESSION = "cron expression";
	private static final String NEW_CRON_EXPRESSION = "new cron expression";

	private LogicAndStoreConverter converter;
	private LogicAndStoreConverter.LogicAsSourceConverter logicAsSourceConverter;
	private LogicAndStoreConverter.StoreAsSourceConverter storeAsSourceConverter;
	private TaskStore store;
	private SchedulerFacade scheduledTaskFacade;
	private SynchronousEventFacade synchronousEventFacade;
	private DefaultTaskManagerLogic taskManagerLogic;

	@Before
	public void setUp() throws Exception {
		logicAsSourceConverter = mock(LogicAndStoreConverter.LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toStore()) //
				.thenReturn(DUMMY_STORABLE_TASK);

		storeAsSourceConverter = mock(LogicAndStoreConverter.StoreAsSourceConverter.class);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(DUMMY_TASK);

		converter = mock(LogicAndStoreConverter.class);
		when(converter.from(any(Task.class))) //
				.thenReturn(logicAsSourceConverter);
		when(converter.from(any(org.cmdbuild.data.store.task.Task.class))) //
				.thenReturn(storeAsSourceConverter);

		store = mock(TaskStore.class);

		scheduledTaskFacade = mock(SchedulerFacade.class);

		synchronousEventFacade = mock(SynchronousEventFacade.class);

		taskManagerLogic = new DefaultTaskManagerLogic(converter, store, scheduledTaskFacade, synchronousEventFacade);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateTaskWithoutAnId() throws Exception {
		// given
		final Task task = mock(Task.class);
		when(task.getId()) //
				.thenReturn(null);

		// when
		taskManagerLogic.update(task);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteTaskWithoutAnId() throws Exception {
		// given
		final Task task = mock(Task.class);
		when(task.getId()) //
				.thenReturn(null);

		// when
		taskManagerLogic.delete(task);
	}

	@Test
	public void allTasksRead() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask first = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance().withId(ID) //
				.withDescription(DESCRIPTION) //
				.withRunningStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();
		final org.cmdbuild.data.store.task.StartWorkflowTask second = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance().withId(ANOTHER_ID) //
				.withDescription(DESCRIPTION) //
				.withRunningStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();
		when(store.list()) //
				.thenReturn(asList(first, second));

		// when
		final Iterable<? extends Task> readed = taskManagerLogic.read();

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).list();
		inOrder.verify(converter, times(2)).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verifyNoMoreInteractions();

		assertThat(size(readed), equalTo(2));
	}

	@Test
	public void specificTaskTypeReaded() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask first = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(1L) //
				.build();
		final org.cmdbuild.data.store.task.StartWorkflowTask second = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(2L) //
				.build();
		when(store.list()) //
				.thenReturn(asList(first, second));

		// when
		final Iterable<Task> readed = taskManagerLogic.read(Task.class);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).list();
		inOrder.verify(converter, times(2)).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verifyNoMoreInteractions();

		assertThat(size(readed), equalTo(2));
	}

	@Test
	public void taskDetailsRead() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.build();
		final org.cmdbuild.data.store.task.ReadEmailTask stored = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(ID) //
				.build();
		when(store.read(any(Storable.class))) //
				.thenReturn(stored);

		// when
		taskManagerLogic.read(task, Task.class);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).read(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(converter).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotReadTaskDetailsWithoutAnId() throws Exception {
		// given
		final Task task = mock(Task.class);
		when(task.getId()) //
				.thenReturn(null);

		// when
		taskManagerLogic.read(task, Task.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotActivateTaskWithoutAnId() throws Exception {
		// when
		taskManagerLogic.activate(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeactivateTaskWithoutAnId() throws Exception {
		// when
		taskManagerLogic.deactivate(null);
	}

	@Test
	public void scheduledTaskCreated() throws Exception {
		// given
		final ReadEmailTask newOne = ReadEmailTask.newInstance().build();
		final org.cmdbuild.data.store.task.ReadEmailTask createdOne = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance().build();
		final ReadEmailTask convertedAfterRead = ReadEmailTask.newInstance().build();
		final Storable storable = mock(Storable.class);
		when(store.create(DUMMY_STORABLE_TASK)) //
				.thenReturn(storable);
		when(store.read(storable)) //
				.thenReturn(createdOne);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(convertedAfterRead);

		// when
		taskManagerLogic.create(newOne);

		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(converter).from(newOne);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).create(DUMMY_STORABLE_TASK);
		inOrder.verify(store).read(storable);
		inOrder.verify(converter).from(createdOne);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).create(convertedAfterRead);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduledTaskUpdated() throws Exception {
		// given
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(ReadEmailTask.newInstance() //
						.withId(ID) //
						.withDescription("should be deleted from scheduler facade") //
						.build() //
				);
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.withDescription(NEW_DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(NEW_CRON_EXPRESSION) //
				.build();

		// when
		taskManagerLogic.update(task);

		// then
		final ArgumentCaptor<ScheduledTask> scheduledTaskCaptor = ArgumentCaptor.forClass(ScheduledTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).read(DUMMY_STORABLE_TASK);
		inOrder.verify(converter).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).delete(scheduledTaskCaptor.capture());
		inOrder.verify(store).update(DUMMY_STORABLE_TASK);
		inOrder.verify(scheduledTaskFacade).create(task);
		inOrder.verifyNoMoreInteractions();

		final ScheduledTask captured = scheduledTaskCaptor.getValue();
		assertThat(captured.getDescription(), equalTo("should be deleted from scheduler facade"));
	}

	@Test
	public void scheduledTaskDeleted() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.build();

		// when
		taskManagerLogic.delete(task);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(scheduledTaskFacade).delete(task);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).delete(DUMMY_STORABLE_TASK);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduledTaskNotStoredWhenActivatedIfAlreadyInThatState() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask stored = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(true) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(ReadEmailTask.newInstance() //
						.withId(ID) //
						.build());

		// when
		taskManagerLogic.activate(ID);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(42L));
		inOrder.verify(converter).from(stored);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).create(any(ReadEmailTask.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduledTaskStoredWhenActivated() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask stored = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(false) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(ReadEmailTask.newInstance() //
						.withId(ID) //
						.withActiveStatus(true) //
						.build());

		// when
		taskManagerLogic.activate(ID);

		// then
		final ArgumentCaptor<org.cmdbuild.data.store.task.Task> storedTaskCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.data.store.task.Task.class);
		final ArgumentCaptor<ScheduledTask> scheduledTaskCaptor = ArgumentCaptor.forClass(ScheduledTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(ID));
		inOrder.verify(store).update(storedTaskCaptor.capture());
		inOrder.verify(converter).from(storedTaskCaptor.capture());
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).create(scheduledTaskCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final org.cmdbuild.data.store.task.Task updatedTask = storedTaskCaptor.getAllValues().get(0);
		assertThat(updatedTask.getId(), equalTo(ID));
		assertThat(updatedTask.isRunning(), is(true));

		final org.cmdbuild.data.store.task.Task convertedTask = storedTaskCaptor.getAllValues().get(1);
		assertThat(convertedTask.getId(), equalTo(ID));
		assertThat(convertedTask.isRunning(), is(true));

		final ScheduledTask scheduledTask = scheduledTaskCaptor.getValue();
		assertThat(scheduledTask.getId(), equalTo(ID));
		assertThat(scheduledTask.isActive(), is(true));
	}

	@Test
	public void scheduledTaskNotStoredWhenDeactivastedIfAlreadyInThatState() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask stored = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(false) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(ReadEmailTask.newInstance() //
						.withId(ID) //
						.build());

		// when
		taskManagerLogic.deactivate(ID);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(42L));
		inOrder.verify(converter).from(stored);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).delete(any(ReadEmailTask.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduledTaskStoredWhenDeactivasted() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask stored = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(true) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(ReadEmailTask.newInstance() //
						.withId(ID) //
						.withActiveStatus(false) //
						.build());

		// when
		taskManagerLogic.deactivate(ID);

		// then
		final ArgumentCaptor<org.cmdbuild.data.store.task.Task> storedTaskCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.data.store.task.Task.class);
		final ArgumentCaptor<ScheduledTask> scheduledTaskCaptor = ArgumentCaptor.forClass(ScheduledTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(ID));
		inOrder.verify(store).update(storedTaskCaptor.capture());
		inOrder.verify(converter).from(storedTaskCaptor.capture());
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).delete(scheduledTaskCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final org.cmdbuild.data.store.task.Task updatedTask = storedTaskCaptor.getAllValues().get(0);
		assertThat(updatedTask.getId(), equalTo(ID));
		assertThat(updatedTask.isRunning(), is(false));

		final org.cmdbuild.data.store.task.Task convertedTask = storedTaskCaptor.getAllValues().get(1);
		assertThat(convertedTask.getId(), equalTo(ID));
		assertThat(convertedTask.isRunning(), is(false));

		final ScheduledTask scheduledTask = scheduledTaskCaptor.getValue();
		assertThat(scheduledTask.getId(), equalTo(ID));
		assertThat(scheduledTask.isActive(), is(false));
	}

	@Test
	public void synchronousEventTaskCreated() throws Exception {
		// given
		final SynchronousEventTask newOne = SynchronousEventTask.newInstance().build();
		final org.cmdbuild.data.store.task.SynchronousEventTask createdOne = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance().build();
		final SynchronousEventTask convertedAfterRead = SynchronousEventTask.newInstance().build();
		final Storable storable = mock(Storable.class);
		when(store.create(DUMMY_STORABLE_TASK)) //
				.thenReturn(storable);
		when(store.read(storable)) //
				.thenReturn(createdOne);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(convertedAfterRead);

		// when
		taskManagerLogic.create(newOne);

		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(converter).from(newOne);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).create(DUMMY_STORABLE_TASK);
		inOrder.verify(store).read(storable);
		inOrder.verify(converter).from(createdOne);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(synchronousEventFacade).create(convertedAfterRead);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void synchronousEventTaskUpdated() throws Exception {
		// given
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(SynchronousEventTask.newInstance() //
						.withId(ID) //
						.withDescription("should be deleted from facade") //
						.build() //
				);
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(ID) //
				.withDescription(NEW_DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.build();

		// when
		taskManagerLogic.update(task);

		// then
		final ArgumentCaptor<SynchronousEventTask> taskCaptor = ArgumentCaptor.forClass(SynchronousEventTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).read(DUMMY_STORABLE_TASK);
		inOrder.verify(converter).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(synchronousEventFacade).delete(taskCaptor.capture());
		inOrder.verify(store).update(DUMMY_STORABLE_TASK);
		inOrder.verify(synchronousEventFacade).create(task);
		inOrder.verifyNoMoreInteractions();

		final Task captured = taskCaptor.getValue();
		assertThat(captured.getDescription(), equalTo("should be deleted from facade"));
	}

	@Test
	public void synchronousEventTaskDeleted() throws Exception {
		// given
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(ID) //
				.build();

		// when
		taskManagerLogic.delete(task);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(synchronousEventFacade).delete(task);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).delete(DUMMY_STORABLE_TASK);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void synchronousEventTaskNotStoredWhenActivatedIfAlreadyInThatState() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask stored = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(true) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(SynchronousEventTask.newInstance() //
						.withId(ID) //
						.build());

		// when
		taskManagerLogic.activate(ID);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(42L));
		inOrder.verify(converter).from(stored);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(synchronousEventFacade).create(any(SynchronousEventTask.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void synchronousEventTaskStoredWhenActivated() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask stored = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(false) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(SynchronousEventTask.newInstance() //
						.withId(ID) //
						.withActiveStatus(true) //
						.build());

		// when
		taskManagerLogic.activate(ID);

		// then
		final ArgumentCaptor<org.cmdbuild.data.store.task.Task> storedTaskCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.data.store.task.Task.class);
		final ArgumentCaptor<SynchronousEventTask> taskCaptor = ArgumentCaptor.forClass(SynchronousEventTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(ID));
		inOrder.verify(store).update(storedTaskCaptor.capture());
		inOrder.verify(converter).from(storedTaskCaptor.capture());
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(synchronousEventFacade).create(taskCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final org.cmdbuild.data.store.task.Task updatedTask = storedTaskCaptor.getAllValues().get(0);
		assertThat(updatedTask.getId(), equalTo(ID));
		assertThat(updatedTask.isRunning(), is(true));

		final org.cmdbuild.data.store.task.Task convertedTask = storedTaskCaptor.getAllValues().get(1);
		assertThat(convertedTask.getId(), equalTo(ID));
		assertThat(convertedTask.isRunning(), is(true));

		final Task task = taskCaptor.getValue();
		assertThat(task.getId(), equalTo(ID));
		assertThat(task.isActive(), is(true));
	}

	@Test
	public void synchronousEventTaskNotStoredWhenDeactivatedIfAlreadyInThatState() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask stored = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(false) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(SynchronousEventTask.newInstance() //
						.withId(ID) //
						.build());

		// when
		taskManagerLogic.deactivate(ID);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(42L));
		inOrder.verify(converter).from(stored);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(synchronousEventFacade).delete(any(SynchronousEventTask.class));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void synchronousEventTaskStoredWhenDeactivated() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask stored = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(ID) //
				.withRunningStatus(true) //
				.build();
		when(store.read(ID)) //
				.thenReturn(stored);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(SynchronousEventTask.newInstance() //
						.withId(ID) //
						.withActiveStatus(false) //
						.build());

		// when
		taskManagerLogic.deactivate(ID);

		// then
		final ArgumentCaptor<org.cmdbuild.data.store.task.Task> storedTaskCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.data.store.task.Task.class);
		final ArgumentCaptor<SynchronousEventTask> taskCaptor = ArgumentCaptor.forClass(SynchronousEventTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade, synchronousEventFacade);
		inOrder.verify(store).read(eq(ID));
		inOrder.verify(store).update(storedTaskCaptor.capture());
		inOrder.verify(converter).from(storedTaskCaptor.capture());
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(synchronousEventFacade).delete(taskCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final org.cmdbuild.data.store.task.Task updatedTask = storedTaskCaptor.getAllValues().get(0);
		assertThat(updatedTask.getId(), equalTo(ID));
		assertThat(updatedTask.isRunning(), is(false));

		final org.cmdbuild.data.store.task.Task convertedTask = storedTaskCaptor.getAllValues().get(1);
		assertThat(convertedTask.getId(), equalTo(ID));
		assertThat(convertedTask.isRunning(), is(false));

		final Task scheduledTask = taskCaptor.getValue();
		assertThat(scheduledTask.getId(), equalTo(ID));
		assertThat(scheduledTask.isActive(), is(false));
	}

}
