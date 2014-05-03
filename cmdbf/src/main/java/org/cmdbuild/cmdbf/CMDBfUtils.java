package org.cmdbuild.cmdbf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dmtf.schemas.cmdbf._1.tns.servicedata.ComparisonOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EqualOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NullOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.PropertyValueType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.SelectedRecordTypeType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.StringOperatorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CMDBfUtils {

	public static final String CMDBUILD_NS = "http://www.cmdbuild.org";

	public static QName getRecordType(final RecordType record) {
		QName type = null;
		if (record.getAny() != null) {
			type = new QName(record.getAny().getNamespaceURI(), record.getAny().getLocalName());
		} else if (record.getPropertySet() != null) {
			type = new QName(record.getPropertySet().getNamespace(), record.getPropertySet().getLocalName());
		}
		return type;
	}

	public static Element getRecordContent(final RecordType record) throws ParserConfigurationException {
		Element xml = null;
		if (record.getAny() != null) {
			xml = record.getAny();
		} else if (record.getPropertySet() != null) {
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			xml = doc.createElementNS(record.getPropertySet().getNamespace(), record.getPropertySet().getLocalName());
			for (final Object property : record.getPropertySet().getAny()) {
				if (property instanceof Element) {
					xml.appendChild((Element) property);
				}
			}
		}
		return xml;
	}

	public static Map<QName, String> parseRecord(final RecordType record) throws ParserConfigurationException {
		final Map<QName, String> properties = new HashMap<QName, String>();
		parseXml(getRecordContent(record), properties);
		return properties;
	}

	private static Map<QName, String> parseXml(final Node node, final Map<QName, String> properties) {
		for (int i = 0; i < node.getAttributes().getLength(); i++) {
			final Node item = node.getAttributes().item(i);
			final QName itemType = (item.getNamespaceURI() == null) ? new QName(item.getNodeName()) : new QName(
					item.getNamespaceURI(), item.getLocalName());
			properties.put(itemType, item.getNodeValue());
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			final Node item = node.getChildNodes().item(i);
			final QName itemType = (item.getNamespaceURI() == null) ? new QName(item.getNodeName()) : new QName(
					item.getNamespaceURI(), item.getLocalName());
			properties.put(itemType, item.getNodeValue());
		}
		return properties;
	}

	static public Map<QName, Set<QName>> parseContentSelector(final ContentSelectorType contentSelector) {
		final Map<QName, Set<QName>> propertyMap = new HashMap<QName, Set<QName>>();
		for (final SelectedRecordTypeType recordSelector : contentSelector.getSelectedRecordType()) {
			QName recordType = null;
			if (recordSelector.getNamespace() != null && recordSelector.getLocalName() != null) {
				recordType = new QName(recordSelector.getNamespace(), recordSelector.getLocalName());
			} else {
				recordType = new QName("");
			}
			Set<QName> propertySet = propertyMap.get(recordType);
			if (propertySet == null) {
				propertySet = new HashSet<QName>();
				propertyMap.put(recordType, propertySet);
			}
			if (!recordSelector.getSelectedProperty().isEmpty()) {
				for (final QNameType property : recordSelector.getSelectedProperty()) {
					propertySet.add(new QName(property.getNamespace(), property.getLocalName()));
				}
			} else {
				propertySet.add(new QName(""));
			}
		}
		return propertyMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public boolean filter(final Map<QName, String> properties, final PropertyValueType propertyValue) {
		final QName propertyType = new QName(propertyValue.getNamespace(), propertyValue.getLocalName());
		final String value = properties.get(propertyType);
		boolean propertyMatch = !propertyValue.isMatchAny();
		if (propertyValue.getEqual() != null) {
			boolean operatorMatch = false;
			for (final EqualOperatorType operator : propertyValue.getEqual()) {
				final Object opValue = operator.getValue();
				if (value != null) {
					if (value instanceof String && opValue instanceof String) {
						final String strValue = operator.isCaseSensitive() ? (String) value : value.toUpperCase();
						final String strOpValue = operator.isCaseSensitive() ? (String) opValue : ((String) opValue)
								.toUpperCase();
						operatorMatch |= strValue.equals(strOpValue);
					} else {
						operatorMatch = value.equals(opValue);
					}
				} else {
					operatorMatch = false;
				}
				if (operator.isNegate()) {
					operatorMatch = !operatorMatch;
				}
			}
			if (propertyValue.isMatchAny()) {
				propertyMatch |= operatorMatch;
			} else {
				propertyMatch &= operatorMatch;
			}
		}
		if (propertyValue.getLess() != null) {
			final ComparisonOperatorType operator = propertyValue.getLess();
			boolean operatorMatch = false;
			final Object opValue = operator.getValue();
			if (value != null && value instanceof Comparable) {
				operatorMatch |= ((Comparable) value).compareTo(opValue) < 0;
			} else {
				operatorMatch = false;
			}
			if (operator.isNegate()) {
				operatorMatch = !operatorMatch;
			}
		}
		if (propertyValue.getLessOrEqual() != null) {
			final ComparisonOperatorType operator = propertyValue.getLessOrEqual();
			boolean operatorMatch = false;
			final Object opValue = operator.getValue();
			if (value != null && value instanceof Comparable) {
				operatorMatch |= ((Comparable) value).compareTo(opValue) <= 0;
			} else {
				operatorMatch = false;
			}
			if (operator.isNegate()) {
				operatorMatch = !operatorMatch;
			}
		}
		if (propertyValue.getGreater() != null) {
			final ComparisonOperatorType operator = propertyValue.getGreater();
			boolean operatorMatch = false;
			final Object opValue = operator.getValue();
			if (value != null && value instanceof Comparable) {
				operatorMatch |= ((Comparable) value).compareTo(opValue) > 0;
			} else {
				operatorMatch = false;
			}
			if (operator.isNegate()) {
				operatorMatch = !operatorMatch;
			}
		}
		if (propertyValue.getGreaterOrEqual() != null) {
			final ComparisonOperatorType operator = propertyValue.getGreater();
			boolean operatorMatch = false;
			final Object opValue = operator.getValue();
			if (value != null && value instanceof Comparable) {
				operatorMatch |= ((Comparable) value).compareTo(opValue) >= 0;
			} else {
				operatorMatch = false;
			}
			if (operator.isNegate()) {
				operatorMatch = !operatorMatch;
			}
		}
		if (propertyValue.getContains() != null) {
			boolean operatorMatch = false;
			for (final StringOperatorType operator : propertyValue.getContains()) {
				final Object opValue = operator.getValue();
				if (value != null && value instanceof String && operator.getValue() instanceof String) {
					final String strValue = operator.isCaseSensitive() ? (String) value : value.toUpperCase();
					final String strOpValue = operator.isCaseSensitive() ? (String) opValue : ((String) opValue)
							.toUpperCase();
					operatorMatch |= strValue.contains(strOpValue);
				} else {
					operatorMatch = false;
				}
				if (operator.isNegate()) {
					operatorMatch = !operatorMatch;
				}
			}
			if (propertyValue.isMatchAny()) {
				propertyMatch |= operatorMatch;
			} else {
				propertyMatch &= operatorMatch;
			}
		}
		if (propertyValue.getLike() != null) {
			boolean operatorMatch = false;
			for (final StringOperatorType operator : propertyValue.getLike()) {
				final Object opValue = operator.getValue();
				if (value != null && operator.getValue() instanceof String) {
					final String strValue = operator.isCaseSensitive() ? (String) value : value.toUpperCase();
					final String strOpValue = operator.isCaseSensitive() ? (String) opValue : ((String) opValue)
							.toUpperCase();
					final String regex = strOpValue.replace('_', '.').replace("%", ".*");
					operatorMatch |= Pattern.matches(regex, strValue);
				} else {
					operatorMatch = false;
				}
				if (operator.isNegate()) {
					operatorMatch = !operatorMatch;
				}
			}
			if (propertyValue.isMatchAny()) {
				propertyMatch |= operatorMatch;
			} else {
				propertyMatch &= operatorMatch;
			}
		}
		if (propertyValue.getIsNull() != null) {
			final NullOperatorType operator = propertyValue.getIsNull();
			boolean operatorMatch = value == null;
			if (operator.isNegate()) {
				operatorMatch = !operatorMatch;
			}
		}
		return propertyMatch;
	}
}
