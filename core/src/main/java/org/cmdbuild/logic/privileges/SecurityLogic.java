package org.cmdbuild.logic.privileges;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.ATTRIBUTES_PRIVILEGES_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GROUP_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGE_FILTER_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.STATUS_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.TYPE_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.View;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;

import com.google.common.collect.Lists;

public class SecurityLogic implements Logic {

	public static final String GROUP_ATTRIBUTE_DISABLEDMODULES = "DisabledModules";
	public static final String GROUP_ATTRIBUTE_DISABLEDCARDTABS = "DisabledCardTabs";
	public static final String GROUP_ATTRIBUTE_DISABLEDPROCESSTABS = "DisabledProcessTabs";
	public static final String GROUP_ATTRIBUTE_HIDESIDEPANEL = "HideSidePanel";
	public static final String GROUP_ATTRIBUTE_FULLSCREEN = "FullScreenMode";
	public static final String GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD = "SimpleHistoryModeForCard";
	public static final String GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS = "SimpleHistoryModeForProcess";
	public static final String GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED = "ProcessWidgetAlwaysEnabled";
	public static final String GROUP_ATTRIBUTE_CLOUD_ADMIN = "CloudAdmin";

	private final CMDataView view;
	private final CMClass grantClass;
	private final ViewConverter viewConverter;
	private final DataViewFilterStore filterStore;

	public SecurityLogic( //
			final CMDataView view, //
			final ViewConverter viewConverter, //
			final DataViewFilterStore filterStore //
	) {
		this.view = view;
		this.grantClass = view.findClass(GRANT_CLASS_NAME);
		this.viewConverter = viewConverter;
		this.filterStore = filterStore;
	}

	public List<PrivilegeInfo> fetchClassPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedClassPrivileges = fetchStoredPrivilegesForGroup( //
				groupId, //
				PrivilegedObjectType.CLASS //
		);

		final Iterable<CMClass> nonReservedActiveClasses = filterNonReservedAndNonBaseClasses();

