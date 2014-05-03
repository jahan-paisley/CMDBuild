package org.cmdbuild.logic.taskmanager;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.task.ReadEmailTaskDefinition;
import org.cmdbuild.data.store.task.StartWorkflowTaskDefinition;
import org.cmdbuild.data.store.task.SynchronousEventTaskDefinition;
import org.cmdbuild.data.store.task.TaskVisitor;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class DefaultLogicAndStoreConverter implements LogicAndStoreConverter {

	/**
	 * Container for all {@link ReadEmailTaskDefinition} parameter names.
	 */
	public static class ReadEmail {

		private ReadEmail() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = "email.";

		public static final String ACCOUNT_NAME = ALL_PREFIX + "account.name";

		private static final String FILTER_PREFIX = ALL_PREFIX + "filter.";
		public static final String FILTER_FROM_REGEX = FILTER_PREFIX + "from.regex";
		public static final String FILTER_SUBJECT_REGEX = FILTER_PREFIX + "subject.regex";

		private static final String RULE_PREFIX = ALL_PREFIX + "rule.";

		private static final String ATTACHMENTS_PREFIX = RULE_PREFIX + "attachments.";
		public static final String ATTACHMENTS_ACTIVE = ATTACHMENTS_PREFIX + "active";
		public static final String ATTACHMENTS_CATEGORY = ATTACHMENTS_PREFIX + "category";

		public static final String NOTIFICATION_ACTIVE = RULE_PREFIX + "notification.active";

		private static final String WORKFLOW_PREFIX = RULE_PREFIX + "workflow.";
		public static final String WORKFLOW_ACTIVE = WORKFLOW_PREFIX + "active";
		public static final String WORKFLOW_ADVANCE = WORKFLOW_PREFIX + "advance";
		public static final String WORKFLOW_CLASS_NAME = WORKFLOW_PREFIX + "class.name";
		public static final String WORKFLOW_FIELDS_MAPPING = WORKFLOW_PREFIX + "fields.mapping";
		private static final String WORKFLOW_ATTACHMENTS_PREFIX = WORKFLOW_PREFIX + "attachments";
		public static final String WORKFLOW_ATTACHMENTS_SAVE = WORKFLOW_ATTACHMENTS_PREFIX + "save";
		public static final String WORKFLOW_ATTACHMENTS_CATEGORY = WORKFLOW_ATTACHMENTS_PREFIX + "category";

		/**
		 * Container for all mapper parameter names.
		 */
		private abstract static class MapperEngine {

			protected static final String ALL_PREFIX = "mapper.";

			public static final String TYPE = ALL_PREFIX + "type";

		}

		/**
		 * Container for all {@link _KeyValueMapperEngine} parameter names.
		 */
		public static class KeyValueMapperEngine extends MapperEngine {

			private KeyValueMapperEngine() {
				// prevents instantiation
			}

			private static final String TYPE_VALUE = "keyvalue";

			private static final String KEY_PREFIX = ALL_PREFIX + "key.";
			public static final String KEY_INIT = KEY_PREFIX + "init";
			public static final String KEY_END = KEY_PREFIX + "end";

			private static final String VALUE_PREFIX = ALL_PREFIX + "value.";
			public static final String VALUE_INIT = VALUE_PREFIX + "init";
			public static final String VALUE_END = VALUE_PREFIX + "end";

		}

	}

	/**
	 * Container for all {@link StartWorkflowTaskDefinition} parameter names.
	 */
	public static class StartWorkflow {

		private StartWorkflow() {
			// prevents instantiation
		}

		public static final String CLASSNAME = "classname";
		public static final String ATTRIBUTES = "attributes";

	}

	/**
	 * Container for all {@link SynchronousEventTaskDefinition} parameter names.
	 */
	public static class SynchronousEvent {

		private SynchronousEvent() {
			// prevents instantiation
		}

		public static final String PHASE = "phase";

		public static final String PHASE_AFTER_CREATE = "after_create";
		public static final String PHASE_BEFORE_UPDATE = "before_update";
		public static final String PHASE_AFTER_UPDATE = "after_update";
		public static final String PHASE_BEFORE_DELETE = "before_delete";

		private static final String FILTER = "filter.";
		public static final String FILTER_GROUPS = FILTER + "groups";
		public static final String FILTER_CLASSNAME = FILTER + "classname";

		private static final String ACTION_SCRIPT_PREFIX = "action.scripting.";
		public static final String ACTION_SCRIPT_ACTIVE = ACTION_SCRIPT_PREFIX + "active";
		public static final String ACTION_SCRIPT_ENGINE = ACTION_SCRIPT_PREFIX + "engine";
		public static final String ACTION_SCRIPT_SCRIPT = ACTION_SCRIPT_PREFIX + "script";
		public static final String ACTION_SCRIPT_SAFE = ACTION_SCRIPT_PREFIX + "safe";

	}

	private static final String KEY_VALUE_SEPARATOR = "=";
	private static final String GROUPS_SEPARATOR = ",";

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultLogicAndStoreConverter.class.getName());

	private static class MapperToParametersConverter implements MapperEngineVisitor {

		public static MapperToParametersConverter of(final MapperEngine mapper) {
			return new MapperToParametersConverter(mapper);
		}

		private final MapperEngine mapper;

		private MapperToParametersConverter(final MapperEngine mapper) {
			this.mapper = mapper;
		}

		private Map<String, String> parameters;

		public Map<String, String> convert() {
			parameters = Maps.newLinkedHashMap();
			mapper.accept(this);
			return parameters;
		}

		@Override
		public void visit(final KeyValueMapperEngine mapper) {
			parameters.put(ReadEmail.MapperEngine.TYPE, ReadEmail.KeyValueMapperEngine.TYPE_VALUE);
			parameters.put(ReadEmail.KeyValueMapperEngine.KEY_INIT, mapper.getKeyInit());
			parameters.put(ReadEmail.KeyValueMapperEngine.KEY_END, mapper.getKeyEnd());
			parameters.put(ReadEmail.KeyValueMapperEngine.VALUE_INIT, mapper.getValueInit());
			parameters.put(ReadEmail.KeyValueMapperEngine.VALUE_END, mapper.getValueEnd());
		}

		@Override
		public void visit(final NullMapperEngine mapper) {
			// nothing to do
		}

	}

	// TODO do in some way with visitor
	private static enum ParametersToMapperConverter {

		KEY_VALUE(ReadEmail.KeyValueMapperEngine.TYPE_VALUE) {

			@Override
			public MapperEngine convert(final Map<String, String> parameters) {
				return KeyValueMapperEngine.newInstance() //
						.withKey( //
								parameters.get(ReadEmail.KeyValueMapperEngine.KEY_INIT), //
								parameters.get(ReadEmail.KeyValueMapperEngine.KEY_END) //
						) //
						.withValue( //
								parameters.get(ReadEmail.KeyValueMapperEngine.VALUE_INIT), //
								parameters.get(ReadEmail.KeyValueMapperEngine.VALUE_END) //
						) //
						.build();
			}

		}, //
		UNDEFINED(EMPTY) {

			@Override
			public MapperEngine convert(final Map<String, String> parameters) {
				return NullMapperEngine.getInstance();
			}

		}, //
		;

		public static ParametersToMapperConverter of(final String type) {
			for (final ParametersToMapperConverter element : values()) {
				if (element.type.equals(type)) {
					return element;
				}
			}
			return UNDEFINED;
		}

		private final String type;

		private ParametersToMapperConverter(final String type) {
			this.type = type;
		}

		public abstract MapperEngine convert(Map<String, String> parameters);

	}

	private static class PhaseToStoreConverter implements SynchronousEventTask.PhaseIdentifier {

		private final SynchronousEventTask task;
		private String converted;

		public PhaseToStoreConverter(final SynchronousEventTask task) {
			this.task = task;
		}

		public String toStore() {
			task.getPhase().identify(this);
			Validate.notNull(converted, "conversion error");
			return converted;
		}

		@Override
		public void afterCreate() {
			converted = SynchronousEvent.PHASE_AFTER_CREATE;
		}

		@Override
		public void beforeUpdate() {
			converted = SynchronousEvent.PHASE_BEFORE_UPDATE;
		}

		@Override
		public void afterUpdate() {
			converted = SynchronousEvent.PHASE_AFTER_UPDATE;
		}

		@Override
		public void beforeDelete() {
			converted = SynchronousEvent.PHASE_BEFORE_DELETE;
		}

	}

	private static class PhaseToLogicConverter {

		private final String stored;

		public PhaseToLogicConverter(final String stored) {
			this.stored = stored;
		}

		public Phase toLogic() {
			final Phase converted;
			if (SynchronousEvent.PHASE_AFTER_CREATE.equals(stored)) {
				converted = Phase.AFTER_CREATE;
			} else if (SynchronousEvent.PHASE_BEFORE_UPDATE.equals(stored)) {
				converted = Phase.BEFORE_UPDATE;
			} else if (SynchronousEvent.PHASE_AFTER_UPDATE.equals(stored)) {
				converted = Phase.AFTER_UPDATE;
			} else if (SynchronousEvent.PHASE_BEFORE_DELETE.equals(stored)) {
				converted = Phase.BEFORE_DELETE;
			} else {
				converted = null;
			}
			Validate.notNull(converted, "conversion error");
			return converted;
		}

	}

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter, TaskVistor {

		private final Task source;

		private org.cmdbuild.data.store.task.Task target;

		public DefaultLogicAsSourceConverter(final Task source) {
			this.source = source;
		}

		@Override
		public org.cmdbuild.data.store.task.Task toStore() {
			logger.info(marker, "converting task '{}' to scheduler job", source);
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			this.target = org.cmdbuild.data.store.task.ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(ReadEmail.ACCOUNT_NAME, task.getEmailAccount()) //
					.withParameter(ReadEmail.FILTER_FROM_REGEX, Joiner.on(LINE_SEPARATOR) //
							.join(task.getRegexFromFilter())) //
					.withParameter(ReadEmail.FILTER_SUBJECT_REGEX, Joiner.on(LINE_SEPARATOR) //
							.join(task.getRegexSubjectFilter())) //
					.withParameter(ReadEmail.NOTIFICATION_ACTIVE, //
							Boolean.toString(task.isNotificationActive())) //
					.withParameter(ReadEmail.ATTACHMENTS_ACTIVE, //
							Boolean.toString(task.isAttachmentsActive())) //
					.withParameter(ReadEmail.ATTACHMENTS_CATEGORY, //
							task.getAttachmentsCategory()) //
					.withParameter(ReadEmail.WORKFLOW_ACTIVE, //
							Boolean.toString(task.isWorkflowActive())) //
					.withParameter(ReadEmail.WORKFLOW_CLASS_NAME, task.getWorkflowClassName()) //
					.withParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(task.getWorkflowAttributes())) //
					.withParameter(ReadEmail.WORKFLOW_ADVANCE, //
							Boolean.toString(task.isWorkflowAdvanceable())) //
					.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, //
							Boolean.toString(task.isWorkflowAttachments())) //
					.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, //
							task.getWorkflowAttachmentsCategory()) //
					.withParameters(MapperToParametersConverter.of(task.getMapperEngine()).convert()) //
					.build();
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			this.target = org.cmdbuild.data.store.task.StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(StartWorkflow.CLASSNAME, task.getProcessClass()) //
					.withParameter(StartWorkflow.ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(task.getAttributes())) //
					.build();
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			this.target = org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withParameter(SynchronousEvent.PHASE, new PhaseToStoreConverter(task).toStore()) //
					.withParameter(SynchronousEvent.FILTER_GROUPS, Joiner.on(GROUPS_SEPARATOR) //
							.join(task.getGroups())) //
					.withParameter(SynchronousEvent.FILTER_CLASSNAME, task.getTargetClassname()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, Boolean.toString(task.isScriptingEnabled())) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, task.getScriptingEngine()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, task.getScriptingScript()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SAFE, Boolean.toString(task.isScriptingSafe())) //
					.build();
		}
	}

	private static class DefaultStoreAsSourceConverter implements StoreAsSourceConverter, TaskVisitor {

		private static final Iterable<String> EMPTY_GROUPS = Collections.emptyList();
		private static final Iterable<String> EMPTY_FILTERS = Collections.emptyList();
		private static final Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

		private final org.cmdbuild.data.store.task.Task source;

		private Task target;

		public DefaultStoreAsSourceConverter(final org.cmdbuild.data.store.task.Task source) {
			this.source = source;
		}

		@Override
		public Task toLogic() {
			logger.info(marker, "converting scheduler job '{}' to scheduled task");
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.ReadEmailTask task) {
			final String fromRegexFilters = task.getParameter(ReadEmail.FILTER_FROM_REGEX);
			final String subjectRegexFilters = task.getParameter(ReadEmail.FILTER_SUBJECT_REGEX);
			final String attributesAsString = defaultString(task.getParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING));
			target = ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withEmailAccount(task.getParameter(ReadEmail.ACCOUNT_NAME)) //
					.withRegexFromFilter( //
							isEmpty(fromRegexFilters) ? EMPTY_FILTERS : Splitter.on(LINE_SEPARATOR) //
									.split(fromRegexFilters)) //
					.withRegexSubjectFilter( //
							isEmpty(subjectRegexFilters) ? EMPTY_FILTERS : Splitter.on(LINE_SEPARATOR) //
									.split(subjectRegexFilters)) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.NOTIFICATION_ACTIVE))) //
					.withAttachmentsActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.ATTACHMENTS_ACTIVE))) //
					.withAttachmentsCategory(task.getParameter(ReadEmail.ATTACHMENTS_CATEGORY)) //
					.withWorkflowActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ACTIVE))) //
					.withWorkflowClassName(task.getParameter(ReadEmail.WORKFLOW_CLASS_NAME)) //
					.withWorkflowAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : Splitter.on(LINE_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.split(attributesAsString)) //
					.withWorkflowAdvanceableStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ADVANCE))) //
					.withWorkflowAttachmentsStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE))) //
					.withWorkflowAttachmentsCategory(task.getParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY)) //
					.withMapperEngine(mapperOf(task.getParameters())) //
					.build();
		}

		private MapperEngine mapperOf(final Map<String, String> parameters) {
			final String type = parameters.get(ReadEmail.MapperEngine.TYPE);
			return ParametersToMapperConverter.of(type).convert(parameters);
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.StartWorkflowTask task) {
			final String attributesAsString = defaultString(task.getParameter(StartWorkflow.ATTRIBUTES));
			target = StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withProcessClass(task.getParameter(StartWorkflow.CLASSNAME)) //
					.withAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : Splitter.on(LINE_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.split(attributesAsString)) //
					.build();
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.SynchronousEventTask task) {
			final String groupsAsString = defaultString(task.getParameter(SynchronousEvent.FILTER_GROUPS));
			target = SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withPhase( //
							new PhaseToLogicConverter(task.getParameter(SynchronousEvent.PHASE)) //
									.toLogic()) //
					.withGroups(isEmpty(groupsAsString) ? EMPTY_GROUPS : Splitter.on(GROUPS_SEPARATOR) //
							.split(groupsAsString)) //
					.withTargetClass(task.getParameter(SynchronousEvent.FILTER_CLASSNAME)) //
					.withScriptingEnableStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE))) //
					.withScriptingEngine(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE)) //
					.withScript(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT)) //
					.withScriptingSafeStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SAFE))) //
					.build();
		}
	}

	@Override
	public LogicAsSourceConverter from(final Task source) {
		return new DefaultLogicAsSourceConverter(source);
	}

	@Override
	public StoreAsSourceConverter from(final org.cmdbuild.data.store.task.Task source) {
		return new DefaultStoreAsSourceConverter(source);
	}

}
