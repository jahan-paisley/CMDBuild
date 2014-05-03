package integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.client.wfservice.PackageInvalid;
import org.junit.Test;

import utils.AbstractLocalSharkServiceTest;

public class XpdlHandlingTest extends AbstractLocalSharkServiceTest {

	@Test
	public void definitionsCannotBeRubbish() throws XpdlException, CMWorkflowException {
		try {
			ws.uploadPackage(xpdlDocument.getPackageId(), new byte[0]);
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause().getMessage(), containsString("The package byte[] representation can't be parsed"));
		}
	}

	@Test
	public void definitionsMustHaveDefaultScriptingLanguage() throws XpdlException, CMWorkflowException {
		try {
			upload(newXpdlNoScriptingLanguage(xpdlDocument.getPackageId()));
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause(), instanceOf(PackageInvalid.class));
			final PackageInvalid sharkException = (PackageInvalid) we.getCause();
			assertThat(sharkException.getMessage(), containsString("Error in package"));
			assertThat(sharkException.getXPDLValidationErrors(), containsString("Unsupported script language"));
		}
		xpdlDocument.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		upload(xpdlDocument);
	}

	@Test
	public void packageVersionIncreasesWithEveryUpload() throws CMWorkflowException {
		String[] versions = ws.getPackageVersions(xpdlDocument.getPackageId());
		assertEquals(0, versions.length);

		upload(xpdlDocument);

		versions = ws.getPackageVersions(xpdlDocument.getPackageId());
		assertThat(versions, is(new String[] { "1" }));

		upload(xpdlDocument);
		upload(xpdlDocument);
		versions = ws.getPackageVersions(xpdlDocument.getPackageId());
		assertThat(versions, is(new String[] { "1", "2", "3" }));
	}

	@Test
	public void anyPackageVersionCanBeDownloaded() throws CMWorkflowException {
		Package pkg = xpdlDocument.getPkg();

		pkg.setName("n1");
		upload(xpdlDocument);

		pkg.setName("n2");
		upload(xpdlDocument);

		pkg.setName("n3");
		upload(xpdlDocument);

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(xpdlDocument.getPackageId(), "1"));
		assertThat(pkg.getName(), is("n1"));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(xpdlDocument.getPackageId(), "3"));
		assertThat(pkg.getName(), is("n3"));
	}

	@Test
	public void xpdl1PackagesAreNotConvertedToXpdl2() throws CMWorkflowException {
		Package pkg = xpdlDocument.getPkg();

		pkg.getPackageHeader().setXPDLVersion("1.0");
		upload(xpdlDocument);

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(xpdlDocument.getPackageId(), "1"));
		assertThat(pkg.getPackageHeader().getXPDLVersion(), is("1.0"));
	}

	@Test
	public void canDownloadAllPackages() throws CMWorkflowException {
		final String ID1 = randomName();
		final String ID2 = randomName();
		final int initialSize = ws.downloadAllPackages().length;

		upload(newXpdl(ID1));

		assertThat(ws.downloadAllPackages().length, is(initialSize + 1));

		upload(newXpdl(ID2));
		upload(newXpdl(ID2));

		assertThat(ws.downloadAllPackages().length, is(initialSize + 2));
	}

}
