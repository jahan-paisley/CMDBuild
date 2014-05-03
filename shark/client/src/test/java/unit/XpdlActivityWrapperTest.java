package unit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlActivityWrapper;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeWidgetFactory;
import org.junit.Test;

public class XpdlActivityWrapperTest {

	private final XpdlActivity xpdlActivity;
	private final XpdlExtendedAttributeWidgetFactory widgetFactory;
	private final XpdlActivityWrapper wrapper;
	private final XpdlExtendedAttributeVariableFactory variableFactory;

	public XpdlActivityWrapperTest() {
		final XpdlDocument doc = new XpdlDocument("PKG");
		xpdlActivity = doc.createProcess("PRO").createActivity("ACT");
		widgetFactory = mock(XpdlExtendedAttributeWidgetFactory.class);
		variableFactory = mock(XpdlExtendedAttributeVariableFactory.class);
		wrapper = new XpdlActivityWrapper(xpdlActivity, variableFactory, widgetFactory);
	}

	@Test
	public void extractsNoVariablesOrWidgetsIfNoExtendedAttributes() {
		assertThat(wrapper.getVariables().size(), is(0));
		assertThat(wrapper.getWidgets().size(), is(0));
		verify(widgetFactory, never()).createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class));
		verify(variableFactory, never()).createVariable(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void extractsNoVariablesForInvalidEntries() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");

		assertThat(wrapper.getVariables().size(), is(0));
		verify(variableFactory, times(2)).createVariable(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void variablesAreExtracted() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");
		when(variableFactory.createVariable(any(XpdlExtendedAttribute.class))).thenReturn(notNullVariableToProcess());

		assertThat(wrapper.getVariables().size(), is(2));
		verify(variableFactory, times(2)).createVariable(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void extractsNoWidgetsForInvalidEntries() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");

		assertThat(wrapper.getWidgets().size(), is(0));
		verify(widgetFactory, times(2)).createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class));
	}

	@Test
	public void widgetsAreExtracted() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");
		when(widgetFactory.createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class))).thenReturn(
				notNullWidget());

		assertThat(wrapper.getWidgets().size(), is(2));
		verify(widgetFactory, times(2)).createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class));
	}

	/*
	 * Utils
	 */

	private CMActivityVariableToProcess notNullVariableToProcess() {
		return new CMActivityVariableToProcess("FakeName", Type.READ_ONLY);
	}

	private CMActivityWidget notNullWidget() {
		return mock(CMActivityWidget.class);
	}
}
