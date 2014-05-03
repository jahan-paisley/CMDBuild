package org.cmdbuild.scheduler.quartz;

import static java.lang.String.format;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import org.cmdbuild.scheduler.OneTimeTrigger;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.Trigger;
import org.cmdbuild.scheduler.TriggerVisitor;
import org.quartz.TriggerBuilder;

public class QuartzTriggerFactory implements TriggerVisitor {

	private final SchedulerExeptionFactory exeptionFactory;

	private org.quartz.Trigger quartzTrigger;

	public QuartzTriggerFactory(final SchedulerExeptionFactory exeptionFactory) {
		this.exeptionFactory = exeptionFactory;
	}

	public org.quartz.Trigger create(final Trigger trigger) {
		trigger.accept(this);
		return quartzTrigger;
	}

	@Override
	public void visit(final OneTimeTrigger trigger) {
		quartzTrigger = TriggerBuilder.newTrigger() //
				.withIdentity(format("onetimetrigger%d", this.hashCode())) //
				.withSchedule(simpleSchedule() //
						.withRepeatCount(0)) //
				.startAt(trigger.getDate()) //
				.build();
	}

	@Override
	public void visit(final RecurringTrigger trigger) {
		try {
			quartzTrigger = TriggerBuilder.newTrigger() //
					.withIdentity(format("crontrigger%d", this.hashCode())) //
					.withSchedule(cronSchedule(trigger.getCronExpression())) //
					.build();
		} catch (final Exception e) {
			throw exeptionFactory.cronExpression(e, trigger.getCronExpression());
		}
	}

}
