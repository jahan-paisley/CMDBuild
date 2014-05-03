package org.cmdbuild.spring.configuration;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.cmdbuild.services.scheduler.DefaultSchedulerExeptionFactory;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Scheduler {

	@Autowired
	private Data data;

	@Autowired
	private Dms dms;

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private Email email;

	@Autowired
	private Notifier notifier;

	@Autowired
	private Workflow workflow;

	@Bean
	public SchedulerLogic defaultSchedulerLogic() {
		return new DefaultSchedulerLogic(defaultSchedulerService());
	}

	@Bean
	public SchedulerService defaultSchedulerService() {
		return new QuartzSchedulerService(schedulerExeptionFactory());
	}

	@Bean
	protected SchedulerExeptionFactory schedulerExeptionFactory() {
		return new DefaultSchedulerExeptionFactory();
	}

}
