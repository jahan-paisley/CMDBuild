package org.cmdbuild.dao.entrytype;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DBIdentifier implements CMIdentifier {

	public static CMIdentifier fromName(final String name) {
		return fromNameAndNamespace(name, CMIdentifier.DEFAULT_NAMESPACE);
	}

	public static CMIdentifier fromNameAndNamespace(final String localname, final String namespace) {
		return new DBIdentifier(localname, namespace);
	}

	private final String localname;
	private final String namespace;

	private final transient int hashCode;
	private final transient String toString;

	public DBIdentifier(final String localname) {
		this(localname, DEFAULT_NAMESPACE);
	}

	public DBIdentifier(final String localname, final String namespace) {
		Validate.isTrue(isNotBlank(localname), "invalid local name");
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
	public String getLocalName() {
		return localname;
	}

	@Override
	public String getNameSpace() {
		return namespace;
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
		if (!(obj instanceof DBIdentifier)) {
			return false;
		}
		final DBIdentifier other = DBIdentifier.class.cast(obj);
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
