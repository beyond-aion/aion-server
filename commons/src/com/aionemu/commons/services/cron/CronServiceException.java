package com.aionemu.commons.services.cron;

public class CronServiceException extends RuntimeException {

	private static final long serialVersionUID = -354186843536711803L;

	public CronServiceException() {
	}

	public CronServiceException(String message) {
		super(message);
	}

	public CronServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public CronServiceException(Throwable cause) {
		super(cause);
	}
}
