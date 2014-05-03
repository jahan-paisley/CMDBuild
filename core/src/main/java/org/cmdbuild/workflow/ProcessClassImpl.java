package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.workflow.user.UserProcessClass;

class ProcessClassImpl implements UserProcessClass {

	private final OperationUser operationUser;
	private final PrivilegeContext privilegeContext;
	private final CMClass clazz;
	private final ProcessDefinitionManager processDefinitionManager;

	public ProcessClassImpl( //
			final OperationUser operationUser, //
			final PrivilegeContext privilegeContext, //
			final CMClass clazz, //
			final ProcessDefinitionManager processDefinitionManager //
	) {
		this.operationUser = operationUser;
		this.privilegeContext = privilegeContext;
		this.clazz = clazz;
		this.processDefinitionManager = processDefinitionManager;
	}

	@Override
	public String getPrivilegeId() {
		return clazz.getPrivilegeId();
	}

	@Override
	public boolean isActive() {
		return clazz.isActive();
	}

	@Override
	public boolean isSystem() {
		return clazz.isSystem();
	}

	@Override
	public boolean isSystemButUsable() {
		return clazz.isSystemButUsable();
	}

	@Override
	public boolean isBaseClass() {
		return clazz.isBaseClass();
	}

	@Override
	public boolean holdsHistory() {
		return clazz.holdsHistory();
	}

	@Override
	public boolean isSimple() {
		return clazz.isSimple();
	}

	@Override
	public String getName() {
		return clazz.getName();
	}

	@Override
	public String getKeyAttributeName() {
		return clazz.getKeyAttributeName();
	}

	@Override
	public CMIdentifier getIdentifier() {
		return clazz.getIdentifier();
	}

	@Override
	public Long getId() {
		return clazz.getId();
	}

	@Override
	public String getDescription() {
		return clazz.getDescription();
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
		return clazz.getActiveAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		return clazz.getAllAttributes();
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		return clazz.getAttribute(name);
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return clazz.getAllAttributes();
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isSuperclass() {
		return clazz.isSuperclass();
	}

	@Override
	public boolean isAncestorOf(final CMClass cmClass) {
		return clazz.isAncestorOf(cmClass);
	}

	@Override
	public CMClass getParent() {
		return clazz.getParent();
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return clazz.getLeaves();
	}

	@Override
	public Iterable<? extends CMClass> getDescendants() {
		return clazz.getDescendants();
	}

	@Override
	public String getDescriptionAttributeName() {
		return clazz.getDescriptionAttributeName();
	}

	@Override
	public String getCodeAttributeName() {
		return clazz.getCodeAttributeName();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		return clazz.getChildren();
	}

	@Override
	public DataSource getDefinitionTemplate() throws CMWorkflowException {
		return processDefinitionManager.getTemplate(this);
	}

	@Override
	public String[] getDefinitionVersions() throws CMWorkflowException {
		return processDefinitionManager.getVersions(this);
	}

	@Override
	public DataSource getDefinition(final String version) throws CMWorkflowException {
		return processDefinitionManager.getDefinition(this, version);
	}

	@Override
	public void updateDefinition(final DataSource pkgDefData) throws CMWorkflowException {
		processDefinitionManager.updateDefinition(this, pkgDefData);
	}

	@Override
	public String getPackageId() throws CMWorkflowException {
		return processDefinitionManager.getPackageId(this);
	}

	@Override
	public String getProcessDefinitionId() throws CMWorkflowException {
		return processDefinitionManager.getProcessDefinitionId(this);
	}

	@Override
	public boolean isUserStoppable() {
		return clazz.isUserStoppable();
	}

	@Override
	public boolean isUsable() {
		try {
			return isActive() && getPackageId() != null;
		} catch (final CMWorkflowException e) {
			return false;
		}
	}

	@Override
	/**
	 * For the user logged with a Default Group (AKA: sum of all groups privileges):
	 * - retrieve the activities for all the groups.
	 * - if there is only one, return this activity
	 * - otherwise, if one of them is the activity for the default group, return this activity
	 * 
	 * For the user logged with a single group, simply check if exists an
	 * activity for that group
	 * 
	 * At last If no start activity are founded, try to retrieve the start activity
	 * as administrator (if the user is an administrator).
	 */
	public CMActivity getStartActivity() throws CMWorkflowException {
		CMActivity startActivity = null;

		if (userLoggedWithDefaultGroup()) {
			// Logged with multiple-group
			final String defaultGroupName = operationUser.getAuthenticatedUser().getDefaultGroupName();
			final Map<String, CMActivity> startActivities = new HashMap<String, CMActivity>();

			// retrieve the activities for user groups
			for (final String groupName : operationUser.getAuthenticatedUser().getGroupNames()) {
				final CMActivity activity = processDefinitionManager.getManualStartActivity(this, groupName);
				if (activity != null) {
					startActivities.put(groupName, activity);
				}
			}

			if (startActivities.keySet().size() > 0) {
				if (startActivities.keySet().size() == 1) {
					startActivity = startActivities.values().iterator().next();
				} else if (startActivities.keySet().contains(defaultGroupName)) {
					startActivity = startActivities.get(defaultGroupName);
				}
			}
		} else {
			// Logged with single group
			final String groupName = operationUser.getPreferredGroup().getName();
			startActivity = processDefinitionManager.getManualStartActivity(this, groupName);
		}

		if (startActivity == null && privilegeContext.hasAdministratorPrivileges()) {

			startActivity = processDefinitionManager.getManualStartActivity(this, null);
		}

		return startActivity;
	}

	private boolean userLoggedWithDefaultGroup() {
		final String defaultGroupName = operationUser.getAuthenticatedUser().getDefaultGroupName();
		return defaultGroupName != null && !"".equals(defaultGroupName);
	}

	@Override
	public boolean isStoppable() {
		return privilegeContext.hasAdministratorPrivileges() || isUserStoppable();
	}

	@Override
	public boolean isStartable() throws CMWorkflowException {
		return (getStartActivity() != null);
	}

	@Override
	public int hashCode() {
		return clazz.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return clazz.equals(obj);
	}

}
