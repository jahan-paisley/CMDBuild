package utils;

import static utils.TestLoggerConstants.LOGGER_CATEGORY;
import static utils.TestLoggerConstants.UNUSED_SHANDLE;

import org.cmdbuild.shark.toolagent.AbstractConditionalToolAgent;

public class ActivityNameLoggerToolAgent extends AbstractConditionalToolAgent {

	@Override
	protected void innerInvoke() throws Exception {
		cus.info(UNUSED_SHANDLE, LOGGER_CATEGORY, toolInfo.getActId());
	}

}
