package org.cmdbuild.logic.data.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.exception.ORMException.ORMExceptionType.ORM_CHANGE_LOOKUPTYPE_ERROR;
import static org.cmdbuild.logic.PrivilegeUtils.assure;
import static org.cmdbuild.logic.data.lookup.Util.actives;
import static org.cmdbuild.logic.data.lookup.Util.toLookupType;
import static org.cmdbuild.logic.data.lookup.Util.typesWith;
import static org.cmdbuild.logic.data.lookup.Util.uniques;
import static org.cmdbuild.logic.data.lookup.Util.withId;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class LookupLogic implements Logic {

	private static final Marker marker = MarkerFactory.getMarker(LookupLogic.class.getName());

	private static class Exceptions {

		private Exceptions() {
			// prevents instantiation
		}

		public static NotFoundException lookupTypeNotFound(final LookupType type) {
			return NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(type.name);
		}

		public static NotFoundException lookupNotFound(final Long id) {
			return NotFoundExceptionType.LOOKUP_NOTFOUND.createException(id.toString());
		}

		public static ORMException multipleElementsWithSameId() {
			return ORMExceptionType.ORM_UNIQUE_VIOLATION.createException();
		}

	}

	private static final Comparator<Lookup> NUMBER_COMPARATOR = new Comparator<Lookup>() {
		@Override
		public int compare(final Lookup o1, final Lookup o2) {
			if (o1.number > o2.number) {
				return 1;
			} else if (o1.number < o2.number) {
				return -1;
			}
			return 0;
		}
	};

	private final LookupStore store;
	private final OperationUser operationUser;
	private final CMDataView dataView;

	public LookupLogic( //
			final LookupStore store, //
			final OperationUser operationUser, //
			final CMDataView dataView) {
		this.store = store;
		this.operationUser = operationUser;
		this.dataView = dataView;
	}

	public Iterable<LookupType> getAllTypes() {
		logger.trace(marker, "getting all lookup types");
		return from(store.list()) //
				.transform(toLookupType()) //
				.filter(uniques());
	}

	public void saveLookupType(final LookupType newType, final LookupType oldType) {
		logger.debug(marker, "saving lookup type, new is '{}', old is '{}'", newType, oldType);

		assure(operationUser.hasAdministratorPrivileges());

		if (isBlank(newType.name)) {
			logger.error("invalid name '{}' for lookup type", newType.name);
			throw ORM_CHANGE_LOOKUPTYPE_ERROR.createException();
		}

		final LookupType existingLookupType = typeForNameAndParent(oldType.name, oldType.parent);
		if (existingLookupType == null) {
			logger.debug(marker, "old one not specified, creating a new one");
			final Lookup lookup = Lookup.newInstance() //
					.withType(newType) //
					.withNumber(1) //
					.withActiveStatus(true) //
					.build();
			store.create(lookup);
		} else {
			logger.debug(marker, "old one specified, modifying existing one");
			for (final Lookup lookup : store.listForType(oldType)) {
				final Lookup newLookup = Lookup.newInstance() //
						.withId(lookup.getId()) //
						.withCode(lookup.code) //
						.withDescription(lookup.description) //
						.withType(newType) //
						.withNumber(lookup.number) //
						.withActiveStatus(lookup.active) //
						.withDefaultStatus(lookup.isDefault) //
						.build();
				store.update(newLookup);
			}

			logger.info(marker, "updates existing classes' attributes");
			for (final CMClass existingClass : dataView.findClasses()) {
				logger.debug(marker, "examining class '{}'", existingClass.getIdentifier().getLocalName());
				for (final CMAttribute existingAttribute : existingClass.getAttributes()) {
					logger.debug(marker, "examining attribute '{}'", existingAttribute.getName());
					existingAttribute.getType().accept(new NullAttributeTypeVisitor() {

						@Override
						public void visit(final LookupAttributeType attributeType) {
							if (asList(oldType.name, newType.name).contains(attributeType.getLookupTypeName())) {
								dataView.updateAttribute(attribute(existingAttribute, newType));
							}
						}

						private CMAttributeDefinition attribute(final CMAttribute attribute, final LookupType type) {
							return new CMAttributeDefinition() {

								@Override
								public String getName() {
									return attribute.getName();
								}

								@Override
								public CMEntryType getOwner() {
									return attribute.getOwner();
								}

								@Override
								public CMAttributeType<?> getType() {
									return new LookupAttributeType(type.name);
								}

								@Override
								public String getDescription() {
									return attribute.getDescription();
								}

								@Override
								public String getDefaultValue() {
									return attribute.getDefaultValue();
								}

								@Override
								public boolean isDisplayableInList() {
									return attribute.isDisplayableInList();
								}

								@Override
								public boolean isMandatory() {
									return attribute.isMandatory();
								}

								@Override
								public boolean isUnique() {
									return attribute.isUnique();
								}

								@Override
								public boolean isActive() {
									return attribute.isActive();
								}

								@Override
								public Mode getMode() {
									return attribute.getMode();
								}

								@Override
								public int getIndex() {
									return attribute.getIndex();
								}

								@Override
								public String getGroup() {
									return attribute.getGroup();
								}

								@Override
								public int getClassOrder() {
									return attribute.getClassOrder();
								}

								@Override
								public String getEditorType() {
									return attribute.getEditorType();
								}

								@Override
								public String getForeignKeyDestinationClassName() {
									return attribute.getForeignKeyDestinationClassName();
								}

								@Override
								public String getFilter() {
									return attribute.getFilter();
								}

							};
						}

					});
				}
			}
		}
	}

	public Iterable<Lookup> getAllLookup( //
			final LookupType type, //
			final boolean activeOnly //
	) {

		logger.debug(marker, "getting all lookups for type '{}'", type);

		final LookupType realType = typeFor(typesWith(type.name));

		logger.trace(marker, "getting all lookups for real type '{}'", realType);

		final Iterable<Lookup> elements = store.listForType(realType);

		if (!elements.iterator().hasNext()) {
			logger.error(marker, "no lookup was found for type '{}'", realType);
			throw Exceptions.lookupTypeNotFound(realType);
		}

		final List<Lookup> list = newArrayList(elements);

		logger.trace(marker, "ordering elements");
		sort(list, NUMBER_COMPARATOR);

		return from(list) //
				.filter(actives(activeOnly));
	}

	public Iterable<Lookup> getAllLookupOfParent(final LookupType type) {
		logger.debug(marker, "getting all lookups for the parent of type '{}'", type);
		final LookupType current = typeFor(typesWith(type.name));
		if (current.parent == null) {
			return new LinkedList<Lookup>();
		}

		final LookupType parent = typeFor(typesWith(current.parent));
		return store.listForType(parent);
	}

	public Lookup getLookup(final Long id) {
		logger.debug(marker, "getting lookup with id '{}'", id);
		final Iterator<Lookup> elements = from(store.list()) //
				.filter(new Predicate<Lookup>() {
					@Override
					public boolean apply(final Lookup input) {
						return input.getId().equals(id);
					};
				}) //
				.iterator();
		if (!elements.hasNext()) {
			throw Exceptions.lookupNotFound(id);
		}
		final Lookup lookup = elements.next();
		if (elements.hasNext()) {
			logger.error(marker, "multiple elements with id '{}'", id);
			throw Exceptions.multipleElementsWithSameId();
		}
		return lookup;
	}

	public void enableLookup(final Long id) {
		logger.debug(marker, "enabling lookup with id '{}'", id);
		assure(operationUser.hasAdministratorPrivileges());
		setActiveStatus(true, id);
	}

	public void disableLookup(final Long id) {
		logger.debug(marker, "disabling lookup with id '{}'", id);
		assure(operationUser.hasAdministratorPrivileges());
		setActiveStatus(false, id);
	}

	private void setActiveStatus(final boolean status, final Long id) {
		logger.debug(marker, "setting active status '{}' for lookup with id '{}'", status, id);
		if (id <= 0) {
			logger.warn(marker, "invalid id '{}', exiting without doing nothing", id);
			return;
		}

		logger.trace(marker, "getting lookup with id '{}'", id);
		final Iterator<Lookup> shouldBeOneOnly = from(store.list()) //
				.filter(withId(id)) //
				.iterator();

		if (!shouldBeOneOnly.hasNext()) {
			throw Exceptions.lookupNotFound(id);
		}

		logger.trace(marker, "updating lookup active to '{}'", status);
		final Lookup lookup = Lookup.newInstance() //
				.clone(shouldBeOneOnly.next()) //
				.withActiveStatus(status) //
				.build();

		store.update(lookup);
	}

	private LookupType typeForNameAndParent(final String name, final String parent) {
		logger.debug(marker, "getting lookup type with name '{}' and parent '{}'", name, parent);
		return typeFor(typesWith(name, parent));
	}

	public LookupType typeFor(final String lookupTypeName) {
		return typeFor(typesWith(lookupTypeName));
	}

	private LookupType typeFor(final Predicate<LookupType> predicate) {
		logger.trace(marker, "getting lookup type for predicate");
		final Iterator<LookupType> shouldBeOneOnly = from(getAllTypes()) //
				.filter(predicate) //
				.iterator();
		final LookupType found;
		if (!shouldBeOneOnly.hasNext()) {
 			logger.warn(marker, "lookup type not found");
			found = null;
		} else {
			logger.info(marker, "lookup type successfully found");
			found = shouldBeOneOnly.next();
		}
		if ((found != null) && shouldBeOneOnly.hasNext()) {
			logger.error(marker, "more than one lookup type has been found");
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		return found;
	}

	public Long createOrUpdateLookup(final Lookup lookup) {
		logger.info(marker, "creating or updating lookup '{}'", lookup);

		assure(operationUser.hasAdministratorPrivileges());

		final Lookup lookupWithRealType = Lookup.newInstance() //
				.clone(lookup) //
				.withType(typeFor(typesWith(lookup.type.name))) //
				.build();

		final Long id;
		if (isNotExistent(lookupWithRealType)) {
			logger.info(marker, "creating lookup '{}'", lookupWithRealType);

			logger.debug(marker, "checking lookup number ('{}'), if not valid assigning a valid one",
					lookupWithRealType.number);
			final Lookup toBeCreated;
			if (hasNoValidNumber(lookupWithRealType)) {
				final int count = size(store.listForType(lookupWithRealType.type));
				toBeCreated = Lookup.newInstance() //
						.clone(lookupWithRealType) //
						.withNumber(count + 1) //
						.build();
			} else {
				toBeCreated = lookupWithRealType;
			}

			final Storable created = store.create(toBeCreated);
			id = Long.valueOf(created.getIdentifier());
		} else {
			logger.info(marker, "updating lookup '{}'", lookupWithRealType);

			logger.debug(marker, "checking lookup number ('{}'), if not valid assigning a valid one",
					lookupWithRealType.number);
			final Lookup toBeUpdated;
			if (hasNoValidNumber(lookupWithRealType)) {
				final Lookup actual = store.read(lookupWithRealType);
				toBeUpdated = Lookup.newInstance() //
						.clone(lookupWithRealType) //
						.withNumber(actual.number) //
						.build();
			} else {
				toBeUpdated = lookupWithRealType;
			}

			store.update(toBeUpdated);
			id = lookupWithRealType.getId();
		}
		return id;
	}

	private static boolean isNotExistent(final Lookup lookup) {
		return lookup.getId() == null || lookup.getId() <= 0;
	}

	private static boolean hasNoValidNumber(final Lookup lookup) {
		return lookup.number == null || lookup.number <= 0;
	}

	/**
	 * Reorders lookups.
	 * 
	 * @param lookupType
	 *            the lookup's type of elements that must be ordered.
	 * @param positions
	 *            the positions of the elements; key is the id of the lookup
	 *            element, value is the new index.
	 */
	public void reorderLookup(final LookupType type, final Map<Long, Integer> positions) {
		logger.trace(marker, "reordering lookups for type '{}'", type);

		assure(operationUser.hasAdministratorPrivileges());

		final LookupType realType = typeFor(typesWith(type.name));
		final Iterable<Lookup> lookups = store.listForType(realType);
		for (final Lookup lookup : lookups) {
			if (positions.containsKey(lookup.getId())) {
				final int index = positions.get(lookup.getId());
				final Lookup updated = Lookup.newInstance() //
						.clone(lookup) //
						.withNumber(index) //
						.build();
				store.update(updated);
			}
		}
	}

}
