package org.cmdbuild.dao.query.clause.from;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.ClassHistory;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;

public class ClassFromClause implements FromClause {

	private final CMDataView dataView;
	private final CMEntryType entryType;
	private final Alias alias;

	public ClassFromClause(final CMDataView dataView, final CMEntryType entryType, final Alias alias) {
		Validate.isTrue(entryType instanceof CMClass, "from clause must be for classes only");
		this.dataView = dataView;
		this.entryType = entryType;
		this.alias = alias;
	}

	@Override
	public CMEntryType getType() {
		return entryType;
	}

	@Override
	public Alias getAlias() {
		return alias;
	}

	@Override
	public boolean isHistory() {
		return entryType instanceof ClassHistory;
	}

	@Override
	public EntryTypeStatus getStatus(final CMEntryType entryType) {
		return new EntryTypeStatus() {

			@Override
			public boolean isAccessible() {
				return dataView.findClass(entryType.getId()) != null;
			}

			@Override
			public boolean isActive() {
				return entryType.isActive();
			}

		};
	}

}
