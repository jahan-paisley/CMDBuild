package unit.services.startup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.services.startup.DefaultStartupManager;
import org.cmdbuild.services.startup.StartupManager;
import org.cmdbuild.services.startup.StartupManager.Condition;
import org.cmdbuild.services.startup.StartupManager.Startable;
import org.junit.Before;
import org.junit.Test;

public class DefaultStartupManagerTest {

	private StartupManager startupManager;

	@Before
	public void setUp() throws Exception {
		startupManager = new DefaultStartupManager();
	}

	@Test
	public void serviceNotStartedIfConditionIsNotSatisfied() throws Exception {
		// given
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, condition(false));

		// when
		startupManager.start();

		// then
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void serviceStartedIfConditionIsSatisfied() throws Exception {
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, condition(true));

		// when
		startupManager.start();

		// then
		verify(startable).start();
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void serviceStartedWhenConditionIsSatisfied() throws Exception {
		// given
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, condition(false, true));

		// when
		startupManager.start();

		// then
		verifyZeroInteractions(startable);

		// when
		startupManager.start();

		// then
		verify(startable).start();
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void serviceNoMoreStartedAfterTheFirstStart() throws Exception {
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, condition(true));

		// when
		startupManager.start();

		// then
		verify(startable).start();

		// when
		startupManager.start();

		// then
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void multipleServicesStartedAtDifferentTimes() throws Exception {
		// given
		final Startable firstStartable = mock(Startable.class);
		final Startable secondStartable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(firstStartable, condition(true));
		startupManager.add(secondStartable, condition(false, true));

		// when
		startupManager.start();

		// then
		verify(firstStartable).start();
		verifyZeroInteractions(secondStartable);

		// when
		startupManager.start();

		// then
		verify(secondStartable).start();
		verifyZeroInteractions(firstStartable);
	}

	/*
	 * Utilities
	 */

	private Condition condition(final Boolean value, final Boolean... values) {
		final Condition condition = mock(Condition.class);
		when(condition.satisfied()) //
				.thenReturn(value, values);
		return condition;
	}

}
