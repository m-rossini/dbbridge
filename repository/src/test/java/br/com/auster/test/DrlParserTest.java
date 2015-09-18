/**
 * 
 */
package br.com.auster.test;

import java.io.IOException;

import br.com.auster.repo.parser.DrlFileParser;
import br.com.auster.repo.parser.ParserFactory;
import br.com.auster.repo.parser.RulePackage;

/**
 * @author pvieira
 *
 */
public class DrlParserTest extends BaseTest {
	
	public void testDrlParsing() {

		DrlFileParser parser = ParserFactory.createSimpleParser();
		RulePackage pkg = null;
		try {
			pkg = parser.parse(this.fibonacciDrlFile);
		} catch (IOException e) {
			fail(e.toString());
		}

		assertNotNull("RulePackage was not created.", pkg);
	}

	public void testDrlWithExpanderParsing() {

		DrlFileParser parser = ParserFactory.createSimpleParser();

		RulePackage pkg = null;
		try {
			pkg = parser.parse(this.sampleDrlWithExpanderFile);
		} catch (IOException e) {
			fail(e.toString());
		}

		assertNotNull("RulePackage was not created.", pkg);
		assertNotNull("RulePackage has no expander.", pkg.getExpander());
		assertNotNull("RulePackage expander is empty.", pkg.getExpander().getContent());
		assertFalse("RulePackage expander is empty.", pkg.getExpander().getContent().length() == 0);
		
	}
	
}
