package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author xavier, Source
 */
public class FlyRingObserver extends ActionObserver {

	private Player player;

	private FlyRing ring;

	private Point3D oldPosition;

	SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(265); // Wings of Aether

	public FlyRingObserver() {
		super(ObserverType.MOVE);
		this.player = null;
		this.ring = null;
		this.oldPosition = null;
	}

	public FlyRingObserver(FlyRing ring, Player player) {
		super(ObserverType.MOVE);
		this.player = player;
		this.ring = ring;
		this.oldPosition = new Point3D(player.getX(), player.getY(), player.getZ());
	}

	@Override
	public void moved() {
		Point3D newPosition = new Point3D(player.getX(), player.getY(), player.getZ());
		boolean passedThrough = false;

		if (ring.getPlane().intersect(oldPosition, newPosition)) {
			Point3D intersectionPoint = ring.getPlane().intersection(oldPosition, newPosition);
			if (intersectionPoint != null) {
				double distance = Math.abs(ring.getPlane().getCenter().distance(intersectionPoint));

				if (distance < ring.getTemplate().getRadius()) {
					passedThrough = true;
				}
			} else {
				if (PositionUtil.isInRange(ring, player, ring.getTemplate().getRadius())) {
					passedThrough = true;
				}
			}
		}

		if (passedThrough) {
			if (ring.getTemplate().getMap() == 400010000 || isQuestactive() || isInstancetactive()) {
				Effect speedUp = new Effect(player, player, skillTemplate, skillTemplate.getLvl(), 0);
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
