package learning;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import utils.DmsServiceResource;
import utils.TestConfiguration;

public class UploadWithMetadataTest {

	private static class MetadataMatcher extends TypeSafeMatcher<Iterable<MetadataGroup>> {

		private final Metadata expectedMetadata;

		public MetadataMatcher(final Metadata metadata) {
			this.expectedMetadata = metadata;
		}

		@Override
		protected boolean matchesSafely(final Iterable<MetadataGroup> metadataGroups) {
			for (final MetadataGroup metadataGroup : metadataGroups) {
				for (final Metadata metadata : metadataGroup.getMetadata()) {
					if (expectedMetadata.getName().equals(metadata.getName())
							&& expectedMetadata.getValue().equals(metadata.getValue())) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void describeTo(final Description description) {
			description //
					.appendText(" contains a metadata with name ") //
					.appendValue(expectedMetadata.getName()) //
					.appendText(" and value ") //
					.appendValue(expectedMetadata.getValue());
		}

		public static TypeSafeMatcher<Iterable<MetadataGroup>> hasMetadata(final String name, final String value) {
			return new MetadataMatcher(new Metadata() {

				@Override
				public String getName() {
					return name;
				}

				@Override
				public String getValue() {
					return value;
				}

			});
		}

	}

	private static final Long CARD_ID = 42L;

	private static final String TEXT_GROUP_NAME = "documentStatistics";
	private static final String CHARACTERS_NAME = "characters";
	private static final String WORDS_NAME = "words";
	private static final String CHARACTERS_VALUE = "12345";
	private static final String WORDS_VALUE = "67890";

	private static final String SUMMARY_GROUP_NAME = "summary";
	private static final String SUMMARY_NAME = "summary";
	private static final String SUMMARY_VALUE = "this is the summary";

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
	public void writeAndReadFileWithMetadata() throws Exception {
		dms.at(CARD_ID).upload(tempFile(), testMetadata());
		assertThat(dms.at(CARD_ID).listDocuments(), hasSize(1));

		final Iterable<MetadataGroup> readedMetadata = readedTestMetadata();
		assertThat(readedMetadata, MetadataMatcher.hasMetadata(CHARACTERS_NAME, CHARACTERS_VALUE));
		assertThat(readedMetadata, MetadataMatcher.hasMetadata(WORDS_NAME, WORDS_VALUE));
		assertThat(readedMetadata, MetadataMatcher.hasMetadata(SUMMARY_NAME, SUMMARY_VALUE));
	}

	private List<MetadataGroup> testMetadata() {
		return Arrays.<MetadataGroup> asList(new MetadataGroup() {

			@Override
			public String getName() {
				return TEXT_GROUP_NAME;
			}

			@Override
			public Iterable<Metadata> getMetadata() {
				return Arrays.asList( //
						metadata(CHARACTERS_NAME, CHARACTERS_VALUE), //
						metadata(WORDS_NAME, WORDS_VALUE));
			}

		}, new MetadataGroup() {

			@Override
			public String getName() {
				return SUMMARY_GROUP_NAME;
			}

			@Override
			public Iterable<Metadata> getMetadata() {
				return Arrays.asList( //
						metadata(SUMMARY_NAME, SUMMARY_VALUE));
			}

		});
	}

	private Metadata metadata(final String name, final String value) {
		return new Metadata() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getValue() {
				return value;
			}

		};
	}

	private Iterable<MetadataGroup> readedTestMetadata() throws Exception {
		final StoredDocument storedDocument = dms.at(CARD_ID).listDocuments().get(0);
		return storedDocument.getMetadataGroups();
	}

}
