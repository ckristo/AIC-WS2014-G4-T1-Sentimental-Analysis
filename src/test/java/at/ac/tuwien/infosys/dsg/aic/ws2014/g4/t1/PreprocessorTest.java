package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.IPreprocessor;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.PreprocessorImpl;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.SentiWordNetDictionary;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.preprocessing.SentiWordNetDictionary.WordNetPosition;

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
		tokens.add("a");
		tokens.add("the");
		tokens.add("test");

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains("a"));
		assertFalse(tokens.contains("the"));
		assertTrue(tokens.contains("test"));
	}

	@Test
	public void testReplaceURLs_1() {
		String testUrl = "http://www.example.com/test/index.php?arg1=val1&arg2=val2";
		List<String> tokens = new ArrayList<>();
		tokens.add(testUrl);

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains(testUrl));
		assertTrue(tokens.contains(IPreprocessor.URL_TOKEN));
	}

	@Test
	public void testReplaceURLs_2() {
		String testUrl = "mailto:test@example.com";
		List<String> tokens = new ArrayList<>();
		tokens.add(testUrl);

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains(testUrl));
		assertTrue(tokens.contains(IPreprocessor.URL_TOKEN));
	}

	@Test
	public void testReplaceURLs_3() {
		String testUrl = "foobar://example.com/#/foo/bar";
		List<String> tokens = new ArrayList<>();
		tokens.add(testUrl);

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains(testUrl));
		assertTrue(tokens.contains(IPreprocessor.URL_TOKEN));
	}

	@Test
	public void testReplaceUsernames() {
		List<String> tokens = new ArrayList<>();
		tokens.add("@test");

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains("@test"));
		assertTrue(tokens.contains(IPreprocessor.USERNAME_TOKEN));
	}

	@Test
	public void testReplaceAbbreviations_1() {
		List<String> tokens = new ArrayList<>();
		tokens.add("lol");
		tokens.add("wth");

		preprocessor.preprocess(tokens);

		assertTrue(tokens.contains("laughing"));
		assertTrue(tokens.contains("out"));
		assertTrue(tokens.contains("loud"));
		assertTrue(tokens.contains("what"));
		assertTrue(tokens.contains("hell"));
	}

	@Test
	public void testReplaceMisspelledWords_1() {
		List<String> tokens = new ArrayList<>();
		tokens.add("huose");
		tokens.add("ball");

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains("huose"));
		assertTrue(tokens.contains("house"));
		assertTrue(tokens.contains("ball"));
	}
	
	@Test
	public void testReplaceMisspelledWords_2() {
		List<String> tokens = new ArrayList<>();
		tokens.add(",");
		
		// TODO: extend this test case
		
		preprocessor.preprocess(tokens);
		
		assertFalse(tokens.contains(","));
	}
	
	@Test
	public void testSplitSlashes() {
		List<String> tokens = new ArrayList<>();
		tokens.add("abc1/def1");
		tokens.add("abc2/def2/ghi2");
		tokens.add("abc3/def3/ghi3/jkl3");
		
		tokens.add("/abc");
		tokens.add("abc/");
		tokens.add("/abc/");
		
		tokens.add("/abc/def");
		tokens.add("abc/def/");
		tokens.add("/abc/def/");
		
		tokens.add("/abc/def/ghi");
		tokens.add("abc/def/ghi/");
		tokens.add("/abc/def/ghi/");
		
		preprocessor.preprocess(tokens);
		
		assertTrue(tokens.contains("abc1"));
		assertTrue(tokens.contains("def1"));
		assertTrue(tokens.contains("abc2"));
		assertTrue(tokens.contains("def2"));
		assertTrue(tokens.contains("ghi2"));
		assertTrue(tokens.contains("abc3"));
		assertTrue(tokens.contains("def3"));
		assertTrue(tokens.contains("ghi3"));
		assertTrue(tokens.contains("jkl3"));
		
		assertTrue(tokens.contains("/abc"));
		assertTrue(tokens.contains("abc/"));
		assertTrue(tokens.contains("/abc/"));
		assertTrue(tokens.contains("/abc/def"));
		assertTrue(tokens.contains("abc/def/"));
		assertTrue(tokens.contains("/abc/def/"));
		assertTrue(tokens.contains("/abc/def/ghi"));
		assertTrue(tokens.contains("abc/def/ghi/"));
		assertTrue(tokens.contains("/abc/def/ghi/"));
		assertFalse(tokens.contains("abc"));
		assertFalse(tokens.contains("def"));
		assertFalse(tokens.contains("ghi"));
	}

	@Test
	public void testRemoveSpecialCharTokens() {
		List<String> tokens = new ArrayList<>();
		tokens.add("...");
		tokens.add("!!!");
		tokens.add("$!.?");
		tokens.add("$$!!!.?*#");
		tokens.add("a)");
		tokens.add("!foobar!");

		preprocessor.preprocess(tokens);

		assertEquals(2, tokens.size());
		assertTrue(tokens.contains("a)"));
		assertTrue(tokens.contains("!foobar!"));
	}

	@Test
	public void testRemoveRepeatedChars() {
		List<String> tokens = new ArrayList<>();
		tokens.add("helloooooo");
		tokens.add("teeeestinnnnng");
		tokens.add("trees");
		tokens.add("looooose");

		preprocessor.preprocess(tokens);

		assertFalse(tokens.contains("helloooooo"));
		assertFalse(tokens.contains("teeeestinnnnng"));
		assertFalse(tokens.contains("looooose"));
		
		assertTrue(tokens.contains("hello"));
		assertTrue(tokens.contains("testing"));
		assertTrue(tokens.contains("trees"));
		assertTrue(tokens.contains("loose"));
	}
}
