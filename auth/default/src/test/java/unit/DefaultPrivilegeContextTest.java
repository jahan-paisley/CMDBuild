package unit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.DefaultPrivileges.SimplePrivilege;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.context.DefaultPrivilegeContext;
import org.cmdbuild.auth.context.DefaultPrivilegeContext.DefaultPrivilegeContextBuilder;
import org.cmdbuild.auth.context.DefaultPrivilegeContextFactory;
import org.junit.Test;

public class DefaultPrivilegeContextTest {

	private static class SimplePrivilegedObject implements CMPrivilegedObject {

		private final String privilegeId;

		private SimplePrivilegedObject(final String privilegeId) {
			this.privilegeId = privilegeId;
		}

		@Override
		public String getPrivilegeId() {
			return privilegeId;
		}
	}

	private static final CMPrivilegedObject DUMMY_PRIV_OBJECT = new SimplePrivilegedObject("dummy");

	private static final CMPrivilege IMPLIED = new SimplePrivilege();
	private static final CMPrivilege IMPLYING = new SimplePrivilege() {

		@Override
		public boolean implies(final CMPrivilege privilege) {
			return super.implies(privilege) || privilege == IMPLIED;
		}
	};

	private final DefaultPrivilegeContextBuilder builder = DefaultPrivilegeContext.newBuilderInstance();

	/*
	 * Builder tests
	 */

