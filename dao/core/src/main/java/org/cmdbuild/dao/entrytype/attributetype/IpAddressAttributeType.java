package org.cmdbuild.dao.entrytype.attributetype;

import java.util.regex.Pattern;

// TODO Change to CMAttributeType<InetAddress>
public class IpAddressAttributeType extends AbstractAttributeType<String> {

	private static final Pattern IPV4REGEX = Pattern
			.compile("^0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])$");

	public IpAddressAttributeType() {
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected String convertNotNullValue(final Object value) {
		final String stringValue = value.toString().trim();
		if (stringValue.isEmpty()) {
			return null;
		} else if (IPV4REGEX.matcher(stringValue).find()) {
			return stringValue;
		} else {
			throw illegalValue(value);
		}
	}

}
