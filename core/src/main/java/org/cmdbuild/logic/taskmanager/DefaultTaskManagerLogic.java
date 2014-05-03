package org.cmdbuild.logic.taskmanager;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.FluentIterable.from;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.task.TaskStore;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class DefaultTaskManagerLogic implements TaskManagerLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(DefaultTaskManagerLogic.class.getName());

	private static interface Action<T> {

		T execute();

	}

	private static class Create implements Action<Long>, TaskVistor {

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final SchedulerFacade schedulerFacade;
		private final SynchronousEventFacade synchronousEventFacade;
		private final Task task;

		public Create(final LogicAndStoreConverter converter, final TaskStore store,
				final SchedulerFacade schedulerFacade, final SynchronousEventFacade synchronousEventFacade,
				final Task task) {
			this.converter = converter;
			this.store = store;
			this.schedulerFacade = schedulerFacade;
			this.synchronousEventFacade = synchronousEventFacade;
			this.task = task;
		}

		@Override
		public Long execute() {
			final org.cmdbuild.data.store.task.Task storable = converter.from(task).toStore();
			final Storable created = store.create(storable);
			final org.cmdbuild.data.store.task.Task read = store.read(created);
			final Task taskWithId = converter.from(read).toLogic();
			taskWithId.accept(this);
			return read.getId();
		}

		@Override
		public void visit(final ReadEmailTask task) {
			schedulerFacade.create(task);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			schedulerFacade.create(task);
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			synchronousEventFacade.create(task);
		}

	}

	private static class ReadAll implements Action<Iterable<Task>> {

		private static Class<Object> ALL_TYPES = Object.class;

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final Class<?> type;

		public ReadAll(final LogicAndStoreConverter converter, final TaskStore store) {
			this(converter, store, null);
		}

		public ReadAll(final LogicAndStoreConverter converter, final TaskStore store, final Class<? extends Task> type) {
			this.converter = converter;
			this.store = store;
			this.type = (type == null) ? ALL_TYPES : type;
		}

		@Override
		public Iterable<Task> execute() {
			return from(store.list()) //
					.transform(toLogic()) //
					.filter(instanceOf(type)) //
					.toList();
		}

		private Function<org.cmdbuild.data.store.task.Task, Task> toLogic() {
			return new Function<org.cmdbuild.data.store.task.Task, Task>() {

				@Override
				public Task apply(final org.cmdbuild.data.store.task.Task input) {
					return converter.from(input).toLogic();
				}

			};
		}

	}

	private static class Read<T extends Task> implements Action<T> {

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final T task;
		private final Class<T> type;

		public Read(final LogicAndStoreConverter converter, final TaskStore store, final T task, final Class<T> type) {
			this.converter = converter;
			this.store = store;
			this.task = task;
			this.type = type;
		}

		@Override
		public T execute() {
			Validate.isTrue(task.getId() != null, "invalid id");
			final org.cmdbuild.data.store.task.Task stored = converter.from(task).toStore();
			final org.cmdbuild.data.store.task.Task read = store.read(stored);
			final Task raw = converter.from(read).toLogic();
			return type.cast(raw);
		}

	}

	private static class Update implements Action<Void> {

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final SchedulerFacade schedulerFacade;
		private final SynchronousEventFacade synchronousEventFacade;
		private final Task task;

		public Update(final LogicAndStoreConverter converter, final TaskStore store,
				final SchedulerFacade schedulerFacade, final SynchronousEventFacade synchronousEventFacade,
				final Task task) {
			this.converter = converter;
			this.store = store;
			this.schedulerFacade = schedulerFacade;
			this.synchronousEventFacade = synchronousEventFacade;
			this.task = task;
		}

		@Override
		public Void execute() {
			Validate.isTrue(task.getId() != null, "invalid id");
			final org.cmdbuild.data.store.task.Task storable = converter.from(task).toStore();
			final org.cmdbuild.data.store.task.Task read = store.read(storable);
			final Task previous = converter.from(read).toLogic();
			previous.accept(before());
			store.update(storable);
			task.accept(after());
			return null;
		}

		private TaskVistor before() {
			return new TaskVistor() {

				@Override
				public void visit(final ReadEmailTask task) {
					schedulerFacade.delete(task);
				}

				@Override
				public void visit(final StartWorkflowTask task) {
					schedulerFacade.delete(task);
				}

				@Override
				public void visit(final SynchronousEventTask task) {
					synchronousEventFacade.delete(task);
				}

			};
		}

		private TaskVistor after() {
			return new TaskVistor() {

				@Override
				public void visit(final ReadEmailTask task) {
					schedulerFacade.create(task);
				}

				@Override
				public void visit(final StartWorkflowTask task) {
					schedulerFacade.create(task);
				}

				@Override
				public void visit(final SynchronousEventTask task) {
					synchronousEventFacade.create(task);
				}

			};
		}

	}

	private static class Delete implements Action<Void>, TaskVistor {

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final SchedulerFacade schedulerFacade;
		private final SynchronousEventFacade synchronousEventFacade;
		private final Task task;

		public Delete(final LogicAndStoreConverter converter, final TaskStore store,
				final SchedulerFacade schedulerFacade, final SynchronousEventFacade synchronousEventFacade,
				final Task task) {
			this.converter = converter;
			this.store = store;
			this.schedulerFacade = schedulerFacade;
			this.synchronousEventFacade = synchronousEventFacade;
			this.task = task;
		}

		@Override
		public Void execute() {
			Validate.isTrue(task.getId() != null, "invalid id");
			task.accept(this);
			final org.cmdbuild.data.store.task.Task storable = converter.from(task).toStore();
			store.delete(storable);
			return null;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			schedulerFacade.delete(task);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			schedulerFacade.delete(task);
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			synchronousEventFacade.delete(task);
		}

	}

	private static class Activate implements Action<Void>, TaskVistor {

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final SchedulerFacade schedulerFacade;
		private final SynchronousEventFacade synchronousEventFacade;
		private final Long id;

		public Activate(final LogicAndStoreConverter converter, final TaskStore store,
				final SchedulerFacade schedulerFacade, final SynchronousEventFacade synchronousEventFacade,
				final Long id) {
			this.converter = converter;
			this.store = store;
			this.schedulerFacade = schedulerFacade;
			this.synchronousEventFacade = synchronousEventFacade;
			this.id = id;
		}

		@Override
		public Void execute() {
			Validate.isTrue(id != null, "invalid id");
			final org.cmdbuild.data.store.task.Task updated;
			final org.cmdbuild.data.store.task.Task stored = store.read(id);
			if (!stored.isRunning()) {
				updated = stored.modify() //
						.withRunningStatus(true) //
						.build();
			} else {
				updated = stored;
			}
			store.update(updated);
			final Task task = converter.from(updated).toLogic();
			task.accept(this);
			return null;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			schedulerFacade.create(task);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			schedulerFacade.create(task);
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			synchronousEventFacade.create(task);
		}

	}

	private static class Deactivate implements Action<Void>, TaskVistor {

		private final LogicAndStoreConverter converter;
		private final TaskStore store;
		private final SchedulerFacade schedulerFacade;
		private final SynchronousEventFacade synchronousEventFacade;
		private final Long id;

		public Deactivate(final LogicAndStoreConverter converter, final TaskStore store,
				final SchedulerFacade schedulerFacade, final SynchronousEventFacade synchronousEventFacade,
				final Long id) {
			this.converter = converter;
			this.store = store;
			this.schedulerFacade = schedulerFacade;
			this.synchronousEventFacade = synchronousEventFacade;
			this.id = id;
		}

		@Override
		public Void execute() {
			Validate.isTrue(id != null, "invalid id");
			final org.cmdbuild.data.store.task.Task updated;
			final org.cmdbuild.data.store.task.Task stored = store.read(id);
			if (stored.isRunning()) {
				updated = stored.modify() //
						.withRunningStatus(false) //
						.build();
				store.update(updated);
			} else {
				updated = stored;
			}
			final Task task = converter.from(updated).toLogic();
			task.accept(this);
			return null;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			schedulerFacade.delete(task);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			schedulerFacade.delete(task);
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			synchronousEventFacade.delete(task);
		}

	}

	private final LogicAndStoreConverter converter;
	private final TaskStore store;
	private final SchedulerFacade schedulerFacade;
	private final SynchronousEventFacade synchronousEventFacade;

	public DefaultTaskManagerLogic(final LogicAndStoreConverter converter, final TaskStore store,
			final SchedulerFacade schedulerFacade, final SynchronousEventFacade synchronousEventFacade) {
		this.converter = converter;
		this.store = store;
		this.schedulerFacade = schedulerFacade;
		this.synchronousEventFacade = synchronousEventFacade;
	}

	@Override
	public Long create(final Task task) {
		logger.info(MARKER, "creating a new task '{}'", task);
		return execute(doCreate(task));
	}

	@Override
	public Iterable<Task> read() {
		logger.info(MARKER, "reading all existing tasks");
		return execute(doReadAll());
	}

	@Override
	public Iterable<Task> read(final Class<? extends Task> type) {
		logger.info(MARKER, "reading all existing tasks for type '{}'", type);
		return execute(doReadAll(type));
	}

	@Override
	public <T extends Task> T read(final T task, final Class<T> type) {
		logger.info(MARKER, "reading task's details of '{}'", task);
		return execute(doRead(task, type));
	}

	@Override
	public void update(final Task task) {
		logger.info(MARKER, "updating an existing task '{}'", task);
		execute(doUpdate(task));
	}

	@Override
	public void delete(final Task task) {
		logger.info(MARKER, "deleting an existing task '{}'", task);
		execute(doDelete(task));
	}

	@Override
	public void activate(final Long id) {
		logger.info(MARKER, "activating the existing task '{}'", id);
		execute(doActivate(id));
	}

	@Override
	public void deactivate(final Long id) {
		logger.info(MARKER, "deactivating the existing task '{}'", id);
		execute(doDeactivate(id));
	}

	private Create doCreate(final Task task) {
		return new Create(converter, store, schedulerFacade, synchronousEventFacade, task);
	}

	private ReadAll doReadAll() {
		return new ReadAll(converter, store);
	}

	private ReadAll doReadAll(final Class<? extends Task> type) {
		return new ReadAll(converter, store, type);
	}

	private <T extends Task> Read<T> doRead(final T task, final Class<T> type) {
		return new Read<T>(converter, store, task, type);
	}

	private Update doUpdate(final Task task) {
		return new Update(converter, store, schedulerFacade, synchronousEventFacade, task);
	}

	private Delete doDelete(final Task task) {
		return new Delete(converter, store, schedulerFacade, synchronousEventFacade, task);
	}

	private Activate doActivate(final Long id) {
		return new Activate(converter, store, schedulerFacade, synchronousEventFacade, id);
	}

	private Deactivate doDeactivate(final Long id) {
		return new Deactivate(converter, store, schedulerFacade, synchronousEventFacade, id);
	}

	private <T> T execute(final Action<T> action) {
		return action.execute();
	}

}
