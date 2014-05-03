package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.services.store.menu.DataViewMenuStore;
import org.cmdbuild.services.store.menu.MenuItemConverter;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Menu {

	@Autowired
	private DashboardLogic dashboardLogic;

	@Autowired
	private GroupFetcher groupFetcher;

	@Autowired
	private PrivilegeContextFactory privilegeContextFactory;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private User user;

	@Autowired
	private UserStore userStore;

	@Autowired
	private View view;

	@Bean
	public MenuItemConverter menuItemConverter() {
		return new MenuItemConverter(systemDataView, systemDataAccessLogicBuilder);
	}

	@Bean
	@Scope(PROTOTYPE)
	public DataViewMenuStore dataViewMenuStore() {
		return new DataViewMenuStore( //
				systemDataView, //
				groupFetcher, //
				dashboardLogic, //
				user.userDataAccessLogicBuilder(), //
				privilegeContextFactory, //
				view.viewLogic(), //
				menuItemConverter(), //
				view.viewConverter(), //
				userStore.getUser());
	}

}
