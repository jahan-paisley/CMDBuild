package org.cmdbuild.workflow;

import static java.util.Arrays.asList;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.workflow.ActivityInstanceImpl.ActivityAdvanceChecker;

public class DefaultActivityAdvanceChecker implements ActivityAdvanceChecker {

	private final OperationUser operationUser;
	private final String activityInstancePerformer;

	public DefaultActivityAdvanceChecker(final OperationUser operationUser, final String activityInstancePerformer) {
		this.operationUser = operationUser;
		this.activityInstancePerformer = activityInstancePerformer;
	}

	@Override
	public boolean isAdvanceable() {
		final boolean advanceable;
		if (userHasAdministratorPrivileges()) {
			advanceable = true;
		} else if (userHasDefaultGroup()) {
			advanceable = activityInstancePerformerIsInUserGroups();
		} else {
			advanceable = activityInstancePerformerIsInSelectedGroup();
		}
		return advanceable;
	}

	private boolean userHasAdministratorPrivileges() {
		return operationUser.hasAdministratorPrivileges();
	}

	private boolean userHasDefaultGroup() {
		return (operationUser.getAuthenticatedUser().getDefaultGroupName() != null);
	}

	private boolean activityInstancePerformerIsInUserGroups() {
		return activityInstancePerformerIsIn(operationUser.getAuthenticatedUser().getGroupNames());
	}

	private boolean activityInstancePerformerIsInSelectedGroup() {
		return activityInstancePerformerIsIn(asList(operationUser.getPreferredGroup().getName()));
	}

	private boolean activityInstancePerformerIsIn(final Iterable<String> groupNames) {
		for (final String name : groupNames) {
			if (name.equals(activityInstancePerformer)) {
				return true;
			}
		}
		return false;
	}

}
