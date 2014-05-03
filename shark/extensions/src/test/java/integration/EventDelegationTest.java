package integration;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static utils.EventManagerMatchers.isActivity;
import static utils.EventManagerMatchers.isProcess;
import static utils.XpdlTestUtils.randomName;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import utils.AbstractLocalSharkServiceTest;

public class EventDelegationTest extends AbstractLocalSharkServiceTest {

	private XpdlProcess process;

	@Before
	public void createBasicProcess() throws Exception {
		process = xpdlDocument.createProcess(randomName());
	}

	@Test
	public void startScriptAndStop() throws Exception {
		final XpdlActivity activity = process.createActivity(randomName());
		activity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		uploadXpdlAndStartProcess(process);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(argThat(isProcess(process)));
		inOrder.verify(eventManager).activityStarted(argThat(isActivity(activity)));
		inOrder.verify(eventManager).activityClosed(argThat(isActivity(activity)));
		inOrder.verify(eventManager).processClosed(argThat(isProcess(process)));
		verifyNoMoreInteractions(eventManager);
	}

	@Test
	public void startStopsAtFirstNoImplementationActivity() throws Exception {
		// order matters for this test
		final XpdlActivity noImplActivity = process.createActivity(randomName());
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);
		process.createTransition(scriptActivity, noImplActivity);

		uploadXpdlAndStartProcess(process);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(argThat(isProcess(process)));
		inOrder.verify(eventManager).activityStarted(argThat(isActivity(scriptActivity)));
		inOrder.verify(eventManager).activityClosed(argThat(isActivity(scriptActivity)));
		inOrder.verify(eventManager).activityStarted(argThat(isActivity(noImplActivity)));
		verifyNoMoreInteractions(eventManager);
	}

	@Test
	public void subflowStartAndStop() throws Exception {
		final XpdlProcess subprocess = xpdlDocument.createProcess(randomName());
		final XpdlActivity scriptActivity = subprocess.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		final XpdlActivity subflowActivity = process.createActivity(randomName());
		subflowActivity.setSubProcess(subprocess);

		uploadXpdlAndStartProcess(process);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(argThat(isProcess(process)));
		inOrder.verify(eventManager).activityStarted(argThat(isActivity(subflowActivity)));
		inOrder.verify(eventManager).processStarted(argThat(isProcess(subprocess)));
		inOrder.verify(eventManager).activityStarted(argThat(isActivity(scriptActivity)));
		inOrder.verify(eventManager).activityClosed(argThat(isActivity(scriptActivity)));
		inOrder.verify(eventManager).processClosed(argThat(isProcess(subprocess)));
		inOrder.verify(eventManager).activityClosed(argThat(isActivity(subflowActivity)));
		inOrder.verify(eventManager).processClosed(argThat(isProcess(process)));
		verifyNoMoreInteractions(eventManager);
	}

	@Test
	public void suspendResume() throws Exception {
		final XpdlActivity noImplementationActivity = process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		ws.suspendProcessInstance(procInstId);
		ws.resumeProcessInstance(procInstId);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(argThat(isProcess(process)));
		inOrder.verify(eventManager).activityStarted(argThat(isActivity(noImplementationActivity)));
		inOrder.verify(eventManager).processSuspended(argThat(isProcess(process)));
		inOrder.verify(eventManager).processResumed(argThat(isProcess(process)));
		verifyNoMoreInteractions(eventManager);
	}

}
