package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

/**
 * Custom exception for the registration service.
 */
public class RegistrationException extends Exception {

	public RegistrationException() {
		super();
	}

	public RegistrationException(String msg) {
		super(msg);
	}

	public RegistrationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
