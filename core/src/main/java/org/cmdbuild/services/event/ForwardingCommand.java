package org.cmdbuild.services.event;

public abstract class ForwardingCommand implements Command {

	private final Command delegate;

	protected ForwardingCommand(final Command delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(final Context context) {
		delegate.execute(context);
	}

}
