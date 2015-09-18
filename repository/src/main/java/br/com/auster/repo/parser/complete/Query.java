package br.com.auster.repo.parser.complete;

import br.com.auster.repo.parser.Item;


public class Query extends Item {

	public Query(String name) {
		super(name);
	}

	public static final String QUERY_KEYWORD = "query";
	public static final String END_KEYWORD = "end";

	private String body;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public String toString() {

		StringBuffer sb = new StringBuffer();
		if (this.comments != null && this.comments.length() > 0) {
			sb.append(this.getFormattedComments()).append("\n");
		}
		sb.append(QUERY_KEYWORD).append(" \"").append(this.getName()).append("\"\n");
		String[] bodyParts = this.body.split("\n");
		for (int i = 0; i < bodyParts.length; i++) {
			sb.append("\t").append(bodyParts[i]).append("\n");
		}
		sb.append(END_KEYWORD).append("\n");

		return sb.toString();
	}


}
