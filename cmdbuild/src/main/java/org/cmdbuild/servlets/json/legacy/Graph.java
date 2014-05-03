package org.cmdbuild.servlets.json.legacy;

import java.util.List;
import java.util.Set;

import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class Graph extends JSONBaseWithSpringContext {

	private final DataAccessLogic dataAccessLogic;
	private final int clusteringThreshold;

	public Graph() {
		dataAccessLogic = systemDataAccessLogic();
		clusteringThreshold = graphProperties().getClusteringThreshold();
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document graphML(@Parameter("data") final String xmlDataString) throws DocumentException {
		final Document xmlData = DocumentHelper.parseText(xmlDataString);
		final Set<Card> nodes = getNodes(xmlData);
		final Set<Card> excludes = getExcludes(xmlData);

		final Set<GraphNode> graphNodes = Sets.newHashSet();
		final Set<GraphEdge> graphEdges = Sets.newHashSet();
		final Set<GraphRelation> graphRelations = Sets.newHashSet();

		for (final Card card : nodes) {
			if (excludes.contains(card)) {
				continue;
			}
			graphNodes.add(new GraphNode(card, graphProperties()));
			final GetRelationListResponse response = dataAccessLogic.getRelationList(card, null);
			for (final DomainInfo domainInfo : response) {
				final boolean serializeOnlyOneNode = Iterables.size(domainInfo) >= clusteringThreshold;
				final int numberOfNodesToSerialize = serializeOnlyOneNode ? 1 : Iterables.size(domainInfo);
				int serializedNodes = 0;

				for (final RelationInfo relationInfo : domainInfo) {
					final GraphRelation graphRelation = new GraphRelation(card, domainInfo, graphProperties());
					if (!graphRelations.contains(graphRelation)) {
						graphRelations.add(graphRelation);
					}
					if (serializedNodes < numberOfNodesToSerialize) {
						final GraphEdge edge = new GraphEdge(card, relationInfo, domainInfo, graphProperties());
						graphEdges.add(edge);
						final CardStorableConverter cardConverter = new CardStorableConverter(relationInfo
								.getTargetCard().getType().getIdentifier().getLocalName());
						graphNodes.add(new GraphNode(cardConverter.convert(relationInfo.getTargetCard()), domainInfo,
								graphProperties()));
						serializedNodes++;
					}
				}
			}
		}

		return XMLSerializer.serializeGraph(graphNodes, graphEdges, graphRelations);
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document declusterize(@Parameter("data") final String xmlDataString) throws DocumentException {
		final Set<GraphNode> graphNodes = Sets.newHashSet();
		final Set<GraphEdge> graphEdges = Sets.newHashSet();
		final Set<GraphRelation> graphRelations = Sets.newHashSet();

		final Document xmlData = DocumentHelper.parseText(xmlDataString);
		final List<Element> xmlElements = DocumentHelper.createXPath("/data/relations/item").selectNodes(xmlData);
		final Element cluster = xmlElements.get(0);
		final Long srcClassId = Long.parseLong(cluster.attributeValue("parentClassId"));
		final Long srcCardId = Long.parseLong(cluster.attributeValue("parentObjId"));
		final Long domainId = Long.parseLong(cluster.attributeValue("domainId"));
		final Long targetClassId = Long.parseLong(cluster.attributeValue("childClassId"));

		final Card srcCard = dataAccessLogic.fetchCard(srcClassId, srcCardId);
		graphNodes.add(new GraphNode(srcCard, graphProperties()));
		final Long class1Id = dataAccessLogic.findDomain(domainId).getClass1().getId();
		final DomainWithSource dom;
		if (class1Id.equals(srcClassId)) {
			dom = DomainWithSource.create(domainId, Source._1.toString());
		} else {
			dom = DomainWithSource.create(domainId, Source._2.toString());
		}
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		for (final DomainInfo domainInfo : response) {
			for (final RelationInfo relationInfo : domainInfo) {
				final CardStorableConverter cardConverter = new CardStorableConverter(relationInfo.getTargetCard()
						.getType().getIdentifier().getLocalName());
				final GraphNode node = new GraphNode(cardConverter.convert(relationInfo.getTargetCard()),
						graphProperties());
				if (!node.getIdClass().equals(targetClassId)) {
					continue;
				}
				graphNodes.add(node);
				final GraphEdge edge = new GraphEdge(srcCard, relationInfo, domainInfo, graphProperties());
				edge.setDeclusterize(true);
				graphEdges.add(edge);

			}
		}
		return XMLSerializer.serializeGraph(graphNodes, graphEdges, graphRelations);
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document card(@Parameter("data") final String xmlDataString) throws DocumentException {
		final Document xmlData = DocumentHelper.parseText(xmlDataString);
		final XPath xpathSelector = DocumentHelper.createXPath("/data/nodes/item");
		final List<?> results = xpathSelector.selectNodes(xmlData);
		final Element element = (Element) results.get(0);
		final Long classId = Long.parseLong(element.attributeValue("classId"));
		final Long objId = Long.parseLong(element.attributeValue("objId"));
		return XMLSerializer.serializeCard(dataAccessLogic.fetchCard(classId, objId));
	}

	private Set<Card> getNodes(final Document xmlData) {
		final Set<Card> nodes = Sets.newHashSet();
		final List<Element> xmlElements = DocumentHelper.createXPath("/data/nodes/item").selectNodes(xmlData);
		for (final Element element : xmlElements) {
			final Long classId = Long.parseLong(element.attributeValue("classId"));
			final Long cardId = Long.parseLong(element.attributeValue("objId"));
			final Card nodeCard = dataAccessLogic.fetchCard(classId, cardId);
			nodes.add(nodeCard);
		}
		return nodes;
	}

	private Set<Card> getExcludes(final Document xmlData) {
		final Set<Card> excludes = Sets.newHashSet();
		final List<Element> xmlElements = DocumentHelper.createXPath("/data/excludes/item").selectNodes(xmlData);
		for (final Element element : xmlElements) {
			final Long classId = Long.parseLong(element.attributeValue("classId"));
			final Long cardId = Long.parseLong(element.attributeValue("objId"));
			final Card excludedCard = dataAccessLogic.fetchCard(classId, cardId);
			excludes.add(excludedCard);
		}
		return excludes;
	}
}
