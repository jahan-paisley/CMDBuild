package org.cmdbuild.cmdbf.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
//import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.workflow.CMProcessClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ClassNamespace extends EntryNamespace {

	private static final String CLASS_DESCRIPTION = "description";
	private static final String CLASS_ACTIVE = "active";
	private static final String CLASS_SUPERCLASS = "superclass";
	private static final String CLASS_TYPE = "type";
	private static final String CLASS_PROCESS = "process";
	private static final String CLASS_STOPPABLE = "stoppable";

	public ClassNamespace(final String name, final DataAccessLogic systemdataAccessLogic,
			final DataAccessLogic userDataAccessLogic, final DataDefinitionLogic dataDefinitionLogic,
			final LookupLogic lookupLogic, final CmdbfConfiguration cmdbfConfiguration) {
		super(name, systemdataAccessLogic, userDataAccessLogic, dataDefinitionLogic, lookupLogic, cmdbfConfiguration);
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

			final Set<String> classes = new HashSet<String>();
			for (CMClass cmClass : getTypes(CMClass.class)) {
				while (cmClass != null) {
					if (classes.add(cmClass.getIdentifier().getLocalName())) {
						XmlSchemaType type = schema.getTypeByName(cmClass.getIdentifier().getLocalName());
						if (type == null) {
							type = getXsd(cmClass, document, schema, imports);
							final XmlSchemaElement element = new XmlSchemaElement(schema, true);
							element.setSchemaTypeName(type.getQName());
							element.setName(type.getName());
						}
					}
					cmClass = cmClass.getParent();
				}
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
			for (final XmlSchemaType type : schema.getSchemaTypes().values()) {
				classFromXsd(type, schema);
			}
			updated = true;
		}
		return updated;
	}

	@Override
	public Iterable<? extends CMClass> getTypes(final Class<?> cls) {
		if (CMClass.class.isAssignableFrom(cls)) {
			return Iterables.filter(systemDataAccessLogic.findActiveClasses(), new Predicate<CMClass>() {
				@Override
				public boolean apply(final CMClass input) {
					return !input.isSystem();
				}
			});
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public QName getTypeQName(final Object type) {
		QName qname = null;
		if (type instanceof CMClass) {
			final CMEntryType entryType = (CMEntryType) type;
			qname = new QName(getNamespaceURI(), entryType.getIdentifier().getLocalName(), getNamespacePrefix());
		}
		return qname;
	}

	@Override
	public CMClass getType(final QName qname) {
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {
			return Iterables.tryFind(userDataAccessLogic.findActiveClasses(), new Predicate<CMClass>() {
				@Override
				public boolean apply(final CMClass input) {
					return input.getIdentifier().getLocalName().equals(qname.getLocalPart());
				}
			}).orNull();
		} else {
			return null;
		}
	}

	@Override
	public boolean serialize(final Node xml, final Object entry) {
		boolean serialized = false;
		if (entry instanceof CMCard) {
			final CMCard card = (CMCard) entry;
			serialized = serialize(xml, card.getType(), card.getValues());
		}
		return serialized;
	}

	@Override
	public Card deserialize(final Node xml) {
		Card value = null;
		final CMEntryType type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if (type != null) {
			final Card.CardBuilder builder = Card.newInstance().withClassName(type.getIdentifier().getLocalName());
			builder.withAllAttributes(deserialize(xml, type));
			value = builder.build();
		}
		return value;
	}

	private XmlSchemaType getXsd(final CMClass cmClass, final Document document, final XmlSchema schema,
			final Set<String> imports) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(cmClass.getIdentifier().getLocalName());

		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(CLASS_DESCRIPTION, cmClass.getDescription());
		properties.put(CLASS_ACTIVE, Boolean.toString(cmClass.isActive()));
		properties.put(CLASS_SUPERCLASS, Boolean.toString(cmClass.isSuperclass()));
		if (cmClass instanceof CMProcessClass) {
			properties.put(CLASS_PROCESS, Boolean.toString(true));
			properties.put(CLASS_STOPPABLE, Boolean.toString(((CMProcessClass) cmClass).isUserStoppable()));
		}
		properties.put(CLASS_TYPE, cmClass.getParent() == null ? EntryType.TableType.simpletable.name()
				: EntryType.TableType.standard.name());
		setAnnotations(type, properties, document);

		final XmlSchemaSequence sequence = new XmlSchemaSequence();
		for (final CMAttribute attribute : cmClass.getAttributes()) {
			if (attribute.isActive() && !attribute.isInherited()) {
				sequence.getItems().add(getXsd(attribute, document, schema, imports));
			}
		}
		if (cmClass.getParent() != null) {
			final XmlSchemaComplexContent content = new XmlSchemaComplexContent();
			final XmlSchemaComplexContentExtension extension = new XmlSchemaComplexContentExtension();
			final QName baseTypeName = getRegistry().getTypeQName(cmClass.getParent());
			extension.setBaseTypeName(baseTypeName);
			extension.setParticle(sequence);
			content.setContent(extension);
			type.setContentModel(content);
		} else {
			type.setParticle(sequence);
		}
		return type;
	}

	private CMClass classFromXsd(final XmlSchemaObject schemaObject, final XmlSchema schema) {
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
		CMClass cmClass = null;
		if (type != null) {
			final Map<String, String> properties = getAnnotations(type);
			if (type instanceof XmlSchemaComplexType) {
				String parent = null;
				XmlSchemaParticle particle = ((XmlSchemaComplexType) type).getParticle();
				if (particle == null) {
					final XmlSchemaContentModel contentModel = ((XmlSchemaComplexType) type).getContentModel();
					if (contentModel != null) {
						final XmlSchemaContent content = contentModel.getContent();
						if (content != null && content instanceof XmlSchemaComplexContentExtension) {
							final XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content;
							particle = extension.getParticle();
							final QName baseType = extension.getBaseTypeName();
							if (baseType != null && getNamespaceURI().equals(baseType.getNamespaceURI())) {
								parent = baseType.getLocalPart();
							}
						}
					}
				}
				CMClass parentClass = null;
				if (parent != null) {
					parentClass = userDataAccessLogic.findClass(parent);
					if (parentClass == null) {
						XmlSchemaType parentType = schema.getTypeByName(parent);
						final Iterator<XmlSchemaElement> iterator = schema.getElements().values().iterator();
						while (parentType == null && iterator.hasNext()) {
							final XmlSchemaElement element = iterator.next();
							if (element.getSchemaType().getName().equals(parent)) {
								parentType = element.getSchemaType();
							}
						}
						if (parentType != null) {
							parentClass = classFromXsd(parentType, schema);
						}
					}
				}
				final ClassBuilder classBuilder = EntryType.newClass().withName(type.getName());
				if (parentClass != null) {
					classBuilder.withParent(parentClass.getId());
				}
				if (properties.containsKey(CLASS_DESCRIPTION)) {
					classBuilder.withDescription(properties.get(CLASS_DESCRIPTION));
				}
				if (properties.containsKey(CLASS_SUPERCLASS)) {
					classBuilder.thatIsSuperClass(Boolean.parseBoolean(properties.get(CLASS_SUPERCLASS)));
				}
				if (properties.containsKey(CLASS_PROCESS)) {
					classBuilder.thatIsProcess(Boolean.parseBoolean(properties.get(CLASS_PROCESS)));
				}
				if (properties.containsKey(CLASS_STOPPABLE)) {
					classBuilder.thatIsUserStoppable(Boolean.parseBoolean(properties.get(CLASS_STOPPABLE)));
				}
				if (properties.containsKey(CLASS_TYPE)) {
					classBuilder.withTableType(Enum.valueOf(EntryType.TableType.class, properties.get(CLASS_TYPE)));
				}
				if (properties.containsKey(CLASS_ACTIVE)) {
					classBuilder.thatIsActive(Boolean.parseBoolean(properties.get(CLASS_ACTIVE)));
				} else {
					classBuilder.thatIsActive(true);
				}
				cmClass = dataDefinitionLogic.createOrUpdate(classBuilder.build());
				if (particle != null && particle instanceof XmlSchemaSequence) {
					final XmlSchemaSequence sequence = (XmlSchemaSequence) particle;
					for (final XmlSchemaSequenceMember schemaItem : sequence.getItems()) {
						if (schemaItem instanceof XmlSchemaElement) {
							final XmlSchemaElement element = (XmlSchemaElement) schemaItem;
							addAttributeFromXsd(element, schema, cmClass);
						}
					}
				}
			}
		}
		return cmClass;
	}
}
