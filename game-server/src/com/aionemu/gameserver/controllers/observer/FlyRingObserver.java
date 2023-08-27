package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author xavier, Source
 */
public class FlyRingObserver extends ActionObserver {

	private final Player player;
	private final FlyRing ring;
	private Vector3f oldPosition;

	public FlyRingObserver(FlyRing ring, Player player) {
		super(ObserverType.MOVE);
		this.player = player;
		this.ring = ring;
		this.oldPosition = new Vector3f(player.getX(), player.getY(), player.getZ());
	}

	@Override
	public void moved() {
		Vector3f newPosition = new Vector3f(player.getX(), player.getY(), player.getZ());
		if (ring.isCrossed(oldPosition, newPosition)) {
			if (ring.getTemplate().getMap() == 400010000 || isQuestactive() || isInstancetactive()) {
				SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(265); // Wings of Aether
				Effect speedUp = new Effect(player, player, skillTemplate, skillTemplate.getLvl());
				speedUp.initialize();
				speedUp.addAllEffectToSucess();
				speedUp.applyEffect();
			}
			QuestEngine.getInstance().onPassFlyingRing(new QuestEnv(null, player, 0), ring.getName());
		}
		oldPosition = newPosition;
	}

	private boolean isInstancetactive() {
		return ring.getPosition().getWorldMapInstance().getInstanceHandler().onPassFlyingRing(player, ring.getName());
	}

	private boolean isQuestactive() {
		int questId = player.getRace() == Race.ASMODIANS ? 2042 : 1044;
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null)
			return false;

		return qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) >= 2 && qs.getQuestVarById(0) <= 8;
	}

}
