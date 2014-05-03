package org.cmdbuild.common.template;

/**
 * Resolver for templates.
 */
public interface TemplateResolver {

	/**
	 * Resolves the specified template.
	 * 
	 * @param template
	 * 
	 * @return the resolved template.
	 */
	String resolve(String template);

}
