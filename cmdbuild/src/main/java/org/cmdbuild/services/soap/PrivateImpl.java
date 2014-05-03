package org.cmdbuild.services.soap;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.jws.WebService;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.common.digest.DigesterFactory;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.services.auth.UserInfo;
import org.cmdbuild.services.soap.connector.ConnectorJobIntrospector;
import org.cmdbuild.services.soap.connector.ConnectorParser;
import org.cmdbuild.services.soap.connector.XmlConnectorParser;
import org.cmdbuild.services.soap.serializer.AttributeSchemaSerializer;
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
import org.cmdbuild.services.soap.types.ReportParams;
import org.cmdbuild.services.soap.types.WSEvent;
import org.cmdbuild.services.soap.types.WSProcessStartEvent;
import org.cmdbuild.services.soap.types.WSProcessUpdateEvent;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.servlets.json.serializers.AbstractAttributeValueVisitor;
import org.cmdbuild.workflow.event.WorkflowEvent;
import org.dom4j.Document;

@WebService(endpointInterface = "org.cmdbuild.services.soap.Private", targetNamespace = "http://soap.services.cmdbuild.org")
public class PrivateImpl extends AbstractWebservice implements Private {

	@Override
	public Card getCard(final String className, final Integer cardId, final Attribute[] attributeList) {
		return getCard(className, Long.valueOf(cardId), attributeList, false);
	}

	private CardExt getCard(final String className, final Long cardId, final Attribute[] attributeList,
			final boolean enableLongDateFormat) {
		return dataAccessLogicHelper().getCardExt(className, cardId, attributeList, enableLongDateFormat);
	}

	@Override
	public CardExt getCardWithLongDateFormat(final String className, final Integer cardId,
			final Attribute[] attributeList) {
		return getCard(className, Long.valueOf(cardId), attributeList, true);
	}

