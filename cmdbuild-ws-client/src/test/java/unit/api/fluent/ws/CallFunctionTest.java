package unit.api.fluent.ws;

import static java.util.Arrays.asList;
import static org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.wsAttribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.util.List;
import java.util.Map;

import org.cmdbuild.api.fluent.FunctionCall;
import org.cmdbuild.services.soap.Attribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CallFunctionTest extends AbstractWsFluentApiTest {

	private static final String FUNCTION_NAME = "function";

	private static final String IN_PARAMETER_1 = "foo";
	private static final String IN_PARAMETER_1_VALUE = randomString();
	private static final String IN_PARAMETER_2 = "bar";
	private static final String IN_PARAMETER_2_VALUE = randomString();
	private static final String OUT_PARAMETER_1 = "baz";
	private static final String OUT_PARAMETER_1_VALUE = randomString();

	@Captor
	private ArgumentCaptor<List<Attribute>> attributeListCaptor;

	private FunctionCall callFunction;

	@Before
	public void createExistingCard() throws Exception {
		callFunction = api() //
				.callFunction(FUNCTION_NAME) // \
				.with(IN_PARAMETER_1, IN_PARAMETER_1_VALUE) //
				.with(IN_PARAMETER_2, IN_PARAMETER_2_VALUE);
	}

	@Test
	public void parametersPassedToProxyWhenExecutingCallableFunction() throws Exception {
		when(proxy().callFunction( //
				anyString(), //
				anyListOf(Attribute.class)) //
		).thenReturn(asList(wsAttribute(OUT_PARAMETER_1, OUT_PARAMETER_1_VALUE)));

		callFunction.execute();

		verify(proxy()).callFunction( //
				eq(callFunction.getFunctionName()), //
				attributeListCaptor.capture());

		final List<Attribute> attributes = attributeListCaptor.getValue();
		assertThat(attributes, containsAttribute(IN_PARAMETER_1, IN_PARAMETER_1_VALUE));
		assertThat(attributes, containsAttribute(IN_PARAMETER_2, IN_PARAMETER_2_VALUE));

		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void outputAttributesAreConvertedToMapStringStringWhenExecutingCallableFunction() throws Exception {
		when(proxy().callFunction( //
				anyString(), //
				anyListOf(Attribute.class)) //
		).thenReturn(asList(wsAttribute(OUT_PARAMETER_1, OUT_PARAMETER_1_VALUE)));

		final Map<String, Object> outputs = callFunction.execute();
		assertThat(outputs.size(), equalTo(1));
		assertThat(outputs.get(OUT_PARAMETER_1), equalTo((Object) OUT_PARAMETER_1_VALUE));
	}

}
