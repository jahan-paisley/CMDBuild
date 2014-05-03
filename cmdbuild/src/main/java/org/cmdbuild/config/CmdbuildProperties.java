package org.cmdbuild.config;

import java.util.Locale;

import org.cmdbuild.services.Settings;

public class CmdbuildProperties extends DefaultProperties implements CmdbuildConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "cmdbuild";

	private static final String REFERENCE_COMBO_LIMIT = "referencecombolimit";
	private static final String STARTING_CLASS = "startingclass";
	private static final String RELATION_LIMIT = "relationlimit";
	private static final String LANGUAGE = "language";
	private static final String POPUP_PERCENTAGE_HEIGHT = "popuppercentageheight";
	private static final String POPUP_PERCENTAGE_WIDTH = "popuppercentagewidth";
	private static final String GRID_CARD_RATIO = "grid_card_ratio";
	private static final String ROW_LIMIT = "rowlimit";
	private static final String LANGUAGE_PROMPT = "languageprompt";
	private static final String SESSION_TIMEOUT = "session.timeout";
	private static final String INSTANCE_NAME = "instance_name";
	private static final String TABS_POSITION = "card_tab_position";
	private static final String LOCK_CARD = "lockcardenabled";
	private static final String LOCKER_CARD_USER_VISIBLE = "lockcarduservisible";
	private static final String LOCK_CARD_TIME_OUT = "lockcardtimeout";

	private static final String DEMO_MODE_ADMIN = "demomode";

	public CmdbuildProperties() {
		super();
		setProperty(REFERENCE_COMBO_LIMIT, "500");
		setProperty(STARTING_CLASS, "");
		setProperty(RELATION_LIMIT, "20");
		setProperty(LANGUAGE, "en");
		setProperty(POPUP_PERCENTAGE_HEIGHT, "80");
		setProperty(POPUP_PERCENTAGE_WIDTH, "80");
		setProperty(GRID_CARD_RATIO, "50");
		setProperty(ROW_LIMIT, "20");
		setProperty(LANGUAGE_PROMPT, String.valueOf(true));
		setProperty(SESSION_TIMEOUT, "");
		setProperty(INSTANCE_NAME, "");
		setProperty(TABS_POSITION, "bottom");
		setProperty(LOCK_CARD, String.valueOf(true));
		setProperty(LOCKER_CARD_USER_VISIBLE, String.valueOf(true));
		setProperty(LOCK_CARD_TIME_OUT, "300");
	}

	public static CmdbuildProperties getInstance() {
		return (CmdbuildProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public Locale getLocale() {
		final String[] splitLang = getLanguage().split("_");
		if (splitLang.length > 1) {
			return new Locale(splitLang[0], splitLang[1]);
		} else {
			return new Locale(splitLang[0]);
		}
	}

	@Override
	public String getLanguage() {
		return getProperty(LANGUAGE);
	}

	@Override
	public void setLanguage(final String language) {
		setProperty(LANGUAGE, language);
	}

	@Override
	public boolean useLanguagePrompt() {
		return Boolean.parseBoolean(getProperty(LANGUAGE_PROMPT));
	}

	@Override
	public void setLanguagePrompt(final boolean languagePrompt) {
		setProperty(LANGUAGE_PROMPT, String.valueOf(languagePrompt));
	}

	@Override
	public String getStartingClassName() {
		return getProperty(STARTING_CLASS);
	}

	@Override
	public void setStartingClass(final String startingClass) {
		setProperty(STARTING_CLASS, startingClass);
	}

	@Override
	public String getDemoModeAdmin() {
		return getProperty(DEMO_MODE_ADMIN, "");
	}

	@Override
	public void setInstanceName(final String instanceName) {
		setProperty(INSTANCE_NAME, instanceName);
	}

	@Override
	public String getInstanceName() {
		return getProperty(INSTANCE_NAME, "");
	}

	@Override
	public void setTabsPosition(final String instanceName) {
		setProperty(TABS_POSITION, instanceName);
	}

	@Override
	public String getTabsPosition() {
		return getProperty(TABS_POSITION, "top");
	}

	@Override
	public int getSessionTimoutOrZero() {
		try {
			return Integer.parseInt(getProperty(SESSION_TIMEOUT));
		} catch (final Exception e) {
			return 0;
		}
	}

	@Override
	public boolean getLockCard() {
		return Boolean.valueOf(getProperty(LOCK_CARD));
	}

	@Override
	public boolean getLockCardUserVisible() {
		return Boolean.valueOf(getProperty(LOCKER_CARD_USER_VISIBLE));
	}

	@Override
	public long getLockCardTimeOut() {
		return Long.valueOf(getProperty(LOCK_CARD_TIME_OUT));
	}

	@Override
	public void setLockCard(final boolean lock) {
		setProperty(LOCK_CARD, String.valueOf(lock));
	}

	@Override
	public void setLockCardUserVisible(final boolean show) {
		setProperty(LOCKER_CARD_USER_VISIBLE, String.valueOf(show));
	}

	@Override
	public void setLockCardTimeOut(final long seconds) {
		setProperty(LOCK_CARD_TIME_OUT, String.valueOf(seconds));
	}

}
