package unit;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.clause.FunctionCall;
import org.junit.Test;

@SuppressWarnings({ "unchecked", "serial" })
public class FunctionCallTest {

	static private final Long USELESS_FUNCTION_ID = null;

	private static class IdentityAttributeType implements CMAttributeType<Object> {

		@Override
		public Object convertValue(final Object value) {
			return value;
		}

		@Override
		public void accept(final CMAttributeTypeVisitor visitor) {
			throw new UnsupportedOperationException();
		}

	}

	@Test(expected = NullPointerException.class)
	public void functionMustBeProvided() {
		FunctionCall.call(null, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void actualParametersMustMatchTheFunctionSignature() {
		final CMFunction function = mock(CMFunction.class);
		final List<CMFunctionParameter> inputParams = mock(List.class);
		when(inputParams.size()).thenReturn(2);
		when(function.getInputParameters()).thenReturn(inputParams);

		FunctionCall.call(function, new Object[1]);
	}

	@Test
	public void parametersCanBeSpecifiedAsAnArray() {
		final DBFunction function = new DBFunction("func", USELESS_FUNCTION_ID, true);
		function.addInputParameter("p1", new IdentityAttributeType());
		function.addInputParameter("p2", new IdentityAttributeType());

		final FunctionCall fc = call(function, "v1", 2);

		assertThat(fc.getParams().get(0), is((Object) "v1"));
		assertThat(fc.getParams().get(1), is((Object) 2));
	}

	@Test
	public void parametersCanBeSpecifiedAsAMap() {
		final DBFunction function = new DBFunction("func", USELESS_FUNCTION_ID, true);
		function.addInputParameter("p1", new IdentityAttributeType());
		function.addInputParameter("p2", new IdentityAttributeType());
		function.addInputParameter("p3", new IdentityAttributeType());

		final FunctionCall fc = call(function, new HashMap<String, Object>() {
			{
				put("p0", "v0"); // should be ignored
				put("p1", 1);
				// p2 not present
				put("p3", 3L);
			}
		});

		assertThat(fc.getParams().get(0), is((Object) 1));
		assertThat(fc.getParams().get(1), is(nullValue()));
		assertThat(fc.getParams().get(2), is((Object) 3L));
	}

}
