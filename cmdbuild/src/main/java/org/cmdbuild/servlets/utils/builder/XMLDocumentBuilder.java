package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * XMLDocumentBuilder parse the httprequest input stream into an xml file.
 */
public class XMLDocumentBuilder extends AbstractParameterBuilder<Document> {

	public Document build(HttpServletRequest r) throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.getInputStream());
	}

}
