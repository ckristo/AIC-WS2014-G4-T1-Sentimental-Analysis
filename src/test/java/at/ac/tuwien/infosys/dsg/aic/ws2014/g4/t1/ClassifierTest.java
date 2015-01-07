package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Constants;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import twitter4j.Status;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for the Preprocessor implementation.
 */
public class ClassifierTest {

	TwitterSentimentClassifierImpl classifier;
	
	/**
	 * Mocked test tweets.
	 */
	Status tweet1 = mock(Status.class);
	{ when(tweet1.getText()).thenReturn("Reading my kindle2...  Love it... Lee childs is good read."); }
	Status tweet2 = mock(Status.class);
	{ when(tweet2.getText()).thenReturn("Ok, first assesment of the #kindle2 ...it fucking rocks!!!"); }
	Status tweet3 = mock(Status.class);
	{ when(tweet3.getText()).thenReturn("dear nike, stop with the flywire. that shit is a waste of science. and ugly. love, @vincentx24x"); }
	Status tweet4 = mock(Status.class);
	{ when(tweet4.getText()).thenReturn("glad i didnt do Bay to Breakers today, it's 1000 freaking degrees in San Francisco wtf"); }
	Status tweet5 = mock(Status.class);
	{ when(tweet5.getText()).thenReturn("need suggestions for a good IR filter for my canon 40D ... got some? pls DM"); }
	Status tweet6 = mock(Status.class);
	{ when(tweet6.getText()).thenReturn("@surfit: I just checked my google for my business- blip shows up as the second entry! Huh. Is that a good or ba... ? http://blip.fm/~6emhv"); }
	Status tweet7 = mock(Status.class);
	{ when(tweet7.getText()).thenReturn("This is a good test tweet"); }
	
	Map<Status, Sentiment> trainingData = new HashMap<>();
	{
		trainingData.put(tweet1, Sentiment.POSITIVE);
		trainingData.put(tweet2, Sentiment.POSITIVE);
		trainingData.put(tweet3, Sentiment.NEGATIVE);
		trainingData.put(tweet4, Sentiment.NEGATIVE);
		trainingData.put(tweet5, Sentiment.NEUTRAL);
		trainingData.put(tweet6, Sentiment.NEUTRAL);
	}
	
	@Before
	public void setUp() throws Exception {
		InputStream is = getClass().getResourceAsStream(Constants.DEFAULT_CONFIG_FILE_RESOURCE);
		ApplicationConfig config = new ApplicationConfig(is);
		classifier = new TwitterSentimentClassifierImpl(config);
	}
	
	@After
	public void tearDown() {
		classifier.getAttributesOutputFile().delete();
		classifier.getClassifierOuptutFile().delete();
		classifier = null;
	}
	
	@Test
	public void testTrainClassifier() throws Exception {
		assertThat(classifier.isTrained(), is(false));
		classifier.train(trainingData);
		assertThat(classifier.isTrained(), is(true));
		assertThat(classifier.getAttributesOutputFile().exists(), is(true));
		assertThat(classifier.getAttributesOutputFile().exists(), is(true));
	}
	
	@Test
	public void testClassifyWithPrevTrainedClassifier() throws Exception {
		classifier.train(trainingData);
		assertThat(classifier.isTrained(), is(true));
		Double[] prob = classifier.classify(tweet7);
		assertThat(prob, is(notNullValue()));
	}
}
