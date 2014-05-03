package org.cmdbuild.services.bim;

import static org.cmdbuild.bim.utils.BimConstants.DEFAULT_TAG_EXPORT;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_AXIS2_PLACEMENT3D;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_ELEMENT_PROXY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_CARTESIAN_POINT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.IFC_DESCRIPTION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_FURNISHING;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCAL_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCATION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_OBJECT_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_OBJECT_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_PRODUCT_DEFINITION_SHAPE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATED_ELEMENTS;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATING_STRUCTURE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATIVE_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_REL_CONTAINED;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TAG;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.isValidId;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.services.bim.DefaultBimDataView.SHAPE_OID;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class DefaultBimFacade implements BimFacade {

	private final BimService service;
	private final TransactionManager transactionManager;
	private final Reader reader;
	private final Iterable<String> candidateTypes = Lists.newArrayList(IFC_BUILDING_ELEMENT_PROXY, IFC_FURNISHING);

	public DefaultBimFacade(final BimService bimservice, final TransactionManager transactionManager) {
		this.service = bimservice;
		this.transactionManager = transactionManager;
		reader = new BimReader(bimservice);
	}

	@Override
	public void openTransaction(final String projectId) {
		transactionManager.open(projectId);
	}

	@Override
	public String commitTransaction() {
		return transactionManager.commit();
	}

	@Override
	public void abortTransaction() {
		transactionManager.abort();
	}

	@Override
	public BimFacadeProject createProjectAndUploadFile(final BimFacadeProject project) {
		final BimProject createdProject = service.createProject(project.getName());
		final String projectId = createdProject.getIdentifier();
		if (project.getFile() != null) {
			final DateTime lastCheckin = service.checkin(createdProject.getIdentifier(), project.getFile());
			final BimProject updatedProject = service.getProjectByPoid(projectId);
			createdProject.setLastCheckin(lastCheckin);
			final String lastRevisionOfProject = service.getLastRevisionOfProject(updatedProject.getIdentifier());
			if (!isValidId(lastRevisionOfProject)) {
				throw new BimError("Upload failed");
			}
		}
		final BimFacadeProject facadeProject = from(createdProject, "");
		return facadeProject;
	}

	@Override
	public String createProject(final String projectName) {
		final BimProject project = service.createProject(projectName);
		return project.getIdentifier();
	}

	@Override
	public BimFacadeProject createBaseAndExportProject(final BimFacadeProject project) {
		final BimProject baseProject = service.createProject(project.getName());
		final String projectId = baseProject.getIdentifier();
		if (project.getFile() != null) {
			final DateTime lastCheckin = service.checkin(baseProject.getIdentifier(), project.getFile());
			baseProject.setLastCheckin(lastCheckin);
			final BimRevision lastRevision = service.getRevision(service.getLastRevisionOfProject(projectId));
			if (!lastRevision.isValid()) {
				throw new BimError("Upload failed");
			}
		}
		final String exportProjectName = "_export_" + project.getName();
		final BimProject exportProject = service.createProject(exportProjectName);
		final BimFacadeProject facadeProject = from(baseProject, exportProject.getIdentifier());
		return facadeProject;
	}

	@Override
	public BimFacadeProject updateProject(final BimFacadeProject project) {
		final String projectId = project.getProjectId();
		BimProject bimProject = service.getProjectByPoid(projectId);
		if (project.getFile() != null) {
			final DateTime checkin = service.checkin(projectId, project.getFile());
			bimProject = service.getProjectByPoid(projectId);
			bimProject.setLastCheckin(checkin);
		}
		if (project.isActive() != bimProject.isActive()) {
			if (project.isActive()) {
				service.enableProject(projectId);
			} else {
				service.disableProject(projectId);
			}
		}
		final BimFacadeProject facadeProject = from(bimProject, "");
		return facadeProject;
	}

	private static BimFacadeProject from(final BimProject createdProject, final String exportProjectId) {
		final BimFacadeProject project = new BimFacadeProject() {

			@Override
			public boolean isSynch() {
				return false;
			}

			@Override
			public boolean isActive() {
				return createdProject.isActive();
			}

			@Override
			public String getProjectId() {
				return createdProject.getIdentifier();
			}

			@Override
			public String getName() {
				return createdProject.getName();
			}

			@Override
			public DateTime getLastCheckin() {
				return createdProject.getLastCheckin();
			}

			@Override
			public File getFile() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setLastCheckin(final DateTime lastCheckin) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getShapeProjectId() {
				throw new UnsupportedOperationException("to do");
			}

			@Override
			public String getExportProjectId() {
				return exportProjectId;
			}

		};
		return project;

	}

	@Override
	public void disableProject(final BimFacadeProject project) {
		login();
		service.disableProject(project.getProjectId());
		logout();

	}

	@Override
	public void enableProject(final BimFacadeProject project) {
		login();
		service.enableProject(project.getProjectId());
		logout();
	}

	@Override
	public DataHandler download(final String projectId) {
		final String revisionId = service.getLastRevisionOfProject(projectId);
		if ((INVALID_ID).equals(revisionId)) {
			return null;
		}
		return service.downloadIfc(revisionId);
	}

	@Override
	public Iterable<Entity> fetchEntitiesOfType(final String ifcType, final String revisionId) {
		return service.getEntitiesByType(ifcType, revisionId);
	}

	@Override
	public List<Entity> readEntityFromProject(final EntityDefinition entityDefinition, final String projectId) {
		login();
		final String revisionId = service.getLastRevisionOfProject(projectId);
		final List<Entity> source = reader.readEntities(revisionId, entityDefinition);
		logout();
		return source;
	}

	private void login() {
	}

	private void logout() {
	}

	@Override
	public String createCard(final Entity entityToCreate, final String targetProjectId) {
		final String ifcType = entityToCreate.getAttributeByName(IFC_TYPE).getValue();
		final String cmId = entityToCreate.getAttributeByName(ID_ATTRIBUTE).getValue();
		final String baseClass = entityToCreate.getAttributeByName(BASE_CLASS_NAME).getValue();
		final String globalId = entityToCreate.getAttributeByName(GLOBALID_ATTRIBUTE).getValue();
		final String code = entityToCreate.getAttributeByName(CODE_ATTRIBUTE).getValue();
		final String description = entityToCreate.getAttributeByName(DESCRIPTION_ATTRIBUTE).getValue();
		final String shapeOid = entityToCreate.getAttributeByName(SHAPE_OID).getValue();
		final String xcord = entityToCreate.getAttributeByName(X_ATTRIBUTE).getValue();
		final String ycord = entityToCreate.getAttributeByName(Y_ATTRIBUTE).getValue();
		final String zcord = entityToCreate.getAttributeByName(Z_ATTRIBUTE).getValue();

		System.out.println("Insert card " + cmId);
		System.out.println("IFC TYPE " + ifcType);
		System.out.println("BASE_CLASS_NAME " + baseClass);
		System.out.println("GLOBALID_ATTRIBUTE " + globalId);
		System.out.println("CODE_ATTRIBUTE " + code);
		System.out.println("DESCRIPTION_ATTRIBUTE " + description);
		System.out.println("SHAPE OID " + shapeOid);
		System.out.println("DEFAULT_TAG_EXPORT " + DEFAULT_TAG_EXPORT);
		System.out.println("X " + xcord);
		System.out.println("Y " + ycord);
		System.out.println("Z " + zcord);

		final String objectOid = service.createObject(transactionManager.getId(), ifcType);
		service.setStringAttribute(transactionManager.getId(), objectOid, IFC_OBJECT_TYPE, baseClass);
		service.setStringAttribute(transactionManager.getId(), objectOid, IFC_GLOBALID, globalId);
		service.setStringAttribute(transactionManager.getId(), objectOid, IFC_NAME, code);
		service.setStringAttribute(transactionManager.getId(), objectOid, IFC_DESCRIPTION, description);
		service.setStringAttribute(transactionManager.getId(), objectOid, IFC_TAG, DEFAULT_TAG_EXPORT);
		service.setReference(transactionManager.getId(), objectOid, "Representation", shapeOid);

		setPlacementForObject(objectOid, xcord, ycord, zcord);

		return objectOid;
	}

	private void setPlacementForObject(final String objectOid, final String xcord, final String ycord,
			final String zcord) {
		final String placementOid = service.createObject(transactionManager.getId(), IFC_LOCAL_PLACEMENT);
		service.setReference(transactionManager.getId(), objectOid, IFC_OBJECT_PLACEMENT, placementOid);
		setCoordinates(placementOid, xcord, ycord, zcord, transactionManager.getId());
	}

	@Override
	public void moveObject(final String projectId, final String globalId, final List<Double> coordinates) {
		System.out.println("move on transaction " + transactionManager.getId());
		openTransaction(projectId);
		System.out.println("open transaction " + transactionManager.getId());
		final String revisionId = getLastRevisionOfProject(projectId);
		System.out.println("revision is " + revisionId);
		final BimserverEntity object = (BimserverEntity) fetchEntityFromGlobalId(revisionId, globalId, candidateTypes);
		System.out.println("object is " + object.getOid());
		final BimserverEntity objectPlacement = (BimserverEntity) getReferencedEntity(revisionId, object,
				IFC_OBJECT_PLACEMENT);
		System.out.println("placement is " + objectPlacement.getOid());
		final BimserverEntity relativePlacement = (BimserverEntity) getReferencedEntity(revisionId, objectPlacement,
				IFC_RELATIVE_PLACEMENT);
		System.out.println("relativePlacement is " + relativePlacement.getOid());
		final BimserverEntity cartesianPoint = (BimserverEntity) getReferencedEntity(revisionId, relativePlacement,
				IFC_LOCATION);
		System.out.println("cartesianPoint is " + cartesianPoint.getOid());
		final String cartesianPointId = String.valueOf(cartesianPoint.getOid());

		service.setDoubleAttributes(transactionManager.getId(), cartesianPointId, IFC_COORDINATES, coordinates);

		String newRevisionId = commitTransaction();
		System.out.println("Revision " + newRevisionId + " created");
		// refreshWithMerge(projectId);
	}

	@Override
	public String removeCard(final Entity entityToRemove, final String targetProjectId) {

		final String oid = entityToRemove.getAttributeByName(OBJECT_OID).getValue();
		service.removeObject(transactionManager.getId(), oid);
		return oid;
	}

	@Override
	public String findShapeWithName(final String shapeName, final String revisionId) {
		final Iterable<Entity> shapeList = service.getEntitiesByType(IFC_PRODUCT_DEFINITION_SHAPE, revisionId);
		for (final Entity shape : shapeList) {
			final Attribute shapeNameAttribute = shape.getAttributeByName("Name");
			if (shapeNameAttribute.getValue() != null && shapeNameAttribute.getValue().equals(shapeName)) {
				System.out.println("Shape found with id " + shape.getKey());
				return shape.getKey();
			}
		}
		return INVALID_ID;
	}

	private void setCoordinates(final String placementId, final String x1, final String x2, final String x3,
			final String transactionId) {
		final double x1d = Double.parseDouble(x1);
		final double x2d = Double.parseDouble(x2);
		final double x3d = Double.parseDouble(x3);

		final String relativePlacementId = service.createObject(transactionId, IFC_AXIS2_PLACEMENT3D);
		service.setReference(transactionId, placementId, IFC_RELATIVE_PLACEMENT, relativePlacementId);
		final String cartesianPointId = service.createObject(transactionId, IFC_CARTESIAN_POINT);
		System.out.println("Set coordinates " + x1d + " " + x2d + " " + x3d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x1d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x2d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x3d);
		service.setReference(transactionId, relativePlacementId, IFC_LOCATION, cartesianPointId);
	}

	@Override
	public String getLastRevisionOfProject(final String projectId) {
		return service.getLastRevisionOfProject(projectId);
	}

	@Override
	public String fetchGlobalIdFromObjectId(final String objectId, final String revisionId) {
		final Entity entity = service.getEntityByOid(revisionId, objectId);
		return entity.getKey();
	}

	@Override
	public DataHandler fetchProjectStructure(final String revisionId) {
		return service.fetchProjectStructure(revisionId);

	}

	@Override
	public BimProject getProjectById(final String projectId) {
		return service.getProjectByPoid(projectId);
	}

	@Override
	public Iterable<String> fetchAllGlobalIdForIfcType(final String ifcType, final String revisionId) {
		final List<String> globalIdList = Lists.newArrayList();
		final Iterable<Entity> entities = service.getEntitiesByType(ifcType, revisionId);
		for (final Entity entity : entities) {
			globalIdList.add(entity.getKey());
		}
		return globalIdList;
	}

	@Override
	public BimProject getProjectByName(final String projectId) {
		return service.getProjectByName(projectId);
	}

	@Override
	public Entity fetchEntityFromGlobalId(final String revisionId, final String globalId,
			final Iterable<String> candidateTypes) {
		Entity entity = Entity.NULL_ENTITY;
		entity = service.getEntityByGuid(revisionId, globalId, candidateTypes);
		return entity;
	}

	@Override
	public String getGlobalidFromOid(final String revisionId, final Long oid) {
		final String globalId = service.getGlobalidFromOid(revisionId, oid);
		return globalId;
	}

	@Override
	public String getContainerOfEntity(final String globalId, final String sourceRevisionId) {
		boolean exit = false;
		String containerGlobalId = StringUtils.EMPTY;
		final Iterable<Entity> allRelations = fetchEntitiesOfType(IFC_REL_CONTAINED, sourceRevisionId);
		for (final Entity relation : allRelations) {
			final ReferenceAttribute relatingStructure = ReferenceAttribute.class.cast(relation
					.getAttributeByName(IFC_RELATING_STRUCTURE));
			final ListAttribute relatedElementsAttribute = ListAttribute.class.cast(relation
					.getAttributeByName(IFC_RELATED_ELEMENTS));
			for (final Attribute relatedElement : relatedElementsAttribute.getValues()) {
				if (globalId.equals(relatedElement.getValue())) {
					containerGlobalId = relatingStructure.getGlobalId();
					exit = true;
					break;
				}
			}
			if (exit) {
				break;
			}
		}
		return containerGlobalId;
	}

	@Override
	public void updateRelations(final Map<String, Map<String, List<String>>> relationsMap, final String targetProjectId) {

		final String sourceRevisionId = service.getLastRevisionOfProject(targetProjectId);

		for (final Entry<String, Map<String, List<String>>> entry : relationsMap.entrySet()) {
			final String spaceGuid = entry.getKey();
			final Iterable<Entity> allRelations = fetchEntitiesOfType(IFC_REL_CONTAINED, sourceRevisionId);

			Entity relation = Entity.NULL_ENTITY;
			for (final Entity rel : allRelations) {
				final ReferenceAttribute relatingStructure = ReferenceAttribute.class.cast(rel
						.getAttributeByName(IFC_RELATING_STRUCTURE));
				if (relatingStructure.getGlobalId().equals(spaceGuid)) {
					relation = rel;
					break;
				}
			}
			String relationOid = StringUtils.EMPTY;
			if (relation.isValid()) {
				final BimserverEntity relationEntity = BimserverEntity.class.cast(relation);
				relationOid = relationEntity.getOid().toString();
			} else {
				if (!relation.isValid()) {
					System.out.println("Relation not found for space " + spaceGuid);
					final Entity space = service.getEntityByGuid(sourceRevisionId, spaceGuid,
							Lists.newArrayList("IfcSpace"));
					final Long spaceOid = BimserverEntity.class.cast(space).getOid();
					relationOid = service.createObject(transactionManager.getId(), IFC_REL_CONTAINED);
					System.out.println("Relation with oid " + relationOid + " created");
					service.setReference(transactionManager.getId(), relationOid, IFC_RELATING_STRUCTURE,
							String.valueOf(spaceOid));
					System.out.println("Relating structure " + spaceOid + " added to relation " + relationOid + "'");
				}
			}
			final ListAttribute relatedElementsAttribute = ListAttribute.class.cast(relation
					.getAttributeByName(IFC_RELATED_ELEMENTS));
			final ArrayList<Long> indicesToRemove = Lists.newArrayList();
			final ArrayList<Long> indicesToReadd = Lists.newArrayList();
			final int size = relatedElementsAttribute != null ? relatedElementsAttribute.getValues().size() : 0;
			final Map<String, List<String>> innerMap = entry.getValue();
			if (innerMap.containsKey("D")) {
				final List<String> objectsToRemove = innerMap.get("D");
				for (int i = 0; i < size; i++) {
					final Attribute relatedElement = relatedElementsAttribute.getValues().get(i);
					final String objectGuid = relatedElement.getValue();
					final Entity element = fetchEntityFromGlobalId(sourceRevisionId, objectGuid, candidateTypes);
					if (!element.isValid()) {
						continue;
					}
					final String objectOid = BimserverEntity.class.cast(element).getOid().toString();
					if (objectsToRemove != null && objectsToRemove.contains(objectOid)) {
						indicesToRemove.add(Long.parseLong(objectOid));
					} else {
						indicesToReadd.add(Long.parseLong(objectOid));
					}
				}
				service.removeAllReferences(transactionManager.getId(), relationOid, IFC_RELATED_ELEMENTS);
				System.out.println("remove all reference from relation '" + relationOid + "'");
				for (final Long indexToAdd : indicesToReadd) {
					service.addReference(transactionManager.getId(), relationOid, IFC_RELATED_ELEMENTS,
							indexToAdd.toString());
					System.out.println("add reference '" + indexToAdd + "' to relation '" + relationOid + "'");
				}
			}
			if (innerMap.containsKey("A")) {
				final List<String> objectsToAdd = entry.getValue().get("A");
				for (final String objectToAdd : objectsToAdd) {
					service.addReference(transactionManager.getId(), relationOid, IFC_RELATED_ELEMENTS, objectToAdd);
					System.out.println("add reference '" + objectToAdd + "' to relation '" + relationOid + "'");
				}
			}
		}
	}

	private Entity getReferencedEntity(final String revisionId, final Entity object, final String attributeName) {
		final long objectPlacementOid = ReferenceAttribute.class.cast(object.getAttributeByName(attributeName))
				.getOid();
		final Entity objectPlacement = service.getEntityByOid(revisionId, Long.toString(objectPlacementOid));
		return objectPlacement;
	}

	@Override
	public void updateExportProject(final String projectId, final String exportProjectId, final String shapeProjectId) {
		service.updateExportProject(projectId, exportProjectId, shapeProjectId);
	}

	@Override
	public void checkin(final String projectId, final File file) {
		service.checkin(projectId, file);
	}

	@Override
	public void branchRevisionToExistingProject(final String projectId, final String exportProjectId) {
		service.branchRevisionToExistingProject(projectId, exportProjectId);
	}

	@Override
	public String mergeProjectsIntoNewProject(final String project1, final String project2) {
		return service.mergeProjectsIntoNewProject(project1, project2);
	}

	@Override
	public Long getOidFromGlobalId(final String globalId, final String revisionId, final Iterable<String> candidateTypes) {
		return service.getOidFromGlobalId(globalId, revisionId, candidateTypes);
	}

}
