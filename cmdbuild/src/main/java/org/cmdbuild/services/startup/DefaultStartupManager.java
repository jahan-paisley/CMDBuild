package org.cmdbuild.services.startup;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultStartupManager implements StartupManager {

	private final Map<Startable, Condition> startables;

	public DefaultStartupManager() {
		startables = Maps.newHashMap();
	}

	@Override
	public void add(final Startable startable, final Condition condition) {
		startables.put(startable, condition);
	}

	@Override
	public void start() {
		final Collection<Startable> started = Lists.newArrayList();
		for (final Startable startable : startables.keySet()) {
			if (startables.get(startable).satisfied()) {
				startable.start();
				started.add(startable);
			}
		}
		for (final Startable startable : started) {
			startables.remove(startable);
		}
	}

}
