package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.ViewPrivilegeFetcher;

public class ViewPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final CMDataView dataView;
	private final ViewConverter viewConverter;
	private Long groupId;

	public ViewPrivilegeFetcherFactory(final CMDataView view, final ViewConverter viewConverter) {
		this.dataView = view;
		this.viewConverter = viewConverter;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new ViewPrivilegeFetcher(dataView, groupId, viewConverter);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
