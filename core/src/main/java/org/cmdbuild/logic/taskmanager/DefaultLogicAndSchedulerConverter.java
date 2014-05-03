package org.cmdbuild.logic.taskmanager;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;

public class DefaultLogicAndSchedulerConverter implements LogicAndSchedulerConverter {

	public static interface JobFactory<T extends ScheduledTask> {

		public Job create(ScheduledTask task);

	}

	public static abstract class AbstractJobFactory<T extends ScheduledTask> implements JobFactory<T> {

		protected static final Logger logger = DefaultLogicAndSchedulerConverter.logger;
		protected static final Marker marker = MarkerFactory.getMarker(AbstractJobFactory.class.getName());

		protected abstract Class<T> getType();

		@Override
		public final Job create(final ScheduledTask task) {
			return doCreate(getType().cast(task));
		}

		protected abstract Job doCreate(T task);

	}

	private static final Logger logger = TaskManagerLogic.logger;

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter, TaskVistor {

		private final Map<Class<? extends ScheduledTask>, JobFactory<? extends ScheduledTask>> factories;
		private final ScheduledTask source;

		private Job job;

		public DefaultLogicAsSourceConverter(
				final Map<Class<? extends ScheduledTask>, JobFactory<? extends ScheduledTask>> factories,
				final ScheduledTask source) {
			this.factories = factories;
			this.source = source;
		}

		@Override
		public Job toJob() {
			source.accept(this);
			Validate.notNull(job, "conversion error");
			return job;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			job = factories.get(task.getClass()).create(task);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			job = factories.get(task.getClass()).create(task);
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			throw new UnsupportedOperationException("invalid task " + task);
		}

	}

	private final Map<Class<? extends ScheduledTask>, JobFactory<? extends ScheduledTask>> factories;

	public DefaultLogicAndSchedulerConverter() {
		factories = Maps.newHashMap();
	}

	public <T extends ScheduledTask> void register(final Class<T> type, final JobFactory<T> factory) {
		factories.put(type, factory);
	};

	@Override
	public LogicAsSourceConverter from(final ScheduledTask source) {
		return new DefaultLogicAsSourceConverter(factories, source);
	}

}
