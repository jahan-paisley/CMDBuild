package org.cmdbuild.workflow;

import static org.cmdbuild.workflow.ProcessAttributes.ActivityDefinitionId;
import static org.cmdbuild.workflow.ProcessAttributes.ActivityInstanceId;
import static org.cmdbuild.workflow.ProcessAttributes.CurrentActivityPerformers;
import static org.cmdbuild.workflow.ProcessAttributes.ProcessInstanceId;
import static org.cmdbuild.workflow.ProcessAttributes.UniqueProcessDefinition;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.workflow.ActivityInstanceImpl.ActivityAdvanceChecker;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfoImpl;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class ProcessInstanceImpl implements UserProcessInstance {

	public static class ProcessInstanceBuilder implements Builder<ProcessInstanceImpl> {

		private OperationUser operationUser;
		private PrivilegeContext privilegeContext;
		private ProcessDefinitionManager processDefinitionManager;
		private CMCard card;
		private LookupHelper lookupHelper;

		@Override
		public ProcessInstanceImpl build() {
			validate();
			return new ProcessInstanceImpl(this);
		}

		private void validate() {
			Validate.notNull(operationUser, "invalid operation user");
			Validate.notNull(privilegeContext, "invalid privilege context");
			Validate.notNull(processDefinitionManager, "invalid process definition manager");
			Validate.notNull(lookupHelper, "invalid lookup helper");
			Validate.notNull(card, "invalid card");
		}

		public ProcessInstanceBuilder withOperationUser(final OperationUser value) {
			operationUser = value;
			return this;
		}

		public ProcessInstanceBuilder withPrivilegeContext(final PrivilegeContext value) {
			privilegeContext = value;
			return this;
		}

		public ProcessInstanceBuilder withProcessDefinitionManager(final ProcessDefinitionManager value) {
			processDefinitionManager = value;
			return this;
		}

		public ProcessInstanceBuilder withLookupHelper(final LookupHelper value) {
			lookupHelper = value;
			return this;
		}

		public ProcessInstanceBuilder withCard(final CMCard value) {
			card = value;
			return this;
		}

	}

	public static ProcessInstanceBuilder newInstance() {
		return new ProcessInstanceBuilder();
	}

	private final OperationUser operationUser;
	private final PrivilegeContext privilegeContext;
	private final ProcessDefinitionManager processDefinitionManager;
	private final CMCard card;
	private final LookupHelper lookupHelper;

	private ProcessInstanceImpl(final ProcessInstanceBuilder builder) {
		this.operationUser = builder.operationUser;
		this.privilegeContext = builder.privilegeContext;
		this.processDefinitionManager = builder.processDefinitionManager;
		this.card = builder.card;
		this.lookupHelper = builder.lookupHelper;
	}

	@Override
	public CMProcessClass getType() {
		return new ProcessClassImpl(operationUser, privilegeContext, card.getType(), processDefinitionManager);
	}

	@Override
	public WSProcessInstanceState getState() {
		final Long id = card.get(ProcessAttributes.FlowStatus.dbColumnName(), IdAndDescription.class).getId();
		return lookupHelper.stateForLookupId(id);
	}

	@Override
	public WSProcessDefInfo getUniqueProcessDefinition() {
		final String value = card.get(UniqueProcessDefinition.dbColumnName(), String.class);
		if (value != null) {
			final String[] components = value.split("#");
			if (components.length == 3) {
				return WSProcessDefInfoImpl.newInstance(components[0], components[1], components[2]);
			}
		}
		return null;
	}

	@Override
	public Long getId() {
		return card.getId();
	}

	@Override
	public Object getCode() {
		return card.getCode();
	}

	@Override
	public Object getDescription() {
		return card.getDescription();
	}

	@Override
	public Object get(final String key) {
		return card.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return card.get(key, requiredType);
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return card.getValues();
	}

	@Override
	public Iterable<Entry<String, Object>> getAllValues() {
		return card.getAllValues();
	}

	@Override
	public String getUser() {
		return card.getUser();
	}

	@Override
	public DateTime getBeginDate() {
		return card.getBeginDate();
	}

	@Override
	public DateTime getEndDate() {
		return card.getEndDate();
	}

	@Override
	public Long getCardId() {
		return card.getId();
	}

	@Override
	public String getProcessInstanceId() {
		return card.get(ProcessInstanceId.dbColumnName(), String.class);
	}

	@Override
	public List<UserActivityInstance> getActivities() {
		final List<UserActivityInstance> out = Lists.newArrayList();
		final String[] activityInstanceIds = card.get(ActivityInstanceId.dbColumnName(), String[].class);
		final String[] activityDefinitionIds = card.get(ActivityDefinitionId.dbColumnName(), String[].class);
		final String[] perfs = card.get(CurrentActivityPerformers.dbColumnName(), String[].class);
		for (int i = 0; i < activityInstanceIds.length; ++i) {
			try {
				final CMActivity activity = processDefinitionManager.getActivity(this, activityDefinitionIds[i]);
				final ActivityAdvanceChecker activityAdvanceChecker = new DefaultActivityAdvanceChecker(operationUser,
						perfs[i]);
				out.add(new ActivityInstanceImpl(this, activity, activityInstanceIds[i], perfs[i],
						activityAdvanceChecker));
			} catch (final CMWorkflowException e) {
				// TODO do in another way
				throw new Error(e);
			}
		}
		return out;
	}

	@Override
	public UserActivityInstance getActivityInstance(final String activityInstanceId) {
		for (final UserActivityInstance activityInstance : getActivities()) {
			if (activityInstance.getId().equals(activityInstanceId)) {
				return activityInstance;
			}
		}
		return null;
	}

}
