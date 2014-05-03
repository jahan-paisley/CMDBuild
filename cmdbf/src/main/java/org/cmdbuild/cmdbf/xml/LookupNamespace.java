package org.cmdbuild.cmdbf.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.Lookup.LookupBuilder;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.data.store.lookup.LookupType.LookupTypeBuilder;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class LookupNamespace extends AbstractNamespace {

	private static final String LOOKUP_NAME = "name";
	private static final String LOOKUP_PARENT = "parent";
	private static final String LOOKUP_PARENTNAME = "parentName";
	private static final String LOOKUP_PARENTID = "parentId";
	private static final String LOOKUP_CODE = "code";
	private static final String LOOKUP_NOTES = "notes";
	private static final String LOOKUP_DEFAULT = "default";

	private final LookupLogic lookupLogic;

	public LookupNamespace(final String name, final LookupLogic lookupLogic, final CmdbfConfiguration cmdbfConfiguration) {
		super(name, cmdbfConfiguration);
		this.lookupLogic = lookupLogic;
	}

	@Override
	public QName getTypeQName(final Object type) {
		if (type instanceof LookupType) {
			return new QName(getNamespaceURI(), ((LookupType) type).name.replace(" ", "-"), getNamespacePrefix());
		} else {
			return null;
		}
	}

	@Override
	public LookupType getType(final QName qname) {
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {

			return Iterables.tryFind(getTypes(LookupType.class), new Predicate<LookupType>() {
				@Override
				public boolean apply(final LookupType input) {
					return getTypeQName(input).equals(qname);
				}
			}).orNull();

		} else {
			return null;
		}
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
			final Set<String> imports = new HashSet<String>();

			for (final LookupType lookupType : getTypes(LookupType.class)) {
				getXsd(lookupType, document, schema, imports);
			}

			for (final String namespace : imports) {
				final XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
				schemaImport.setNamespace(namespace);
				schemaImport.setSchemaLocation(getRegistry().getByNamespaceURI(namespace).getSchemaLocation());
			}
			return schema;
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	@Override
	public boolean updateSchema(final XmlSchema schema) {
		boolean updated = false;
		if (getNamespaceURI().equals(schema.getTargetNamespace())) {
			final Map<String, Long> idMap = new HashMap<String, Long>();
			for (final XmlSchemaType type : schema.getSchemaTypes().values()) {
				lookupTypeFromXsd(type, idMap, schema);
			}
			updated = true;
		}
		return updated;
	}

	@Override
	public Iterable<LookupType> getTypes(final Class<?> cls) {
		if (LookupType.class.isAssignableFrom(cls)) {
			return lookupLogic.getAllTypes();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean serializeValue(final Node xml, final Object entry) {
		boolean serialized = false;
		if (entry instanceof LookupValue) {
			final LookupValue value = (LookupValue) entry;
			if (xml instanceof Element) {
				if (value.getId() != null) {
					((Element) xml).setAttribute(SystemNamespace.LOOKUP_ID, value.getId().toString());
				}
				if (value.getLooupType() != null) {
					((Element) xml).setAttribute(SystemNamespace.LOOKUP_TYPE_NAME, value.getLooupType());
				}
			}
			if (value.getDescription() != null) {
				xml.setTextContent(value.getDescription());
			}
			serialized = true;
		}
		return serialized;
	}

	@Override
	public LookupValue deserializeValue(final Node xml, final Object type) {
		LookupValue value = null;
		if (LookupValue.class.equals(type)) {
			Long id = null;
			String lookupType = null;
			if (xml instanceof Element) {
				final Element element = (Element) xml;
				final String idValue = element.getAttribute(SystemNamespace.LOOKUP_ID);
				if (idValue != null) {
					id = Long.parseLong(idValue);
				}
				lookupType = element.getAttribute(SystemNamespace.LOOKUP_TYPE_NAME);
			}
			value = new LookupValue(id, xml.getTextContent(), lookupType);
		}
		return value;
	}

	private XmlSchemaType getXsd(final LookupType lookupType, final Document document, final XmlSchema schema,
			final Set<String> imports) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(getTypeQName(lookupType).getLocalPart());
		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(LOOKUP_NAME, lookupType.name);
		properties.put(LOOKUP_PARENT, lookupType.parent);
		setAnnotations(type, properties, document);
		final XmlSchemaSimpleContent contentModel = new XmlSchemaSimpleContent();
		final XmlSchemaSimpleContentRestriction restriction = new XmlSchemaSimpleContentRestriction();
		final QName baseLookupQName = getRegistry().getTypeQName(LookupValue.class);
		imports.add(baseLookupQName.getNamespaceURI());
		restriction.setBaseTypeName(baseLookupQName);
		for (final Lookup lookup : lookupLogic.getAllLookup(lookupType, true)) {
			if (lookup.description != null && lookup.description.length() > 0) {
				final XmlSchemaFacet facet = new XmlSchemaEnumerationFacet();
				facet.setValue(lookup.description);
				final Map<String, String> lookupProperties = new HashMap<String, String>();
				if (lookup.parent != null) {
					lookupProperties.put(LOOKUP_PARENTNAME, lookup.parent.description);
					lookupProperties.put(LOOKUP_PARENTID, Long.toString(lookup.parent.getId()));
				}
				lookupProperties.put(SystemNamespace.LOOKUP_ID, Long.toString(lookup.getId()));
				lookupProperties.put(LOOKUP_CODE, lookup.code);
				lookupProperties.put(LOOKUP_NOTES, lookup.notes);
				lookupProperties.put(LOOKUP_DEFAULT, Boolean.toString(lookup.isDefault));
				setAnnotations(facet, lookupProperties, document);
				restriction.getFacets().add(facet);
			}
		}
		contentModel.setContent(restriction);
		type.setContentModel(contentModel);
		return type;
	}

	private LookupType lookupTypeFromXsd(final XmlSchemaObject schemaObject, final Map<String, Long> idMap,
			final XmlSchema schema) {
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
		LookupType lookupType = null;
		if (type != null) {
			if (type instanceof XmlSchemaComplexType) {
				final XmlSchemaContentModel contentModel = ((XmlSchemaComplexType) type).getContentModel();
				if (contentModel != null) {
					final XmlSchemaContent content = contentModel.getContent();
					if (content != null && content instanceof XmlSchemaSimpleContentRestriction) {
						final XmlSchemaSimpleContentRestriction restriction = (XmlSchemaSimpleContentRestriction) content;
						if (restriction.getBaseTypeName().equals(
								org.apache.ws.commons.schema.constants.Constants.XSD_STRING)) {
							final Map<String, String> properties = getAnnotations(type);
							final String parent = properties.get(LOOKUP_PARENT);
							String name = properties.get(LOOKUP_NAME);
							if (name == null) {
								name = type.getName();
							}
							final LookupTypeBuilder lookupTypeBuilder = LookupType.newInstance().withName(name);
							LookupType parentLookupType = null;
							if (parent != null && !parent.isEmpty()) {
								lookupTypeBuilder.withParent(parent);
								parentLookupType = getLookupType(parent);
							}
							lookupType = lookupTypeBuilder.build();
							final LookupType oldLookupType = getLookupType(lookupType.name);
							lookupLogic.saveLookupType(lookupType, oldLookupType);
							for (final XmlSchemaFacet facet : restriction.getFacets()) {
								if (facet instanceof XmlSchemaEnumerationFacet) {
									final XmlSchemaEnumerationFacet enumeration = (XmlSchemaEnumerationFacet) facet;
									final String value = (String) enumeration.getValue();
									final Map<String, String> lookupProperties = getAnnotations(enumeration);
									final String parentId = lookupProperties.get(LOOKUP_PARENTID);
									final String parentName = lookupProperties.get(LOOKUP_PARENTNAME);
									final Lookup lookupParent = getLookup(parentLookupType, parentId, parentName, null,
											idMap);
									final String lookupId = lookupProperties.get(SystemNamespace.LOOKUP_ID);
									final Lookup oldLookup = getLookup(lookupType, lookupId, value, lookupParent, idMap);
									final LookupBuilder lookupBuilder = Lookup.newInstance().withType(lookupType);
									if (oldLookup != null) {
										lookupBuilder.withId(oldLookup.getId());
									}
									lookupBuilder.withActiveStatus(true);
									lookupBuilder.withDescription(value).build();
									if (lookupParent != null) {
										lookupBuilder.withParent(lookupParent);
									}
									final String isDefault = lookupProperties.get(LOOKUP_DEFAULT);
									if (isDefault != null) {
										lookupBuilder.withDefaultStatus(Boolean.parseBoolean(isDefault));
									}
									lookupBuilder.withCode(lookupProperties.get(LOOKUP_CODE));
									lookupBuilder.withNotes(lookupProperties.get(LOOKUP_NOTES));
									final Long newId = lookupLogic.createOrUpdateLookup(lookupBuilder.build());
									idMap.put(lookupId, newId);
								}
							}
						}
					}
				}
			}
		}
		return lookupType;
	}

	private LookupType getLookupType(final String name) {
		return Iterables.find(lookupLogic.getAllTypes(), new Predicate<LookupType>() {
			@Override
			public boolean apply(final LookupType input) {
				return input.name.equals(name);
			}
		});
	}

	private Lookup getLookup(final LookupType type, final String id, final String name, final Lookup parent,
			final Map<String, Long> idMap) {
		Lookup lookup = null;
		if (id != null && !id.isEmpty()) {
			Long lookupId = idMap != null ? idMap.get(id) : null;
			if (lookupId == null) {
				lookupId = new Long(id);
			}
			try {
				lookup = lookupLogic.getLookup(lookupId);
			} catch (final NotFoundException e) {
			}
		}
		if (lookup == null && type != null && name != null) {
			lookup = Iterables.find(lookupLogic.getAllLookup(type, false), new Predicate<Lookup>() {
				@Override
				public boolean apply(final Lookup input) {
					return input.description.equals(name) && (parent == null || input.parent.equals(parent));
				}
			});
		}
		return lookup;
	}
}
