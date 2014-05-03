package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.logic.NavigationTreeLogic;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.serializers.DomainTreeNodeJSONMapper;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONException;
import org.json.JSONObject;

public class NavigationTree extends JSONBaseWithSpringContext {
	
	private static class DTO {

		private final Entry<String, String> delegate;

		public DTO(final Entry<String, String> delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(NAME)
		public String getName() {
			return delegate.getKey();
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return delegate.getValue();
		}


	}
	
	@Admin
	@JSONExported
	public JsonResponse get() throws JSONException {
		final NavigationTreeLogic logic = navigationTreeLogic();
		Map<String, String> trees = logic.get();
		final List<DTO> jsonTrees = new ArrayList<DTO>();
		for ( Entry<String, String> tree : trees.entrySet()) {
			jsonTrees.add(new DTO(tree));
			
		}
		return JsonResponse.success(jsonTrees);
	}

	@JSONExported
	public JsonResponse read(
			@Parameter(NAME) final String name
			) throws JSONException {
		final NavigationTreeLogic logic = navigationTreeLogic();
		DomainTreeNode root = logic.getTree(name);
		JSONObject response = DomainTreeNodeJSONMapper.serialize(root, true);
		return JsonResponse.success(response.toString());
	}

	@Admin
	@JSONExported
	public JsonResponse remove(
			@Parameter(NAME) final String name
			) throws JSONException {
		final NavigationTreeLogic logic = navigationTreeLogic();
		logic.delete(name);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public JsonResponse create(
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter("structure") final String jsonConfiguration 
			) throws JSONException {
		final NavigationTreeLogic logic = navigationTreeLogic();
		final JSONObject structure = new JSONObject(jsonConfiguration);
		final DomainTreeNode root = DomainTreeNodeJSONMapper.deserialize(structure);

		logic.create(name, description, isActive, root);
		return JsonResponse.success();
	}
	
	@Admin
	@JSONExported
	public JsonResponse save(
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter("structure") final String jsonConfiguration 
			) throws JSONException {
		final NavigationTreeLogic logic = navigationTreeLogic();
		final JSONObject structure = new JSONObject(jsonConfiguration);
		final DomainTreeNode root = DomainTreeNodeJSONMapper.deserialize(structure);

		logic.save(name, description, isActive, root);
		return JsonResponse.success();
	}
	
}