	@Override
	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		return getCards(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery, false);
	}

	private CardList getCards(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery, final boolean enableLongDateFormat) {
		return dataAccessLogicHelper().getCardList(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, cqlQuery, enableLongDateFormat);
	}

	@Override
	public CardList getCardListWithLongDateFormat(final String className, final Attribute[] attributeList,
			final Query queryType, final Order[] orderType, final Integer limit, final Integer offset,
			final String fullTextQuery, final CQLQuery cqlQuery) {
		return getCards(className, attributeList, queryType, orderType, limit, offset, fullTextQuery, cqlQuery, true);
	}

	@Override
	public CardListExt getCardListExt(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, final Integer offset, final String fullTextQuery,
			final CQLQuery cqlQuery) {
		return dataAccessLogicHelper().getCardListExt(className, attributeList, queryType, orderType, limit, offset,
				fullTextQuery, cqlQuery);
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
	public Workflow updateWorkflow(final Card card, final boolean completeTask, final WorkflowWidgetSubmission[] widgets) {
		return workflowLogicHelper().updateProcess(card, widgets, completeTask);
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
	public ActivitySchema getActivityObjects(final String className, final Integer cardid) {
		return workflowLogicHelper().getActivitySchema(className, cardid);
	}

	@Override
	public Reference[] getReference(final String className, final Query query, final Order[] orderType,
			final Integer limit, final Integer offset, final String fullTextQuery, final CQLQuery cqlQuery) {
		return dataAccessLogicHelper()
				.getReference(className, query, orderType, limit, offset, fullTextQuery, cqlQuery);
	}

	@Override
	public MenuSchema getCardMenuSchema() {
		return dataAccessLogicHelper().getVisibleClassesTree();
	}

	@Override
	public MenuSchema getActivityMenuSchema() {
		return dataAccessLogicHelper().getVisibleProcessesTree();
	}

	@Override
	public MenuSchema getMenuSchema() {
		return dataAccessLogicHelper().getMenuSchemaForPreferredGroup();
	}

	@Override
	public org.cmdbuild.services.soap.types.Report[] getReportList(final String type, final int limit, final int offset) {
		return dataAccessLogicHelper().getReportsByType(type, limit, offset);
	}

	@Override
	public AttributeSchema[] getReportParameters(final int id, final String extension) {
		return dataAccessLogicHelper().getReportParameters(id, extension);
	}

	@Override
	public DataHandler getReport(final int id, final String extension, final ReportParams[] params) {
		return dataAccessLogicHelper().getReport(id, extension, params);
	}

	@Override
	public DataHandler getBuiltInReport(final String reportId, final String extension, final ReportParams[] params) {
		return dataAccessLogicHelper().getReport(reportId, extension, params);
	}

	@Override
	public String sync(final String xml) {
		logger.info("Calling webservice ExternalSync.sync");
		logger.debug("xml message:" + xml);
		final ConnectorParser parser = new XmlConnectorParser(xml);
		final Document document = parser.parse();
		final ConnectorJobIntrospector introspector = new ConnectorJobIntrospector(document, userDataAccessLogic(),
				userDataView(), lookupStore());
		return introspector.submitJobs();
	}

	@Override
	public UserInfo getUserInfo() {
		return authenticationLogicHelper().getUserInfo();
	}

	/*
	 * r2.2
	 */

	@Override
	public ClassSchema getClassSchema(final String className) {
		return dataAccessLogicHelper().getClassSchema(className);
	}

	/*
	 * r2.3
	 */

	@Override
	public Attribute[] callFunction(final String functionName, final Attribute[] params) {
		logger.info(format("calling function '%s' with parameters:", functionName));
		if (params != null) {
			for (final Attribute attribute : params) {
				logger.info(format("- %s",
						ToStringBuilder.reflectionToString(attribute, ToStringStyle.SHORT_PREFIX_STYLE)));
			}
		}
		final CMFunction function = userDataView().findFunctionByName(functionName);
		final Object[] actualParams = convertFunctionInput(function, params);

		final Alias f = NameAlias.as("f");
		final CMQueryResult queryResult = userDataView().select(anyAttribute(function, f)) //
				.from(call(function, actualParams), f) //
				.run();

		if (queryResult.isEmpty()) {
			return new Attribute[0];
		} else {
			final CMQueryRow row = queryResult.iterator().next();
			final Attribute[] outParams = convertFunctionOutput(function, row.getValueSet(f));
			logger.info(format("output parameters for function '%s':", functionName));
			if (outParams != null) {
				for (final Attribute attribute : outParams) {
					logger.info(format("- %s",
							ToStringBuilder.reflectionToString(attribute, ToStringStyle.SHORT_PREFIX_STYLE)));
				}
			}
			return outParams;
		}
	}

	/**
	 * Converts the web service parameters to objects suited for the persistence
	 * layer.
	 * 
	 * Actually it does not need to convert from String to the native Object
	 * because the persistence layer does it automatically!
	 * 
	 * @param function
	 * @param params
	 *            received from the web services
	 * @return params for the persistence layer
	 */
	private Object[] convertFunctionInput(final CMFunction function, final Attribute[] wsParams) {
		final Map<String, String> paramsMap = new HashMap<String, String>();
		if (wsParams != null) {
			for (final Attribute p : wsParams) {
				paramsMap.put(p.getName(), p.getValue());
			}
		}
		final List<CMFunctionParameter> functionParams = function.getInputParameters();
		final List<String> params = new ArrayList<String>(functionParams.size());
		for (final CMFunctionParameter fp : functionParams) {
			final String functionParamName = fp.getName();
			final String stringValue = paramsMap.get(functionParamName);
			params.add(stringValue);
		}
		return params.toArray();
	}

	private Attribute[] convertFunctionOutput(final CMFunction function, final CMValueSet valueSet) {
		final List<CMFunctionParameter> outputParams = function.getOutputParameters();
		final Attribute[] output = new Attribute[outputParams.size()];
		int i = 0;
		for (final CMFunctionParameter p : outputParams) {
			final Attribute a = nativeValueToWsAttribute(p, valueSet);
			output[i] = a;
			++i;
		}
		return output;
	}

	private Attribute nativeValueToWsAttribute(final CMFunctionParameter functionParam, final CMValueSet valueSet) {
		final Attribute a = new Attribute();
		final String paramName = functionParam.getName();
		a.setName(paramName);
		final CMAttributeType<?> type = functionParam.getType();
		final Object value = valueSet.get(paramName);
		a.setValue(nativeValueToWsString(type, value));
		return a;
	}

	private String nativeValueToWsString(final CMAttributeType<?> type, final Object value) {
		return (value == null) ? EMPTY : new AbstractAttributeValueVisitor(type, value) {

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				throw new UnsupportedOperationException("regclasses not supported");
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				throw new UnsupportedOperationException("lookups not supported");
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				throw new UnsupportedOperationException("references not supported");
			}

		}.convertValue().toString();
	}

	@Override
	public void notify(final WSEvent wsEvent) {
		logger.info("event received");
		wsEvent.accept(new WSEvent.Visitor() {

			@Override
			public void visit(final WSProcessStartEvent wsEvent) {
				logger.info(format("event for process start: %d / %s / %s", //
						wsEvent.getSessionId(), wsEvent.getProcessDefinitionId(), wsEvent.getProcessInstanceId()));
				final WorkflowEvent event = WorkflowEvent.newProcessStartEvent(wsEvent.getProcessDefinitionId(),
						wsEvent.getProcessInstanceId());
				workflowEventManager().pushEvent(wsEvent.getSessionId(), event);
			}

			@Override
			public void visit(final WSProcessUpdateEvent wsEvent) {
				logger.info(format("event for process update: %d / %s / %s", //
						wsEvent.getSessionId(), wsEvent.getProcessDefinitionId(), wsEvent.getProcessInstanceId()));
				final WorkflowEvent event = WorkflowEvent.newProcessUpdateEvent(wsEvent.getProcessDefinitionId(),
						wsEvent.getProcessInstanceId());
				workflowEventManager().pushEvent(wsEvent.getSessionId(), event);
			}

		});
	}

	@Override
	public List<FunctionSchema> getFunctionList() {
		final List<FunctionSchema> functionSchemas = new ArrayList<FunctionSchema>();
		for (final CMFunction function : userDataView().findAllFunctions()) {
			functionSchemas.add(functionSchemaFor(function));
		}
		return functionSchemas;
	}

	private FunctionSchema functionSchemaFor(final CMFunction function) {
		final FunctionSchema functionSchema = new FunctionSchema();
		functionSchema.setName(function.getIdentifier().getLocalName());
		functionSchema.setInput(attributeSchemasFrom(function.getInputParameters()));
		functionSchema.setOutput(attributeSchemasFrom(function.getOutputParameters()));
		return functionSchema;
	}

	private List<AttributeSchema> attributeSchemasFrom(final List<CMFunctionParameter> parameters) {
		final List<AttributeSchema> attributeSchemas = new ArrayList<AttributeSchema>();
		for (final CMFunction.CMFunctionParameter parameter : parameters) {
			attributeSchemas.add(AttributeSchemaSerializer.serialize(parameter));
		}
		return attributeSchemas;
	}

	@Override
	public String generateDigest(final String plainText, final String digestAlgorithm) throws NoSuchAlgorithmException {
		if (digestAlgorithm == null) {
			logger.error("The digest algorithm is null");
			throw new IllegalArgumentException(
					"Both the argument must not be null. Specify the text to be encrypted and a valid digest algorithm");
		}
		if (plainText == null) {
			return null;
		}
		final Digester digester = DigesterFactory.createDigester(digestAlgorithm);
		logger.info("Generating digest with algorithm " + digester + " ("
				+ (digester.isReversible() ? "reversible" : "irreversible") + ")");
		return digester.encrypt(plainText);
	}

}
