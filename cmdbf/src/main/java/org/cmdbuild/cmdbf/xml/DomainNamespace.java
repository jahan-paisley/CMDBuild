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
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DomainNamespace extends EntryNamespace {

	private static final String DOMAIN_DESCRIPTION = "description";
	private static final String DOMAIN_ACTIVE = "active";
	private static final String DOMAIN_DESCRIPTION1 = "description1";
	private static final String DOMAIN_DESCRIPTION2 = "description2";
	private static final String DOMAIN_CLASS1 = "class1";
	private static final String DOMAIN_CLASS2 = "class2";
	private static final String DOMAIN_CARDINALITY = "cardinality";
	private static final String DOMAIN_MASTER_DETAIL = "masterDetail";
	private static final String DOMAIN_MASTER_DETAIL_DESCRIPTION = "masterDetailDescription";

	public DomainNamespace(final String name, final DataAccessLogic systemdataAccessLogic,
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

			final Set<String> imports = new HashSet<String>();
			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
			for (final CMDomain domain : getTypes(CMDomain.class)) {
				final XmlSchemaType type = getXsd(domain, document, schema, imports);
				final XmlSchemaElement element = new XmlSchemaElement(schema, true);
				element.setSchemaTypeName(type.getQName());
				element.setName(type.getName());
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
				domainFromXsd(type, schema);
			}
			updated = true;
		}
		return updated;
	}

	@Override
	public Iterable<? extends CMDomain> getTypes(final Class<?> cls) {
		if (CMDomain.class.isAssignableFrom(cls)) {
			return Iterables.filter(systemDataAccessLogic.findActiveDomains(), new Predicate<CMDomain>() {
				@Override
				public boolean apply(final CMDomain input) {
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
		if (type instanceof CMDomain) {
			final CMEntryType entryType = (CMEntryType) type;
			qname = new QName(getNamespaceURI(), entryType.getIdentifier().getLocalName(), getNamespacePrefix());
		}
		return qname;
	}

	@Override
	public CMDomain getType(final QName qname) {
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {
			return Iterables.tryFind(userDataAccessLogic.findActiveDomains(), new Predicate<CMDomain>() {
				@Override
				public boolean apply(final CMDomain input) {
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
		if (entry instanceof CMRelation) {
			final CMRelation relation = (CMRelation) entry;
			serialized = serialize(xml, relation.getType(), relation.getValues());
		}
		return serialized;
	}

	@Override
	public RelationDTO deserialize(final Node xml) {
		RelationDTO value = null;
		final CMDomain type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if (type != null) {
			value = new RelationDTO();
			value.domainName = type.getIdentifier().getLocalName();
			value.relationAttributeToValue = deserialize(xml, type);
			value.master = Source._1.name();			
		}
		return value;
	}

	private XmlSchemaType getXsd(final CMDomain domain, final Document document, final XmlSchema schema,
			final Set<String> imports) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(domain.getIdentifier().getLocalName());

		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(DOMAIN_DESCRIPTION, domain.getDescription());
		properties.put(DOMAIN_ACTIVE, Boolean.toString(domain.isActive()));
		properties.put(DOMAIN_CLASS1, domain.getClass1().getIdentifier().getLocalName());
		properties.put(DOMAIN_CLASS2, domain.getClass2().getIdentifier().getLocalName());
		properties.put(DOMAIN_DESCRIPTION1, domain.getDescription1());
		properties.put(DOMAIN_DESCRIPTION2, domain.getDescription2());
		properties.put(DOMAIN_CARDINALITY, domain.getCardinality());
		properties.put(DOMAIN_MASTER_DETAIL, Boolean.toString(domain.isMasterDetail()));
		properties.put(DOMAIN_MASTER_DETAIL_DESCRIPTION, domain.getMasterDetailDescription());
		setAnnotations(type, properties, document);

		final XmlSchemaSequence sequence = new XmlSchemaSequence();
		for (final CMAttribute attribute : domain.getAttributes()) {
			if (attribute.isActive() && !attribute.isInherited()) {
				sequence.getItems().add(getXsd(attribute, document, schema, imports));
			}
		}
		type.setParticle(sequence);
		return type;
	}

	private CMDomain domainFromXsd(final XmlSchemaObject schemaObject, final XmlSchema schema) {
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
		CMDomain domain = null;
		if (type != null) {
			final Map<String, String> properties = getAnnotations(type);
			if (type instanceof XmlSchemaComplexType) {
				final XmlSchemaParticle particle = ((XmlSchemaComplexType) type).getParticle();

				final DomainBuilder domainBuilder = Domain.newDomain().withName(type.getName());
				if (properties.containsKey(DOMAIN_CLASS1)) {
					domainBuilder.withIdClass1(userDataAccessLogic.findClass(properties.get(DOMAIN_CLASS1)).getId());
				}
				if (properties.containsKey(DOMAIN_CLASS2)) {
					domainBuilder.withIdClass2(userDataAccessLogic.findClass(properties.get(DOMAIN_CLASS2)).getId());
				}
				if (properties.containsKey(DOMAIN_DESCRIPTION)) {
					domainBuilder.withDescription(properties.get(DOMAIN_DESCRIPTION));
				}
				if (properties.containsKey(DOMAIN_DESCRIPTION1)) {
					domainBuilder.withDirectDescription(properties.get(DOMAIN_DESCRIPTION1));
				}
				if (properties.containsKey(DOMAIN_DESCRIPTION2)) {
					domainBuilder.withInverseDescription(properties.get(DOMAIN_DESCRIPTION2));
				}
				if (properties.containsKey(DOMAIN_CARDINALITY)) {
					domainBuilder.withCardinality(properties.get(DOMAIN_CARDINALITY));
				}
				if (properties.containsKey(DOMAIN_MASTER_DETAIL_DESCRIPTION)) {
					domainBuilder.withMasterDetailDescription(properties.get(DOMAIN_MASTER_DETAIL_DESCRIPTION));
				}
				if (properties.containsKey(DOMAIN_ACTIVE)) {
					domainBuilder.thatIsActive(Boolean.parseBoolean(properties.get(DOMAIN_ACTIVE)));
				} else {
					domainBuilder.thatIsActive(true);
				}
				if (properties.containsKey(DOMAIN_MASTER_DETAIL)) {
					domainBuilder.thatIsMasterDetail(Boolean.parseBoolean(properties.get(DOMAIN_MASTER_DETAIL)));
				}
				domain = dataDefinitionLogic.createOrUpdate(domainBuilder.build());

				if (particle != null && particle instanceof XmlSchemaSequence) {
					final XmlSchemaSequence sequence = (XmlSchemaSequence) particle;
					for (final XmlSchemaSequenceMember schemaItem : sequence.getItems()) {
						if (schemaItem instanceof XmlSchemaElement) {
							final XmlSchemaElement element = (XmlSchemaElement) schemaItem;
							addAttributeFromXsd(element, schema, domain);
						}
					}
				}
			}
		}
		return domain;
	}
}
