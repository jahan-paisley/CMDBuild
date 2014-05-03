package org.cmdbuild.bim.service.bimserver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.joda.time.DateTime;

public abstract class ForwardingBimServerClient implements BimserverClient {

	private final BimserverClient delegate;

	protected ForwardingBimServerClient(final BimserverClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public void connect() {
		delegate.connect();
	}

	@Override
	public void disconnect() {
		delegate.disconnect();
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}

	@Override
	public BimProject createProjectWithName(final String projectName) {
		return delegate.createProjectWithName(projectName);
	}

	@Override
	public List<BimProject> getAllProjects() {
		return delegate.getAllProjects();
	}

	@Override
	public BimProject createProjectWithNameAndParent(final String projectName, final String parentIdentifier) {
		return delegate.createProjectWithNameAndParent(projectName, parentIdentifier);
	}

	@Override
	public void disableProject(final String projectId) {
		delegate.disableProject(projectId);
	}

	@Override
	public void branchRevisionToExistingProject(final String revisionId, final String destinationProjectId) {
		delegate.branchRevisionToExistingProject(revisionId, destinationProjectId);
	}

	@Override
	public void branchRevisionToNewProject(final String revisionId, final String projectName) {
		delegate.branchRevisionToNewProject(revisionId, projectName);
	}

	@Override
	public DateTime checkin(final String projectId, final File file, final boolean merge) {
		return delegate.checkin(projectId, file, merge);
	}

	@Override
	public DataHandler downloadIfc(final String roid) {
		return delegate.downloadIfc(roid);
	}

	@Override
	public DataHandler fetchProjectStructure(final String revisionId) {
		return delegate.fetchProjectStructure(revisionId);
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(final String projectId) {
		return delegate.downloadLastRevisionOfProject(projectId);
	}

	@Override
	public void addDoubleAttribute(final String transactionId, final String objectId, final String attributeName,
			final double value) {
		delegate.addDoubleAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void addReference(final String transactionId, final String objectId, final String relationName,
			final String referenceId) {
		delegate.addReference(transactionId, objectId, relationName, referenceId);
	}

	@Override
	public void addStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		delegate.addStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void abortTransaction(final String transactionId) {
		delegate.abortTransaction(transactionId);
	}

	@Override
	public String commitTransaction(final String transactionId) {
		return delegate.commitTransaction(transactionId);
	}

	@Override
	public String createObject(final String transactionId, final String className) {
		return delegate.createObject(transactionId, className);
	}

	@Override
	public void removeObject(final String transactionId, final String objectOid) {
		delegate.removeObject(transactionId, objectOid);
	}

	@Override
	public void enableProject(final String projectId) {
		delegate.enableProject(projectId);
	}

	@Override
	public Iterable<Entity> getEntitiesByType(final String type, final String revisionId) {
		return delegate.getEntitiesByType(type, revisionId);
	}

	@Override
	public Entity getEntityByGuid(final String revisionId, final String guid, final Iterable<String> candidateTypes) {
		return delegate.getEntityByGuid(revisionId, guid, candidateTypes);
	}

	@Override
	public Entity getEntityByOid(final String revisionId, final String objectId) {
		return delegate.getEntityByOid(revisionId, objectId);
	}

	@Override
	public BimProject getProjectByName(final String name) {
		return delegate.getProjectByName(name);
	}

	@Override
	public BimProject getProjectByPoid(final String projectId) {
		return delegate.getProjectByPoid(projectId);
	}

	@Override
	public Entity getReferencedEntity(final ReferenceAttribute reference, final String revisionId) {
		return delegate.getReferencedEntity(reference, revisionId);
	}

	@Override
	public BimRevision getRevision(final String identifier) {
		return delegate.getRevision(identifier);
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(final BimProject project) {
		return delegate.getRevisionsOfProject(project);
	}

	@Override
	public String openTransaction(final String projectId) {
		return delegate.openTransaction(projectId);
	}

	@Override
	public void setReference(final String transactionId, final String objectId, final String referenceName,
			final String relatedObjectId) {
		delegate.setReference(transactionId, objectId, referenceName, relatedObjectId);
	}

	@Override
	public void setStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		delegate.setStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void setDoubleAttributes(final String transactionId, final String objectId, final String attributeName,
			final List<Double> values) {
		delegate.setDoubleAttributes(transactionId, objectId, attributeName, values);
	}

	@Override
	public void removeReference(final String transactionId, final String objectId, final String attributeName,
			final int index) {
		delegate.removeReference(transactionId, objectId, attributeName, index);
	}

	@Override
	public void removeAllReferences(final String transactionId, final String objectId, final String attributeName) {
		delegate.removeAllReferences(transactionId, objectId, attributeName);
	}

	@Override
	public Map<String, Long> getGlobalIdOidMap(final String revisionId) {
		return delegate.getGlobalIdOidMap(revisionId);
	}

	@Override
	public void updateExportProject(final String projectId, final String exportProjectId, final String shapeProjectId) {
		delegate.updateExportProject(projectId, exportProjectId, shapeProjectId);
	}

	@Override
	public String getLastRevisionOfProject(final String projectId) {
		return delegate.getLastRevisionOfProject(projectId);
	}

	@Override
	public String mergeProjectsIntoNewProject(final String project1Id, final String project2Id) {
		return delegate.mergeProjectsIntoNewProject(project1Id, project2Id);
	}

	@Override
	public Long getOidFromGlobalId(final String globalId, final String revisionId, final Iterable<String> candidateTypes) {
		return delegate.getOidFromGlobalId(globalId, revisionId, candidateTypes);
	}

	@Override
	public void unsetReference(final String transactionId, final String objectId, final String referenceName) {
		delegate.unsetReference(transactionId, objectId, referenceName);
	}

}
