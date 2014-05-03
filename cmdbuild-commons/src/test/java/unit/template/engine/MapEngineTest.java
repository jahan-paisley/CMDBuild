package unit.template.engine;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.common.template.engine.Engines;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class MapEngineTest {

	private HashMap<String, Object> parameterMap;
	private Engine engine;

	@Before
	public void setUp() throws Exception {
		parameterMap = Maps.newHashMap();
		engine = Engines.map(parameterMap);
	}

	@Test
	public void evaluatesToNullIfParameterNotPresent() {
		// when
		final Object value = engine.eval("Any Name");

		// then
		assertThat(value, is(nullValue()));
	}

	@Test
	public void evaluatesValuesAsTheyWerePut() {
		// given
		final String aString = "A string";
		final Integer anInteger = Integer.valueOf(42);
		final Long aLong = Long.valueOf(123456789L);
		final Object anObject = new Object();
		parameterMap.put("string", aString);
		parameterMap.put("integer", anInteger);
		parameterMap.put("long", aLong);
		parameterMap.put("object", anObject);

		// when
		final Object stringValue = engine.eval("string");
		final Object integerValue = engine.eval("integer");
		final Object longValue = engine.eval("long");
		final Object objectValue = engine.eval("object");

		// then
		assertThat(stringValue, equalTo((Object) aString));
		assertThat(integerValue, equalTo((Object) anInteger));
		assertThat(longValue, equalTo((Object) aLong));
		assertThat(objectValue, equalTo(anObject));
	}

}
