package org.cmdbuild.logic;

import java.util.Map;

import org.cmdbuild.model.domainTree.DomainTreeNode;

public interface NavigationTreeLogic extends Logic {

	void create(final String name, final String description, final boolean active, DomainTreeNode deserialize);

	void save(final String name, final String description, final boolean active, DomainTreeNode deserialize);

	Map<String, String> get();

	DomainTreeNode getTree(final String name);

	void delete(final String name);

}
