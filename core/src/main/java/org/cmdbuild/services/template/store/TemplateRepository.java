package org.cmdbuild.services.template.store;

public interface TemplateRepository {

	/**
	 * Returns the template associated with the specified name.
	 * 
	 * @param name
	 * 
	 * @return the template, {@code null} if not found.
	 */
	String getTemplate(String name);

}
