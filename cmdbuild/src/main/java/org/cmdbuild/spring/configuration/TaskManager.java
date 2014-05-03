package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.task.DefaultTaskStore;
import org.cmdbuild.data.store.task.TaskDefinition;
import org.cmdbuild.data.store.task.TaskDefinitionConverter;
import org.cmdbuild.data.store.task.TaskParameter;
import org.cmdbuild.data.store.task.TaskParameterConverter;
import org.cmdbuild.data.store.task.TaskStore;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter.ObserverFactory;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.DefaultObserverFactory;
import org.cmdbuild.logic.taskmanager.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.DefaultSynchronousEventFacade;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.LogicAndObserverConverter;
import org.cmdbuild.logic.taskmanager.LogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.LogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ReadEmailTaskJobFactory;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTaskJobFactory;
import org.cmdbuild.logic.taskmanager.SynchronousEventFacade;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.logic.taskmanager.TransactionalTaskManagerLogic;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.event.DefaultObserverCollector;
import org.cmdbuild.services.event.ObserverCollector;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class TaskManager {

	@Autowired
	private Api api;

	@Autowired
	private ConfigurableEmailServiceFactory configurableEmailServiceFactory;

	@Autowired
	private Data data;

	@Autowired
	private DatabaseConfiguration databaseConfiguration;

	@Autowired
	private Dms dms;

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private Email email;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserStore userStore;

	@Autowired
	private Workflow workflow;

	@Bean
	public TaskManagerLogic taskManagerLogic() {
		return new TransactionalTaskManagerLogic(defaultTaskManagerLogic());
	}

	private DefaultTaskManagerLogic defaultTaskManagerLogic() {
		return new DefaultTaskManagerLogic(taskConverter(), defaultTaskStore(), defaultSchedulerTaskFacade(),
				defaultSynchronousEventFacade());
	}

	@Bean
	protected SchedulerFacade defaultSchedulerTaskFacade() {
		return new DefaultSchedulerFacade(scheduler.defaultSchedulerService(), defaultLogicAndSchedulerConverter());
	}

	@Bean
	public ObserverCollector observerCollector() {
		return new DefaultObserverCollector();
	}

	@Bean
	protected LogicAndStoreConverter taskConverter() {
		return new DefaultLogicAndStoreConverter();
	}

	@Bean
	protected TaskStore defaultTaskStore() {
		return new DefaultTaskStore(dataViewSchedulerJobStore(), dataViewSchedulerJobParameterStore());
	}

	@Bean
	protected Store<TaskDefinition> dataViewSchedulerJobStore() {
		return DataViewStore.newInstance(systemDataView, schedulerJobConverter());
	}

	@Bean
	protected StorableConverter<TaskDefinition> schedulerJobConverter() {
		return new TaskDefinitionConverter();
	}

	@Bean
	protected Store<TaskParameter> dataViewSchedulerJobParameterStore() {
		return DataViewStore.newInstance(systemDataView, schedulerJobParameterStoreConverter());
	}

	@Bean
	protected StorableConverter<TaskParameter> schedulerJobParameterStoreConverter() {
		return new TaskParameterConverter();
	}

	@Bean
	protected LogicAndSchedulerConverter defaultLogicAndSchedulerConverter() {
		final DefaultLogicAndSchedulerConverter converter = new DefaultLogicAndSchedulerConverter();
		converter.register(ReadEmailTask.class, readEmailTaskJobFactory());
		converter.register(StartWorkflowTask.class, startWorkflowTaskJobFactory());
		return converter;
	}

	@Bean
	protected ReadEmailTaskJobFactory readEmailTaskJobFactory() {
		return new ReadEmailTaskJobFactory( //
				email.emailAccountStore(), //
				configurableEmailServiceFactory, //
				email.subjectHandler(), //
				email.emailPersistence(), //
				workflow.systemWorkflowLogicBuilder() //
						.build(), //
				dms.dmsLogic(), //
				data.systemDataView());
	}

	@Bean
	protected StartWorkflowTaskJobFactory startWorkflowTaskJobFactory() {
		return new StartWorkflowTaskJobFactory(workflow.systemWorkflowLogicBuilder().build());
	}

	@Bean
	protected SynchronousEventFacade defaultSynchronousEventFacade() {
		return new DefaultSynchronousEventFacade(observerCollector(), logicAndObserverConverter());
	}

	@Bean
	protected LogicAndObserverConverter logicAndObserverConverter() {
		return new DefaultLogicAndObserverConverter(observerFactory());
	}

	@Bean
	protected ObserverFactory observerFactory() {
		return new DefaultObserverFactory(userStore, api.systemFluentApi());
	}

}
