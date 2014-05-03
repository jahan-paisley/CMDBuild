package org.cmdbuild.bim.mapper.xml;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.mapper.Parser;
import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.CatalogFactory;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.implementation.AttributeDefinitionFactory;
import org.cmdbuild.bim.model.implementation.ImportEntityDefinition;
import org.cmdbuild.bim.model.implementation.ListAttributeDefinition;
import org.cmdbuild.bim.model.implementation.ReferenceAttributeDefinition;
import org.cmdbuild.bim.model.implementation.SimpleAttributeDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class XmlImportCatalogFactory implements CatalogFactory {

	private static class XmlCatalog implements Catalog {

		private final List<EntityDefinition> entities;
		private final List<String> names;

		public XmlCatalog(final List<EntityDefinition> entities, final List<String> names) {
			this.entities = entities;
			this.names = names;
		}

		@Override
		public Iterable<EntityDefinition> getEntitiesDefinitions() {
			return entities;
		}

		@Override
		public String toString() {
			String summary = "";
			for (final EntityDefinition entity : entities) {
				summary = summary + "ENTITY " + entity.getTypeName().toUpperCase() + "\n";
				final Iterable<AttributeDefinition> attributes = entity.getAttributes();
				for (final AttributeDefinition attribute : attributes) {
					summary = summary + attribute.toString() + "\n";
				}
			}
			return summary;
		}

		@Override
		public EntityDefinition getEntityDefinition(final int i) {
			return entities.get(i);
		}

		@Override
		public int getSize() {
			return entities.size();
		}

		@Override
		public boolean contains(final String entityDefintionName) {
			return names.contains(entityDefintionName);
		}

		@Override
		public List<Integer> getPositionsOf(final String entityDefintionName) {
			final int maxsize = getSize();
			final List<Integer> indices = Lists.newArrayList();
			for (int i = 0; i < maxsize; i++) {
				final String name = names.get(i);
				if (name.equals(entityDefintionName)) {
					indices.add(i);
				}
			}
			return indices;
		}
	}

	private static final Logger logger = LoggerSupport.logger;
	private final Parser parser;
	private final List<EntityDefinition> entities;
	private final List<String> names = Lists.newArrayList();

	public XmlImportCatalogFactory(final File xmlFile) {
		parser = new XmlParser(xmlFile);
		entities = Lists.newArrayList();
	}

	private XmlImportCatalogFactory(final String xmlString) {
		parser = new XmlParser(xmlString);
		entities = Lists.newArrayList();
	}

	public static XmlImportCatalogFactory withXmlStringMapper(final String xmlString) {
		return new XmlImportCatalogFactory(xmlString);
	}

	@Override
	public Catalog create() {
		parseEntities();
		return new XmlCatalog(entities, names);
	}

	/**
	 * This method populates the catalog according to the XML file of the the
	 * parser
	 * */
	private void parseEntities() {
		String path = XmlParser.ROOT;
		try {
			final int numberOfTypesToRead = parser.getNumberOfNestedEntities(path);
			logger.info(numberOfTypesToRead + " entries");
			for (int i = 1; i <= numberOfTypesToRead; i++) {
				path = XmlParser.ROOT + "/entity[" + i + "]";
				final String name = parser.getEntityName(XmlParser.ROOT + "/entity[" + i + "]");
				final EntityDefinition entityDefinition = new ImportEntityDefinition(name);
				final String label = parser.getEntityLabel(path);
				logger.info("{} - {}", name, label);
				entityDefinition.setLabel(label);
				readEntity(entityDefinition, path);
				entities.add(entityDefinition);
				names.add(name);
			}
		} catch (final BimError e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * This method sets the attributes of entityDefinition according to the XML
	 * file of the parser. If a nested entity is found the method is called
	 * recursively.
	 * 
	 * @param entityDefinition
	 *            : the entry of the catalog which has to be built
	 * @param path
	 *            : a string which the parser uses in order to read the right
	 *            piece of the XML file
	 * */
	private void readEntity(final EntityDefinition entityDefinition, String path) {
		for (int i = 1; i <= parser.getNumberOfAttributes(path); i++) {
			final String type = parser.getAttributeType(path, i);
			final String label = parser.getAttributeLabel(path, i);
			final String value = parser.getAttributeValue(path, i);
			final String attributeName = parser.getAttributeName(path, i);
			final AttributeDefinitionFactory factory = new AttributeDefinitionFactory(type);
			final AttributeDefinition attributeDefinition = factory.createAttribute(attributeName);
			attributeDefinition.setLabel(label);
			if (!value.equals("")) {
				((SimpleAttributeDefinition) attributeDefinition).setValue(value);
			}
			entityDefinition.getAttributes().add(attributeDefinition);
			if (attributeDefinition instanceof ReferenceAttributeDefinition) {
				final String path_tmp = path;
				path = path + "/attributes/attribute[" + i + "]";
				final int numberOfNestedEntities = parser.getNumberOfNestedEntities(path);
				if (numberOfNestedEntities != 1) {
					throw new BimError("Expected 1 nested entity, found " + numberOfNestedEntities);
				}
				final EntityDefinition referencedEntityDefinition = new ImportEntityDefinition("");
				((ReferenceAttributeDefinition) attributeDefinition).setReference(referencedEntityDefinition);
				path = path + "/entity";
				readEntity(referencedEntityDefinition, path);
				path = path_tmp;
			} else if (attributeDefinition instanceof ListAttributeDefinition) {
				final String path_tmp = path;
				path = path + "/attributes/attribute[" + i + "]";
				final int numberOfNestedEntities = parser.getNumberOfNestedEntities(path);
				if (numberOfNestedEntities == 0) {
				} else if (numberOfNestedEntities > 0) {
					for (int j = 1; j <= numberOfNestedEntities; j++) {
						final String path0 = path;
						path = path + "/entity[" + j + "]";
						final EntityDefinition referencedEntityDefinition = new ImportEntityDefinition("");
						((ListAttributeDefinition) attributeDefinition).setReference(referencedEntityDefinition);
						((ListAttributeDefinition) attributeDefinition).getAllReferences().add(
								referencedEntityDefinition);
						readEntity(referencedEntityDefinition, path);
						path = path0;
					}
				} else {
					throw new BimError("error reading reference list " + attributeName);
				}
				path = path_tmp;
			}
		}
	}

}
