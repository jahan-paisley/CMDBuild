package org.cmdbuild.logic.data;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_11;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.logic.data.Utils.definitionForClassOrdering;
import static org.cmdbuild.logic.data.Utils.definitionForExisting;
import static org.cmdbuild.logic.data.Utils.definitionForNew;
import static org.cmdbuild.logic.data.Utils.definitionForReordering;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.MetadataConverter;
import org.cmdbuild.data.converter.MetadataGroupable;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction.Visitor;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Create;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Delete;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Update;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.Metadata;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Business Logic Layer for data definition.
 */
public class DefaultDataDefinitionLogic implements DataDefinitionLogic {

	public static class MetadataActions {

		public static class Create implements MetadataAction {

			@Override
			public void accept(final Visitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Update implements MetadataAction {

			@Override
			public void accept(final Visitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Delete implements MetadataAction {

			@Override
			public void accept(final Visitor visitor) {
				visitor.visit(this);
			}

		}

		public static final MetadataAction CREATE = new Create();
		public static final MetadataAction UPDATE = new Update();
		public static final MetadataAction DELETE = new Delete();

	}

	private static final Function<ClassOrder, String> ATTRIBUTE_NAME_AS_KEY = new Function<ClassOrder, String>() {

		@Override
		public String apply(final ClassOrder input) {
			return input.attributeName;
		}

	};

	private static class FunctionItemWrapper implements FunctionItem {

		private final CMFunction delegate;

		public FunctionItemWrapper(final CMFunction delegate) {
			this.delegate = delegate;
		}

		@Override
		public String name() {
			return delegate.getName();
		}

	}

	private static Function<CMFunction, FunctionItem> toFunctionItem = new Function<CMFunction, FunctionItem>() {

		@Override
		public FunctionItem apply(final CMFunction input) {
			return new FunctionItemWrapper(input);
		}

	};

	private static final String UPDATE_CLASS_INDEXES_FUNCTION_NAME = "_cm_create_class_default_order_indexes";

	private static final NameAlias FUNCTION_ALIAS = NameAlias.as("f");

	private static CMClass NO_PARENT = null;

	private final CMDataView view;

	public DefaultDataDefinitionLogic(final CMDataView dataView) {
		this.view = dataView;
	}

	@Override
	public CMDataView getView() {
		return view;
	}

	/**
	 * if forceCreation is true, check if already exists a table with the same
	 * name of the given entryType
	 */
	@Override
	public CMClass createOrUpdate(final EntryType entryType, final boolean forceCreation) {
		if (forceCreation && view.findClass(entryType.getName()) != null) {

			throw ORMExceptionType.ORM_DUPLICATE_TABLE.createException();
		}

		return createOrUpdate(entryType);
	}

	@Override
	public CMClass createOrUpdate(final EntryType entryType) {
		logger.info("creating or updating class '{}'", entryType);

		final CMClass existingClass = view.findClass(identifierFrom(entryType));

		final Long parentId = entryType.getParentId();
		final CMClass parentClass = (parentId == null) ? NO_PARENT : view.findClass(parentId.longValue());

		final CMClass createdOrUpdatedClass;
		if (existingClass == null) {
			logger.info("class not already created, creating a new one");
			createdOrUpdatedClass = view.create(definitionForNew(entryType, parentClass));
		} else {
			logger.info("class already created, updating existing one");
			createdOrUpdatedClass = view.update(definitionForExisting(entryType, existingClass));
		}
		return createdOrUpdatedClass;
	}

	private CMIdentifier identifierFrom(final EntryType entryType) {
		return identifierFrom(entryType.getName(), entryType.getNamespace());
	}

	private CMIdentifier identifierFrom(final String localname, final String namespace) {
		return new CMIdentifier() {

			@Override
			public String getLocalName() {
				return localname;
			}

			@Override
			public String getNameSpace() {
				return namespace;
			}

		};
	}

	/**
	 * TODO: delete also privileges that refers to the deleted class
	 */
	@Override
	public void deleteOrDeactivate(final String className) {
		logger.info("deleting class '{}'", className);
		final CMClass existingClass = view.findClass(className);
		if (existingClass == null) {
			logger.warn("class '{}' not found", className);
			return;
		}
		final boolean hasChildren = size(existingClass.getChildren()) > 0;
		if (existingClass.isSuperclass() && hasChildren) {
			throw ORMException.ORMExceptionType.ORM_TABLE_HAS_CHILDREN.createException();
		}
		try {
			logger.warn("deleting existing class '{}'", className);
			view.delete(existingClass);
		} catch (final Exception e) {
			logger.error("error deleting class", e);
			logger.warn("class contains data");
			throw ORMException.ORMExceptionType.ORM_CONTAINS_DATA.createException();
		}

	}

	@Override
	public CMAttribute createOrUpdate(final Attribute attribute) {
		logger.info("creating or updating attribute '{}'", attribute.toString());

		final CMEntryType owner = findOwnerOf(attribute);
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());

		final CMAttribute createdOrUpdatedAttribute;
		if (existingAttribute == null) {
			logger.info("attribute not already created, creating a new one");

			// force for the new attribute to have the last (1 based) index
			final int numberOfAttribute = size(owner.getAttributes());
			attribute.setIndex(numberOfAttribute + 1);

			validate(attribute);
			createdOrUpdatedAttribute = view.createAttribute(definitionForNew(attribute, owner));
		} else {
			logger.info("attribute already created, updating existing one");
			createdOrUpdatedAttribute = view.updateAttribute(definitionForExisting(attribute, existingAttribute));
		}

		logger.info("setting metadata for attribute '{}'", attribute.getName());
		final Map<MetadataAction, List<Metadata>> elementsByAction = attribute.getMetadata();
		final Store<Metadata> store = DataViewStore.newInstance(view, //
				MetadataGroupable.of(createdOrUpdatedAttribute), //
				MetadataConverter.of(createdOrUpdatedAttribute));
		for (final MetadataAction action : elementsByAction.keySet()) {
			final Iterable<Metadata> elements = elementsByAction.get(action);
			for (final Metadata element : elements) {
				action.accept(new Visitor() {

					@Override
					public void visit(final Create action) {
						store.create(element);
					}

					@Override
					public void visit(final Update action) {
						store.update(element);
					}

					@Override
					public void visit(final Delete action) {
						store.delete(element);
					}

				});
			}
		}

		return createdOrUpdatedAttribute;
	}

	private CMEntryType findOwnerOf(final Attribute attribute) {
		logger.debug("getting entry type with name '{}' and namespace '{}'", attribute.getOwnerName(),
				attribute.getOwnerNamespace());
		CMEntryType entryType;

		final CMIdentifier identifier = identifierFrom(attribute.getOwnerName(), attribute.getOwnerNamespace());

		// try with classes
		entryType = view.findClass(identifier);
		if (entryType != null) {
			logger.debug("class found");
			return entryType;
		}

		// try with domains
		entryType = view.findDomain(identifier);
		if (entryType != null) {
			logger.debug("domain found");
			return entryType;
		}

		logger.warn("not found");
		throw ORMExceptionType.ORM_TYPE_ERROR.createException();
	}

	private void validate(final Attribute attribute) {
		new CMAttributeTypeVisitor() {

			@Override
			public void visit(final BooleanAttributeType attributeType) {
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final CMIdentifier identifier = attributeType.getIdentifier();
				Validate.isTrue(identifier.getNameSpace() == CMIdentifier.DEFAULT_NAMESPACE,
						"non-default namespaces not supported at this level");
				final CMDomain domain = view.findDomain(identifier.getLocalName());
				Validate.isTrue(Arrays.asList(CARDINALITY_1N.value(), CARDINALITY_N1.value()).contains(
						domain.getCardinality()));
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
			}

			public void validate(final Attribute attribute) {
				attribute.getType().accept(this);
			}

			@Override
			public void visit(final StringArrayAttributeType attributeType) {
			}

		} //
		.validate(attribute);
	}

	@Override
	public void deleteOrDeactivate(final Attribute attribute) {
		logger.info("deleting attribute '{}'", attribute.toString());
		final CMEntryType owner = findOwnerOf(attribute);
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
			return;
		}
		try {
			logger.info("deleting metadata for attribute '{}'", attribute.getName());
			final Store<Metadata> store = DataViewStore.newInstance(view, //
					MetadataGroupable.of(existingAttribute), //
					MetadataConverter.of(existingAttribute));
			final Iterable<Metadata> allMetadata = store.list();
			for (final Metadata metadata : allMetadata) {
				store.delete(metadata);
			}

			logger.info("deleting existing attribute '{}'", attribute.getName());
			view.delete(existingAttribute);
		} catch (final Exception e) {
			logger.warn("error deleting attribute", e);
			/**
			 * TODO: move the throw exception to dao level when all exception
			 * system will be re-organized. Here catch only an ORM_CONTAINS_DATA
			 * exception, thrown from dao
			 */
			if (e.getMessage().contains("CM_CONTAINS_DATA")) {
				throw ORMExceptionType.ORM_CONTAINS_DATA.createException();
			}
		}
	}

