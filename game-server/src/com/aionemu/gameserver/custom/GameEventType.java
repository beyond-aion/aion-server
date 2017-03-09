package com.aionemu.gameserver.custom;

import java.util.LinkedList;
import java.util.List;

import com.aionemu.gameserver.custom.nochsanapvp.NochsanaEvent;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Woge
 */

public enum GameEventType {

	EVENT_1(NochsanaEvent.class, 6, "Battle for Nochsana");

	private final Class<? extends GameEvent> eventClazz;
	private final int playersPerRace;
	private final String eventName;
	private List<GameEvent> activeEvents = new LinkedList<>();
	private Race priorityRace;

	private GameEventType(Class<? extends GameEvent> eventClazz, int playersPerRace, String eventName) {
		this.eventClazz = eventClazz;
		this.playersPerRace = playersPerRace;
		this.eventName = eventName;
	}

	public int getMaxPlayerPerRace() {
		return playersPerRace;
	}

	public String getViewableName() {
		return eventName;
	}

	public String getID() {
		String[] split = this.name().split("_");
		return split[1];
	}

	public Race getComparableRace() {
		return this.priorityRace;
	}

	public List<GameEvent> getActiveSubEvents() {
		return activeEvents;
	}

	public synchronized GameEvent getGameEventByPriority(Player player) {

		if (activeEvents.size() == 0) {
			return null;
		}

		priorityRace = player.getRace();
		activeEvents.sort(null);
		return activeEvents.get(activeEvents.size() - 1);
	}

	public synchronized void unsetActiveEvent(GameEvent event) {
		activeEvents.remove(event);
	}

	public GameEvent createGameEvent() {
		GameEvent event = null;

		try {
			event = eventClazz.newInstance();
			event.setGameType(this);
			activeEvents.add(event);
		} catch (Exception e) {
			BattleService.getInstance().logError("Could not create new class instance of " + (eventClazz != null ? eventClazz.getName() : "null"), e);
		}
		return event;
	}

	public Class<? extends GameEvent> getGameEvent() {
		return eventClazz;
	}

}
