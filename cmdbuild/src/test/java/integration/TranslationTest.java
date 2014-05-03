package integration;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.services.Settings;
import org.cmdbuild.services.TranslationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TranslationTest {

	private final String lang;

	private static TranslationService ts;

	static {
		final String webRoot = System.getProperty("user.dir").concat("/src/main/webapp/"); // TODO
		Settings.getInstance().setRootPath(webRoot);
		ts = TranslationService.getInstance();
	}

	@Parameters
	public static Collection<Object[]> data() {
		final List<Object[]> data = new ArrayList<Object[]>();

		for (final String lang : ts.getTranslationList().keySet()) {
			final Object[] params = { lang };
			data.add(params);
		}
		return data;
	}

	public TranslationTest(final String lang) {
		this.lang = lang;
	}

	@Test
	public void translationsAreValidJSON() {
		assertThat("The traslation " + lang + " is wrong", ts.getTranslationObject(lang).length(), is(not(equalTo(0))));
	}
}
