package org.cmdbuild.services.soap;

import java.util.List;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;

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

@WebService(targetNamespace = "http://soap.services.cmdbuild.org")
public interface Webservices {

	public CardList getCardList(@WebParam(name = "className") String className,
			@WebParam(name = "attributeList") Attribute[] attributeList, @WebParam(name = "queryType") Query queryType,
			@WebParam(name = "orderType") Order[] orderType, @WebParam(name = "limit") Integer limit,
			@WebParam(name = "offset") Integer offset, @WebParam(name = "fullTextQuery") String fullTextQuery);

	public Card getCard(@WebParam(name = "className") String className, @WebParam(name = "cardId") Integer cardId,
			@WebParam(name = "attributeList") Attribute[] attributeList);

	public CardList getCardHistory(@WebParam(name = "className") String className,
			@WebParam(name = "cardId") int cardId, @WebParam(name = "limit") Integer limit,
			@WebParam(name = "offset") Integer offset);

	public int createCard(@WebParam(name = "cardType") Card cardType);

	public boolean updateCard(@WebParam(name = "card") Card card);

	public boolean deleteCard(@WebParam(name = "className") String className, @WebParam(name = "cardId") int cardId);

	public int createLookup(@WebParam(name = "lookup") Lookup lookup);

	public boolean deleteLookup(@WebParam(name = "lookupId") int lookupId);

	public boolean updateLookup(@WebParam(name = "lookup") Lookup lookup);

	public Lookup getLookupById(@WebParam(name = "id") int id);

	public Lookup[] getLookupList(@WebParam(name = "type") String type, @WebParam(name = "value") String value,
			@WebParam(name = "parentList") boolean parentList);

	public Lookup[] getLookupListByCode(@WebParam(name = "type") String type, @WebParam(name = "code") String code,
			@WebParam(name = "parentList") boolean parentList);

	public boolean createRelation(@WebParam(name = "relation") Relation relation);

	public boolean createRelationWithAttributes(@WebParam(name = "relation") Relation relation,
			@WebParam(name = "attributes") List<Attribute> attributes);

	public boolean deleteRelation(@WebParam(name = "relation") Relation relation);

	public List<Relation> getRelationList(@WebParam(name = "domain") String domain,
			@WebParam(name = "className") String className, @WebParam(name = "cardId") int cardId);

	public List<Attribute> getRelationAttributes(@WebParam(name = "relation") Relation relation);

	public Relation[] getRelationHistory(@WebParam(name = "relation") Relation relation);

	public Attachment[] getAttachmentList(@WebParam(name = "className") String className,
			@WebParam(name = "cardId") int cardId);

	@XmlMimeType("application/octet-stream")
	public boolean uploadAttachment(@WebParam(name = "className") String className,
			@WebParam(name = "objectid") int objectid, @WebParam(name = "file") DataHandler file,
			@WebParam(name = "filename") String filename, @WebParam(name = "category") String category,
			@WebParam(name = "description") String description);

	@XmlMimeType("application/octet-stream")
	public DataHandler downloadAttachment(@WebParam(name = "className") String className,
			@WebParam(name = "objectid") int objectid, @WebParam(name = "filename") String filename);

	public boolean deleteAttachment(@WebParam(name = "className") String className,
			@WebParam(name = "cardId") int cardId, @WebParam(name = "filename") String filename);

	public boolean updateAttachmentDescription(@WebParam(name = "className") String className,
			@WebParam(name = "cardId") int cardId, @WebParam(name = "filename") String filename,
			@WebParam(name = "description") String description);

	public Workflow startWorkflow(@WebParam(name = "card") Card card,
			@WebParam(name = "completeTask") boolean completeTask);

	public boolean updateWorkflow(@WebParam(name = "card") Card card,
			@WebParam(name = "completeTask") boolean completeTask);

	public String getProcessHelp(@WebParam(name = "classname") String classname,
			@WebParam(name = "cardid") Integer cardid);

	public AttributeSchema[] getAttributeList(@WebParam(name = "className") String className);

	public AttributeSchema[] getActivityObjects(@WebParam(name = "className") String className,
			@WebParam(name = "cardid") Integer cardid);

	public MenuSchema getActivityMenuSchema();

	public Reference[] getReference(@WebParam(name = "className") String className,
			@WebParam(name = "query") Query query, @WebParam(name = "orderType") Order[] orderType,
			@WebParam(name = "limit") Integer limit, @WebParam(name = "offset") Integer offset,
			@WebParam(name = "fullTextQuery") String fullTextQuery);

	public MenuSchema getCardMenuSchema();

	public MenuSchema getMenuSchema();

	public boolean resumeWorkflow(@WebParam(name = "card") Card card,
			@WebParam(name = "completeTask") boolean completeTask);
}