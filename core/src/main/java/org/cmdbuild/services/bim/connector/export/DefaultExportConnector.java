package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.DEFAULT_TAG_EXPORT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_ELEMENT_PROXY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_DESCRIPTION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_FURNISHING;
import static org.cmdbuild.bim.utils.BimConstants.IFC_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TAG;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.bim.utils.BimConstants.isValidId;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.logic.bim.project.ConversionUtils.TO_MODIFIABLE_PERSISTENCE_PROJECT;
import static org.cmdbuild.services.bim.DefaultBimDataView.CONTAINER_GUID;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.bim.mapper.DefaultAttribute;
import org.cmdbuild.bim.mapper.DefaultEntity;
import org.cmdbuild.bim.mapper.xml.XmlExportCatalogFactory;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.model.bim.StorableLayer;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataChangedException;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataNotChangedException;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

public class DefaultExportConnector implements GenericMapper {

	private final BimFacade serviceFacade;
	private final BimPersistence persistence;
	private final BimDataView bimDataView;
	private final Map<String, Map<String, String>> shapeNameToOidMap;
	private final Iterable<String> candidateTypes = Lists.newArrayList(IFC_BUILDING_ELEMENT_PROXY, IFC_FURNISHING);
	private Catalog catalog;
	private String exportProjectId;
	private String sourceProjectId;
	private Long rootCardId;

