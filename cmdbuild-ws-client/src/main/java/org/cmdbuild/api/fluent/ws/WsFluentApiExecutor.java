package org.cmdbuild.api.fluent.ws;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.api.fluent.ws.ClassAttribute.classAttribute;
import static org.cmdbuild.api.fluent.ws.FunctionInput.functionInput;
import static org.cmdbuild.api.fluent.ws.FunctionOutput.functionOutput;
import static org.cmdbuild.api.fluent.ws.ReportHelper.DEFAULT_TYPE;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.CreateReport;
import org.cmdbuild.api.fluent.DownloadedReport;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingCard.Attachment;
import org.cmdbuild.api.fluent.ExistingProcessInstance;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Function;
import org.cmdbuild.api.fluent.FunctionCall;
import org.cmdbuild.api.fluent.Lookup;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewProcessInstance;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.ProcessInstanceDescriptor;
import org.cmdbuild.api.fluent.QueryAllLookup;
import org.cmdbuild.api.fluent.QueryClass;
import org.cmdbuild.api.fluent.QuerySingleLookup;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.api.fluent.RelationsQuery;
import org.cmdbuild.common.Constants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.ReportParams;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;

public class WsFluentApiExecutor implements FluentApiExecutor {

	public enum WsType {

		BOOLEAN(Constants.Webservices.BOOLEAN_TYPE_NAME), //
		CHAR(Constants.Webservices.CHAR_TYPE_NAME), //
		DATE(Constants.Webservices.DATE_TYPE_NAME), //
		DECIMAL(Constants.Webservices.DECIMAL_TYPE_NAME), //
		DOUBLE(Constants.Webservices.DOUBLE_TYPE_NAME), //
		FOREIGNKEY(Constants.Webservices.FOREIGNKEY_TYPE_NAME), //
		INET(Constants.Webservices.INET_TYPE_NAME), //
		INTEGER(Constants.Webservices.INTEGER_TYPE_NAME), //
		LOOKUP(Constants.Webservices.LOOKUP_TYPE_NAME), //
		REFERENCE(Constants.Webservices.REFERENCE_TYPE_NAME), //
		STRING(Constants.Webservices.STRING_TYPE_NAME), //
		TEXT(Constants.Webservices.TEXT_TYPE_NAME), //
		TIMESTAMP(Constants.Webservices.TIMESTAMP_TYPE_NAME), //
		TIME(Constants.Webservices.TIME_TYPE_NAME), //
		UNKNOWN(Constants.Webservices.UNKNOWN_TYPE_NAME);

		private final String wsTypeName;

		private WsType(final String wsTypeName) {
			this.wsTypeName = wsTypeName;
		}

		public static WsType from(final String wsTypeName) {
			for (final WsType wsType : values()) {
				if (wsType.wsTypeName.equals(wsTypeName)) {
					return wsType;
				}
			}
			return UNKNOWN;
		}

	}

	public interface EntryTypeConverter {

		Object toClientType(EntryTypeAttribute entryTypeAttribute, String wsValue);

		String toWsType(EntryTypeAttribute entryTypeAttribute, Object clientValue);

	}

	public interface RawTypeConverter {

		String toWsType(WsType wsType, Object value);

	}

	private static final FluentApiExecutor NULL_NEVER_USED_EXECUTOR = null;

	private static final String OPERATOR_EQUALS = "EQUALS";
	private static final String OPERATOR_AND = "AND";

	private static final List<Attribute> ALL_ATTRIBUTES = null;
	private static final Query NO_QUERY = null;
	private static final List<Order> NO_ORDERING = null;
	private static final int NO_LIMIT = 0;
	private static final int OFFSET_BEGINNING = 0;
	private static final String NO_FULLTEXT = null;
	private static final CqlQuery NO_CQL = null;

	private static final EntryTypeConverter IDENTITY_ENTRY_TYPE_CONVERTER = new EntryTypeConverter() {

		public String toClientType(final EntryTypeAttribute entityAttribute, final String wsValue) {
			return wsValue;
		}

		public String toWsType(final EntryTypeAttribute entityAttribute, final Object value) {
			return IDENTITY_RAW_TYPE_CONVERTER.toWsType(WsType.UNKNOWN, value);
		}

	};

	private static final RawTypeConverter IDENTITY_RAW_TYPE_CONVERTER = new RawTypeConverter() {

		public String toWsType(final WsType wsType, final Object value) {
			return (value != null) ? value.toString() : StringUtils.EMPTY;
		}

	};

	private final Private proxy;

