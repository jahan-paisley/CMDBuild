package org.cmdbuild.model.widget.service;

import org.w3c.dom.Document;

/**
 * Interface representing a generic external service (e.g. a web service) that
 * is invoked from a client
 */
public interface ExternalService {

	/**
	 * It invokes the external service
	 * 
	 * @return a Document object. It is a tree data structure that is an
	 *         abstraction of an XML document.
	 */
	Document invoke();

}
