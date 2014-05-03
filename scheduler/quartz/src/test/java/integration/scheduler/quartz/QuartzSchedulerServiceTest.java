package integration.scheduler.quartz;

import java.util.Date;

import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.OneTimeTrigger;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import support.scheduler.quartz.ExecutionListenerJob;
import support.scheduler.quartz.JobExecutionProbe;
import support.scheduler.quartz.SelfRemovingJob;
import utils.async.Poller;

public class QuartzSchedulerServiceTest {

	private static final long POLLING_TIMEOUT = 1000L;
	private static final long POLLING_INTERVAL = 100L;

	private static final int ONCE = 1;
	private static final int THREE_TIMES = 3;
	private static final long IN_THREE_SECONDS = (3 - 1) * 1000L;
	private static final String EVERY_SECOND = "0/1 * * * * ?";

	private SchedulerService scheduler;

	@Before
	public void initScheduler() {
		scheduler = new QuartzSchedulerService(new SchedulerExeptionFactory() {

			@Override
			public RuntimeException internal(final Throwable cause) {
				return new RuntimeException();
			}

			@Override
			public RuntimeException cronExpression(final Throwable cause, final String expression) {
				return new RuntimeException(expression);
			}

		});
	}

	@After
	public void cleanScheduler() {
		scheduler.stop();
	}

	@Test(expected = RuntimeException.class)
	public void quartzForbidsAddingTheSameJobTwice() throws InterruptedException {
		final Job nullJob = createNullJob();
		final Trigger trigger = OneTimeTrigger.at(new Date());
		scheduler.add(nullJob, trigger);
		scheduler.add(nullJob, trigger);
	}

	@Test()
	public void quartzAllowsRemovalOfUnexistentJob() throws InterruptedException {
		final Job nullJob = createNullJob();
		scheduler.remove(nullJob);
	}

	@Test
	public void quartzExecutesAnImmediateJob() throws InterruptedException {
		final Job nullJob = createNullJob();
		final Trigger immediately = OneTimeTrigger.at(new Date());
		assertEventually(nullJob, willBeExecuted(immediately));
	}

	@Test
	public void quartzExecutesADeferredJob() throws InterruptedException {
		final Job nullJob = createNullJob();
		final Trigger timeout = OneTimeTrigger.at(afterMillis(POLLING_INTERVAL / 2));
		assertEventually(nullJob, willBeExecutedAfter(timeout));
	}

	@Test
	public void quartzCanHandleMultipleJobs() {
		final Trigger timeout = OneTimeTrigger.at(farFarAway());
		scheduler.add(createNullJob(), timeout);
		scheduler.add(createNullJob(), timeout);
	}

	@Test
	public void quartzExecutesARecurringJob() throws InterruptedException {
		final Job nullJob = createNullJob();
		final Trigger everySecond = RecurringTrigger.at(EVERY_SECOND);
		assertEventually(nullJob, willBeExecutedApproximately(everySecond, THREE_TIMES), IN_THREE_SECONDS);
	}

	@Test
	public void quartzRemovesARecurringJob() throws InterruptedException {
		final Job nullJob = createSelfRemovingJob();
		final Trigger everySecond = RecurringTrigger.at(EVERY_SECOND);
		assertEventually(nullJob, willBeExecuted(everySecond, ONCE), IN_THREE_SECONDS);
	}

	/*
	 * Test helpers
	 */

	private ExecutionListenerJob createNullJob() {
		return new ExecutionListenerJob();
	}

	private ExecutionListenerJob createSelfRemovingJob() {
		return new SelfRemovingJob(scheduler);
	}

	public static Date afterMillis(final long millis) {
		final Date now = new Date();
		return new Date(now.getTime() + millis);
	}

	private Date farFarAway() {
		return afterMillis(1000000L);
	}

	private JobExecutionProbe willBeExecuted(final Trigger trigger) {
		return JobExecutionProbe.jobWasExecuted(trigger);
	}

	private JobExecutionProbe willBeExecutedAfter(final Trigger timeout) {
		return JobExecutionProbe.jobWasExecutedAfter((OneTimeTrigger) timeout);
	}

	private JobExecutionProbe willBeExecuted(final Trigger trigger, final int times) {
		return JobExecutionProbe.jobExecutionCounter((RecurringTrigger) trigger, times, times);
	}

	private JobExecutionProbe willBeExecutedApproximately(final Trigger trigger, final int times) {
		return JobExecutionProbe.jobExecutionCounter((RecurringTrigger) trigger, times, times + 1);
	}

	private void assertEventually(final Job job, final JobExecutionProbe probe) throws InterruptedException {
		assertEventually(job, probe, POLLING_TIMEOUT);
	}

	private void assertEventually(final Job job, final JobExecutionProbe probe, final long timeout)
			throws InterruptedException {
		probe.setJob((ExecutionListenerJob) job);
		scheduler.add(job, probe.getTrigger());
		scheduler.start();
		new Poller(timeout, POLLING_INTERVAL).check(probe);
		scheduler.stop();
	}
}
