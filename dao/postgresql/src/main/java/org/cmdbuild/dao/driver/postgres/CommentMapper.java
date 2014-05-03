package org.cmdbuild.dao.driver.postgres;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

abstract class CommentMapper {

	public static interface CommentValueConverter {

		String getMetaValueFromComment(String commentValue);

		String getCommentValueFromMeta(String metaValue);

	}

	private final BiMap<String, String> translationTable = HashBiMap.create();
	private final Map<String, CommentValueConverter> valueConverterTable = Maps.newHashMap();

	public String getMetaNameFromComment(final String commentName) {
		return translationTable.get(commentName);
	}

	public String getCommentNameFromMeta(final String metaName) {
		return translationTable.inverse().get(metaName);
	}

	public String getMetaValueFromComment(final String commentName, final String commentValue) {
		if (valueConverterTable.containsKey(commentName)) {
			return valueConverterTable.get(commentName).getMetaValueFromComment(commentValue);
		} else {
			return commentValue;
		}
	}

	public String getCommentValueFromMeta(final String commentName, final String metaValue) {
		if (valueConverterTable.containsKey(commentName)) {
			return valueConverterTable.get(commentName).getCommentValueFromMeta(metaValue);
		} else {
			return metaValue;
		}
	}

	protected void define(final String commentName, final String metaName) {
		translationTable.put(commentName, metaName);
	}

	protected void define(final String commentName, final String metaName, final CommentValueConverter valueConverter) {
		translationTable.put(commentName, metaName);
		if (valueConverter != null) {
			valueConverterTable.put(commentName, valueConverter);
		}
	}

}
