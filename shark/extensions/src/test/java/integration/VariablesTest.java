package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static utils.EventManagerMatchers.isActivity;
import static utils.EventManagerMatchers.isProcess;
import static utils.XpdlTestUtils.randomName;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Test;

import utils.AbstractLocalSharkServiceTest;

public class VariablesTest extends AbstractLocalSharkServiceTest {

	private static final String A_BOOLEAN = "aBoolean";
	private static final String AN_INTEGER = "anInteger";
	private static final String A_STRING = "aString";
	private static final String A_DATE = "aDate";
	private static final String A_LOOKUP = "aLookup";
	private static final String A_FLOAT = "aFloat";

	private static final String UNDEFINED = "undefined";

	private static final String A_REFERENCE = "reference";

	private XpdlProcess process;

	@Before
	public void createBasicProcess() throws Exception {
		process = xpdlDocument.createProcess(randomName());

		process.addField(A_BOOLEAN, StandardAndCustomTypes.BOOLEAN);
		process.addField(AN_INTEGER, StandardAndCustomTypes.INTEGER);
		process.addField(A_STRING, StandardAndCustomTypes.STRING);
		process.addField(A_DATE, StandardAndCustomTypes.DATETIME);
		process.addField(A_FLOAT, StandardAndCustomTypes.FLOAT);

		xpdlDocument.createCustomTypeDeclarations();
		process.addField(A_REFERENCE, StandardAndCustomTypes.REFERENCE);
		process.addField(A_LOOKUP, StandardAndCustomTypes.LOOKUP);
	}

	@Test
	public void variablesModifiedFromScript() throws Exception {
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, "aBoolean = true; anInteger = 42; aString = \"foo\";");

		final XpdlActivity noImplActivity = process.createActivity(randomName());

		process.createTransition(scriptActivity, noImplActivity);

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).activityClosed(argThat(isActivity(scriptActivity)));

		final Map<String, Object> variables = ws.getProcessInstanceVariables(procInstId);

		assertThat((Boolean) variables.get(A_BOOLEAN), equalTo(true));
		assertThat((Long) variables.get(AN_INTEGER), equalTo(42L));
		assertThat((String) variables.get(A_STRING), equalTo("foo"));
	}

	@Test
	public void variableModifiedFromMultipleScripts() throws Exception {
		final XpdlActivity firstScriptActivity = process.createActivity(randomName());
		firstScriptActivity.setScriptingType(ScriptLanguage.JAVA, "anInteger = 1;");

		final XpdlActivity secondScriptActivity = process.createActivity(randomName());
		secondScriptActivity.setScriptingType(ScriptLanguage.JAVA, "anInteger++;");

		final XpdlActivity noImplActivity = process.createActivity(randomName());

		process.createTransition(firstScriptActivity, secondScriptActivity);
		process.createTransition(secondScriptActivity, noImplActivity);

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).activityClosed(argThat(isActivity(firstScriptActivity)));

		final Map<String, Object> variables = ws.getProcessInstanceVariables(procInstId);

		assertThat((Long) variables.get(AN_INTEGER), equalTo(2L));
	}

	@Test
	public void undefinedVariableSettedThenRead() throws Exception {
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).processStarted(argThat(isProcess(process)));

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put(UNDEFINED, "baz");
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat((String) readVariables.get(UNDEFINED), equalTo("baz"));
	}

	@Test
	public void localVariablesSettedWithinScriptCannotBeRead() throws Exception {
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, "the_answer = 42;");

		final XpdlActivity noImplActivity = process.createActivity(randomName());

		process.createTransition(scriptActivity, noImplActivity);

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).activityClosed(argThat(isActivity(scriptActivity)));

		final Map<String, Object> variables = ws.getProcessInstanceVariables(procInstId);

		assertThat(variables.get("the_answer"), equalTo(null));
	}

	@Test
	public void declareTypesSettedThenRead() throws Exception {
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).processStarted(argThat(isProcess(process)));

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put(A_REFERENCE, newReference(42));
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat(readVariables.get(A_REFERENCE), hasProperty("id", equalTo(42)));
	}

	@Test
	public void declaredVariablesAreInitializedWithDefaultValuesOrEmptyConstructors() throws Exception {
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).processStarted(argThat(isProcess(process)));
		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);

		assertThat((Boolean) readVariables.get(A_BOOLEAN), equalTo(false));
		assertThat((Long) readVariables.get(AN_INTEGER), equalTo(0L));
		assertThat(readVariables.get(A_DATE), is(notNullValue()));
		assertThat((Double) readVariables.get(A_FLOAT), equalTo(0.0));

		assertThat((ReferenceType) readVariables.get(A_REFERENCE), equalTo(new ReferenceType()));
		assertThat((LookupType) readVariables.get(A_LOOKUP), equalTo(new LookupType()));
	}

	/*
	 * Utils
	 */

	private ReferenceType newReference(final int id) {
		final ReferenceType reference = new ReferenceType();
		reference.setId(id);
		return reference;
	}

}
