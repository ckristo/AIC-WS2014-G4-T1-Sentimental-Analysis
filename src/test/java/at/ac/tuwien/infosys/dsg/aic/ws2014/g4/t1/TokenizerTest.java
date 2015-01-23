package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.ITokenizer;
import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.classifier.TokenizerImpl;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the Preprocessor implementation.
 */
public class TokenizerTest {

	ITokenizer tokenizer;
	
	@Before
	public void setUp() {
		tokenizer = new TokenizerImpl();
	}
	
	public void tearDown() {
		tokenizer = null;
	}
	
	@Test
	public void testTokenizer() {
		String test = "To get TO something, you have to go THROUGH something. Don't worry, God'll be there. #Test #TestimonyTuesdays";
		
		List<String> tokens = tokenizer.tokenize(test);
		
		assertTrue(tokens.size() > 0);
		assertTrue(tokens.contains("something"));
		assertTrue(tokens.contains("THROUGH"));
		assertTrue(tokens.contains("God'll"));
		assertTrue(tokens.contains("#Test"));
	}
	
}
