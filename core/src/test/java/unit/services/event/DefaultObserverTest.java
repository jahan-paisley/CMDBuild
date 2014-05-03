package unit.services.event;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.event.Command;
import org.cmdbuild.services.event.Context;
import org.cmdbuild.services.event.Contexts.AfterCreate;
import org.cmdbuild.services.event.Contexts.AfterUpdate;
import org.cmdbuild.services.event.Contexts.BeforeDelete;
import org.cmdbuild.services.event.Contexts.BeforeUpdate;
import org.cmdbuild.services.event.DefaultObserver;
import org.cmdbuild.services.event.DefaultObserver.Phase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DefaultObserverTest {

	private static final CMCard UNIMPORTANT = mock(CMCard.class);
	private static final CMCard ANOTHER_UNIMPORTANT = mock(CMCard.class);

	@Test
	public void singleCommandExecutedOnAfterCreatePhase() {
		// given
		final Command command = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command, Phase.AFTER_CREATE) //
				.build();

		// when
		observer.afterCreate(UNIMPORTANT);

		// then
		final ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(command).execute(contextCaptor.capture());
		final Context capturedContext = contextCaptor.getValue();
		final AfterCreate afterCreate = AfterCreate.class.cast(capturedContext);
		assertThat(afterCreate.card, is(UNIMPORTANT));
	}

	@Test
	public void multipleCommandsExecutedOnAfterCreatePhase() {
		// given
		final Command command1 = mock(Command.class);
		final Command command2 = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command1, Phase.AFTER_CREATE) //
				.add(command2, Phase.AFTER_CREATE) //
				.build();

		// when
		observer.afterCreate(UNIMPORTANT);

		// then
		verify(command1).execute(any(AfterCreate.class));
		verify(command2).execute(any(AfterCreate.class));
	}

	@Test
	public void commandExecutedForAfterCreatePhaseOthersAreNotExecuted() {
		// given
		final Command AFTER_CREATE = mock(Command.class);
		final Command BEFORE_UPDATE = mock(Command.class);
		final Command AFTER_UPDATE = mock(Command.class);
		final Command BEFORE_DELETE = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(AFTER_CREATE, Phase.AFTER_CREATE) //
				.add(BEFORE_UPDATE, Phase.BEFORE_UPDATE) //
				.add(AFTER_UPDATE, Phase.AFTER_UPDATE) //
				.add(BEFORE_DELETE, Phase.BEFORE_DELETE) //
				.build();

		// when
		observer.afterCreate(UNIMPORTANT);

		// then
		verify(AFTER_CREATE).execute(any(AfterCreate.class));
	}

	@Test
	public void singleCommandExecutedOnBeforeUpdatePhase() {
		// given
		final Command command = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command, Phase.BEFORE_UPDATE) //
				.build();

		// when
		observer.beforeUpdate(UNIMPORTANT, ANOTHER_UNIMPORTANT);

		// then
		final ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(command).execute(contextCaptor.capture());
		final Context capturedContext = contextCaptor.getValue();
		final BeforeUpdate beforeUpdate = BeforeUpdate.class.cast(capturedContext);
		assertThat(beforeUpdate.actual, is(UNIMPORTANT));
		assertThat(beforeUpdate.next, is(ANOTHER_UNIMPORTANT));
	}

	@Test
	public void multipleCommandsExecutedOnBeforeUpdatePhase() {
		// given
		final Command command1 = mock(Command.class);
		final Command command2 = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command1, Phase.BEFORE_UPDATE) //
				.add(command2, Phase.BEFORE_UPDATE) //
				.build();

		// when
		observer.beforeUpdate(UNIMPORTANT, ANOTHER_UNIMPORTANT);

		// then
		verify(command1).execute(any(BeforeUpdate.class));
		verify(command2).execute(any(BeforeUpdate.class));
	}

	@Test
	public void commandExecutedForBeforeUpdatePhaseOthersAreNotExecuted() {
		// given
		final Command AFTER_CREATE = mock(Command.class);
		final Command BEFORE_UPDATE = mock(Command.class);
		final Command AFTER_UPDATE = mock(Command.class);
		final Command BEFORE_DELETE = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(AFTER_CREATE, Phase.AFTER_CREATE) //
				.add(BEFORE_UPDATE, Phase.BEFORE_UPDATE) //
				.add(AFTER_UPDATE, Phase.AFTER_UPDATE) //
				.add(BEFORE_DELETE, Phase.BEFORE_DELETE) //
				.build();

		// when
		observer.beforeUpdate(UNIMPORTANT, ANOTHER_UNIMPORTANT);

		// then
		verify(BEFORE_UPDATE).execute(any(BeforeUpdate.class));
	}

	@Test
	public void singleCommandExecutedOnAfterUpdatePhase() {
		// given
		final Command command = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command, Phase.AFTER_UPDATE) //
				.build();

		// when
		observer.afterUpdate(UNIMPORTANT, ANOTHER_UNIMPORTANT);

		// then
		final ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(command).execute(contextCaptor.capture());
		final Context capturedContext = contextCaptor.getValue();
		final AfterUpdate afterUpdate = AfterUpdate.class.cast(capturedContext);
		assertThat(afterUpdate.previous, is(UNIMPORTANT));
		assertThat(afterUpdate.actual, is(ANOTHER_UNIMPORTANT));
	}

	@Test
	public void multipleCommandsExecutedOnAfterUpdatePhase() {
		// given
		final Command command1 = mock(Command.class);
		final Command command2 = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command1, Phase.AFTER_UPDATE) //
				.add(command2, Phase.AFTER_UPDATE) //
				.build();

		// when
		observer.afterUpdate(UNIMPORTANT, ANOTHER_UNIMPORTANT);

		// then
		verify(command1).execute(any(BeforeUpdate.class));
		verify(command2).execute(any(BeforeUpdate.class));
	}

	@Test
	public void commandExecutedForAfterUpdatePhaseOthersAreNotExecuted() {
		// given
		final Command AFTER_CREATE = mock(Command.class);
		final Command BEFORE_UPDATE = mock(Command.class);
		final Command AFTER_UPDATE = mock(Command.class);
		final Command BEFORE_DELETE = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(AFTER_CREATE, Phase.AFTER_CREATE) //
				.add(BEFORE_UPDATE, Phase.BEFORE_UPDATE) //
				.add(AFTER_UPDATE, Phase.AFTER_UPDATE) //
				.add(BEFORE_DELETE, Phase.BEFORE_DELETE) //
				.build();

		// when
		observer.afterUpdate(UNIMPORTANT, ANOTHER_UNIMPORTANT);

		// then
		verify(AFTER_UPDATE).execute(any(AfterUpdate.class));
	}

	@Test
	public void singleCommandExecutedOnBeforeDeletePhase() {
		// given
		final Command command = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command, Phase.BEFORE_DELETE) //
				.build();

		// when
		observer.beforeDelete(UNIMPORTANT);

		// then
		final ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(command).execute(contextCaptor.capture());
		final Context capturedContext = contextCaptor.getValue();
		final BeforeDelete beforeDelete = BeforeDelete.class.cast(capturedContext);
		assertThat(beforeDelete.card, is(UNIMPORTANT));
	}

	@Test
	public void multipleCommandsExecutedOnBeforeDeletePhase() {
		// given
		final Command command1 = mock(Command.class);
		final Command command2 = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(command1, Phase.BEFORE_DELETE) //
				.add(command2, Phase.BEFORE_DELETE) //
				.build();

		// when
		observer.beforeDelete(UNIMPORTANT);

		// then
		verify(command1).execute(any(BeforeDelete.class));
		verify(command2).execute(any(BeforeDelete.class));
	}

	@Test
	public void commandExecutedForBeforeDeletePhaseOthersAreNotExecuted() {
		// given
		final Command AFTER_CREATE = mock(Command.class);
		final Command BEFORE_UPDATE = mock(Command.class);
		final Command AFTER_UPDATE = mock(Command.class);
		final Command BEFORE_DELETE = mock(Command.class);
		final DefaultObserver observer = DefaultObserver.newInstance() //
				.add(AFTER_CREATE, Phase.AFTER_CREATE) //
				.add(BEFORE_UPDATE, Phase.BEFORE_UPDATE) //
				.add(AFTER_UPDATE, Phase.AFTER_UPDATE) //
				.add(BEFORE_DELETE, Phase.BEFORE_DELETE) //
				.build();

		// when
		observer.beforeDelete(UNIMPORTANT);

		// then
		verify(BEFORE_DELETE).execute(any(BeforeDelete.class));
	}

}
