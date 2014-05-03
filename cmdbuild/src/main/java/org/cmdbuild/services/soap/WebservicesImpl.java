package org.cmdbuild.services.soap;

import java.util.List;

import javax.activation.DataHandler;
import javax.jws.WebService;

import org.cmdbuild.logger.Log;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.types.Attachment;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.Lookup;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Workflow;

@WebService(targetNamespace = "http://soap.services.cmdbuild.org", endpointInterface = "org.cmdbuild.services.soap.Webservices")
public class WebservicesImpl extends AbstractWebservice implements Webservices {

	@Override
	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery) {
		return dataAccessLogicHelper().getCardList(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, null, false);
	}

	@Override
	public Card getCard(final String className, final Integer cardId, final Attribute[] attributeList) {
		return dataAccessLogicHelper().getCardExt(className, Long.valueOf(cardId), attributeList, false);
	}

	@Override
	public CardList getCardHistory(final String className, final int cardId, final Integer limit, final Integer offset) {
		return dataAccessLogicHelper().getCardHistory(className, cardId, limit, offset);
	}

	@Override
	public int createCard(final Card card) {
		return dataAccessLogicHelper().createCard(card);
	}

	@Override
	public boolean updateCard(final Card card) {
		return dataAccessLogicHelper().updateCard(card);
	}

	@Override
	public boolean deleteCard(final String className, final int cardId) {
		return dataAccessLogicHelper().deleteCard(className, cardId);
	}

	@Override
	public int createLookup(final Lookup lookup) {
		return lookupLogicHelper().createLookup(lookup);
	}

	@Override
	public boolean deleteLookup(final int lookupId) {
		return lookupLogicHelper().disableLookup(lookupId);
	}

	@Override
	public boolean updateLookup(final Lookup lookup) {
		return lookupLogicHelper().updateLookup(lookup);
	}

	@Override
	public Lookup getLookupById(final int id) {
		return lookupLogicHelper().getLookupById(id);
	}

	@Override
	public Lookup[] getLookupList(final String type, final String value, final boolean parentList) {
		return lookupLogicHelper().getLookupListByDescription(type, value, parentList);
	}

	@Override
	public Lookup[] getLookupListByCode(final String type, final String code, final boolean parentList) {
		return lookupLogicHelper().getLookupListByCode(type, code, parentList);
	}

	@Override
	public boolean createRelation(final Relation relation) {
		return dataAccessLogicHelper().createRelation(relation);
	}

	@Override
	public boolean createRelationWithAttributes(final Relation relation, final List<Attribute> attributes) {
		return dataAccessLogicHelper().createRelationWithAttributes(relation, attributes);
	}

	@Override
	public boolean deleteRelation(final Relation relation) {
		return dataAccessLogicHelper().deleteRelation(relation);
	}

	@Override
	public List<Relation> getRelationList(final String domain, final String className, final int cardId) {
		return dataAccessLogicHelper().getRelations(className, domain, Long.valueOf(cardId));
	}

	@Override
	public List<Attribute> getRelationAttributes(final Relation relation) {
		return dataAccessLogicHelper().getRelationAttributes(relation);
	}

	@Override
	public Relation[] getRelationHistory(final Relation relation) {
		return dataAccessLogicHelper().getRelationHistory(relation);
	}

	@Override
	public Attachment[] getAttachmentList(final String className, final int cardId) {
		return dmsLogicHelper().getAttachmentList(className, Long.valueOf(cardId));
	}

	@Override
	public boolean uploadAttachment(final String className, final int objectid, final DataHandler file,
			final String filename, final String category, final String description) {
		return dmsLogicHelper().uploadAttachment(className, Long.valueOf(objectid), file, filename, category,
				description);
	}

	@Override
	public DataHandler downloadAttachment(final String className, final int objectid, final String filename) {
		return dmsLogicHelper().download(className, Long.valueOf(objectid), filename);
	}

	@Override
	public boolean deleteAttachment(final String className, final int cardId, final String filename) {
		return dmsLogicHelper().delete(className, Long.valueOf(cardId), filename);
	}

	@Override
	public boolean updateAttachmentDescription(final String className, final int cardId, final String filename,
			final String description) {
		return dmsLogicHelper().updateDescription(className, Long.valueOf(cardId), filename, description);
	}

	@Override
	public Workflow startWorkflow(final Card card, final boolean completeTask) {
		return workflowLogicHelper().updateProcess(card, completeTask);
	}

	@Override
	public boolean updateWorkflow(final Card card, final boolean completeTask) {
		workflowLogicHelper().updateProcess(card, completeTask);
		return true;
	}

	@Override
	public String getProcessHelp(final String classname, final Integer cardid) {
		return workflowLogicHelper().getInstructions(classname, cardid);
	}

	@Override
	public AttributeSchema[] getAttributeList(final String className) {
		return dataAccessLogicHelper().getAttributeList(className);
	}

	@Override
	public AttributeSchema[] getActivityObjects(final String className, final Integer cardid) {
		final List<AttributeSchema> attributeSchemaList = workflowLogicHelper().getAttributeSchemaList(className,
				cardid);
		return attributeSchemaList.toArray(new AttributeSchema[attributeSchemaList.size()]);
	}

	@Override
	public MenuSchema getActivityMenuSchema() {
		return dataAccessLogicHelper().getVisibleProcessesTree();
	}

	@Override
	public Reference[] getReference(final String className, final Query query, final Order[] orderType,
			final Integer limit, final Integer offset, final String fullTextQuery) {
		return dataAccessLogicHelper().getReference(className, query, orderType, limit, offset, fullTextQuery, null);
	}

	@Override
	public MenuSchema getCardMenuSchema() {
		return dataAccessLogicHelper().getVisibleClassesTree();
	}

	@Override
	public MenuSchema getMenuSchema() {
		return dataAccessLogicHelper().getMenuSchemaForPreferredGroup();
	}

	@Override
	public boolean resumeWorkflow(final Card card, final boolean completeTask) {
		if (completeTask) {
			Log.SOAP.warn("ignoring completeTask parameter because it does not make any sense");
		}
		try {
			workflowLogicHelper().resumeProcess(card);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

}
