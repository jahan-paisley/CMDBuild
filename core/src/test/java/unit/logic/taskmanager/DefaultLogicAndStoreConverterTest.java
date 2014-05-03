package unit.logic.taskmanager;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.ReadEmail;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.StartWorkflow;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.SynchronousEvent;
import org.cmdbuild.logic.taskmanager.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.MapperEngine;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

public class DefaultLogicAndStoreConverterTest {

	private DefaultLogicAndStoreConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new DefaultLogicAndStoreConverter();
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final ReadEmailTask source = ReadEmailTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withEmailAccount("email account") //
				.withRegexFromFilter(asList("regex", "from", "filter")) //
				.withRegexSubjectFilter(asList("regex", "subject", "filter")) //
				.withNotificationStatus(true) //
				.withAttachmentsActive(true) //
				.withAttachmentsCategory("category") //
				.withWorkflowActive(true) //
				.withWorkflowClassName("workflow class name") //
				.withWorkflowAttributes(attributes) //
				.withWorkflowAdvanceableStatus(true) //
				.withWorkflowAttachmentsStatus(true) //
				.withWorkflowAttachmentsCategory("workflow's attachments category") //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ReadEmailTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(ReadEmail.ACCOUNT_NAME, "email account"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_FROM_REGEX, "regex\nfrom\nfilter"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_SUBJECT_REGEX, "regex\nsubject\nfilter"));
		assertThat(parameters, hasEntry(ReadEmail.NOTIFICATION_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.ATTACHMENTS_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.ATTACHMENTS_CATEGORY, "category"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_CLASS_NAME, "workflow class name"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_FIELDS_MAPPING, Joiner.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.join(attributes)));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ADVANCE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, "workflow's attachments category"));
	}

	@Test
	public void keyValueMapperSuccessfullyConvertedToStore() throws Exception {
		// given
		final ReadEmailTask source = ReadEmailTask.newInstance() //
				.withMapperEngine(KeyValueMapperEngine.newInstance() //
						.withKey("key_init", "key_end") //
						.withValue("value_init", "value_end") //
						.build() //
				).build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.TYPE, "keyvalue"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.KEY_INIT, "key_init"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.KEY_END, "key_end"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.VALUE_INIT, "value_init"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.VALUE_END, "value_end"));
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask source = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(ReadEmail.ACCOUNT_NAME, "email account") //
				.withParameter(ReadEmail.FILTER_FROM_REGEX, "regex\nfrom\nfilter") //
				.withParameter(ReadEmail.FILTER_SUBJECT_REGEX, "regex\nsubject\nfilter") //
				.withParameter(ReadEmail.NOTIFICATION_ACTIVE, "true") //
				.withParameter(ReadEmail.ATTACHMENTS_ACTIVE, "true") //
				.withParameter(ReadEmail.ATTACHMENTS_CATEGORY, "category") //
				.withParameter(ReadEmail.WORKFLOW_ACTIVE, "true") //
				.withParameter(ReadEmail.WORKFLOW_CLASS_NAME, "workflow class name") //
				.withParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING, "foo=bar\nbar=baz\nbaz=foo") //
				.withParameter(ReadEmail.WORKFLOW_ADVANCE, "true") //
				.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, "true") //
				.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, "workflow's attachments category") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ReadEmailTask.class));
		final ReadEmailTask converted = ReadEmailTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getEmailAccount(), equalTo("email account"));
		assertThat(converted.isNotificationActive(), equalTo(true));
		assertThat(converted.getRegexFromFilter(), containsInAnyOrder("regex", "from", "filter"));
		assertThat(converted.getRegexSubjectFilter(), containsInAnyOrder("regex", "subject", "filter"));
		assertThat(converted.isAttachmentsActive(), equalTo(true));
		assertThat(converted.getAttachmentsCategory(), equalTo("category"));
		assertThat(converted.isWorkflowActive(), equalTo(true));
		assertThat(converted.getWorkflowClassName(), equalTo("workflow class name"));
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		assertThat(converted.getWorkflowAttributes(), equalTo(attributes));
		assertThat(converted.isWorkflowAdvanceable(), equalTo(true));
		assertThat(converted.isWorkflowAttachments(), equalTo(true));
		assertThat(converted.getWorkflowAttachmentsCategory(), equalTo("workflow's attachments category"));
	}

	@Test
	public void keyValueMapperSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask source = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withParameter(ReadEmail.KeyValueMapperEngine.TYPE, "keyvalue") //
				.withParameter(ReadEmail.KeyValueMapperEngine.KEY_INIT, "key_init") //
				.withParameter(ReadEmail.KeyValueMapperEngine.KEY_END, "key_end") //
				.withParameter(ReadEmail.KeyValueMapperEngine.VALUE_INIT, "value_init") //
				.withParameter(ReadEmail.KeyValueMapperEngine.VALUE_END, "value_end") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ReadEmailTask.class));
		final ReadEmailTask converted = ReadEmailTask.class.cast(_converted);
		final MapperEngine mapper = converted.getMapperEngine();
		assertThat(mapper, instanceOf(KeyValueMapperEngine.class));
		final KeyValueMapperEngine keyValueMapper = KeyValueMapperEngine.class.cast(mapper);
		assertThat(keyValueMapper.getKeyInit(), equalTo("key_init"));
		assertThat(keyValueMapper.getKeyEnd(), equalTo("key_end"));
		assertThat(keyValueMapper.getValueInit(), equalTo("value_init"));
		assertThat(keyValueMapper.getValueEnd(), equalTo("value_end"));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final StartWorkflowTask source = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withProcessClass("class name") //
				.withAttributes(attributes) //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.StartWorkflowTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(StartWorkflow.CLASSNAME, "class name"));
		assertThat(parameters, hasEntry(StartWorkflow.ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.join(attributes)));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.StartWorkflowTask source = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, "foo=bar\nbar=baz\nbaz=foo") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		assertThat(converted.getAttributes(), equalTo(attributes));
	}

	@Test
	public void startWorkflowTaskWithEmptyAttributesSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.StartWorkflowTask source = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, EMPTY) //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		assertThat(converted.getAttributes().isEmpty(), is(true));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final SynchronousEventTask source = SynchronousEventTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withPhase(Phase.AFTER_CREATE) //
				.withGroups(asList("foo", "bar", "baz")) //
				.withTargetClass("classname") //
				.withScriptingEnableStatus(true) //
				.withScriptingEngine("groovy") //
				.withScript("blah blah blah") //
				.withScriptingSafeStatus(true) //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.SynchronousEventTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(SynchronousEvent.PHASE, "after_create"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_GROUPS, "foo,bar,baz"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_CLASSNAME, "classname"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SAFE, "true"));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final SynchronousEventTask afterCreate = SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_CREATE) //
				.build();
		final SynchronousEventTask beforeUpdate = SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_UPDATE) //
				.build();
		final SynchronousEventTask afterUpdate = SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_UPDATE) //
				.build();
		final SynchronousEventTask beforeDelete = SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_DELETE) //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task convertedAfterCreate = converter.from(afterCreate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeUpdate = converter.from(beforeUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedAfterUpdate = converter.from(afterUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeDelete = converter.from(beforeDelete).toStore();

		// then
		assertThat(convertedAfterCreate.getParameters(), hasEntry(SynchronousEvent.PHASE, "after_create"));
		assertThat(convertedBeforeUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "before_update"));
		assertThat(convertedAfterUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "after_update"));
		assertThat(convertedBeforeDelete.getParameters(), hasEntry(SynchronousEvent.PHASE, "before_delete"));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask source = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withParameter(SynchronousEvent.PHASE, "after_create") //
				.withParameter(SynchronousEvent.FILTER_GROUPS, "foo,bar,baz") //
				.withParameter(SynchronousEvent.FILTER_CLASSNAME, "classname") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_SAFE, "true") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(SynchronousEventTask.class));
		final SynchronousEventTask converted = SynchronousEventTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(converted.getGroups(), containsInAnyOrder("foo", "bar", "baz"));
		assertThat(converted.getTargetClassname(), equalTo("classname"));
		assertThat(converted.isScriptingEnabled(), equalTo(true));
		assertThat(converted.getScriptingEngine(), equalTo("groovy"));
		assertThat(converted.getScriptingScript(), equalTo("blah blah blah"));
		assertThat(converted.isScriptingSafe(), equalTo(true));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask afterCreate = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "after_create") //
				.build();
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeUpdate = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "before_update") //
				.build();
		final org.cmdbuild.data.store.task.SynchronousEventTask afterUpdate = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "after_update") //
				.build();
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeDelete = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "before_delete") //
				.build();

		// when
		final SynchronousEventTask convertedAfterCreate = SynchronousEventTask.class.cast(converter.from(afterCreate)
				.toLogic());
		final SynchronousEventTask convertedBeforeUpdate = SynchronousEventTask.class.cast(converter.from(beforeUpdate)
				.toLogic());
		final SynchronousEventTask convertedAfterUpdate = SynchronousEventTask.class.cast(converter.from(afterUpdate)
				.toLogic());
		final SynchronousEventTask convertedBeforeDelete = SynchronousEventTask.class.cast(converter.from(beforeDelete)
				.toLogic());

		// then
		assertThat(convertedAfterCreate.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(convertedBeforeUpdate.getPhase(), equalTo(Phase.BEFORE_UPDATE));
		assertThat(convertedAfterUpdate.getPhase(), equalTo(Phase.AFTER_UPDATE));
		assertThat(convertedBeforeDelete.getPhase(), equalTo(Phase.BEFORE_DELETE));
	}

}
