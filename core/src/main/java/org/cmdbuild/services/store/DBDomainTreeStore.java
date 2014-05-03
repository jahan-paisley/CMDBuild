package org.cmdbuild.services.store;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.domainTree.DomainTreeNode;

public class DBDomainTreeStore {
	private enum Attributes {
		BASE_NODE("BaseNode"), //
		DESCRIPTION("Description"), //
		DIRECT("Direct"), //
		DOMAIN_NAME("DomainName"), //
		FILTER("TargetFilter"), //
		ID_GROUP("IdGroup"), //
		ID_PARENT("IdParent"), //
		TARGET_CLASS_DESCRIPTION("TargetClassDescription"), //
		TARGET_CLASS_NAME("TargetClassName"), //
		TYPE("Type");

		private String name;

		Attributes(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private static final String TABLE_NAME = "_DomainTreeNavigation";
	private final CMDataView dataView;

	public DBDomainTreeStore(final CMDataView dataView) {
		this.dataView = dataView;
	}

	public void createOrReplaceTree(final String treeType, String description, final DomainTreeNode root) {
		removeTree(treeType);
		saveNode(treeType, description, root);
	}

	public void removeTree(final String treeType) {
		final CMClass table = getTable();
		final CMQueryResult domainTreeNodes = dataView //
				.select(anyAttribute(table)) //
				.from(table) //
				.where(//
				condition( //
						attribute(table, Attributes.TYPE.getName()), eq(treeType) //
				)//
				) //
				.run();

		for (final CMQueryRow domainTreeNode : domainTreeNodes) {
			dataView.delete(domainTreeNode.getCard(table));
		}

	}
	public Map<String, String> getTreeNames() {
		final CMClass table = getTable();
		final CMQueryResult domainTreeNames = dataView //
				.select(anyAttribute(table)) //
				.from(table) //
				.run();
		final Map<String, String> names = new HashMap<String, String>();
		for (final CMQueryRow layerAsQueryRow : domainTreeNames) {
			final String name = (String) layerAsQueryRow.getCard(table).get(Attributes.TYPE.getName());
			final String description = (String) layerAsQueryRow.getCard(table).get(Attributes.DESCRIPTION.getName());
			names.put(name, description);
		}
		return names;
	}

	public DomainTreeNode getDomainTree(final String treeType) {
		final Map<Long, DomainTreeNode> treeNodes = new HashMap<Long, DomainTreeNode>();

		final CMClass table = getTable();
		final CMQueryResult domainTreeNodes = dataView //
				.select(anyAttribute(table)) //
				.from(table) //
				.where(//
				condition( //
						attribute(table, Attributes.TYPE.getName()), eq(treeType) //
				)//
				) //
				.run();

		DomainTreeNode root = null;

		for (final CMQueryRow domainTreeNodeQueryRow : domainTreeNodes) {
			final CMCard domainTreeNodeCard = domainTreeNodeQueryRow.getCard(table);
			final DomainTreeNode currentTreeNode = cardToDomainTreeNode(domainTreeNodeCard);
			for (final DomainTreeNode treeNode : treeNodes.values()) {
				// Link children to current node
				if (treeNode.getIdParent() != null && treeNode.getIdParent().equals(currentTreeNode.getId())) {
					currentTreeNode.addChildNode(treeNode);
				}
			}

			// link the currentNode as child of a node
			// if already created
			if (currentTreeNode.getIdParent() != null) {
				final DomainTreeNode maybeParent = treeNodes.get(currentTreeNode.getIdParent());
				if (maybeParent != null) {
					maybeParent.addChildNode(currentTreeNode);
				}
			} else {
				root = currentTreeNode;
			}

			treeNodes.put(currentTreeNode.getId(), currentTreeNode);
		}

		return root;
	}

	private void saveNode(final String treeType, String description, final DomainTreeNode root) {
		final CMCard newNode = dataView.createCardFor(getTable()).set(Attributes.DIRECT.getName(), root.isDirect())
				.set(Attributes.DOMAIN_NAME.getName(), root.getDomainName()).set(Attributes.TYPE.getName(), treeType)
				.set(Attributes.ID_GROUP.getName(), root.getIdGroup())
				.set(Attributes.ID_PARENT.getName(), root.getIdParent())
				.set(Attributes.TARGET_CLASS_NAME.getName(), root.getTargetClassName())
				.set(Attributes.TARGET_CLASS_DESCRIPTION.getName(), root.getTargetClassDescription())
				.set(Attributes.FILTER.getName(), root.getTargetFilter())
				.set(Attributes.DESCRIPTION.getName(), description)
				.set(Attributes.BASE_NODE.getName(), root.isBaseNode()).save();

		final Long id = newNode.getId();
		for (final DomainTreeNode child : root.getChildNodes()) {
			child.setIdParent(id);
			saveNode(treeType, description, child);
		}
	}

	private DomainTreeNode cardToDomainTreeNode(final CMCard card) {
		final DomainTreeNode domainTreeNode = new DomainTreeNode();
		domainTreeNode.setId(card.getId());
		domainTreeNode.setDescription((String) card.get(Attributes.DESCRIPTION.getName()));
		domainTreeNode.setDirect(booleanCast(card.get(Attributes.DIRECT.getName())));
		domainTreeNode.setDomainName((String) card.get(Attributes.DOMAIN_NAME.getName()));
		domainTreeNode.setType((String) card.get(Attributes.TYPE.getName()));
		domainTreeNode.setIdGroup(safeLongCast(card.get(Attributes.ID_GROUP.getName())));
		domainTreeNode.setIdParent(safeLongCast(card.get(Attributes.ID_PARENT.getName())));
		domainTreeNode.setTargetClassDescription((String) card.get(Attributes.TARGET_CLASS_DESCRIPTION.getName()));
		domainTreeNode.setTargetClassName(((String) card.get(Attributes.TARGET_CLASS_NAME.getName())));
		domainTreeNode.setBaseNode((booleanCast(card.get(Attributes.BASE_NODE.getName()))));
		domainTreeNode.setTargetFilter((String) card.get(Attributes.FILTER.getName()));

		return domainTreeNode;
	}

	private CMClass getTable() {
		return dataView.findClass(TABLE_NAME);
	}

	// getValue method of a Card returns
	// an Object. For the Ids it returns an Integer
	// but we want a Long. Cast them ignoring null values
	private Long safeLongCast(final Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Long) {
			return (Long) o;
		} else if (o instanceof Integer) {
			return ((Integer) o).longValue();
		}
		return null;
	}

	private boolean booleanCast(final Object o) {
		if (o == null) {
			return false;
		} else {
			return (Boolean) o;
		}
	}
}
