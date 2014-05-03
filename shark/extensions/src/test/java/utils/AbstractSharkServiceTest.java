package utils;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static utils.XpdlTestUtils.randomName;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.client.utilities.SharkInterfaceWrapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;

public class AbstractSharkServiceTest implements XpdlTest {

	protected static AbstractSharkService ws;
	protected XpdlDocument xpdlDocument;

	@Rule
	public TestRule testWatcher = new WriteXpdlOnFailure(this);

	@Before
	public void createXpdlDocument() throws Exception {
		xpdlDocument = newXpdl(randomName());
	}

	@Override
	public XpdlDocument getXpdlDocument() {
		return xpdlDocument;
	}

	/**
	 * Returns the WAPI connection to Shark.
	 * 
	 * Shark should have been already initialized by the
	 * {@link AbstractSharkService}.
	 * 
	 * @return Shark WAPI interface
	 * @throws Exception
	 */
	protected final WAPI wapi() throws Exception {
		return SharkInterfaceWrapper.getShark().getWAPIConnection();
	}

	/*
	 * Utils
	 */

	/**
	 * Creates a new {@link XpdlDocument} with default scripting language
	 * {@code ScriptLanguages.JAVA}.
	 */
	protected XpdlDocument newXpdl(final String packageId) throws XpdlException {
		final XpdlDocument xpdl = newXpdlNoScriptingLanguage(packageId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		return xpdl;
	}

	/**
	 * Creates a new {@link XpdlDocument} with no default scripting language.
	 */
	protected XpdlDocument newXpdlNoScriptingLanguage(final String packageId) throws XpdlException {
		return new XpdlDocument(packageId);
	}

	/**
	 * Uploads the {@link XpdlDocument} and starts the specified
	 * {@link XpdlProcess}.
	 * 
	 * @return the process instance's info
	 */
	protected WSProcessInstInfo uploadXpdlAndStartProcess(final XpdlProcess xpdlProcess) throws CMWorkflowException,
			XpdlException {
		upload(xpdlDocument);
		return startProcess(xpdlProcess);
	}

	/**
	 * Uploads an {@link XpdlDocument}.
	 * 
	 * @throws CMWorkflowException
	 * @throws XpdlException
	 */
	protected void upload(final XpdlDocument xpdlDocument) throws XpdlException, CMWorkflowException {
		ws.uploadPackage(xpdlDocument.getPackageId(), serialize(xpdlDocument));
	}

	/**
	 * Uploads an XPDL resource give its name.
	 * 
	 * @throws Exception
	 * @return the uploaded package
	 */
	protected Package uploadXpdlResource(final String resourceName) throws Exception {
		final URL url = ClassLoader.getSystemResource(resourceName);
		assertThat(url, not(nullValue()));

		final File file = new File(url.toURI());
		final byte[] data = FileUtils.readFileToByteArray(file);

		final ByteArrayInputStream is = new ByteArrayInputStream(data);
		final Package pkg = XpdlPackageFactory.readXpdl(is);

		ws.uploadPackage(pkg.getId(), data);

		return pkg;
	}

	/**
	 * Starts the specified {@link XpdlProcess}.
	 * 
	 * @return the process instance's info
	 */
	protected WSProcessInstInfo startProcess(final XpdlProcess xpdlProcess) throws CMWorkflowException, XpdlException {
		return startProcess(xpdlProcess.getId());
	}

	/**
	 * Starts the specified process Id.
	 * 
	 * @return the process instance's info
	 */
	protected WSProcessInstInfo startProcess(final String processId) throws CMWorkflowException, XpdlException {
		return ws.startProcess(xpdlDocument.getPackageId(), processId);
	}

	/**
	 * Serializes an {@link XpdlDocument} in a byte array.
	 */
	protected byte[] serialize(final XpdlDocument xpdl) throws XpdlException {
		return XpdlPackageFactory.xpdlByteArray(xpdl.getPkg());
	}

	/**
	 * Returns the instance variables for the specified process instance.
	 * 
	 * @param processInstInfo
	 * @return the instance variables for the specified process instance
	 * @throws CMWorkflowException
	 */
	protected Map<String, Object> instanceVariablesForProcess(final WSProcessInstInfo processInstInfo)
			throws CMWorkflowException {
		return ws.getProcessInstanceVariables(processInstInfo.getProcessInstanceId());
	}

}