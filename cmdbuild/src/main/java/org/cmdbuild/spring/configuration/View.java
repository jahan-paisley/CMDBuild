package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class View {

	@Autowired
	private Data data;

	@Autowired
	private UserStore userStore;

	@Bean
	public ViewConverter viewConverter() {
		return new ViewConverter(data.systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	public ViewLogic viewLogic() {
		return new ViewLogic(data.systemDataView(), viewConverter(), userStore.getUser());
	}

}
