/**
 * 
 */
package br.com.auster.repo.parser.complete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import br.com.auster.repo.parser.DrlFileParser;
import br.com.auster.repo.parser.RulePackage;

/**
 * @author pvieira
 *
 */
public class CompleteDrlFileParser extends DrlFileParser {

	/**
	 * 
	 */
	public CompleteDrlFileParser() {}

	private Query createQuery(String line, BufferedReader br) {

		int startPosition = line.indexOf(Query.QUERY_KEYWORD) + Query.QUERY_KEYWORD.length();
		line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
		
		Query query = new Query(line);
		
		try {
			while (!(line = br.readLine()).equals(Query.END_KEYWORD)) {
				line = line.replaceAll("\"", "").trim();
				query.setBody(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return query;
	}

	private CompleteRulePackage createRulePackage(String line) {

		String pkgName = null;
		if (line.startsWith(CompleteRulePackage.PACKAGE_KEYWORD)) {
			int startPosition = line.indexOf(CompleteRulePackage.PACKAGE_KEYWORD) + CompleteRulePackage.PACKAGE_KEYWORD.length();
			int endPosition = line.lastIndexOf(';');
			pkgName = line.substring(startPosition, endPosition).trim();
		}
		CompleteRulePackage pkg = new CompleteRulePackage(pkgName);

		return pkg;		
	}

	private Function createFunction(String line, BufferedReader br)  {

		Function function = new Function();
		function.setSignature(line);
		try {
			StringBuffer funcBody = new StringBuffer();

			while (!(line = br.readLine()).equals("}")) {
				line = line.trim();
				funcBody.append(line).append("\n");
			}
			function.setBody(funcBody.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return function;
	}

	private Rule createRule(String line, BufferedReader br) {

		int startPosition = line.indexOf(Rule.RULE_KEYWORD) + Rule.RULE_KEYWORD.length();
		line = line.substring(startPosition, line.length()).trim();
		Rule rule = new Rule(line);

		try {
			while (!(line = br.readLine()).equals(Rule.END_KEYWORD)) {
				line = line.trim();
				
				if (line.startsWith(Rule.ACTIVATION_ATTRIBUTE)) {
					startPosition = line.indexOf(Rule.ACTIVATION_ATTRIBUTE) + Rule.ACTIVATION_ATTRIBUTE.length();
					line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
					rule.addAttribute(Rule.ACTIVATION_ATTRIBUTE, line);
				} else if (line.startsWith(Rule.AGENDA_ATTRIBUTE)) {
					startPosition = line.indexOf(Rule.AGENDA_ATTRIBUTE) + Rule.AGENDA_ATTRIBUTE.length();
					line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
					rule.addAttribute(Rule.AGENDA_ATTRIBUTE, line);
				} else if (line.startsWith(Rule.AUTOFOCUS_ATTRIBUTE)) {
					startPosition = line.indexOf(Rule.AUTOFOCUS_ATTRIBUTE) + Rule.AUTOFOCUS_ATTRIBUTE.length();
					line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
					rule.addAttribute(Rule.AUTOFOCUS_ATTRIBUTE, line);
				} else if (line.startsWith(Rule.DURATION_ATTRIBUTE)) {
					startPosition = line.indexOf(Rule.DURATION_ATTRIBUTE) + Rule.DURATION_ATTRIBUTE.length();
					line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
					rule.addAttribute(Rule.DURATION_ATTRIBUTE, line);
				} else if (line.startsWith(Rule.NOLOOP_ATTRIBUTE)) {
					startPosition = line.indexOf(Rule.NOLOOP_ATTRIBUTE) + Rule.NOLOOP_ATTRIBUTE.length();
					line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
					rule.addAttribute(Rule.NOLOOP_ATTRIBUTE, line);
				} else if (line.startsWith(Rule.SALIENCE_ATTRIBUTE)) {
					startPosition = line.indexOf(Rule.SALIENCE_ATTRIBUTE) + Rule.SALIENCE_ATTRIBUTE.length();
					line = line.substring(startPosition, line.length()).replaceAll("\"", "").trim();
					rule.addAttribute(Rule.SALIENCE_ATTRIBUTE, line);
				} else if (line.startsWith(Rule.WHEN_KEYWORD)) {
					HandSides hs = createHandSides(line, br);
					rule.setHandSides(hs);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rule;
	}

	private HandSides createHandSides(String line, BufferedReader br) throws IOException {

		StringBuffer lhs = new StringBuffer();
		StringBuffer rhs = new StringBuffer();
		try {
			boolean isRhs = false;
			while (!(line = br.readLine().trim()).equals(Rule.END_KEYWORD)) {
				if (line.trim().startsWith(Rule.THEN_KEYWORD)) {
					isRhs = true;
					continue;
				}
				if (isRhs) {
					rhs.append(line).append("\n");
				} else {
					lhs.append(line).append("\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HandSides hs = new HandSides();
		
		hs.setLhs(lhs.toString());
		hs.setRhs(rhs.toString());

		return hs;
	}

	@Override
	public RulePackage parse(File drlFile) throws IOException {
		
		FileReader fr = new FileReader(drlFile);
		BufferedReader br = new BufferedReader(fr);

		CompleteRulePackage pkg = null;

		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();

			if (line.length() == 0) {
				continue;
			}
			if (line.startsWith(RulePackage.PACKAGE_KEYWORD)) {
				pkg = createRulePackage(line);
			} else  if (line.startsWith(CompleteRulePackage.IMPORT_KEYWORD)) {
				String importString = line.trim();
				int startPosition = importString.indexOf(CompleteRulePackage.IMPORT_KEYWORD) + CompleteRulePackage.IMPORT_KEYWORD.length();
				int endPosition = importString.lastIndexOf(';');
				pkg.addImport(importString.substring(startPosition, endPosition).trim());
			} else if (line.startsWith(CompleteRulePackage.GLOBAL_KEYWORD)) {
				String globalString = line.trim();
				int startPosition = globalString.indexOf(CompleteRulePackage.GLOBAL_KEYWORD) + CompleteRulePackage.GLOBAL_KEYWORD.length();
				int endPosition = globalString.lastIndexOf(';');
				pkg.addGlobal(globalString.substring(startPosition, endPosition).trim());
			} else if (line.startsWith(Expander.EXPANDER_KEYWORD)) {
				String expanderString = line.trim();
				int startPosition = expanderString.indexOf(Expander.EXPANDER_KEYWORD) + Expander.EXPANDER_KEYWORD.length();
				int endPosition = expanderString.lastIndexOf(';');
				String fileName = expanderString.substring(startPosition, endPosition).trim();
				String filePath = drlFile.getParentFile().getAbsolutePath();
				File dslFile = new File(filePath + File.separator + fileName);
				String dslName = fileName.substring(0, fileName.lastIndexOf(".dsl"));
				pkg.setExpander(createExpander(dslName, dslFile));
			} else if (line.startsWith(Rule.RULE_KEYWORD)) {
				Rule rule = createRule(line, br);
				pkg.addRule(rule);
			} else if (line.startsWith(Function.FUNCTION_KEYWORD)) {
				Function function = createFunction(line, br);
				pkg.addFunction(function);
			} else if (line.startsWith(Query.QUERY_KEYWORD)) {
				Query query = createQuery(line, br);
				pkg.addQuery(query);
			}
		}

		fr.close();
		br.close();

		return pkg;
	}
}
