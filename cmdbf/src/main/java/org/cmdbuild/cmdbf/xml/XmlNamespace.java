package org.cmdbuild.cmdbf.xml;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Node;

public interface XmlNamespace {

	public void setRegistry(XmlRegistry registry);

	public XmlRegistry getRegistry();

	public boolean isEnabled();

	public String getSystemId();

	public String getNamespaceURI();

	public String getNamespacePrefix();

	public String getSchemaLocation();

	public XmlSchema getSchema();

	public boolean updateSchema(XmlSchema schema);

	public Iterable<? extends Object> getTypes(Class<?> cls);

	public QName getTypeQName(Object type);

	public Object getType(QName qname);

	public boolean serialize(Node xml, Object object);

	public Object deserialize(Node xml);

	public boolean serializeValue(Node xml, Object object);

	public Object deserializeValue(Node xml, Object type);
}