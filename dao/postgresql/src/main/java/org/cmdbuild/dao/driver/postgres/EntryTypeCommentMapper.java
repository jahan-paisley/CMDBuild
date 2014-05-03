package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;

abstract class EntryTypeCommentMapper extends CommentMapper {

	protected static final String DESCR = "DESCR";
	private static final String MODE = "MODE";
	private static final String STATUS = "STATUS";

	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_NOACTIVE = "noactive";

	{
		define(STATUS, EntryTypeMetadata.ACTIVE, new CommentValueConverter() {

			@Override
			public String getMetaValueFromComment(final String commentValue) {
				// Set to active by default for backward compatibility
				return Boolean.valueOf(!STATUS_NOACTIVE.equalsIgnoreCase(commentValue)).toString();
			}

			@Override
			public String getCommentValueFromMeta(final String metaValue) {
				return Boolean.parseBoolean(metaValue) ? STATUS_ACTIVE : STATUS_NOACTIVE;
			}

		});
		define(MODE, EntryTypeMetadata.MODE, new CommentValueConverter() {

			@Override
			public String getMetaValueFromComment(final String commentValue) {
				return commentValue.toLowerCase().trim();
			}

			@Override
			public String getCommentValueFromMeta(final String metaValue) {
				return metaValue;
			}

		});
	}

}
