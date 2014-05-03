package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.ATTRIBUTES_PRIVILEGES_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GROUP_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGE_FILTER_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.TYPE_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;

public abstract class AbstractPrivilegeFetcher implements PrivilegeFetcher {

	private static final Logger logger = Log.PERSISTENCE;
	private static final Marker marker = MarkerFactory.getMarker(AbstractPrivilegeFetcher.class.getName());

	private static final List<CMAttribute> EMPTY_ATTRIBUTES = Collections.emptyList();

	private final CMDataView view;
	private final Long groupId;

	protected AbstractPrivilegeFetcher(final CMDataView view, final Long groupId) {
		this.view = view;
		this.groupId = groupId;
	}

	/**
	 * Template method that uses methods that will be defined in subclasses
	 */
	@Override
	public Iterable<PrivilegePair> fetch() {
		final CMClass privilegeClass = view.findClass(GRANT_CLASS_NAME);
		final CMQueryResult result = view
				.select(anyAttribute(privilegeClass))
				.from(privilegeClass)
				.where(and(condition(attribute(privilegeClass, GROUP_ID_ATTRIBUTE), eq(groupId)),
						condition(attribute(privilegeClass, TYPE_ATTRIBUTE), eq(getPrivilegedObjectType().getValue()))))
				.run();

		final List<PrivilegePair> privilegesForDefinedType = Lists.newArrayList();

		for (final CMQueryRow row : result) {
			final CMCard privilegeCard = row.getCard(privilegeClass);
			final SerializablePrivilege privObject = extractPrivilegedObject(privilegeCard);
			if (privObject == null) {
				logger.warn(marker, "cannot get privilege object for privilege card '{}'", privilegeCard.getId());
			} else {
				final CMPrivilege privilege = extractPrivilegeMode(privilegeCard);
				final PrivilegePair privilegePair = new PrivilegePair(privObject, getPrivilegedObjectType().getValue(),
						privilege);
				privilegePair.privilegeFilter = extractPrivilegeFilter(privilegeCard);
				privilegePair.attributesPrivileges = extractAttributesPrivileges(privilegeCard);
				privilegesForDefinedType.add(privilegePair);
			}
		}
		return privilegesForDefinedType;
	}

	private String extractPrivilegeFilter(final CMCard privilegeCard) {
		if (getPrivilegedObjectType().getValue().equals(PrivilegedObjectType.CLASS.getValue())) {
			final Object privilegeFilter = privilegeCard.get(PRIVILEGE_FILTER_ATTRIBUTE);
			if (privilegeFilter != null) {
				return (String) privilegeFilter;
			}
		}
		return null;
	}

	private String[] extractAttributesPrivileges(final CMCard privilegeCard) {
		final Iterable<? extends CMAttribute> attributes = getClassAttributes(privilegeCard);
		final List<String> mergedAttributesPrivileges = Lists.newArrayList();

		// Extract the stored privileges
		final Object groupLevelAttributesPrivilegesObject = privilegeCard.get(ATTRIBUTES_PRIVILEGES_ATTRIBUTE);
		final String[] groupLevelAttributesPrivileges;
		if (groupLevelAttributesPrivilegesObject != null) {
			groupLevelAttributesPrivileges = (String[]) groupLevelAttributesPrivilegesObject;
		} else {
			groupLevelAttributesPrivileges = new String[0];
		}

		/*
		 * Iterate the class attributes: if retrieve a group defined privilege
		 * for the current attribute use it, otherwise use the editing mode
		 * defined globally for the attribute
		 */
		for (final CMAttribute attribute : attributes) {
			final String privilege = getGroupLevelPrivilegeForAttribute(attribute, groupLevelAttributesPrivileges);
			if (privilege != null) {
				mergedAttributesPrivileges.add(privilege);
			} else {
				// use the mode defined in the attribute configuration
				final String mode = attribute.getMode().name().toLowerCase();
				mergedAttributesPrivileges.add(String.format("%s:%s", attribute.getName(), mode));
			}
		}

		return mergedAttributesPrivileges.toArray(new String[mergedAttributesPrivileges.size()]);
	}

	private String getGroupLevelPrivilegeForAttribute(final CMAttribute attribue, final String[] privileges) {
		String privilege = null;

		for (int i = 0, l = privileges.length; i < l; ++i) {
			final String currentPrivilege = privileges[i];
			if (currentPrivilege != null && currentPrivilege.startsWith(attribue.getName() + ":")) {
				privilege = currentPrivilege;
				break;
			}
		}

		return privilege;
	}

	private Iterable<? extends CMAttribute> getClassAttributes(final CMCard privilegeCard) {
		final CMClass cmClass = view.findClass( //
				privilegeCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE, Long.class) //
				);

		/*
		 * cmClass is null if the privilege card describes privileges over
		 * filter/dashboard/view
		 */
		return (cmClass == null) ? EMPTY_ATTRIBUTES : cmClass.getAttributes();
	}

	/*****************************************************************************
	 * The following methods must be defined by all classes that extend this
	 * class
	 *****************************************************************************/

	protected abstract PrivilegedObjectType getPrivilegedObjectType();

	protected abstract SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard);

	protected abstract CMPrivilege extractPrivilegeMode(final CMCard privilegeCard);

}
