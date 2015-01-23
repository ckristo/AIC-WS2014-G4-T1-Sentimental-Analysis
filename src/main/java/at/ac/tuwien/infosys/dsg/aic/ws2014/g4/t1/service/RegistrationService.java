package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Registration service.
 */
public class RegistrationService implements IRegistrationService {

	/**
	 * The session duration (in milliseconds).
	 */
	private static final int SESSION_DURATION = 86400000; // = 1 day in milliseconds

	/**
	 * Map that contains all user sessions (key = session token, value = user
	 * session object).
	 */
	private final Map<String, UserSession> sessions = new HashMap<>();

	/**
	 * The singleton instance.
	 */
	private static RegistrationService instance;

	/**
	 * Constructor.
	 *
	 * Do not allow to instantiate objects -- singleton!
	 */
	private RegistrationService() {
	}

	/**
	 * Returns the registration service singleton instance.
	 *
	 * @return the registration service instance.
	 */
	public static RegistrationService getInstance() {
		if (instance == null) {
			instance = new RegistrationService();
		}
		return instance;
	}

	@Override
	public UserSession register(String username) throws RegistrationException {
		// check if a session for the user exists and is still alive
		UserSession prevSession = getActiveSessionByUsername(username);
		if (prevSession != null) {
			if (!prevSession.isExpired()) {
				throw new UserAlreadyRegisteredException("Cannot register user '" + username + "': there's an open session'");
			} else {
				// remove an old, expired session
				sessions.remove(prevSession.getSessionToken());
			}
		}

		// start a new session
		String token = generateSessionToken();
		Date expires = new Date(new Date().getTime() + SESSION_DURATION);
		UserSession session = new UserSession(new User(username), token, expires);

		sessions.put(token, session);

		return session;
	}

	@Override
	public UserSession getActiveSessionByUsername(String username) {
		for (Map.Entry<String, UserSession> entry : sessions.entrySet()) {
			UserSession session = entry.getValue();
			if (Objects.equals(session.getUser().getName(), username)) {
				if (!session.isExpired()) {
					return session;
				} else {
					// remove an old, expired session
					sessions.remove(session.getSessionToken());
				}
			}
		}
		return null;
	}

	@Override
	public UserSession getActiveSessionBySessionToken(String sessionToken) {
		UserSession session = sessions.get(sessionToken);
		if (session != null) {
			if (session.isExpired()) {
				sessions.remove(session.getSessionToken());
			} else {
				return session;
			}
		}
		return null;
	}

	/**
	 * Generates a new session token.
	 *
	 * @return a new session token.
	 */
	private String generateSessionToken() {
		SecureRandom random = new SecureRandom();
		byte[] randomBytes = new byte[128];

		// generate new unique session token
		String token = null;
		do {
			random.nextBytes(randomBytes);
			token = DigestUtils.sha256Hex(randomBytes);
		} while (sessions.containsKey(token));

		return token;
	}

}
