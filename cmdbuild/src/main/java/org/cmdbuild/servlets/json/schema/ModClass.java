package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DEFAULT_VALUE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAINS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_CARDINALITY;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_FIRST_CLASS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_IS_MASTER_DETAIL;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_MASTER_DETAIL_LABEL;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_SECOND_CLASS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.EDITOR_TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FIELD_MODE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.FK_DESTINATION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FORCE_CREATION;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.INDEX;
import static org.cmdbuild.servlets.json.ComunicationConstants.INHERIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.IS_PROCESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.LENGTH;
import static org.cmdbuild.servlets.json.ComunicationConstants.LOOKUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.META_DATA;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.NOT_NULL;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRECISION;
import static org.cmdbuild.servlets.json.ComunicationConstants.SCALE;
import static org.cmdbuild.servlets.json.ComunicationConstants.SHOW_IN_GRID;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUPERCLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.TABLE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TABLE_TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPES;
import static org.cmdbuild.servlets.json.ComunicationConstants.UNIQUE;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_STOPPABLE;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMTableType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic.FunctionItem;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction.Visitor;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Create;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Delete;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Update;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.Metadata;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer.JsonModeMapper;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModClass extends JSONBaseWithSpringContext {

	private static class JsonFunctionItem implements FunctionItem {

		private final FunctionItem delegate;

		public JsonFunctionItem(final FunctionItem delegate) {
			this.delegate = delegate;
		}

		@Override
		@JsonProperty(NAME)
		public String name() {
			return delegate.name();
		}

	}

	private static Function<FunctionItem, FunctionItem> toJsonFunction = new Function<FunctionItem, FunctionItem>() {

		@Override
		public FunctionItem apply(final FunctionItem input) {
			return new JsonFunctionItem(input);
		}

	};

	@JSONExported
	public JSONObject getAllClasses( //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException, AuthException, CMWorkflowException {

		final JSONArray serializedClasses = new JSONArray();
		final Iterable<? extends CMClass> fetchedClasses;
		final Iterable<? extends UserProcessClass> processClasses;

		if (activeOnly) {
			fetchedClasses = userDataAccessLogic().findActiveClasses();
			processClasses = filter(workflowLogic().findActiveProcessClasses(), processesWithXpdlAssociated());
		} else {
			fetchedClasses = userDataAccessLogic().findAllClasses();
			processClasses = workflowLogic().findAllProcessClasses();
		}

		final Iterable<? extends CMClass> nonProcessClasses = filter(fetchedClasses, nonProcessClasses());
		final Iterable<? extends CMClass> classesToBeReturned = activeOnly ? filter(nonProcessClasses,
				nonSystemButUsable()) : nonProcessClasses;

		for (final CMClass cmClass : classesToBeReturned) {
			final JSONObject classObject = classSerializer().toClient(cmClass);
			Serializer.addAttachmentsData(classObject, cmClass, dmsLogic());
			serializedClasses.put(classObject);
		}

		for (final UserProcessClass userProcessClass : processClasses) {
			final JSONObject classObject = classSerializer().toClient(userProcessClass, activeOnly);
			Serializer.addAttachmentsData(classObject, userProcessClass, dmsLogic());
			serializedClasses.put(classObject);

			// do this check only for the request
			// of active classes AKA the management module
			if (activeOnly) {
				try {
					alertAdminIfNoStartActivity(userProcessClass);
				} catch (final Exception ex) {
					logger.error(String.format("Error retrieving start activity for process",
							userProcessClass.getName()));
				}
			}
		}

		return new JSONObject() {
			{
				put("classes", serializedClasses);
			}
		};
	}

	/**
	 * @param element
	 * @throws CMWorkflowException
	 */
	private void alertAdminIfNoStartActivity(final UserProcessClass element) throws CMWorkflowException {
		try {
			workflowLogic().getStartActivityOrDie(element.getName());
		} catch (final CMDBWorkflowException ex) {
			// throw an exception to say to the user
			// that the XPDL has no adminStart
			if (WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.equals(ex.getExceptionType())
					&& !element.isSuperclass() && sessionVars().getUser().hasAdministratorPrivileges()) {
				requestListener().warn(ex);
			} else {
				throw ex;
			}
		}
	}

	private Predicate<CMClass> nonProcessClasses() {
		final CMClass processBaseClass = userDataAccessLogic().getView().findClass(Constants.BASE_PROCESS_CLASS_NAME);
		final Predicate<CMClass> nonProcessClasses = new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return !processBaseClass.isAncestorOf(input);
			}
		};
		return nonProcessClasses;
	}

	private Predicate<UserProcessClass> processesWithXpdlAssociated() {
		final Predicate<UserProcessClass> processesWithXpdlAssociated = new Predicate<UserProcessClass>() {
			@Override
			public boolean apply(final UserProcessClass input) {
				boolean apply = false;
				try {
					apply = input.getName().equals(Constants.BASE_PROCESS_CLASS_NAME) //
							|| input.isSuperclass() //
							|| input.getDefinitionVersions().length > 0;
				} catch (final CMWorkflowException e) {
				}
				return apply;
			}
		};
		return processesWithXpdlAssociated;
	}

	/**
	 * 
	 * @return a predicate that will filter classes whose mode does not start
	 *         with sys... (e.g. sysread or syswrite)
	 */
	private Predicate<CMClass> nonSystemButUsable() {
		final Predicate<CMClass> predicate = new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return !input.isSystemButUsable();
			}
		};
		return predicate;
	}

	@JSONExported
	public JSONObject saveTable( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = INHERIT, required = false) final int idParent, //
			@Parameter(value = SUPERCLASS, required = false) final boolean isSuperClass, //
			@Parameter(value = IS_PROCESS, required = false) final boolean isProcess, //
			@Parameter(value = TABLE_TYPE, required = false) String tableType, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter(USER_STOPPABLE) final boolean isProcessUserStoppable, //
			@Parameter(FORCE_CREATION) final boolean forceCreation) throws JSONException, CMDBException {

		if (tableType == "") {
			tableType = EntryType.TableType.standard.name();
		}

		final EntryType entryType = EntryType.newClass() //
				.withTableType(EntryType.TableType.valueOf(tableType)).withName(name) //
				.withDescription(description) //
				.withParent(Long.valueOf(idParent)) //
				.thatIsSuperClass(isSuperClass) //
				.thatIsProcess(isProcess) //
				.thatIsUserStoppable(isProcessUserStoppable) //
				.thatIsActive(isActive) //
				.thatIsSystem(false) //
				.build();

		final CMClass cmClass = dataDefinitionLogic().createOrUpdate(entryType, forceCreation);
		return classSerializer().toClient(cmClass, TABLE);
	}

	@JSONExported
	public void deleteTable(@Parameter(value = CLASS_NAME) final String className) throws JSONException, CMDBException {
		dataDefinitionLogic().deleteOrDeactivate(className);
	}

	@JSONExported
	public JSONObject getAttributeList(@Parameter(value = ACTIVE, required = false) final boolean onlyActive, //
			@Parameter(value = CLASS_NAME) final String className) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();

		Iterable<? extends CMAttribute> attributesForClass;
		final DataAccessLogic dataLogic = userDataAccessLogic();
		if (onlyActive) {
			attributesForClass = dataLogic.findClass(className).getActiveAttributes();
		} else {
			attributesForClass = dataLogic.findClass(className).getAttributes();
		}

		out.put(ATTRIBUTES, AttributeSerializer.withView(dataLogic.getView()).toClient(attributesForClass, onlyActive));
		return out;
	}

	@JSONExported
	public void saveOrderCriteria(@Parameter(value = ATTRIBUTES) final JSONObject orderCriteria, //
			@Parameter(value = CLASS_NAME) final String className) throws Exception {

		final List<ClassOrder> classOrders = Lists.newArrayList();
		final Iterator<?> keysIterator = orderCriteria.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			classOrders.add(ClassOrder.from(key, orderCriteria.getInt(key)));
		}
		dataDefinitionLogic().changeClassOrders(className, classOrders);
	}

	/**
	 * 
	 * @param tableTypeStirng
	 *            can be CLASS or SIMPLECLASS
	 * @return a list of attribute types that a class or superclass can have.
	 * @throws JSONException
	 * @throws AuthException
	 */
	@JSONExported
	public JSONObject getAttributeTypes( //
			@Parameter(TABLE_TYPE) final String tableTypeStirng) //
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);
		final List<CMAttributeType<?>> types = new LinkedList<CMAttributeType<?>>();
		for (final CMAttributeType<?> type : tableType.getAvaiableAttributeList()) {
			types.add(type);
		}
		out.put(TYPES, AttributeSerializer.toClient(types));
		return out;
	}

	@Admin
	@JSONExported
	public JSONObject saveAttribute( //
			final JSONObject serializer, //
			@Parameter(value = NAME, required = false) final String name, //
			@Parameter(value = TYPE, required = false) final String attributeTypeString, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = DEFAULT_VALUE, required = false) final String defaultValue, //
			@Parameter(SHOW_IN_GRID) final boolean isBaseDSP, //
			@Parameter(NOT_NULL) final boolean isNotNull, //
			@Parameter(UNIQUE) final boolean isUnique, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter(FIELD_MODE) final String fieldMode, //
			@Parameter(value = LENGTH, required = false) final int length, //
			@Parameter(value = PRECISION, required = false) final int precision, //
			@Parameter(value = SCALE, required = false) final int scale, //
			@Parameter(value = LOOKUP, required = false) final String lookupType, //
			@Parameter(value = DOMAIN_NAME, required = false) final String domainName, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = FK_DESTINATION, required = false) final String fkDestinationName, //
			@Parameter(value = GROUP, required = false) final String group, //
			@Parameter(value = META_DATA, required = false) final JSONObject meta, //
			@Parameter(value = EDITOR_TYPE, required = false) final String editorType, //
			@Parameter(value = CLASS_NAME) final String className) throws Exception {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwnerName(className) //
				.withDescription(description) //
				.withGroup(group) //
				.withType(attributeTypeString) //
				.withLength(length) //
				.withPrecision(precision) //
				.withScale(scale) //
				.withLookupType(lookupType) //
				.withDomain(domainName) //
				.withDefaultValue(defaultValue) //
				.withMode(JsonModeMapper.modeFrom(fieldMode)) //
				.withEditorType(editorType) //
				.withFilter(filter) //
				.withForeignKeyDestinationClassName(fkDestinationName) //
				.thatIsDisplayableInList(isBaseDSP) //
				.thatIsMandatory(isNotNull) //
				.thatIsUnique(isUnique) //
				.thatIsActive(isActive) //
				.withMetadata(buildMetadataByAction(meta)) //
				.build();
		final DataDefinitionLogic logic = dataDefinitionLogic();
		final CMAttribute cmAttribute = logic.createOrUpdate(attribute);
		final JSONObject result = AttributeSerializer.withView(logic.getView()).toClient(cmAttribute,
				buildMetadataForSerialization(attribute.getMetadata()));
		serializer.put(ATTRIBUTE, result);
		return serializer;
	}

	private enum MetaStatus {

		DELETED(MetadataActions.DELETE), //
		MODIFIED(MetadataActions.UPDATE), //
		NEW(MetadataActions.CREATE), //
		UNDEFINED(null), //
		;

		private final MetadataAction action;

		private MetaStatus(final MetadataAction action) {
			this.action = action;
		}

		public boolean hasAction() {
			return (action != null);
		}

		public MetadataAction getAction() {
			return action;
		}

		public static MetaStatus forStatus(final String status) {
			for (final MetaStatus value : values()) {
				if (value.name().equals(status)) {
					return value;
				}
			}
			return UNDEFINED;
		}

	}

	private Map<MetadataAction, List<Metadata>> buildMetadataByAction(final JSONObject meta) throws Exception {
		final Map<MetadataAction, List<Metadata>> metadataMap = Maps.newHashMap();
		if (meta != null) {
			final Iterator<?> jsonMetadata = meta.keys();
			while (jsonMetadata.hasNext()) {
				final String name = (String) jsonMetadata.next();
				final JSONObject info = meta.getJSONObject(name);
				final String value = info.getString("value");
				final MetaStatus status = MetaStatus.forStatus(info.getString("status"));
				if (status.hasAction()) {
					final MetadataAction action = status.getAction();
					List<Metadata> list = metadataMap.get(action);
					if (list == null) {
						list = Lists.newArrayList();
						metadataMap.put(action, list);
					}
					list.add(new Metadata(name, value));

				}
			}
		}
		return metadataMap;
	}

	private Iterable<Metadata> buildMetadataForSerialization(final Map<MetadataAction, List<Metadata>> metadataByAction) {
		final List<Metadata> metadata = Lists.newArrayList();
		for (final MetadataAction action : metadataByAction.keySet()) {
			final Iterable<Metadata> elements = metadataByAction.get(action);
			action.accept(new Visitor() {

				@Override
				public void visit(final Create action) {
					filter(elements, new Predicate<Metadata>() {
						@Override
						public boolean apply(final Metadata input) {
							metadata.add(input);
							return true;
						}
					});
				}

				@Override
				public void visit(final Update action) {
					filter(elements, new Predicate<Metadata>() {
						@Override
						public boolean apply(final Metadata input) {
							metadata.add(input);
							return true;
						}
					});
				}

				@Override
				public void visit(final Delete action) {
					filter(elements, new Predicate<Metadata>() {
						@Override
						public boolean apply(final Metadata input) {
							// nothing to do
							return true;
						}
					});
				}

			});
		}
		return metadata;
	}

	@JSONExported
	public void deleteAttribute( //
			@Parameter(NAME) final String attributeName, //
			@Parameter(CLASS_NAME) final String className) {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwnerName(className) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(attribute);
	}

	@JSONExported
	public void reorderAttribute( //
			@Parameter(ATTRIBUTES) final String jsonAttributeList, //
			@Parameter(CLASS_NAME) final String className //
	) throws Exception {
		final List<Attribute> attributes = Lists.newArrayList();
		final JSONArray jsonAttributes = new JSONArray(jsonAttributeList);
		for (int i = 0; i < jsonAttributes.length(); i++) {
			final JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			attributes.add(Attribute.newAttribute().withOwnerName(className)//
					.withName(jsonAttribute.getString(NAME)) //
					.withIndex(jsonAttribute.getInt(INDEX)).build());
		}

		for (final Attribute attribute : attributes) {
			dataDefinitionLogic().reorder(attribute);
		}
	}

	@JSONExported
	public JSONObject getAllDomains(@Parameter(value = ACTIVE, required = false) final boolean activeOnly)
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final Iterable<? extends CMDomain> almostAllDomains;
		if (activeOnly) {
			almostAllDomains = filter(userDataAccessLogic().findActiveDomains(), domainsWithActiveClasses());
		} else {
			almostAllDomains = userDataAccessLogic().findAllDomains();
		}
		final Iterable<? extends CMDomain> domains = filter(almostAllDomains,
				nonActivityClassesWhenWorkflowIsNotEnabled());

		final JSONArray jsonDomains = new JSONArray();
		out.put(DOMAINS, jsonDomains);
		for (final CMDomain domain : domains) {
			jsonDomains.put(domainSerializer().toClient(domain, activeOnly));
		}
		return out;
	}

	private <T extends CMDomain> Predicate<T> domainsWithActiveClasses() {
		final Predicate<T> predicate = new Predicate<T>() {
			@Override
			public boolean apply(final T input) {
				return input.getClass1().isActive() && input.getClass2().isActive();
			}
		};
		return predicate;
	}

	private <T extends CMDomain> Predicate<T> nonActivityClassesWhenWorkflowIsNotEnabled() {
		final boolean workflowEnabled = workflowLogic().isWorkflowEnabled();
		final CMClass activityClass = userDataView().getActivityClass();
		return new Predicate<T>() {
			@Override
			public boolean apply(final T input) {
				final boolean class1IsActivity = activityClass.isAncestorOf(input.getClass1());
				final boolean class2IsActivity = activityClass.isAncestorOf(input.getClass2());
				return (!class1IsActivity && !class2IsActivity) ? true : workflowEnabled;
			}
		};
	}

	@Admin
	@JSONExported
	public JSONObject saveDomain( //
			@Parameter(value = ID) final long domainId, //
			@Parameter(value = NAME, required = false) final String domainName, //
			@Parameter(value = DOMAIN_FIRST_CLASS_ID, required = false) final int classId1, //
			@Parameter(value = DOMAIN_SECOND_CLASS_ID, required = false) final int classId2, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = DOMAIN_CARDINALITY, required = false) final String cardinality, //
			@Parameter(DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS) final String descriptionDirect, //
			@Parameter(DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS) final String descriptionInverse, //
			@Parameter(DOMAIN_IS_MASTER_DETAIL) final boolean isMasterDetail, //
			@Parameter(value = DOMAIN_MASTER_DETAIL_LABEL, required = false) final String mdLabel, //
			@Parameter(ACTIVE) final boolean isActive //
	) throws JSONException, AuthException, NotFoundException {
		final Domain domain = Domain.newDomain() //
				.withName(domainName) //
				.withIdClass1(classId1) //
				.withIdClass2(classId2) //
				.withDescription(description) //
				.withCardinality(cardinality) //
				.withDirectDescription(descriptionDirect) //
				.withInverseDescription(descriptionInverse) //
				.thatIsMasterDetail(isMasterDetail) //
				.withMasterDetailDescription(mdLabel) //
				.thatIsActive(isActive) //
				.build();
		final CMDomain createdOrUpdated;
		if (domainId == -1) {
			createdOrUpdated = dataDefinitionLogic().create(domain);
		} else {
			createdOrUpdated = dataDefinitionLogic().update(domain);
		}
		return domainSerializer().toClient(createdOrUpdated, false, DOMAIN);
	}

	@JSONExported
	public void deleteDomain(@Parameter(value = DOMAIN_NAME, required = false) final String domainName //
	) throws JSONException {

		dataDefinitionLogic().deleteDomainIfExists(domainName);
	}

	@Admin
	@JSONExported
	public JSONObject getDomainList(@Parameter(CLASS_NAME) final String className //
	) throws JSONException {

		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		// TODO system really needed
		final List<CMDomain> domainsForSpecifiedClass = systemDataAccessLogic().findDomainsForClassWithName(className);
		for (final CMDomain domain : domainsForSpecifiedClass) {
			if (!domain.isSystem()) {
				jsonDomains.put(domainSerializer().toClient(domain, className));
			}
		}
		out.put(DOMAINS, jsonDomains);
		return out;
	}

	/**
	 * Given a class name, this method retrieves all the attributes for all the
	 * SIMPLE classes that have at least one attribute of type foreign key whose
	 * target class is the specified class or an ancestor of it
	 * 
	 * @param className
	 * @return
	 * @throws Exception
	 */
	@JSONExported
	public JSONArray getFKTargetingClass( //
			@Parameter(CLASS_NAME) final String className //
	) throws Exception {

		// TODO: improve performances by getting only simple classes (the
		// database should filter the simple classes)
		final DataAccessLogic logic = userDataAccessLogic();
		final CMClass targetClass = logic.findClass(className);
		final JSONArray fk = new JSONArray();

		for (final CMClass activeClass : logic.findActiveClasses()) {
			final boolean isSimpleClass = !activeClass.holdsHistory();

			if (isSimpleClass) {
				for (final CMAttribute attribute : activeClass.getActiveAttributes()) {
					final String referencedClassName = attribute.getForeignKeyDestinationClassName();
					if (referencedClassName == null) {
						continue;
					}

					final CMClass referencedClass = logic.findClass(referencedClassName);
					if (referencedClass.isAncestorOf(targetClass)) {
						final boolean serializeAlsoClassId = true;
						final JSONObject jsonAttribute = AttributeSerializer //
								.withView(logic.getView()) //
								.toClient(attribute, serializeAlsoClassId);

						fk.put(jsonAttribute);
					}
				}
			}
		}

		return fk;
	}

	/**
	 * Retrieves all domains with cardinality 1:N or N:1 in which the class with
	 * the specified name is on the 'N' side
	 * 
	 * @param className
	 * @return
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public JSONObject getReferenceableDomainList(@Parameter(CLASS_NAME) final String className) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final Iterable<? extends CMDomain> referenceableDomains = systemDataAccessLogic().findReferenceableDomains(
				className);
		for (final CMDomain domain : referenceableDomains) {
			jsonDomains.put(domainSerializer().toClient(domain, false));
		}
		out.put(DOMAINS, jsonDomains);
		return out;
	}

	@JSONExported
	public JsonResponse getFunctions() {
		return JsonResponse.success( //
				from(dataDefinitionLogic().functions()) //
						.transform(toJsonFunction) //
						.toList());
	}

}
