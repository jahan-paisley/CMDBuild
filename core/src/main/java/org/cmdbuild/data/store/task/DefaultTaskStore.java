package org.cmdbuild.data.store.task;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.cmdbuild.data.store.task.TaskParameterGroupable.groupedBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps.EntryTransformer;

/**
 * This {@link Store} handles the saving process of {@link Task} elements.
 * 
 * @since 2.2
 */
public class DefaultTaskStore implements TaskStore {

	private static final Marker MARKER = MarkerFactory.getMarker(DefaultTaskStore.class.getName());

	private static final Function<TaskParameter, String> TASK_PARAMETER_TO_KEY = new Function<TaskParameter, String>() {

		@Override
		public String apply(final TaskParameter input) {
			return input.getKey();
		}

	};

	private static final Function<TaskParameter, String> TASK_PARAMETER_TO_VALUE = new Function<TaskParameter, String>() {

		@Override
		public String apply(final TaskParameter input) {
			return input.getValue();
		}

	};

	private static final Function<TaskParameter, String> BY_NAME = TASK_PARAMETER_TO_KEY;

	private static interface Action<T> {

		T execute();

	}

	private static abstract class AbstractAction<T> implements Action<T> {

		protected final Store<TaskDefinition> definitionsStore;
		protected final Store<TaskParameter> parametersStore;

		protected AbstractAction(final Store<TaskDefinition> definitionsStore,
				final Store<TaskParameter> parametersStore) {
			this.definitionsStore = definitionsStore;
			this.parametersStore = parametersStore;
		}

		protected TaskDefinition definitionOf(final Task task) {
			return new TaskVisitor() {

				private TaskDefinition.Builder<? extends TaskDefinition> builder;

				public TaskDefinition.Builder<? extends TaskDefinition> builder() {
					task.accept(this);
					Validate.notNull(builder, "cannot create builder");
					return builder;
				}

				@Override
				public void visit(final ReadEmailTask task) {
					builder = ReadEmailTaskDefinition.newInstance();
				}

				@Override
				public void visit(final StartWorkflowTask task) {
					builder = StartWorkflowTaskDefinition.newInstance();
				}

				@Override
				public void visit(final SynchronousEventTask task) {
					builder = SynchronousEventTaskDefinition.newInstance();
				}

			}.builder() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunning(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.build();
		}

		protected Task of(final TaskDefinition definition, final Iterable<TaskParameter> parameters) {

			return new TaskDefinitionVisitor() {

				private Task.Builder<? extends Task> builder;

				public Task.Builder<? extends Task> builder() {
					definition.accept(this);
					Validate.notNull(builder, "cannot create builder");
					return builder;
				}

				@Override
				public void visit(final ReadEmailTaskDefinition taskDefinition) {
					builder = ReadEmailTask.newInstance();
				}

				@Override
				public void visit(final StartWorkflowTaskDefinition taskDefinition) {
					builder = StartWorkflowTask.newInstance();
				}

				@Override
				public void visit(final SynchronousEventTaskDefinition taskDefinition) {
					builder = SynchronousEventTask.newInstance();
				}

			}.builder() //
					.withId(definition.getId()) //
					.withDescription(definition.getDescription()) //
					.withRunningStatus(definition.isRunning()) //
					.withCronExpression(definition.getCronExpression()) //
					.withParameters(transformValues( //
							uniqueIndex(parameters, TASK_PARAMETER_TO_KEY), //
							TASK_PARAMETER_TO_VALUE)) //
					.build();
		}

		protected EntryTransformer<String, String, TaskParameter> toTaskParameterMapOf(final TaskDefinition definition) {
			return new EntryTransformer<String, String, TaskParameter>() {

				@Override
				public TaskParameter transformEntry(final String key, final String value) {
					return TaskParameter.newInstance().withOwner(definition.getId()) //
							.withKey(key) //
							.withValue(value) //
							.build();
				}

			};
		}

	}

	private static class Create extends AbstractAction<Storable> {

		private static final Iterable<TaskParameter> NOT_NEEDED = Collections.emptyList();

		private final Task storable;

		public Create(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Task storable) {
			super(definitions, parametersStore);
			this.storable = storable;
		}

		@Override
		public Storable execute() {
			final TaskDefinition definition = definitionOf(storable);
			final Storable createdDefinition = definitionsStore.create(definition);
			final TaskDefinition readedDefinition = definitionsStore.read(createdDefinition);
			for (final TaskParameter element : transformEntries(storable.getParameters(),
					toTaskParameterMapOf(readedDefinition)).values()) {
				parametersStore.create(element);
			}
			return of(readedDefinition, NOT_NEEDED);
		}
	}

	private static class Read extends AbstractAction<Task> {

		private final Storable storable;

		public Read(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Storable storable) {
			super(definitions, parametersStore);
			this.storable = storable;
		}

		@Override
		public Task execute() {
			final Task task = Task.class.cast(storable);
			final TaskDefinition definition = definitionsStore.read(definitionOf(task));
			final Iterable<TaskParameter> parameters = parametersStore.list(groupedBy(definition));
			return of(definition, parameters);
		}

	}

	private static class ReadAll extends AbstractAction<List<Task>> {

		private final Groupable groupable;

		public ReadAll(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore) {
			this(definitions, parametersStore, null);
		}

