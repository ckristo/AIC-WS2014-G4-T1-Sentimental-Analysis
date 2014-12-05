package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import twitter4j.Status;

/**
 * Unit test for the Preprocessor implementation.
 */
public class Demo1Test {

	ITwitterSentimentClassifier classifier;
	
	/**
	 * Mocked test tweets.
	 */
	Status status1 = mock(Status.class);
	{ when(status1.getText()).thenReturn("Ok, first asessment of the #kindle2 it fuckin rocks"); } // positive
	Status status2 = mock(Status.class);
	{ when(status2.getText()).thenReturn("glad i didnt do Bay to Breakers today, it's 1000 freaking degrees in NYC wtf"); } // negative
	
	@Before
	public void setUp() {
		classifier = new TwitterSentimentClassifierImpl();
	}
	
	public void tearDown() {
		classifier = null;
	}
	
	@Test
	public void testDemo() throws Exception {
		classifier.classify(status1);
		classifier.classify(status2);
	}
	
}
