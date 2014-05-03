package org.cmdbuild.logic;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.domainTree.DomainTreeCardNode;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.GeoFeature;

public interface GISLogic extends Logic {

	interface CardMapping {

		String getName();

		String getDesription();

	}

	interface ClassMapping {

		void addCardMapping(String cardId, CardMapping mapping);

		Set<String> cards();

		CardMapping get(String cardId);

	}

	boolean isGisEnabled();

	LayerMetadata createGeoAttribute(String targetClassName, LayerMetadata layerMetaData) throws Exception;

	LayerMetadata modifyGeoAttribute(String targetClassName, String name, String description, int minimumZoom,
			int maximumZoom, String style) throws Exception;

	void deleteGeoAttribute(String masterTableName, String attributeName) throws Exception;

	/**
	 * Retrieve the geoFeature for the given card. If more than one layer are
	 * defined, take the first
	 * 
	 * @param card
	 * @return
	 * @throws Exception
	 */
	GeoFeature getFeature(Card card) throws Exception;

	List<GeoFeature> getFeatures(String masterClassName, String layerName, String bbox) throws Exception;

	void updateFeatures(Card ownerCard, Map<String, Object> attributes) throws Exception;

	void createGeoServerLayer(LayerMetadata layerMetaData, FileItem file) throws IOException, Exception;

	void modifyGeoServerLayer(String name, String description, int maximumZoom, int minimumZoom, FileItem file,
			Set<String> cardBinding) throws Exception;

	void deleteGeoServerLayer(String name) throws Exception;

	List<LayerMetadata> getGeoServerLayers() throws Exception;

	Map<String, ClassMapping> getGeoServerLayerMapping() throws Exception;

	List<LayerMetadata> list() throws Exception;

	void setLayerVisisbility(String layerFullName, String visibleTable, boolean visible) throws Exception;

	void reorderLayers(int oldIndex, int newIndex) throws Exception;

	void saveGisTreeNavigation(DomainTreeNode root);

	void removeGisTreeNavigation();

	DomainTreeNode getGisTreeNavigation();

	DomainTreeCardNode expandDomainTree(DataAccessLogic dataAccesslogic);

}
