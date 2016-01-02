package com.aionemu.gameserver.controllers.observer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
public abstract class AbstractQuestZoneObserver extends ActionObserver {

	protected final Player player;
	protected final Vector3f startPos;
	protected final long startTime;
	protected final ZoneTemplate observedZone;
	protected Vector3f oldPos;
	protected int stepCount;
	private AtomicBoolean isRunning = new AtomicBoolean();

	public AbstractQuestZoneObserver(Player player, ZoneTemplate zoneTemplate) {
		super(ObserverType.ALL);
		this.player = player;
		this.startPos = new Vector3f(player.getX(), player.getY(), player.getZ());
		this.oldPos = startPos.clone();
		this.startTime = System.currentTimeMillis();
		this.observedZone = zoneTemplate;
	}

	@Override
	public void moved() {
		if (!isRunning.getAndSet(true)) {
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					try {
						Vector3f currentPos = new Vector3f(player.getX(), player.getY(), player.getZ());
						Vector3f center = new Vector3f(observedZone.getSphere().getX(), observedZone.getSphere().getY(), observedZone.getSphere().getZ());
						float distance = startPos.distance(currentPos);
						float distanceFromCenter = center.distance(currentPos);
						if (oldPos.distance(currentPos) > 1) {
							stepCount++;
							oldPos = currentPos;
						}
						onMoved(distance, distanceFromCenter, stepCount, System.currentTimeMillis() - startTime);
					} finally {
						isRunning.set(false);
					}
				}
			});
		}
	}

	public abstract void onMoved(float distanceScouted, float distanceToCenter, int steps, long timeSpent);
}
