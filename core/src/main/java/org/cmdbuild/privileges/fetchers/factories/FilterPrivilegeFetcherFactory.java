package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.privileges.fetchers.FilterPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.services.store.DataViewFilterStore;

public class FilterPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final CMDataView dataView;
	private final DataViewFilterStore filterStore;
	private Long groupId;

	public FilterPrivilegeFetcherFactory(final CMDataView dataView, final DataViewFilterStore filterStore) {
		this.dataView = dataView;
		this.filterStore = filterStore;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new FilterPrivilegeFetcher(dataView, groupId, filterStore);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
