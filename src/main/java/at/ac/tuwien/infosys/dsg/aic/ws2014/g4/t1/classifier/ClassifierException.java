package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

/**
 * Exception thrown by the classifier in case of an error.
 */
public class ClassifierException extends Exception {
	
	public ClassifierException() {}
	
	public ClassifierException(Exception ex) {
		super(ex);
	}
	
	public ClassifierException(String msg, Exception ex) {
		super(msg, ex);
	}
}
