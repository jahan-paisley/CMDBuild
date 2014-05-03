package org.cmdbuild.services.soap;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.cmdbuild.services.auth.UserInfo;
import org.cmdbuild.services.soap.structure.ActivitySchema;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.ClassSchema;
import org.cmdbuild.services.soap.structure.FunctionSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.types.Attachment;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardExt;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.CardListExt;
import org.cmdbuild.services.soap.types.Lookup;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.services.soap.types.Relation;
import org.cmdbuild.services.soap.types.Report;
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.soap.types.WSEvent;
import org.cmdbuild.services.soap.types.Workflow;

@WebService(targetNamespace = "http://soap.services.cmdbuild.org")
@XmlSeeAlso({ org.cmdbuild.services.soap.types.WSProcessStartEvent.class,
		org.cmdbuild.services.soap.types.WSProcessUpdateEvent.class })
public interface Private {
	public CardList getCardList(@WebParam(name = "className") String className,
			@WebParam(name = "attributeList") Attribute[] attributeList, @WebParam(name = "queryType") Query queryType,
			@WebParam(name = "orderType") Order[] orderType, @WebParam(name = "limit") Integer limit,
			@WebParam(name = "offset") Integer offset, @WebParam(name = "fullTextQuery") String fullTextQuery,
			@WebParam(name = "cqlQuery") CQLQuery cqlQuery);

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

	public boolean uploadAttachment(@WebParam(name = "className") String className,
			@WebParam(name = "objectid") int objectid,
			@WebParam(name = "file") @XmlMimeType("application/octet-stream") DataHandler file,
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

	public Workflow updateWorkflow(@WebParam(name = "card") Card card,
			@WebParam(name = "completeTask") boolean completeTask,
			@WebParam(name = "widgets") WorkflowWidgetSubmission[] widgets);

	public String getProcessHelp(@WebParam(name = "classname") String classname,
			@WebParam(name = "cardid") Integer cardid);

	public AttributeSchema[] getAttributeList(@WebParam(name = "className") String className);

	public ActivitySchema getActivityObjects(@WebParam(name = "className") String className,
			@WebParam(name = "cardid") Integer cardid);

	public MenuSchema getActivityMenuSchema();

	public Reference[] getReference(@WebParam(name = "className") String className,
			@WebParam(name = "query") Query query, @WebParam(name = "orderType") Order[] orderType,
			@WebParam(name = "limit") Integer limit, @WebParam(name = "offset") Integer offset,
			@WebParam(name = "fullTextQuery") String fullTextQuery, @WebParam(name = "cqlQuery") CQLQuery cqlQuery);

	public MenuSchema getCardMenuSchema();

	public MenuSchema getMenuSchema();

	public Report[] getReportList(@WebParam(name = "type") String type, @WebParam(name = "limit") int limit,
			@WebParam(name = "offset") int offset);

	public AttributeSchema[] getReportParameters(@WebParam(name = "id") int id,
			@WebParam(name = "extension") String extension);

	@XmlMimeType("application/octet-stream")
	public DataHandler getReport(@WebParam(name = "id") int id, @WebParam(name = "extension") String extension,
			@WebParam(name = "params") ReportParams[] params);

	public String sync(@WebParam(name = "xml") String xml);

	public UserInfo getUserInfo();

	/*
	 * r2.1
	 */

	// HACK The Project Manager forced us to do this
	public CardList getCardListWithLongDateFormat(@WebParam(name = "className") String className,
			@WebParam(name = "attributeList") Attribute[] attributeList, @WebParam(name = "queryType") Query queryType,
			@WebParam(name = "orderType") Order[] orderType, @WebParam(name = "limit") Integer limit,
			@WebParam(name = "offset") Integer offset, @WebParam(name = "fullTextQuery") String fullTextQuery,
			@WebParam(name = "cqlQuery") CQLQuery cqlQuery);

	/*
	 * r2.2
	 */

	public ClassSchema getClassSchema(@WebParam(name = "className") String className);

	public CardListExt getCardListExt(@WebParam(name = "className") String className,
			@WebParam(name = "attributeList") Attribute[] attributeList, @WebParam(name = "queryType") Query queryType,
			@WebParam(name = "orderType") Order[] orderType, @WebParam(name = "limit") Integer limit,
			@WebParam(name = "offset") Integer offset, @WebParam(name = "fullTextQuery") String fullTextQuery,
			@WebParam(name = "cqlQuery") CQLQuery cqlQuery);

	/*
	 * r2.3
	 */

	public Attribute[] callFunction(@WebParam(name = "functionName") String functionName,
			@WebParam(name = "params") Attribute[] params);

	/**
	 * Notify CMDBuild of an external event.
	 * 
	 * @param event
	 *            a generic event
	 */
	public void notify(@WebParam(name = "event") WSEvent event);

	/**
	 * Returns available functions list.
	 */
	public List<FunctionSchema> getFunctionList();

	/**
	 * 
	 * @param plainText
	 * @param digestAlgorithm
	 *            for now three algorithms are allowed: "SHA1", "MD5", "BASE64"
	 * @return an encrypted text produced by the digest algorithm with the plain
	 *         text as input
	 */
	public String generateDigest(@WebParam(name = "plainText") String plainText,
			@WebParam(name = "digestAlgorithm") String digestAlgorithm) throws NoSuchAlgorithmException;

	public CardExt getCardWithLongDateFormat(@WebParam(name = "className") String className,
			@WebParam(name = "cardId") Integer cardId, @WebParam(name = "attributeList") Attribute[] attributeList);

	/*
	 * r2.4
	 */

	@XmlMimeType("application/octet-stream")
	public DataHandler getBuiltInReport( //
			@WebParam(name = "id") String reportId, //
			@WebParam(name = "extension") String extension, //
			@WebParam(name = "params") ReportParams[] params);

}
