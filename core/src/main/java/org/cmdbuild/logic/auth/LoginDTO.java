package org.cmdbuild.logic.auth;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.auth.UserStore;

public class LoginDTO {

	public static class Builder implements org.cmdbuild.common.Builder<LoginDTO> {

		private String loginString;
		private String unencryptedPassword;
		private String loginGroupName;
		private UserStore userStore;
		public boolean passwordRequired = true;

		/**
		 * 
		 * @param loginString
		 *            could be the either the username or the email
		 * @return
		 */
		public Builder withLoginString(final String loginString) {
			this.loginString = loginString;
			return this;
		}

		public Builder withPassword(final String unencryptedPassword) {
			this.unencryptedPassword = unencryptedPassword;
			this.passwordRequired = true;
			return this;
		}

		public Builder withGroupName(final String loginGroupName) {
			this.loginGroupName = loginGroupName;
			return this;
		}

		public Builder withUserStore(final UserStore userStore) {
			this.userStore = userStore;
			return this;
		}

		public Builder withNoPasswordRequired() {
			this.passwordRequired = false;
			return this;
		}

		@Override
		public LoginDTO build() {
			Validate.notNull(userStore);
			return new LoginDTO(this);
		}

	}

	private final String loginString;
	private final String unencryptedPassword;
	private final String loginGroupName;
	private final UserStore userStore;
	private final boolean passwordRequired;
	private final transient String toString;

	private LoginDTO(final Builder builder) {
		this.loginString = builder.loginString;
		this.unencryptedPassword = builder.unencryptedPassword;
		this.loginGroupName = builder.loginGroupName;
		this.userStore = builder.userStore;
		this.passwordRequired = builder.passwordRequired;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public String getLoginString() {
		return loginString;
	}

	public String getPassword() {
		return unencryptedPassword;
	}

	public String getLoginGroupName() {
		return loginGroupName;
	}

	public UserStore getUserStore() {
		return userStore;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	@Override
	public String toString() {
		return toString;
	}

}
