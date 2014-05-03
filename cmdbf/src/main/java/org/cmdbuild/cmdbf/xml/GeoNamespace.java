package org.cmdbuild.cmdbf.xml;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.store.DBLayerMetadataStore;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GeoNamespace extends AbstractNamespace {
	private final GISLogic gisLogic;
	private final DBLayerMetadataStore layerMetadataStore;
	public static final String GEO_NAME = "name";
	public static final String GEO_DESCRIPTION = "description";
	public static final String GEO_MAPSTYLE = "mapStyle";
	public static final String GEO_TYPE = "type";
	public static final String GEO_INDEX = "index";
	public static final String GEO_MINIMUMZOOM = "minimumZoom";
	public static final String GEO_MAXIMUMZOOM = "maximumZoom";
	public static final String GEO_VISIBILITY = "visibility";

	public GeoNamespace(final String name, final CMDataView dataView, final GISLogic gisLogic,
			final CmdbfConfiguration cmdbfConfiguration) {
		super(name, cmdbfConfiguration);
		this.gisLogic = gisLogic;
		this.layerMetadataStore = new DBLayerMetadataStore(dataView);
	}

	@Override
	public boolean isEnabled() {
		return gisLogic.isGisEnabled();
	}

	@Override
	public XmlSchema getSchema() {
		try {
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
			final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
			XmlSchema schema = null;
			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);

			for (final GeoClass geoClass : getTypes(GeoClass.class)) {
				final XmlSchemaType type = getXsd(geoClass, document, schema);
				final XmlSchemaElement element = new XmlSchemaElement(schema, true);
				element.setSchemaTypeName(type.getQName());
				element.setName(type.getName());
			}
			return schema;
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	@Override
	public boolean updateSchema(final XmlSchema schema) {
		try {
			boolean updated = false;
			if (getNamespaceURI().equals(schema.getTargetNamespace())) {
				for (final XmlSchemaType type : schema.getSchemaTypes().values()) {
					GeoClassFromXsd(type, schema);
				}
				updated = true;
			}
			return updated;
		} catch (final Exception e) {
			throw new Error(e);
		}
	}

	@Override
	public Iterable<GeoClass> getTypes(final Class<?> cls) {
		if (GeoClass.class.isAssignableFrom(cls)) {
			final Map<String, GeoClass> types = new HashMap<String, GeoClass>();
			try {
				for (final LayerMetadata layer : gisLogic.list()) {
					if (layer.getFullName() != null) {
						final String[] parts = layer.getFullName().split("_");
						if (parts.length > 2) {
							final String typeName = parts[1];
							if (typeName.length() > 0) {
								GeoClass geoClass = types.get(typeName);
								if (geoClass == null) {
									geoClass = new GeoClass(typeName);
									types.put(typeName, geoClass);
								}
								geoClass.put(layer.getName(), layer);
							}
						}
					}
				}
			} catch (final Exception e) {
				throw new Error(e);
			}
			return types.values();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public QName getTypeQName(final Object type) {
		if (type instanceof GeoClass) {
			return new QName(getNamespaceURI(), ((GeoClass) type).getName(), getNamespacePrefix());
		} else {
			return null;
		}
	}

	@Override
	public GeoClass getType(final QName qname) {
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {
			try {
				final GeoClass geoClass = new GeoClass(qname.getLocalPart());
				for (final LayerMetadata layer : layerMetadataStore.list(geoClass.getName())) {
					geoClass.put(layer.getName(), layer);
				}
				return geoClass.isEmpty() ? null : geoClass;
			} catch (final Exception e) {
				throw new Error(e);
			}
		} else {
			return null;
		}
	}

	@Override
	public boolean serialize(final Node xml, final Object entry) {
		boolean serialized = false;
		if (entry instanceof GeoCard) {
			final GeoCard geoCard = (GeoCard) entry;
			final GeoClass type = geoCard.getType();
			final QName qName = getTypeQName(type);
			final Element xmlElement = xml.getOwnerDocument().createElementNS(qName.getNamespaceURI(),
					getNamespacePrefix() + ":" + qName.getLocalPart());
			for (final LayerMetadata layer : type.getLayers()) {
				final Geometry value = geoCard.get(layer.getName());
				final Element property = xml.getOwnerDocument().createElementNS(getNamespaceURI(),
						getNamespacePrefix() + ":" + layer.getName());
				property.setTextContent(value != null ? value.toString() : null);
				xmlElement.appendChild(property);
			}
			xml.appendChild(xmlElement);
			serialized = true;
		}
		return serialized;
	}

	@Override
	public Object deserialize(final Node xml) {
		GeoCard value = null;
		final GeoClass type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if (type != null) {
			final Map<String, String> properties = new HashMap<String, String>();
			for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
				final Node item = xml.getChildNodes().item(i);
				if (item instanceof Element) {
					final Element child = (Element) item;
					String name = child.getLocalName();
					if (name == null) {
						name = child.getTagName();
					}
					properties.put(name, child.getTextContent());
				}
			}
			value = new GeoCard(type);
			for (final LayerMetadata layer : type.getLayers()) {
				final String geometry = properties.get(layer.getName());
				if (geometry != null && !geometry.isEmpty()) {
					try {
						value.set(layer.getName(), PGgeometry.geomFromString(geometry));
					} catch (final SQLException e) {
						throw new Error(e);
					}
				}
			}
		}
		return value;
	}

	private XmlSchemaType getXsd(final GeoClass geoClass, final Document document, final XmlSchema schema) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(getTypeQName(geoClass).getLocalPart());
		final XmlSchemaSequence sequence = new XmlSchemaSequence();
		for (final LayerMetadata layer : geoClass.getLayers()) {
			final XmlSchemaElement element = new XmlSchemaElement(schema, false);
			element.setName(layer.getName());
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
			final Map<String, String> properties = new HashMap<String, String>();
			properties.put(GEO_DESCRIPTION, layer.getDescription());
			properties.put(GEO_MAPSTYLE, layer.getMapStyle());
			properties.put(GEO_TYPE, layer.getType());
			properties.put(GEO_INDEX, Integer.toString(layer.getIndex()));
			properties.put(GEO_MINIMUMZOOM, Integer.toString(layer.getMinimumZoom()));
			properties.put(GEO_MAXIMUMZOOM, Integer.toString(layer.getMaximumzoom()));
			properties.put(GEO_VISIBILITY, layer.getVisibilityAsString());
			setAnnotations(element, properties, document);
			sequence.getItems().add(element);
		}
		type.setParticle(sequence);
		return type;
	}

	private GeoClass GeoClassFromXsd(final XmlSchemaObject schemaObject, final XmlSchema schema) throws Exception {
		XmlSchemaType type = null;
		if (schemaObject instanceof XmlSchemaType) {
			type = (XmlSchemaType) schemaObject;
		} else if (schemaObject instanceof XmlSchemaElement) {
			final XmlSchemaElement element = (XmlSchemaElement) schemaObject;
			type = element.getSchemaType();
			if (type == null) {
				final QName typeName = element.getSchemaTypeName();
				type = schema.getTypeByName(typeName);
			}
		}
		GeoClass geoClass = getType(type.getQName());
		if (type != null) {
			if (type instanceof XmlSchemaComplexType) {
				final XmlSchemaParticle particle = ((XmlSchemaComplexType) type).getParticle();
				if (particle != null && particle instanceof XmlSchemaSequence) {
					if (geoClass == null) {
						geoClass = new GeoClass(type.getName());
					}
					final XmlSchemaSequence sequence = (XmlSchemaSequence) particle;
					for (final XmlSchemaSequenceMember schemaItem : sequence.getItems()) {
						if (schemaItem instanceof XmlSchemaElement) {
							final XmlSchemaElement element = (XmlSchemaElement) schemaItem;
							final Map<String, String> properties = getAnnotations(element);
							LayerMetadata layer = geoClass.get(element.getName());
							if (layer == null) {
								layer = new LayerMetadata();
								layer.setName(element.getName());
								layer.setDescription(properties.get(GEO_DESCRIPTION));
								layer.setMapStyle(properties.get(GEO_MAPSTYLE));
								layer.setType(properties.get(GEO_TYPE));
								if (properties.get(GEO_INDEX) != null) {
									layer.setIndex(Integer.parseInt(properties.get(GEO_INDEX)));
								}
								if (properties.get(GEO_MINIMUMZOOM) != null) {
									layer.setMinimumZoom(Integer.parseInt(properties.get(GEO_MINIMUMZOOM)));
								}
								if (properties.get(GEO_MAXIMUMZOOM) != null) {
									layer.setMaximumzoom(Integer.parseInt(properties.get(GEO_MAXIMUMZOOM)));
								}
								layer.setVisibilityFromString(properties.get(GEO_VISIBILITY));
								layer.setCardBindingFromString(null);
								layer = gisLogic.createGeoAttribute(type.getName(), layer);
							} else {
								layer = gisLogic.modifyGeoAttribute(
										type.getName(),
										layer.getName(),
										properties.get(GEO_DESCRIPTION),
										(properties.get(GEO_MINIMUMZOOM) != null) ? Integer.parseInt(properties
												.get(GEO_MINIMUMZOOM)) : layer.getMinimumZoom(),
										(properties.get(GEO_MAXIMUMZOOM) != null) ? Integer.parseInt(properties
												.get(GEO_MAXIMUMZOOM)) : layer.getMaximumzoom(), properties
												.get(GEO_MAPSTYLE));
							}
							geoClass.put(layer.getName(), layer);
						}
					}
				}
			}
		}
		return geoClass;
	}
}
