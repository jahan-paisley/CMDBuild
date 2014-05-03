package org.cmdbuild.workflow.service;

public interface RemoteSharkServiceConfiguration {

	public static interface ChangeListener {

		void configurationChanged();

	}

	String getServerUrl();

	String getUsername();

	String getPassword();

	void addListener(ChangeListener listener);

}