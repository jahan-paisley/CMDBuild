package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.cmdbuild.services.soap.utils.SoapToJsonUtils.createJsonFilterFrom;
import static org.cmdbuild.services.soap.utils.SoapToJsonUtils.toJsonArray;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import net.sf.jasperreports.engine.util.ObjectUtils;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.report.ReportFactoryDB;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.report.ReportParameterConverter;
import org.cmdbuild.services.auth.PrivilegeManager.PrivilegeType;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.soap.serializer.MenuSchemaSerializer;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.ClassSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLParameter;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card.ValueSerializer;
import org.cmdbuild.services.soap.types.CardExt;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.CardListExt;
import org.cmdbuild.services.soap.types.Metadata;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Report;
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.soap.utils.DateTimeSerializer;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class DataAccessLogicHelper implements SoapLogicHelper {

	private static final Marker marker = MarkerFactory.getMarker(DataAccessLogicHelper.class.getName());

	private static final String ACTIVITY_DESCRIPTION_ATTRIBUTE = "ActivityDescription";
	private static final String INVALID_ACTIVITY_DESCRIPTION = EMPTY;

	private static final List<Attribute> EMPTY_ATTRIBUTES = Collections.emptyList();

	private static final Comparator<Card> BEGIN_DATE_DESC = new Comparator<Card>() {

		@Override
		public int compare(final Card o1, final Card o2) {
			final DateTime beginDate1 = o1.getBeginDate();
			final DateTime beginDate2 = o2.getBeginDate();
			return beginDate2.compareTo(beginDate1);
		}

	};

	private static final Function<Card, org.cmdbuild.services.soap.types.Card> TO_SOAP_CARD = new Function<Card, org.cmdbuild.services.soap.types.Card>() {

		@Override
		public org.cmdbuild.services.soap.types.Card apply(final Card input) {
			return new org.cmdbuild.services.soap.types.Card(input);
		}

	};

	private final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;
	private final WorkflowLogic workflowLogic;
	private final OperationUser operationUser;
	private final javax.sql.DataSource dataSource;
	private final SerializationStuff serializationUtils;
	private final AuthenticationStore authenticationStore;
	private final CmdbuildConfiguration configuration;
	private final MetadataStoreFactory metadataStoreFactory;
	private final CardAdapter cardAdapter;

	private MenuStore menuStore;
	private ReportStore reportStore;
	private LookupStore lookupStore;

	public DataAccessLogicHelper( //
			final CMDataView dataView, //
			final DataAccessLogic datAccessLogic, //
			final WorkflowLogic workflowLogic, //
			final OperationUser operationUser, //
			final javax.sql.DataSource dataSource, //
			final AuthenticationStore authenticationStore, //
			final CmdbuildConfiguration configuration, //
			final MetadataStoreFactory metadataStoreFactory, //
			final CardAdapter cardAdapter //
	) {
		this.dataView = dataView;
		this.dataAccessLogic = datAccessLogic;
		this.workflowLogic = workflowLogic;
		this.operationUser = operationUser;
		this.dataSource = dataSource;
		this.serializationUtils = new SerializationStuff(dataView, metadataStoreFactory);
		this.authenticationStore = authenticationStore;
		this.configuration = configuration;
		this.metadataStoreFactory = metadataStoreFactory;
		this.cardAdapter = cardAdapter;
	}

	public void setMenuStore(final MenuStore menuStore) {
		this.menuStore = menuStore;
	}

	public void setLookupStore(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public void setReportStore(final ReportStore reportStore) {
		this.reportStore = reportStore;
	}

	public AttributeSchema[] getAttributeList(final String className) {
		logger.info(marker, "getting attributes schema for class '{}'", className);
		final List<AttributeSchema> attributes = Lists.newArrayList();
		for (final CMAttribute cmAttribute : dataAccessLogic.findClass(className).getActiveAttributes()) {
			attributes.add(serializationUtils.serialize(cmAttribute));
		}
		return attributes.toArray(new AttributeSchema[attributes.size()]);
	}

	public int createCard(final org.cmdbuild.services.soap.types.Card card) {
		return dataAccessLogic.createCard(transform(card)).intValue();
	}

	public boolean updateCard(final org.cmdbuild.services.soap.types.Card card) {
		dataAccessLogic.updateCard(transform(card));
		return true;
	}

	public boolean deleteCard(final String className, final int cardId) {
		dataAccessLogic.deleteCard(className, Long.valueOf(cardId));
		return true;
	}

	public boolean createRelation(final Relation relation) {
		dataAccessLogic.createRelations(transform(relation));
		return true;
	}

	public boolean createRelationWithAttributes(final Relation relation, final List<Attribute> attributes) {
		final RelationDTO relationDTO = transform(relation);
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		relationDTO.relationAttributeToValue = transform(attributes, domain);
		dataAccessLogic.createRelations(relationDTO);
		return true;
	}

	public List<Attribute> getRelationAttributes(final Relation relation) {
		final List<Attribute> relationAttributes = Lists.newArrayList();
		final CMClass sourceClass = dataView.findClass(relation.getClass1Name());
		final CMClass destinationClass = dataView.findClass(relation.getClass2Name());
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final CMRelation fetchedRelation = dataAccessLogic.getRelation(Long.valueOf(relation.getCard1Id()),
				Long.valueOf(relation.getCard2Id()), domain, sourceClass, destinationClass);
		for (final Entry<String, Object> entry : fetchedRelation.getAllValues()) {
			final CMAttributeType<?> attributeType = domain.getAttribute(entry.getKey()).getType();
			final Attribute attribute = new Attribute();
			attribute.setName(entry.getKey());
			attribute.setValue(entry.getValue() != null ? entry.getValue().toString() : EMPTY);
			if (attributeType instanceof LookupAttributeType) {
				if (entry.getValue() != null) {
					final IdAndDescription cardReference = (IdAndDescription) entry.getValue();
					attribute.setCode(cardReference.getId() != null ? cardReference.getId().toString() : null);
					attribute.setValue(fetchLookupDecription((cardReference.getId())));
				} else {
					attribute.setCode(EMPTY);
					attribute.setValue(EMPTY);
				}
			} else if (attributeType instanceof DateAttributeType || //
					attributeType instanceof TimeAttributeType || //
					attributeType instanceof DateTimeAttributeType) {
				attribute.setValue(org.cmdbuild.services.soap.types.Card.LEGACY_VALUE_SERIALIZER
						.serializeValueForAttribute(attributeType, entry.getKey(), entry.getValue()));
			}
			relationAttributes.add(attribute);
		}
		return relationAttributes;
	}

	public boolean deleteRelation(final Relation relation) {
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final DomainWithSource dom = DomainWithSource.create(domain.getId(), Source._1.toString());
		final Card srcCard = Card.newInstance() //
				.withClassName(relation.getClass1Name()) //
				.withId(Long.valueOf(relation.getCard1Id())) //
				.build();
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		for (final DomainInfo domainInfo : response) {
			for (final RelationInfo relationInfo : domainInfo) {
				if (relationInfo.getTargetId().equals(Long.valueOf(relation.getCard2Id()))) {
					final RelationDTO relationToDelete = transform(relation);
					relationToDelete.relationId = relationInfo.getRelationId();
					dataAccessLogic.deleteRelation(relationToDelete.domainName, relationToDelete.relationId);
				}
			}
		}
		return true;
	}

	private Card transform(final org.cmdbuild.services.soap.types.Card card) {
		final CMEntryType entryType = dataView.findClass(card.getClassName());
		final Card cardModel = Card.newInstance() //
				.withClassName(card.getClassName()) //
				.withId(Long.valueOf(card.getId())) //
				.withAllAttributes(transform(attributesOf(card), entryType)) //
				.withUser(operationUser.getAuthenticatedUser().getUsername()) //
				.build();
		return cardModel;
	}

	private List<Attribute> attributesOf(final org.cmdbuild.services.soap.types.Card card) {
		final List<Attribute> attributes = card.getAttributeList();
		return (attributes == null) ? EMPTY_ATTRIBUTES : attributes;
	}

	private Map<String, Object> transform(final List<Attribute> attributes, final CMEntryType entryType) {
		final Map<String, Object> keysAndValues = Maps.newHashMap();
		for (final Attribute attribute : attributes) {
			final CMAttributeType<?> attributeType = entryType.getAttribute(attribute.getName()).getType();
			final String name = attribute.getName();
			Object value = attribute.getValue();
			if (attributeType instanceof LookupAttributeType) {
				final LookupAttributeType lookupAttributeType = (LookupAttributeType) attributeType;
				final String lookupTypeName = lookupAttributeType.getLookupTypeName();
				Long lookupId = null;
				if (isNotBlank((String) value) && isNumeric((String) value)) {
					if (existsLookup(lookupTypeName, Long.parseLong((String) value))) {
						lookupId = Long.parseLong((String) value);
					}
				} else {
					final Iterable<Lookup> lookupList = lookupStore.list();
					for (final Lookup lookup : lookupList) {
						if (lookup.active && //
								lookup.type.name.equals(lookupTypeName) && //
								lookup.description != null && //
								ObjectUtils.equals(lookup.description, value)) {
							lookupId = lookup.getId();
							break;
						}
					}
				}
				value = lookupId == null ? null : lookupId.toString();
			} else if (attributeType instanceof DateAttributeType || //
					attributeType instanceof TimeAttributeType || //
					attributeType instanceof DateTimeAttributeType) {
				value = new DateTimeSerializer(attribute.getValue()).getValue();
			}

			if (value != null) {
				keysAndValues.put(name, value);
			}
		}
		return keysAndValues;
	}

	private boolean existsLookup(final String lookupTypeName, final Long lookupId) {
		final Iterable<Lookup> lookupList = lookupStore.list();
		for (final Lookup lookup : lookupList) {
			if (lookup.type.name.equals(lookupTypeName) && lookup.getId().equals(lookupId)) {
				return true;
			}
		}
		return false;
	}

	private RelationDTO transform(final Relation relation) {
		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.domainName = relation.getDomainName();
		relationDTO.master = Source._1.toString();
		relationDTO.addSourceCard(Long.valueOf(relation.getCard1Id()), relation.getClass1Name());
		relationDTO.addDestinationCard(Long.valueOf(relation.getCard2Id()), relation.getClass2Name());
		return relationDTO;
	}

	private Relation transform(final RelationInfo relationInfo, final long source) {
		final QueryDomain queryDomain = relationInfo.getQueryDomain();
		final CMDomain domain = queryDomain.getDomain();
		final Relation relation = new Relation();
		relation.setBeginDate(relationInfo.getRelationBeginDate().toGregorianCalendar());
		final DateTime endDate = relationInfo.getRelationEndDate();
		relation.setEndDate(endDate != null ? endDate.toGregorianCalendar() : null);
		relation.setStatus(CardStatus.ACTIVE.value());
		relation.setDomainName(domain.getIdentifier().getLocalName());

		final String targetName = relationInfo.getTargetCard().getType().getName();
		if (queryDomain.getQuerySource().equals(Source._1.toString())) {
			relation.setClass1Name(domain.getClass1().getName());
			relation.setClass2Name(targetName);
		} else {
			relation.setClass1Name(targetName);
			relation.setClass2Name(domain.getClass2().getName());
		}

		final CMRelation _relation = relationInfo.getRelation();
		if (queryDomain.getQuerySource().equals(Source._1.toString())) {
			relation.setCard1Id(_relation.getCard1Id().intValue());
			relation.setCard2Id(_relation.getCard2Id().intValue());
		} else {
			relation.setCard1Id(_relation.getCard2Id().intValue());
			relation.setCard2Id(_relation.getCard1Id().intValue());
		}
		return relation;
	}

	private String fetchLookupDecription(final Long lookupId) {
		if (lookupId == null) {
			return null;
		} else {
			final Lookup fetchedLookup = lookupStore.read(fakeLookupWithId(lookupId));
			return fetchedLookup.description;
		}
	}

	private Storable fakeLookupWithId(final Long lookupId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return String.valueOf(lookupId);
			}
		};
	}

	public List<Relation> getRelations(final String className, final String domainName, final Long cardId) {
		final CMDomain domain = dataView.findDomain(domainName);
		final CMClass cmClass = dataView.findClass(className);
		final DomainWithSource dom;
		if (domainName != null) {
			if (cmClass == null) {
				dom = DomainWithSource.create(domain.getId(), Source._1.toString());
			} else if (domain.getClass1().isAncestorOf(cmClass)) {
				dom = DomainWithSource.create(domain.getId(), Source._1.toString());
			} else {
				dom = DomainWithSource.create(domain.getId(), Source._2.toString());
			}
		} else {
			dom = null;
		}
		final Card srcCard = buildCard(cardId, (className == null) ? domain.getClass1().getName() : className);
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		final List<Relation> relations = Lists.newArrayList();
		for (final DomainInfo domainInfo : response) {
			for (final RelationInfo relationInfo : domainInfo) {
				relations.add(transform(relationInfo, cardId));
			}
		}
		return relations;
	}

	private Card buildCard(final Long cardId, final String className) {
		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		return card;
	}

	public Relation[] getRelationHistory(final Relation relation) {
		final List<Relation> historicRelations = Lists.newArrayList();
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final Card srcCard = buildCard(Long.valueOf(relation.getCard1Id()), relation.getClass1Name());
		final GetRelationHistoryResponse response = dataAccessLogic.getRelationHistory(srcCard, domain);
		for (final RelationInfo relationInfo : response) {
			if (relationInfo.getRelation().getCard1Id().equals(Long.valueOf(relation.getCard1Id()))
					&& relationInfo.getRelation().getCard2Id().equals(Long.valueOf(relation.getCard2Id()))) {
				historicRelations.add(transform(relationInfo, relation.getCard1Id()));
			}
		}
		return historicRelations.toArray(new Relation[historicRelations.size()]);
	}

	public CardExt getCardExt(final String className, final Long cardId, final Attribute[] attributeList,
			final boolean enableLongDateFormat) {
		final Card fetchedCard;
		if (attributeList == null || attributeList.length == 0) {
			fetchedCard = dataAccessLogic.fetchCard(className, cardId);
		} else {
			final QueryOptions queryOptions = QueryOptions.newQueryOption() //
					.onlyAttributes(displayableAttributes(attributeList)) //
					.build();
			fetchedCard = dataAccessLogic.fetchCardShort(className, cardId, queryOptions);
		}
		return transformToCardExt(fetchedCard, attributeList, enableLongDateFormat);
	}

	private JSONArray displayableAttributes(final Attribute[] attributeList) {
		final JSONArray array = new JSONArray();
		for (final Attribute attribute : attributeList) {
			array.put(attribute.getName());
		}
		return array;
	}

	private CardExt transformToCardExt(final Card card, final Attribute[] attributeList,
			final boolean enableLongDateFormat) {
		final CardExt cardExt;
		final ValueSerializer valueSerializer = enableLongDateFormat ? org.cmdbuild.services.soap.types.Card.HACK_VALUE_SERIALIZER
				: org.cmdbuild.services.soap.types.Card.LEGACY_VALUE_SERIALIZER;
		if (attributeList == null || attributeList.length == 0) {
			cardExt = new CardExt(card, valueSerializer);
		} else {
			cardExt = new CardExt(card, attributeList, valueSerializer);
		}
		addExtras(card, cardExt);
		return cardExt;
	}

	private void addExtras(final Card card, final CardExt cardExt) {
		final CMClass activityClass = dataAccessLogic.findClass(Constants.BASE_PROCESS_CLASS_NAME);
		if (activityClass.isAncestorOf(card.getType())) {
			final UserProcessInstance processInstance = workflowLogic.getProcessInstance(card.getClassName(),
					card.getId());
			final WorkflowLogicHelper workflowLogicHelper = new WorkflowLogicHelper(workflowLogic, dataView,
					metadataStoreFactory, cardAdapter);
			UserActivityInstance activityInstance = null;
			try {
				activityInstance = workflowLogicHelper.selectActivityInstanceFor(processInstance);
			} catch (final CMWorkflowException e) {
				activityInstance = null;
			}
			addActivityExtras(activityInstance, cardExt);
			addActivityMetadata(activityInstance, cardExt);
		} else {
			addMetadata(card, cardExt);
		}
	}

	private void addActivityExtras(final UserActivityInstance actInst, final CardExt cardExt) {
		String activityDescription = INVALID_ACTIVITY_DESCRIPTION;
		if (actInst != null) {
			try {
				activityDescription = actInst.getDefinition().getDescription();
			} catch (final CMWorkflowException e) {
				// keep the placeholder description
			}
		}
		cardExt.getAttributeList().add(newAttribute(ACTIVITY_DESCRIPTION_ATTRIBUTE, activityDescription));
	}

	private Attribute newAttribute(final String name, final String value) {
		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(value);
		return attribute;
	}

	private void addActivityMetadata(final UserActivityInstance actInst, final CardExt cardExt) {
		final PrivilegeType privileges;
		if (actInst != null) {
			privileges = actInst.isWritable() ? PrivilegeType.WRITE : PrivilegeType.READ;
		} else {
			privileges = PrivilegeType.NONE;
		}
		addPrivilege(privileges, cardExt);
	}

	private void addPrivilege(final PrivilegeType privilege, final CardExt cardExt) {
		cardExt.setMetadata(Arrays.asList(newPrivilegeMetadata(privilege)));
	}

	private Metadata newPrivilegeMetadata(final PrivilegeType privilege) {
		final Metadata meta = new Metadata();
		meta.setKey(MetadataService.RUNTIME_PRIVILEGES_KEY);
		meta.setValue(privilegeSerialization(privilege));
		return meta;
	}

	private String privilegeSerialization(final PrivilegeType privileges) {
		return privileges.toString().toLowerCase();
	}

	// TODO: fetch privileges for table outside...
	private void addMetadata(final Card card, final CardExt cardExt) {
		final CMClass type = card.getType();
		final PrivilegeType privilege;
		if (operationUser.hasWriteAccess(type)) {
			privilege = PrivilegeType.WRITE;
		} else if (operationUser.hasReadAccess(type)) {
			privilege = PrivilegeType.READ;
		} else {
			privilege = PrivilegeType.NONE;
		}
		addPrivilege(privilege, cardExt);
	}

	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery, final boolean enableLongDateFormat) {
		final FetchCardListResponse response = cardList(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, cqlQuery);
		return toCardList(response, attributeList, enableLongDateFormat);
	}

	public CardListExt getCardListExt(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		final FetchCardListResponse response = cardList(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, cqlQuery);
		return toCardListExt(response);
	}

	private FetchCardListResponse cardList(final String className, final Attribute[] attributeList,
			final Query queryType, final Order[] orderType, final Integer limit, final Integer offset,
			final String fullTextQuery, final CQLQuery cqlQuery) {
		final CMClass targetClass = dataView.findClass(className);
		final QueryOptions queryOptions = new GuestFilter(authenticationStore, dataView) //
				.apply(targetClass, QueryOptions.newQueryOption() //
						.limit(limit != null ? limit : Integer.MAX_VALUE) //
						.offset(offset != null ? offset : 0) //
						.filter(createJsonFilterFrom(queryType, fullTextQuery, cqlQuery, targetClass, lookupStore)) //
						.orderBy(toJsonArray(orderType, attributeList)) //
						.onlyAttributes(toJsonArray(attributeList)) //
						.parameters(parametersOf(cqlQuery)) //
						.build());
		return dataAccessLogic.fetchCards(className, queryOptions);
	}

	private Map<String, Object> parametersOf(final CQLQuery cqlQuery) {
		final boolean hasParameters = (cqlQuery != null) && (cqlQuery.getParameters() != null);
		return hasParameters ? toMap(cqlQuery.getParameters()) : new HashMap<String, Object>();
	}

	private CardList toCardList(final FetchCardListResponse response, final Attribute[] subsetAttributesForSelect,
			final boolean enableLongDateFormat) {
		final CardList cardList = new CardList();
		final int totalNumberOfCards = response.getTotalNumberOfCards();
		cardList.setTotalRows(totalNumberOfCards);
		for (final Card card : response.getPaginatedCards()) {
			final ValueSerializer valueSerializer = enableLongDateFormat ? org.cmdbuild.services.soap.types.Card.HACK_VALUE_SERIALIZER
					: org.cmdbuild.services.soap.types.Card.LEGACY_VALUE_SERIALIZER;
			final org.cmdbuild.services.soap.types.Card soapCard = new org.cmdbuild.services.soap.types.Card(card,
					valueSerializer);
			removeNotSelectedAttributesFrom(soapCard, subsetAttributesForSelect);
			cardList.addCard(soapCard);
		}
		return cardList;
	}

	private void removeNotSelectedAttributesFrom(final org.cmdbuild.services.soap.types.Card soapCard,
			final Attribute[] attributesSubset) {
		if (attributesSubset == null || attributesSubset.length == 0) {
			return;
		}
		final List<Attribute> onlyRequestedAttributes = Lists.newArrayList();
		for (final Attribute cardAttribute : attributesOf(soapCard)) {
			if (belongsToAttributeSubset(cardAttribute, attributesSubset)) {
				onlyRequestedAttributes.add(cardAttribute);
			}
		}
		soapCard.setAttributeList(onlyRequestedAttributes);
	}

	private boolean belongsToAttributeSubset(final Attribute attribute, final Attribute[] attributesSubset) {
		for (final Attribute attr : attributesSubset) {
			if (attr.getName().equals(attribute.getName())) {
				return true;
			}
		}
		return false;
	}

	private CardListExt toCardListExt(final FetchCardListResponse response) {
		final CardListExt cardListExt = new CardListExt();
		final int totalNumberOfCards = response.getTotalNumberOfCards();
		cardListExt.setTotalRows(totalNumberOfCards);
		for (final Card card : response.getPaginatedCards()) {
			final CardExt cardExt = new CardExt(card);
			addExtras(card, cardExt);
			cardListExt.addCard(cardExt);
		}
		return cardListExt;
	}

	private Map<String, Object> toMap(final List<CQLParameter> cqlParameters) {
		final Map<String, Object> parameters = Maps.newHashMap();
		for (final CQLParameter cqlParameter : cqlParameters) {
			parameters.put(cqlParameter.getKey(), cqlParameter.getValue());
		}
		return parameters;
	}

	public Reference[] getReference(final String classname, final Query query, final Order[] order,
			final Integer limit, final Integer offset, final String fullText, final CQLQuery cqlQuery) {
		final CardListExt cardList = getCardListExt(classname, null, query, order, limit, offset, fullText, cqlQuery);
		return from(cardList.getCards()) //
				.transform(new Function<CardExt, Reference>() {
					@Override
					public Reference apply(final CardExt input) {
						final Reference reference = new Reference();
						reference.setId(input.getId());
						reference.setClassname(classname);
						reference.setDescription(descriptionOf(input));
						reference.setTotalRows(cardList.getTotalRows());
						return reference;
					}
				}) //
				.toArray(Reference.class);
	}

	private String descriptionOf(final CardExt card) {
		for (final Attribute attribute : card.getAttributeList()) {
			if ("Description".equals(attribute.getName())) {
				return attribute.getValue();
			}
		}
		return EMPTY;
	}

	public CardList getCardHistory(final String className, final int cardId, final Integer limit, final Integer offset) {
		logger.info(marker, "getting history for '{}' card with id '{}'", className, cardId);
		final CardList cardList = new CardList();
		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(Long.valueOf(cardId)) //
				.build();
		final GetCardHistoryResponse response = dataAccessLogic.getCardHistory(card);

		final List<Card> ordered = Ordering.from(BEGIN_DATE_DESC).sortedCopy(response);
		for (final org.cmdbuild.services.soap.types.Card element : from(ordered) //
				.skip((offset != null) ? offset : 0) //
				.limit(((limit != null) && (limit != 0)) ? limit : Integer.MAX_VALUE) //
				.transform(TO_SOAP_CARD) //
		) {
			cardList.addCard(element);
		}
		cardList.setTotalRows(size(response));
		return cardList;
	}

	public ClassSchema getClassSchema(final String className) {
		logger.info(marker, "getting schema for class '{}'");
		final CMClass clazz = dataAccessLogic.findClass(className);

		final ClassSchema classSchema = new ClassSchema();
		classSchema.setName(clazz.getIdentifier().getLocalName());
		classSchema.setDescription(clazz.getDescription());
		classSchema.setSuperClass(clazz.isSuperclass());

		final List<AttributeSchema> attributes = Lists.newArrayList();
		for (final CMAttribute attribute : clazz.getAttributes()) {
			if (attribute.isSystem() || !attribute.isActive()) {
				logger.debug(marker, "skipping attribute '{}'", attribute.getName());
				continue;
			}
			logger.debug(marker, "keeping attribute '{}'", attribute.getName());
			attributes.add(serializationUtils.serialize(attribute));
		}
		classSchema.setAttributes(attributes);

		return classSchema;
	}

	public MenuSchema getVisibleClassesTree() {
		final CMClass rootClass = dataView.findClass("Class");
		final MenuSchemaSerializer serializer = new MenuSchemaSerializer(menuStore, operationUser, dataAccessLogic,
				workflowLogic);
		return serializer.serializeVisibleClassesFromRoot(rootClass);
	}

	public MenuSchema getVisibleProcessesTree() {
		final CMClass rootClass = dataView.findClass("Activity");
		final MenuSchemaSerializer serializer = new MenuSchemaSerializer(menuStore, operationUser, dataAccessLogic,
				workflowLogic);
		return serializer.serializeVisibleClassesFromRoot(rootClass);
	}

	public MenuSchema getMenuSchemaForPreferredGroup() {
		final MenuSchemaSerializer serializer = new MenuSchemaSerializer(menuStore, operationUser, dataAccessLogic,
				workflowLogic);
		return serializer.serializeMenuTree();
	}

	public Report[] getReportsByType(final String type, final int limit, final int offset) {
		final List<Report> pagedReports = new ArrayList<Report>();
		final ReportType reportType = ReportType.valueOf(type.toUpperCase());
		int numRecords = 0;
		final List<org.cmdbuild.model.Report> fetchedReports = reportStore.findReportsByType(reportType);
		for (final org.cmdbuild.model.Report report : fetchedReports) {
			if (report.isUserAllowed()) {
				++numRecords;
				if (limit > 0 && numRecords > offset && numRecords <= offset + limit) {
					pagedReports.add(transform(report));
				}
			}
		}
		return pagedReports.toArray(new Report[pagedReports.size()]);
	}

	private Report transform(final org.cmdbuild.model.Report reportModel) {
		final Report report = new Report();
		report.setDescription(reportModel.getDescription());
		report.setId(reportModel.getId());
		report.setTitle(reportModel.getCode());
		report.setType(reportModel.getType().toString());
		return report;
	}

	public AttributeSchema[] getReportParameters(final int id, final String extension) {
		ReportFactoryDB reportFactory;
		try {
			reportFactory = new ReportFactoryDB(dataSource, configuration, reportStore, id,
					ReportExtension.valueOf(extension.toUpperCase()));
			final List<AttributeSchema> reportParameterList = new ArrayList<AttributeSchema>();
			for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
				final CMAttribute reportAttribute = ReportParameterConverter.of(reportParameter).toCMAttribute();
				final AttributeSchema attribute = serializationUtils.serialize(reportAttribute);
				reportParameterList.add(attribute);
			}
			return reportParameterList.toArray(new AttributeSchema[reportParameterList.size()]);
		} catch (final SQLException e) {
			Log.SOAP.error("SQL error in report", e);
		} catch (final IOException e) {
			Log.SOAP.error("Error reading report", e);
		} catch (final ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report", e);
		}
		return null;
	}

	public DataHandler getReport(final int id, final String extension, final ReportParams[] params) {
		final ReportExtension reportExtension = ReportExtension.valueOf(extension.toUpperCase());
		try {
			final ReportFactoryDB reportFactory = new ReportFactoryDB(dataSource, configuration, reportStore, id,
					reportExtension);
			if (params != null) {
				for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
					for (final ReportParams param : params) {
						if (param.getKey().equals(reportParameter.getName())) {
							// update parameter
							reportParameter.parseValue(param.getValue());
						}
					}
				}
			}
			reportFactory.fillReport();
			String filename = reportFactory.getReportCard().getCode().replaceAll(" ", "");
			// add extension
			filename += "." + reportFactory.getReportExtension().toString().toLowerCase();
			// send to stream
			final DataSource dataSource = TempDataSource.create(filename, reportFactory.getContentType());
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final SQLException e) {
			Log.SOAP.error("SQL error in report", e);
		} catch (final IOException e) {
			Log.SOAP.error("Error reading report", e);
		} catch (final ClassNotFoundException e) {
			Log.SOAP.error("Cannot find class in report", e);
		} catch (final Exception e) {
			Log.SOAP.error("Error getting report", e);
		}
		return null;
	}

	public DataHandler getReport(final String reportId, final String extension, final ReportParams[] params) {
		try {
			final BuiltInReport builtInReport = BuiltInReport.from(reportId);
			final ReportFactory reportFactory = builtInReport //
					.newBuilder(dataView, authenticationStore, configuration) //
					.withExtension(extension) //
					.withProperties(propertiesFrom(params)) //
					.withDataSource(dataSource) //
					.withDataAccessLogic(dataAccessLogic) //
					.withOperationUser(operationUser) //
					.build();
			reportFactory.fillReport();
			final DataSource dataSource = TempDataSource.create(null, reportFactory.getContentType());
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final Throwable e) {
			throw new Error(e);
		}
	}

	private Map<String, String> propertiesFrom(final ReportParams[] params) {
		final Map<String, String> properties = Maps.newHashMap();
		if (params != null) {
			for (final ReportParams param : params) {
				properties.put(param.getKey(), param.getValue());
			}
		}
		return properties;
	}
}
