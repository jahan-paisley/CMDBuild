package org.cmdbuild.api.fluent.ws;

public abstract class EntryTypeAttribute {

	public interface Visitor {

		void visit(ClassAttribute classAttribute);

		void visit(FunctionInput functionInput);

		void visit(FunctionOutput functionOutput);

	}

	protected final String entryTypeName;
	private final String attributeName;

	protected EntryTypeAttribute(final String entryTypeName, final String attributeName) {
		this.entryTypeName = entryTypeName;
		this.attributeName = attributeName;
	}

	public abstract void accept(Visitor visitor);

	public String getAttributeName() {
		return attributeName;
	}

}
