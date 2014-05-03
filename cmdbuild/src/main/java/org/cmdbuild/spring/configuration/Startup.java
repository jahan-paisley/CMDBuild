package org.cmdbuild.spring.configuration;

import java.util.Collections;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.services.startup.DefaultStartupLogic;
import org.cmdbuild.services.startup.DefaultStartupManager;
import org.cmdbuild.services.startup.StartupLogic;
import org.cmdbuild.services.startup.StartupManager;
import org.cmdbuild.services.startup.StartupManager.Condition;
import org.cmdbuild.services.startup.StartupManager.Startable;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Startup {

	@Autowired
	private Cache cache;

	@Autowired
	private Dms dms;

	@Autowired
	private Other other;

	@Autowired
	private Properties properties;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private TaskManager taskManager;

	@Bean
	public StartupLogic startupLogic() {
		return new DefaultStartupLogic( //
				startupManager(), //
				other.patchManager(), //
				cache.defaultCachingLogic() //
		);
	}

	@Bean
	protected StartupManager startupManager() {
		final StartupManager startupManager = new DefaultStartupManager();
		startupManager.add(startScheduler(), databaseIsOk());
		startupManager.add(clearDmsTemporaryFolder(), always());
		return startupManager;
	}

	@Bean
	protected Startable startScheduler() {
		return new Startable() {

			private final SchedulerLogic schedulerLogic = scheduler.defaultSchedulerLogic();
			private final TaskManagerLogic taskManagerLogic = taskManager.taskManagerLogic();

			@Override
			public void start() {
				schedulerLogic.startScheduler();

				for (final Task task : taskManagerLogic.read()) {
					if (task.isActive()) {
						taskManagerLogic.activate(task.getId());
					}
				}
			}

		};
	}

	@Bean
	protected Startable clearDmsTemporaryFolder() {
		return new Startable() {

			private final Logger logger = Log.CMDBUILD;

			/*
			 * we need to call it now, even if not used, because DmsService will
			 * be configured when injected inside DmsLogic
			 */
			@SuppressWarnings("unused")
			private final DmsLogic dmsLogic = dms.dmsLogic();
			private final DmsConfiguration dmsConfiguration = properties.dmsProperties();
			private final DmsService dmsService = dms.dmsService();
			private final DocumentCreatorFactory documentCreatorFactory = dms.documentCreatorFactory();

			private final Iterable<String> ROOT = Collections.emptyList();

			@Override
			public void start() {
				logger.info("clearing DMS temporary folder");
				try {
					if (dmsConfiguration.isEnabled()) {
						final DocumentSearch all = documentCreatorFactory.createTemporary(ROOT) //
								.createDocumentSearch(null, null);
						dmsService.delete(all);
					}
				} catch (final Throwable e) {
					logger.warn("error clearing DMS temporary", e);
				}
			}

		};
	}

	@Bean
	protected Condition databaseIsOk() {
		return new Condition() {

			@Override
			public boolean satisfied() {
				return properties.databaseProperties().isConfigured() && other.patchManager().isUpdated();
			}

		};
	}

	@Bean
	protected Condition always() {
		return new Condition() {

			@Override
			public boolean satisfied() {
				return true;
			}

		};
	}

}
