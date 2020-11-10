package com.aionemu.chatserver.service;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.ShutdownHook;
import com.aionemu.chatserver.configs.main.CSConfig;

/**
 * @author nrg
 */
public class RestartService {

	private static final Logger log = LoggerFactory.getLogger(RestartService.class);
	private static final RestartService instance = new RestartService();

	private RestartService() {
		if (CSConfig.CHATSERVER_RESTART_FREQUENCY != null)
			setTimer();
	}

	public static RestartService getInstance() {
		return instance;
	}

	private void setTimer() {
		// get time to restart
		String[] time = getRestartTime();
		int hour = Integer.parseInt(time[0]);
		int minute = Integer.parseInt(time[1]);

		// calculate the correct time based on frequency
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		boolean isMissed = calendar.getTimeInMillis() < System.currentTimeMillis();

		// switch frequency
		switch (CSConfig.CHATSERVER_RESTART_FREQUENCY) {
			case DAILY:
				if (isMissed) // execute next day if we missed the time today (what is mostly the case)
					calendar.add(Calendar.DAY_OF_YEAR, 1);
				break;
			case WEEKLY:
				calendar.add(Calendar.WEEK_OF_YEAR, 1);
				break;
			case MONTHLY:
				calendar.add(Calendar.MONTH, 1);
		}

		// Restart timer
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				log.info("Restart task is triggered - restarting chatserver!");
				ShutdownHook shutdownHook = ShutdownHook.getInstance();
				shutdownHook.setRestartOnly(true);
				shutdownHook.start();
			}
		}, calendar.getTime());
		log.info("Scheduled next restart for {}", calendar.getTime().toString());
	}

	private String[] getRestartTime() {
		String[] time;
		if ((time = CSConfig.CHATSERVER_RESTART_TIME.split(":")).length != 2) {
			log.warn("You did not state a valid restart time. Using 5:00 AM as default value!");
			return new String[] { "5", "0" };
		}
		return time;
	}
}
