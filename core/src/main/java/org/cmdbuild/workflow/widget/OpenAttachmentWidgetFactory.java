package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.OpenAttachment;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class OpenAttachmentWidgetFactory extends ValuePairWidgetFactory {

	public OpenAttachmentWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return "openAttachment";
	}

	@Override
	public Widget createWidget(final Map<String, Object> valuePairs) {
		return new OpenAttachment();
	}

}
