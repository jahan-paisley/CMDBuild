package org.cmdbuild.bim.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.joda.time.DateTime;

public interface BimService {

	// File management

	DateTime checkin(String projectId, File file);

	DateTime checkin(String projectId, File file, boolean merge);

	void branchRevisionToNewProject(String revisionId, String projectName);

	void branchRevisionToExistingProject(String revisionId, String destinationProjectId);

	FileOutputStream downloadLastRevisionOfProject(String projectId);

	DataHandler downloadIfc(String revisionId);

	FileOutputStream downloadJson(String revisionId);

	// Project management

	BimProject createProject(String name);

	BimProject createSubProject(String projectName, String parentIdentifier);

	List<BimProject> getAllProjects();

	List<BimRevision> getRevisionsOfProject(BimProject project);

	BimRevision getRevision(String revisionIdentifier);

	List<BimRevision> getLastRevisionOfAllProjects();

	BimProject getProjectByName(String name);

	BimProject getProjectByPoid(String projectId);

	void disableProject(String projectId);

	void enableProject(String projectId);

	// File contents

	Iterable<Entity> getEntitiesByType(String className, String revisionId);

	Entity getEntityByGuid(String revisionId, String globalId, Iterable<String> candidateTypes);

	Entity getEntityByOid(String revisionId, String objectId);

	List<BimRevision> getRevisionsFromGuid(String guid);

	List<String> getAvailableClasses();

	List<String> getAvailableClassesInRevision(String revisionId);

	Entity getReferencedEntity(ReferenceAttribute reference, String revisionId);

	// File editing

	String openTransaction(String projectId);

	String commitTransaction(String id);

	void abortTransaction(String transactionId);

	String createObject(String transactionId, String className);

	void removeObject(String transactionId, String oid);

	void removeAllReferences(String transactionId, String objectId, String attributeName);

	void unsetReference(String transactionId, String objectId, String attributeName);

	void setStringAttribute(String transactionId, String objectId, String attributeName, String value);

	void addStringAttribute(String transactionId, String objectId, String attributeName, String value);

	void setReference(String transactionId, String objectId, String attributeName, String globalIdOid);

	void addReference(String transactionId, String relationId, String attributeName, String objectId);

	void addDoubleAttribute(String transactionId, String locationId, String attributeName, double d);

	void setDoubleAttributes(String transactionId, String locationId, String attributeName, List<Double> values);

	DataHandler fetchProjectStructure(String revisionId);

	Map<String, Long> getGlobalIdOidMap(String revisionId);

	String getGlobalidFromOid(String revisionId, Long oid);

	void updateExportProject(String projectId, String exportProjectId, String shapeProjectId);

	String getLastRevisionOfProject(String projectId);

	String mergeProjectsIntoNewProject(String shapeProjectId, String exportProjectId);

	Long getOidFromGlobalId(String globalId, String revisionId, Iterable<String> candidateTypes);

}
