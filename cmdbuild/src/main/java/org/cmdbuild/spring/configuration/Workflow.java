package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SYSTEM;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.Builder;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logger.WorkflowLogger;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.template.engine.EngineNames;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.cmdbuild.workflow.ActivityPerformerTemplateResolverFactory;
import org.cmdbuild.workflow.DataViewWorkflowPersistence;
import org.cmdbuild.workflow.DefaultGroupQueryAdapter;
import org.cmdbuild.workflow.DefaultWorkflowEngine;
import org.cmdbuild.workflow.DefaultWorkflowEngine.DefaultWorkflowEngineBuilder;
import org.cmdbuild.workflow.DefaultXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.SharkTypesConverter.SharkTypesConverterBuilder;
import org.cmdbuild.workflow.UpdateOperationListenerImpl;
import org.cmdbuild.workflow.WorkflowEventManagerImpl;
import org.cmdbuild.workflow.WorkflowPersistence;
import org.cmdbuild.workflow.WorkflowTypesConverter;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.cmdbuild.workflow.service.AbstractSharkService.UpdateOperationListener;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.XpdlManager;
import org.cmdbuild.workflow.xpdl.XpdlManager.GroupQueryAdapter;
import org.cmdbuild.workflow.xpdl.XpdlProcessDefinitionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Workflow {

	@Autowired
	@Qualifier(DEFAULT)
	private AuthenticationService authenticationService;

	@Autowired
	private Data data;

	@Autowired
	private Email email;

	@Autowired
	private FilesStore filesStore;

	@Autowired
	private LookupStore lookupStore;

	@Autowired
	private Notifier notifier;

	@Autowired
	@Qualifier(SYSTEM)
	private PrivilegeContext systemPrivilegeContext;

	@Autowired
	private SystemUser systemUser;

	@Autowired
	private Template template;

	@Autowired
	private WorkflowConfiguration workflowConfiguration;

	@Bean
	public AbstractSharkService workflowService() {
		return new RemoteSharkService(workflowConfiguration);
	}

	@Bean
	public WorkflowLogger workflowLogger() {
		return new WorkflowLogger();
	}

	@Bean
	protected GroupQueryAdapter groupQueryAdapter() {
		return new DefaultGroupQueryAdapter(authenticationService);
	}

	@Bean
	protected XpdlExtendedAttributeVariableFactory xpdlExtendedAttributeVariableFactory() {
		return new SharkStyleXpdlExtendedAttributeVariableFactory();
	}

	@Bean
	protected ValuePairXpdlExtendedAttributeWidgetFactory xpdlExtendedAttributeWidgetFactory() {
		return new DefaultXpdlExtendedAttributeWidgetFactory( //
				template.storeTemplateRepository(), //
				notifier, //
				data.systemDataView(), //
				email.emailLogic(), //
				email.emailTemplateLogic());
	}

	@Bean
	protected XpdlProcessDefinitionStore processDefinitionStore() {
		return new XpdlProcessDefinitionStore(workflowService(), xpdlExtendedAttributeVariableFactory(),
				xpdlExtendedAttributeWidgetFactory());
	}

	@Bean
	protected ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory() {
		return new ActivityPerformerTemplateResolverFactory( //
				template.databaseTemplateEngine(), //
				EngineNames.DB_TEMPLATE);
	}

	@Bean
	protected TemplateResolver activityPerformerTemplateResolver() {
		return activityPerformerTemplateResolverFactory().create();
	}

	@Bean
	public ProcessDefinitionManager processDefinitionManager() {
		return new XpdlManager( //
				groupQueryAdapter(), //
				processDefinitionStore(), //
				activityPerformerTemplateResolver());
	}

	@Bean
	public WorkflowTypesConverter workflowTypesConverter() {
		return new SharkTypesConverterBuilder() //
				.withDataView(data.systemDataView()) //
				.withLookupStore(lookupStore) //
				.build();
	}

	@Bean
	@Scope(PROTOTYPE)
	protected WorkflowPersistence systemWorkflowPersistence() {
		return DataViewWorkflowPersistence.newInstance() //
				.withPrivilegeContext(systemPrivilegeContext) //
				.withOperationUser(systemUser.operationUserWithSystemPrivileges()) //
				.withDataView(data.systemDataView()) //
				.withProcessDefinitionManager(processDefinitionManager()) //
				.withLookupStore(lookupStore) //
				.withWorkflowService(workflowService()) //
				.withActivityPerformerTemplateResolverFactory(activityPerformerTemplateResolverFactory()) //
				.build();
	}

	@Bean
	protected WorkflowEventManager workflowEventManager() {
		return new WorkflowEventManagerImpl(systemWorkflowPersistence(), workflowService(), workflowTypesConverter());
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(SYSTEM)
	protected Builder<DefaultWorkflowEngine> systemWorkflowEngineBuilder() {
		return new DefaultWorkflowEngineBuilder() //
				.withOperationUser(systemUser.operationUserWithSystemPrivileges()) //
				.withPersistence(systemWorkflowPersistence()) //
				.withService(workflowService()) //
				.withTypesConverter(workflowTypesConverter()) //
				.withEventListener(workflowLogger()) //
				.withAuthenticationService(authenticationService);
	}

	@Bean
	@Scope(PROTOTYPE)
	public SystemWorkflowLogicBuilder systemWorkflowLogicBuilder() {
		return new SystemWorkflowLogicBuilder( //
				systemPrivilegeContext, //
				systemWorkflowEngineBuilder(), //
				data.systemDataView(), //
				data.systemDataView(), //
				lookupStore, //
				workflowConfiguration, //
				filesStore);
	}

	@Bean
	protected UpdateOperationListener updateOperationListener() {
		return new UpdateOperationListenerImpl(workflowService(), workflowEventManager());
	}

}
