package org.cmdbuild.shark.util;

import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.api.SchemaApi.ClassInfo;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.BshToolAgent;

/**
 * 
 * Needed only for supporting the tool agent "createReferenceObj" (interpreted
 * by {@link BshToolAgent}).
 * 
 */
public class CmdbuildUtils {

	public interface CmdbuildTableStruct {

		int getId();

	}

	private static CmdbuildUtils instance = new CmdbuildUtils();

	public static CmdbuildUtils getInstance() {
		return instance;
	}

	private WorkflowApi workflowApi;

	private CmdbuildUtils() {
	}

	public void configure(final CallbackUtilities cus) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final ConfigurationHelper configurationHelper = new ConfigurationHelper(cus);
		final SharkWorkflowApiFactory factory = configurationHelper.getWorkflowApiFactory();
		factory.setup(cus);
		workflowApi = factory.createWorkflowApi();
	}

	public CmdbuildTableStruct getStructureFromName(final String name) throws Exception {
		final WorkflowApi.ClassInfo classInfo = workflowApi.findClass(name);
		return tableStructFrom(classInfo);
	}

	private CmdbuildTableStruct tableStructFrom(final ClassInfo classInfo) {
		return new CmdbuildTableStruct() {
			@Override
			public int getId() {
				return classInfo.getId();
			}
		};
	}

}
