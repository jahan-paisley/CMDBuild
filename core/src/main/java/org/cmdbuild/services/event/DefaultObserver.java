package org.cmdbuild.services.event;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultObserver implements Observer {

	private static final Marker marker = MarkerFactory.getMarker(DefaultObserver.class.getName());

	public static enum Phase {
		AFTER_CREATE, //
		BEFORE_UPDATE, //
		AFTER_UPDATE, //
		BEFORE_DELETE, //
	}

	public static class Builder implements org.cmdbuild.common.Builder<DefaultObserver> {

		private final Map<Phase, List<Command>> commandsByPhase;

		private Builder() {
			// use factory method

			commandsByPhase = Maps.newEnumMap(Phase.class);
			for (final Phase element : Phase.values()) {
				commandsByPhase.put(element, Lists.<Command> newArrayList());
			}
		}

		@Override
		public DefaultObserver build() {
			validate();
			return new DefaultObserver(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder add(final Command command, final Phase phase) {
			commandsByPhase.get(phase).add(command);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Map<Phase, ? extends Iterable<Command>> commandsByPhase;

	private DefaultObserver(final Builder builder) {
		this.commandsByPhase = builder.commandsByPhase;
	}

	@Override
	public void afterCreate(final CMCard current) {
		executeAllForPhase(Phase.AFTER_CREATE, Contexts.afterCreate() //
				.withCard(current));
	}

	@Override
	public void beforeUpdate(final CMCard current, final CMCard next) {
		executeAllForPhase(Phase.BEFORE_UPDATE, Contexts.beforeUpdate() //
				.withActual(current) //
				.withNext(next));
	}

	@Override
	public void afterUpdate(final CMCard previous, final CMCard current) {
		executeAllForPhase(Phase.AFTER_UPDATE, Contexts.afterUpdate() //
				.withPrevious(previous) //
				.withActual(current));
	}

	@Override
	public void beforeDelete(final CMCard current) {
		executeAllForPhase(Phase.BEFORE_DELETE, Contexts.beforeDelete() //
				.withCard(current));
	}

	private void executeAllForPhase(final Phase phase, final org.cmdbuild.common.Builder<? extends Context> context) {
		executeAllForPhase(phase, context.build());
	}

	private void executeAllForPhase(final Phase phase, final Context context) {
		logger.debug(marker, "executing all commands for phase '{}'", phase);
		for (final Command command : commandsByPhase.get(phase)) {
			command.execute(context);
		}
	}

}
