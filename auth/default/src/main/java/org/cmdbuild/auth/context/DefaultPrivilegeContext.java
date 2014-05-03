package org.cmdbuild.auth.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultPrivilegeContext implements PrivilegeContext {

	public static class DefaultPrivilegedObjectMetadata implements PrivilegedObjectMetadata {

		private final List<String> privilegeFilters;
		private final List<String> disabledAttributes;

		private DefaultPrivilegedObjectMetadata() {
			privilegeFilters = new ArrayList<String>();
			disabledAttributes = new ArrayList<String>();
		}

		@Override
		public List<String> getFilters() {
			return privilegeFilters;
		}

		@Override
		public List<String> getAttributesPrivileges() {
			return disabledAttributes;
		}

	}

	public static class DefaultPrivilegeContextBuilder implements Builder<DefaultPrivilegeContext> {

		private final Map<String, List<CMPrivilege>> objectPrivileges;
		private final Map<String, List<String>> privilegeFilters;
		private final Map<String, List<String>> disabledAttributes;

		private DefaultPrivilegeContextBuilder() {
			objectPrivileges = new HashMap<String, List<CMPrivilege>>();
			privilegeFilters = new HashMap<String, List<String>>();
			disabledAttributes = new HashMap<String, List<String>>();
		}

		/**
		 * @deprecated use withPrivileges instead in order to store also
		 *             privileges for rows and columns
		 */
		@Deprecated
		public void withPrivilege(final CMPrivilege privilege, final CMPrivilegedObject object) {
			Validate.notNull(object);
			Validate.notNull(privilege);
			addPrivilege(privilege, object.getPrivilegeId());
		}

		/**
		 * @deprecated use withPrivileges instead in order to store also
		 *             privileges for rows and columns
		 */
		@Deprecated
		public void withPrivilege(final CMPrivilege privilege) {
			Validate.notNull(privilege);
			addPrivilege(privilege, DefaultPrivileges.GLOBAL_PRIVILEGE_ID);
		}

		public void withPrivileges(final Iterable<PrivilegePair> privileges) {
			Validate.notNull(privileges);
			for (final PrivilegePair pair : privileges) {
				addPrivilege(pair.privilege, pair.name);
				if (pair.privilegedObjectType != null
						&& pair.privilegedObjectType.equals(PrivilegedObjectType.CLASS.getValue())) {
					addPrivilegeFilter(pair.name, pair.privilegeFilter);
					calculateDisabledAttributes(pair.name, Arrays.asList(pair.attributesPrivileges));
				}
			}
		}

		private void addPrivilege(final CMPrivilege privilege, final String privilegeId) {
			final List<CMPrivilege> grantedPrivileges = getOrCreatePrivilegeList(privilegeId);
			mergePrivilege(privilege, grantedPrivileges);
		}

		private List<CMPrivilege> getOrCreatePrivilegeList(final String privilegeId) {
			final List<CMPrivilege> grantedPrivileges;
			if (objectPrivileges.containsKey(privilegeId)) {
				grantedPrivileges = objectPrivileges.get(privilegeId);
			} else {
				grantedPrivileges = new ArrayList<CMPrivilege>(1);
				objectPrivileges.put(privilegeId, grantedPrivileges);
			}
			return grantedPrivileges;
		}

		private void mergePrivilege(final CMPrivilege newPrivilege, final List<CMPrivilege> grantedPrivileges) {
			final Iterator<CMPrivilege> iter = grantedPrivileges.iterator();
			while (iter.hasNext()) {
				final CMPrivilege oldPrivilege = iter.next();
				if (oldPrivilege.implies(newPrivilege)) {
					// New pivilege is implied by exising privilege
					return;
				}
				if (newPrivilege.implies(oldPrivilege)) {
					iter.remove();
				}
			}
			grantedPrivileges.add(newPrivilege);
		}

		private void addPrivilegeFilter(final String privilegeId, final String privilegeFilter) {
			List<String> currentlyStoredPrivilegeFilters;
			if (!privilegeFilters.containsKey(privilegeId)) {
				currentlyStoredPrivilegeFilters = new ArrayList<String>();
				privilegeFilters.put(privilegeId, currentlyStoredPrivilegeFilters);
			} else {
				currentlyStoredPrivilegeFilters = privilegeFilters.get(privilegeId);
			}
			currentlyStoredPrivilegeFilters.add(privilegeFilter);
		}

		private void calculateDisabledAttributes(final String privilegeId, final List<String> attributesToDisable) {
			List<String> storedDisabledAttributes;
			if (!disabledAttributes.containsKey(privilegeId)) {
				storedDisabledAttributes = new ArrayList<String>();
				storedDisabledAttributes.addAll(attributesToDisable);
				disabledAttributes.put(privilegeId, storedDisabledAttributes);
			} else {
				storedDisabledAttributes = disabledAttributes.get(privilegeId);
				removeAttributesNotSatisfyingIntersectionBetween(storedDisabledAttributes, attributesToDisable);
			}
		}

		private void removeAttributesNotSatisfyingIntersectionBetween(
				final List<String> currentlyStoredDisabledAttributes, final List<String> attributesToDisable) {

			final List<String> attributesToRemoveFromDisabled = Lists.newArrayList();
			for (final String currentlyStoredAttribute : currentlyStoredDisabledAttributes) {
				if (!attributesToDisable.contains(currentlyStoredAttribute)) {
					attributesToRemoveFromDisabled.add(currentlyStoredAttribute);
				}
			}
			currentlyStoredDisabledAttributes.removeAll(attributesToRemoveFromDisabled);
		}

		@Override
		public DefaultPrivilegeContext build() {
			final Map<String, PrivilegedObjectMetadata> metadata = buildPrivilegedObjectMetadata();
			return new DefaultPrivilegeContext(this, metadata);
		}

		private Map<String, PrivilegedObjectMetadata> buildPrivilegedObjectMetadata() {
			final Map<String, PrivilegedObjectMetadata> metadataMap = Maps.newHashMap();
			for (final String privilegeId : privilegeFilters.keySet()) {
				final PrivilegedObjectMetadata metadata = new DefaultPrivilegedObjectMetadata();
				final List<String> privilegeFiltersForClass = privilegeFilters.get(privilegeId);
				if (!privilegeFiltersForClass.contains(null)) {
					metadata.getFilters().addAll(privilegeFiltersForClass);
				}
				metadata.getAttributesPrivileges().addAll(disabledAttributes.get(privilegeId));
				metadataMap.put(privilegeId, metadata);
			}
			return metadataMap;
		}

		public Map<String, List<CMPrivilege>> getObjectPrivileges() {
			return objectPrivileges;
		}

	}

	public static DefaultPrivilegeContextBuilder newBuilderInstance() {
		return new DefaultPrivilegeContextBuilder();
	}

	private final Map<String, List<CMPrivilege>> objectPrivileges;
	private final Map<String, PrivilegedObjectMetadata> privilegedObjectMetadata;

	private DefaultPrivilegeContext(final DefaultPrivilegeContextBuilder builder,
			final Map<String, PrivilegedObjectMetadata> metadata) {
		this.objectPrivileges = builder.getObjectPrivileges();
		this.privilegedObjectMetadata = metadata;
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return hasPrivilege(DefaultPrivileges.ADMINISTRATOR);
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return hasPrivilege(DefaultPrivileges.DATABASE_DESIGNER);
	}

	@Override
	public boolean hasPrivilege(final CMPrivilege privilege) {
		return hasPrivilege(privilege, DefaultPrivileges.GLOBAL_PRIVILEGE_ID);
	}

	@Override
	public boolean hasReadAccess(final CMPrivilegedObject privilegedObject) {
		if (privilegedObject instanceof CMDomain) {
			final CMDomain domain = (CMDomain) privilegedObject;
			return domainHasPrivilege(domain, DefaultPrivileges.READ);
		}
		return hasPrivilege(DefaultPrivileges.READ, privilegedObject);
	}

	private boolean domainHasPrivilege(final CMDomain domain, final CMPrivilege privilege) {
		final CMClass class1 = domain.getClass1();
		final CMClass class2 = domain.getClass2();
		return hasPrivilege(privilege, class1) //
				&& hasPrivilege(privilege, class2);
	}

	@Override
	public boolean hasWriteAccess(final CMPrivilegedObject privilegedObject) {
		if (privilegedObject instanceof CMDomain) {
			final CMDomain domain = (CMDomain) privilegedObject;
			return domainHasPrivilege(domain, DefaultPrivileges.WRITE);
		}
		return hasPrivilege(DefaultPrivileges.WRITE, privilegedObject);
	}

	@Override
	public boolean hasPrivilege(final CMPrivilege requested, final CMPrivilegedObject privilegedObject) {
		return hasPrivilege(requested, DefaultPrivileges.GLOBAL_PRIVILEGE_ID)
				|| hasPrivilege(requested, privilegedObject.getPrivilegeId());
	}

	private final boolean hasPrivilege(final CMPrivilege requested, final String privilegeId) {
		final List<CMPrivilege> grantedPrivileges = objectPrivileges.get(privilegeId);
		if (grantedPrivileges != null) {
			for (final CMPrivilege granted : grantedPrivileges) {
				if (granted.implies(requested)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the privileges for an object. Used by tests.
	 */
	public List<CMPrivilege> getPrivilegesFor(final CMPrivilegedObject object) {
		return objectPrivileges.get(object.getPrivilegeId());
	}

	/**
	 * Note: must be used only by tests
	 */
	public List<PrivilegePair> getAllPrivileges() {
		final List<PrivilegePair> allPrivileges = new ArrayList<PrivilegePair>();
		for (final Map.Entry<String, List<CMPrivilege>> entry : objectPrivileges.entrySet()) {
			for (final CMPrivilege priv : entry.getValue()) {
				final PrivilegePair privPair = new PrivilegePair(entry.getKey(), priv);
				allPrivileges.add(privPair);
			}
		}
		return allPrivileges;
	}

	@Override
	public PrivilegedObjectMetadata getMetadata(final CMPrivilegedObject privilegedObject) {
		final String privilegeId = privilegedObject.getPrivilegeId();
		return privilegedObjectMetadata.get(privilegeId);
	}

}
