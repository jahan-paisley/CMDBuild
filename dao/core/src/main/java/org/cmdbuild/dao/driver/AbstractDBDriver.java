package org.cmdbuild.dao.driver;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractDBDriver implements DBDriver, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(AbstractDBDriver.class.getName());

	private static class Identifier {

		public static Identifier from(final CMTypeObject typeObject) {
			return from(typeObject.getIdentifier());
		}

		public static Identifier from(final CMIdentifier identifier) {
			return new Identifier(identifier.getLocalName(), identifier.getNameSpace());
		}

		public final String localname;
		public final String namespace;

		private final transient int hashCode;
		private final transient String toString;

		public Identifier(final String localname, final String namespace) {
			this.localname = localname;
			this.namespace = namespace;

			this.hashCode = new HashCodeBuilder() //
					.append(localname) //
					.append(namespace) //
					.hashCode();
			this.toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.append("localname", localname) //
					.append("namespace", namespace) //
					.toString();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Identifier)) {
				return false;
			}
			final Identifier other = Identifier.class.cast(obj);
			return new EqualsBuilder() //
					.append(localname, other.localname) //
					.append(namespace, other.namespace) //
					.isEquals();
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	public static class DefaultTypeObjectCache implements TypeObjectCache {

		private final Map<Class<? extends CMTypeObject>, Map<Long, CMTypeObject>> storeById;
		private final Map<Class<? extends CMTypeObject>, Map<Identifier, CMTypeObject>> storeByIdentifier;

		public DefaultTypeObjectCache() {
			storeById = Maps.newHashMap();
			storeById.put(DBClass.class, newMapById());
			storeById.put(DBDomain.class, newMapById());
			storeById.put(DBFunction.class, newMapById());

			storeByIdentifier = Maps.newHashMap();
			storeByIdentifier.put(DBClass.class, newMapByIdentifier());
			storeByIdentifier.put(DBDomain.class, newMapByIdentifier());
			storeByIdentifier.put(DBFunction.class, newMapByIdentifier());
		}

		private static Map<Long, CMTypeObject> newMapById() {
			return Maps.newHashMap();
		}

		private static Map<Identifier, CMTypeObject> newMapByIdentifier() {
			return Maps.newHashMap();
		}

		@Override
		public boolean isEmpty(final Class<? extends CMTypeObject> typeObjectClass) {
			synchronized (this) {
				return storeById.get(typeObjectClass).isEmpty();
			}
		}

		@Override
		public void add(final CMTypeObject typeObject) {
			synchronized (this) {
				storeById.get(classOf(typeObject)).put(idOf(typeObject), typeObject);
				storeByIdentifier.get(classOf(typeObject)).put(identifierOf(typeObject), typeObject);
			}
		}

		@Override
		public void remove(final CMTypeObject typeObject) {
			synchronized (this) {
				storeById.get(classOf(typeObject)).remove(idOf(typeObject));
				storeByIdentifier.get(classOf(typeObject)).remove(identifierOf(typeObject));
			}
		}

		@Override
		public <T extends CMTypeObject> Iterable<T> fetch(final Class<T> typeObjectClass) {
			final List<T> elements = Lists.newArrayList();
			for (final Entry<Long, CMTypeObject> entry : storeById.get(typeObjectClass).entrySet()) {
				elements.add((T) entry.getValue());
			}
			return elements;
		}

		@Override
		public void clear() {
			synchronized (this) {
				logger.info(marker, "clearing all cache");
				clearClasses();
				clearDomains();
				clearFunctions();
			}
		}

		private void clearClasses() {
			logger.info(marker, "clearing classes cache");
			storeById.get(DBClass.class).clear();
			storeByIdentifier.get(DBClass.class).clear();
		}

		private void clearDomains() {
			logger.info(marker, "clearing domains cache");
			storeById.get(DBDomain.class).clear();
			storeByIdentifier.get(DBDomain.class).clear();
		}

		private void clearFunctions() {
			logger.info(marker, "clearing functions cache");
			storeById.get(DBFunction.class).clear();
			storeByIdentifier.get(DBFunction.class).clear();
		}

		private static Class<? extends CMTypeObject> classOf(final CMTypeObject typeObject) {
			return typeObject.getClass();
		}

		private static Long idOf(final CMTypeObject typeObject) {
			return typeObject.getId();
		}

		private static Identifier identifierOf(final CMTypeObject typeObject) {
			return Identifier.from(typeObject);
		}

	}

	protected TypeObjectCache cache;

	protected AbstractDBDriver(final TypeObjectCache cache) {
		Validate.notNull(cache, "The driver cache cannot be null");
		this.cache = cache;
	}

	@Override
	public final DBClass findClass(final Long id) {
		for (final DBClass dbClass : findAllClasses()) {
			if (dbClass.getId().equals(id)) {
				return dbClass;
			}
		}
		return null;
	}

	@Override
	public final DBClass findClass(final String name) {
		return findClass(name, CMIdentifier.DEFAULT_NAMESPACE);
	}

	@Override
	public DBClass findClass(final String localname, final String namespace) {
		for (final DBClass dbClass : findAllClasses()) {
			final CMIdentifier identifier = dbClass.getIdentifier();
			if (new EqualsBuilder() //
					.append(identifier.getLocalName(), localname) //
					.append(identifier.getNameSpace(), namespace) //
					.isEquals()) {
				return dbClass;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomain(final Long id) {
		for (final DBDomain dbDomain : findAllDomains()) {
			if (dbDomain.getId().equals(id)) {
				return dbDomain;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomain(final String localname) {
		return findDomain(localname, CMIdentifier.DEFAULT_NAMESPACE);
	}

	@Override
	public DBDomain findDomain(final String localname, final String namespace) {
		for (final DBDomain dbDomain : findAllDomains()) {
			final CMIdentifier identifier = dbDomain.getIdentifier();
			if (new EqualsBuilder() //
					.append(identifier.getLocalName(), localname) //
					.append(identifier.getNameSpace(), namespace) //
					.isEquals()) {
				return dbDomain;
			}
		}
		return null;
	}

	@Override
	public DBFunction findFunction(final String localname) {
		for (final DBFunction dbFunction : findAllFunctions()) {
			final CMIdentifier identifier = dbFunction.getIdentifier();
			if (new EqualsBuilder() //
					.append(identifier.getLocalName(), localname) //
					.append(identifier.getNameSpace(), CMIdentifier.DEFAULT_NAMESPACE) //
					.isEquals()) {
				return dbFunction;
			}
		}
		return null;
	}

	public void clearCache() {
		cache.clear();
	}

}
