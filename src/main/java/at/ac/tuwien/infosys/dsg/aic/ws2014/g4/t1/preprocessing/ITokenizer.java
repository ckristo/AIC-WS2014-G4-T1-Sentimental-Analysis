package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.List;

public interface ITokenizer {

	/**
	 * Performs tokenization of an input string.
	 * @param string the input string.
	 * @return a list of tokenized strings.
	 */
	public List<String> tokenize(String string);
}
