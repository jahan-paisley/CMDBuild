package org.cmdbuild.dao.view.user;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entrytype.CMDomain;

public class UserDomain extends UserEntryType implements CMDomain {

	static UserDomain newInstance(final UserDataView view, final CMDomain inner) {
		final PrivilegeContext privilegeContext = view.getPrivilegeContext();
		if (isUserAccessible(privilegeContext, inner)) {
			return new UserDomain(view, inner);
		} else {
			return null;
		}
	}

	public static boolean isUserAccessible(final PrivilegeContext privilegeContext, final CMDomain inner) {
		if (inner == null) {
			return false;
		}

		if (inner.isSystem() && !inner.isSystemButUsable()) {
			return false;
		}

		return (privilegeContext.hasDatabaseDesignerPrivileges() || //
		(UserClass.isUserAccessible(privilegeContext, inner.getClass1()) && //
		UserClass.isUserAccessible(privilegeContext, inner.getClass2())));
	}

	private final CMDomain inner;

	private UserDomain(final UserDataView view, final CMDomain inner) {
		super(inner, view);
		this.inner = inner;
	}

	@Override
	public UserClass getClass1() {
		return UserClass.newInstance(view, inner.getClass1());
	}

	@Override
	public UserClass getClass2() {
		return UserClass.newInstance(view, inner.getClass2());
	}

	@Override
	public String getDescription1() {
		return inner.getDescription1();
	}

	@Override
	public String getDescription2() {
		return inner.getDescription2();
	}

	@Override
	public String getCardinality() {
		return inner.getCardinality();
	}

	@Override
	public boolean isMasterDetail() {
		return inner.isMasterDetail();
	}

	@Override
	public String getMasterDetailDescription() {
		return inner.getMasterDetailDescription();
	}

}
