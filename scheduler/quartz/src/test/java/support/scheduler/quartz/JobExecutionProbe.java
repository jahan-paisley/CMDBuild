package support.scheduler.quartz;

import java.util.Date;

import org.cmdbuild.scheduler.OneTimeTrigger;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.Trigger;
import org.hamcrest.Description;

import utils.async.Probe;

public abstract class JobExecutionProbe implements Probe {

	protected ExecutionListenerJob job;
	private final Trigger trigger;

	protected JobExecutionProbe(final Trigger trigger) {
		this.trigger = trigger;
	}

	public void setJob(final ExecutionListenerJob job) {
		this.job = job;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public static JobExecutionProbe jobWasExecuted(final Trigger trigger) {
		return new JobExecutionProbe(trigger) {
			private boolean wasExecuted = false;

			@Override
			public void describeAcceptanceCriteriaTo(final Description d) {
				d.appendText("job executed");
			}

			@Override
			public void describeFailureTo(final Description d) {
				d.appendText("job was not executed in time");
			}

			@Override
			public boolean isSatisfied() {
				return wasExecuted;
			}

			@Override
			public boolean isDone() {
				return wasExecuted;
			}

			@Override
			public void sample() {
				wasExecuted = job.hasBeenExecuted();
			}
		};
	}

	public static JobExecutionProbe jobWasExecutedAfter(final OneTimeTrigger trigger) {
		return new JobExecutionProbe(trigger) {
			private Date executionTime = null;
			private final Date timeout = trigger.getDate();

			@Override
			public void describeAcceptanceCriteriaTo(final Description d) {
				d.appendText("job should have been executed after " + timeout.getTime());
			}

			@Override
			public void describeFailureTo(final Description d) {
				d.appendText("job was executed at " + executionTime.getTime());
			}

			@Override
			public boolean isSatisfied() {
				return !executionTime.before(timeout);
			}

			@Override
			public boolean isDone() {
				return (executionTime != null);
			}

			@Override
			public void sample() {
				executionTime = job.getLastExecutionTime();
			}
		};
	}

	public static JobExecutionProbe jobExecutionCounter(final RecurringTrigger trigger, final int minTimes,
			final int maxTimes) {
		return new JobExecutionProbe(trigger) {
			private int totalExecutions = 0;

			@Override
			public void describeAcceptanceCriteriaTo(final Description d) {
				d.appendText(String.format("job should have been executed between %d and %d times", minTimes, maxTimes));
			}

			@Override
			public void describeFailureTo(final Description d) {
				d.appendText(String.format("job was executed %d times", totalExecutions));
			}

			@Override
			public boolean isSatisfied() {
				return (totalExecutions >= minTimes) && (totalExecutions <= maxTimes);
			}

			@Override
			public boolean isDone() {
				return false;
			}

			@Override
			public void sample() {
				totalExecutions = job.getTotalExecutions();
			}
		};
	}

}
