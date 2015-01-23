package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

import java.util.Objects;

/**
 * Class that represents a user handled by the registration service.
 */
public class User {

	/**
	 * The user name.
	 */
	private final String name;

	/**
	 * Constructor.
	 *
	 * @param name the user's name
	 */
	public User(String name) {
		this.name = name;
	}

	/**
	 * Returns the user's name.
	 *
	 * @return the user's name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 17 * hash + Objects.hashCode(this.name);
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
		final User other = (User) obj;
		return Objects.equals(this.name, other.name);
	}

}
