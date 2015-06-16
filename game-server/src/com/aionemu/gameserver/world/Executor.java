package com.aionemu.gameserver.world;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xavier
 */
public abstract class Executor<T extends AionObject> {

	private static final Logger log = LoggerFactory.getLogger(Executor.class);

	public abstract boolean run(T object);

	private final void runImpl(Collection<T> objects) {
		try {
			for (T o : objects) {
				if (o != null) {
					if (!Executor.this.run(o))
						break;
				}
			}
		}
		catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public final void execute(final Collection<T> objects, boolean now) {
		if (now) {
			runImpl(objects);
		}
		else {
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					runImpl(objects);
				}
			});
		}
	}

	public final void execute(final Collection<T> objects) {
		execute(objects, false);
	}
}
