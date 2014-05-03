package org.cmdbuild.servlets.json.serializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailLogic.EmailWithAttachmentNames;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;

public class JsonWorkflowDTOs {

	private static final Logger logger = Log.JSONRPC;

	private JsonWorkflowDTOs() {
	}

	public static class JsonActivityDefinition {

		private final CMActivity activity;
		private final String performer;

		public JsonActivityDefinition(final CMActivity activity) {
			this(activity, activity.getFirstNonAdminPerformer().getValue());
		}

		public JsonActivityDefinition(final CMActivity activity, final String performer) {
			this.activity = activity;
			this.performer = performer;
		}

		public String getPerformerName() {
			return performer;
		}

		public String getDescription() {
			return activity.getDescription();
		}

		public String getInstructions() {
			return activity.getInstructions();
		}

		public Iterable<CMActivityVariableToProcess> getVariables() {
			return activity.getVariables();
		}

		public Iterable<CMActivityWidget> getWidgets() throws CMWorkflowException {
			return activity.getWidgets();
		}

	}

	/*
	 * The base info to show the activities in the grid
	 */
	public static class JsonActivityInstanceInfo {
		private final UserActivityInstance activityInstance;

		public JsonActivityInstanceInfo(final UserActivityInstance activityInstance) {
			this.activityInstance = activityInstance;
		}

		public String getId() {
			return activityInstance.getId();
		}

		public String getPerformerName() {
			return activityInstance.getPerformerName();
		}

		public String getDescription() throws CMWorkflowException {
			return activityInstance.getDefinition().getDescription();
		}

		public Boolean isWritable() {
			return activityInstance.isWritable();
		}

	}

	/*
	 * Merge the base info with the info in the activity definition
	 */
	public static class JsonActivityInstance extends JsonActivityDefinition {

		private final UserActivityInstance activityInstance;
		private final JsonActivityInstanceInfo info;

		public JsonActivityInstance(final UserActivityInstance activityInstance) throws CMWorkflowException {
			super(activityInstance.getDefinition());
			this.activityInstance = activityInstance;
			info = new JsonActivityInstanceInfo(activityInstance);
		}

		public String getId() {
			return info.getId();
		}

		@Override
		public String getPerformerName() {
			return info.getPerformerName();
		}

		public Boolean isWritable() {
			return info.isWritable();
		}

		@Override
		public Iterable<CMActivityWidget> getWidgets() {
			try {
				return activityInstance.getWidgets();
			} catch (final CMWorkflowException e) {
				// TODO Log & warn!
				return Collections.emptyList();
			}
		}
	}

	public static class JsonProcessCard extends AbstractJsonResponseSerializer {

		private static final Marker marker = MarkerFactory.getMarker(JsonProcessCard.class.getName());

		private final UserProcessInstance processInstance;

		public JsonProcessCard(final UserProcessInstance processInstance) {
			this.processInstance = processInstance;
		}

		public Long getId() {
			return processInstance.getCardId();
		}

		public String getBeginDate() {
			return formatDateTime(processInstance.getBeginDate());
		}

		public long getBeginDateAsLong() {
			return processInstance.getBeginDate().getMillis();
		}

		public String getEndDate() {
			return formatDateTime(processInstance.getEndDate());
		}

		public Map<String, Object> getValues() {
			final Map<String, Object> output = new HashMap<String, Object>();
			for (final CMAttribute attr : processInstance.getType().getActiveAttributes()) {
				final String name = attr.getName();
				logger.debug(marker, "serializing attribute '{}'", name);
				final Object javaValue = processInstance.get(name);
				if (javaValue == null) {
					output.put(name, "");
				} else {
					final Object jsonValue = javaToJsonValue(attr.getType(), javaValue);
					output.put(name, jsonValue);
				}
			}

			return output;
		}

		public List<JsonActivityInstanceInfo> getActivityInstanceInfoList() throws CMWorkflowException {
			final List<JsonActivityInstanceInfo> out = new ArrayList<JsonActivityInstanceInfo>();

			for (final UserActivityInstance ai : processInstance.getActivities()) {
				out.add(new JsonActivityInstanceInfo(ai));
			}

			return out;
		}

		public String getFlowStatus() {
			return processInstance.getState().name();
		}

		public Object getClassId() {
			return processInstance.getType().getId();
		}

		public String getClassName() {
			return processInstance.getType().getName();
		}

		public String getClassDescription() {
			return processInstance.getType().getDescription();
		}

		@Override
		protected Object javaToJsonValue(final CMAttributeType<?> type, final Object value) {
			return new JsonAttributeValueVisitor(type, value).convertValue();
		}
	}

	public static class JsonEmail extends AbstractJsonResponseSerializer {

		private final Email email;
		private final List<String> attachmentNames;

		public JsonEmail(final EmailWithAttachmentNames email) {
			this.email = email.getEmail();
			this.attachmentNames = Lists.newArrayList(email.getAttachmentNames());
		}

		public Long getId() {
			return email.getId();
		}

		public String getFromAddress() {
			return email.getFromAddress();
		}

		public String getToAddresses() {
			return email.getToAddresses();
		}

		public String getCcAddresses() {
			return email.getCcAddresses();
		}

		public String getSubject() {
			return email.getSubject();
		}

		public String getContent() {
			return email.getContent();
		}

		public String getDate() {
			return formatDateTime(email.getDate());
		}

		public String getStatus() {
			return email.getStatus().getLookupName();
		}

		public String getNotifyWith() {
			return email.getNotifyWith();
		}

		public List<String> getAttachments() {
			return attachmentNames;
		}

	}
}
