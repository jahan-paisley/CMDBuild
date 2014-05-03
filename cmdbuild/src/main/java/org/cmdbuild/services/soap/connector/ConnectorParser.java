package org.cmdbuild.services.soap.connector;

import org.dom4j.Document;

public interface ConnectorParser {

	/**
	 * Parses a string and returns a Document (a tree with informations about
	 * tags, attributes and content)
	 * 
	 * @param string
	 * @return
	 */
	Document parse();

}
