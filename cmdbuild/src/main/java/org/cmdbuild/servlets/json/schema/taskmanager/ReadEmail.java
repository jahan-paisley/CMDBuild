package org.cmdbuild.servlets.json.schema.taskmanager;

import static org.apache.commons.lang3.StringUtils.*;
import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTACHMENTS_ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTACHMENTS_CATEGORY;
import static org.cmdbuild.servlets.json.ComunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER_FROM_ADDRESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER_SUBJECT;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.MAPPER_KEY_END;
import static org.cmdbuild.servlets.json.ComunicationConstants.MAPPER_KEY_INIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.MAPPER_VALUE_END;
import static org.cmdbuild.servlets.json.ComunicationConstants.MAPPER_VALUE_INIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.NOTIFICATION_ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_ADVANCEABLE;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_ATTACHMENTS_CATEGORY;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_ATTRIBUTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_SAVE_ATTACHMENTS;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.taskmanager.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.taskmanager.Utils.toMap;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.MapperEngine;
import org.cmdbuild.logic.taskmanager.NullMapperEngine;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReadEmail extends JSONBaseWithSpringContext {

	private static class JsonReadEmailTask {

		private final ReadEmailTask delegate;
		private final KeyValueMapperEngine engine;

		public JsonReadEmailTask(final ReadEmailTask delegate) {
			this.delegate = delegate;
			final MapperEngine current = delegate.getMapperEngine();
			this.engine = (current instanceof KeyValueMapperEngine) ? KeyValueMapperEngine.class.cast(current) : null;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return delegate.getDescription();
		}

		@JsonProperty(ACTIVE)
		public boolean isActive() {
			return delegate.isActive();
		}

		@JsonProperty(CRON_EXPRESSION)
		public String getCronExpression() {
			return delegate.getCronExpression();
		}

		@JsonProperty(EMAIL_ACCOUNT)
		public String getEmailAccount() {
			return delegate.getEmailAccount();
		}

		@JsonProperty(FILTER_FROM_ADDRESS)
		// TODO send array as string?
		public Iterable<String> getRegexFromFilter() {
			return delegate.getRegexFromFilter();
		}

		@JsonProperty(FILTER_SUBJECT)
		// TODO send array as string?
		public Iterable<String> getRegexSubjectFilter() {
			return delegate.getRegexSubjectFilter();
		}

		@JsonProperty(NOTIFICATION_ACTIVE)
		public boolean isNotificationActive() {
			return delegate.isNotificationActive();
		}

		@JsonProperty(ATTACHMENTS_ACTIVE)
		public boolean isAttachmentsActive() {
			return delegate.isAttachmentsActive();
		}

		@JsonProperty(ATTACHMENTS_CATEGORY)
		public String getAttachmentsCategory() {
			return delegate.getAttachmentsCategory();
		}

		@JsonProperty(WORKFLOW_ACTIVE)
		public boolean isWorkflowActive() {
			return delegate.isWorkflowActive();
		}

		@JsonProperty(WORKFLOW_CLASS_NAME)
		public String getWorkflowClassName() {
			return delegate.getWorkflowClassName();
		}

		@JsonProperty(WORKFLOW_ATTRIBUTES)
		// TODO send object as string?
		public Map<String, String> getWorkflowAttributes() {
			return delegate.getWorkflowAttributes();
		}

		@JsonProperty(WORKFLOW_ADVANCEABLE)
		public boolean isWorkflowAdvanceable() {
			return delegate.isWorkflowAdvanceable();
		}

		@JsonProperty(WORKFLOW_SAVE_ATTACHMENTS)
		public boolean isWorkflowAttachments() {
			return delegate.isWorkflowAttachments();
		}

		@JsonProperty(WORKFLOW_ATTACHMENTS_CATEGORY)
		public String getWorkflowAttachmentsCategory() {
			return delegate.getWorkflowAttachmentsCategory();
		}

		@JsonProperty(MAPPER_KEY_INIT)
		public String getKeyInit() {
			return (engine == null) ? null : engine.getKeyInit();
		}

		@JsonProperty(MAPPER_KEY_END)
		public String getKeyEnd() {
			return (engine == null) ? null : engine.getKeyEnd();
		}

		@JsonProperty(MAPPER_VALUE_INIT)
		public String getValueInit() {
			return (engine == null) ? null : engine.getValueInit();
		}

		@JsonProperty(MAPPER_VALUE_END)
		public String getValueEnd() {
			return (engine == null) ? null : engine.getValueEnd();
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create(
			//
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = FILTER_FROM_ADDRESS, required = false) final JSONArray filterFromAddress, //
			@Parameter(value = FILTER_SUBJECT, required = false) final JSONArray filterSubject, //
			@Parameter(value = EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = ATTACHMENTS_CATEGORY, required = false) final String attachmentsCategory, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes, //
			@Parameter(value = WORKFLOW_ATTACHMENTS_CATEGORY, required = false) final String workflowAttachmentsCategory, //
			@Parameter(value = MAPPER_KEY_INIT, required = false) final String keyInit, //
			@Parameter(value = MAPPER_KEY_END, required = false) final String keyEnd, //
			@Parameter(value = MAPPER_VALUE_INIT, required = false) final String valueInit, //
			@Parameter(value = MAPPER_VALUE_END, required = false) final String valueEnd //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withEmailAccount(emailAccount) //
				//
				// filters
				.withRegexFromFilter(toIterable(filterFromAddress)) //
				.withRegexSubjectFilter(toIterable(filterSubject)) //
				//
				// send notification
				// TODO not necessary
				.withNotificationStatus(isNotBlank(emailTemplate)) //
				//
				// store attachments
				.withAttachmentsActive(isNotBlank(attachmentsCategory)) //
				.withAttachmentsCategory(attachmentsCategory) //
				//
				// workflow (start process and, maybe, store attachments)
				.withWorkflowActive(isNotBlank(workflowClassName)) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAttachmentsStatus(isNotBlank(workflowAttachmentsCategory)) //
				.withWorkflowAttachmentsCategory(workflowAttachmentsCategory) //
				//
				// mapping
				.withMapperEngine(mapperEngine(keyInit, keyEnd, valueInit, valueEnd) //
				)
				//
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.build();
		final ReadEmailTask readed = taskManagerLogic().read(task, ReadEmailTask.class);
		return JsonResponse.success(new JsonReadEmailTask(readed));
	}

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(ReadEmailTask.class);
		return JsonResponse.success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse update(
			//
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = FILTER_FROM_ADDRESS, required = false) final JSONArray filterFromAddress, //
			@Parameter(value = FILTER_SUBJECT, required = false) final JSONArray filterSubject, //
			@Parameter(value = EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = ATTACHMENTS_CATEGORY, required = false) final String attachmentsCategory, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes, //
			@Parameter(value = WORKFLOW_ATTACHMENTS_CATEGORY, required = false) final String workflowAttachmentsCategory, //
			@Parameter(value = MAPPER_KEY_INIT, required = false) final String keyInit, //
			@Parameter(value = MAPPER_KEY_END, required = false) final String keyEnd, //
			@Parameter(value = MAPPER_VALUE_INIT, required = false) final String valueInit, //
			@Parameter(value = MAPPER_VALUE_END, required = false) final String valueEnd //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withEmailAccount(emailAccount) //
				//
				// filters
				.withRegexFromFilter(toIterable(filterFromAddress)) //
				.withRegexSubjectFilter(toIterable(filterSubject)) //
				//
				// send notification
				// TODO not necessary
				.withNotificationStatus(isNotBlank(emailTemplate)) //
				//
				// store attachments
				.withAttachmentsActive(isNotBlank(attachmentsCategory)) //
				.withAttachmentsCategory(attachmentsCategory) //
				//
				// workflow (start process and, maybe, store attachments)
				.withWorkflowActive(isNotBlank(workflowClassName)) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAttachmentsStatus(isNotBlank(workflowAttachmentsCategory)) //
				.withWorkflowAttachmentsCategory(workflowAttachmentsCategory) //
				//
				// mapping
				.withMapperEngine(mapperEngine(keyInit, keyEnd, valueInit, valueEnd))
				//
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

	private MapperEngine mapperEngine(final String keyInit, final String keyEnd, final String valueInit,
			final String valueEnd) {
		final MapperEngine engine;
		// TODO use dedicated flag
		if (isBlank(keyInit) && isBlank(keyEnd) && isBlank(valueInit) && isBlank(valueEnd)) {
			engine = NullMapperEngine.getInstance();
		} else {
			engine = KeyValueMapperEngine.newInstance() //
					.withKey(keyInit, keyEnd) //
					.withValue(valueInit, valueEnd) //
					.build();
		}
		return engine;
	}

}
