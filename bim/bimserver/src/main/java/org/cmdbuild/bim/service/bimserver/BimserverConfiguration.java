package org.cmdbuild.bim.service.bimserver;



public interface BimserverConfiguration {

	public static interface ChangeListener {

		void configurationChanged();

	}

	String getUrl();

	String getUsername();

	String getPassword();

	void addListener(ChangeListener listener);

	boolean isEnabled();

	void disable();
}
