package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public class ReferenceAttributeType extends AbstractReferenceAttributeType {

	private final CMIdentifier identifier;

	/**
	 * @deprecated use {@link #ReferenceAttributeType(CMIdentifier)} instead.
	 */
	@Deprecated
	public ReferenceAttributeType(final String domain) {
		this(new CMIdentifier() {

			@Override
			public String getLocalName() {
				return domain;
			}

			@Override
			public String getNameSpace() {
				return CMIdentifier.DEFAULT_NAMESPACE;
			}

		});
	}

	public ReferenceAttributeType(final CMDomain domain) {
		this(domain.getIdentifier());
	}

	public ReferenceAttributeType(final CMIdentifier identifier) {
		this.identifier = identifier;
	}

	public CMIdentifier getIdentifier() {
		return identifier;
	}

	public String getDomainName() {
		return identifier.getLocalName();
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

}
