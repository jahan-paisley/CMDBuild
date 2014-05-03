package unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.DefaultPrivileges.SimplePrivilege;
import org.junit.Test;

public class DefaultPrivilegesTest {

	@Test
	public void simplePrivilegesAreIndependent() {
		assertFalse(new SimplePrivilege().implies(new SimplePrivilege()));
	}

	@Test
	public void readOrWritePrivilegeImpliesItself() {
		assertTrue(DefaultPrivileges.WRITE.implies(DefaultPrivileges.WRITE));
		assertTrue(DefaultPrivileges.READ.implies(DefaultPrivileges.READ));
	}

	@Test
	public void writeImpliesReadPrivilege() {
		assertTrue(DefaultPrivileges.WRITE.implies(DefaultPrivileges.READ));
		assertTrue(DefaultPrivileges.WRITE.implies(DefaultPrivileges.WRITE));
		assertFalse(DefaultPrivileges.READ.implies(DefaultPrivileges.WRITE));
	}

	@Test
	public void godImpliesEveryPrivilege() {
		assertTrue(DefaultPrivileges.GOD.implies(DefaultPrivileges.READ));
		assertTrue(DefaultPrivileges.GOD.implies(DefaultPrivileges.WRITE));
		assertTrue(DefaultPrivileges.GOD.implies(new SimplePrivilege()));
	}

	@Test
	public void databaseDesignerIsIndependent() {
		assertTrue(DefaultPrivileges.DATABASE_DESIGNER.implies(DefaultPrivileges.DATABASE_DESIGNER));
	}

	@Test
	public void administratorIsIndependent() {
		assertTrue(DefaultPrivileges.ADMINISTRATOR.implies(DefaultPrivileges.ADMINISTRATOR));
		assertFalse(DefaultPrivileges.ADMINISTRATOR.implies(DefaultPrivileges.DATABASE_DESIGNER));
		assertFalse(DefaultPrivileges.ADMINISTRATOR.implies(DefaultPrivileges.READ));
		assertFalse(DefaultPrivileges.ADMINISTRATOR.implies(DefaultPrivileges.WRITE));
	}
}