	@Test(expected = NullPointerException.class)
	public void nullGlobalPrivilegeCannotBeAdded() {
		builder.withPrivilege(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPrivilegedObjectCannotBeAdded() {
		builder.withPrivilege(new SimplePrivilege(), null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPrivilegeCannotBeAdded() {
		builder.withPrivilege(null, DUMMY_PRIV_OBJECT);
	}

	@Test
	public void globalPrivilegesAreRegisteredOnTheGlobalObject() {
		// given
		builder.withPrivilege(IMPLIED);
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// when
		final List<PrivilegePair> privileges = privilegeCtx.getAllPrivileges();

		// then
		assertThat(privileges.size(), is(1));
	}

	@Test
	public void objectPrivilegesAreRegisteredOnThatObject() {
		// given
		builder.withPrivilege(IMPLIED, DUMMY_PRIV_OBJECT);
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// when
		final List<PrivilegePair> privileges = privilegeCtx.getAllPrivileges();

		// then
		assertThat(privileges.size(), is(1));
		assertThat(privileges.get(0).name, is(DUMMY_PRIV_OBJECT.getPrivilegeId()));
	}

	@Test
	public void samePrivilegeIsNotRegisteredTwice() {
		// given
		builder.withPrivilege(IMPLIED);
		builder.withPrivilege(IMPLIED);

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertThat(privilegeCtx.getAllPrivileges().size(), is(1));
	}

	@Test
	public void differentPrivilegesAreBothRegistered() {
		// given
		builder.withPrivilege(new SimplePrivilege());
		builder.withPrivilege(new SimplePrivilege());

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertThat(privilegeCtx.getAllPrivileges().size(), is(2));
	}

	@Test
	public void privilegesAreUntouchedIfAlreadyImplied() {
		// given
		builder.withPrivilege(IMPLYING);
		builder.withPrivilege(IMPLIED);
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// when
		final List<PrivilegePair> privileges = privilegeCtx.getAllPrivileges();

		// then
		assertThat(privileges.size(), is(1));
		assertThat(privileges.get(0).privilege, is(IMPLYING));
	}

	@Test
	public void listOfPrivilegesAreMergedAsSinglePrivileges() {
		// given
		final CMPrivilegedObject a = new SimplePrivilegedObject("a");
		final CMPrivilegedObject b = new SimplePrivilegedObject("b");
		final CMPrivilegedObject c = new SimplePrivilegedObject("c");
		final CMPrivilegedObject d = new SimplePrivilegedObject("d");
		final CMPrivilegedObject e = new SimplePrivilegedObject("e");

		builder.withPrivileges(new ArrayList<PrivilegePair>() {
			{
				add(new PrivilegePair(a.getPrivilegeId(), IMPLIED));
				add(new PrivilegePair(b.getPrivilegeId(), IMPLYING));
				add(new PrivilegePair(c.getPrivilegeId(), new SimplePrivilege()));
				add(new PrivilegePair(d.getPrivilegeId(), new SimplePrivilege()));
			}
		});
		builder.withPrivileges(new ArrayList<PrivilegePair>() {
			{
				add(new PrivilegePair(a.getPrivilegeId(), IMPLYING));
				add(new PrivilegePair(b.getPrivilegeId(), IMPLIED));
				add(new PrivilegePair(c.getPrivilegeId(), new SimplePrivilege()));
				add(new PrivilegePair(e.getPrivilegeId(), new SimplePrivilege()));
			}
		});

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertThat(privilegeCtx.getPrivilegesFor(a).size(), is(1));
		assertThat(privilegeCtx.getPrivilegesFor(b).size(), is(1));
		assertThat(privilegeCtx.getPrivilegesFor(c).size(), is(2));
		assertThat(privilegeCtx.getPrivilegesFor(d).size(), is(1));
		assertThat(privilegeCtx.getPrivilegesFor(e).size(), is(1));
	}

	/*
	 * DefaultPrivilegeContext tests
	 */

	@Test
	public void ifEmptyItHasNoPrivileges() {
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		assertThat(privilegeCtx.getAllPrivileges().size(), is(0));
		assertFalse(privilegeCtx.hasAdministratorPrivileges());
		assertFalse(privilegeCtx.hasDatabaseDesignerPrivileges());
	}

	@Test
	public void globalPrivilegesAreAppliedToEveryObject() {
		// given
		builder.withPrivilege(IMPLIED);

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertTrue(privilegeCtx.hasPrivilege(IMPLIED));
		assertTrue(privilegeCtx.hasPrivilege(IMPLIED, DUMMY_PRIV_OBJECT));
	}

	@Test
	public void objectPrivilegesAreNotAppliedGlobally() {
		// given
		builder.withPrivilege(IMPLIED, DUMMY_PRIV_OBJECT);

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertFalse(privilegeCtx.hasPrivilege(IMPLIED));
		assertTrue(privilegeCtx.hasPrivilege(IMPLIED, DUMMY_PRIV_OBJECT));
	}

	@Test
	public void withGodPrivilegesYouCanDoEverything() {
		// given
		builder.withPrivilege(DefaultPrivileges.GOD);

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertTrue(privilegeCtx.hasPrivilege(DefaultPrivileges.ADMINISTRATOR));
		assertTrue(privilegeCtx.hasPrivilege(DefaultPrivileges.DATABASE_DESIGNER));
		assertTrue(privilegeCtx.hasReadAccess(DUMMY_PRIV_OBJECT));
		assertTrue(privilegeCtx.hasWriteAccess(DUMMY_PRIV_OBJECT));
	}

	@Test
	public void readPrivilegeGrantsReadAccess() {
		// given
		builder.withPrivilege(DefaultPrivileges.READ, DUMMY_PRIV_OBJECT);

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertTrue(privilegeCtx.hasReadAccess(DUMMY_PRIV_OBJECT));
		assertFalse(privilegeCtx.hasWriteAccess(DUMMY_PRIV_OBJECT));
	}

	@Test
	public void writePrivilegeGrantsReadAndWriteAccess() {
		// given
		builder.withPrivilege(DefaultPrivileges.WRITE, DUMMY_PRIV_OBJECT);

		// when
		final DefaultPrivilegeContext privilegeCtx = builder.build();

		// then
		assertTrue(privilegeCtx.hasReadAccess(DUMMY_PRIV_OBJECT));
		assertTrue(privilegeCtx.hasWriteAccess(DUMMY_PRIV_OBJECT));
	}

	@Test
	public void shouldHaveCorrectPrivilegesWhenPrivilegeContextBuiltFromGroups() {
		// given
		final CMGroup group = mock(CMGroup.class);
		final CMPrivilegedObject a = new SimplePrivilegedObject("a");
		final CMPrivilegedObject b = new SimplePrivilegedObject("b");
		final List<PrivilegePair> privileges = new ArrayList<PrivilegePair>();
		privileges.add(new PrivilegePair(a.getPrivilegeId(), DefaultPrivileges.READ));
		privileges.add(new PrivilegePair(b.getPrivilegeId(), DefaultPrivileges.WRITE));
		when(group.getAllPrivileges()).thenReturn(privileges);
		final DefaultPrivilegeContextFactory ctxFactory = new DefaultPrivilegeContextFactory();

		// when
		final PrivilegeContext privilegeCtx = ctxFactory.buildPrivilegeContext(group);

		// then
		assertTrue(privilegeCtx.hasReadAccess(a));
		assertFalse(privilegeCtx.hasWriteAccess(a));
		assertTrue(privilegeCtx.hasReadAccess(b));
		assertTrue(privilegeCtx.hasWriteAccess(b));
	}

}
