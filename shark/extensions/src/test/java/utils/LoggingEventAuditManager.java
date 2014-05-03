package utils;

import org.cmdbuild.workflow.DelegatingEventAuditManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggingEventAuditManager extends DelegatingEventAuditManager {

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		setEventManager(new LoggerEventManager(cus));
	}

}
