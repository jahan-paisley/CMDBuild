package org.cmdbuild.logic.commands;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.DomainHistory.history;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.data.Card;

public class GetRelationHistory extends AbstractGetRelation {

	public GetRelationHistory(final CMDataView view) {
		super(view);
	}

	public GetRelationHistoryResponse exec(final Card source) {
		return exec(source, anyDomain());
	}

	public GetRelationHistoryResponse exec(final Card source, final CMDomain domain) {
		Validate.notNull(source);
		final CMClass sourceClass = view.findClass(source.getClassName());
		final Iterable<? extends CMDomain> domains = view.findDomainsFor(sourceClass);
		final CMQueryResult relationList;
		if (isEmpty(domains)) {
			relationList = CMQueryResult.EMPTY;
		} else {
			relationList = getRelationQuery(source, history(domain)).run();
		}
		return createResponse(relationList);
	}

	private GetRelationHistoryResponse createResponse(final CMQueryResult relationList) {
		final GetRelationHistoryResponse out = new GetRelationHistoryResponse();
		for (final CMQueryRow row : relationList) {
			final QueryRelation rel = row.getRelation(DOM_ALIAS);
			final CMCard dst = row.getCard(DST_ALIAS);
			out.addRelation(rel, dst);
		}
		return out;
	}

	public static class GetRelationHistoryResponse extends GetRelationResponse implements Iterable<RelationInfo> {

		private final List<RelationInfo> relations;

		private GetRelationHistoryResponse() {
			this.relations = new ArrayList<RelationInfo>();
		}

		@Override
		protected void doAddRelation(final RelationInfo relationInfo) {
			relations.add(relationInfo);
		}

		@Override
		public Iterator<RelationInfo> iterator() {
			return relations.iterator();
		}

	}
}
