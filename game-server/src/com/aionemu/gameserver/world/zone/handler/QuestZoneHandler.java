package com.aionemu.gameserver.world.zone.handler;

import java.lang.annotation.IncompleteAnnotationException;

import com.aionemu.gameserver.controllers.observer.AbstractQuestZoneObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.world.zone.ZoneInstance;

import javolution.util.FastMap;

/**
 * @author Rolandas
 */
public abstract class QuestZoneHandler extends GeneralZoneHandler {

	FastMap<Integer, AbstractQuestZoneObserver> observed = new FastMap<Integer, AbstractQuestZoneObserver>();
	protected int questId;

	public QuestZoneHandler() {
		ZoneNameAnnotation annotation = getClass().getAnnotation(ZoneNameAnnotation.class);
		if (annotation == null || annotation.questId() == 0 || DataManager.QUEST_DATA.getQuestById(annotation.questId()) == null)
			throw new IncompleteAnnotationException(ZoneNameAnnotation.class, "questId");
		questId = annotation.questId();
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (!(creature instanceof Player))
			return;
		AbstractQuestZoneObserver observer = createObserver((Player) creature, zone.getZoneTemplate());
		creature.getObserveController().addObserver(observer);
		observed.put(creature.getObjectId(), observer);
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		if (!(creature instanceof Player))
			return;
		AbstractQuestZoneObserver observer = observed.get(creature.getObjectId());
		if (observer != null) {
			creature.getObserveController().removeObserver(observer);
			observed.remove(creature.getObjectId());
		}
	}

	public abstract AbstractQuestZoneObserver createObserver(Player player, ZoneTemplate zoneTemplate);

}
