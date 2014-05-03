package org.cmdbuild.servlets.json.legacy;

import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.model.data.Card;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.common.collect.Iterables;

public class GraphEdge extends GraphItem {

	private final Card srcCard;
	private final RelationInfo relation;
	private final DomainInfo domainInfo;
	private final GraphProperties properties;
	private final int count;
	private boolean declusterize = false;

	public GraphEdge(final Card srcCard, final RelationInfo relation, final DomainInfo domainInfo,
			final GraphProperties properties) {
		this.relation = relation;
		this.domainInfo = domainInfo;
		this.srcCard = srcCard;
		this.properties = properties;
		this.count = Iterables.size(domainInfo);
	}

	public void setDeclusterize(final boolean declusterize) {
		this.declusterize = declusterize;
	}

	private boolean isCluster() {
		return (this.count >= properties.getClusteringThreshold());
	}

	private String getEdgeSourceId() {
		return String.format("node_%d_%d", srcCard.getClassId(), srcCard.getId());
	}

	private String getEdgeTargetId() {
		CMCard targetCard = (CMCard) relation.getTargetCard();
		String targetIdString;
		if (isCluster() && !declusterize) {
			targetIdString = String.format("node_%d", targetCard.getType().getId());
		} else {
			targetIdString = String.format("node_%d_%d", targetCard.getType().getId(),
					targetCard.getId());
		}
		return targetIdString;
	}

	private String getDomainDescription() {
		return domainInfo.getDescription();
	}

	public Element toXMLElement() {
		Element edge = DocumentHelper.createElement("edge");
		edge.addAttribute("source", getEdgeSourceId());
		edge.addAttribute("target", getEdgeTargetId());
		edge.add(serializeData("domaindescription", getDomainDescription()));
		return edge;
	}
}
