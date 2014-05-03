package org.cmdbuild.logic.workflow;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.common.template.TemplateResolvers.identity;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.logic.Action;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class StartProcess implements Action {

	private static final Marker marker = MarkerFactory.getMarker(StartProcess.class.getName());

	public static interface Hook {

		void started(UserProcessInstance userProcessInstance);

	}

	private static Hook NULL_HOOK = new Hook() {

		@Override
		public void started(final UserProcessInstance userProcessInstance) {
			// nothing to do
		}
	};

	public static class Builder implements org.apache.commons.lang3.builder.Builder<StartProcess> {

		private static final Map<String, Object> EMPTY_ATTRIBUTES = Collections.emptyMap();

		private WorkflowLogic workflowLogic;
		private Hook hook;
		private TemplateResolver templateResolver;
		private String className;
		private final Map<String, Object> attributes;
		private Boolean advance;

		private Builder() {
			// use factory method
			attributes = Maps.newHashMap();
		}

		@Override
		public StartProcess build() {
			validate();
			return new StartProcess(this);
		}

		private void validate() {
			Validate.notNull(workflowLogic, "missing workflow logic");
			Validate.notBlank(className, "missing class name");
			hook = defaultIfNull(hook, NULL_HOOK);
			templateResolver = defaultIfNull(templateResolver, identity());
			advance = defaultIfNull(advance, Boolean.TRUE);
		}

		public Builder withWorkflowLogic(final WorkflowLogic workflowLogic) {
			this.workflowLogic = workflowLogic;
			return this;
		}

		public Builder withHook(final Hook hook) {
			this.hook = hook;
			return this;
		}

		public Builder withTemplateResolver(final TemplateResolver templateResolver) {
			this.templateResolver = templateResolver;
			return this;
		}

		public Builder withClassName(final String classname) {
			this.className = classname;
			return this;
		}

		public Builder withAttribute(final String name, final Object value) {
			if (isNotBlank(name)) {
				attributes.put(name, value);
			}
			return this;
		}

		public Builder withAttributes(final Map<String, ?> attributes) {
			for (final Entry<String, ?> entry : defaultIfNull(attributes, EMPTY_ATTRIBUTES).entrySet()) {
				withAttribute(entry.getKey(), entry.getValue());
			}
			return this;
		}

		public Builder withAdvanceStatus(final boolean advance) {
			this.advance = advance;
			return this;
		}

	}

	private static final Map<String, Object> NO_WIDGETS = Collections.emptyMap();

	public static Builder newInstance() {
		return new Builder();
	}

	private final WorkflowLogic workflowLogic;
	private final Hook hook;
	private final TemplateResolver templateResolver;
	private final String className;
	private final Map<String, Object> attributes;
	private final boolean advance;

	private StartProcess(final Builder builder) {
		this.workflowLogic = builder.workflowLogic;
		this.hook = builder.hook;
		this.templateResolver = builder.templateResolver;
		this.className = builder.className;
		this.attributes = builder.attributes;
		this.advance = builder.advance;
	}

	@Override
	public void execute() {
		try {
			final UserProcessInstance userProcessInstance = workflowLogic.startProcess( //
					className, //
					newHashMap(transformValues(attributes, APPLY_TEMPLATE_RESOLVER)), //
					NO_WIDGETS, //
					advance);
			hook.started(userProcessInstance);
		} catch (final CMWorkflowException e) {
			logger.error(marker, "error starting process", e);
			throw new RuntimeException(e);
		}
	}

	private final Function<Object, Object> APPLY_TEMPLATE_RESOLVER = new Function<Object, Object>() {

		@Override
		public Object apply(final Object input) {
			final Object resolved;
			if (input instanceof String) {
				final String template = String.class.cast(input);
				resolved = templateResolver.resolve(template);
			} else {
				resolved = input;
			}
			return resolved;
		}

	};

}
