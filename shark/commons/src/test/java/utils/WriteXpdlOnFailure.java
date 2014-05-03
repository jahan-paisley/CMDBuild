package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class WriteXpdlOnFailure extends TestWatcher {

	/**
	 * Annotate tests with this annotation to print the XPDL on success as well.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WriteXpdl {
	}

	private final String tmpDir = System.getProperty("java.io.tmpdir");

	private final XpdlTest xpdlTest;

	public WriteXpdlOnFailure(final XpdlTest xpdlTest) {
		Validate.notNull(xpdlTest);
		this.xpdlTest = xpdlTest;
	}

	@Override
	protected void succeeded(final Description description) {
		if (description.getAnnotation(WriteXpdl.class) != null) {
			writeXpdl(description);
		}
	}

	@Override
	protected void failed(final Throwable e, final Description description) {
		writeXpdl(description);
	}

	private void writeXpdl(final Description description) {
		final XpdlDocument xpdl = xpdlTest.getXpdlDocument();
		if (xpdl != null) {
			try {
				final String fileName = getFileName(description);
				System.err.println("Saving XPDL from test to " + fileName);
				final FileOutputStream fos = new FileOutputStream(fileName);
				XpdlPackageFactory.writeXpdl(xpdl.getPkg(), fos);
			} catch (final Throwable t) {
				System.err.println("Cannot save XPDL: " + t.getMessage());
			}
		}
	}

	private String getFileName(final Description description) {
		final String testName = String.format("%s.%s", description.getClassName(), description.getMethodName());
		return String.format("%s%s%s.xpdl", tmpDir, File.separator, testName.replace(".", "_"));
	}

}
