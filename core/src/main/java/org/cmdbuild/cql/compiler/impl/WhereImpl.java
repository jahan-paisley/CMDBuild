package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.Where;

public class WhereImpl extends WhereElementImpl implements Where {
	boolean isDefault = false;

	public void setDefault() {
		isDefault = true;
	}

	public boolean isDefault() {
		return isDefault;
	}
}
