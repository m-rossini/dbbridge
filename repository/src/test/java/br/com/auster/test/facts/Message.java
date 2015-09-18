package br.com.auster.test.facts;

import java.util.ArrayList;

public class Message {
	public static final int HELLO = 0;

	public static final int GOODBYE = 1;

	private String message;
	
	private ArrayList list;

	private int status;

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ArrayList getList() {
		return list;
	}

	public void setList(ArrayList list) {
		this.list = list;
	}
}
