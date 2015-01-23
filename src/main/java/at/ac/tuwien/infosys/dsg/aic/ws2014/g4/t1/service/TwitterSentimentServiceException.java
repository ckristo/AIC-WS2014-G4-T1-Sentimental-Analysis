package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.service;

/**
 * Custom exception for the Twitter sentiment service.
 */
public class TwitterSentimentServiceException extends Exception {

	public TwitterSentimentServiceException() {
		super();
	}

	public TwitterSentimentServiceException(String msg) {
		super(msg);
	}

	public TwitterSentimentServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
