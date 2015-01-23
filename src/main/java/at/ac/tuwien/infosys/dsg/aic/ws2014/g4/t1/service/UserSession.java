package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

import java.util.Date;
import java.util.Objects;

/**
 * Class represents a user session handled by the registration service.
 */
public class UserSession {

	/**
	 * The user that holds the session.
	 */
	private final User user;

	/**
	 * The session token.
	 */
	private final String sessionToken;

	/**
	 * The expiration date of the session.
	 */
	private Date expirationDate;

	/**
	 * Constructor.
	 *
	 * @param user the user
	 * @param sessionToken the session token
	 */
	public UserSession(User user, String sessionToken) {
		this.user = user;
		this.sessionToken = sessionToken;
	}

	/**
	 * Constructor.
	 *
	 * @param user the user
	 * @param sessionToken the session token
	 * @param expirationDate the expiration date
	 */
	public UserSession(User user, String sessionToken, Date expirationDate) {
		this.user = user;
		this.sessionToken = sessionToken;
		this.expirationDate = expirationDate;
	}

	/**
	 * Returns the user associated with the session.
	 *
	 * @return the user associated with the session.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Returns the session token of the session.
	 *
	 * @return the session token
	 */
	public String getSessionToken() {
		return sessionToken;
	}

	/**
	 * Returns the date when the session will expire.
	 *
	 * @return the date when the session will expire (null means never).
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * Checks if the session is expired.
	 *
	 * @return true if the session is expired, false otherwise
	 */
	public boolean isExpired() {
		return (expirationDate != null) ? expirationDate.before(new Date()) : false;
	}

	/**
	 * Sets the expiration date of the session.
	 *
	 * @param expirationDate the new expiration date
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.user);
		hash = 47 * hash + Objects.hashCode(this.sessionToken);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UserSession other = (UserSession) obj;
		return Objects.equals(this.user, other.user)
				&& Objects.equals(this.sessionToken, other.sessionToken);
	}

}
