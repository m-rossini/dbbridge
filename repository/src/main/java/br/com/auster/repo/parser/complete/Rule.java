package br.com.auster.repo.parser.complete;

import java.util.HashMap;
import java.util.Map;

import br.com.auster.repo.parser.Item;

public class Rule extends Item {

	public static final String RULE_KEYWORD = "rule";
	public static final String WHEN_KEYWORD = "when";
	public static final String THEN_KEYWORD = "then";
	public static final String END_KEYWORD = "end";

	public static final String SALIENCE_ATTRIBUTE = "salience";
	public static final String AGENDA_ATTRIBUTE = "agenda-group";
	public static final String AUTOFOCUS_ATTRIBUTE = "auto-focus";
	public static final String ACTIVATION_ATTRIBUTE = "activation-group";
	public static final String NOLOOP_ATTRIBUTE = "no-loop";
	public static final String DURATION_ATTRIBUTE = "duration";

	private HandSides handSides = new HandSides();
    
    private HashMap<String, String> attributes;

	public Rule(String name) {
		super(name.replaceAll("\"", ""));
		attributes = new HashMap<String, String>();
	}
	
	public void setHandSides(HandSides handSides) {
		this.handSides = handSides;
	}

	public String getLhs() {
		return this.handSides.getLhs().replaceAll("\n", " ");
	}

	public void setLhs(String lhs) {
		this.handSides.setLhs(lhs);
	}

	public String getRhs() {
		return this.handSides.getRhs().replaceAll("\n", " ");
	}

	public void setRhs(String rhs) {
		this.handSides.setLhs(rhs);
	}

	public void addAttribute(String attributeName, String attributeValue) {
		this.attributes.put(attributeName, attributeValue);
	}
	
	public String getAttribute(String attributeName) {
		
		return this.attributes.get(attributeName);
	}
	
	public Map<String, String> getAttributesMap() {
		
		return this.attributes;
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();
		if (this.comments != null && this.comments.length() > 0) {
			sb.append(this.getFormattedComments()).append("\n");
		}
		sb.append(RULE_KEYWORD).append(" \"").append(this.getName()).append("\"\n");

		for (String key: this.attributes.keySet()) {
			sb.append("\t").append(key).append(" ");
			if (key.equals(AGENDA_ATTRIBUTE) || key.equals(ACTIVATION_ATTRIBUTE)) {
				sb.append("\"").append(this.attributes.get(key)).append("\"");
			} else {
				sb.append(this.attributes.get(key));
			}
			sb.append("\n");
		}

		sb.append("\t").append(WHEN_KEYWORD).append("\n");
		String[] handSideParts = this.handSides.getLhs().split("\n");
		for (int i = 0; i < handSideParts.length; i++) {
			sb.append("\t\t").append(handSideParts[i]).append("\n");
		}
		sb.append("\t").append(THEN_KEYWORD).append("\n");
		handSideParts = this.handSides.getRhs().split("\n");
		for (int i = 0; i < handSideParts.length; i++) {
			sb.append("\t\t").append(handSideParts[i]).append("\n");
		}
		sb.append(END_KEYWORD).append("\n");

		return sb.toString();
	}
}
