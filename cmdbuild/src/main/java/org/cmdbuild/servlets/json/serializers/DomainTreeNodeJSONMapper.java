package org.cmdbuild.servlets.json.serializers;

import java.util.List;

import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DomainTreeNodeJSONMapper {

	public static final String BASE_NODE = "baseNode", //
			CHILD_NODES = "childNodes", //
			DESCRIPTION = "description", //
			DIRECT = "direct", //
			DOMAIN_NAME = "domainName", //
			TARGET_FILTER = "filter", //
			ID = "id", //
			ID_PARENT = "idParent", //
			ID_GROUP = "idGroup", //
			TARGET_CLASS_NAME = "targetClassName", //
			TARGET_CLASS_DESCRIPTION = "targetClassDescription", //
			TYPE = "type";

	public static DomainTreeNode deserialize(final JSONObject jsonTreeNode) throws JSONException {
		final DomainTreeNode treeNode = new DomainTreeNode();

		treeNode.setBaseNode(readBooleanOrFalse(jsonTreeNode, BASE_NODE));
		treeNode.setDirect(readBooleanOrFalse(jsonTreeNode, DIRECT));
		treeNode.setTargetClassName(readStringOrNull(jsonTreeNode, TARGET_CLASS_NAME));
		treeNode.setTargetClassDescription(readStringOrNull(jsonTreeNode, TARGET_CLASS_DESCRIPTION));
		treeNode.setDomainName(readStringOrNull(jsonTreeNode, DOMAIN_NAME));
		treeNode.setType(readStringOrNull(jsonTreeNode, TYPE));
		treeNode.setId(readLongOrNull(jsonTreeNode, ID));
		treeNode.setIdParent(readLongOrNull(jsonTreeNode, ID_PARENT));
		treeNode.setIdGroup(readLongOrNull(jsonTreeNode, ID_GROUP));
		treeNode.setTargetFilter(readStringOrNull(jsonTreeNode, TARGET_FILTER));
		treeNode.setDescription(readStringOrNull(jsonTreeNode, DESCRIPTION));

		JSONArray jsonChildNodes = new JSONArray();
		if (jsonTreeNode.has(CHILD_NODES)) {
			jsonChildNodes = (JSONArray) jsonTreeNode.get(CHILD_NODES);
		}

		for (int i = 0, l = jsonChildNodes.length(); i < l; ++i) {
			final JSONObject jsonChild = (JSONObject) jsonChildNodes.get(i);
			treeNode.addChildNode(deserialize(jsonChild));
		}

		return treeNode;
	}

	public static JSONObject serialize(final DomainTreeNode treeNode, final Boolean deeply) throws JSONException {
		final JSONObject jsonTreeNode = new JSONObject();
		if (treeNode == null) {
			return jsonTreeNode;
		}

		jsonTreeNode.put(BASE_NODE, treeNode.isBaseNode());
		jsonTreeNode.put(DIRECT, treeNode.isDirect());
		jsonTreeNode.put(TARGET_CLASS_NAME, treeNode.getTargetClassName());
		jsonTreeNode.put(TARGET_CLASS_DESCRIPTION, treeNode.getTargetClassDescription());
		jsonTreeNode.put(DOMAIN_NAME, treeNode.getDomainName());
		jsonTreeNode.put(TYPE, treeNode.getType());
		jsonTreeNode.put(ID, treeNode.getId());
		jsonTreeNode.put(ID_PARENT, treeNode.getIdParent());
		jsonTreeNode.put(ID_GROUP, treeNode.getIdGroup());
		jsonTreeNode.put(TARGET_FILTER, treeNode.getTargetFilter());
		jsonTreeNode.put(DESCRIPTION, treeNode.getDescription());

		if (deeply) {
			jsonTreeNode.put(CHILD_NODES, serialize(treeNode.getChildNodes(), deeply));
		}

		return jsonTreeNode;
	}

	public static JSONArray serialize(final List<DomainTreeNode> nodes, final Boolean deeply) throws JSONException {
		final JSONArray jsonChildNodes = new JSONArray();
		for (final DomainTreeNode child : nodes) {
			jsonChildNodes.put(serialize(child, deeply));
		}

		return jsonChildNodes;
	}

	private static Boolean readBooleanOrFalse(final JSONObject src, final String key) throws JSONException {
		if (src.has(key)) {
			return src.getBoolean(key);
		} else {
			return false;
		}
	}

	private static String readStringOrNull(final JSONObject src, final String key) throws JSONException {
		if (src.has(key)) {
			return src.getString(key);
		} else {
			return null;
		}
	}

	private static Long readLongOrNull(final JSONObject src, final String key) throws JSONException {
		if (src.has(key)) {
			return src.getLong(key);
		} else {
			return null;
		}
	}
}
