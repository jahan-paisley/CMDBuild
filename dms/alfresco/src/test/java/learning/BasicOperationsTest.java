package learning;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utils.DmsServiceResource.AUTHOR;
import static utils.DmsServiceResource.CATEGORY;
import static utils.DmsServiceResource.DESCRIPTION;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import utils.DmsServiceResource;
import utils.TestConfiguration;

public class BasicOperationsTest {

	private static final Long CARD_ID = 42L;
	private static final String CATEGORY_WITH_SPACES = "Category with spaces";
	private static final String SAMPLE_CONTENT = "sample content for uploaded file";

	@Rule
	public DmsServiceResource dms = DmsServiceResource.newInstance() //
			.withConfiguration(new TestConfiguration()) //
			.build();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	protected File tempFile() throws IOException {
		final File file = temporaryFolder.newFile();
		FileUtils.writeStringToFile(file, SAMPLE_CONTENT);
		return file;
	}

	@Before
	public void clearLocation() throws Exception {
		dms.at(CARD_ID).clear();
		assertThat(dms.at(CARD_ID).listDocuments(), is(empty()));
	}

	@Test
	public void fileUploadedAndDeleted() throws Exception {
		assertTrue(dms.at(CARD_ID).listDocuments().isEmpty());
		dms.at(CARD_ID).upload(tempFile());
		assertThat(dms.at(CARD_ID).listDocuments(), hasSize(1));
		dms.at(CARD_ID).delete(dms.at(CARD_ID).listDocuments().get(0).getName());
		assertTrue(dms.at(CARD_ID).listDocuments().isEmpty());
	}

	@Test
	public void uploadedFileSuccessfullyQueried() throws Exception {
		final File file = tempFile();
		assertTrue(dms.at(CARD_ID).listDocuments().isEmpty());
		dms.at(CARD_ID).upload(file);
		assertThat(dms.at(CARD_ID).listDocuments(), hasSize(1));

		final StoredDocument storedDocument = dms.at(CARD_ID).listDocuments().get(0);
		assertThat(storedDocument.getAuthor(), equalTo(AUTHOR));
		assertThat(storedDocument.getCategory(), equalTo(CATEGORY));
		assertThat(storedDocument.getDescription(), equalTo(DESCRIPTION));
		assertThat(storedDocument.getName(), equalTo(file.getName()));

		dms.at(CARD_ID).delete(storedDocument.getName());
		assertTrue(dms.at(CARD_ID).listDocuments().isEmpty());
	}

	@Test(expected = DmsError.class)
	public void deleteMissingFileThrowsExeption() throws Exception {
		dms.at(CARD_ID).delete(tempFile().getName());
	}

	@Test
	public void categoryWithSpacesAreAllowed() throws Exception {
		assertTrue(dms.at(CARD_ID).listDocuments().isEmpty());
		dms.at(CARD_ID).upload(tempFile(), CATEGORY_WITH_SPACES);
		assertThat(dms.at(CARD_ID).listDocuments(), hasSize(1));

		final StoredDocument storedDocument = dms.at(CARD_ID).listDocuments().get(0);
		assertThat(storedDocument.getCategory(), equalTo(CATEGORY_WITH_SPACES));
	}

}
