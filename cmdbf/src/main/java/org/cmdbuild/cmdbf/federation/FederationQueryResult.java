package org.cmdbuild.cmdbf.federation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.cmdbf.CMDBfItem;
import org.cmdbuild.cmdbf.CMDBfQueryResult;
import org.cmdbuild.cmdbf.CMDBfRelationship;
import org.cmdbuild.cmdbf.ContentSelectorFunction;
import org.cmdbuild.cmdbf.ItemSet;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.cmdbf.PathSet;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EdgesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NodesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class FederationQueryResult extends CMDBfQueryResult {

	private final Collection<QueryResultType> mdrQueryResults;

	public FederationQueryResult(final QueryType body, final Collection<ManagementDataRepository> mdrCollection)
			throws QueryErrorFault, InvalidPropertyTypeFault, UnknownTemplateIDFault, ExpensiveQueryErrorFault,
			XPathErrorFault, UnsupportedSelectorFault, UnsupportedConstraintFault {
		super(body);
		mdrQueryResults = new ArrayList<QueryResultType>();
		for (final ManagementDataRepository mdr : mdrCollection) {
			// TODO preprocess query for reconciliation
			mdrQueryResults.add(mdr.graphQuery(body));
		}
		execute();
	}

	@Override
	protected Collection<CMDBfItem> getItems(final String templateId, final Set<CMDBfId> instanceId,
			final RecordConstraintType recordConstraint) {
		final Collection<CMDBfItem> items = new ArrayList<CMDBfItem>();
		for (final QueryResultType result : mdrQueryResults) {
			if (result.getNodes() != null) {
				final NodesType templateResult = Iterables.find(result.getNodes(), new Predicate<NodesType>() {
					@Override
					public boolean apply(final NodesType input) {
						return templateId.equals(input.getTemplateId());
					}
				});
				if (templateResult != null) {
					for (final ItemType item : templateResult.getItem()) {
						// TODO items reconciliation
						final CMDBfItem cmdbfItem = new CMDBfItem(item);
						if (filter(cmdbfItem, instanceId, recordConstraint)) {
							items.add(cmdbfItem);
						}
					}
				}
			}
		}
		return items;
	}

	@Override
	protected Collection<CMDBfRelationship> getRelationships(final String templateId, final Set<CMDBfId> instanceId,
			final Set<CMDBfId> source, final Set<CMDBfId> target, final RecordConstraintType recordConstraint) {
		final Collection<CMDBfRelationship> relationships = new ArrayList<CMDBfRelationship>();
		for (final QueryResultType result : mdrQueryResults) {
			if (result.getEdges() != null) {
				final EdgesType templateResult = Iterables.find(result.getEdges(), new Predicate<EdgesType>() {
					@Override
					public boolean apply(final EdgesType input) {
						return templateId.equals(input.getTemplateId());
					}
				});
				if (templateResult != null) {
					// TODO items reconciliation
					for (final RelationshipType relationship : templateResult.getRelationship()) {
						final CMDBfRelationship cmdbfRelationship = new CMDBfRelationship(relationship);
						if (source.contains(cmdbfRelationship.getSource())
								&& target.contains(cmdbfRelationship.getTarget())) {
							if (filter(cmdbfRelationship, instanceId, recordConstraint)) {
								relationships.add(cmdbfRelationship);
							}
						}
					}
				}
			}
		}
		return relationships;
	}

	@Override
	protected void fetchItemRecords(final String templateId, final ItemSet<CMDBfItem> items,
			final ContentSelectorType contentSelector) {
		for (final CMDBfItem item : items) {
			Iterables.transform(item.records(), new ContentSelectorFunction(contentSelector));
		}

	}

	@Override
	protected void fetchRelationshipRecords(final String templateId, final PathSet relationships,
			final ContentSelectorType contentSelector) {
		for (final CMDBfRelationship relationship : relationships) {
			Iterables.transform(relationship.records(), new ContentSelectorFunction(contentSelector));
		}

	}

	@Override
	protected CMDBfId resolveAlias(final MdrScopedIdType alias) {
		return alias instanceof CMDBfId ? (CMDBfId) alias : new CMDBfId(alias);
	}

	@Override
	protected void fetchAlias(final CMDBfItem item) {
	}
}
