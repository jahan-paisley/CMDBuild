package utils;

import static java.lang.String.format;
import static utils.TestLoggerConstants.LOGGER_CATEGORY;
import static utils.TestLoggerConstants.UNUSED_SHANDLE;

import org.cmdbuild.workflow.SimpleEventManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggerEventManager implements SimpleEventManager {

	public static final String ACTIVITY_STARTED_LOG = "activityStarted";
	public static final String ACTIVITY_CLOSED_LOG = "activityClosed";
	public static final String PROCESS_STARTED_LOG = "processStarted";
	public static final String PROCESS_CLOSED_LOG = "processClosed";
	public static final String PROCESS_SUSPENDED_LOG = "processSuspended";
	public static final String PROCESS_RESUMED_LOG = "processResumed";

	private final CallbackUtilities cus;

	public LoggerEventManager(final CallbackUtilities cus) {
		this.cus = cus;
	}

	@Override
	public void activityClosed(final ActivityInstance activityInstance) {
		logWithId(ACTIVITY_CLOSED_LOG, activityInstance.getActivityDefinitionId());
	}

	@Override
	public void activityStarted(final ActivityInstance activityInstance) {
		logWithId(ACTIVITY_STARTED_LOG, activityInstance.getActivityDefinitionId());
	}

	@Override
	public void processClosed(final ProcessInstance processInstance) {
		logWithId(PROCESS_CLOSED_LOG, processInstance.getProcessDefinitionId());
	}

	@Override
	public void processResumed(final ProcessInstance processInstance) {
		logWithId(PROCESS_RESUMED_LOG, processInstance.getProcessDefinitionId());
	}

	@Override
	public void processStarted(final ProcessInstance processInstance) {
		logWithId(PROCESS_STARTED_LOG, processInstance.getProcessDefinitionId());
	}

	@Override
	public void processSuspended(final ProcessInstance processInstance) {
		logWithId(PROCESS_SUSPENDED_LOG, processInstance.getProcessDefinitionId());
	}

	private void logWithId(final String message, final String id) {
		cus.info(UNUSED_SHANDLE, LOGGER_CATEGORY, format("%s: %s", message, id));
	}

}
