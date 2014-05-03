package org.cmdbuild.model.widget.service.soap;

import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Maps;

public class SoapRequest {

	public static class SoapRequestBuilder {

		private String namespacePrefix;
		private String namespaceUri;
		private String methodName;
		private final Map<String, String> parameters;

		private SoapRequestBuilder() {
			parameters = Maps.newHashMap();
		}

		public SoapRequestBuilder withNamespacePrefix(final String prefix) {
			this.namespacePrefix = prefix;
			return this;
		}

		public SoapRequestBuilder withNamespaceUri(final String uri) {
			this.namespaceUri = uri;
			return this;
		}

		public SoapRequestBuilder callingMethod(final String methodName) {
			this.methodName = methodName;
			return this;
		}

		public SoapRequestBuilder withParameters(final Map<String, String> parameters) {
			this.parameters.putAll(parameters);
			return this;
		}

		public SoapRequestBuilder withParameter(final String name, final String value) {
			this.parameters.put(name, value);
			return this;
		}

		public SoapRequest build() {
			Validate.notNull(methodName);
			Validate.notEmpty(methodName);
			validateNamespaceUriAndPrefix();
			return new SoapRequest(this);
		}

		private void validateNamespaceUriAndPrefix() {
			if (namespacePrefix != null && (namespaceUri == null || namespaceUri.isEmpty())) {
				throwException();
			}
		}

		private void throwException() {
			throw new IllegalArgumentException(
					"When you specify the namespace prefix, you have also to specify the uri");
		}

	}

	private final String namespacePrefix;
	private final String namespaceUri;
	private final String methodToBeCalled;
	private final Map<String, String> parameters;

	private SoapRequest(final SoapRequestBuilder builder) {
		this.methodToBeCalled = builder.methodName;
		this.parameters = builder.parameters;
		this.namespacePrefix = builder.namespacePrefix;
		this.namespaceUri = builder.namespaceUri;
	}

	public SOAPMessage create() throws SOAPException {
		final MessageFactory messageFactory = MessageFactory.newInstance();
		final SOAPMessage message = messageFactory.createMessage();
		final SOAPPart part = message.getSOAPPart();
		final SOAPEnvelope envelope = part.getEnvelope();
		final SOAPBody body = envelope.getBody();
		createBodyWithMethodAndParameters(body);
		message.saveChanges();
		return message;
	}

	private void createBodyWithMethodAndParameters(final SOAPBody body) throws SOAPException {
		final SOAPElement method = body.addChildElement(methodToBeCalled, namespacePrefix, namespaceUri);
		for (final String paramName : parameters.keySet()) {
			final String value = parameters.get(paramName);
			final SOAPElement paramTag = method.addChildElement(paramName);
			paramTag.addTextNode(value);
		}
	}

	public static SoapRequestBuilder newSoapRequest() {
		return new SoapRequestBuilder();
	}

}
