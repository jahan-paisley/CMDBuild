package unit.data.store.task;

import static java.util.Arrays.asList;
import static org.cmdbuild.data.store.task.TaskParameterConverter.OWNER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.task.DefaultTaskStore;
import org.cmdbuild.data.store.task.ReadEmailTaskDefinition;
import org.cmdbuild.data.store.task.StartWorkflowTask;
import org.cmdbuild.data.store.task.StartWorkflowTaskDefinition;
import org.cmdbuild.data.store.task.Task;
import org.cmdbuild.data.store.task.TaskDefinition;
import org.cmdbuild.data.store.task.TaskParameter;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class DefaultTaskStoreTest {

	private static class TaskAssert {

		public static TaskAssert of(final Task underTest) {
			return new TaskAssert(underTest);
		}

		private final Task underTest;

		private TaskAssert(final Task underTest) {
			this.underTest = underTest;
		}

		public TaskAssert id(final Matcher<Long> matcher) {
			assertThat(underTest.getId(), matcher);
			return this;
		}

		public TaskAssert description(final Matcher<String> matcher) {
			assertThat(underTest.getDescription(), matcher);
			return this;
		}

		public TaskAssert valueOfParameter(final String key, final Matcher<String> matcher) {
			assertThat(underTest.getParameters(), hasKey(key));
			assertThat(underTest.getParameters().get(key), matcher);
			return this;
		}

	}

	private static class TaskDefinitionAssert {

		public static TaskDefinitionAssert of(final TaskDefinition underTest) {
			return new TaskDefinitionAssert(underTest);
		}

		private final TaskDefinition underTest;

		private TaskDefinitionAssert(final TaskDefinition underTest) {
			this.underTest = underTest;
		}

		public TaskDefinitionAssert id(final Matcher<Long> matcher) {
			assertThat(underTest.getId(), matcher);
			return this;
		}

		public TaskDefinitionAssert description(final Matcher<String> matcher) {
			assertThat(underTest.getDescription(), matcher);
			return this;
		}

		public TaskDefinitionAssert runningStatus(final Matcher<Boolean> matcher) {
			assertThat(underTest.isRunning(), matcher);
			return this;
		}

		public TaskDefinitionAssert cronExpression(final Matcher<String> matcher) {
			assertThat(underTest.getCronExpression(), matcher);
			return this;
		}

	}

	private static class TaskParameterAssert {

		public static TaskParameterAssert of(final Iterable<TaskParameter> underTest) {
			return new TaskParameterAssert(underTest);
		}

		private final Map<String, TaskParameter> underTest;

		private TaskParameterAssert(final Iterable<TaskParameter> underTest) {
			this.underTest = Maps.uniqueIndex(underTest, new Function<TaskParameter, String>() {

				@Override
				public String apply(final TaskParameter input) {
					return input.getKey();
				}

			});
		}

		public TaskParameterAssert hasParameter(final String key) {
			assertThat(underTest, hasKey(key));
			return this;
		}

		public TaskParameterAssert ownerOfParameter(final String key, final Matcher<Long> matcher) {
			assertThat(underTest, hasKey(key));
			assertThat(underTest.get(key).getOwner(), matcher);
			return this;
		}

		public TaskParameterAssert valueOfParameter(final String key, final Matcher<String> matcher) {
			assertThat(underTest, hasKey(key));
			assertThat(underTest.get(key).getValue(), matcher);
			return this;
		}

	}

	private Store<TaskDefinition> definitionsStore;
	private Store<TaskParameter> parametersStore;
	private DefaultTaskStore store;

	@Before
	public void setUp() throws Exception {
		definitionsStore = mock(Store.class);
		parametersStore = mock(Store.class);
		store = new DefaultTaskStore(definitionsStore, parametersStore);
	}

	@Test
	public void elementCreated() throws Exception {
		// given
		final StartWorkflowTask newOne = StartWorkflowTask.newInstance() //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.build();
		when(definitionsStore.read(any(Storable.class))) //
				.thenReturn(StartWorkflowTaskDefinition.newInstance() //
						.withId(42L) //
						.build());

		// when
		store.create(newOne);

		// then
		final ArgumentCaptor<TaskDefinition> definitionCaptor = ArgumentCaptor.forClass(TaskDefinition.class);
		final ArgumentCaptor<TaskParameter> parameterCaptor = ArgumentCaptor.forClass(TaskParameter.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(definitionsStore).create(definitionCaptor.capture());
		inOrder.verify(definitionsStore).read(any(Storable.class));
		inOrder.verify(parametersStore, times(2)).create(parameterCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		TaskDefinitionAssert.of(definitionCaptor.getValue()) //
				.description(equalTo("description")) //
				.runningStatus(equalTo(true)) //
				.cronExpression(equalTo("cron expression"));
		TaskParameterAssert.of(parameterCaptor.getAllValues()) //
				.valueOfParameter("foo", equalTo("bar")) //
				.valueOfParameter("bar", equalTo("baz"));
	}

	@Test
	public void elementRead() throws Exception {
		// given
		when(definitionsStore.read(any(Storable.class))) //
				.thenReturn(StartWorkflowTaskDefinition.newInstance() //
						.withId(123L) //
						.withDescription("description") //
						.build());

		when(parametersStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("foo").withValue("FOO") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("bar").withValue("BAR") //
								.build() //
						));
		final StartWorkflowTask existing = StartWorkflowTask.newInstance() //
				.withId(123L) //
				.build();

		// when
		final Task element = store.read(existing);

		// then
		final ArgumentCaptor<TaskDefinition> definitionCaptor = ArgumentCaptor.forClass(TaskDefinition.class);
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(definitionsStore).read(definitionCaptor.capture());
		inOrder.verify(parametersStore).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) 123L));

		TaskDefinitionAssert.of(definitionCaptor.getValue()) //
				.id(equalTo(123L));

		TaskAssert.of(element) //
				.id(equalTo(123L)) //
				.description(equalTo("description")) //
				.valueOfParameter("foo", equalTo("FOO")) //
				.valueOfParameter("bar", equalTo("BAR"));
	}

	@Test
	public void allElementsRead() throws Exception {
		// given
		when(definitionsStore.list()) //
				.thenReturn(asList( //
						(TaskDefinition) StartWorkflowTaskDefinition.newInstance() //
								.withId(123L) //
								.withDescription("first") //
								.build(), //
						(TaskDefinition) StartWorkflowTaskDefinition.newInstance() //
								.withId(456L) //
								.withDescription("second") //
								.build()));

		when(parametersStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("foo").withValue("FOO") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("bar").withValue("BAR") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(456L).withKey("baz").withValue("BAZ") //
								.build() //
						));

		// when
		final List<Task> elements = store.list();

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(definitionsStore).list();
		inOrder.verify(parametersStore, times(2)).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		assertThat(elements, hasSize(2));

		final Groupable firstCapturedGroupable = groupableCaptor.getAllValues().get(0);
		assertThat(firstCapturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(firstCapturedGroupable.getGroupAttributeValue(), equalTo((Object) 123L));

		final Groupable secondCapturedGroupable = groupableCaptor.getAllValues().get(1);
		assertThat(secondCapturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(secondCapturedGroupable.getGroupAttributeValue(), equalTo((Object) 456L));

		TaskAssert.of(elements.get(0)) //
				.id(equalTo(123L)) //
				.description(equalTo("first")) //
				.valueOfParameter("foo", equalTo("FOO")) //
				.valueOfParameter("bar", equalTo("BAR"));

		TaskAssert.of(elements.get(1)) //
				.id(equalTo(456L)) //
				.description(equalTo("second")) //
				.valueOfParameter("baz", equalTo("BAZ"));
	}

	@Test
	public void elementsReadByGroup() throws Exception {
		// given
		when(definitionsStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						(TaskDefinition) StartWorkflowTaskDefinition.newInstance() //
								.withId(123L) //
								.withDescription("first") //
								.build(), //
						(TaskDefinition) StartWorkflowTaskDefinition.newInstance() //
								.withId(456L) //
								.withDescription("second") //
								.build()));

		when(parametersStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("foo").withValue("FOO") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("bar").withValue("BAR") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(456L).withKey("baz").withValue("BAZ") //
								.build() //
						));
		final Groupable groupable = mock(Groupable.class);

		// when
		final List<Task> elements = store.list(groupable);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(definitionsStore).list(groupable);
		inOrder.verify(parametersStore, times(2)).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		assertThat(elements, hasSize(2));

		final Groupable firstCapturedGroupable = groupableCaptor.getAllValues().get(0);
		assertThat(firstCapturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(firstCapturedGroupable.getGroupAttributeValue(), equalTo((Object) 123L));

		final Groupable secondCapturedGroupable = groupableCaptor.getAllValues().get(1);
		assertThat(secondCapturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(secondCapturedGroupable.getGroupAttributeValue(), equalTo((Object) 456L));

		TaskAssert.of(elements.get(0)) //
				.id(equalTo(123L)) //
				.description(equalTo("first")) //
				.valueOfParameter("foo", equalTo("FOO")) //
				.valueOfParameter("bar", equalTo("BAR"));

		TaskAssert.of(elements.get(1)) //
				.id(equalTo(456L)) //
				.description(equalTo("second")) //
				.valueOfParameter("baz", equalTo("BAZ"));
	}

	@Test
	public void elementUpdated() throws Exception {
		// given
		final StartWorkflowTask newOne = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "FOO") //
				.withParameter("bar", "bar") //
				.build();

		when(parametersStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						TaskParameter.newInstance() //
								.withOwner(42L).withKey("foo").withValue("foo") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(42L).withKey("baz").withValue("baz") //
								.build() //
						));

		// when
		store.update(newOne);

		// then
		final ArgumentCaptor<TaskDefinition> definitionCaptor = ArgumentCaptor.forClass(TaskDefinition.class);
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final ArgumentCaptor<TaskParameter> createParameterCaptor = ArgumentCaptor.forClass(TaskParameter.class);
		final ArgumentCaptor<TaskParameter> updateParameterCaptor = ArgumentCaptor.forClass(TaskParameter.class);
		final ArgumentCaptor<TaskParameter> deleteParameterCaptor = ArgumentCaptor.forClass(TaskParameter.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(definitionsStore).update(definitionCaptor.capture());
		inOrder.verify(parametersStore).list(groupableCaptor.capture());
		inOrder.verify(parametersStore).create(createParameterCaptor.capture());
		inOrder.verify(parametersStore).update(updateParameterCaptor.capture());
		inOrder.verify(parametersStore).delete(deleteParameterCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		TaskDefinitionAssert.of(definitionCaptor.getValue()) //
				.description(equalTo("description")) //
				.runningStatus(equalTo(true)) //
				.cronExpression(equalTo("cron expression"));

		TaskParameterAssert.of(createParameterCaptor.getAllValues()) //
				.valueOfParameter("bar", equalTo("bar"));

		TaskParameterAssert.of(updateParameterCaptor.getAllValues()) //
				.valueOfParameter("foo", equalTo("FOO"));

		TaskParameterAssert.of(deleteParameterCaptor.getAllValues()) //
				.valueOfParameter("baz", equalTo("baz"));
	}

	@Test
	public void elementDeleted() throws Exception {
		// given
		final StartWorkflowTask existingOne = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.build();
		when(definitionsStore.read(any(TaskDefinition.class))) //
				.thenReturn(StartWorkflowTaskDefinition.newInstance() //
						.withId(42L) //
						.build());
		final TaskParameter foo = TaskParameter.newInstance() //
				.withOwner(42L).withKey("foo").withValue("foo") //
				.build();
		final TaskParameter bar = TaskParameter.newInstance() //
				.withOwner(42L).withKey("bar").withValue("bar") //
				.build();
		when(parametersStore.list(any(Groupable.class))) //
				.thenReturn(asList(foo, bar));

		// when
		store.delete(existingOne);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final ArgumentCaptor<TaskParameter> parameterCaptor = ArgumentCaptor.forClass(TaskParameter.class);
		final ArgumentCaptor<TaskDefinition> definitionCaptor = ArgumentCaptor.forClass(TaskDefinition.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(parametersStore).list(groupableCaptor.capture());
		inOrder.verify(parametersStore, times(2)).delete(parameterCaptor.capture());
		inOrder.verify(definitionsStore).delete(definitionCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) existingOne.getId()));

		TaskParameterAssert.of(parameterCaptor.getAllValues()) //
				.hasParameter("foo").ownerOfParameter("foo", equalTo(42L)) //
				.hasParameter("bar").ownerOfParameter("bar", equalTo(42L));

		TaskDefinitionAssert.of(definitionCaptor.getValue()) //
				.id(equalTo(42L));
	}

	@Test
	public void elementReadById() throws Exception {
		// given
		when(definitionsStore.list()) //
				.thenReturn(asList( //
						StartWorkflowTaskDefinition.newInstance() //
								.withId(123L) //
								.withDescription("description") //
								.build(), //
						ReadEmailTaskDefinition.newInstance()//
								.withId(456L) //
								.withDescription("another description") //
								.build()));
		when(parametersStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("foo").withValue("FOO") //
								.build(), //
						TaskParameter.newInstance() //
								.withOwner(123L).withKey("bar").withValue("BAR") //
								.build()));

		// when
		final Task element = store.read(123L);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(definitionsStore, parametersStore);
		inOrder.verify(definitionsStore).list();
		inOrder.verify(parametersStore).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(OWNER));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) 123L));

		TaskAssert.of(element) //
				.id(equalTo(123L)) //
				.description(equalTo("description")) //
				.valueOfParameter("foo", equalTo("FOO")) //
				.valueOfParameter("bar", equalTo("BAR"));
	}

}
