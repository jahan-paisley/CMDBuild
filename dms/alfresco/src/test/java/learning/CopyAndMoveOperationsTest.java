package learning;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.cmdbuild.dms.StoredDocument;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import utils.DmsServiceResource;
import utils.TestConfiguration;

public class CopyAndMoveOperationsTest {

	private static final Long SOURCE_CARD_ID = 42L;
	private static final Long TARGET_CARD_ID = 123L;

	@Rule
	public DmsServiceResource dms = DmsServiceResource.newInstance() //
			.withConfiguration(new TestConfiguration()) //
			.build();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void clearLocation() throws Exception {
		dms.at(SOURCE_CARD_ID).create();
		dms.at(TARGET_CARD_ID).create();

		dms.at(SOURCE_CARD_ID).clear();
		assertThat(dms.at(SOURCE_CARD_ID).listDocuments(), is(empty()));

		dms.at(TARGET_CARD_ID).clear();
		assertThat(dms.at(TARGET_CARD_ID).listDocuments(), is(empty()));
	}

	@Test
	public void fileMoved() throws Exception {
		// given
		final File file = temporaryFolder.newFile("foo");
		dms.at(SOURCE_CARD_ID).upload(file);

		// when
		dms.at(SOURCE_CARD_ID).move("foo", TARGET_CARD_ID);

		// then
		final Iterable<StoredDocument> sourceDocuments = dms.at(SOURCE_CARD_ID).listDocuments();
		assertThat(size(sourceDocuments), is(0));

		final Iterable<StoredDocument> targetDocuments = dms.at(TARGET_CARD_ID).listDocuments();
		assertThat(size(targetDocuments), is(1));
		assertThat(get(targetDocuments, 0).getName(), equalTo("foo"));
	}

	@Test
	public void fileCopied() throws Exception {
		// given
		final File file = temporaryFolder.newFile("foo");
		dms.at(SOURCE_CARD_ID).upload(file);

		// when
		dms.at(SOURCE_CARD_ID).copy("foo", TARGET_CARD_ID);

		// then
		final Iterable<StoredDocument> sourceDocuments = dms.at(SOURCE_CARD_ID).listDocuments();
		assertThat(size(sourceDocuments), is(1));

		final Iterable<StoredDocument> targetDocuments = dms.at(TARGET_CARD_ID).listDocuments();
		assertThat(size(targetDocuments), is(1));
		assertThat(get(targetDocuments, 0).getName(), equalTo("foo"));
	}

}