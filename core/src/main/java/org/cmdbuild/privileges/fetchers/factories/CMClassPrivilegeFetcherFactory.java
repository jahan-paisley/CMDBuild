package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.privileges.fetchers.CMClassPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;

public class CMClassPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final CMDataView dataView;
	private Long groupId;

	public CMClassPrivilegeFetcherFactory(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new CMClassPrivilegeFetcher(dataView, groupId);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