		for (final CMClass clazz : nonReservedActiveClasses) {
			final Long classId = clazz.getId();
			if (!isPrivilegeAlreadyStored(classId, fetchedClassPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, PrivilegeMode.NONE);

				final List<String> attributesPrivileges = new ArrayList<String>();
				for (final CMAttribute attribute : clazz.getAttributes()) {
					final String mode = attribute.getMode().name().toLowerCase();
					attributesPrivileges.add(String.format("%s:%s", attribute.getName(), mode));
				}

				pi.setAttributesPrivileges( //
				attributesPrivileges.toArray(new String[attributesPrivileges.size()]) //
				);

				fetchedClassPrivileges.add(pi);
			}
		}
		return fetchedClassPrivileges;
	}

	public List<PrivilegeInfo> fetchViewPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedViewPrivileges = fetchStoredPrivilegesForGroup(groupId,
				PrivilegedObjectType.VIEW);
		final Iterable<View> allViews = fetchAllViews();
		for (final View view : allViews) {
			final Long viewId = view.getId();
			if (!isPrivilegeAlreadyStored(viewId, fetchedViewPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, view, PrivilegeMode.NONE);
				fetchedViewPrivileges.add(pi);
			}
		}
		return fetchedViewPrivileges;
	}

	public List<PrivilegeInfo> fetchFilterPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedFilterPrivileges = fetchStoredPrivilegesForGroup(groupId,
				PrivilegedObjectType.FILTER);
		final Iterable<Filter> allGroupsFilters = fetchAllGroupsFilters();
		for (final Filter filter : allGroupsFilters) {
			final Long filterId = Long.valueOf(filter.getId());
			if (!isPrivilegeAlreadyStored(filterId, fetchedFilterPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, filter, PrivilegeMode.NONE);
				fetchedFilterPrivileges.add(pi);
			}
		}
		return fetchedFilterPrivileges;
	}

	private Iterable<View> fetchAllViews() {
		// TODO must be an external dependency
		final DataViewStore<View> viewStore = DataViewStore.newInstance(view, viewConverter);
		return viewStore.list();
	}

	private Iterable<Filter> fetchAllGroupsFilters() {
		return filterStore.fetchAllGroupsFilters();
	}

	/**
	 * Fetches the privileges for specified group. NOTE that the group has no
	 * privilege if it is retrieved and fetched as 'none' or if it is not stored
	 * in the database
	 */
	private List<PrivilegeInfo> fetchStoredPrivilegesForGroup(final Long groupId, final PrivilegedObjectType type) {
		final PrivilegeFetcherFactory privilegeFetcherFactory = getPrivilegeFetcherFactoryForType(type);
		privilegeFetcherFactory.setGroupId(groupId);
		final PrivilegeFetcher privilegeFetcher = privilegeFetcherFactory.create();
		final Iterable<PrivilegePair> privilegePairs = privilegeFetcher.fetch();
		return fromPrivilegePairToPrivilegeInfo(privilegePairs, groupId);
	}

	/**
	 * TODO: use a visitor instead to be sure to consider all cases
	 */
	private PrivilegeFetcherFactory getPrivilegeFetcherFactoryForType(final PrivilegedObjectType type) {
		switch (type) {
		case VIEW:
			// TODO must me an external dependency
			return new ViewPrivilegeFetcherFactory(view, viewConverter);
		case CLASS:
			return new CMClassPrivilegeFetcherFactory(view);
		case FILTER:
			return new FilterPrivilegeFetcherFactory(view, filterStore);
		default:
			return null;
		}
	}

	private List<PrivilegeInfo> fromPrivilegePairToPrivilegeInfo(final Iterable<PrivilegePair> privilegePairs,
			final Long groupId) {
		final List<PrivilegeInfo> list = Lists.newArrayList();
		for (final PrivilegePair privilegePair : privilegePairs) {
			final SerializablePrivilege privilegedObject = privilegePair.privilegedObject;
			final CMPrivilege privilege = privilegePair.privilege;
			PrivilegeInfo privilegeInfo;
			if (privilege.implies(DefaultPrivileges.WRITE)) {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, PrivilegeMode.WRITE);
			} else if (privilege.implies(DefaultPrivileges.READ)) {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, PrivilegeMode.READ);
			} else {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, PrivilegeMode.NONE);
			}
			privilegeInfo.setPrivilegeFilter(privilegePair.privilegeFilter);
			privilegeInfo.setAttributesPrivileges(privilegePair.attributesPrivileges);
			list.add(privilegeInfo);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private Iterable<CMClass> filterNonReservedAndNonBaseClasses() {
		final Iterable<CMClass> classes = (Iterable<CMClass>) view.findClasses();
		final List<CMClass> nonReservedClasses = Lists.newArrayList();
		for (final CMClass clazz : classes) {
			if (!clazz.isSystem() && !clazz.isBaseClass()) {
				nonReservedClasses.add(clazz);
			}
		}
		return nonReservedClasses;
	}

	private boolean isPrivilegeAlreadyStored(final Long privilegedObjectId, final List<PrivilegeInfo> fetchedPrivileges) {
		for (final PrivilegeInfo privilegeInfo : fetchedPrivileges) {
			if (privilegeInfo.getPrivilegedObjectId() != null
					&& privilegeInfo.getPrivilegedObjectId().equals(privilegedObjectId)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * FIXME
	 * 
	 * this methods is called for two different purposes
	 * 
	 * 1) change the mode
	 * 
	 * 2) change the row and column privilege configuration remove the mode
	 * 
	 * Only flag and implement two different methods or uniform the values set
	 * in the privilegeInfo object to have always all the attributes and update
	 * them all
	 */
	public void saveClassPrivilege(final PrivilegeInfo privilegeInfo, final boolean modeOnly) {
		/*
		 * Extract the grants defined for the given group id
		 */
		final CMQueryResult grantRows = view.select(anyAttribute(grantClass)).from(grantClass).where( //
				and( //
				condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId())), //
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(PrivilegedObjectType.CLASS.getValue())) //
				) //
				) //
				.run();

		/*
		 * FIXME why does not add a condition to to the query, and extract only
		 * the row for the given entryTypeId ???
		 */
		for (final CMQueryRow row : grantRows) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long entryTypeId = grantCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE, Long.class);
			if (entryTypeId.equals(privilegeInfo.getPrivilegedObjectId())) {

				if (modeOnly) {
					// replace the privilegeInfo with the
					// data already stored to not override them
					final Object filter = grantCard.get(PRIVILEGE_FILTER_ATTRIBUTE);
					if (filter != null) {
						privilegeInfo.setPrivilegeFilter((String) filter);
					}

					final Object attributes = grantCard.get(ATTRIBUTES_PRIVILEGES_ATTRIBUTE);
					if (attributes != null) {
						privilegeInfo.setAttributesPrivileges((String[]) attributes);
					}
				} else {
					/*
					 * Iterate over the attributes privileges and keep only the
					 * ones that override the mode of the attribute
					 */
					final CMEntryType entryType = view.findClass(entryTypeId);
					final Map<String, String> attributeModes = attributesMode(entryType);
					final List<String> attributesPrivilegesToSave = new ArrayList<String>();
					for (final String attributePrivilege : privilegeInfo.getAttributesPrivileges()) {
						final String[] parts = attributePrivilege.split(":");
						final String attributeName = parts[0];
						final String privilege = parts[1];
						if (attributeModes.containsKey(attributeName)) {
							if (!attributeModes.get(attributeName).equals(privilege)) {
								attributesPrivilegesToSave.add(attributePrivilege);
							}
						}
					}

					privilegeInfo.setAttributesPrivileges( //
							attributesPrivilegesToSave.toArray( //
									new String[attributesPrivilegesToSave.size()] //
									));
				}

				updateGrantCard(grantCard, privilegeInfo);
				return;
			}
		}

		createClassGrantCard(privilegeInfo);
	}

	private Map<String, String> attributesMode(final CMEntryType entryType) {
		final Map<String, String> privileges = new HashMap<String, String>();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.isActive()) {
				final String mode = attribute.getMode().name().toLowerCase();
				privileges.put(attribute.getName(), mode);
			}
		}

		return privileges;
	}

	public void saveViewPrivilege(final PrivilegeInfo privilegeInfo) {
		final CMQueryResult result = view
				.select(anyAttribute(grantClass))
				.from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId())),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(PrivilegedObjectType.VIEW.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long storedViewId = ((Integer) grantCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE)).longValue();
			if (storedViewId.equals(privilegeInfo.getPrivilegedObjectId())) {
				updateGrantCard(grantCard, privilegeInfo);
				return;
			}
		}

		createViewGrantCard(privilegeInfo);
	}

	public void saveFilterPrivilege(final PrivilegeInfo privilegeInfo) {
		final CMQueryResult result = view
				.select(anyAttribute(grantClass))
				.from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId())),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(PrivilegedObjectType.FILTER.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long storedViewId = ((Integer) grantCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE)).longValue();
			if (storedViewId.equals(privilegeInfo.getPrivilegedObjectId())) {
				updateGrantCard(grantCard, privilegeInfo);
				return;
			}
		}

		createFilterGrantCard(privilegeInfo);
	}

	private void updateGrantCard(final CMCard grantCard, final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition mutableGrantCard = view.update(grantCard);
		if (privilegeInfo.getMode() != null) {
			// check if null to allow the update of other attributes
			// without specify the mode
			mutableGrantCard.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()); //
		}

		mutableGrantCard //
				.set(PRIVILEGE_FILTER_ATTRIBUTE, privilegeInfo.getPrivilegeFilter()) //
				.set(ATTRIBUTES_PRIVILEGES_ATTRIBUTE, privilegeInfo.getAttributesPrivileges()) //
				.save();
	}

	private void createClassGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);

		// manage the null value for the privilege mode
		// could happens updating row and column privileges
		PrivilegeMode privilegeMode = privilegeInfo.getMode();
		if (privilegeMode == null) {
			privilegeMode = PrivilegeMode.NONE;
		}

		grantCardToBeCreated //
				.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_CLASS_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeMode.getValue()) //
				.set(TYPE_ATTRIBUTE, PrivilegedObjectType.CLASS.getValue()) //
				.set(PRIVILEGE_FILTER_ATTRIBUTE, privilegeInfo.getPrivilegeFilter()) //
				.set(ATTRIBUTES_PRIVILEGES_ATTRIBUTE, privilegeInfo.getAttributesPrivileges()) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.save();
	}

	private void createViewGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_OBJECT_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()) //
				.set(TYPE_ATTRIBUTE, PrivilegedObjectType.VIEW.getValue()) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.save();
	}

	private void createFilterGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_OBJECT_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()) //
				.set(TYPE_ATTRIBUTE, PrivilegedObjectType.FILTER.getValue()) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.save();
	}

	public UIConfiguration fetchGroupUIConfiguration(final Long groupId) {
		final CMClass roleClass = view.findClass("Role");
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.run().getOnlyRow();
		final CMCard roleCard = row.getCard(roleClass);
		final UIConfiguration uiConfiguration = new UIConfiguration();

		final String[] disabledModules = (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDMODULES);
		if (!isStringArrayNull(disabledModules)) {
			uiConfiguration.setDisabledModules(disabledModules);
		}

		final String[] disabledCardTabs = (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDCARDTABS);
		if (!isStringArrayNull(disabledCardTabs)) {
			uiConfiguration.setDisabledCardTabs(disabledCardTabs);
		}

		final String[] disabledProcessTabs = (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS);
		if (!isStringArrayNull(disabledProcessTabs)) {
			uiConfiguration.setDisabledProcessTabs(disabledProcessTabs);
		}
		uiConfiguration.setHideSidePanel((Boolean) roleCard.get(GROUP_ATTRIBUTE_HIDESIDEPANEL));
		uiConfiguration.setFullScreenMode((Boolean) roleCard.get(GROUP_ATTRIBUTE_FULLSCREEN));
		uiConfiguration.setSimpleHistoryModeForCard((Boolean) roleCard.get(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD));
		uiConfiguration.setSimpleHistoryModeForProcess((Boolean) roleCard.get(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS));
		uiConfiguration.setProcessWidgetAlwaysEnabled((Boolean) roleCard
				.get(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED));
		uiConfiguration.setCloudAdmin((Boolean) roleCard.get(GROUP_ATTRIBUTE_CLOUD_ADMIN));

		return uiConfiguration;
	}

	private boolean isStringArrayNull(final String[] stringArray) {
		if (stringArray == null) {
			return true;
		} else if (stringArray.length == 0) {
			return true;
		} else if (stringArray.length == 1 && stringArray[0] == null) {
			return true;
		}
		return false;
	}

	public void saveGroupUIConfiguration(final Long groupId, final UIConfiguration configuration) {
		final CMClass roleClass = view.findClass("Role");
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.run().getOnlyRow();
		final CMCard roleCard = row.getCard(roleClass);
		final CMCardDefinition cardDefinition = view.update(roleCard);
		if (isStringArrayNull(configuration.getDisabledModules())) {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDMODULES, null);
		} else {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDMODULES, configuration.getDisabledModules());
		}
		if (isStringArrayNull(configuration.getDisabledCardTabs())) {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDCARDTABS, null);
		} else {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDCARDTABS, configuration.getDisabledCardTabs());
		}
		if (isStringArrayNull(configuration.getDisabledProcessTabs())) {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS, null);
		} else {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS, configuration.getDisabledProcessTabs());
		}
		cardDefinition.set(GROUP_ATTRIBUTE_HIDESIDEPANEL, configuration.isHideSidePanel());
		cardDefinition.set(GROUP_ATTRIBUTE_FULLSCREEN, configuration.isFullScreenMode());
		cardDefinition.set(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD, configuration.isSimpleHistoryModeForCard());
		cardDefinition.set(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS, configuration.isSimpleHistoryModeForProcess());
		cardDefinition.set(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED, configuration.isProcessWidgetAlwaysEnabled());
		// FIXME: manage cloud admin
		cardDefinition.save();
	}

}
