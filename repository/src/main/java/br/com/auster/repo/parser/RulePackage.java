package br.com.auster.repo.parser;

import br.com.auster.repo.parser.complete.Expander;

public abstract class RulePackage extends Item {

	public static final String PACKAGE_KEYWORD = "package";

	protected Expander expander;

	public RulePackage(String name) {
		super(name);
	}

	/**
	 * @return the expander
	 */
	public Expander getExpander() {

		return this.expander;
	}

	/**
	 * @param expander the expander to set
	 */
	public void setExpander(Expander expander) {

		this.expander = expander;		
	}

	public boolean hasExpander() {
		
		return (this.getExpander() != null);
	}

	/**
	 * @return the content
	 */
	public abstract String getContent();

}
