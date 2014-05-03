package org.cmdbuild.common.template;

public class TemplateResolvers {

	private static class IdentityTemplateResolver implements TemplateResolver {

		@Override
		public String resolve(final String template) {
			return template;
		}

	}

	private static IdentityTemplateResolver IDENTITY_TEMPLATE_RESOLVER = new IdentityTemplateResolver();

	public static TemplateResolver identity() {
		return IDENTITY_TEMPLATE_RESOLVER;
	}

	private TemplateResolvers() {
		// prevents instantiation
	}

}
