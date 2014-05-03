package integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.LocalSharkService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.AbstractLocalSharkServiceTest;

public class AdvanceActivityTest extends AbstractLocalSharkServiceTest {

	private static class ExtendedLocalSharkService extends LocalSharkService {

		public ExtendedLocalSharkService(final Config config) {
			super(config);
		}

		public void runButDontCloseActivityInstance(final String procInstId, final String actInstId)
				throws CMWorkflowException {
			new TransactedExecutor<Void>() {
				@Override
				protected Void command() throws CMWorkflowException {
					try {
						wapi().changeActivityInstanceState(handle(), procInstId, actInstId,
								WMActivityInstanceState.OPEN_RUNNING);
					} catch (final Exception e) {
						throw new CMWorkflowException(e);
					}
					return null;
				}
			}.execute();
		}
	}

	private static ExtendedLocalSharkService extendedWs;

	@BeforeClass
	public static void initWorkflowService() {
		extendedWs = new ExtendedLocalSharkService(new LocalSharkService.Config() {
			@Override
			public String getUsername() {
				return AbstractLocalSharkServiceTest.USERNAME;
			}
		});
		ws = extendedWs;
	}

	private XpdlProcess process;

	@Test
	public void activitiesStillRunningCanBeAdvanced() throws Exception {
		process = xpdlDocument.createProcess(randomName());
		process.createTransition( //
				process.createActivity("A"), //
				process.createActivity("B") //
		);
		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		final String actInstId = firstAndOnlyActivityInstanceId(procInstId);

		extendedWs.runButDontCloseActivityInstance(procInstId, actInstId);
		ws.advanceActivityInstance(procInstId, actInstId);
	}

	private String firstAndOnlyActivityInstanceId(final String procInstId) throws CMWorkflowException {
		final WSActivityInstInfo[] activities = ws.findOpenActivitiesForProcessInstance(procInstId);
		assertThat(activities.length, is(1));
		return activities[0].getActivityInstanceId();
	}

}
