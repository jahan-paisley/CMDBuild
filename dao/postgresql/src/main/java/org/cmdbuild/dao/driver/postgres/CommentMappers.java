package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.function.DBFunction.FunctionMetadata;

class CommentMappers {

	private CommentMappers() {
		// prevents instantiation
	}

	public static final CommentMapper CLASS_COMMENT_MAPPER = new EntryTypeCommentMapper() {

		private static final String SUPERCLASS = "SUPERCLASS";
		private static final String TYPE = "TYPE";
		private static final String USERSTOPPABLE = "USERSTOPPABLE";

		public static final String TYPE_CLASS = "class";
		public static final String TYPE_SIMPLECLASS = "simpleclass";

		{
			define(DESCR, EntryTypeMetadata.DESCRIPTION);
			define(SUPERCLASS, ClassMetadata.SUPERCLASS);
			define(TYPE, EntryTypeMetadata.HOLD_HISTORY, new CommentValueConverter() {
				@Override
				public String getMetaValueFromComment(final String commentValue) {
					return Boolean.valueOf(!TYPE_SIMPLECLASS.equals(commentValue)).toString();
				}

				@Override
				public String getCommentValueFromMeta(final String metaValue) {
					return Boolean.valueOf(metaValue) ? TYPE_CLASS : TYPE_SIMPLECLASS;
				}
			});
			define(USERSTOPPABLE, EntryTypeMetadata.USER_STOPPABLE);
		}
	};

	public static final CommentMapper DOMAIN_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			// Excellent name choice!
			define("LABEL", EntryTypeMetadata.DESCRIPTION);
			define("CLASS1", DomainMetadata.CLASS_1);
			define("CLASS2", DomainMetadata.CLASS_2);
			/*
			 * The descriptions should be the attribute descriptions to support
			 * n-ary domains
			 */
			define("DESCRDIR", DomainMetadata.DESCRIPTION_1);
			define("DESCRINV", DomainMetadata.DESCRIPTION_2);
			define("CARDIN", DomainMetadata.CARDINALITY);
			define("MASTERDETAIL", DomainMetadata.MASTERDETAIL);
			define("MDLABEL", DomainMetadata.MASTERDETAIL_DESCRIPTION);
		}
	};

	public static final CommentMapper FUNCTION_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			define("CATEGORIES", FunctionMetadata.CATEGORIES);
		}
	};

	public static final CommentMapper ATTRIBUTE_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			// TODO mapping
			define(DESCR, EntryTypeMetadata.DESCRIPTION);
			define("BASEDSP", AttributeMetadata.BASEDSP);
			define("CLASSORDER", AttributeMetadata.CLASSORDER);
			define("EDITORTYPE", AttributeMetadata.EDITOR_TYPE);
			define("GROUP", AttributeMetadata.GROUP);
			define("INDEX", AttributeMetadata.INDEX);
			define("FIELDMODE", AttributeMetadata.FIELD_MODE);
			define("LOOKUP", AttributeMetadata.LOOKUP_TYPE);
			define("REFERENCEDIRECT", AttributeMetadata.REFERENCE_DIRECT);
			define("REFERENCEDOM", AttributeMetadata.REFERENCE_DOMAIN);
			define("REFERENCETYPE", AttributeMetadata.REFERENCE_TYPE);
			define("FKTARGETCLASS", AttributeMetadata.FK_TARGET_CLASS);
			define("FILTER", AttributeMetadata.FILTER);
		}
	};

}
