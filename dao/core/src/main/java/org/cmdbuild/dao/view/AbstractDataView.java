package org.cmdbuild.dao.view;

import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.QuerySpecsBuilderImpl;

public abstract class AbstractDataView implements CMDataView {

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return new QuerySpecsBuilderImpl(viewForBuilder(), viewForRunner()) //
				.select(attrDef);
	}

	protected CMDataView viewForBuilder() {
		return this;
	}

	protected CMDataView viewForRunner() {
		return this;
	}

}