	@Override
	public void reorder(final Attribute attribute) {
		logger.info("reordering attribute '{}'", attribute.toString());
		final CMEntryType owner = findOwnerOf(attribute);
		final CMAttribute existingAttribute = owner.getAttribute(attribute.getName());
		if (existingAttribute == null) {
			logger.warn("attribute '{}' not found", attribute.getName());
			return;
		}
		view.updateAttribute(definitionForReordering(attribute, existingAttribute));
	}

	@Override
	public void changeClassOrders(final String className, final List<ClassOrder> classOrders) {
		logger.info("changing classorders '{}' for class '{}'", classOrders, className);

		final Map<String, ClassOrder> mappedClassOrders = Maps.uniqueIndex(classOrders, ATTRIBUTE_NAME_AS_KEY);

		final CMClass owner = view.findClass(className);
		for (final CMAttribute attribute : owner.getAttributes()) {
			view.updateAttribute(definitionForClassOrdering(Attribute.newAttribute() //
					.withOwnerName(owner.getName()) //
					.withName(attribute.getName()) //
					.withClassOrder(valueOrDefaultIfNull(mappedClassOrders.get(attribute.getName()))) //
					.build(), //
					attribute));
		}

		final CMFunction function = view.findFunctionByName(UPDATE_CLASS_INDEXES_FUNCTION_NAME);
		if (function != null) {
			final Object[] actualParams = new Object[] { className };
			view.select(anyAttribute(function, FUNCTION_ALIAS)) //
					.from(call(function, actualParams), FUNCTION_ALIAS) //
					.run();
		}
	}

