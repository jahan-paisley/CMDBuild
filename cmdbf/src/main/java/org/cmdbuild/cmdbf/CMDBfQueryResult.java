package org.cmdbuild.cmdbf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.logger.Log;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EdgesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemTemplateType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NodesType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipTemplateType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;

import com.google.common.collect.Iterables;

public abstract class CMDBfQueryResult extends QueryResultType {
	private final QueryType body;

	public CMDBfQueryResult(final QueryType body) throws QueryErrorFault {
		this.body = body;
	}

	public void execute() throws QueryErrorFault {
		final Map<String, ItemSet<CMDBfItem>> itemMap = new HashMap<String, ItemSet<CMDBfItem>>();
		final Map<String, PathSet> relationshipMap = new HashMap<String, PathSet>();

		for (final ItemTemplateType itemTemplate : body.getItemTemplate()) {
			final ItemSet<CMDBfItem> templateItems = new ItemSet<CMDBfItem>();
			itemMap.put(itemTemplate.getId(), templateItems);
			templateItems.addAll(getItems(itemTemplate));
		}

		for (final RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {
			final PathSet pathSet = new PathSet();
			relationshipMap.put(relationshipTemplate.getId(), pathSet);

			ItemSet<CMDBfItem> sourceSet = null;
			if (relationshipTemplate.getSourceTemplate() != null) {
				final ItemTemplateType sourceTemplate = (ItemTemplateType) relationshipTemplate.getSourceTemplate()
						.getRef();
				if (sourceTemplate != null) {
					sourceSet = itemMap.get(sourceTemplate.getId());
				}
			}

			ItemSet<CMDBfItem> targetSet = null;
			if (relationshipTemplate.getTargetTemplate() != null) {
				final ItemTemplateType targetTemplate = (ItemTemplateType) relationshipTemplate.getTargetTemplate()
						.getRef();
				if (targetTemplate != null) {
					targetSet = itemMap.get(targetTemplate.getId());
				}
			}

			if (relationshipTemplate.getDepthLimit() != null) {
				ItemSet<CMDBfItem> intermediateSet;
				final ItemTemplateType intermediateTemplate = (ItemTemplateType) relationshipTemplate.getDepthLimit()
						.getIntermediateItemTemplate();
				if (intermediateTemplate != null) {
					intermediateSet = itemMap.get(intermediateTemplate.getId());
				} else {
					intermediateSet = new ItemSet<CMDBfItem>();
				}
				final ItemSet<CMDBfItem> visitedSet = new ItemSet<CMDBfItem>();
				visitedSet.addAll(sourceSet);
				boolean loop = true;
				for (long i = 0; loop; i++) {
					for (final CMDBfRelationship relationship : getRelationships(relationshipTemplate, sourceSet,
							targetSet)) {
						pathSet.add(relationship);
					}
					final ItemSet<CMDBfItem> nextSourceSet = new ItemSet<CMDBfItem>();
					for (final CMDBfRelationship relationship : getRelationships(relationshipTemplate, sourceSet,
							intermediateSet)) {
						pathSet.add(relationship);
						final CMDBfItem target = intermediateSet.get(relationship.getTarget());
						if (visitedSet.add(target)) {
							nextSourceSet.add(target);
						}
					}
					sourceSet = nextSourceSet;
					loop = !sourceSet.isEmpty()
							&& (relationshipTemplate.getDepthLimit().getMaxIntermediateItems() == null || i < relationshipTemplate
									.getDepthLimit().getMaxIntermediateItems().longValue());
				}
			} else {
				pathSet.addAll(getRelationships(relationshipTemplate, sourceSet, targetSet));
			}
		}

		boolean loop;
		do {
			loop = false;
			for (final RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {
				ItemTemplateType sourceTemplate = null;
				ItemSet<CMDBfItem> sourceSet = null;
				Integer sourceMin = null;
				Integer sourceMax = null;
				if (relationshipTemplate.getSourceTemplate() != null) {
					sourceTemplate = (ItemTemplateType) relationshipTemplate.getSourceTemplate().getRef();
					if (sourceTemplate != null) {
						sourceSet = itemMap.get(sourceTemplate.getId());
					}
					sourceMin = relationshipTemplate.getSourceTemplate().getMinimum();
					sourceMax = relationshipTemplate.getSourceTemplate().getMaximum();
				}

				ItemTemplateType targetTemplate = null;
				ItemSet<CMDBfItem> targetSet = null;
				Integer targetMin = null;
				Integer targetMax = null;
				if (relationshipTemplate.getTargetTemplate() != null) {
					targetTemplate = (ItemTemplateType) relationshipTemplate.getTargetTemplate().getRef();
					if (targetTemplate != null) {
						targetSet = itemMap.get(targetTemplate.getId());
					}
					targetMin = relationshipTemplate.getTargetTemplate().getMinimum();
					targetMax = relationshipTemplate.getTargetTemplate().getMaximum();
				}

				ItemTemplateType intermediateTemplate = null;
				ItemSet<CMDBfItem> intermediateSet = new ItemSet<CMDBfItem>();
				if (relationshipTemplate.getDepthLimit() != null) {
					intermediateTemplate = (ItemTemplateType) relationshipTemplate.getDepthLimit()
							.getIntermediateItemTemplate();
					if (intermediateTemplate != null) {
						intermediateSet = itemMap.get(intermediateTemplate.getId());
					}
				}

				final PathSet pathSet = relationshipMap.get(relationshipTemplate.getId());
				removeInvalidPaths(pathSet, sourceSet, intermediateSet, targetSet, sourceMin, sourceMax, targetMin,
						targetMax);
			}

			for (final ItemTemplateType itemTemplate : body.getItemTemplate()) {
				final ItemSet<CMDBfItem> nodeSet = itemMap.get(itemTemplate.getId());
				for (final RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {

					String sourceTemplateId = null;
					if (relationshipTemplate.getSourceTemplate() != null) {
						final ItemTemplateType sourceTemplate = (ItemTemplateType) relationshipTemplate
								.getSourceTemplate().getRef();
						if (sourceTemplate != null) {
							sourceTemplateId = sourceTemplate.getId();
						}
					}

					String targetTemplateId = null;
					if (relationshipTemplate.getTargetTemplate() != null) {
						final ItemTemplateType targetTemplate = (ItemTemplateType) relationshipTemplate
								.getTargetTemplate().getRef();
						if (targetTemplate != null) {
							targetTemplateId = targetTemplate.getId();
						}
					}

					String intermediateTemplateId = null;
					if (relationshipTemplate.getDepthLimit() != null) {
						final ItemTemplateType intermediateTemplate = (ItemTemplateType) relationshipTemplate
								.getDepthLimit().getIntermediateItemTemplate();
						if (intermediateTemplate != null) {
							intermediateTemplateId = intermediateTemplate.getId();
						}
					}

					final PathSet pathSet = relationshipMap.get(relationshipTemplate.getId());

					if (itemTemplate.getId().equals(sourceTemplateId)
							|| itemTemplate.getId().equals(intermediateTemplateId)) {
						final Set<CMDBfId> sourceSet = new HashSet<CMDBfId>();
						for (final CMDBfId id : pathSet.sourceSet()) {
							final Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(id);
							if (relationSet != null && !relationSet.isEmpty()) {
								sourceSet.add(id);
							}
						}
						loop |= nodeSet.retainAll(sourceSet);
					}

					if (itemTemplate.getId().equals(targetTemplateId)
							|| itemTemplate.getId().equals(intermediateTemplateId)) {
						final Set<CMDBfId> targetSet = new HashSet<CMDBfId>();

						for (final CMDBfId id : pathSet.targetSet()) {
							final Collection<CMDBfRelationship> relationSet = pathSet.relationSetByTarget(id);
							if (relationSet != null && !relationSet.isEmpty()) {
								targetSet.add(id);
							}
						}
						loop |= nodeSet.retainAll(targetSet);
					}
				}
			}

		} while (loop);

		try {
			for (final ItemTemplateType itemTemplate : body.getItemTemplate()) {
				if (!itemTemplate.isSuppressFromResult()) {
					final NodesType nodesResult = new NodesType();
					nodesResult.setTemplateId(itemTemplate.getId());
					getNodes().add(nodesResult);

					final ItemSet<CMDBfItem> itemSet = itemMap.get(itemTemplate.getId());
					fetchItemRecords(itemTemplate.getId(), itemSet, itemTemplate.getContentSelector());
					for (final CMDBfItem item : itemSet) {
						fetchAlias(item);
						final ItemType resultItem = new ItemType();
						resultItem.getInstanceId().addAll(item.instanceIds());
						nodesResult.getItem().add(resultItem);
						resultItem.getRecord().addAll(item.records());
					}
				}
			}

			for (final RelationshipTemplateType relationshipTemplate : body.getRelationshipTemplate()) {
				if (!relationshipTemplate.isSuppressFromResult()) {
					final EdgesType edgesResult = new EdgesType();
					edgesResult.setTemplateId(relationshipTemplate.getId());
					getEdges().add(edgesResult);

					final PathSet relationshipSet = relationshipMap.get(relationshipTemplate.getId());
					fetchRelationshipRecords(relationshipTemplate.getId(), relationshipSet,
							relationshipTemplate.getContentSelector());
					for (final CMDBfRelationship relationship : relationshipSet) {
						fetchAlias(relationship);
						final RelationshipType resultRelationship = new RelationshipType();
						resultRelationship.getInstanceId().addAll(relationship.instanceIds());
						resultRelationship.setSource(relationship.getSource());
						resultRelationship.setTarget(relationship.getTarget());
						edgesResult.getRelationship().add(resultRelationship);
						resultRelationship.getRecord().addAll(relationship.records());
					}
				}
			}
		} catch (final Throwable e) {
			Log.CMDBUILD.error("CMDBf graphQuery", e);
			throw new QueryErrorFault(e.getMessage(), e);
		}
	}

	private Set<CMDBfItem> getItems(final ItemTemplateType itemTemplate) {
		ItemSet<CMDBfItem> itemTemplateResultSet = null;
		Set<CMDBfId> instanceId = null;
		if (itemTemplate.getInstanceIdConstraint() != null) {
			instanceId = new HashSet<CMDBfId>();
			for (final MdrScopedIdType alias : itemTemplate.getInstanceIdConstraint().getInstanceId()) {
				final CMDBfId id = resolveAlias(alias);
				if (id != null) {
					instanceId.add(id);
				}
			}
		}
		if (itemTemplate.getRecordConstraint() != null && !itemTemplate.getRecordConstraint().isEmpty()) {
			final Iterator<RecordConstraintType> recordIterator = itemTemplate.getRecordConstraint().iterator();
			while (recordIterator.hasNext() && (itemTemplateResultSet == null || !itemTemplateResultSet.isEmpty())) {
				final RecordConstraintType recordConstraint = recordIterator.next();
				final ItemSet<CMDBfItem> recordConstraintResultSet = new ItemSet<CMDBfItem>();
				recordConstraintResultSet.addAll(getItems(itemTemplate.getId(), instanceId, recordConstraint));
				if (itemTemplateResultSet == null) {
					itemTemplateResultSet = recordConstraintResultSet;
				} else {
					itemTemplateResultSet.retainAll(recordConstraintResultSet);
				}
			}
		} else {
			itemTemplateResultSet = new ItemSet<CMDBfItem>();
			itemTemplateResultSet.addAll(getItems(itemTemplate.getId(), instanceId, null));
		}
		return itemTemplateResultSet;
	}

	private Set<CMDBfRelationship> getRelationships(final RelationshipTemplateType relationshipTemplate,
			final ItemSet<CMDBfItem> sources, final ItemSet<CMDBfItem> targets) {
		ItemSet<CMDBfRelationship> relationshipTemplateResultSet = null;
		Set<CMDBfId> instanceId = null;
		if (relationshipTemplate.getInstanceIdConstraint() != null) {
			instanceId = new HashSet<CMDBfId>();
			for (final MdrScopedIdType alias : relationshipTemplate.getInstanceIdConstraint().getInstanceId()) {
				final CMDBfId id = resolveAlias(alias);
				if (id != null) {
					instanceId.add(id);
				}
			}
		}
		if (relationshipTemplate.getRecordConstraint() != null && !relationshipTemplate.getRecordConstraint().isEmpty()) {
			final Iterator<RecordConstraintType> recordIterator = relationshipTemplate.getRecordConstraint().iterator();
			while (recordIterator.hasNext()
					&& (relationshipTemplateResultSet == null || !relationshipTemplateResultSet.isEmpty())) {
				final RecordConstraintType recordConstraint = recordIterator.next();
				final ItemSet<CMDBfRelationship> recordConstraintResultSet = new ItemSet<CMDBfRelationship>();
				recordConstraintResultSet.addAll(getRelationships(relationshipTemplate.getId(), instanceId,
						sources != null ? sources.idSet() : null, targets != null ? targets.idSet() : null,
						recordConstraint));
				if (relationshipTemplateResultSet == null) {
					relationshipTemplateResultSet = recordConstraintResultSet;
				} else {
					relationshipTemplateResultSet.retainAll(recordConstraintResultSet);
				}
			}
		} else {
			relationshipTemplateResultSet = new ItemSet<CMDBfRelationship>();
			relationshipTemplateResultSet.addAll(getRelationships(relationshipTemplate.getId(), instanceId,
					sources != null ? sources.idSet() : null, targets != null ? targets.idSet() : null, null));
		}
		return relationshipTemplateResultSet;
	}

	private void removeInvalidPaths(final PathSet pathSet, final ItemSet<CMDBfItem> sourceSet,
			final ItemSet<CMDBfItem> intermediateSet, final ItemSet<CMDBfItem> targetSet, final Integer sourceMin,
			final Integer sourceMax, final Integer targetMin, final Integer targetMax) {
		boolean loop = false;
		do {
			final Set<CMDBfRelationship> validated = new HashSet<CMDBfRelationship>();
			for (final CMDBfId source : pathSet.sourceSet()) {
				if (sourceSet == null || sourceSet.contains(source)) {
					final Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(source);
					if (relationSet != null) {
						for (final CMDBfRelationship relationship : relationSet) {
							if (!validated.contains(relationship)) {
								if (validatePath(relationship.getTarget(), validated, pathSet, intermediateSet,
										targetSet)) {
									validated.add(relationship);
								}
							}
						}
					}
				}
			}
			pathSet.retainAll(validated);

			if (sourceMin != null || sourceMax != null) {
				final List<CMDBfId> invalid = new ArrayList<CMDBfId>();
				for (final CMDBfId source : pathSet.sourceSet()) {
					final Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(source);
					final int count = relationSet != null ? relationSet.size() : 0;
					if ((sourceMin != null && count < sourceMin) || (sourceMax != null && count > sourceMax)) {
						invalid.add(source);
					}
				}
				loop = pathSet.removeAllBySource(invalid);
			}

			if (targetMin != null || targetMax != null) {
				final List<CMDBfId> invalid = new ArrayList<CMDBfId>();
				for (final CMDBfId target : pathSet.targetSet()) {
					final Collection<CMDBfRelationship> relationSet = pathSet.relationSetByTarget(target);
					final int count = relationSet != null ? relationSet.size() : 0;
					if ((targetMin != null && count < targetMin) || (targetMax != null && count > targetMax)) {
						invalid.add(target);
					}
				}
				loop = pathSet.removeAllByTarget(invalid);
			}
		} while (loop);
	}

	private boolean validatePath(final CMDBfId target, final Set<CMDBfRelationship> validated, final PathSet pathSet,
			final Set<CMDBfItem> intermediateSet, final Set<CMDBfItem> targetSet) {
		boolean isValid = (targetSet == null || targetSet.contains(target));
		if (!isValid && (intermediateSet == null || intermediateSet.contains(target))) {
			final Collection<CMDBfRelationship> relationSet = pathSet.relationSetBySource(target);
			if (relationSet != null) {
				final Iterator<CMDBfRelationship> iterator = relationSet.iterator();
				while (!isValid && iterator.hasNext()) {
					final CMDBfRelationship relationship = iterator.next();
					if (!validated.contains(relationship)) {
						if (validatePath(relationship.getTarget(), validated, pathSet, intermediateSet, targetSet)) {
							validated.add(relationship);
							isValid = true;
						}
					} else {
						isValid = true;
					}
				}
			}
		}
		return isValid;
	}

	protected boolean filter(final CMDBfItem item, final Set<CMDBfId> idSet, final RecordConstraintType recordConstraint) {
		boolean match = true;
		if (idSet != null) {
			match = Iterables.any(item.instanceIds(), new IdConstraintPredicate(idSet));
		}
		if (match && recordConstraint != null) {
			match = Iterables.any(item.records(), new RecordConstraintPredicate(recordConstraint));
		}
		return match;
	}

	abstract protected Collection<CMDBfItem> getItems(String templateId, Set<CMDBfId> instanceId,
			RecordConstraintType recordConstraint);

	abstract protected Collection<CMDBfRelationship> getRelationships(String templateId, Set<CMDBfId> instanceId,
			Set<CMDBfId> source, Set<CMDBfId> target, RecordConstraintType recordConstraint);

	abstract protected void fetchItemRecords(String templateId, ItemSet<CMDBfItem> items,
			ContentSelectorType contentSelector);

	abstract protected void fetchRelationshipRecords(String templateId, PathSet relationships,
			ContentSelectorType contentSelector);

	abstract protected CMDBfId resolveAlias(MdrScopedIdType alias);

	abstract protected void fetchAlias(CMDBfItem item);
}