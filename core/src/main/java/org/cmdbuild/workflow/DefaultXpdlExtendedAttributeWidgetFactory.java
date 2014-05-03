package org.cmdbuild.workflow;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.CalendarWidgetFactory;
import org.cmdbuild.workflow.widget.CreateModifyCardWidgetFactory;
import org.cmdbuild.workflow.widget.GridWidgetFactory;
import org.cmdbuild.workflow.widget.LinkCardsWidgetFactory;
import org.cmdbuild.workflow.widget.ManageEmailWidgetFactory;
import org.cmdbuild.workflow.widget.ManageRelationWidgetFactory;
import org.cmdbuild.workflow.widget.NavigationTreeWidgetFactory;
import org.cmdbuild.workflow.widget.OpenAttachmentWidgetFactory;
import org.cmdbuild.workflow.widget.OpenNoteWidgetFactory;
import org.cmdbuild.workflow.widget.OpenReportWidgetFactory;
import org.cmdbuild.workflow.widget.StartWorkflowWidgetFactory;
import org.cmdbuild.workflow.widget.PresetFromCardWidgetFactory;
import org.cmdbuild.workflow.widget.WebServiceWidgetFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;

public class DefaultXpdlExtendedAttributeWidgetFactory extends ValuePairXpdlExtendedAttributeWidgetFactory {

	public DefaultXpdlExtendedAttributeWidgetFactory(final TemplateRepository templateRepository,
			final Notifier notifier, final CMDataView dataView, final EmailLogic emailLogic,
			final EmailTemplateLogic emailTemplateLogic) {
		addWidgetFactory(new CalendarWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new CreateModifyCardWidgetFactory(templateRepository, notifier, dataView));
		addWidgetFactory(new LinkCardsWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new ManageRelationWidgetFactory(templateRepository, notifier, dataView));
		addWidgetFactory(new ManageEmailWidgetFactory(templateRepository, notifier, emailLogic, emailTemplateLogic));
		addWidgetFactory(new OpenAttachmentWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new OpenNoteWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new OpenReportWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new WebServiceWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new PresetFromCardWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new StartWorkflowWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new NavigationTreeWidgetFactory(templateRepository, notifier));
		addWidgetFactory(new GridWidgetFactory(templateRepository, notifier));
	}

}
