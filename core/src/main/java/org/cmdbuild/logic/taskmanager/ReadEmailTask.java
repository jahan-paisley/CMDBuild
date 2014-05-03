package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Iterables.addAll;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ReadEmailTask implements ScheduledTask {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ReadEmailTask> {

		private static final Iterable<String> EMPTY_FILTER = Collections.emptyList();
		private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private String emailAccount;
		private final Collection<String> regexFromFilter = Lists.newArrayList();
		private final Collection<String> regexSubjectFilter = Lists.newArrayList();
		private Boolean notificationActive;
		private Boolean attachmentsActive;
		private String attachmentsCategory;
		private Boolean workflowActive;
		private String workflowClassName;
		private final Map<String, String> workflowAttributes = Maps.newHashMap();
		private Boolean workflowAdvanceable;
		private Boolean workflowAttachments;
		private String workflowAttachmentsCategory;
		private MapperEngine mapper;

		private Builder() {
			// use factory method
		}

		@Override
		public ReadEmailTask build() {
			validate();
			return new ReadEmailTask(this);
		}

		private void validate() {
			active = (active == null) ? false : active;

			notificationActive = (notificationActive == null) ? false : notificationActive;

			attachmentsActive = (attachmentsActive == null) ? false : attachmentsActive;
			if (attachmentsActive) {
				Validate.notNull(attachmentsCategory, "missing attachments category");
			}
			workflowActive = (workflowActive == null) ? false : workflowActive;
			workflowAdvanceable = (workflowAdvanceable == null) ? false : workflowAdvanceable;
			workflowAttachments = (workflowAttachments == null) ? false : workflowAttachments;
			if (workflowActive) {
				Validate.notNull(workflowClassName, "missing workflow's class name");
				if (workflowAttachments) {
					Validate.notNull(workflowAttachmentsCategory, "missing workflow's attachments category");
				}
			}

			mapper = defaultIfNull(mapper, NullMapperEngine.getInstance());
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withEmailAccount(final String emailAccount) {
			this.emailAccount = emailAccount;
			return this;
		}

		public Builder withRegexFromFilter(final Iterable<String> regexFromFilter) {
			addAll(this.regexFromFilter, defaultIfNull(regexFromFilter, EMPTY_FILTER));
			return this;
		}

		public Builder withRegexSubjectFilter(final Iterable<String> regexSubjectFilter) {
			addAll(this.regexSubjectFilter, defaultIfNull(regexSubjectFilter, EMPTY_FILTER));
			return this;
		}

		public Builder withNotificationStatus(final Boolean notificationActive) {
			this.notificationActive = notificationActive;
			return this;
		}

		public Builder withAttachmentsActive(final Boolean attachmentsActive) {
			this.attachmentsActive = attachmentsActive;
			return this;
		}

		public Builder withAttachmentsCategory(final String category) {
			this.attachmentsCategory = category;
			return this;
		}

		public Builder withWorkflowActive(final Boolean workflowActive) {
			this.workflowActive = workflowActive;
			return this;
		}

		public Builder withWorkflowClassName(final String workflowClassName) {
			this.workflowClassName = workflowClassName;
			return this;
		}

		public Builder withWorkflowAttributes(final Map<String, String> workflowAttributes) {
			this.workflowAttributes.putAll(defaultIfNull(workflowAttributes, EMPTY_ATTRIBUTES));
			return this;
		}

		public Builder withWorkflowAdvanceableStatus(final Boolean workflowAdvanceable) {
			this.workflowAdvanceable = workflowAdvanceable;
			return this;
		}

		public Builder withWorkflowAttachmentsStatus(final Boolean workflowAttachments) {
			this.workflowAttachments = workflowAttachments;
			return this;
		}

		public Builder withWorkflowAttachmentsCategory(final String workflowAttachmentsCategory) {
			this.workflowAttachmentsCategory = workflowAttachmentsCategory;
			return this;
		}

		public Builder withMapperEngine(final MapperEngine mapper) {
			this.mapper = mapper;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final String emailAccount;
	private final Iterable<String> regexFromFilter;
	private final Iterable<String> regexSubjectFilter;
	private final boolean notificationActive;
	private final boolean attachmentsActive;
	private final String attachmentsCategory;
	private final boolean workflowActive;
	private final String workflowClassName;
	private final Map<String, String> workflowAttributes;
	private final boolean workflowAdvanceable;
	private final boolean workflowAttachments;
	private final String workflowAttachmentsCategory;
	private final MapperEngine mapper;

	private ReadEmailTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.emailAccount = builder.emailAccount;
		this.regexFromFilter = builder.regexFromFilter;
		this.regexSubjectFilter = builder.regexSubjectFilter;
		this.notificationActive = builder.notificationActive;
		this.attachmentsActive = builder.attachmentsActive;
		this.attachmentsCategory = builder.attachmentsCategory;
		this.workflowActive = builder.workflowActive;
		this.workflowClassName = builder.workflowClassName;
		this.workflowAttributes = builder.workflowAttributes;
		this.workflowAdvanceable = builder.workflowAdvanceable;
		this.workflowAttachments = builder.workflowAttachments;
		this.workflowAttachmentsCategory = builder.workflowAttachmentsCategory;
		this.mapper = builder.mapper;
	}

	@Override
	public void accept(final TaskVistor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public boolean isNotificationActive() {
		return notificationActive;
	}

	public Iterable<String> getRegexFromFilter() {
		return regexFromFilter;
	}

	public Iterable<String> getRegexSubjectFilter() {
		return regexSubjectFilter;
	}

	public boolean isAttachmentsActive() {
		return attachmentsActive;
	}

	public String getAttachmentsCategory() {
		return attachmentsCategory;
	}

	public boolean isWorkflowActive() {
		return workflowActive;
	}

	public String getWorkflowClassName() {
		return workflowClassName;
	}

	public Map<String, String> getWorkflowAttributes() {
		return workflowAttributes;
	}

	public boolean isWorkflowAdvanceable() {
		return workflowAdvanceable;
	}

	public boolean isWorkflowAttachments() {
		return workflowAttachments;
	}

	public String getWorkflowAttachmentsCategory() {
		return workflowAttachmentsCategory;
	}

	public MapperEngine getMapperEngine() {
		return mapper;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
