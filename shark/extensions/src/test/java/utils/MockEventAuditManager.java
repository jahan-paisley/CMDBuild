package utils;

import static org.mockito.Mockito.mock;

import org.cmdbuild.workflow.DelegatingEventAuditManager;
import org.cmdbuild.workflow.SimpleEventManager;

public class MockEventAuditManager extends DelegatingEventAuditManager {

	public static final SimpleEventManager mock;

	static {
		mock = mock(SimpleEventManager.class);
	}

	public MockEventAuditManager() {
		super();
		setEventManager(mock);
	}

}
