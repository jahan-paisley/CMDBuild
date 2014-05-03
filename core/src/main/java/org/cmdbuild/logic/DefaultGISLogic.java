package org.cmdbuild.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.domainTree.DomainTreeCardNode;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureStore;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.services.store.DBDomainTreeStore;
import org.cmdbuild.services.store.DBLayerMetadataStore;
import org.cmdbuild.utils.OrderingUtils;
import org.cmdbuild.utils.OrderingUtils.PositionHandler;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

public class DefaultGISLogic implements GISLogic {

	private static class DefaultCardMapping implements CardMapping {

		private final String name;
		private final String description;

		public DefaultCardMapping(final String name, final String description) {
			this.name = name;
			this.description = description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDesription() {
			return description;
		}

	}

	private static class DefaultClassMapping implements ClassMapping {

		private final Map<String, CardMapping> map = Maps.newHashMap();

		@Override
		public void addCardMapping(final String cardId, final CardMapping mapping) {
			map.put(cardId, mapping);
		}

		@Override
		public Set<String> cards() {
			return map.keySet();
		}

		@Override
		public CardMapping get(final String cardId) {
			return map.get(cardId);
		}

	}

	private static final String DOMAIN_TREE_TYPE = "gisnavigation";
	private static final String GEOSERVER = "_Geoserver";
	private static final String GEO_TABLESPACE = "gis";
	private static final String GEO_ATTRIBUTE_TABLE_NAME_FORMAT = "Detail_%s_%s";
	private static final String GEO_TABLE_NAME_FORMAT = GEO_TABLESPACE + "." + GEO_ATTRIBUTE_TABLE_NAME_FORMAT;

	private final CMDataView dataView;
	private final DBLayerMetadataStore layerMetadataStore;
	private final GeoFeatureStore geoFeatureStore;
	private final DBDomainTreeStore domainTreeStore;
	private final GisConfiguration configuration;
	private final GeoServerService geoServerService;

	public DefaultGISLogic( //
			final CMDataView dataView, //
			final GeoFeatureStore geoFeatureStore, //
			final GisConfiguration configuration, //
			final GeoServerService geoServerService //
	) {
		this.dataView = dataView;
		this.layerMetadataStore = new DBLayerMetadataStore(dataView);
		this.domainTreeStore = new DBDomainTreeStore(dataView);
		this.geoFeatureStore = geoFeatureStore;
		this.configuration = configuration;
		this.geoServerService = geoServerService;
	}

	/* Geo attributes */

	@Override
	public boolean isGisEnabled() {
		return configuration.isEnabled();
	}

	@Override
	@Transactional
	public LayerMetadata createGeoAttribute( //
			final String targetClassName, //
			final LayerMetadata layerMetaData //
	) throws Exception {

		ensureGisIsEnabled();
		final String geoAttributeTableName = createGeoAttributeTable(targetClassName, layerMetaData);
		layerMetaData.setFullName(geoAttributeTableName);

		return layerMetadataStore.createLayer(layerMetaData);
	}

	@Override
	@Transactional
	public LayerMetadata modifyGeoAttribute( //
			final String targetClassName, //
			final String name, //
			final String description, //
			final int minimumZoom, //
			final int maximumZoom, //
			final String style) throws Exception {

		ensureGisIsEnabled();
		return modifyLayerMetadata(targetClassName, name, description, minimumZoom, maximumZoom, style, null);
	}

	@Override
	@Transactional
	public void deleteGeoAttribute( //
			final String masterTableName, //
			final String attributeName //
	) throws Exception {

		ensureGisIsEnabled();
		geoFeatureStore.deleteGeoTable(masterTableName, attributeName);
		layerMetadataStore.deleteLayer(fullName(masterTableName, attributeName));
	}

	/**
	 * Retrieve the geoFeature for the given card. If more than one layer are
	 * defined, take the first
	 * 
	 * @param card
	 * @return
	 * @throws Exception
	 */
	@Override
	public GeoFeature getFeature(final Card card) throws Exception {
		ensureGisIsEnabled();

		final List<LayerMetadata> layers = layerMetadataStore.list(card.getClassName());
		GeoFeature geoFeature = null;

		if (layers.size() > 0) {
			final LayerMetadata layer = layers.get(0);
			geoFeature = geoFeatureStore.readGeoFeature(layer, card);
		}

		return geoFeature;
	}

