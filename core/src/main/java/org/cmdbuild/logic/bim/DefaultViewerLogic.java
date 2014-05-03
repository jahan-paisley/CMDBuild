package org.cmdbuild.logic.bim;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.bim.utils.BimConstants.CARDID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CARD_DESCRIPTION_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSID_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.CLASSNAME_FIELD_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_ELEMENT_PROXY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_STOREY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_FURNISHING;
import static org.cmdbuild.bim.utils.BimConstants.IFC_SPACE;
import static org.cmdbuild.bim.utils.BimConstants.isValidId;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.bim.LayerLogic.Layer;
import org.cmdbuild.model.bim.StorableLayer;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.DefaultBimDataView.BimCard;
import org.cmdbuild.services.bim.connector.export.ConnectorFramework;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultViewerLogic implements ViewerLogic {

	private final BimFacade bimServiceFacade;
	private final BimPersistence bimPersistence;
	private final BimDataView bimDataView;
	private final ExportPolicy exportPolicy;
	private final Map<String, Map<String, Long>> guidToOidMap = Maps.newHashMap();
	private final LayerLogic layerLogic;
	private final SynchronizationLogic synchronizationLogic;
	private final ConnectorFramework connectorFramework;

	public DefaultViewerLogic( //
			final BimFacade bimServiceFacade, //
			final BimPersistence bimPersistence, //
			final BimDataView bimDataView, //
			final ExportPolicy exportStrategy, //
			final LayerLogic layerLogic, //
			final SynchronizationLogic synchronizationLogic, //
			final ConnectorFramework connectorFramework) {

		this.bimPersistence = bimPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataView = bimDataView;
		this.exportPolicy = exportStrategy;
		this.layerLogic = layerLogic;
		this.synchronizationLogic = synchronizationLogic;
		this.connectorFramework = connectorFramework;
	}

	// methods for the viewer

	@Override
	public BimCard fetchCardDataFromObjectId(final String objectId, final String revisionId) {
		final String globalId = bimServiceFacade.fetchGlobalIdFromObjectId(objectId, revisionId);
		final BimCard bimCard = bimDataView.getBimDataFromGlobalid(globalId);
		return bimCard;
	}

	@Override
	public String getDescriptionOfRoot(final Long cardId, final String className) {
		final Long rootId = getRootId(cardId, className);
		final StorableLayer rootLayer = bimPersistence.findRoot();
		if (rootLayer == null || rootLayer.getClassName() == null || rootLayer.getClassName().isEmpty()) {
			throw new BimError("Root layer not configured");
		}
		final CMCard rootCard = bimDataView.fetchCard(rootLayer.getClassName(), rootId);
		final String description = String.class.cast(rootCard.getDescription());
		return description;
	}

	@Override
	public String getBaseRevisionIdForViewer(final Long cardId, final String className) {
		final String baseProjectId = getBaseProjectIdForCardOfClass(cardId, className);
		final String revisionId = getLastRevisionOfProject(baseProjectId);
		return revisionId;
	}

	@Override
	public String getExportedRevisionIdForViewer(final Long cardId, final String className) {

		final String baseProjectId = getBaseProjectIdForCardOfClass(cardId, className);
		String outputRevisionId = EMPTY;
		if (isValidId(baseProjectId)) {
			if (exportPolicy.forceUpdate()) {
				synchronizationLogic.exportIfc(baseProjectId);
			}
			outputRevisionId = String.class.cast(connectorFramework.getLastGeneratedOutput(baseProjectId));
		}
		return outputRevisionId;
	}

	@Override
	public String getBaseProjectId(final Long cardId, final String className) {
		return getBaseProjectIdForCardOfClass(cardId, className);
	}

	@Override
	public String getOutputForBimViewer(final String revisionId, final String baseProjectId) {
		System.out.println("----------\nGet output for viewer: open revision " + revisionId);
		final DataHandler jsonFile = bimServiceFacade.fetchProjectStructure(revisionId);
		try {
			final Long rootCardId = getRootCardIdForProjectId(baseProjectId);
			final Reader reader = new InputStreamReader(jsonFile.getInputStream(), "UTF-8");
			final BufferedReader fileReader = new BufferedReader(reader);
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode rootNode = mapper.readTree(fileReader);
			final JsonNode data = rootNode.findValue("data");
			final JsonNode properties = data.findValue("properties");
			final Iterable<Layer> activeLayers = layerLogic.getActiveLayers();
			for (final Layer layer : activeLayers) {
				final String className = layer.getClassName();
				final String rootClassName = layerLogic.getRootLayer().getClassName();
				List<BimCard> bimCards = Lists.newArrayList();
				if (className.equals(rootClassName)) {
					final BimCard rootBimCard = bimDataView.getBimCardFromRootId(className, rootCardId);
					if (rootBimCard != null) {
						bimCards.add(rootBimCard);
					}
				} else {
					final String rootReferenceName = layer.getRootReference();
					if (isNotBlank(rootReferenceName)) {
						bimCards = bimDataView.getBimCardsWithGivenValueOfRootReferenceAttribute(className, rootCardId,
								rootReferenceName);
					}
				}
				for (final BimCard bimCard : bimCards) {
					final String guid = bimCard.getGlobalId();
					Long oid = (long) -1;
					if (guidToOidMap.containsKey(revisionId)) {
						final Map<String, Long> revisionMap = guidToOidMap.get(revisionId);
						if (revisionMap.containsKey(guid)) {
							oid = revisionMap.get(guid);
						}
					} else {
						oid = bimServiceFacade.getOidFromGlobalId(guid, revisionId, Lists.newArrayList(IFC_BUILDING,
								IFC_BUILDING_STOREY, IFC_SPACE, IFC_BUILDING_ELEMENT_PROXY, IFC_FURNISHING));
						if (guidToOidMap.containsKey(revisionId)) {
							final Map<String, Long> revisionMap = guidToOidMap.get(revisionId);
							revisionMap.put(guid, oid);
						} else {
							final Map<String, Long> revisionMap = Maps.newHashMap();
							revisionMap.put(guid, oid);
						}
					}
					final String oidAsString = String.valueOf(oid);
					if (isValidId(oidAsString)) {
						final ObjectNode property = (ObjectNode) properties.findValue(oidAsString);
						final ObjectNode cmdbuildData = mapper.createObjectNode();
						cmdbuildData.put(CARDID_FIELD_NAME, bimCard.getId());
						cmdbuildData.put(CLASSID_FIELD_NAME, bimCard.getClassId());
						cmdbuildData.put(CLASSNAME_FIELD_NAME, bimCard.getClassName());
						cmdbuildData.put(CARD_DESCRIPTION_FIELD_NAME, bimCard.getCardDescription());
						property.put("cmdbuild_data", cmdbuildData);
					}
				}
			}
			return rootNode.toString();
		} catch (final Throwable t) {
			throw new BimError("Cannot read the Json", t);
		}
	}

	private Long getRootId(final Long cardId, final String className) {
		Long rootId = null;
		final StorableLayer rootLayer = bimPersistence.findRoot();
		if (rootLayer == null || rootLayer.getClassName() == null || rootLayer.getClassName().isEmpty()) {
			throw new BimError("Root layer not configured");
		}
		if (className.equals(rootLayer.getClassName())) {
			rootId = cardId;
		} else {
			final StorableLayer layer = bimPersistence.readLayer(className);
			if (layer == null || layer.getRootReference() == null || layer.getRootReference().isEmpty()) {
				throw new BimError("'" + className + "' layer not configured");
			}
			final String referenceRoot = layer.getRootReference();
			rootId = bimDataView.getRootId(cardId, className, referenceRoot);
			if (rootId == null) {
				throw new BimError(referenceRoot + " is null for card '" + cardId + "' of class '" + className + "'");
			}
		}
		return rootId;
	}

	private String getBaseProjectIdForCardOfClass(final Long cardId, final String className) {
		final Long rootId = getRootId(cardId, className);
		final StorableLayer rootLayer = bimPersistence.findRoot();
		final String baseProjectId = getProjectIdForRootClass(rootId, rootLayer.getClassName());
		return baseProjectId;
	}

	private String getProjectIdForRootClass(final Long rootId, final String rootClassName) {
		final Long projectCardId = bimDataView.getProjectCardIdFromRootCard(rootId, rootClassName);
		final String projectId = bimPersistence.getProjectIdFromCardId(projectCardId);
		return projectId;
	}

	private Long getRootCardIdForProjectId(final String projectId) {

		final String rootClassName = bimPersistence.findRoot().getClassName();

		Long cardId = (long) -1;

		cardId = bimDataView.getRootCardIdFromProjectId(projectId, rootClassName);
		return cardId;
	}

	private String getLastRevisionOfProject(final String projectId) {
		String revisionId = EMPTY;
		if (!projectId.isEmpty()) {
			revisionId = bimServiceFacade.getLastRevisionOfProject(projectId);
		}
		return revisionId;
	}

	@Override
	public void moveObjectToPosition(final String projectId, final String className, final String globalId,
			final List<Double> coordinates) {
		bimServiceFacade.moveObject(projectId, globalId, coordinates);
		bimDataView.moveObject(className, globalId, coordinates);
	}

}