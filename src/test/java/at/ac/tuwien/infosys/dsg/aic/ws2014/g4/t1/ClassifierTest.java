package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITwitterSentimentClassifier.Sentiment;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.SentiWordNetDictionary;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.SentiWordNetDictionary.WordNetPosition;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TwitterSentimentClassifierImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.ApplicationConfig;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.Constants;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import twitter4j.Status;

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
	Status tweet8 = mock(Status.class);
	{ when(tweet8.getText()).thenReturn("This is a rather bad test tweet"); }
	Status tweet9 = mock(Status.class);
	{ when(tweet9.getText()).thenReturn("This is a neutral test tweet"); }
	
	Map<Status, Sentiment> trainingSet = new HashMap<>();
	{
		trainingSet.put(tweet1, Sentiment.POSITIVE);
		trainingSet.put(tweet2, Sentiment.POSITIVE);
		trainingSet.put(tweet3, Sentiment.NEGATIVE);
		trainingSet.put(tweet4, Sentiment.NEGATIVE);
		trainingSet.put(tweet5, Sentiment.NEUTRAL);
		trainingSet.put(tweet6, Sentiment.NEUTRAL);
	}
	Map<Status, Sentiment> testSet = new HashMap<>();
	{
		testSet.put(tweet7, Sentiment.POSITIVE);
		testSet.put(tweet8, Sentiment.NEGATIVE);
		testSet.put(tweet9, Sentiment.NEUTRAL);
	}
	
	File exportTrainingDataFile = null;
	
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
		if (exportTrainingDataFile != null) {
			exportTrainingDataFile.delete();
		}
		classifier = null;
	}
	
	@Test
	public void testIfClassifierIsUntrained() {
		assertThat(classifier.isTrained(), is(false));
	}
	
	@Test
	public void testTrainClassifier() throws Exception {
		classifier.train(trainingSet);
		assertThat(classifier.isTrained(), is(true));
		
		assertThat(classifier.getAttributesOutputFile().exists(), is(true));
		assertThat(classifier.getAttributesOutputFile().exists(), is(true));
	}
	
	@Test
	public void testClassifyWithPrevTrainedClassifier() throws Exception {
		classifier.train(trainingSet);
		assertThat(classifier.isTrained(), is(true));
		
		Sentiment sentiment = classifier.classify(tweet7);
		assertThat(sentiment, is(notNullValue()));
	}
	
	@Test(expected = IllegalStateException.class)
	public void testClassifyWithUntrainedClassifier() throws Exception {
		classifier.classify(tweet7);
	}
	
	@Test
	public void testExportProcessedTrainingSet() throws Exception {
		classifier.processTrainingSet(trainingSet);
		assertThat(classifier.isTrained(), is(false));
		
		exportTrainingDataFile = new File(classifier.getExportDirectory(), "testData.arff");
		classifier.exportProcessedTrainingDataToArffFile(exportTrainingDataFile.getName());
		assertThat(exportTrainingDataFile.exists(), is(true));
	}
	
	@Test
	public void testTrainClassifierFromArffFile() throws Exception {
		classifier.processTrainingSet(trainingSet);
		exportTrainingDataFile = new File(classifier.getExportDirectory(), "testData.arff");
		classifier.exportProcessedTrainingDataToArffFile(exportTrainingDataFile.getName());
		assertThat(classifier.isTrained(), is(false));
		
		classifier.loadProcessedTrainingDataFromArffFile(exportTrainingDataFile);
		assertThat(classifier.isTrained(), is(false));
		
		classifier.train();
		assertThat(classifier.isTrained(), is(true));
		
		classifier.classify(tweet7);
	}
	
	@Test
	public void testCompareDifferentTrainingProcedures() throws Exception {
		Sentiment s1, s2, s3;
		
		// approach 1
		classifier.train(trainingSet);
		assertThat(classifier.isTrained(), is(true));
		s1 = classifier.classify(tweet1);
		
		// approach 2
		classifier.processTrainingSet(trainingSet);
		classifier.train();
		s2 = classifier.classify(tweet1);
		
		// approach 3
		exportTrainingDataFile = new File(classifier.getExportDirectory(), "testData.arff");
		classifier.exportProcessedTrainingDataToArffFile(exportTrainingDataFile.getName());
		classifier.loadProcessedTrainingDataFromArffFile(exportTrainingDataFile);
		s3 = classifier.classify(tweet1);
		
		assertThat(s1, is(equalTo(s2)));
		assertThat(s2, is(equalTo(s3)));
	}
	
	@Test
	public void testProcessTestSet() throws Exception {
		// train classifier
		classifier.train(trainingSet);
		assertThat(classifier.isTrained(), is(true));
		
		// process test data
		classifier.processTestSet(testSet);
	}
	
	@Test
	public void testEvaluateClassifier() throws Exception {
		// train classifier
		classifier.train(trainingSet);
		assertThat(classifier.isTrained(), is(true));
		
		// evaluate classifier
		classifier.evaluate(testSet);
	}
	
	@Test
	public void testEvaluateClassifierWithProcessedTestSet() throws Exception {
		// train classifier
		classifier.train(trainingSet);
		assertThat(classifier.isTrained(), is(true));
		
		// process test data
		classifier.processTestSet(testSet);
		
		// evaluate classifier
		classifier.evaluate();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testProcessTestDataWithoutTrainedClassifier() throws Exception {
		classifier.processTestSet(trainingSet);
	}
	
	@Test
	public void testSentiWordNetDictionary() {
		SentiWordNetDictionary dict = SentiWordNetDictionary.getInstance();
		double sentiment;
		
		sentiment = dict.getSentimentValue("good", WordNetPosition.A);
		assertTrue(sentiment > 0.0d);
		
		sentiment = dict.getSentimentValue("bad", WordNetPosition.A);
		assertTrue(sentiment < 0.0d);
	}
}