		public ReadAll(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Groupable groupable) {
			super(definitions, parametersStore);
			this.groupable = groupable;
		}

		@Override
		public List<Task> execute() {
			final Iterable<TaskDefinition> list = (groupable == null) ? definitionsStore.list() : definitionsStore
					.list(groupable);
			return from(list) //
					.transform(new Function<TaskDefinition, Task>() {

						@Override
						public Task apply(final TaskDefinition input) {
							final Iterable<TaskParameter> parameters = parametersStore.list(groupedBy(input));
							return of(input, parameters);
						}

					}) //
					.toList();
		}

	}

	private static class Update extends AbstractAction<Void> {

		private final Task storable;

		public Update(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Task storable) {
			super(definitions, parametersStore);
			this.storable = storable;
		}

		@Override
		public Void execute() {
			final TaskDefinition definition = definitionOf(storable);
			definitionsStore.update(definition);

			final Map<String, TaskParameter> left = transformEntries(storable.getParameters(),
					toTaskParameterMapOf(definition));
			final Map<String, TaskParameter> right = uniqueIndex(parametersStore.list(groupedBy(definition)), BY_NAME);
			final MapDifference<String, TaskParameter> difference = difference(left, right);
			for (final TaskParameter element : difference.entriesOnlyOnLeft().values()) {
				parametersStore.create(element);
			}
			for (final ValueDifference<TaskParameter> valueDifference : difference.entriesDiffering().values()) {
				final TaskParameter element = valueDifference.leftValue();
				parametersStore.update(TaskParameter.newInstance() //
						.withId(valueDifference.rightValue().getId()) //
						.withOwner(element.getOwner()) //
						.withKey(element.getKey()) //
						.withValue(element.getValue()) //
						.build());
			}
			for (final TaskParameter element : difference.entriesOnlyOnRight().values()) {
				parametersStore.delete(element);
			}
			return null;
		}

	}

	private static class Delete extends AbstractAction<Void> {

		private final Storable storable;

		public Delete(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Storable storable) {
			super(definitions, parametersStore);
			this.storable = storable;
		}

		@Override
		public Void execute() {
			Validate.isInstanceOf(Task.class, storable);
			final Task task = Task.class.cast(storable);
			final TaskDefinition definition = definitionsStore.read(definitionOf(task));
			for (final TaskParameter element : parametersStore.list(groupedBy(definition))) {
				parametersStore.delete(element);
			}
			definitionsStore.delete(definition);
			return null;
		}

	}

	private static class ReadById extends AbstractAction<Task> {

		private final Long id;

		public ReadById(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Long id) {
			super(definitions, parametersStore);
			this.id = id;
		}

		@Override
		public Task execute() {
			for (final TaskDefinition definition : definitionsStore.list()) {
				if (definition.getId().equals(id)) {
					final Iterable<TaskParameter> parameters = parametersStore.list(groupedBy(definition));
					return of(definition, parameters);
				}
			}
			throw new NoSuchElementException();
		}

	}

	private final Store<TaskDefinition> definitionsStore;
	private final Store<TaskParameter> parametersStore;

	public DefaultTaskStore(final Store<TaskDefinition> definitionsStore, final Store<TaskParameter> parametersStore) {
		this.definitionsStore = definitionsStore;
		this.parametersStore = parametersStore;
	}

	@Override
	public Storable create(final Task storable) {
		logger.info(MARKER, "creating new element '{}'", storable);
		return execute(doCreate(storable));
	}

	@Override
	public Task read(final Storable storable) {
		logger.info(MARKER, "reading existing element '{}'", storable);
		return execute(doRead(storable));
	}

	@Override
	public List<Task> list() {
		logger.info(MARKER, "getting all existing elements");
		return execute(doReadAll());
	}

	@Override
	public List<Task> list(final Groupable groupable) {
		logger.info(MARKER, "getting all existing elements for group '{}'", groupable);
		return execute(doReadAll(groupable));
	}

	@Override
	public void update(final Task storable) {
		logger.info(MARKER, "updating existing element '{}'", storable);
		execute(doUpdate(storable));
	}

	@Override
	public void delete(final Storable storable) {
		logger.info(MARKER, "deleting existing element '{}'", storable);
		execute(doDelete(storable));
	}

	@Override
	public Task read(final Long id) {
		logger.info(MARKER, "reading existing element with id '{}'", id);
		return execute(doRead(id));
	}

	private Create doCreate(final Task storable) {
		return new Create(definitionsStore, parametersStore, storable);
	}

	private Read doRead(final Storable storable) {
		return new Read(definitionsStore, parametersStore, storable);
	}

	private ReadAll doReadAll() {
		return new ReadAll(definitionsStore, parametersStore);
	}

	private ReadAll doReadAll(final Groupable groupable) {
		return new ReadAll(definitionsStore, parametersStore, groupable);
	}

	private Update doUpdate(final Task storable) {
		return new Update(definitionsStore, parametersStore, storable);
	}

	private Delete doDelete(final Storable storable) {
		return new Delete(definitionsStore, parametersStore, storable);
	}

	private ReadById doRead(final Long id) {
		return new ReadById(definitionsStore, parametersStore, id);
	}

	private <T> T execute(final Action<T> action) {
		return action.execute();
	}

}
