package org.cmdbuild.servlets.json;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;
import static org.cmdbuild.spring.configuration.User.BEAN_USER_DATA_VIEW;

import javax.sql.DataSource;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.NavigationTreeLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogicBuilder;
import org.cmdbuild.logic.bim.DefaultLayerLogic;
import org.cmdbuild.logic.bim.DefaultSynchronizationLogic;
import org.cmdbuild.logic.bim.DefaultViewerLogic;
import org.cmdbuild.logic.bim.LayerLogic;
import org.cmdbuild.logic.bim.SynchronizationLogic;
import org.cmdbuild.logic.bim.ViewerLogic;
import org.cmdbuild.logic.bim.project.DefaultProjectLogic;
import org.cmdbuild.logic.bim.project.ProjectLogic;
import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.email.EmailAccountLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.setup.SetupLogic;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.UserWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.localization.Localization;
import org.cmdbuild.services.startup.StartupLogic;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.json.serializers.ClassSerializer;
import org.cmdbuild.servlets.json.serializers.DomainSerializer;
import org.cmdbuild.servlets.json.serializers.RelationAttributeSerializer;
import org.cmdbuild.workflow.ActivityPerformerTemplateResolverFactory;

public class JSONBaseWithSpringContext extends JSONBase {

	protected OperationUser operationUser() {
		return applicationContext().getBean("operationUser", OperationUser.class);
	}

	/*
	 * Properties
	 */

	protected CmdbuildProperties cmdbuildConfiguration() {
		return applicationContext().getBean(CmdbuildProperties.class);
	}

	protected GraphProperties graphProperties() {
		return applicationContext().getBean(GraphProperties.class);
	}

	/*
	 * Database
	 */

	protected DataSource dataSource() {
		return applicationContext().getBean(DataSource.class);
	}

	protected PatchManager patchManager() {
		return applicationContext().getBean(PatchManager.class);
	}

	protected CMDataView systemDataView() {
		return applicationContext().getBean(DBDataView.class);
	}

	protected CMDataView userDataView() {
		return applicationContext().getBean(BEAN_USER_DATA_VIEW, CMDataView.class);
	}

	/*
	 * Stores
	 */

	protected FilterStore filterStore() {
		return applicationContext().getBean(FilterStore.class);
	}

	protected LanguageStore languageStore() {
		return applicationContext().getBean(LanguageStore.class);
	}

	protected LookupStore lookupStore() {
		return applicationContext().getBean(LookupStore.class);
	}

	protected MenuStore menuStore() {
		return applicationContext().getBean(MenuStore.class);
	}

	protected ReportStore reportStore() {
		return applicationContext().getBean(ReportStore.class);
	}

	protected UserStore userStore() {
		return applicationContext().getBean(UserStore.class);
	}

	/*
	 * Logics
	 */

	protected AuthenticationLogic authLogic() {
		return applicationContext().getBean(DefaultAuthenticationLogicBuilder.class).build();
	}

	protected ProjectLogic bimProjectLogic() {
		return applicationContext().getBean(DefaultProjectLogic.class);
	}

	protected LayerLogic bimLayerLogic() {
		return applicationContext().getBean(DefaultLayerLogic.class);
	}

	protected SynchronizationLogic bimConnectorLogic() {
		return applicationContext().getBean(DefaultSynchronizationLogic.class);
	}

	protected ViewerLogic viewerLogic() {
		return applicationContext().getBean(DefaultViewerLogic.class);
	}

	protected CachingLogic cachingLogic() {
		return applicationContext().getBean(CachingLogic.class);
	}

	protected DashboardLogic dashboardLogic() {
		return applicationContext().getBean(DashboardLogic.class);
	}

	protected DataAccessLogic systemDataAccessLogic() {
		return applicationContext().getBean(SystemDataAccessLogicBuilder.class).build();
	}

	protected DataAccessLogic userDataAccessLogic() {
		return applicationContext().getBean(UserDataAccessLogicBuilder.class).build();
	}

	protected DataDefinitionLogic dataDefinitionLogic() {
		return applicationContext().getBean(DataDefinitionLogic.class);
	}

	protected DmsLogic dmsLogic() {
		return applicationContext().getBean(DmsLogic.class);
	}

	protected EmailAccountLogic emailAccountLogic() {
		return applicationContext().getBean(EmailAccountLogic.class);
	}

	protected EmailLogic emailLogic() {
		return applicationContext().getBean(EmailLogic.class);
	}

	protected EmailTemplateLogic emailTemplateLogic() {
		return applicationContext().getBean(EmailTemplateLogic.class);
	}

	protected GISLogic gisLogic() {
		return applicationContext().getBean(GISLogic.class);
	}

	protected LookupLogic lookupLogic() {
		return applicationContext().getBean(LookupLogic.class);
	}

	protected NavigationTreeLogic navigationTreeLogic() {
		return applicationContext().getBean(NavigationTreeLogic.class);
	}

	protected SchedulerLogic schedulerLogic() {
		return applicationContext().getBean(SchedulerLogic.class);
	}

	protected SecurityLogic securityLogic() {
		return applicationContext().getBean(SecurityLogic.class);
	}

	protected SetupLogic setUpLogic() {
		return applicationContext().getBean(SetupLogic.class);
	}

	protected StartupLogic startupLogic() {
		return applicationContext().getBean(StartupLogic.class);
	}

	protected ViewLogic viewLogic() {
		return applicationContext().getBean(ViewLogic.class);
	}

	protected WorkflowLogic workflowLogic() {
		return applicationContext().getBean(UserWorkflowLogicBuilder.class).build();
	}

	protected WorkflowLogic systemWorkflowLogic() {
		return applicationContext().getBean(SystemWorkflowLogicBuilder.class).build();
	}

	protected TaskManagerLogic taskManagerLogic() {
		return applicationContext().getBean(TaskManagerLogic.class);
	}

	/*
	 * Localization
	 */

	protected Localization localization() {
		return new Localization() {

			@Override
			public String get(final String key) {
				return TranslationService.getInstance() //
						.getTranslation(languageStore().getLanguage(), key);
			}
		};
	}

	/*
	 * 
	 * Utilities
	 */

	protected ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory() {
		return applicationContext().getBean(ActivityPerformerTemplateResolverFactory.class);
	}

	/*
	 * Web
	 */

	protected RequestListener requestListener() {
		return applicationContext().getBean(RequestListener.class);
	}

	@Deprecated
	protected SessionVars sessionVars() {
		return applicationContext().getBean(SessionVars.class);
	}

	/*
	 * Serialization
	 */

	protected ClassSerializer classSerializer() {
		return applicationContext().getBean(ClassSerializer.class);
	}

	protected DomainSerializer domainSerializer() {
		return applicationContext().getBean(DomainSerializer.class);
	}

	protected CardSerializer cardSerializer() {
		return applicationContext().getBean(CardSerializer.class);
	}

	protected RelationAttributeSerializer relationAttributeSerializer() {
		return applicationContext().getBean(RelationAttributeSerializer.class);
	}

}
