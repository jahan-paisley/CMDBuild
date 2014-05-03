package org.cmdbuild.cmdbf.xml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

abstract public class EntryNamespace extends AbstractNamespace {

	private static final String ATTRIBUTE_DESCRIPTION = "description";
	private static final String ATTRIBUTE_ACTIVE = "active";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_DEFAULT = "default";
	private static final String ATTRIBUTE_DISPLAYABLE = "displayable";
	private static final String ATTRIBUTE_MANDATORY = "mandatory";
	private static final String ATTRIBUTE_UNIQUE = "unique";
	private static final String ATTRIBUTE_MODE = "mode";
	private static final String ATTRIBUTE_INDEX = "index";
	private static final String ATTRIBUTE_GROUP = "group";
	private static final String ATTRIBUTE_ORDER = "order";
	private static final String ATTRIBUTE_EDITOR = "editor";
	private static final String ATTRIBUTE_LENGTH = "lenght";
	private static final String ATTRIBUTE_PRECISION = "precision";
	private static final String ATTRIBUTE_SCALE = "scale";
	private static final String ATTRIBUTE_LOOKUP = "lookup";
	private static final String ATTRIBUTE_DOMAIN = "domain";
	private static final String VALUE = "value";

	protected final DataAccessLogic systemDataAccessLogic;
	protected final DataAccessLogic userDataAccessLogic;
	protected final DataDefinitionLogic dataDefinitionLogic;
	protected final LookupLogic lookupLogic;

	public EntryNamespace(final String name, final DataAccessLogic systemDataAccessLogic,
			final DataAccessLogic userDataAccessLogic, final DataDefinitionLogic dataDefinitionLogic,
			final LookupLogic lookupLogic, final CmdbfConfiguration cmdbfConfiguration) {
		super(name, cmdbfConfiguration);
		this.systemDataAccessLogic = systemDataAccessLogic;
		this.userDataAccessLogic = userDataAccessLogic;
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.lookupLogic = lookupLogic;
	}

