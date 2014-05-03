package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver.EntryFiller;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class CardEntryFiller extends EntryFiller<CMCard> {

	@Override
	public CMCard getOutput() {
		return new ForwardingCard(input) {

			private Map<String, Object> _values = Maps.newHashMap(values);

			@Override
			public Iterable<Entry<String, Object>> getAllValues() {
				return _values.entrySet();
			}

			@Override
			public Iterable<Entry<String, Object>> getValues() {
				return from(getAllValues()) //
						.filter(new Predicate<Map.Entry<String, Object>>() {
							@Override
							public boolean apply(final Entry<String, Object> input) {
								final String name = input.getKey();
								final CMAttribute attribute = getType().getAttribute(name);
								return (attribute != null) && !attribute.isSystem();
							}
						});
			}

		};
	}

}
