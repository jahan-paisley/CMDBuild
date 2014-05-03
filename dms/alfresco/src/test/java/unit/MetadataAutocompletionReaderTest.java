package unit;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.MetadataAutocompletion;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.alfresco.utils.XmlAutocompletionReader;
import org.junit.Before;
import org.junit.Test;

import utils.TestConfiguration;

public class MetadataAutocompletionReaderTest {

	private MetadataAutocompletion.Reader reader;
	private AutocompletionRules autocompletionRules;

	@Before
	public void createParser() throws Exception {
		final DmsConfiguration configuration = new TestConfiguration();
		final String content = configuration.getMetadataAutocompletionFileContent();
		reader = new XmlAutocompletionReader(content);
		autocompletionRules = reader.read();
	}

	@Test
	public void groupNamesSuccessfullyParsed() throws Exception {
		assertThat(autocompletionRules.getMetadataGroupNames(), hasItems("aGroup", "anotherGroup"));
	}

	@Test
	public void metadataNamesSuccessfullyParsed() throws Exception {
		assertThat(autocompletionRules.getMetadataNamesForGroup("aGroup"), hasItems("foo", "bar"));
		assertThat(autocompletionRules.getMetadataNamesForGroup("anotherGroup"), hasItems("baz"));
	}

	@Test
	public void metadataRulesSuccessfullyParsed() throws Exception {
		final Map<String, String> fooRules = autocompletionRules.getRulesForGroupAndMetadata("aGroup", "foo");
		assertThat(fooRules, hasEntry("aClass", "foo"));
		assertThat(fooRules, hasEntry("anotherClass", "bar"));

		final Map<String, String> barRules = autocompletionRules.getRulesForGroupAndMetadata("aGroup", "bar");
		assertThat(barRules, hasEntry("anotherClass", "baz"));

		final Map<String, String> bazRules = autocompletionRules.getRulesForGroupAndMetadata("anotherGroup", "baz");
		assertThat(bazRules, hasEntry("anotherClass", "foo"));
	}

}
