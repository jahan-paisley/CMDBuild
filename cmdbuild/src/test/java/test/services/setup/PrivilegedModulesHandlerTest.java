package test.services.setup;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.logic.setup.SetupLogic.ModulesHandler;
import org.cmdbuild.services.setup.PrivilegedModule;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.junit.Before;
import org.junit.Test;

public class PrivilegedModulesHandlerTest {

	private ModulesHandler moduleHandler;
	private PrivilegedModulesHandler privilegedModuleHandler;

	@Before
	public void setUp() throws Exception {
		moduleHandler = mock(ModulesHandler.class);

		final PrivilegeContext privilegeContext = mock(PrivilegeContext.class);

		privilegedModuleHandler = new PrivilegedModulesHandler(moduleHandler, privilegeContext);
	}

	@Test
	public void returnedModuleIsWrapped() throws Exception {
		// given
		final Module module = mock(Module.class);
		when(moduleHandler.get("foo")) //
				.thenReturn(module);

		// when
		final Module read = privilegedModuleHandler.get("foo");

		// then
		assertThat(read, instanceOf(PrivilegedModule.class));
	}

	@Test
	public void returnedModuleIsNotWrappedIfSkipped() throws Exception {
		// given
		final Module module = mock(Module.class);
		when(moduleHandler.get("foo")) //
				.thenReturn(module);
		privilegedModuleHandler.skipPrivileges("foo");

		// when
		final Module read = privilegedModuleHandler.get("foo");

		// then
		assertThat(read, not(instanceOf(PrivilegedModule.class)));
	}

}
