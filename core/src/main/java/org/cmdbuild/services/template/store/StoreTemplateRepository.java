package org.cmdbuild.services.template.store;

import java.util.NoSuchElementException;

import org.cmdbuild.data.store.Store;

public class StoreTemplateRepository implements TemplateRepository {

	private final Store<Template> store;

	public StoreTemplateRepository(final Store<Template> store) {
		this.store = store;
	}

	@Override
	public String getTemplate(final String name) {
		try {
			final Template template = Template.of(name);
			final Template found = store.read(template);
			return (found == null) ? null : found.getValue();
		} catch (final NoSuchElementException e) {
			return null;
		}
	}

}
