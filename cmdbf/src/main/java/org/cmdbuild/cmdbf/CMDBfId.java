package org.cmdbuild.cmdbf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;

public class CMDBfId extends MdrScopedIdType {

	private static final char[] escapeArray = { '\\', ',' };
	private static final Pattern pattern = Pattern
			.compile("\\{((?:[^\\,\\\\]|\\\\\\,|\\\\\\\\)*),((?:[^\\,\\\\]|\\\\\\,|\\\\\\\\)*)\\}");

	public CMDBfId(final MdrScopedIdType id) {
		this(id.getMdrId(), id.getLocalId());
	}

	public CMDBfId(final String mdrId, final String localId) {
		setMdrId(mdrId);
		setLocalId(localId);
	}

	@Override
	public boolean equals(final Object obj) {
		boolean equals = false;
		if (obj instanceof MdrScopedIdType) {
			final MdrScopedIdType id = (MdrScopedIdType) obj;
			if (getMdrId() == null) {
				equals = id.getMdrId() == null;
			} else {
				equals = getMdrId().equals(id.getMdrId());
			}
			if (equals) {
				if (getLocalId() == null) {
					equals = id.getLocalId() == null;
				} else {
					equals = getLocalId().equals(id.getLocalId());
				}
			}
		}
		return equals;
	}

	@Override
	public int hashCode() {
		int hash = 23;
		hash *= 37;
		if (getMdrId() != null) {
			hash += getMdrId().hashCode();
		}
		hash *= 37;
		if (getLocalId() != null) {
			hash += getLocalId().hashCode();
		}
		return hash;
	}

	@Override
	public String toString() {
		return toString(this);
	}

	public static String toString(final MdrScopedIdType id) {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("'{");
		buffer.append(escape(id.getMdrId()));
		buffer.append(",");
		buffer.append(escape(id.getLocalId()));
		buffer.append("}'");
		return buffer.toString();
	}

	public static CMDBfId valueOf(final String value) {
		final Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			final String mdrId = matcher.group(1);
			final String localId = matcher.group(2);
			return new CMDBfId(unescape(mdrId), unescape(localId));
		} else {
			return null;
		}
	}

	private static String escape(final String value) {
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			final char c = value.charAt(i);
			for (int j = 0; j < escapeArray.length; j++) {
				if (c == escapeArray[j]) {
					buffer.append('\\');
				}
			}
			buffer.append(c);
		}
		return buffer.toString();
	}

	private static String unescape(final String value) {
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\\' && i + 1 < value.length()) {
				c = value.charAt(++i);
			}
			buffer.append(c);
		}
		return buffer.toString();
	}
}
