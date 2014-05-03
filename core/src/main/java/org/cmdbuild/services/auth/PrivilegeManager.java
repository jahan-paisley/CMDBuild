package org.cmdbuild.services.auth;

import org.cmdbuild.auth.acl.CMPrivilegedObject;

public interface PrivilegeManager {

	public enum PrivilegeType {
		READ("r"), //
		WRITE("w"), //
		NONE("-"), //
		;

		private final String type;

		PrivilegeType(final String type) {
			this.type = type;
		}

		public String getGrantType() {
			return this.type;
		}

		public static PrivilegeType intersection(final PrivilegeType first, final PrivilegeType second) {
			if (first == PrivilegeType.WRITE && second == PrivilegeType.WRITE) {
				return PrivilegeType.WRITE;
			}
			if (first == PrivilegeType.NONE || second == PrivilegeType.NONE) {
				return PrivilegeType.NONE;
			}
			return PrivilegeType.READ;
		}

		public static PrivilegeType union(final PrivilegeType first, final PrivilegeType second) {
			if (first == PrivilegeType.WRITE || second == PrivilegeType.WRITE) {
				return PrivilegeType.WRITE;
			}
			if (first == PrivilegeType.NONE && second == PrivilegeType.NONE) {
				return PrivilegeType.NONE;
			}
			return PrivilegeType.READ;
		}
	}

	boolean isAdmin();

	void assureAdminPrivilege();

	void assureReadPrivilege(CMPrivilegedObject table);

	boolean hasReadPrivilege(CMPrivilegedObject table);

	void assureWritePrivilege(CMPrivilegedObject table);

	boolean hasWritePrivilege(CMPrivilegedObject table);

	void assureCreatePrivilege(CMPrivilegedObject table);

	boolean hasCreatePrivilege(CMPrivilegedObject table);

	PrivilegeType getPrivilege(CMPrivilegedObject schema);

}
