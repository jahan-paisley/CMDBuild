package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Predicate;

public class IsReadableClass implements Predicate<CMCard> {

	private final PrivilegeContext privilegeContext;
	private final CMDataView view;

	public IsReadableClass(final CMDataView view, final PrivilegeContext privilegeContext) {
		this.privilegeContext = privilegeContext;
		this.view = view;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Long idElementClass = menuCard.get(ELEMENT_CLASS_ATTRIBUTE, Long.class);
		if (idElementClass == null) {
			return false;
		}
		final CMClass referencedClass = view.findClass(idElementClass);
		if (referencedClass == null) {
			return false;
		}
		return privilegeContext.hasReadAccess(referencedClass);
	}

}
