package org.cmdbuild.logic.data.access;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.json.JSONException;

/**
 * Business Logic Layer for Data Access
 */
public interface DataAccessLogic extends Logic {

	CMDataView getView();

	Map<Object, List<RelationInfo>> relationsBySource(String sourceTypeName, DomainWithSource dom);

	GetRelationListResponse getRelationList(Card srcCard, DomainWithSource dom, QueryOptions options);

	GetRelationListResponse getRelationList(Card srcCard, DomainWithSource dom);

	GetRelationListResponse getRelationListEmptyForWrongId(Card srcCard, DomainWithSource dom);

	GetRelationHistoryResponse getRelationHistory(Card srcCard);

	GetRelationHistoryResponse getRelationHistory(Card srcCard, CMDomain domain);

	CMRelation getRelation(final Long srcCardId, final Long dstCardId, final CMDomain domain,
			final CMClass sourceClass, final CMClass destinationClass);

	GetCardHistoryResponse getCardHistory(Card srcCard);

	CMClass findClass(Long classId);

	CMClass findClass(String className);

	CMDomain findDomain(Long domainId);

	CMDomain findDomain(String domainName);

	/**
	 * 
	 * @return only active classes (all classes, included superclasses, simple
	 *         classes and process classes).
	 */
	Iterable<? extends CMClass> findActiveClasses();

	/**
	 * 
	 * @return active and non active domains
	 */
	Iterable<? extends CMDomain> findAllDomains();

	/**
	 * 
	 * @return only active domains
	 */
	Iterable<? extends CMDomain> findActiveDomains();

	Iterable<? extends CMDomain> findReferenceableDomains(String className);

	/**
	 * 
	 * @return active and non active classes
	 */
	Iterable<? extends CMClass> findAllClasses();

	/**
	 * Fetches the card with the specified Id from the class with the specified
	 * name
	 * 
	 * @param className
	 * @param cardId
	 * @throws NoSuchElementException
	 *             if the card with the specified Id number does not exist or it
	 *             is not unique
	 * @return the card with the specified Id.
	 */
	Card fetchCard(String className, Long cardId);

	CMCard fetchCMCard(String className, Long cardId);

	Card fetchCardShort(String className, Long cardId, QueryOptions queryOptions);

	Card fetchCard(Long classId, Long cardId);

	/**
	 * Retrieve the cards of a given class that matches the given query options
	 * 
	 * @param className
	 * @param queryOptions
	 * @return a FetchCardListResponse
	 */
	FetchCardListResponse fetchCards(String className, QueryOptions queryOptions);

	/**
	 * Execute a given SQL function to select a set of rows Return these rows as
	 * fake cards
	 * 
	 * @param functionName
	 * @param queryOptions
	 * @return
	 */
	FetchCardListResponse fetchSQLCards(String functionName, QueryOptions queryOptions);

	/**
	 * 
	 * @param className
	 * @param cardId
	 * @param queryOptions
	 * @return a long (zero based) with the position of this card in relation of
	 *         current sorting and filter
	 */
	CMCardWithPosition getCardPosition(String className, Long cardId, QueryOptions queryOptions);

	/**
	 * Call createCard forwarding the given card, and saying to manage also the
	 * attributes over references domains
	 * 
	 * @param card
	 * @return
	 */
	Long createCard(Card card);

	/**
	 * 
	 * @param userGivenCard
	 * @param manageAlsoDomainsAttributes
	 *            if true iterate over the attributes to extract the ones with
	 *            type ReferenceAttributeType. For that attributes fetch the
	 *            relation and update the attributes if present in the
	 *            userGivenCard
	 * @return
	 */
	Long createCard(Card userGivenCard, boolean manageAlsoDomainsAttributes);

	void updateCard(Card card);

	void updateFetchedCard(Card card, Map<String, Object> attributes);

	void deleteCard(String className, Long cardId);

	/**
	 * Retrieves all domains in which the class with id = classId is involved
	 * (both direct and inverse relation)
	 * 
	 * @param className
	 *            the class name involved in the relation
	 * @return a list of all domains defined for the class
	 */
	List<CMDomain> findDomainsForClassWithName(String className);

	/**
	 * Tells if the given class is a subclass of Activity
	 * 
	 * @return {@code true} if if the given class is a subclass of Activity,
	 *         {@code false} otherwise
	 */
	boolean isProcess(CMClass target);

	/**
	 * Relations.... move the following code to another class
	 * 
	 * @return all created relation'ids.
	 */
	Iterable<Long> createRelations(RelationDTO relationDTO);

	void updateRelation(RelationDTO relationDTO);

	void deleteRelation(String domainName, Long relationId);

	void deleteDetail(Card master, Card detail, String domainName);

	public void deleteRelation(final String srcClassName, final Long srcCardId, final String dstClassName,
			final Long dstCardId, final CMDomain domain);

	File exportClassAsCsvFile(String className, String separator);

	CSVData importCsvFileFor(FileItem csvFile, Long classId, String separator) throws IOException, JSONException;

	CMCard resolveCardReferences(final CMClass entryType, final CMCard card);

	void lockCard(Long cardId);

	void unlockCard(Long cardId);

	void unlockAllCards();

}
