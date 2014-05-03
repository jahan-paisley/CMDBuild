package unit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.SingleActivityWidgetFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;
import org.junit.Test;

public class ValuePairXpdlExtendedAttributeWidgetFactoryTest {

	private final ValuePairXpdlExtendedAttributeWidgetFactory widgetFactory;

	public ValuePairXpdlExtendedAttributeWidgetFactoryTest() {
		widgetFactory = new ValuePairXpdlExtendedAttributeWidgetFactory();
	}

	@Test
	public void createReturnsNullOnUnsupportedWidgets() {
		assertNull(createWidget("Unsupported", "Something"));
	}

	@Test
	public void createDelegatesCreationToTheSpecificWidgetFactory() {
		final SingleActivityWidgetFactory aFactory = mock(SingleActivityWidgetFactory.class);
		final CMActivityWidget aWidget = addFactoryReturningWidget(aFactory, "A");

		assertNull(createWidget("A", "Serialization"));
		assertNull(createWidget(null, "Serialization"));

		widgetFactory.addWidgetFactory(aFactory);

		assertThat(createWidget("A", "Serialization"), is(aWidget));

		verify(aFactory, times(1)).createWidget(eq("Serialization"), any(CMValueSet.class));
	}

	@Test
	public void widgetSerializationCannotBeNull() {
		final SingleActivityWidgetFactory aFactory = mock(SingleActivityWidgetFactory.class);
		addFactoryReturningWidget(aFactory, "A");

		widgetFactory.addWidgetFactory(aFactory);

		assertNull(createWidget("A", null));

		verify(aFactory, never()).createWidget(anyString(), any(CMValueSet.class));
	}

	/*
	 * Utils
	 */

	private CMActivityWidget addFactoryReturningWidget(final SingleActivityWidgetFactory aFactory, final String name) {
		final CMActivityWidget aWidget = mock(CMActivityWidget.class);
		when(aFactory.getWidgetName()).thenReturn(name);
		when(aFactory.createWidget(anyString(), any(CMValueSet.class))).thenReturn(aWidget);
		return aWidget;
	}

	private CMActivityWidget createWidget(final String key, final String value) {
		return widgetFactory.createWidget(new XpdlExtendedAttribute(key, value), mock(CMValueSet.class));
	}

}
