package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Filter {

	@Autowired
	private Data data;

	@Autowired
	private UserStore userStore;

	@Bean
	@Scope(PROTOTYPE)
	public DataViewFilterStore dataViewFilterStore() {
		return new DataViewFilterStore(data.systemDataView(), operationUser());
	}

	@Bean
	@Scope(PROTOTYPE)
	protected OperationUser operationUser() {
		return userStore.getUser();
	}

}
