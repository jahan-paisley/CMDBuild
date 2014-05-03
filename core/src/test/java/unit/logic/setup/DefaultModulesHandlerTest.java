package unit.logic.setup;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.logic.setup.DefaultModulesHandler;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.logic.setup.SetupLogic.ModulesHandler;
import org.junit.Before;
import org.junit.Test;

public class DefaultModulesHandlerTest {

	private static final String MODULE_NAME = "foo";

	private ModulesHandler defaultModulesHandler;
	private DefaultModulesHandler modulesHandler;

	@Before
	public void setUp() throws Exception {
		defaultModulesHandler = mock(ModulesHandler.class);

		modulesHandler = new DefaultModulesHandler(defaultModulesHandler);
	}

	@Test
	public void noOverrides() {
		// when
		modulesHandler.get(MODULE_NAME);

		// then
		verify(defaultModulesHandler).get(MODULE_NAME);
		verifyNoMoreInteractions(defaultModulesHandler);
	}

	@Test
	public void overrideCalled() {
		// given
		final Module module = mock(Module.class);
		modulesHandler.override(MODULE_NAME, module);

		// when
		final Module getModule = modulesHandler.get(MODULE_NAME);

		// then
		assertThat(getModule, is(module));
		verifyNoMoreInteractions(defaultModulesHandler);
	}

}
