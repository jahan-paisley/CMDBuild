package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;

import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

public class SynchronousEvent extends JSONBaseWithSpringContext {

	private static class JsonSynchronousEventTask {

		private final SynchronousEventTask delegate;

		public JsonSynchronousEventTask(final SynchronousEventTask delegate) {
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

	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(id) //
				.build();
		final SynchronousEventTask readed = taskManagerLogic().read(task, SynchronousEventTask.class);
		return JsonResponse.success(new JsonSynchronousEventTask(readed));
	}

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(SynchronousEventTask.class);
		return JsonResponse.success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@JSONExported
	public JsonResponse readAllByWorkflow( //
			@Parameter(value = ID) final Long id //
	) {
		// TODO
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

}