	private int valueOrDefaultIfNull(final ClassOrder classOrder) {
		return (classOrder == null) ? 0 : classOrder.value;
	}

	/**
	 * TODO: remove it and use the create method and update method
	 */
	@Override
	@Deprecated
	public CMDomain createOrUpdate(final Domain domain) {
		logger.info("creating or updating domain '{}'", domain);

		final CMDomain existing = view.findDomain(domain.getName());

		final CMDomain createdOrUpdated;
		if (existing == null) {
			logger.info("domain not already created, creating a new one");
			final CMClass class1 = view.findClass(domain.getIdClass1());
			final CMClass class2 = view.findClass(domain.getIdClass2());
			createdOrUpdated = view.create(definitionForNew(domain, class1, class2));
		} else {
			logger.info("domain already created, updating existing one");
			createdOrUpdated = view.update(definitionForExisting(domain, existing));
		}
		return createdOrUpdated;
	}

	@Override
	public CMDomain create(final Domain domain) {
		final CMDomain existing = view.findDomain(domain.getName());
		final CMDomain createdDomain;
		if (existing != null) {
			logger.error("Error creating a domain with name {}. A domain with the same name already exists.",
					domain.getName());
			throw ORMExceptionType.ORM_ERROR_DOMAIN_CREATE.createException();
		}

		logger.info("Domain not already created, creating a new one");
		final CMClass class1 = view.findClass(domain.getIdClass1());
		final CMClass class2 = view.findClass(domain.getIdClass2());
		createdDomain = view.create(definitionForNew(domain, class1, class2));
		return createdDomain;
	}

	@Override
	public CMDomain update(final Domain domain) {
		final CMDomain existing = view.findDomain(domain.getName());
		final CMDomain updatedDomain;
		if (existing == null) {
			logger.error("Cannot update the domain with name {}. It does not exist", domain.getName());
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}

		logger.info("Updating domain with name {}", domain.getName());
		updatedDomain = view.update(definitionForExisting(domain, existing));
		return updatedDomain;
	}

	@Override
	public void deleteDomainIfExists(final String name) {
		logger.info("deleting domain '{}'", name);

		final CMDomain domain = view.findDomain(name);
		if (domain == null) {
			logger.warn("domain '{}' not found", name);
		} else {
			final boolean hasReference;
			final String cardinality = domain.getCardinality();
			if (asList(CARDINALITY_11.value(), CARDINALITY_1N.value()).contains(cardinality)) {
				final CMClass table = view.findClass(domain.getClass2().getName());
				hasReference = searchReference(table, domain);
			} else if (asList(CARDINALITY_11.value(), CARDINALITY_N1.value()).contains(cardinality)) {
				final CMClass table = view.findClass(domain.getClass1().getName());
				hasReference = searchReference(table, domain);
			} else {
				hasReference = false;
			}

			if (hasReference) {
				throw ORMExceptionType.ORM_DOMAIN_HAS_REFERENCE.createException();
			} else {
				view.delete(domain);
			}
		}
	}

	private static boolean searchReference(final CMClass table, final CMDomain domain) {
		if (classContainsReferenceAttributeToDomain(table, domain)) {
			return true;
		}
		for (final CMClass descendant : table.getDescendants()) {
			if (classContainsReferenceAttributeToDomain(descendant, domain)) {
				return true;
			}
		}
		return false;
	}

	private static boolean classContainsReferenceAttributeToDomain(final CMClass table, final CMDomain domain) {
		for (final CMAttribute attribute : table.getAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = ReferenceAttributeType.class.cast(attributeType);
				final String referenceDomainName = referenceAttributeType.getIdentifier().getLocalName();
				if (referenceDomainName.equals(domain.getIdentifier().getLocalName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Iterable<FunctionItem> functions() {
		return from(view.findAllFunctions()) //
				.transform(toFunctionItem);
	}

}
