package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlApplication;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.enhydra.shark.api.internal.toolagent.ToolAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.AbstractLocalSharkServiceTest;
import utils.DummyToolAgent;

public class ConditionalToolAgentTest extends AbstractLocalSharkServiceTest {

	@Before
	public void intializeDummyToolAgent() throws Exception {
		DummyToolAgent.init();
		when(DummyToolAgent.conditionEvaluator.evaluate()).thenReturn(true);
	}

	@After
	public void resetMock() throws Exception {
		reset(DummyToolAgent.listener);
	}

	@Test
	public void statusIsRunningAtBeginning() throws Exception {
		startProcess();
		verify(DummyToolAgent.listener).invoked();
		verify(DummyToolAgent.listener).startingStatus(ToolAgent.APP_STATUS_RUNNING);
	}

	@Test
	public void statusIsFinishedIfNoError() throws Exception {
		startProcess();
		verify(DummyToolAgent.listener).invoked();
		assertThat(DummyToolAgent._status, equalTo(ToolAgent.APP_STATUS_FINISHED));
	}

	@Test
	public void statusIsInvalidOnErrorAndHigherLevelExceptionIsExpected() throws Exception {
		try {
			DummyToolAgent.throwException = true;
			startProcess();
			fail();
		} catch (final CMWorkflowException e) {
			verify(DummyToolAgent.listener).invoked();
			assertThat(DummyToolAgent._status, equalTo(ToolAgent.APP_STATUS_INVALID));
		}
	}

	@Test
	public void toolAgentInvokedWhenConditionEvaluedTrue() throws Exception {
		startProcess();
		verify(DummyToolAgent.listener).invoked();
	}

	@Test
	public void toolAgentInvokedWhenConditionEvaluedFalse() throws Exception {
		reset(DummyToolAgent.conditionEvaluator);
		when(DummyToolAgent.conditionEvaluator.evaluate()).thenReturn(false);
		startProcess();
		verify(DummyToolAgent.listener, never()).invoked();
	}

	private void startProcess() throws Exception {
		final XpdlProcess process = xpdlDocument.createProcess(randomName());

		final XpdlApplication application = process.createApplication(randomName());
		application.addExtendedAttribute("ToolAgentClass", DummyToolAgent.class.getName());

		final XpdlActivity activity = process.createActivity(randomName());
		activity.setTaskApplication(application.getId());

		uploadXpdlAndStartProcess(process);
	}

}
