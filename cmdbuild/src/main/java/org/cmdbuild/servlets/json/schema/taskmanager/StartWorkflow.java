package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_ATTRIBUTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.taskmanager.Utils.toMap;

import java.util.List;
import java.util.Map;

import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONObject;

import com.google.common.base.Predicate;

public class StartWorkflow extends JSONBaseWithSpringContext {

	private static class JsonStartWorkflowTask {

		private final StartWorkflowTask delegate;

		public JsonStartWorkflowTask(final StartWorkflowTask delegate) {
			this.delegate = delegate;
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

		@JsonProperty(WORKFLOW_CLASS_NAME)
		public String getProcessClass() {
			return delegate.getProcessClass();
		}

		@JsonProperty(WORKFLOW_ATTRIBUTES)
		public Map<String, String> getAttributes() {
			return delegate.getAttributes();
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(WORKFLOW_CLASS_NAME) final String className, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject jsonParameters //
	) {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withProcessClass(className) //
				.withAttributes(toMap(jsonParameters)) //
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(id) //
				.build();
		final StartWorkflowTask readed = taskManagerLogic().read(task, StartWorkflowTask.class);
		return JsonResponse.success(new JsonStartWorkflowTask(readed));
	}

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(StartWorkflowTask.class);
		return JsonResponse.success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@JSONExported
	public JsonResponse readAllByWorkflow( //
			@Parameter(WORKFLOW_CLASS_NAME) final String className //
	) {
		final List<? extends Task> tasks = from(taskManagerLogic().read(StartWorkflowTask.class)) //
				.filter(StartWorkflowTask.class) //
				.filter(className(className)) //
				.toList();
		return JsonResponse.success(tasks);
	}

	private Predicate<StartWorkflowTask> className(final String className) {
		return new Predicate<StartWorkflowTask>() {

			@Override
			public boolean apply(final StartWorkflowTask input) {
				return input.getProcessClass().equals(className);
			}

		};
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(WORKFLOW_CLASS_NAME) final String className, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject jsonParameters //
	) {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withProcessClass(className) //
				.withAttributes(toMap(jsonParameters)) //
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

	/*
	 * Utilities
	 */

}
