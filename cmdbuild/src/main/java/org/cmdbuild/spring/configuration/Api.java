package org.cmdbuild.spring.configuration;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.core.api.fluent.LogicFluentApiExecutor;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Api {

	@Autowired
	private Data data;

	@Autowired
	private LookupLogic lookupLogic;

	@Bean
	public FluentApi systemFluentApi() {
		return new FluentApi(systemFluentApiExecutor());
	}

	@Bean
	public FluentApiExecutor systemFluentApiExecutor() {
		return new LogicFluentApiExecutor(data.systemDataAccessLogicBuilder().build(), lookupLogic);
	}

}