	protected XmlSchemaElement getXsd(final CMAttribute attribute, final Document document, final XmlSchema schema,
			final Set<String> imports) {
		final XmlSchemaElement element = new XmlSchemaElement(schema, false);
		element.setName(attribute.getName());

		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(ATTRIBUTE_DESCRIPTION, attribute.getDescription());
		properties.put(ATTRIBUTE_DEFAULT, attribute.getDefaultValue());
		properties.put(ATTRIBUTE_DISPLAYABLE, Boolean.toString(attribute.isDisplayableInList()));
		properties.put(ATTRIBUTE_MANDATORY, Boolean.toString(attribute.isMandatory()));
		properties.put(ATTRIBUTE_UNIQUE, Boolean.toString(attribute.isUnique()));
		properties.put(ATTRIBUTE_ACTIVE, Boolean.toString(attribute.isActive()));
		properties.put(ATTRIBUTE_MODE, attribute.getMode().name());
		properties.put(ATTRIBUTE_INDEX, Integer.toString(attribute.getIndex()));
		properties.put(ATTRIBUTE_GROUP, attribute.getGroup());
		properties.put(ATTRIBUTE_ORDER, Integer.toString(attribute.getClassOrder()));
		properties.put(ATTRIBUTE_EDITOR, attribute.getEditorType());

		if (attribute.isMandatory()) {
			element.setMinOccurs(1);
		} else {
			element.setMinOccurs(0);
		}
		element.setMaxOccurs(1);
		if (attribute.getDefaultValue() != null) {
			element.setDefaultValue(attribute.getDefaultValue());
		}

		attribute.getType().accept(new CMAttributeTypeVisitor() {

			@Override
			public void visit(final TimeAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_TIME);
				properties.put(ATTRIBUTE_TYPE, "TIME");
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				properties.put(ATTRIBUTE_TYPE, "TEXT");
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				properties.put(ATTRIBUTE_TYPE, "STRING");
				properties.put(ATTRIBUTE_LENGTH, Integer.toString(attributeType.length));
				final XmlSchemaSimpleType type = new XmlSchemaSimpleType(schema, false);
				final XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
				restriction.setBaseTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				final XmlSchemaLengthFacet facet = new XmlSchemaLengthFacet();
				facet.setValue(attributeType.length);
				restriction.getFacets().add(facet);
				type.setContent(restriction);
				element.setSchemaType(type);
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final QName qname = getRegistry().getTypeQName(IdAndDescription.class);
				imports.add(qname.getNamespaceURI());
				element.setSchemaTypeName(qname);
				properties.put(ATTRIBUTE_TYPE, "REFERENCE");
				properties.put(ATTRIBUTE_DOMAIN, attributeType.getIdentifier().getLocalName());
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				final LookupType lookupType = LookupType.newInstance().withName(attributeType.getLookupTypeName())
						.build();
				final QName lookupQName = getRegistry().getTypeQName(lookupType);
				imports.add(lookupQName.getNamespaceURI());
				element.setSchemaTypeName(lookupQName);
				properties.put(ATTRIBUTE_TYPE, "LOOKUP");
				properties.put(ATTRIBUTE_LOOKUP, attributeType.getLookupTypeName());
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				properties.put(ATTRIBUTE_TYPE, "INET");
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_INTEGER);
				properties.put(ATTRIBUTE_TYPE, "INTEGER");
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				properties.put(ATTRIBUTE_TYPE, "FOREIGNKEY");
				properties.put(ATTRIBUTE_DOMAIN, attributeType.getIdentifier().getLocalName());
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DOUBLE);
				properties.put(ATTRIBUTE_TYPE, "DOUBLE");
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DECIMAL);
				properties.put(ATTRIBUTE_TYPE, "DECIMAL");
				properties.put(ATTRIBUTE_PRECISION, Integer.toString(attributeType.precision));
				properties.put(ATTRIBUTE_SCALE, Integer.toString(attributeType.scale));
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DATE);
				properties.put(ATTRIBUTE_TYPE, "DATE");
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DATETIME);
				properties.put(ATTRIBUTE_TYPE, "TIMESTAMP");
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				// TODO set type
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				properties.put(ATTRIBUTE_TYPE, "UNDEFINED");
			}

			@Override
			public void visit(final BooleanAttributeType attributeType) {
				element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_BOOLEAN);
				properties.put(ATTRIBUTE_TYPE, "BOOLEAN");
			}

			@Override
			public void visit(final StringArrayAttributeType stringArrayAttributeType) {
				final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, false);
				final XmlSchemaElement valueElement = new XmlSchemaElement(schema, false);
				valueElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				valueElement.setName(VALUE);
				valueElement.setMaxOccurs(-1);
				type.setParticle(valueElement);
				element.setSchemaType(type);
				// TODO set type
				properties.put(ATTRIBUTE_TYPE, "UNDEFINED");
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
				properties.put(ATTRIBUTE_TYPE, "CHAR");
				final XmlSchemaSimpleType type = new XmlSchemaSimpleType(schema, false);
				final XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
				restriction.setBaseTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
				final XmlSchemaLengthFacet facet = new XmlSchemaLengthFacet();
				facet.setValue(1);
				restriction.getFacets().add(facet);
				type.setContent(restriction);
				element.setSchemaType(type);
			}
		});
		setAnnotations(element, properties, document);
		return element;
	}

	protected void addAttributeFromXsd(final XmlSchemaElement element, final XmlSchema schema, final CMEntryType type) {
		final Map<String, String> properties = getAnnotations(element);
		final AttributeBuilder attributeBuilder = Attribute.newAttribute().withName(element.getName());
		attributeBuilder.withOwnerName(type.getIdentifier().getLocalName());
		if (properties.containsKey(ATTRIBUTE_TYPE)) {
			attributeBuilder.withType(properties.get(ATTRIBUTE_TYPE));
		}
		if (properties.containsKey(ATTRIBUTE_DESCRIPTION)) {
			attributeBuilder.withDescription(properties.get(ATTRIBUTE_DESCRIPTION));
		}
		if (properties.containsKey(ATTRIBUTE_MODE)) {
			attributeBuilder.withMode(Enum.valueOf(CMAttribute.Mode.class, properties.get(ATTRIBUTE_MODE)));
		}
		if (properties.containsKey(ATTRIBUTE_ORDER)) {
			attributeBuilder.withClassOrder(Integer.parseInt(properties.get(ATTRIBUTE_ORDER)));
		}
		if (properties.containsKey(ATTRIBUTE_DEFAULT)) {
			attributeBuilder.withDefaultValue(properties.get(ATTRIBUTE_DEFAULT));
		}
		if (properties.containsKey(ATTRIBUTE_EDITOR)) {
			attributeBuilder.withEditorType(properties.get(ATTRIBUTE_EDITOR));
		}
		if (properties.containsKey(ATTRIBUTE_GROUP)) {
			attributeBuilder.withGroup(properties.get(ATTRIBUTE_GROUP));
		}
		if (properties.containsKey(ATTRIBUTE_INDEX)) {
			attributeBuilder.withIndex(Integer.parseInt(properties.get(ATTRIBUTE_INDEX)));
		}
		if (properties.containsKey(ATTRIBUTE_DOMAIN)) {
			attributeBuilder.withDomain(properties.get(ATTRIBUTE_DOMAIN));
		}
		if (properties.containsKey(ATTRIBUTE_LOOKUP)) {
			attributeBuilder.withLookupType(properties.get(ATTRIBUTE_LOOKUP));
		}
		if (properties.containsKey(ATTRIBUTE_LENGTH)) {
			attributeBuilder.withLength(Integer.parseInt(properties.get(ATTRIBUTE_LENGTH)));
		}
		if (properties.containsKey(ATTRIBUTE_PRECISION)) {
			attributeBuilder.withPrecision(Integer.parseInt(properties.get(ATTRIBUTE_PRECISION)));
		}
		if (properties.containsKey(ATTRIBUTE_SCALE)) {
			attributeBuilder.withScale(Integer.parseInt(properties.get(ATTRIBUTE_SCALE)));
		}
		if (properties.containsKey(ATTRIBUTE_ACTIVE)) {
			attributeBuilder.thatIsActive(Boolean.parseBoolean(properties.get(ATTRIBUTE_ACTIVE)));
		}
		if (properties.containsKey(ATTRIBUTE_DISPLAYABLE)) {
			attributeBuilder.thatIsDisplayableInList(Boolean.parseBoolean(properties.get(ATTRIBUTE_DISPLAYABLE)));
		}
		if (properties.containsKey(ATTRIBUTE_UNIQUE)) {
			attributeBuilder.thatIsUnique(Boolean.parseBoolean(properties.get(ATTRIBUTE_UNIQUE)));
		}
		if (properties.containsKey(ATTRIBUTE_MANDATORY)) {
			attributeBuilder.thatIsMandatory(Boolean.parseBoolean(properties.get(ATTRIBUTE_MANDATORY)));
		}
		dataDefinitionLogic.createOrUpdate(attributeBuilder.build());
	}

	protected boolean serialize(final Node xml, final CMEntryType type, final Iterable<Entry<String, Object>> attributes) {
		boolean serialized = false;
		final QName qName = getTypeQName(type);
		if (qName != null) {
			final Element xmlElement = xml.getOwnerDocument().createElementNS(qName.getNamespaceURI(),
					qName.getPrefix() + ":" + qName.getLocalPart());
			for (final Entry<String, Object> attribute : attributes) {
				final Object value = attribute.getValue();
				if (value != null) {
					final CMAttribute cmAttribute = type.getAttribute(attribute.getKey());
					if (cmAttribute != null && cmAttribute.isActive()) {
						final Element property = xml.getOwnerDocument().createElementNS(qName.getNamespaceURI(),
								qName.getPrefix() + ":" + attribute.getKey());
						final AttributeSerializer serializer = new AttributeSerializer(property, value);
						cmAttribute.getType().accept(serializer);
						xmlElement.appendChild(property);
					}
				}
			}
			xml.appendChild(xmlElement);
			serialized = true;
		}
		return serialized;
	}

	protected Map<String, Object> deserialize(final Node xml, final CMEntryType type) {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
			final Node item = xml.getChildNodes().item(i);
			if (item instanceof Element) {
				final Element child = (Element) item;
				String name = child.getLocalName();
				if (name == null) {
					name = child.getTagName();
				}
				final CMAttribute attribute = type.getAttribute(name);
				if (attribute != null && attribute.isActive()) {
					final AttributeDeserializer deserializer = new AttributeDeserializer(child);
					attribute.getType().accept(deserializer);
					attributes.put(name, deserializer.getValue());
				}
			}
		}
		return attributes;
	}

	private class AttributeSerializer implements CMAttributeTypeVisitor {
		private DatatypeFactory datatypeFactory;
		private Object value = null;
		private Element xml = null;

		public AttributeSerializer(final Element xml, final Object value) {
			try {
				this.xml = xml;
				this.value = value;
				this.datatypeFactory = DatatypeFactory.newInstance();
			} catch (final DatatypeConfigurationException e) {
				throw new Error(e);
			}
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(toXml((DateTime) value));
			}
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent((String) value);
			}
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent((String) value);
			}
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			if (value != null) {
				getRegistry().serializeValue(xml, value);
			}
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			if (value != null) {
				getRegistry().serializeValue(xml, value);
			}
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent((String) value);
			}
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(((Integer) value).toString());
			}
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(((Long) value).toString());
			}
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(((Double) value).toString());
			}
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(((BigDecimal) value).toString());
			}
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(toXml((DateTime) value));
			}
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(toXml((DateTime) value));
			}
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			if (value != null) {
				final CMEntryType type = userDataAccessLogic.findClass((Long) value);
				xml.setTextContent(type.getIdentifier().getLocalName());
			}
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			if (value != null) {
				xml.setTextContent(((Boolean) value).toString());
			}
		}

		@Override
		public void visit(final StringArrayAttributeType stringArrayAttributeType) {
			if (value != null) {
				for (final String item : (String[]) value) {
					final Element element = xml.getOwnerDocument().createElementNS(getNamespaceURI(),
							getNamespacePrefix() + ":" + VALUE);
					element.setTextContent(item);
					xml.appendChild(element);
				}
			}
		}

		@Override
		public void visit(final CharAttributeType charAttributeType) {
			if (value != null) {
				xml.setTextContent((String) value);
			}
		}

		private String toXml(final DateTime value) {
			final XMLGregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(value.toGregorianCalendar());
			return calendar.toXMLFormat();
		}
	}

	private class AttributeDeserializer implements CMAttributeTypeVisitor {

		private DatatypeFactory datatypeFactory;
		private Object value = null;
		private Element xml = null;

		public AttributeDeserializer(final Element xml) {
			try {
				this.xml = xml;
				this.datatypeFactory = DatatypeFactory.newInstance();
			} catch (final DatatypeConfigurationException e) {
				throw new Error(e);
			}
		}

		public Object getValue() {
			return value;
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = parseDateTime(text);
			}
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			value = xml.getTextContent();
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			value = xml.getTextContent();
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			value = getRegistry().deserializeValue(xml, IdAndDescription.class);
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			value = getRegistry().deserializeValue(xml, LookupValue.class);
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			value = xml.getTextContent();
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = new Integer(text);
			}
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = new Long(text);
			}
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = new Double(text);
			}
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = new BigDecimal(text);
			}
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = parseDateTime(text);
			}

		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = parseDateTime(text);
			}
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				final CMEntryType type = userDataAccessLogic.findClass(text);
				if (type != null) {
					value = type.getId();
				}
			}
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			final String text = xml.getTextContent();
			if (text != null && !text.isEmpty()) {
				value = Boolean.parseBoolean(text);
			}
		}

		@Override
		public void visit(final StringArrayAttributeType stringArrayAttributeType) {
			final List<String> values = new ArrayList<String>();
			for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
				final Node child = xml.getChildNodes().item(i);
				if (child instanceof Element) {
					final Element item = (Element) child;
					if (item.getNamespaceURI().equals(getNamespaceURI()) && item.getLocalName().equals(VALUE)) {
						values.add(item.getTextContent());
					}
				}
			}
			value = values.toArray(new String[0]);
		}

		@Override
		public void visit(final CharAttributeType charAttributeType) {
			value = xml;

		}

		private DateTime parseDateTime(final String xmlValue) {
			final XMLGregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(xmlValue);
			return new DateTime(calendar.toGregorianCalendar());
		}
	}
}
