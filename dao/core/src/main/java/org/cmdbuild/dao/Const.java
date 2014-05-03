package org.cmdbuild.dao;

public class Const {

	private Const() {
		// prevents instantiation
	}

	public static class User {

		private User() {
			// prevents instantiation
		}

		public static final String USERNAME = "Username";
		public static final String DESCRIPTION = "Description";
		public static final String PASSWORD = "Password";
		public static final String EMAIL = "Email";
		public static final String ACTIVE = "Active";

	}

	public static class Role {

		private Role() {
			// prevents instantiation
		}

		public static final String CODE = "Code";
		public static final String DESCRIPTION = "Description";
		public static final String EMAIL = "Email";
		public static final String ADMINISTRATOR = "Administrator";
		public static final String STARTING_CLASS = "startingClass";
		public static final String RESTRICTED_ADINISTRATOR = "CloudAdmin";
		public static final String ACTIVE = "Active";
		public static final String DISABLED_MODULES = "DisabledModules";

	}

	public static class UserRole {

		private UserRole() {
			// prevents instantiation
		}

		public static final String DEFAULT_GROUP = "DefaultGroup";

	}

}
