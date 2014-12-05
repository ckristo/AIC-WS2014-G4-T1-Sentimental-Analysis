package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import twitter4j.Status;

/**
 * Unit test for the Preprocessor implementation.
 */
public class ClassifierTest {

	ITwitterSentimentClassifier classifier;
	
	/**
	 * Mocked test tweets.
	 */
	Status status1 = mock(Status.class);
	{ when(status1.getText()).thenReturn("Reading my kindle2...  Love it... Lee childs is good read."); } // positive
	Status status2 = mock(Status.class);
	{ when(status2.getText()).thenReturn("Ok, first assesment of the #kindle2 ...it fucking rocks!!!"); } // positive
	Status status3 = mock(Status.class);
	{ when(status3.getText()).thenReturn("dear nike, stop with the flywire. that shit is a waste of science. and ugly. love, @vincentx24x"); } // negative
	Status status4 = mock(Status.class);
	{ when(status4.getText()).thenReturn("glad i didnt do Bay to Breakers today, it's 1000 freaking degrees in San Francisco wtf"); } // negative
	Status status5 = mock(Status.class);
	{ when(status5.getText()).thenReturn("need suggestions for a good IR filter for my canon 40D ... got some? pls DM"); } // neutral
	Status status6 = mock(Status.class);
	{ when(status6.getText()).thenReturn("@surfit: I just checked my google for my business- blip shows up as the second entry! Huh. Is that a good or ba... ? http://blip.fm/~6emhv"); } // neutral
	
	@Before
	public void setUp() {
		classifier = new TwitterSentimentClassifierImpl();
	}
	
	public void tearDown() {
		classifier = null;
	}
	
	@Test
	public void testTrainClassifier() throws Exception {
		Map<Status, Sentiment> testData = new HashMap<>();
		testData.put(status1, Sentiment.POSITIVE);
		testData.put(status2, Sentiment.POSITIVE);
		testData.put(status3, Sentiment.NEGATIVE);
		testData.put(status4, Sentiment.NEGATIVE);
		testData.put(status5, Sentiment.NEUTRAL);
		testData.put(status6, Sentiment.NEUTRAL);
		classifier.train(testData);
	}
	
}
