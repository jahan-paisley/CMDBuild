package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_OBJECT_ID_ATTRIBUTE;

import java.util.NoSuchElementException;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;

public class FilterPrivilegeFetcher extends AbstractPrivilegeFetcher {

	private final DataViewFilterStore filterStore;

	public FilterPrivilegeFetcher(final CMDataView view, final Long groupId, final DataViewFilterStore filterStore) {
		super(view, groupId);
		this.filterStore = filterStore;
	}

	@Override
	protected PrivilegedObjectType getPrivilegedObjectType() {
		return PrivilegedObjectType.FILTER;
	}

	@Override
	protected SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard) {
		final Integer filterId = (Integer) privilegeCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE);
		Filter privilegedFilter = null;
		try {
			privilegedFilter = filterStore.fetchFilter(filterId.longValue());
		} catch (final NoSuchElementException ex) {
			Log.CMDBUILD.warn("Cannot fetch filter with id " + filterId
					+ ". Check all references to that filter in Grant table");
		}
		return privilegedFilter;
	}

	@Override
	protected CMPrivilege extractPrivilegeMode(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(MODE_ATTRIBUTE);
		if (PrivilegeMode.READ.getValue().equals(type)) {
			return DefaultPrivileges.READ;
		} else if (PrivilegeMode.WRITE.getValue().equals(type)) {
			return DefaultPrivileges.WRITE;
		}
		return DefaultPrivileges.NONE;
	}
}
