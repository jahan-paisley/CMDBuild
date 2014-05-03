package org.cmdbuild.services.event;

public interface ObserverCollector {

	interface IdentifiableObserver extends Observer {

		String getIdentifier();

	}

	void add(IdentifiableObserver element);

	void remove(IdentifiableObserver element);

	public Observer allInOneObserver();

}