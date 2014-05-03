package unit.workflow;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ManageEmailWidgetFactory;
import org.junit.Before;
import org.junit.Test;

public class ManageEmailWidgetFactoryTest {

	private static final EmailLogic UNUSED_EMAIL_LOGIC = null;

	private ManageEmailWidgetFactory factory;

	private EmailTemplateLogic emailTemplateLogic;

	@Before
	public void setUp() {
		emailTemplateLogic = mock(EmailTemplateLogic.class);
		factory = new ManageEmailWidgetFactory( //
				mock(TemplateRepository.class), //
				mock(Notifier.class), //
				UNUSED_EMAIL_LOGIC, //
				emailTemplateLogic);
	}

	@Test
	public void singleEmailTemplateDefinition() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //
				"CCAddresses='cc@a.a'\n" + //
				"Subject='the subject'\n" + //
				"Content='the content'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), is(1));
		final ManageEmail.EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), is("to@a.a"));
		assertThat(t.getCcAddresses(), is("cc@a.a"));
		assertThat(t.getSubject(), is("the subject"));
		assertThat(t.getContent(), is("the content"));
	}

	@Test
	public void moreThanOneEmailTemplateDefinitions() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //
				"CCAddresses='cc@a.a'\n" + //
				"Subject='the subject'\n" + //
				"Content='the content'\n" + //

				"ToAddresses1='to@a.a 1'\n" + //
				"CCAddresses1='cc@a.a 1'\n" + //
				"Subject1='the subject 1'\n" + //
				"Content1='the content 1'\n" + //

				"Content2='the content 2'\n" + //

				"Condition3='condition'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), is(4));

		ManageEmail.EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), is("to@a.a"));
		assertThat(t.getCcAddresses(), is("cc@a.a"));
		assertThat(t.getSubject(), is("the subject"));
		assertThat(t.getContent(), is("the content"));

		t = w.getEmailTemplates().get(1);
		assertThat(t.getToAddresses(), is("to@a.a 1"));
		assertThat(t.getCcAddresses(), is("cc@a.a 1"));
		assertThat(t.getSubject(), is("the subject 1"));
		assertThat(t.getContent(), is("the content 1"));

		t = w.getEmailTemplates().get(2);
		assertThat(t.getContent(), is("the content 2"));

		t = w.getEmailTemplates().get(3);
		assertThat(t.getCondition(), is("condition"));
	}

	@Test
	public void readAlsoTheTemplates() {
		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"ToAddresses='to@a.a'\n" + //

				"ToAddresses1='to@a.a 1'\n" + //

				"Ashibabalea='from Ashi when baba={client:lea}'\n" + //

				"Foo='Bar'\n", //
				mock(CMValueSet.class));

		assertThat(w.getEmailTemplates().size(), is(2));

		final Map<String, String> templates = w.getTemplates();
		assertThat(templates.keySet().size(), is(2));
		assertThat(templates.get("Ashibabalea"), is("from Ashi when baba={client:lea}"));
		assertThat(templates.get("Foo"), is("Bar"));
	}

	@Test
	public void emailTemplateApplied() {
		final EmailTemplateLogic.Template template = mock(EmailTemplateLogic.Template.class);
		when(template.getTo()).thenReturn("to@example.com");
		when(template.getCc()).thenReturn("cc@example.com");
		when(template.getSubject()).thenReturn("the subject");
		when(template.getBody()).thenReturn("the body");
		when(emailTemplateLogic.read("foo")) //
				.thenReturn(template);

		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"Template='foo'\n", //
				mock(CMValueSet.class));

		verify(emailTemplateLogic).read("foo");

		assertThat(w.getEmailTemplates().size(), is(1));
		final ManageEmail.EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), is("to@example.com"));
		assertThat(t.getCcAddresses(), is("cc@example.com"));
		assertThat(t.getSubject(), is("the subject"));
		assertThat(t.getContent(), is("the body"));
	}

	@Test
	public void multipleEmailTemplatesApplied() {
		final EmailTemplateLogic.Template foo = mock(EmailTemplateLogic.Template.class, "foo");
		when(foo.getTo()).thenReturn("foo_to@example.com");
		when(foo.getCc()).thenReturn("foo_cc@example.com");
		when(foo.getSubject()).thenReturn("subject of foo");
		when(foo.getBody()).thenReturn("content of foo");
		final EmailTemplateLogic.Template bar = mock(EmailTemplateLogic.Template.class, "bar");
		when(bar.getTo()).thenReturn("bar_to@example.com");
		when(bar.getCc()).thenReturn("bar_cc@example.com");
		when(bar.getSubject()).thenReturn("subject of bar");
		when(bar.getBody()).thenReturn("content of bar");
		when(emailTemplateLogic.read("foo")) //
				.thenReturn(foo);
		when(emailTemplateLogic.read("bar")) //
				.thenReturn(bar);

		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"Template1='foo'\n" + //
				"Template2='bar'\n", //
				mock(CMValueSet.class));

		verify(emailTemplateLogic, times(2)).read(anyString());

		assertThat(w.getEmailTemplates().size(), is(2));

		// needs to be sorted since we don't know how they are internally sorted
		final List<ManageEmail.EmailTemplate> emailTemplates = w.getEmailTemplates();
		Collections.sort(emailTemplates, new Comparator<ManageEmail.EmailTemplate>() {
			@Override
			public int compare(final ManageEmail.EmailTemplate o1, final ManageEmail.EmailTemplate o2) {
				return o1.getToAddresses().compareTo(o2.getToAddresses());
			};
		});

		final ManageEmail.EmailTemplate t0 = w.getEmailTemplates().get(0);
		assertThat(t0.getToAddresses(), is("bar_to@example.com"));
		assertThat(t0.getCcAddresses(), is("bar_cc@example.com"));
		assertThat(t0.getSubject(), is("subject of bar"));
		assertThat(t0.getContent(), is("content of bar"));

		final ManageEmail.EmailTemplate t1 = w.getEmailTemplates().get(1);
		assertThat(t1.getToAddresses(), is("foo_to@example.com"));
		assertThat(t1.getCcAddresses(), is("foo_cc@example.com"));
		assertThat(t1.getSubject(), is("subject of foo"));
		assertThat(t1.getContent(), is("content of foo"));
	}

	@Test
	public void emailTemplateCanBeOverrideInSomeParts() {
		final EmailTemplateLogic.Template template = mock(EmailTemplateLogic.Template.class);
		when(template.getTo()).thenReturn("to@example.com");
		when(template.getCc()).thenReturn("cc@example.com");
		when(template.getSubject()).thenReturn("the subject");
		when(template.getBody()).thenReturn("the content");
		when(emailTemplateLogic.read("foo")) //
				.thenReturn(template);

		final ManageEmail w = (ManageEmail) factory.createWidget(EMPTY + //
				"Template='foo'\n" + //
				"ToAddresses='lol@example.com'\n" + //
				"Subject='rotfl'\n", //
				mock(CMValueSet.class));

		verify(emailTemplateLogic).read("foo");

		assertThat(w.getEmailTemplates().size(), is(1));
		final ManageEmail.EmailTemplate t = w.getEmailTemplates().get(0);
		assertThat(t.getToAddresses(), is("lol@example.com"));
		assertThat(t.getCcAddresses(), is("cc@example.com"));
		assertThat(t.getSubject(), is("rotfl"));
		assertThat(t.getContent(), is("the content"));
	}

}
