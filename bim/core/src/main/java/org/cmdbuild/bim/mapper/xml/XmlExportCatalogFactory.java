package org.cmdbuild.bim.mapper.xml;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.mapper.Parser;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.CatalogFactory;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.implementation.ExportEntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class XmlExportCatalogFactory implements CatalogFactory {

	private static class XmlCatalog implements Catalog {

		/** all the entries of the catalog */
		private final List<EntityDefinition> entities;

		/**
		 * @param entities
		 *            : a set of EntityDefinitions to assign to the field
		 *            entities of the catalog
		 * */
		public XmlCatalog(final List<EntityDefinition> entities) {
			this.entities = entities;
		}

		@Override
		public Iterable<EntityDefinition> getEntitiesDefinitions() {
			return entities;
		}

		@Override
		public EntityDefinition getEntityDefinition(int i) {
			return entities.get(i);
		}

		@Override
		public int getSize() {
			return entities.size();
		}

		@Override
		public boolean contains(String entityDefintionName) {
			// TODO Auto-generated method stub
			throw new BimError("NON IMPLEMENTATO!");
		}

		@Override
		public List<Integer> getPositionsOf(String entityDefintionName) {
			// TODO Auto-generated method stub
			throw new BimError("NON IMPLEMENTATO!");
		}
	}

	private static final Logger logger = LoggerSupport.logger;
	private final Parser parser;
	private final List<EntityDefinition> entities;

	public static XmlExportCatalogFactory withXmlString(final String xmlString) {
		return new XmlExportCatalogFactory(xmlString);
	}

	public XmlExportCatalogFactory(final File xmlFile) {
		parser = new XmlParser(xmlFile);
		entities = Lists.newArrayList();
	}

	private XmlExportCatalogFactory(final String xmlString) {
		parser = new XmlParser(xmlString);
		entities = Lists.newArrayList();
	}

	@Override
	public Catalog create() {
		parseEntities();
		return new XmlCatalog(entities);
	}

	/**
	 * This method populates the catalog according to the XML file of the the
	 * parser
	 * */
	private void parseEntities() {
		String path = XmlParser.ROOT;
		int numberOfTypesToRead = parser.getNumberOfNestedEntities(path);
		logger.info("" + numberOfTypesToRead);
		for (int i = 1; i <= numberOfTypesToRead; i++) {
			path = XmlParser.ROOT + "/entity[" + i + "]";
			String name = parser.getEntityName(path);
			EntityDefinition entityDefinition = new ExportEntityDefinition(name);
			String label = parser.getEntityLabel(path);
			String shape = parser.getEntityShape(path);
			String containerAttribute = parser.getEntityContainerAttribute(path);
			logger.info("Reading class  " + name + " corresponding to " + label + " with shape " + shape);
			entityDefinition.setLabel(label);
			entityDefinition.setShape(shape);
			entityDefinition.setContainerAttribute(containerAttribute);
			entities.add(entityDefinition);
		}
	}

}
