package org.cmdbuild.model.domainTree;

import static org.apache.commons.lang3.StringUtils.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainTreeCardNode {

	private String className, text;
	private boolean leaf, checked, expanded, baseNode;
	private Long classId, cardId;
	private DomainTreeCardNode parent;
	private final Map<Object, DomainTreeCardNode> children;

	public class DomainTreeCardNodeComparator implements Comparator<DomainTreeCardNode> {

		@Override
		public int compare(final DomainTreeCardNode o1, final DomainTreeCardNode o2) {
			return defaultString(o1.getText()).compareTo(defaultString(o2.getText()));
		}

	}

	public DomainTreeCardNode() {
		children = new HashMap<Object, DomainTreeCardNode>();
		classId = new Long(0);
		cardId = new Long(0);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public void setLeaf(final boolean leaf) {
		this.leaf = leaf;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(final boolean checked) {
		setChecked(checked, false);
	}

	public void setChecked(final boolean checked, final boolean deeply) {
		setChecked(checked, deeply, false);
	}

	public void setChecked(final boolean checked, final boolean deeply, final boolean alsoAncestor) {
		this.checked = checked;

		if (deeply) {
			for (final DomainTreeCardNode child : getChildren()) {
				child.setChecked(checked, true, false);
			}
		}

		if (alsoAncestor) {
			DomainTreeCardNode parent = parent();
			while (parent != null) {
				parent.setChecked(true, false, false);
				parent = parent.parent();
			}
		}
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(final boolean expanded) {
		this.expanded = expanded;
	}

	public boolean isBaseNode() {
		return baseNode;
	}

	public void setBaseNode(final boolean baseNode) {
		this.baseNode = baseNode;
	}

	public Long getClassId() {
		return classId;
	}

	public void setClassId(final Long classId) {
		this.classId = classId;
	}

	public Long getCardId() {
		return cardId;
	}

	public void setCardId(final Long cardId) {
		this.cardId = cardId;
	}

	public List<DomainTreeCardNode> getChildren() {
		final List<DomainTreeCardNode> childrenList = new ArrayList<DomainTreeCardNode>(children.values());
		Collections.sort(childrenList, new DomainTreeCardNodeComparator());

		return childrenList;
	}

	public void addChild(final DomainTreeCardNode child) {
		children.put(child.getCardId(), child);
		child.setParent(this);
	}

	public DomainTreeCardNode getChildById(final Object id) {
		return children.get(id);
	}

	public void setParent(final DomainTreeCardNode parent) {
		this.parent = parent;
	}

	public DomainTreeCardNode parent() {
		return parent;
	}

	public void sortByText() {

	}
}
