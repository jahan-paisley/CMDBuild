package org.cmdbuild.bim.service.bimserver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.joda.time.DateTime;

public class BimserverService implements BimService {

	private final BimserverClient client;

	public BimserverService(final BimserverClient client) {
		this.client = client;
	}

	@Override
	public void abortTransaction(final String transactionId) {
		client.abortTransaction(transactionId);
	}

	@Override
	public void addDoubleAttribute(final String transactionId, final String objectId, final String attributeName,
			final double value) {
		client.addDoubleAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void addReference(final String transactionId, final String objectId, final String relationName,
			final String referenceId) {
		client.addReference(transactionId, objectId, relationName, referenceId);
	}

	@Override
	public void addStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		client.addStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void branchRevisionToExistingProject(final String revisionId, final String destinationProjectId) {
		client.branchRevisionToExistingProject(revisionId, destinationProjectId);
	}

	@Override
	public void branchRevisionToNewProject(final String revisionId, final String projectName) {
		client.branchRevisionToNewProject(revisionId, projectName);
	}

	@Override
	public DateTime checkin(final String projectId, final File file) {
		return checkin(projectId, file, false);
	}

	@Override
	public DateTime checkin(final String projectId, final File file, final boolean merge) {
		return client.checkin(projectId, file, merge);
	}

	@Override
	public String commitTransaction(final String transactionId) {
		return client.commitTransaction(transactionId);
	}

	@Override
	public String createObject(final String transactionId, final String className) {
		return client.createObject(transactionId, className);
	}

	@Override
	public BimProject createProject(final String projectName) {
		return client.createProjectWithName(projectName);
	}

	@Override
	public BimProject createSubProject(final String projectName, final String parentIdentifier) {
		return client.createProjectWithNameAndParent(projectName, parentIdentifier);
	}

	@Override
	public void enableProject(final String projectId) {
		client.enableProject(projectId);
	}

	@Override
	public void disableProject(final String projectId) {
		client.disableProject(projectId);
	}

	@Override
	public DataHandler downloadIfc(final String revisionId) {
		return client.downloadIfc(revisionId);
	}

	@Override
	public DataHandler fetchProjectStructure(final String revisionId) {
		return client.fetchProjectStructure(revisionId);
	}

	@Override
	public FileOutputStream downloadJson(final String roid) {
		throw new BimError("Not implemented");
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(final String projectId) {
		return client.downloadLastRevisionOfProject(projectId);

	}

	@Override
	public List<BimProject> getAllProjects() {
		return client.getAllProjects();
	}

	@Override
	public List<String> getAvailableClasses() {
		throw new BimError("Not implemented");
	}

	@Override
	public List<String> getAvailableClassesInRevision(final String revisionId) {
		throw new BimError("Not implemented");
	}

	@Override
	public Iterable<Entity> getEntitiesByType(final String className, final String revisionId) {
		return client.getEntitiesByType(className, revisionId);
	}

	@Override
	public Map<String, Long> getGlobalIdOidMap(final String revisionId) {
		return client.getGlobalIdOidMap(revisionId);
	}

	@Override
	public Entity getEntityByGuid(final String revisionId, final String guid, final Iterable<String> candidateTypes) {
		return client.getEntityByGuid(revisionId, guid, candidateTypes);
	}

	@Override
	public Entity getEntityByOid(final String revisionId, final String objectId) {
		return client.getEntityByOid(revisionId, objectId);
	}

	@Override
	public List<BimRevision> getLastRevisionOfAllProjects() {
		throw new BimError("Not implemented");
	}

	@Override
	public BimProject getProjectByName(final String name) {
		return client.getProjectByName(name);
	}

	@Override
	public BimProject getProjectByPoid(final String projectId) {
		return client.getProjectByPoid(projectId);
	}

	@Override
	public Entity getReferencedEntity(final ReferenceAttribute reference, final String revisionId) {
		return client.getReferencedEntity(reference, revisionId);
	}

	@Override
	public BimRevision getRevision(final String identifier) {
		return client.getRevision(identifier);
	}

	@Override
	public List<BimRevision> getRevisionsFromGuid(final String guid) {
		throw new BimError("not implemented");
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(final BimProject project) {
		return client.getRevisionsOfProject(project);
	}

	@Override
	public String openTransaction(final String projectId) {
		return client.openTransaction(projectId);
	}

	@Override
	public void removeAllReferences(final String transactionId, final String objectId, final String attributeName) {
		client.removeAllReferences(transactionId, objectId, attributeName);
	}

	@Override
	public void setReference(final String transactionId, final String objectId, final String referenceName,
			final String relatedObjectId) {
		client.setReference(transactionId, objectId, referenceName, relatedObjectId);
	}

	@Override
	public void setStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		client.setStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void unsetReference(final String transactionId, final String objectId, final String referenceName) {
		client.unsetReference(transactionId, objectId, referenceName);
	}

	@Override
	public String getGlobalidFromOid(final String revisionId, final Long oid) {
		return client.getEntityByOid(revisionId, oid.toString()).getKey();
	}

	@Override
	public void removeObject(final String transactionId, final String oid) {
		client.removeObject(transactionId, oid);
	}

	@Override
	public void updateExportProject(final String projectId, final String exportProjectId, final String shapeProjectId) {
		client.updateExportProject(projectId, exportProjectId, shapeProjectId);
	}

	@Override
	public String getLastRevisionOfProject(final String projectId) {
		return client.getLastRevisionOfProject(projectId);
	}

	@Override
	public String mergeProjectsIntoNewProject(final String project1, final String project2) {
		return client.mergeProjectsIntoNewProject(project1, project2);
	}

	@Override
	public Long getOidFromGlobalId(final String globalId, final String revisionId, final Iterable<String> candidateTypes) {
		return client.getOidFromGlobalId(globalId, revisionId, candidateTypes);
	}

	@Override
	public void setDoubleAttributes(final String transactionId, final String locationId, final String attributeName,
			final List<Double> values) {
		client.setDoubleAttributes(transactionId, locationId, attributeName, values);
	}

}