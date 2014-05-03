package org.cmdbuild.data.view;

import static com.google.common.collect.FluentIterable.from;

import java.util.Collections;

import org.cmdbuild.dao.entry.ForwardingAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingClass;
import org.cmdbuild.dao.entrytype.ForwardingDomain;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.QuerySpecsBuilderImpl;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Grants additional permissions for those classes/domains whose are requested
 * by name/id/identifier. Those entry types, if not found in the default data
 * view, are searched in the fallback one. If found, they are returned to the
 * client with a limited number of attributes.
 */
public class PermissiveDataView extends ForwardingDataView {

	private static class PermissiveClass extends ForwardingClass {

		private final Predicate<CMAttribute> DESCRIPTION_ATTRIBUTE_ONLY = new Predicate<CMAttribute>() {

			@Override
			public boolean apply(final CMAttribute input) {
				return input.getName().equals(getDescriptionAttributeName());
			}

		};

		public PermissiveClass(final CMClass inner) {
			super(inner);
		}

		@Override
		public Iterable<? extends CMAttribute> getAllAttributes() {
			return from(super.getAllAttributes()) //
					.filter(DESCRIPTION_ATTRIBUTE_ONLY) //
					.transform(TO_PERMISSIVE_ATTRIBUTE);
		}

	}

	private static class PermissiveDomain extends ForwardingDomain {

		public PermissiveDomain(final CMDomain inner) {
			super(inner);
		}

		@Override
		public Iterable<? extends CMAttribute> getAllAttributes() {
			return Collections.emptyList();
		}

	}

	private static class PermissiveAttribute extends ForwardingAttribute {

		public PermissiveAttribute(final CMAttribute inner) {
			super(inner);
		}

		@Override
		public Mode getMode() {
			return Mode.READ;
		}

	}

	private static final Function<CMAttribute, PermissiveAttribute> TO_PERMISSIVE_ATTRIBUTE = new Function<CMAttribute, PermissiveAttribute>() {

		@Override
		public PermissiveAttribute apply(final CMAttribute input) {
			return new PermissiveAttribute(input);
		}

	};

	private final CMDataView fallbackDataView;

	public PermissiveDataView(final CMDataView defaultDataView, final CMDataView fallbackDataView) {
		super(defaultDataView);
		this.fallbackDataView = fallbackDataView;
	}

	@Override
	public CMClass findClass(final Long id) {
		final CMClass found = super.findClass(id);
		final CMClass foundInFallback = (found != null) ? null : fallbackDataView.findClass(id);
		return (found != null) ? found : (foundInFallback != null) ? new PermissiveClass(foundInFallback) : null;
	}

	@Override
	public CMClass findClass(final String name) {
		final CMClass found = super.findClass(name);
		final CMClass foundInFallback = (found != null) ? null : fallbackDataView.findClass(name);
		return (found != null) ? found : (foundInFallback != null) ? new PermissiveClass(foundInFallback) : null;
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		final CMClass found = super.findClass(identifier);
		final CMClass foundInFallback = (found != null) ? null : fallbackDataView.findClass(identifier);
		return (found != null) ? found : (foundInFallback != null) ? new PermissiveClass(foundInFallback) : null;
	}

	@Override
	public CMDomain findDomain(final Long id) {
		final CMDomain found = super.findDomain(id);
		final CMDomain foundInFallback = (found != null) ? null : fallbackDataView.findDomain(id);
		return (found != null) ? found : (foundInFallback != null) ? new PermissiveDomain(foundInFallback) : null;
	}

	@Override
	public CMDomain findDomain(final String name) {
		final CMDomain found = super.findDomain(name);
		final CMDomain foundInFallback = (found != null) ? null : fallbackDataView.findDomain(name);
		return (found != null) ? found : (foundInFallback != null) ? new PermissiveDomain(foundInFallback) : null;
	}

	@Override
	public final QuerySpecsBuilder select(final Object... attrDef) {
		return new QuerySpecsBuilderImpl(fallbackDataView, this) //
				.select(attrDef);
	}

}
