package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.TASK_READ_EMAIL;
import static org.cmdbuild.servlets.json.ComunicationConstants.TASK_START_WORKFLOW;
import static org.cmdbuild.servlets.json.ComunicationConstants.TASK_SYNCHRONOUS_EVENT;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.TaskVistor;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TaskManager extends JSONBaseWithSpringContext {

	private static enum TaskType {

		READ_EMAIL(TASK_READ_EMAIL), //
		START_WORKFLOW(TASK_START_WORKFLOW), //
		SYNCHRONOUS_EVENT(TASK_SYNCHRONOUS_EVENT), //
		;

		private final String forJson;

		private TaskType(final String forJson) {
			this.forJson = forJson;
		}

		public String forJson() {
			return forJson;
		}

	}

	private static class TaskTypeResolver implements TaskVistor {

		public static TaskTypeResolver of(final Task task) {
			return new TaskTypeResolver(task);
		}

		private final Task task;
		private TaskType type;

		private TaskTypeResolver(final Task task) {
			this.task = task;
		}

		public TaskType find() {
			task.accept(this);
			Validate.notNull(type, "type not found");
			return type;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			type = TaskType.READ_EMAIL;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			type = TaskType.START_WORKFLOW;
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			type = TaskType.SYNCHRONOUS_EVENT;
		}

	}

	private static class JsonTask {

		private final Task delegate;

		public JsonTask(final Task delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(TYPE)
		public String getType() {
			return TaskTypeResolver.of(delegate).find().forJson();
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

	}

	public static class JsonElements<T> {

		public static <T> JsonElements<T> of(final Iterable<? extends T> elements) {
			return new JsonElements<T>(elements);
		}

		private final List<? extends T> elements;

		private JsonElements(final Iterable<? extends T> elements) {
			this.elements = Lists.newArrayList(elements);
		}

		@JsonProperty(ELEMENTS)
		public List<? extends T> getElements() {
			return elements;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	public static final Function<Task, JsonTask> TASK_TO_JSON_TASK = new Function<Task, JsonTask>() {

		@Override
		public JsonTask apply(final Task input) {
			return new JsonTask(input);
		}

	};

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<Task> tasks = taskManagerLogic().read();
		return JsonResponse.success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@JSONExported
	public JsonResponse start( //
			@Parameter(value = ID) final Long id //
	) {
		taskManagerLogic().activate(id);
		return JsonResponse.success();
	}

	@JSONExported
	public JsonResponse stop( //
			@Parameter(value = ID) final Long id //
	) {
		taskManagerLogic().deactivate(id);
		return JsonResponse.success();
	}

}
