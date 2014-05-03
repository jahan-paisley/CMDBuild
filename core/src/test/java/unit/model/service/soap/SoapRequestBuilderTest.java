package unit.model.service.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.cmdbuild.model.widget.service.soap.SoapRequest;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;

public class SoapRequestBuilderTest {

	private static final String PREFIX = "it";
	private static final String URI = "http://it.example";
	private static final String METHOD_NAME = "method";
	private final Map<String, String> expectedParams = Maps.newHashMap();
	private static final String FIRST_PARAM_NAME = "first";
	private static final String SECOND_PARAM_NAME = "second";
	private static final String THIRD_PARAM_NAME = "third";

	@Before
	public void setUp() {
		expectedParams.put(FIRST_PARAM_NAME, "value1");
		expectedParams.put(SECOND_PARAM_NAME, "value2");
		expectedParams.put(THIRD_PARAM_NAME, "value3");
	}

	@Test
	public void shouldCreateRequestWithoutParameters() throws Exception {
		// given
		final SoapRequest request = SoapRequest.newSoapRequest() //
				.withNamespacePrefix(PREFIX) //
				.withNamespaceUri(URI) //
				.callingMethod(METHOD_NAME) //
				.build();

		// when
		final SOAPMessage message = request.create();

		// then
		final SOAPBody body = message.getSOAPBody();
		final Document document = body.extractContentAsDocument();
		final Node root = getDocumentRoot(document);
		assertEquals(0, root.getChildNodes().getLength());
	}

	@Test
	public void shouldCreateRequestWithoutNamespaceUriAndPrefix() throws Exception {
		// given
		final SoapRequest request = SoapRequest.newSoapRequest() //
				.callingMethod(METHOD_NAME) //
				.build();

		// when
		final SOAPMessage message = request.create();

		// then
		final SOAPBody body = message.getSOAPBody();
		final Document document = body.extractContentAsDocument();
		final Node methodTag = getDocumentRoot(document);
		assertEquals(METHOD_NAME, methodTag.getNodeName());
		assertNull(methodTag.getNamespaceURI());
		assertNull(methodTag.getPrefix());
	}

	@Test
	public void shouldCreateRequestWithParametersValue() throws Exception {
		// given
		final Map<String, String> params = Maps.newHashMap();
		params.put(SECOND_PARAM_NAME, "2nd_param");
		params.put(THIRD_PARAM_NAME, "3rd_param");
		final SoapRequest request = SoapRequest.newSoapRequest() //
				.callingMethod(METHOD_NAME) //
				.withParameter(FIRST_PARAM_NAME, "18") //
				.withParameters(params) //
				.build();

		// when
		final SOAPMessage message = request.create();
		final SOAPBody body = message.getSOAPBody();
		final Document document = body.extractContentAsDocument();
		final Node methodTag = getDocumentRoot(document);
		final NodeList parameters = methodTag.getChildNodes();

		// then
		assertEquals(3, methodTag.getChildNodes().getLength());
		assertEquals(METHOD_NAME, methodTag.getNodeName());
		for (int i = 0; i < parameters.getLength(); i++) {
			assertTrue(expectedParams.containsKey(parameters.item(i).getNodeName()));
		}
	}

	@Test
	public void shouldCreateRequestWithCorrectNamespaceUriAndPrefx() throws Exception {
		// given
		final SoapRequest request = SoapRequest.newSoapRequest() //
				.withNamespacePrefix(PREFIX) //
				.withNamespaceUri(URI) //
				.callingMethod(METHOD_NAME) //
				.build();

		// when
		final SOAPMessage message = request.create();
		message.writeTo(System.out);
		final SOAPBody body = message.getSOAPBody();
		final Document document = body.extractContentAsDocument();
		final Node methodTag = getDocumentRoot(document);

		// then
		assertEquals(URI, methodTag.getNamespaceURI());
		assertEquals(PREFIX, methodTag.getPrefix());
	}

	private Node getDocumentRoot(final Document document) {
		final NodeList nodeList = document.getChildNodes();
		if (nodeList.getLength() == 1) {
			return nodeList.item(0);
		}
		fail("There is more than one root or the document does not have a root");
		return null;
	}

}
