package unit.workflow;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.workflow.ActivityInstanceImpl.ActivityAdvanceChecker;
import org.cmdbuild.workflow.DefaultActivityAdvanceChecker;
import org.junit.Test;

public class DefaultActivityAdvanceCheckerTest {

	private final AuthenticatedUser authUser = mock(AuthenticatedUser.class);
	private final PrivilegeContext privilegeCtx = mock(PrivilegeContext.class);
	private final CMGroup selectedGroup = mock(CMGroup.class);
	private final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, selectedGroup);

	@Test
	public void advanceableWhenAdministratorPrivileges() throws Exception {
		// given
		when(privilegeCtx.hasAdministratorPrivileges()) //
				.thenReturn(true);

		final ActivityAdvanceChecker activityAdvanceChecker = new DefaultActivityAdvanceChecker(operationUser, "foo");

		// when
		final boolean advanceable = activityAdvanceChecker.isAdvanceable();

		// then
		assertThat(advanceable, equalTo(true));
		verifyZeroInteractions(authUser);
		verify(privilegeCtx).hasAdministratorPrivileges();
		verifyNoMoreInteractions(privilegeCtx);
		verifyZeroInteractions(selectedGroup);
	}

	@Test
	public void advanceableWhenDefaultGroupIsSelectedAndPerformerNameContainedInGroupList() throws Exception {
		// given
		when(authUser.getDefaultGroupName()) //
				.thenReturn("bar");
		when(authUser.getGroupNames()) //
				.thenReturn(newHashSet("foo", "bar", "baz"));

		final ActivityAdvanceChecker activityAdvanceChecker = new DefaultActivityAdvanceChecker(operationUser, "foo");

		// when
		final boolean advanceable = activityAdvanceChecker.isAdvanceable();

		// then
		assertThat(advanceable, equalTo(true));
		verify(authUser).getDefaultGroupName();
		verify(authUser).getGroupNames();
		verifyNoMoreInteractions(authUser);
		verify(privilegeCtx).hasAdministratorPrivileges();
		verifyNoMoreInteractions(privilegeCtx);
		verifyZeroInteractions(selectedGroup);
	}

	@Test
	public void advanceableWhenNoDefaultGroupSelectedButPerformerNameContainedInGroupList() throws Exception {
		// given
		when(authUser.getDefaultGroupName()) //
				.thenReturn(null);
		when(authUser.getGroupNames()) //
				.thenReturn(newHashSet("foo", "bar", "baz"));
		when(selectedGroup.getName()) //
				.thenReturn("foo");

		final ActivityAdvanceChecker activityAdvanceChecker = new DefaultActivityAdvanceChecker(operationUser, "foo");

		// when
		final boolean advanceable = activityAdvanceChecker.isAdvanceable();

		// then
		assertThat(advanceable, equalTo(true));
		verify(authUser).getDefaultGroupName();
		verifyNoMoreInteractions(authUser);
		verify(privilegeCtx).hasAdministratorPrivileges();
		verifyNoMoreInteractions(privilegeCtx);
		verify(selectedGroup).getName();
		verifyNoMoreInteractions(selectedGroup);
	}

	@Test
	public void notAdvanceableIfCurrentGroupDoesNotMatch() throws Exception {
		// given
		when(authUser.getDefaultGroupName()) //
				.thenReturn(null);
		when(authUser.getGroupNames()) //
				.thenReturn(newHashSet("bar", "baz"));
		when(selectedGroup.getName()) //
				.thenReturn("bar");

		final ActivityAdvanceChecker activityAdvanceChecker = new DefaultActivityAdvanceChecker(operationUser, "foo");

		// when
		final boolean advanceable = activityAdvanceChecker.isAdvanceable();

		// then
		assertThat(advanceable, equalTo(false));
		verify(authUser).getDefaultGroupName();
		verifyNoMoreInteractions(authUser);
		verify(privilegeCtx).hasAdministratorPrivileges();
		verifyNoMoreInteractions(privilegeCtx);
		verify(selectedGroup).getName();
		verifyNoMoreInteractions(selectedGroup);
	}

}
