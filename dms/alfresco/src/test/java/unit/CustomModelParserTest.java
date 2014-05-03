package unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.alfresco.utils.CustomModelParser;
import org.junit.Before;
import org.junit.Test;

import utils.TestConfiguration;

public class CustomModelParserTest {

	private CustomModelParser customModelParser;

	@Before
	public void createParser() throws Exception {
		final DmsConfiguration configuration = new TestConfiguration();
		final String content = configuration.getAlfrescoCustomModelFileContent();
		final String prefix = configuration.getAlfrescoCustomPrefix();
		customModelParser = new CustomModelParser(content, prefix);
	}

	@Test
	public void readAspectsByType() throws Exception {
		final Map<String, List<String>> aspectsByType = customModelParser.getAspectsByType();

		final Collection<String> types = aspectsByType.keySet();
		assertThat(types, hasItem("CMDBuild SuperType"));
		assertThat(types, hasItem("A type"));
		assertThat(types, hasItem("Another type"));
		assertThat(types, hasItem("Document"));
		assertThat(types, hasItem("Image"));

		assertThat(aspectsByType.get("A type"), hasItem("foo"));
		assertThat(aspectsByType.get("A type"), hasItem("bar"));
		assertThat(aspectsByType.get("Another type"), hasItem("baz"));
		assertThat(aspectsByType.get("Document"), hasItems("documentStatistics", "taggable", "summary"));
		assertThat(aspectsByType.get("Document"), hasItem("summary"));
		assertThat(aspectsByType.get("Image"), hasItem("displayable"));
	}

	@Test
	public void readConstraints() throws Exception {
		final Map<String, List<String>> constraintsByType = customModelParser.getConstraintsByMetadata();
		assertThat(constraintsByType, hasEntry(equalTo("bazTextWithContraints"), hasItems("foo", "bar", "baz")));
	}

}
