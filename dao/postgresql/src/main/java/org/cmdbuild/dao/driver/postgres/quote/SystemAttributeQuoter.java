package org.cmdbuild.dao.driver.postgres.quote;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;

public class SystemAttributeQuoter implements Quoter {

	public static String quote(final SystemAttributes attribute) {
		return new SystemAttributeQuoter(attribute).quote();
	}

	private final SystemAttributes attribute;

	public SystemAttributeQuoter(final SystemAttributes attribute) {
		this.attribute = attribute;
	}

	@Override
	public String quote() {
		return IdentQuoter.quote(attribute.getDBName());
	}

}
