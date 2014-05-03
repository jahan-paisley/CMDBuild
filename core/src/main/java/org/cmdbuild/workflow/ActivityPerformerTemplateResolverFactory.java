package org.cmdbuild.workflow;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.services.template.engine.DatabaseEngine;

public class ActivityPerformerTemplateResolverFactory {

	private final DatabaseEngine databaseTemplateEngine;
	private final String prefix;

	public ActivityPerformerTemplateResolverFactory(final DatabaseEngine databaseTemplateEngine,
			final String prefix) {
		this.databaseTemplateEngine = databaseTemplateEngine;
		this.prefix = prefix;
	}

	public TemplateResolver create() {
		return EngineBasedTemplateResolver.newInstance() //
				.withEngine(databaseTemplateEngine, prefix) //
				.build();
	}

}
