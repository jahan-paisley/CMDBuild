package org.cmdbuild.data.store;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.Holder;
import org.cmdbuild.common.SingletonHolder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.Utils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class DataViewStore<T extends Storable> implements Store<T> {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewStore.class.getName());

	private static final String DEFAULT_IDENTIFIER_ATTRIBUTE_NAME = ID_ATTRIBUTE;

	public static final Groupable NOT_GROUPABLE = new Groupable() {

		@Override
		public String getGroupAttributeName() {
			return null;
		}

		@Override
		public Object getGroupAttributeValue() {
			throw new IllegalStateException("should never call this");
		}

	};

	public static interface StorableConverter<T extends Storable> {

		String SYSTEM_USER = "system"; // FIXME

		/**
		 * @return the name of the class in the store.
		 */
		String getClassName();

		/**
		 * @return the name of the identifier attribute.
		 */
		String getIdentifierAttributeName();

		/**
		 * Converts a card into a {@link Storable}.
		 * 
		 * @param card
		 *            the cards that needs to be converted.
		 * 
		 * @return the instance of {@link Storable} representing the card.
		 */
		Storable storableOf(CMCard card);

		/**
		 * Converts a card into a {@link T}.
		 * 
		 * @param card
		 *            the cards that needs to be converted.
		 * 
		 * @return the instance of {@link T} representing the card.
		 */
		T convert(CMCard card);

		/**
		 * Converts a generic type into a map of <String, Object>, corresponding
		 * to attribute <name, value>
		 * 
		 * @param storable
		 * @return
		 */
		Map<String, Object> getValues(T storable);

		String getUser(T storable);

	}

	public static class ForwardingStorableConverter<T extends Storable> implements StorableConverter<T> {

		private final StorableConverter<T> inner;

		protected ForwardingStorableConverter(final StorableConverter<T> storableConverter) {
			this.inner = storableConverter;
		}

		@Override
		public String getClassName() {
			return inner.getClassName();
		}

		@Override
		public String getIdentifierAttributeName() {
			return inner.getIdentifierAttributeName();
		}

		@Override
		public Storable storableOf(final CMCard card) {
			return inner.storableOf(card);
		}

		@Override
		public T convert(final CMCard card) {
			return inner.convert(card);
		}

		@Override
		public Map<String, Object> getValues(final T storable) {
			return inner.getValues(storable);
		}

		@Override
		public String getUser(final T storable) {
			return inner.getUser(storable);
		}

	}

	public static abstract class BaseStorableConverter<T extends Storable> implements StorableConverter<T> {

		protected Logger logger = DataViewStore.logger;

		@Override
		public String getIdentifierAttributeName() {
			return DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;
		}

		@Override
		public Storable storableOf(final CMCard card) {
			return new Storable() {

				@Override
				public String getIdentifier() {
					final String attributeName = getIdentifierAttributeName();
					final String value;
					if (DEFAULT_IDENTIFIER_ATTRIBUTE_NAME.equals(attributeName)) {
						value = Long.toString(card.getId());
					} else {
						value = card.get(getIdentifierAttributeName(), String.class);
					}
					return value;
				}

			};
		}

		@Override
		public String getUser(final T storable) {
			return SYSTEM_USER;
		};

		// TODO use static methods directly instead
		protected String readStringAttribute(final CMCard card, final String attributeName) {
			return Utils.readString(card, attributeName);
		}

		// TODO use static methods directly instead
		protected Long readLongAttribute(final CMCard card, final String attributeName) {
			return Utils.readLong(card, attributeName);
		}

	}

	public static <T extends Storable> DataViewStore<T> newInstance(final CMDataView view,
			final StorableConverter<T> converter) {
		return new DataViewStore<T>(view, NOT_GROUPABLE, converter);
	}

	public static <T extends Storable> DataViewStore<T> newInstance(final CMDataView view, final Groupable groupable,
			final StorableConverter<T> converter) {
		return new DataViewStore<T>(view, groupable, converter);
	}

	private final CMDataView view;
	private final Groupable groupable;
	private final StorableConverter<T> converter;
	private final Holder<CMClass> storeClassHolder;

	private DataViewStore(final CMDataView view, final Groupable groupable, final StorableConverter<T> converter) {
		this.view = view;
		this.groupable = groupable;
		this.converter = wrap(converter);
		this.storeClassHolder = new SingletonHolder<CMClass>() {

			@Override
			protected CMClass doGet() {
				final String className = converter.getClassName();
				final CMClass target = view.findClass(className);
				if (target == null) {
					logger.error(marker, "class '{}' has not been found", converter.getClassName());
					throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
				}
				return target;
			}

		};
	}

	private StorableConverter<T> wrap(final StorableConverter<T> converter) {
		return new ForwardingStorableConverter<T>(converter) {

			@Override
			public String getIdentifierAttributeName() {
				final String name = super.getIdentifierAttributeName();
				return (name == null) ? DEFAULT_IDENTIFIER_ATTRIBUTE_NAME : name;
			}

		};
	}

	private CMClass storeClass() {
		return storeClassHolder.get();
	}

	@Override
	public Storable create(final T storable) {
		logger.debug(marker, "creating a new storable element");

		logger.trace(marker, "getting data to be stored");
		final String user = converter.getUser(storable);
		final Map<String, Object> values = converter.getValues(storable);

		logger.trace(marker, "filling new card's attributes");
		final CMCardDefinition card = view.createCardFor(storeClass());
		fillCard(card, values, user);

		logger.debug(marker, "saving card");
		return converter.storableOf(card.save());
	}

	@Override
	public T read(final Storable storable) {
		logger.info(marker, "reading storable element with identifier '{}'", storable.getIdentifier());

		final CMCard card = findCard(storable);

		logger.debug(marker, "converting card to storable element");
		return converter.convert(card);
	}

	@Override
	public void update(final T storable) {
		logger.debug(marker, "updating storable element with identifier '{}'", storable.getIdentifier());

		logger.trace(marker, "getting data to be stored");
		final String user = converter.getUser(storable);
		final Map<String, Object> values = converter.getValues(storable);

		logger.trace(marker, "filling existing card's attributes");
		final CMCard card = findCard(storable);
		final CMCardDefinition updatedCard = view.update(card);
		fillCard(updatedCard, values, user);

		logger.debug(marker, "saving card");
		updatedCard.save();
	}

	@Override
	public void delete(final Storable storable) {
		logger.debug(marker, "deleting storable element with identifier '{}'", storable.getIdentifier());
		final CMCard cardToDelete = findCard(storable);
		view.delete(cardToDelete);
	}

	/**
	 * Returns the {@link CMCard} corresponding to the {@link Storable} object.<br>
	 */
	private CMCard findCard(final Storable storable) {
		logger.debug(marker, "looking for storable element with identifier '{}'", storable.getIdentifier());
		return view //
				.select(anyAttribute(storeClass())) //
				.from(storeClass()) //
				.where(whereClauseFor(storable)) //
				.run() //
				.getOnlyRow() //
				.getCard(storeClass());
	}

	private void fillCard(final CMCardDefinition card, final Map<String, Object> values, final String user) {
		logger.debug(marker, "filling card's attributes with values '{}'", values);
		for (final Entry<String, Object> entry : values.entrySet()) {
			logger.debug(marker, "setting attribute '{}' with value '{}'", entry.getKey(), entry.getValue());
			card.set(entry.getKey(), entry.getValue());
		}
		card.setUser(user);
	}

	/**
	 * Builds the where clause for the specified {@link Storable} object.
	 */
	private WhereClause whereClauseFor(final Storable storable) {
		logger.debug(marker, "building specific where clause");

		final String attributeName = converter.getIdentifierAttributeName();
		final Object attributeValue;
		if (attributeName == DEFAULT_IDENTIFIER_ATTRIBUTE_NAME) {
			logger.debug(marker, "using default one identifier attribute, converting to default type");
			attributeValue = Long.parseLong(storable.getIdentifier());
		} else {
			attributeValue = storable.getIdentifier();
		}

		return and(builtInGroupWhereClause(), condition(attribute(storeClass(), attributeName), eq(attributeValue)));
	}

	@Override
	public List<T> list() {
		logger.debug(marker, "listing all storable elements");
		return list(NOT_GROUPABLE);
	}

	@Override
	public List<T> list(final Groupable groupable) {
		logger.debug(marker, "listing all storable elements with additional grouping condition '{}'", groupable);
		final CMQueryResult result = view //
				.select(anyAttribute(storeClass())) //
				.from(storeClass()) //
				.where(and(builtInGroupWhereClause(), groupWhereClause(groupable))) //
				.run();

		final List<T> list = transform(newArrayList(result), new Function<CMQueryRow, T>() {
			@Override
			public T apply(final CMQueryRow input) {
				return converter.convert(input.getCard(storeClass()));
			}
		});
		return list;
	}

	/**
	 * Creates a {@link WhereClause} for the grouping.
	 * 
	 * @return the {@link WhereClause} for the grouping, {@link TrueWhereClause}
	 *         if no grouping is available.
	 */
	private WhereClause builtInGroupWhereClause() {
		logger.debug(marker, "building built-in group where clause");
		return groupWhereClause(groupable);
	}

	private WhereClause groupWhereClause(final Groupable groupable) {
		logger.debug(marker, "building group where clause");
		final WhereClause clause;
		final String attributeName = groupable.getGroupAttributeName();
		if (attributeName != null) {
			logger.debug(marker, "group attribute name is '{}', building where clause", attributeName);
			final Object attributeValue = groupable.getGroupAttributeValue();
			clause = condition(attribute(storeClass(), attributeName), eq(attributeValue));
		} else {
			logger.debug(marker, "group attribute name not specified");
			clause = trueWhereClause();
		}
		return clause;
	}

}
