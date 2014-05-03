package org.cmdbuild.bim.service.bimserver;

import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.ws.soap.SOAPFaultException;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.soap.SoapBimServerClientFactory;
import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDownloadResult;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevision;
import org.bimserver.interfaces.objects.SSerializerPluginConfiguration;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.UserException;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.Deserializer;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.Serializer;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration.ChangeListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultBimserverClient implements BimserverClient, ChangeListener {

	private final BimserverConfiguration configuration;
	private BimServerClient client;

	public DefaultBimserverClient(final BimserverConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.addListener(this);
		connect();
	}

	@Override
	public void connect() {
		synchronized (this) {
			try {
				if (!isConnected() && configuration.isEnabled()) {
					final BimServerClientFactory factory = new SoapBimServerClientFactory(configuration.getUrl());
					client = factory.create(new UsernamePasswordAuthenticationInfo(configuration.getUsername(),
							configuration.getPassword()));
					System.out.println("Bimserver connection established");
				}
			} catch (final Throwable t) {
				System.out.println("Bimserver connection failed");
			}
		}
	}

	@Override
	public void disconnect() {
		synchronized (this) {
			client = null;
		}
	}

	@Override
	public boolean isConnected() {
		synchronized (this) {
			boolean success = false;
			try {
				success = client.getBimsie1AuthInterface().isLoggedIn();
			} catch (final Throwable t) {
				// TODO log
			}
			return success;
		}
	}

	@Override
	public void configurationChanged() {
		disconnect();
	}

	@Override
	public List<BimProject> getAllProjects() {
		try {
			final List<SProject> bimserverProjects = client.getServiceInterface().getAllReadableProjects();

			final List<BimProject> projects = new ArrayList<BimProject>();

			for (final SProject bimserverProject : bimserverProjects) {
				final BimProject project = new BimserverProject(bimserverProject);
				projects.add(project);
			}
			return projects;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimProject createProjectWithName(final String projectName) {
		try {
			final BimProject project = new BimserverProject(client.getBimsie1ServiceInterface().addProject(projectName));
			return project;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimProject createProjectWithNameAndParent(final String projectName, final String parentIdentifier) {
		try {
			final Long parentPoid = new Long(parentIdentifier);
			final BimProject project = new BimserverProject(client.getBimsie1ServiceInterface().addProjectAsSubProject(
					projectName, parentPoid));
			return project;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void disableProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			client.getBimsie1ServiceInterface().deleteProject(poid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void branchRevisionToExistingProject(final String revisionId, final String destinationProjectId) {
		try {
			final Long roid = Long.parseLong(revisionId);
			final Long poid = Long.parseLong(destinationProjectId);
			client.getBimsie1ServiceInterface().branchToExistingProject(roid, poid, "", true);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void branchRevisionToNewProject(final String revisionId, final String projectName) {
		try {
			final Long roid = Long.parseLong(revisionId);
			client.getBimsie1ServiceInterface().branchToNewProject(roid, projectName, "", true);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public DateTime checkin(final String projectId, final File file, final boolean merge) {
		try {
			final Long poid = Long.parseLong(projectId);
			final Deserializer deserializer = new BimserverDeserializer(client.getBimsie1ServiceInterface()
					.getSuggestedDeserializerForExtension("ifc"));
			final DataSource dataSource = new FileDataSource(file);
			final DataHandler dataHandler = new DataHandler(dataSource);
			checkin(poid, "", deserializer.getOid(), file.length(), file.getName(), dataHandler, merge, true);
			final String roid = getLastRevisionOfProject(projectId);
			final BimRevision revision = getRevision(roid);
			return new DateTime(revision.getDate());
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public DataHandler downloadIfc(final String roid) {
		try {
			final Serializer serializer = getSerializerByContentType("application/ifc");
			final long downloadId = client.getBimsie1ServiceInterface().download(new Long(roid), serializer.getOid(),
					true, true);
			final SDownloadResult bimserverResult = client.getBimsie1ServiceInterface().getDownloadData(
					new Long(downloadId));
			final DataHandler dataHandler = bimserverResult.getFile();
			return dataHandler;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			if (!getProjectByPoid(projectId).isActive()) {
				throw new BimError("Cannot download disabled projects");
			}
			final Long roid = client.getBimsie1ServiceInterface().getProjectByPoid(poid).getLastRevisionId();
			final Serializer serializer = getSerializerByContentType("application/ifc");
			final long downloadId = client.getBimsie1ServiceInterface().download(new Long(roid), serializer.getOid(),
					true, true);
			final SDownloadResult bimserverResult = client.getBimsie1ServiceInterface().getDownloadData(
					new Long(downloadId));
			final DataHandler dataHandler = bimserverResult.getFile();
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
			outputStream.close();
			return outputStream;
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public BimProject getProjectByName(final String name) {
		BimProject bimProject = null;
		final List<BimProject> projects = new ArrayList<BimProject>();
		List<SProject> bimserverProjects;
		try {
			bimserverProjects = client.getBimsie1ServiceInterface().getProjectsByName(name);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
		if (bimserverProjects == null) {
			return BimProject.NULL_PROJECT;
		}
		for (final SProject bimserverProject : bimserverProjects) {
			final BimProject project = new BimserverProject(bimserverProject);
			projects.add(project);
		}
		if (projects.size() == 0) {
			return BimProject.NULL_PROJECT;
		} else if (projects.size() == 1) {
			bimProject = projects.get(0);
		} else if (projects.size() > 1) {
			throw new BimError("More than one project found with name '" + name + "'");
		} else {
			throw new BimError("No projects found with name '" + name + "'");
		}
		return bimProject;
	}

	@Override
	public BimProject getProjectByPoid(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			final SProject bimserverProject = client.getBimsie1ServiceInterface().getProjectByPoid(poid);
			final BimProject project = new BimserverProject(bimserverProject);
			return project;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String getLastRevisionOfProject(final String identifier) {
		try {
			final Long poid = new Long(identifier);
			final SProject project = client.getBimsie1ServiceInterface().getProjectByPoid(poid);
			return String.valueOf(project.getLastRevisionId());
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimRevision getRevision(final String identifier) {
		final Long roid = new Long(identifier);
		BimRevision revision = BimRevision.NULL_REVISION;
		try {
			if (roid != -1) {
				revision = new BimserverRevision(client.getBimsie1ServiceInterface().getRevision(roid));
			}
			return revision;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(final BimProject project) {
		try {
			final List<BimRevision> revisions = Lists.newArrayList();
			final Long poid = new Long(project.getIdentifier());
			final List<org.bimserver.interfaces.objects.SRevision> srevisions = client.getBimsie1ServiceInterface()
					.getAllRevisionsOfProject(poid);
			if (srevisions != null) {
				for (final SRevision srevision : srevisions) {
					final BimRevision revision = new BimserverRevision(srevision);
					revisions.add(revision);
				}
			}
			return revisions;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void enableProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			client.getBimsie1ServiceInterface().undeleteProject(poid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	private Serializer getSerializerByContentType(final String contentType) {
		try {
			final Serializer serializer = new BimserverSerializer(client.getBimsie1ServiceInterface()
					.getSerializerByContentType(contentType));
			return serializer;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	private void checkin(final Long poid, final String comment, final Long deserializerOid, final Long fileSize,
			final String fileName, final DataHandler ifcFile, final boolean merge, final boolean sync) {
		try {
			client.getServiceInterface().checkin(poid, comment, deserializerOid, fileSize, fileName, ifcFile, merge,
					sync);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public DataHandler fetchProjectStructure(final String revisionId) {
		try {
			final Long roid = Long.parseLong(revisionId);
			final SSerializerPluginConfiguration pluginConfiguration = client.getPluginInterface()
					.getSerializerByPluginClassName("org.bimserver.geometry.jsonshell.SceneJsShellSerializerPlugin");
			final long downloadId = client.getBimsie1ServiceInterface().download(roid, pluginConfiguration.getOid(),
					true, false);
			final SDownloadResult bimserverResult = client.getBimsie1ServiceInterface().getDownloadData(
					new Long(downloadId));
			return bimserverResult.getFile();
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public Iterable<Entity> getEntitiesByType(final String type, final String revisionId) {
		try {
			final Long roid = new Long(revisionId);
			final List<Entity> entities = new ArrayList<Entity>();
			final List<SDataObject> objects = client.getBimsie1LowLevelInterface().getDataObjectsByType(roid, type);
			if (objects != null) {
				for (final SDataObject object : objects) {
					final Entity entity = new BimserverEntity(object);
					entities.add(entity);
				}
			}
			return entities;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public Map<String, Long> getGlobalIdOidMap(final String revisionId) {
		try {
			final Long roid = new Long(revisionId);
			final Map<String, Long> globalIdMap = Maps.newHashMap();
			final List<SDataObject> objects = client.getBimsie1LowLevelInterface().getDataObjects(roid);
			if (objects != null) {
				for (final SDataObject object : objects) {
					if (object.getGuid() != null && !object.getGuid().isEmpty()) {
						globalIdMap.put(object.getGuid(), object.getOid());
					}
				}
			}
			return globalIdMap;
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public Entity getEntityByGuid(final String revisionId, final String guid, final Iterable<String> candidateTypes) {
		Entity entity = Entity.NULL_ENTITY;
		final Long roid = new Long(revisionId);
		try {
			final SDataObject object = client.getBimsie1LowLevelInterface().getDataObjectByGuid(roid, guid);
			entity = new BimserverEntity(object);
		} catch (final UserException e) {
		} catch (final SOAPFaultException e) {
			try {
				if (candidateTypes != null) {
					for (final String type : candidateTypes) {
						System.out.println("Search among type '" + type + "'...");
						final List<SDataObject> objectList = client.getBimsie1LowLevelInterface().getDataObjectsByType(
								roid, type);
						if (objectList != null) {
							for (final SDataObject object : objectList) {
								if (object.getGuid().equals(guid)) {
									entity = new BimserverEntity(object);
									System.out.println("found!");
									return entity;
								}
							}
						}
					}
				}
			} catch (final Throwable t) {
				throw new BimError(t);
			}
		} catch (final Throwable t) {
			throw new BimError(t);
		}
		return entity;
	}

	@Override
	public Entity getEntityByOid(final String revisionId, final String objectId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			final Long oid = new Long(objectId);
			final SDataObject object = client.getBimsie1LowLevelInterface().getDataObjectByOid(roid, oid);
			entity = new BimserverEntity(object);
		} catch (final UserException e) {
			// warning: objectId not found
		} catch (final Throwable e) {
			throw new BimError(e);
		}
		return entity;
	}

	@Override
	public Entity getReferencedEntity(final ReferenceAttribute reference, final String revisionId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			if (reference.getGlobalId() != null) {
				final String guid = reference.getGlobalId();
				entity = new BimserverEntity(client.getBimsie1LowLevelInterface().getDataObjectByGuid(roid, guid));
			} else {
				final Long oid = reference.getOid();
				entity = new BimserverEntity(client.getBimsie1LowLevelInterface().getDataObjectByOid(roid, oid));
			}
			return entity;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void addDoubleAttribute(final String transactionId, final String objectId, final String attributeName,
			final double value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			client.getBimsie1LowLevelInterface().addDoubleAttribute(tid, oid, attributeName, value);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void addReference(final String transactionId, final String objectId, final String relationName,
			final String referenceId) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = Long.parseLong(objectId);
			final Long refid = Long.parseLong(referenceId);
			client.getBimsie1LowLevelInterface().addReference(tid, oid, relationName, refid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void addStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = Long.parseLong(objectId);
			client.getBimsie1LowLevelInterface().addStringAttribute(tid, oid, attributeName, value);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String createObject(final String transactionId, final String className) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = client.getBimsie1LowLevelInterface().createObject(tid, className);
			return oid.toString();
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void removeObject(final String transactionId, final String objectOid) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = Long.parseLong(objectOid);
			client.getBimsie1LowLevelInterface().removeObject(tid, oid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void abortTransaction(final String transactionId) {
		try {
			final Long tid = Long.parseLong(transactionId);
			client.getBimsie1LowLevelInterface().abortTransaction(tid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String commitTransaction(final String transactionId) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long roid = client.getBimsie1LowLevelInterface().commitTransaction(tid, "");
			return roid.toString();
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String openTransaction(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			final Long tid = client.getBimsie1LowLevelInterface().startTransaction(poid);
			return tid.toString();
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void setReference(final String transactionId, final String objectId, final String referenceName,
			final String relatedObjectId) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid1 = new Long(objectId);
			final Long oid2 = new Long(relatedObjectId);
			client.getBimsie1LowLevelInterface().setReference(tid, oid1, referenceName, oid2);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void setStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			client.getBimsie1LowLevelInterface().setStringAttribute(tid, oid, attributeName, value);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void removeReference(final String transactionId, final String objectId, final String attributeName,
			final int index) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			client.getBimsie1LowLevelInterface().removeReference(tid, oid, attributeName, index);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public void removeAllReferences(final String transactionId, final String objectId, final String attributeName) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			client.getBimsie1LowLevelInterface().removeAllReferences(tid, oid, attributeName);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void unsetReference(final String transactionId, final String objectId, final String referenceName) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			client.getBimsie1LowLevelInterface().unsetReference(tid, oid, referenceName);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Deprecated
	@Override
	public void updateExportProject(final String projectId, final String exportProjectId, final String shapeProjectId) {
		final String baseRevision = getLastRevisionOfProject(projectId);
		final String shapeRevision = getLastRevisionOfProject(shapeProjectId);
		if (INVALID_ID.equals(baseRevision) || INVALID_ID.equals(shapeRevision)) {
			return;
		}
		try {
			final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd-HHmmss");
			final String str = fmt.print(new DateTime());
			final String tmpName = String.format("tmp-%s-%s", projectId, str);
			final BimProject tmpProject = createProjectWithName(tmpName);
			System.out.println("tmp project " + tmpProject.getIdentifier() + " for merge created");
			final BimProject shapeProject = createProjectWithNameAndParent("shapes", tmpProject.getIdentifier());
			final BimProject baseProject = createProjectWithNameAndParent("base", tmpProject.getIdentifier());
			branchRevisionToExistingProject(baseRevision, baseProject.getIdentifier());
			branchRevisionToExistingProject(shapeRevision, shapeProject.getIdentifier());
			final String mergedRevisionId = getLastRevisionOfProject(tmpProject.getIdentifier());
			System.out.println("merged revision " + mergedRevisionId + " for export created");
			if (INVALID_ID.equals(mergedRevisionId)) {
				throw new BimError("merged revision for export not created");
			}

			final DataHandler mergedData = downloadIfc(mergedRevisionId);
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file); //
			mergedData.writeTo(outputStream);
			checkin(exportProjectId, file, false);
			file.delete();

			// branchToExistingProject(mergedRevisionId, exportProjectId);

			final String exportRevisionId = getLastRevisionOfProject(exportProjectId);
			System.out.println("Revision " + exportRevisionId + " for export created");
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public String mergeProjectsIntoNewProject(final String project1Id, final String project2Id) {
		final String revision1 = getLastRevisionOfProject(project1Id);
		final String revision2 = getLastRevisionOfProject(project2Id);
		final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd-HHmmss");
		final String str = fmt.print(new DateTime());
		final String tmpName = String.format("tmp-%s", str);
		final BimProject tmpProject = createProjectWithName(tmpName);
		final String mergedProjectId = tmpProject.getIdentifier();
		System.out.println("tmp project " + mergedProjectId + " for merge created");
		final BimProject project1 = createProjectWithNameAndParent("project1", mergedProjectId);
		final BimProject project2 = createProjectWithNameAndParent("project2", mergedProjectId);
		System.out.println("Branching revision " + revision1 + "...");
		branchRevisionToExistingProject(revision1, project1.getIdentifier());
		System.out.println("Done");
		System.out.println("Branching revision " + revision2 + "...");
		branchRevisionToExistingProject(revision2, project2.getIdentifier());
		System.out.println("Done");
		final String mergedRevisionId = getLastRevisionOfProject(mergedProjectId);
		System.out.println("merged revision " + mergedRevisionId + " for export created");
		if (INVALID_ID.equals(mergedRevisionId)) {
			throw new BimError("merged revision for export not created");
		}
		return mergedProjectId;
	}

	@Override
	public Long getOidFromGlobalId(final String globalId, final String revisionId, final Iterable<String> candidateTypes) {
		Long oid = Long.valueOf(INVALID_ID);
		final Entity entityByGuid = getEntityByGuid(revisionId, globalId, candidateTypes);
		if (entityByGuid.isValid()) {
			oid = BimserverEntity.class.cast(entityByGuid).getOid();
		}
		return oid;
	}

	@Override
	public void setDoubleAttributes(final String transactionId, final String objectId, final String attributeName,
			final List<Double> values) {
		try {
			final Long tid = Long.valueOf(transactionId);
			final Long oid = Long.valueOf(objectId);
			client.getBimsie1LowLevelInterface().setDoubleAttributes(tid, oid, attributeName, values);
		} catch (final Throwable t) {
			throw new BimError(t);
		}
	}

}
