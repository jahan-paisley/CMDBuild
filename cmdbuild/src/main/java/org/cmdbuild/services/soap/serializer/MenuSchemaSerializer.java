package org.cmdbuild.services.soap.serializer;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.auth.PrivilegeManager.PrivilegeType;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;

import com.google.common.collect.Iterables;

public class MenuSchemaSerializer {

	private static final String PROCESS_SUPERCLASS = "superprocessclass";
	private static final String SUPERCLASS = "superclass";
	private static final String PROCESS = "processclass";
	private static final String CLASS = "class";
	private final OperationUser operationUser;
	private final DataAccessLogic dataAccessLogic;
	private final WorkflowLogic workflowLogic;
	private final MenuStore menuStore;

	public MenuSchemaSerializer( //
			final MenuStore menuStore, //
			final OperationUser operationUser, //
			final DataAccessLogic dataAccessLogic, //
			final WorkflowLogic workflowLogic //
	) {
		this.operationUser = operationUser;
		this.dataAccessLogic = dataAccessLogic;
		this.workflowLogic = workflowLogic;
		this.menuStore = menuStore;
	}

	public MenuSchema serializeVisibleClassesFromRoot(final CMClass root) {
		final MenuSchema menuSchema = new MenuSchema();
		menuSchema.setId(root.getId().intValue());
		menuSchema.setDescription(root.getDescription());
		menuSchema.setClassname(root.getIdentifier().getLocalName());
		// FIXME: add metadata serialization... wth are metadata for a class?
		setMenuTypeFromTypeAndChildren(menuSchema, false, root.isSuperclass());
		menuSchema.setPrivilege(getPrivilegeFor(root).getGrantType());
		menuSchema.setDefaultToDisplay(isStartingClass(root));

		final List<MenuSchema> children = new ArrayList<MenuSchema>();
		if (Iterables.size(root.getChildren()) > 0) {
			for (final CMClass childClass : root.getChildren()) {
				children.add(serializeVisibleClassesFromRoot(childClass));
			}
		}
		menuSchema.setChildren(children.toArray(new MenuSchema[children.size()]));
		return menuSchema;
	}

	private void setMenuTypeFromTypeAndChildren(final MenuSchema schema, final boolean isProcess,
			final boolean isSuperclass) {
		String type;
		if (isSuperclass) {
			type = isProcess ? PROCESS_SUPERCLASS : SUPERCLASS;
		} else {
			type = isProcess ? PROCESS : CLASS;
		}
		schema.setMenuType(type);
	}

	private PrivilegeType getPrivilegeFor(final CMClass cmClass) {
		if (operationUser.hasWriteAccess(cmClass)) {
			return PrivilegeType.WRITE;
		} else if (operationUser.hasReadAccess(cmClass)) {
			return PrivilegeType.READ;
		} else {
			return PrivilegeType.NONE;
		}
	}

	private boolean isStartingClass(final CMClass cmClass) {
		final Long startingClassId = operationUser.getPreferredGroup().getStartingClassId();
		return (startingClassId == null) ? false : startingClassId.equals(cmClass.getId());
	}

	public MenuSchema serializeMenuTree() {
		final MenuItem rootMenuItem = menuStore.getMenuToUseForGroup(operationUser.getPreferredGroup().getName());
		return serializeMenuTree(rootMenuItem);
	}

	private MenuSchema serializeMenuTree(final MenuItem rootMenuItem) {
		final MenuSchema menuSchema = new MenuSchema();
		menuSchema.setDescription(rootMenuItem.getDescription());
		if (isReport(rootMenuItem) || isView(rootMenuItem) || isDashboard(rootMenuItem)) {
			menuSchema.setId(rootMenuItem.getReferencedElementId().intValue());
		} else if (rootMenuItem.getId() != null) {
			menuSchema.setId(rootMenuItem.getId().intValue());
		}
		menuSchema.setMenuType(rootMenuItem.getType().getValue().toLowerCase());

		if (isClass(rootMenuItem) || (isProcess(rootMenuItem) && isProcessUsable(rootMenuItem.getReferedClassName()))) {
			final CMClass menuEntryClass = dataAccessLogic.findClass(rootMenuItem.getReferedClassName());
			menuSchema.setId(menuEntryClass.getId().intValue());
			menuSchema.setDefaultToDisplay(isStartingClass(menuEntryClass));
			menuSchema.setClassname(menuEntryClass.getIdentifier().getLocalName());
			final PrivilegeType privilege = getPrivilegeFor(menuEntryClass);
			menuSchema.setPrivilege(privilege.toString());
		}

		final List<MenuSchema> children = new ArrayList<MenuSchema>();
		MenuSchema childMenuSchema = new MenuSchema();
		for (final MenuItem childMenuItem : rootMenuItem.getChildren()) {
			childMenuSchema = serializeMenuTree(childMenuItem);
			children.add(childMenuSchema);
		}

		menuSchema.setChildren(children.toArray(new MenuSchema[children.size()]));
		return menuSchema;
	}

	private boolean isProcessUsable(final String processClassName) {
		return workflowLogic.isProcessUsable(processClassName);
	}

	private boolean isClass(final MenuItem menuItem) {
		return menuItem.getType().getValue().equals(MenuItemType.CLASS.getValue());
	}

	private boolean isProcess(final MenuItem menuItem) {
		return menuItem.getType().getValue().equals(MenuItemType.PROCESS.getValue());
	}

	private boolean isDashboard(final MenuItem menuItem) {
		return menuItem.getType().getValue().equals(MenuItemType.DASHBOARD.getValue());
	}

	private boolean isView(final MenuItem menuItem) {
		return menuItem.getType().getValue().equals(MenuItemType.VIEW.getValue());
	}

	private boolean isFolder(final MenuItem menuItem) {
		return menuItem.getType().getValue().equals(MenuItemType.SYSTEM_FOLDER.getValue())
				|| menuItem.getType().getValue().equals(MenuItemType.FOLDER.getValue());
	}

	private boolean isReport(final MenuItem menuItem) {
		final MenuItemType menuItemType = menuItem.getType();
		return (menuItemType.getValue().equals(MenuItemType.REPORT_CSV.getValue())
				|| menuItemType.getValue().equals(MenuItemType.REPORT_PDF.getValue())
				|| menuItemType.getValue().equals(MenuItemType.REPORT_ODT.getValue()) || menuItemType.getValue()
				.equals(MenuItemType.REPORT_XML.getValue()));
	}

}
