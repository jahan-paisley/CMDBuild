package org.cmdbuild.cmdbf.cmdbmdr;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.cmdbf.CMDBfItem;
import org.cmdbuild.cmdbf.CMDBfQueryResult;
import org.cmdbuild.cmdbf.CMDBfRelationship;
import org.cmdbuild.cmdbf.CMDBfUtils;
import org.cmdbuild.cmdbf.ContentSelectorFunction;
import org.cmdbuild.cmdbf.ItemSet;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.cmdbf.PathSet;
import org.cmdbuild.cmdbf.xml.DmsDocument;
import org.cmdbuild.cmdbf.xml.GeoCard;
import org.cmdbuild.cmdbf.xml.GeoClass;
import org.cmdbuild.cmdbf.xml.XmlRegistry;
import org.cmdbuild.common.Constants;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureStore;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.DeregistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidMDRFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidRecordFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.UnsupportedRecordTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.AcceptedType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ComparisonOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeclinedType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterInstanceResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EqualOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NullOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.PropertyValueType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType.RecordMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterInstanceResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.StringOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ObjectFactory;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.PropertyValueOperatorsType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryCapabilities;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypeList;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypes;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RegistrationServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ServiceDescription;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.XPathType;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.postgis.Geometry;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CmdbMDR implements ManagementDataRepository {

	// private static final Alias SOURCE_ALIAS = NameAlias.as("SOURCE");
	private static final Alias TARGET_ALIAS = NameAlias.as("TARGET");
	private static final String ENTRY_RECORDID_PREFIX = "entry:";
	private static final String DOCUMENT_RECORDID_PREFIX = "doc:";
	private static final String GEO_RECORDID_PREFIX = "geo:";

	private final XmlRegistry xmlRegistry;
	private final MdrScopedIdRegistry aliasRegistry;
	private final DataAccessLogic dataAccessLogic;
	private final DmsLogic dmsLogic;
	private final GISLogic gisLogic;
	private final GeoFeatureStore geoFeatureStore;
	private final OperationUser operationUser;
	private final CmdbfConfiguration cmdbfConfiguration;
	private final DmsConfiguration dmsConfiguration;
	private final DatabaseConfiguration databaseConfiguration;

	private class CmdbQueryResult extends CMDBfQueryResult {

		private final Map<String, Map<Long, Long>> typeMap;

		public CmdbQueryResult(final QueryType body) throws QueryErrorFault {
			super(body);
			this.typeMap = new HashMap<String, Map<Long, Long>>();
			execute();
		}

		@Override
		protected Collection<CMDBfItem> getItems(final String templateId, final Set<CMDBfId> instanceId,
				final RecordConstraintType recordConstraint) {
			try {
				final Map<Long, Long> templateTypeMap = new HashMap<Long, Long>();
				typeMap.put(templateId, templateTypeMap);
				return CmdbMDR.this.getItems(instanceId, recordConstraint, templateTypeMap);
			} catch (final Exception e) {
				throw new Error(e);
			}
		}

		@Override
		protected Collection<CMDBfRelationship> getRelationships(final String templateId,
				final Set<CMDBfId> instanceId, final Set<CMDBfId> source, final Set<CMDBfId> target,
				final RecordConstraintType recordConstraint) {
			final Map<Long, Long> templateTypeMap = new HashMap<Long, Long>();
			typeMap.put(templateId, templateTypeMap);
			return CmdbMDR.this.getRelationships(instanceId, source, target, recordConstraint, templateTypeMap);
		}

		@Override
		protected void fetchItemRecords(final String templateId, final ItemSet<CMDBfItem> items,
				final ContentSelectorType contentSelector) {
			final Map<Long, Long> templateTypeMap = typeMap.get(templateId);
			CmdbMDR.this.fetchItemRecords(items, contentSelector, templateTypeMap);
		}

		@Override
		protected void fetchRelationshipRecords(final String templateId, final PathSet relationships,
				final ContentSelectorType contentSelector) {
			final Map<Long, Long> templateTypeMap = typeMap.get(templateId);
			CmdbMDR.this.fetchRelationshipRecords(relationships, contentSelector, templateTypeMap);
		}

		@Override
		protected CMDBfId resolveAlias(final MdrScopedIdType alias) {
			return aliasRegistry.resolveAlias(alias);
		}

		@Override
		protected void fetchAlias(final CMDBfItem item) {
			final Set<CMDBfId> idSet = new HashSet<CMDBfId>();
			for (final CMDBfId id : item.instanceIds()) {
				idSet.addAll(aliasRegistry.getAlias(id));
			}
			item.instanceIds().addAll(idSet);
		}
	}

	public CmdbMDR(final XmlRegistry xmlRegistry, final DataAccessLogic dataAccessLogic, final DmsLogic dmsLogic,
			final GISLogic gisLogic, final GeoFeatureStore geoFeatureStore, final OperationUser operationUser,
			final MdrScopedIdRegistry aliasRegistry, final CmdbfConfiguration cmdbfConfiguration,
			final DmsConfiguration dmsConfiguration, final DatabaseConfiguration databaseConfiguration) {
		this.xmlRegistry = xmlRegistry;
		this.dataAccessLogic = dataAccessLogic;
		this.dmsLogic = dmsLogic;
		this.gisLogic = gisLogic;
		this.geoFeatureStore = geoFeatureStore;
		this.operationUser = operationUser;
		this.aliasRegistry = aliasRegistry;
		this.cmdbfConfiguration = cmdbfConfiguration;
		this.dmsConfiguration = dmsConfiguration;
		this.databaseConfiguration = databaseConfiguration;
	}

	@Override
	public String getMdrId() {
		return cmdbfConfiguration.getMdrId();
	}

	@Override
	public QueryServiceMetadata getQueryServiceMetadata() {
		final ObjectFactory factory = new ObjectFactory();
		final QueryServiceMetadata queryServiceMetadata = factory.createQueryServiceMetadata();
		queryServiceMetadata.setServiceDescription(getServiceDescription(factory));
		queryServiceMetadata.setRecordTypeList(getRecordTypesList(factory));
		queryServiceMetadata.setQueryCapabilities(getQueryCapabilities(factory));
		return queryServiceMetadata;
	}

	@Override
	public RegistrationServiceMetadata getRegistrationServiceMetadata() {
		final ObjectFactory factory = new ObjectFactory();
		final RegistrationServiceMetadata registrationServiceMetadata = factory.createRegistrationServiceMetadata();
		registrationServiceMetadata.setServiceDescription(getServiceDescription(factory));
		registrationServiceMetadata.setRecordTypeList(getRecordTypesList(factory));
		return registrationServiceMetadata;
	}

	@Override
	public QueryResultType graphQuery(final QueryType body) throws InvalidPropertyTypeFault, UnknownTemplateIDFault,
			ExpensiveQueryErrorFault, QueryErrorFault, XPathErrorFault, UnsupportedSelectorFault,
			UnsupportedConstraintFault {
		return new CmdbQueryResult(body);
	}

	@Override
	@Transactional
	public RegisterResponseType register(final RegisterRequestType body) throws UnsupportedRecordTypeFault,
			InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault {
		if (getMdrId().equals(body.getMdrId())) {
			final RegisterResponseType registerResponse = new RegisterResponseType();
			if (body.getItemList() != null) {
				for (final ItemType item : body.getItemList().getItem()) {
					final CMDBfItem cmdbfItem = new CMDBfItem(item);

					final RegisterInstanceResponseType registerInstanceResponse = new RegisterInstanceResponseType();
					final MdrScopedIdType instanceId = item.getInstanceId().get(0);
					registerInstanceResponse.setInstanceId(instanceId);
					try {
						registerItem(cmdbfItem);
						final AcceptedType accepted = new AcceptedType();
						for (final CMDBfId id : cmdbfItem.instanceIds()) {
							accepted.getAlternateInstanceId().add(id);
						}
						registerInstanceResponse.setAccepted(accepted);
					} catch (final Throwable e) {
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(cause.getMessage());
							cause = cause.getCause();
						}
						registerInstanceResponse.setDeclined(declined);
					}
					registerResponse.getRegisterInstanceResponse().add(registerInstanceResponse);
				}
			}
			if (body.getRelationshipList() != null) {
				for (final RelationshipType relationship : body.getRelationshipList().getRelationship()) {
					final CMDBfRelationship cmdbfRelationship = new CMDBfRelationship(relationship);

					final RegisterInstanceResponseType registerInstanceResponse = new RegisterInstanceResponseType();
					final MdrScopedIdType instanceId = relationship.getInstanceId().get(0);
					registerInstanceResponse.setInstanceId(instanceId);
					try {
						registerRelationship(cmdbfRelationship);
						final AcceptedType accepted = new AcceptedType();
						for (final CMDBfId id : cmdbfRelationship.instanceIds()) {
							accepted.getAlternateInstanceId().add(id);
						}
						registerInstanceResponse.setAccepted(accepted);
					} catch (final Throwable e) {
						Log.CMDBUILD.error("CMDBf register", e);
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(cause.getMessage());
							cause = cause.getCause();
						}
						registerInstanceResponse.setDeclined(declined);
					}
					registerResponse.getRegisterInstanceResponse().add(registerInstanceResponse);
				}
			}
			return registerResponse;
		} else {
			throw new InvalidMDRFault(body.getMdrId());
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public DeregisterResponseType deregister(final DeregisterRequestType body) throws DeregistrationErrorFault,
			InvalidMDRFault {
		if (getMdrId().equals(body.getMdrId())) {
			final DeregisterResponseType deregisterResponse = new DeregisterResponseType();
			if (body.getRelationshipIdList() != null) {
				for (final MdrScopedIdType instanceId : body.getRelationshipIdList().getInstanceId()) {
					final DeregisterInstanceResponseType deregisterInstanceResponse = new DeregisterInstanceResponseType();
					deregisterInstanceResponse.setInstanceId(instanceId);
					try {
						deregisterRelationship(instanceId);
					} catch (final Exception e) {
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(cause.getMessage());
							cause = cause.getCause();
						}
						deregisterInstanceResponse.setDeclined(declined);
					}
					deregisterResponse.getDeregisterInstanceResponse().add(deregisterInstanceResponse);
				}
			}
			if (body.getItemIdList() != null) {
				for (final MdrScopedIdType instanceId : body.getItemIdList().getInstanceId()) {
					final DeregisterInstanceResponseType deregisterInstanceResponse = new DeregisterInstanceResponseType();
					deregisterInstanceResponse.setInstanceId(instanceId);
					try {
						deregisterItem(instanceId);
					} catch (final Exception e) {
						Log.CMDBUILD.error("CMDBf deregister", e);
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(cause.getMessage());
							cause = cause.getCause();
						}
						deregisterInstanceResponse.setDeclined(declined);
					}
					deregisterResponse.getDeregisterInstanceResponse().add(deregisterInstanceResponse);
				}
			}
			return deregisterResponse;
		} else {
			throw new InvalidMDRFault(body.getMdrId());
		}
	}

	private ServiceDescription getServiceDescription(final ObjectFactory factory) {
		final ServiceDescription serviceDescription = factory.createServiceDescription();
		serviceDescription.setMdrId(getMdrId());
		return serviceDescription;
	}

	private QueryCapabilities getQueryCapabilities(final ObjectFactory factory) {
		final QueryCapabilities queryCapabilities = factory.createQueryCapabilities();

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ContentSelectorType contentSelectorType = factory
				.createContentSelectorType();
		contentSelectorType.setPropertySelector(true);
		contentSelectorType.setRecordTypeSelector(true);
		queryCapabilities.setContentSelectorSupport(contentSelectorType);

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordConstraintType recordConstraintType = factory
				.createRecordConstraintType();
		recordConstraintType.setRecordTypeConstraint(true);
		recordConstraintType.setPropertyValueConstraint(true);
		final PropertyValueOperatorsType propertyValueOperatorsType = factory.createPropertyValueOperatorsType();
		propertyValueOperatorsType.setContains(true);
		propertyValueOperatorsType.setEqual(true);
		propertyValueOperatorsType.setGreater(true);
		propertyValueOperatorsType.setGreaterOrEqual(true);
		propertyValueOperatorsType.setIsNull(true);
		propertyValueOperatorsType.setLess(true);
		propertyValueOperatorsType.setLessOrEqual(true);
		propertyValueOperatorsType.setLike(true);
		recordConstraintType.setPropertyValueOperators(propertyValueOperatorsType);
		queryCapabilities.setRecordConstraintSupport(recordConstraintType);

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RelationshipTemplateType relationshipTemplateType = factory
				.createRelationshipTemplateType();
		relationshipTemplateType.setDepthLimit(true);
		relationshipTemplateType.setMinimumMaximum(true);
		queryCapabilities.setRelationshipTemplateSupport(relationshipTemplateType);

		final XPathType xPathType = factory.createXPathType();
		queryCapabilities.setXpathSupport(xPathType);
		return queryCapabilities;
	}

	private RecordTypeList getRecordTypesList(final ObjectFactory factory) {
		final Map<String, RecordTypes> recordTypesMap = new HashMap<String, RecordTypes>();
		if (databaseConfiguration.isConfigured()) {
			for (final Object type : Iterables.concat(xmlRegistry.getTypes(CMClass.class),
					xmlRegistry.getTypes(CMDomain.class), xmlRegistry.getTypes(DocumentTypeDefinition.class),
					xmlRegistry.getTypes(GeoClass.class))) {
				final QName typeQName = xmlRegistry.getTypeQName(type);
				final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordType recordType = factory.createRecordType();
				recordType.setLocalName(typeQName.getLocalPart());
				if (type instanceof CMClass) {
					final CMClass cmClass = (CMClass) type;
					recordType.setAppliesTo("item");
					if (cmClass.getParent() != null) {
						final QName parentQName = xmlRegistry.getTypeQName(cmClass.getParent());
						final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QNameType qName = factory.createQNameType();
						qName.setNamespace(parentQName.getNamespaceURI());
						qName.setLocalName(parentQName.getLocalPart());
						recordType.getSuperType().add(qName);
					}
				} else if (type instanceof CMDomain) {
					recordType.setAppliesTo("relationship");
				} else if (type instanceof DocumentTypeDefinition) {
					recordType.setAppliesTo("item");
				} else if (type instanceof GeoClass) {
					recordType.setAppliesTo("item");
				}

				RecordTypes recordTypes = recordTypesMap.get(typeQName.getNamespaceURI());
				if (recordTypes == null) {
					recordTypes = new RecordTypes();
					recordTypes.setNamespace(typeQName.getNamespaceURI());
					recordTypes.setSchemaLocation(xmlRegistry.getByNamespaceURI(typeQName.getNamespaceURI())
							.getSchemaLocation());
					recordTypesMap.put(typeQName.getNamespaceURI(), recordTypes);
				}
				recordTypes.getRecordType().add(recordType);
			}
		}
		final RecordTypeList recordTypeList = factory.createRecordTypeList();
		recordTypeList.getRecordTypes().addAll(recordTypesMap.values());
		return recordTypeList;
	}

	private void registerItem(final CMDBfItem item) throws Exception {
		final Collection<Long> idList = new ArrayList<Long>();
		for (final CMDBfId alias : item.instanceIds()) {
			final CMDBfId id = aliasRegistry.resolveAlias(alias);
			if (id != null) {
				idList.add(aliasRegistry.getInstanceId(id));
			}
		}

		CMClass cmType = null;
		Card.CardBuilder cardBuilder = null;
		DateTime recordLastModified = null;
		for (final RecordType record : item.records()) {
			final QName recordQName = CMDBfUtils.getRecordType(record);
			final Object recordType = xmlRegistry.getType(recordQName);
			if (recordType instanceof CMClass) {
				if (cmType == null || cmType.isAncestorOf((CMClass) recordType)) {
					cmType = (CMClass) recordType;
				} else if (!(cmType.equals(recordType) || ((CMClass) recordType).isAncestorOf(cmType))) {
					throw new UnsupportedRecordTypeFault("Incompatible record type " + recordQName);
				}

				final Element xml = CMDBfUtils.getRecordContent(record);
				final Card newCard = (Card) xmlRegistry.deserialize(xml);
				if (cardBuilder == null) {
					cardBuilder = Card.newInstance();
				}
				cardBuilder.withAllAttributes(newCard.getAttributes());

				if (record.getRecordMetadata() != null && record.getRecordMetadata().getLastModified() != null) {
					final DateTime lastModified = new DateTime(record.getRecordMetadata().getLastModified()
							.toGregorianCalendar().getTimeInMillis());
					if (recordLastModified == null || lastModified.isBefore(recordLastModified)) {
						cardBuilder.withBeginDate(lastModified);
						recordLastModified = lastModified;
					}
				}
			} else if (!(recordType instanceof DocumentTypeDefinition || recordType instanceof GeoClass)) {
				throw new UnsupportedRecordTypeFault("Unsupported record type " + recordQName);
			}
		}

		if (cmType == null) {
			cmType = dataAccessLogic.findClass(Constants.BASE_CLASS_NAME);
		}

		Card card = null;
		final CMCard cmCard = Iterables.getOnlyElement(findCards(idList, cmType, null, null), null);
		if (cmCard != null) {
			card = CardStorableConverter.of(cmCard).convert(cmCard);
		}

		if (cardBuilder != null) {
			cardBuilder.withClassName(cmType.getName());

			if (card == null) {
				final Long id = dataAccessLogic.createCard(cardBuilder.build());
				item.instanceIds().add(aliasRegistry.getCMDBfId(id));
				cardBuilder.withId(id);
				card = cardBuilder.build();
			} else {
				cardBuilder.withId(card.getId());
				final Card newCard = cardBuilder.build();

				boolean modified = false;
				for (final String key : newCard.getAttributes().keySet()) {
					Object newVal = newCard.getAttribute(key);
					final Object oldVal = card.getAttribute(key);
					if (newVal instanceof String && ((String) newVal).isEmpty()) {
						newVal = null;
					}
					if (newVal != null) {
						modified |= !newVal.equals(oldVal);
					} else {
						modified |= oldVal != null;
					}
				}
				if (modified) {
					final DateTime cardDate = card.getBeginDate();
					final DateTime newCardDate = newCard.getBeginDate();
					if (cardDate == null || newCardDate == null || !newCardDate.isBefore(cardDate)) {
						dataAccessLogic.updateCard(newCard);
					} else {
						throw new RegistrationErrorFault("Out of date");
					}
				}
			}
		}

		if (card != null) {
			aliasRegistry.addAlias(card.getId(), item.instanceIds());
			item.instanceIds().addAll(aliasRegistry.getAlias(aliasRegistry.getCMDBfId(card.getId())));
		}

		for (final RecordType record : item.records()) {
			final QName recordQName = CMDBfUtils.getRecordType(record);
			final Object recordType = xmlRegistry.getType(recordQName);
			DateTime recordDate = null;
			if (record.getRecordMetadata() != null && record.getRecordMetadata().getLastModified() != null) {
				recordDate = new DateTime(record.getRecordMetadata().getLastModified().toGregorianCalendar()
						.getTimeInMillis());
			}
			if (card != null) {
				if (recordType instanceof DocumentTypeDefinition) {
					final Element xml = CMDBfUtils.getRecordContent(record);
					final DmsDocument newDocument = (DmsDocument) xmlRegistry.deserialize(xml);

					DateTime documentDate = null;
					if (recordDate != null) {
						final List<StoredDocument> documents = dmsLogic.search(card.getClassName(), card.getId());
						final Iterator<StoredDocument> documentIterator = documents.iterator();
						while (recordDate == null && documentIterator.hasNext()) {
							final StoredDocument document = documentIterator.next();
							if (document.getName().equals(newDocument.getName())) {
								documentDate = new DateTime(document.getModified().getTime());
							}
						}
					}
					if (recordDate == null || documentDate == null || !recordDate.isBefore(documentDate)) {
						if (newDocument.getInputStream() != null) {
							dmsLogic.upload(operationUser.getAuthenticatedUser().getUsername(), card.getClassName(),
									card.getId(), newDocument.getInputStream(), newDocument.getName(),
									newDocument.getCategory(), newDocument.getDescription(),
									newDocument.getMetadataGroups());
						} else {
							dmsLogic.updateDescriptionAndMetadata(card.getClassName(), card.getId(),
									newDocument.getName(), newDocument.getDescription(),
									newDocument.getMetadataGroups());
						}
					} else {
						throw new RegistrationErrorFault("Record " + recordQName + " Out of date");
					}
				} else if (recordType instanceof GeoClass) {
					final Element xml = CMDBfUtils.getRecordContent(record);
					final GeoCard geoCard = (GeoCard) xmlRegistry.deserialize(xml);
					final JSONObject jsonObject = new JSONObject();
					for (final LayerMetadata layer : geoCard.getType().getLayers()) {
						final Geometry value = geoCard.get(layer.getName());
						if (value != null) {
							jsonObject.put(layer.getName(), value.toString());
						}
					}
					gisLogic.updateFeatures(card,
							Collections.<String, Object> singletonMap("geoAttributes", jsonObject.toString()));
				}
			} else {
				throw new RegistrationErrorFault("Card for record " + recordQName + " not found");
			}
		}
	}

	private void registerRelationship(final CMDBfRelationship relationship) throws Exception {
		final Collection<Long> idList = new ArrayList<Long>();
		for (final CMDBfId alias : relationship.instanceIds()) {
			final CMDBfId id = aliasRegistry.resolveAlias(alias);
			if (id != null) {
				idList.add(aliasRegistry.getInstanceId(id));
			}
		}
		final CMDBfId sourceId = aliasRegistry.resolveAlias(relationship.getSource());
		final CMDBfId targetId = aliasRegistry.resolveAlias(relationship.getTarget());

		CMDomain cmType = null;
		RelationDTO newRelation = null;
		DateTime recordLastModified = null;
		for (final RecordType record : relationship.records()) {
			final QName recordQName = CMDBfUtils.getRecordType(record);
			final Object recordType = xmlRegistry.getType(recordQName);
			if (recordType instanceof CMDomain) {
				if (cmType == null) {
					cmType = (CMDomain) recordType;
				} else if (!(cmType.equals(recordType))) {
					throw new UnsupportedRecordTypeFault("Incompatible record type " + recordQName);
				}

				final Element xml = CMDBfUtils.getRecordContent(record);
				final RelationDTO recordRelation = (RelationDTO) xmlRegistry.deserialize(xml);
				if (newRelation == null) {
					newRelation = recordRelation;
				} else {
					newRelation.relationAttributeToValue.putAll(recordRelation.relationAttributeToValue);
				}

				if (record.getRecordMetadata() != null && record.getRecordMetadata().getLastModified() != null) {
					final DateTime lastModified = new DateTime(record.getRecordMetadata().getLastModified()
							.toGregorianCalendar().getTimeInMillis());
					if (recordLastModified == null || lastModified.isBefore(recordLastModified)) {
						recordLastModified = lastModified;
					}
				}
			}
		}

		CMRelation relation = null;
		Long relationId = null;
		if (cmType != null) {
			relation = Iterables.getOnlyElement(findRelations(idList, null, null, cmType, null, null), null);
			if (relation == null && sourceId != null && targetId != null) {
				relation = Iterables.getOnlyElement(
						findRelations(null, Arrays.asList(aliasRegistry.getInstanceId(sourceId)),
								Arrays.asList(aliasRegistry.getInstanceId(targetId)), cmType, null, null), null);
			}
		}

		if (newRelation != null) {
			newRelation.domainName = cmType.getName();

			if (relation == null) {
				if (sourceId != null && targetId != null) {
					final CMCard source = Iterables.getOnlyElement(
							findCards(Arrays.asList(aliasRegistry.getInstanceId(sourceId)), cmType.getClass1(), null,
									new ArrayList<QName>()), null);
					final CMCard target = Iterables.getOnlyElement(
							findCards(Arrays.asList(aliasRegistry.getInstanceId(targetId)), cmType.getClass2(), null,
									new ArrayList<QName>()), null);

					if (source == null) {
						throw new RegistrationErrorFault("Source not found");
					}
					if (target == null) {
						throw new RegistrationErrorFault("Target not found");
					}

					newRelation.addSourceCard(source.getId(), source.getType().getIdentifier().getLocalName());
					newRelation.addDestinationCard(target.getId(), target.getType().getIdentifier().getLocalName());
					relationId = Iterables.getOnlyElement(dataAccessLogic.createRelations(newRelation));
					relationship.instanceIds().add(aliasRegistry.getCMDBfId(relationId));
				}
			} else {
				boolean modified = false;
				for (final String key : newRelation.relationAttributeToValue.keySet()) {
					Object newVal = newRelation.relationAttributeToValue.get(key);
					final Object oldVal = relation.get(key);
					if (newVal instanceof String && ((String) newVal).isEmpty()) {
						newVal = null;
					}
					if (newVal != null) {
						modified |= !newVal.equals(oldVal);
					} else {
						modified |= oldVal != null;
					}
				}
				relationId = relation.getId();
				if (modified) {
					final DateTime relationDate = relation.getBeginDate();
					if (relationDate == null || recordLastModified == null
							|| !recordLastModified.isBefore(relationDate)) {
						newRelation.relationId = relation.getId();
						newRelation.addSourceCard(relation.getCard1Id(), relation.getType().getClass1().getIdentifier()
								.getLocalName());
						newRelation.addDestinationCard(relation.getCard2Id(), relation.getType().getClass2()
								.getIdentifier().getLocalName());
						dataAccessLogic.updateRelation(newRelation);
					} else {
						throw new RegistrationErrorFault("Out of date");
					}
				}
			}
		}
		if (relationId != null) {
			aliasRegistry.addAlias(relationId, relationship.instanceIds());
			relationship.instanceIds().addAll(aliasRegistry.getAlias(aliasRegistry.getCMDBfId(relationId)));
		}
	}

	private void deregisterItem(final MdrScopedIdType instanceId) throws Exception {
		final CMDBfId id = aliasRegistry.resolveAlias(instanceId);
		CMCard card = null;
		if (id != null) {
			for (final CMClass cmClass : dataAccessLogic.findActiveClasses()) {
				if (card == null && !cmClass.isSuperclass()) {
					card = Iterables.getOnlyElement(
							findCards(Arrays.asList(aliasRegistry.getInstanceId(id)), cmClass, null,
									new ArrayList<QName>()), null);
				}
			}
			if (card != null) {
				final String recordId = aliasRegistry.getRecordId(instanceId);
				if (recordId == null || recordId.startsWith(ENTRY_RECORDID_PREFIX)) {
					aliasRegistry.removeAlias(card);
					dataAccessLogic.deleteCard(card.getType().getIdentifier().getLocalName(), card.getId());
				} else if (recordId.startsWith(DOCUMENT_RECORDID_PREFIX)) {
					final String name = recordId.substring(DOCUMENT_RECORDID_PREFIX.length());
					dmsLogic.delete(card.getType().getIdentifier().getLocalName(), card.getId(), name);
				} else if (recordId.startsWith(GEO_RECORDID_PREFIX)) {
					final QName qname = xmlRegistry.getTypeQName(new GeoClass(card.getType().getIdentifier()
							.getLocalName()));
					final GeoClass geoClass = (GeoClass) xmlRegistry.getType(qname);
					final JSONObject jsonObject = new JSONObject();
					for (final LayerMetadata layer : geoClass.getLayers()) {
						jsonObject.put(layer.getName(), "");
					}
					gisLogic.updateFeatures(Card.newInstance(card.getType()).withId(card.getId()).build(),
							Collections.<String, Object> singletonMap("geoAttributes", jsonObject.toString()));
				}
			}
		}
		if (card == null) {
			throw new DeregistrationErrorFault("Not found");
		}
	}

	private void deregisterRelationship(final MdrScopedIdType instanceId) throws Exception {
		final CMDBfId id = aliasRegistry.resolveAlias(instanceId);
		CMRelation relation = null;
		if (id != null) {
			for (final CMDomain domain : dataAccessLogic.findActiveDomains()) {
				if (relation == null) {
					relation = Iterables.getOnlyElement(
							findRelations(Arrays.asList(aliasRegistry.getInstanceId(id)), null, null, domain, null,
									new ArrayList<QName>()), null);
				}
			}
			if (relation != null) {
				aliasRegistry.removeAlias(relation);
				dataAccessLogic.deleteRelation(relation.getType().getIdentifier().getLocalName(), relation.getId());
			}
		}
		if (relation == null) {
			throw new DeregistrationErrorFault("Not found");
		}
	}

	private Collection<CMDBfItem> getItems(final Set<CMDBfId> instanceId, final RecordConstraintType recordConstraint,
			final Map<Long, Long> typeMap) throws Exception {
		try {
			final List<CMClass> typeList = new ArrayList<CMClass>();
			final Map<String, GeoClass> geoTypes = new HashMap<String, GeoClass>();
			final Set<String> documentTypes = new HashSet<String>();
			if (recordConstraint != null) {
				for (final QNameType recordType : recordConstraint.getRecordType()) {
					final Object type = xmlRegistry.getType(new QName(recordType.getNamespace(), recordType
							.getLocalName()));
					if (type instanceof CMClass) {
						typeList.add((CMClass) type);
					} else if (type instanceof DocumentTypeDefinition) {
						documentTypes.add(((DocumentTypeDefinition) type).getName());
					} else if (type instanceof GeoClass) {
						final GeoClass geoClass = (GeoClass) type;
						geoTypes.put(geoClass.getName(), geoClass);
					}
				}
			}
			if (recordConstraint == null || recordConstraint.getRecordType().isEmpty()
					|| (typeList.isEmpty() && !documentTypes.isEmpty())) {
				for (final CMClass cmClass : dataAccessLogic.findActiveClasses()) {
					if (!cmClass.isSystem() && !cmClass.isSuperclass()) {
						typeList.add(cmClass);
					}
				}
			}

			final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Map<Long, List<Long>> idMap = buildIdMap(instanceId, typeMap);
			final List<CMDBfItem> instanceList = new ArrayList<CMDBfItem>();
			for (final CMClass type : typeList) {
				List<Long> idList = null;
				if (idMap != null) {
					idList = idMap.get(type.getId());
					final List<Long> unresolvedIdList = idMap.get(null);
					if (idList == null) {
						idList = unresolvedIdList;
					} else if (unresolvedIdList != null) {
						idList.addAll(unresolvedIdList);
					}
				}
				if (idMap == null || idList != null) {
					for (final CMCard card : findCards(idList, type,
							recordConstraint != null ? recordConstraint.getPropertyValue() : null,
							new ArrayList<QName>())) {
						boolean match = true;
						if (match && !documentTypes.isEmpty()) {
							match = false;
							for (final StoredDocument doc : dmsLogic.search(card.getType().getIdentifier()
									.getLocalName(), card.getId())) {
								match |= documentTypes.contains(doc.getCategory());
								if (match && !recordConstraint.getPropertyValue().isEmpty()) {
									final RecordType record = getRecord(aliasRegistry.getCMDBfId(card), doc, null, xml);
									final Map<QName, String> properties = CMDBfUtils.parseRecord(record);
									match &= Iterables.all(recordConstraint.getPropertyValue(),
											new Predicate<PropertyValueType>() {
												@Override
												public boolean apply(final PropertyValueType input) {
													return CMDBfUtils.filter(properties, input);
												}
											});
								}
							}
						}
						if (match && !geoTypes.isEmpty()) {
							match = false;
							final GeoClass geoClass = geoTypes.get(type.getIdentifier().getLocalName());
							if (geoClass != null) {
								final RecordType record = getRecord(aliasRegistry.getCMDBfId(card), card, geoClass, xml);
								match = record != null;
								if (match && !recordConstraint.getPropertyValue().isEmpty()) {
									final Map<QName, String> properties = CMDBfUtils.parseRecord(record);
									match &= Iterables.all(recordConstraint.getPropertyValue(),
											new Predicate<PropertyValueType>() {
												@Override
												public boolean apply(final PropertyValueType input) {
													return CMDBfUtils.filter(properties, input);
												}
											});
								}
							}
						}
						if (match) {
							instanceList.add(getCMDBfItem(card));
							typeMap.put(card.getId(), card.getType().getId());
						}
					}
				}
			}

			return instanceList;
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	private Collection<CMDBfRelationship> getRelationships(final Set<CMDBfId> instanceId, final Set<CMDBfId> source,
			final Set<CMDBfId> target, final RecordConstraintType recordConstraint, final Map<Long, Long> typeMap) {
		final List<CMDomain> domainList = new ArrayList<CMDomain>();
		if (recordConstraint == null || recordConstraint.getRecordType().isEmpty()) {
			for (final CMDomain domain : dataAccessLogic.findActiveDomains()) {
				if (!domain.isSystem()) {
					domainList.add(domain);
				}
			}
		} else {
			for (final QNameType recordType : recordConstraint.getRecordType()) {
				final Object type = xmlRegistry
						.getType(new QName(recordType.getNamespace(), recordType.getLocalName()));
				if (type instanceof CMDomain) {
					domainList.add((CMDomain) type);
				}
			}
		}

		final Map<Long, List<Long>> idMap = buildIdMap(instanceId, typeMap);
		final List<CMDBfRelationship> relationshipList = new ArrayList<CMDBfRelationship>();
		for (final CMDomain type : domainList) {
			List<Long> idList = null;
			if (idMap != null) {
				idList = idMap.get(type.getId());
				final List<Long> unresolvedIdList = idMap.get(null);
				if (idList == null) {
					idList = unresolvedIdList;
				} else if (unresolvedIdList != null) {
					idList.addAll(unresolvedIdList);
				}
			}
			if (idMap == null || idList != null) {
				for (final CMRelation relation : findRelations(idList, getIdList(source), getIdList(target), type,
						recordConstraint != null ? recordConstraint.getPropertyValue() : null, new ArrayList<QName>())) {
					relationshipList.add(getCMDBfRelationship(relation));
					typeMap.put(relation.getId(), relation.getType().getId());
				}
			}
		}
		return relationshipList;
	}

	private void fetchItemRecords(final ItemSet<CMDBfItem> items, final ContentSelectorType contentSelector,
			final Map<Long, Long> typeMap) {
		try {
			final Map<Long, List<Long>> idMap = buildIdMap(items.idSet(), typeMap);

			Map<QName, Set<QName>> propertyMap = null;
			if (contentSelector != null) {
				propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
			}

			final Set<String> documentTypes = new HashSet<String>();
			final HashMap<String, GeoClass> geoTypes = new HashMap<String, GeoClass>();
			if (propertyMap != null) {
				for (final QName qname : propertyMap.keySet()) {
					if (qname.getNamespaceURI() != null) {
						final Object type = xmlRegistry.getType(qname);
						if (type instanceof DocumentTypeDefinition) {
							documentTypes.add(((DocumentTypeDefinition) type).getName());
						} else if (type instanceof GeoClass) {
							final GeoClass geoClass = (GeoClass) type;
							geoTypes.put(geoClass.getName(), geoClass);
						}
					}
				}
			}

			final ContentSelectorFunction contentSelectorFunction = new ContentSelectorFunction(contentSelector);
			final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			for (final Long typeId : idMap.keySet()) {
				if (typeId != null) {
					final CMClass type = dataAccessLogic.findClass(typeId);
					final Collection<QName> properties = getTypeProperties(type, propertyMap);
					if (propertyMap == null || properties != null) {
						for (final CMCard card : findCards(idMap.get(typeId), type, null, properties)) {
							final CMDBfItem item = items.get(aliasRegistry.getCMDBfId(card));
							item.records().add(getRecord(card, xml));
						}
					}
					if (dmsConfiguration.isEnabled()) {
						if (!documentTypes.isEmpty()) {
							for (final Long cardId : idMap.get(typeId)) {
								for (final StoredDocument document : dmsLogic.search(type.getIdentifier()
										.getLocalName(), cardId)) {
									if (documentTypes.contains(document.getCategory())) {
										final DataHandler dataHandler = dmsLogic.download(type.getIdentifier()
												.getLocalName(), cardId, document.getName());
										final CMDBfId id = aliasRegistry.getCMDBfId(cardId);
										final CMDBfItem item = items.get(id);
										final RecordType record = getRecord(id, document, dataHandler.getInputStream(),
												xml);
										item.records().add(contentSelectorFunction.apply(record));
									}
								}
							}
						}
					}
					if (gisLogic.isGisEnabled()) {
						final GeoClass geoClass = geoTypes.get(type.getIdentifier().getLocalName());
						if (geoClass != null) {
							for (final CMCard card : findCards(idMap.get(typeId), type, null, null)) {
								final CMDBfId id = aliasRegistry.getCMDBfId(card);
								final CMDBfItem item = items.get(id);
								final RecordType record = getRecord(id, card, geoClass, xml);
								if (record != null) {
									item.records().add(contentSelectorFunction.apply(record));
								}
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			throw new Error(e);
		}
	}

	private void fetchRelationshipRecords(final PathSet relationships, final ContentSelectorType contentSelector,
			final Map<Long, Long> typeMap) {
		try {
			final Map<Long, List<Long>> idMap = buildIdMap(relationships.idSet(), typeMap);

			Map<QName, Set<QName>> propertyMap = null;
			if (contentSelector != null) {
				propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
			}
			final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			for (final Long typeId : idMap.keySet()) {
				if (typeId != null) {
					final CMDomain type = dataAccessLogic.findDomain(typeId);
					final Collection<QName> properties = getTypeProperties(type, propertyMap);
					if (propertyMap == null || properties != null) {
						for (final CMRelation relation : findRelations(idMap.get(typeId), null, null, type, null,
								properties)) {
							final CMDBfItem item = relationships.get(aliasRegistry.getCMDBfId(relation));
							item.records().add(getRecord(relation, xml));
						}
					}
				}
			}
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	private Collection<CMCard> findCards(final Collection<Long> instanceId, final CMClass type,
			final Collection<PropertyValueType> filters, final Collection<QName> properties) {
		final List<CMCard> cardList = new ArrayList<CMCard>();

		final List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		if (properties != null && !properties.contains(new QName(""))) {
			for (final QName property : properties) {
				if (type.getAttribute(property.getLocalPart()) != null) {
					attributes.add(attribute(type, property.getLocalPart()));
				}
			}
		} else {
			attributes.add(anyAttribute(type));
		}

		boolean isSatisfiable = true;
		final List<WhereClause> conditions = new ArrayList<WhereClause>();
		if (instanceId != null) {
			isSatisfiable = applyIdFilter(attribute(type, Constants.ID_ATTRIBUTE), instanceId, conditions);
		}
		if (filters != null) {
			isSatisfiable &= applyPropertyFilter(type, filters, conditions);
		}
		if (isSatisfiable) {
			final QuerySpecsBuilder queryBuilder = dataAccessLogic.getView().select(attributes.toArray()).from(type);
			if (!conditions.isEmpty()) {
				if (conditions.size() == 1) {
					queryBuilder.where(conditions.get(0));
				} else if (conditions.size() == 2) {
					queryBuilder.where(and(conditions.get(0), conditions.get(1)));
				} else {
					queryBuilder.where(and(conditions.get(0), conditions.get(1),
							conditions.subList(2, conditions.size()).toArray(new WhereClause[0])));
				}
			} else {
				queryBuilder.where(trueWhereClause());
			}
			for (final CMQueryRow row : queryBuilder.run()) {
				final CMCard card = row.getCard(type);
				cardList.add(card);
			}
		}
		return cardList;
	}

	private Collection<CMRelation> findRelations(final Collection<Long> instanceId, final Collection<Long> source,
			final Collection<Long> target, final CMDomain type, final Collection<PropertyValueType> filters,
			final Collection<QName> properties) {
		final List<CMRelation> relationList = new ArrayList<CMRelation>();

		final List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		if (properties != null && !properties.contains(new QName(""))) {
			for (final QName property : properties) {
				if (type.getAttribute(property.getLocalPart()) != null) {
					attributes.add(attribute(type, property.getLocalPart()));
				}
			}
		} else {
			attributes.add(anyAttribute(type));
		}

		boolean isSatisfiable = true;
		final List<WhereClause> conditions = new ArrayList<WhereClause>();
		conditions.add(condition(attribute(type, "_Src"), eq(Source._1.name())));

		if (source != null) {
			// isSatisfiable = applyIdFilter(attribute(SOURCE_ALIAS,
			// Constants.ID_ATTRIBUTE), source, conditions);
			isSatisfiable = applyIdFilter(attribute(type.getClass1(), Constants.ID_ATTRIBUTE), source, conditions);
		}
		if (target != null) {
			isSatisfiable &= applyIdFilter(attribute(TARGET_ALIAS, Constants.ID_ATTRIBUTE), target, conditions);
		}
		if (instanceId != null) {
			isSatisfiable &= applyIdFilter(attribute(type, Constants.ID_ATTRIBUTE), instanceId, conditions);
		}
		if (filters != null) {
			isSatisfiable &= applyPropertyFilter(type, filters, conditions);
		}
		if (isSatisfiable) {
			final QuerySpecsBuilder queryBuilder = dataAccessLogic.getView().select(attributes.toArray());
			// queryBuilder.from(type.getClass1(), SOURCE_ALIAS);
			queryBuilder.from(type.getClass1());
			queryBuilder.join(type.getClass2(), TARGET_ALIAS, over(type));
			if (!conditions.isEmpty()) {
				if (conditions.size() == 1) {
					queryBuilder.where(conditions.get(0));
				} else if (conditions.size() == 2) {
					queryBuilder.where(and(conditions.get(0), conditions.get(1)));
				} else {
					queryBuilder.where(and(conditions.get(0), conditions.get(1),
							conditions.subList(2, conditions.size()).toArray(new WhereClause[0])));
				}
			} else {
				queryBuilder.where(trueWhereClause());
			}
			for (final CMQueryRow row : queryBuilder.run()) {
				final CMRelation relation = row.getRelation(type).getRelation();
				relationList.add(relation);
			}
		}
		return relationList;
	}

	private CMDBfItem getCMDBfItem(final CMCard element) {
		return new CMDBfItem(aliasRegistry.getCMDBfId(element));
	}

	private CMDBfRelationship getCMDBfRelationship(final CMRelation relation) {
		return new CMDBfRelationship(aliasRegistry.getCMDBfId(relation),
				aliasRegistry.getCMDBfId(relation.getCard1Id()), aliasRegistry.getCMDBfId(relation.getCard2Id()));
	}

	private RecordType getRecord(final CMEntry element, final Document xml) {
		try {
			final DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, element);
			final Element xmlElement = (Element) root.getFirstChild();
			final RecordMetadata recordMetadata = new RecordMetadata();
			final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(element,
					ENTRY_RECORDID_PREFIX + element.getType().getIdentifier().getLocalName()).getLocalId());
			final GregorianCalendar calendar = element.getBeginDate().toGregorianCalendar();
			recordMetadata.setLastModified(datatypeFactory.newXMLGregorianCalendar(calendar));
			final RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} catch (final DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}

	private RecordType getRecord(final CMDBfId id, final StoredDocument document, final InputStream inputStream,
			final Document xml) {
		try {
			final DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, new DmsDocument(document, inputStream));
			final Element xmlElement = (Element) root.getFirstChild();
			final RecordMetadata recordMetadata = new RecordMetadata();
			final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(id, DOCUMENT_RECORDID_PREFIX + document.getName())
					.getLocalId());
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(document.getCreated());
			recordMetadata.setLastModified(datatypeFactory.newXMLGregorianCalendar(calendar));
			final RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} catch (final DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}

	private RecordType getRecord(final CMDBfId id, final CMCard card, final GeoClass geoClass, final Document xml)
			throws Exception {
		final GeoCard geoCard = new GeoCard(geoClass);
		final Card masterCard = Card.newInstance(card.getType()).withId(card.getId()).build();
		for (final LayerMetadata layer : geoClass.getLayers()) {
			final GeoFeature feature = geoFeatureStore.readGeoFeature(layer, masterCard);
			if (feature != null) {
				geoCard.set(layer.getName(), feature.getGeometry());
			}
		}
		if (!geoCard.isEmpty()) {
			final DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, geoCard);
			final Element xmlElement = (Element) root.getFirstChild();
			final RecordMetadata recordMetadata = new RecordMetadata();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(id, GEO_RECORDID_PREFIX + geoCard.getType().getName())
					.getLocalId());
			final RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} else {
			return null;
		}
	}

	private boolean applyIdFilter(final QueryAliasAttribute attribute, final Collection<Long> idList,
			final List<WhereClause> conditions) {
		boolean isSatisfiable = true;
		if (idList.isEmpty()) {
			isSatisfiable = false;
		} else {
			conditions.add(condition(attribute, in(idList.toArray())));
		}
		return isSatisfiable;
	}

	private boolean applyPropertyFilter(final CMEntryType type, final Collection<PropertyValueType> propertyValueList,
			final List<WhereClause> conditions) {
		boolean isSatisfiable = true;
		final Iterator<PropertyValueType> iterator = propertyValueList.iterator();
		while (isSatisfiable && iterator.hasNext()) {
			final PropertyValueType propertyValue = iterator.next();

			final CMAttribute attribute = type.getAttribute(propertyValue.getLocalName());
			if (attribute != null) {
				final List<WhereClause> expressions = new ArrayList<WhereClause>();
				if (propertyValue.getEqual() != null) {
					for (final EqualOperatorType operator : propertyValue.getEqual()) {
						WhereClause filter = condition(attribute(type, attribute.getName()), eq(operator.getValue()));
						if (operator.isNegate()) {
							filter = not(filter);
						}
						expressions.add(filter);
					}
				}
				if (propertyValue.getLess() != null) {
					final ComparisonOperatorType operator = propertyValue.getLess();
					WhereClause filter = condition(attribute(type, attribute.getName()), lt(operator.getValue()));
					if (operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getLessOrEqual() != null) {
					final ComparisonOperatorType operator = propertyValue.getLessOrEqual();
					WhereClause filter = condition(attribute(type, attribute.getName()), gt(operator.getValue()));
					if (!operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getGreater() != null) {
					final ComparisonOperatorType operator = propertyValue.getGreater();
					WhereClause filter = condition(attribute(type, attribute.getName()), gt(operator.getValue()));
					if (operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getGreaterOrEqual() != null) {
					final ComparisonOperatorType operator = propertyValue.getGreater();
					WhereClause filter = condition(attribute(type, attribute.getName()), lt(operator.getValue()));
					if (!operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getContains() != null) {
					for (final StringOperatorType operator : propertyValue.getContains()) {
						WhereClause filter = condition(attribute(type, attribute.getName()),
								contains(operator.getValue()));
						if (operator.isNegate()) {
							filter = not(filter);
						}
						expressions.add(filter);
					}
				}
				if (propertyValue.getLike() != null) {
					for (final StringOperatorType operator : propertyValue.getLike()) {
						WhereClause filter = null;
						if (operator.getValue().startsWith("%") && operator.getValue().endsWith("%")) {
							filter = condition(attribute(type, attribute.getName()), contains(operator.getValue()
									.substring(1, operator.getValue().length() - 1)));
						} else if (operator.getValue().startsWith("%")) {
							filter = condition(attribute(type, attribute.getName()), endsWith(operator.getValue()
									.substring(1)));
						} else if (operator.getValue().endsWith("%")) {
							filter = condition(attribute(type, attribute.getName()), beginsWith(operator.getValue()
									.substring(0, operator.getValue().length() - 1)));
						} else {
							filter = condition(attribute(type, attribute.getName()), contains(operator.getValue()));
						}
						if (operator.isNegate()) {
							filter = not(filter);
						}
						expressions.add(filter);
					}
				}
				if (propertyValue.getIsNull() != null) {
					final NullOperatorType operator = propertyValue.getIsNull();
					WhereClause filter = condition(attribute(type, attribute.getName()), isNull());
					if (operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (!expressions.isEmpty()) {
					WhereClause propertyFilter = null;
					if (propertyValue.isMatchAny()) {
						if (expressions.size() == 1) {
							propertyFilter = expressions.get(0);
						} else if (expressions.size() == 2) {
							propertyFilter = or(expressions.get(0), expressions.get(1));
						} else {
							propertyFilter = or(expressions.get(0), expressions.get(1),
									expressions.subList(2, expressions.size()).toArray(new WhereClause[0]));
						}
					} else {
						if (expressions.size() == 1) {
							propertyFilter = expressions.get(0);
						} else if (expressions.size() == 2) {
							propertyFilter = and(expressions.get(0), expressions.get(1));
						} else {
							propertyFilter = and(expressions.get(0), expressions.get(1),
									expressions.subList(2, expressions.size()).toArray(new WhereClause[0]));
						}
					}
					conditions.add(propertyFilter);
				}
			} else {
				isSatisfiable = false;
			}
		}
		return isSatisfiable;
	}

	private Collection<Long> getIdList(final Iterable<? extends MdrScopedIdType> instanceId) {
		Collection<Long> idSet = null;
		if (instanceId != null) {
			idSet = new HashSet<Long>();
			for (final MdrScopedIdType id : instanceId) {
				if (aliasRegistry.isLocal(id)) {
					idSet.add(aliasRegistry.getInstanceId(id));
				}
			}
		}
		return idSet;
	}

	private Map<Long, List<Long>> buildIdMap(final Iterable<? extends MdrScopedIdType> instanceId,
			final Map<Long, Long> typeMap) {
		Map<Long, List<Long>> idMap = null;
		if (instanceId != null) {
			idMap = new HashMap<Long, List<Long>>();
			final Collection<Long> idSet = getIdList(instanceId);
			for (final Long id : idSet) {
				final Long typeId = typeMap.get(id);
				List<Long> idList = idMap.get(typeId);
				if (idList == null) {
					idList = new ArrayList<Long>();
					idMap.put(typeId, idList);
				}
				idList.add(id);
			}
		}
		return idMap;
	}

	private Collection<QName> getTypeProperties(CMEntryType type, final Map<QName, Set<QName>> propertyMap) {
		Set<QName> properties = null;
		if (propertyMap != null) {
			if (propertyMap.containsKey(new QName(""))) {
				if (properties == null) {
					properties = new HashSet<QName>();
				}
				for (final QName property : propertyMap.get(new QName(""))) {
					properties.add(property);
				}
			}
			while (type != null) {
				final Set<QName> propertySet = propertyMap.get(xmlRegistry.getTypeQName(type));
				if (propertySet != null) {
					if (properties == null) {
						properties = new HashSet<QName>();
					}
					for (final QName property : propertySet) {
						properties.add(property);
					}
				}
				if (type instanceof CMClass) {
					type = ((CMClass) type).getParent();
				} else {
					type = null;
				}
			}
		}
		return properties;
	}
}
