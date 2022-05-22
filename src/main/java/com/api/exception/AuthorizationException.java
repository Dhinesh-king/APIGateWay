package com.api.exception;

public class AuthorizationException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AuthorizationException() {
		super("The Token is Invalid or the user have no authority to perform this action.");
	}
	
}
