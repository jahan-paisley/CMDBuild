package org.cmdbuild.cmdbf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.PropertySetType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;
import org.w3c.dom.Node;

import com.google.common.base.Function;

public class ContentSelectorFunction implements Function<RecordType, RecordType> {

	private Map<QName, Set<QName>> propertyMap;

	public ContentSelectorFunction(final ContentSelectorType contentSelector) {
		if (contentSelector != null) {
			propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
		} else {
			propertyMap = null;
		}
	}

	@Override
	public RecordType apply(final RecordType input) {
		if (propertyMap != null) {
			final QName recordType = CMDBfUtils.getRecordType(input);
			Set<QName> properties = null;
			properties = propertyMap.get(recordType);
			if (propertyMap.containsKey(new QName(""))) {
				if (properties == null) {
					properties = new HashSet<QName>();
				}
				for (final QName property : propertyMap.get(new QName(""))) {
					properties.add(property);
				}
			}
			if (properties != null) {
				if (properties != null && !properties.contains(new QName(""))) {
					final List<Node> nodes = new ArrayList<Node>();
					if (input.getAny() != null) {
						for (int i = 0; i < input.getAny().getChildNodes().getLength(); i++) {
							final Node node = input.getAny().getChildNodes().item(i);
							if (filter(properties, node)) {
								nodes.add(node);
							}
						}
						final PropertySetType propertySet = new PropertySetType();
						propertySet.setNamespace(input.getAny().getNamespaceURI());
						propertySet.setLocalName(input.getAny().getLocalName());
						propertySet.getAny().addAll(nodes);
						input.setPropertySet(propertySet);
						input.setAny(null);
					} else if (input.getPropertySet() != null) {
						for (final Object property : input.getPropertySet().getAny()) {
							if (property instanceof Node && filter(properties, (Node) property)) {
								nodes.add((Node) property);
							}
						}
						input.getPropertySet().getAny().retainAll(nodes);
					}
				}
			}
		}
		return input;
	}

	private boolean filter(final Set<QName> properties, final Node node) {
		final QName itemType = (node.getNamespaceURI() == null) ? new QName(node.getNodeName()) : new QName(
				node.getNamespaceURI(), node.getLocalName());
		return properties.contains(itemType);
	}
}