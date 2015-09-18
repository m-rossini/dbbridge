/**
 * 
 */
package br.com.auster.repo.parser.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import br.com.auster.repo.parser.DrlFileParser;
import br.com.auster.repo.parser.RulePackage;
import br.com.auster.repo.parser.complete.Expander;

/**
 * @author pvieira
 *
 */
public class SimpleDrlFileParser extends DrlFileParser {

	/**
	 * 
	 */
	public SimpleDrlFileParser() {}

	@Override
	public RulePackage parse(File drlFile) throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(drlFile);
			return parse(fr, drlFile.getParentFile().getAbsolutePath());
		 } finally {
			 if (fr != null) { fr.close(); };
		 }
	}
	
	public RulePackage parse(Reader _reader, String _abspath) throws IOException {
		
		BufferedReader br = new BufferedReader(_reader);

		SimpleRulePackage pkg = null;
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {

			sb.append(line);
			line = line.trim();

			if (line.length() == 0) {
				sb.append("\n");
				continue;
			}
			sb.append("\n");

			if (line.startsWith(RulePackage.PACKAGE_KEYWORD)) {

				String pkgName = null;
				int startPosition = line.indexOf(RulePackage.PACKAGE_KEYWORD) + RulePackage.PACKAGE_KEYWORD.length();
				int endPosition = line.lastIndexOf(';');
				pkgName = line.substring(startPosition, endPosition).trim();

				pkg = new SimpleRulePackage(pkgName);

			} else if (line.startsWith(Expander.EXPANDER_KEYWORD)) {

				String expanderString = line.trim();
				int startPosition = expanderString.indexOf(Expander.EXPANDER_KEYWORD) + Expander.EXPANDER_KEYWORD.length();
				int endPosition = expanderString.lastIndexOf(';');
				String fileName = expanderString.substring(startPosition, endPosition).trim();
				String filePath = _abspath;
				File dslFile = new File(filePath + File.separator + fileName);
				String dslName = fileName.substring(0, fileName.lastIndexOf(".dsl"));

				pkg.setExpander(createExpander(dslName, dslFile));
			}
		}

		pkg.setContent(sb.toString());

		
		br.close();

		return pkg;
	}

}
