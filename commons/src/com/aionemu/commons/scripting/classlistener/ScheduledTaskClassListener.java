package com.aionemu.commons.scripting.classlistener;

import java.lang.reflect.Modifier;
import java.util.List;

import org.quartz.JobDetail;

import com.aionemu.commons.scripting.metadata.Scheduled;
import com.aionemu.commons.services.CronService;

public class ScheduledTaskClassListener implements ClassListener {

	@Override
	@SuppressWarnings({ "unchecked" })
	public void postLoad(Class<?>[] classes) {
		for (Class<?> clazz : classes) {
			if (isValidClass(clazz)) {
				scheduleClass((Class<? extends Runnable>) clazz);
			}
		}
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void preUnload(Class<?>[] classes) {
		for (Class<?> clazz : classes) {
			if (isValidClass(clazz)) {
				unScheduleClass((Class<? extends Runnable>) clazz);
			}
		}
	}

	public boolean isValidClass(Class<?> clazz) {

		if (!Runnable.class.isAssignableFrom(clazz)) {
			return false;
		}

		final int modifiers = clazz.getModifiers();

		if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
			return false;

		if (!Modifier.isPublic(modifiers))
			return false;

		if (!clazz.isAnnotationPresent(Scheduled.class)) {
			return false;
		}

		Scheduled scheduled = clazz.getAnnotation(Scheduled.class);
		if (scheduled.disabled()) {
			return false;
		}

		if (scheduled.value().length == 0) {
			return false;
		}

		return true;
	}

	protected void scheduleClass(Class<? extends Runnable> clazz) {
		Scheduled metadata = clazz.getAnnotation(Scheduled.class);

		try {
			if (metadata.instancePerCronExpression()) {
				for (String s : metadata.value()) {
					getCronService().schedule(clazz.getDeclaredConstructor().newInstance(), s, metadata.longRunningTask());
				}
			} else {
				Runnable r = clazz.getDeclaredConstructor().newInstance();
				for (String s : metadata.value()) {
					getCronService().schedule(r, s, metadata.longRunningTask());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to schedule runnable " + clazz.getName(), e);
		}
	}

	protected void unScheduleClass(Class<? extends Runnable> clazz) {
		List<JobDetail> jobs = getCronService().findJobs(clazz, false);
		for (JobDetail job : jobs)
			getCronService().cancel(job);
	}

	protected CronService getCronService() {
		if (CronService.getInstance() == null) {
			throw new RuntimeException("CronService is not initialized");
		}

		return CronService.getInstance();
	}
}
