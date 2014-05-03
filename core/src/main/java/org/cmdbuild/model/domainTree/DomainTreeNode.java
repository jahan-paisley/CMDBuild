package org.cmdbuild.model.domainTree;

import java.util.LinkedList;
import java.util.List;

public class DomainTreeNode {
	private String targetClassName, targetClassDescription, //
			domainName, type, //
			targetFilter, description;
	private Long idParent, idGroup, id;
	private boolean direct, baseNode;
	private List<DomainTreeNode> childNodes;

	public DomainTreeNode() {
		childNodes = new LinkedList<DomainTreeNode>();
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getTargetClassName() {
		return targetClassName;
	}

	public void setTargetClassName(final String targetClassName) {
		this.targetClassName = targetClassName;
	}

	public String getTargetClassDescription() {
		return targetClassDescription;
	}

	public void setTargetClassDescription(final String targetClassDescription) {
		this.targetClassDescription = targetClassDescription;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(final String domainName) {
		this.domainName = domainName;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Long getIdParent() {
		return idParent;
	}

	public void setIdParent(final Long idParent) {
		this.idParent = idParent;
	}

	public Long getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(final Long idGroup) {
		this.idGroup = idGroup;
	}

	public boolean isDirect() {
		return direct;
	}

	public void setDirect(final boolean direct) {
		this.direct = direct;
	}

	public boolean isBaseNode() {
		return baseNode;
	}

	public void setBaseNode(final boolean baseNode) {
		this.baseNode = baseNode;
	}

	public List<DomainTreeNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(final List<DomainTreeNode> childNodes) {
		this.childNodes = childNodes;
	}

	public void addChildNode(final DomainTreeNode childNode) {
		childNodes.add(childNode);
	}

	public String getTargetFilter() {
		return targetFilter;
	}

	public void setTargetFilter(final String filter) {
		this.targetFilter = filter;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

}
