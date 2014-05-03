package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.services.store.DBDashboardStore;
import org.cmdbuild.services.store.DashboardStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Dashboard {

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserStore userStore;

	@Bean
	public DashboardStore dashboardStore() {
		return new DBDashboardStore(systemDataView);
	}

	@Bean
	@Scope(PROTOTYPE)
	public DashboardLogic dashboardLogic() {
		return new DashboardLogic(systemDataView, dashboardStore(), userStore.getUser());
	}

}
