package br.com.auster.repo.parser.complete;

import java.util.ArrayList;
import java.util.List;

import br.com.auster.repo.parser.Item;

public class Function extends Item {

	public static final String FUNCTION_KEYWORD = "function";

	private String body;
	private String returnType;
	private List<String> argsList;

	public Function(String name) {
		super(name);
		this.argsList = new ArrayList<String>();
	}
	
	public Function() {
		this("");
	}

	public String getBody() {
		return body.replaceAll("\n", " ");
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSignature() {
		
		StringBuffer sb = new StringBuffer();
		sb.append(this.returnType).append(" ");
		sb.append(this.getName()).append("(");
		for (String arg : this.argsList) {
			sb.append(arg).append(", ");
		}
		sb.setLength(sb.length() - 2); // removes the comma
		sb.append(")");
		
		return sb.toString();
	}

	public void setSignature(String signature) {

		signature = signature.trim();
		if (signature.startsWith(FUNCTION_KEYWORD)) {
			int startPosition = signature.indexOf(FUNCTION_KEYWORD) + FUNCTION_KEYWORD.length();
			signature = signature.substring(startPosition, signature.length());
		}
		if (signature.endsWith("{")) {
			signature = signature.substring(0, signature.length() - 1).trim();
		}
		
		this.setName(signature.substring(signature.indexOf(' '), signature.indexOf('(')).trim());
		this.setReturnType(signature.substring(0, signature.indexOf(' ')).trim());

		String[] argsArr = signature.substring(signature.indexOf('(') + 1, signature.indexOf(')')).trim().split(",");
		
		for (int i = 0; i < argsArr.length; i++) {
			this.argsList.add(argsArr[i].trim());	
		}
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public List<String> getArgsList() {
		return argsList;
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();
		if (this.comments != null && this.comments.length() > 0) {
			sb.append(this.getFormattedComments()).append("\n");
		}
		sb.append(FUNCTION_KEYWORD).append(" ").append(this.getSignature()).append(" {").append("\n");
		String[] bodyParts = this.body.split("\n");
		for (int i = 0; i < bodyParts.length; i++) {
			sb.append("\t").append(bodyParts[i]).append("\n");
		}
		sb.append("}").append("\n");

		return sb.toString();
	}

}
