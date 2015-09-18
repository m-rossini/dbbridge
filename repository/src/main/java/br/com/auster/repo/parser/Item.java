package br.com.auster.repo.parser;

public abstract class Item {

	protected String name;
	protected String comments;

	public Item (String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComments() {
		return comments;
	}

	public String getFormattedComments() {

		StringBuffer sb = new StringBuffer();
		if (this.comments!= null && this.comments.length() > 0) {
			sb.append("/*").append("\n");
			sb.append(" * ").append(this.comments).append("\n");
			sb.append(" */");
		}

		return sb.toString();
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
}
