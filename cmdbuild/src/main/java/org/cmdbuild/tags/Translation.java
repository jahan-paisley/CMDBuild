package org.cmdbuild.tags;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.TranslationService;

public class Translation extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String key = null;

	public void setKey(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int doStartTag() {
		try {
			final JspWriter out = pageContext.getOut();
			final String lang = applicationContext().getBean(LanguageStore.class).getLanguage();
			out.println(TranslationService.getInstance().getTranslation(lang, key));
		} catch (final IOException e) {
			Log.CMDBUILD.debug("Error printing translation: " + key, e);
		}

		return Tag.SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return Tag.EVAL_PAGE;
	}
}
