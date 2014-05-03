package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.DataType;
import org.enhydra.jxpdl.elements.ExternalReference;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.jxpdl.elements.Participant;
import org.enhydra.jxpdl.elements.TypeDeclaration;
import org.junit.Test;

public class XpdlDocumentTest extends AbstractXpdlTest {

	private static final String TEST_ROLE_PARTICIPANT_ID = randomName();
	private static final String TEST_SYSTEM_PARTICIPANT_ID = randomName();

	@Test
	public void createdPackageHasNameAndXpdlVersion() {
		final Package pkg = xpdlDocument.getPkg();

		assertThat(pkg.getId(), is(TEST_PKG_ID));
		assertThat(xpdlDocument.getPackageId(), is(TEST_PKG_ID));
		assertThat(pkg.getPackageHeader().getXPDLVersion(), is("2.1"));

		assertThat(pkg.getName(), is(""));
		assertThat(pkg.getApplications().size(), is(0));
		assertThat(pkg.getParticipants().size(), is(0));
		assertThat(pkg.getPools().size(), is(0));
		assertThat(pkg.getWorkflowProcesses().size(), is(0));
	}

	@Test
	public void itDoesNotAlterThePassedPackage() {
		final Package emptyPkg = new Package();
		final Package passedPkg = new Package();

		assertThat(emptyPkg, is(equalTo(passedPkg)));
		assertThat(emptyPkg, is(not(sameInstance(passedPkg))));

		xpdlDocument = new XpdlDocument(passedPkg);

		assertThat(emptyPkg, is(equalTo(passedPkg)));
		assertThat(xpdlDocument.getPkg(), is(sameInstance(passedPkg)));
	}

	@Test
	public void customTypesCanBeAdded() {
		xpdlDocument.createCustomTypeDeclarations();
		final Package pkg = xpdlDocument.getPkg();

		for (final StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			if (t.isCustom()) {
				TypeDeclaration tdec = pkg.getTypeDeclarations().getTypeDeclaration(t.getDeclaredTypeId());
				assertThat(tdec.getDataTypes().getChoosen(), is(instanceOf(ExternalReference.class)));
				assertThat(tdec.getDataTypes().getExternalReference().getLocation(), is(t.getDeclaredTypeLocation()));

				tdec = pkg.getTypeDeclarations().getTypeDeclaration(
						t.getDeclaredTypeId() + XpdlDocument.ARRAY_DECLARED_TYPE_NAME_SUFFIX);
				assertThat(tdec.getDataTypes().getChoosen(), is(instanceOf(ExternalReference.class)));
				assertThat(tdec.getDataTypes().getExternalReference().getLocation(), is(t.getDeclaredTypeLocation()
						+ XpdlDocument.ARRAY_DECLARED_TYPE_LOCATION_SUFFIX));
			}
		}
	}

	@Test
	public void defaultScriptLanguageCanBeAdded() {
		xpdlDocument.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		final Package pkg = xpdlDocument.getPkg();

		assertThat(pkg.getScript().getType(), is("text/java"));
	}

	@Test
	public void fieldsCanBeAddedToThePackage() {
		final Package pkg = xpdlDocument.getPkg();

		assertThat(pkg.getDataFields().size(), is(0));

		for (final StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			xpdlDocument.addPackageField(t.name(), t);
		}

		assertThat(pkg.getDataFields().size(), is(StandardAndCustomTypes.values().length));
		for (final StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			final DataType dt = pkg.getDataField(t.name()).getDataType();
			assertMatchesType(dt, t);
		}
	}

	@Test
	public void participantsCanBeAddedToThePackage() {
		final Package pkg = xpdlDocument.getPkg();

		assertThat(pkg.getParticipants().size(), is(0));

		xpdlDocument.addRoleParticipant(TEST_ROLE_PARTICIPANT_ID);
		xpdlDocument.addSystemParticipant(TEST_SYSTEM_PARTICIPANT_ID);

		assertThat(pkg.getParticipants().size(), is(2));
		Participant p = pkg.getParticipant(TEST_ROLE_PARTICIPANT_ID);
		assertThat(p.getId(), is(TEST_ROLE_PARTICIPANT_ID));
		assertThat(p.getParticipantType().getType(), is(XPDLConstants.PARTICIPANT_TYPE_ROLE));

		p = pkg.getParticipant(TEST_SYSTEM_PARTICIPANT_ID);
		assertThat(p.getId(), is(TEST_SYSTEM_PARTICIPANT_ID));
		assertThat(p.getParticipantType().getType(), is(XPDLConstants.PARTICIPANT_TYPE_SYSTEM));

		assertTrue(xpdlDocument.hasRoleParticipant(TEST_ROLE_PARTICIPANT_ID));
		assertFalse(xpdlDocument.hasRoleParticipant(TEST_SYSTEM_PARTICIPANT_ID));
	}

}
