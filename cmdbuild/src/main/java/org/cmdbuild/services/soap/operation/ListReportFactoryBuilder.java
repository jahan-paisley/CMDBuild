package org.cmdbuild.services.soap.operation;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactoryTemplateList;

import com.google.common.collect.Lists;

public class ListReportFactoryBuilder implements ReportFactoryBuilder<ReportFactory> {

	private static final String CLASSNAME_PROPERTY = "classname";
	private static final String ATTRIBUTES_PROPERTY = "attributes";
	private static final String ATTRIBUTES_SEPARATOR = ",";

	private final CMDataView dataView;
	private final AuthenticationStore authenticationStore;
	private final CmdbuildConfiguration configuration;

	private OperationUser operationUser;
	private DataSource dataSource;
	private DataAccessLogic dataAccessLogic;
	private String extension;
	private Map<String, String> properties;

	public ListReportFactoryBuilder( //
			final CMDataView dataView, //
			final AuthenticationStore authenticationStore, //
			final CmdbuildConfiguration configuration //
	) {
		this.dataView = dataView;
		this.configuration = configuration;
		this.authenticationStore = authenticationStore;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withOperationUser(final OperationUser operationUser) {
		this.operationUser = operationUser;
		return this;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withDataAccessLogic(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
		return this;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withExtension(final String extension) {
		this.extension = extension;
		return this;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withProperties(final Map<String, String> properties) {
		this.properties = properties;
		return this;
	}

	@Override
	public ReportFactory build() {
		try {
			return new ReportFactoryTemplateList( //
					dataSource, //
					ReportExtension.valueOf(extension.toUpperCase()), //
					queryOptions(), //
					attributes(), //
					className(), //
					dataAccessLogic, //
					dataView, //
					configuration);
		} catch (final Throwable e) {
			throw new Error(e);
		}
	}

	private QueryOptions queryOptions() {
		final GuestFilter guestFilter = new GuestFilter(authenticationStore, dataView);
		final QueryOptions unfilteredCardQuery = QueryOptions.newQueryOption().build();
		final QueryOptions filteredCardQuery;
		final CMClass targetClass = dataView.findClass(className());
		if (dataView.getActivityClass().isAncestorOf(targetClass)) {
			filteredCardQuery = guestFilter.apply(targetClass, unfilteredCardQuery);
		} else {
			filteredCardQuery = unfilteredCardQuery;
		}
		return filteredCardQuery;
	}

	private List<String> attributes() {
		final String attributes = properties.get(ATTRIBUTES_PROPERTY);
		if (attributes == null) {
			return Lists.newArrayList();
		}
		return Lists.newArrayList(attributes.split(ATTRIBUTES_SEPARATOR));
	}

	private String className() {
		return properties.get(CLASSNAME_PROPERTY);
	}

}
