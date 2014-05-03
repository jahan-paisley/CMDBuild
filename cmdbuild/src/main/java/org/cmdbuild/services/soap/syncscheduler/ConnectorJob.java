package org.cmdbuild.services.soap.syncscheduler;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axis.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.data.Card.CardBuilder;
import org.cmdbuild.services.soap.connector.DomainDirection;
import org.dom4j.Element;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ConnectorJob implements Runnable {

	private Action action;
	private boolean isMaster; // id of the master card
	private Long masterCardId; // id of the master card
	private String masterClassName; // name of the class of the master class
	private Long detailCardId; // id of the detail card
	private String detailClassName; // name of the class of the detail class
	private String domainName; // name of the domain between master and detail
	private DomainDirection domainDirection;
	private boolean isShared; // relation 1:N -> 1 detail : N master
	private List<String> sharedIds; // the "id" used to search for the details
	private Element elementCard;
	private final int jobNumber;
	private static int jobNumberCounter = 0;
	private final CMDataView view;
	private final DataAccessLogic dataAccessLogic;
	private final LookupStore lookupStore;

	public ConnectorJob(final CMDataView view, final DataAccessLogic dataAccessLogic, final LookupStore lookupStore) {
		this.dataAccessLogic = dataAccessLogic;
		this.lookupStore = lookupStore;
		this.view = view;
		jobNumber = ++jobNumberCounter;
	}

	private final Map<String, String> referenceToMaster = new HashMap<String, String>();

	public enum Action {
		CREATE("create"), UPDATE("update"), DELETE("delete");

		private final String action;

		Action(final String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}

		public static Action getAction(final String action) throws Exception {
			if (Action.CREATE.getAction().equals(action)) {
				return Action.CREATE;
			} else {
				if (Action.UPDATE.getAction().equals(action)) {
					return Action.UPDATE;
				} else if (Action.DELETE.getAction().equals(action)) {
					return Action.DELETE;
				}
			}
			throw new Exception();
		}
	}

	@Override
	public void run() {
		try {
			if (action != null) {
				switch (action) {
				case CREATE:
					Log.SOAP.info("ExternalSync - create job started [" + jobNumber + "]");
					create();
					break;
				case DELETE:
					Log.SOAP.info("ExternalSync - delete job started [" + jobNumber + "]");
					delete();
					break;
				case UPDATE:
					Log.SOAP.info("ExternalSync - update job started [" + jobNumber + "]");
					update();
					break;
				default:
					throw new Exception("No action selected");
				}
			} else {
				Log.SOAP.info("External Sync - running the current process has failed, try to star the next job");
				throw new Exception("No action selected");
			}
		} catch (final Exception e) {
			Log.SOAP.info("External Sync - running the current process has failed, try to start the next job " + e);
		}
	}

	/** GETTER / SETTER **/
	public void setIsMaster(final boolean isMaster) {
		this.isMaster = isMaster;
	}

	public void setMasterClassName(final String name) {
		this.masterClassName = name;
	}

	public void setMasterCardId(final Long id) {
		this.masterCardId = id;
	}

	public void setDetailClassName(final String name) {
		this.detailClassName = name;
	}

	public void setDetailCardId(final Long id) {
		this.detailCardId = id;
	}

	public void setElementCard(final Element element) {
		this.elementCard = element;
	}

	public void setDomainName(final String name) {
		this.domainName = name;
	}

	public void setAction(final Action action) {
		this.action = action;
	}

	public void setDomainDirection(final DomainDirection domainDirection) {
		this.domainDirection = domainDirection;
	}

	public void setIsShared(final boolean isShared) {
		this.isShared = isShared;
	}

	public void setDetailIdentifiers(final List<String> identifiers) {
		this.sharedIds = identifiers;
	}

	/**
	 * @throws AxisFault
	 **/

	private void create() throws Exception {
		if (isMaster) {
			createCard();
		} else {
			if (this.masterCardId > 0) {
				if (detailHasReferenceToMaster()) {
					final String referenceName = referenceToMaster.get(detailClassName);
					detailCardId = createCard(referenceName);
				} else {
					detailCardId = createCard();
					if (detailCardId > 0)
						createRelation();
				}
			} else
				throw new Exception("MasterCardId is 0");
		}
	}

	private void update() {
		updateCard();
		Log.SOAP.info("ExternalSync - end update card");

	}

	private void delete() throws Exception {
		if (isMaster) {
			deleteCard();
		} else {
			if (this.masterCardId > 0) {
				if ((!this.isShared) || (this.isShared && isLastSharedDetail())) {
					deleteCard();
					Log.SOAP.info("ExternalSync - deleting card ");
				} else {
					Log.SOAP.info("ExternalSync - card detail is shared and has other relations - cannot delete! ");
				}
				if (!detailHasReferenceToMaster())
					deleteRelation();
			} else
				throw new Exception("MasterCardId is 0");
		}
	}

	private boolean isLastSharedDetail() {
		final Card cardDetail = dataAccessLogic.fetchCard(detailClassName, detailCardId);
		final CMDomain domain = view.findDomain(domainName);
		final DomainWithSource domWithSource;
		if (domain.getClass1().getIdentifier().getLocalName().equals(detailClassName)) {
			domWithSource = DomainWithSource.create(domain.getId(), Source._1.toString());
		} else {
			domWithSource = DomainWithSource.create(domain.getId(), Source._2.toString());
		}
		final GetRelationListResponse response = dataAccessLogic.getRelationList(cardDetail, domWithSource);
		return response.getTotalNumberOfRelations() > 0;
	}

	/**********************
	 ** CARD MANAGEMENT **
	 **********************/
	private Long createCard() {
		return createCard("");
	}

	private Long createCard(final String referenceName) {
		if (isShared) {
			// searching for an existent object
			final CMClass detailClass = view.findClass(detailClassName);
			final QuerySpecsBuilder querySpecsBuilder = view.select(anyAttribute(detailClass)) //
					.from(detailClass);
			final List<WhereClause> whereClauses = Lists.newArrayList();
			for (final String attributeName : sharedIds) {
				final String attributeValue = searchAttributeValue(attributeName);
				whereClauses.add(condition(attribute(detailClass, attributeName), eq(attributeValue)));
			}
			final WhereClause globalWhereClause = createWhereClauseFrom(whereClauses);
			final CMQueryResult result = querySpecsBuilder.where(globalWhereClause).run();
			final boolean existingCard = Iterables.size(result) > 0;
			if (existingCard) {
				final CMQueryRow row = result.iterator().next();
				return row.getCard(detailClass).getId();
			}
		}
		// detail does not exist, must be inserted
		Log.SOAP.info("ExternalSync - insert a new card [class:" + this.detailClassName + "]");
		try {
			final CMClass fetchedClass = view.findClass(detailClassName);
			if (fetchedClass == null) {
				Log.SOAP.info("The class " + fetchedClass.getName()
						+ " does not exist or the user does not have the privileges to read it");
				throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
			}
			final CardBuilder cardBuilder = Card.newInstance(fetchedClass);
			setCardValues(fetchedClass, cardBuilder);
			if (!referenceName.equals(StringUtils.EMPTY)) {
				Log.SOAP.info("ExternalSync - the card [class:" + this.detailClassName
						+ "] has a reference to the card-master");
				cardBuilder.withAttribute(referenceName, masterCardId);
			}
			final Card cardToCreate = cardBuilder.build();
			dataAccessLogic.createCard(cardToCreate);
			return cardToCreate.getId();
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new card", e);
		}
		return 0L;
	}

	private WhereClause createWhereClauseFrom(final List<WhereClause> whereClauses) {
		if (whereClauses.isEmpty()) {
			return TrueWhereClause.trueWhereClause();
		} else if (whereClauses.size() == 1) {
			return whereClauses.get(0);
		} else if (whereClauses.size() == 2) {
			return and(whereClauses.get(0), whereClauses.get(1));
		} else {
			return and(whereClauses.get(0), whereClauses.get(1),
					whereClauses.subList(2, whereClauses.size()).toArray(new WhereClause[whereClauses.size() - 2]));
		}
	}

	@SuppressWarnings("unchecked")
	private void setCardValues(final CMClass cmClass, final CardBuilder cardBuilder) {
		final Iterator<Element> attributeIterator = elementCard.elementIterator();
		while (attributeIterator.hasNext()) {
			final Element cardAttribute = attributeIterator.next();
			final String attributeName = cardAttribute.getName();
			final String attributeValue = cardAttribute.getText();
			final CMAttribute attribute = cmClass.getAttribute(attributeName);
			if (attribute == null) {
				continue;
			}
			if (attribute.getType() instanceof ReferenceAttributeType) {
				addReferenceAttributeTo(attribute, cardBuilder, attributeValue);
			} else if (attribute.getType() instanceof LookupAttributeType) {
				addLookupAttributeTo(attribute, cardBuilder, attributeValue);
			} else {
				cardBuilder.withAttribute(attributeName, attributeValue);
			}
		}
	}

	private CMClass referencedClass(final CMDomain domain, final CMClass sourceClass) {
		if (domain.getClass1().getName().equals(sourceClass.getName())) {
			return domain.getClass2();
		} else {
			return domain.getClass1();
		}
	}

	private void addReferenceAttributeTo(final CMAttribute attribute, final CardBuilder cardBuilder,
			final String attributeValue) {
		final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType) attribute.getType();
		if (!attributeValue.equals(StringUtils.EMPTY)) {
			final String domainName = referenceAttributeType.getDomainName();
			final CMDomain referenceDomain = view.findDomain(domainName);
			final CMClass referencedClass = referencedClass(referenceDomain, (CMClass) attribute.getOwner());
			final CMQueryResult result = view.select(anyAttribute(referencedClass)) //
					.from(referencedClass) //
					.where(condition(attribute(referencedClass, "Description"), eq(attributeValue))) //
					.limit(1) //
					.run();
			if (result.size() > 0) {
				final Long referencedCardId = result.getOnlyRow().getCard(referencedClass).getId();
				cardBuilder.withAttribute(attribute.getName(), referencedCardId);
			}
		}
	}

	private void addLookupAttributeTo(final CMAttribute attribute, final CardBuilder cardBuilder,
			final String attributeValue) {
		final LookupAttributeType lookupAttributeType = (LookupAttributeType) attribute.getType();
		final String lookupTypeName = lookupAttributeType.getLookupTypeName();
		for (final Lookup lookupDto : lookupStore.listForType(LookupType.newInstance() //
				.withName(lookupTypeName) //
				.build())) {
				if (lookupDto.description.equals(attributeValue)) {
					cardBuilder.withAttribute(attribute.getName(), lookupDto.getId());
				}
		}
	}

	/**
	 * params className the classname of the card to update params cardId the id
	 * of the card to update params elementCard the xml Element containing all
	 * values to insert
	 */
	private void updateCard() {
		Log.SOAP.info("ExternalSync - update card [id:" + this.detailCardId + " classname: " + this.detailClassName
				+ "]");
		try {

			if (this.detailCardId > 0) {
				final Card cardToUpdate = dataAccessLogic.fetchCard(detailClassName, detailCardId);

				Log.SOAP.info("ExternalSync - set card fields [id:" + this.detailCardId + " classname: "
						+ this.detailClassName + "]");

				final CardBuilder cardBuilder = Card.newInstance().clone(cardToUpdate);
				setCardValues(view.findClass(detailClassName), cardBuilder);

				Log.SOAP.info("ExternalSync - end set card fields [id:" + this.detailCardId + " classname: "
						+ this.detailClassName + "]");

				dataAccessLogic.updateCard(cardBuilder.build());

				Log.SOAP.info("ExternalSync - end save card [id:" + this.detailCardId + " classname: "
						+ this.detailClassName + "]");
			} else {
				Log.SOAP.warn("ExternalSync - required an update of card with cardId " + this.detailCardId
						+ " (classname " + this.detailClassName + ")");
			}
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while updating card [id:" + this.detailCardId
					+ " classname: " + this.detailClassName + "]", e);
		}
	}

	/**
	 * params className the classname of the card to update params cardId the id
	 * of the card to update params elementCard the xml Element containg all
	 * values to insert
	 */
	private int deleteCard() {
		Log.SOAP.info("ExternalSync - delete card [id:" + this.detailCardId + " classname: " + this.detailClassName
				+ "]");
		try {
			dataAccessLogic.deleteCard(detailClassName, detailCardId);
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while deleting card [id:" + this.detailCardId
					+ " classname: " + this.detailClassName + "]", e);
		}
		return 0;
	}

	/****************************
	 *** RELATION MANAGEMENT ***
	 ****************************/

	private void createRelation() {

		Log.SOAP.info("ExternalSync - create new relation between " + "card [id:" + this.masterCardId + " classname: "
				+ this.masterClassName + "] and " + "card [id:" + detailCardId + " classname: " + detailClassName
				+ "] " + "on domain: " + domainName);
		try {

			final RelationDTO relationToCreate = new RelationDTO();
			relationToCreate.relationId = null;
			relationToCreate.domainName = domainName;
			if (DomainDirection.DIRECT.equals(this.domainDirection)) {
				relationToCreate.master = Source._1.toString();
				relationToCreate.addSourceCard(masterCardId, masterClassName);
				relationToCreate.addDestinationCard(detailCardId, detailClassName);
			} else {
				relationToCreate.master = Source._2.toString();
				relationToCreate.addSourceCard(detailCardId, detailClassName);
				relationToCreate.addDestinationCard(masterCardId, masterClassName);
			}

			dataAccessLogic.createRelations(relationToCreate);
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new relation", e);
			Log.SOAP.debug("Exception parameters" + e.getExceptionParameters());
		}
	}

	private void deleteRelation() {
		Log.SOAP.info("ExternalSync - deleting relation between " + "card [id:" + this.masterCardId + " classname: "
				+ this.masterClassName + "] and " + "card [id:" + detailCardId + " classname: " + this.detailClassName
				+ "] " + "on domain: " + domainName);
		try {
			final CMDomain domain = view.findDomain(domainName);
			dataAccessLogic.deleteRelation(masterClassName, masterCardId, detailClassName, detailCardId, domain);
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new relation", e);
		}
	}

	private boolean detailHasReferenceToMaster() {
		if (referenceToMaster.containsKey(this.detailClassName)) {
			final String referenceName = referenceToMaster.get(this.detailClassName);
			return referenceName != null && !referenceName.equals("");
		} else {
			final CMClass detailClass = view.findClass(detailClassName);
			for (final CMAttribute attribute : detailClass.getAttributes()) {
				if (attribute.getType() instanceof ReferenceAttributeType) {
					final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType) attribute.getType();
					final String referencedDomainName = referenceAttributeType.getDomainName();
					if (referencedDomainName.equals(domainName)) {
						referenceToMaster.put(this.detailClassName, attribute.getName());
						return true;
					}
				}
			}
			referenceToMaster.put(domainName, StringUtils.EMPTY);
			return false;
		}
	}

	@SuppressWarnings(value = { "unchecked" })
	private String searchAttributeValue(final String attributeName) {
		final Iterator<Element> attributeIterator = elementCard.elementIterator();
		while (attributeIterator.hasNext()) {
			final Element cardAttribute = attributeIterator.next();
			if (cardAttribute.getName().equals(attributeName)) {
				return cardAttribute.getText();
			}
		}
		return "";
	}
}
