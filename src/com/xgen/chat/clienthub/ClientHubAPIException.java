package com.xgen.chat.clienthub;

@SuppressWarnings("serial")
public class ClientHubAPIException extends Exception {
	public ClientHubAPIException(){ }
	public ClientHubAPIException(String message) { super(message); }
	public ClientHubAPIException(Throwable cause) { super(cause); }
}
