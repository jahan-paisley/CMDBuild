package org.cmdbuild.services.scheduler;

public abstract class ForwardingCommand implements Command {

	private final Command delegate;

	public ForwardingCommand(final Command delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute() {
		delegate.execute();
	}

}