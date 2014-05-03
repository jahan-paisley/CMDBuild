package org.cmdbuild.services.soap.connector;

import org.cmdbuild.logger.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

public class XmlConnectorParser implements ConnectorParser {

	private final String xmlString;

	public XmlConnectorParser(final String xmlString) {
		this.xmlString = xmlString;
	}

	@Override
	public Document parse() {
		Document document = null;
		try {
			document = DocumentHelper.parseText(xmlString);
		} catch (final DocumentException e) {
			Log.SOAP.error("Cannot parse the xml string");
		}
		return document;
	}

}
