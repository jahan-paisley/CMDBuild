package org.cmdbuild.cmdbf.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.MetadataType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

public class DocumentNamespace extends AbstractNamespace {

	public static final String DOCUMENT_NAME = "name";
	public static final String DOCUMENT_DESCRIPTION = "description";
	public static final String DOCUMENT_CONTENT = "content";

	private static final Predicate<Lookup> LOOKUP_WITH_DESCRIPTION = new Predicate<Lookup>() {

		@Override
		public boolean apply(final Lookup input) {
			return input.description != null && !input.description.isEmpty();
		}

	};

	private final Function<Lookup, DocumentTypeDefinition> LOOKUP_TO_DOCUMENT_TYPE_DEFINITION = new Function<Lookup, DocumentTypeDefinition>() {

		@Override
		public DocumentTypeDefinition apply(final Lookup input) {
			return new DocumentTypeDefinition() {

				@Override
				public String getName() {
					return input.description;
				}

				@Override
				public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
					return dmsLogic.getCategoryDefinition(input.description).getMetadataGroupDefinitions();
				}
			};
		}

	};

	private final DmsLogic dmsLogic;
	private final LookupLogic lookupLogic;
	private final DmsConfiguration dmsConfiguration;

	public DocumentNamespace(final String name, final DmsLogic dmsLogic, final LookupLogic lookupLogic,
			final CmdbfConfiguration cmdbfConfiguration, final DmsConfiguration dmsConfiguration) {
		super(name, cmdbfConfiguration);
		this.dmsLogic = dmsLogic;
		this.lookupLogic = lookupLogic;
		this.dmsConfiguration = dmsConfiguration;
	}

	@Override
	public boolean isEnabled() {
		return dmsConfiguration.isEnabled();
	}

	@Override
	public XmlSchema getSchema() {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
		XmlSchema schema = null;

		schema = new XmlSchema(getNamespaceURI(), schemaCollection);
		schema.setId(getSystemId());
		schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);

		if (dmsConfiguration.isEnabled()) {
			for (final DocumentTypeDefinition documentTypeDefinition : getTypes(DocumentTypeDefinition.class)) {
				final XmlSchemaType type = getXsd(documentTypeDefinition, schema);
				final XmlSchemaElement element = new XmlSchemaElement(schema, true);
				element.setSchemaTypeName(type.getQName());
				element.setName(type.getName());
			}
		}
		return schema;
	}

	@Override
	public boolean updateSchema(final XmlSchema schema) {
		return false;
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypes(final Class<?> cls) {
		if (DocumentTypeDefinition.class.isAssignableFrom(cls)) {
			final LookupType lookupType = getLookupType(dmsLogic.getCategoryLookupType());
			final Iterable<Lookup> allLookups = lookupLogic.getAllLookup(lookupType, true);
			return FluentIterable.from(allLookups) //
					.filter(LOOKUP_WITH_DESCRIPTION) //
					.transform(LOOKUP_TO_DOCUMENT_TYPE_DEFINITION);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public QName getTypeQName(final Object type) {
		if (type instanceof DocumentTypeDefinition) {
			return new QName(getNamespaceURI(), ((DocumentTypeDefinition) type).getName(), getNamespacePrefix());
		} else {
			return null;
		}
	}

	@Override
	public DocumentTypeDefinition getType(final QName qname) {
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {
			return dmsLogic.getCategoryDefinition(qname.getLocalPart());
		} else {
			return null;
		}
	}

	@Override
	public boolean serialize(final Node xml, final Object entry) {
		boolean serialized = false;
		if (entry instanceof DmsDocument) {
			final DmsDocument document = (DmsDocument) entry;
			final DocumentTypeDefinition documentTypeDefinition = dmsLogic
					.getCategoryDefinition(document.getCategory());
			final QName qName = getTypeQName(documentTypeDefinition);
			final Element xmlElement = xml.getOwnerDocument().createElementNS(qName.getNamespaceURI(),
					getNamespacePrefix() + ":" + qName.getLocalPart());

			final Element nameProperty = xml.getOwnerDocument().createElementNS(getNamespaceURI(),
					getNamespacePrefix() + ":" + DOCUMENT_NAME);
			nameProperty.setTextContent(document.getName());
			xmlElement.appendChild(nameProperty);

			final Element descriptionProperty = xml.getOwnerDocument().createElementNS(getNamespaceURI(),
					getNamespacePrefix() + ":" + DOCUMENT_DESCRIPTION);
			descriptionProperty.setTextContent(document.getDescription());
			xmlElement.appendChild(descriptionProperty);

			try {
				final String content = new String(Base64.encodeBase64(IOUtils.toByteArray(document.getInputStream())),
						"ASCII");
				final Element contentProperty = xml.getOwnerDocument().createElementNS(getNamespaceURI(),
						getNamespacePrefix() + ":" + DOCUMENT_CONTENT);
				contentProperty.setTextContent(content);
				xmlElement.appendChild(contentProperty);
			} catch (final UnsupportedEncodingException e) {
				Log.CMDBUILD.error("DocumentNamespace getXml", e);
			} catch (final IOException e) {
				Log.CMDBUILD.error("DocumentNamespace getXml", e);
			}

			for (final MetadataGroup group : document.getMetadataGroups()) {
				for (final Metadata metadata : group.getMetadata()) {
					final String value = metadata.getValue();
					if (value != null) {
						final Element property = xml.getOwnerDocument().createElementNS(getNamespaceURI(),
								getNamespacePrefix() + ":" + metadata.getName());
						property.setTextContent(metadata.getValue());
						xmlElement.appendChild(property);
					}
				}
			}

			xml.appendChild(xmlElement);
			serialized = true;
		}
		return serialized;
	}

	@Override
	public DmsDocument deserialize(final Node xml) {
		DmsDocument value = null;
		final DocumentTypeDefinition type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
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
			final Map<String, Map<String, String>> autoCompletionRules = dmsLogic.getAutoCompletionRulesByClass(type
					.getName());
			for (final String group : autoCompletionRules.keySet()) {
				properties.putAll(autoCompletionRules.get(group));
			}

			final Map<String, MetadataGroup> metadataGroups = new HashMap<String, MetadataGroup>();
			for (final String key : properties.keySet()) {
				DmsMetadataGroup group = null;
				for (final MetadataGroupDefinition groupDefinition : type.getMetadataGroupDefinitions()) {
					for (final MetadataDefinition metadataDefinition : groupDefinition.getMetadataDefinitions()) {
						if (metadataDefinition.getName().equals(key)) {
							group = (DmsMetadataGroup) metadataGroups.get(groupDefinition.getName());
							if (group == null) {
								group = new DmsMetadataGroup(groupDefinition.getName());
								metadataGroups.put(group.getName(), group);
							}
						}
					}
					if (group != null && !group.containsKey(key)) {
						final DmsMetadata metadata = new DmsMetadata(key, properties.get(key));
						group.put(key, metadata);
					}
				}
			}

			value = new DmsDocument();
			value.setCategory(type.getName());
			value.setName(properties.get(DOCUMENT_NAME));
			value.setDescription(properties.get(DOCUMENT_DESCRIPTION));
			value.setMetadataGroups(metadataGroups.values());
			if (properties.containsKey(DOCUMENT_CONTENT)) {
				final String content = properties.get(DOCUMENT_CONTENT);
				try {
					value.setInputStream(new ByteArrayInputStream(Base64.decodeBase64(content.getBytes("ASCII"))));
				} catch (final UnsupportedEncodingException e) {
					throw new Error(e);
				}
			}
		}
		return value;
	}

	private XmlSchemaType getXsd(final DocumentTypeDefinition documentType, final XmlSchema schema) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(documentType.getName());
		final XmlSchemaSequence sequence = new XmlSchemaSequence();

		final XmlSchemaElement nameElement = new XmlSchemaElement(schema, false);
		nameElement.setName(DOCUMENT_NAME);
		nameElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		sequence.getItems().add(nameElement);

		final XmlSchemaElement descriptionElement = new XmlSchemaElement(schema, false);
		descriptionElement.setName(DOCUMENT_DESCRIPTION);
		descriptionElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		sequence.getItems().add(descriptionElement);

		final XmlSchemaElement contentElement = new XmlSchemaElement(schema, false);
		contentElement.setName(DOCUMENT_CONTENT);
		contentElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_BASE64);
		sequence.getItems().add(contentElement);

		for (final MetadataGroupDefinition metadataGroup : documentType.getMetadataGroupDefinitions()) {
			for (final MetadataDefinition metadata : metadataGroup.getMetadataDefinitions()) {
				sequence.getItems().add(getXsd(metadata, schema));
			}
		}

		type.setParticle(sequence);
		return type;
	}

	private XmlSchemaElement getXsd(final MetadataDefinition metadata, final XmlSchema schema) {
		final XmlSchemaElement element = new XmlSchemaElement(schema, false);
		element.setName(metadata.getName());

		if (metadata.isMandatory()) {
			element.setMinOccurs(1);
		} else {
			element.setMinOccurs(0);
		}
		element.setMaxOccurs(1);

		if (metadata.getType() == MetadataType.BOOLEAN) {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_BOOLEAN);
		} else if (metadata.getType() == MetadataType.INTEGER) {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_INTEGER);
		} else if (metadata.getType() == MetadataType.FLOAT) {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_FLOAT);
		} else if (metadata.getType() == MetadataType.DATE) {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DATE);
		} else if (metadata.getType() == MetadataType.DATETIME) {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DATETIME);
		} else if (metadata.getType() == MetadataType.TEXT) {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		} else if (metadata.getType() == MetadataType.LIST) {
			final XmlSchemaSimpleType type = new XmlSchemaSimpleType(schema, false);
			final XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
			restriction.setBaseTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
			for (final String value : metadata.getListValues()) {
				final XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
				facet.setValue(value);
				restriction.getFacets().add(facet);
			}
			type.setContent(restriction);
			element.setSchemaType(type);
		} else {
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		}
		return element;
	}

	private LookupType getLookupType(final String type) {
		return Iterables.find(lookupLogic.getAllTypes(), new Predicate<LookupType>() {
			@Override
			public boolean apply(final LookupType input) {
				return input.name.equals(type);
			}
		});
	}
}
