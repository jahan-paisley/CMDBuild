package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.WebService;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class WebServiceWidgetFactory extends ValuePairWidgetFactory {

	/*
	 * **************************************************
	 * Follows the list of key/value pairs that define the configuration of a
	 * Web Service Widget **************************************************
	 * 
	 * ButtonLabel="" EndPoint="" Method="doSomthing" NameSpacePrefix=""
	 * NameSpaceURI="" NodesToUseAsRows="" NodesToUseAsColumns=""
	 * SingleSelect="true" Mandatory="false", ReadOnly="false"
	 * 
	 * some request parameters like param1="cql"
	 * 
	 * OutputVariableName as String[]
	 */

	private static final String WIDGET_NAME = "webService";

	private static final String ENDPOINT = "EndPoint";
	private static final String METHOD = "Method";
	private static final String NS_PREFIX = "NameSpacePrefix";
	private static final String NS_URI = "NameSpaceURI";
	private static final String NODES_TO_USE_AS_ROWS = "NodesToUseAsRows";
	private static final String NODES_TO_USE_AS_COLUMNS = "NodesToUseAsColumns";
	private static final String MANDATORY = "Mandatory";
	private static final String SINGLE_SELECT = "SingleSelect";
	private static final String READ_ONLY = "ReadOnly";
	private static final String OUTPUT_SEPARATOR = "OutputSeparator";

	public WebServiceWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final WebService webService = new WebService();
		webService.setEndPoint(readString(valueMap.get(ENDPOINT)));
		webService.setMethod(readString(valueMap.get(METHOD)));
		webService.setNameSpacePrefix(readString(valueMap.get(NS_PREFIX)));
		webService.setNameSpaceURI(readString(valueMap.get(NS_URI)));
		webService.setNodesToUseAsRows(readCommaSeparatedString(valueMap.get(NODES_TO_USE_AS_ROWS)));
		webService.setNodesToUseAsColumns(readCommaSeparatedString(valueMap.get(NODES_TO_USE_AS_COLUMNS)));
		webService.setMandatory(readBooleanTrueIfTrue(valueMap.get(MANDATORY)));
		webService.setSingleSelect(readBooleanTrueIfTrue(valueMap.get(SINGLE_SELECT)));
		webService.setReadOnly(readBooleanTrueIfTrue(valueMap.get(READ_ONLY)));
		webService.setOutputSeparator(readString(valueMap.get(OUTPUT_SEPARATOR)));

		webService.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		webService.setCallParameters(extractUnmanagedStringParameters(valueMap, //
				BUTTON_LABEL, //
				ENDPOINT, //
				METHOD, //
				NS_PREFIX, //
				NS_URI, //
				NODES_TO_USE_AS_ROWS, //
				NODES_TO_USE_AS_COLUMNS, //
				MANDATORY, //
				SINGLE_SELECT, //
				READ_ONLY, //
				OUTPUT_SEPARATOR //
				) //
				);

		return webService;
	}

}
