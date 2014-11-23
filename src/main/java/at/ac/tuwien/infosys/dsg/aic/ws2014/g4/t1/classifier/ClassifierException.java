package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

/**
 * Special exception class, used to tell the generic Exceptions returned by WEKA
 * methods apart from others.
 */
public class ClassifierException extends Exception {
	ClassifierException(Exception e) {
		super("exception in WEKA classifier", e);
	}
}
