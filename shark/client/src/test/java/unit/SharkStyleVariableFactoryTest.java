package unit;

import static org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory.VARIABLE_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;
import org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory.VariableSuffix;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.junit.Test;

public class SharkStyleVariableFactoryTest {

	XpdlExtendedAttributeVariableFactory variableFactory = new SharkStyleXpdlExtendedAttributeVariableFactory();

	@Test
	public void returnsNullForInvalidEntries() {
		assertNull(createVariable("Rubbish", "Foo"));
		assertNull(createVariable(VARIABLE_PREFIX + VariableSuffix.VIEW, null));
	}

	@Test
	public void returnsVariableName() {
		assertThat(createVariable(VARIABLE_PREFIX + VariableSuffix.VIEW, "VarName").getName(), is("VarName"));
	}

	@Test
	public void returnsVariableType() {
		assertThat(createVariable(VARIABLE_PREFIX + VariableSuffix.VIEW, "Foo").getType(), is(Type.READ_ONLY));
		assertThat(createVariable(VARIABLE_PREFIX + VariableSuffix.UPDATE, "Bar").getType(), is(Type.READ_WRITE));
		assertThat(createVariable(VARIABLE_PREFIX + VariableSuffix.UPDATEREQUIRED, "Baz").getType(),
				is(Type.READ_WRITE_REQUIRED));
	}

	/*
	 * Utils
	 */

	private CMActivityVariableToProcess createVariable(final String key, final String value) {
		return variableFactory.createVariable(new XpdlExtendedAttribute(key, value));
	}

}
