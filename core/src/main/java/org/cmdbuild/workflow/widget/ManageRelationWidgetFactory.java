package org.cmdbuild.workflow.widget;

import static org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class ManageRelationWidgetFactory extends ValuePairWidgetFactory {

	private final static String WIDGET_NAME = "manageRelation";

	public final static String DOMAIN = "DomainName";
	private final static String FUNCTIONS = "EnabledFunctions";
	public final static String CLASS_NAME = "ClassName";
	public final static String CARD_CQL_SELECTOR = "ObjId";
	public static final String OBJ_REF = "ObjRef";
	public final static String REQUIRED = "Required";
	public final static String IS_DIRECT = "IsDirect";

	private final CMDataView dataView;

	public ManageRelationWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier,
			final CMDataView dataView) {
		super(templateRespository, notifier);
		this.dataView = dataView;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final String className;
		final ManageRelation widget = new ManageRelation();

		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setDomainName(readString(valueMap.get(DOMAIN)));

		if (valueMap.containsKey(OBJ_REF)) {
			className = configureWidgetFromReference(widget, valueMap);
		} else {
			className = configureWidgetFromClassName(widget, valueMap);
		}
		widget.setRequired(readBooleanTrueIfPresent(valueMap.get(REQUIRED)));
		setSource(widget, valueMap.get(IS_DIRECT));
		setEnabledFunctions(widget, readString(valueMap.get(FUNCTIONS)));

		configureWidgetDestinationClassName(widget, readString(valueMap.get(DOMAIN)), className);

		return widget;
	}

	private void configureWidgetDestinationClassName(final ManageRelation widget, final String domainName,
			final String className) {
		if (!StringUtils.isEmpty(domainName)) {
			final CMDomain domain = dataView.findDomain(domainName);
			final String class1 = domain.getClass1().getName();
			final String class2 = domain.getClass2().getName();

			final String destinationClassName = class1.equals(className) ? class2 : class1;
			widget.setDestinationClassName(destinationClassName);
		} else {
			widget.setDestinationClassName(readString(null));
		}
	}

	private void setSource(final ManageRelation widget, final Object isDirect) {
		if (isDirect != null) {
			final String source = readBooleanTrueIfTrue(isDirect) ? "_1" : "_2";
			widget.setSource(source);
		}
	}

	private void setEnabledFunctions(final ManageRelation widget, final String functions) {
		if (functions == null) {
			return;
		} else {
			widget.setCanCreateRelation(isEnabled(functions, 0));
			widget.setCanCreateAndLinkCard(isEnabled(functions, 1));
			widget.setMultiSelection(isEnabled(functions, 2));
			widget.setSingleSelection(isEnabled(functions, 3));
			widget.setCanModifyARelation(isEnabled(functions, 4));
			widget.setCanRemoveARelation(isEnabled(functions, 5));
			widget.setCanModifyALinkedCard(isEnabled(functions, 6));
			widget.setCanRemoveALinkedCard(isEnabled(functions, 7));
		}
	}

	private boolean isEnabled(final String functions, final int index) {
		boolean enabled = false;
		try {
			final char c = functions.charAt(index);
			enabled = c == '1';
		} catch (final IndexOutOfBoundsException e) {
			// ignore
		}

		return enabled;
	}

	private String configureWidgetFromClassName(final ManageRelation widget, final Map<String, Object> valueMap) {
		final String className = readString(valueMap.get(CLASS_NAME));
		final String cardIdOrCql = readString(valueMap.get(CARD_CQL_SELECTOR));
		Validate.notEmpty(className, CLASS_NAME + " is required");

		widget.setClassName(className);
		widget.setObjId(cardIdOrCql);

		return className;
	}

	private String configureWidgetFromReference(final ManageRelation widget, final Map<String, Object> valueMap) {
		final Long id = Long.class.cast(valueMap.get(OBJ_REF));

		final CMClass queryClass = dataView.findClass(Constants.BASE_CLASS_NAME);
		final CMCard card = dataView.select(attribute(queryClass, DESCRIPTION_ATTRIBUTE)) //
				.from(queryClass) //
				.where(condition(attribute(queryClass, ID_ATTRIBUTE), eq(id))) //
				.run() //
				.getOnlyRow() //
				.getCard(queryClass);

		widget.setClassName(card.getType().getName());
		widget.setObjId(card.getId().toString());

		return card.getType().getName();
	}
}
