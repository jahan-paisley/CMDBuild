package org.cmdbuild.exception;

public class DmsException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final Type type;

	public enum Type {
		DMS_ATTACHMENT_DELETE_ERROR, //
		DMS_ATTACHMENT_NOTFOUND, //
		DMS_ATTACHMENT_UPLOAD_ERROR, //
		DMS_DOCUMENT_TYPE_DEFINITION_ERROR, //
		DMS_AUTOCOMPLETION_RULES_ERROR, //
		DMS_UPDATE_ERROR, //
		;

		public DmsException createException(final String... parameters) {
			return new DmsException(this, parameters);
		}
	}

	private DmsException(final Type type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public Type getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}

}
