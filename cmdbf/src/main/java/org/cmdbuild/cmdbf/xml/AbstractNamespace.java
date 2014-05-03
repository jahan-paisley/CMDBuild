package org.cmdbuild.cmdbf.xml;

import java.util.HashMap;
import java.util.Map;

import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaAnnotationItem;
//import org.apache.ws.commons.schema.XmlSchemaAnnotationItem;
import org.apache.ws.commons.schema.XmlSchemaAppInfo;
import org.cmdbuild.cmdbf.CMDBfUtils;
import org.cmdbuild.config.CmdbfConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

abstract public class AbstractNamespace implements XmlNamespace {

	private final String name;
	private XmlRegistry registry;
	private final CmdbfConfiguration configuration;

	@Override
	public void setRegistry(final XmlRegistry registry) {
		this.registry = registry;
	}

	@Override
	public XmlRegistry getRegistry() {
		return registry;
	}

	public AbstractNamespace(final String name, final CmdbfConfiguration configuration) {
		this.name = name;
		this.configuration = configuration;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getSystemId() {
		return name + ".xsd";
	}

	@Override
	public String getNamespaceURI() {
		return configuration.getMdrId() + "/" + name;
	}

	@Override
	public String getNamespacePrefix() {
		return name;
	}

	@Override
	public String getSchemaLocation() {
		return configuration.getSchemaLocation() + "/" + getSystemId();
	}

	@Override
	public boolean serialize(final Node xml, final Object entry) {
		return false;
	}

	@Override
	public Object deserialize(final Node xml) {
		return null;
	}

	@Override
	public boolean serializeValue(final Node xml, final Object entry) {
		return false;
	}

	@Override
	public Object deserializeValue(final Node xml, final Object type) {
		return null;
	}

	protected Map<String, String> getAnnotations(final XmlSchemaAnnotated annotated) {
		final Map<String, String> properties = new HashMap<String, String>();
		final XmlSchemaAnnotation annotation = annotated.getAnnotation();
		if (annotation != null) {
			for (final XmlSchemaAnnotationItem item : annotation.getItems()) {
				if (item instanceof XmlSchemaAppInfo) {
					final XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo) item;
					final NodeList nodeList = appInfo.getMarkup();
					if (nodeList != null) {
						for (int j = 0; j < nodeList.getLength(); j++) {
							final Node node = nodeList.item(j);
							if (node instanceof Element) {
								final Element element = (Element) node;
								if (CMDBfUtils.CMDBUILD_NS.equals(element.getNamespaceURI())) {
									properties.put(element.getLocalName(), element.getTextContent());
								}
							}
						}
					}
				}
			}
		}
		return properties;
	}

	protected void setAnnotations(final XmlSchemaAnnotated annotated, final Map<String, String> properties,
			final Document document) {
		if (!properties.isEmpty()) {
			final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
			final XmlSchemaAppInfo appInfo = new XmlSchemaAppInfo();
			final DocumentFragment appInfoMarkup = document.createDocumentFragment();
			for (final String name : properties.keySet()) {
				final Element element = document.createElementNS(CMDBfUtils.CMDBUILD_NS, name);
				element.setTextContent(properties.get(name));
				appInfoMarkup.appendChild(element);
			}
			appInfo.setMarkup(appInfoMarkup.getChildNodes());
			annotation.getItems().add(appInfo);
			annotated.setAnnotation(annotation);
		}
	}
}
