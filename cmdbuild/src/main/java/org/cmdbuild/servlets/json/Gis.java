package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.ROOT;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.GISLogic.ClassMapping;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.domainTree.DomainTreeCardNode;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.servlets.json.serializers.DomainTreeNodeJSONMapper;
import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Gis extends JSONBaseWithSpringContext {

	@JSONExported
	@Admin
	public void addGeoAttribute( //
			@Parameter(CLASS_NAME) final String targetClassName, //
			@Parameter("name") final String name, //
			@Parameter("description") final String description, //
			@Parameter("type") final String type, //
			@Parameter("minZoom") final int minimumZoom, //
			@Parameter("maxZoom") final int maximumzoom, //
			@Parameter("style") final JSONObject mapStyle) throws Exception { //

		final GISLogic logic = gisLogic();
		final LayerMetadata layerMetaData = new LayerMetadata(name, //
				description, //
				type, //
				minimumZoom, //
				maximumzoom, //
				0, //
				mapStyle.toString(), //
				null //
		);

		layerMetaData.addVisibility(targetClassName);
		logic.createGeoAttribute(targetClassName, layerMetaData);
	}

	@JSONExported
	@Admin
	public void modifyGeoAttribute( //
			@Parameter(CLASS_NAME) final String targetClassName, //
			@Parameter("name") final String name, //
			@Parameter("description") final String description, //
			@Parameter("minZoom") final int minimumZoom, //
			@Parameter("maxZoom") final int maximumzoom, //
			@Parameter("style") final JSONObject jsonStyle) //
			throws JSONException, Exception {

		final GISLogic logic = gisLogic();
		logic.modifyGeoAttribute( //
				targetClassName, //
				name, //
				description, //
				minimumZoom, //
				maximumzoom, //
				jsonStyle.toString() //
		);
	}

	@JSONExported
	@Admin
	public void deleteGeoAttribute( //
			@Parameter("masterTableName") final String masterTableName, //
			@Parameter("name") final String name //
	) throws JSONException, Exception {

		final GISLogic logic = gisLogic();
		logic.deleteGeoAttribute(masterTableName, name);
	}

	@JSONExported
	@SkipExtSuccess
	public JSONObject getGeoCardList( //
			@Parameter(value = CLASS_NAME, required = true) final String masterClassName, //
			@Parameter(value = "bbox", required = true) final String bbox, //
			@Parameter(value = "attribute", required = true) final String layerName //
	) throws JSONException, Exception {

		final JSONArray features = new JSONArray();
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();

		final GISLogic logic = gisLogic();

		for (final GeoFeature geoFeature : logic.getFeatures(masterClassName, layerName, bbox)) {
			features.put(geoSerializer.serialize(geoFeature));
		}

		return geoSerializer.getNewFeatureCollection(features);
	}

	/**
	 * It is used to center the map to a specific card
	 * 
	 * @return the feature for the first geometry attribute
	 */
	@JSONExported
	public JSONObject getFeature( //
			@Parameter(value = CLASS_NAME, required = true) final String className, //
			@Parameter(value = CARD_ID, required = true) final Long cardId //
	) throws JSONException, Exception { //

		JSONObject jsonFeature = new JSONObject();
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GISLogic logic = gisLogic();
		final GeoFeature feature = logic.getFeature( //
				Card.newInstance() //
						.withClassName(className) //
						.withId(cardId) //
						.build() //
				);

		if (feature != null) {
			jsonFeature = geoSerializer.serialize(feature);
		}

		return jsonFeature;
	}

	@JSONExported
	public JSONObject getAllLayers() throws JSONException, Exception {
		final GISLogic logic = gisLogic();
		final List<LayerMetadata> layers = logic.list();
		final JSONObject out = new JSONObject();
		out.put("layers", GeoJSONSerializer.serializeGeoLayers(layers));
		return out;
	}

	@Admin
	@JSONExported
	public void setLayerVisibility( //
			@Parameter("layerFullName") final String layerFullName, //
			@Parameter("tableName") final String visibleTableName, //
			@Parameter("visible") final boolean visible) //
			throws Exception {

		final GISLogic logic = gisLogic();
		logic.setLayerVisisbility(layerFullName, visibleTableName, visible);
	}

	@Admin
	@JSONExported
	public void setLayersOrder( //
			@Parameter(value = "oldIndex", required = true) final int oldIndex, //
			@Parameter(value = "newIndex", required = true) final int newIndex //
	) throws Exception { //

		final GISLogic logic = gisLogic();
		logic.reorderLayers(oldIndex, newIndex);
	}

	/* DomainTreeNavigation */

	@Admin
	@JSONExported
	public void saveGISTreeNavigation( //
			@Parameter("structure") final String jsonConfiguraiton //
	) throws JSONException {

		final GISLogic logic = gisLogic();
		final JSONObject structure = new JSONObject(jsonConfiguraiton);
		final DomainTreeNode root = DomainTreeNodeJSONMapper.deserialize(structure);

		logic.saveGisTreeNavigation(root);
	}

	@Admin
	@JSONExported
	public void removeGISTreeNavigation() {
		final GISLogic logic = gisLogic();
		logic.removeGisTreeNavigation();
	}

	@JSONExported
	public JSONObject getGISTreeNavigation() throws Exception {
		final GISLogic logic = gisLogic();
		DomainTreeNode root = null;
		Map<String, ClassMapping> geoServerLayerMapping = null;
		if (logic.isGisEnabled()) {
			root = logic.getGisTreeNavigation();
			geoServerLayerMapping = logic.getGeoServerLayerMapping();
		}

		final JSONObject response = new JSONObject();
		if (root != null) {
			response.put(ROOT, DomainTreeNodeJSONMapper.serialize(root, true));
		}
		if (geoServerLayerMapping != null) {
			response.put("geoServerLayersMapping", GeoJSONSerializer.serialize(geoServerLayerMapping));
		}

		return response;
	}

	@JSONExported
	public JSONObject expandDomainTree() throws JSONException {
		final JSONObject response = new JSONObject(); 
		final DomainTreeCardNode domainTreeCardNode = gisLogic().expandDomainTree(systemDataAccessLogic());
		response.put(ROOT, new JSONObject(domainTreeCardNode));
		return response;
	}

	/*
	 * GEO SERVER
	 */

	@JSONExported
	@Admin
	public void addGeoServerLayer(@Parameter("name") final String name,
			@Parameter("description") final String description, //
			@Parameter("cardBinding") final JSONArray cardBindingString, //
			@Parameter("type") final String type, //
			@Parameter("minZoom") final int minimumZoom, //
			@Parameter("maxZoom") final int maximumzoom, //
			@Parameter(value = "file", required = true) final FileItem file) throws IOException, Exception { //

		final GISLogic logic = gisLogic();
		final LayerMetadata layerMetaData = new LayerMetadata(name, description, type, minimumZoom, maximumzoom, 0,
				null, null);
		layerMetaData.setCardBinding(fromJsonToSet(cardBindingString));
		logic.createGeoServerLayer(layerMetaData, file);
	}

	@JSONExported
	@Admin
	public void modifyGeoServerLayer(@Parameter("name") final String name, //
			@Parameter("description") final String description, //
			@Parameter("cardBinding") final JSONArray cardBindingString, //
			@Parameter("minZoom") final int minimumZoom, //
			@Parameter("maxZoom") final int maximumZoom, //
			@Parameter(required = false, value = "file") final FileItem file) throws Exception {

		final GISLogic logic = gisLogic();
		logic.modifyGeoServerLayer(name, description, maximumZoom, minimumZoom, file, fromJsonToSet(cardBindingString));
	}

	@JSONExported
	@Admin
	public void deleteGeoServerLayer(@Parameter("name") final String name) throws Exception {

		final GISLogic logic = gisLogic();
		logic.deleteGeoServerLayer(name);
	}

	@JSONExported
	@Admin
	public JSONObject getGeoserverLayers(final JSONObject serializer) throws Exception {
		final GISLogic logic = gisLogic();
		serializer.put("layers", GeoJSONSerializer.serializeGeoLayers(logic.getGeoServerLayers()));
		return serializer;
	}

	private Set<String> fromJsonToSet(final JSONArray json) throws JSONException {
		final HashSet<String> out = new HashSet<String>();
		for (int i = 0, l = json.length(); i < l; ++i) {
			final JSONObject jsonCardBinding = (JSONObject) json.get(i);
			out.add(jsonCardBinding.getString("className") + "_" + jsonCardBinding.getString("idCard"));
		}
		return out;
	}
}
