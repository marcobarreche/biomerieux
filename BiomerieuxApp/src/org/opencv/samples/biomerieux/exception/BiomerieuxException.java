package org.opencv.samples.biomerieux.exception;

@SuppressWarnings("serial")
public class BiomerieuxException extends Exception {

	public BiomerieuxException() {
		super();
	}

	public BiomerieuxException(String detailMessage) {
		super(detailMessage);
	}

	public BiomerieuxException(Throwable throwable) {
		super(throwable);
		
	}

	public BiomerieuxException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
