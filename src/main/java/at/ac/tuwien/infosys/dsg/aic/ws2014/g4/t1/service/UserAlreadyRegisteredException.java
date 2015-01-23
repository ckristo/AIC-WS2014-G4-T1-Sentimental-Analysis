package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

/**
 * Custom registration exception used to indicate that a user is registered
 * already.
 */
public class UserAlreadyRegisteredException extends RegistrationException {

	public UserAlreadyRegisteredException() {
		super();
	}

	public UserAlreadyRegisteredException(String msg) {
		super(msg);
	}

	public UserAlreadyRegisteredException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
