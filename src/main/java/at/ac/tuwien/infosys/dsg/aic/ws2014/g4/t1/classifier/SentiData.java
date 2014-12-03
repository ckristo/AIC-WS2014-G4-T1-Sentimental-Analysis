package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier;

import java.util.List;

public class SentiData {
	private List<String> tokens;
	private double sentiment;

	public SentiData(List<String> toks, double s) {
		this.tokens = toks;
		this.sentiment = s;
	}

	public double getSentiment() {
		return sentiment;
	}

	public List<String> getTokens() {
		return tokens;
	}
}
