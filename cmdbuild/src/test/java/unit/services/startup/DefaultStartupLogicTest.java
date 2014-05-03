package unit.services.startup;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.startup.DefaultStartupLogic;
import org.cmdbuild.services.startup.StartupManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultStartupLogicTest {

	private DefaultStartupLogic startupLogic;

	private StartupManager startupManager;
	private PatchManager patchManager;
	private CachingLogic cachingLogic;

	@Before
	public void setUp() throws Exception {
		startupManager = mock(StartupManager.class);
		patchManager = mock(PatchManager.class);
		cachingLogic = mock(CachingLogic.class);

		startupLogic = new DefaultStartupLogic(startupManager, patchManager, cachingLogic);
	}

	@Test
	public void earlyStartIsForStartupManagerOnly() throws Exception {
		// when
		startupLogic.earlyStart();

		// then
		verify(startupManager).start();
		verifyNoMoreInteractions(startupManager, patchManager, cachingLogic);
	}

	@Test
	public void migrationRequiredIsForPatchManagerOnly() throws Exception {
		// when
		startupLogic.migrationRequired();

		// then
		verify(patchManager).isUpdated();
		verifyNoMoreInteractions(startupManager, patchManager, cachingLogic);
	}

	@Test
	public void migrationIsRequiredWhenPatchManagerIsNotUpdated() throws Exception {
		when(patchManager.isUpdated()) //
				.thenReturn(false);

		// when
		final boolean required = startupLogic.migrationRequired();

		// then
		assertThat(required, equalTo(true));
		verify(patchManager).isUpdated();
		verifyNoMoreInteractions(startupManager, patchManager, cachingLogic);
	}

	@Test
	public void migrationIsNotRequiredWhenPatchManagerIsUpdated() throws Exception {
		when(patchManager.isUpdated()) //
				.thenReturn(true);

		// when
		final boolean required = startupLogic.migrationRequired();

		// then
		assertThat(required, equalTo(false));
		verify(patchManager).isUpdated();
		verifyNoMoreInteractions(startupManager, patchManager, cachingLogic);
	}

	@Test
	public void migrationImpliesPatchManagerThenStartupManagerAndFinallyCachingLogic() throws Exception {
		// when
		startupLogic.migrate();

		// then
		final InOrder inOrder = inOrder(startupManager, patchManager, cachingLogic);
		inOrder.verify(patchManager).applyPatchList();
		inOrder.verify(startupManager).start();
		inOrder.verify(cachingLogic).clearCache();
	}

}
