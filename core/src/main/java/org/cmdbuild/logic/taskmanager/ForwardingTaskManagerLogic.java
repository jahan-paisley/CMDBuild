package org.cmdbuild.logic.taskmanager;

public abstract class ForwardingTaskManagerLogic implements TaskManagerLogic {

	private final TaskManagerLogic delegate;

	protected ForwardingTaskManagerLogic(final TaskManagerLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	public Long create(final Task task) {
		return delegate.create(task);
	}

	@Override
	public Iterable<Task> read() {
		return delegate.read();
	}

	@Override
	public Iterable<Task> read(final Class<? extends Task> type) {
		return delegate.read(type);
	}

	@Override
	public <T extends Task> T read(final T task, final Class<T> type) {
		return delegate.read(task, type);
	}

	@Override
	public void update(final Task task) {
		delegate.update(task);
	}

	@Override
	public void delete(final Task task) {
		delegate.delete(task);
	}

	@Override
	public void activate(final Long id) {
		delegate.activate(id);
	}

	@Override
	public void deactivate(final Long id) {
		delegate.deactivate(id);
	}

}
