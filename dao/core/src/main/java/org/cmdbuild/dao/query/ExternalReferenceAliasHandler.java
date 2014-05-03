package org.cmdbuild.dao.query;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;

public class ExternalReferenceAliasHandler {

	private static final String QUERY_PATTERN = "%s#%s";
	private static final String RESULT_PATTERN = "%s#%s#%s";
	public static final String EXTERNAL_ATTRIBUTE = Constants.DESCRIPTION_ATTRIBUTE;

	private final String entryTypeAlias;
	private final String attributeName;

	public ExternalReferenceAliasHandler(final CMEntryType entryType, final CMAttribute attribute) {
		this(entryType.getName(), attribute);
	}

	public ExternalReferenceAliasHandler(final String entryTypeAlias, final CMAttribute attribute) {
		this.entryTypeAlias = entryTypeAlias;
		this.attributeName = attribute.getName();
	}

	public ExternalReferenceAliasHandler(final String entryTypeAlias, final String attributeName) {
		this.entryTypeAlias = entryTypeAlias;
		this.attributeName = attributeName;
	}

	public String forQuery() {
		return String.format(QUERY_PATTERN, //
				entryTypeAlias, //
				attributeName);
	}

	public String forResult() {
		return String.format(RESULT_PATTERN, //
				entryTypeAlias, //
				attributeName, //
				EXTERNAL_ATTRIBUTE);
	}

}