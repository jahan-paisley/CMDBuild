package org.cmdbuild.logic.data.access.resolver;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Map;
import java.util.Set;

import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ForeignReferenceResolver<T extends CMEntry> {

	public static abstract class EntryFiller<T extends CMEntry> {

		protected Map<String, Object> values = Maps.newHashMap();
		protected T input;

		public void setInput(final T input) {
			this.input = input;
			this.values = Maps.newHashMap();
		}

		public void setValue(final String name, final Object value) {
			values.put(name, value);
		}

		public abstract T getOutput();

	}

	public static class ForeignReferenceResolverBuilder<T extends CMEntry> implements
			Builder<ForeignReferenceResolver<T>> {

		private CMDataView systemDataView;
		private CMClass entryType;
		private Iterable<T> entries;
		public EntryFiller<T> entryFiller;
		public LookupStore lookupStore;
		public AbstractSerializer<T> serializer;

		@Override
		public ForeignReferenceResolver<T> build() {
			return new ForeignReferenceResolver<T>(this);
		}

		public ForeignReferenceResolverBuilder<T> withSystemDataView(final CMDataView value) {
			systemDataView = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withEntryType(final CMClass value) {
			entryType = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withEntries(final Iterable<T> value) {
			entries = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withEntryFiller(final EntryFiller<T> value) {
			entryFiller = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withLookupStore(final LookupStore value) {
			lookupStore = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withSerializer(final AbstractSerializer<T> value) {
			serializer = value;
			return this;
		}

	}

	public static <T extends CMEntry> ForeignReferenceResolverBuilder<T> newInstance() {
		return new ForeignReferenceResolverBuilder<T>();
	}

	private final CMDataView systemDataView;
	private final CMClass entryType;
	private final Iterable<T> entries;
	private final EntryFiller<T> entryFiller;
	private final LookupStore lookupStore;
	private final AbstractSerializer<T> serializer;

	private final Map<CMClass, Set<Long>> idsByEntryType = newHashMap();
	private final Map<Long, String> representationsById = newHashMap();

	public ForeignReferenceResolver(final ForeignReferenceResolverBuilder<T> builder) {
		this.systemDataView = builder.systemDataView;
		this.entryType = (builder.entryType == null) ? builder.systemDataView.findClass("Class") : builder.entryType;
		this.entries = builder.entries;
		this.entryFiller = builder.entryFiller;
		this.lookupStore = builder.lookupStore;
		this.serializer = builder.serializer;
	}

	public Iterable<T> resolve() {

		return from(entries) //
				.transform(new Function<T, T>() {

					@Override
					public T apply(final T input) {

						entryFiller.setInput(input);

						for (final CMAttribute attribute : input.getType().getAllAttributes()) {
							final String attributeName = attribute.getName();
							
							final Object rawValue;
							try {
								rawValue = input.get(attributeName);
							} catch (IllegalArgumentException e) {
								// This could happen for ImportCSV because
								// the fake card has no the whole attributes
								// of the relative CMClass
								continue;
							}

							/**
							 * must be kept in the same order. If not, an
							 * attribute with null value will not be returned
							 */
							entryFiller.setValue(attributeName, rawValue);
							if (rawValue == null) {
								continue;
							}

							serializer.setRawValue(rawValue);
							serializer.setAttributeName(attributeName);
							serializer.setLookupStore(lookupStore);
							serializer.setEntryFiller(entryFiller);
							attribute.getType().accept(serializer);

						}
						return entryFiller.getOutput();
					}

				});
	}

}