	@Override
	public List<GeoFeature> getFeatures( //
			final String masterClassName, //
			final String layerName, //
			final String bbox //
	) throws Exception {

		ensureGisIsEnabled();

		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, masterClassName, layerName);
		final LayerMetadata layerMetaData = layerMetadataStore.get(fullName);

		return geoFeatureStore.readGeoFeatures(layerMetaData, bbox);
	}

	@Override
	public void updateFeatures( //
			final Card ownerCard, //
			final Map<String, Object> attributes //
	) throws Exception {

		ensureGisIsEnabled();

		final String geoAttributesJsonString = (String) attributes.get("geoAttributes");
		if (geoAttributesJsonString != null) {
			final JSONObject geoAttributesObject = new JSONObject(geoAttributesJsonString);
			final String[] geoAttributesName = JSONObject.getNames(geoAttributesObject);
			final CMClass masterTable = dataView.findClass(ownerCard.getClassName());

			if (geoAttributesName != null) {
				for (final String name : geoAttributesName) {
					final LayerMetadata layerMetaData = layerMetadataStore.get(fullName(masterTable.getIdentifier()
							.getLocalName(), name));
					final String value = geoAttributesObject.getString(name);

					final GeoFeature geoFeature = geoFeatureStore.readGeoFeature(layerMetaData, ownerCard);

					if (geoFeature == null) {
						// the feature does not exists
						// create it
						if (value != null && !value.trim().isEmpty()) {
							geoFeatureStore.createGeoFeature(layerMetaData, value, ownerCard.getId());
						}
					} else {
						if (value != null && !value.trim().isEmpty()) {
							// there is a non empty value
							// update the geometry
							geoFeatureStore.updateGeoFeature(layerMetaData, value, ownerCard.getId());
						} else {
							// the new value is blank, so delete the feature
							geoFeatureStore.deleteGeoFeature(layerMetaData, ownerCard.getId());
						}
					}
				}
			}
		}
	}

	/* GeoServer */

	@Override
	public void createGeoServerLayer( //
			final LayerMetadata layerMetaData, //
			final FileItem file //
	) throws IOException, Exception {

		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		final String geoServerLayerName = geoServerService.createStoreAndLayer(layerMetaData, file.getInputStream());
		if (geoServerLayerName == null) {
			throw new Exception("Geoserver has not create the layer");
		}

		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, GEOSERVER, layerMetaData.getName());
		layerMetaData.setFullName(fullName);
		layerMetaData.setGeoServerName(geoServerLayerName);

