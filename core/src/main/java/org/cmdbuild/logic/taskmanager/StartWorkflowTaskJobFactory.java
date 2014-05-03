package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Maps.transformValues;

import org.cmdbuild.logic.Action;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.scheduler.Command;
import org.cmdbuild.services.scheduler.DefaultJob;
import org.cmdbuild.services.scheduler.SafeCommand;

import com.google.common.base.Functions;

public class StartWorkflowTaskJobFactory extends AbstractJobFactory<StartWorkflowTask> {

	private static class SchedulerCommandWrapper implements Command {

		public static SchedulerCommandWrapper of(final Action delegate) {
			return new SchedulerCommandWrapper(delegate);
		}

		private final Action delegate;

		private SchedulerCommandWrapper(final Action delegate) {
			this.delegate = delegate;
		}

		@Override
		public void execute() {
			delegate.execute();
		}

	}

	private final WorkflowLogic workflowLogic;

	public StartWorkflowTaskJobFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
	}

	@Override
	protected Class<StartWorkflowTask> getType() {
		return StartWorkflowTask.class;
	}

	@Override
	protected Job doCreate(final StartWorkflowTask task) {
		final String name = task.getId().toString();
		return DefaultJob.newInstance() //
				.withName(name) //
				.withAction( //
						SafeCommand.of( //
								SchedulerCommandWrapper.of( //
										StartProcess.newInstance() //
												.withWorkflowLogic(workflowLogic) //
												.withClassName(task.getProcessClass()) //
												.withAttributes( //
														transformValues( //
																task.getAttributes(), //
																Functions.identity()) //
												) //
												.build() //
										) //
								) //
				) //
				.build();
	}

}
