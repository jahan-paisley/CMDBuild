package integration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static utils.EventManagerMatchers.isActivity;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Test;

import utils.AbstractLocalSharkServiceTest;
import utils.MockSharkWorkflowApiFactory;

public class InjectedApiTest extends AbstractLocalSharkServiceTest {

	private XpdlProcess process;

	@Before
	public void createProcess() throws Exception {
		process = xpdlDocument.createProcess(randomName());
	}

	@Test
	public void apiSuccessfullyCalled() throws Exception {
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, //
				"cmdb.newCard(\"Funny\")" //
						+ ".with(\"Code\", \"code\")" //
						+ ".create();");

		final XpdlActivity noImplActivity = process.createActivity(randomName());

		process.createTransition(scriptActivity, noImplActivity);

		uploadXpdlAndStartProcess(process);
		verify(eventManager).activityClosed(argThat(isActivity(scriptActivity)));

		verify(MockSharkWorkflowApiFactory.fluentApiExecutor).create(any(NewCard.class));
		verifyNoMoreInteractions(MockSharkWorkflowApiFactory.fluentApiExecutor);
	}

}
