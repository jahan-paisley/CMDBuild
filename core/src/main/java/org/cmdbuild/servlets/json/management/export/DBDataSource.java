package org.cmdbuild.servlets.json.management.export;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.collect.Lists;

public class DBDataSource implements CMDataSource {

	private final CMDataView view;
	private final CMClass sourceClass;

	public DBDataSource(final CMDataView view, final CMClass sourceClass) {
		Validate.notNull(sourceClass);
		this.view = view;
		this.sourceClass = sourceClass;
	}

	@Override
	public List<String> getHeaders() {
		final List<String> attributeNames = Lists.newArrayList();
		final List<CMAttribute> attributes = Lists.newArrayList(sourceClass.getActiveAttributes());

		final Comparator<CMAttribute> comp = new Comparator<CMAttribute>() {
			@Override
			public int compare(CMAttribute o1, CMAttribute o2) {
				if (o1.getIndex() < o2.getIndex()) {
					return -1;
				} else if (o1.getIndex() > o2.getIndex()) {
					return 1;
				} else {
					return 0;
				}
			}
		};

		Collections.sort(attributes, comp);

		for (final CMAttribute attribute: attributes) {
			attributeNames.add(attribute.getName());
		}

		return attributeNames;
	}

	@Override
	public Iterable<CMEntry> getEntries() {
		final List<CMEntry> entries = Lists.newArrayList();
		final CMQueryResult result = view.select(anyAttribute(sourceClass)).from(sourceClass).run();
		for (final CMQueryRow row : result) {
			entries.add(row.getCard(sourceClass));
		}
		return entries;
	}

}
