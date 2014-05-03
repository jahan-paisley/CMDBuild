package unit.template.engine;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.junit.Test;

public class EngineBasedTemplateResolverTest {

	private static final Object NULL_OBJECT = null;

	@Test
	public void aSimpleStringIsKeptAsItIs() {
		// given
		final String template = "A simple string";
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance().build();

		// when
		final String value = tr.resolve(template);

		assertThat(value, equalTo(template));
	}

	@Test
	public void inexistentEngineIsExpandedWithNull() {
		// given
		final Engine engine = mock(Engine.class);
		when(engine.eval(anyString())).thenReturn(null);

		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance().build();

		// when
		final String value = tr.resolve("{e1:param}");

		// then
		assertThat(value, equalTo(String.valueOf(NULL_OBJECT)));
	}

	@Test
	public void inexistentVariablesAreExpandedWithNull() {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("param", "value"), "e1") //
				.build();

		// when
		final String value = tr.resolve("{e1:inexsistent}");

		// then
		assertThat(value, equalTo(String.valueOf(NULL_OBJECT)));
	}

	@Test
	public void simpleVariablesAreExpandedWithEnginesEvaluation() {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("stringParam", "string param"), "e1") //
				.withEngine(engineWithParam("integerParam", Integer.valueOf(42)), "e2") //
				.build();

		// when
		final String stringValue = tr.resolve("{e1:stringParam}");
		final String integerValue = tr.resolve("{e2:integerParam}");

		// then
		assertThat(stringValue, equalTo("string param"));
		assertThat(integerValue, equalTo(String.valueOf(Integer.valueOf(42))));
	}

	@Test
	public void leadingPartsAreKeptIntact() {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("param", 42), "e1") //
				.build();

		// when
		final String value = tr.resolve("XXX{e1:param}");

		// then
		assertThat(value, equalTo(format("XXX%s", 42)));
	}

	@Test
	public void moreThanOneParameterIsExpanded() {
		// given
		final Object value1 = 42, value2 = "st";

		final Engine engine = mock(Engine.class);
		when(engine.eval("param1")).thenReturn(value1);
		when(engine.eval("param2")).thenReturn(value2);

		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engine, "e1") //
				.build();

		// when
		final String value = tr.resolve("XXX{e1:param1}YYY{e1:param2}ZZZ");

		// then
		assertThat(value, equalTo(format("XXX%sYYY%sZZZ", value1, value2)));
	}

	/*
	 * Utilities
	 */

	private Engine engineWithParam(final String name, final Object value) {
		final Engine engine = mock(Engine.class);
		when(engine.eval(name)).thenReturn(value);
		return engine;
	}

}
