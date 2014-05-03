package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassSerializer extends Serializer {

	private static final String WRITE_PRIVILEGE = "priv_write", CREATE_PRIVILEGE = "priv_create";

	private final CMDataView dataView;
	private final WorkflowLogic workflowLogic;
	private final PrivilegeContext privilegeContext;

	public ClassSerializer( //
			final CMDataView dataView, //
			final SystemWorkflowLogicBuilder workflowLogicBuilder, //
			final PrivilegeContext privilegeContext //
	) {
		this.dataView = dataView;
		this.workflowLogic = workflowLogicBuilder.build();
		this.privilegeContext = privilegeContext;
	}

	private JSONObject toClient(final UserProcessClass element, final String wrapperLabel,
			final boolean addManagementInfo) throws JSONException, CMWorkflowException {
		final JSONObject jsonObject = toClient(CMClass.class.cast(element), wrapperLabel);
		jsonObject.put("type", "processclass");

		try {
			jsonObject.put("startable", element.isStartable());
		} catch (final CMWorkflowException ex) {
			Log.CMDBUILD.warn("Cannot fetch if the process '{}' is startable", element.getName());
		} catch (final CMDBWorkflowException ex) {
			if (WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.equals(ex.getExceptionType())) {
				RequestListener.getCurrentRequest().pushWarning(ex);
			}
		}

		if (addManagementInfo) {
			jsonObject.put("userstoppable", element.isStoppable());
		} else {
			jsonObject.put("userstoppable", element.isUserStoppable());
		}

		return jsonObject;
	}

	public JSONObject toClient(final CMClass cmClass, final String wrapperLabel) throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		final CMClass activityClass = dataView.findClass(Constants.BASE_PROCESS_CLASS_NAME);
		if (activityClass.isAncestorOf(cmClass)) {
			final UserProcessClass userProcessClass = workflowLogic.findProcessClass(cmClass.getName());
			if (userProcessClass != null) {
				jsonObject.put("type", "processclass");
				jsonObject.put("userstoppable", userProcessClass.isUserStoppable());
				try {
					jsonObject.put("startable", userProcessClass.isStartable());
				} catch (final CMWorkflowException e) {
				}
			}
		} else {
			jsonObject.put("type", "class");
		}
		jsonObject.put("id", cmClass.getId());
		jsonObject.put("name", cmClass.getName());
		jsonObject.put("text", cmClass.getDescription());
		jsonObject.put("superclass", cmClass.isSuperclass());
		jsonObject.put("active", cmClass.isActive());
		jsonObject.put("tableType", cmClass.holdsHistory() ? "standard" : "simpletable");
		jsonObject.put("selectable", !cmClass.getName().equals(Constants.BASE_CLASS_NAME));
		jsonObject.put("system", cmClass.isSystemButUsable());

		// TODO complete
		// addGeoFeatureTypes(jsonTable, table);
		addMetadata(jsonObject, cmClass);
		addAccessPrivileges(cmClass, jsonObject);

		final CMClass parent = cmClass.getParent();
		if (parent != null) {
			jsonObject.put("parent", parent.getId());
		}

		// Wrap the serialization if required
		if (wrapperLabel != null) {
			final JSONObject out = new JSONObject();
			out.put(wrapperLabel, jsonObject);
			return out;
		} else {
			return jsonObject;
		}
	}

	public JSONObject toClient(final UserProcessClass element, final boolean addManagementInfo) throws JSONException,
			CMWorkflowException {
		return toClient(element, null, addManagementInfo);
	}

	public JSONObject toClient(final CMClass element) throws JSONException {
		return toClient(element, null);
	}

	private void addAccessPrivileges(final CMEntryType entryType, final JSONObject json) throws JSONException {
		final boolean writePrivilege = privilegeContext.hasWriteAccess(entryType);
		json.put(WRITE_PRIVILEGE, writePrivilege);
		boolean createPrivilege = writePrivilege;
		if (entryType instanceof CMClass) {
			createPrivilege &= !((CMClass) entryType).isSuperclass();
		}

		json.put(CREATE_PRIVILEGE, createPrivilege);
	}

}
