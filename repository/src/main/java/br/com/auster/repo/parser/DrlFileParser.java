package br.com.auster.repo.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import br.com.auster.repo.parser.complete.Expander;


public abstract class DrlFileParser {
	
	protected Expander createExpander(String name, File dslFile) throws IOException {

		FileReader fr = new FileReader(dslFile);
		BufferedReader br = new BufferedReader(fr);

		StringBuffer dslContent = new StringBuffer();

		String dslLine = null;
		while ((dslLine = br.readLine()) != null) {
			dslContent.append(dslLine).append("\n");
		}

		Expander dsl = new Expander(name);
		dsl.setContent(dslContent.toString());	

		return dsl;
	}

	/**
	 * Parses and builds a <code>RulePackage</code> from a DRL file with expander (DSL file)
	 * @param drlFile the DRL <code>File</code> to parse from
	 * @param dslFile the DSL <code>File</code> to parse from
	 */
	public abstract RulePackage parse(File drlFile) throws IOException;
		
}