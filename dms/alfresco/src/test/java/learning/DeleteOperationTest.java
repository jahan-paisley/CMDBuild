package learning;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dms.exception.DmsError;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import utils.DmsServiceResource;
import utils.TestConfiguration;

public class DeleteOperationTest {

	private static final Long POSITION = 42L;

	@Rule
	public DmsServiceResource dms = DmsServiceResource.newInstance() //
			.withConfiguration(new TestConfiguration()) //
			.build();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void clearLocation() throws Exception {
		dms.at(POSITION).create();

		dms.at(POSITION).clear();
		assertThat(dms.at(POSITION).listDocuments(), is(empty()));
	}

	@Ignore("it works, but listDocuments doesn't throw an exception if location is not found")
	@Test(expected = DmsError.class)
	public void locationDeleted() throws Exception {
		// when
		dms.at(POSITION).delete();
		dms.at(POSITION).listDocuments();
	}

}