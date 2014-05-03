package unit.workflow;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ManageRelationWidgetFactory;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.junit.Ignore;
import org.junit.Test;

public class ManageRelationWidgetFactoryTest {

	private final ValuePairWidgetFactory factory;

	public ManageRelationWidgetFactoryTest() {
		factory = new ManageRelationWidgetFactory(mock(TemplateRepository.class), mock(Notifier.class),
				mock(CMDataView.class));
	}

	@Ignore
	@Test(expected = NullPointerException.class)
	public void missingClassNameThrowsException() throws Exception {
		factory.createWidget("IsDirect='true'\n", mock(CMValueSet.class));
	}

	@Test
	public void testSource() {
		ManageRelation w = createFrom("ClassName='foo'\nIsDirect='true'\n");
		assertThat(w.getSource(), is("_1"));

		w = createFrom("ClassName='foo'\nIsDirect='false'\n");
		assertThat(w.getSource(), is("_2"));

		w = createFrom("ClassName='foo'\nIsDirect='asdf asd'\n");
		assertThat(w.getSource(), is("_2"));

		w = createFrom("ClassName='foo'\n");
		assertThat(w.getSource(), is(nullValue()));
	}

	private ManageRelation createFrom(final String serialization) {
		return (ManageRelation) factory.createWidget(serialization, mock(CMValueSet.class));
	}

}
