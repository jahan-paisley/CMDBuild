package org.cmdbuild.auth;

public interface PasswordAuthenticator extends CMAuthenticator {

	interface PasswordChanger {

		/**
		 * Change user password
		 * 
		 * @param oldPassword
		 *            old password
		 * @param newPassword
		 *            new password
		 * @return if password change was successful
		 */
		boolean changePassword(String oldPassword, String newPassword);
	}

	/**
	 * 
	 * @param login
	 *            login informations
	 * @param password
	 *            unencrypted password
	 * @return if the password matches
	 */
	boolean checkPassword(Login login, String password);

	/**
	 * 
	 * @param login
	 * @return the unencrypted password or null
	 */
	String fetchUnencryptedPassword(Login login);

	/**
	 * 
	 * @param login
	 * @return password changer or null if no change possible
	 */
	PasswordChanger getPasswordChanger(Login login);

}
