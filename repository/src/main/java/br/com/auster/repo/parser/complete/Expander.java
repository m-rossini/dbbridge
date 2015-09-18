/**
 * 
 */
package br.com.auster.repo.parser.complete;

import br.com.auster.repo.parser.Item;

/**
 * @author pvieira
 *
 */
public class Expander extends Item {

	public static final String EXPANDER_KEYWORD = "expander";
	public static final String DEFAULT_EXPANDER_EXTENSION = "dsl";

	private String content;

	/**
	 * @param name
	 */
	public Expander(String name) {
		super(name);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(EXPANDER_KEYWORD).append(" ");
		sb.append(this.getName()).append(".").append(DEFAULT_EXPANDER_EXTENSION);

		return sb.toString();
	}

}
