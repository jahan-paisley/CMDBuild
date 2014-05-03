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
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.View;

public class ViewPrivilegeFetcher extends AbstractPrivilegeFetcher {

	private final CMDataView view;
	private final ViewConverter converter;

	public ViewPrivilegeFetcher(final CMDataView view, final Long groupId, final ViewConverter converter) {
		super(view, groupId);
		this.view = view;
		this.converter = converter;
	}

	@Override
	protected PrivilegedObjectType getPrivilegedObjectType() {
		return PrivilegedObjectType.VIEW;
	}

	@Override
	protected SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard) {
		final Integer viewId = (Integer) privilegeCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE);
		final DataViewStore<View> viewStore = DataViewStore.newInstance(view, converter);
		View view = null;
		try {
			view = viewStore.read(getFakeViewWithId(viewId));
		} catch (final NoSuchElementException ex) {
			Log.CMDBUILD.warn("Cannot fetch view with id " + viewId
					+ ". Check all references to that view in Grant table");
		}
		return view;
	}

	private Storable getFakeViewWithId(final Integer viewId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return viewId.toString();
			}
		};
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
