package unit.logic.taskmanager;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.MapperEngine;
import org.cmdbuild.logic.taskmanager.EngineBasedMapper;
import org.cmdbuild.logic.taskmanager.EngineBasedMapper.Builder;
import org.cmdbuild.logic.taskmanager.Mapper;
import org.junit.Test;

public class EngineBasedMapperTest {

	@Test(expected = NullPointerException.class)
	public void textIsMandatory() throws Exception {
		// given
		final MapperEngine engine = mock(MapperEngine.class);
		final Builder builder = EngineBasedMapper.newInstance() //
				.withEngine(engine);

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void engineIsMandatory() throws Exception {
		// given
		final Builder builder = EngineBasedMapper.newInstance() //
				.withText(null);

		// when
		builder.build();
	}

	@Test
	public void textCanBeEmpty() throws Exception {
		// given
		final MapperEngine engine = mock(MapperEngine.class);
		final Builder builder = EngineBasedMapper.newInstance() //
				.withText(" ") //
				.withEngine(engine);

		// when
		builder.build();
	}

	@Test
	public void textCanBeBlank() throws Exception {
		// given
		final MapperEngine engine = mock(MapperEngine.class);
		final Builder builder = EngineBasedMapper.newInstance() //
				.withText(" \t") //
				.withEngine(engine);

		// when
		builder.build();
	}

	@Test
	public void keyValueTextSuccessfullyProcessed() throws Exception {
		// given
		final String text = "" //
				+ "<key_init>foo</key_end> ... <value_init>FOO</value_end>   \n" //
				+ "... <key_init>bar</key_end><value_init>BAR</value_end>\n" //
				+ "<key_init>baz</key_end><value_init>BAZ</value_end>...\n" //
				+ "";
		final Mapper processor = EngineBasedMapper.newInstance() //
				.withText(text) //
				.withEngine(KeyValueMapperEngine.newInstance() //
						.withKey("key_init", "key_end") //
						.withValue("value_init", "value_end") //
						.build()) //
				.build();

		// when
		final Map<String, String> processed = processor.map();

		// then
		assertThat(processed, hasEntry("foo", "FOO"));
		assertThat(processed, hasEntry("bar", "BAR"));
		assertThat(processed, hasEntry("baz", "BAZ"));
	}

}
