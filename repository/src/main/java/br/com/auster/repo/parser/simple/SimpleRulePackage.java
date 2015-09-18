package br.com.auster.repo.parser.simple;

import br.com.auster.repo.parser.RulePackage;

public class SimpleRulePackage extends RulePackage {
	
	private String content;

	public SimpleRulePackage(String name) {
		super(name);
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {

		return this.content;
	}
	
	public String getExpanderName() {
		
		return this.getExpander().getName();
	}

}
