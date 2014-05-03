package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public class ForeignKeyAttributeType extends AbstractReferenceAttributeType {

	private final CMIdentifier identifier;

	/**
	 * @deprecated use {@link #ReferenceAttributeType(CMIdentifier)} instead.
	 */
	@Deprecated
	public ForeignKeyAttributeType(final String destinationClassName) {
		this(new CMIdentifier() {

			@Override
			public String getLocalName() {
				return destinationClassName;
			}

			@Override
			public String getNameSpace() {
				return CMIdentifier.DEFAULT_NAMESPACE;
			}

		});
	}

	public ForeignKeyAttributeType(final CMEntryType entryType) {
		this(entryType.getIdentifier());
	}

	public ForeignKeyAttributeType(final CMIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	public CMIdentifier getIdentifier() {
		return identifier;
	}

	public String getForeignKeyDestinationClassName() {
		return identifier.getLocalName();
	}

}