	private interface EntityAttributeCreator {
		EntryTypeAttribute attributeFor(String entryTypeName, String attributeName);
	}

	private static EntityAttributeCreator cardAttributeCreator = new EntityAttributeCreator() {

		public EntryTypeAttribute attributeFor(final String entryTypeName, final String attributeName) {
			return classAttribute(entryTypeName, attributeName);
		}

	};

	private static EntityAttributeCreator functionInputCreator = new EntityAttributeCreator() {

		public EntryTypeAttribute attributeFor(final String entryTypeName, final String attributeName) {
			return functionInput(entryTypeName, attributeName);
		}

	};

	private static EntityAttributeCreator functionOutputCreator = new EntityAttributeCreator() {

		public EntryTypeAttribute attributeFor(final String entryTypeName, final String attributeName) {
			return functionOutput(entryTypeName, attributeName);
		}

	};

	private EntryTypeConverter entryTypeConverter;
	private RawTypeConverter rawTypeConverter;

	public WsFluentApiExecutor(final Private proxy) {
		this.proxy = proxy;
		this.entryTypeConverter = IDENTITY_ENTRY_TYPE_CONVERTER;
		this.rawTypeConverter = IDENTITY_RAW_TYPE_CONVERTER;
	}

	public void setEntryTypeConverter(final EntryTypeConverter entryTypeConverter) {
		this.entryTypeConverter = (entryTypeConverter == null) ? IDENTITY_ENTRY_TYPE_CONVERTER : entryTypeConverter;
	}

	public void setRawTypeConverter(final RawTypeConverter rawTypeConverter) {
		this.rawTypeConverter = (rawTypeConverter == null) ? IDENTITY_RAW_TYPE_CONVERTER : rawTypeConverter;
	}

