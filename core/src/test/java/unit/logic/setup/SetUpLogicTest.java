package unit.logic.setup;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.logic.setup.SetupLogic;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.logic.setup.SetupLogic.ModulesHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Maps;

public class SetUpLogicTest {

	private static String MODULE_NAME = "foo";

	private Module module;
	private ModulesHandler modulesHandler;
	private SetupLogic setUpLogic;

	@Before
	public void setUp() throws Exception {
		module = mock(Module.class);

		modulesHandler = mock(ModulesHandler.class);
		when(modulesHandler.get(MODULE_NAME)) //
				.thenReturn(module);

		setUpLogic = new SetupLogic(modulesHandler);
	}

	@Test
	public void load() throws Exception {
		// when
		setUpLogic.load(MODULE_NAME);

		// then
		final InOrder inOrder = inOrder(modulesHandler, module);
		inOrder.verify(modulesHandler).get(MODULE_NAME);
		inOrder.verify(module).retrieve();
		verifyNoMoreInteractions(modulesHandler, module);
	}

	@Test
	public void save() throws Exception {
		// given
		final Map<String, String> values = Maps.newHashMap();

		// when
		setUpLogic.save(MODULE_NAME, values);

		// then
		final InOrder inOrder = inOrder(modulesHandler, module);
		inOrder.verify(modulesHandler).get(MODULE_NAME);
		inOrder.verify(module).store(values);
		verifyNoMoreInteractions(modulesHandler, module);
	}

}
