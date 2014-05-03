package integration;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.enhydra.jxpdl.elements.BasicType;
import org.enhydra.jxpdl.elements.DataType;
import org.enhydra.jxpdl.elements.DataTypes;
import org.enhydra.jxpdl.elements.DeclaredType;
import org.junit.Before;

import utils.XpdlTest;

public abstract class AbstractXpdlTest implements XpdlTest {

	protected final String TEST_PKG_ID = randomName();
	protected XpdlDocument xpdlDocument;

	@Before
	public void createDocument() {
		xpdlDocument = new XpdlDocument(TEST_PKG_ID);
	}

	@Override
	public XpdlDocument getXpdlDocument() {
		return xpdlDocument;
	}

	protected static void assertMatchesType(final DataType dt, final StandardAndCustomTypes t) {
		final DataTypes dataTypes = dt.getDataTypes();
		if (t.isCustom()) {
			assertThat(dataTypes.getChoosen(), is(instanceOf(DeclaredType.class)));
			assertThat(dataTypes.getDeclaredType().getId(), is(t.getDeclaredTypeId()));
		} else {
			assertThat(dataTypes.getChoosen(), is(instanceOf(BasicType.class)));
			assertThat(dataTypes.getBasicType().getType(), is(t.name()));
		}
	}

}