	public CardDescriptor create(final NewCard card) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(card);
		final int id = proxy.createCard(soapCard);
		return new CardDescriptor(card.getClassName(), id);
	}

	public void update(final ExistingCard card) {
		if (!card.getAttributes().isEmpty()) {
			final org.cmdbuild.services.soap.Card soapCard = soapCardFor(card);
			soapCard.setId(card.getId());
			proxy.updateCard(soapCard);
		}

		for (final Attachment attachment : card.getAttachments()) {
			try {
				final DataSource dataSource = new URLDataSource(new URL(attachment.getUrl()));
				final DataHandler dataHandler = new DataHandler(dataSource);
				proxy.uploadAttachment( //
						card.getClassName(), //
						card.getId(), //
						dataHandler, //
						attachment.getName(), //
						attachment.getCategory(), //
						attachment.getDescription());
			} catch (final MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void delete(final ExistingCard card) {
		proxy.deleteCard(card.getClassName(), card.getId());
	}

	public Card fetch(final ExistingCard card) {
		final org.cmdbuild.services.soap.Card soapCard = proxy.getCardWithLongDateFormat( //
				card.getClassName(), //
				card.getId(), //
				requestedAttributesFor(card.getRequestedAttributes()));
		return cardFor(soapCard);
	}

	public List<Card> fetchCards(final QueryClass card) {
		final CardList cardList = proxy.getCardListWithLongDateFormat( //
				card.getClassName(), //
				requestedAttributesFor(card.getRequestedAttributes()), //
				queriedAttributesFor(card), //
				NO_ORDERING, //
				NO_LIMIT, //
				OFFSET_BEGINNING, //
				NO_FULLTEXT, //
				NO_CQL);
		return cardsFor(cardList);
	}

	private List<Attribute> requestedAttributesFor(final Set<String> names) {
		if (names.isEmpty()) {
			return ALL_ATTRIBUTES;
		} else {
			final List<Attribute> attributeNames = new ArrayList<Attribute>();
			for (final String attributeName : names) {
				final Attribute attribute = new Attribute();
				attribute.setName(attributeName);
				attributeNames.add(attribute);
			}
			return attributeNames;
		}
	}

	private Query queriedAttributesFor(final Card card) {
		if (card.getAttributes().isEmpty()) {
			return NO_QUERY;
		} else {
			final List<Query> queries = queriesFor(card);
			if (queries.size() == 1) {
				return queries.get(0);
			}
			final FilterOperator filterOperator = new FilterOperator();
			filterOperator.setOperator(OPERATOR_AND);
			filterOperator.getSubquery().addAll(queriesFor(card));
			return queryFor(filterOperator);
		}
	}

	private List<Query> queriesFor(final Card card) {
		final List<Query> queries = new ArrayList<Query>();
		for (final String name : card.getAttributeNames()) {
			final String wsValue = entryTypeConverter.toWsType(classAttribute(card.getClassName(), name),
					card.get(name));
			final Query attributeQuery = new Query() {
				{
					setFilter(wsEqualsFilter(name, wsValue));
				}
			};
			queries.add(attributeQuery);
		}
		return queries;
	}

	private Query queryFor(final FilterOperator filterOperator) {
		final Query query = new Query();
		query.setFilterOperator(filterOperator);
		return query;
	}

	private List<Card> cardsFor(final CardList cardList) {
		final List<Card> cards = new ArrayList<Card>();
		for (final org.cmdbuild.services.soap.Card soapCard : cardList.getCards()) {
			cards.add(cardFor(soapCard));
		}
		return unmodifiableList(cards);
	}

	private Card cardFor(final org.cmdbuild.services.soap.Card soapCard) {
		final ExistingCard card = existingCardFrom(soapCard);
		for (final Attribute attribute : soapCard.getAttributeList()) {
			final String attributeName = attribute.getName();
			final String wsValue = wsValueFor(attribute);
			card.with( //
					attributeName, //
					entryTypeConverter.toClientType(classAttribute(soapCard.getClassName(), attributeName), wsValue));
		}
		return card;
	}

	private ExistingCard existingCardFrom(final org.cmdbuild.services.soap.Card soapCard) {
		return new FluentApi(NULL_NEVER_USED_EXECUTOR) //
				.existingCard(soapCard.getClassName(), soapCard.getId());
	}

	public void create(final NewRelation relation) {
		proxy.createRelation(soapRelationFor(relation));
	}

	public void delete(final ExistingRelation relation) {
		proxy.deleteRelation(soapRelationFor(relation));
	}

	private org.cmdbuild.services.soap.Relation soapRelationFor(final Relation relation) {
		final org.cmdbuild.services.soap.Relation soapRelation = new org.cmdbuild.services.soap.Relation();
		soapRelation.setDomainName(relation.getDomainName());
		soapRelation.setClass1Name(relation.getClassName1());
		soapRelation.setCard1Id(relation.getCardId1());
		soapRelation.setClass2Name(relation.getClassName2());
		soapRelation.setCard2Id(relation.getCardId2());
		return soapRelation;
	}

	public List<Relation> fetch(final RelationsQuery query) {
		final List<org.cmdbuild.services.soap.Relation> soapRelations = proxy.getRelationList( //
				query.getDomainName(), //
				query.getClassName(), //
				query.getCardId());
		final List<Relation> relations = new ArrayList<Relation>();
		for (final org.cmdbuild.services.soap.Relation soapRelation : soapRelations) {
			relations.add(relationFor(soapRelation));
		}
		return unmodifiableList(relations);
	}

	private Relation relationFor(final org.cmdbuild.services.soap.Relation soapRelation) {
		final Relation relation = new Relation(soapRelation.getDomainName());
		relation.setCard1(soapRelation.getClass1Name(), soapRelation.getCard1Id());
		relation.setCard2(soapRelation.getClass2Name(), soapRelation.getCard2Id());
		return relation;
	}

	public Map<String, Object> execute(final FunctionCall function) {
		final List<Attribute> outputs = proxy.callFunction( //
				function.getFunctionName(), //
				wsInputAttributesFor(function));
		return unmodifiableMap(clientAttributesFor(function, outputs));
	}

	public DownloadedReport download(final CreateReport report) {
		final ReportHelper helper = new ReportHelper(proxy);
		final org.cmdbuild.services.soap.Report soapReport = helper.getReport(DEFAULT_TYPE, report.getTitle());
		final List<AttributeSchema> paramSchemas = helper.getParamSchemas(soapReport, report.getFormat());
		final List<ReportParams> reportParams = compileParams(paramSchemas, report.getParameters());
		final DataHandler dataHandler = helper.getDataHandler(soapReport, report.getFormat(), reportParams);
		final File file = helper.temporaryFile(report.getTitle(), report.getFormat());
		helper.saveToFile(dataHandler, file);
		return new DownloadedReport(file);
	}

	private List<ReportParams> compileParams(final List<AttributeSchema> paramSchemas, final Map<String, Object> params) {
		final List<ReportParams> reportParameters = new ArrayList<ReportParams>();
		for (final AttributeSchema attributeSchema : paramSchemas) {
			final String paramName = attributeSchema.getName();
			final Object paramValue = params.get(paramName);
			final WsType wsType = WsType.from(attributeSchema.getType());
			final ReportParams parameter = new ReportParams();
			parameter.setKey(paramName);
			parameter.setValue(rawTypeConverter.toWsType(wsType, paramValue));
			reportParameters.add(parameter);
		}
		if (reportParameters.isEmpty()) {
			reportParameters.add(new ReportParams());
		}
		return reportParameters;
	}

	public ProcessInstanceDescriptor createProcessInstance(final NewProcessInstance processCard,
			final AdvanceProcess advance) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(processCard);
		final boolean advanceProcess = (advance == AdvanceProcess.YES);
		final List<WorkflowWidgetSubmission> emptyWidgetsSubmission = emptyList();
		final org.cmdbuild.services.soap.Workflow workflowInfo = proxy.updateWorkflow(soapCard, advanceProcess,
				emptyWidgetsSubmission);
		return new ProcessInstanceDescriptor(processCard.getClassName(), workflowInfo.getProcessid(),
				workflowInfo.getProcessinstanceid());
	}

	public void updateProcessInstance(final ExistingProcessInstance processCard, final AdvanceProcess advance) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(processCard);
		final boolean advanceProcess = (advance == AdvanceProcess.YES);
		final List<WorkflowWidgetSubmission> emptyWidgetsSubmission = emptyList();
		proxy.updateWorkflow(soapCard, advanceProcess, emptyWidgetsSubmission);
	}

	/*
	 * Utils
	 */

	private List<Attribute> wsInputAttributesFor(final Function function) {
		return wsAttributesFor(functionInputCreator, function.getFunctionName(), function.getInputs());
	}

	private Map<String, Object> clientAttributesFor(final Function function, final List<Attribute> wsAttributes) {
		return clientAttributesFor(functionOutputCreator, function.getFunctionName(), wsAttributes);
	}

	private org.cmdbuild.services.soap.Card soapCardFor(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = new org.cmdbuild.services.soap.Card();
		soapCard.setClassName(card.getClassName());
		if (card.getId() != null) {
			soapCard.setId(card.getId());
		}
		soapCard.getAttributeList().addAll(wsAttributesFor(card));
		return soapCard;
	}

	private List<Attribute> wsAttributesFor(final Card card) {
		return wsAttributesFor(cardAttributeCreator, card.getClassName(), card.getAttributes());
	}

	private List<Attribute> wsAttributesFor(final EntityAttributeCreator attributeCreator, final String className,
			final Map<String, Object> attributes) {
		final List<Attribute> wsAttributes = new ArrayList<Attribute>(attributes.size());
		for (final Map.Entry<String, Object> e : attributes.entrySet()) {
			final String wsValue = entryTypeConverter.toWsType(attributeCreator.attributeFor(className, e.getKey()),
					e.getValue());
			wsAttributes.add(wsAttribute(e.getKey(), wsValue));
		}
		return wsAttributes;
	}

	private Map<String, Object> clientAttributesFor(final EntityAttributeCreator attributeCreator,
			final String entryTypeName, final List<Attribute> wsAttributes) {
		final Map<String, Object> clientAttributes = new HashMap<String, Object>();
		for (final Attribute attribute : wsAttributes) {
			final String attributeName = attribute.getName();
			final String wsValue = wsValueFor(attribute);
			clientAttributes.put( //
					attributeName, //
					entryTypeConverter.toClientType(attributeCreator.attributeFor(entryTypeName, attributeName),
							wsValue) //
					);
		}
		return clientAttributes;
	}

	/*
	 * WS object factories
	 */

	private String wsValueFor(final Attribute wsAttribute) {
		return isReferenceOrLookup(wsAttribute) ? wsAttribute.getCode() : wsAttribute.getValue();
	}

	private boolean isReferenceOrLookup(final Attribute wsAttribute) {
		return isNotBlank(wsAttribute.getCode());
	}

	public Filter wsEqualsFilter(final String attributeName, final String attibuteValue) {
		return new Filter() {
			{
				setName(attributeName);
				setOperator(OPERATOR_EQUALS);
				getValue().add(attibuteValue);
			}
		};
	}

	public static Attribute wsAttribute(final String attributeName, final String attributeValue) {
		return new Attribute() {
			{
				setName(attributeName);
				setValue(attributeValue);
			}
		};
	}

	public Iterable<Lookup> fetch(final QueryAllLookup queryLookup) {
		throw new UnsupportedOperationException("TODO");
	}

	public Lookup fetch(final QuerySingleLookup querySingleLookup) {
		throw new UnsupportedOperationException("TODO");
	}

}
