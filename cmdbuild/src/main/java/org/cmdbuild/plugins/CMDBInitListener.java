package org.cmdbuild.plugins;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.services.startup.StartupLogic;
import org.slf4j.Logger;

public class CMDBInitListener implements ServletContextListener {

	private static final Logger logger = Log.CMDBUILD;

	public interface CmdbuildModuleLoader {
		public void init(ServletContext ctxt) throws Exception;
	}

	@Override
	public void contextDestroyed(final ServletContextEvent ctxt) {
		stopSchedulerService();
	}

	@Override
	public void contextInitialized(final ServletContextEvent evt) {
		loadPlugins(evt);
		applicationContext().getBean(StartupLogic.class).earlyStart();
	}

	private void loadPlugins(final ServletContextEvent evt) {
		// start all 'configuration loaders'
		final String basepack = "org.cmdbuild.plugins.";
		final String[] loaders = evt.getServletContext().getInitParameter("moduleLoaders").split(",");
		for (final String loader : loaders) {
			try {
				logger.debug("Initialize plugin: " + loader);
				((CmdbuildModuleLoader) Class.forName(basepack + loader).newInstance()).init(evt.getServletContext());
			} catch (final Exception e) {
				logger.error("Failed to load '" + loader + "' module!");
				e.printStackTrace();
			}
		}
	}

	private void stopSchedulerService() {
		applicationContext().getBean(SchedulerLogic.class).stopScheduler();
	}

}
