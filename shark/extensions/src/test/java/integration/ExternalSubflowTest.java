package integration;

import static org.cmdbuild.common.collect.Factory.entry;
import static org.cmdbuild.common.collect.Factory.linkedHashMapOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static utils.EventManagerMatchers.hasActivityDefinitionId;
import static utils.EventManagerMatchers.hasProcessDefinitionId;

import org.cmdbuild.workflow.SimpleEventManager.ActivityInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import utils.AbstractLocalSharkServiceTest;

@SuppressWarnings("unchecked")
public class ExternalSubflowTest extends AbstractLocalSharkServiceTest {

	private static final String PARENT_VARIABLE = "ParentVariable";
	private static final String PARENT_PACKAGE = "parent";
	private static final String PARENT_PROCESS = "Parent";
	private static final String CHILD_PACKAGE = "child";
	private static final String CHILD_PROCESS = "Child";
	private static final String CHILD_INPUT_VARIABLE = "FormalIn";
	private static final String CHILD_OUTPUT_VARIABLE = "FormalOut";

	private final ArgumentCaptor<ActivityInstance> activityInstanceCaptor = ArgumentCaptor
			.forClass(ActivityInstance.class);

	@Before
	public void uploadPackages() throws Exception {
		uploadXpdlResource("xpdl/Child.xpdl");
		uploadXpdlResource("xpdl/Parent.xpdl");
	}

	@Test
	public void spawnChildProcess() throws Exception {
		final String procInstId = ws.startProcess(PARENT_PACKAGE, PARENT_PROCESS).getProcessInstanceId();
		final String actInstId = ws.findOpenActivitiesForProcessInstance(procInstId)[0].getActivityInstanceId();

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(argThat(hasProcessDefinitionId(PARENT_PROCESS)));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("ParentStart")));

		ws.setProcessInstanceVariables(procInstId, linkedHashMapOf(entry(PARENT_VARIABLE, "Something")));
		ws.advanceActivityInstance(procInstId, actInstId);

		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("GiveBirth")));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("ChildStart")));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("CopyInputToOutput")));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("ChildEnd")));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("VerifyVariable")));

		assertThat(ws.getProcessInstanceVariables(procInstId).get(PARENT_VARIABLE),
				is(equalTo((Object) "Copy of Something")));
	}

	@Test
	public void startChildProcessDirectly() throws Exception {
		final String procInstId = ws.startProcess(CHILD_PACKAGE, CHILD_PROCESS).getProcessInstanceId();
		final String actInstId = ws.findOpenActivitiesForProcessInstance(procInstId)[0].getActivityInstanceId();

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(argThat(hasProcessDefinitionId(CHILD_PROCESS)));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("ChildStart")));
		inOrder.verify(eventManager).activityStarted(argThat(hasActivityDefinitionId("ChildUserActivity")));

		ws.setProcessInstanceVariables(procInstId, linkedHashMapOf(entry(CHILD_INPUT_VARIABLE, "Something else")));
		ws.advanceActivityInstance(procInstId, actInstId);

		assertThat(ws.getProcessInstanceVariables(procInstId).get(CHILD_OUTPUT_VARIABLE),
				is(equalTo((Object) "Copy of Something else")));
	}

	/*
	 * Utils
	 */

	protected ActivityInstance activityInstanceCapturer() {
		return activityInstanceCaptor.capture();
	}

	protected ActivityInstance capturedActivityInstance() {
		return activityInstanceCaptor.getValue();
	}
}
