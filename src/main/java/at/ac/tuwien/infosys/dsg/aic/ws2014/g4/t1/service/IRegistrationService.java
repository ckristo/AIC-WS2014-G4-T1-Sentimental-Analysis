package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

/**
 * Registration service interface.
 */
public interface IRegistrationService {

	/**
	 * Performs registration of a new user.
	 *
	 * @param username the username to register (must be unique)
	 * @return the created user session
	 * @throws RegistrationException if the registration couldn't be performed
	 */
	UserSession register(String username) throws RegistrationException;

	/**
	 * Searches for an active session for user with a given username.
	 *
	 * @param username the username to search for.
	 * @return the found user session or null if no session is active for a user
	 * with the given username.
	 */
	UserSession getActiveSessionByUsername(String username);

	/**
	 * Returns the user session for a given session token.
	 *
	 * @param sessionToken the session token.
	 * @return the user session for the session token, or null if there's no
	 * session for the given session token.
	 */
	UserSession getActiveSessionBySessionToken(String sessionToken);

}
