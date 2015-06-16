package com.aionl.slf4j.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;


/**
 * @author zhkchi
 *
 */
public class ChatLogFilter extends Filter<ILoggingEvent> {

	/**
	 * Decides what to do with logging event.<br>
	 * This method accepts only log events that contain exceptions.
	 * 
	 * @param loggingEvent
	 *          log event that is going to be filtred.
	 * @return {@link org.apache.log4j.spi.Filter#ACCEPT} if chatlog, {@link org.apache.log4j.spi.Filter#DENY} otherwise
	 */
	@Override
	public FilterReply decide(ILoggingEvent loggingEvent) {
		Object message = loggingEvent.getMessage();

		if (((String) message).startsWith("[MESSAGE]")) {
			return FilterReply.ACCEPT;
		}

		return FilterReply.DENY;
	}
}
