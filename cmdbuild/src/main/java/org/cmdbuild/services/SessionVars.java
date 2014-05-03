package org.cmdbuild.services;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.model.Report;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.cmdbuild.spring.annotations.CmdbuildComponent;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * Should be merged with the RequestListener
 */

@CmdbuildComponent
public class SessionVars implements UserStore, AuthenticationStore, LanguageStore {

	private static final String AUTH_KEY = "auth";
	private static final String AUTH_TYPE_KEY = "authType";
	private static final String LANGUAGE_KEY = "language";
	private static final String LOGIN_KEY = "login";
	private static final String REPORTFACTORY_KEY = "ReportFactorySessionObj";
	private static final String NEWREPORT_KEY = "newReport";
	private static final String CSVDATA_KEY = "csvdata";

	private final RequestListener requestListener;
	private final CmdbuildConfiguration configuration;

	@Autowired
	public SessionVars( //
			final RequestListener requestListener, //
			final CmdbuildConfiguration configuration //
	) {
		this.requestListener = requestListener;
		this.configuration = configuration;
	}

	@Override
	public OperationUser getUser() {
		OperationUser operationUser = (OperationUser) requestListener.getCurrentSessionObject(AUTH_KEY);
		if (operationUser == null) {
			operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), new NullGroup());
			setUser(operationUser);
		}
		return operationUser;
	}

	@Override
	public void setUser(final OperationUser user) {
		requestListener.setCurrentSessionObject(AUTH_KEY, user);
	}

	@Override
	public UserType getType() {
		UserType type = (UserType) requestListener.getCurrentSessionObject(AUTH_TYPE_KEY);
		if (type == null) {
			type = UserType.APPLICATION;
			setType(type);
		}
		return type;
	}

	@Override
	public void setType(final UserType type) {
		requestListener.setCurrentSessionObject(AUTH_TYPE_KEY, type);
	}

	@Override
	public Login getLogin() {
		Login type = (Login) requestListener.getCurrentSessionObject(LOGIN_KEY);
		if (type == null) {
			type = Login.newInstance(EMPTY);
			setLogin(type);
		}
		return type;
	}

	@Override
	public void setLogin(final Login login) {
		requestListener.setCurrentSessionObject(LOGIN_KEY, login);
	}

	@Override
	public String getLanguage() {
		String language = (String) requestListener.getCurrentSessionObject(LANGUAGE_KEY);
		if (language == null) {
			language = configuration.getLanguage();
			setLanguage(language);
		}
		return language;
	}

	@Override
	public void setLanguage(final String language) {
		requestListener.setCurrentSessionObject(LANGUAGE_KEY, language);
	}

	public ReportFactory getReportFactory() {
		return (ReportFactory) requestListener.getCurrentSessionObject(REPORTFACTORY_KEY);
	}

	public void setReportFactory(final ReportFactory value) {
		requestListener.setCurrentSessionObject(REPORTFACTORY_KEY, value);
	}

	public void removeReportFactory() {
		requestListener.removeCurrentSessionObject(REPORTFACTORY_KEY);
	}

	public Report getNewReport() {
		return (Report) requestListener.getCurrentSessionObject(NEWREPORT_KEY);
	}

	public void setNewReport(final Report newReport) {
		requestListener.setCurrentSessionObject(NEWREPORT_KEY, newReport);
	}

	public void removeNewReport() {
		requestListener.removeCurrentSessionObject(NEWREPORT_KEY);
	}

	public CSVData getCsvData() {
		return (CSVData) requestListener.getCurrentSessionObject(CSVDATA_KEY);
	}

	public void setCsvData(final CSVData value) {
		requestListener.setCurrentSessionObject(CSVDATA_KEY, value);
	}
}
