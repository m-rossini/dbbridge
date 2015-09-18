/**
 * 
 */
package br.com.auster.repo.parser;

import br.com.auster.repo.parser.simple.SimpleDrlFileParser;

/**
 * @author pvieira
 *
 */
public class ParserFactory {
	
	/*
	 * TBD
	 * @return
	 */
	public static DrlFileParser createSimpleParser() {

		return new SimpleDrlFileParser();
	}
}
