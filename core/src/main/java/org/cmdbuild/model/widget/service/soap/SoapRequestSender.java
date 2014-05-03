package org.cmdbuild.model.widget.service.soap;

import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.widget.service.soap.exception.ConnectionException;
import org.slf4j.Logger;

public class SoapRequestSender {

	private static final Logger logger = Log.CMDBUILD;

	private final String endpointUrl;

	public SoapRequestSender(final String endpointUrl) {
		Validate.notEmpty(endpointUrl, "invalid endpoint's URL");
		this.endpointUrl = endpointUrl;
	}

	public SOAPMessage send(final SoapRequest request) throws ConnectionException {
		SOAPConnection connection = null;
		try {
			connection = SOAPConnectionFactory.newInstance() //
					.createConnection();
			logger.info("sending SOAP request to endpoint " + endpointUrl);
			final SOAPMessage requestMessage = dumpAndReturn(request.create());
			return dumpAndReturn(connection.call(requestMessage, this.endpointUrl));
		} catch (final SOAPException e) {
			final String message = new StringBuilder() //
					.append("Message send failed. Possible causes:").append(SystemUtils.LINE_SEPARATOR) //
					.append("1) service is not deployed").append(SystemUtils.LINE_SEPARATOR) //
					.append("2) URL and/or the port number of the endpoint are not correct") //
					.toString();
			logger.error(message, e);
			throw new ConnectionException(message, e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SOAPException e) {
					logger.error("error closing connection", e);
				}
			}
		}
	}

	/**
	 * Dumps the specified {@link SOAPMessage} and returns the
	 * {@link SOAPMessage} itself.
	 */
	private SOAPMessage dumpAndReturn(final SOAPMessage soapMessage) {
		try {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			soapMessage.writeTo(buffer);
			logger.trace(buffer.toString());
		} catch (final Exception e) {
			logger.trace("error dumping SOAP message");
		}
		return soapMessage;
	}
}
