package utils;

import org.cmdbuild.workflow.SimpleEventManager;
import org.cmdbuild.workflow.service.LocalSharkService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;

public abstract class AbstractLocalSharkServiceTest extends AbstractSharkServiceTest {

	public static final String USERNAME = "admin";

	protected SimpleEventManager eventManager;

	@BeforeClass
	public static void initWorkflowService() {
		ws = new LocalSharkService(new LocalSharkService.Config() {
			@Override
			public String getUsername() {
				return USERNAME;
			}
		});
	}

	@Before
	public void initializeEventManager() {
		eventManager = MockEventAuditManager.mock;
	}

	@After
	public void resetEventManagerMock() {
		Mockito.reset(eventManager);
	}

}