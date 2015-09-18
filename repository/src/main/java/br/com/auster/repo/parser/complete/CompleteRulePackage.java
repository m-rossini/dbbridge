package br.com.auster.repo.parser.complete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.com.auster.repo.parser.RulePackage;

public class CompleteRulePackage extends RulePackage {

	public static final String IMPORT_KEYWORD = "import";
	public static final String GLOBAL_KEYWORD = "global";

	private List<String> imports;
	private List<String> globals;
	private Map<String, Rule> rules;
	private Map<String, Function> functions;
	private Map<String, Query> queries;

	public CompleteRulePackage(String name) {
		super(name);
		this.imports = new ArrayList<String>();
		this.globals = new ArrayList<String>();
		this.rules = new TreeMap<String, Rule>();
		this.functions = new HashMap<String, Function>();
		this.queries = new HashMap<String, Query>();
	}

	public void addImport(String importString) {

		this.imports.add(importString);		
	}

	public List<String> getImports() {

		return this.imports;
	}

	public void addGlobal(String globalString) {

		this.globals.add(globalString);		
	}

	public List<String> getGlobals() {

		return this.globals;
	}

	public void addRule(String alias, Rule rule) {

		this.rules.put(alias, rule);		
	}

	public void addRule(Rule rule) {

		this.addRule(rule.getName(), rule);		
	}

	public Rule getRule(String name) {

		return this.rules.get(name);
	}
	
	public List<Rule> getRules() {
		
		return new ArrayList<Rule>(this.rules.values());
	}

	public void addFunction(String alias, Function function) {

		this.functions.put(alias, function);		
	}

	public void addFunction(Function function) {

		this.addFunction(function.getName(), function);		
	}

	public Function getFunction(String name) {

		return this.functions.get(name);
	}
	
	public List<Function> getFunctions() {
		
		return new ArrayList<Function>(this.functions.values());
	}

	public void addQuery(String alias, Query query) {

		this.queries.put(alias, query);		
	}

	public void addQuery(Query query) {

		this.addQuery(query.getName(), query);		
	}

	public Query getQuery(String name) {

		return this.queries.get(name);
	}
	
	public List<Query> getQueries() {

		return new ArrayList<Query>(this.queries.values());
	}
	
	public String getContent() {
		
		return this.toString();
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();

		// package namespace
		if (this.comments != null && this.comments.length() > 0) {
			sb.append(this.getFormattedComments()).append("\n");
		}
		sb.append(PACKAGE_KEYWORD).append(" ").append(this.getName()).append("\n");
		sb.append("\n");

		// imports
		if (!this.imports.isEmpty()) {
			for (String str: this.imports) {
				sb.append(IMPORT_KEYWORD).append(" ").append(str).append(";\n");
			}
			sb.append("\n");
		}

		// expander
		if (this.expander != null) {
			sb.append(this.expander.toString()).append(";\n");
			sb.append("\n");
		}

		// globals
		if (!this.globals.isEmpty()) {
			for (String str: this.globals) {
				sb.append(GLOBAL_KEYWORD).append(" ").append(str).append(";\n");
			}
			sb.append("\n");
		}

		// functions
		if (!this.functions.isEmpty()) {
			for (Function function: this.functions.values()) {
				sb.append(function.toString()).append("\n");
			}
			sb.append("\n");
		}
		// queries
		if (!this.queries.isEmpty()) {
			for (Query query: this.queries.values()) {
				sb.append(query.toString()).append("\n");
			}
			sb.append("\n");
		}
		// rules
		for (Rule rule: this.rules.values()) {
			sb.append(rule.toString()).append("\n");
		}

		return sb.toString();
	}
}
