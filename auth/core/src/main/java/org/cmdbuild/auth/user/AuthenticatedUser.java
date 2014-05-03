package org.cmdbuild.auth.user;

import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;

/**
 * An authenticated user is essentially a user that can change the password.
 * Note that he can change his password if and only if he has previously logged
 * in successfully
 */
public interface AuthenticatedUser extends CMUser {

	/**
	 * A user is anonymous in these cases: 1) the user is not authenticated
	 * (e.g. bad credentials) 2) the user is a service user
	 * 
	 * @return true in one of the cases above
	 */
	public boolean isAnonymous();

	/**
	 * 
	 * @param passwordChanger
	 *            an object that lets the user to change his password
	 */
	public void setPasswordChanger(PasswordChanger passwordChanger);

	/**
	 * Method used to replace the old password with a new one
	 * 
	 * @return true if the password has been changed successfully, false
	 *         otherwise
	 */
	public boolean changePassword(final String oldPassword, final String newPassword);

	/**
	 * Returns true if the user can change password, false otherwise
	 */
	public boolean canChangePassword();

}
