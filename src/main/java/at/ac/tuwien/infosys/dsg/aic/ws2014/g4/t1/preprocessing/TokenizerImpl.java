package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing;

import java.util.List;

import cmu.arktweetnlp.Twokenize;

public class TokenizerImpl implements ITokenizer {

	/**
	 * Performs tokenization of an input string using the Twokenize tokenizer.
	 * @param string the input string.
	 * @return a list of tokens strings.
	 */
	@Override
	public List<String> tokenize(String string) {
		return Twokenize.tokenize(string);
	}

}
