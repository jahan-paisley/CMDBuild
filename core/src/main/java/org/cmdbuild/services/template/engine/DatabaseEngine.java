package org.cmdbuild.services.template.engine;

import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.services.template.store.TemplateRepository;

public class DatabaseEngine implements Engine {

	public static DatabaseEngine of(final TemplateRepository templateRepository) {
		return new DatabaseEngine(templateRepository);
	}

	private final TemplateRepository templateRepository;

	private DatabaseEngine(final TemplateRepository templateRepository) {
		this.templateRepository = templateRepository;
	}

	@Override
	public Object eval(final String expression) {
		return templateRepository.getTemplate(expression);
	}

}