		layerMetadataStore.createLayer(layerMetaData);
	}

	@Override
	@Transactional
	public void modifyGeoServerLayer( //
			final String name, //
			final String description, //
			final int maximumZoom, //
			final int minimumZoom, //
			final FileItem file, //
			final Set<String> cardBinding //
	) throws Exception {

		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		final LayerMetadata layerMetadata = modifyLayerMetadata(GEOSERVER, name, description, minimumZoom, maximumZoom,
				null, cardBinding);

		if (file != null && file.getSize() > 0) {
			geoServerService.modifyStoreData(layerMetadata, file.getInputStream());
		}
	}

	@Override
	@Transactional
	public void deleteGeoServerLayer(final String name) throws Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, GEOSERVER, name);
		final LayerMetadata layer = layerMetadataStore.get(fullName);
		geoServerService.deleteStoreAndLayers(layer);
		layerMetadataStore.deleteLayer(fullName);
	}

	@Override
	@Transactional
	public List<LayerMetadata> getGeoServerLayers() throws Exception {
		ensureGisIsEnabled();

		return layerMetadataStore.list(GEOSERVER);
	}

	@Override
	public Map<String, ClassMapping> getGeoServerLayerMapping() throws Exception {
		final List<LayerMetadata> geoServerLayers = getGeoServerLayers();
		final Map<String, ClassMapping> mapping = Maps.newHashMap();

		for (final LayerMetadata layer : geoServerLayers) {
			for (final String bindedCard : layer.getCardBinding()) {

				// A cardInfo is ClassName_CardId
				final String[] cardInfo = bindedCard.split("_");
				final String className = cardInfo[0];
				final String cardId = cardInfo[1];

				ClassMapping classMapping;
				if (mapping.containsKey(className)) {
					classMapping = mapping.get(className);
				} else {
					classMapping = new DefaultClassMapping();
					mapping.put(className, classMapping);
				}

				classMapping.addCardMapping(cardId, new DefaultCardMapping(layer.getName(), layer.getDescription()));
			}
		}

		return mapping;
	}

	/* Common layers methods */

	@Override
	public List<LayerMetadata> list() throws Exception {
		ensureGisIsEnabled();
		return layerMetadataStore.list();
	}

	@Override
	public void setLayerVisisbility( //
			final String layerFullName, //
			final String visibleTable, //
			final boolean visible //
	) throws Exception {

		ensureGisIsEnabled();
		layerMetadataStore.updateLayerVisibility(layerFullName, visibleTable, visible);
	}

	@Override
	@Transactional
	public void reorderLayers(final int oldIndex, final int newIndex) throws Exception {
		ensureGisIsEnabled();

		OrderingUtils.alterPosition(list(), oldIndex, newIndex, new PositionHandler<LayerMetadata>() {
			@Override
			public int getPosition(final LayerMetadata l) {
				return l.getIndex();
			}

			@Override
			public void setPosition(final LayerMetadata l, final int p) {
				layerMetadataStore.setLayerIndex(l, p);
			}
		});
	}

	/* DomainTreeNavigation */

	@Override
	public void saveGisTreeNavigation(final DomainTreeNode root) {
		domainTreeStore.createOrReplaceTree(DOMAIN_TREE_TYPE, null, root);
	}

	@Override
	public void removeGisTreeNavigation() {
		domainTreeStore.removeTree(DOMAIN_TREE_TYPE);
	}

	@Override
	public DomainTreeNode getGisTreeNavigation() {
		return domainTreeStore.getDomainTree(DOMAIN_TREE_TYPE);
	}

	@Override
	public DomainTreeCardNode expandDomainTree(final DataAccessLogic dataAccesslogic) {
		final Map<String, Long> domainIds = getDomainIds(dataAccesslogic);
		final Map<Long, DomainTreeCardNode> nodes = new HashMap<Long, DomainTreeCardNode>();

		final DomainTreeNode root = this.getGisTreeNavigation();
		final DomainTreeCardNode rootCardNode = new DomainTreeCardNode();

		if (root != null) {
			rootCardNode.setText(root.getTargetClassDescription());
			rootCardNode.setExpanded(true);
			rootCardNode.setLeaf(false);

			nodes.put(rootCardNode.getCardId(), rootCardNode);

			final FetchCardListResponse domainTreeCards = dataAccesslogic.fetchCards( //
					root.getTargetClassName(), //
					QueryOptions.newQueryOption().build() //
					);

			for (final Card card : domainTreeCards) {
				final DomainTreeCardNode node = new DomainTreeCardNode();
				node.setText(card.getAttribute("Description", String.class));
				node.setClassName(card.getClassName());
				node.setClassId(new Long(card.getClassId()));
				node.setCardId(new Long(card.getId()));
				node.setLeaf(false);

				rootCardNode.addChild(node);
				nodes.put(node.getCardId(), node);
			}

			fetchRelationsByDomain(dataAccesslogic, domainIds, root, nodes);
			rootCardNode.sortByText();
			setDefaultCheck(nodes);
		}

		return rootCardNode;
	}

	/*
	 * the default check is that: identify the base nodes, AKA the nodes created
	 * expanding the base domain this nodes represents the base level, and we
	 * want that only the first child of a siblings group was checked. Also all
	 * the ancestors of this node must be checked
	 */
	private void setDefaultCheck(final Map<Long, DomainTreeCardNode> nodes) {
		final Map<Object, DomainTreeCardNode> visitedNodes = new HashMap<Object, DomainTreeCardNode>();

		for (final DomainTreeCardNode node : nodes.values()) {
			if (node.isBaseNode()) {
				final DomainTreeCardNode parent = node.parent();
				if (parent != null && !visitedNodes.containsKey(parent.getCardId())) {

					parent.getChildren().get(0).setChecked(true, true, true);
					visitedNodes.put(parent.getCardId(), parent);
				}
			}
		}
	}

	private void fetchRelationsByDomain(final DataAccessLogic dataAccesslogic, final Map<String, Long> domainIds,
			final DomainTreeNode root, final Map<Long, DomainTreeCardNode> nodes) {

		final Map<Object, Map<Object, List<RelationInfo>>> relationsByDomain = new HashMap<Object, Map<Object, List<RelationInfo>>>();

		for (final DomainTreeNode domainTreeNode : root.getChildNodes()) {
			final Long domainId = domainIds.get(domainTreeNode.getDomainName());
			final String querySource = domainTreeNode.isDirect() ? "_1" : "_2";
			final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
			final Map<Object, List<RelationInfo>> relations = dataAccesslogic.relationsBySource(
					root.getTargetClassName(), dom);
			relationsByDomain.put(domainId, relations);
			final boolean leaf = domainTreeNode.getChildNodes().size() == 0;
			final boolean baseNode = domainTreeNode.isBaseNode();
			fillNodes(nodes, relationsByDomain, leaf, baseNode);
			fetchRelationsByDomain(dataAccesslogic, domainIds, domainTreeNode, nodes);
		}
	}

	private void fillNodes(final Map<Long, DomainTreeCardNode> nodes,
			final Map<Object, Map<Object, List<RelationInfo>>> relationsByDomain, final boolean leaf,
			final boolean baseNode) {

		for (final Map<Object, List<RelationInfo>> relationsBySource : relationsByDomain.values()) {
			for (final Object sourceCardId : relationsBySource.keySet()) {
				final DomainTreeCardNode parent = nodes.get(sourceCardId);

				if (parent == null) {
					continue;
				}

				for (final RelationInfo ri : relationsBySource.get(sourceCardId)) {
					final DomainTreeCardNode child = new DomainTreeCardNode();
					String text = ri.getTargetDescription();
					if (text == null || text.equals("")) {
						text = ri.getTargetCode();
					}

					child.setText(text);
					child.setCardId(ri.getTargetId());
					child.setClassId(ri.getTargetType().getId());
					child.setClassName(ri.getTargetType().getIdentifier().getLocalName());
					child.setLeaf(leaf);
					child.setBaseNode(baseNode);

					parent.addChild(child);
					nodes.put(child.getCardId(), child);
				}
			}
		}
	}

	/* private methods */

	private String createGeoAttributeTable( //
			final String targetClassName, //
			final LayerMetadata layerMetadata //
	) {

		return geoFeatureStore.createGeoTable(targetClassName, layerMetadata);
	}

	private LayerMetadata modifyLayerMetadata(final String targetTableName, final String name,
			final String description, final int minimumZoom, final int maximumZoom, final String style,
			final Set<String> cardBinding) {

		final String fullName = fullName(targetTableName, name);
		final LayerMetadata changes = new LayerMetadata();
		changes.setDescription(description);
		changes.setMinimumZoom(minimumZoom);
		changes.setMaximumzoom(maximumZoom);
		changes.setMapStyle(style);
		changes.setCardBinding(cardBinding);

		return layerMetadataStore.updateLayer(fullName, changes);
	}

	private void ensureGisIsEnabled() throws Exception {
		if (!isGisEnabled()) {
			throw new Exception("GIS Module is non enabled");
		}
	}

	private void ensureGeoServerIsEnabled() throws Exception {
		if (!configuration.isGeoServerEnabled()) {
			throw new Exception("GEOServer is non enabled");
		}
	}

	private String fullName(final String masterTableName, final String name) {
		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, masterTableName, name);
		return fullName;
	}

	private Map<String, Long> getDomainIds(final DataAccessLogic dataAccessLogic) {
		final Map<String, Long> domainIds = new HashMap<String, Long>();

		for (final CMDomain d : dataAccessLogic.findActiveDomains()) {
			domainIds.put(d.getIdentifier().getLocalName(), new Long(d.getId()));
		}

		return domainIds;
	}

}