	public DefaultExportConnector(final BimDataView dataView, final BimFacade bimServiceFacade,
			final BimPersistence bimPersistence, final ExportPolicy exportProjectPolicy) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
		shapeNameToOidMap = Maps.newHashMap();
	}

	@Override
	public void setTarget(final Object input, final Output output) {
		final String baseProjectId = String.class.cast(input);
		exportProjectId = getExportProjectId(baseProjectId);
		final boolean shapesLoaded = areShapesOfCatalogAlreadyLoadedInRevision();
		if (!shapesLoaded) {
			output.outputInvalid(exportProjectId);
		}
	}

	@Override
	public void executeSynchronization(final Map<String, Entity> entriesToCreate,
			final Map<String, ValueDifference<Entity>> entriesToUpdate, final Map<String, Entity> entriesToRemove,
			final Output output) {
		try {
			for (final String keyToCreate : entriesToCreate.keySet()) {
				final Entity entityToCreate = entriesToCreate.get(keyToCreate);
				output.createTarget(entityToCreate, exportProjectId);
			}
			for (final String keyToUpdate : entriesToUpdate.keySet()) {
				final ValueDifference<Entity> entityToUpdate = entriesToUpdate.get(keyToUpdate);
				final Entity entityToRemove = entityToUpdate.rightValue();
				final Entity entityToCreate = entityToUpdate.leftValue();
				final boolean toUpdate = areDifferent(entityToRemove, entityToCreate);
				if (toUpdate) {
					output.createTarget(entityToCreate, exportProjectId);
					output.deleteTarget(entityToRemove, exportProjectId);
				}
			}
			for (final String keyToRemove : entriesToRemove.keySet()) {
				final Entity entityToRemove = entriesToRemove.get(keyToRemove);
				output.deleteTarget(entityToRemove, exportProjectId);
			}

		} catch (final DataChangedException d) {
			serviceFacade.abortTransaction();
			throw new DataChangedException();
		} catch (final DataNotChangedException d) {
			serviceFacade.abortTransaction();
		}
	}

	@Override
	public void afterExecution(final Output output) {
		output.finalActions(exportProjectId);
		System.out.println("Commit transaction...");
		final String revisionId = serviceFacade.commitTransaction();
		System.out.println("Revision " + revisionId + " created at " + new DateTime());

		/*
		 * In order to see the generated objects I have to download and upload
		 * again the file. This is due to some problems with BimServer cache.
		 */
		try {
			final DataHandler exportedData = serviceFacade.download(exportProjectId);
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			exportedData.writeTo(outputStream);
			final String newExportProject = serviceFacade.createProject(revisionId + "-exported");
			serviceFacade.checkin(newExportProject, file);
			System.out.println("export file on new project " + newExportProject + " is ready");
			final PersistenceProject baseProject = persistence.read(sourceProjectId);
			final PersistenceProject updatedProject = TO_MODIFIABLE_PERSISTENCE_PROJECT.apply(baseProject);
			updatedProject.setExportProjectId(newExportProject);
			persistence.saveProject(updatedProject);
		} catch (final Throwable t) {
			throw new BimError("Problem while downloading and uploading the generated file", t);
		}
	}

	@Override
	public void beforeExecution() {
		serviceFacade.openTransaction(exportProjectId);
	}

	private boolean areShapesOfCatalogAlreadyLoadedInRevision() {
		boolean allShapesAreLoaded = true;
		final String revisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		for (final EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
			final String shapeOid = getShapeOid(revisionId, catalogEntry.getShape());
			if (!isValidId(shapeOid)) {
				allShapesAreLoaded = false;
				break;
			}
		}
		return allShapesAreLoaded;
	}

	private boolean areDifferent(final Entity entityToRemove, final Entity entityToCreate) {
		final String oldName = entityToRemove.getAttributeByName(IFC_NAME).getValue();
		final String newName = entityToCreate.getAttributeByName(CODE_ATTRIBUTE).getValue();
		if (!StringUtils.equals(oldName, newName)) {
			return true;
		}
		final String oldDescription = entityToRemove.getAttributeByName(IFC_DESCRIPTION).getValue();
		final String newDescription = entityToCreate.getAttributeByName(DESCRIPTION_ATTRIBUTE).getValue();
		if (!StringUtils.equals(oldDescription, newDescription)) {
			return true;
		}
		final String oldType = entityToRemove.getTypeName();
		final String newType = entityToCreate.getAttributeByName(IFC_TYPE).getValue();
		if (!StringUtils.equals(oldType, newType)) {
			return true;
		}
		final String oldSpace = entityToRemove.getAttributeByName(CONTAINER_GUID).getValue();
		final String newSpace = entityToCreate.getAttributeByName(CONTAINER_GUID).getValue();
		if (!StringUtils.equals(oldSpace, newSpace)) {
			return true;
		}
		return false;
	}

	private String getShapeOid(final String revisionId, final String shapeName) {
		String shapeOid = StringUtils.EMPTY;
		if (shapeNameToOidMap.containsKey(revisionId)) {
			final Map<String, String> mapForCurrentRevision = shapeNameToOidMap.get(revisionId);
			if (mapForCurrentRevision.containsKey(shapeName)) {
				shapeOid = mapForCurrentRevision.get(shapeName);
			} else {
				shapeOid = serviceFacade.findShapeWithName(shapeName, revisionId);
				if (isValidId(shapeOid)) {
					mapForCurrentRevision.put(shapeName, shapeOid);
				}
			}
		} else {
			shapeOid = serviceFacade.findShapeWithName(shapeName, revisionId);
			if (isValidId(shapeOid)) {
				final Map<String, String> mapForCurrentRevision = Maps.newHashMap();
				shapeNameToOidMap.put(revisionId, mapForCurrentRevision);
				mapForCurrentRevision.put(shapeName, shapeOid);
			}
		}
		return shapeOid;
	}

	private String getExportProjectId(final String masterProjectId) {
		final String targetProjectId = persistence.read(masterProjectId).getExportProjectId();
		if (targetProjectId == null || targetProjectId.isEmpty() || targetProjectId.equals(INVALID_ID)) {
			throw new BimError("Project for export not found");
		}
		return targetProjectId;
	}

	private String getExportRevisionId(final String masterProjectId) {
		final String exportProjectId = getExportProjectId(masterProjectId);
		final String exportRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		if (exportRevisionId == null || exportRevisionId.isEmpty() || exportRevisionId.equals("-1")) {
			throw new BimError("Revision for export not found");
		}
		return exportRevisionId;
	}

	@Override
	public String getLastGeneratedOutput(final Object input) {
		final String inputProjectId = String.class.cast(input);
		final String exportProjectId = getExportProjectId(inputProjectId);
		final String outputRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		return outputRevisionId;
	}

	@Override
	public void setConfiguration(final Object input) {
		sourceProjectId = String.class.cast(input);
		rootCardId = getRootCardIdForProjectId(sourceProjectId);
		final PersistenceProject project = persistence.read(sourceProjectId);
		final String xmlMapping = project.getExportMapping();
		catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();
	}

	@Override
	public Map<String, Entity> getTargetData() {
		final String exportRevisionId = serviceFacade.getLastRevisionOfProject(exportProjectId);
		final Map<String, Entity> targetData = Maps.newHashMap();
		for (final String type : candidateTypes) {
			final Iterable<Entity> entityList = serviceFacade.fetchEntitiesOfType(type, exportRevisionId);
			for (final Entity entity : entityList) {
				final Attribute tag = entity.getAttributeByName(IFC_TAG);
				if (!tag.isValid() || !DEFAULT_TAG_EXPORT.equals(tag.getValue())) {
					continue;
				} else {
					final String globalId = entity.getKey();
					final Long oid = BimserverEntity.class.cast(entity).getOid();
					final String name = entity.getAttributeByName(IFC_NAME).getValue();
					final String description = entity.getAttributeByName(IFC_DESCRIPTION).getValue();
					final DefaultEntity targetEntity = DefaultEntity.withTypeAndKey(entity.getTypeName(), globalId);
					final String containerGlobalId = serviceFacade.getContainerOfEntity(globalId, exportRevisionId);

					targetEntity.addAttribute(DefaultAttribute.withNameAndValue(IFC_NAME, name));
					targetEntity.addAttribute(DefaultAttribute.withNameAndValue(IFC_DESCRIPTION, description));
					targetEntity.addAttribute(DefaultAttribute.withNameAndValue(OBJECT_OID, oid.toString()));
					targetEntity.addAttribute(DefaultAttribute.withNameAndValue(CONTAINER_GUID, containerGlobalId));
					if (entity.isValid()) {
						targetData.put(globalId, targetEntity);
					}
				}
			}
		}
		return targetData;
	}

	@Override
	public Map<String, Entity> getSourceData() {
		final Map<String, Entity> dataSource = Maps.newHashMap();
		for (final EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
			final String className = catalogEntry.getLabel();
			final String containerAttributeName = catalogEntry.getContainerAttribute();
			final String shapeOid = getShapeOid(getExportRevisionId(sourceProjectId), catalogEntry.getShape());
			final String ifcType = catalogEntry.getTypeName();
			final StorableLayer layer = persistence.readLayer(className);
			final String rootReference = layer.getRootReference();
			final String containerClassName = persistence.findContainer().getClassName();

			final List<CMCard> cardsOfSource = bimDataView.getCardsWithAttributeAndValue(
					DBIdentifier.fromName(className), rootCardId, rootReference);
			for (final CMCard card : cardsOfSource) {
				final Entity cardToExport = bimDataView.getCardDataForExport(card.getId(), className,
						containerAttributeName, containerClassName, shapeOid, ifcType);
				if (cardToExport.isValid()) {
					dataSource.put(cardToExport.getKey(), cardToExport);
				}
			}
		}
		return dataSource;
	}

	private Long getRootCardIdForProjectId(final String projectId) {

		final String rootClassName = persistence.findRoot().getClassName();

		final Long cardId = bimDataView.getRootCardIdFromProjectId(projectId, rootClassName);

		return cardId;
	}

}
