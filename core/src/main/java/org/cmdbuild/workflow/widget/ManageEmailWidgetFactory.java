package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.EmailTemplate;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ManageEmailWidgetFactory extends ValuePairWidgetFactory {

	// TODO change logger
	private static final Logger logger = Log.WORKFLOW;
	private static final Marker marker = MarkerFactory.getMarker(ManageEmailWidgetFactory.class.getName());

	private static final String IMPLICIT_TEMPLATE_NAME = "implicitTemplateName";
	private final static String FROM_ADDRESS = "FromAddress";
	private final static String TO_ADDRESSES = "ToAddresses";
	private final static String CC_ADDRESSES = "CCAddresses";
	private final static String SUBJECT = "Subject";
	private final static String CONTENT = "Content";
	private final static String CONDITION = "Condition";
	private final static String READ_ONLY = "ReadOnly";
	private final static String NOTIFY_TEMPLATE_NAME = "NotifyWith";
	private final static String TEMPLATE = "Template";

	private final static String WIDGET_NAME = "manageEmail";

	private final EmailLogic emailLogic;
	private final EmailTemplateLogic emailTemplateLogic;

	public ManageEmailWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier,
			final EmailLogic emailLogic, final EmailTemplateLogic emailTemplateLogic) {
		super(templateRespository, notifier);
		this.emailLogic = emailLogic;
		this.emailTemplateLogic = emailTemplateLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	/*
	 * naive but fast to write solution ...first do it works...
	 */
	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final ManageEmail widget = new ManageEmail(emailLogic);
		// I want to preserve the order
		final Map<String, EmailTemplate> emailTemplate = new LinkedHashMap<String, EmailTemplate>();
		final Set<String> managedParameters = new HashSet<String>();
		managedParameters.add(READ_ONLY);
		managedParameters.add(BUTTON_LABEL);

		final Map<String, String> templates = getAttributesStartingWith(valueMap, TEMPLATE);
		for (final String key : templates.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, TEMPLATE);
			final String templateName = readString(valueMap.get(key));
			if (isNotBlank(templateName)) {
				try {
					final EmailTemplateLogic.Template _template = emailTemplateLogic.read(templateName);
					template.setFromAddress(_template.getFrom());
					template.setToAddresses(_template.getTo());
					template.setCcAddresses(_template.getCc());
					template.setSubject(_template.getSubject());
					template.setContent(_template.getBody());
				} catch (final Exception e) {
					logger.warn(marker, "error getting template, skipping", e);
				}
			}
		}
		managedParameters.addAll(templates.keySet());

		final Map<String, String> fromAddresses = getAttributesStartingWith(valueMap, FROM_ADDRESS);
		for (final String key : fromAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, FROM_ADDRESS);
			template.setFromAddress(readString(valueMap.get(key)));
		}
		managedParameters.addAll(fromAddresses.keySet());

		final Map<String, String> toAddresses = getAttributesStartingWith(valueMap, TO_ADDRESSES);
		for (final String key : toAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, TO_ADDRESSES);
			template.setToAddresses(readString(valueMap.get(key)));
		}
		managedParameters.addAll(toAddresses.keySet());

		final Map<String, String> ccAddresses = getAttributesStartingWith(valueMap, CC_ADDRESSES);
		for (final String key : ccAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, CC_ADDRESSES);
			template.setCcAddresses(readString(valueMap.get(key)));
		}
		managedParameters.addAll(ccAddresses.keySet());

		final Map<String, String> subjects = getAttributesStartingWith(valueMap, SUBJECT);
		for (final String key : subjects.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, SUBJECT);
			template.setSubject(readString(valueMap.get(key)));
		}
		managedParameters.addAll(subjects.keySet());

		final Map<String, String> notifyWithThemplate = getAttributesStartingWith(valueMap, NOTIFY_TEMPLATE_NAME);
		for (final String key : notifyWithThemplate.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, NOTIFY_TEMPLATE_NAME);
			template.setNotifyWith(readString(valueMap.get(key)));
		}
		managedParameters.addAll(subjects.keySet());

		final Map<String, String> contents = getAttributesStartingWith(valueMap, CONTENT);
		for (final String key : contents.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, CONTENT);
			template.setContent(readString(valueMap.get(key)));
		}
		managedParameters.addAll(contents.keySet());

		final Map<String, String> conditions = getAttributesStartingWith(valueMap, CONDITION);
		for (final String key : conditions.keySet()) {
			final EmailTemplate template = getTemplateForKey(key, emailTemplate, CONDITION);
			template.setCondition(readString(valueMap.get(key)));
		}
		managedParameters.addAll(conditions.keySet());

		widget.setEmailTemplates(emailTemplate.values());
		widget.setTemplates(extractUnmanagedStringParameters(valueMap, managedParameters));
		widget.setReadOnly(readBooleanTrueIfPresent(valueMap.get(READ_ONLY)));

		return widget;
	}

	private Map<String, String> getAttributesStartingWith(final Map<String, Object> valueMap, final String prefix) {
		final Map<String, String> out = new HashMap<String, String>();

		for (final String key : valueMap.keySet()) {
			if (key.startsWith(prefix)) {
				out.put(key, readString(valueMap.get(key)));
			}
		}

		return out;
	}

	private EmailTemplate getTemplateForKey(final String key, final Map<String, EmailTemplate> templates,
			final String attributeName) {
		String postFix = key.replaceFirst(attributeName, EMPTY);
		if (isEmpty(postFix)) {
			postFix = IMPLICIT_TEMPLATE_NAME;
		}
		if (templates.containsKey(postFix)) {
			return templates.get(postFix);
		} else {
			final EmailTemplate t = new EmailTemplate();
			templates.put(postFix, t);
			return t;
		}
	}
}
