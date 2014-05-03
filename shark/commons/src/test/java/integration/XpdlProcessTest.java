package integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utils.XpdlTestUtils.randomName;

import java.util.List;

import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlActivitySet;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.enhydra.jxpdl.elements.DataType;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.jxpdl.elements.WorkflowProcess;
import org.junit.Test;

public class XpdlProcessTest extends AbstractXpdlTest {

	private static final String TEST_WP_ID = randomName();
	private static final String TEST_XA_KEY = randomName();
	private static final String TEST_XA_VALUE = randomName();

	@Test
	public void processIsCreatedWithTheIdProvided() {
		final Package pkg = xpdlDocument.getPkg();

		assertThat(pkg.getWorkflowProcesses().size(), is(0));
		assertThat(pkg.getWorkflowProcess(TEST_WP_ID), is(nullValue()));

		xpdlDocument.createProcess(TEST_WP_ID);

		assertThat(pkg.getWorkflowProcesses().size(), is(1));
		assertThat(pkg.getWorkflowProcess(TEST_WP_ID).getId(), is(TEST_WP_ID));
	}

	@Test
	public void processesCanBeListes() {
		xpdlDocument.createProcess("A");

		assertThat(xpdlDocument.findAllProcesses().size(), is(1));
		assertThat(xpdlDocument.findAllProcesses().get(0).getId(), is("A"));

		xpdlDocument.createProcess("B");
		xpdlDocument.createProcess("C");

		assertThat(xpdlDocument.findAllProcesses().size(), is(3));
	}

	@Test
	public void fieldsCanBeAddedToAProcess() {
		final XpdlProcess proc = xpdlDocument.createProcess(TEST_WP_ID);
		final WorkflowProcess wp = xpdlDocument.getPkg().getWorkflowProcess(TEST_WP_ID);

		assertThat(wp.getDataFields().size(), is(0));

		for (final StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			proc.addField(t.name(), t);
		}

		assertThat(wp.getDataFields().size(), is(StandardAndCustomTypes.values().length));
		for (final StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			final DataType dt = wp.getDataField(t.name()).getDataType();
			assertMatchesType(dt, t);
		}
		assertThat(xpdlDocument.getPkg().getDataFields().size(), is(0));
	}

	@Test
	public void extendedAttributesCanBeAddedToProcess() {
		final XpdlProcess proc = xpdlDocument.createProcess(TEST_WP_ID);
		final WorkflowProcess wp = xpdlDocument.getPkg().getWorkflowProcess(TEST_WP_ID);

		assertThat(wp.getExtendedAttributes().size(), is(0));
		assertThat(proc.getFirstExtendedAttributeValue(TEST_XA_KEY), is(nullValue()));

		proc.addExtendedAttribute(TEST_XA_KEY, TEST_XA_VALUE);

		assertThat(wp.getExtendedAttributes().size(), is(1));
		assertThat(wp.getExtendedAttributes().getFirstExtendedAttributeForName(TEST_XA_KEY).getVValue(),
				is(TEST_XA_VALUE));
		assertThat(proc.getFirstExtendedAttributeValue(TEST_XA_KEY), is(TEST_XA_VALUE));
	}

	@Test
	public void startActivitiesCanBeQueried() {
		final XpdlProcess xpdlProcess = xpdlDocument.createProcess(randomName());

		assertTrue(xpdlProcess.getStartActivities().isEmpty());
		assertTrue(xpdlProcess.getManualStartActivitiesRecursive().isEmpty());

		xpdlProcess.createActivity("A1");

		assertThat(xpdlProcess.getStartActivities().size(), is(1));
		assertThat(xpdlProcess.getManualStartActivitiesRecursive().size(), is(1));

		final XpdlActivitySet as2 = xpdlProcess.createActivitySet("AS2");
		as2.createActivity("A2.1");
		as2.createActivity("A2.2");
		xpdlProcess.createActivity("A2").setBlockType(as2);

		assertThat(xpdlProcess.getStartActivities().size(), is(2));
		assertThat(xpdlProcess.getManualStartActivitiesRecursive().size(), is(3));

		final XpdlActivitySet as23 = xpdlProcess.createActivitySet("AS2.3");
		as23.createActivity("A2.3.1");
		as23.createActivity("A2.3.2");
		as23.createActivity("A2.3.3");
		as2.createActivity("A2.3").setBlockType(as23);

		assertThat(xpdlProcess.getStartActivities().size(), is(2));
		assertThat(xpdlProcess.getManualStartActivitiesRecursive().size(), is(6));
	}

	@Test
	public void startActivitiesAreFoundEvenAfterStartEvents() {
		final XpdlProcess xpdlProcess = xpdlDocument.createProcess(randomName());
		XpdlActivity start = xpdlProcess.createActivity("S");
		start.setStartEventType();
		XpdlActivity noImpl = xpdlProcess.createActivity("NI");
		xpdlProcess.createTransition(start, noImpl);

		List<XpdlActivity> startActivities = xpdlProcess.getStartActivities();
		assertThat(startActivities.size(), is(1));
		assertThat(startActivities.get(0).getId(), is("S"));

		List<XpdlActivity> manualStartActivities = xpdlProcess.getManualStartActivitiesRecursive();
		assertThat(manualStartActivities.size(), is(1));
		assertThat(manualStartActivities.get(0).getId(), is("NI"));
	}


	@Test
	public void participantsAreQueriedFromProcessOrDocument() {
		final XpdlProcess xpdlProcess = xpdlDocument.createProcess(randomName());
		xpdlDocument.addRoleParticipant("DocumentRole");
		xpdlDocument.addSystemParticipant("DocumentSystem");
		xpdlProcess.addRoleParticipant("ProcessRole");

		assertTrue(xpdlProcess.hasRoleParticipant("DocumentRole"));
		assertTrue(xpdlProcess.hasRoleParticipant("ProcessRole"));
		assertFalse(xpdlProcess.hasRoleParticipant("DocumentSystem"));
	}
}
