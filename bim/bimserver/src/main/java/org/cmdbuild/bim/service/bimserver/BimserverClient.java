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

public interface BimserverClient {

	// connection

	void connect();

	void disconnect();

	boolean isConnected();

	// projects

	List<BimProject> getAllProjects();

	BimProject createProjectWithName(final String projectName);

	BimProject createProjectWithNameAndParent(String projectName, String parentIdentifier);

	void disableProject(String projectId);

	void branchRevisionToExistingProject(String revisionId, String destinationProjectId);

	void branchRevisionToNewProject(String revisionId, String projectName);

	DateTime checkin(String projectId, File file, boolean merge);

	DataHandler downloadIfc(String roid);

	DataHandler fetchProjectStructure(String revisionId);

	FileOutputStream downloadLastRevisionOfProject(String projectId);

	// modify data

	void addDoubleAttribute(String transactionId, String objectId, String attributeName, double value);

	void addReference(String transactionId, String objectId, String relationName, String referenceId);

	void addStringAttribute(String transactionId, String objectId, String attributeName, String value);

	// transactions

	void abortTransaction(String transactionId);

	String commitTransaction(String transactionId);

	String createObject(String transactionId, String className);

	void enableProject(String projectId);

	Iterable<Entity> getEntitiesByType(String type, String revisionId);

	Map<String, Long> getGlobalIdOidMap(String revisionId);

	Entity getEntityByGuid(String revisionId, String guid, Iterable<String> candidateTypes);

	Entity getEntityByOid(String revisionId, String objectId);

	BimProject getProjectByName(String name);

	BimProject getProjectByPoid(String projectId);

	Entity getReferencedEntity(ReferenceAttribute reference, String revisionId);

	BimRevision getRevision(String identifier);

	List<BimRevision> getRevisionsOfProject(BimProject project);

	String openTransaction(String projectId);

	void setReference(String transactionId, String objectId, String referenceName, String relatedObjectId);

	void setStringAttribute(String transactionId, String objectId, String attributeName, String value);

	void setDoubleAttributes(String transactionId, String locationId, String attributeName, List<Double> values);

	void unsetReference(String transactionId, String objectId, String referenceName);

	void removeReference(String transactionId, String objectId, String attributeName, int index);

	void removeObject(String transactionId, String oid);

	void removeAllReferences(String transactionId, String objectId, String attributeName);

	void updateExportProject(String projectId, String exportProjectId, String shapeProjectId);

	String getLastRevisionOfProject(String projectId);

	String mergeProjectsIntoNewProject(String shapeProjectId, String shapeProjectId2);

	Long getOidFromGlobalId(String globalId, String revisionId, Iterable<String> candidateTypes);

}
