package unit.auth;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.DefaultPrivileges.SimplePrivilege;
import org.cmdbuild.auth.acl.GroupImpl;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.auth.user.AnonymousUser;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.junit.Before;
import org.junit.Test;

public class OperationUserTest {

	private AuthenticatedUser mockAuthUser;
	private PrivilegeContext mockPrivilegeCtx;
	private CMGroup mockGroup;
	private static final AuthenticatedUser ANONYMOUS_USER = new AnonymousUser();
	final SerializablePrivilege po1;
	private final CMGroup g1;
	private final CMGroup g2;

	public OperationUserTest() {
		po1 = new SerializablePrivilege() {

			@Override
			public String getPrivilegeId() {
				return "pid";
			}

			@Override
			public Long getId() {
				return new Long(0);
			}

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getDescription() {
				return "";
			}

		};

		g1 = GroupImpl.newInstance().withName("g1") //
				.withPrivilege(new PrivilegePair(po1, PrivilegedObjectType.CLASS.getValue(), DefaultPrivileges.READ)) //
				.withPrivilege(new PrivilegePair(new SimplePrivilege())) //
				.build();

		g2 = GroupImpl.newInstance().withName("g2") //
				.withPrivilege(new PrivilegePair(po1, PrivilegedObjectType.CLASS.getValue(), DefaultPrivileges.WRITE)) //
				.withPrivilege(new PrivilegePair(new SimplePrivilege())) //
				.build();
	}

	@Before
	public void setUp() {
		mockAuthUser = mock(AuthenticatedUser.class);
		mockPrivilegeCtx = mock(PrivilegeContext.class);
		mockGroup = mock(CMGroup.class);
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailCreationIfAuthenticatedUserIsNull() {
		final OperationUser operationUser = new OperationUser(null, new NullPrivilegeContext(), new NullGroup());
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailCreationIfPrivilegeContextIsNull() {
		final OperationUser operationUser = new OperationUser(ANONYMOUS_USER, null, new NullGroup());
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailCreationIfSelectedGroupIsNull() {
		final OperationUser operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), null);
	}

	/*
	 * CMSecurityManager wrap
	 */

	@Test
	public void forwardsCallsToThePrivilegeContext() {
		// given
		final CMPrivilege p = new SimplePrivilege();
		final OperationUser operationUser = new OperationUser(mockAuthUser, mockPrivilegeCtx, mockGroup);

		// when
		operationUser.hasReadAccess(po1);
		operationUser.hasWriteAccess(po1);
		operationUser.hasDatabaseDesignerPrivileges();
		operationUser.hasAdministratorPrivileges();
		operationUser.hasPrivilege(p);
		operationUser.hasPrivilege(p, po1);

		// then
		verify(mockPrivilegeCtx, times(1)).hasReadAccess(po1);
		verify(mockPrivilegeCtx, times(1)).hasWriteAccess(po1);
		verify(mockPrivilegeCtx, times(1)).hasDatabaseDesignerPrivileges();
		verify(mockPrivilegeCtx, times(1)).hasAdministratorPrivileges();
		verify(mockPrivilegeCtx, times(1)).hasPrivilege(p, po1);
	}

	/*
	 * Preferred group
	 */

	@Test
	public void allowsSelectingANullGroup() {
		// given
		final OperationUser operationUser = new OperationUser(mockAuthUser, mockPrivilegeCtx, mockGroup);

		// when
		operationUser.selectGroup(null);

		// then
		assertThat(operationUser.getPreferredGroup(), is(nullValue()));
	}

	@Test
	public void canSelectAnExistingGroup() {
		// given
		when(mockAuthUser.getGroupNames()).thenReturn(groupSet(g1.getName(), g2.getName()));
		final OperationUser operationUser = new OperationUser(mockAuthUser, mockPrivilegeCtx, mockGroup);

		// when
		operationUser.selectGroup(g1);

		// then
		assertThat(operationUser.getPreferredGroup(), is(g1));
		assertThat(operationUser.getPreferredGroup().getName(), is(g1.getName()));
	}

	@Test
	public void aSingleGroupIsAutomaticallySelected() {
		// given
		final OperationUser operationUser = new OperationUser(mockAuthUser, mockPrivilegeCtx, mockGroup);

		// then
		assertThat(operationUser.getPreferredGroup(), is(mockGroup));
	}

	/*
	 * Utility methods
	 */

	private Set<String> groupSet(final String... groupNames) {
		final Set<String> groups = new HashSet<String>();
		for (final String g : groupNames) {
			groups.add(g);
		}
		return groups;
	}
}
