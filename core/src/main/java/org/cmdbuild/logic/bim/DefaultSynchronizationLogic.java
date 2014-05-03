package org.cmdbuild.logic.bim;

import static org.cmdbuild.logic.bim.project.ConversionUtils.TO_MODIFIABLE_PERSISTENCE_PROJECT;

import java.util.List;

import org.cmdbuild.bim.mapper.xml.XmlImportCatalogFactory;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.ConnectorFramework;
import org.cmdbuild.services.bim.connector.export.DefaultExportListener;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.cmdbuild.services.bim.connector.export.Output;

public class DefaultSynchronizationLogic implements SynchronizationLogic {

	private final BimFacade bimServiceFacade;
	private final BimPersistence bimPersistence;
	private final Mapper mapper;
	private final ExportPolicy exportPolicy;
	private final ConnectorFramework connectorFramework;

	public DefaultSynchronizationLogic( //
			final BimFacade bimServiceFacade, //
			final BimPersistence bimPersistence, //
			final Mapper mapper, //
			final ExportPolicy exportStrategy, //
			final ConnectorFramework connectorFramework) {

		this.bimPersistence = bimPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.mapper = mapper;
		this.exportPolicy = exportStrategy;
		this.connectorFramework = connectorFramework;
	}

	@Override
	public void importIfc(final String projectId) {

		final PersistenceProject immutableProject = bimPersistence.read(projectId);
		final String xmlMapping = immutableProject.getImportMapping();
		System.out.println("import mapping: \n" + xmlMapping);
		final Catalog catalog = XmlImportCatalogFactory.withXmlStringMapper(xmlMapping).create();

		for (final EntityDefinition entityDefinition : catalog.getEntitiesDefinitions()) {
			final List<Entity> source = bimServiceFacade.readEntityFromProject(entityDefinition,
					immutableProject.getProjectId());
			if (source.size() > 0) {
				mapper.update(source);
			}
		}

		final PersistenceProject projectSynchronized = TO_MODIFIABLE_PERSISTENCE_PROJECT.apply(immutableProject);
		projectSynchronized.setProjectId(projectId);
		projectSynchronized.setSynch(true);
		bimPersistence.saveProject(projectSynchronized);
	}

	@Override
	public void exportIfc(final String projectId) {
		final Output output = new DefaultExportListener(bimServiceFacade, exportPolicy);
		connectorFramework.executeConnector(projectId, output);
	}

}
