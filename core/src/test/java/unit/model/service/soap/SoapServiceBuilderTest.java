package unit.model.service.soap;

import static org.junit.Assert.assertNotNull;

import org.cmdbuild.model.widget.service.soap.SoapService;
import org.junit.Test;

public class SoapServiceBuilderTest {

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfUrlIsNull() throws Exception {
		// given
		final String nullUrl = null;

		// when
		SoapService.newSoapService() //
				.withEndpointUrl(nullUrl) //
				.withNamespacePrefix("p") //
				.withNamespaceUri("http://localhost") //
				.callingMethod("method").build();

	}

	@Test
	public void namespacePrefixAndUriAreOptional() throws Exception {
		// given
		final String url = "http://example:8080/";

		// when
		final SoapService service = SoapService.newSoapService() //
				.withEndpointUrl(url) //
				.callingMethod("method") //
				.build();

		// then
		assertNotNull(service);

	}

	@Test(expected = IllegalArgumentException.class)
	public void ifNamespacePrefixIsSpecifiedAlsoNamespaceUriMustBeSpecified() throws Exception {
		// given
		final String url = "http://example:8080/";

		// when
		SoapService.newSoapService() //
				.withEndpointUrl(url) //
				.withNamespacePrefix("it") //
				.callingMethod("method") //
				.build();

	}

	@Test
	public void ifNamespaceUriIsSpecifiedNamespacePrefixIsOptional() throws Exception {
		// given
		final String url = "http://example:8080/";

		// when
		final SoapService service = SoapService.newSoapService() //
				.withEndpointUrl(url) //
				.withNamespaceUri("http://it.example") //
				.callingMethod("method") //
				.build();

		// then
		assertNotNull(service);

	}

	@Test(expected = NullPointerException.class)
	public void methodNameCannotBeNull() throws Exception {
		// given
		final String url = "http://example:8080/";

		// when
		SoapService.newSoapService() //
				.withEndpointUrl(url) //
				.withNamespaceUri("http://it.example") //
				.callingMethod(null) //
				.build();

	}

	@Test(expected = IllegalArgumentException.class)
	public void methodNameCannotBeEmpty() throws Exception {
		// given
		final String url = "http://example:8080/";

		// when
		SoapService.newSoapService() //
				.withEndpointUrl(url) //
				.withNamespaceUri("http://it.example") //
				.callingMethod("") //
				.build();

	}

}
