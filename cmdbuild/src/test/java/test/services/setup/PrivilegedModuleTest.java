package test.services.setup;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.services.setup.PrivilegedModule;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class PrivilegedModuleTest {

	private Map<String, String> values;
	private Module module;
	private PrivilegeContext privilegeContext;
	private PrivilegedModule privilegedModule;

	@Before
	public void setUp() throws Exception {
		values = Maps.newHashMap();
		values.put("foo.bar.baz", "42");
		values.put("foo.bar.baz.password", "secr3t!");

		module = mock(Module.class);
		when(module.retrieve()) //
				.thenReturn(values);

		privilegeContext = mock(PrivilegeContext.class);

		privilegedModule = new PrivilegedModule(module, privilegeContext);
	}

	@Test
	public void storedDataHasNoPrivilegeManagement() throws Exception {
		// when
		privilegedModule.store(values);

		// then
		verify(module).store(values);
		verifyNoMoreInteractions(module, privilegeContext);
	}

	@Test
	public void administratorsCanSeeAll() throws Exception {
		// given
		when(privilegeContext.hasAdministratorPrivileges()) //
				.thenReturn(true);

		// when
		final Map<String, String> values = privilegedModule.retrieve();

		// then
		assertThat(values, hasKey("foo.bar.baz"));
		assertThat(values, hasKey("foo.bar.baz.password"));
		verify(module).retrieve();
		verify(privilegeContext, atLeast(1)).hasAdministratorPrivileges();
		verifyNoMoreInteractions(module, privilegeContext);
	}

	@Test
	public void nonAdministratorsCanSeeAllButPasswords() throws Exception {
		// given
		when(privilegeContext.hasAdministratorPrivileges()) //
				.thenReturn(false);

		// when
		final Map<String, String> values = privilegedModule.retrieve();

		// then
		assertThat(values, hasKey("foo.bar.baz"));
		assertThat(values, not(hasKey("foo.bar.baz.password")));
		verify(module).retrieve();
		verify(privilegeContext, atLeast(1)).hasAdministratorPrivileges();
		verifyNoMoreInteractions(module, privilegeContext);
	}

}
