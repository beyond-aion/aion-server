package zone;

import com.aionemu.gameserver.controllers.observer.AbstractQuestZoneObserver;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.handler.QuestZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;

/**
 * @author Rolandas
 */
@ZoneNameAnnotation(
	value = "LF1A_SENSORYAREA_Q1012_2_206005_4_210030000 LF1A_SENSORYAREA_Q1012_3_206006_6_210030000 LF1A_SENSORYAREA_Q1012_1_206004_8_210030000",
	questId = 1012)
public class _1012SensoryArea extends QuestZoneHandler {

	@Override
	public AbstractQuestZoneObserver createObserver(Player player, ZoneTemplate zoneTemplate) {
		return new AbstractQuestZoneObserver(player, zoneTemplate) {

			@Override
			public void onMoved(float distanceScouted, float distanceToCenter, int steps, long timeSpent) {
				// Another way to do the same is like this:
				//
				// QuestEngine.getInstance().onEnterZone(new QuestEnv(null, player, questId), observedZone.getName());
				//
				// In this case, it will be handled inside the quest script. But you need to register
				// each zone in the quest script. For scouting you may save distance, time and send the event back
				// to the QuestEngine when the criteria are met. It will work like a delayed onEnterZone event.
				final QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs == null || qs.getQuestVars().getQuestVars() != 1)
					return;
				qs.setQuestVarById(0, 2); // 2
				PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
			}
		};
	}

}
