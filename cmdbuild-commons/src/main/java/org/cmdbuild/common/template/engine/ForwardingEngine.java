package org.cmdbuild.common.template.engine;

public abstract class ForwardingEngine implements Engine {

	private final Engine delegate;

	protected ForwardingEngine(final Engine delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object eval(final String expression) {
		return delegate.eval(expression);
	}

}
