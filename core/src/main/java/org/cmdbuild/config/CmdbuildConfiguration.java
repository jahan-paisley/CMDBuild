package org.cmdbuild.config;

import java.util.Locale;

public interface CmdbuildConfiguration {

	Locale getLocale();

	String getLanguage();

	void setLanguage(String language);

	boolean useLanguagePrompt();

	void setLanguagePrompt(boolean languagePrompt);

	String getStartingClassName();

	void setStartingClass(String startingClass);

	String getDemoModeAdmin();

	void setInstanceName(String instanceName);

	String getInstanceName();

	void setTabsPosition(String instanceName);

	String getTabsPosition();

	int getSessionTimoutOrZero();

	boolean getLockCard();

	boolean getLockCardUserVisible();

	long getLockCardTimeOut();

	void setLockCard(boolean lock);

	void setLockCardUserVisible(boolean show);

	void setLockCardTimeOut(long seconds);

}
