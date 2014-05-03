package unit.serializers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.servlets.json.serializers.DomainTreeNodeJSONMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class DomainTreeNodeJSONMapperTest {

	@Test
	public void serializeSingleNode() throws JSONException {
		DomainTreeNode treeNode = buildDummyTreeNode(new Long(1));

		JSONObject jsonNode = DomainTreeNodeJSONMapper.serialize(treeNode, true);
		assertEquals(true, jsonNode.get("direct"));
		assertEquals("TargetClassName", jsonNode.get("targetClassName"));
		assertEquals("TargetClassDescription", jsonNode.get("targetClassDescription"));
		assertEquals("DomainName", jsonNode.get("domainName"));
		assertEquals("Type", jsonNode.get("type"));
		assertEquals(new Long(1), jsonNode.get("id"));
		assertEquals(new Long(100), jsonNode.get("idParent"));
		assertEquals(new Long(1), jsonNode.get("idGroup"));
	}

	@Test
	public void serializeNestedNode() throws JSONException {
		DomainTreeNode root = buildDummyTreeNode(new Long(1));
		DomainTreeNode aChild = buildDummyTreeNode(new Long(2), root.getId());
		DomainTreeNode anotherChild = buildDummyTreeNode(new Long(3), root.getId());
		DomainTreeNode aGrandson = buildDummyTreeNode(new Long(4), anotherChild.getId());

		root.addChildNode(aChild);
		root.addChildNode(anotherChild);
		anotherChild.addChildNode(aGrandson);

		JSONObject jsonRoot = DomainTreeNodeJSONMapper.serialize(root, true);

		JSONArray jsonChildNodes = (JSONArray) jsonRoot.get("childNodes");
		assertEquals(2, jsonChildNodes.length());

		JSONObject jsonAChild = jsonChildNodes.getJSONObject(0);
		assertEquals(0, ((JSONArray) jsonAChild.get("childNodes")).length());
		assertEquals(aChild.getId(), jsonAChild.get("id"));
		assertEquals(root.getId(), jsonAChild.get("idParent"));

		JSONObject jsonAnotherChild = jsonChildNodes.getJSONObject(1);
		assertEquals(1, ((JSONArray) jsonAnotherChild.get("childNodes")).length());
		assertEquals(anotherChild.getId(), jsonAnotherChild.get("id"));
		assertEquals(root.getId(), jsonAnotherChild.get("idParent"));
	}

	@Test
	public void deserializeSingleNode() throws JSONException {
		JSONObject jsonTreeNode = buildDummuJSONTreeNode(new Long(1), new Long(1));

		DomainTreeNode treeNode = DomainTreeNodeJSONMapper.deserialize(jsonTreeNode);
		assertTrue(treeNode.isDirect());
		assertEquals("A targetClassName", treeNode.getTargetClassName());
		assertEquals("A targetClassDescription", treeNode.getTargetClassDescription());
		assertEquals("A DomainName", treeNode.getDomainName());
		assertEquals("A Type", treeNode.getType());
		assertEquals(new Long(1), treeNode.getId());
		assertEquals(new Long(1), treeNode.getIdGroup());
		assertEquals(new Long(1), treeNode.getIdParent());
	}

	@Test
	public void deserializeNestedNodes() throws JSONException {
		JSONObject jsonRoot = buildDummuJSONTreeNode(new Long(1), null);

		JSONArray jsonChildNodes = new JSONArray();
		jsonChildNodes.put(buildDummuJSONTreeNode(new Long(2), new Long(1)));
		jsonChildNodes.put(buildDummuJSONTreeNode(new Long(3), new Long(1)));
		jsonRoot.put("childNodes", jsonChildNodes);

		DomainTreeNode root = DomainTreeNodeJSONMapper.deserialize(jsonRoot);
		assertTrue(root.isDirect());
		assertEquals("A targetClassName", root.getTargetClassName());
		assertEquals("A targetClassDescription", root.getTargetClassDescription());
		assertEquals("A DomainName", root.getDomainName());
		assertEquals("A Type", root.getType());
		assertEquals(new Long(1), root.getId());
		assertEquals(new Long(1), root.getIdGroup());
		assertEquals(new Long(0), root.getIdParent());

		List<DomainTreeNode> childNodes = root.getChildNodes();
		assertEquals(2, childNodes.size());
		DomainTreeNode aChild = childNodes.get(0);
		assertEquals(new Long(2), aChild.getId());
		assertEquals(new Long(1), aChild.getIdParent());
		aChild = childNodes.get(1);
		assertEquals(new Long(3), aChild.getId());
		assertEquals(new Long(1), aChild.getIdParent());
	}

	private DomainTreeNode buildDummyTreeNode(Long id) {
		return buildDummyTreeNode(id, null);
	}

	private DomainTreeNode buildDummyTreeNode(Long id, Long parentId) {
		DomainTreeNode treeNode = new DomainTreeNode();

		treeNode.setId(id);
		treeNode.setTargetClassName("TargetClassName");
		treeNode.setTargetClassDescription("TargetClassDescription");
		treeNode.setDomainName("DomainName");
		treeNode.setType("Type");
		treeNode.setIdGroup(new Long(1));
		treeNode.setDirect(true);
		if (parentId == null) {
			treeNode.setIdParent(new Long(100));
		} else {
			treeNode.setIdParent(parentId);
		}

		return treeNode;
	}

	private JSONObject buildDummuJSONTreeNode(Long id, Long idParent) throws JSONException {
		JSONObject jsonTreeNode = new JSONObject();

		jsonTreeNode.put("direct", true);
		jsonTreeNode.put("targetClassName", "A targetClassName");
		jsonTreeNode.put("targetClassDescription", "A targetClassDescription");
		jsonTreeNode.put("domainName", "A DomainName");
		jsonTreeNode.put("type", "A Type");
		jsonTreeNode.put("id", id);
		jsonTreeNode.put("idGroup", new Long(1));

		if (idParent == null) {
			jsonTreeNode.put("idParent", new Long(0));
		} else {
			jsonTreeNode.put("idParent", idParent);
		}
		return jsonTreeNode;
	}

}