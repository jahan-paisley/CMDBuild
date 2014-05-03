package org.cmdbuild.servlets.json.legacy;

import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.model.data.Card;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.common.collect.Iterables;

public class GraphRelation {

	private final DomainInfo domainInfo;
	private final Card srcCard;
	private final int count;
	private final GraphProperties properties;

	public GraphRelation(final Card srcCard, final DomainInfo domainInfo, final GraphProperties properties) {
		this.srcCard = srcCard;
		this.count = Iterables.size(domainInfo);
		this.domainInfo = domainInfo;
		this.properties = properties;
	}

	private String getDescription() {
		return String.format("%s - %s", srcCard.getAttribute("Description"), domainInfo.getQueryDomain()
				.getDescription());
	}

	private boolean isClusterized() {
		int clusteringThreshold = properties.getClusteringThreshold();
		return (clusteringThreshold <= count);
	}

	Element toXMLElement() {
		Element relationItem = DocumentHelper.createElement("item");
		relationItem.addAttribute("parentClassId", String.valueOf(srcCard.getClassId()));
		relationItem.addAttribute("parentObjId", String.valueOf(srcCard.getId()));
		relationItem.addAttribute("domainId", String.valueOf(domainInfo.getQueryDomain().getDomain().getId()));
		relationItem.addAttribute("childClassId", String.valueOf(domainInfo.getQueryDomain().getTargetClass().getId()));
		relationItem.addAttribute("elements", String.valueOf(count));
		relationItem.addAttribute("clusterize", String.valueOf(isClusterized()));
		relationItem.addAttribute("description", getDescription());
		return relationItem;
	}

	public boolean equals(Object o) {
		if (o instanceof GraphRelation) {
			GraphRelation other = ((GraphRelation) o);
			return (this.domainInfo.equals(other.domainInfo) && this.srcCard.equals(other.srcCard));
		}
		return false;
	}

	public int hashCode() {
		return this.domainInfo.hashCode() + this.srcCard.hashCode();
	}

}
