package org.cmdbuild.core.api.fluent;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.CreateReport;
import org.cmdbuild.api.fluent.DownloadedReport;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingProcessInstance;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.FunctionCall;
import org.cmdbuild.api.fluent.Lookup;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewProcessInstance;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.ProcessInstanceDescriptor;
import org.cmdbuild.api.fluent.QueryAllLookup;
import org.cmdbuild.api.fluent.QueryClass;
import org.cmdbuild.api.fluent.QuerySingleLookup;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.api.fluent.RelationsQuery;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LogicFluentApiExecutor implements FluentApiExecutor {

	private final DataAccessLogic dataAccessLogic;
	private final LookupLogic lookupLogic;

	public LogicFluentApiExecutor(final DataAccessLogic dataAccessLogic, final LookupLogic lookupLogic) {
		this.dataAccessLogic = dataAccessLogic;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public CardDescriptor create(final NewCard card) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void update(final ExistingCard card) {
		final org.cmdbuild.model.data.Card cardToSave = FLUENT_API_TO_MODEL_CARD.apply(card);
		dataAccessLogic.updateCard(cardToSave);
	}

	final static Function<ExistingCard, org.cmdbuild.model.data.Card> FLUENT_API_TO_MODEL_CARD = new Function<ExistingCard, org.cmdbuild.model.data.Card>() {

		@Override
		public org.cmdbuild.model.data.Card apply(final ExistingCard input) {
			final String className = input.getClassName();
			final org.cmdbuild.model.data.Card.CardBuilder builder = org.cmdbuild.model.data.Card.newInstance()
					.withClassName(className) //
					.withId((input.getId() == null) ? null : input.getId().longValue());
			final Map<String, Object> attributeMap = input.getAttributes();
			for (final Entry<String, Object> entry : attributeMap.entrySet()) {
				builder.withAttribute(entry.getKey(), entry.getValue());
			}
			final org.cmdbuild.model.data.Card result = builder.build();
			return result;
		}
	};

	@Override
	public void delete(final ExistingCard card) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Card fetch(final ExistingCard card) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public List<Card> fetchCards(final QueryClass card) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void create(final NewRelation relation) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void delete(final ExistingRelation relation) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public List<Relation> fetch(final RelationsQuery query) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Map<String, Object> execute(final FunctionCall function) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public DownloadedReport download(final CreateReport report) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public ProcessInstanceDescriptor createProcessInstance(final NewProcessInstance processCard,
			final AdvanceProcess advance) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void updateProcessInstance(final ExistingProcessInstance processCard, final AdvanceProcess advance) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Iterable<Lookup> fetch(final QueryAllLookup queryLookup) {
		final LookupType type = LookupType.newInstance().withName(queryLookup.getType()).build();
		final boolean activeOnly = true;
		final Iterable<org.cmdbuild.data.store.lookup.Lookup> allLookup = lookupLogic.getAllLookup(type, activeOnly);
		final Iterable<Lookup> result = Iterables.transform(allLookup, STORE_TO_API_LOOKUP);
		return result;
	}

	@Override
	public Lookup fetch(final QuerySingleLookup querySingleLookup) {
		final Integer id = querySingleLookup.getId();
		final org.cmdbuild.data.store.lookup.Lookup input = lookupLogic.getLookup(Long.valueOf(id.intValue()));
		final Lookup result = STORE_TO_API_LOOKUP.apply(input);
		return result;
	}

	private final static Function<org.cmdbuild.data.store.lookup.Lookup, Lookup> STORE_TO_API_LOOKUP = //
	new Function<org.cmdbuild.data.store.lookup.Lookup, Lookup>() {
		@Override
		public Lookup apply(final org.cmdbuild.data.store.lookup.Lookup input) {
			return new LookupWrapper(input);
		}
	};
}
