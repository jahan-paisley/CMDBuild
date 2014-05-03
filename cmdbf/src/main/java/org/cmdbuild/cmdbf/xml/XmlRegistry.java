package org.cmdbuild.cmdbf.xml;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class XmlRegistry {
	private final Collection<XmlNamespace> namespaces;

	public XmlRegistry(final Collection<XmlNamespace> namespaces) {
		this.namespaces = namespaces;
		for (final XmlNamespace namespace : namespaces) {
			namespace.setRegistry(this);
		}
	}

	private Iterable<XmlNamespace> getNamespaces() {
		return Iterables.filter(namespaces, new Predicate<XmlNamespace>() {
			@Override
			public boolean apply(final XmlNamespace input) {
				return input.isEnabled();
			}
		});
	}

	public Iterable<String> getSystemIds() {
		return Iterables.transform(getNamespaces(), new Function<XmlNamespace, String>() {
			@Override
			public String apply(final XmlNamespace input) {
				return input.getSystemId();
			}
		});
	}

	public XmlNamespace getBySystemId(final String systemId) {
		return Iterables.find(getNamespaces(), new Predicate<XmlNamespace>() {
			@Override
			public boolean apply(final XmlNamespace input) {
				return input.getSystemId().equals(systemId);
			}
		});
	}

	public XmlNamespace getByNamespaceURI(final String namespace) {
		return Iterables.find(getNamespaces(), new Predicate<XmlNamespace>() {
			@Override
			public boolean apply(final XmlNamespace input) {
				return input.getNamespaceURI().equals(namespace);
			}
		});
	}

	public XmlSchema getSchema(final String systemId) {
		return getBySystemId(systemId).getSchema();
	}

	public boolean updateSchema(final XmlSchema schema) {
		return Iterables.any(getNamespaces(), new Predicate<XmlNamespace>() {
			@Override
			public boolean apply(final XmlNamespace input) {
				return input.updateSchema(schema);
			}
		});
	}

	public Iterable<? extends Object> getTypes(final Class<?> cls) {
		return Iterables.concat(Iterables.transform(getNamespaces(),
				new Function<XmlNamespace, Iterable<? extends Object>>() {
					@Override
					public Iterable<? extends Object> apply(final XmlNamespace input) {
						return input.getTypes(cls);
					}
				}));
	}

	public QName getTypeQName(final Object type) {
		return Iterables.tryFind(Iterables.transform(getNamespaces(), new Function<XmlNamespace, QName>() {
			@Override
			public QName apply(final XmlNamespace input) {
				return input.getTypeQName(type);
			}
		}), Predicates.notNull()).orNull();
	}

	public Object getType(final QName qname) {
		return Iterables.tryFind(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Object>() {
			@Override
			public Object apply(final XmlNamespace input) {
				return input.getType(qname);
			}
		}), Predicates.notNull()).orNull();
	}

	public boolean serialize(final Node xml, final Object object) {
		return Iterables.any(getNamespaces(), new Predicate<XmlNamespace>() {
			@Override
			public boolean apply(final XmlNamespace input) {
				return input.serialize(xml, object);
			}
		});
	}

	public Object deserialize(final Node xml) {
		return Iterables.find(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Object>() {
			@Override
			public Object apply(final XmlNamespace input) {
				return input.deserialize(xml);
			}
		}), Predicates.notNull());
	}

	public boolean serializeValue(final Node xml, final Object object) {
		return Iterables.any(getNamespaces(), new Predicate<XmlNamespace>() {
			@Override
			public boolean apply(final XmlNamespace input) {
				return input.serializeValue(xml, object);
			}
		});
	}

	public Object deserializeValue(final Node xml, final Object type) {
		return Iterables.find(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Object>() {
			@Override
			public Object apply(final XmlNamespace input) {
				return input.deserializeValue(xml, type);
			}
		}), Predicates.notNull());
	}
}
