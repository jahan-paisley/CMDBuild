package org.cmdbuild.model.widget.service.soap;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.widget.service.ExternalService;
import org.cmdbuild.model.widget.service.soap.SoapRequest.SoapRequestBuilder;
import org.cmdbuild.model.widget.service.soap.exception.ConnectionException;
import org.cmdbuild.model.widget.service.soap.exception.WebServiceException;
import org.w3c.dom.Document;

public class SoapService implements ExternalService {

	public static class SoapServiceBuilder {

		private final SoapRequestBuilder requestBuilder;
		private SoapRequestSender requestSender;

		private SoapServiceBuilder() {
			requestBuilder = SoapRequest.newSoapRequest();
		}

		/**
		 * Note that it must include also the port if it differs from 80
		 */
		public SoapServiceBuilder withEndpointUrl(final String endpointUrl) {
			requestSender = new SoapRequestSender(endpointUrl);
			return this;
		}

		public SoapServiceBuilder withNamespacePrefix(final String prefix) {
			requestBuilder.withNamespacePrefix(prefix);
			return this;
		}

		public SoapServiceBuilder withNamespaceUri(final String uri) {
			requestBuilder.withNamespaceUri(uri);
			return this;
		}

		public SoapServiceBuilder callingMethod(final String methodName) {
			requestBuilder.callingMethod(methodName);
			return this;
		}

		public SoapServiceBuilder withParameters(final Map<String, String> params) {
			requestBuilder.withParameters(params);
			return this;
		}

		public SoapServiceBuilder withParameter(final String name, final String value) {
			requestBuilder.withParameter(name, value);
			return this;
		}

		public SoapService build() {
			final SoapRequest request = requestBuilder.build();
			Validate.notNull(requestSender);
			return new SoapService(requestSender, request);
		}

	}

	private final SoapRequestSender sender;
	private final SoapRequest request;
	private static final String FAULT_NODE = "Fault";

	private SoapService(final SoapRequestSender sender, final SoapRequest request) {
		this.sender = sender;
		this.request = request;
	}

	@Override
	public Document invoke() throws ConnectionException, WebServiceException {
		try {
			final SOAPMessage response = sender.send(request);
			final SOAPBody responseBody = response.getSOAPBody();
			final Document document = responseBody.extractContentAsDocument();
			checkIfResponseContainsExceptions(document);
			return document;
		} catch (final SOAPException ex) {
			Log.CMDBUILD.error(ex.getMessage());
			return createNewEmptyDocument();
		}
	}

	private void checkIfResponseContainsExceptions(final Document document) throws WebServiceException {
		if (document.getChildNodes().item(0).getNodeName().contains(FAULT_NODE)) {
			throw new WebServiceException(
					"Check if the namespaces, the method name and the parameters of the web service are correct");
		}
	}

	private Document createNewEmptyDocument() {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (final ParserConfigurationException e) {
			Log.CMDBUILD.warn("Cannot create an empty Document response");
			return null;
		}
	}

	public static SoapServiceBuilder newSoapService() {
		return new SoapServiceBuilder();
	}

}
