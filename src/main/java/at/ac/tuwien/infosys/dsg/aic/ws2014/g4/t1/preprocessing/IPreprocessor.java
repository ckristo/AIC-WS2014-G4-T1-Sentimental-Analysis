package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.List;

public interface IPreprocessor {
	
	/**
	 * Performs the preprocessing step.
	 * @param tokens the list of raw tokens that will be manipulated by the preprocessor.
	 */
	public void preprocess(List<String> tokens);
	
}
