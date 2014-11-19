package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.IPreprocessor;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.PreprocessorImpl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for the Preprocessor implementation.
 */
public class PreprocessorTest {

	IPreprocessor preprocessor;
	
	@Before
	public void setUp() {
		preprocessor = new PreprocessorImpl();
	}
	
	public void tearDown() {
		preprocessor = null;
	}
	
	@Test
	public void testRemoveStopwords() {
		List<String> tokens = new ArrayList<>();
		tokens.add("the");
		tokens.add("test");
		
		preprocessor.preprocess(tokens);
		
		assertFalse(tokens.contains("the"));
		assertTrue(tokens.contains("test"));
	}
	
}
